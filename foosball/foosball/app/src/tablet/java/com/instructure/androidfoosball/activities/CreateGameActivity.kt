package com.instructure.androidfoosball.activities

import android.graphics.Color
import android.media.AudioManager
import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import com.afollestad.materialdialogs.MaterialDialog
import com.google.firebase.database.*
import com.instructure.androidfoosball.App
import com.instructure.androidfoosball.R
import com.instructure.androidfoosball.ktmodels.*
import com.instructure.androidfoosball.adapters.UserAdapter
import com.instructure.androidfoosball.utils.copyToRealmOrUpdate
import com.instructure.androidfoosball.utils.edit
import com.instructure.androidfoosball.utils.mCommentator
import com.instructure.androidfoosball.utils.setVisible
import com.instructure.androidfoosball.views.ConfirmPinDialog
import com.instructure.androidfoosball.views.TeamLayout
import kotlinx.android.synthetic.tablet.activity_create_game.*
import org.jetbrains.anko.onClick
import org.jetbrains.anko.startActivity

class CreateGameActivity : AppCompatActivity() {

    private val mTable = Table.getSelectedTable()
    private val mIncomingNfcRef = FirebaseDatabase.getInstance().reference.child("incoming").child(mTable.id)

    private val BEST_OF_DEFAULT = 3
    private val POINTS_DEFAULT = 5

    private var bestOf = 0
        set(value) {
            field = value
            bestOfButton.text = value.toString()
        }

    private var points = 0
        set(value) {
            field = value
            pointButton.text = value.toString()
        }

