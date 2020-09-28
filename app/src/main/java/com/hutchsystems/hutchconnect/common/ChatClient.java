package com.hutchsystems.hutchconnect.common;


import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.bluetooth.BluetoothAdapter;
import android.content.Intent;
import android.os.Bundle;
import android.os.Environment;
import android.speech.tts.TextToSpeech;

import androidx.core.app.NotificationCompat;

import android.util.Log;

import com.hutchsystems.hutchconnect.MainActivity;
import com.hutchsystems.hutchconnect.R;
import com.hutchsystems.hutchconnect.beans.GPSData;
import com.hutchsystems.hutchconnect.beans.MessageBean;
import com.hutchsystems.hutchconnect.beans.NotificationBean;
import com.hutchsystems.hutchconnect.db.DailyLogDB;
import com.hutchsystems.hutchconnect.db.EventDB;
import com.hutchsystems.hutchconnect.db.LogDB;
import com.hutchsystems.hutchconnect.db.MessageDB;
import com.hutchsystems.hutchconnect.db.NotificationDB;
import com.hutchsystems.hutchconnect.db.UserDB;
import com.hutchsystems.hutchconnect.tasks.NotificationTask;
import com.hutchsystems.hutchconnect.tasks.ReportIssue;
import com.hutchsystems.hutchconnect.tasks.UploadFileToServer;

import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.File;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.Date;

/**
 * Created by Dev-1 on 4/14/2016.
 */
public class ChatClient {

    public static ChatMessageInterface mListener;

    public static INotifyDashboards iNotifyDashboards;

    public static ChatMessageReceiveIndication icListner;
    public static SwipeEvent seListner;
    public static RefreshData rListner;
    // private ChatThread mConnectedThread;
    private static Socket socket = null;
    public static final int PORT_NUMBER = (ConstantFlag.LiveFg ? 32504 : 32402); //ELD: 32101; Ecompliance: 3396
    public static final String HOST = (ConstantFlag.LiveFg ? "207.194.137.58" : "209.97.200.208");//"192.168.0.7";//"207.194.137.58";//"10.0.2.2";//207.194.137.58
    private static Thread chatThread = null, hbTread;
    public static PrintWriter out = null;
    public static BufferedReader in = null;
    private static Date lastHeartBeatTime = Utility.newDate();
    private static Date lastDataSentTime = new Date();

    // Created By: Deepak Sharma
    // Created Date: 14 April 2016
    // Purpose: connect to server
    public static void connect() {

        if (!Utility.isInternetOn())
            return;

        if (out != null && !out.checkError())
            return;

        if (chatThread != null) {
            chatThread.interrupt();
            chatThread = null;
        }

        chatThread = new Thread(new Runnable() {

            @Override
            public void run() {
                chatThread.setName("ChatClient-Connect");

                try {
                    connecting = true;
                    socket = new Socket(HOST, PORT_NUMBER);
                    lastHeartBeatTime = Utility.newDate();
                    //  socket.setKeepAlive(true);
                    //socket.setSoTimeout(0);
                    out = new PrintWriter(socket.getOutputStream());
                    in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
                    MessageBean bean = MessageDB.CreateMessage(Utility.IMEI, Utility.user1.getAccountId(), Utility.user1.getAccountId(), "Connect");
                    MessageDB.Send(bean);

                    if (Utility.user2.getAccountId() > 0) {
                        bean = MessageDB.CreateMessage(Utility.IMEI, Utility.user2.getAccountId(), Utility.user2.getAccountId(), "Connect");
                        MessageDB.Send(bean);
                    }
                    if (icListner != null) {
                        icListner.onServerStatusChanged(true);
                    }
                    Log.i("ChatInfo:", "Connected");
                    connecting = false;
                    while (true) {
                        String msg = null;
                        try {
                            msg = in.readLine();
                        } catch (Exception e) {
                            out = null;
                        }

                        if (msg == null) {
                            Log.i("ChatInfo:", "Disconnected-Conn");
                            break;
                        } else {
                            receive(msg);
                        }
                    }

                } catch (Exception e) {
                    Utility.printError(e.getMessage());
                    // LogFile.write(ChatClient.class.getName() + "::checkConnection Error:" + e.getMessage(), LogFile.DATABASE, LogFile.ERROR_LOG);
                }
                connecting = false;

            }
        });
        chatThread.start();

    }

