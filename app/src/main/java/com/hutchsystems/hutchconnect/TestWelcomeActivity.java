package com.hutchsystems.hutchconnect;

import android.os.Bundle;
import androidx.appcompat.app.AppCompatActivity;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;

import androidx.constraintlayout.widget.ConstraintLayout;

import com.hutchsystems.hutchconnect.adapters.InspectionAdapter;
import com.hutchsystems.hutchconnect.beans.TripInspectionBean;
import com.hutchsystems.hutchconnect.common.Utility;
import com.hutchsystems.hutchconnect.db.TripInspectionDB;
import com.hutchsystems.hutchconnect.fragments.DvirFragment;

import java.util.List;

public class TestWelcomeActivity extends AppCompatActivity {

    TextView tvTitle, tvDescription, tvNotes;
    ImageView butGo;
    LinearLayout layoutInstruction;
    ConstraintLayout layoutSettings;

    ListView lvData;
    Button butProceed;

    final String TAG = DvirFragment.class.getName();

    ListView lvCurrentInspections;
    ImageButton butNewInspection;
    boolean actionFg = false;

    //List<TripInspectionBean> listInspections;
    InspectionAdapter adapter;

    int driverId = 0;

    private DvirFragment.OnFragmentInteractionListener mListener;
    private LinearLayout layoutOption;
    private ImageButton fabTrailer;
    private ImageButton fabPowerUnit;

    private List<TripInspectionBean> getListInspections() {
        String previousDate = Utility.getPreviousDateOnly(-1);
        List<TripInspectionBean> listInspections = TripInspectionDB.getInspections(previousDate);

        boolean inspections = TripInspectionDB.getInspections(Utility.getCurrentDate(), Utility.onScreenUserId);
        if (inspections) {
            if (mListener != null)
                mListener.onUpdateInspectionIcon();
        }
        return listInspections;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_new_inspection);

        // dvir Screen
//        lvCurrentInspections = (ListView) findViewById(R.id.lvCurrentInspections);
//        layoutOption = (LinearLayout) findViewById(R.id.layoutOption);
//
//        adapter = new InspectionAdapter(this, null, getListInspections());
//        lvCurrentInspections.setAdapter(adapter);
//
//        butNewInspection = (ImageButton) findViewById(R.id.btnNewInspection);
//
//        fabTrailer = (ImageButton) findViewById(R.id.fabTrailer);
//
//        fabPowerUnit = (ImageButton) findViewById(R.id.fabPowerUnit);
//
//        ArrayList<VehicleBean> hookedList = TrailerDB.getHookedVehicleInfo();
//        hookedList.remove(0);
//        if(hookedList.size() ==0)
//        {
//            fabTrailer.setEnabled(false);
//        }
//
//
//        if (Utility.InspectorModeFg) {
//            butNewInspection.setVisibility(View.GONE);
//        }


        // setup complete screem
//        lvData = (ListView) findViewById(R.id.lvData);
//          /*  tvAppVersion = (TextView) view.findViewById(R.id.tvAppVersion);
//            tvAppVersion.setText(getString(R.string.version) + " " + Utility.ApplicationVersion);*/
//        String[] dataItems = {"Internet Connection Check", "Hutch Systems Connection Check", "Downloading Configuration", "Bluetooth Check", "BTB Connection Check", "BTB Heartbeat Check", "GPS Satellite Check", "UTC Time Check", "GPS Coordinates Check", "Current Location Check"};
//        SetupCompleteAdapter adapter = new SetupCompleteAdapter(this, R.layout.setup_activity, dataItems);
//        lvData.setAdapter(adapter);
//        butProceed = (Button) findViewById(R.id.butProceed);
//        butProceed.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View view) {
//
//            }
//        });

        // welcome screem
//        tvTitle = (TextView) findViewById(R.id.tvTitle);
//        tvDescription = (TextView) findViewById(R.id.tvDescription);
//        tvNotes = (TextView) findViewById(R.id.tvNotes);
//        tvNotes.setVisibility(View.GONE);
//        tvDescription.setVisibility(View.VISIBLE);
//        layoutInstruction = (LinearLayout) findViewById(R.id.layoutInstruction);
//        layoutInstruction.setVisibility(View.VISIBLE);
//        layoutSettings = (ConstraintLayout) findViewById(R.id.layoutSettings);
//        layoutSettings.setVisibility(View.GONE);
//        butGo = (ImageView) findViewById(R.id.butGo);
//        butGo.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View view) {
//                layoutInstruction.setVisibility(View.GONE);
//                layoutSettings.setVisibility(View.VISIBLE);
//                tvTitle.setText(R.string.configuration);
//                tvTitle.setCompoundDrawablesWithIntrinsicBounds(R.drawable.settings, 0, 0, 0);
//                tvDescription.setText("Please enter installer id provided by hutch systems. If you are setting up ELD for Trial Client then turn off Production Mode. and for light duty vehicle turn off Heavy Duty.");
//                tvDescription.setVisibility(View.GONE);
//                tvNotes.setVisibility(View.VISIBLE);
//            }
//        });
    }
}