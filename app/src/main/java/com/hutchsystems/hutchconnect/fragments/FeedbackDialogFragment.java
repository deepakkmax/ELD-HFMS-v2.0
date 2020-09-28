package com.hutchsystems.hutchconnect.fragments;

import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import androidx.fragment.app.DialogFragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.RatingBar;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.hutchsystems.hutchconnect.R;
import com.hutchsystems.hutchconnect.common.Utility;
import com.hutchsystems.hutchconnect.common.WebService;
import com.hutchsystems.hutchconnect.common.WebUrl;
import com.hutchsystems.hutchconnect.db.HelpDB;

import org.json.JSONObject;



public class FeedbackDialogFragment extends DialogFragment {


    int ticketID;
    String ticketTitle,ticketNo;
    private Button btnSubmit;
    private TextView tvTicketID;
    private RatingBar ratingBar;
    private EditText etComment;
    private ImageButton imgCancel;
    public FeedbackDialogInterface mListener;
    private RelativeLayout loadingPanel;

    public FeedbackDialogFragment() {

    }

    public static FeedbackDialogFragment newInstance() {
        FeedbackDialogFragment fragment = new FeedbackDialogFragment();
        return fragment;
    }

    public void setTicketID(int ticketID) {
        this.ticketID = ticketID;
    }

    public void setTicketNo(String ticketNo)
    {
        this.ticketNo=ticketNo;
    }

    public void setTicketTitle(String ticketTitle) {
        this.ticketTitle = ticketTitle;
    }

    private void initialize(View view) {
        // Loading Pannel
        loadingPanel = (RelativeLayout) view.findViewById(R.id.loadingPanel);
        imgCancel = (ImageButton) view.findViewById(R.id.imgCancel);
        imgCancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                dismiss();

            }
        });
        btnSubmit = (Button) view.findViewById(R.id.btnSubmit);
        btnSubmit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (ratingBar.getRating() > 0) {
                    int rating = (int) ratingBar.getRating();
                    if (!Utility.isInternetOn()) {
                        Utility.showMsg("Internet is not connected!");
                        return;
                    }
                    new PostFeedback(rating, etComment.getText().toString(), ticketID).execute();

                } else {
                    Toast.makeText(Utility.context, "Please Rate ", Toast.LENGTH_LONG).show();
                }


            }
        });


        tvTicketID = (TextView) view.findViewById(R.id.tvTicketID);
        ratingBar = (RatingBar) view.findViewById(R.id.ratingbar);
        etComment = (EditText) view.findViewById(R.id.etComment);

        tvTicketID.setText(ticketTitle + ":" + ticketNo);


    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_rating_comment_dialog, container, false);
        initialize(view);

        getDialog().getWindow().requestFeature(Window.FEATURE_NO_TITLE);
        getDialog().getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_RESIZE);
        this.setCancelable(false);
        return view;
    }


    // For callback of dismmiss Dialog
    public interface FeedbackDialogInterface {

        void onFeedbackDissmisDialog();

    }
    private void showLoaderAnimation(boolean isShown) {
        try {
            if (isShown) {
                new Handler().postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        if (getActivity() != null) {
                            getActivity().runOnUiThread(new Runnable() {
                                @Override
                                public void run() {

                                    showLoaderAnimation(false);
                                }
                            });
                        }
                    }
                }, 30000);
                loadingPanel.setVisibility(View.VISIBLE);
                getActivity().getWindow().setFlags(WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE,
                        WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE);
            } else {
                loadingPanel.setVisibility(View.GONE);
                getActivity().getWindow().clearFlags(WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE);
            }
        } catch (Exception e) {

        }
    }

    @Override
    public void show(FragmentManager manager, String tag) {

        FragmentTransaction fragmentTransaction = manager.beginTransaction();
        fragmentTransaction.add(this, FeedbackDialogFragment.class.getName());
        fragmentTransaction.commitAllowingStateLoss();
    }

    // Created By: Pallavi Wattamwar
    // Created Date: 27 September 2018
    // Purpose: POST Feedback
    public static Boolean PostFeedback(int rating, String feedback, int ticketid) {

        Boolean status =true;
        WebService ws = new WebService();

        try {
            String ticketDate = Utility.getCurrentPSTDateTime();
            JSONObject obj = new JSONObject();
            obj.put("TicketId", ticketid);
            obj.put("UserFeedback", feedback);
            obj.put("Rating", rating);
            obj.put("VehicleId", Utility.vehicleId);
            obj.put("CompanyId", Utility.companyId);
            obj.put("CreatedBy", Utility.onScreenUserId);
            obj.put("CreatedDate", ticketDate);
            obj.put("ModifiedBy", Utility.onScreenUserId);
            obj.put("ModifiedDate", ticketDate);
            obj.put("StatusId", 1);

            String result = ws.doPost(
                    WebUrl.TICKET_RATING_POST,
                    obj.toString());
            if (result == null || result.isEmpty())
                return status;

            if (result != null) {
                HelpDB.updateCommentAndRating(ticketid, feedback, rating);
            }

        } catch (Exception e) {
            Utility.printError(e.getMessage());
            status =false;
        }
        return status;
    }

    public class PostFeedback extends AsyncTask<Void, Void, Boolean> {
        int rating, ticketID;
        String feedback;

        public PostFeedback(int rating, String feedback, int ticketNo) {
            this.rating = rating;
            this.ticketID = ticketNo;
            this.feedback = feedback;
        }

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            showLoaderAnimation(true);
            btnSubmit.setEnabled(false);
        }

        @Override
        protected Boolean doInBackground(Void... voids) {
            Boolean result = PostFeedback(rating, feedback, ticketID);
            return result;
        }

        @Override
        protected void onPostExecute(Boolean result) {
            super.onPostExecute(result);
            btnSubmit.setEnabled(true);
            showLoaderAnimation(false);
            if(result) {
                if (mListener != null) {
                    mListener.onFeedbackDissmisDialog();
                }
                Toast.makeText(Utility.context,"Thank you for your valuable feedback",Toast.LENGTH_LONG).show();
                dismiss();
            }
        }
    }

}
