package com.hutchsystems.hutchconnect.tasks;

import android.os.AsyncTask;

import com.hutchsystems.hutchconnect.common.GetCall;

public class LogEventSync extends AsyncTask<String, Void, Boolean> {

    private PostTaskListener<Boolean> postTaskListener;

    public LogEventSync(PostTaskListener<Boolean> postTaskListener) {
        this.postTaskListener = postTaskListener;
    }

    @Override
    protected Boolean doInBackground(String... param) {
        String lastEventDate = param[0];
        boolean result = GetCall.DailyLogEventGetSync(lastEventDate);
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

