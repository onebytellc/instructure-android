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

package com.instructure.candroid.util;

import android.content.Context;
import android.net.Uri;
import com.instructure.candroid.BuildConfig;
import com.instructure.candroid.activity.CandroidPSPDFActivity;
import com.instructure.canvasapi.model.User;
import com.instructure.canvasapi.utilities.APIHelpers;
import com.instructure.pandautils.loaders.OpenMediaAsyncTaskLoader;
import com.pspdfkit.PSPDFKit;
import com.pspdfkit.configuration.activity.PSPDFActivityConfiguration;
import com.pspdfkit.configuration.annotations.AnnotationEditingConfiguration;
import com.pspdfkit.configuration.page.PageFitMode;
import com.pspdfkit.configuration.page.PageScrollDirection;
import com.pspdfkit.ui.PSPDFActivity;

import java.io.File;

public class FileUtils {

	public static int millisecondsInADay = 86400000;
	//NOT recursive
	public static boolean deleteOldFilesInDirectory(File file, int daysOld)
	{
		long currentTime = System.currentTimeMillis();
		long duration = daysOld * millisecondsInADay;

		try
		{
			if(file.isDirectory())
			{
				String[] files = file.list();
				for(int i = 0; i < files.length; i++)
				{
					File fileToDelete = new File(file, files[i]);
					if(fileToDelete.lastModified() + duration <= currentTime)
						fileToDelete.delete();
				}
			}
			return true;
		}
		catch(Exception E)
		{
			return false;
		}
	}

	public static void showPdfDocument(Uri uri, OpenMediaAsyncTaskLoader.LoadedMedia loadedMedia, final Context context){
		User cachedUser = APIHelpers.getCacheUser(context);
		String userName = "";
		if(cachedUser != null) {
			userName = APIHelpers.getCacheUser(context).getShortName();
		}
		final PSPDFActivityConfiguration pspdfActivityConfiguration = new PSPDFActivityConfiguration.Builder(context, BuildConfig.PSPDFKIT_LICENSE_KEY)
				.scrollDirection(PageScrollDirection.HORIZONTAL)
				.showThumbnailGrid()
				.showThumbnailBar()
				.enableDocumentEditor()
				.annotationEditingConfiguration(new AnnotationEditingConfiguration.Builder(context).defaultAnnotationCreator(userName).build())
				.fitMode(PageFitMode.FIT_TO_WIDTH)
				.build();

		if(PSPDFKit.isOpenableUri(context, uri)){
			context.startActivity(PSPDFActivity.IntentBuilder.fromUri(context, uri, pspdfActivityConfiguration).activity(CandroidPSPDFActivity.class).build());
		} else {
			//If we still can't open this PDF, we will then attempt to pass it off to the user's pdfviewer
			context.startActivity(loadedMedia.getIntent());
		}

	}
}
