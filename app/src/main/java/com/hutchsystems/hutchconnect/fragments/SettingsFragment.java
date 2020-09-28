package com.hutchsystems.hutchconnect.fragments;

import android.content.Context;
import android.content.pm.PackageInfo;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;

import com.google.android.material.tabs.TabLayout;
import com.hutchsystems.hutchconnect.R;
import com.hutchsystems.hutchconnect.common.CanMessages;
import com.hutchsystems.hutchconnect.common.ConstantFlag;
import com.hutchsystems.hutchconnect.common.LogFile;
import com.hutchsystems.hutchconnect.common.Utility;
import com.hutchsystems.hutchconnect.db.LogDB;

import androidx.fragment.app.Fragment;

public class SettingsFragment extends Fragment implements View.OnClickListener {
    final String TAG = SettingsFragment.class.getName();

    int driverId = 0;
    int batteryLevel;

    ScrollView layoutTimeZone;
    LinearLayout layoutSettings;
    ScrollView layoutSettingsScroll;
    ScrollView layoutSystemInfor;
    TextView tvAndroidVersion;
    TextView tvAppVersion;
    TextView tvIMEI;
    TextView tvBatteryLevel;
    TextView tvBluetoothName, tvBTBMacAddress, tvProductCode;
    Button butBack;
    ImageView ivAppVersion;
    TabLayout tabLayout;

    ScrollableViewpager viewPager;
    PagerAdapter pagerAdapter;

    Handler handler = new Handler();
    //update battery level in information of the system
    Runnable updateBattery = new Runnable() {
        @Override
        public void run() {
            if (tvBatteryLevel != null) {
                tvBatteryLevel.setText(Utility.BatteryLevel + "%");
            }

            handler.postDelayed(this, 30000);
        }
    };

    private OnFragmentInteractionListener mListener;

    public SettingsFragment() {
    }


    private void initialize(View view) {
        try {
            Log.i(TAG, "initialize the view");
            //using TabLayout to show 3 tabs: System, Display, and Sound
            tabLayout = (TabLayout) view.findViewById(R.id.tab_layout);
            tabLayout.addTab(tabLayout.newTab().setText(R.string.system));
            tabLayout.addTab(tabLayout.newTab().setText(R.string.display));
            tabLayout.addTab(tabLayout.newTab().setText(R.string.sound));

            //using ViewPager to support sliding for changing tab
            viewPager = view.findViewById(R.id.pager);
            //using custom adapter to change size of displayed tab
            pagerAdapter = new PagerAdapter(getChildFragmentManager(), this);
            viewPager.setAdapter(pagerAdapter);
            viewPager.addOnPageChangeListener(new TabLayout.TabLayoutOnPageChangeListener(tabLayout));

            tabLayout.setOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
                @Override
                public void onTabSelected(TabLayout.Tab tab) {
                    viewPager.setCurrentItem(tab.getPosition());
                }

                @Override
                public void onTabUnselected(TabLayout.Tab tab) {

                }

                @Override
                public void onTabReselected(TabLayout.Tab tab) {

                }
            });

            //use another layout for selecting timezone, should change it to Spinner to match with the FirstUser screen
            layoutSettings = (LinearLayout) view.findViewById(R.id.layoutSettings);
            layoutSettingsScroll = (ScrollView) view.findViewById(R.id.layoutSettingsScroll);
            layoutSettingsScroll.setVisibility(View.VISIBLE);

            layoutSystemInfor = (ScrollView) view.findViewById(R.id.layoutSystemInfor);
            ivAppVersion = (ImageView) view.findViewById(R.id.ivAppVersion);
            ivAppVersion.setImageResource(ConstantFlag.ELDFg ? R.drawable.ic_setting_eld_ver1 : R.drawable.ic_setting_aobrd_ver);
            tvAndroidVersion = (TextView) view.findViewById(R.id.tvAndroidVersion);
            String androidOS = Build.VERSION.RELEASE;
            tvAndroidVersion.setText(androidOS);
            tvAppVersion = (TextView) view.findViewById(R.id.tvAppVersion);
            try {
                PackageInfo pInfo = getActivity().getPackageManager().getPackageInfo(getActivity().getPackageName(), 0);
                String version = pInfo.versionName;
                tvAppVersion.setText(version);
            } catch (Exception e) {

            }

