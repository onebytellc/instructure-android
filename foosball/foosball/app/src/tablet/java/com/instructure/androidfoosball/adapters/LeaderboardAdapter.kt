package com.instructure.androidfoosball.adapters

import android.content.Context
import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.ViewGroup
import com.instructure.androidfoosball.R
import com.instructure.androidfoosball.ktmodels.User
import com.instructure.androidfoosball.holders.LeaderboardViewHolder


class LeaderboardAdapter (private val mContext: Context, private val mUsers: List<User>) : RecyclerView.Adapter<LeaderboardViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): LeaderboardViewHolder {
        val v = LayoutInflater.from(parent.context).inflate(R.layout.adapter_leaderboard, parent, false)
        return LeaderboardViewHolder(v)
    }

    override fun onBindViewHolder(holder: LeaderboardViewHolder, position: Int) {
        holder.bind(mContext, mUsers[position], position)
    }

    override fun getItemCount() = mUsers.size
}
