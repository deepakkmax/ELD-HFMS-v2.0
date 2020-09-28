package com.hutchsystems.hutchconnect.tasks;

import android.os.AsyncTask;

import com.hutchsystems.hutchconnect.common.PostCall;

public class PostDailyLog extends AsyncTask<String, Void, Boolean> {
    String TAG = PostData.class.getName();

    private PostTaskListener<Boolean> postTaskListener;

    public PostDailyLog(PostTaskListener<Boolean> postTaskListener) {
        this.postTaskListener = postTaskListener;
    }

    @Override
    protected Boolean doInBackground(String... param) {
        String logId = param[0];
        boolean status = PostCall.postDailyLogById(Integer.parseInt(logId));
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
