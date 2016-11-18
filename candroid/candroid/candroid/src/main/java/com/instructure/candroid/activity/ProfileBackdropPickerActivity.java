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

package com.instructure.candroid.activity;

import android.content.Context;
import android.content.Intent;
import android.graphics.drawable.AnimationDrawable;
import android.os.Build;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.GridView;
import android.widget.ImageView;
import com.instructure.candroid.R;
import com.instructure.pandautils.activities.BaseActionBarActivity;
import com.instructure.pandautils.utils.Const;
import com.makeramen.roundedimageview.RoundedImageView;
import com.squareup.picasso.Picasso;

public class ProfileBackdropPickerActivity extends BaseActionBarActivity implements
        GridView.OnItemClickListener {

    private ImageView mProgress;
    private GridView mGridView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getSupportActionBar().setTitle(R.string.chooseBackgroundImage);

        mProgress = (ImageView) findViewById(R.id.progress_indicator);
        mGridView = (GridView) findViewById(R.id.gridView);

        mProgress.setVisibility(View.VISIBLE);

        // Get the background, which has been compiled to an AnimationDrawable object.
        AnimationDrawable frameAnimation = (AnimationDrawable) mProgress.getBackground();
        // Start the animation (looped playback by default).
        frameAnimation.start();

        loadAdapter(getResources().getStringArray(R.array.backdropURLs));

        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            getSupportActionBar().setElevation(Const.ACTIONBAR_ELEVATION);
        }
    }

    @Override
    public int contentResId() {
        return R.layout.profile_background_picker_activity;
    }

    @Override
    public boolean showHomeAsUp() {
        return true;
    }

    @Override
    public boolean showTitleEnabled() {
        return true;
    }

    @Override
    public void onUpPressed() {
        finish();
    }

    private void loadAdapter(String[] urls) {
        mProgress.setVisibility(View.GONE);
        GridViewAdapter adapter = new GridViewAdapter(getApplicationContext(), R.layout.profile_background_grid_item, urls);
        mGridView.setAdapter(adapter);
        mGridView.setOnItemClickListener(this);
    }

    public class GridViewAdapter extends ArrayAdapter<String> {

        private LayoutInflater inflater;
        private ViewHolder viewHolder;

        public GridViewAdapter(Context context, int resource, String[] objects) {
            super(context, resource, objects);
            inflater = getLayoutInflater();
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {

            if(convertView == null) {
                convertView = inflater.inflate(R.layout.profile_background_grid_item, parent, false);
                viewHolder = new ViewHolder();
                viewHolder.imageView = (RoundedImageView)convertView.findViewById(R.id.gridImage);
                convertView.setTag(viewHolder);
            } else {
                viewHolder = (ViewHolder) convertView.getTag();
            }

            String itemUrl = getItem(position);

            if(itemUrl != null) {
                Picasso.with(getApplicationContext())
                        .load(itemUrl)
                        .placeholder(R.drawable.ic_empty)
                        .fit()
                        .centerCrop()
                        .into(viewHolder.imageView);
            }

            return convertView;
        }
    }

    static class ViewHolder {
        RoundedImageView imageView;
    }

    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        String item = (String)parent.getItemAtPosition(position);
        Intent intent = new Intent();
        intent.putExtra(Const.URL, item);
        setResult(Const.PROFILE_BACKGROUND_SELECTED_RESULT_CODE, intent);
        finish();
    }
}
