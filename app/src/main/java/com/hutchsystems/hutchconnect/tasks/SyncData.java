package com.hutchsystems.hutchconnect.tasks;

import android.os.AsyncTask;
import android.util.Log;

import com.hutchsystems.hutchconnect.common.GetCall;
import com.hutchsystems.hutchconnect.common.PostCall;
import com.hutchsystems.hutchconnect.common.Utility;

public class SyncData extends AsyncTask<String, Void, Boolean> {
    String TAG = SyncData.class.getName();

    private PostTaskListener<Boolean> postTaskListener;

    public SyncData(PostTaskListener<Boolean> postTaskListener) {
        this.postTaskListener = postTaskListener;
    }

    @Override
    protected Boolean doInBackground(String... param) {
        Log.i(TAG, "Sync data");
        if (!Utility.isInternetOn())
            return false;

        PostCall.LogPostAll();

        boolean status = GetCall.CarrierInfoSync();
        if (status) {
            status = GetCall.AccountSync(0);
            if (status) {
                status = GetCall.TrailerInfoGetSync();
                if (status) {

                    // get Annotation
                    GetCall.AnnotationInfoGetSync();
                    GetCall.CardDetailGetSync();
                    if (status && Utility.onScreenUserId > 0) {
                        String fromDate = Utility.getPreviousDateOnly(-15);
                        String date = fromDate + " 00:00:00";
                        GetCall.DailyLogEventGetSync(date);

                        // sync rule from web
                        GetCall.DailylogRuleGetSync(fromDate, date);

                        // sync codriver from web
                        GetCall.DailylogCoDriverGetSync(fromDate, date);

                        // last 2 days record
                        fromDate = Utility.getPreviousDateOnly(-1);
                        // sync DVIR from web for last 2 days only
                        GetCall.DVIRGetSync(fromDate);
                        // Sync CTPAT Inspection
                        //GetCall.CTPATGetSync(fromDate);

                        //sync training document
                        GetCall.DocumentGetSync();

                       /* fromDate = Utility.getPreviousDateOnly(-15);
                        date = fromDate + " 00:00:00";
                        GetCall.DailyLogEventGetSync(date);*/

                       status= GetCall.ScheduleGetSync();

                       if (status)
                       {
                           status = PostCall.POSTScheduleSYNC();
                       }
                    }

                   /* if (Utility.packageId > 2)
                        GetCall.GeofenceGetSync();*/
                }
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