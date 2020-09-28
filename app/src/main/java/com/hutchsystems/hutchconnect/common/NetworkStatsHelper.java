package com.hutchsystems.hutchconnect.common;

import android.Manifest;
import android.annotation.TargetApi;
import android.app.Activity;
import android.app.usage.NetworkStats;
import android.app.usage.NetworkStatsManager;
import android.content.Context;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.net.ConnectivityManager;
import android.net.NetworkCapabilities;
import android.net.TrafficStats;
import android.os.Build;
import android.os.RemoteException;
import android.telephony.TelephonyManager;

import com.hutchsystems.hutchconnect.beans.Package;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

/**
 * Created by SAMSUNG on 15-03-2017.
 */

@TargetApi(Build.VERSION_CODES.M)
public class NetworkStatsHelper {
    NetworkStatsManager networkStatsManager;
    int packageUid;
    Activity activity;

    public NetworkStatsHelper(NetworkStatsManager networkStatsManager, Activity activity) {
        this.networkStatsManager = networkStatsManager;
        this.activity = activity;
    }

    public NetworkStatsHelper(NetworkStatsManager networkStatsManager, int packageUid) {
        this.networkStatsManager = networkStatsManager;
        this.packageUid = packageUid;
    }

    public long getAllRxBytesMobile(Context context) {
        NetworkStats.Bucket bucket;
        try {
            bucket = networkStatsManager.querySummaryForDevice(ConnectivityManager.TYPE_MOBILE,
                    getSubscriberId(context, ConnectivityManager.TYPE_MOBILE),
                    0,
                    System.currentTimeMillis());
        } catch (RemoteException e) {
            return -1;
        }
        return bucket.getRxBytes();
    }

    public long getAllTxBytesMobile(Context context) {
        NetworkStats.Bucket bucket;
        try {
            bucket = networkStatsManager.querySummaryForDevice(ConnectivityManager.TYPE_MOBILE,
                    getSubscriberId(context, ConnectivityManager.TYPE_MOBILE),
                    0,
                    System.currentTimeMillis());
        } catch (RemoteException e) {
            return -1;
        }
        return bucket.getTxBytes();
    }


    public long getAllBytesMobile(Context context) {
        NetworkStats.Bucket bucket;
        try {
            bucket = networkStatsManager.querySummaryForDevice(ConnectivityManager.TYPE_MOBILE,
                    getSubscriberId(context, ConnectivityManager.TYPE_MOBILE),
                    0,
                    System.currentTimeMillis());
        } catch (RemoteException e) {
            return -1;
        }
        long total = (bucket.getRxBytes() + bucket.getTxBytes()) / 1024;
        return total;
    }

    public long getAllRxBytesWifi() {
        NetworkStats.Bucket bucket;
        try {
            bucket = networkStatsManager.querySummaryForDevice(ConnectivityManager.TYPE_WIFI,
                    "",
                    0,
                    System.currentTimeMillis());
        } catch (RemoteException e) {
            return -1;
        }
        return bucket.getRxBytes();
    }

    public long getAllTxBytesWifi() {
        NetworkStats.Bucket bucket;
        try {
            bucket = networkStatsManager.querySummaryForDevice(ConnectivityManager.TYPE_WIFI,
                    "",
                    0,
                    System.currentTimeMillis());
        } catch (RemoteException e) {
            return -1;
        }
        return bucket.getTxBytes();
    }

    public long getPackageRxBytesMobile(Context context) {
        NetworkStats networkStats = null;
        try {
            networkStats = networkStatsManager.queryDetailsForUid(
                    ConnectivityManager.TYPE_MOBILE,
                    getSubscriberId(context, ConnectivityManager.TYPE_MOBILE),
                    0,
                    System.currentTimeMillis(),
                    packageUid);
        } catch (Exception e) {
            return -1;
        }
        NetworkStats.Bucket bucket = new NetworkStats.Bucket();
        networkStats.getNextBucket(bucket);
        networkStats.getNextBucket(bucket);
        return bucket.getRxBytes();
    }

    public long getPackageTxBytesMobile(Context context) {
        NetworkStats networkStats = null;
        try {
            networkStats = networkStatsManager.queryDetailsForUid(
                    ConnectivityManager.TYPE_MOBILE,
                    getSubscriberId(context, ConnectivityManager.TYPE_MOBILE),
                    0,
                    System.currentTimeMillis(),
                    packageUid);
        } catch (Exception e) {
            return -1;
        }
        NetworkStats.Bucket bucket = new NetworkStats.Bucket();
        networkStats.getNextBucket(bucket);
        return bucket.getTxBytes();
    }

    public long getPackageRxBytesWifi() {
        NetworkStats networkStats = null;
        try {
            networkStats = networkStatsManager.queryDetails(
                    NetworkCapabilities.TRANSPORT_WIFI,
                    "",
                    0,
                    System.currentTimeMillis());
        } catch (Exception e) {
            return -1;
        }
        NetworkStats.Bucket bucket = new NetworkStats.Bucket();
        networkStats.getNextBucket(bucket);
        return bucket.getRxBytes();
    }

    public long getPackageTxBytesWifi() {
        NetworkStats networkStats = null;
        long bytes = 0;
        Calendar c = Calendar.getInstance();
        c.add(Calendar.DAY_OF_MONTH, -30);
        try {
            networkStats = networkStatsManager.querySummary(
                    NetworkCapabilities.TRANSPORT_WIFI,
                    "",
                    c.getTimeInMillis(),
                    System.currentTimeMillis());
        } catch (Exception e) {
            return -1;
        }
        NetworkStats.Bucket bucket = new NetworkStats.Bucket();
        while (networkStats.hasNextBucket()) {
            networkStats.getNextBucket(bucket);
            bytes = bytes + bucket.getTxBytes() + bucket.getRxBytes();
        }
        return bucket.getTxBytes();
    }

