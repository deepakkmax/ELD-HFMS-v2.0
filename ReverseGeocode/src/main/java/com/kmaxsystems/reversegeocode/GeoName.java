/*
The MIT License (MIT)
[OSI Approved License]
The MIT License (MIT)

Copyright (c) 2014 Daniel Glasson

Permission is hereby granted, free of charge, to any person obtaining a copy
of this software and associated documentation files (the "Software"), to deal
in the Software without restriction, including without limitation the rights
to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
copies of the Software, and to permit persons to whom the Software is
furnished to do so, subject to the following conditions:

The above copyright notice and this permission notice shall be included in
all copies or substantial portions of the Software.

THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
THE SOFTWARE.
*/

package com.kmaxsystems.reversegeocode;

import com.kmaxsystems.reversegeocode.kdtree.KDNodeComparator;

import java.util.Comparator;

import static java.lang.Math.cos;
import static java.lang.Math.sin;
import static java.lang.Math.toRadians;

/**
 * Here now I have used just name of plce as name , latitude , longitude and country later you can add more as per your requirements* *
 */

public class GeoName extends KDNodeComparator<GeoName> {
    public String name;
    public double latitude;
    public double longitude;
    public double point[] = new double[3]; // The 3D coordinates of the point
    public String county, state;


    GeoName(String data) {
        // String[] names = data.split("\t"); // after converting your excle file to txt eact column is sepetated by '\t' so seperating it, returns string array
        // String[] names = data.split("\\|");
        String[] names = data.split(",");

        name = names[0];  // name is in column position 1
        latitude = Double.parseDouble(names[1]);  //latitude is in 9th position
        longitude = Double.parseDouble(names[2]); //longitude is in 10th position
        setPoint();
        state = names[3];// country name or code is in 5th position
    }

    GeoName(Double latitude, Double longitude) {
        name = county = "Search";
        this.latitude = latitude;
        this.longitude = longitude;
        setPoint();
    }

    private void setPoint() {
        point[0] = cos(toRadians(latitude)) * cos(toRadians(longitude));
        point[1] = cos(toRadians(latitude)) * sin(toRadians(longitude));
        point[2] = sin(toRadians(latitude));
    }

    @Override
    public String toString() {
        return "GeoName{" +
                "name='" + name + '\'' +
                ", county='" + county + '\'' +
                '}';
    }

    //abstract class KDNodeComparator has the methods squaredDistance(),axisSquaredDistance()
    //Return squared distance between current and other as passed into it
    //these methods are used by KDTree to find findNearest comparing distance with other creating leaf of tree
    @Override
    protected double squaredDistance(GeoName other) {
        double x = this.point[0] - other.point[0];
        double y = this.point[1] - other.point[1];
        double z = this.point[2] - other.point[2];
        return (x * x) + (y * y) + (z * z);
    }

    // Return squared distance between one axis only
    @Override
    protected double axisSquaredDistance(GeoName other, int axis) {
        double distance = point[axis] - other.point[axis];
        return distance * distance;
    }

    // This should return a comparator for whatever axis is passed in
    @Override
    protected Comparator<GeoName> getComparator(int axis) {
        return GeoNameComparator.values()[axis];
    }

    //compares betewwn two points
    protected static enum GeoNameComparator implements Comparator<GeoName> {
        x {
            @Override
            public int compare(GeoName a, GeoName b) {
                return Double.compare(a.point[0], b.point[0]);
            }
        },
        y {
            @Override
            public int compare(GeoName a, GeoName b) {
                return Double.compare(a.point[1], b.point[1]);
            }
        },
        z {
            @Override
            public int compare(GeoName a, GeoName b) {
                return Double.compare(a.point[2], b.point[2]);
            }
        };
    }
}
