package com.hutchsystems.hutchconnect.fragments;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.res.Configuration;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;

import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.hutchsystems.hutchconnect.R;
import com.hutchsystems.hutchconnect.adapters.TrainingAdapter;
import com.hutchsystems.hutchconnect.beans.TicketBean;
import com.hutchsystems.hutchconnect.beans.UserBean;
import com.hutchsystems.hutchconnect.common.LogFile;
import com.hutchsystems.hutchconnect.common.Utility;
import com.hutchsystems.hutchconnect.common.WebService;
import com.hutchsystems.hutchconnect.common.WebUrl;
import com.hutchsystems.hutchconnect.db.HelpDB;
import com.hutchsystems.hutchconnect.db.LogDB;
import com.hutchsystems.hutchconnect.db.TrainingDocumentBean;
import com.hutchsystems.hutchconnect.db.TrainingDocumentDB;
import com.hutchsystems.hutchconnect.tasks.CheckTicketStatus;
import com.hutchsystems.hutchconnect.tasks.ReportIssue;

import org.json.JSONObject;

import java.util.ArrayList;

public class HelpFragment extends Fragment implements View.OnClickListener, TrainingAdapter.ITrainingDocument {
    TextView tvBTB_TicketNo, tvBTB_Status, tvHOS_TicketNo, tvHOS_Status, tvGPS_TicketNo, tvGPS_Status,
            tvTechnical_TicketNo, tvTechnical_Status, tvRI_TicketNo, tvRI_Status, tvTraining_TicketNo, tvTraining_Status;
    ImageButton btnBTB, btbHOS, btbGPS, btnTechnical, btnRI, btnTraining;
    EditText edCallbackNumber;

    private OnFragmentInteractionListener mListener;

    @Override
    public void onClick(View view) {
        if (!Utility.isInternetOn()) {
            Utility.showMsg("Internet is not connected!");
            return;
        }

        if (Utility.companyId == 0 || Utility.vehicleId == 0 || Utility.onScreenUserId == 0) {
            Utility.showMsg("There is an issue while creating ticket. Please restart app!");
            return;
        }

        int viewId = view.getId();
        submitTicket(viewId);

    }

    int type = 0;
    String title = "";
    String number = "";

