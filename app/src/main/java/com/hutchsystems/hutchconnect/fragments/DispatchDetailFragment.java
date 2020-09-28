package com.hutchsystems.hutchconnect.fragments;

import android.content.Context;
import android.content.res.Configuration;
import android.os.Bundle;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.DefaultItemAnimator;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.hutchsystems.hutchconnect.R;
import com.hutchsystems.hutchconnect.adapters.DispatchDetailAdapter;
import com.hutchsystems.hutchconnect.beans.DispatchDetailBean;
import com.hutchsystems.hutchconnect.beans.DrayageDispatchBean;
import com.hutchsystems.hutchconnect.db.DrayageDispatchDB;

import java.util.ArrayList;

public class DispatchDetailFragment extends Fragment {

    TextView tvDispatchBookingNo, tvPickupPoint, tvDropPoint, tvEmptyReturnPoint, tvNotes, tvStatus;
    private OnFragmentInteractionListener mListener;
    RecyclerView rvDispatchDetail;
    private int dispatchId;
    ImageView imgPlay, imgAction;

    public DispatchDetailFragment() {
        // Required empty public constructor
    }

    public static final String ARG_DISPATCH_ID = "dispatchid";

    // TODO: Rename and change types and number of parameters
    public static DispatchDetailFragment newInstance(int dispatchId) {
        DispatchDetailFragment fragment = new DispatchDetailFragment();
        Bundle args = new Bundle();
        args.putInt(ARG_DISPATCH_ID, dispatchId);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        dispatchId = getArguments().getInt(ARG_DISPATCH_ID);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_dispatch_detail, container, false);
        initialize(view);
        return view;
    }

    int statusId = 1;

    private void initialize(View view) {
        tvDispatchBookingNo = (TextView) view.findViewById(R.id.tvDispatchBookingNo);
        tvPickupPoint = (TextView) view.findViewById(R.id.tvPickupPoint);
        tvDropPoint = (TextView) view.findViewById(R.id.tvDropPoint);
        tvEmptyReturnPoint = (TextView) view.findViewById(R.id.tvEmptyReturnPoint);
        tvNotes = (TextView) view.findViewById(R.id.tvNotes);
        tvStatus = (TextView) view.findViewById(R.id.tvStatus);
        imgPlay = (ImageView) view.findViewById(R.id.imgPlay);
        imgPlay.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                if (statusId == 1) {
                    mListener.startDispatch(dispatchId);
                    statusId = 2;
                    imgPlay.setImageResource(R.drawable.stop_big_dispatch);
                } else if (statusId == 2) {
                    mListener.stopDispatch(dispatchId);
                    statusId = 3;
                    imgPlay.setVisibility(View.GONE);
                }
            }
        });

        imgAction = (ImageView) view.findViewById(R.id.imgAction);

        rvDispatchDetail = (RecyclerView) view.findViewById(R.id.rvDispatchDetail);
        RecyclerView.LayoutManager layoutManager = new LinearLayoutManager(getContext());
        rvDispatchDetail.setLayoutManager(layoutManager);
        rvDispatchDetail.setItemAnimator(new DefaultItemAnimator());
        ArrayList<DispatchDetailBean> list = DrayageDispatchDB.GetDetailByDispatchId(dispatchId);
        DrayageDispatchBean dispatch = DrayageDispatchDB.GetById(dispatchId);

        String[] status = {"Pending", "In-Progress", "Completed"};
        statusId = dispatch.getStatus();
        tvStatus.setText(status[dispatch.getStatus() - 1]);
        if (statusId == 2) {
            imgAction.setImageResource(R.drawable.in_progress_icon);
            imgPlay.setImageResource(R.drawable.stop_big_dispatch);
        } else if (statusId == 3) {
            imgAction.setImageResource(R.drawable.completed);
            imgPlay.setVisibility(View.GONE);
        } else {
            imgAction.setImageResource(R.drawable.pending_icon);
            imgPlay.setImageResource(R.drawable.play_big_dispatch);
        }
        tvDispatchBookingNo.setText("Dispatch No: " + dispatch.getDispatchNo() + " | Booking No: " + dispatch.getBookingNo());

        tvPickupPoint.setText(dispatch.getPickupCompany() + " - " + dispatch.getPickupAddress());
        tvDropPoint.setText(dispatch.getDropCompany() + " - " + dispatch.getDropAddress());
        if (dispatch.getEmptyReturnCompany().isEmpty()) {
            tvEmptyReturnPoint.setVisibility(View.GONE);
        } else {
            tvEmptyReturnPoint.setVisibility(View.VISIBLE);
            tvEmptyReturnPoint.setText(dispatch.getEmptyReturnCompany() + " - " + dispatch.getEmptyReturnAddress());
        }
        tvNotes.setText(dispatch.getNotes());
        if (dispatch.getNotes().isEmpty()) {
            tvNotes.setVisibility(View.GONE);
        } else {
            tvNotes.setVisibility(View.VISIBLE);
        }
        String[] pickDropList = {dispatch.getPickupCompany() + " - " + dispatch.getPickupAddress(), dispatch.getDropCompany() + " - " + dispatch.getDropAddress(), dispatch.getEmptyReturnCompany() + " - " + dispatch.getEmptyReturnAddress()};
        rvDispatchDetail.setAdapter(new DispatchDetailAdapter(list, pickDropList, mListener));
    }


    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);

        try {
            LayoutInflater inflater = (LayoutInflater) getActivity().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            ViewGroup viewGroup = (ViewGroup) getView();
            View view = inflater.inflate(R.layout.fragment_dispatch_detail, viewGroup, false);
            viewGroup.removeAllViews();
            viewGroup.addView(view);
            initialize(view);

        } catch (Exception exe) {
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

    public interface OnFragmentInteractionListener {
        // TODO: Update argument type and name
        void onAddContainerDetail(int dispatchDetailId);

        void startDispatch(int dispatchId);

        void stopDispatch(int dispatchId);
    }
}
