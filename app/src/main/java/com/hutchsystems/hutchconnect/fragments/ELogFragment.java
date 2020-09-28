package com.hutchsystems.hutchconnect.fragments;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.DashPathEffect;
import android.graphics.Paint;
import android.graphics.PorterDuff;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.speech.tts.TextToSpeech;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.view.WindowManager;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.hutchsystems.hutchconnect.MainActivity;
import com.hutchsystems.hutchconnect.R;
import com.hutchsystems.hutchconnect.adapters.EventAdapter;
import com.hutchsystems.hutchconnect.beans.DailyLogBean;
import com.hutchsystems.hutchconnect.beans.DutyStatusBean;
import com.hutchsystems.hutchconnect.beans.EventBean;
import com.hutchsystems.hutchconnect.beans.GPSData;
import com.hutchsystems.hutchconnect.beans.RuleBean;
import com.hutchsystems.hutchconnect.beans.TicketBean;
import com.hutchsystems.hutchconnect.beans.UserBean;
import com.hutchsystems.hutchconnect.common.AlertMonitor;
import com.hutchsystems.hutchconnect.common.CanMessages;
import com.hutchsystems.hutchconnect.common.ChatClient;
import com.hutchsystems.hutchconnect.common.CommonTask;
import com.hutchsystems.hutchconnect.common.ConstantFlag;
import com.hutchsystems.hutchconnect.common.CustomDateFormat;
import com.hutchsystems.hutchconnect.common.LogFile;
import com.hutchsystems.hutchconnect.common.Utility;
import com.hutchsystems.hutchconnect.common.ZoneList;
import com.hutchsystems.hutchconnect.db.DailyLogDB;
import com.hutchsystems.hutchconnect.db.EventDB;
import com.hutchsystems.hutchconnect.db.HourOfServiceDB;
import com.hutchsystems.hutchconnect.db.LogDB;
import com.hutchsystems.hutchconnect.db.ReportDB;
import com.hutchsystems.hutchconnect.db.SettingsDB;
import com.hutchsystems.hutchconnect.db.UserDB;
import com.hutchsystems.hutchconnect.tasks.CheckTicketStatus;
import com.hutchsystems.hutchconnect.tasks.LogEventSync;
import com.hutchsystems.hutchconnect.tasks.PostData;
import com.hutchsystems.hutchconnect.tasks.SyncData;
import com.hutchsystems.hoursofservice.model.DutyStatus;
import com.hutchsystems.hoursofservice.model.ViolationModel;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;

import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;

import static com.hutchsystems.hutchconnect.common.Utility.DIALOGBOX_ICON;
import static com.hutchsystems.hutchconnect.common.Utility.DIALOGBOX_TITLE;

