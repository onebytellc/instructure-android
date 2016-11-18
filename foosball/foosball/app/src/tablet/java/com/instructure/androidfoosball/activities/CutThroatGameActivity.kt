package com.instructure.androidfoosball.activities

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.graphics.Color
import android.media.AudioManager
import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.view.View
import com.afollestad.materialdialogs.MaterialDialog
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.instructure.androidfoosball.App
import com.instructure.androidfoosball.R
import com.instructure.androidfoosball.ktmodels.CutThroatGame
import com.instructure.androidfoosball.ktmodels.GameStatus
import com.instructure.androidfoosball.ktmodels.Table
import com.instructure.androidfoosball.ktmodels.TableSide
import com.instructure.androidfoosball.push.PushIntentService
import com.instructure.androidfoosball.receivers.GoalReceiver
import com.instructure.androidfoosball.utils.*
import com.instructure.androidfoosball.views.WinCutThroatGameDialog
import kotlinx.android.synthetic.tablet.activity_game_cut_throat.*
import org.jetbrains.anko.onClick
import org.w3c.dom.Comment

class CutThroatGameActivity : AppCompatActivity() {

    companion object {
        val EXTRA_GAME_ID = "gameId"
    }

    val mGameId by lazy { intent.getStringExtra(EXTRA_GAME_ID) ?: "" }
    val mGame by lazy { App.realm.where(CutThroatGame::class.java).equalTo("id", mGameId).findFirst()!! }
    val mTable = Table.getSelectedTable()
    val mDatabase: DatabaseReference = FirebaseDatabase.getInstance().reference

    private val goalReceiver: GoalReceiver = GoalReceiver { side ->
        when (side) {
            TableSide.SIDE_1 -> rotate()
            TableSide.SIDE_2 -> goal()
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_game_cut_throat)
        volumeControlStream = AudioManager.STREAM_MUSIC
        setupViews()
        setupGame()
        GoalReceiver.register(this, goalReceiver, 100)
        updateGameStatusBusy()
    }

    private fun setupViews() {

        // Get table colors
        val sideOneColor = Color.parseColor(mTable.sideOneColor)
        val sideTwoColor = Color.parseColor(mTable.sideTwoColor)

        // Set TeamLayout colors
        doublesLayout.setBgColor(sideOneColor)
        singlesLayout.setBgColor(sideTwoColor)

        // Hide extras layout if there are only 3 players
        if (mGame.players.size == 3) extrasLayout.visibility = View.GONE

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

        // Tap team to count a goal
        doublesLayout.onClick { goalReceiver.onGoal(TableSide.SIDE_1) }
        singlesLayout.onClick { goalReceiver.onGoal(TableSide.SIDE_2) }

    }

    private fun setupGame() {
        pointsToWinView.text = mGame.pointsToWin.toString()
        rotateAfterView.text = mGame.rotateAfter.toString()
        refreshPlayers()
        mCommentator.announceGameStart()
    }

    private fun refreshPlayers() {
        val (singles, doubles, extras) = mGame.players.shift(mGame.singleIdx).split(1, 2)
        singlesLayout.players = singles.toMutableList()
        doublesLayout.players = doubles.toMutableList()
        extrasLayout.players = extras.toMutableList()
    }

    private fun goal() {
        mCommentator.announce(Commentator.Sfx.DING.name)
        mGame.edit {
            getSingle().score++
            pointsSinceRotation++
        }

        if (mGame.hasWinner()) {
            mCommentator.announce("${Commentator.Sfx.WINNING_GOAL} ${mGame.getWinner()?.name} wins the game.")
            mCommentator.queueAnnounce(mGame.getWinner()?.customVictoryPhrase ?: "")
            WinCutThroatGameDialog(this, mGame, { endGame() }).show()
        } else if (mGame.rotateAfter > 0 && mGame.pointsSinceRotation >= mGame.rotateAfter) {
            rotate()
        } else {
            singlesLayout.addPlayer(mGame.getSingle())
        }

    }

    private fun rotate() {
        mCommentator.queueAnnounce("${Commentator.Sfx.ROTATE_DING} Rotate.")
        mGame.edit {
            pointsSinceRotation = 0
            singleIdx = (singleIdx + 1) % players.size
        }
        refreshPlayers()
        mCommentator.queueAnnounce(mGame.getSingle().user.name)
    }

    private fun endGame() {
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
        am.set(AlarmManager.RTC_WAKEUP, 10000, pi)
    }

    override fun onDestroy() {
        super.onDestroy()
        GoalReceiver.unregister(this, goalReceiver)
    }

    private fun updateGameStatusBusy() {
        mDatabase.child("tables").child(mTable.id).child("currentGame").setValue("BUSY")
    }

    private fun updateGameStatusFree() {
        mDatabase.child("tables").child(mTable.id).child("currentGame").setValue("FREE")

    }
}
