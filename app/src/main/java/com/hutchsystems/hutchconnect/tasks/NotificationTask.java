package com.hutchsystems.hutchconnect.tasks;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import androidx.core.app.NotificationCompat;

import com.hutchsystems.hutchconnect.MainActivity;
import com.hutchsystems.hutchconnect.R;
import com.hutchsystems.hutchconnect.beans.NotificationBean;
import com.hutchsystems.hutchconnect.common.GetCall;
import com.hutchsystems.hutchconnect.common.Utility;
import com.hutchsystems.hutchconnect.db.NotificationDB;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;

public class NotificationTask extends AsyncTask<String, Void, String> {


    @Override
    protected String doInBackground(String... strings) {
        String result = GetCall.getNotification();

        return result;
    }

    @Override
    protected void onPostExecute(String result) {
        super.onPostExecute(result);
        try {
            if (result!=null && !result.isEmpty()) {

                JSONArray obj = new JSONArray(result);
                ArrayList<NotificationBean> al = new ArrayList<>();
                for (int i = 0; i < obj.length(); i++) {
                    JSONObject json = obj.getJSONObject(i);
                    NotificationBean bean = new NotificationBean();
                    bean.setComment(json.getString("Comment"));
                    bean.setNotiFicationDate(json.getString("NotificationDate"));
                    bean.setNotificationID(json.getInt("NotificationId"));
                    bean.setTitle(json.getString("Title"));
                    // show notification
                    showNotification(bean);
                    al.add(bean);
                }

                if (al.size() > 0) {
                    // Save notification in database
                    NotificationDB.Save(al);
                }
            }

        }catch (Exception e)
        {
            e.printStackTrace();
        }

    }
    // Created By: Pallavi Wattamwar
    // Created Date: 30 July 2018
    // Purpose: Show Notification
    public static void showNotification(NotificationBean bean) {


        Intent resultIntent = new Intent(Utility.context, MainActivity.class);
        Bundle b = new Bundle();
        b.putString("intentFrom","Notification");
        resultIntent.putExtras(b);


        PendingIntent pendingIntent = PendingIntent.getActivity(Utility.context, 1, resultIntent,PendingIntent.FLAG_UPDATE_CURRENT);

        NotificationCompat.Builder mBuilder =
                new NotificationCompat.Builder(Utility.context)
                        .setSmallIcon(R.drawable.ic_launcher)
                        .setContentTitle(bean.getTitle())
                        .setContentText(bean.getComment());

        mBuilder.setContentIntent(pendingIntent);


        NotificationManager mNotifyMgr =
                (NotificationManager) (Utility.context).getSystemService(Utility.context.NOTIFICATION_SERVICE);
        mBuilder.setDefaults(Notification.DEFAULT_SOUND);
        mBuilder.setAutoCancel(true);
        mNotifyMgr.notify(bean.getNotificationID(), mBuilder.build());


    }
}
