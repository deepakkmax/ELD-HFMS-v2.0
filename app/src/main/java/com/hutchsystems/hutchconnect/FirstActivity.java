package com.hutchsystems.hutchconnect;

import android.app.AppOpsManager;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Build;
import android.provider.Settings;

import androidx.core.app.ActivityCompat;

import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;

import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;

import com.hutchsystems.hutchconnect.adapters.PermissionAdapter;
import com.hutchsystems.hutchconnect.beans.PermissionBean;
import com.hutchsystems.hutchconnect.common.ConstantFlag;
import com.hutchsystems.hutchconnect.common.GetCall;
import com.hutchsystems.hutchconnect.common.Utility;
import com.hutchsystems.hutchconnect.db.CarrierInfoDB;

import java.util.ArrayList;

public class FirstActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_first);
        Utility.context = this;
        initialize();
    }


    Button btnAllowAccess;
    ListView lvPermission;
    TextView tvAppVersion, tvTitle, tvMessage;
    LinearLayout layoutStatus;

    private void initialize() {
        layoutStatus = (LinearLayout) findViewById(R.id.layoutStatus);
        layoutStatus.setVisibility(View.GONE);
        tvTitle = (TextView) findViewById(R.id.tvTitle);
        tvMessage = (TextView) findViewById(R.id.tvMessage);
        tvAppVersion = (TextView) findViewById(R.id.tvAppVersion);
        btnAllowAccess = (Button) findViewById(R.id.btnAllowAccess);
        btnAllowAccess.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (!Utility.hasPermissions(Utility.PERMISSIONS)) {
                    ActivityCompat.requestPermissions(FirstActivity.this, Utility.PERMISSIONS, Utility.PERMISSION_ALL);
                } else if (!Settings.System.canWrite(FirstActivity.this)) {
                    checkWriteSetting();
                } else if (!ConstantFlag.HutchConnectFg) {
                    requestReadNetworkHistoryAccess();
                }
            }
        });
        lvPermission = (ListView) findViewById(R.id.lvPermission);
        lvPermission.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                PermissionBean obj = (PermissionBean) adapterView.getItemAtPosition(i);
                if (obj.getName().equals("WriteSystemSetting")) {
                    checkWriteSetting();
                } else if (obj.getName().equals("DataUsagePermission")) {
                    requestReadNetworkHistoryAccess();
                } else {

                    ActivityCompat.requestPermissions(FirstActivity.this, new String[]{obj.getName()}, Utility.PERMISSION_ALL);
                }
            }
        });
        Utility.VersionGet(this);
        tvAppVersion.setText(getString(R.string.version) + " " + Utility.ApplicationVersion);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
    }

    @Override
    protected void onResume() {
        super.onResume();
        ArrayList<PermissionBean> permission = Utility.hasAllPermissions();

        if (permission.size() > 0) {
            PermissionAdapter adapter = new PermissionAdapter(getApplicationContext(), R.layout.activity_first, permission);
            lvPermission.setAdapter(adapter);
        } else {

            SharedPreferences prefs = this.getSharedPreferences("HutchGroup", MODE_PRIVATE);
            if (prefs.getBoolean("firstrun", true)) {
                startActivity(new Intent(FirstActivity.this, SetupActivity.class));
                finish();
            } else {

                if (checkStatus()) {
                    finish();
                } else {
                    layoutStatus.setVisibility(View.VISIBLE);
                    new Thread(new Runnable() {
                        @Override
                        public void run() {
                            GetCall.CarrierInfoSync();

                        }
                    }).start();
                }

            }


        }
    }

    // Created By: Deepak Sharma
    // Created Date: 10 July 2019
    // Purpose: check status of company, vehicle, device and unidentified driver
    private boolean checkStatus() {

        boolean status = false;
        String errorMessage = "";
        String title = "";

        if (CarrierInfoDB.duplicateDeviceAssignment()) {
            title = "Duplicate Device Assignment!";
            errorMessage = getString(R.string.duplicate_device_alert);
        } else if (Utility.CompanyStatusId != 1) {
            title = "Company is not active!";
            errorMessage = getString(R.string.company_active_alert);
        } else if (Utility.VehicleStatusId != 1) {
            title = "Vehicle is not active!";
            errorMessage = getString(R.string.vehicle_active_alert);
        } else if (!(Utility.DeviceStatus == 1)) {
            title = "Device is not active!";
            errorMessage = getString(R.string.device_active_alert);
        } else if (Utility.LatePaymentFg) {
            title = "Payment Late!";
            errorMessage = getString(R.string.late_payment_alert);

        } else {
            status = true;
        }

        if (!status) {
            tvTitle.setText(title);
            tvMessage.setText(errorMessage);
        }
        return status;
    }

    @Override
    protected void onStart() {
        super.onStart();
    }


    private void checkWriteSetting() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (!Settings.System.canWrite(getApplicationContext())) {
                Intent intent = new Intent(Settings.ACTION_MANAGE_WRITE_SETTINGS);
                intent.setData(Uri.parse("package:" + getPackageName()));
                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                startActivity(intent);
            }
        }
    }

    private void requestReadNetworkHistoryAccess() {

        final AppOpsManager appOps = (AppOpsManager) getSystemService(Context.APP_OPS_SERVICE);
        int mode = appOps.checkOpNoThrow(AppOpsManager.OPSTR_GET_USAGE_STATS,
                android.os.Process.myUid(), getPackageName());
        if (mode != AppOpsManager.MODE_ALLOWED) {
            Intent intent = new Intent(Settings.ACTION_USAGE_ACCESS_SETTINGS);
            //intent.setData(Uri.parse("package:" + getPackageName()));
            startActivity(intent);
        }
    }
}
