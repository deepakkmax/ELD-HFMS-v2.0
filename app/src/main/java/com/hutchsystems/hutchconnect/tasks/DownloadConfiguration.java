package com.hutchsystems.hutchconnect.tasks;

import android.os.AsyncTask;

import com.hutchsystems.hutchconnect.common.GetCall;

public class DownloadConfiguration extends AsyncTask<String, Void, Boolean> {
    String TAG = DownloadConfiguration.class.getName();

    private PostTaskListener<Boolean> postTaskListener;

    public DownloadConfiguration(PostTaskListener<Boolean> postTaskListener) {
        this.postTaskListener = postTaskListener;
    }

    @Override
    protected Boolean doInBackground(String... param) {

        boolean status = true;
        try {

            //GetCall.DeviceBalanceGet();
            GetCall.CarrierInfoSync();
            GetCall.AccountSync(0);
            GetCall.CardDetailGetSync();
        } catch (Exception exe) {
            status = false;
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



