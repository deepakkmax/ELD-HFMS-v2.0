package com.hutchsystems.hutchconnect.db.table;

import android.provider.BaseColumns;

/**
 * Created by Deepak Sharma on 7/21/2017.
 */

public class DrayageDispatchContract {
    private DrayageDispatchContract() {
    }

    public static class DrayageDispatch implements BaseColumns {
        public static final String TABLE_NAME = "dispatch";

        public static final String COLUMN_DISPATCHID = "dispatchid";
        public static final String COLUMN_DISPATCHDATE = "dispatchdate";
        public static final String COLUMN_DISPATCHNO = "dispatchno";
        public static final String COLUMN_BOOKINGNO = "bookingno";
        public static final String COLUMN_DRIVERID = "driverid";
        public static final String COLUMN_CUSTOMER = "customer";
        public static final String COLUMN_CUSTOMERADDRESS = "customeraddress";
        public static final String COLUMN_CUSTOMERLATITUDE = "customerlatitude";
        public static final String COLUMN_CUSTOMERLONGITUDE = "customerlongitude";

        public static final String COLUMN_STEAMSHIPLINECOMPANY = "steamshiplinecompany";
        public static final String COLUMN_SSLADDRESS = "ssladdress";
        public static final String COLUMN_SSLLATITUDE = "ssllatitude";
        public static final String COLUMN_SSLLONGITUDE = "ssllongitude";

        public static final String COLUMN_PICKUPCOMPANY = "pickupcompany";
        public static final String COLUMN_PICKUPADDRESS = "pickupaddress";
        public static final String COLUMN_PICKUPLATITUDE = "pickuplatitude";
        public static final String COLUMN_PICKUPLONGITUDE = "pickuplongitude";

        public static final String COLUMN_DROPCOMPANY = "dropcompany";
        public static final String COLUMN_DROPADDRESS = "dropaddress";
        public static final String COLUMN_DROPLATITUDE = "droplatitude";
        public static final String COLUMN_DROPLONGITUDE = "droplongitude";

        public static final String COLUMN_EMPTYRETURNCOMPANY = "emptyreturncompany";
        public static final String COLUMN_EMPTYRETURNADDRESS = "emptyreturnaddress";
        public static final String COLUMN_EMPTYRETURNLATITUDE = "emptyreturnlatitude";
        public static final String COLUMN_EMPTYRETURNLONGITUDE = "emptyreturnlongitude";

        public static final String COLUMN_NOOFCONTAINER = "noofcontainer";
        public static final String COLUMN_NOTES = "notes";
        public static final String COLUMN_STATUS= "status";
        public static final String COLUMN_SYNCFG = "syncfg";

    }

    public static class DrayageDispatchDetail {
        public static final String TABLE_NAME = "dispatchdetail";

        public static final String COLUMN_PICKDROPID = "pickdropid";
        public static final String COLUMN_DISPATCHID = "dispatchid";
        public static final String COLUMN_DISPATCHDETAILID = "dispatchdetailid";
        public static final String COLUMN_APPOINTMENTNO = "appointmentno";
        public static final String COLUMN_APPOINTMENTDATE = "appointmentdate";
        public static final String COLUMN_CONTAINERNO = "containerno";
        public static final String COLUMN_CONTAINERSIZE = "containersize";
        public static final String COLUMN_CONTAINERTYPE = "containertype";
        public static final String COLUMN_CONTAINERGRADE = "containergrade";

        public static final String COLUMN_MAXGROSSWEIGHT = "maxgrossweight";
        public static final String COLUMN_TAREWEIGHT = "tareweight";
        public static final String COLUMN_MAXPAYLOAD = "maxpayload";
        public static final String COLUMN_MANUFACTURINGDATE = "manufacturingdate";
        public static final String COLUMN_SEALNO1 = "sealno1";
        public static final String COLUMN_SEALNO2 = "sealno2";
        public static final String COLUMN_DOCUMENT_PATH = "documentpath";
        public static final String COLUMN_MODIFIEDDATE = "modifieddate";
        public static final String COLUMN_SYNCFG = "syncfg";
    }

    public static class DrayageRouteDetail {
        public static final String TABLE_NAME = "routedetail";

        public static final String COLUMN_DISPATCHID = "dispatchid";
        public static final String COLUMN_PICKDROPID = "pickdropid"; //1- Pickup, 2- Dropw, 3- Return Empty
        public static final String COLUMN_ARRIVALDATE = "arrivaldate";
        public static final String COLUMN_DEPARTUREDATE = "departuredate";
        public static final String COLUMN_SYNCFG = "syncfg";

    }
}
