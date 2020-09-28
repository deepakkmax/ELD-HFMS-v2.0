package com.hutchsystems.hutchconnect.tasks;

import android.os.AsyncTask;
import android.os.Environment;
import android.util.Log;

import com.hutchsystems.hutchconnect.common.Mail;
import com.hutchsystems.hutchconnect.common.Utility;

import java.io.File;

/**
 * Created by Deepak Sharma on 3/9/2017.
 */

public class ReportIssue extends AsyncTask<String, Void, Boolean> {
    String TAG = "ReportIssue";
    IMailProgress mListner;

    public ReportIssue(IMailProgress mListner) {
        this.mListner = mListner;
    }

    @Override
    protected Boolean doInBackground(String... param) {
        boolean status;
        String dbName = param[2];
        String filePath = Environment.getExternalStorageDirectory() + "/" + dbName; //LogFile.DATABASE_BACKUP_NAME;
        try {
            File file = new File(filePath);
            if (!file.exists()) {
                return true;
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
        m.setBody(content);

        try {
            m.addAttachment(filePath);
        } catch (Exception e) {

        }


        try {
            if (m.send()) {
                Log.i(TAG, "Email was sent successfully");
                status = true;

                /*File file = new File(filePath);
                if (file.exists()) {
                    file.delete();
                }*/

            } else {
                status = false;
            }
        } catch (Exception e) {
            //Toast.makeText(MailApp.this, "There was a problem sending the email.", Toast.LENGTH_LONG).show();
            Log.i(TAG, "Could not send mail" + e.getMessage());
            status = false;
        }
        return status;
    }

    @Override
    protected void onPreExecute() {
        super.onPreExecute();
    }

    @Override
    protected void onPostExecute(Boolean aBoolean) {
        super.onPostExecute(aBoolean);
        mListner.onMailSent(aBoolean);
    }

    public interface IMailProgress {

        void onMailSent(boolean status);

    }
}
