package com.instructure.androidfoosball.adapters


import android.annotation.SuppressLint
import android.content.Context
import android.view.View
import android.view.ViewGroup
import android.widget.BaseAdapter
import android.widget.TextView
import com.instructure.androidfoosball.R
import com.instructure.androidfoosball.ktmodels.User
import com.instructure.androidfoosball.utils.setAvatar
import de.hdodenhof.circleimageview.CircleImageView

/**
 * Copyright (c) 2016 Instructure. All rights reserved.

 * Simple adapter example for custom items in the dialog
 */
class UserAdapter(private val mContext: Context, private val mUsers: List<User>) : BaseAdapter() {
    private val mAvatarSize: Int

    init {
        mAvatarSize = mContext.resources.getDimension(R.dimen.avatar_size_small).toInt()
    }

    override fun getCount() = mUsers.size

    override fun getItem(position: Int) = mUsers[position]

    override fun getItemId(position: Int) = position.toLong()

    override fun hasStableIds() = true

    @SuppressLint("ViewHolder")
    override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {
        var convertView = convertView
        val holder: UserHolder
        if (convertView == null) {
            convertView = View.inflate(mContext, R.layout.adapter_user_dialog, null)
            holder = UserHolder()
            holder.userName = convertView!!.findViewById(R.id.userName) as TextView
            holder.avatar = convertView.findViewById(R.id.avatar) as CircleImageView
            convertView.tag = holder

        } else {
            holder = convertView.tag as UserHolder
        }
        holder.avatar?.setAvatar(mUsers[position], mAvatarSize)
        holder.userName?.text = mUsers[position].name
        return convertView
    }

    internal class UserHolder {
        var userName: TextView? = null
        var avatar: CircleImageView? = null
    }
}
