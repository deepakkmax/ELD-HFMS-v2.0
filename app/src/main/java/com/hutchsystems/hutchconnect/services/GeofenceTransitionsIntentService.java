package com.hutchsystems.hutchconnect.services;

import android.app.IntentService;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.os.Build;

import androidx.annotation.RequiresApi;
import androidx.core.app.NotificationCompat;

import com.google.android.gms.location.Geofence;
import com.google.android.gms.location.GeofencingEvent;
import com.hutchsystems.hutchconnect.MainActivity;
import com.hutchsystems.hutchconnect.R;
import com.hutchsystems.hutchconnect.beans.GeofenceBean;
import com.hutchsystems.hutchconnect.common.Utility;
import com.hutchsystems.hutchconnect.db.DrayageDispatchDB;
import com.hutchsystems.hutchconnect.db.GeofenceDB;

import java.util.List;

/**
 * Created by Deepak Sharma on 5/10/2017.
 */

public class GeofenceTransitionsIntentService extends IntentService {
    protected static final String TAG = "GeofenceTransitionsIS";

    public GeofenceTransitionsIntentService() {
        super(TAG);
    }

    @RequiresApi(api = Build.VERSION_CODES.O)
    protected void onHandleIntent(Intent intent) {
        GeofencingEvent geofencingEvent = GeofencingEvent.fromIntent(intent);
        if (geofencingEvent.hasError()) {

            return;
        }
        if (Utility.vehicleId == 0)
            return;

        // Get the transition type.
        int geofenceTransition = geofencingEvent.getGeofenceTransition();

        // Test that the reported transition was of interest.
        if (geofenceTransition == Geofence.GEOFENCE_TRANSITION_ENTER ||
                geofenceTransition == Geofence.GEOFENCE_TRANSITION_EXIT) {
            int regionInFg = geofenceTransition == Geofence.GEOFENCE_TRANSITION_ENTER ? 1 : 0;
            if (regionInFg == 1) {
                showNotification("Entered", "Entered the Location");
            } else
                showNotification("Exited", "Exited the Location");

            String date = Utility.getCurrentDateTime();
            int driverId = Utility.activeUserId;
            if (driverId == 0) {
                driverId = Utility.unIdentifiedDriverId;
            }
            // Get the geofences that were triggered. A single event can trigger
            // multiple geofences.
            List<Geofence> triggeringGeofences = geofencingEvent.getTriggeringGeofences();
            try {
                for (Geofence fence : triggeringGeofences) {
                    if (Utility.hasFeature("15")) {
                        String[] key = fence.getRequestId().split("-");
                        int pickDropId = Integer.parseInt(key[2]);
                        int dispatchId = Integer.parseInt(key[1]);
                        DrayageDispatchDB.SaveRoute(date, pickDropId, dispatchId, regionInFg);
                    } else {
                        GeofenceBean bean = new GeofenceBean();
                        int portId = Integer.parseInt(fence.getRequestId());
                        bean.setPortId(portId);
                        bean.setRegionInFg(regionInFg);
                        bean.setEnterDate(date);
                        bean.setExitDate(date);
                        bean.setEnterByDriverId(driverId);
                        bean.setExitByDriverId(driverId);
                        bean.setVehicleId(Utility.vehicleId);
                        GeofenceDB.GeofenceMonitorSave(bean);
                    }
                }
            } catch (Exception e) {
            }

            if (mListner != null) {
                mListner.refresh();
            }

        } else {
        }
    }

    @androidx.annotation.RequiresApi(api = Build.VERSION_CODES.O)
    public void showNotification(String text, String bigText) {

        // 1. Create a NotificationManager
        NotificationManager notificationManager =
                (NotificationManager) this.getSystemService(Context.NOTIFICATION_SERVICE);

        // 2. Create a PendingIntent for AllGeofencesActivity
        Intent intent = new Intent(this, MainActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP | Intent.FLAG_ACTIVITY_CLEAR_TOP);
        PendingIntent pendingNotificationIntent = PendingIntent.getActivity(this, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT);
        NotificationCompat.Builder notificationBuilder = new NotificationCompat.Builder(this, "");
        // 3. Create and send a notification
        notificationBuilder.setSmallIcon(R.drawable.ic_launcher)
                .setContentTitle(text)
                .setContentText(text)
                .setContentIntent(pendingNotificationIntent)
                .setStyle(new NotificationCompat.BigTextStyle().bigText(bigText))
                .setPriority(NotificationCompat.PRIORITY_HIGH)
                .setAutoCancel(true);
        notificationManager.notify(0, notificationBuilder.build());
/*
        Notification.Builder builder = null;
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
            int importance = NotificationManager.IMPORTANCE_DEFAULT;
            NotificationChannel notificationChannel = new NotificationChannel("my_channel_id_01", "eld", importance);
            notificationManager.createNotificationChannel(notificationChannel);
            builder = new Notification.Builder(getApplicationContext(), notificationChannel.getId());
        } else {
            builder = new Notification.Builder(getApplicationContext(),"my_channel_id_01");
        }

        builder.setSmallIcon(R.mipmap.ic_launcher)
                .setContentTitle(text)
                .setContentText(text)
                .setContentIntent(pendingNotificationIntent)
                .setStyle(new NotificationCompat.BigTextStyle().bigText(bigText))
                .setPriority(NotificationCompat.PRIORITY_HIGH)
                .setAutoCancel(true);
        notificationManager.notify(0, builder.build());*/
    }

    public static IGeofence mListner;

    public interface IGeofence {
        void refresh();
    }
}