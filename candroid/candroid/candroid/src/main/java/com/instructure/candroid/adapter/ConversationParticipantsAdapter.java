/*
 * Copyright (C) 2016 - present  Instructure, Inc.
 *
 *     This program is free software: you can redistribute it and/or modify
 *     it under the terms of the GNU General Public License as published by
 *     the Free Software Foundation, version 3 of the License.
 *
 *     This program is distributed in the hope that it will be useful,
 *     but WITHOUT ANY WARRANTY; without even the implied warranty of
 *     MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *     GNU General Public License for more details.
 *
 *     You should have received a copy of the GNU General Public License
 *     along with this program.  If not, see <http://www.gnu.org/licenses/>.
 *
 */

package com.instructure.candroid.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;
import com.instructure.candroid.R;
import com.instructure.canvasapi.model.BasicUser;
import com.instructure.loginapi.login.util.ProfileUtils;
import java.util.List;

import de.hdodenhof.circleimageview.CircleImageView;

public class ConversationParticipantsAdapter extends ArrayAdapter<BasicUser> {

    private List<BasicUser> mData;
    private Context mContext;
    private String mDefaultText;
    private LayoutInflater mInflater;

    public ConversationParticipantsAdapter(Context context, List<BasicUser> data, String defaultText) {
        super(context, R.layout.actionbar_participants_item, data);
        mContext = context;
        mData = data;
        mDefaultText = defaultText;
        mInflater = LayoutInflater.from(mContext);
    }

    public BasicUser getItem(int position) {
        return mData.get(position);
    }

    @Override
    public boolean areAllItemsEnabled() {
        return false;
    }

    @Override
    public boolean isEnabled(int position) {
        return false;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        return buildView(position, convertView, parent, false);
    }

    @Override
    public View getDropDownView(int position, View convertView, ViewGroup parent) {
        return buildView(position, convertView, parent, true);
    }

    private View buildView(int position, View convertView, ViewGroup parent, boolean isDropdown){
        final ParticipantViewHolder holder;
        if(convertView == null){
            convertView = mInflater.inflate(R.layout.actionbar_participants_item, parent, false);
            holder = new ParticipantViewHolder();
            holder.userName = (TextView)convertView.findViewById(R.id.username);
            holder.avatar = (CircleImageView)convertView.findViewById(R.id.avatar);
            holder.indicator = convertView.findViewById(R.id.indicator);
            convertView.setTag(holder);
        }else{
            holder = (ParticipantViewHolder) convertView.getTag();
        }

        if(isDropdown){
            holder.avatar.setVisibility(View.VISIBLE);
            holder.userName.setText(mData.get(position).getUsername());
            holder.userName.setTextColor(mContext.getResources().getColor(R.color.canvasTextDark));
            ProfileUtils.configureAvatarView(mContext, mData.get(position).getUsername(), mData.get(position).getAvatarUrl(), holder.avatar);
        }else{
            holder.avatar.setVisibility(View.GONE);
            holder.userName.setText(mDefaultText);
            holder.userName.setTextColor(mContext.getResources().getColor(R.color.whiteNoAlpha));
            holder.indicator.setVisibility(View.VISIBLE);
        }

        return convertView;
    }

    private static class ParticipantViewHolder{
        TextView userName;
        CircleImageView avatar;
        View indicator;
    }
}
