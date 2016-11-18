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
import android.view.View;

import com.instructure.candroid.binders.FileBinder;
import com.instructure.candroid.holders.FileViewHolder;
import com.instructure.candroid.interfaces.AdapterToFragmentLongClick;
import com.instructure.canvasapi.api.FileFolderAPI;
import com.instructure.canvasapi.model.CanvasContext;
import com.instructure.canvasapi.model.FileFolder;
import com.instructure.canvasapi.utilities.APIHelpers;
import com.instructure.canvasapi.utilities.CanvasCallback;
import com.instructure.canvasapi.utilities.LinkHeaders;

import java.util.ArrayList;

import retrofit.RetrofitError;
import retrofit.client.Response;

public class FileListRecyclerAdapter extends BaseListRecyclerAdapter<FileFolder, FileViewHolder>{

    //region Models
    private long mCurrentFolderId = 0;
    private String mFolderName;
    private FileFolder mLongClickItem;
    private CanvasContext mCanvasContext;
    private boolean mIsFilesAllPagesLoaded = false;
    private boolean mIsFoldersAllPagesLoaded = false;
    //endregion

    //region Callbacks
    private CanvasCallback<FileFolder[]> mFolderCallback;
    private CanvasCallback<FileFolder[]> mFileCallback;
    private ArrayList<FileFolder> mDeletedFileFolders = new ArrayList<FileFolder>();
    private AdapterToFragmentLongClick<FileFolder> mAdaptertoFragmentLongCLick;
    //endregion

    /* This is the real constructor and should be called to create instances of this adapter */
    public FileListRecyclerAdapter(Context context, CanvasContext canvasContext,
        long folderId, String folderName,
        AdapterToFragmentLongClick<FileFolder> adapterToFragmentLongClick) {
        this(context, canvasContext, folderId, folderName, adapterToFragmentLongClick, true);
    }

    /* This overloaded constructor is for testing purposes ONLY, and should not be used to create instances of this adapter. */
    protected FileListRecyclerAdapter(Context context, CanvasContext canvasContext,
                                   long folderId, String folderName,
                                   AdapterToFragmentLongClick<FileFolder> adapterToFragmentLongClick, boolean isLoadData) {
        super(context, FileFolder.class);
        setItemCallback(new ItemComparableCallback<FileFolder>() {
            @Override
            public int compare(FileFolder o1, FileFolder o2) {
                return o1.compareTo(o2);
            }

            @Override
            public boolean areContentsTheSame(FileFolder item1, FileFolder item2) {
                return compareFileFolders(item1, item2);
            }

            @Override
            public boolean areItemsTheSame(FileFolder item1, FileFolder item2) {
                return item1.getId() == item2.getId();
            }

            @Override
            public long getUniqueItemId(FileFolder fileFolder) {
                return fileFolder.getId();
            }
        });

        mCanvasContext = canvasContext;
        mAdaptertoFragmentLongCLick = adapterToFragmentLongClick;
        mCurrentFolderId = folderId;
        mFolderName = folderName;

        if(isLoadData){
            loadData();
        }
    }

    @Override
    public void bindHolder(FileFolder baseItem, FileViewHolder holder, int position) {
        FileBinder.bind(holder, baseItem, getContext(), mCanvasContext, mAdaptertoFragmentLongCLick);
    }

    @Override
    public FileViewHolder createViewHolder(View v, int viewType) {
        return new FileViewHolder(v);
    }

    @Override
    public int itemLayoutResId(int viewType) {
        return FileViewHolder.holderResId();
    }

    @Override
    public boolean isPaginated() {
        return true;
    }

    @Override
    public void contextReady() {

    }

