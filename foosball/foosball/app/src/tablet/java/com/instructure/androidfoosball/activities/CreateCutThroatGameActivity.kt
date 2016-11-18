package com.instructure.androidfoosball.activities

import android.media.AudioManager
import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import com.afollestad.materialdialogs.MaterialDialog
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.instructure.androidfoosball.App
import com.instructure.androidfoosball.R
import com.instructure.androidfoosball.adapters.UserAdapter
import com.instructure.androidfoosball.ktmodels.*
import com.instructure.androidfoosball.utils.copyToRealmOrUpdate
import com.instructure.androidfoosball.utils.elseIfBlank
import com.instructure.androidfoosball.utils.mCommentator
import com.instructure.androidfoosball.utils.setVisible
import io.realm.RealmList
import kotlinx.android.synthetic.tablet.activity_create_cut_throat_game.*
import org.jetbrains.anko.onClick
import org.jetbrains.anko.startActivity

class CreateCutThroatGameActivity : AppCompatActivity() {

    private val mTable = Table.getSelectedTable()
    private val mIncomingNfcRef = FirebaseDatabase.getInstance().reference.child("incoming").child(mTable.id)

    private val ROTATE_AFTER_DEFAULT = 2
    private val POINTS_DEFAULT = 5

    private var rotateAfter = 0
        set(value) {
            field = value
            rotateAfterButton.text = if (value == 0) String(Character.toChars(0x1F60E)) else value.toString()
        }

    private var points = 0
        set(value) {
            field = value
            pointButton.text = value.toString()
            if (rotateAfter > value) rotateAfter = value
        }

    private val nfcListener = object : ValueEventListener {
        override fun onDataChange(dataSnapshot: DataSnapshot) {
            val nfc = dataSnapshot.getValue(NfcAssignment::class.java)
            if (nfc.sideOne.isBlank() && nfc.sideTwo.isBlank()) return
            fun getUserById(userId: String): User? = App.realm.where(User::class.java).equalTo("id", userId).findFirst()
            when {
                nfc.sideOne.isNotBlank() -> getUserById(nfc.sideOne)?.let { addUser(it) }
                nfc.sideTwo.isNotBlank() -> getUserById(nfc.sideTwo)?.let { addUser(it) }
            }
            mIncomingNfcRef.setValue(NfcAssignment())
        }

        override fun onCancelled(databaseError: DatabaseError) { }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_create_cut_throat_game)
        volumeControlStream = AudioManager.STREAM_MUSIC
        setupViews()
        mIncomingNfcRef.addValueEventListener(nfcListener)
    }

    private fun setupViews() {
        // Player selection
        playersLayout.onAddPlayerClicked = { selectPlayer() }

        // On players changed
        playersLayout.onPlayersChanged = { onPlayersChanged() }

        // Points selection
        points = POINTS_DEFAULT
        pointButton.onClick {
            val options = (3..15).toList()
            MaterialDialog.Builder(this)
                    .items(options)
                    .itemsCallback { materialDialog, view, i, charSequence -> points = options[i] }
                    .show()
        }

        // Rotate after selection
        rotateAfter = ROTATE_AFTER_DEFAULT
        rotateAfterButton.onClick {
            val options = (0..points).toList()
            MaterialDialog.Builder(this)
                    .items(options.map { if (it == 0) "NEVER" else it.toString() })
                    .itemsCallback { materialDialog, view, i, charSequence -> rotateAfter = options[i] }
                    .show()
        }

        // Start game
        startGameButton.onClick { createGame() }
    }

    private fun onPlayersChanged() {
        // Show start button if ready
        val ready = playersLayout.players.size >= 3
        assignTeamsView.setVisible(!ready)
        startGameButton.setVisible(ready)
    }

    private fun selectPlayer() {
        val users: List<User> = App.realm.where(User::class.java).findAllSorted("name").toList()
        MaterialDialog.Builder(this)
                .title(R.string.pick_a_user)
                .adapter(UserAdapter(this, users)) { dialog, itemView, which, text ->
                    dialog.dismiss()
                    addUser(users[which])
                }.show()
    }

    private fun addUser(user: User) {
        playersLayout.addUser(user)
        mCommentator.announce(user.customAssignmentPhrase.elseIfBlank(user.name))
    }

    private fun createGame() {
        val game = CutThroatGame()
        game.status = GameStatus.ONGOING.name
        game.pointsToWin = points
        game.rotateAfter = rotateAfter
        game.startTime = System.currentTimeMillis()
        game.players = playersLayout.players.mapTo(RealmList()) { it }
        game.copyToRealmOrUpdate()

        startActivity<CutThroatGameActivity>(CutThroatGameActivity.EXTRA_GAME_ID to game.id)
        finish()
    }

    override fun onDestroy() {
        super.onDestroy()
        mIncomingNfcRef.removeEventListener(nfcListener)
    }
}
