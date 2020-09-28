package com.hutchsystems.hutchconnect.tasks;

import android.os.AsyncTask;

import com.hutchsystems.hutchconnect.common.PostCall;
import com.hutchsystems.hutchconnect.common.Utility;

public class AutoSyncData extends AsyncTask<String, Void, Boolean> {
    String TAG = AutoSyncData.class.getName();

    private PostTaskListener<Boolean> postTaskListener;

    public AutoSyncData(PostTaskListener<Boolean> postTaskListener) {
        this.postTaskListener = postTaskListener;
    }

    @Override
    protected Boolean doInBackground(String... param) {
        //LogFile.write(TAG + "::AutoSyncData: Start", LogFile.AUTOMATICALLY_TASK, LogFile.AUTOSYNC_LOG);
        boolean status = false;
        if (!Utility.isInternetOn()) {
            return status;
        }

        status = PostCall.LogPostAll();


        if (status) {

            Utility.savePreferences("InternetConnectedDate", Utility.getCurrentDateTime());
            /*  status = GetCall.DailyLogEditRequestSync();*/
            /* if (status) {*/
            status = PostCall.PostAccount();
            if (status) {
                PostCall.PostDVIR();

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
                //  PostCall.PostLogs();

            }

            /* }*/
        }
        return status;
    }

    @Override
    protected void onPreExecute() {
        // showMainView(false);
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