    private static boolean connecting = false;

    public static void checkConnection() {
        if (hbTread != null) {
            hbTread.interrupt();
            hbTread = null;
        }

        hbTread = new Thread(new Runnable() {
            @Override
            public void run() {
                hbTread.setName("ChatClient-HB");

                while (true) {
                    try {
                        Thread.sleep(5 * 1000);

                        if (connecting || !Utility.isInternetOn()) continue;

                        connecting = true;

                        if (Utility.user1.getAccountId() > 0) {
                            long differ = (Utility.newDate().getTime() - lastHeartBeatTime.getTime()) / 1000;
                            if (out == null || out.checkError() || differ > 10) {
                                if (icListner != null) {
                                    icListner.onServerStatusChanged(false);
                                }
                                reconnect();
                            }
                            // send heart beat
                            SendHB();
                        }

                    } catch (InterruptedException e) {
                        Utility.printError(e.getMessage());
                        break;
                        //LogFile.write(ChatClient.class.getName() + "::checkConnection Error:" + e.getMessage(), LogFile.DATABASE, LogFile.ERROR_LOG);
                    }
                    connecting = false;
                }
            }
        });
        hbTread.start();
    }

    // Created By: Deepak Sharma
    // Created Date: 27 June 2016
    // Purpose: send Heart Beat
    public static void SendHB() {
        try {
            Log.i("ChatClient", "Hearbeat");
            String message = "HB:" + Utility.vehicleId;

            // time elapsed from last data sent to server with heart beat in minutes
            long differ = (new Date().getTime() - lastDataSentTime.getTime()) / (1000 * 60);

            if (differ >= 5) {
                // set status code to send to exe
                setStatusCode();

                int driverId = Utility.activeUserId;
                int coDriverId = 0;

                if (Utility.user2.getAccountId() > 0)
                    coDriverId = driverId == Utility.user2.getAccountId() ? Utility.user1.getAccountId() : Utility.user2.getAccountId();

                // if no driver is login then copy id of unidentified driver
                if (driverId == 0) {
                    driverId = Utility.unIdentifiedDriverId;
                }

                int violationId = 0;
                int hourLeft = 0;

                // only if active driver is on screen and app found upcoming/current violation
                if (driverId == Utility.onScreenUserId && MainActivity._violationFg) {
                    violationId = MainActivity.ViolationId;

                    hourLeft = (int) Math.round((MainActivity.ViolationDT.getTime() - Utility.newDate().getTime()) / (1000 * 60d));
                    if (hourLeft < 0) {
                        hourLeft = 0;
                    }
                }

                //String message = "HB:" + Utility.vehicleId;
                message = "HB:" + Utility.vehicleId + ":" + driverId + ":" + GPSData.DrivingTimeRemaining + ":" + GPSData.WorkShiftRemaining + ":" + GPSData.TimeRemaining70 + ":" + GPSData.TimeRemaining120 + ":" + GPSData.TimeRemainingUS70 + ":" + coDriverId + ":" + GPSData.StatusCode + ":" + violationId + ":" + hourLeft;

                lastDataSentTime = new Date();
            }

            if (out != null && !out.checkError()) {

                out.println(message);
                out.flush();
            } else {
                Log.i("ChatClient", "no send");

            }

        } catch (Exception e) {
            //  LogFile.write(ChatClient.class.getName() + "::send Error:" + e.getMessage(), LogFile.DATABASE, LogFile.ERROR_LOG);
            Utility.printError(e.getMessage());
        }
    }


