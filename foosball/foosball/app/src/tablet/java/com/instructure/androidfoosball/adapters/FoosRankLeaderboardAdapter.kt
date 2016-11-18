package com.instructure.androidfoosball.adapters

import android.content.Context
import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.ViewGroup
import com.instructure.androidfoosball.R
import com.instructure.androidfoosball.holders.FoosRankViewHolder
import com.instructure.androidfoosball.ktmodels.User


class FoosRankLeaderboardAdapter (private val mContext: Context, private val mUsers: List<User>, val foosRankCallback: (User) -> Unit) : RecyclerView.Adapter<FoosRankViewHolder>() {

    override fun getItemCount(): Int {
        return mUsers.size
    }

    override fun onBindViewHolder(holder: FoosRankViewHolder, position: Int) {
        holder.bind(mContext, mUsers[position], position, foosRankCallback)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): FoosRankViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.adapter_foos_rank, parent, false)
        return FoosRankViewHolder(view)
    }



}