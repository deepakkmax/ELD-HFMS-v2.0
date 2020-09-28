package com.hutchsystems.hutchconnect.fragments;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;
import androidx.recyclerview.widget.DefaultItemAnimator;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.EditText;

import com.hutchsystems.hutchconnect.MainActivity;
import com.hutchsystems.hutchconnect.R;
import com.hutchsystems.hutchconnect.adapters.TrailerRecycleAdapter;
import com.hutchsystems.hutchconnect.beans.VehicleBean;
import com.hutchsystems.hutchconnect.common.CommonTask;
import com.hutchsystems.hutchconnect.common.Utility;
import com.hutchsystems.hutchconnect.db.DailyLogDB;
import com.hutchsystems.hutchconnect.db.VehicleDB;

import androidx.fragment.app.DialogFragment;
import android.widget.ImageButton;

import java.util.ArrayList;

public class TrailerDialogFragment extends DialogFragment implements TrailerRecycleAdapter.IViewHolder {

    public OnFragmentInteractionListener mListener;
    TrailerRecycleAdapter rAdapter;
    ArrayList<VehicleBean> list;
    RecyclerView rvTrailer;
    EditText etSearch;
    String hooked = "";
    ImageButton imgCancel;

    public TrailerDialogFragment() {
        // Required empty public constructor
    }

    public static TrailerDialogFragment newInstance() {
        TrailerDialogFragment fragment = new TrailerDialogFragment();
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
        View view = inflater.inflate(R.layout.fragment_trailer_dialog, container);
        initialize(view);
        getDialog().getWindow().requestFeature(Window.FEATURE_NO_TITLE);
        getDialog().getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_RESIZE);
        this.setCancelable(false);
        return view;
    }

    private void initialize(View view) {
        hooked = "";
        for (String id : Utility.hookedTrailers) {
            hooked += "'" + id + "', ";
        }

        if (!hooked.isEmpty()) {
            hooked = hooked.replaceAll(", $", "");
        }
        TrailerRecycleAdapter.mListner = this;
        rvTrailer = (RecyclerView) view.findViewById(R.id.rvTrailer);
        RecyclerView.LayoutManager mLayoutManager = new LinearLayoutManager(getContext());
        rvTrailer.setLayoutManager(mLayoutManager);
        rvTrailer.setItemAnimator(new DefaultItemAnimator());
        imgCancel = (ImageButton) view.findViewById(R.id.imgCancel);
        imgCancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Utility.hideKeyboard(getActivity(), v);
                dismiss();
                if (mListener != null)
                    mListener.refresh();
            }
        });
        etSearch = (EditText) view.findViewById(R.id.etSearch);
        etSearch.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

            }

            @Override
            public void afterTextChanged(Editable s) {
                BindTrailers(s.toString(), hooked);
            }
        });
        BindTrailers("", hooked);
    }

    private void BindTrailers(String search, String except) {
        list = VehicleDB.TrailerGet(search, except);
        rAdapter = new TrailerRecycleAdapter(list);
        rvTrailer.setAdapter(rAdapter);
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mListener = null;
        TrailerRecycleAdapter.mListner = null;

    }

    @Override
    public void onItemClick(final View view) {
        final AlertDialog ad = new AlertDialog.Builder(getContext())
                .create();
        ad.setCancelable(true);
        ad.setCanceledOnTouchOutside(false);
        ad.setTitle(getString(R.string.hook_trailer_title));
        ad.setIcon(R.drawable.ic_launcher);
        ad.setMessage(getString(R.string.hook_trailer_message));

        ad.setButton(DialogInterface.BUTTON_POSITIVE, getString(R.string.yes),
                new DialogInterface.OnClickListener() {

                    @Override
                    public void onClick(DialogInterface dialog,
                                        int which) {

                        int position = rvTrailer.getChildLayoutPosition(view);
                        VehicleBean bean = list.get(position);
                        int trailerId = bean.getVehicleId();
                        if (mListener != null)
                            mListener.hooked(trailerId);
                        String trailerNo = Utility.getPreferences("trailer_number", "");
                        if (trailerNo.isEmpty()) {
                            trailerNo = bean.getUnitNo();
                        } else {
                            trailerNo += "," + bean.getUnitNo();
                        }

                        Utility.savePreferences("trailer_number", trailerNo);
                        DailyLogDB.TrailerUpdate(Utility.activeUserId, trailerNo);
                        MainActivity.postData(CommonTask.Post_DailyLog_DriverInfo);

                       //Post Trailer hook and Unhook information
                        MainActivity.postData(CommonTask.Post_TrailerStatus);
                        dismiss();
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

    public interface OnFragmentInteractionListener {
        void hooked(int trailerId);

        void refresh();
    }


    @Override
    public void onResume() {
        ViewGroup.LayoutParams params = getDialog().getWindow().getAttributes();
        params.width = WindowManager.LayoutParams.MATCH_PARENT;
        params.height = WindowManager.LayoutParams.WRAP_CONTENT;
        getDialog().getWindow().setAttributes((android.view.WindowManager.LayoutParams) params);

        super.onResume();
    }
}
