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

package com.instructure.parentapp.adapter;

import android.content.Context;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.instructure.canvasapi2.models.Student;
import com.instructure.pandautils.utils.Utils;
import com.instructure.parentapp.BuildConfig;
import com.instructure.parentapp.R;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;

import de.hdodenhof.circleimageview.CircleImageView;

/**
 * Copyright (c) 2016 Instructure. All rights reserved.
 */
public class CarouselPagerAdapter extends PagerAdapter {

    private ArrayList<Student> mUserList = new ArrayList<>();
    private Context mContext;
    private LayoutInflater mLayoutInflater;

    public CarouselPagerAdapter(Context context) {
        mContext = context;
        mLayoutInflater = LayoutInflater.from(mContext);
    }

    @Override
    public int getItemPosition(Object object) {
        return POSITION_NONE;
    }
    @Override
    public int getCount() {
        return mUserList.size();
    }

    @Override
    public boolean isViewFromObject(View view, Object object) {
        return view == object;
    }

    @Override
    public Object instantiateItem(ViewGroup container, final int position) {
        View itemView = mLayoutInflater.inflate(R.layout.carousel_item_view, container, false);
        CircleImageView avatarView = (CircleImageView) itemView.findViewById(R.id.avatar);
        Utils.testSafeContentDescription(avatarView,
                String.format(mContext.getString(R.string.avatar_content_desc), position),
                mUserList.get(position).getStudentName(),
                BuildConfig.IS_TESTING);
        itemView.setTag(position);
        Picasso.with(mContext).load(mUserList.get(position).getAvatarUrl()).placeholder(R.drawable.ic_cv_user_white).error(R.drawable.ic_cv_user_white).fit().into(avatarView);

        avatarView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(mContext instanceof ViewPager.OnPageChangeListener) {
                    ((ViewPager.OnPageChangeListener) mContext).onPageSelected(position);
                }
            }
        });
        container.addView(itemView);

        return itemView;
    }

    @Override
    public void destroyItem(ViewGroup container, int position, Object object) {

    }

    public Student getItem(int position){
        return mUserList.get(position);
    }

    public void addItem(Student user){
        mUserList.add(user);
        notifyDataSetChanged();
    }

    public void addAll(ArrayList<Student> users){
        mUserList.addAll(users);
        notifyDataSetChanged();
    }

    public void clear() {
        mUserList.clear();
        notifyDataSetChanged();
    }

}
