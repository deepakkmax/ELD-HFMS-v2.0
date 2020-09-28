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

import com.hutchsystems.hutchconnect.R;
import com.hutchsystems.hutchconnect.adapters.RuleAdapter;
import com.hutchsystems.hutchconnect.beans.RuleBean;
import com.hutchsystems.hutchconnect.common.ChatClient;
import com.hutchsystems.hutchconnect.common.Utility;
import com.hutchsystems.hutchconnect.db.DailyLogDB;

import java.util.ArrayList;

public class RuleListFragment extends Fragment implements ChatClient.RefreshData {

    private OnFragmentInteractionListener mListener;
    RecyclerView rvRule;
    RuleAdapter adapter;

    public RuleListFragment() {
        // Required empty public constructor
    }

    private void initialize(View view) {

        ChatClient.rListner = this;
        rvRule = (RecyclerView) view.findViewById(R.id.rvRule);
        RecyclerView.LayoutManager mLayoutManager = new LinearLayoutManager(getContext());
        rvRule.setLayoutManager(mLayoutManager);
        rvRule.setItemAnimator(new DefaultItemAnimator());

        ruleGet();
    }

    private void ruleGet() {
        data = DailyLogDB.getRuleByDriverId(Utility.onScreenUserId);
        adapter = new RuleAdapter(data);
        rvRule.setAdapter(adapter);
    }

    public static RuleListFragment newInstance() {
        RuleListFragment fragment = new RuleListFragment();
        Bundle args = new Bundle();
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_rule_list, container, false);
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

    public interface OnFragmentInteractionListener {
        // TODO: Update argument type and name
        void onFragmentInteraction(Uri uri);
    }
    @Override
    public void onCallPost() {

    }

    @Override
    public void onCallSync() {

    }

    ArrayList<RuleBean> data = new ArrayList<>();
    @Override
    public void onRefresh() {
        getActivity().runOnUiThread(new Runnable() {
            @Override
            public void run() {
                data.clear();
                data.addAll(DailyLogDB.getRuleByDriverId(Utility.onScreenUserId));
                adapter.notifyDataSetChanged();
            }
        });
    }

}
