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

import com.kmaxsystems.reversegeocode.kdtree.Index;
import com.kmaxsystems.reversegeocode.kdtree.KDTree;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;

import javax.crypto.Cipher;
import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;

/**
 * Uses KD-trees to quickly find the nearest point
 */
public class ReverseGeoCoder {

    KDTree<GeoName> kdTree;
    private String _fileName;
    private ArrayList<Index> indexArrayList;
    public boolean loadedFg;

    public ReverseGeoCoder() {

    }

    public void initialize(InputStream indexStream) throws Exception {
        initializeIndexFile(indexStream);
    }

    private void initializeKDTree()
            throws Exception {

        String source = "Navigation/Locations/" + _fileName;

        InputStream inputStream = getClass().getResourceAsStream(source);

        initializeKDTree(inputStream);

        inputStream.close();
    }

    // Created By: Deepak Sharma
    // Created Date: 4 September 2018
    // Purpose: initialize KD Tree
    private void initializeKDTree(InputStream inputStream)
            throws Exception {
        ArrayList<GeoName> placesList = new ArrayList<>();

        // Read the geonames file in the directory
        BufferedReader in = new BufferedReader(new InputStreamReader(inputStream));
        String str;

        while ((str = in.readLine()) != null) {  //reading rowwise
            //str = EncryptOrDecrypt(str,_fileName);
            GeoName newPlace = new GeoName(str);  //convert string of a row to GeoName
            placesList.add(newPlace);  //adding to geoname arraylist
        }

        in.close();
        inputStream.close();

        kdTree = new KDTree<>(placesList);//k-d tree  with all the coordinates in the fi

    }

    //returns newarest geoName
    public GeoName nearestPlace(double latitude, double longitude, InputStream inputStream) throws Exception {

        if (inputStream != null) {
            initializeKDTree(inputStream);
        }

        return kdTree.findNearest(new GeoName(latitude, longitude));
    }

    // Created By: Deepak Sharma
    // Created Date: 4 September 2018
    // Purpose: load index file into array list
    private void initializeIndexFile(InputStream indexStream) throws Exception {

        indexArrayList = new ArrayList<>();

        BufferedReader in = new BufferedReader(new InputStreamReader(indexStream));
        String str = null;

        while ((str = in.readLine()) != null) {  //reading row wise

            // str = EncryptOrDecrypt(str, "index");
            String[] data = str.split(":");

            Index index = new Index();

            index.setFileName(data[0]);
            index.setMinLat(Double.parseDouble(data[1]));
            index.setMaxLat(Double.parseDouble(data[2]));
            indexArrayList.add(index);
        }
        in.close();
    }

    // Created By: Deepak Sharma
    // Created Date: 4 September 2018
    // Purpose: get file name from index array List with matching latitude
    public String getFileName(double latitude) throws Exception {
        String currentFile = "";

        for (Index index : indexArrayList) {
            if (latitude >= index.getMinLat() && latitude <= index.getMaxLat()) {
                currentFile = index.getFileName();
                break;
            }
        }

        // if loaded file is same then don't initialize KD tree
        if (currentFile.equals(_fileName)) {
            currentFile = "";
        } else {
            _fileName = currentFile;
        }

        return currentFile;
    }

    public String EncryptOrDecrypt(String input, String keyText) {
        char[] key = keyText.toCharArray(); //Your key in char array
        StringBuilder output = new StringBuilder();

        for (int i = 0; i < input.length(); i++) {
            output.append((char) (input.charAt(i) ^ key[i % key.length]));
        }

        return output.toString();
    }

}