    private val nfcListener = object : ValueEventListener {
        override fun onDataChange(dataSnapshot: DataSnapshot) {
            val nfc = dataSnapshot.getValue(NfcAssignment::class.java)
            if (nfc.sideOne.isBlank() && nfc.sideTwo.isBlank()) return
            fun getUserById(userId: String): User? = App.realm.where(User::class.java).equalTo("id", userId).findFirst()
            when {
                nfc.sideOne.isNotBlank() -> getUserById(nfc.sideOne)?.let { addUser(it, teamOneLayout) }
                nfc.sideTwo.isNotBlank() -> getUserById(nfc.sideTwo)?.let { addUser(it, teamTwoLayout) }
            }
            mIncomingNfcRef.setValue(NfcAssignment())
        }

        override fun onCancelled(databaseError: DatabaseError) { }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_create_game)
        volumeControlStream = AudioManager.STREAM_MUSIC
        setupViews()
        mIncomingNfcRef.addValueEventListener(nfcListener)
    }

    private fun setupViews() {
        // Set initial team names
        teamOneNameView.setText(mTable.sideOneName)
        teamTwoNameView.setText(mTable.sideTwoName)

        // Set team colors
        teamOneLayout.setTeamColor(Color.parseColor(mTable.sideOneColor))
        teamTwoLayout.setTeamColor(Color.parseColor(mTable.sideTwoColor))

        // Player selection listeners
        teamOneLayout.onAddPlayerClicked = { selectPlayer(teamOneLayout) }
        teamTwoLayout.onAddPlayerClicked = { selectPlayer(teamTwoLayout) }

        // Team changed listeners
        teamOneLayout.onTeamChanged = { onTeamChanged(TableSide.SIDE_1) }
        teamTwoLayout.onTeamChanged = { onTeamChanged(TableSide.SIDE_2) }

        // Best of selection
        bestOf = BEST_OF_DEFAULT
        bestOfButton.onClick {
            val options = (1..9 step 2).toList()
            MaterialDialog.Builder(this)
                    .items(options)
                    .itemsCallback { materialDialog, view, i, charSequence -> bestOf = options[i] }
                    .show()
        }

        // Points selection
        points = POINTS_DEFAULT
        pointButton.onClick {
            val options = (3..15).toList()
            MaterialDialog.Builder(this)
                    .items(options)
                    .itemsCallback { materialDialog, view, i, charSequence -> points = options[i] }
                    .show()
        }

        // Start game
        startGameButton.onClick {
            if (teamOneLayout.team.users.size != teamTwoLayout.team.users.size) {
                MaterialDialog.Builder(this)
                        .title(R.string.uneven_teams)
                        .content(R.string.uneven_teams_content)
                        .positiveText(android.R.string.yes)
                        .onPositive { materialDialog, dialogAction -> createGame() }
                        .negativeText(android.R.string.no)
                        .show()
            } else {
                createGame()
            }
        }
    }

    private fun onTeamChanged(side: TableSide) {
        // Update average team win rates
        winRateTeamOne.text = if (teamOneLayout.hasUsers()) getString(R.string.avg_win_rate_formatted).format(teamOneLayout.team.getAverageWinRate()) else ""
        winRateTeamTwo.text = if (teamTwoLayout.hasUsers()) getString(R.string.avg_win_rate_formatted).format(teamTwoLayout.team.getAverageWinRate()) else ""

        if (side == TableSide.SIDE_1) {
            // Update team one custom name
            val customNameTeamOne = App.realm.where(CustomTeamName::class.java).equalTo("teamHash", teamOneLayout.team.getTeamHash()).findFirst()
            teamOneNameView.setText(if (customNameTeamOne != null) customNameTeamOne.name else mTable.sideOneName)
        } else {
            // Update team two custom name
            val customNameTeamTwo = App.realm.where(CustomTeamName::class.java).equalTo("teamHash", teamTwoLayout.team.getTeamHash()).findFirst()
            teamTwoNameView.setText(if (customNameTeamTwo != null) customNameTeamTwo.name else mTable.sideTwoName)
        }


        // Show start button if ready
        val ready = teamOneLayout.hasUsers() && teamTwoLayout.hasUsers()
        assignTeamsView.setVisible(!ready)
        startGameButton.setVisible(ready)
    }

    private fun selectPlayer(teamLayout: TeamLayout) {
        val users: List<User> = App.realm.where(User::class.java).findAllSorted("name").toList()
        MaterialDialog.Builder(this)
                .title(R.string.pick_a_user)
                .adapter(UserAdapter(this, users)) { dialog, itemView, which, text ->
                    dialog.dismiss()
                    ConfirmPinDialog(this, users[which]) { user ->
                        addUser(user, teamLayout)
                    }.show()
                }.show()
    }

    private fun addUser(user: User, teamLayout: TeamLayout) {
        (if (teamLayout == teamOneLayout) teamTwoLayout else teamOneLayout).removeUser(user)
        if (teamLayout.addUser(user)) {
            mCommentator.announcePlayerAssignment(
                    user,
                    if (teamLayout == teamOneLayout) mTable.sideOneName else mTable.sideTwoName
            )
        }
    }

    private fun createGame() {

        // Set/update team one custom name
        val teamOne = teamOneLayout.team
        teamOneNameView.text.toString().apply {
            if (isNotBlank() && this != mTable.sideOneName) {
                teamOne.customName = this
                CustomTeamName(teamOne.getTeamHash(), this).edit { it.copyToRealmOrUpdate(this) }
            }
        }

        // Set/update team two custom name
        val teamTwo = teamTwoLayout.team
        teamTwoNameView.text.toString().apply {
            if (isNotBlank() && this != mTable.sideTwoName) {
                teamTwo.customName = this
                CustomTeamName(teamTwo.getTeamHash(), this).edit { it.copyToRealmOrUpdate(this) }
            }
        }

        // Create first round
        val round = Round(
                pointsToWin = points,
                sideOneTeam = teamOne,
                sideTwoTeam = teamTwo,
                startTime = System.currentTimeMillis()
        )

        // Create and save game
        val game = Game()
        game.bestOf = bestOf
        game.rounds.add(round)
        game.startTime = round.startTime
        game.status = GameStatus.ONGOING.name
        game.teamOne = teamOne
        game.teamTwo = teamTwo
        game.copyToRealmOrUpdate()

        startActivity<GameActivity>(GameActivity.EXTRA_GAME_ID to game.id)
        finish()

    }

    override fun onDestroy() {
        super.onDestroy()
        mIncomingNfcRef.removeEventListener(nfcListener)
    }
}