    private void submitTicket(int viewId) {

        switch (viewId) {
            case R.id.btnBTB:
                type = 1;
                title = "Bluetooth Issue";
                break;
            case R.id.btbHOS:
                type = 2;
                title = "HOS Issue";
                break;
            case R.id.btbGPS:
                type = 3;
                title = "Location Issue";
                break;
            case R.id.btnTechnical:
                type = 4;
                title = "Technical Issue";
                break;
            case R.id.btnRI:
                type = 5;
                title = "Roadside Inspection Issue";
                break;
            case R.id.btnTraining:
                type = 6;
                title = "Training";
                break;
        }

        if (type == 6) {
            //if selection type is training then show alert for request training and other list
            AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
            /*builder.setTitle("Training Ticket");

            builder.setIcon(R.drawable.ic_launcher);*/
            LayoutInflater inflater = getLayoutInflater();
            View view = inflater.inflate(R.layout.trainning_ticket, null);

            TextView tvTraining = view.findViewById(R.id.Training);
            ImageButton imgCancel=view.findViewById(R.id.imgCancel);

            RecyclerView recycleTrainingList = view.findViewById(R.id.recycleTrainingList);

            builder.setView(view);

            final AlertDialog alertDialog = builder.show();

            alertDialog.setCancelable(true);
            tvTraining.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    alertDialog.dismiss();
                    // create and check duplicate ticket
                    createUpdateTicket(type, number, title);
                }
            });
            imgCancel.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    alertDialog.dismiss();
                }
            });
            ArrayList<TrainingDocumentBean> trainingDocList = TrainingDocumentDB.getDocuments();

            RecyclerView.LayoutManager layoutManager = new LinearLayoutManager(getContext());
            recycleTrainingList.setLayoutManager(layoutManager);
            TrainingAdapter devicesAdapter = new TrainingAdapter(getActivity(), trainingDocList, alertDialog);
            recycleTrainingList.setAdapter(devicesAdapter);

        } else {
            // create and check duplicate ticket
            createUpdateTicket(type, number, title);
        }

    }

    private void createUpdateTicket(int type, String number, String title) {
        if (type > 0) {
            boolean duplicate = false;
            for (int i = 0; i < tickets.length; i++) {
                if (tickets[i] > 0 && i != type - 1) {
                    duplicate = true;
                }
            }

            if (duplicate) {
                Utility.showMsg("App does not allow to create multiple tickets!");
                return;
            }
            number = edCallbackNumber.getText().toString();
            if (number.isEmpty() || number.length() < 10) {
                Utility.showMsg("Please enter your callback number!");
            } else {
                if (tickets[type - 1] > 0) {
                    new TicketPost(type, title, number).execute();
                } else {
                    submitTicket(type, title, number);
                }
            }
        }
    }

    private void submitTicket(final int type, final String title, final String number) {
        final AlertDialog ad = new AlertDialog.Builder(getActivity())
                .create();
        ad.setCancelable(true);
        ad.setCanceledOnTouchOutside(false);
        ad.setTitle("Ticket Confirmation?");
        ad.setIcon(R.drawable.ic_launcher);
        ad.setMessage("Are you sure you want to create ticket?");
        ad.setButton(DialogInterface.BUTTON_POSITIVE, getString(R.string.yes),
                new DialogInterface.OnClickListener() {

                    @Override
                    public void onClick(DialogInterface dialog,
                                        int which) {
                        if (Utility.isInternetOn()) {
                            new TicketPost(type, title, number).execute();

                            // if ticket type is HOS then email database to dev@hutchsystems.com
                            if (type == 2) {
                                emailDatabase();
                            }
                        }
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

    // Created By: Deepak Sharma
    // Created Date: 18 Feb 2020
    // Purpose: Email database to dev@hutchsystems.com
    private void emailDatabase() {
        String dbName = Utility.backupDB();


        if (dbName.isEmpty())
            return;
        showLoaderAnimation(true);
        String content = "App Version: " + Utility.ApplicationVersion + "\nCompany: " + Utility.CarrierName + "\nCompanyId: " + Utility.companyId + "\nVehicleID: " + Utility.vehicleId + "\n" + "Unit No: " + Utility.UnitNo + "\n" + "IMEI: " + Utility.IMEI;
        String title = "Database from ELD: " + Utility.ApplicationVersion + " - " + Utility.IMEI + " - Unit No: " + Utility.UnitNo;
        new ReportIssue(mailListner).execute(title, content, dbName);
    }


    ReportIssue.IMailProgress mailListner = new ReportIssue.IMailProgress() {


        @Override
        public void onMailSent(boolean status) {

        }
    };

    private void initialize(View view) {
        try {
            loadingPanel = (RelativeLayout) getActivity().findViewById(R.id.loadingPanel);
            tvBTB_TicketNo = (TextView) view.findViewById(R.id.tvBTB_TicketNo);
            tvBTB_Status = (TextView) view.findViewById(R.id.tvBTB_Status);
            btnBTB = (ImageButton) view.findViewById(R.id.btnBTB);
            btnBTB.setOnClickListener(this);

            tvHOS_TicketNo = (TextView) view.findViewById(R.id.tvHOS_TicketNo);
            tvHOS_Status = (TextView) view.findViewById(R.id.tvHOS_Status);
            btbHOS = (ImageButton) view.findViewById(R.id.btbHOS);
            btbHOS.setOnClickListener(this);

            tvGPS_TicketNo = (TextView) view.findViewById(R.id.tvGPS_TicketNo);
            tvGPS_Status = (TextView) view.findViewById(R.id.tvGPS_Status);
            btbGPS = (ImageButton) view.findViewById(R.id.btbGPS);
            btbGPS.setOnClickListener(this);

            tvTechnical_TicketNo = (TextView) view.findViewById(R.id.tvTechnical_TicketNo);
            tvTechnical_Status = (TextView) view.findViewById(R.id.tvTechnical_Status);
            btnTechnical = (ImageButton) view.findViewById(R.id.btnTechnical);
            btnTechnical.setOnClickListener(this);

            tvRI_TicketNo = (TextView) view.findViewById(R.id.tvRI_TicketNo);
            tvRI_Status = (TextView) view.findViewById(R.id.tvRI_Status);
            btnRI = (ImageButton) view.findViewById(R.id.btnRI);
            btnRI.setOnClickListener(this);

            tvTraining_TicketNo = (TextView) view.findViewById(R.id.tvTraining_TicketNo);
            tvTraining_Status = (TextView) view.findViewById(R.id.tvTraining_Status);
            btnTraining = (ImageButton) view.findViewById(R.id.btnTraining);
            btnTraining.setOnClickListener(this);

            edCallbackNumber = (EditText) view.findViewById(R.id.edCallbackNumber);

            UserBean user = null;//Utility.user1.isOnScreenFg() ? Utility.user1 : Utility.user2;

            if (Utility.user1.isOnScreenFg()) {
                user = Utility.user1;
            } else {
                user = Utility.user2;
            }
            String number = user.getMobileNo();
            if (number != null && !number.isEmpty()) {
                edCallbackNumber.setText(number);
            }
        } catch (Exception exe) {

        }
    }

    private void resetControl() {
        try {
            int visibility = View.GONE; //View.INVISIBLE
            tvBTB_TicketNo.setVisibility(visibility);
            tvBTB_Status.setVisibility(visibility);

            tvHOS_TicketNo.setVisibility(visibility);
            tvHOS_Status.setVisibility(visibility);

            tvGPS_TicketNo.setVisibility(visibility);
            tvGPS_Status.setVisibility(visibility);

            tvTechnical_TicketNo.setVisibility(visibility);
            tvTechnical_Status.setVisibility(visibility);

            tvRI_TicketNo.setVisibility(visibility);
            tvRI_Status.setVisibility(visibility);

            tvTraining_TicketNo.setVisibility(visibility);
            tvTraining_Status.setVisibility(visibility);
            tickets = new Integer[]{0, 0, 0, 0, 0, 0};
        } catch (Exception exe) {
        }
    }

    Integer[] tickets = new Integer[]{0, 0, 0, 0, 0, 0};

    private void refreshTicket() {
        ArrayList<TicketBean> list = HelpDB.Get();
        for (TicketBean bean : list) {
            String status = "Status: " + TicketStatusGet(bean.getTicketStatus());
            String ticketNo = "Ticket # " + bean.getTicketNo();
            int ticketId = bean.getTicketId();
            tickets[bean.getType() - 1] = ticketId;

            switch (bean.getType()) {
                case 1:
                    tvBTB_TicketNo.setVisibility(View.VISIBLE);
                    tvBTB_Status.setVisibility(View.VISIBLE);
                    tvBTB_TicketNo.setText(ticketNo);
                    tvBTB_Status.setText(status);
                    break;
                case 2:
                    tvHOS_TicketNo.setVisibility(View.VISIBLE);
                    tvHOS_Status.setVisibility(View.VISIBLE);
                    tvHOS_TicketNo.setText(ticketNo);
                    tvHOS_Status.setText(status);
                    break;
                case 3:
                    tvGPS_TicketNo.setVisibility(View.VISIBLE);
                    tvGPS_Status.setVisibility(View.VISIBLE);
                    tvGPS_TicketNo.setText(ticketNo);
                    tvGPS_Status.setText(status);
                    break;
                case 4:
                    tvTechnical_TicketNo.setVisibility(View.VISIBLE);
                    tvTechnical_Status.setVisibility(View.VISIBLE);
                    tvTechnical_TicketNo.setText(ticketNo);
                    tvTechnical_Status.setText(status);
                    break;
                case 5:
                    tvRI_TicketNo.setVisibility(View.VISIBLE);
                    tvRI_Status.setVisibility(View.VISIBLE);
                    tvRI_TicketNo.setText(ticketNo);
                    tvRI_Status.setText(status);
                    break;
                case 6:
                    tvTraining_TicketNo.setVisibility(View.VISIBLE);
                    tvTraining_Status.setVisibility(View.VISIBLE);
                    tvTraining_TicketNo.setText(ticketNo);
                    tvTraining_Status.setText(status);
                    break;
            }
        }
    }

    // Pop up For Feedback
    public void showFeedbackDialog(TicketBean ticket) {

        try {


            FragmentManager manager = getFragmentManager();
            FragmentTransaction ft = manager.beginTransaction();

            Fragment prev = manager.findFragmentByTag("rating_dialog");
            if (prev != null) {
                ft.remove(prev);
                ft.commitNow();
                ft = manager.beginTransaction();
            }

            FeedbackDialogFragment ratingDialog = FeedbackDialogFragment.newInstance();
            ratingDialog.setTicketID(ticket.getTicketId());
            ratingDialog.setTicketTitle(ticket.getTitle());
            ratingDialog.setTicketNo(ticket.getTicketNo());
            ratingDialog.show(ft, "rating_dialog");
        } catch (Exception e) {
            LogFile.write(ELogFragment.class.getName() + "::showFeedbackDialog Error:" + e.getMessage(), LogFile.USER_INTERACTION, LogFile.ERROR_LOG);
            e.printStackTrace();
            LogDB.writeLogs(HelpFragment.class.getName(), "showFeedbackDialog", e.getMessage(), Utility.printStackTrace(e));

        }

    }

    String[] status = {"New", "Assigned", "In-Progress", "Pending", "Resolved", "Re-Opened", "Closed"};

    public String TicketStatusGet(int s) {

        return status[s - 1];
    }

    public HelpFragment() {

    }

    public static HelpFragment newInstance() {
        HelpFragment fragment = new HelpFragment();

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
        View view = inflater.inflate(R.layout.fragment_help, container, false);
        initialize(view);
        resetControl();

        refreshTicket();

        // Check Ticket Status in the Database  is New, Assigned, In-Progress, Pending
        if (HelpDB.Get().size() > 0) {
            // Check Tickets Status in to the Server
            new CheckTicketStatus(ticketPostTaskListner).execute();
        }

        return view;
    }


    CheckTicketStatus.PostTaskListener<TicketBean> ticketPostTaskListner = new CheckTicketStatus.PostTaskListener<TicketBean>() {
        @Override
        public void onPostTask(TicketBean ticket) {

            if (ticket != null) {
                // ticket status is resolved or Closed  then we display feedback dialog
                if (ticket.getTicketStatus() == 5 || ticket.getTicketStatus() == 7) {
                    showFeedbackDialog(ticket);
                }
                // Refresh the Help Screen
                resetControl();
                refreshTicket();

            }
        }
    };

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        try {

            LayoutInflater inflater = (LayoutInflater) getActivity().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            View view = inflater.inflate(R.layout.fragment_help, null);

            ViewGroup viewGroup = (ViewGroup) getView();

            viewGroup.removeAllViews();
            viewGroup.addView(view);

            initialize(view);

            resetControl();

            refreshTicket();

            // Check Ticket Status in the Database  is New, Assigned, In-Progress, Pending
            if (HelpDB.Get().size() > 0) {
                // Check Tickets Status in to the Server
                new CheckTicketStatus(ticketPostTaskListner).execute();
            }

        } catch (Exception e) {
            LogFile.write(DutyStatusChangeDialog.class.getName() + "::onConfigurationChanged Error:" + e.getMessage(), LogFile.USER_INTERACTION, LogFile.ERROR_LOG);
        }
    }


    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        if (context instanceof OnFragmentInteractionListener) {
            mListener = (OnFragmentInteractionListener) context;
            TrainingAdapter.mListener = this;
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
        void onFragmentInteraction(Uri uri);
    }


    private class TicketPost extends AsyncTask<Void, Void, TicketBean> {
        int type;
        String title, comment;

        public TicketPost(int type, String title, String comment) {
            this.type = type;
            this.title = title;
            this.comment = comment;
        }

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            showLoaderAnimation(true);
        }

        @Override
        protected TicketBean doInBackground(Void... voids) {
            TicketBean ticket = PostTicketSYNC(type, title, comment);
            return ticket;
        }

        @Override
        protected void onPostExecute(TicketBean ticket) {
            super.onPostExecute(ticket);
            // if status is resolved or closed then show popup for feddback
            if (ticket.getTicketStatus() == 5 || ticket.getTicketStatus() == 7) {
                showFeedbackDialog(ticket);
            }
            showLoaderAnimation(false);
            resetControl();
            refreshTicket();
        }
    }


    // Created By: Deepak Sharma
    // Created Date: 29 January 2016
    // Purpose: POST EVENT SYNC
    public TicketBean PostTicketSYNC(int type, String title, String comment) {
        TicketBean ticket = new TicketBean();
        ticket.setTicketId(0);
        ticket.setType(type);
        ticket.setTitle(title);
        WebService ws = new WebService();

        try {
            String ticketDate = Utility.getCurrentPSTDateTime();
            JSONObject obj = new JSONObject();
            obj.put("TicketId", tickets[type - 1]);
            obj.put("TicketTypeId", 4);
            obj.put("TicketDate", ticketDate);
            obj.put("Title", title);
            obj.put("Comment", comment);
            obj.put("VehicleId", Utility.vehicleId);
            obj.put("CompanyId", Utility.companyId);
            obj.put("CreatedBy", Utility.onScreenUserId);
            obj.put("CreatedDate", ticketDate);
            obj.put("ModifiedBy", Utility.onScreenUserId);
            obj.put("ModifiedDate", ticketDate);
            obj.put("StatusId", 1);

            String result = ws.doPost(
                    WebUrl.TICKET_POST,
                    obj.toString());

            if (result != null) {
                JSONObject response = new JSONObject(result);

                int id = response.getInt("TicketId");
                String ticketNo = response.getString("TicketNo");
                int clientStatusId = response.getInt("ClientStatusId");
                if (id > 0) {
                    ticket.setTicketId(id);
                    ticket.setTicketNo(ticketNo);
                    ticket.setTicketStatus(clientStatusId);

                    HelpDB.Save(type, id, ticketNo, title, comment, ticketDate, clientStatusId);
                }
            }

        } catch (Exception e) {
            ticket.setTicketNo(e.getMessage());
            Utility.printError(e.getMessage());
        }
        return ticket;
    }

    RelativeLayout loadingPanel;

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
    public void downloadDocument(final String serverPath, final String clientPath) {

        showLoaderAnimation(true);
        new Thread(new Runnable() {
            @Override
            public void run() {

                WebService obj = new WebService();
                final boolean status = obj.downloadFile(serverPath, clientPath);

                getActivity().runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        if (status) {
                            Utility.openPdf(clientPath);
                        } else {
                            Utility.showAlertMsg("Error downloading file.");
                        }
                        showLoaderAnimation(false);
                    }
                });
            }
        }).start();
    }
}
