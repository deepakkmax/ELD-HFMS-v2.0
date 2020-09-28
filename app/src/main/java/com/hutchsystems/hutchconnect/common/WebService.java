package com.hutchsystems.hutchconnect.common;

import android.os.StrictMode;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.HttpURLConnection;
import java.net.URL;

import static com.hutchsystems.hutchconnect.common.WebUrl.SEND_OUTPUT_FILE_TO_FMCSA;

public class WebService {

    // Created By: Deepak Sharma
    // Created Date: 18 March 2020
    // Purpose: get data from service
    public String doGet(String serviceUrl) {

        String response = null;

        URL url;

        HttpURLConnection urlConnection = null;

        try {
            // url of the web service
            url = new URL(serviceUrl.replace(" ", "%20"));

            StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitAll().build();

            StrictMode.setThreadPolicy(policy);

            // open connection
            urlConnection = (HttpURLConnection) url
                    .openConnection();

            // get input stream instance
            InputStream in = urlConnection.getInputStream();

            InputStreamReader inputStream = new InputStreamReader(in);
            // write response in this object
            ByteArrayOutputStream outputStream = new ByteArrayOutputStream();

            int data;

            while ((data = inputStream.read()) != -1) {
                outputStream.write(data);
            }

            response = outputStream.toString();
        } catch (Exception exe) {
            exe.printStackTrace();
        } finally {
            if (urlConnection != null) {
                urlConnection.disconnect();
            }
        }

        return response;
    }

    // Created By: Deepak Sharma
    // Created Date: 18 March 2020
    // Purpose: post data from service
    public String doPost(String serviceUrl, String data) {

        String response = null;
        URL url;
        HttpURLConnection urlConnection = null;
        PrintWriter printWriter = null;

        try {

            // url of the web service
            url = new URL(serviceUrl);

            // open connection
            urlConnection = (HttpURLConnection) url.openConnection();

            // we have to post data so use POST as method
            urlConnection.setRequestMethod("POST");

            // content type to be posted
            urlConnection.setRequestProperty("Content-Type", "application/json");

            // get instance of print writer
            printWriter = new PrintWriter(urlConnection.getOutputStream());

            // write to output stream using print writer
            printWriter.write(data);

            // close print writer instance
            printWriter.close();

            // get input stream instance
            InputStream inputStream = urlConnection.getInputStream();

            // write response in this object
            ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
            int i;
            while ((i = inputStream.read()) != -1) {
                outputStream.write(i);
            }
            response = outputStream.toString();

        } catch (Exception exe) {
            exe.printStackTrace();
        } finally {
            if (printWriter != null)
                printWriter.close();
            if (urlConnection != null)
                urlConnection.disconnect();

        }
        return response;
    }

    // Created By: Deepak Sharma
    // Created Date: 18 March 2020
    // post output file to server with email or Webservice
    public String PostOutputFileWithSendOption(String path, String comment, String type, int contentLength, int sendOption) {
        String response = null;
        URL url;
        HttpURLConnection urlConnection = null;
        PrintWriter printWriter = null;
        try {
            String boundary = "********";
            String crlf = "\r\n";
            String twoHyphens = "--";

            // url of the web service
            url = new URL(SEND_OUTPUT_FILE_TO_FMCSA);
            // open connection
            urlConnection = (HttpURLConnection) url.openConnection();

            File sourceFile = new File(path);

            // extract file name from file path
            String filename = path.substring(path.lastIndexOf("/") + 1);
            // we have to post data so use POST as method
            urlConnection.setRequestMethod("POST");

            urlConnection.setRequestProperty(
                    "Content-Type", "multipart/form-data;boundary=" + boundary);

            // set file name in header of request
            urlConnection.setRequestProperty("filename", filename);

            // set type of the file in header
            urlConnection.setRequestProperty("type", type);

            // set comment in header of request
            urlConnection.setRequestProperty("comment", comment);

            // set length of the content in header. it will be used in web service to separate multipart entity stream from unnecassary headers
            urlConnection.setRequestProperty("length", contentLength + "");

            // set sendOption in header of request
            urlConnection.setRequestProperty("sendOption", sendOption + "");

            // start content wrapper
            DataOutputStream request = new DataOutputStream(urlConnection.getOutputStream());

            request.writeBytes(twoHyphens + boundary + crlf);
            request.writeBytes("Content-Disposition: form-data; name=\"" + filename.replace(".csv", "") + "\";filename=\"" + filename + "\"" + crlf);
            request.writeBytes(crlf);

            request.write(Utility.convertToByte(sourceFile));

            request.writeBytes(crlf);
            request.writeBytes(twoHyphens + boundary + twoHyphens + crlf);
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


        } catch (Exception exe) {
            exe.printStackTrace();
        } finally {
            if (printWriter != null)
                printWriter.close();
            if (urlConnection != null)
                urlConnection.disconnect();

        }
        return response;
    }


    // Created By: Deepak Sharma
    // Created Date: 18 March 2020
    // Purpose: get data from service
    public boolean downloadFile(String serverPath, String clientPath) {

        boolean response = true;

        URL url;

        HttpURLConnection urlConnection = null;

        try {
            // url of the web service
            url = new URL(serverPath);

            // open connection
            urlConnection = (HttpURLConnection) url
                    .openConnection();

            // get input stream instance
            InputStream in = urlConnection.getInputStream();

            // write response in this object
            FileOutputStream f = new FileOutputStream(new File(clientPath));

            byte[] buffer = new byte[1024];
            int data = 0;
            while ((data = in.read(buffer)) > 0) {
                f.write(buffer, 0, data);
            }
            f.close();
            in.close();
        } catch (Exception exe) {
            exe.printStackTrace();
            response = false;
        } finally {
            if (urlConnection != null) {
                urlConnection.disconnect();
            }
        }

        return response;
    }

}