    //region Setup Callbacks
    @Override
    public void setupCallbacks() {
        mFolderCallback = new CanvasCallback<FileFolder[]>(this) {

            @Override
            public void firstPage(FileFolder[] folderList, LinkHeaders linkHeaders, Response response) {
                mIsFoldersAllPagesLoaded = linkHeaders.nextURL == null;
                mAdaptertoFragmentLongCLick.onRefreshFinished();
                setNextUrl(linkHeaders.nextURL);

                addAll(folderList);
                if(linkHeaders.nextURL == null){
                    getFiles(APIHelpers.isCachedResponse(response));
                }
            }
            
            @Override
            public boolean onFailure(RetrofitError retrofitError) {
                return false;
            }
        };

        mFileCallback = new CanvasCallback<FileFolder[]>(this) {

            @Override
            public void firstPage(FileFolder[] fileList, LinkHeaders linkHeaders, Response response) {
                mIsFilesAllPagesLoaded = linkHeaders.nextURL == null;
                mAdaptertoFragmentLongCLick.onRefreshFinished();
                setNextUrl(linkHeaders.nextURL);

                addAll(fileList);
            }

            @Override
            public boolean onFailure(RetrofitError retrofitError) {
                return false;
            }
        };
    }
    //endregion

    @Override
    public void loadFirstPage() {
        //First request all folders, folders callback will call files
        if (mCurrentFolderId > 0) {
            FileFolderAPI.getFirstPageFolders(mCurrentFolderId, mFolderCallback);
        } else {
            FileFolderAPI.getFirstPageFoldersRoot(mCanvasContext, mFolderCallback);
        }
    }

    private void getFiles(boolean isCached) {
        if (mCurrentFolderId > 0) {
            FileFolderAPI.getFirstPageFilesChained(mCurrentFolderId, isCached, mFileCallback);
        } else {
            FileFolderAPI.getFirstPageFilesRootChained(mCanvasContext, isCached, mFileCallback);
        }
    }

    @Override
    public void loadNextPage(String nextURL) {
        //The FileFolderAPI request works for both files and folders
        FileFolderAPI.getNextPageFileFolders(nextURL, mIsFoldersAllPagesLoaded ? mFileCallback : mFolderCallback);
    }

    private void removeDeletedFileFolders() {
        for (FileFolder fileFolder : mDeletedFileFolders) {
            remove(fileFolder);
        }
    }

    @Override
    public boolean shouldShowLoadingFooter() {
        //override here to let both api calls properly display pagination loader
        return (!mIsFilesAllPagesLoaded && size() > 0) && isPaginated();
    }

    @Override
    public boolean isAllPagesLoaded() {
        return mIsFilesAllPagesLoaded && mIsFoldersAllPagesLoaded;
    }

    //region GETTERS/SETTERS
    public void setCurrentFolderId(long id){
        mCurrentFolderId = id;
    }

    public long getCurrentFolderId(){
        return mCurrentFolderId;
    }

    public void setCurrentFolderName(String name){
        mFolderName = name;
    }

    public String getCurrentFolderName(){
        return mFolderName;
    }

    public void setLongClickItem(FileFolder fileFolder){
        mLongClickItem = fileFolder;
    }

    public FileFolder getLongClickItem(){
        return mLongClickItem;
    }

    public ArrayList<FileFolder> getDeletedFileFolders(){
        return mDeletedFileFolders;
    }
    //endregion

    private boolean compareFileFolders(FileFolder oldItem, FileFolder newItem){
        //object items
        if(oldItem.getDisplayName() != null && newItem.getDisplayName() != null){
            boolean sameName = oldItem.getDisplayName().equals(newItem.getDisplayName());
            boolean sameSize = oldItem.getSize() == newItem.getSize();
            return sameName && sameSize;
        }

        //folder objects
        if(oldItem.getName() != null && newItem.getName() != null){
            boolean sameName = oldItem.getName().equals(newItem.getName());
            boolean sameSize = oldItem.getSize() == newItem.getSize();
            return sameName && sameSize;
        }

        //if old and new aren't one of the same object types then contents have changed
        return false;
    }
}
