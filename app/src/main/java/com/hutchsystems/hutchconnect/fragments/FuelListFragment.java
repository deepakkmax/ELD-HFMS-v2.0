package com.hutchsystems.hutchconnect.fragments;

import android.content.Context;
import android.net.Uri;
import android.os.Bundle;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.DefaultItemAnimator;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;

import com.hutchsystems.hutchconnect.R;
import com.hutchsystems.hutchconnect.adapters.FuelDetailAdapter;
import com.hutchsystems.hutchconnect.beans.FuelDetailBean;
import com.hutchsystems.hutchconnect.common.SwipeHolder;
import com.hutchsystems.hutchconnect.common.SwipeOnItemTouchAdapter;
import com.hutchsystems.hutchconnect.common.Utility;
import com.hutchsystems.hutchconnect.db.EventDB;
import com.hutchsystems.hutchconnect.db.FuelDetailDB;

import java.util.ArrayList;

public class FuelListFragment extends Fragment implements FuelDetailAdapter.IFuel {
    RecyclerView rvFuel;
    ImageButton btnNewFuel;
    FuelDetailAdapter adapter;
    private OnFragmentInteractionListener mListener;

    public FuelListFragment() {
        // Required empty public constructor
    }

    public static FuelListFragment newInstance() {
        FuelListFragment fragment = new FuelListFragment();
        return fragment;
    }

    private void initialize(View view) {
        FuelDetailAdapter.mlistner = this;
        rvFuel = (RecyclerView) view.findViewById(R.id.rvFuel);
        RecyclerView.LayoutManager mLayoutManager = new LinearLayoutManager(getContext());
        rvFuel.setLayoutManager(mLayoutManager);
        rvFuel.setItemAnimator(new DefaultItemAnimator());
        btnNewFuel = (ImageButton) view.findViewById(R.id.btnNewFuel);
        btnNewFuel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                int currentStatus = EventDB.getCurrentDutyStatus(Utility.onScreenUserId);
                String message = getString(R.string.add_fuel_onduty_alert);
                if (currentStatus == 4 || currentStatus == 5) {
                    mListener.onAddFuel();
                } else {
                    Utility.showAlertMsg(message);
                }
            }
        });

        fuelGet();
        new Thread(new Runnable() {
            @Override
            public void run() {
                FuelDetailDB.deleteDocument();
            }
        }).start();

        rvFuel.addOnItemTouchListener(new SwipeOnItemTouchAdapter(getActivity(), rvFuel, mLayoutManager) {
            @Override
            public void onItemHiddenClick(SwipeHolder swipeHolder, int position) {

                //call reset to hide.
                swipeHolder.reset();
            }

            @Override
            public void onItemClick(int position) {

            }
        });
    }

    private void fuelGet() {
        String previousDate = Utility.getPreviousDateOnly(-15);
        ArrayList<FuelDetailBean> data = FuelDetailDB.getFuelDetail(previousDate);
        adapter = new FuelDetailAdapter(data,getActivity());
        rvFuel.setAdapter(adapter);
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_fuel_list, container, false);
        initialize(view);
        return view;
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
    public void onClick(int id) {
        if (mListener != null) {
            mListener.onViewFuel(id);
        }

    }

    public interface OnFragmentInteractionListener {
        // TODO: Update argument type and name
        void onFragmentInteraction(Uri uri);

        void onAddFuel();

        void onViewFuel(int id);
    }


//    @Override
//    public void onConfigurationChanged(Configuration newConfig) {
//        super.onConfigurationChanged(newConfig);
//
//        try {
//            LayoutInflater inflater = (LayoutInflater) getActivity().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
//            ViewGroup viewGroup = (ViewGroup) getView();
//            View view = inflater.inflate(R.layout.fragment_fuel_list, viewGroup, false);
//            viewGroup.removeAllViews();
//            viewGroup.addView(view);
//            initialize(view);
//
//        } catch (Exception exe) {
//        }
//
//    }
}
