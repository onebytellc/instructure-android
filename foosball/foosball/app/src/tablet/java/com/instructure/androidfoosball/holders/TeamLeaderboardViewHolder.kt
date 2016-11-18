package com.instructure.androidfoosball.holders

import android.content.Context
import android.support.v7.widget.RecyclerView
import android.text.TextUtils
import android.view.View
import com.instructure.androidfoosball.R
import com.instructure.androidfoosball.activities.LeaderboardActivity
import com.instructure.androidfoosball.ktmodels.CustomTeam
import com.instructure.androidfoosball.ktmodels.User
import com.instructure.androidfoosball.utils.getWinRate
import com.instructure.androidfoosball.utils.setAvatar
import kotlinx.android.synthetic.tablet.adapter_team_leaderboard.view.*


class TeamLeaderboardViewHolder(view: View) : RecyclerView.ViewHolder(view) {
    fun bind(context: Context, team: CustomTeam, users: Map<String, User>, position: Int) {
        val playerOne: User? = users.get(team.users[0])
        val playerTwo: User? = users.get(team.users[1])
        if(playerOne == null || playerTwo == null) {
            return
        }
        val winRate = team.getWinRate(LeaderboardActivity.MIN_GAMES_FOR_TEAM_RANKING)
        itemView.team_winRate.text = when {
            winRate >= 0 -> context.getString(R.string.win_rate_format).format(winRate)
            else -> context.getString(R.string.no_ranking)
        }
        itemView.team_position.text = "${position + 1}"
        if(TextUtils.isEmpty(team.teamName)) {
            itemView.team_name.visibility = 8
        } else {
            itemView.team_name.visibility = 0
        }
        itemView.team_name.text = team.teamName
        itemView.wins.text = context.getString(R.string.leaderboard_wins).format(team.teamWins)
        itemView.losses.text = context.getString(R.string.leaderboard_losses).format(team.teamLosses)
        itemView.avatar_player_one.setAvatar(playerOne, context.resources.getDimension(R.dimen.avatar_size_medium).toInt())
        itemView.avatar_player_two.setAvatar(playerTwo, context.resources.getDimension(R.dimen.avatar_size_medium).toInt())

    }
}
