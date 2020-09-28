package com.hutchsystems.hutchconnect.tasks;

import android.location.Location;
import android.os.AsyncTask;
import android.os.Environment;
import android.util.Log;

import com.hutchsystems.hutchconnect.MainActivity;
import com.hutchsystems.hutchconnect.common.LogFile;
import com.hutchsystems.hutchconnect.common.Utility;
import com.hutchsystems.hutchconnect.db.LogDB;
import com.kmaxsystems.reversegeocode.GeoName;

import java.io.File;
import java.io.InputStream;

/**
 * Created by Deepak Sharma on 9/5/2018.
 */

public class NearestDistanceTask extends AsyncTask<Void, Void, Boolean> {
    String errorMessage = "";

    String TAG = SyncData.class.getName();

    private PostTaskListener<Boolean> postTaskListener;

    public NearestDistanceTask(PostTaskListener<Boolean> postTaskListener) {
        this.postTaskListener = postTaskListener;
    }

    @Override
    protected void onPreExecute() {

    }

    @Override
    protected Boolean doInBackground(Void... none) {
        if (!MainActivity.objReverseGeocoder.loadedFg)
            return false;

        Log.e(TAG, "Get current location");
        try {
            return reverseGeoCode();
        } catch (Exception e) {
            return false;
        }

    }

    protected void onPostExecute(Boolean status) {
        if (postTaskListener != null)
            postTaskListener.onPostTask(status);
    }

    public interface PostTaskListener<K> {
        void onPostTask(K result);
    }

    // Created By: Deepak Sharma
    // Created Date: 5 September 2018
    // Purpose: get nearest location
    private Boolean reverseGeoCode() {
        try {

            Log.i("ReverseGeocode", "ReverseGeocode:Starts");
            /*Utility.currentLocation.setLatitude(34.12);
            Utility.currentLocation.setLongitude(-83.76);*/
            double latitude = Utility.truncate(Utility.currentLocation.getLatitude(), 2);
            double longitude = Utility.truncate(Utility.currentLocation.getLongitude(), 2);

            if (latitude <= 0 || longitude > -52d || longitude < -139d) {
                return false;
            }

            return reverseGeoCode(latitude, longitude);
        } catch (Exception exe) {
            LogFile.write(NearestDistanceTask.class.getName() + "reverseGeoCode: " + exe.getMessage(), LogFile.ERROR_LOG, LogFile.ERROR_LOG);
            LogDB.writeLogs(NearestDistanceTask.class.getName(),"reverseGeoCode" ,exe.getMessage(),Utility.printStackTrace(exe));

            return false;
        }
    }


    // Created By: Deepak Sharma
    // Created Date: 5 September 2018
    // Purpose: get nearest location
    private Boolean reverseGeoCode(double latitude, double longitude) {
        try {

            Log.i("ReverseGeocode", "ReverseGeocode:Starts");
            File sdcard = Environment.getExternalStorageDirectory();
            InputStream inputStream = null;
            String fileName = MainActivity.objReverseGeocoder.getFileName(latitude);

            if (!fileName.isEmpty()) {
                inputStream = Utility.context.getAssets().open("Locations/" + fileName);
            }

            GeoName nearestPlace = MainActivity.objReverseGeocoder.nearestPlace(latitude, longitude, inputStream);
            String distanceUnit = Utility._appSetting.getUnit() == 1 ? "KMs " : "Miles ";
            String location = String.format("%.2f", getDistance(nearestPlace, latitude, longitude)) + " " + distanceUnit + getBearing(nearestPlace, latitude, longitude) + " " + nearestPlace.name + ", " + nearestPlace.state;
            if (!location.isEmpty()) {
                Utility.currentLocation.setLocationDescription(location);
            } else {
                LogFile.write(NearestDistanceTask.class.getName() + "reverseGeoCode: " + "Empty location returned", LogFile.ERROR_LOG, LogFile.ERROR_LOG);
            }
            Log.i("ReverseGeocode", "ReverseGeocode:Ends");
        } catch (Exception exe) {
            LogFile.write(NearestDistanceTask.class.getName() + "reverseGeoCode: " + exe.getMessage(), LogFile.ERROR_LOG, LogFile.ERROR_LOG);
            LogDB.writeLogs(NearestDistanceTask.class.getName(),"reverseGeoCode" ,exe.getMessage(),Utility.printStackTrace(exe));
            return false;
        }
        return true;
    }



    public float getDistance(GeoName nearestPlace, double latitude, double longitude) {

        Location loc1 = new Location("");  //for initial location ; here from gps
        loc1.setLatitude(latitude); //getting latitude reading from gps
        loc1.setLongitude(longitude); //getting longitude reading from gps

        Location loc2 = new Location(""); // for nearest palce location
        loc2.setLatitude(nearestPlace.latitude);
        loc2.setLongitude(nearestPlace.longitude);

        float distanceInMeters = loc1.distanceTo(loc2); //inbuilt menthod to return distance in meter between two locations
        return (distanceInMeters / (Utility._appSetting.getUnit() == 1 ? 1000f : 1609.34f)); //dividing distance in meters by 1609.34(approx) to get in miles
    }

    public double getAtan2(double y, double x) {
//        The function  atan2(y,x)} {\displaystyle \operatorname {atan2} (y,x)}
//       is defined as the angle in the Euclidean plane, given in radians, between the positive x-axis and the ray to the point (x,y) ≠ (0,0)
        return Math.atan2(y, x);
    }

    //    gives the direction
    public String getBearing(GeoName nearestPlace, double latitude, double longitude) {

        Location loc1 = new Location("");  //for initial location ; here from gps
        loc1.setLatitude(latitude); //getting latitude reading from gps
        loc1.setLongitude(longitude); //getting longitude reading from gps

        Location loc2 = new Location(""); // for nearest palce location
        loc2.setLatitude(nearestPlace.latitude);
        loc2.setLongitude(nearestPlace.longitude);

        float bearing = (loc2.bearingTo(loc1));
        if (bearing < 0) {
            bearing += 360f;
        }
        bearing = Math.round(bearing / 22.5f);

        String[] directions = {"N", "NNE", "NE", "ENE", "E", "ESE", "SE", "SSE", "S", "SSW", "SW", "WSW", "W", "WNW", "NW", "NNW", "N"};
        return directions[(int) bearing];

    }

}
