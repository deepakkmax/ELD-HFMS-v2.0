package com.hutchsystems.hutchconnect.tasks;

import android.os.AsyncTask;
import android.util.Log;

import com.hutchsystems.hutchconnect.beans.EventBean;
import com.hutchsystems.hutchconnect.common.GetCall;
import com.hutchsystems.hutchconnect.common.Utility;
import com.hutchsystems.hutchconnect.db.EventDB;

public class SyncUserData extends AsyncTask<String, Void, Boolean> {
    String TAG = SyncUserData.class.getName();

    private PostTaskListener<Boolean> postTaskListener;

    public SyncUserData(PostTaskListener<Boolean> postTaskListener) {
        this.postTaskListener = postTaskListener;
    }

    @Override
    protected Boolean doInBackground(String... param) {
        Log.i(TAG, "sync data");
        boolean result = GetCall.AccountSync(Utility.onScreenUserId);
        if (result) {
            EventBean event = EventDB.previousDutyStatusGet(Utility.onScreenUserId, Utility.getCurrentDateTime());
            String fromDate = Utility.dateOnlyStringGet(event.getEventDateTime());
            result = GetCall.LogInfoSync(Utility.getCurrentDate(), Utility.getCurrentDate());
        }

        return result;
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

