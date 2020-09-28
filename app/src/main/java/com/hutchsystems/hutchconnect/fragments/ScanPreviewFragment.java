package com.hutchsystems.hutchconnect.fragments;

import android.content.Context;
import android.os.Bundle;
import androidx.fragment.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import com.hutchsystems.hutchconnect.R;
import com.hutchsystems.hutchconnect.common.BitmapUtility;

import java.util.ArrayList;

/**
 * A simple {@link Fragment} subclass.
 * Activities that contain this fragment must implement the
 * {@link ScanPreviewFragment.OnFragmentInteractionListener} interface
 * to handle interaction events.
 * Use the {@link ScanPreviewFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class ScanPreviewFragment extends Fragment {
    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_INDEX = "index";
    private static final String ARG_PATH = "path";
    private static final String ARG_PATHS = "paths";

    // TODO: Rename and change types of parameters
    private String path;
    private ArrayList<String> paths;
    private int index;

    ImageView imgPreview;
    Button btndDelete, btnSave;
    TextView tvCount;

    private void initialize(View view) {
        imgPreview = (ImageView) view.findViewById(R.id.imgPreview);
        imgPreview.setImageBitmap(BitmapUtility.decodeSampledBitmapFromFile(path, 500, 800));

    }

    private OnFragmentInteractionListener mListener;

    public ScanPreviewFragment() {

    }

    public static ScanPreviewFragment newInstance(ArrayList<String> paths, int index) {
        ScanPreviewFragment fragment = new ScanPreviewFragment();
        Bundle args = new Bundle();
        args.putString(ARG_PATH, paths.get(index));
        args.putStringArrayList(ARG_PATHS, paths);
        args.putInt(ARG_INDEX, index);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            path = getArguments().getString(ARG_PATH);
            paths = getArguments().getStringArrayList(ARG_PATHS);
            index = getArguments().getInt(ARG_INDEX);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_scan_preview, container, false);
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
        void delete(int i);

        void save();
    }
}
