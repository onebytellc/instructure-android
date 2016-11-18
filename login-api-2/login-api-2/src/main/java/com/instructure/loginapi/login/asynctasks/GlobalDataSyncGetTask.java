package com.instructure.loginapi.login.asynctasks;

import android.content.Context;
import android.os.AsyncTask;

import com.instructure.loginapi.login.api.GlobalDataSyncAPI;
import com.instructure.loginapi.login.model.GlobalDataSync;
import com.instructure.loginapi.login.util.Utils;

public class GlobalDataSyncGetTask extends AsyncTask<Void,Void,GlobalDataSync> {

    private Context mContext;
    private GlobalDataSyncCallbacks mCallback;
    private GlobalDataSyncAPI.NAMESPACE mNamespace;

    public interface GlobalDataSyncCallbacks {
        public void globalDataResults(GlobalDataSync data);
    }

    public GlobalDataSyncGetTask(Context context, GlobalDataSyncCallbacks callback, GlobalDataSyncAPI.NAMESPACE namespace) {
        mContext = context;
        mCallback = callback;
        mNamespace = namespace;
    }

    @Override
    protected GlobalDataSync doInBackground(Void... params) {

        GlobalDataSync data = GlobalDataSyncAPI.getGlobalData(mContext, mNamespace);

        if(data != null) {
            Utils.d(data.toString());
            //Evey time we get the data cache it.
            GlobalDataSync.setCachedGlobalData(mContext, data, mNamespace);
        }

        return data;
    }

    @Override
    protected void onPostExecute(GlobalDataSync data) {
        super.onPostExecute(data);

        if(mCallback != null) {
            mCallback.globalDataResults(data);
        }
    }
}
