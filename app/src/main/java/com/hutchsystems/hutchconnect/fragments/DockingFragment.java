package com.hutchsystems.hutchconnect.fragments;

import android.content.Context;
import android.os.Bundle;
import androidx.fragment.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

import com.hutchsystems.hutchconnect.R;


public class DockingFragment extends Fragment implements View.OnClickListener {
    static DockingFragment mInstance = null;

    private OnFragmentInteractionListener mListener;

    Button butDock;

    public DockingFragment() {
        // Required empty public constructor
    }

    private void initialize(View view) {
        butDock = (Button) view.findViewById(R.id.btnDock);
        butDock.setOnClickListener(this);
    }

    public static DockingFragment newInstance() {
        //InputFragment fragment = new InputFragment();
        if (mInstance == null) {
            mInstance = new DockingFragment();
        }
        return mInstance;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.activity_docking, container, false);

        initialize(view);
        return view;
    }

    @Override
    public void onClick(View view) {
        switch ( view.getId() ) {
            case R.id.btnDock:
                if (mListener != null) {
                    mListener.onDocking();
                }

                break;
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
        //void onFragmentInteraction(Uri uri);
        void onDocking();
    }
}
