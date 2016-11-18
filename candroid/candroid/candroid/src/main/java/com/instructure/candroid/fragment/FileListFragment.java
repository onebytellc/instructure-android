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

package com.instructure.candroid.fragment;

import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.view.ContextMenu;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import com.instructure.candroid.R;
import com.instructure.candroid.adapter.FileListRecyclerAdapter;
import com.instructure.candroid.decorations.DividerDecoration;
import com.instructure.candroid.delegate.Navigation;
import com.instructure.candroid.dialog.FileUploadDialog;
import com.instructure.candroid.interfaces.AdapterToFragmentLongClick;
import com.instructure.candroid.util.ApplicationManager;
import com.instructure.candroid.util.DownloadMedia;
import com.instructure.candroid.util.FragUtils;
import com.instructure.candroid.util.Param;
import com.instructure.canvasapi.api.FileFolderAPI;
import com.instructure.canvasapi.model.CanvasContext;
import com.instructure.canvasapi.model.Course;
import com.instructure.canvasapi.model.FileFolder;
import com.instructure.canvasapi.model.Tab;
import com.instructure.canvasapi.utilities.CanvasCallback;
import com.instructure.canvasapi.utilities.CanvasRestAdapter;
import com.instructure.canvasapi.utilities.LinkHeaders;
import com.instructure.pandarecycler.PandaRecyclerView;
import com.instructure.pandautils.utils.Const;
import com.instructure.pandautils.utils.PermissionUtils;

import retrofit.client.Response;

public class FileListFragment extends OrientationChangeFragment {

    //region Views
    private TextView folderNameTextView;
    private PandaRecyclerView mRecyclerView;
    //endregion

    //region Models
    private FileListRecyclerAdapter mRecyclerAdapter;
    private long currentFolderId;
    private String currentFolderName;
    //endregion

    //region Callbacks
    private AdapterToFragmentLongClick<FileFolder> mLongClickCallback;
    private FileUploadDialog uploadFileSourceFragment;
    //endregion

    @Override
    public FRAGMENT_PLACEMENT getFragmentPlacement(Context context) {return FRAGMENT_PLACEMENT.MASTER; }

    @Override
    public String getFragmentTitle() {
        return getString(R.string.files);
    }

    @Override
    protected String getSelectedParamName() {
        return Param.FILE_ID;
    }

    @Override
    public String getTabId() {
        return Tab.FILES_ID;
    }

