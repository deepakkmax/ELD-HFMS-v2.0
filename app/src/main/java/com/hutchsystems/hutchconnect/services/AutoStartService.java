package com.hutchsystems.hutchconnect.services;

import android.app.ActivityManager;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.os.IBinder;
import android.os.PowerManager;
import android.util.Log;

import com.hutchsystems.hutchconnect.MainActivity;
import com.hutchsystems.hutchconnect.common.LogFile;
import com.hutchsystems.hutchconnect.common.Utility;
import com.hutchsystems.hutchconnect.db.LogDB;

import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

public class AutoStartService extends Service {
    private static final String TAG = AutoStartService.class.getName();
    private static final int INTERVAL = 1 * 60 * 1000; //90000; // poll every 2 minute
    private static final int INITIAL_INTERVAL = 2 * 60 * 1000; // starts after 2 minute
    private static final String APP_PACKAGE_NAME = "com.hutchgroup.e_log";

    public static boolean stopTask, pauseTask;
    private PowerManager.WakeLock mWakeLock;
    boolean pmode_scrn_on = true;

    @Override
    public void onCreate() {
        super.onCreate();
        stopTask = false;
        pauseTask = false;
        // Screen will never switch off this way
        mWakeLock = null;
        if (pmode_scrn_on) {
            PowerManager pm = (PowerManager) getSystemService(Context.POWER_SERVICE);
            mWakeLock = pm.newWakeLock(PowerManager.SCREEN_DIM_WAKE_LOCK, "a_tag");
            mWakeLock.acquire();
        }

        // Start your (polling) task
        TimerTask task = new TimerTask() {
            @Override
            public void run() {
                try {
                    // If you wish to stop the task/polling
                    if (stopTask) {
                        this.cancel();
                    }

                    if (!stopTask && !pauseTask) {
                        Log.i("AutoStart", "check app is foreground or not");
                        // The first in the list of RunningTasks is always the foreground task.
                        ActivityManager am = (ActivityManager) getApplicationContext().getSystemService(Context.ACTIVITY_SERVICE);
                        List<ActivityManager.RunningAppProcessInfo> appProcesses = am.getRunningAppProcesses();

                        if (appProcesses == null)
                            return;

                        for (ActivityManager.RunningAppProcessInfo appProcess : appProcesses) {

                            if (appProcess.processName.equals(APP_PACKAGE_NAME) && appProcess.importance != ActivityManager.RunningAppProcessInfo.IMPORTANCE_FOREGROUND
                                    && appProcess.importance != ActivityManager.RunningAppProcessInfo.IMPORTANCE_TOP_SLEEPING) {

                                Intent LaunchIntent = new Intent(getApplicationContext(), MainActivity.class);
                                LaunchIntent.setAction(Intent.ACTION_MAIN);
                                LaunchIntent.addCategory(Intent.CATEGORY_LAUNCHER);
                                LaunchIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                                startActivity(LaunchIntent);
                            }
                        }
                    }
                } catch (Exception exe) {
                    String sStackTrace = exe.getMessage() + "\n" + Utility.printStackTrace(exe);
                    LogFile.write(AutoStartService.class.getName() + ":: TimerTask Error: " + sStackTrace, LogFile.ERROR_LOG, LogFile.ERROR_LOG);
                    LogDB.writeLogs(AutoStartService.class.getName(),"TimerTask Error:" ,exe.getMessage(),Utility.printStackTrace(exe));
                }

            }
        };

        Timer timer = new Timer();
        timer.scheduleAtFixedRate(task, INITIAL_INTERVAL, INTERVAL);

    }

    @Override
    public void onDestroy() {
        stopTask = true;
        if (mWakeLock != null)
            mWakeLock.release();
        super.onDestroy();
    }

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }


    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        return 0;
    }
}