    // Deepak Sharma
    // 21 July 2016
    // set status code for gps data
    private static void setStatusCode() {
        if (ConstantFlag.HutchConnectFg) {
            //Utility.diagnosticEngineSynchronizationTime
            int timeDifference = (int) ((System.currentTimeMillis() - CanMessages.diagnosticEngineSynchronizationTime) / 1000);

            // if time difference between data transfer less than 10 second it means  Hutch Connect is connected and vice verse
            GPSData.BTDataStatusFg = timeDifference < 10 ? 1 : 0;

            if (!Utility.HutchConnectStatusFg) {
                GPSData.BTEnabledFg = BluetoothAdapter.getDefaultAdapter().isEnabled() ? 1 : 0;
            } else {
                GPSData.BTEnabledFg = 1;
            }

            // 1: on Battery, TripInspection complted, In violation,Bluetooth is connected
            String code1 = Utility.convertBinaryToHex((GPSData.ACPowerFg == 0 ? "1" : "0") + "" + GPSData.TripInspectionCompletedFg + "" + (GPSData.NoHOSViolationFgFg == 1 ? "0" : "1") + "" + (Utility.HutchConnectStatusFg ? "1" : "0"));
            String code2 = Utility.convertBinaryToHex(GPSData.BTDataStatusFg + "" + GPSData.BTEnabledFg + "" + GPSData.WifiOnFg + "" + GPSData.TPMSWarningOnFg);
            String code3 = Utility.convertBinaryToHex((Utility.dataDiagnosticIndicatorFg ? "1" : "0") + (Utility.malFunctionIndicatorFg ? "1" : "0") + (Float.valueOf(CanMessages.RPM) == 0f ? "0" : "1") + "0");
            String code4 = "0";
            GPSData.StatusCode = code1 + code2 + code3 + code4;
        } else {
            String code1 = Utility.convertBinaryToHex((GPSData.ACPowerFg == 0 ? "1" : "0") + "" + GPSData.TripInspectionCompletedFg + "" + (GPSData.NoHOSViolationFgFg == 1 ? "0" : "1") + "" + GPSData.CellOnlineFg);
            String code2 = Utility.convertBinaryToHex(GPSData.RoamingFg + "" + GPSData.DTCOnFg + "" + GPSData.WifiOnFg + "" + GPSData.TPMSWarningOnFg);
            String code3 = Utility.convertBinaryToHex((Utility.dataDiagnosticIndicatorFg ? "1" : "0") + (Utility.malFunctionIndicatorFg ? "1" : "0") + (Float.valueOf(CanMessages.RPM) == 0f ? "0" : "1") + "0");
            String code4 = "0";
            GPSData.StatusCode = code1 + code2 + code3 + code4;
        }
    }


    private synchronized static void reconnect() {

        try {

            socket.close();
            Log.i("ChatClient", "reconnect Called");
            Thread.sleep(1000);
        } catch (Exception e) {

            Utility.printError(e.getMessage());
            //  LogFile.write(ChatClient.class.getName() + "::reconnect Error:" + e.getMessage(), LogFile.DATABASE, LogFile.ERROR_LOG);
        }
        connect();

    }

    // Created By: Deepak Sharma
    // Created Date: 14 April 2016
    // Purpose: disconnect connection from server
    public static void disconnect() {
        Thread thDisconnect = new Thread(new Runnable() {

            @Override
            public void run() {
                try {
                    if (hbTread != null) {
                        hbTread.interrupt();
                        hbTread = null;
                    }

                    socket.close();
                    in = null;
                    out = null;
                    Log.i("ChatInfo:", "Disconnected");
                } catch (Exception e) {

                    Utility.printError(e.getMessage());
                    // LogFile.write(ChatClient.class.getName() + "::disconnect Error:" + e.getMessage(), LogFile.DATABASE, LogFile.ERROR_LOG);
                }
            }
        });
        thDisconnect.setName("ChatClient-Disconnect");
        thDisconnect.start();
    }

    // Created By: Deepak Sharma
    // Created Date: 14 April 2016
    // Purpose: received message to server
    public static void send(final String message) {

        Thread thSend = new Thread(new Runnable() {

            @Override
            public void run() {
                try {
                    Log.i("ChatClient", "send");
                    if (out != null && !out.checkError()) {
                        Log.i("ChatClient", "sending " + message);
                        out.println(message);
                        out.flush();
                    } else {
                        Log.i("ChatClient", "no send");

                    }

                } catch (Exception e) {
                    Log.i("ChatClient", "sending error: " + e.getMessage());
                    Utility.printError(e.getMessage());
                    LogFile.write(ChatClient.class.getName() + "::send Error:" + e.getMessage(), LogFile.DATABASE, LogFile.ERROR_LOG);
                    LogDB.writeLogs(ChatClient.class.getName(), "send Error:", e.getMessage(), Utility.printStackTrace(e));
                }
            }
        });
        thSend.setName("ChatClient-Send");
        thSend.start();
    }

