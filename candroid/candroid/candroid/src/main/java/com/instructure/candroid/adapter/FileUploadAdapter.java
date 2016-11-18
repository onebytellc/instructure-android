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
import android.content.Intent;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.MimeTypeMap;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TableRow;
import android.widget.TextView;


import com.instructure.candroid.R;
import com.instructure.canvasapi.model.CanvasContext;
import com.instructure.pandautils.models.FileSubmitObject;
import com.instructure.pandautils.utils.CanvasContextColor;

import org.w3c.dom.Text;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.zip.Inflater;

public class FileUploadAdapter extends BaseAdapter{

    Context context;
    CanvasContext canvasContext;
    List<FileSubmitObject> fileSubmitObjects;

    public FileUploadAdapter(Context context, CanvasContext canvasContext, List<FileSubmitObject> fileSubmitObjects){
        this.context = context;
        this.canvasContext = canvasContext;
        this.fileSubmitObjects = fileSubmitObjects;
    }

    @Override
    public int getCount() {
        return fileSubmitObjects.size();
    }

    @Override
    public Object getItem(int position) {
        return fileSubmitObjects.get(position);
    }

    @Override
    public long getItemId(int position) {
        return fileSubmitObjects.get(position).getName().hashCode();
    }

    @Override
    public View getView(final int position, View convertView, ViewGroup parent) {
        final ViewHolder holder;

        if (convertView == null) {
				/* There is no view at this position, we create a new one.
		           In this case by inflating an xml layout */
            convertView = (LayoutInflater.from(context).inflate(R.layout.listview_item_row_attachedfiles, null));

            holder = new ViewHolder();
            holder.fileName = (TextView) convertView.findViewById(R.id.fileName);
            holder.fileIcon = (ImageView) convertView.findViewById(R.id.fileIcon);
            holder.remove = (Button) convertView.findViewById(R.id.removeFile);
            holder.fileSize = (TextView) convertView.findViewById(R.id.fileSize);
            holder.uploadProgress = (ProgressBar) convertView.findViewById(R.id.progressBar);
            convertView.setTag(holder);
        } else {
				/* We recycle a View that already exists */
            holder = (ViewHolder) convertView.getTag();
        }

        FileSubmitObject fso = fileSubmitObjects.get(position);

        if(fso.getContentType().contains("image")){
            holder.fileIcon.setImageDrawable(CanvasContextColor.getColoredDrawable(context, R.drawable.ic_cv_image, canvasContext));
        }else{
            holder.fileIcon.setImageDrawable(CanvasContextColor.getColoredDrawable(context, R.drawable.ic_cv_document, canvasContext));
        }

        if(fso.getCurrentState()== FileSubmitObject.STATE.UPLOADING){

            holder.uploadProgress.setIndeterminate(true);
            holder.uploadProgress.setVisibility(View.VISIBLE);
            holder.fileIcon.setVisibility(View.GONE);
            holder.remove.setVisibility(View.GONE);
        }
        else if(fso.getCurrentState()== FileSubmitObject.STATE.COMPLETE){
            holder.fileIcon.setImageDrawable(CanvasContextColor.getColoredDrawable(context, R.drawable.ic_cv_checkmark_dark, canvasContext));
            holder.remove.setVisibility(View.GONE);
            holder.uploadProgress.setIndeterminate(false);
            holder.uploadProgress.setVisibility(View.GONE);
            holder.fileIcon.setVisibility(View.VISIBLE);
        }
        else if(fso.getCurrentState()== FileSubmitObject.STATE.NORMAL){
            holder.remove.setBackgroundDrawable(CanvasContextColor.getColoredDrawable(context, R.drawable.ic_cv_delete_round, canvasContext));
            holder.remove.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    //they are added in the same order, so the position should be the same
                    fileSubmitObjects.remove(position);
                    notifyDataSetChanged();
                }
            });
        }

        holder.fileName.setText(fileSubmitObjects.get(position).getName());
        holder.fileSize.setText(humanReadableByteCount(fileSubmitObjects.get(position).getSize()));

        return convertView;
    }

    public void setFileState(FileSubmitObject fso, FileSubmitObject.STATE newState){
        int index = fileSubmitObjects.indexOf(fso);
        if(index != -1){
            fileSubmitObjects.get(index).setState(newState);
        }
        notifyDataSetChanged();
    }

    public void setFilesToUploading(){
        for(FileSubmitObject fso : fileSubmitObjects){
            fso.setState(FileSubmitObject.STATE.UPLOADING);
        }
        notifyDataSetChanged();
    }


    public void clear() {
        fileSubmitObjects.clear();
        notifyDataSetChanged();
    }

    public static class ViewHolder {
        ImageView fileIcon;
        TextView fileName;
        TextView fileSize;
        Button remove;
        ProgressBar uploadProgress;
    }

    public static String humanReadableByteCount(long bytes) {
        int unit = 1024;
        if (bytes < unit) return bytes + " B";
        int exp = (int) (Math.log(bytes) / Math.log(unit));
        String pre = String.valueOf(("KMGTPE").charAt(exp-1));
        return String.format("%.1f %sB", bytes / Math.pow(unit, exp), pre);
    }
}

