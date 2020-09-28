package com.hutchsystems.hutchconnect.scanapi;

/*import com.dynamsoft.camerasdk.exception.DcsCameraNotAuthorizedException;
import com.dynamsoft.camerasdk.exception.DcsException;
import com.dynamsoft.camerasdk.exception.DcsValueNotValidException;
import com.dynamsoft.camerasdk.exception.DcsValueOutOfRangeException;
import com.dynamsoft.camerasdk.io.DcsPNGEncodeParameter;
import com.dynamsoft.camerasdk.model.DcsDocument;
import com.dynamsoft.camerasdk.model.DcsImage;
import com.dynamsoft.camerasdk.view.DcsDocumentEditorView;
import com.dynamsoft.camerasdk.view.DcsDocumentEditorViewListener;
import com.dynamsoft.camerasdk.view.DcsVideoView;
import com.dynamsoft.camerasdk.view.DcsVideoViewListener;
import com.dynamsoft.camerasdk.view.DcsView;*/
import androidx.appcompat.app.AppCompatActivity;

public class ScanActivityDynmSoftSdk extends AppCompatActivity {
/*
    private DcsView dcsView;
    private TextView tvTitle;
    private TextView tvShow;
    private static final int CAMERA_OK = 10;
    private static final int REQUEST_EXTERNAL_STORAGE = 1;
    private static String[] PERMISSIONS_STORAGE = {
            "android.permission.READ_EXTERNAL_STORAGE",
            "android.permission.WRITE_EXTERNAL_STORAGE"};
    ArrayList<String> path = new ArrayList<>();
    private View progressView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        try {


            setContentView(R.layout.activity_scan_dynmsoftsdk);
            // set the licence number
            try {
             //  DcsView.setLicense(getApplicationContext(), "C7B8171720BD576F13436A003D1673C8A08A457E");
               DcsView.setLicense(getApplicationContext(), "0C3AE0382070170538FA323860E3F48D94E33CD5");

            } catch (DcsValueNotValidException e) {
                e.printStackTrace();
            }

            // intialization of view
            dcsView = (DcsView) findViewById(R.id.dcsview_id);
            progressView = findViewById(R.id.progressBar);

            //Set the current view.
            dcsView.setCurrentView(DcsView.DVE_VIDEOVIEW);


            try {
                //Set the current mode as document
                dcsView.getVideoView().setMode(DcsView.DME_DOCUMENT);
            } catch (DcsValueOutOfRangeException e) {
                e.printStackTrace();
            }
          //Set the next view after the cancel button is clicked on the video view.
            dcsView.getVideoView().setNextViewAfterCancel(DcsView.DVE_VIDEOVIEW);

        //    Set the next view after the capture button is clicked on the video view.
            dcsView.getVideoView().setNextViewAfterCapture(dcsView.DVE_EDITORVIEW);


            //  Set the next view after the ok button is clicked on the edtior
            dcsView.getDocumentEditorView().setNextViewAfterOK(dcsView.DVE_EDITORVIEW);



            dcsView.getVideoView().setListener(new DcsVideoViewListener() {
                @Override
                public boolean onPreCapture(DcsVideoView dcsVideoView) {
                    return true;
                }

                @Override
                public void onCaptureFailure(DcsVideoView dcsVideoView, DcsException e) {

                }

                @Override
                public void onPostCapture(DcsVideoView dcsVideoView, DcsImage dcsImage) {

                    // For hiding of spinner
                    waitSpinnerInvisible();
                    Log.d(ScanActivityDynmSoftSdk.class.getName(), "onPostCapture");

                }

                @Override
                public void onCancelTapped(DcsVideoView dcsVideoView) {
                        finish();

                }

                @Override
                public void onCaptureTapped(DcsVideoView dcsVideoView) {

                    // For showing  spinner
                    waitSpinnerVisible();
                    Log.d(ScanActivityDynmSoftSdk.class.getName(), "onCaptureTapped");


                }

                @Override
                public void onDocumentDetected(DcsVideoView dcsVideoView, DcsDocument dcsDocument) {

                }
            });
            dcsView.getDocumentEditorView().setListener(new DcsDocumentEditorViewListener() {
                @Override
                public void onCancelTapped(DcsDocumentEditorView dcsDocumentEditorView) {
                    Log.d(ScanActivityDynmSoftSdk.class.getName(), "onCancal");

                    dcsView.getDocumentEditorView().setNextViewAfterCancel(DcsView.DVE_VIDEOVIEW);
                }

                @Override
                public void onOkTapped(DcsDocumentEditorView dcsDocumentEditorView, DcsException e) {
                    Log.d(ScanActivityDynmSoftSdk.class.getName(), "onOkTapped");

                  try{
                      // For Save image in to local storage
                      new LoadImage(Utility.context).execute();
                  }catch (Exception exeception)
                  {
                      exeception.printStackTrace();
                  }



                }
            });

            requestPermissions();
        } catch (Exception e) {
            e.printStackTrace();
        }

    }



    @Override
    protected void onResume() {
        super.onResume();
        if (dcsView.getCurrentView() == DcsView.DVE_VIDEOVIEW) {
            try {
                dcsView.getVideoView().preview();
            } catch (DcsCameraNotAuthorizedException e) {
                e.printStackTrace();
            }
            //  saveImage(dcsView);
        }
    }

    @Override
    protected void onStart() {
        super.onStart();
        requestPermissions();
        if (dcsView.getCurrentView() == DcsView.DVE_VIDEOVIEW) {
            try {
                dcsView.getVideoView().preview();
            } catch (DcsCameraNotAuthorizedException e) {
                e.printStackTrace();
            }
        }
    }

    @Override
    protected void onStop() {
        super.onStop();
        dcsView.getVideoView().stopPreview();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        dcsView.getVideoView().destroyCamera();
    }



    private void requestPermissions() {
        if (Build.VERSION.SDK_INT > 22) {
            try {
                if (ContextCompat.checkSelfPermission(ScanActivityDynmSoftSdk.this, "android.permission.WRITE_EXTERNAL_STORAGE") != PackageManager.PERMISSION_GRANTED) {
                    ActivityCompat.requestPermissions(ScanActivityDynmSoftSdk.this, PERMISSIONS_STORAGE, REQUEST_EXTERNAL_STORAGE);
                }
                if (ContextCompat.checkSelfPermission(ScanActivityDynmSoftSdk.this, android.Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
                    ActivityCompat.requestPermissions(ScanActivityDynmSoftSdk.this, new String[]{android.Manifest.permission.CAMERA}, CAMERA_OK);
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        } else {
            // do nothing
        }
    }

    // Created By: Pallavi Wattamwar
    // Created Date: 1 Feb 2019
    // Purpose: for creating pdf
    public void createPdf() {
        try {
            Intent intent = getIntent();
            String fileName = intent.getExtras().getString(MediaStore.EXTRA_OUTPUT);
            File myFile = new File(fileName);

            Image img = Image.getInstance(path.get(0));
            Document document = new Document(img);
            PdfWriter.getInstance(document, new FileOutputStream(myFile));
            document.open();
            for (String image : path) {
                img = Image.getInstance(image);
                document.setPageSize(img);
                document.newPage();
                img.setAbsolutePosition(0, 0);
                document.add(img);
            }
            document.close();
            for (String image : path) {
                try {
                    new File(image).delete();
                } catch (Exception exe) {
                    Log.i(ScanActivityDynmSoftSdk.class.getName(), exe.getMessage());
                }
            }


        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    // Created By: Pallavi Wattamwar
    // Created Date: 1 Feb 2019
    // Purpose: save image
    public void saveImage()
    {

        String folderName = "scan";
        String fileName = "";
        File folder = new File(Environment.getExternalStorageDirectory().toString()
                + "/" + folderName);
        if (!folder.exists()) {
            folder.mkdirs();
            Log.d(MainActivity.class.getName(), "wrote: created folder " + folder.getPath());
        }
        fileName = Environment.getExternalStorageDirectory().toString()
                + "/" + folderName + "/DOC-"
                + Utility.getCurrentDateTime()
                + ".jpg";

        dcsView.getIO().save(new int[]{dcsView.getBuffer().getCurrentIndex()}, fileName, new DcsPNGEncodeParameter());
        path.add(fileName);
        BitmapUtility.compressAndSaveBitmap(fileName, 75);
        try {
            createPdf();
        } catch (Exception e1) {
            e1.printStackTrace();
        }
    }

    // Created By: Pallavi Wattamwar
    // Created Date: 1 Feb 2019
    // Purpose: save image in to device and genrate pdf
    class LoadImage extends AsyncTask<String, String, Boolean> {
        Context context;
        ProgressDialog progDailog;

        public LoadImage(Context context) {
            this.context = context;
        }

        @Override
        protected void onPreExecute() {
            super.onPreExecute();


            waitSpinnerVisible();
        }
        @Override
        protected Boolean doInBackground(String... aurl) {
            try
            {
                saveImage();
            }catch (Exception e)
            {
                e.printStackTrace();
                return false;
            }

            return true;
        }
        @Override
        protected void onPostExecute(Boolean result) {
            super.onPostExecute(result);
            waitSpinnerInvisible();
            Log.d(ScanActivityDynmSoftSdk.class.getName(), "onOkTapped done");
            setResult(RESULT_OK, getIntent());
            finish();
        }
    }
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        try {
            DcsView.setLicense(getApplicationContext(), "your license number");
        } catch (DcsValueNotValidException e) {
            e.printStackTrace();
        }
    }
    // Created By: Pallavi Wattamwar
    // Created Date: 1 Feb 2019
    // Purpose: set progressview visible
    public void waitSpinnerVisible() {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                progressView.setVisibility(View.VISIBLE);
            }
        });

    }

    // Created By: Pallavi Wattamwar
    // Created Date: 1 Feb 2019
    // Purpose: set progressview invisible
    public void waitSpinnerInvisible() {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                progressView.setVisibility(View.GONE);

            }
        });


    }*/
}
