package com.instructure.androidfoosball.holders

import android.content.Context
import android.support.v7.widget.RecyclerView
import android.view.View
import com.instructure.androidfoosball.R
import com.instructure.androidfoosball.ktmodels.User
import com.instructure.androidfoosball.utils.setAvatar
import kotlinx.android.synthetic.main.adapter_foos_rank.view.*
import org.jetbrains.anko.onClick


class FoosRankViewHolder(view: View) : RecyclerView.ViewHolder(view) {
    fun bind(context: Context, user: User, position: Int, foosRankCallback: (User) -> Unit) {
        itemView.foosRank.text = when {
            user.rankedGamesPlayed > 0 -> user.foosRanking.toString()
            else -> context.getString(R.string.no_ranking)
        }
        itemView.position.text = "${position + 1}"
        itemView.name.text = user.name
        itemView.avatar.setAvatar(user, context.resources.getDimension(R.dimen.avatar_size_medium).toInt())
        itemView.onClick { foosRankCallback(user) }
    }
}