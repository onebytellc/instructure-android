package com.instructure.androidfoosball.adapters

import android.content.Context
import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.ViewGroup
import com.instructure.androidfoosball.R
import com.instructure.androidfoosball.holders.TeamLeaderboardViewHolder
import com.instructure.androidfoosball.ktmodels.CustomTeam
import com.instructure.androidfoosball.ktmodels.User


class TeamLeaderboardAdapter (private val mContext: Context, private val mTeams: List<CustomTeam>, private val mUsers: Map<String, User>) : RecyclerView.Adapter<TeamLeaderboardViewHolder>() {
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): TeamLeaderboardViewHolder {
        val v = LayoutInflater.from(parent.context).inflate(R.layout.adapter_team_leaderboard, parent, false)
        return TeamLeaderboardViewHolder(v)
    }

    override fun onBindViewHolder(holder: TeamLeaderboardViewHolder, position: Int) {
        holder.bind(mContext, mTeams[position], mUsers, position)
    }

    override fun getItemCount() = mTeams.size
}