    // Created By: Deepak Sharma
    // Created Date: 14 April 2016
    // Purpose: received message from server
    private static void receive(String message) {

        try {

            if (message.startsWith("@")) {
                lastHeartBeatTime = Utility.newDate();
                Log.d("supporcommand", message);
                String[] obj = message.split(";");

                for (String cmd : obj) {
                    message = cmd;


                    String[] data = message.split(",");
                    String toDevice = data[1];
                    String fromDevice = data[2];
                    Utility.SupportIMEI = toDevice;
                    if (message.startsWith("@C") || message.startsWith("@RP")) {

                        // post database to server and return db name to hutch support app
                        if (message.startsWith("@C"))
                            PostDatabase(toDevice);
                        else {
                            // send preferences, vehicledata and flag data
                            String sharedPrefrence = "@P,";
                            sharedPrefrence = sharedPrefrence + Utility.IMEI + "," + toDevice + "," +
                                    Utility.getSharedPrefrences() + ";@V," + Utility.getVehicleData() + ";"
                                    + Utility.getFlagsData();
                            send(sharedPrefrence);

                        }
                    } else if (message.startsWith("@N")) {// on drawer menu selected
                        int position = Integer.parseInt(data[3]);
                        if (icListner != null) {
                            icListner.onRemoteNavigationItemChanged(position, true);
                        }
                    } else if (message.startsWith("@M")) {// on drawer menu selected
                        int position = Integer.parseInt(data[3]);
                        if (icListner != null) {
                            icListner.onRemoteNavigationItemChanged(position, false);
                        }
                    } else if (message.startsWith("@EU")) { // Event Update
                        EventDB.UpdateEvent(Integer.parseInt(data[3]), data[4], Integer.parseInt(data[5]),
                                Integer.parseInt(data[6]), Integer.parseInt(data[7]), data[8], data[9],
                                data[10], data[11], data[12], data[13], data[14], data[15], data[16], data[17],
                                Integer.parseInt(data[18]));
                        if (rListner != null)
                            rListner.onRefresh();
                    } else if (message.startsWith("@RU")) { // Rule Update
                        DailyLogDB.editRule(Integer.parseInt(data[3]), data[4], Integer.parseInt(data[5]));
                        if (rListner != null)
                            rListner.onRefresh();
                    } else if (message.startsWith("@ER")) {// post/ sync elog data
                        boolean isPost = Boolean.parseBoolean(data[3]);
                        if (icListner != null) {
                            if (isPost)
                                icListner.onPostData();
                            else icListner.onSyncData();
                        }
                    } else if (message.startsWith("@IR")) {// post/ sync inspect dailylog data
                        boolean isPost = Boolean.parseBoolean(data[3]);
                        if (rListner != null) {
                            if (isPost)
                                rListner.onCallPost();
                            else rListner.onCallSync();
                        }
                    } else if (message.startsWith("@S")) { // inspect daily log swipe
                        int position = Integer.parseInt(data[3]);
                        if (seListner != null) seListner.onSwipe(position);
                    } else if (message.startsWith("@DB")) // send db to email
                    {
                        emailDatabase();
                    } else if (message.startsWith("@LF")) // send log file
                    {
                        LogFile.sendEld2020Data();
                    }
                }
                return;
            }


            if (message.equals("HB")) {
                lastHeartBeatTime = Utility.newDate();
                System.out.println("HeartBeat: " + lastHeartBeatTime);
                return;
            }

            Log.i("ChatClient", "receive");
            Log.i("ChatClient", "RecievedMessage:" + message);
            // Looper.prepare();
            System.out.println("RecievedMessage:" + message);
          /*  if (message.equals("HB")) {
                lastHeartBeatTime = Utility.newDate();
                System.out.println("HeartBeat: " + lastHeartBeatTime);
                return;
            }*/
            JSONObject json = new JSONObject(message);
            String flag = json.getString("Flag");

            MessageBean bean = new MessageBean();
            // declare notification bean here
            if (flag.equals("Notification")) {
                // declare notification bean
                NotificationBean notificationBean = new NotificationBean();
                // load value from json object to notification bean

                notificationBean.setComment(json.getString("Comment"));
                notificationBean.setNotiFicationDate(json.getString("NotificationDate"));
                notificationBean.setNotificationID(json.getInt("NotificationId"));
                notificationBean.setTitle(json.getString("Title"));

                NotificationDB.Save(notificationBean);

                NotificationTask.showNotification(notificationBean);

                return;
            }

            bean.setMessage(json.getString("Message"));
            bean.setCreatedById(json.getInt("CreatedById"));
            bean.setMessageToId(json.getInt("MessageToId"));
            bean.setMessageDate(json.getString("MessageDate"));
            bean.setDeliveredFg(0);
            bean.setReadFg(0);
            bean.setSendFg(0);
            bean.setDeviceId(json.getString("DeviceId"));
            bean.setSyncFg(1);
            bean.setFlag(json.getString("Flag"));


            if (flag.equals("Message")) {
                //if (MessageActivity.mHandler != null) {
                //MessageActivity.mHandler.sendMessage(obj);
                MessageDB.Save(bean);

                if (mListener != null) {
                    mListener.onMessageUpdated(bean, MESSAGE);
                } else {
                    if (icListner != null) {
                        icListner.onMessageReceived();
                    }
                    getNotification(bean);
                }

                // send notification to dashboard
                if (iNotifyDashboards != null) {
                    iNotifyDashboards.onMessageReceived();
                }


                //}
                // MessageActivity.received(bean);
            } else if (flag.equals("Read")) {

                MessageDB.MessageStatusUpdate(Utility.onScreenUserId, bean.getMessageToId());
                if (mListener != null) {
                    mListener.onMessageUpdated(bean, READFG);
                }

            } else if (flag.equals("Online")) {
                Log.i("ChatClient", "online");
                if (!Utility.onlineUserList.contains(bean.getCreatedById() + "")) {
                    Utility.onlineUserList.add(bean.getCreatedById() + "");
                }

                if (mListener != null) {
                    mListener.onMessageUpdated(bean, ONLINE);
                }
            } else if (flag.equals("OnlineList")) {
                String[] arrUser = bean.getMessage().split(",");
                for (String user : arrUser) {
                    if (!Utility.onlineUserList.contains(user)) {
                        Utility.onlineUserList.add(user);
                    }
                }
            } else if (flag.equals("Offline")) {
                Log.i("ChatClient", "offline");
                Utility.onlineUserList.remove(bean.getCreatedById() + "");

                if (mListener != null) {
                    mListener.onMessageUpdated(bean, OFFLINE);
                }
            } else if (flag.equals("Offline2")) {
                String arr[] = bean.getMessage().split(",");

                for (String u : arr) {
                    Utility.onlineUserList.remove(u);

                }

                if (mListener != null) {
                    mListener.onMessageUpdated(bean, OFFLINE2);
                }
            } else if (flag.equals("HB")) {
                lastHeartBeatTime = Utility.newDate();
                System.out.println("HeartBeat: " + lastHeartBeatTime);
            }

        } catch (Exception e) {
            //   Log.i("ChatClient", "receive error: " + e.getMessage());
            Utility.printError(e.getMessage());
            //LogFile.write(ChatClient.class.getName() + "::receive Error:" + e.getMessage(), LogFile.DATABASE, LogFile.ERROR_LOG);

        }
    }

