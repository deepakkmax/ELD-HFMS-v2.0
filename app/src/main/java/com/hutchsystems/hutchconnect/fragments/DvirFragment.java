package com.hutchsystems.hutchconnect.fragments;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.net.Uri;
import android.os.Bundle;
import androidx.fragment.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.ListView;

import com.hutchsystems.hutchconnect.MainActivity;
import com.hutchsystems.hutchconnect.R;
import com.hutchsystems.hutchconnect.adapters.InspectionAdapter;
import com.hutchsystems.hutchconnect.beans.TripInspectionBean;
import com.hutchsystems.hutchconnect.common.CommonTask;
import com.hutchsystems.hutchconnect.common.LogFile;
import com.hutchsystems.hutchconnect.common.Utility;
import com.hutchsystems.hutchconnect.db.EventDB;
import com.hutchsystems.hutchconnect.db.LogDB;
import com.hutchsystems.hutchconnect.db.TripInspectionDB;

import java.io.File;
import java.util.ArrayList;
import java.util.List;


public class DvirFragment extends Fragment implements View.OnClickListener, InspectionAdapter.ItemClickListener {
    final String TAG = DvirFragment.class.getName();

    ListView lvCurrentInspections;
    ImageButton butNewInspection;
    boolean actionFg = false;

    //List<TripInspectionBean> listInspections;
    InspectionAdapter adapter;

    int driverId = 0;

    private OnFragmentInteractionListener mListener;
    private LinearLayout layoutOption;
    private ImageButton fabTrailer;
    private ImageButton fabPowerUnit;

    public DvirFragment() {
        // Required empty public constructor
        //mInstance = this;
    }


