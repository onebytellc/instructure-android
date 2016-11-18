package com.instructure.loginapi.login.asynctasks;

import android.content.Context;
import android.os.AsyncTask;

import com.instructure.loginapi.login.api.GlobalDataSyncAPI;
import com.instructure.loginapi.login.model.GlobalDataSync;

public class GlobalDataSyncPostTask extends AsyncTask<GlobalDataSync,Void,Void>{

    private GlobalDataSyncAPI.NAMESPACE mNamespace;
    private Context mContext;

    public GlobalDataSyncPostTask(Context context, GlobalDataSyncAPI.NAMESPACE namespace) {
        mContext = context;
        mNamespace = namespace;
    }

    @Override
    protected Void doInBackground(GlobalDataSync... params) {

        GlobalDataSyncAPI.setGlobalData(mContext, mNamespace, params[0]);
        GlobalDataSync.setCachedGlobalData(mContext, params[0], mNamespace);

        return null;
    }
}