    private static void getNotification(MessageBean bean) {
        int activeUserId = Utility.user1.isOnScreenFg() ? Utility.user1.getAccountId() : Utility.user2.getAccountId();
        if (activeUserId != bean.getMessageToId() && !Utility.motionFg) {
            return;
        }

        String userName = UserDB.getUserName(bean.getCreatedById());

        Intent resultIntent = new Intent(Utility.context, MainActivity.class);
        Bundle b = new Bundle();
        b.putInt("UserId", bean.getCreatedById());
        b.putString("UserName", userName);
        resultIntent.putExtras(b);

        PendingIntent pendingIntent = PendingIntent.getActivity(Utility.context, 0, resultIntent, Intent.FILL_IN_ACTION);

        NotificationCompat.Builder mBuilder =
                new NotificationCompat.Builder(Utility.context)
                        .setSmallIcon(R.drawable.ic_launcher)
                        .setContentTitle(userName)
                        .setContentText(bean.getMessage());

        mBuilder.setContentIntent(pendingIntent);
        /*Intent resultIntent = new Intent(Utility.context, MessageActivity.class);
        Bundle b = new Bundle();
        b.putInt("UserId", bean.getCreatedById());
        b.putString("UserName", userName);
        resultIntent.putExtras(b);

        PendingIntent resultPendingIntent =
                PendingIntent.getActivity(
                        Utility.context,
                        0,
                        resultIntent,
                        PendingIntent.FLAG_UPDATE_CURRENT
                );
        mBuilder.setContentIntent(resultPendingIntent);*/

        NotificationManager mNotifyMgr =
                (NotificationManager) (Utility.context).getSystemService(Utility.context.NOTIFICATION_SERVICE);
        mBuilder.setDefaults(Notification.DEFAULT_SOUND);
        mBuilder.setAutoCancel(true);
        mNotifyMgr.notify(1001, mBuilder.build());

        if (Utility._appSetting.getMessageReading() == 1 && MainActivity.textToSpeech != null) {

            //   String toUserName = Utility.user1.getAccountId() == bean.getMessageToId() ? Utility.user1.getFirstName() : Utility.user2.getFirstName();
            String textToSpeech = bean.getMessage();
            MainActivity.textToSpeech.playSilence(2000, TextToSpeech.QUEUE_ADD, null);
            MainActivity.textToSpeech.speak(textToSpeech, TextToSpeech.QUEUE_ADD, null);

        }

    }


