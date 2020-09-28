package com.kmaxsystems.reversegeocode.kdtree;

/**
 * Created by Deepak Sharma on 9/4/2018.
 */

public class Index {
    String fileName;
    double minLat, maxLat;

    public String getFileName() {
        return fileName;
    }

    public void setFileName(String fileName) {
        this.fileName = fileName;
    }

    public double getMinLat() {
        return minLat;
    }

    public void setMinLat(double minLat) {
        this.minLat = minLat;
    }

    public double getMaxLat() {
        return maxLat;
    }

    public void setMaxLat(double maxLat) {
        this.maxLat = maxLat;
    }
}
