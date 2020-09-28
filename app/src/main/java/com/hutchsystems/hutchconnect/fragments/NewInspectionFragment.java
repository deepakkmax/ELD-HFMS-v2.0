package com.hutchsystems.hutchconnect.fragments;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.DatePickerDialog;
import android.app.TimePickerDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.res.Configuration;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.location.Address;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import android.os.StrictMode;
import android.provider.MediaStore;
import android.text.format.DateFormat;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.HorizontalScrollView;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TableLayout;
import android.widget.TextView;
import android.widget.TimePicker;

import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;

import com.hutchsystems.hutchconnect.R;
import com.hutchsystems.hutchconnect.beans.AppSettings;
import com.hutchsystems.hutchconnect.beans.TripInspectionBean;
import com.hutchsystems.hutchconnect.beans.VehicleBean;
import com.hutchsystems.hutchconnect.common.BitmapUtility;
import com.hutchsystems.hutchconnect.common.CanMessages;
import com.hutchsystems.hutchconnect.common.CustomDateFormat;
import com.hutchsystems.hutchconnect.common.LogFile;
import com.hutchsystems.hutchconnect.common.Utility;
import com.hutchsystems.hutchconnect.db.LogDB;
import com.hutchsystems.hutchconnect.db.TripInspectionDB;
import com.hutchsystems.hutchconnect.db.VehicleDB;
import com.hutchsystems.hutchconnect.services.AutoStartService;
import com.hutchsystems.hutchconnect.tasks.GeocodeTask;
import com.hutchsystems.hutchconnect.tasks.NearestDistanceTask;

import java.io.File;
import java.io.IOException;
import java.text.Format;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

public class NewInspectionFragment extends Fragment implements View.OnClickListener, CompoundButton.OnCheckedChangeListener, DefectSelectionDialog.DefectSelectionDialogInterface, SignatureDialogFragment.OnFragmentInteractionListener {
    String TAG = NewInspectionFragment.class.getName();
    private OnFragmentInteractionListener mListener;

    final int DEFECT_SELECTION_CODE = 1;
    final int TAKE_PHOTO_CODE = 0;
    final int VIEW_PHOTO = 2;

    TextView tvDateTime;
    EditText tvLocation;
    EditText tvOdometer;

    EditText edTruckNum;
    EditText edTrailerNum;
    EditText edComments;

    CheckBox switchDefect;
    CheckBox switchDefectRepaired;
    CheckBox switchSafeToDrive;


    //Button butCertify;
    //Button butClose;
    ImageButton fabCertify;
    HorizontalScrollView horizontalScrollView;
    TableLayout tableDefects;

    TextView tvDefectItemsLabel;
    TextView tvDefectItems;
    Button butAddDefect;
    Button butAddPiture, btnAddSignature;
    ImageView imgSignature;

    LinearLayout layoutImages;

    double latitude;
    double longitude;

    int defect;
    int defectRepaired;
    int safeToDrive;
    String defectItems;

    String certifyMessage;
    boolean viewMode, TrailerDVIRFg;

    String location = "";

    int count = 0;
    String imageFile;
    List<String> listImages;
    Bitmap scaleBitmap = null;
    RadioGroup rbgType;

    //Toolbar toolbar;
    DefectSelectionDialog defectSelectionDialog;
    ViewImageDialog viewImageDialog;
    int countCurrentLocationFailed;
    String currentDatetime;
    // Spinner spinnerTrailerValue;

    int second = 0;

    GeocodeTask.PostTaskListener<Address> geocodeTaskListener = new GeocodeTask.PostTaskListener<Address>() {
        @Override
        public void onPostTask(Address address) {
            if (address == null) {
                countCurrentLocationFailed++;
                if (countCurrentLocationFailed < 5) {
                    callGeocodeTask();
                }
                tvLocation.setText("");
            } else {
                String addressName = "";

                String fulladdress = address.getAddressLine(0);
                String city = address.getLocality();
                String state = address.getAdminArea();
                String country = address.getCountryName();
                String postalCode = address.getPostalCode();
                //addressName = add + ", " + city + ", " + state + ", " + country + " " + postalCode;
                addressName = fulladdress;//city + ", " + state;
                tvLocation.setText(addressName);
            }
        }
    };
    private TextView tvDate, tvTime;


    public NewInspectionFragment() {

    }

    int inspectionType = 0;

