package com.hutchsystems.hutchconnect.tasks;

import android.os.AsyncTask;

import com.hutchsystems.hutchconnect.common.Utility;
import com.hutchsystems.hutchconnect.db.DocumentDB;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.File;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.HttpURLConnection;
import java.net.URL;

import static com.hutchsystems.hutchconnect.common.WebUrl.FILE_UPLOAD_URL;

public class UploadFileToServer extends AsyncTask<String, Integer, Boolean> {
    long totalSize = 0;
    IFileProgress mListner;
    boolean newFg = false;
    String path;
    String type;

    public UploadFileToServer(IFileProgress mListner, boolean isNew) {
        this.mListner = mListner;
        this.newFg = isNew;
    }

    @Override
    protected void onPreExecute() {
        mListner.onUploadStart();
        super.onPreExecute();
    }

    @Override
    protected void onProgressUpdate(Integer... progress) {
        mListner.onFileUploading(progress[0]);
    }

    @Override
    protected Boolean doInBackground(String... params) {
        type = params[0];
        path = params[1];
        return uploadFile();
    }

    // Created By: Deepak Sharma
    // Created Date: 18 March 2020
    // post output file to server with email or Webservice
    public boolean uploadFile() {
        String response = null;
        boolean status = false;
        URL url;
        HttpURLConnection urlConnection = null;
        PrintWriter printWriter = null;
        try {

            // url of the web service
            url = new URL(FILE_UPLOAD_URL);
            // open connection
            urlConnection = (HttpURLConnection) url.openConnection();

            File sourceFile = new File(path);

            // extract file name from file path
            String filename = path.substring(path.lastIndexOf("/") + 1);
            // we have to post data so use POST as method
            urlConnection.setRequestMethod("POST");

            // set file name in header of request
            urlConnection.setRequestProperty("filename", filename);

            // set type of the file in header
            urlConnection.setRequestProperty("type", type);

            // start content wrapper
            DataOutputStream request = new DataOutputStream(urlConnection.getOutputStream());

            request.write(Utility.convertToByte(sourceFile));

            request.flush();
            request.close();
            InputStream responseStream = new
                    BufferedInputStream(urlConnection.getInputStream());

            BufferedReader responseStreamReader =
                    new BufferedReader(new InputStreamReader(responseStream));

            String line = "";
            StringBuilder stringBuilder = new StringBuilder();

            while ((line = responseStreamReader.readLine()) != null) {
                stringBuilder.append(line).append("\n");
            }
            responseStreamReader.close();

            response = stringBuilder.toString();
            status = true;

        } catch (Exception exe) {
            exe.printStackTrace();
        } finally {
            if (printWriter != null)
                printWriter.close();
            if (urlConnection != null)
                urlConnection.disconnect();

        }
        return status;
    }

    @Override
    protected void onPostExecute(Boolean status) {
        mListner.onFileUploaded(status);
        if (newFg && !status) {
            DocumentDB.Save(type, path);
        }
        super.onPostExecute(status);
    }

    public interface IFileProgress {
        void onFileUploading(int progress);

        void onFileUploaded(boolean status);

        void onUploadStart();
    }
}