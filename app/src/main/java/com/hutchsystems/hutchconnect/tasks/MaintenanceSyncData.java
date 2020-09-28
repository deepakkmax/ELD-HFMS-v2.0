package com.hutchsystems.hutchconnect.tasks;

import android.os.AsyncTask;

import com.hutchsystems.hutchconnect.common.GetCall;
import com.hutchsystems.hutchconnect.common.Utility;

/**
 * Created by Deepak Sharma on 2/7/2017.
 */

public class MaintenanceSyncData extends AsyncTask<Void, Void, Boolean> {

    @Override
    protected void onPreExecute() {
        super.onPreExecute();
    }

    @Override
    protected Boolean doInBackground(Void... voids) {
       // GetCall.DeviceBalanceGet();
        GetCall.CarrierInfoSync();
        GetCall.AccountSync(0);
        GetCall.CardDetailGetSync();

        return true;
    }

    @Override
    protected void onPostExecute(Boolean result) {
        super.onPostExecute(result);
        if (result) {
            Utility.savePreferences("post_daily", Utility.getCurrentDate());
        }
    }
}
