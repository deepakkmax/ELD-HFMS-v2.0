package com.hutchsystems.hutchconnect.fragments;

import android.content.Context;
import android.net.Uri;
import android.os.Bundle;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentStatePagerAdapter;
import androidx.fragment.app.FragmentTransaction;
import androidx.viewpager.widget.PagerAdapter;
import androidx.viewpager.widget.ViewPager;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.hutchsystems.hutchconnect.R;
import com.hutchsystems.hutchconnect.common.ChatClient;
import com.hutchsystems.hutchconnect.common.Utility;
import com.hutchsystems.hutchconnect.db.DailyLogDB;

import java.util.Date;
import java.util.List;


public class InspectLogFragment extends Fragment  implements ChatClient.SwipeEvent{

    private ViewPager pager;
    private PagerAdapter adapter;
    private OnFragmentInteractionListener mListener;
    private int totalPage = 15;
    private Date startDate;
    private int position = -1;

    public static final String ARG_Position = "position";

    public InspectLogFragment() {
        // Required empty public constructor
    }


    public static InspectLogFragment newInstance() {
        return new InspectLogFragment();

    }

    public static InspectLogFragment newInstance(int position) {
        InspectLogFragment fragment = new InspectLogFragment();
        Bundle args = new Bundle();
        args.putSerializable(ARG_Position, position);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            position = getArguments().getInt(ARG_Position);
        }
    }

    View view = null;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        if (view == null) {
            // Inflate the layout for this fragment
            view = inflater.inflate(R.layout.fragment_inspect_log, container, false);
            initialize(view);
        }
        return view;
    }

/*    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);

        try {
            LayoutInflater inflater = (LayoutInflater) getActivity().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            ViewGroup viewGroup = (ViewGroup) getView();
            View view = inflater.inflate(R.layout.fragment_inspect_log, viewGroup, false);
            viewGroup.removeAllViews();
            viewGroup.addView(view);
            initialize(view);

        } catch (Exception exe) {
        }
    }*/

    private void CheckCountry() {
        try {
            String[] addresses = Utility.FullAddress.split(",");
            if (addresses.length >= 4) {
                if (addresses[3].trim().toUpperCase().equals("US")) {
                    totalPage = 8;
                } else {

                }
            }
        } catch (Exception exe) {

        }
    }

    private void initialize(View view) {

        ChatClient.seListner=this;
        CheckCountry();

        pager = (ViewPager) view.findViewById(R.id.pager);
        adapter = new SlideAdapter(getFragmentManager());
        pager.setAdapter(adapter);
        int currentRule = DailyLogDB.getCurrentRule(Utility.onScreenUserId);
       // if (Utility.InspectorModeFg && currentRule > 2) {
       //     totalPage = 8;
       // }
        startDate = Utility.dateOnlyGet(Utility.newDate());
        startDate = Utility.addDays(startDate, -(totalPage - 1));

        if (position == -1) {
            position = totalPage - 1;
        } else {
            position = totalPage - position - 1;
        }
        pager.setCurrentItem(position);

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
        //pager.setAdapter(null);
        mListener = null;
        try {
            FragmentManager fragmentManager = getFragmentManager();
            List<Fragment> fragments = fragmentManager.getFragments();
            if (fragments != null) {
                FragmentTransaction ft = fragmentManager.beginTransaction();
                for (Fragment f : fragments) {
                    //You can perform additional check to remove some (not all) fragments:
                    if (f instanceof DetailFragment) {
                        ft.remove(f);
                    }
                }

                ft.commitAllowingStateLoss();
            }
        } catch (Exception exe) {
        }
    }

    @Override
    public void onSwipe(final int position) {
        getActivity().runOnUiThread(new Runnable() {
            @Override
            public void run() {
                pager.setCurrentItem(position);
            }
        });
    }

    public interface OnFragmentInteractionListener {
        // TODO: Update argument type and name
        void onFragmentInteraction(Uri uri);
    }

    private class SlideAdapter extends FragmentStatePagerAdapter {
        public SlideAdapter(FragmentManager fm) {
            super(fm);
        }

        @Override
        public Fragment getItem(int position) {
            Date date = Utility.addDays(startDate, position);
            return DetailFragment.newInstance(date);
        }

        @Override
        public int getCount() {
            return totalPage;
        }
    }
}
