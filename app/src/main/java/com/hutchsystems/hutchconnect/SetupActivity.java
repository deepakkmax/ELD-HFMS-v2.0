package com.hutchsystems.hutchconnect;

/*
 * SetupActivity
 * Purpose: To check and make sure ELD application's requirement is match before running the ELD
 */

import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.ActivityInfo;
import android.content.res.Configuration;
import android.os.Bundle;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;
import androidx.appcompat.app.AppCompatActivity;

import com.hutchsystems.hutchconnect.common.Utility;
import com.hutchsystems.hutchconnect.fragments.BTBConnectionFragment;
import com.hutchsystems.hutchconnect.fragments.CanBusDataFragment;
import com.hutchsystems.hutchconnect.fragments.GpsSignalFragment;
import com.hutchsystems.hutchconnect.fragments.SummaryFragment;
import com.hutchsystems.hutchconnect.fragments.WelcomeSetupFragment;
import com.hutchsystems.hutchconnect.fragments.WirelessConnectivityFragment;


public class SetupActivity extends AppCompatActivity implements WelcomeSetupFragment.OnFragmentInteractionListener,
        WirelessConnectivityFragment.OnFragmentInteractionListener,
        CanBusDataFragment.OnFragmentInteractionListener,
        BTBConnectionFragment.OnFragmentInteractionListener,
        GpsSignalFragment.OnFragmentInteractionListener,
        SummaryFragment.OnFragmentInteractionListener {

    String TAG = SetupActivity.class.getName();
    SharedPreferences prefs = null;

    SummaryFragment summaryFragment;

    /*
     * Review date: Jun 29 2016
     * Reviewed by: Minh Tran
     * Purpose: create activity for setup
     * Input: the Bundle to get data transfer from the other Activity (it's required by Android)
     * Output: no output
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //saving the context to check and grant permissions
        Utility.context = this;
        //check and grant permisstions
        //  Utility.checkAndGrantPermissions();
        //get IMEI of the device
        Utility.IMEIGet();

        //check if device is phone, set the application support only Portrait mode
        if (!Utility.isLargeScreen(getApplicationContext())) {
            //set the orientation of application to Portrait mode
            setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
        }

        //setup layout for the Activity
        setContentView(R.layout.setup_activity);

        //get fragment manager to add Fragment into Activity
        FragmentManager manager = getSupportFragmentManager();
        //create Welcome fragment to add it in
        WelcomeSetupFragment fragment = new WelcomeSetupFragment();

        //get the transaction to add fragment
        FragmentTransaction ft = manager.beginTransaction();
        //call replace to add fragment into the layout
        ft.replace(R.id.container, fragment);
        //set the transition if open for fragment
        ft.setTransition(FragmentTransaction.TRANSIT_FRAGMENT_OPEN);
        //commit the transaction to show fragment
        ft.commit();

        //create summary fragment to save status of setup progress
        summaryFragment = new SummaryFragment();
    }

    @Override
    protected void onResume() {
        super.onResume();

    }

    /*
     * Review date: Jun 29 2016
     * Reviewed by: Minh Tran
     * Purpose: catch the event when user change orientation of device
     * Input: the new configuration (it is from Android)
     * Output: no output
     */
    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);

        //check if it is phone, call return for no handle configuration in fragment
        if (!Utility.isLargeScreen(getApplicationContext())) {
            return;
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
    }



    /*
     * Review date: Jun 29 2016
     * Reviewed by: Minh Tran
     * Purpose: call from interface to go to WirelessConnectivity screen
     * Input: no input
     * Output: no output
     */
    @Override
    public void onNextToWirelessConnectivity() {
        //create Wireless fragment
        WirelessConnectivityFragment fragment = new WirelessConnectivityFragment();

        //call replace to show the fragment
        replaceFragment(fragment);
    }

    /*
     * Review date: Jun 29 2016
     * Reviewed by: Minh Tran
     * Purpose: call from interface to go to Bluetooth Connectivity screen or GPS checking
     * Input: no input
     * Output: no output
     */
    @Override
    public void onNextFromWirelessConnectivity() {
        proceedToELD();
    }

    /*
     * Review date: Jun 29 2016
     * Reviewed by: Minh Tran
     * Purpose: call from interface to show data from CanBus
     * Input: no input
     * Output: no output
     */
    @Override
    public void onNextToCanbusRead() {
        //create canbus fragment to show data
        CanBusDataFragment fragment = new CanBusDataFragment();

        //call replace to show the fragment
        replaceFragment(fragment);
    }

    /*
     * Review date: Jun 29 2016
     * Reviewed by: Minh Tran
     * Purpose: call from interface to show GPS checking screen
     * Input: no input
     * Output: no output
     */
    @Override
    public void onNextFromCanBusDataFragment() {
        //create GPS checking fragment
        GpsSignalFragment fragment = new GpsSignalFragment();

        //call replace to show the fragment
        replaceFragment(fragment);
    }

    /*
     * Review date: Jun 29 2016
     * Reviewed by: Minh Tran
     * Purpose: call from interface to go to summary screen
     * Input: no input
     * Output: no output
     */
    @Override
    public void onNextToSummary() {
        //call replace to show Summary
        replaceFragment(summaryFragment);
    }

    @Override
    public void onFinishSetup() {
        proceedToELD();
    }
    /*
     * Review date: Jun 29 2016
     * Reviewed by: Minh Tran
     * Purpose: replace function to show the fragment
     * Input: fragment to display
     * Output: no output
     */
    private void replaceFragment(Fragment fragment) {
        //get fragment manager to add Fragment into Activity
        FragmentManager manager = getSupportFragmentManager();

        //get the transaction to add fragment
        FragmentTransaction ft = manager.beginTransaction();
        //call replace to add fragment into the layout
        ft.replace(R.id.container, fragment);
        //set the transition if open for fragment
        ft.setTransition(FragmentTransaction.TRANSIT_FRAGMENT_OPEN);
        //commit the transaction to show fragment
        ft.commitAllowingStateLoss();
    }

    /*
     * Review date: Jun 29 2016
     * Reviewed by: Minh Tran
     * Purpose: call from interface to go to ELD
     * Input: no input
     * Output: no output
     */
    @Override
    public void proceedToELD() {
        //get the preference of the application
        prefs = this.getSharedPreferences("HutchGroup", getBaseContext().MODE_PRIVATE);
        //save the value false for firstrun
        prefs.edit().putBoolean("firstrun", false).commit();
        Utility.restart(getApplicationContext(), 3000);
    }
    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        Fragment fragment = getSupportFragmentManager().findFragmentById(R.id.container);
        //send result to welcome fragment
        if(fragment instanceof WelcomeSetupFragment){
            fragment.onActivityResult(requestCode, resultCode, data);
        }

    }

}
