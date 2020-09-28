package com.hutchsystems.hutchconnect.fragments;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.graphics.Rect;
import android.hardware.usb.UsbDevice;
import android.hardware.usb.UsbManager;
import android.media.MediaScannerConnection;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.ParcelFileDescriptor;
import androidx.fragment.app.DialogFragment;
import android.text.InputType;
import android.util.Log;
import android.util.Patterns;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.RadioButton;
import android.widget.RelativeLayout;
import android.widget.ScrollView;

import com.hutchsystems.hutchconnect.R;
import com.hutchsystems.hutchconnect.common.DocumentType;
import com.hutchsystems.hutchconnect.common.LogFile;
import com.hutchsystems.hutchconnect.common.OutputFile;
import com.hutchsystems.hutchconnect.common.StorageDirectory;
import com.hutchsystems.hutchconnect.common.Utility;
import com.hutchsystems.hutchconnect.common.WebService;
import com.hutchsystems.hutchconnect.common.WebUrl;
import com.hutchsystems.hutchconnect.db.LogDB;

import org.json.JSONObject;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.util.List;

public class OutputFileSendDialog extends DialogFragment implements CompoundButton.OnCheckedChangeListener{

    ScrollView  scrollViewFileSend;
    EditText etComment, etPassword;
    RadioButton rdUsbDrive, rdBluetooth, rdEmail, rdWebService;
    Button btnSend;
    String TAG = OutputFileSendDialog.class.getName();
    //UsbManager mUsbManager;
    private static final String ACTION_USB_PERMISSION = "com.hutchgroup.elog.USB_PERMISSION";

    private static final int SAVE_REQUEST_CODE = 42;
    PendingIntent mPermissionIntent;

    private static int TIMEOUT = 0;
    private boolean forceClaim = true;

    UsbManager mUsbManager = null;
    IntentFilter filterAttached_and_Detached = null;
    ImageButton imgCancel;

