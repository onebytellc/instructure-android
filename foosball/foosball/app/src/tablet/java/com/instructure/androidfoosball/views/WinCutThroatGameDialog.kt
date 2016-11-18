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
import com.instructure.androidfoosball.ktmodels.CutThroatGame
import com.instructure.androidfoosball.ktmodels.Game
import com.instructure.androidfoosball.ktmodels.Table
import com.instructure.androidfoosball.utils.bind
import com.instructure.androidfoosball.views.TeamLayout
import org.jetbrains.anko.onClick

class WinCutThroatGameDialog(
        context: Context,
        val game: CutThroatGame,
        val onEndGame: () -> Unit
) : Dialog(context, R.style.AppTheme) {

    private val COUNTDOWN_SECONDS = 15

    private val playerLayout: TeamLayout by bind(R.id.playerLayout)
    private val victoryTrophy: ImageView by bind(R.id.victoryTrophy)
    private val playerNameView: TextView by bind(R.id.playerNameView)
    private val countdownView: CountdownCircle by bind(R.id.countdownView)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setCancelable(false)
        setCanceledOnTouchOutside(false)
        setContentView(R.layout.dialog_win_game_cut_throat)
        setupViews()
        setupListeners()
        animateTrophy()
    }

    private fun setupViews() {
        game.getWinner()?.let { user ->
            playerLayout.addUser(user)
            playerNameView.text = context.getString(R.string.team_win_game).format(user.name)
        }
    }

    private fun animateTrophy() {
        // Set initial scale
        victoryTrophy.scaleX = 0f
        victoryTrophy.scaleY = 0f

        // Start scale animation
        AnimatorSet().apply {
            playTogether(
                    ObjectAnimator.ofFloat(victoryTrophy, "scaleX", 0f, 1f),
                    ObjectAnimator.ofFloat(victoryTrophy, "scaleY", 0f, 1f)
            )
            duration = 600
            startDelay = 200
            interpolator = OvershootInterpolator()
        }.start()

        // Start ongoing rotation animation
        ObjectAnimator.ofFloat(victoryTrophy, "rotation", -6f, 6f).apply {
            repeatCount = ObjectAnimator.INFINITE
            repeatMode = ObjectAnimator.REVERSE
            duration = 3000
        }.start()
    }

    private fun setupListeners() {

        countdownView.startCountdown(COUNTDOWN_SECONDS) { seconds ->
            if (seconds > 0) {
                //countdownSeconds.text = seconds.toString()
            } else {
                onEndGame()
                dismiss()
            }
        }

        countdownView.onClick {
            onEndGame()
            dismiss()
        }
    }
}