    //region Lifecycle
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setUpCallbacks();
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if(uploadFileSourceFragment != null){
            uploadFileSourceFragment.onActivityResult(requestCode, resultCode, data);
        }
    }

    @Override
    public View populateView(LayoutInflater inflater, ViewGroup container) {
        View rootView = inflater.inflate(R.layout.course_files_layout, container, false);
        if(mRecyclerAdapter == null) {
            mRecyclerAdapter = new FileListRecyclerAdapter(getContext(),
                    getCanvasContext(), currentFolderId, currentFolderName, mLongClickCallback);
        }
        PandaRecyclerView recyclerView = configureRecyclerView(rootView, getContext(), mRecyclerAdapter, R.id.swipeRefreshLayout, R.id.emptyPandaView, R.id.listView);
        recyclerView.addItemDecoration(new DividerDecoration(getContext()));
        mRecyclerView = recyclerView;
        configureViews(rootView);

        if (mRecyclerAdapter.getCurrentFolderName() == null) {
            setRootFolderName();
        } else {
            folderNameTextView.setText(mRecyclerAdapter.getCurrentFolderName());
        }

        return rootView;
    }

    //endregion

    //region Menu
    @Override
    public void createOptionsMenu(Menu menu, MenuInflater inflater) {
        super.createOptionsMenu(menu, inflater);
        //If it's my files or I'm a teacher.
        if(getCanvasContext().getType() == CanvasContext.Type.USER ||
                (getCanvasContext().getType() == CanvasContext.Type.COURSE &&
                        ((Course)getCanvasContext()).isTeacher())) {
            inflater.inflate(R.menu.file_upload_menu, menu);
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem menuItem) {

        if(menuItem.getItemId() == R.id.fileUpload) {
            if(!CanvasRestAdapter.isNetworkAvaliable(getContext())) {
                Toast.makeText(getContext(), getContext().getString(R.string.notAvailableOffline), Toast.LENGTH_SHORT).show();
                return true;
            }

            Long parentFolderId = null;
            if (mRecyclerAdapter.getCurrentFolderId() != 0){
                parentFolderId = mRecyclerAdapter.getCurrentFolderId();
            }

            Bundle bundle;
            if (getCanvasContext().getType() == CanvasContext.Type.COURSE) {
                bundle = FileUploadDialog.createCourseBundle(null, (Course) getCanvasContext(), parentFolderId);
            } else {
                bundle = FileUploadDialog.createFilesBundle(null, parentFolderId);
            }

            uploadFileSourceFragment = FileUploadDialog.newInstance(getChildFragmentManager(),bundle);
            uploadFileSourceFragment.setDialogLifecycleCallback(new FileUploadDialog.DialogLifecycleCallback() {
                @Override
                public void onCancel(Dialog dialog) {

                }

                @Override
                public void onAllUploadsComplete(Dialog dialog) {
                    if (mRecyclerAdapter != null) {
                        mRecyclerAdapter.loadData();
                    }
                }
            });
            uploadFileSourceFragment.show(getChildFragmentManager(), FileUploadDialog.TAG);
            return true;
        }

        return super.onOptionsItemSelected(menuItem);
    }

    @Override
    public void onCreateContextMenu(ContextMenu menu, View v, ContextMenu.ContextMenuInfo menuInfo) {
        //get the file touched
        FileFolder file = mRecyclerAdapter.getLongClickItem();
        //check if it's a folder
        if(file == null || file.getFullName() != null) {
            return;
        }
        if(file.getContentType() != null && file.getContentType().contains("pdf")) {
            menu.add(getResources().getString(R.string.openAlternate));
        }
        menu.add(getResources().getString(R.string.open));

        if(ApplicationManager.isDownloadManagerAvailable(getContext()) && file.getDisplayName() != null) {
            menu.add(getResources().getString(R.string.download));
        }
            //If it's a course and they're a teacher or it's their files, allow them to delete
            if( getCanvasContext()!= null &&
                    (getCanvasContext().getType() == CanvasContext.Type.USER ||
                    (getCanvasContext().getType() == CanvasContext.Type.COURSE && ((Course)getCanvasContext()).isTeacher()))){
                menu.add(getResources().getString(R.string.delete));
        }
    }

    @Override
    public boolean onContextItemSelected(android.view.MenuItem item) {
        if (mRecyclerAdapter.getLongClickItem() == null) return false;

        final FileFolder file = mRecyclerAdapter.getLongClickItem();
        if(item.getTitle().equals(getResources().getString(R.string.open))) {
            //Open media
            openMedia(file.getContentType(), file.getUrl(), file.getDisplayName());
        } else if (item.getTitle().equals(getResources().getString(R.string.download))) {
            if(PermissionUtils.hasPermissions(getActivity(), PermissionUtils.WRITE_EXTERNAL_STORAGE)) {
                //Download media
                DownloadMedia.downloadMedia(getContext(), file.getUrl(), file.getDisplayName(), file.getDisplayName());
            } else {
                requestPermissions(PermissionUtils.makeArray(PermissionUtils.WRITE_EXTERNAL_STORAGE), PermissionUtils.WRITE_FILE_PERMISSION_REQUEST_CODE);
            }
        } else if (item.getTitle().equals(getResources().getString(R.string.delete))) {
            FileFolderAPI.deleteFile(file.getId(), new CanvasCallback<Response>(this) {
                @Override
                public void firstPage(Response response, LinkHeaders linkHeaders, Response response2) {
                    mRecyclerAdapter.remove(file);
                    // Mark to be removed from the cache.
                    mRecyclerAdapter.getDeletedFileFolders().add(file);
                }
            });
        } else if (item.getTitle().equals(getResources().getString(R.string.openAlternate))) {
            //Open media with outside apps
            openMedia(file.getContentType(), file.getUrl(), file.getDisplayName(), true);
        }
        return true;
    }
    //endregion

    public void configureViews(View rootView) {
        registerForContextMenu(mRecyclerView);
        folderNameTextView = (TextView) rootView.findViewById(R.id.folderName);
    }

    private void setRootFolderName() {
        if(getCanvasContext().getType() == CanvasContext.Type.COURSE) {
            folderNameTextView.setText(getString(R.string.rootCourse));
        }  else if (getCanvasContext().getType() == CanvasContext.Type.GROUP){
            folderNameTextView.setText(getString(R.string.rootGroup));
        } else {
            folderNameTextView.setText(getString(R.string.rootUser));
        }
    }

    private void setUpCallbacks(){
        mLongClickCallback = new AdapterToFragmentLongClick<FileFolder>() {
            @Override
            public void onRowClicked(FileFolder fileFolder, int position, boolean isOpenDetail) {
                if (fileFolder.getFullName() != null) {
                    Navigation navigation = getNavigation();
                    if(navigation != null) {
                        Bundle bundle = createBundle(getCanvasContext(), fileFolder.getId(), fileFolder.getName());
                        navigation.addFragment(FragUtils.getFrag(FileListFragment.class, bundle));
                    }

                } else {
                    openMedia(fileFolder.getContentType(), fileFolder.getUrl(), fileFolder.getDisplayName());
                }
            }

            @Override
            public void onRefreshFinished() {
                setRefreshing(false);
            }

            @Override
            public void onRowLongClicked(FileFolder fileFolder, int position) {
                if(!CanvasRestAdapter.isNetworkAvaliable(getContext())) {
                    Toast.makeText(getContext(), getContext().getString(R.string.notAvailableOffline), Toast.LENGTH_SHORT).show();
                } else {
                    mRecyclerAdapter.setLongClickItem(fileFolder);
                }
            }
        };
    }
    //endregion

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if(requestCode == PermissionUtils.WRITE_FILE_PERMISSION_REQUEST_CODE) {
            if(PermissionUtils.permissionGranted(permissions, grantResults, PermissionUtils.WRITE_EXTERNAL_STORAGE)) {
                Toast.makeText(getActivity(), R.string.filePermissionGranted, Toast.LENGTH_LONG).show();
            } else {
                Toast.makeText(getActivity(), R.string.filePermissionDenied, Toast.LENGTH_LONG).show();
            }
        }
    }

    //region Intent
    @Override
    public void handleIntentExtras(Bundle extras) {
        super.handleIntentExtras(extras);
        currentFolderId = extras.getLong(Const.FOLDER_ID);
        currentFolderName = extras.getString(Const.FOLDER_NAME);
    }

    public static Bundle createBundle(CanvasContext canvasContext, long folderId, String folderName) {
        Bundle extras = createBundle(canvasContext);
        extras.putLong(Const.FOLDER_ID, folderId);
        extras.putString(Const.FOLDER_NAME, folderName);
        return extras;
    }
    //endregion

    @Override
    public boolean allowBookmarking() {
        return (getCanvasContext().getType() == CanvasContext.Type.COURSE || getCanvasContext().getType() == CanvasContext.Type.GROUP);
    }
}