    private final BroadcastReceiver mUsbReceiver = new BroadcastReceiver() {

        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            if (ACTION_USB_PERMISSION.equals(action)) {
                synchronized (this) {
                    final UsbDevice device = intent.getParcelableExtra(UsbManager.EXTRA_DEVICE);

                    if (intent.getBooleanExtra(UsbManager.EXTRA_PERMISSION_GRANTED, false)) {
                        if (device != null) {
//Body of your click handler
                            Thread thread = new Thread(new Runnable() {
                                @Override
                                public void run() {
                                    //TransferToUSB(device);
                                    Log.i("UsbFile", "SaveTestFile UsbReceiver");
                                    SaveReportToUSB();
                                }
                            });
                            thread.start();
                        }
                    } else {
                        Log.d(TAG, "permission denied for device " + device);
                    }
                }
            }
        }
    };
    private RadioButton rdEmailTo,rdFax;

    public OutputFileSendDialog() {

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.activity_output_file_send_, container);
        try {

            initialize(view);
            // getDialog().setTitle("Send ELD Data");
            getDialog().getWindow().requestFeature(Window.FEATURE_NO_TITLE);
            getDialog().getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_RESIZE);
            this.setCancelable(true);

        } catch (Exception e) {
            LogFile.write(OutputFileSendDialog.class.getName() + "::onCreateView Error: " + e.getMessage(), LogFile.USER_INTERACTION, LogFile.ERROR_LOG);
        }
        return view;
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        try {

        } catch (Exception e) {
            LogFile.write(OutputFileSendDialog.class.getName() + "::onActivityCreated Error: " + e.getMessage(), LogFile.USER_INTERACTION, LogFile.ERROR_LOG);
            LogDB.writeLogs(OutputFileSendDialog.class.getName(), "onActivityCreated", e.getMessage(), Utility.printStackTrace(e));

        }
    }

    @Override
    public void onResume() {
        Log.i(TAG, "onResume");
        ViewGroup.LayoutParams params = getDialog().getWindow().getAttributes();
        params.width = WindowManager.LayoutParams.MATCH_PARENT;
        params.height = WindowManager.LayoutParams.WRAP_CONTENT;
        getDialog().getWindow().setAttributes((android.view.WindowManager.LayoutParams) params);

        super.onResume();
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

    int sendOption = 0;

    private void initialize(final View view) {

        loadingPanel = (RelativeLayout) view.findViewById(R.id.loadingPanel);
        // register broad cast reciever to check permission to communicate with usb
        mUsbManager = (UsbManager) getActivity().getSystemService(Context.USB_SERVICE);
        //mPermissionIntent = PendingIntent.getBroadcast(getActivity(), 0, new Intent(ACTION_USB_PERMISSION), 0);
        //IntentFilter filter = new IntentFilter(ACTION_USB_PERMISSION);
        //getActivity().registerReceiver(mUsbReceiver, filter);

        //
        filterAttached_and_Detached = new IntentFilter(UsbManager.ACTION_USB_ACCESSORY_DETACHED);
        filterAttached_and_Detached.addAction(UsbManager.ACTION_USB_DEVICE_ATTACHED);
        filterAttached_and_Detached.addAction(ACTION_USB_PERMISSION);
        //
        getActivity().registerReceiver(mUsbReceiver, filterAttached_and_Detached);

        etComment = (EditText) view.findViewById(R.id.etComment);
        scrollViewFileSend = (ScrollView) view.findViewById(R.id.scrollViewFileSend);
        etPassword = (EditText) view.findViewById(R.id.etPassword);
        etPassword.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                int action = event.getActionMasked();

                switch (action) {

                    case MotionEvent.ACTION_DOWN:
                        etPassword.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_VISIBLE_PASSWORD);
                        break;

                    case MotionEvent.ACTION_UP:
                        etPassword.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_PASSWORD);
                        break;
                }

                return v.onTouchEvent(event);
            }
        });
        rdUsbDrive = (RadioButton) view.findViewById(R.id.rdUsbDrive);
        rdUsbDrive.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (isChecked) {
                    etPassword.setVisibility(View.VISIBLE);
                } else {
                    etPassword.setVisibility(View.GONE);
                }
            }
        });
        view.getViewTreeObserver().addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
            @Override
            public void onGlobalLayout() {
                Rect r = new Rect();
                //r will be populated with the coordinates of your view that area still visible.
                view.getWindowVisibleDisplayFrame(r);
                //when keyboard close, view scroll from top
                scrollViewFileSend.fullScroll(ScrollView.FOCUS_UP);
            }
        });
        rdUsbDrive.setEnabled(false);
        rdBluetooth = (RadioButton) view.findViewById(R.id.rdBluetooth);
        rdBluetooth.setOnCheckedChangeListener(this);
        rdBluetooth.setEnabled(false);

        rdEmail = (RadioButton) view.findViewById(R.id.rdEmail);
        rdEmail.setOnCheckedChangeListener(this);

        rdWebService = (RadioButton) view.findViewById(R.id.rdWebService);
        rdWebService.setOnCheckedChangeListener(this);

        rdEmailTo = (RadioButton) view.findViewById(R.id.rdEmailTo);
        rdEmailTo.setOnCheckedChangeListener(this);

        rdFax=(RadioButton) view.findViewById(R.id.rdFax);
        rdFax.setOnCheckedChangeListener(this);

        imgCancel = (ImageButton) view.findViewById(R.id.imgCancel);
        imgCancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Utility.hideKeyboard(getActivity(), v);
                dismiss();
            }
        });

        btnSend = (Button) view.findViewById(R.id.btnSend);
        btnSend.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Utility.hideKeyboard(getActivity(), v);
                String confirmation = "";
                String comment=etComment.getText().toString().trim();

                if (rdUsbDrive.isChecked()) {
                    sendOption = 0;
                    confirmation = "Are you sure you want to transfer File to USB Device?";

                } else if (rdBluetooth.isChecked()) {
                    sendOption = 1;
                    confirmation = "Are you sure you want to Send Data to Bluetooth Device?";
                } else if (rdEmail.isChecked()) {
                    sendOption = 2;
                    confirmation = "Are you sure you want to Send Data to FMCSA Email?";

                } else if (rdWebService.isChecked()) {
                    sendOption = 3;
                    confirmation = "Are you sure you want to Send Data to FMCSA Web Service?";

                }  else if (rdEmailTo.isChecked()) {

                    if(comment.isEmpty())
                    {
                        Utility.showMsg(getString(R.string.email_Alert) );
                        return;
                    } else if(!Patterns.EMAIL_ADDRESS.matcher(comment).matches()){

                        Utility.showMsg(getString(R.string.email_validate_alert) );
                        return;
                    } else
                    {
                        sendOption = 4;
                        confirmation = "Are you sure you want to Send Data using Email?";

                    }

                } else if (rdFax.isChecked()) {
                    if(comment.isEmpty())
                    {
                        Utility.showMsg(getString(R.string.fax_Alert));
                        return;
                    }else if(!Patterns.PHONE.matcher(comment).matches()){

                        Utility.showMsg(getString(R.string.fax_validate_alert) );
                        return;
                    }
                    else
                    {
                        sendOption = 5;
                        confirmation = "Are you sure you want to Send Data using Fax?";
                    }

                }else {
                    Utility.showMsg(getString(R.string.send_method_alert));
                    return;
                }

                final AlertDialog ad = new AlertDialog.Builder(getActivity())
                        .create();
                ad.setCancelable(true);
                ad.setCanceledOnTouchOutside(false);
                ad.setTitle("Send Data Confirmation?");
                ad.setIcon(R.drawable.ic_launcher);
                ad.setMessage(confirmation);
                ad.setButton(DialogInterface.BUTTON_POSITIVE, getString(R.string.yes),
                        new DialogInterface.OnClickListener() {

                            @Override
                            public void onClick(DialogInterface dialog,
                                                int which) {
                                if (Utility.isInternetOn()) {
                                    SendFile(sendOption);
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
        });

    }

    @Override
    public void onDestroy() {
        // TODO Auto-generated method stub
        super.onDestroy();
        getActivity().unregisterReceiver(mUsbReceiver);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode,
                                 Intent resultData) {

        Uri currentUri = null;

        if (resultCode == Activity.RESULT_OK) {
            if (requestCode == SAVE_REQUEST_CODE) {

                if (resultData != null) {
                    currentUri = resultData.getData();
                    if (currentUri.getHost().equals("com.android.externalstorage.documents")) {
                        final String encodedPath = currentUri.getEncodedPath();
                        final String path = encodedPath.substring(encodedPath.indexOf("%3A") + 3);
                        final File[] storagePoints = new File("/storage").listFiles();

                        // document/primary is in /storage/emulated/legacy and thus will fail the exists check in the else handling loop check
                        if (encodedPath.startsWith("/document/primary")) {
                            // External file stored in Environment path
                            final File externalFile = new File(Environment.getExternalStorageDirectory(), path);
                            MediaScannerConnection.scanFile(getActivity(),
                                    new String[]{externalFile.getAbsolutePath()}, null, null);
                        } else {
                            // External file stored in one of the mount points, check each mount point for the file
                            for (int i = 0, j = storagePoints.length; i < j; ++i) {
                                final File externalFile = new File(storagePoints[i], path);
                                if (externalFile.exists()) {
                                    MediaScannerConnection.scanFile(getActivity(),
                                            new String[]{externalFile.getAbsolutePath()}, null, null);
                                    break;
                                }
                            }
                        }
                    }

                    writeFileContent(currentUri);
                }
            }
        }
    }

    private void writeFileContent(Uri uri) {
        try {
            Utility.showMsg("WriteFile: " + uri);
            ParcelFileDescriptor pfd = getActivity().getContentResolver().openFileDescriptor(uri, "w");

            FileOutputStream out = new FileOutputStream(pfd.getFileDescriptor());

            String textContent = OutputFile.getOutputFile(etComment.getText() + "");
            Utility.showMsg("textContent: " + textContent);

            PrintWriter pw = new PrintWriter(out);
            pw.println(textContent);
            pw.flush();
            pw.close();
            out.flush();
            out.close();

            pfd.close();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
            Utility.showMsg("FileNotFoundException: " + e.getMessage());
        } catch (IOException e) {
            e.printStackTrace();
            Utility.showMsg("IOException: " + e.getMessage());
        }
    }

    // various option to send Output file to fmcsa
    private void SendFile(int sendOption) {
        try {

            // comment for output file
            String comment = etComment.getText() + "";

            // create data in format of fmcsa guidelines
            String data = OutputFile.getOutputFile(comment);

            // get file path of external storage cache folder
            String path = getContext().getExternalCacheDir() + "/" + OutputFile.fileName;

            // save file to external storage cache folder
            writeFile(data, path);

            if (sendOption == 0) { // if user select transfer mode as USB
                sendToUSB();
            } else if (sendOption == 1) { // Bluetooth Mode
                sendToBluetooth(path);
            } else if (sendOption == 2) { // send output file to fmcsa email address
                // new EmailSend(comment, path, data.length()).execute();
                new PostDataUsingEmailOrWebService(comment, path, data.length(), 2).execute();
            } else if (sendOption == 3) { // post output file to fmcsa web service
                //new PostData(OutputFile.fileName, data, comment).execute();
                new PostDataUsingEmailOrWebService(comment, path, data.length(), 3).execute();
            } else if (sendOption == 4) { // post output file using email
             new PostEmailFax(comment,1).execute();
            }
            else if (sendOption == 5) { // post output file using fax
                new PostEmailFax(comment,2).execute();
            }
        } catch (Exception ex) {

        }
    }



    // Created By: Pallavi Wattamwar
    // Created Date: 21 January 2019
    // Purpose: post Email or Fax
    public boolean postData(String inputValue,int sendOption)
    {
        Boolean status =true;

        WebService ws = new WebService();
        try {
            JSONObject obj = new JSONObject();
            obj.put("FromDate", Utility.getPreviousDateOnly(ELogFragment.canadaFg ? -14: -7));
            obj.put("ToDate", Utility.getCurrentDate());
            obj.put("SendTo",inputValue);
            obj.put("DriverId",Utility.onScreenUserId);
            // if Send Option is Email then we Send SendOption = 1
            // if Send Option is Fax then we Send SendOption= 2
            obj.put("SendOption",sendOption);

            String result = ws.doPost(
                    WebUrl.MAIL_POST,
                    obj.toString());

            if (result == null || result.isEmpty())
                return status;

        }catch (Exception e)
        {
            status = false;
            e.printStackTrace();
        }
        return status;
    }

    // Created By: Pallavi Wattamwar
    // Created Date: 21 January 2019
    // Purpose: asynck task for posting  Email or Fax
    public class PostEmailFax extends AsyncTask<Void, Void, Boolean> {

        String inputValue;
        // if Send Option is Email then we Send SendOption = 1
        // if Send Option is Fax then we Send SendOption= 2
        int sendOption;

        public PostEmailFax(  String inputValue,int sendOption) {
            this.inputValue =inputValue;
            this.sendOption=sendOption;
        }

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            showLoaderAnimation(true);
            btnSend.setEnabled(false);
        }

        @Override
        protected Boolean doInBackground(Void... voids) {
            Boolean result = postData(inputValue,sendOption);
            return result;
        }

        @Override
        protected void onPostExecute(Boolean result) {
            super.onPostExecute(result);
            btnSend.setEnabled(true);
            showLoaderAnimation(false);
            if(result) {
                Utility.showMsg("Logs are Successfully Scheduled to be sent..");

                dismiss();
            }
        }


    }
    // Created By: Deepak Sharma
    // Created Date: 12 June 2018
    // used to send file to USB
    private void sendToUSB() {
        String password = etPassword.getText().toString();
        if (password.isEmpty()) {
            Utility.showMsg(getString(R.string.password_alert));
        } else {
            String encryptedPassword, salt;
            if (Utility.user1.isOnScreenFg()) {
                encryptedPassword = Utility.user1.getPassword();
                salt = Utility.user1.getSalt();
            } else {
                encryptedPassword = Utility.user2.getPassword();
                salt = Utility.user2.getSalt();

            }

            if (!Utility.computeSHAHash(password, salt).equals(encryptedPassword)) {
                Utility.showMsg(getString(R.string.valid_password));
            } else {
                if (StorageDirectory.getUSBDirectory().isEmpty()) {
                    Utility.showMsg(getString(R.string.usb_attach_alert));
                } else {
                    //this cannot work from API 19+, because we don't have permission to write on USB, cannot use UsbManager to grant permission
                    SaveReportToUSB();
                }
            }
        }
    }

    // Created By: Deepak Sharma
    // Created Date: 12 June 2018
    // used to send file to Bluetooth device using android inbuilt api
    private void sendToBluetooth(String path) {

        // instantialte file object for provided path
        File file = new File(path);

        // if file does not exists abort process
        if (!file.exists()) {
            return;
        }

        // create implicit intent for action send
        Intent intent = new Intent(Intent.ACTION_SEND);

        // set anonymous type of file
        intent.setType("*/*");

        // set package belong to bluetooth api
        intent.setPackage("com.android.bluetooth");

        // get uri from file
        Uri uri = Uri.fromFile(file);

        // pass file as stream
        intent.putExtra(Intent.EXTRA_STREAM, uri);

        // get package manager instance
        PackageManager pm = getActivity().getPackageManager();

        // get list of package
        List<ResolveInfo> list = pm.queryIntentActivities(intent, 0);
        if (list.size() > 0) {
            String packageName = null;
            String className = null;
            boolean found = false;

            // iterate list
            for (ResolveInfo info : list) {
                packageName = info.activityInfo.packageName;

                // check if bluetooth package exist
                if (packageName.equals("com.android.bluetooth")) {
                    className = info.activityInfo.name;
                    found = true;
                    break;
                }
            }

            if (!found) {
                Utility.showMsg("Bluetooth is not found.");

            } else {
                // set class name and package name for intent
                intent.setClassName(packageName, className);

                // start activity to transfer data to bluetooth
                startActivity(intent);
            }
        }
    }

    private void SaveReportToUSB() {
        try {
            String root = StorageDirectory.getUSBDirectory();
            //Utility.showMsg("Root folder=" + root);
            File rootDir = new File(root);
            rootDir.mkdirs();

            String data = OutputFile.getOutputFile(etComment.getText() + "");

            File file = new File(rootDir, OutputFile.fileName);
            if (file.exists()) {
                file.delete();
            }

            FileOutputStream out = new FileOutputStream(file);
            PrintWriter pw = new PrintWriter(out);
            pw.println(data);
            pw.flush();
            pw.close();
            out.flush();
            out.close();

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
        try {
            switch (buttonView.getId()) {
                case R.id.rdEmailTo:
                    if(isChecked)
                    {
                        etComment.setHint("Please Enter Email Id");
                    }
                    break;
                case R.id.rdFax:
                    if(isChecked)
                    {
                        etComment.setHint("Please Enter Fax No ");
                    }

                    break;
                case R.id.rdBluetooth:
                case R.id.rdEmail:
                case R.id.rdWebService:
                    if(isChecked)
                    {
                        etComment.setHint("enter output file comment ");
                    }
                      break;

            }
        } catch (Exception e)
        {
            e.printStackTrace();
        }

    }

    // Created By: Pallavi Wattamwar
    // Created Date: 24 January 2019
    //purpose:  async task method to send out put file using email or web service
    private class PostDataUsingEmailOrWebService extends AsyncTask<Void, Void, String> {
        String body, filePath;
        int length;
        //   2 for email, 3 for webservice
        int sendOption;

        public PostDataUsingEmailOrWebService(String body, String filePath, int length, int sendOption) {

            this.body = body;
            this.filePath = filePath;
            this.length = length;
            this.sendOption = sendOption;
        }

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            showLoaderAnimation(true);
            btnSend.setEnabled(false);
        }

        @Override
        protected String doInBackground(Void... voids) {
            String status = "";

            try {
                // initialze web service class
                WebService obj = new WebService();

                // call method of posting output file to fmcsa server using webservice or email
                // sendOption == 2 : Email
                // sendOption == 3 : Webservice

                status = obj.PostOutputFileWithSendOption(filePath, body, DocumentType.DOCUMENT_OUTPUT_FILE, length, sendOption);

                // delete out put file if exists
                File file = new File(filePath);
                if (file.exists()) {
                    file.delete();
                }
            } catch (Exception e) {
                Log.i(TAG, "Could not send mail" + e.getMessage());
                // status = false;
            }
            return status;
        }

        @Override
        protected void onPostExecute(String result) {
            super.onPostExecute(result);
            try {
                btnSend.setEnabled(true);
                showLoaderAnimation(false);

                String message = sendOption == 2 ? "Email is not sent." : "Data is not submitted. Please contact Hutch Support.";
                if (result != null && !result.isEmpty()) {
                    JSONObject response = new JSONObject(result);

                    int id = response.getInt("Id");
                   // id ==0 means success
                    // id == -1 means Failure
                    if (id == 0 )
                    {
                        message = sendOption == 2 ? "Email is sent successfully." : "Data is submitted successfully";
                        Utility.savePreferences("data_transfer", Utility.getCurrentDate());

                        dismiss();
                    }else
                    {
                        message = response.getString("Message");
                    }

                }


                Utility.showMsg(message);

            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    public static void writeFile(String data, String filePath) {

        try {

            File f = new File(filePath);

            FileOutputStream fileout = new FileOutputStream(f, true);
            OutputStreamWriter outputWriter = new OutputStreamWriter(fileout);
            outputWriter.write(data);
            outputWriter.close();
            fileout.close();

        } catch (Exception exe) {

        }
    }
}