    private void initialize(View view) {
        try {
            rbgType = (RadioGroup) view.findViewById(R.id.rbgType);
            rbgType.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
                @Override
                public void onCheckedChanged(RadioGroup group, int checkedId) {
                    switch (checkedId) {
                        case R.id.rbPre:
                            inspectionType = 0;
                            break;

                        case R.id.rbInter:
                            inspectionType = 1;
                            break;

                        case R.id.rbPost:
                            inspectionType = 2;
                            break;
                    }
                }
            });
            /*   tvDateTime = (TextView) view.findViewById(R.id.tvDateTime);*/
            tvLocation = view.findViewById(R.id.tvLocation);
            tvLocation.setText("");
            tvOdometer = (EditText) view.findViewById(R.id.tvOdometer);

            edTruckNum = (EditText) view.findViewById(R.id.edTruckValue);
            edTrailerNum = (EditText) view.findViewById(R.id.edTrailerValue);
            // Spinner Trailer Value
            //spinnerTrailerValue =(Spinner) view.findViewById(R.id.spinnerTrailerValue);
            ArrayList<VehicleBean> hookedTrailers = VehicleDB.TrailerGet();

            ArrayList<String> trailerList = new ArrayList<>();
            trailerList.add("");

            for (VehicleBean bean : hookedTrailers) {
                trailerList.add(bean.getUnitNo());
            }

            ArrayAdapter trailerArray = new ArrayAdapter(Utility.context, R.layout.spinner_rule_item, trailerList);
            trailerArray.setDropDownViewResource(R.layout.spinner_rule_item);
            //Setting the ArrayAdapter data on the Spinner
            //spinnerTrailerValue.setAdapter(trailerArray);
//            if(TrailerDVIRFg)
//            {
//                spinnerTrailerValue.setVisibility(View.VISIBLE);
//                edTrailerNum.setVisibility(View.GONE);
//            }
//            else
//            {
//                edTrailerNum.setVisibility(View.VISIBLE);
//                spinnerTrailerValue.setVisibility(View.GONE);
//            }

            edComments = (EditText) view.findViewById(R.id.edComments);

            fabCertify = (ImageButton) view.findViewById(R.id.fabInspectionCertify);
            fabCertify.setOnClickListener(this);

            butAddDefect = (Button) view.findViewById(R.id.butAddDefect);
            butAddDefect.setOnClickListener(this);
            butAddPiture = (Button) view.findViewById(R.id.butAddPicture);
            butAddPiture.setOnClickListener(this);

            switchDefect = (CheckBox) view.findViewById(R.id.switchDefect);
            switchDefect.setOnCheckedChangeListener(this);
            switchDefectRepaired = (CheckBox) view.findViewById(R.id.switchDefectRepaired);
            switchDefectRepaired.setOnCheckedChangeListener(this);
            switchSafeToDrive = (CheckBox) view.findViewById(R.id.switchSafeToDrive);
            switchSafeToDrive.setOnCheckedChangeListener(this);

            tvDefectItemsLabel = (TextView) view.findViewById(R.id.tvDefectedItemsLabel);
            tvDefectItems = (TextView) view.findViewById(R.id.tvDefectedItems);

            layoutImages = (LinearLayout) view.findViewById(R.id.linear);
            defect = 0;
            defectRepaired = 0;
            safeToDrive = 0;

            horizontalScrollView = (HorizontalScrollView) view.findViewById(R.id.horizontal_scroll);
            horizontalScrollView.setVisibility(View.GONE);

            tableDefects = (TableLayout) view.findViewById(R.id.tableDefects);
            tableDefects.setVisibility(View.GONE);

            imgSignature = (ImageView) view.findViewById(R.id.imgSignature);
            btnAddSignature = (Button) view.findViewById(R.id.btnAddSignature);
            btnAddSignature.setOnClickListener(this);

            // For Date
            tvDate = (TextView) view.findViewById(R.id.tvDate);
            initializeDatePicker();


            // For Time
            tvTime = (TextView) view.findViewById(R.id.tvTime);

