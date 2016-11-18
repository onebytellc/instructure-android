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
import android.net.Uri;
import android.support.annotation.NonNull;
import android.view.Menu;
import android.view.MenuItem;
import com.instructure.candroid.R;
import com.pspdfkit.datastructures.Range;
import com.pspdfkit.document.processor.PSPDFProcessorTask;
import com.pspdfkit.document.sharing.DefaultDocumentSharingController;
import com.pspdfkit.document.sharing.DocumentSharingIntentHelper;
import com.pspdfkit.document.sharing.DocumentSharingManager;
import com.pspdfkit.document.sharing.SharingOptions;
import com.pspdfkit.ui.PSPDFActivity;

import java.util.List;


public class CandroidPSPDFActivity extends PSPDFActivity {

    @Override
    public boolean onPrepareOptionsMenu(@NonNull Menu menu) {
        super.onPrepareOptionsMenu(menu);

        getMenuInflater().inflate(R.menu.pspdf_activity_menu, menu);

        return true;
    }

    @Override public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        super.onOptionsItemSelected(item);

        if(item.getItemId() == R.id.upload_item) {
            uploadDocumentToCanvas();
            return true;
        } else {
            return false;
        }
    }

    private void uploadDocumentToCanvas(){
        DocumentSharingManager.shareDocument(
                new CandroidDocumentSharingController(this),
                getDocument(),
                new SharingOptions(PSPDFProcessorTask.AnnotationProcessingMode.FLATTEN));
    }


    private class CandroidDocumentSharingController extends DefaultDocumentSharingController {

        private Context mContext;

        public CandroidDocumentSharingController(@NonNull Context context) {
            super(context);
            mContext = context;
        }

        @Override
        protected void onDocumentPrepared(@NonNull Uri shareUri) {
            Intent intent = new Intent(mContext, ShareFileUploadActivity.class);
            intent.setType(DocumentSharingIntentHelper.MIME_TYPE_PDF);
            intent.putExtra(Intent.EXTRA_STREAM, shareUri);
            intent.setAction(Intent.ACTION_SEND);
            mContext.startActivity(intent);
        }
    }
}
