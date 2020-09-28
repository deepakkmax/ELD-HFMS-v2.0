package com.hutchsystems.hutchconnect.tasks;

import android.os.AsyncTask;
import android.os.Environment;
import android.util.Log;

import com.hutchsystems.hutchconnect.common.LogFile;
import com.hutchsystems.hutchconnect.common.Mail;
import com.hutchsystems.hutchconnect.common.Utility;

import java.io.File;
import java.util.Calendar;

public class MailSync extends AsyncTask<String, Void, Boolean> {
    String TAG = MailSync.class.getName();

    private PostTaskListener<Boolean> postTaskListener;
    private boolean attachmentUsed;
    public boolean ELD2020DataFg = false;

    public MailSync(final PostTaskListener<Boolean> postTaskListener, boolean attachment) {
        this.postTaskListener = postTaskListener;
        attachmentUsed = attachment;
    }


    public MailSync() {
        this.postTaskListener = postTaskListener;
        attachmentUsed = true;
        ELD2020DataFg=true;
    }



    @Override
    protected Boolean doInBackground(String... param) {
        boolean status;

        String filePath = getLogFilePath(LogFile.ERROR_LOG);

        // if data from Hutch Connect is to send
        if (ELD2020DataFg) {
            String path = "/HutchConnectLogs/" + "eldhutchconnect-data-" + Utility.getCurrentDate() + ".txt";

            if (isExternalStorageWritable()) {
                path = Environment.getExternalStorageDirectory() + path;
            }


            filePath = path;
        }

        try {
            if (attachmentUsed) {
                File file = new File(filePath);
                if (!file.exists()) {
                    return true;
                }
            }
        } catch (Exception exe) {
        }

        Mail m = new Mail(Utility.DEV_EMAIL, "#72Such5");

        String[] toArr = {Utility.DEV_EMAIL}; //{"support@hutchsystems.com"};
        m.setTo(toArr);
        m.setFrom(Utility.DEV_EMAIL);
        String subject = param[0];
        m.setSubject(subject);

        String content = param[1];
        m.setBody("From IMEI:" + Utility.IMEI + "\n" + content);

        if (attachmentUsed) {
            try {
                m.addAttachment(filePath);
            } catch (Exception e) {
                //Toast.makeText(MailApp.this, "There was a problem sending the email.", Toast.LENGTH_LONG).show();
                Log.i(TAG, "Could not attach file" + e.getMessage());
            }
        }

        try {
            if (m.send()) {
                //Toast.makeText(MailApp.this, "Email was sent successfully.", Toast.LENGTH_LONG).show();
                Log.i(TAG, "Email was sent successfully");
                status = true;

                File file = new File(filePath);
                if (file.exists()) {
                    file.delete();
                }

            } else {
                //Toast.makeText(MailApp.this, "Email was not sent.", Toast.LENGTH_LONG).show();
                Log.i(TAG, "Email was not sent");
                status = false;
            }
        } catch (Exception e) {
            //Toast.makeText(MailApp.this, "There was a problem sending the email.", Toast.LENGTH_LONG).show();
            Log.i(TAG, "Could not send mail" + e.getMessage());
            status = false;
        }
        return status;

    }

    public boolean isExternalStorageWritable() {
        String state = Environment.getExternalStorageState();
        if (Environment.MEDIA_MOUNTED.equals(state)) {
            return true;
        }
        return false;
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

    public String getLogFilePath(int logType) {
        String path = "";
        String logName = "";
        switch (logType) {
            case LogFile.ERROR_LOG:
                logName = LogFile.LOG_ERROR_NAME;
                break;
            case LogFile.CANBUS_LOG:
                logName = LogFile.LOG_CANBUS_NAME + "-" + Utility.getCurrentDate() + "-" + Calendar.getInstance().get(Calendar.HOUR_OF_DAY);
                break;
            case LogFile.NOLOGIN_LOG:
                logName = LogFile.LOG_NOLOGIN_NAME;
                break;
            case LogFile.DRIVEREVENT_LOG:
                logName = LogFile.LOG_DRIVEREVENT_NAME;
                break;
            case LogFile.AUTOSYNC_LOG:
                logName = LogFile.LOG_AUTOSYNC_NAME;
                break;
            case LogFile.TCPCLIENT_LOG:
                logName = LogFile.LOG_TCPCLIENT_NAME;
                break;
            case LogFile.GPS_LOG:
                logName = LogFile.LOG_GPS_NAME;
                break;
            case LogFile.DATABASE_BACKUP:
                logName = LogFile.DATABASE_BACKUP_NAME;
                break;
        }

        if (logType != LogFile.DATABASE_BACKUP)
            logName += LogFile.LOG_EXTENSION;
        if (isExternalStorageWritable()) {
            path = Environment.getExternalStorageDirectory() + "/" + logName;
        } else {
            path = logName;
        }
        return path;
    }

    public interface PostTaskListener<K> {
        void onPostTask(K result);
    }
}