            tvTime.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {

                    showTimePicker();
                }
            });


            if (viewMode) {
                tvLocation.setEnabled(false);
                tvOdometer.setEnabled(false);
                edTruckNum.setEnabled(false);
                edTrailerNum.setEnabled(false);
                for (View v : rbgType.getTouchables()) {
                    v.setEnabled(false);
                }

                edComments.setEnabled(false);

                switchDefect.setEnabled(false);
                switchDefectRepaired.setEnabled(false);
                switchSafeToDrive.setEnabled(false);

                butAddDefect.setVisibility(View.GONE);
                butAddPiture.setVisibility(View.GONE);

                //butCertify.setVisibility(View.GONE);
                //butClose.setVisibility(View.VISIBLE);
                fabCertify.setVisibility(View.GONE);
                tvTime.setOnClickListener(null);
                //actionBar.setTitle("Inspection");
            }

            listImages = new ArrayList<String>();

            countCurrentLocationFailed = 0;

            //toolbar = (Toolbar) view.findViewById(R.id.toolbar);
            //.setSupportActionBar(toolbar);
        } catch (Exception e) {
            LogFile.write(NewInspectionFragment.class.getName() + "::initialize Error:" + e.getMessage(), LogFile.USER_INTERACTION, LogFile.ERROR_LOG);
            LogDB.writeLogs(NewInspectionFragment.class.getName(), "initialize", e.getMessage(), Utility.printStackTrace(e));

        }
    }

    // Created By: Pallavi Wattamwar
    // Created Date: 17 January 2018
    // Purpose: For initialization of date picker.
    private void initializeDatePicker() {


        final DatePickerDialog.OnDateSetListener datepicker = new DatePickerDialog.OnDateSetListener() {

            @Override
            public void onDateSet(DatePicker view, int year, int monthOfYear,
                                  int dayOfMonth) {

                Calendar myCalendar = Calendar.getInstance();
                myCalendar.set(Calendar.YEAR, year);
                myCalendar.set(Calendar.MONTH, monthOfYear);
                myCalendar.set(Calendar.DAY_OF_MONTH, dayOfMonth);
                Date todayDate = Utility.newDateOnly();
                Date prevDate = Utility.getDate(todayDate, -15);
                if (prevDate.after(myCalendar.getTime())) {
                    Utility.showMsg("Date before 15 days is not allowed!");
                } else {
                    String date = Utility.format(myCalendar.getTime(), CustomDateFormat.dt1);
                    String logDate = Utility.getDate(date);
                    tvDate.setText(logDate);
                }
            }

        };

        tvDate.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {

                Date date = Utility.parse(tvDate.getText().toString() + " 00:00:00");
                Calendar myCalendar = Calendar.getInstance();
                myCalendar.setTime(date);
                DatePickerDialog dialog = new DatePickerDialog(getContext(), datepicker, myCalendar.get(Calendar.YEAR), myCalendar.get(Calendar.MONTH),
                        myCalendar.get(Calendar.DAY_OF_MONTH));

                dialog.show();
            }
        });
    }

    // Created By: Pallavi Wattamwar
    // Created Date: 17 January 2018
    // Purpose: For show and initialization  of time  picker .
    private void showTimePicker() {
        try {

            String[] times = tvTime.getText().toString().split(":");
            int hour = Integer.valueOf(times[0]);
            int minute = Integer.valueOf(times[1]);
            second = Integer.valueOf(times[2].replace(" AM", "").replace(" PM", ""));


            TimePickerDialog mTimePicker;
            mTimePicker = new TimePickerDialog(getActivity(), new TimePickerDialog.OnTimeSetListener() {
                @Override
                public void onTimeSet(TimePicker view, int hourOfDay, int minute) {
                    Calendar calendar = Calendar.getInstance();

                    String s;
                    Format formatter;

                    calendar.set(Calendar.HOUR_OF_DAY, hourOfDay);
                    calendar.set(Calendar.MINUTE, minute);
                    calendar.set(Calendar.SECOND, second); //reset seconds to zero

                    formatter = new SimpleDateFormat(CustomDateFormat.t1);
                    s = formatter.format(calendar.getTime());

                    //String s1 = Utility.getTimeByFormat(s);
                    tvTime.setText(s);
                    //butSave.setEnabled(true);
                }
            }, hour, minute, DateFormat.is24HourFormat(getActivity()));
            mTimePicker.show();
        } catch (Exception e) {
            LogFile.write(NewInspectionFragment.class.getName() + "::showTimePicker Error: " + e.getMessage(), LogFile.USER_INTERACTION, LogFile.ERROR_LOG);
            LogDB.writeLogs(NewInspectionFragment.class.getName(), "showTimePicker", e.getMessage(), Utility.printStackTrace(e));

        }
    }


    public static NewInspectionFragment newInstance() {
        return new NewInspectionFragment();

    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //setRetainInstance(true);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.activity_new_inspection, container, false);

        defectItems = "";

        viewMode = false;
        if (getArguments() != null) {
            if (getArguments().getBoolean("view_mode", false)) {
                viewMode = true;
            }
        }
// For Trailer inspection
        TrailerDVIRFg = false;
        if (getArguments() != null) {
            TrailerDVIRFg = getArguments().getBoolean("isTrailerDVIR", false);

        }
        initialize(view);
        String format = CustomDateFormat.dt5; //12hr
        if (Utility._appSetting.getTimeFormat() == AppSettings.AppTimeFormat.HR24.ordinal()) {
            format = CustomDateFormat.dt6;
        }

        if (!viewMode) {
            currentDatetime = Utility.getCurrentDateTime();


           /* String currentDate = Utility.convertDate(Utility.newDate(), format);
            tvDateTime.setText(currentDate);*/

            // set Current Date and Time
            tvDate.setText(Utility.getDate(Utility.getCurrentDateTime()));
            tvTime.setText(Utility.getCurrentTime());


            latitude = Utility.currentLocation.getLatitude();
            longitude = Utility.currentLocation.getLongitude();
            //tvLocation.setText(Utility.currentLocation.getLocationDescription());
            double odometerReading = Double.valueOf(CanMessages.OdometerReading);

            String unit = "Kms";
            if (Utility._appSetting.getUnit() == 2) {
                odometerReading = odometerReading * .62137d;
                unit = "Miles";
            }

            tvOdometer.setText(String.format("%.0f", odometerReading));
            String plateNo = Utility.PlateNo == null || Utility.PlateNo.isEmpty() ? "" : " (" + Utility.PlateNo + ")";
            edTruckNum.setText(Utility.UnitNo + plateNo);
            edTrailerNum.setText(Utility.TrailerNumber);
            //tvLocation.setText(Utility.currentLocation.getLocationDescription());
            callGeocodeTask();
        } else {
            TripInspectionBean bean = (TripInspectionBean) getArguments().getSerializable("trip_inspection");
            inspectionType = bean.getType();
            currentDatetime = bean.getInspectionDateTime();

     /*       String tripDate = Utility.convertDate(bean.getInspectionDateTime(), format);
            tvDateTime.setText(tripDate);*/
            String tripTime = bean.getInspectionDateTime();
            // get Date and Time from TripInspectionBean
            tvDate.setText(Utility.getDate(bean.getInspectionDateTime()));

            tvTime.setText(Utility.getTime12(tripTime));

            // remove on click listner because of view only
            tvDate.setOnClickListener(null);
            tvTime.setOnClickListener(null);


            tvLocation.setText(bean.getLocation());
            double odometerReading = Double.valueOf(bean.getOdometerReading());
            String unit = "Kms";
            if (Utility._appSetting.getUnit() == 2) {
                odometerReading = odometerReading * .62137d;
                unit = "Miles";
            }

            tvOdometer.setText(String.format("%.0f", odometerReading));

            edTruckNum.setText(bean.getTruckNumber());
            edTrailerNum.setText(bean.getTrailerNumber());
            edComments.setText(bean.getComments());

            if (bean.getDefect() == 1) {
                switchDefect.setChecked(true);
                tvDefectItems.setVisibility(View.VISIBLE);
                tvDefectItemsLabel.setVisibility(View.VISIBLE);
                switchDefectRepaired.setVisibility(View.VISIBLE);
            } else {
                switchDefect.setChecked(false);
            }

            if (bean.getDefectRepaired() == 1) {
                switchDefectRepaired.setChecked(true);
            } else {
                switchDefectRepaired.setChecked(false);
                if (bean.getDefect() == 1) {
                    switchSafeToDrive.setVisibility(View.VISIBLE);
                }
            }

            if (bean.getSafeToDrive() == 1) {
                switchSafeToDrive.setChecked(true);
            } else {
                switchSafeToDrive.setChecked(false);
            }

            defectItems = bean.getDefectItems();
            if (defectItems != null && defectItems.length() > 0) {
                String[] index = defectItems.split(",");
                String text = "";
                for (int i = 0; i < index.length; i++) {
                    int idx = Integer.valueOf(index[i]);
                    text += getResources().getStringArray(R.array.defect_items)[idx];
                    if (i < index.length - 1) {
                        text += ", ";
                    }
                }
                tableDefects.setVisibility(View.VISIBLE);
                tvDefectItemsLabel.setVisibility(View.VISIBLE);
                tvDefectItems.setVisibility(View.VISIBLE);
                tvDefectItems.setText(text);
            }

            if (!bean.getPictures().equals("")) {
                horizontalScrollView.setVisibility(View.VISIBLE);
                String[] images = bean.getPictures().split(",");
                if (images.length > 0) {
                    for (int i = 0; i < images.length; i++) {
                        listImages.add(images[i]);

                        scaleBitmap = BitmapUtility.decodeSampledBitmapFromFile(images[i], 128, 72);

                        final ImageView imageView = new ImageView(getActivity());
                        imageView.setId(listImages.size() - 1);
                        imageView.setPadding(2, 2, 2, 2);
                        imageView.setImageBitmap(scaleBitmap);
                        imageView.setScaleType(ImageView.ScaleType.FIT_XY);
                        imageView.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                if (viewImageDialog == null) {
                                    viewImageDialog = new ViewImageDialog();
                                }
                                if (viewImageDialog.isAdded()) {
                                    viewImageDialog.dismiss();
                                }
                                viewImageDialog.setImagePath(listImages.get(imageView.getId()),false);
                                viewImageDialog.show(getFragmentManager(), "viewImage_dialog");
                            }
                        });

                        layoutImages.addView(imageView);
                    }
                    System.gc();
                }
            }
        }

        ((RadioButton) rbgType.getChildAt(inspectionType)).setChecked(true);
        drawBackground();
        return view;
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
      /*  try {
            LayoutInflater inflater = (LayoutInflater) getActivity().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            ViewGroup viewGroup = (ViewGroup) getView();
            View view = inflater.inflate(R.layout.activity_new_inspection, viewGroup, false);
            viewGroup.removeAllViews();
            viewGroup.addView(view);
            initialize(view);
        } catch (Exception exe) {
        }*/
    }
    boolean isChecked;
    @Override
    public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
        if (viewMode)
            return;
        switch (buttonView.getId()) {
            case R.id.switchDefect:
                if (isChecked) {
                    defect = 1;

                    switchDefectRepaired.setVisibility(View.VISIBLE);
                    switchSafeToDrive.setVisibility(View.VISIBLE);

                    butAddDefect.setVisibility(View.VISIBLE);
                    butAddPiture.setVisibility(View.VISIBLE);

                    //butCertify.setEnabled(false);
                    fabCertify.setEnabled(true);
                    certifyMessage = "";
                } else {
                    defect = 0;
                    switchDefectRepaired.setVisibility(View.GONE);
                    tableDefects.setVisibility(View.GONE);
                    defectItems = "";
                    switchSafeToDrive.setVisibility(View.GONE);

                    //lvDefectItems.setVisibility(View.INVISIBLE);
                    butAddDefect.setVisibility(View.GONE);
                    butAddPiture.setVisibility(View.GONE);
                    tvDefectItemsLabel.setVisibility(View.GONE);
                    tvDefectItems.setVisibility(View.GONE);

                    //butCertify.setEnabled(true);
                    fabCertify.setEnabled(true);
                }
                this.isChecked = isChecked;
                break;
            case R.id.switchDefectRepaired:
                if (isChecked) {
                    defectRepaired = 1;
                    certifyMessage = getResources().getString(R.string.repaired_msg);

                    switchSafeToDrive.setVisibility(View.GONE);
                } else {
                    defectRepaired = 0;
                    certifyMessage = "";

                    switchSafeToDrive.setVisibility(View.VISIBLE);
                }
                break;
            case R.id.switchSafeToDrive:
                if (isChecked) {
                    safeToDrive = 1;
                    certifyMessage = getResources().getString(R.string.safetodrive_msg);
                } else {
                    safeToDrive = 0;
                    certifyMessage = getResources().getString(R.string.no_safetodrive_msg);
                }
                break;
        }
    }

    @Override
    public void onClick(View view) {
        try {

            Utility.hideKeyboard(getActivity(), view);
            switch (view.getId()) {
                case R.id.butAddDefect:
                    if (defectSelectionDialog == null) {
                        defectSelectionDialog = new DefectSelectionDialog();
                    }
                    if (defectSelectionDialog.isAdded()) {
                        defectSelectionDialog.dismiss();
                    }
                    defectSelectionDialog.mListener = this;
                    defectSelectionDialog.setItems(defectItems);
                    defectSelectionDialog.show(getFragmentManager(), "defectSelection_dialog");
                    break;

                case R.id.butAddPicture:
                    final String dir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES) + "/hutch/";
                    File newdir = new File(dir);
                    if (!newdir.exists()) {
                        newdir.mkdirs();
                    }
                    final String path = dir + Utility.getCurrentDate() + "/";
                    File currentDir = new File(path);
                    currentDir.mkdirs();

                    // Here, the counter will be incremented each time, and the
                    // picture taken by camera will be stored as 1.jpg,2.jpg
                    // and likewise.
                    count++;
                    imageFile = path + Utility.getStringTime(Utility.getCurrentDateTime()) + ".jpg";
                    File newfile = new File(imageFile);
                    try {
                        newfile.createNewFile();

                    } catch (IOException e) {
                    }

                    //call to stop AutoStartService because it will launch another activity,
                    //that means our application will go to background

                    AutoStartService.pauseTask = true;
                    Intent cameraIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
                    if (cameraIntent.resolveActivity(getActivity().getPackageManager()) != null) {

                        StrictMode.VmPolicy.Builder builder = new StrictMode.VmPolicy.Builder();
                        StrictMode.setVmPolicy(builder.build());

                        Uri photoURI = Uri.fromFile(newfile);
                        cameraIntent.putExtra(MediaStore.EXTRA_OUTPUT, photoURI);
                        startActivityForResult(cameraIntent, TAKE_PHOTO_CODE);
                    }
                    break;

                //case R.id.butCertify:
                case R.id.fabInspectionCertify:
                    if(isChecked && defectItems.length() == 0){
                        Utility.showAlertMsg("Please select defects!");
                        return;
                    }

                    String location = tvLocation.getText().toString();

//                    if (TrailerDVIRFg && spinnerTrailerValue.getSelectedItemPosition()==0)
//                    {
//
//                        Utility.showAlertMsg("Please select trailer!");
//                        return;
//                    }

                    if (TrailerDVIRFg && edTrailerNum.getText().toString().trim().equals("")) {

                        Utility.showAlertMsg("Please enter trailer no.!");
                        return;
                    }
                    if (location.isEmpty()) {
                        Utility.showAlertMsg("Please enter location!");
                        return;
                    }
                    // handling for mandatory Signature
                    if (!(new File(Utility.GetSignaturePath()).exists())) {
                        Utility.showAlertMsg("Please Add Signature");
                        return;
                    }
                    // Handling for Restriction of Selecting  future Date
                    Date currentSelectedTime = Utility.parse(tvDate.getText() + " " + tvTime.getText());
                    if (currentSelectedTime.after(Utility.newDate12())) {
                        Utility.showAlertMsg(Utility.context.getString(R.string.future_time_alert));
                        return;
                    }
                    showCertifyMessage();
                    break;
                case R.id.btnAddSignature:
                    launchSignature();
                    break;
            }
        } catch (Exception e) {
            LogFile.write(NewEventFragment.class.getName() + "::onClick Error: " + e.getMessage(), LogFile.USER_INTERACTION, LogFile.ERROR_LOG);
            LogDB.writeLogs(NewInspectionFragment.class.getName(), "onClick", e.getMessage(), Utility.printStackTrace(e));

        }
    }

    private void drawBackground() {
        try {
            String filePath = Utility.GetSignaturePath();
            File file = new File(filePath);
            if (file.exists()) {
                Bitmap bitmap = BitmapFactory.decodeFile(filePath);
                BitmapDrawable bitmapDrawable = new BitmapDrawable(getResources(), bitmap);
                imgSignature.setBackground(bitmapDrawable);
                imgSignature.setVisibility(View.VISIBLE);
                btnAddSignature.setVisibility(View.GONE);
            } else {
                btnAddSignature.setVisibility(View.VISIBLE);
                imgSignature.setVisibility(View.GONE);
            }
        } catch (Exception exe) {
        }
    }

    public void launchSignature() {
        try {

            FragmentManager manager = getFragmentManager();
            FragmentTransaction ft = manager.beginTransaction();

            Fragment prev = manager.findFragmentByTag("signature_dialog");
            if (prev != null) {
                ft.remove(prev);
                //ft.addToBackStack(null);
                ft.commitNow();
                ft = manager.beginTransaction();
            }

            SignatureDialogFragment signatureDialog = SignatureDialogFragment.newInstance();
            signatureDialog.mListner = this;
            signatureDialog.show(ft, "signature_dialog");

        } catch (Exception e) {
            LogFile.write(ELogFragment.class.getName() + "::launchRuleChange Error:" + e.getMessage(), LogFile.USER_INTERACTION, LogFile.ERROR_LOG);
            LogDB.writeLogs(NewInspectionFragment.class.getName(), "launchSignature", e.getMessage(), Utility.printStackTrace(e));

        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent intent) {
        try {
            if (requestCode == DEFECT_SELECTION_CODE) {
                if (resultCode == Activity.RESULT_OK) {
                    defectItems = intent.getStringExtra("selected_items");
                    if (defectItems.length() > 0) {
                        String[] index = defectItems.split(",");
                        String text = "";
                        for (int i = 0; i < index.length; i++) {
                            int idx = Integer.valueOf(index[i]);
                            text += getResources().getStringArray(R.array.defect_items)[idx];
                            if (i < index.length - 1) {
                                text += ", ";
                            }
                        }
                        tableDefects.setVisibility(View.VISIBLE);
                        tvDefectItemsLabel.setVisibility(View.VISIBLE);
                        tvDefectItems.setVisibility(View.VISIBLE);
                        tvDefectItems.setText(text);

                        //butCertify.setEnabled(true);
                        fabCertify.setEnabled(true);
                    }
                }
            } else if (requestCode == TAKE_PHOTO_CODE && resultCode == Activity.RESULT_OK) {

                BitmapUtility.compressAndSaveBitmap(imageFile,false);
                listImages.add(imageFile);

                scaleBitmap = BitmapUtility.decodeSampledBitmapFromFile(imageFile, 128, 72);


                final ImageView imageView = new ImageView(getActivity());
                imageView.setId(listImages.size() - 1);
                imageView.setPadding(2, 2, 2, 2);
                imageView.setImageBitmap(scaleBitmap);
                imageView.setScaleType(ImageView.ScaleType.FIT_XY);
                imageView.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        if (viewImageDialog == null) {
                            viewImageDialog = new ViewImageDialog();
                        }
                        if (viewImageDialog.isAdded()) {
                            viewImageDialog.dismiss();
                        }
                        viewImageDialog.setImagePath(listImages.get(imageView.getId()),false);
                        viewImageDialog.show(getFragmentManager(), "viewImage_dialog");
                    }
                });
                //add delete button for current captured image
                Button butDelete = new Button(getActivity());
                butDelete.setText("Delete");
                butDelete.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        String image = listImages.get(imageView.getId());
                        listImages.remove(image);
                        layoutImages.removeViewAt(imageView.getId());
                    }
                });
                LinearLayout layout = new LinearLayout(getActivity());
                layout.setOrientation(LinearLayout.VERTICAL);
                layout.addView(imageView);
                layout.addView(butDelete);
                layoutImages.addView(layout);
                //layoutImages.addView(imageView);
                horizontalScrollView.setVisibility(View.VISIBLE);
            } else if (requestCode == VIEW_PHOTO && resultCode == Activity.RESULT_OK) {

            }
        } catch (Exception e) {
            LogFile.write(NewInspectionFragment.class.getName() + "::onActivityResult Error: " + e.getMessage(), LogFile.USER_INTERACTION, LogFile.ERROR_LOG);
            LogDB.writeLogs(NewInspectionFragment.class.getName(), "onActivityResult", e.getMessage(), Utility.printStackTrace(e));

        }
        AutoStartService.pauseTask = false;
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
        if (switchDefect == null)
            return;

        switchDefect.setOnCheckedChangeListener(null);

        switchDefectRepaired.setOnCheckedChangeListener(null);

        switchSafeToDrive.setOnCheckedChangeListener(null);

        //butBack.setOnClickListener(null);
        if (defectSelectionDialog != null) {
            defectSelectionDialog.mListener = null;
        }
        fabCertify.setOnClickListener(null);


        butAddDefect.setOnClickListener(null);
        butAddPiture.setOnClickListener(null);
    }

    public void showCertifyMessage() {
        try {
            certifyMessage = "";
            if (defect == 0) {
                certifyMessage = getResources().getString(R.string.defect_msg);
            } else {
                if (defectRepaired == 1) {
                    certifyMessage = getResources().getString(R.string.repaired_msg);
                } else {
                    if (safeToDrive == 1) {
                        certifyMessage = getResources().getString(R.string.safetodrive_msg);
                    } else {
                        certifyMessage = getResources().getString(R.string.no_safetodrive_msg);
                    }
                }
            }

            final AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
            builder.setMessage(certifyMessage)
                    .setTitle(getString(R.string.e_log))
                    .setIcon(R.drawable.ic_launcher)
                    .setCancelable(false)
                    .setPositiveButton(getString(R.string.ok), new DialogInterface.OnClickListener() {
                        public void onClick(@SuppressWarnings("unused") final DialogInterface dialog, @SuppressWarnings("unused") final int id) {
                            //save to DB
                            int driverId = Utility.user1.isOnScreenFg() ? Utility.user1.getAccountId() : Utility.user2.getAccountId();
                            String driverName = Utility.user1.isOnScreenFg() ? (Utility.user1.getFirstName() + " " + Utility.user1.getLastName()) : (Utility.user2.getFirstName() + " " + Utility.user2.getLastName());

                            String pictures = "";
                            for (int i = 0; i < listImages.size(); i++) {
                                pictures += listImages.get(i);
                                if (i < listImages.size() - 1) {
                                    pictures += ",";
                                }
                            }

                            String odometer = "0";
                            try {
                                double odometerReading = Double.parseDouble(tvOdometer.getText().toString());
                                if (Utility._appSetting.getUnit() == 2) {
                                    odometerReading = odometerReading * 1.60934d;

                                }
                                odometer = String.format("%.0f", odometerReading);
                            } catch (Exception e) {
                                odometer = "0";
                            }

                            // Changed from currentDatetime To tvDate.getText().toString()+" "+ tvTime.getText().toString()

                            // Changed from edTrailerNum.getText().toString() To trailerNum
                            // String trailerNum = TrailerDVIRFg ? spinnerTrailerValue.getSelectedItem().toString() : edTrailerNum.getText().toString();
                            String trailerNum = edTrailerNum.getText().toString();

                            TripInspectionDB.CreateTripInspection(tvDate.getText().toString() + " " + tvTime.getText().toString(), driverId, driverName, inspectionType, defect, defectRepaired, safeToDrive,
                                    defectItems, Double.toString(latitude), Double.toString(longitude), tvLocation.getText().toString(),
                                    odometer, edTruckNum.getText().toString(), trailerNum, edComments.getText().toString(), pictures, TrailerDVIRFg ? 1 : 0);


                            dialog.cancel();
                            if (mListener != null) {
                                mListener.finishInspection();
                            }
                        }
                    })
                    .setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
                        public void onClick(final DialogInterface dialog, @SuppressWarnings("unused") final int id) {
                            dialog.cancel();
                        }
                    });
            final AlertDialog alert = builder.create();
            alert.show();
        } catch (Exception e) {
            LogFile.write(NewInspectionFragment.class.getName() + "::showCertifyMessage Error:" + e.getMessage(), LogFile.USER_INTERACTION, LogFile.ERROR_LOG);
            LogDB.writeLogs(NewInspectionFragment.class.getName(), "showCertifyMessage", e.getMessage(), Utility.printStackTrace(e));

        }
    }

    @Override
    public void setSelectedItems(String selectedIndices) {
        defectItems = selectedIndices;
        if (defectItems.length() > 0) {
            String[] index = defectItems.split(",");
            String text = "";
            for (int i = 0; i < index.length; i++) {
                int idx = Integer.valueOf(index[i]);
                text += getResources().getStringArray(R.array.defect_items)[idx];
                if (i < index.length - 1) {
                    text += ", ";
                }
            }
            tableDefects.setVisibility(View.VISIBLE);
            tvDefectItemsLabel.setVisibility(View.VISIBLE);
            tvDefectItems.setVisibility(View.VISIBLE);
            tvDefectItems.setText(text);

            //butCertify.setEnabled(true);
            fabCertify.setEnabled(true);
        }else {
            tableDefects.setVisibility(View.GONE);
            tvDefectItemsLabel.setVisibility(View.GONE);
            tvDefectItems.setVisibility(View.GONE);
            tvDefectItems.setText("");
        }
    }


    private void callGeocodeTask() {
        tvLocation.setText("");
        if (Utility._appSetting.getLocationSource() == 1) {
            new NearestDistanceTask(nearestLocationTaskListener).executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
        } else if (Utility._appSetting.getLocationSource() == 2) {
            new GeocodeTask(geocodeTaskListener).executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
        } else if (Utility.mapLoadedFg) {
            tvLocation.setText(Utility.currentLocation.getLocationDescription());

        }
    }

    NearestDistanceTask.PostTaskListener<Boolean> nearestLocationTaskListener = new NearestDistanceTask.PostTaskListener<Boolean>() {
        @Override
        public void onPostTask(Boolean status) {
            if (status) {
                tvLocation.setText(Utility.currentLocation.getLocationDescription());
            } else {
                tvLocation.setText("");
            }
        }
    };

    public interface OnFragmentInteractionListener {
        // TODO: Update argument type and name
        void finishInspection();

        void stopService();

        void onSignatureUpload(String data, String path);
    }


    @Override
    public void onSignatureUpload(String data, String path) {
        drawBackground();
        mListener.onSignatureUpload(data, path);
    }
}
