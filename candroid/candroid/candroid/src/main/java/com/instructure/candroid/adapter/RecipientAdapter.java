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
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.net.Uri;
import android.os.Handler;
import android.text.TextUtils;
import android.widget.Filter;
import com.android.ex.chips.BaseRecipientAdapter;
import com.android.ex.chips.RecipientManager;
import com.android.ex.chips.RecipientEntry;
import com.instructure.candroid.view.CanvasRecipientManager;
import com.instructure.canvasapi.api.RecipientAPI;
import com.instructure.canvasapi.model.Recipient;
import com.instructure.canvasapi.utilities.APIStatusDelegate;
import com.instructure.canvasapi.utilities.CanvasCallback;
import com.instructure.canvasapi.utilities.LinkHeaders;
import java.util.ArrayList;
import java.util.List;

import retrofit.client.Response;

public class RecipientAdapter extends BaseRecipientAdapter{

    private RecipientManager recipientManager;

    public RecipientAdapter(Context context) {
        super(context);
    }

    @Override
    public RecipientManager getRecipientManager() {
        recipientManager = CanvasRecipientManager.getInstance(getContext())
                                                 .setRecipientCallback(this)
                                                 .setPhotoCallback(this);
        return recipientManager;
    }

    public CanvasRecipientManager getCanvasRecipientManager() {
        return CanvasRecipientManager.getInstance(getContext())
                .setRecipientCallback(this)
                .setPhotoCallback(this);
    }
}