public class ELogFragment extends Fragment implements View.OnClickListener, RuleChangeDialog.RuleChangeDialogInterface,
        DutyStatusChangeDialog.DutyStatusChangeDialogInterface, InputInformationDialog.InputInformationDialogInterface, TotalMilesFragment.OnFragmentInteractionListener, ChatClient.RefreshData,
        DataPostDialogFragment.IDataPostDialog {
    String TAG = ELogFragment.class.getName();
    static String fullFormat = "yyyy-MM-dd HH:mm:ss";
    public String title = "Daily Log";
    private ArrayList<DutyStatusBean> dutyStatusList = new ArrayList<>();
    private OnFragmentInteractionListener mListener;

    private ImageView imgDutyStatus;
    private ImageButton btnShipmentDetail;
    //Bitmap bitMapRestore;
    Bitmap bmp;
    Canvas canvas;

    Date currentDate;
    CheckBox chkRules;
    TextView tvOffDutyTime, tvSleeperTime, tvDrivingTime, tvOnDutyTime, tvViolation, tvViolationDate, tvRemaingTime, tvCoDriver, tvSwitchUser, tvSBSplit;
    TextView tvVehicleMiles, tvTotalDistance, tvTotalDistanceValue, tvAccumulatedMilesValue, tvCanadaRule, tvCanadaRuleValue, tvUSRule, tvUSRuleValue, tvRestTime, tvRestTimeValue;

    Button butDutyStatus;
    Button butRemainingTime;
    ProgressBar pbTimeCountProgress, pbTotalDrivingHours, pbTotalWorkShiftHour, pbTotalCanadaRule, pbTotalUSRule;

    TextView tvTimeZone, tvTotalDrivingHours, tvTotalWorkShiftHours;

    ListView lvEvents;
    EventAdapter eventAdapter;

    int dailyLogId, totalDistance = 0, statusStartOdometerReading;
    public int currentStatus = 1;
    public static Date statusDT;
    private Handler handlerElog = new Handler();

    public static int currentRule = 1;
    List<String> listRules;
    boolean bDialogShowing;

    RuleChangeDialog ruleChangeDialog;
    public int certifyFg;

    private boolean firstLogin = false;
    InputInformationDialog infosDialog;

    PostData.PostTaskListener<Boolean> postDataPostTaskListener = new PostData.PostTaskListener<Boolean>() {
        @Override
        public void onPostTask(Boolean result) {

            showLoaderAnimation(false);
        }
    };

    SyncData.PostTaskListener<Boolean> syncDataPostTaskListener = new SyncData.PostTaskListener<Boolean>() {
        @Override
        public void onPostTask(Boolean result) {
            boolean specialCategoryChanged = Utility.specialCategoryChanged();
            if (specialCategoryChanged) {
                //update special category of current user
                int driverId = Utility.user1.isOnScreenFg() ? Utility.user1.getAccountId() : Utility.user2.getAccountId();
                String specialCategory = UserDB.getSpecialCategory(driverId);
                UserBean currentUser = Utility.user1.isOnScreenFg() ? Utility.user1 : Utility.user2;
                if (currentUser != null) {
                    currentUser.setSpecialCategory(specialCategory);
                }
            }

            if (mListener != null) {
                mListener.updateWebserviceIcon(true);
                mListener.updateSpecialCategoryChanged(specialCategoryChanged);

            }


            ELogFragment.this.refresh();
            showLoaderAnimation(false);
            try {
                if (getContext() != null) {
                    getContext().getSharedPreferences("HutchGroup", getContext().MODE_PRIVATE).edit().putString("timezoneid", Utility.TimeZoneId).commit();
                    //tvTimeZone.setText(Utility.TimeZoneOffsetUTC);
                    tvTimeZone.setText(ZoneList.getTimezoneName(false));
                }
            } catch (Exception e) {

            }


        }
    };

    // Changed Class name
    CheckTicketStatus.PostTaskListener<TicketBean> ticketPostTaskListner = new CheckTicketStatus.PostTaskListener<TicketBean>() {
        @Override
        public void onPostTask(TicketBean result) {


            if (result != null) {
                // if ticket status is resolved or Closed  then we display feedback dialog
                if (result.getTicketStatus() == 5 || result.getTicketStatus() == 7) {
                    showFeedbackDialog(result);
                }
            }

        }
    };
    private ImageButton fabDefereDay;
    private TextView tvDeffered;

    // Created By: Deepak Sharma
    // Created Date: 1 Aug 2018
    // Purpose: refresh duration of the last item of event list
    private void lastItemRefresh() {
        try {

            if (eventAdapter.getCount() > 0) {
                EventBean bean = eventAdapter.getItem(0);
                String time = Utility.getDuration(Utility.parse(bean.getEventDateTime()), Utility.newDate());
                bean.setDuration(time);
                eventAdapter.notifyDataSetChanged();
            }
        } catch (Exception exe) {

        }
    }


    private void updateTitle() {
        try {
            String title = getContext().getResources().getString(R.string.title_daily_log);
            title += " - " + Utility.convertDate(Utility.newDate(), CustomDateFormat.d10);
            mListener.setTitle(title);
        } catch (Exception exe) {
        }

    }

    private Runnable runnableStatus = new Runnable() {
        @Override
        public void run() {
            try {
                lastItemRefresh();
                Date now = Utility.dateOnlyGet(Utility.newDate());
                if (!now.equals(currentDate)) {
                    updateTitle();
                    currentDate = Utility.dateOnlyGet(Utility.newDate());
                    // create daily log record for the current date
                    dailyLogId = DailyLogDB.DailyLogCreate(Utility.onScreenUserId, Utility.ShippingNumber, Utility.TrailerNumber, "");
                    todayDistance = 0;
                    GPSData.TotalDistance = 0;

                    ELogFragment.this.refresh();
                }

                boolean violationOnGrid = false;

                if (Utility._appSetting.getGraphLine() == 1) {
                    ELogFragment.this.DutyStatusGet();
                    // only refresh violation on grid if line is upto current
                    violationOnGrid = true;

                }

                if (getActivity() != null) {

                    // calculate violation without refreshing duty status and without changing hours in dailylog table
                    new ViolationSync(false, false, violationOnGrid).execute();
                    //AutoHoursCalculate();
                }

                checkToSpeak();

                handlerElog.postDelayed(this, 60000);
            } catch (Exception e) {
                LogFile.write(ELogFragment.class.getName() + "::runnalbeStatus Error:" + e.getMessage(), LogFile.USER_INTERACTION, LogFile.ERROR_LOG);
                LogDB.writeLogs(ELogFragment.class.getName(), "runnalbeStatus", e.getMessage(), Utility.printStackTrace(e));

            }
        }
    };

    boolean fabShowing;

    int finalHeight = 0, finalWidth = 0, imageWidth = 0, imageHeight = 0;
    View inforHeader;
    ImageButton fabMenu, fabPost, fabSync, fabChangeRule, fabCertify, fabUncertify, fabActive, fabSwitchUser, fabUndocking;

    FloatingActionButton fabStop;
    LinearLayout layoutUndocking;
    ConstraintLayout layout_menu;
    View restView;
    ProgressBar rlLoadingPanel;

    TextView tvSpecialStatus;

    final Handler odometerHandler = new Handler();
    final Runnable odometerRunnable = new Runnable() {
        @Override
        public void run() {
            updateOdometer();

            odometerHandler.postDelayed(this, 10000);
        }
    };

    public ELogFragment() {
        // Required empty public constructor
        //mInstance = this;
    }

    public static ELogFragment newInstance() {
        ELogFragment fragment = new ELogFragment();
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setRetainInstance(true);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        // Inflate the layout for this fragment
        Configuration config = getResources().getConfiguration();
        View view = inflater.inflate(R.layout.fragment_elog, container, false);

        try {
            ChatClient.rListner = this;
            title = getString(R.string.daily_log);

            // get/set User time zone id
            UserDB.getUserTimeZone(Utility.onScreenUserId);

            currentDate = Utility.dateOnlyGet(Utility.newDate());

            DailyLogBean dailyLog = DailyLogDB.getDailyLogInfo(Utility.onScreenUserId, Utility.getCurrentDate());
            dailyLogId = dailyLog.get_id();
            // certifyFg = dailyLog.getCertifyFG();
            // Check getUncertified DailyLog logs of the previous day
            certifyFg = DailyLogDB.getUncertifiedDailyLog(Utility.onScreenUserId).size() > 0 ? 0 : 1;

            todayDistance = dailyLog.getTotalDistance();

            if (dailyLogId == 0) {
                dailyLogId = DailyLogDB.DailyLogCreate(Utility.onScreenUserId, Utility.ShippingNumber, Utility.TrailerNumber, "");
                todayDistance = 0;
            } else {
                todayDistance = EventDB.CalculateDistanceByLogId(dailyLogId);
            }

            // get current day's duty status
            dutyStatusList = EventDB.DutyStatusGetToday(Utility.onScreenUserId);

            mListener.setCertify(certifyFg);
            lvEvents = (ListView) view.findViewById(R.id.lvEvent);
            eventAdapter = new EventAdapter(this.getActivity(), getListEvents());

            if (config.orientation == Configuration.ORIENTATION_LANDSCAPE) {
                inforHeader = inflater.inflate(R.layout.elog_scrollview, null, false);
            } else if (config.orientation == Configuration.ORIENTATION_PORTRAIT) {
                inforHeader = inflater.inflate(R.layout.elog_scrollview_portrait, null, false);
            }


            lvEvents.addHeaderView(inforHeader);
            lvEvents.setAdapter(eventAdapter);

            initializeControls(view);

            // set variable currentstatus and statusdate and refresh button text
            onCurrentStatusUpdate();
            // set last off duty status
            setLastOffDate();

            imgDutyStatus = (ImageView) view.findViewById(R.id.imgDutyStatus_portrait);
            //imgDutyStatus = imgViewPortrait;


            ViewTreeObserver vto = imgDutyStatus.getViewTreeObserver();
            vto.addOnPreDrawListener(new ViewTreeObserver.OnPreDrawListener() {
                @Override
                public boolean onPreDraw() {
                    imgDutyStatus.getViewTreeObserver().removeOnPreDrawListener(this);
                    DisplayMetrics displayMetrics = getResources().getDisplayMetrics();
                    finalHeight = displayMetrics.heightPixels;
                    finalWidth = displayMetrics.widthPixels;

                    getActivity().getWindowManager().getDefaultDisplay().getMetrics(displayMetrics);
                    float density = displayMetrics.density;
                    finalWidth = Math.round(displayMetrics.widthPixels / density);
                    finalHeight = Math.round(displayMetrics.heightPixels / density);


                    // get width of graph
                    Utility._GridWidth = imgDutyStatus.getWidth();
                    Utility._GridHeight = imgDutyStatus.getHeight();

                    // get height of graph
                    imageHeight = imgDutyStatus.getHeight();
                    return false;
                }
            });

            Thread thBitMap = new Thread(new Runnable() {
                @Override
                public void run() {
                    while (true) {
                        try {
                            Thread.sleep(10);
                        } catch (InterruptedException exe) {
                            break;
                        }

                        if (finalHeight != 0 && finalWidth != 0) {

                            getActivity().runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    initializeBitmap(true);

                                }
                            });

                            break;
                        }
                    }
                }
            });
            thBitMap.setName("Elog-Bitmap1");
            thBitMap.start();


            new ViolationSync(true, false).execute();

            if (firstLogin) {
                firstLogin = false;

                // to get event from other devices
                if (Utility.isInternetOn()) {
                    new LogEventSync(refreshElogFragment).execute(Utility.LastEventDate);
                }

                FragmentManager manager = getFragmentManager();
                FragmentTransaction ft = manager.beginTransaction();
                Fragment prev = manager.findFragmentByTag("shippingtrailers_dialog");
                if (prev != null) {
                    ft.remove(prev);
                    ft.commitNow();
                    ft = manager.beginTransaction();
                }

                infosDialog = new InputInformationDialog();

                infosDialog.mListener = this;

                infosDialog.show(ft, "shippingtrailers_dialog");
            }

            updateTitle();

            checkTicketStatus();

        } catch (Exception e) {
            LogFile.write(ELogFragment.class.getName() + "::onCreateView Error:" + e.getMessage(), LogFile.USER_INTERACTION, LogFile.ERROR_LOG);
            LogDB.writeLogs(ELogFragment.class.getName(), "onCreateView", e.getMessage(), Utility.printStackTrace(e));

        }

        return view;
    }

    @Override
    public void onInputFinished() {
        Log.d("Input", "fragment onInputFinished");
        infosDialog.mListener = null;
    }

    @Override
    public void onInputSaved(String shippId, String trailerId) {
        Log.d("Input", "fragment onInputSaved");

        if (Utility.ShippingNumber.equals("") || Utility.TrailerNumber.equals(""))
            btnShipmentDetail.setBackground(ContextCompat.getDrawable(getActivity(), R.drawable.ic_elog_ship));
        else
            btnShipmentDetail.setBackground(ContextCompat.getDrawable(getActivity(), R.drawable.ic_event_shipment_24));

        infosDialog.mListener = null;
    }

    @Override
    public void onResume() {
        super.onResume();
    }


    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        try {
            Log.d(TAG, "fragment onConfigurationChanged");
            //lvEvents.removeHeaderView(inforHeader);
            LayoutInflater inflater = (LayoutInflater) getActivity().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            View view = inflater.inflate(R.layout.fragment_elog, null);

            ViewGroup viewGroup = (ViewGroup) getView();

            viewGroup.removeAllViews();
            viewGroup.addView(view);


            lvEvents = (ListView) view.findViewById(R.id.lvEvent);

            Log.d(TAG, "onCreateView");
            if (newConfig.orientation == Configuration.ORIENTATION_LANDSCAPE) {
                inforHeader = inflater.inflate(R.layout.elog_scrollview, null, false);
                //lvEvents.addHeaderView(header);
            } else if (newConfig.orientation == Configuration.ORIENTATION_PORTRAIT) {
                inforHeader = inflater.inflate(R.layout.elog_scrollview_portrait, null, false);
                //lvEvents.addHeaderView(header);
            }
            lvEvents.addHeaderView(inforHeader);

            initializeControls(view);
            lvEvents.setAdapter(eventAdapter);
            imgDutyStatus = (ImageView) view.findViewById(R.id.imgDutyStatus_portrait);
            ViewTreeObserver vto = imgDutyStatus.getViewTreeObserver();
            vto.addOnPreDrawListener(new ViewTreeObserver.OnPreDrawListener() {
                @Override
                public boolean onPreDraw() {
                    imgDutyStatus.getViewTreeObserver().removeOnPreDrawListener(this);
                    DisplayMetrics displayMetrics = getResources().getDisplayMetrics();
                    finalHeight = displayMetrics.heightPixels;
                    finalWidth = displayMetrics.widthPixels;

                    getActivity().getWindowManager().getDefaultDisplay().getMetrics(displayMetrics);
                    float density = displayMetrics.density;
                    finalWidth = Math.round(displayMetrics.widthPixels / density);
                    finalHeight = Math.round(displayMetrics.heightPixels / density);
                    //Log.e(TAG, "Height = " + finalHeight + " - Width = " + finalWidth);
                    return false;
                }
            });

            // set variable currentstatus and statusdate and refresh button text
            onCurrentStatusUpdate();
            Thread thBitMap = new Thread(new Runnable() {
                @Override
                public void run() {
                    while (true) {
                        if (finalHeight != 0 && finalWidth != 0) {
                            getActivity().runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    initializeBitmap(true);

                                    new ViolationSync(false, false).execute();
                                }
                            });

                            break;
                        }
                    }
                }
            });
            thBitMap.setName("Elog-Bitmap2");
            thBitMap.start();
            updateTitle();

        } catch (Exception e) {
            LogFile.write(ELogFragment.class.getName() + "::onConfigurationChanged Error:" + e.getMessage(), LogFile.USER_INTERACTION, LogFile.ERROR_LOG);
            LogDB.writeLogs(ELogFragment.class.getName(), "onConfigurationChanged", e.getMessage(), Utility.printStackTrace(e));

        }
    }

    @Override
    public void onPause() {
        super.onPause();
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        if (context instanceof OnFragmentInteractionListener) {
            mListener = (OnFragmentInteractionListener) context;

            odometerHandler.postDelayed(odometerRunnable, 500);
        } else {
            throw new RuntimeException(context.toString()
                    + " must implement OnFragmentInteractionListener");
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();
        try {
            mListener = null;
            handlerElog.removeCallbacksAndMessages(null);
            odometerHandler.removeCallbacksAndMessages(null);
            //odometerHandler.removeCallbacks(odometerRunnable);
            finalHeight = 0;
            finalWidth = 0;
            canvas = null;
            infosDialog = null;
        } catch (Exception e) {
            LogFile.write(ELogFragment.class.getName() + "::onDetach Error:" + e.getMessage(), LogFile.USER_INTERACTION, LogFile.ERROR_LOG);
            LogDB.writeLogs(ELogFragment.class.getName(), "onDetach", e.getMessage(), Utility.printStackTrace(e));

        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
    }

    // show popup for feedback dialog
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
            LogDB.writeLogs(com.hutchsystems.hutchconnect.fragments.ELogFragment.class.getName(), "showFeedbackDialog", e.getMessage(), Utility.printStackTrace(e));

        }

    }

    // Created By: Pallavi Wattamwar
    // Created Date: 28 November 2018
    // Purpose: Show Confirmation Dialog to Create Manually Log For Missing Log Dates
    // Parameter : ArrayList of Missing Event
    public void showEventDialog(final ArrayList<String> missingEventList) {
        final AlertDialog alertDialog = new AlertDialog.Builder(Utility.context).create();
        alertDialog.setCancelable(true);
        alertDialog.setCanceledOnTouchOutside(false);
        alertDialog.setTitle("E-Log");
        alertDialog.setIcon(DIALOGBOX_ICON);
        alertDialog.setMessage(getString(R.string.Manually_Create_Logs));
        alertDialog.setButton(DialogInterface.BUTTON_POSITIVE, Utility.context.getString(R.string.ok),
                new DialogInterface.OnClickListener() {

                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        updateMissingLogData(missingEventList);
                    }
                });

        alertDialog.show();

    }

    @Override
    public void onDismissDataPost() {
        launchDutyStatusChange(false);
    }


    /**
     * This interface must be implemented by activities that contain this
     * fragment to allow an interaction in this fragment to be communicated
     * to the activity and potentially other fragments contained in that
     * activity.
     * <p/>
     * See the Android Training lesson <a href=
     * "http://developer.android.com/training/basics/fragments/communicating.html"
     * >Communicating with Other Fragments</a> for more information.
     */

    public interface OnFragmentInteractionListener {
        // TODO: Update argument type and name
        void onFragmentInteraction(Uri uri);

        void changeRule(int rule);

        void setDutyStatus(int status);

        void setActiveDutyStatus(int status);

        void setTotalDistance(int total);

        void setStartOdoMeter(int odo);

        void setStartEngineHour(int value);

        void resetInspectionIcon();

        void setCertify(int certifyFg);

        void updateWebserviceIcon(boolean active);

        void updateSpecialCategoryChanged(boolean value);

        void activeUser();

        void changeUser();

        void undocking();

        void setTitle(String title);

        void onUploadDocument();

        void setProtocol();

        void onUpdateViolation(boolean status);

        void displayAnnotation();

        void callCertifyLogBook();
    }

    @Override
    public void onClick(View view) {
        try {
            switch (view.getId()) {
               /* case R.id.tvCurrentRuleLabel:
                case R.id.butChangeRule:
                    //launch the ativity Rule Change
                    launchRuleChange();
                    break;*/
                case R.id.butRemainingTime:
                case R.id.tvRemaingTime:
                case R.id.butDutyStatus:
                    if (!Utility.HutchConnectStatusFg && ConstantFlag.LiveFg) {
                        Utility.showAlertMsg("App does not allow to change status while bluetooth is disconnected!");
                        return;
                    }

                    ArrayList<DailyLogBean> list = EventDB.DailyLogUnPostedGet(Utility.onScreenUserId, dailyLogId);

                    if (list.size() > 0) {
                        launchDataPostDialog(list);
                    } else {
                        //lauch the activity Duty Status Change
                        launchDutyStatusChange(false);
                    }

                    break;
            }
        } catch (Exception e) {
            LogFile.write(ELogFragment.class.getName() + "::onClick Error:" + e.getMessage(), LogFile.USER_INTERACTION, LogFile.ERROR_LOG);
            LogDB.writeLogs(ELogFragment.class.getName(), "onClick", e.getMessage(), Utility.printStackTrace(e));

        }

    }

    private void setLastOffDate() {
        if (currentStatus < 3) {
            HourOfServiceDB.getLastOffDutyTime(Utility.onScreenUserId);
        }
    }

    // Created By: Sahil Bansal
    // Created Date: 25 November 2019
    // Purpose: initialize bitmap to draw graph
    private void initializeBitmap(final boolean addRunnable) {

        try {
            if (bmp != null) {
                bmp.recycle();

            }

            // get width of graph
            imageWidth = imgDutyStatus.getWidth();

            if (imageWidth == 0)
                imageWidth = Utility._GridWidth;

            // get height of graph
            imageHeight = imgDutyStatus.getHeight();

            if (imageHeight == 0)
                imageHeight = Utility._GridHeight;

            if (imageWidth != 0 && imageHeight != 0) {

                // initialize bitmap of graph dimensions
                bmp = Bitmap.createBitmap(imageWidth, imageHeight, Bitmap.Config.ARGB_8888);

                // initialize canvas to draw on
                canvas = new Canvas(bmp);

                initialize(addRunnable);
            }
        } catch (Exception exe) {

            //Utility.printError(exe.getMessage());
            Log.d(TAG, "Error: " + exe.getMessage());
            LogFile.write(DetailFragment.class.getName() + "::initializeBitmap Error: " + exe.getMessage(), LogFile.USER_INTERACTION, LogFile.ERROR_LOG);
            LogDB.writeLogs(DetailFragment.class.getName(), "::initializeBitmap Error:", exe.getMessage(), Utility.printStackTrace(exe));

        }

    }

    private void initializeControls(View view) {
        try {
            btnShipmentDetail = (ImageButton) view.findViewById(R.id.btnShipmentDetail);

            if (Utility.ShippingNumber.equals("") || Utility.TrailerNumber.equals(""))
                btnShipmentDetail.setBackground(ContextCompat.getDrawable(getActivity(), R.drawable.ic_elog_ship));
            else
                btnShipmentDetail.setBackground(ContextCompat.getDrawable(getActivity(), R.drawable.ic_event_shipment_24));

            btnShipmentDetail.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    FragmentManager manager = getFragmentManager();
                    FragmentTransaction ft = manager.beginTransaction();
                    Fragment prev = manager.findFragmentByTag("shippingtrailers_dialog");
                    if (prev != null) {
                        ft.remove(prev);
                        ft.commitNow();
                        ft = manager.beginTransaction();
                    }

                    infosDialog = new InputInformationDialog();

                    infosDialog.mListener = ELogFragment.this;

                    infosDialog.show(ft, "shippingtrailers_dialog");

                }
            });

            tvSpecialStatus = (TextView) view.findViewById(R.id.tvSpecialStatus);

            chkRules = (CheckBox) view.findViewById(R.id.chkRules);
            chkRules.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    if (!Utility.HutchConnectStatusFg) {
                        Utility.showAlertMsg("App does not allow to change status while bluetooth is disconnected!");
                        chkRules.setChecked(!chkRules.isChecked());
                        return;
                    }

                    callChangeRule();
                    chkRules.setChecked(!chkRules.isChecked());

                }
            });
            //chkRules.setClickable(false);
            tvOffDutyTime = (TextView) view.findViewById(R.id.tvOffDutyTime);
            tvSleeperTime = (TextView) view.findViewById(R.id.tvSleeperTime);
            tvDrivingTime = (TextView) view.findViewById(R.id.tvDrivingTime);
            tvOnDutyTime = (TextView) view.findViewById(R.id.tvOnDutyTime);
            tvViolation = (TextView) view.findViewById(R.id.tvViolation);
            tvViolation.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    try {
                        // Commented for new logic of HOS
                        /*if (currentStatus != 3 || ViolationDT == null) {
                            return;
                        }*/
                        if (!MainActivity._violationFg) {
                            return;
                        }

                        final AlertDialog alertDialog = new AlertDialog.Builder(Utility.context).create();
                        alertDialog.setCancelable(true);
                        alertDialog.setCanceledOnTouchOutside(false);
                        alertDialog.setTitle(MainActivity.ViolationTitle);
                        alertDialog.setIcon(DIALOGBOX_ICON);
                        alertDialog.setMessage(MainActivity.ViolationDescription);
                        alertDialog.setButton(DialogInterface.BUTTON_NEUTRAL, getString(R.string.ok),
                                new DialogInterface.OnClickListener() {

                                    @Override
                                    public void onClick(DialogInterface dialog, int which) {
                                        alertDialog.cancel();
                                    }
                                });
                        alertDialog.show();
                    } catch (Exception ex) {
                        LogFile.write("onViolationClick Alert Msg: " + ex.getMessage(), LogFile.USER_INTERACTION, LogFile.ERROR_LOG);
                        LogDB.writeLogs(ELogFragment.class.getName(), "::onViolationClick Alert Msg:", ex.getMessage(), Utility.printStackTrace(ex));
                    }
                }
            });
            tvViolation.setSelected(true);
            tvViolationDate = (TextView) view.findViewById(R.id.tvViolationDate);

            tvRemaingTime = (TextView) view.findViewById(R.id.tvRemaingTime);
            tvRemaingTime.setOnClickListener(this);

            tvTimeZone = (TextView) view.findViewById(R.id.tvTimeZoneValue);
            tvTimeZone.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    fabSwitchUser.performClick();
                }
            });

            tvVehicleMiles = (TextView) view.findViewById(R.id.tvVehicleMilesValue);
            tvVehicleMiles.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    fabSwitchUser.performClick();
                }
            });
            tvVehicleMiles.setVisibility(View.VISIBLE);
            tvAccumulatedMilesValue = (TextView) view.findViewById(R.id.tvAccumulatedMilesValue);
            tvSBSplit = (TextView) view.findViewById(R.id.tvSBSplit);
            tvSBSplit.setVisibility(View.GONE);

            tvTotalDistance = (TextView) view.findViewById(R.id.tvTotalDistanceValue);
            tvTotalDistanceValue = (TextView) view.findViewById(R.id.tvTotalDistanceValue);
            tvTotalDistanceValue.setVisibility(View.VISIBLE);
            tvTotalDistanceValue.setText("N/A");
            //tvTotalDistanceValue.setVisibility(View.GONE);
            /*tvTotalDistanceValue.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    launchTotalDistanceDialog();
                }
            });*/

            updateTodayDistance(todayDistance);

            tvCanadaRule = (TextView) view.findViewById(R.id.tvCanadaRule);
            tvCanadaRuleValue = (TextView) view.findViewById(R.id.tvCanadaRuleValue);
            tvUSRule = (TextView) view.findViewById(R.id.tvUSRule);
            tvUSRule.setSelected(true);
            tvUSRuleValue = (TextView) view.findViewById(R.id.tvUSRuleValue);

            tvRestTime = (TextView) view.findViewById(R.id.tvRestTime);
            tvRestTimeValue = (TextView) view.findViewById(R.id.tvRestTimeValue);

            tvCoDriver = (TextView) view.findViewById(R.id.tvCoDriverValue);
            tvCoDriver.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    fabSwitchUser.performClick();
                }
            });

            tvSwitchUser = (TextView) getActivity().findViewById(R.id.tvSwitchUser);

            if (Utility.user2.getAccountId() == 0) {
                tvSwitchUser.setText("Login Team");
            } else {
                if (Utility.user1.isOnScreenFg()) {
                    tvSwitchUser.setText("Switch Driver");
                } else
                    tvSwitchUser.setText("Switch Driver");
            }

            butDutyStatus = (Button) view.findViewById(R.id.butDutyStatus);
            butDutyStatus.setOnClickListener(this);

            butRemainingTime = (Button) view.findViewById(R.id.butRemainingTime);
            butRemainingTime.setOnClickListener(this);

            pbTimeCountProgress = (ProgressBar) view.findViewById(R.id.whiteProgressBar);
            pbTotalDrivingHours = (ProgressBar) view.findViewById(R.id.redProgressBar);
            pbTotalWorkShiftHour = (ProgressBar) view.findViewById(R.id.blueProgressBar);
            pbTotalCanadaRule = (ProgressBar) view.findViewById(R.id.greenProgressBar);
            pbTotalUSRule = (ProgressBar) view.findViewById(R.id.yellowProgressBar);

            tvTotalDrivingHours = (TextView) view.findViewById(R.id.tvDrivingHoursValue);
            tvTotalWorkShiftHours = (TextView) view.findViewById(R.id.tvWorkShiftValue);


            // as per requirement of gary sir do not show time zone offset just show the name of timezone
            // tvTimeZone.setText(Utility.TimeZoneOffsetUTC);
            tvTimeZone.setText(ZoneList.getTimezoneName(false));

            getRules();
            currentRule = DailyLogDB.getCurrentRule(Utility.onScreenUserId);
            chkRules.setChecked(currentRule == 1 || currentRule == 2 || currentRule > 4);
            pbTotalCanadaRule.setMax(currentRule == 2 ? 120 * 60 : 70 * 60);
            pbTotalUSRule.setMax(currentRule == 4 ? 60 * 60 : 70 * 60);

            //tvCurrentRuleLabel.setText(getRule(currentRule - 1));
            if (currentRule == 1 || currentRule == 2 || currentRule == 7) //Canada rule
            {
                if (currentRule == 7) {
                    pbTotalCanadaRule.setMax(0);
                }

                pbTimeCountProgress.setMax(13 * 60);
                pbTotalDrivingHours.setMax(13 * 60);
                pbTotalWorkShiftHour.setMax(16 * 60);
                tvCanadaRule.setText(getRule(currentRule - 1));
            } else if (currentRule == 3 || currentRule == 4) {
                pbTimeCountProgress.setMax(11 * 60);
                pbTotalDrivingHours.setMax(11 * 60);
                pbTotalWorkShiftHour.setMax(14 * 60);
                tvUSRule.setText(getRule(currentRule - 1));
                tvCanadaRule.setText(getRule(0)); //default canada 70 hour rule label if current rule is US
            } else if (currentRule == 5) {
                pbTimeCountProgress.setMax(13 * 60);
                pbTotalDrivingHours.setMax(13 * 60);
                pbTotalWorkShiftHour.setMax(15 * 60);
                tvCanadaRule.setText(getRule(currentRule - 1));
                pbTotalCanadaRule.setMax(0);
                Utility._appSetting.setUnit(1);
                SettingsDB.CreateSettings();
            } else if (currentRule == 6) {
                pbTimeCountProgress.setMax(13 * 60);
                pbTotalDrivingHours.setMax(13 * 60);
                pbTotalWorkShiftHour.setMax(15 * 60);
                tvCanadaRule.setText(getRule(currentRule - 1));
                pbTotalCanadaRule.setMax(80 * 60);
                Utility._appSetting.setUnit(1);
                SettingsDB.CreateSettings();

            }


            //update co-driver name if existed
            if (Utility.user1.isOnScreenFg()) {
                if (Utility.user2.getAccountId() > 0) {
                    tvCoDriver.setText(Utility.user2.getFirstName() + " " + Utility.user2.getLastName());
                }
            } else {
                tvCoDriver.setText(Utility.user1.getFirstName() + " " + Utility.user1.getLastName());
            }

            rlLoadingPanel = (ProgressBar) view.findViewById(R.id.loadingPanel);
            fabPost = (ImageButton) getActivity().findViewById(R.id.fab_post);
            fabSync = (ImageButton) getActivity().findViewById(R.id.fab_sync);
            fabChangeRule = (ImageButton) getActivity().findViewById(R.id.fab_change_rule);
            fabCertify = (ImageButton) getActivity().findViewById(R.id.fab_certify);
            fabCertify.setEnabled(false);
            fabUncertify = (ImageButton) getActivity().findViewById(R.id.fab_uncertify);
            fabActive = (ImageButton) getActivity().findViewById(R.id.fab_active);
            fabSwitchUser = (ImageButton) getActivity().findViewById(R.id.fab_switch_user);

            fabStop = (FloatingActionButton) getActivity().findViewById(R.id.fab_stop);
            restView = (View) getActivity().findViewById(R.id.restView);
            layout_menu = (ConstraintLayout) getActivity().findViewById(R.id.layout_floating_menu);

            /*  layoutUndocking = (LinearLayout) getActivity().findViewById(R.id.layout_fab_undocking);*/

            fabMenu = (ImageButton) view.findViewById(R.id.fab);
            if (Utility.user1.getAccountType() == 2) {
                fabMenu.setVisibility(View.GONE);
            }
            //switch for Defered Day
            fabDefereDay = (ImageButton) getActivity().findViewById(R.id.fab_DeferedSwitch);
            tvDeffered = (TextView) getActivity().findViewById(R.id.tvDeffered);

            fabMenu.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    Animation show_fab_menu = AnimationUtils.loadAnimation(getActivity().getApplication(), R.anim.fade_in);
                    Animation hide_fab_menu = AnimationUtils.loadAnimation(getActivity().getApplication(), R.anim.fade_out);

                    if (layout_menu.getVisibility() == View.INVISIBLE) {
                        android.animation.ObjectAnimator.ofFloat(fabMenu, "rotation", 0, 45).start();
                        layout_menu.startAnimation(show_fab_menu);
                        layout_menu.setVisibility(View.VISIBLE);
                        restView.setVisibility(View.VISIBLE);
                     /*   if (certifyFg == 1) {
                            fabCertify.setVisibility(View.VISIBLE);
                            fabUncertify.setVisibility(View.INVISIBLE);
                        } else {
                            fabCertify.setVisibility(View.INVISIBLE);
                            fabUncertify.setVisibility(View.VISIBLE);
                        }*/
                        int deferredDay = DailyLogDB.getDeferedDay(Utility.onScreenUserId, 0);

                        fabDefereDay.setEnabled(deferredDay > 0 ? false : true);
                        if (deferredDay > 0) {
                            tvDeffered.setText("Deferred Day " + deferredDay);
                            fabDefereDay.getBackground().setAlpha(100);
                        } else {
                            tvDeffered.setText(getString(R.string.action_defered_day));
                            fabDefereDay.getBackground().setAlpha(255);
                        }
                        fabShowing = true;
                    } else {
                        android.animation.ObjectAnimator.ofFloat(fabMenu, "rotation", 45, 0).start();
                        layout_menu.startAnimation(hide_fab_menu);
                        layout_menu.setVisibility(View.INVISIBLE);
                        restView.setVisibility(View.INVISIBLE);
                        fabShowing = false;
                    }

                }
            });

            restView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Animation hide_fab_menu = AnimationUtils.loadAnimation(getActivity().getApplication(), R.anim.fade_out);

                    if (layout_menu.getVisibility() == View.VISIBLE) {
                        android.animation.ObjectAnimator.ofFloat(fabMenu, "rotation", 45, 0).start();
                        layout_menu.startAnimation(hide_fab_menu);
                        layout_menu.setVisibility(View.INVISIBLE);
                        restView.setVisibility(View.INVISIBLE);
                        fabShowing = false;
                    }
                }
            });

            fabPost.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    try {
                        Log.d(TAG, "Post");
                        android.animation.ObjectAnimator.ofFloat(fabMenu, "rotation", 45, 0).start();
                        layout_menu.setVisibility(View.INVISIBLE);
                        restView.setVisibility(View.INVISIBLE);
                        callPost();
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            });

            fabSync.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Log.d(TAG, "Sync");
                    android.animation.ObjectAnimator.ofFloat(fabMenu, "rotation", 45, 0).start();
                    layout_menu.setVisibility(View.INVISIBLE);
                    restView.setVisibility(View.INVISIBLE);
                    callSync();
                }
            });

            fabChangeRule.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (!Utility.HutchConnectStatusFg) {
                        Utility.showAlertMsg("App does not allow to change status while bluetooth is disconnected!");
                        return;
                    }

                    Log.d(TAG, "Change Rule");
                    android.animation.ObjectAnimator.ofFloat(fabMenu, "rotation", 45, 0).start();
                    layout_menu.setVisibility(View.INVISIBLE);
                    restView.setVisibility(View.INVISIBLE);
                    callChangeRule();
                }
            });

            fabUncertify.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Log.d(TAG, "Certify");
                    android.animation.ObjectAnimator.ofFloat(fabMenu, "rotation", 45, 0).start();
                    layout_menu.setVisibility(View.INVISIBLE);
                    restView.setVisibility(View.INVISIBLE);
                    // callCertify();

                    // Open Logbook
                    if (mListener != null) {
                        mListener.callCertifyLogBook();
                    }
                }
            });

            fabActive.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Log.d(TAG, "Active");
                    android.animation.ObjectAnimator.ofFloat(fabMenu, "rotation", 45, 0).start();
                    layout_menu.setVisibility(View.INVISIBLE);
                    restView.setVisibility(View.INVISIBLE);
                    if (MainActivity.activeCurrentDutyStatus == 3 || MainActivity.activeCurrentDutyStatus == 5 || MainActivity.activeCurrentDutyStatus == 6) {
                        Utility.showAlertMsg(getString(R.string.codriver_change_dutystatus_off_sleeper_onduty));
                        return;
                    }
                    callActive();
                }
            });

            fabActive.setEnabled(Utility.onScreenUserId != Utility.activeUserId);


            // on click of the Deferday button
            fabDefereDay.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Log.d(TAG, "Defered Day ");
                    android.animation.ObjectAnimator.ofFloat(fabMenu, "rotation", 45, 0).start();
                    layout_menu.setVisibility(View.INVISIBLE);
                    restView.setVisibility(View.INVISIBLE);

                    callDeferDay();

                }
            });


            fabSwitchUser.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Log.d(TAG, "Switch User");
                    android.animation.ObjectAnimator.ofFloat(fabMenu, "rotation", 45, 0).start();
                    layout_menu.setVisibility(View.INVISIBLE);
                    restView.setVisibility(View.INVISIBLE);

                    SwitchDriver();
                }
            });

           /* fabStop.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Log.d(TAG, "STOP");
                    android.animation.ObjectAnimator.ofFloat(fabMenu, "rotation", 45, 0).start();
                    CanMessages.Speed = "0";
                }
            });*/

            fabUndocking = (ImageButton) getActivity().findViewById(R.id.fab_undocking);
            fabUndocking.setEnabled(false);
            fabUndocking.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    callUndocking();
                }
            });

            currentDate = Utility.dateOnlyGet(Utility.newDate());

            CheckCountry();
        } catch (Exception e) {
            LogFile.write(ELogFragment.class.getName() + "::initializeControls Error:" + e.getMessage(), LogFile.USER_INTERACTION, LogFile.ERROR_LOG);
            LogDB.writeLogs(ELogFragment.class.getName(), "::initializeControls:", e.getMessage(), Utility.printStackTrace(e));

        }
    }

    private void initialize(boolean addRunnable) {
        try {

            DutyStatusGet();
            if (addRunnable) {
                try {
                    handlerElog.removeCallbacksAndMessages(null);
                    handlerElog.postDelayed(runnableStatus, 60000);
                } catch (Exception exe) {

                }
            }

        } catch (Exception e) {
            LogFile.write(ELogFragment.class.getName() + "::initialize Error:" + e.getMessage(), LogFile.USER_INTERACTION, LogFile.ERROR_LOG);
            LogDB.writeLogs(ELogFragment.class.getName(), "::initialize Error:", e.getMessage(), Utility.printStackTrace(e));

        }
    }

    private void DutyStatusGet() {
        try {

            drawLine(dutyStatusList, currentDate);
            StatusHourGet(dutyStatusList);
            eventAdapter.notifyDataSetChanged();


        } catch (Exception e) {
            LogFile.write(ELogFragment.class.getName() + "::DutyStatusGet Error:" + e.getMessage(), LogFile.USER_INTERACTION, LogFile.ERROR_LOG);
            LogDB.writeLogs(ELogFragment.class.getName(), "::DutyStatusGet Error:", e.getMessage(), Utility.printStackTrace(e));

        }
    }

    //Created By: Deepak Sharma
    //Created Date: 5/27/2016
    //Purpose: get rule when event changed
    private RuleBean eventRuleGet(Date eventTime, ArrayList<RuleBean> ruleList) {
        RuleBean obj = new RuleBean();
        obj.setRuleId(1);
        obj.setRuleStartTime(currentDate);
        obj.setRuleEndTime(Utility.addDays(currentDate, 1));
        for (RuleBean bean : ruleList) {
            obj.setRuleEndTime(bean.getRuleStartTime());
            if (bean.getRuleStartTime().before(eventTime) || bean.getRuleStartTime().equals(eventTime)) {
                return bean;
            }
        }
        return obj;
    }

    //Created By: Deepak Sharma
    //Created Date: 5/27/2016
    //Purpose: get rule when event changed
    private ArrayList<RuleBean> eventRuleListGet(Date eventStartTime, Date eventEndTime, ArrayList<RuleBean> ruleList) {
        ArrayList<RuleBean> obj = new ArrayList<>();

        for (RuleBean bean : ruleList) {
            if ((bean.getRuleStartTime().after(eventStartTime) || bean.getRuleStartTime().equals(eventStartTime)) && bean.getRuleStartTime().before(eventEndTime)) {
                obj.add(bean);
            }
        }
        Collections.sort(obj, RuleBean.dateAsc);
        return obj;
    }

    private void drawViolationArea() {

        ArrayList<ViolationModel> vList = MainActivity.violationList;
        int startMinutes = 0;
        Date endTime = null, startTime = null;
        for (int i = 0; i < vList.size(); i++) {

            startTime = vList.get(i).getViolationDate();

            if (!Utility.dateOnlyGet(startTime).equals(Utility.newDateOnly()))
                continue;

            startMinutes = (int) (startTime.getTime() - currentDate.getTime()) / (1000 * 60);
            if (endTime != null && startTime.before(endTime)) {
                continue;
            }
            endTime = Utility.addSeconds(startTime, (int) vList.get(i).getViolationDuration());
            if (Utility._appSetting.getGraphLine() == 1 && endTime.after(Utility.newDate())) {
                if (startTime.after(Utility.newDate()))
                    break;
                endTime = Utility.newDate();
            }

            int endMinutes = (int) (endTime.getTime() - currentDate.getTime()) / (1000 * 60);
            drawRect(getX(startMinutes), getRectY(1), getX(endMinutes), getRectY(4));

        }

    }

    private void drawLine(ArrayList<DutyStatusBean> dutyStatus, Date logDate) {
        //clear bitmap bmp 333
        canvas.drawColor(Color.TRANSPARENT, PorterDuff.Mode.MULTIPLY);
        try {


            String logDT = Utility.format(logDate, fullFormat);
            ArrayList<RuleBean> ruleList = DailyLogDB.getRuleByDate(logDT, Utility.onScreenUserId, dailyLogId);
            Collections.sort(ruleList, RuleBean.dateDesc);
            int ruleId = 1, startMinutes, endMinutes = 0;

            for (int i = 0; i < dutyStatus.size(); i++) {
                DutyStatusBean item = dutyStatus.get(i);
                int status = item.getStatus();
                int specialStatus = item.getSpecialStatus();
                Date startTime = Utility.parse(item.getStartTime()), endTime = Utility.parse(item.getEndTime());

                // graph line upto current time
                if (Utility._appSetting.getGraphLine() == 1 && i == dutyStatus.size() - 1) {
                    endTime = Utility.newDate();
                }

                RuleBean rule = eventRuleGet(startTime, ruleList);
                ruleId = rule.getRuleId();
                if (rule.getRuleEndTime().before(endTime)) {

                    startMinutes = (int) (startTime.getTime() - logDate.getTime()) / (1000 * 60);
                    endMinutes = (int) (rule.getRuleEndTime().getTime() - logDate.getTime()) / (1000 * 60);
                    if (i == 0 && startMinutes > 0) {
                        startMinutes = 0;
                    }
                    drawLine(getX(startMinutes), getY(status), getX(endMinutes), getY(status), ruleId, specialStatus);

                    //Log.d(TAG, getX(startMinutes) +"/"+ getY(status)+"/"+ getX(endMinutes)+"/"+ getY(status) + "--" + ruleId + "|status=" + status);
                    startTime = rule.getRuleEndTime();
                    ArrayList<RuleBean> ruleEventList = eventRuleListGet(startTime, endTime, ruleList);
                    for (RuleBean ruleBean : ruleEventList) {
                        if (ruleBean.getRuleStartTime().equals(rule.getRuleStartTime()))
                            continue;
                        startMinutes = (int) (startTime.getTime() - logDate.getTime()) / (1000 * 60);
                        endMinutes = (int) ((ruleBean.getRuleEndTime().before(endTime) ? ruleBean.getRuleEndTime() : endTime).getTime() - logDate.getTime()) / (1000 * 60);
                        ruleId = ruleBean.getRuleId();
                        drawLine(getX(startMinutes), getY(status), getX(endMinutes), getY(status), ruleId, specialStatus);

                        startTime = ruleBean.getRuleEndTime();
                    }
                } else {

                    startMinutes = (int) (startTime.getTime() - logDate.getTime()) / (1000 * 60);
                    endMinutes = (int) (endTime.getTime() - logDate.getTime()) / (1000 * 60);
                    if (i == 0 && startMinutes > 0) {
                        startMinutes = 0;
                    }

                    drawLine(getX(startMinutes), getY(status), getX(endMinutes), getY(status), ruleId, specialStatus);
                }

                if (i < dutyStatus.size() - 1) {
                    item = dutyStatus.get(i + 1);
                    drawLine(getX(endMinutes), getY(status), getX(endMinutes), getY(item.getStatus()), ruleId, item.getSpecialStatus());
                }
            }

            if (dutyStatus.size() == 0) {
                if (ruleList.size() > 0) {
                    ruleId = ruleList.get(0).getRuleId();

                }
                endMinutes = 1439;
                drawLine(getX(0), getY(1), getX(endMinutes), getY(1), ruleId, 0);
            } else {
                int status = dutyStatus.get(dutyStatus.size() - 1).getStatus();
                if (Utility._appSetting.getGraphLine() == 1) {
                    drawLine(getX(endMinutes), getY(status), getX(1439), getY(status), ruleId, 0, true);
                }
            }
            imgDutyStatus.setImageDrawable(null);
            imgDutyStatus.setImageBitmap(bmp);

        } catch (Exception exe) {
            Log.d(TAG, "drawLine got exception");
            LogFile.write(ELogFragment.class.getName() + "::drawLine2 Error:" + exe.getMessage(), LogFile.USER_INTERACTION, LogFile.ERROR_LOG);
            LogDB.writeLogs(ELogFragment.class.getName(), "::drawLine2 Error:", exe.getMessage(), Utility.printStackTrace(exe));
        }
    }

    DashPathEffect effects = new DashPathEffect(new float[]{4, 2, 4, 2}, 0);

    private void drawLine(float x, float y, float xend, float yend, int ruleId, int specialStatus, boolean isDotted) {
        try {
            Paint p = new Paint();
            int color;
            if (ruleId <= 2) {
                color = Utility._appSetting.getColorLineCanada();
            } else {
                color = Utility._appSetting.getColorLineUS();
            }
            color = color == 0 ? Color.BLUE : color;
            p.setColor(color);
            int width = 4;

            if (specialStatus > 0) {
                width = 10;
            }
            p.setStrokeWidth(width);
            if (isDotted) {
                p.setPathEffect(effects);
            }
          /*  if (personalUseFg == 1) {
                p.setPathEffect(effects);
            }*/
            canvas.drawLine(x, y, xend, yend, p);
        } catch (Exception e) {
            LogFile.write(ELogFragment.class.getName() + "::drawLine1 Error:" + e.getMessage(), LogFile.USER_INTERACTION, LogFile.ERROR_LOG);
            LogDB.writeLogs(ELogFragment.class.getName(), "::drawLine1 Error:", e.getMessage(), Utility.printStackTrace(e));

        }
    }

    private void drawLine(float x, float y, float xend, float yend, int ruleId, int specialStatus) {
        try {
            Paint p = new Paint();
            int color;
            if (ruleId <= 2) {
                color = Utility._appSetting.getColorLineCanada();
            } else {
                color = Utility._appSetting.getColorLineUS();
            }
            color = color == 0 ? Color.BLUE : color;
            p.setColor(color);

            int width = 4;
            if (specialStatus > 0) {
                width = 10;
            }
           /* if (!Utility.isLargeScreen(getContext().getApplicationContext())) {
                width = 1;
            }*/
            p.setStrokeWidth(width);
          /*  if (personalUseFg == 1) {
                p.setPathEffect(effects);
            }*/
            canvas.drawLine(x, y, xend, yend, p);
        } catch (Exception e) {
            LogFile.write(ELogFragment.class.getName() + "::drawLine1 Error:" + e.getMessage(), LogFile.USER_INTERACTION, LogFile.ERROR_LOG);
            LogDB.writeLogs(ELogFragment.class.getName(), "::drawLine1:", e.getMessage(), Utility.printStackTrace(e));
        }
    }

    private void drawRect(float x, float y, float xend, float yend) {

        try {
            Paint p = new Paint();

            p.setColor(getResources().getColor(R.color.red15));
            //p.setStrokeWidth(1);

            canvas.drawRect(x, y, xend, yend, p);
        } catch (Exception e) {
            LogFile.write(ELogFragment.class.getName() + "::drawRect Error:" + e.getMessage(), LogFile.USER_INTERACTION, LogFile.ERROR_LOG);
            LogDB.writeLogs(ELogFragment.class.getName(), "::drawRect Error:", e.getMessage(), Utility.printStackTrace(e));

        }
    }

    public void launchRuleChange() {
        try {

            FragmentManager manager = getFragmentManager();
            FragmentTransaction ft = manager.beginTransaction();

            Fragment prev = manager.findFragmentByTag("rulechange_dialog");
            if (prev != null) {
                ft.remove(prev);
                //ft.addToBackStack(null);
                ft.commitNow();
                ft = manager.beginTransaction();
            }

            RuleChangeDialog ruleChangeDialog = new RuleChangeDialog();
            ruleChangeDialog.mListener = this;

            ruleChangeDialog.setCurrentRule(currentRule);
            ruleChangeDialog.show(ft, "rulechange_dialog");
        } catch (Exception e) {
            LogFile.write(ELogFragment.class.getName() + "::launchRuleChange Error:" + e.getMessage(), LogFile.USER_INTERACTION, LogFile.ERROR_LOG);
            LogDB.writeLogs(ELogFragment.class.getName(), "launchRuleChange", e.getMessage(), Utility.printStackTrace(e));
        }
    }

    public void launchDutyStatusChange(boolean fromPopupDialog) {
        try {
            /*if (getFragmentManager().getBackStackEntryCount() > 0) {
                for (int i = 0; i < getFragmentManager().getBackStackEntryCount(); i++)
                    getFragmentManager().popBackStackImmediate();
            }*/

            FragmentManager manager = getFragmentManager();
            FragmentTransaction ft = manager.beginTransaction();

            Fragment prev = manager.findFragmentByTag("dutystatus_dialog");
            if (prev != null) {
                ft.remove(prev);
                //ft.addToBackStack(null);
                ft.commitNow();
                ft = manager.beginTransaction();
            }

            DutyStatusChangeDialog dutyDialog = DutyStatusChangeDialog.newInstance();
            dutyDialog.mListener = this;

            if (fromPopupDialog) {
                dutyDialog.setCurrentStatus(3);
                dutyDialog.setActiveDriver(true);
            } else {
                dutyDialog.setCurrentStatus(currentStatus);
                dutyDialog.setActiveDriver(false);
            }
            bDialogShowing = true;

            dutyDialog.show(ft, "dutystatus_dialog");
        } catch (Exception e) {
            LogFile.write(ELogFragment.class.getName() + "::launchDutyStatusChange Error:" + e.getMessage(), LogFile.USER_INTERACTION, LogFile.ERROR_LOG);
            LogDB.writeLogs(ELogFragment.class.getName(), "launchDutyStatusChange", e.getMessage(), Utility.printStackTrace(e));

        }
    }

    // Created By: Deepak Sharma
    // Created Date: 6 May 2020
    // Purpose: launch dialog to post data
    public void launchDataPostDialog(final ArrayList<DailyLogBean> data) {
        try {
            final AlertDialog alertDialog = new AlertDialog.Builder(getContext()).create();
            alertDialog.setCancelable(true);
            alertDialog.setCanceledOnTouchOutside(false);
            alertDialog.setTitle(DIALOGBOX_TITLE);
            alertDialog.setIcon(DIALOGBOX_ICON);
            alertDialog.setMessage("You have unposted data. You must post all data proceeding.");
            alertDialog.setButton(DialogInterface.BUTTON_NEUTRAL, "OK",
                    new DialogInterface.OnClickListener() {

                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            alertDialog.cancel();

                            try {
                                FragmentManager manager = getFragmentManager();
                                FragmentTransaction ft = manager.beginTransaction();

                                Fragment prev = manager.findFragmentByTag("data_post_dialog");
                                if (prev != null) {
                                    ft.remove(prev);

                                    ft.commitNow();
                                    ft = manager.beginTransaction();
                                }

                                DataPostDialogFragment dataPostDialog = DataPostDialogFragment.newInstance(data, Utility.onScreenUserId, dailyLogId);
                                dataPostDialog.mListener = ELogFragment.this;
                                dataPostDialog.show(ft, "data_post_dialog");
                            } catch (Exception exe) {

                            }
                        }
                    });
            alertDialog.show();

        } catch (Exception e) {
            LogFile.write(ELogFragment.class.getName() + "::launchDutyStatusChange Error:" + e.getMessage(), LogFile.USER_INTERACTION, LogFile.ERROR_LOG);
            LogDB.writeLogs(ELogFragment.class.getName(), "launchDutyStatusChange", e.getMessage(), Utility.printStackTrace(e));

        }
    }

    private int getX(int minutes) {
        float boxWidth = (float) imageWidth;//(float) imgDutyStatus.getWidth();
        //return (int) ((float) Math.ceil(minutes / boxWidth) * boxWidth - boxWidth);
        int x = (int) ((float) boxWidth / 1440 * minutes);
        return x;
    }

    private int getY(int status) {

        int headerHeight = Math.round(imageHeight / 5.27f);
        float boxHeight = (float) (imageHeight - headerHeight) / 4;

        int y = (int) ((status - 1) * boxHeight + boxHeight / 2 + headerHeight);

        return y;
    }

  /*  private int getY(int status) {
        float boxHeight = (float) (imageHeight - 54.0) / 4;
        if (!Utility.isLargeScreen(getContext().getApplicationContext())) {
            boxHeight = (float) (imageHeight - 22.0) / 4;
        }

        int y = (int) ((status - 1) * boxHeight + boxHeight / 2 + 64.0);

        if (!Utility.isLargeScreen(getContext().getApplicationContext())) {
            y = (int) ((status - 1) * boxHeight + boxHeight / 2 + 22.0);
        } else {
            if ((Configuration.ORIENTATION_PORTRAIT == getResources().getConfiguration().orientation)) {
                boxHeight = (float) (imageHeight - 44.0) / 4;
                y = (int) ((status - 1) * boxHeight + boxHeight / 2 + 44.0);
            } else {
                y = (int) ((status - 1) * boxHeight + boxHeight / 2 + 54.0);
            }
        }

        return y;
    }*/

    private int getRectY(int status) {
        float boxHeight = (float) (imageHeight - 54.0) / 4;
        if (!Utility.isLargeScreen(getContext().getApplicationContext())) {
            boxHeight = (float) (imageHeight - 22.0) / 4;
        }

        //int y = (int) ((status - 1) * boxHeight + boxHeight / 2 + 64.0);
        int y = 0;
        if (!Utility.isLargeScreen(getContext().getApplicationContext())) {
            y = (int) (status * boxHeight + 22.0);
            if (status == 1) {
                y = 22;
            }
        } else {
            if ((Configuration.ORIENTATION_PORTRAIT == getResources().getConfiguration().orientation)) {
                boxHeight = (float) (imageHeight - 44.0) / 4;

                y = (int) (status * boxHeight + 44.0);
                if (status == 1) {
                    y = 44;
                }

            } else {

                y = (int) (status * boxHeight + 54.0);
                if (status == 1) {
                    y = 54;
                }
            }

        }

        return y;
    }

    private void getRules() {
        try {
            listRules = new ArrayList<String>();
            listRules.add(getResources().getString(R.string.cycle_1));
            listRules.add(getResources().getString(R.string.cycle_2));
            listRules.add(getResources().getString(R.string.us_rule_1));
            listRules.add(getResources().getString(R.string.us_rule_2));
            listRules.add(getResources().getString(R.string.canada_rule_AB));
            listRules.add(getResources().getString(R.string.canada_logging_truck));
            listRules.add(getResources().getString(R.string.canada_oil_well_service));
        } catch (Exception e) {
            LogFile.write(ELogFragment.class.getName() + "::getRules Error:" + e.getMessage(), LogFile.USER_INTERACTION, LogFile.ERROR_LOG);
            LogDB.writeLogs(ELogFragment.class.getName(), "::getRules Error:", e.getMessage(), Utility.printStackTrace(e));

        }
    }

    private String getRule(int ruleIdx) {
        if (listRules == null)
            return "";
        if (ruleIdx < 0 || ruleIdx >= listRules.size())
            return "";
        return listRules.get(ruleIdx);
    }

    private void StatusHourGet(ArrayList<DutyStatusBean> dutyStatus) {
        try {
            int offDuty = 0, sleeper = 0, driving = 0, onDuty = 0;
            for (DutyStatusBean bean : dutyStatus) {

                Date startTime = Utility.parse(bean.getStartTime());
                //startTime = Utility.addSeconds(startTime, -startTime.getSeconds());

                Date endTime = Utility.parse(bean.getEndTime());
                //endTime = Utility.addSeconds(endTime, -endTime.getSeconds());

                // accuracy upto seconds
                int totalHours = Math.round((endTime.getTime() - startTime.getTime()) / (1000));

                if (Utility._appSetting.getGraphLine() == 1) {
                    endTime = Utility.parse(bean.getEndTime());
                    if (endTime.after(Utility.newDate())) {
                        startTime = Utility.parse(bean.getStartTime());
                        totalHours = Math.round(((Utility.newDate()).getTime() - startTime.getTime()) / (1000));
                        bean.setTotalMinutes(totalHours);
                    }
                }

                switch (bean.getStatus()) {
                    case 1:
                        offDuty += totalHours;
                        break;
                    case 2:
                        sleeper += totalHours;
                        break;
                    case 3:
                        driving += totalHours;
                        break;
                    case 4:
                        onDuty += totalHours;
                        break;
                }
            }
            tvOffDutyTime.setText(Utility.getTimeFromSecondsInMin(offDuty));
            tvSleeperTime.setText(Utility.getTimeFromSecondsInMin(sleeper));
            tvDrivingTime.setText(Utility.getTimeFromSecondsInMin(driving));
            tvOnDutyTime.setText(Utility.getTimeFromSecondsInMin(onDuty));
        } catch (Exception e) {
            LogFile.write(ELogFragment.class.getName() + "::StatusHourGet Error:" + e.getMessage(), LogFile.USER_INTERACTION, LogFile.ERROR_LOG);
            LogDB.writeLogs(ELogFragment.class.getName(), "StatusHourGet", e.getMessage(), Utility.printStackTrace(e));
        }
    }

    private List<EventBean> getListEvents() {
        List<EventBean> dutyStatusList = EventDB.DutyStatusChangedEventGetByLogId(Utility.onScreenUserId, dailyLogId);

        TotalDistanceGet();
        return AddViolationToEvents(dutyStatusList);
    }


    // Deepak Sharma
    // 3 June 2016
    // add violation to events
    private List<EventBean> AddViolationToEvents(List<EventBean> eventList) {
        if (Utility._appSetting.getShowViolation() == 0)
            return eventList;

        ArrayList<ViolationModel> vList = MainActivity.violationList;

        Date currTime = Utility.newDate();
        for (int i = 0; i < vList.size(); i++) {

            Date startTime = vList.get(i).getViolationDate();
            int totalMinutes = (int) (vList.get(i).getViolationDuration() / 60d);
            // Date endTime = Utility.addMinutes(startTime, totalMinutes);

            Date vDate = Utility.dateOnlyGet(startTime);
            boolean isCurrent = vDate.equals(currentDate);
            if (!isCurrent)
                continue;

           /* if (startTime.after(currTime) || endTime.before(currentDate))
                continue;
*/
            EventBean event = new EventBean();
            event.setEventType(-1); //-1 for violation
            event.setEventDateTime(Utility.format(startTime, fullFormat));
            event.setViolation(vList.get(i).getRule());
            event.setViolationTitle(vList.get(i).getTitle());
            event.setViolationExplanation(vList.get(i).getDescription());
            event.setViolationMintes(Utility.getTimeFromMinute(totalMinutes));


            eventList.add(event);
        }


        Collections.sort(eventList, EventBean.dateDesc);
        return eventList;
    }

    //Created By: Deepak Sharma
    //Created Date: 12 March 2016
    //Purpose: Total distance of driver of day
    private void TotalDistanceGet() {
        try {
            List<EventBean> dutyStatusList = EventDB.TotalDistanceGetByLogId(dailyLogId);
            totalDistance = 0;
            statusStartOdometerReading = 0;
            int engineHour = 0;
            for (int i = 0; i < dutyStatusList.size(); i++) {
                EventBean bean = dutyStatusList.get(i);

                if (bean.getEventCode() == 3) {
                    if (statusStartOdometerReading == 0)
                        statusStartOdometerReading = Double.valueOf(bean.getOdometerReading()).intValue();
                } else {
                    if (statusStartOdometerReading > 0)
                        totalDistance += (Double.valueOf(bean.getOdometerReading()).intValue() - statusStartOdometerReading);
                    statusStartOdometerReading = 0;
                }
                engineHour = Double.valueOf(bean.getEngineHour()).intValue();
            }
            //send totalDistance, startOdometer to MainActivity
            mListener.setStartOdoMeter(statusStartOdometerReading);
            mListener.setTotalDistance(totalDistance);
            mListener.setStartEngineHour(engineHour);
            updateOdometer();
        } catch (Exception e) {
            LogDB.writeLogs(ELogFragment.class.getName(), "StatusHourGet", e.getMessage(), Utility.printStackTrace(e));

            LogFile.write(ELogFragment.class.getName() + "::StatusHourGet" + e.getMessage(), LogFile.USER_INTERACTION, LogFile.ERROR_LOG);
        }
    }

    public static ArrayList<DutyStatusBean> DutyStatusGet(Date date, ArrayList<DutyStatusBean> list) {

        ArrayList<DutyStatusBean> data = new ArrayList<>();
        try {
            Date nextDay = Utility.addDays(date, 1);

            for (int i = 0; i < list.size(); i++) {
                Date startDate = Utility.parse(list.get(i).getStartTime());
                Date endDate = Utility.parse(list.get(i).getEndTime());
                //Log.d(TAG, "Duty Status= " + list.get(i).getStatus() + " - Start Time=" + list.get(i).getStartTime() + " / End Time=" + list.get(i).getEndTime());
                int status = list.get(i).getStatus();
                int personalUseFg = list.get(i).getPersonalUse();
                startDate = startDate.before(date) ? date : startDate;
                endDate = endDate.after(nextDay) ? nextDay : endDate;
                if ((startDate.after(date) || endDate.after(date)) && startDate.before(nextDay)) {
                    int totalMinutes = (int) Math.round((endDate.getTime() - startDate.getTime()) / (1000 * 60.0));

                    DutyStatusBean bean = new DutyStatusBean();
                    bean.setStartTime(Utility.format(startDate, fullFormat));
                    bean.setEndTime(Utility.format(endDate, fullFormat));
                    bean.setStatus(status);
                    bean.setTotalMinutes(totalMinutes);
                    bean.setPersonalUse(personalUseFg);
                    data.add(bean);
                }
            }

        } catch (Exception e) {
            LogFile.write(ELogFragment.class.getName() + "::DutyStatusGet Error:" + e.getMessage(), LogFile.USER_INTERACTION, LogFile.ERROR_LOG);
            LogDB.writeLogs(ELogFragment.class.getName(), "::DutyStatusGet Error:", e.getMessage(), Utility.printStackTrace(e));

        }
        Collections.sort(data, DutyStatusBean.dateAsc);
        return data;
    }


    private void checkToSpeak() {
        try {
            if (currentStatus == 3) {

                // Commented for new logic of HOS
               /* if (ViolationDT == null) {
                    return;
                }*/

                if (!MainActivity._violationFg) {
                    return;
                }
                //   int hourLeft = (int) (ViolationDT.getTime() - (Utility.newDate()).getTime()) / (1000 * 60);
                int hourLeft = (int) (MainActivity.ViolationDT.getTime() - (Utility.newDate()).getTime()) / (1000 * 60);

                if (hourLeft < 0) {
                    hourLeft = 0;
                }

                if (Utility._appSetting.getViolationReading() == 1) {
                    if (hourLeft == 0) {
                        //Speech
                        MainActivity.textToSpeech.speak(getString(R.string.driving_hour_expired), TextToSpeech.QUEUE_ADD, null);
                    } else if (hourLeft <= 60) {
                        if (hourLeft % 5 == 0) {
                            MainActivity.textToSpeech.speak(getString(R.string.caution_driving_hours_about_to_expire) + hourLeft + getString(R.string.mintes_of_driving_time_remaining), TextToSpeech.QUEUE_ADD, null);

                        }
                    }
                }
            }
        } catch (Exception exe) {
            LogFile.write(ELogFragment.class.getName() + "::checkToSpeak Error:" + exe.getMessage(), LogFile.USER_INTERACTION, LogFile.ERROR_LOG);
            LogDB.writeLogs(ELogFragment.class.getName(), "::checkToSpeak Error:", exe.getMessage(), Utility.printStackTrace(exe));

        }
    }

    //Created By: Deepak Sharma
    //Created Date: 11/06/2018
    //Purpose: Show remaing hours
    private void ShowRemainingHours() {

        // show work shift remaining time in text view
        tvTotalWorkShiftHours.setText(Utility.getTimeFromMinute(GPSData.WorkShiftRemaining));

        // show workshift remaingin time in progress bar
        pbTotalWorkShiftHour.setProgress(pbTotalWorkShiftHour.getMax() - GPSData.WorkShiftRemaining);

        // if current rule is canada
        if (currentRule == 1 || currentRule == 2 || currentRule == 7) {

            if (currentRule == 7) {
                pbTotalCanadaRule.setProgress(0);
                tvCanadaRuleValue.setText("N/A");
            } else {
                pbTotalCanadaRule.setProgress(pbTotalCanadaRule.getMax() - (currentRule == 2 ? GPSData.TimeRemaining120 : GPSData.TimeRemaining70));
                tvCanadaRuleValue.setText(Utility.getTimeFromMinute((currentRule == 2 ? GPSData.TimeRemaining120 : GPSData.TimeRemaining70)));
            }
            // get previous rule of driver other than current country rule
            String except = "'1','2','5','6','7'";
            int previousRule = DailyLogDB.getPreviousRule(Utility.user1.getAccountId(), except);

            pbTotalUSRule.setProgress(pbTotalUSRule.getMax() - (previousRule == 4 ? GPSData.TimeRemainingUS60 : GPSData.TimeRemainingUS70));
            tvUSRuleValue.setText(Utility.getTimeFromMinute((previousRule == 4 ? GPSData.TimeRemainingUS60 : GPSData.TimeRemainingUS70)));
        } else if (currentRule == 5) { // rule of alberta

            pbTotalCanadaRule.setProgress(0);
            tvCanadaRuleValue.setText("N/A");

            // get previous rule of driver other than current country rule
            String except = "'1','2','5','6','7'";
            int previousRule = DailyLogDB.getPreviousRule(Utility.user1.getAccountId(), except);

            pbTotalUSRule.setProgress(pbTotalUSRule.getMax() - (previousRule == 4 ? GPSData.TimeRemainingUS60 : GPSData.TimeRemainingUS70));
            tvUSRuleValue.setText(Utility.getTimeFromMinute((previousRule == 4 ? GPSData.TimeRemainingUS60 : GPSData.TimeRemainingUS70)));
        } else if (currentRule == 6) { // rule of alberta


            pbTotalCanadaRule.setProgress(pbTotalCanadaRule.getMax() - GPSData.TimeRemaining80);


            tvCanadaRuleValue.setText(Utility.getTimeFromMinute(GPSData.TimeRemaining80));

            // get previous rule of driver other than current country rule
            String except = "'1','2','5','6','7'";
            int previousRule = DailyLogDB.getPreviousRule(Utility.user1.getAccountId(), except);

            pbTotalUSRule.setProgress(pbTotalUSRule.getMax() - (previousRule == 4 ? GPSData.TimeRemainingUS60 : GPSData.TimeRemainingUS70));
            tvUSRuleValue.setText(Utility.getTimeFromMinute((previousRule == 4 ? GPSData.TimeRemainingUS60 : GPSData.TimeRemainingUS70)));
        } else if (currentRule == 3 || currentRule == 4) { // if current rule is US

            pbTotalUSRule.setProgress(pbTotalUSRule.getMax() - (currentRule == 4 ? GPSData.TimeRemainingUS60 : GPSData.TimeRemainingUS70));
            tvUSRuleValue.setText(Utility.getTimeFromMinute((currentRule == 4 ? GPSData.TimeRemainingUS60 : GPSData.TimeRemainingUS70)));

            // get previous rule of driver other than current country rule
            String except = "'3','4'";
            int previousRule = DailyLogDB.getPreviousRule(Utility.user1.getAccountId(), except);

            switch (previousRule) {
                case 1:
                case 2:
                    pbTotalCanadaRule.setMax((previousRule == 2 ? 120 * 60 : 70 * 60));

                    pbTotalCanadaRule.setProgress(pbTotalCanadaRule.getMax() - (previousRule == 2 ? GPSData.TimeRemaining120 : GPSData.TimeRemaining70));
                    tvCanadaRuleValue.setText(Utility.getTimeFromMinute((previousRule == 2 ? GPSData.TimeRemaining120 : GPSData.TimeRemaining70)));
                    break;
                case 5:
                case 7:
                    pbTotalCanadaRule.setMax(0 * 60);
                    pbTotalCanadaRule.setProgress(0);
                    tvCanadaRuleValue.setText("N/A");
                    break;
                case 6:
                    pbTotalCanadaRule.setMax(80 * 60);
                    pbTotalCanadaRule.setProgress(pbTotalCanadaRule.getMax() - GPSData.TimeRemaining80);
                    break;
            }
        }

        // check if violation time has been passed
        boolean violationFg = false;
        if (MainActivity._violationFg) {

            // check how much time is left to violation
            int hourLeft = (int) Math.round((MainActivity.ViolationDT.getTime() - Utility.newDate().getTime()) / (1000 * 60d));
            if (hourLeft < 0) {
                hourLeft = 0;
            }
            violationFg = hourLeft == 0;

            pbTimeCountProgress.setProgress(pbTimeCountProgress.getMax() - hourLeft);
            tvViolation.setText((hourLeft == 0 && currentStatus == 3 ? ("(" + getString(R.string.stop_driving) + ") ") : "") + MainActivity.ViolationTitle);
            tvViolationDate.setText(Utility.ConverDateFormat(MainActivity.ViolationDT));
            tvRemaingTime.setText(Utility.getTimeFromMinute(hourLeft));
            tvTotalDrivingHours.setText(Utility.getTimeFromMinute(hourLeft));
            pbTotalDrivingHours.setProgress(pbTotalDrivingHours.getMax() - hourLeft);
            GPSData.DrivingTimeRemaining = hourLeft;
            Utility.setViolationColor(hourLeft, tvViolation, getActivity().getApplicationContext());

        } else {

            tvViolation.setText("N/A");
            tvViolationDate.setText("");
            tvRemaingTime.setText("N/A");
            tvTotalDrivingHours.setText("N/A");
        }


        // when duty status is driving,on duty or yard move show hours remaining else show hours elapsed
        if (currentStatus == 3) {
            butRemainingTime.setText(getString(R.string.remaining));
        } else {
            butRemainingTime.setText(getString(R.string.elapsed));

            // show N/A if duty status is PU,Off, SB
           /* tvViolation.setText("N/A");
            tvViolationDate.setText("");
            tvTotalDrivingHours.setText("N/A");

            pbTotalDrivingHours.setProgress(0);*/
            pbTimeCountProgress.setProgress(0);

            int size = MainActivity.dutyStatusArrayList.size();
            if (size > 0) {
                // get last status time
                Date lastStatusDT = MainActivity.dutyStatusArrayList.get(size - 1).getEventDateTime();
                try {
                    // check if previous duty status are same as current duty status
                    for (int j = MainActivity.dutyStatusArrayList.size() - 2; j >= 0; j--) {
                        DutyStatus item = MainActivity.dutyStatusArrayList.get(j);

                        if (item.getStatus() != currentStatus) {
                            lastStatusDT = MainActivity.dutyStatusArrayList.get(j + 1).getEventDateTime();
                            break;
                        }
                    }
                } catch (Exception exe) {

                }

                // show hours elapsed
                int hourLeft = (int) Math.round((Utility.newDate().getTime() - lastStatusDT.getTime()) / (1000 * 60d));
                tvRemaingTime.setText(Utility.getTimeFromMinute(hourLeft));
            } else {
                tvRemaingTime.setText("N/A");
            }
        }

        setTotalRestTime();

        // violation indicator on flag bar
        if (mListener != null) {

            boolean violationAlertFg = false;
            // check if violation exists in list voilation icon shows when voilation exists for canada we show last 14 days violation and last 7 day violation for us
            for (ViolationModel vBean : MainActivity.violationList) {
                if (vBean.getViolationDate().before(Utility.newDate()) && !vBean.isVirtualFg() && vBean.getViolationDate().after(Utility.parse(Utility.getPreviousDate(ELogFragment.canadaFg ? -14 : -7)))) {
                    violationAlertFg = true;
                    break;
                }
            }

            mListener.onUpdateViolation(violationAlertFg);
        }
    }


    // set onduty or rest time as per current status
    private void setTotalRestTime() {

        int size = MainActivity.dutyStatusArrayList.size();

        if (size > 0) {

            // get last status time
            Date lastStatusDT = MainActivity.dutyStatusArrayList.get(size - 1).getEventDateTime();
            if (currentStatus <= 2 || currentStatus == 5) {
                tvRestTime.setText(R.string.rest_time);

                for (int j = MainActivity.dutyStatusArrayList.size() - 2; j >= 0; j--) {
                    DutyStatus item = MainActivity.dutyStatusArrayList.get(j);


                    if (item.getStatus() == 3 || item.getStatus() == 4 || item.getStatus() == 6) {
                        lastStatusDT = MainActivity.dutyStatusArrayList.get(j + 1).getEventDateTime();
                        break;
                    }
                }

                // get time remaining to qualify split sleep in US for 8 hours sleeper
                if ((currentRule == 3 || currentRule == 4) && currentStatus == 2) {
                    Date lastSleepersDT = MainActivity.dutyStatusArrayList.get(size - 1).getEventDateTime();
                    for (int j = MainActivity.dutyStatusArrayList.size() - 2; j >= 0; j--) {
                        DutyStatus item = MainActivity.dutyStatusArrayList.get(j);


                        if (item.getStatus() != 2) {
                            lastSleepersDT = MainActivity.dutyStatusArrayList.get(j + 1).getEventDateTime();
                            break;
                        }
                    }

                    // show hours elapsed
                    int sleeperMinute = (int) Math.round((Utility.newDate().getTime() - lastSleepersDT.getTime()) / (1000 * 60d));
                    int timeLeft = 8 * 60 - sleeperMinute;

                    if (timeLeft < 0) {
                        timeLeft = 0;
                    }
                    tvSBSplit.setVisibility(View.VISIBLE);
                    tvSBSplit.setText("SB: " + Utility.getTimeFromMinute(timeLeft));
                }

            } else {
                tvRestTime.setText(R.string.onduty_time);

                for (int j = MainActivity.dutyStatusArrayList.size() - 2; j >= 0; j--) {
                    DutyStatus item = MainActivity.dutyStatusArrayList.get(j);

                    if (item.getStatus() == 1 || item.getStatus() == 2 || item.getStatus() == 5) {
                        lastStatusDT = MainActivity.dutyStatusArrayList.get(j + 1).getEventDateTime();
                        break;
                    }
                }
            }


            // show hours elapsed
            int hourLeft = (int) Math.round((Utility.newDate().getTime() - lastStatusDT.getTime()) / (1000 * 60d));
            tvRestTimeValue.setText(Utility.getTimeFromMinute(hourLeft));
        }
    }


    public void updateOdometer() {
        try {
            if (tvVehicleMiles != null && tvAccumulatedMilesValue != null) {
                double odometerReading = Double.valueOf(CanMessages.OdometerReading); // odometer from can bus is in km
                double odometerReadingSincePowerOn = Double.valueOf(Utility.OdometerReadingSincePowerOn);
                String unit = "Kms";
                if (Utility._appSetting.getUnit() == 2) {
                    odometerReading = odometerReading * .62137d;
                    odometerReadingSincePowerOn = odometerReadingSincePowerOn * .62137d;
                    unit = "Miles";
                }

                tvVehicleMiles.setText(Double.valueOf(odometerReading).intValue() + "/" + String.format("%.1f", Double.valueOf(CanMessages.EngineHours)));

                try {
                    if (!CanMessages.RPM.equals("0")) {
                        String accumulatedVehicleMiles = (Double.valueOf(odometerReading).intValue() - Double.valueOf(odometerReadingSincePowerOn).intValue()) + "";
                        String elapsedEngineHours = String.format("%.1f", Double.valueOf(CanMessages.EngineHours) - Double.valueOf(Utility.EngineHourSincePowerOn));
                        tvAccumulatedMilesValue.setText(accumulatedVehicleMiles + " " + unit);
                    } else {
                        tvAccumulatedMilesValue.setText("--");

                    }

                    tvSpecialStatus.setText(ReportDB.SpecialStatusFg ? "S" : "");
                } catch (Exception ex) {

                }
            }
        } catch (Exception e) {
            LogFile.write(ELogFragment.class.getName() + "::updateOdometer Error:" + e.getMessage(), LogFile.USER_INTERACTION, LogFile.ERROR_LOG);
            LogDB.writeLogs(com.hutchsystems.hutchconnect.fragments.ELogFragment.class.getName(), "updateOdometer", e.getMessage(), Utility.printStackTrace(e));

        }
    }

    private void saveDutyStatusFlag() {
        try {
            if (getActivity() != null) {
                SharedPreferences.Editor e = (getActivity().getSharedPreferences("HutchGroup", getActivity().getBaseContext().MODE_PRIVATE))
                        .edit();
                e.putInt("duty_status_flag", Utility.statusFlag);
                e.commit();
            }
        } catch (Exception e) {
            LogFile.write(ELogFragment.class.getName() + "::saveDutyStatusFlag Error:" + e.getMessage(), LogFile.USER_INTERACTION, LogFile.ERROR_LOG);
            LogDB.writeLogs(ELogFragment.class.getName(), "saveDutyStatusFlag", e.getMessage(), Utility.printStackTrace(e));
        }
    }

    private void onCurrentStatusUpdate() {
        int index = dutyStatusList.size() - 1; // last index
        currentStatus = dutyStatusList.get(index).getStatus();
        statusDT = Utility.parse(dutyStatusList.get(index).getStartTime());

        int eventType = dutyStatusList.get(index).getEventType();
        int eventCode = dutyStatusList.get(index).getEventCode();

        if (currentStatus == 1 && eventType == 3 && eventCode == 1) {
            currentStatus = 5;

        } else if (currentStatus == 4 && eventType == 3 && eventCode == 2) {

            currentStatus = 6;
        }

        mListener.setDutyStatus(currentStatus);
        butDutyStatus.setText(getResources().getStringArray(R.array.duty_status)[currentStatus - 1]);
    }

    public void refresh() {
        try {
            if (canvas != null) {

                //Utility.dutyStatusList = HourOfServiceDB.DutyStatusGet15Days(currentDate, Utility.onScreenUserId + "", false);

                dailyLogId = DailyLogDB.getDailyLog(Utility.onScreenUserId, Utility.getCurrentDate());
                currentDate = Utility.dateOnlyGet(Utility.newDate());

                // get current day's duty status
                dutyStatusList = EventDB.DutyStatusGetToday(Utility.onScreenUserId);

                onCurrentStatusUpdate();

                // set last off duty status
                setLastOffDate();

                // draw line on canvas
                Thread thBitMap = new Thread(new Runnable() {
                    @Override
                    public void run() {
                        while (true) {
                            if (finalHeight != 0 && finalWidth != 0 && getActivity() != null) {
                                getActivity().runOnUiThread(new Runnable() {
                                    @Override
                                    public void run() {
                                        initializeBitmap(false);
                                        new ViolationSync(true, true).execute();
                                    }
                                });

                                break;
                            }
                        }
                    }
                });

                thBitMap.setName("Elog-Bitmap3");
                thBitMap.start();


            } else {
                Log.d(TAG, "Canvas is null");
            }
            if (lvEvents != null && getActivity() != null) {
                getActivity().runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        eventAdapter.changeItems(getListEvents());

                        updateTodayDistance();
                    }
                });
            } else {
                Log.d(TAG, "lvEvents is null");
            }
        } catch (Exception e) {
            LogFile.write(ELogFragment.class.getName() + "::refresh Error:" + e.getMessage(), LogFile.USER_INTERACTION, LogFile.ERROR_LOG);
            LogDB.writeLogs(ELogFragment.class.getName(), "refresh", e.getMessage(), Utility.printStackTrace(e));

        }
    }

    public void setupUI(View view) {
        //Set up touch listener for non-text box views to hide keyboard.
        if (!(view instanceof ListView)) {
            view.setOnTouchListener(new View.OnTouchListener() {
                public boolean onTouch(View v, MotionEvent event) {
                    unselectEvent();
                    return false;
                }
            });
        }

        //If a layout container, iterate over children and seed recursion.
        if (view instanceof ViewGroup) {
            for (int i = 0; i < ((ViewGroup) view).getChildCount(); i++) {
                View innerView = ((ViewGroup) view).getChildAt(i);
                setupUI(innerView);
            }
        }
    }

    public void unselectEvent() {
        Log.d(TAG, "unselectEvent");
        eventAdapter.unselectEvent();
    }


    public void autoChagneRule(int rule) {
        currentRule = rule;
        chkRules.setChecked(currentRule == 1 || currentRule == 2 || currentRule == 7);
        if (currentRule == 1 || currentRule == 2) {
            pbTimeCountProgress.setMax(13 * 60);
            pbTotalDrivingHours.setMax(13 * 60);
            pbTotalWorkShiftHour.setMax(16 * 60);
            tvCanadaRule.setText(getRule(currentRule - 1));
        } else if (currentRule == 5 || currentRule == 6) {
            pbTimeCountProgress.setMax(13 * 60);
            pbTotalDrivingHours.setMax(13 * 60);
            pbTotalWorkShiftHour.setMax(15 * 60);
            tvCanadaRule.setText(getRule(currentRule - 1));

            Utility._appSetting.setUnit(1);
            SettingsDB.CreateSettings();
        } else {
            pbTimeCountProgress.setMax(11 * 60);
            pbTotalDrivingHours.setMax(11 * 60);
            pbTotalWorkShiftHour.setMax(14 * 60);
            tvUSRule.setText(getRule(currentRule - 1));
        }
    }

    @Override
    public void onSavedRule(int rule) {
        try {
            int previousRule = DailyLogDB.getPreviousRule(Utility.onScreenUserId, "");

            if (rule != currentRule || previousRule == 0) {
                DailyLogDB.DailyLogRuleSave(Utility.onScreenUserId, rule, Utility._CurrentDateTime, Utility._CurrentDateTime);
                DailyLogDB.DailyLogSyncRevert(Utility.onScreenUserId, dailyLogId);

                // Post rule
                MainActivity.postData(CommonTask.Post_DailyLog_DriverInfo);
                currentRule = rule;
                chkRules.setChecked(currentRule == 1 || currentRule == 2 || currentRule > 4);

                if (mListener != null) {
                    mListener.changeRule(currentRule);
                }

                //tvCurrentRuleLabel.setText(getRule(currentRule - 1));
                pbTotalCanadaRule.setMax(currentRule == 2 ? 120 * 60 : 70 * 60);
                pbTotalUSRule.setMax(currentRule == 4 ? 60 * 60 : 70 * 60);
                int unit = 1;
                // set max value of driving hours according to US/Canada rule
                if (currentRule == 1 || currentRule == 2 || currentRule == 7) {
                    pbTimeCountProgress.setMax(13 * 60);
                    pbTotalDrivingHours.setMax(13 * 60);
                    pbTotalWorkShiftHour.setMax(16 * 60);
                    tvCanadaRule.setText(getRule(currentRule - 1));
                    unit = 1;
                  /*  Utility._appSetting.setUnit(1);
                    SettingsDB.CreateSettings();*/
                } else if (currentRule == 5 || currentRule == 6) {
                    pbTimeCountProgress.setMax(13 * 60);
                    pbTotalDrivingHours.setMax(13 * 60);
                    pbTotalWorkShiftHour.setMax(15 * 60);
                    tvCanadaRule.setText(getRule(currentRule - 1));
                    unit = 1;
                    /*Utility._appSetting.setUnit(1);
                    SettingsDB.CreateSettings();*/
                } else if (currentRule == 3 || currentRule == 4) {
                    pbTimeCountProgress.setMax(11 * 60);
                    pbTotalDrivingHours.setMax(11 * 60);
                    pbTotalWorkShiftHour.setMax(14 * 60);
                    tvUSRule.setText(getRule(currentRule - 1));
                    unit = 2;
                /*    Utility._appSetting.setUnit(2);
                    SettingsDB.CreateSettings();*/
                }


                // change unit on rule change
                if (Utility._appSetting.getChangeUnitOnRuleChange() == 1) {
                    Utility._appSetting.setUnit(unit);
                    SettingsDB.CreateSettings();
                    updateTodayDistance();
                    updateOdometer();
                }

                DutyStatusGet();


                // execute violation
                // Purpose : changed dutyStatusRefresh = true for Refreshing the driving hours
                new ViolationSync(true, true).execute();


                Utility._appSetting.setDefaultRule(rule);
                SettingsDB.CreateSettings();
            }
        } catch (Exception e) {
            LogFile.write(ELogFragment.class.getName() + "::onSavedRule Error:" + e.getMessage(), LogFile.USER_INTERACTION, LogFile.ERROR_LOG);
            LogDB.writeLogs(ELogFragment.class.getName(), "onSavedRule", e.getMessage(), Utility.printStackTrace(e));

        }
    }

    private void DutyStatusAdd(int status, Date statusDate) {
        String statusDateString = Utility.format(statusDate, fullFormat);

        DutyStatusBean bean = new DutyStatusBean();
        bean.setStartTime(statusDateString);
        int specialStatus = 0;
        if (status == 5) {
            status = 1;
            bean.setPersonalUse(1);
            specialStatus = 1;
        } else if (status == 6) {
            status = 4;
            specialStatus = 2;
        }
        bean.setStatus(status);

        if (dutyStatusList.size() > 0) {
            int index = dutyStatusList.size() - 1;
            DutyStatusBean lastDS = dutyStatusList.get(index);
            lastDS.setEndTime(statusDateString);

            Date startTime = Utility.parse(lastDS.getStartTime());
            int totalMinute = (int) Math.round((statusDate.getTime() - startTime.getTime()) / (1000 * 60.0));  // total duration of previous status
            lastDS.setTotalMinutes(totalMinute); // set total minute of previous status
        }

        if (Utility.dutyStatusList.size() > 0) {
            int index = 0;
            DutyStatusBean lastDS = Utility.dutyStatusList.get(index);
            lastDS.setEndTime(statusDateString);

            Date startTime = Utility.parse(lastDS.getStartTime());
            int totalMinute = (int) Math.round((statusDate.getTime() - startTime.getTime()) / (1000 * 60.0));  // total duration of previous status
            lastDS.setTotalMinutes(totalMinute); // set total minute of previous status
        }

        String endTime = Utility.addDate(Utility.getCurrentDate(), 1) + " 00:00:00";
        int totalMinute = (int) Math.round((Utility.parse(endTime).getTime() - statusDate.getTime()) / (1000 * 60.0));
        bean.setEndTime(endTime);
        bean.setTotalMinutes(totalMinute);
        bean.setSpecialStatus(specialStatus);
        dutyStatusList.add(bean);

        bean = new DutyStatusBean();
        bean.setStartTime(statusDateString);
        bean.setStatus(status);
        bean.setSpecialStatus(specialStatus);
        endTime = Utility.addDate(Utility.getCurrentDate(), 2) + " 00:00:00";
        totalMinute = (int) Math.round((Utility.parse(endTime).getTime() - statusDate.getTime()) / (1000 * 60.0));
        bean.setEndTime(endTime);
        bean.setTotalMinutes(totalMinute);
        Utility.dutyStatusList.add(0, bean);
    }

    @Override
    public void onSavedDutyStatus(int status, boolean saveForActiveDriver, String annotation, String location) {
        try {

            currentStatus = status;
            bDialogShowing = false;
            if (currentStatus == 5) {
                Utility.statusFlag = 1;
                Utility.powerOnOff = 2;
            } else if (currentStatus == 6) {
                Utility.statusFlag = 2;
            } else {
                Utility.statusFlag = 0;
            }
            mListener.setActiveDutyStatus(currentStatus);


            if (saveForActiveDriver && Utility.onScreenUserId != Utility.activeUserId) {
                EventDB.calculateDistance(Utility.activeUserId, Utility.getCurrentDate());
                return;
            }

            statusStartOdometerReading = Double.valueOf(CanMessages.OdometerReading).intValue();
            statusDT = Utility.newDate();
            DutyStatusAdd(currentStatus, statusDT);
            // Utility.dutyStatusList = HourOfServiceDB.DutyStatusGet15Days(currentDate, Utility.onScreenUserId + "", false);

            butDutyStatus.setText(getResources().getStringArray(R.array.duty_status)[currentStatus - 1]);
            DutyStatusGet();
            if (currentStatus < 3 || currentStatus == 5) {
                if (Utility.lastOffStatusDate == null) {
                    /*// set last offduty status date
                        setLastOffDate();*/
                    Utility.lastOffStatusDate = statusDT;
                }
            } else {
                Utility.lastOffStatusDate = null;
            }


            if (currentStatus == 3) {

            } else {
                totalDistance = totalDistance + (Double.valueOf(CanMessages.OdometerReading).intValue() - statusStartOdometerReading);
            }
            saveDutyStatusFlag();
            new ViolationSync(true, true).execute();

            // If Previous day Certified Logs pending then only update flag
            // mListener.setCertify(0);
            //  setCerifyFlag(0);
            mListener.setDutyStatus(currentStatus);
            todayDistance = EventDB.calculateDistance(dailyLogId);

            if (lvEvents != null) {
                getActivity().runOnUiThread(new Runnable() {
                    @Override
                    public void run() {

                        updateTodayDistance(todayDistance);
                        eventAdapter.changeItems(getListEvents());

                    }
                });
            }
            // dutyDialog.dismiss();
            bDialogShowing = false;

            MainActivity.postData(CommonTask.Post_DailyLog_DriverInfo);

            // check dutystatus is onduty and check annotation is dvir or fueling according to annotation display screen
            if (annotation != null && currentStatus == 4) {
                MainActivity.iSFueling = annotation.toLowerCase().contains("fueling");
                MainActivity.isDVIR = annotation.toLowerCase().contains("dvir");
                if (mListener != null) {
                    mListener.displayAnnotation();
                }

            }


        } catch (Exception e) {
            Log.d(TAG, "onSavedDutyStatus error " + e.getMessage());
            LogFile.write(ELogFragment.class.getName() + "::onSavedDutyStatus Error:" + e.getMessage(), LogFile.USER_INTERACTION, LogFile.ERROR_LOG);
            LogDB.writeLogs(ELogFragment.class.getName(), "onSavedDutyStatus", e.getMessage(), Utility.printStackTrace(e));

        }
    }

    public void updateTodayDistance(int distance) {
        try {
            if (tvTotalDistanceValue != null) {
                try {
                    String unit = "Kms";
                    if (Utility._appSetting.getUnit() == 2) {
                        distance = Double.valueOf(distance * .62137d).intValue();
                        unit = "Miles";
                    }
                    tvTotalDistanceValue.setText(distance + " " + unit);
                } catch (Exception ex) {

                }
            }
        } catch (Exception e) {
            LogFile.write(ELogFragment.class.getName() + "::updateTodayDistance(distance) Error:" + e.getMessage(), LogFile.USER_INTERACTION, LogFile.ERROR_LOG);
        }
    }

    public void updateTodayDistance() {
        try {
            if (dailyLogId > 0) {
                todayDistance = EventDB.CalculateDistanceByLogId(dailyLogId);
            }

            if (tvTotalDistanceValue != null) {

                int distance = todayDistance;
                String unit = "Kms";
                if (Utility._appSetting.getUnit() == 2) {
                    distance = Double.valueOf(distance * .62137d).intValue();
                    unit = "Miles";
                }
                tvTotalDistanceValue.setText(distance + " " + unit);

            }
        } catch (Exception e) {
            LogFile.write(ELogFragment.class.getName() + "::updateTodayDistance Error:" + e.getMessage(), LogFile.USER_INTERACTION, LogFile.ERROR_LOG);
        }
    }

    int todayDistance;

    private void calculateDistance() {
        try {
            int distance = Double.valueOf(CanMessages.OdometerReading).intValue() - GPSData.OdometerSinceDriving;
            if (distance > 0) {
                GPSData.TotalDistance += distance;
                todayDistance = GPSData.TotalDistance;
                Utility.savePreferences("today_distance", GPSData.TotalDistance);
                int logId = DailyLogDB.getDailyLog(Utility.activeUserId, Utility.getCurrentDate());

                // if dailylog record does not exists for the day
                if (logId == 0) {
                    logId = DailyLogDB.DailyLogCreate(Utility.activeUserId, Utility.ShippingNumber, Utility.TrailerNumber, "");
                }

                DailyLogDB.DailyLogDistanceSave(logId, Utility.activeUserId, GPSData.TotalDistance);
            }
        } catch (Exception exe) {
        }
    }

    @Override
    public void onDissmisDialog() {
        // dutyDialog.dismiss();
        bDialogShowing = false;
    }

    public void SwitchDriver() {
        Log.d(TAG, "SwitchDriver");
        if (mListener != null) {
            mListener.changeUser();
        }
    }

    private void callActive() {
        final AlertDialog ad = new AlertDialog.Builder(getContext())
                .create();
        String message = getString(R.string.driving_mode_message);
        String title = getString(R.string.driving_mode_title);
        if (Utility.motionFg) {
            message = getString(R.string.active_driver_message_inmotion);
            title = getString(R.string.active_driver);
        }
        ad.setCancelable(true);
        ad.setCanceledOnTouchOutside(false);
        ad.setTitle(title);
        ad.setIcon(R.drawable.ic_launcher);
        ad.setMessage(message);
        if (Utility.motionFg) {
            ad.setButton(DialogInterface.BUTTON_NEGATIVE, getString(R.string.ok),
                    new DialogInterface.OnClickListener() {

                        @Override
                        public void onClick(DialogInterface dialog,
                                            int which) {
                            ad.cancel();
                        }
                    });
        } else {
            ad.setButton(DialogInterface.BUTTON_POSITIVE, getString(R.string.yes),
                    new DialogInterface.OnClickListener() {

                        @Override
                        public void onClick(DialogInterface dialog,
                                            int which) {


                            setActive();
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
        }
        ad.show();
    }

    // set driver active
    private void setActive() {
        if (Utility.user2.getAccountId() > 0 && !Utility.motionFg) {
            // switch driver status
            Utility.user1.setActive(!Utility.user1.isActive());
            Utility.user2.setActive(!Utility.user2.isActive());
            Utility.activeUserId = Utility.user1.isActive() ? Utility.user1.getAccountId() : Utility.user2.getAccountId();
            getContext().getSharedPreferences("HutchGroup", getContext().MODE_PRIVATE).edit().putInt("activeuserid", Utility.activeUserId).commit();

            if (mListener != null) {
                mListener.activeUser();
            }

            // if matchine is on check fuel economy
            if (Float.valueOf(CanMessages.RPM) > 0f) {
                AlertMonitor.FuelEconomyViolationGet();

                // switch driver case to get fuel economy per driver
                Utility.OdometerReadingStart = CanMessages.OdometerReading;
                Utility.savePreferences("OdometerReadingStart", Utility.OdometerReadingStart);


                // switch driver case to get fuel economy per driver
                Utility.FuelUsedStart = CanMessages.TotalFuelConsumed;
                Utility.savePreferences("FuelUsedStart", Utility.FuelUsedStart);
            }

            fabActive.setEnabled(Utility.onScreenUserId != Utility.activeUserId);
        }
    }

    public void callCertify() {
        Log.d(TAG, "Call Certify");
        try {
            final AlertDialog ad = new AlertDialog.Builder(Utility.context)
                    .create();
            ad.setCancelable(true);
            ad.setCanceledOnTouchOutside(false);
            ad.setTitle(getString(R.string.certify_log_title));
            ad.setIcon(R.drawable.ic_launcher);
            ad.setMessage(getString(R.string.certify_log_message));
            ad.setButton(DialogInterface.BUTTON_POSITIVE, getString(R.string.certify),
                    new DialogInterface.OnClickListener() {

                        @Override
                        public void onClick(DialogInterface dialog,
                                            int which) {
                            int driverId = Utility.user1.isOnScreenFg() ? Utility.user1.getAccountId() : Utility.user2.getAccountId();


                            int dailyLogId = DailyLogDB.getDailyLog(driverId, Utility.getCurrentDate());
                            DailyLogDB.certifyLogBook(driverId, dailyLogId + "");

                            setCerifyFlag(1);
                            if (mListener != null)
                                mListener.setCertify(1);
                        }
                    });
            ad.setButton(DialogInterface.BUTTON_NEGATIVE, getString(R.string.not_ready),
                    new DialogInterface.OnClickListener() {

                        @Override
                        public void onClick(DialogInterface dialog,
                                            int which) {
                            ad.cancel();
                        }
                    });
            ad.show();
        } catch (Exception e) {
            LogFile.write(DailyLogDB.class.getName() + "::callCertify Error:" + e.getMessage(), LogFile.DATABASE, LogFile.ERROR_LOG);
            LogDB.writeLogs(ELogFragment.class.getName(), "::callCertify Error:", e.getMessage(), Utility.printStackTrace(e));

        }
    }

    public void callChangeRule() {
        Log.d(TAG, "Call change rule");
        launchRuleChange();
    }

    public void callSync() {
        Log.d(TAG, "Call Sync");
        showLoaderAnimation(true);
        new SyncData(syncDataPostTaskListener).execute();
    }

    // check the ticket status is resolved
    public void checkTicketStatus() {
        // Rename Class name TicketPost to CheckTicketStatus

        new CheckTicketStatus(ticketPostTaskListner).execute();
    }

    public void callUndocking() {
        Log.d(TAG, "Call Undocking");
        fabMenu.setEnabled(false);
        Animation hide_fab_menu = AnimationUtils.loadAnimation(getActivity().getApplication(), R.anim.fade_out);

        if (layout_menu.getVisibility() == View.VISIBLE) {
            layout_menu.startAnimation(hide_fab_menu);
            layout_menu.setVisibility(View.INVISIBLE);
            restView.setVisibility(View.INVISIBLE);
        }
        if (mListener != null) {
            mListener.undocking();
        }
    }

    public void callPost() {
        final AlertDialog ad = new AlertDialog.Builder(getActivity())
                .create();
        ad.setCancelable(true);
        ad.setCanceledOnTouchOutside(false);
        ad.setTitle(getString(R.string.send_data_confirmation_title));
        ad.setIcon(R.drawable.ic_launcher);
        ad.setMessage(getString(R.string.send_data_message));
        ad.setButton(DialogInterface.BUTTON_POSITIVE, getString(R.string.yes),
                new DialogInterface.OnClickListener() {

                    @Override
                    public void onClick(DialogInterface dialog,
                                        int which) {
                        if (Utility.isInternetOn()) {
                            Log.d(TAG, "run post data");
                            showLoaderAnimation(true);
                            new PostData(postDataPostTaskListener).execute();
                            mListener.onUploadDocument();
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

    // Created By: Pallavi Wattamwar
    // Created Date: 29 November 2018
    // Purpose: Confirmation Dialog For DeferDay and logic of defer Day
    public void callDeferDay() {

        // Checking for Canadian Rule
        if (MainActivity.currentRule < 3) {
            final AlertDialog ad = new AlertDialog.Builder(getActivity())
                    .create();
            ad.setCancelable(true);
            ad.setCanceledOnTouchOutside(false);
            ad.setTitle("E-Log");
            ad.setIcon(R.drawable.ic_launcher);
            ad.setMessage(getResources().getString(R.string.deferred_day_title));
            ad.setButton(DialogInterface.BUTTON_POSITIVE, getString(R.string.yes),
                    new DialogInterface.OnClickListener() {

                        @Override
                        public void onClick(DialogInterface dialog,
                                            int which) {

                            DailyLogDB.LogUpdateForDeferedDay(Utility.getCurrentDate(), Utility.onScreenUserId, 1);

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
        } else {
            Utility.showAlertMsg(getResources().getString(R.string.deferred_day_alert_message));
        }


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
                                    Log.d(TAG, "End animation");
                                    showLoaderAnimation(false);
                                }
                            });
                        }
                    }
                }, 30000);
                rlLoadingPanel.setVisibility(View.VISIBLE);
                getActivity().getWindow().setFlags(WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE,
                        WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE);
            } else {
                rlLoadingPanel.setVisibility(View.GONE);
                getActivity().getWindow().clearFlags(WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE);
            }
        } catch (Exception e) {
            LogFile.write(ELogFragment.class.getName() + "::showLoaderAnimation Error:" + e.getMessage(), LogFile.USER_INTERACTION, LogFile.ERROR_LOG);
            LogDB.writeLogs(ELogFragment.class.getName(), "showLoaderAnimation", e.getMessage(), Utility.printStackTrace(e));

        }
    }

    public void setCerifyFlag(int value) {
        certifyFg = value;
        getActivity().runOnUiThread(new Runnable() {
            @Override
            public void run() {
                if (certifyFg == 1) {
                    fabCertify.setVisibility(View.VISIBLE);
                    fabUncertify.setVisibility(View.INVISIBLE);
                } else if (certifyFg == 0) {
                    fabCertify.setVisibility(View.INVISIBLE);
                    fabUncertify.setVisibility(View.VISIBLE);
                }
            }
        });
    }

    public void setFirstLogin(boolean value) {
        firstLogin = value;
    }

    // Created By: Pallavi Wattamwar
    // Created Date: 28 November 2018
    // Purpose: Create Logs For Missing Dates
    // Parameter : Array List of missingDateList
    public void updateMissingLogData(ArrayList<String> missingDateList) {


        for (int i = 0; i < missingDateList.size(); i++) {
            // Create Missing Log Dates
            int logId = DailyLogDB.DailyLogCreateByDate(Utility.onScreenUserId, missingDateList.get(i), "", "", "");
            //  EventDB.EventCreate(missingDateList.get(i) + " 00:00:00", 1, 1, getString(R.string.duty_status_changed_to_off_duty), 1, 1, logId, Utility.onScreenUserId, "", MainActivity.currentRule);
            // Certify the logs
            DailyLogDB.DailyLogCertify("", Utility.onScreenUserId, logId + "");

        }

    }

    // Created By: Pallavi Wattamwar
    // Created Date: 28 November 2018
    // Purpose: Check Missing Logs Data
    // Return Type: Array List Return missing log Dates
    public ArrayList<String> checkMissingLogDate() {
        ArrayList<String> missingLogDates = new ArrayList<>();

        // For Canadian rule Display 15 days log and for us rule display 8 days
        String previousDate = Utility.getPreviousDate(canadaFg ? -15 : -8);

        int count = 0;
        // Check the difference between days
        while (count < Utility.getDiffDay(previousDate, Utility.getCurrentDate())) {
            String logDate = Utility.getDateFromString(previousDate, count);
            int logId = DailyLogDB.getDailyLog(Utility.onScreenUserId, logDate);
            if (logId == 0) {//missing day

                missingLogDates.add(logDate);
            }
            count++;
        }

        return missingLogDates;
    }

    LogEventSync.PostTaskListener<Boolean> refreshElogFragment = new LogEventSync.PostTaskListener<Boolean>() {
        @Override
        public void onPostTask(Boolean result) {
            if (result) {
                try {
                    // get the missing logdates
                    ArrayList<String> missingDateList = checkMissingLogDate();
                    if (missingDateList.size() > 0) {
                        showEventDialog(missingDateList);
                    }
                    ELogFragment.this.refresh();
                } catch (Exception exe) {
                }
            } else {
            }

        }
    };

    // Created By: Deepak Sharma
    // Created Date: 8 April 2017
    // Purpose: calculate violation
    private class ViolationSync extends AsyncTask<Void, Void, Boolean> {
        boolean dutyStatusRefresh;
        boolean reCertifyFg;
        boolean violationOnGrid = true;

        public ViolationSync(boolean data, boolean reCertifyFg) {
            this.dutyStatusRefresh = data;
            this.reCertifyFg = reCertifyFg;
        }


        public ViolationSync(boolean data, boolean reCertifyFg, boolean violationOnGrid) {
            this.dutyStatusRefresh = data;
            this.reCertifyFg = reCertifyFg;
            this.violationOnGrid = violationOnGrid;
        }


        @Override
        protected Boolean doInBackground(Void... voids) {


            MainActivity.ProcessViolation(dutyStatusRefresh);

            return true;
        }

        @Override
        protected void onPostExecute(Boolean aBoolean) {
            super.onPostExecute(aBoolean);

            // show remaining hours
            if (getActivity() != null)
                ShowRemainingHours();

            if (reCertifyFg)
                DailyLogDB.DailyLogHoursReCertify(Utility.onScreenUserId, dailyLogId);

            if (Utility._appSetting.getViolationOnGrid() == 1) {
                if (getContext() != null && violationOnGrid)
                    drawViolationArea();

            }

        }
    }

    public static boolean canadaFg;

    private void CheckCountry() {
        String[] addresses = Utility.FullAddress.split(",");
        if (addresses.length >= 4) {
            if (addresses[3].trim().toUpperCase().equals("US")) {
                canadaFg = false;
            } else {
                canadaFg = true;
            }
        }
    }

    @Override
    public void onTotalDistanceSave(String total) {
        try {
            tvTotalDistanceValue.setText(total);
            int distance = Integer.parseInt(total);
            todayDistance = distance;
            if (Utility._appSetting.getUnit() == 2) {
                distance = Math.round(distance * 1.60934f);
            }
            DailyLogDB.DailyLogDistanceSave(dailyLogId, Utility.onScreenUserId, distance, 0);

            GPSData.TotalDistance = distance;

        } catch (Exception exe) {

        }
    }


    @Override
    public void onCallPost() {

    }

    @Override
    public void onCallSync() {

    }

    @Override
    public void onRefresh() {
        getActivity().runOnUiThread(new Runnable() {
            @Override
            public void run() {
                eventAdapter.changeItems(getListEvents());
            }
        });
    }
}
