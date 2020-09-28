package com.hutchsystems.hutchconnect.tasks;

import android.os.AsyncTask;

import com.hutchsystems.hutchconnect.common.PostCall;
import com.hutchsystems.hutchconnect.common.Utility;

public class PostData extends AsyncTask<String, Void, Boolean> {
    String TAG = PostData.class.getName();

    private PostTaskListener<Boolean> postTaskListener;

    public PostData(PostTaskListener<Boolean> postTaskListener) {
        this.postTaskListener = postTaskListener;
    }

    @Override
    protected Boolean doInBackground(String... param) {
        //LogFile.write(TAG + "::PostData: called by user", LogFile.AUTOMATICALLY_TASK, LogFile.AUTOSYNC_LOG);
        boolean status = false;

        if (!Utility.isInternetOn()) {
            return status;
        }

        status = PostCall.LogPostAll();
        if (status) {
            status = PostCall.PostAccount();
            if (status) {
                PostCall.PostDVIR();
                PostCall.PostDTC();
                PostCall.PostAlert();
                PostCall.PostFuelDetail();
                PostCall.PostIncidentDetail();
                PostCall.PostViolation();
                PostCall.PostGeofenceMonitor();
                PostCall.PostDocumentDetail();

                // Post Odometer source and Protocol
                PostCall.PostSetupinfo();

                PostCall.PostDeviceBalance();


                PostCall.POSTMaintenanceDueHistory();
                PostCall.POSTMaintenance();
                // Post Logs
                // PostCall.PostLogs();
            }
        }
        return status;
    }

    @Override
    protected void onPreExecute() {
        super.onPreExecute();
    }

    @Override
    protected void onPostExecute(Boolean result) {

        if (result != null && postTaskListener != null)
            postTaskListener.onPostTask(result);
    }

    public interface PostTaskListener<K> {
        void onPostTask(K result);
    }
}


