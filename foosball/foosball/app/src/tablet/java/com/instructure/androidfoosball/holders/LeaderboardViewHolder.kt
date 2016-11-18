package com.instructure.androidfoosball.holders

import android.content.Context
import android.support.v7.widget.RecyclerView
import android.view.View
import com.instructure.androidfoosball.R
import com.instructure.androidfoosball.ktmodels.User
import com.instructure.androidfoosball.activities.LeaderboardActivity
import com.instructure.androidfoosball.utils.getWinRate
import com.instructure.androidfoosball.utils.setAvatar
import kotlinx.android.synthetic.tablet.adapter_leaderboard.view.*


class LeaderboardViewHolder(view: View) : RecyclerView.ViewHolder(view) {
    fun bind(context: Context, user: User, position: Int) {
        val winRate = user.getWinRate(LeaderboardActivity.MIN_GAMES_FOR_RANKING)
        itemView.winRate.text = when {
            winRate >= 0 -> context.getString(R.string.win_rate_format).format(winRate)
            else -> context.getString(R.string.no_ranking)
        }
        itemView.position.text = "${position + 1}"
        itemView.name.text = user.name
        itemView.wins.text = context.getString(R.string.leaderboard_wins).format(user.wins)
        itemView.losses.text = context.getString(R.string.leaderboard_losses).format(user.losses)
        itemView.avatar.setAvatar(user, context.resources.getDimension(R.dimen.avatar_size_medium).toInt())
    }
}