            tvIMEI = (TextView) view.findViewById(R.id.tvIMEI);
            tvIMEI.setText(Utility.IMEI);
            tvIMEI.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    Utility.executeQuery();
                }
            });

            tvBatteryLevel = (TextView) view.findViewById(R.id.tvBatteryLevel);
            tvBatteryLevel.setText(batteryLevel + "%");
            tvBluetoothName = (TextView) view.findViewById(R.id.tvBluetoothName);
            String BTName = "Hutch Connect Device";

            // if device is not connected
            if (CanMessages.deviceName != null) {
                BTName = CanMessages.deviceName;
            }
            tvBluetoothName.setText(BTName);

            tvBTBMacAddress = (TextView) view.findViewById(R.id.tvBTBMacAddress);
            tvBTBMacAddress.setText(Utility.MACAddress);

            tvProductCode = (TextView) view.findViewById(R.id.tvProductCode);
            tvProductCode.setText(Utility.EldIdentifier);

            butBack = (Button) view.findViewById(R.id.butBack);
            butBack.setOnClickListener(this);

            driverId = Utility.user1.isOnScreenFg() ? Utility.user1.getAccountId() : Utility.user2.getAccountId();
        } catch (Exception e) {
            LogFile.write(SettingsFragment.class.getName() + "::initialize error: " + e.getMessage(), LogFile.USER_INTERACTION, LogFile.ERROR_LOG);
            LogDB.writeLogs(SettingsFragment.class.getName(), "initialize", e.getMessage(), Utility.printStackTrace(e));

        }

    }

    public static SettingsFragment newInstance() {
        SettingsFragment fragment = new SettingsFragment();
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        Log.i(TAG, "onCreateView");
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_settings, container, false);
        initialize(view);

        handler.postDelayed(updateBattery, 50);
        return view;
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        Log.i(TAG, "onAttach");
        if (context instanceof OnFragmentInteractionListener) {
            mListener = (OnFragmentInteractionListener) context;
        } else {
            throw new RuntimeException(context.toString()
                    + " must implement OnFragmentInteractionListener");
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();
        Log.i(TAG, "Detach");
        mListener = null;
        if (handler != null) {
            handler.removeCallbacksAndMessages(null);
        }
    }

    @Override
    public void onResume() {
        super.onResume();

    }

    @Override
    public void onClick(View view) {
        try {
            switch (view.getId()) {
                case R.id.butBack:
                    layoutSystemInfor.setVisibility(View.GONE);
                    layoutSettingsScroll.setVisibility(View.VISIBLE);
                    butBack.setVisibility(View.GONE);
                    break;
            }
        } catch (Exception e) {
            LogFile.write(SettingsFragment.class.getName() + "::onClick Error: " + e.getMessage(), LogFile.USER_INTERACTION, LogFile.ERROR_LOG);
            LogDB.writeLogs(SettingsFragment.class.getName(), "onClick", e.getMessage(), Utility.printStackTrace(e));


        }
    }


    /**
     * This interface must be implemented by activities that contain this
     * fragment to allow an interaction in this fragment to be communicated
     * to the activity and potentially other fragments contained in that
     * activity.
     * <p/>
     * See the Android Training lesson <a href=
     * "http://developer.android.com/training/basics/fragments/communicating.html"
     * >Communicating with Other Fragments</a> for more information.
     */
    public interface OnFragmentInteractionListener {
        // TODO: Update argument type and name
        //void onFragmentInteraction(Uri uri);
    }


    public void showSystemInformation() {
        layoutSystemInfor.setVisibility(View.VISIBLE);
        layoutSettingsScroll.setVisibility(View.GONE);
        butBack.setVisibility(View.VISIBLE);
    }
}
