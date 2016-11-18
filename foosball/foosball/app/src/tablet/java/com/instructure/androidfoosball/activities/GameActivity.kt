package com.instructure.androidfoosball.activities

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.graphics.Color
import android.media.AudioManager
import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.view.View
import android.widget.ImageView
import android.widget.LinearLayout
import com.afollestad.materialdialogs.MaterialDialog
import com.google.firebase.database.*
import com.instructure.androidfoosball.App
import com.instructure.androidfoosball.R
import com.instructure.androidfoosball.ktmodels.*
import com.instructure.androidfoosball.ktmodels.CustomTeam
import com.instructure.androidfoosball.push.PushIntentService
import com.instructure.androidfoosball.receivers.GoalReceiver
import com.instructure.androidfoosball.utils.*
import com.instructure.androidfoosball.views.WinGameDialog
import com.instructure.androidfoosball.views.WinRoundDialog
import kotlinx.android.synthetic.tablet.activity_game.*
import org.jetbrains.anko.onClick
import org.jetbrains.anko.textColor
import java.util.*


class GameActivity : AppCompatActivity() {

    companion object {
        val EXTRA_GAME_ID = "gameId"
    }

    val mGameId by lazy { intent.getStringExtra(EXTRA_GAME_ID) ?: "" }
    val mGame by lazy { App.realm.where(Game::class.java).equalTo("id", mGameId).findFirst()!! }
    val mDatabase: DatabaseReference = FirebaseDatabase.getInstance().reference
    val mTable = Table.getSelectedTable()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_game)
        volumeControlStream = AudioManager.STREAM_MUSIC
        setupViews()
        setupRound()
        GoalReceiver.register(this, goalReceiver, 100)
        updateGameStatusBusy()
    }

    private fun setupViews() {

        // Get table colors
        val sideOneColor = Color.parseColor(mTable.sideOneColor)
        val sideTwoColor = Color.parseColor(mTable.sideTwoColor)

        // Set team name colors
        teamOneName.textColor = sideOneColor
        teamTwoName.textColor = sideTwoColor

        // Set TeamLayout colors
        teamOneLayout.setTeamColor(sideOneColor)
        teamTwoLayout.setTeamColor(sideTwoColor)

        // Pause game
        pauseGameButton.onClick {
            shortToast(R.string.game_paused)
            updateGameStatusFree()
            finish()
        }

        // Quit game
        quitGameButton.onClick {
            MaterialDialog.Builder(this)
                    .title(R.string.quit_game)
                    .content(R.string.confirm_quit_game)
                    .negativeText(android.R.string.cancel)
                    .positiveText(R.string.quit_game)
                    .onPositive { materialDialog, dialogAction ->
                        mGame.edit { status = GameStatus.CANCELED.name }
                        updateGameStatusFree()
                        finish()
                    }
                    .show()
        }

        // Undo last goal
        undoView.onClick { undoGoal() }

        // Tap team to count a goal
        teamOneLayout.onClick { goalReceiver.onGoal(TableSide.SIDE_1) }
        teamTwoLayout.onClick { goalReceiver.onGoal(TableSide.SIDE_2) }

    }

    private fun undoGoal() {
        if (mGame.currentRound().goalHistory.isNotEmpty()) {
            val shameTeam = mGame.currentRound().goalHistory.last().team
            val opposingTeam = if (shameTeam == mGame.teamOne) mGame.teamTwo else mGame.teamOne
            mCommentator.announceUndoGoal(shameTeam, opposingTeam, mGame.currentRound(), mTable)
            mGame.currentRound().goalHistory.edit { if (isNotEmpty()) remove(last()) }
            refreshScore()
        }
    }

    private fun nextRound(incrementBestOf: Boolean = false) {

        val thisRound = mGame.currentRound()

        // Update player stats
        updateTeamStats(thisRound.getWinningTeam()!!.users, thisRound.getLosingTeam()!!.users)

        // Create next round
        val nextRound = Round(
                pointsToWin = thisRound.pointsToWin,
                sideOneTeam = thisRound.sideTwoTeam,
                sideTwoTeam = thisRound.sideOneTeam,
                startTime = System.currentTimeMillis()
        )

        // Save changes to Realm
        App.realm.inTransaction {
            if (incrementBestOf) mGame.bestOf += 2
            thisRound.endTime = System.currentTimeMillis()
            mGame.rounds.add(nextRound)
        }

        // Update UI with new round
        setupRound()
    }

    private fun setupRound() {
        val round = mGame.currentRound()

        teamOneName.text = round.sideOneTeam.customName.elseIfBlank(mTable.sideOneName)
        teamTwoName.text = round.sideTwoTeam.customName.elseIfBlank(mTable.sideTwoName)

        teamOneLayout.team = round.sideOneTeam
        teamTwoLayout.team = round.sideTwoTeam

        roundNumberView.text = mGame.rounds.size.toString()
        maxRoundsView.text = mGame.bestOf.toString()
        pointsToWinView.text = round.pointsToWin.toString()

        setWinStars(teamOneStarsContainer, mGame.getTeamWinCount(round.sideOneTeam))
        setWinStars(teamTwoStarsContainer, mGame.getTeamWinCount(round.sideTwoTeam))

        roundTimerView.setStartTime(round.startTime)
        gameTimerView.setStartTime(mGame.startTime)

        refreshScore()
        mCommentator.announceGameStart()
    }

    private fun setWinStars(container: LinearLayout, count: Int) {
        container.removeAllViews()
        val dimen = 32f.dp().toInt()
        val params = LinearLayout.LayoutParams(dimen, dimen)
        kotlin.repeat(count) {
            val v = ImageView(this)
            v.setImageResource(R.drawable.ic_star_amber_a400_48dp)
            container.addView(v, params)
        }
    }

    private val goalReceiver: GoalReceiver = GoalReceiver { side ->
        mGame.currentRound().recordGoal(side)
        mCommentator.announceGoal(side, mGame.currentRound(), mTable)
        refreshScore()
    }

    private fun refreshScore() {
        val round = mGame.currentRound()

        // Get scores
        val scoreTeamOne = round.getScore(round.sideOneTeam)
        val scoreTeamTwo = round.getScore(round.sideTwoTeam)

        // Update score views
        teamOneScore.text = scoreTeamOne.toString()
        teamTwoScore.text = scoreTeamTwo.toString()

        if (scoreTeamOne + scoreTeamTwo > 0) {
            undoView.visibility = View.VISIBLE
            goalTimerView.setStartTime(round.goalHistory.last().time)
        } else {
            undoView.visibility = View.GONE
            goalTimerView.setStartTime(round.startTime)
        }

        val servingSide = mGame.getServingSide()
        teamOneServingIndicator.visibility = if (servingSide == TableSide.SIDE_1) View.VISIBLE else View.GONE
        teamTwoServingIndicator.visibility = if (servingSide == TableSide.SIDE_2) View.VISIBLE else View.GONE

        if (mGame.hasWinner()) {
            mGame.getWinningTeam()!!.users.map { it.customVictoryPhrase }.filter { it.isNotBlank() }.apply {
                if (isNotEmpty()) mCommentator.queueAnnounce(joinToString(". "))
            }
            WinGameDialog(this, mGame, { nextRound(true) }, { undoGoal() }, { endGame() }).show()
        } else if (round.hasWinner()) {
            WinRoundDialog(this, mGame, { nextRound() }, { undoGoal() }).show()
        }

        setGameRoundStats(mGame)
    }

    private fun endGame() {
        updateTeamStats(mGame.currentRound().getWinningTeam()!!.users, mGame.currentRound().getLosingTeam()!!.users)
        mGame.edit { status = GameStatus.FINISHED.name }
        updateGameStatusFree()

        finish()
    }

    override fun onStart() {
        super.onStart()
        val am = this.getSystemService(Context.ALARM_SERVICE) as AlarmManager
        val pi = PendingIntent.getService(this, 0, PushIntentService.getIntent(this, mTable.pushId, mTable.name), PendingIntent.FLAG_CANCEL_CURRENT)
        am.cancel(pi)
    }

    override fun onStop() {
        super.onStop()
        val am = this.getSystemService(Context.ALARM_SERVICE) as AlarmManager
        val pi = PendingIntent.getService(this, 0, PushIntentService.getIntent(this, mTable.pushId, mTable.name), PendingIntent.FLAG_CANCEL_CURRENT)
        am.set(AlarmManager.RTC_WAKEUP, 90000, pi)
    }

    override fun onDestroy() {
        super.onDestroy()
        GoalReceiver.unregister(this, goalReceiver)
    }

    private fun updateTeamStats(winningTeam: List<User>, losingTeam: List<User>) {
        mDatabase.child("users").addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                // Get fresh users
                val users = dataSnapshot.children.map { it.getValue(User::class.java).apply { id = it.key } }

                // Grab fresh winners and losers
                val winners = users.filter { user -> winningTeam.any { it.id == user.id } }
                val losers = users.filter { user -> losingTeam.any { it.id == user.id } }

                // Update FoosRanking
                RankingUtils.updateFoosRankings(winners, losers)

                // Increment wins
                winners.forEach { it.wins++ }
                losers.forEach { it.losses++ }

                // Post changes to firebase
                (winners + losers).forEach { updatePlayerStats(it) }
            }

            override fun onCancelled(databaseError: DatabaseError) { }
        })

        //don't worry about teams with guests
        (winningTeam + losingTeam).forEach { if(it.guest) return }

        //create winning team object
        val mWinningTeam = CustomTeam()
        val winningUsers = ArrayList<String>()
        winningUsers.add(winningTeam[0].id)
        winningUsers.add(winningTeam[1].id)
        mWinningTeam.users = winningUsers

        val customNameTeamOne = App.realm.where(CustomTeamName::class.java).equalTo("teamHash", mWinningTeam.getTeamHash()).findFirst()
        mWinningTeam.teamName = if (customNameTeamOne != null) customNameTeamOne.name else ""
        mWinningTeam.id = mWinningTeam.getTeamHash()

        //create losing team object
        val mLosingTeam = CustomTeam()
        val losingUsers = ArrayList<String>()
        losingUsers.add(losingTeam[0].id)
        losingUsers.add(losingTeam[1].id)

        mLosingTeam.users = losingUsers
        val losingTeamName = App.realm.where(CustomTeamName::class.java).equalTo("teamHash", mLosingTeam.getTeamHash()).findFirst()
        mLosingTeam.teamName = if (losingTeamName != null) losingTeamName.name else ""
        mLosingTeam.id = mLosingTeam.getTeamHash()


        //now update the team database in firebase
        mDatabase.child("customTeams").addListenerForSingleValueEvent(object : ValueEventListener {

            override fun onDataChange(dataSnapshot: DataSnapshot) {
                // check if the winning team exists
                if (dataSnapshot.hasChild(mWinningTeam.getTeamHash())) {
                    // update values

                    val customTeam = dataSnapshot.child(mWinningTeam.getTeamHash()).getValue(CustomTeam::class.java)
                    customTeam.teamWins++
                    customTeam.teamName = mWinningTeam.teamName
                    updateTeamStats(customTeam)

                } else {
                    //add the team
                    mWinningTeam.teamWins++
                    addTeam(mWinningTeam)
                }

                // check if the losing team exists
                if (dataSnapshot.hasChild(mLosingTeam.getTeamHash())) {
                    // update values
                    val customTeam = dataSnapshot.child(mLosingTeam.getTeamHash()).getValue(CustomTeam::class.java)
                    customTeam.teamLosses++
                    customTeam.teamName = mLosingTeam.teamName
                    updateTeamStats(customTeam)

                } else {
                    //add the team
                    mLosingTeam.teamLosses++
                    addTeam(mLosingTeam)
                }
            }
            override fun onCancelled(databaseError: DatabaseError) { }
        })

    }

    private fun addTeam(team: CustomTeam) {
        mDatabase.child("customTeams").child(team.getTeamHash()).setValue(team)
    }

    private fun updateTeamStats(team: CustomTeam) {
        mDatabase.child("customTeams").child(team.id).apply {
            child("teamName").setValue(team.teamName)
            child("teamWins").setValue(team.teamWins)
            child("teamLosses").setValue(team.teamLosses)
        }
    }

    private fun updatePlayerStats(player: User) {
        // Do not update guest users
        if (player.guest) return

        mDatabase.child("users").child(player.id).apply {
            child("wins").setValue(player.wins)
            child("losses").setValue(player.losses)
            child("foosRanking").setValue(player.foosRanking)
            child("rankedGamesPlayed").setValue(player.rankedGamesPlayed)
            child("foosRankMap").setValue(player.foosRankMap)
        }
    }

    private fun updateGameStatusBusy() {
        mDatabase.child("tables")
                .child(mTable.id)
                .child("currentGame").setValue("BUSY")
    }

    private fun updateGameStatusFree() {
        mDatabase.child("tables").child(mTable.id).apply {
            child("currentGame").setValue("FREE")
            child("currentScoreTeamOne").setValue("")
            child("currentScoreTeamTwo").setValue("")
            child("currentBestOf").setValue("")
            child("currentPointsToWin").setValue("")
            child("currentRound").setValue("")
            child("teamOne").setValue(null)
            child("teamTwo").setValue(null)
        }
    }

    private fun setGameRoundStats(game: Game) {
        val round = mGame.currentRound()

        // Get scores
        val scoreTeamOne = round.getScore(round.sideOneTeam)
        val scoreTeamTwo = round.getScore(round.sideTwoTeam)

        mDatabase.child("tables").child(mTable.id).apply {
            child("currentScoreTeamOne").setValue(scoreTeamOne.toString())
            child("currentScoreTeamTwo").setValue(scoreTeamTwo.toString())
            child("currentBestOf").setValue(game.bestOf.toString())
            child("currentPointsToWin").setValue(round.pointsToWin.toString())
            child("currentRound").setValue(game.rounds.size.toString())
            child("teamOne").setValue(game.teamOne)
            child("teamTwo").setValue(game.teamTwo)
        }
    }
}
