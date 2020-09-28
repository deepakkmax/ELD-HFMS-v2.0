/*
 * Copyright (C) 2014 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.hutchsystems.hutchconnect.common;

/**
 * Defines several constants used between {@link CanMessages} and the UI.
 */
public interface Constants {

    // Message types sent from the BluetoothChatService Handler
    public static final int MESSAGE_STATE_CHANGE = 1;
    public static final int MESSAGE_READ = 2;
    public static final int MESSAGE_WRITE = 3;
    public static final int MESSAGE_DEVICE_NAME = 4;
    public static final int MESSAGE_TOAST = 5;

    // Key names received from the BluetoothChatService Handler
    public static final String DEVICE_NAME = "device_name";
    public static final String TOAST = "toast";
    public static final float DRIVING_SPEED_LIMIT = 8f;
    public static final int STOP_TIME = 3;
    public static final int STOPPING_TIME = 300;//5 mins
    public static final int ONE_HOUR_DRIVING = 3600; //1 hour
    public static final int HALF_HOUR_DRIVING = 1800;

    /* Reports related constants*/

    public static final int GPS_TRACKING=2;
    public static final int ENGINE_POWER_UP=101;
    public static final int ENGINE_POWER_DOWN=102;
    public static final int DEVICE_POWER_LOW=103;
    public static final int DEVICE_POWER_LOSS=104;
    public static final int DRIVING=105;
    public static final int ON_DUTY=106;
    public static final int ILC=107;
    public static final int DEVICE_POWER_GAIN=108;
    public static final int MIDNIGHT_EVENT=109;
    public static final int GAUGE_CLUSTER=110;
    public static final int BLUETOOTH_CONNECTED=111;
    public static final int BLUETOOTH_DISCONNECTED=112;
    public static final int GPS_FIXED=113;
    public static final int GPS_NOT_FIXED=114;
    public static final int GAUGE_CLUSTER_SHOW=115;
    public static final int GAUGE_CLUSTER_HIDE=116;

    public static final String CREATEDDATE="CreatedDate";
    public static final String DOCUMENTCONTENTTYPE="DocumentContentType";
    public static final String DOCUMENTID="DocumentId";
    public static final String DOCUMENTNAME="DocumentName";
    public static final String DOCUMENTPATH="DocumentPath";
    public static final String DOCUMENTSIZE="DocumentSize";
    public static final String DOCUMENTTYPE="DocumentType";
    public static final String IMPORTEDDATE="ImportedDate";
    public static final String MODIFIEDDATE="ModifiedDate";

    public static final String STATUSID="StatusId";




}
