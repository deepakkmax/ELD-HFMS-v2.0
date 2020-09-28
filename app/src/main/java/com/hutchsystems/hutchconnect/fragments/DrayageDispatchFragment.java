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

import com.hutchsystems.hutchconnect.R;
import com.hutchsystems.hutchconnect.adapters.DispatchAdapter;
import com.hutchsystems.hutchconnect.beans.DrayageDispatchBean;
import com.hutchsystems.hutchconnect.common.Utility;
import com.hutchsystems.hutchconnect.db.DrayageDispatchDB;

import java.util.ArrayList;

public class DrayageDispatchFragment extends Fragment {

    RecyclerView rvDispatch;
    private OnFragmentInteractionListener mListener;

    public DrayageDispatchFragment() {
        // Required empty public constructor
    }

    // TODO: Rename and change types and number of parameters
    public static DrayageDispatchFragment newInstance() {
        DrayageDispatchFragment fragment = new DrayageDispatchFragment();

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
        View view = inflater.inflate(R.layout.fragment_drayage_dispatch, container, false);
        initialize(view);
        return view;
    }

    private void initialize(View view) {
        rvDispatch = (RecyclerView) view.findViewById(R.id.rvDispatch);
        RecyclerView.LayoutManager layoutManager = new LinearLayoutManager(getContext());
        rvDispatch.setLayoutManager(layoutManager);
        rvDispatch.setItemAnimator(new DefaultItemAnimator());
        ArrayList<DrayageDispatchBean> list = DrayageDispatchDB.Get(Utility.onScreenUserId, Utility.getCurrentDate() + " 00:00:00");
        rvDispatch.setAdapter(new DispatchAdapter(list, mListener));
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
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);

        try {
            LayoutInflater inflater = (LayoutInflater) getActivity().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            ViewGroup viewGroup = (ViewGroup) getView();
            View view = inflater.inflate(R.layout.fragment_drayage_dispatch, viewGroup, false);
            viewGroup.removeAllViews();
            viewGroup.addView(view);
            initialize(view);

        } catch (Exception exe) {
        }

    }

    @Override
    public void onDetach() {
        super.onDetach();
        mListener = null;
    }

    /**
     * This interface must be implemented by activities that contain this
     * fragment to allow an interaction in this fragment to be communicated
     * to the activity and potentially other fragments contained in that
     * activity.
     * <p>
     * See the Android Training lesson <a href=
     * "http://developer.android.com/training/basics/fragments/communicating.html"
     * >Communicating with Other Fragments</a> for more information.
     */
    public interface OnFragmentInteractionListener {
        // TODO: Update argument type and name
        void onDispatchDetail(int id);
        void startDispatch(int dispatchId);
        void stopDispatch(int dispatchId);
    }
}