    private void initialize(View view) {
        //driverId = Utility.user1.isOnScreenFg() ? Utility.user1.getAccountId() : Utility.user2.getAccountId();

        lvCurrentInspections = (ListView) view.findViewById(R.id.lvCurrentInspections);
        layoutOption = (LinearLayout) view.findViewById(R.id.layoutOption);

        adapter = new InspectionAdapter(getContext(), this, getListInspections());
        lvCurrentInspections.setAdapter(adapter);

        butNewInspection = (ImageButton) view.findViewById(R.id.btnNewInspection);
        butNewInspection.setOnClickListener(this);

        fabTrailer = (ImageButton) view.findViewById(R.id.fabTrailer);
        fabTrailer.setOnClickListener(this);

        fabPowerUnit = (ImageButton) view.findViewById(R.id.fabPowerUnit);
        fabPowerUnit.setOnClickListener(this);

        if (Utility.InspectorModeFg) {
            butNewInspection.setVisibility(View.GONE);
        }

        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    clearDvir();
                } catch (Exception e) {

                }
            }
        }).start();
    }

    public static DvirFragment newInstance() {
//        if (mInstance == null) {
//            mInstance = new DvirFragment();
//        }
        DvirFragment fragment = new DvirFragment();
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_dvir, container, false);
        initialize(view);
        return view;

    }

    private void clearDvir() {
        String previousDate = Utility.getPreviousDateOnly(-1); // remove dvir older than 2 days
        ArrayList<TripInspectionBean> list = TripInspectionDB.getInspectionsToRemove(previousDate);
        StringBuilder ids = new StringBuilder();
        for (TripInspectionBean trip : list) {
            int id = trip.getId();
            String picture = trip.getPictures();
            try {
                if (!picture.equals("")) {
                    String[] pictures = picture.split(",");
                    for (int i = 0; i < pictures.length; i++) {
                        String path = pictures[i];
                        File file = new File(path);
                        if (file.exists()) {
                            file.delete();
                        }

                        File directory = file.getParentFile();
                        if (directory.isDirectory()) {
                            File[] contents = directory.listFiles();
                            if (contents.length == 0) {
                                directory.delete();
                            }
                        }
                    }
                }
            } catch (Exception exe) {

            }
            ids.append(id + ",");
        }

        if (list.size() > 0) {
            ids.setLength(ids.length() - 1);
            TripInspectionDB.removeDVIR(ids.toString());
        }
    }

    // TODO: Rename method, update argument and hook method into UI event
    public void onButtonPressed(Uri uri) {
        if (mListener != null) {
            //mListener.onFragmentInteraction(uri);
        }
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
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
        mListener = null;
    }

    @Override
    public void onResume() {
        super.onResume();
    }

    @Override
    public void onClick(View view) {
        try {
            switch (view.getId()) {
                case R.id.btnNewInspection:

                    if (mListener != null) {
                        int currentStatus = EventDB.getCurrentDutyStatus(Utility.onScreenUserId);
                        String message = getString(R.string.dvir_add_alert);
                        if (currentStatus == 4 || currentStatus == 5) {
                            showhideAction();
                        } else {
                            Utility.showAlertMsg(message);
                        }
                    }
                    break;
                case R.id.fabTrailer:
                     actionFg =false ;
                     mListener.newInspection(true);
                    break;
                case R.id.fabPowerUnit:
                     actionFg =false ;
                     mListener.newInspection(false);
                    break;
            }
        } catch (Exception e) {
            LogFile.write(DvirFragment.class.getName() + "::onClick Error: " + e.getMessage(), LogFile.USER_INTERACTION, LogFile.ERROR_LOG);
            LogDB.writeLogs(DvirFragment.class.getName(),"::onClick Error:" ,e.getMessage(),Utility.printStackTrace(e));

        }
    }

    private void showhideAction() {
        if (!actionFg) {
            final Animation animationFadeIn = AnimationUtils.loadAnimation(getContext(), R.anim.fade_in);
            layoutOption.setVisibility(View.VISIBLE);
            android.animation.ObjectAnimator.ofFloat(butNewInspection, "rotation", 0, 45).start();
            layoutOption.startAnimation(animationFadeIn);
        } else {
            final Animation animationFadeOut = AnimationUtils.loadAnimation(getContext(), R.anim.fade_out);
            layoutOption.setVisibility(View.INVISIBLE);
            android.animation.ObjectAnimator.ofFloat(butNewInspection, "rotation", 45, 0).start();
            layoutOption.startAnimation(animationFadeOut);

        }
        actionFg = !actionFg;
    }

    @Override
    public void viewInspection(TripInspectionBean bean){
        Log.d(TAG, "View the inspection id:" + bean.getId() + " type " + bean.getType());
        if (mListener != null) {
            mListener.viewInspection(true, bean);
        }
    }

    @Override
    public void onClickOfDelete(View v,final int postion,final TripInspectionBean tripinfo) {
        final AlertDialog ad = new AlertDialog.Builder(getContext())
                .create();
        ad.setCancelable(true);
        ad.setCanceledOnTouchOutside(false);
        ad.setTitle(getString(R.string.dvir_title));
        ad.setIcon(R.drawable.ic_launcher);
        ad.setMessage(getString(R.string.dvir_alert_message));

        ad.setButton(DialogInterface.BUTTON_POSITIVE, getString(R.string.yes),
                new DialogInterface.OnClickListener() {

                    @Override
                    public void onClick(DialogInterface dialog,
                                        int which) {

                        // get the position of the the list and delete that item which is user selected
                        TripInspectionDB.DeleteDVIR(tripinfo.getId());
                        adapter.changeItems(getListInspections());

                        MainActivity.postData(CommonTask.Post_Dvir);
                        ad.cancel();


                    }
                });
        ad.setButton(DialogInterface.BUTTON_NEGATIVE, getString(R.string.no),
                new DialogInterface.OnClickListener() {

                    @Override
                    public void onClick(DialogInterface dialog,
                                        int which) {
                        ad.cancel();
                    }
                });

        ad.show();
    }

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


    //these callbacks are used to call Activity update the fragment with new data or change to another fragment
    public interface OnFragmentInteractionListener {
        // TODO: Update argument type and name
        //void onFragmentInteraction(Uri uri);
        void onUpdateInspectionIcon();

        // isTrailerDVIR for trailer dvir
        void newInspection(Boolean isTrailerDVIR);

        void viewInspection(boolean viewMode, TripInspectionBean bean);
    }


}