    // Created By: Deepak Sharma
    // Created Date: 26 Feb 2020
    // Purpose: Post database to server
    public static void PostDatabase(final String toDeviceId) {
        String databasePath = Environment.getExternalStorageDirectory() + "/" + Utility.backupDB();

        // get database name that would be saved on server
        final String fileName = databasePath.substring(databasePath.lastIndexOf("/") + 1);
        File file = new File(databasePath);

        // if database file is created successfully then push it to hutch support device via web server
        if (file.exists()) {

            // size of database file
            final long contentLength = file.length();

            new UploadFileToServer(new UploadFileToServer.IFileProgress() {

                @Override
                public void onUploadStart() {

                }

                @Override
                public void onFileUploading(int progress) {

                }

                @Override
                public void onFileUploaded(boolean status) {
                    if (status) {

                        Log.d("supporcommand", "Send Database");
                        String sharedPrefrence = "@P," + Utility.IMEI + "," + toDeviceId + "," +
                                Utility.getSharedPrefrences() + ";@V," + Utility.getVehicleData() + ";"
                                + Utility.getFlagsData() + ";" + "@D," + fileName + "," + contentLength + ";";

                        send(sharedPrefrence);
                    } else {

                    }
                }

            }, true).execute(DocumentType.DOCUMENT_DATABASE_BACKUP, databasePath);
        }
    }


    // Created By: Deepak Sharma
    // Created Date: 18 Feb 2020
    // Purpose: Email database to dev@hutchsystems.com
    private static void emailDatabase() {
        String dbName = Utility.backupDB();


        if (dbName.isEmpty())
            return;

        String content = "App Version: " + Utility.ApplicationVersion + "\nCompany: " + Utility.CarrierName + "\nCompanyId: " + Utility.companyId + "\nVehicleID: " + Utility.vehicleId + "\n" + "Unit No: " + Utility.UnitNo + "\n" + "IMEI: " + Utility.IMEI;
        String title = "Database from ELD: " + Utility.ApplicationVersion + " - " + Utility.IMEI + " - Unit No: " + Utility.UnitNo;
        new ReportIssue(new ReportIssue.IMailProgress() {


            @Override
            public void onMailSent(boolean status) {

            }
        }).execute(title, content, dbName);
    }


    public static final int MESSAGE = 1, ONLINE = 2, OFFLINE = 3, OFFLINE2 = 4, ONLINELIST = 5, READFG = 6;

    public interface ChatMessageInterface {
        void onMessageUpdated(MessageBean obj, int flag);
    }

    public interface ChatMessageReceiveIndication {

        void onMessageReceived();

        void onServerStatusChanged(boolean status);

        void onRemoteNavigationItemChanged(int itemId, boolean drawerFg);

        void onSyncData();

        void onPostData();
    }


    public interface RefreshData {
        void onCallPost();

        void onCallSync();

        void onRefresh();
    }

    public interface SwipeEvent {
        void onSwipe(int position);
    }

    public interface INotifyDashboards {
        void onMessageReceived();
    }
}
