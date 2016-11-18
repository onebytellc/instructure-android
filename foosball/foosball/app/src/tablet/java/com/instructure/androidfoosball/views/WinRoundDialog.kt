package com.instructure.androidfoosball.views

import android.animation.AnimatorSet
import android.animation.ObjectAnimator
import android.app.Dialog
import android.content.Context
import android.os.Bundle
import android.view.View
import android.view.animation.OvershootInterpolator
import android.widget.ImageView
import android.widget.TextView
import com.instructure.androidfoosball.R
import com.instructure.androidfoosball.ktmodels.Game
import com.instructure.androidfoosball.ktmodels.Table
import com.instructure.androidfoosball.utils.bind
import com.instructure.androidfoosball.views.CountdownCircle
import com.instructure.androidfoosball.views.TeamLayout
import org.jetbrains.anko.onClick

class WinRoundDialog(context: Context, val game: Game, val onNextRound: () -> Unit, val onUndoGoal: () -> Unit) : Dialog(context) {

    private val COUNTDOWN_SECONDS = 8

    private val countdownView: CountdownCircle by bind(R.id.countdownView)
    private val countdownSeconds: TextView by bind(R.id.countdownSeconds)
    private val teamLayout: TeamLayout by bind(R.id.teamLayout)
    private val victoryStar: ImageView by bind(R.id.victoryStar)
    private val undoView: View by bind(R.id.undoView)
    private val teamNameView: TextView by bind(R.id.winningTeamNameView)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setCancelable(false)
        setCanceledOnTouchOutside(false)
        setContentView(R.layout.dialog_win_round)
        setupViews()
        setupListeners()
        animateStar()
    }

    private fun setupViews() {
        game.currentRound().getWinningTeam()?.let { team ->
            teamLayout.team = team
            teamNameView.text = context.getString(R.string.team_win_round).format(game.currentRound().getTeamName(team, Table.getSelectedTable()))
        }
    }

    private fun animateStar() {
        // Set initial scale
        victoryStar.scaleX = 0f
        victoryStar.scaleY = 0f

        // Start scale animation
        AnimatorSet().apply {
            playTogether(
                    ObjectAnimator.ofFloat(victoryStar, "scaleX", 0f, 1f),
                    ObjectAnimator.ofFloat(victoryStar, "scaleY", 0f, 1f)
            )
            duration = 600
            startDelay = 200
            interpolator = OvershootInterpolator()
        }.start()

        // Start ongoing rotation animation
        ObjectAnimator.ofFloat(victoryStar, "rotation", 0f, 360f).apply {
            repeatCount = ObjectAnimator.INFINITE
            duration = 10000
        }.start()
    }

    private fun setupListeners() {
        // Countdown timer
        countdownView.startCountdown(COUNTDOWN_SECONDS) { seconds ->
            if (seconds > 0) {
                countdownSeconds.text = seconds.toString()
            } else {
                onNextRound()
                dismiss()
            }
        }

        // Undo goal
        undoView.onClick {
            onUndoGoal()
            dismiss()
        }

        // Next round (Swap now)
        countdownView.onClick {
            onNextRound()
            dismiss()
        }
    }
}
