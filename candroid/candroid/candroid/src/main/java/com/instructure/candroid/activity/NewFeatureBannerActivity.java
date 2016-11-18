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

import android.app.Activity;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;

import com.instructure.candroid.R;
import com.instructure.candroid.util.ApplicationManager;
import com.instructure.pandautils.utils.Const;

public class NewFeatureBannerActivity extends Activity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.new_feature_banner_activity);

        boolean funModeDisabled = ApplicationManager.getPrefs(this).load(Const.FUN_MODE, false);
        ImageView close = (ImageView) findViewById(R.id.closeBtn);
        ImageView superPanda = (ImageView)findViewById(R.id.superPanda);
        superPanda.setVisibility(funModeDisabled ? View.GONE : View.VISIBLE);

        View container = findViewById(R.id.container);
        container.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
                overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out);
            }
        });

        close.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
                overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out);
            }
        });
    }

    @Override
    public void finish() {
        super.finish();

        //remember that we've seen this new feature before
        ApplicationManager.getPrefs(this).save(Const.VIEWED_NEW_FEATURE_BANNER, true);
    }

}