    private String getSubscriberId(Context context, int networkType) {
//        if (ConnectivityManager.TYPE_MOBILE == networkType) {
        TelephonyManager tm = (TelephonyManager) context.getSystemService(Context.TELEPHONY_SERVICE);
        if (activity.checkSelfPermission(Manifest.permission.READ_PHONE_STATE) != PackageManager.PERMISSION_GRANTED) {
            // TODO:A Consider calling
            //    Activity#requestPermissions
            // here to request the missing permissions, and then overriding
            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
            //                                          int[] grantResults)
            // to handle the case where the user grants the permission. See the documentation
            // for Activity#requestPermissions for more details.
            return "";
        }
        return tm.getSubscriberId();
    }

    public String[] getDataUsageAllApp() {
        StringBuilder dataUsage = new StringBuilder();
        NetworkStats networkStats = null;
        List<Package> packageInfoList = getPackagesData();
        long grandTotal = 0;
        float eldData = 0, totalUsageData = 0;

        if (packageInfoList.size() > 0) {
            String packageName = packageInfoList.get(0).getPackageName();
            String appName = packageInfoList.get(0).getName();

            // Get UID of the selected process
            int uid = getPackageUid(packageName);

            try {
                // eld Data
                float total = 0;
                // get Data from prefrence
                float eldSavedDataUsage = Utility.getPreferences("eld_data_usage", 0f);

                // get data from device
                total += (TrafficStats.getUidTxBytes(uid) + TrafficStats.getUidRxBytes(uid));

                // convert data to mb
                total = ((int) ((total * 100) / (1024 * 1024))) / 100f;
                eldData = total;

                // save data to prefrence
                Utility.savePreferences("eld_data_usage", total);

                // compare data
                BigDecimal eldtotalBigDecimal = new BigDecimal(total);
                BigDecimal eldsavedBigDecimal = new BigDecimal(eldSavedDataUsage);

                // if device data is greater than prefrence data
                if (eldtotalBigDecimal.compareTo(eldsavedBigDecimal) == 1) {
                    BigDecimal result = eldtotalBigDecimal.subtract(eldsavedBigDecimal);
                    eldData = (float) Utility.truncate(result.floatValue(), 2);
                }

                // total Data
                // get Data from prefrence
                float savedDataUsage = Utility.getPreferences("total_data_usage", 0f);

                // get data from device
                grandTotal = TrafficStats.getTotalRxBytes() + TrafficStats.getTotalTxBytes();

                // convert device data to mb
                totalUsageData = ((int) ((grandTotal * 100) / (1024 * 1024))) / 100f;

                // save data to prefrence
                Utility.savePreferences("total_data_usage", totalUsageData);

                // compare data
                BigDecimal totalBigDecimal = new BigDecimal(totalUsageData);
                BigDecimal savedBigDecimal = new BigDecimal(savedDataUsage);

                // if device data is greater than prefrence data
                if (totalBigDecimal.compareTo(savedBigDecimal) == 1) {
                    BigDecimal result = totalBigDecimal.subtract(savedBigDecimal);
                    totalUsageData = (float) Utility.truncate(result.floatValue(), 2);
                }

                if (total > 0) {
                    //String data = appName + ": Send: " + send + ", Received: " + received;
                    String data = appName + ": " + total;
                    dataUsage.append(data);
                    dataUsage.append("\n");
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        String[] data = new String[]{dataUsage.toString(), totalUsageData + "", eldData + ""};
        return data;
    }

    public static boolean isPackage(CharSequence s) {
        PackageManager packageManager = Utility.context.getPackageManager();
        try {
            packageManager.getPackageInfo(s.toString(), PackageManager.GET_META_DATA);
        } catch (PackageManager.NameNotFoundException e) {
            return false;
        }
        return true;
    }

    public static int getPackageUid(String packageName) {
        PackageManager packageManager = Utility.context.getPackageManager();
        int uid = -1;
        try {
            PackageInfo packageInfo = packageManager.getPackageInfo(packageName, PackageManager.GET_META_DATA);
            uid = packageInfo.applicationInfo.uid;
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
        }
        return uid;
    }

//    private List<Package> getPackagesData() {
//        PackageManager packageManager = Utility.context.getPackageManager();
//        List<PackageInfo> packageInfoList = packageManager.getInstalledPackages(PackageManager.GET_META_DATA);
//        List<Package> packageList = new ArrayList<>(packageInfoList.size());
//        for (PackageInfo packageInfo : packageInfoList) {
//            Package packageItem = new Package();
//            packageItem.setVersion(packageInfo.versionName);
//            packageItem.setPackageName(packageInfo.packageName);
//            packageList.add(packageItem);
//            ApplicationInfo ai = null;
//            try {
//                ai = packageManager.getApplicationInfo(packageInfo.packageName, PackageManager.GET_META_DATA);
//            } catch (PackageManager.NameNotFoundException e) {
//                e.printStackTrace();
//            }
//            if (ai == null) {
//                continue;
//            }
//            CharSequence appName = packageManager.getApplicationLabel(ai);
//            if (appName != null) {
//                packageItem.setName(appName.toString());
//            }
//        }
//        return packageList;
//    }

    private List<Package> getPackagesData() {
        List<Package> packageList = new ArrayList<>();

        Package packageItem = new Package();
        packageItem.setVersion(Utility.ApplicationVersion);
        packageItem.setPackageName("com.hutchsystems.hutchconnect");
        packageItem.setName("Hutch Connect");
        packageList.add(packageItem);

        return packageList;
    }
}
