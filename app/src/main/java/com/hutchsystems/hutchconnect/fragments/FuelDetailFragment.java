package com.hutchsystems.hutchconnect.fragments;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.ColorMatrix;
import android.graphics.ColorMatrixColorFilter;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.media.ExifInterface;
import android.net.Uri;
import android.os.Bundle;
import android.os.StrictMode;
import android.provider.MediaStore;

import androidx.core.content.FileProvider;
import androidx.fragment.app.Fragment;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RadioButton;
import android.widget.Spinner;
import android.widget.TextView;

import com.hutchsystems.hutchconnect.MainActivity;
import com.hutchsystems.hutchconnect.R;
import com.hutchsystems.hutchconnect.beans.CardBean;
import com.hutchsystems.hutchconnect.beans.FuelDetailBean;
import com.hutchsystems.hutchconnect.beans.UserBean;
import com.hutchsystems.hutchconnect.common.BarCodeGenerator;
import com.hutchsystems.hutchconnect.common.BitmapUtility;
import com.hutchsystems.hutchconnect.common.DocumentType;
import com.hutchsystems.hutchconnect.common.Utility;
import com.hutchsystems.hutchconnect.db.FuelDetailDB;
import com.hutchsystems.hutchconnect.scanapi.ScanActivity;
import com.hutchsystems.hutchconnect.services.AutoStartService;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;

public class FuelDetailFragment extends Fragment implements View.OnClickListener {
    TextView tvDriver, tvPowerUnit, tvTrailer, tvDuration;
    EditText etQuantity, etPrice, etDEFFuelQuantity, etDEFFuelPrice, etReeferFuelQuantity, etReeferFuelPrice, etCashAdvance;
    Spinner spFuelCardNo;
    Button btnAttach;
    ImageButton btnSave;
    final int DEFECT_SELECTION_CODE = 1;
    final int TAKE_PHOTO_CODE = 0;
    final int VIEW_PHOTO = 2;
    RadioButton rbLitre, rbGallon;
    String fuelDateTime;
    public int id = 0, cardId = 0;

    private OnFragmentInteractionListener mListener;
    private Bitmap scaleBitmap;
    ImageView ivAddImage, ivDelete;
    private ViewImageDialog viewImageDialog;

    public FuelDetailFragment() {
        // Required empty public constructor
    }

    public static FuelDetailFragment newInstance() {
        FuelDetailFragment fragment = new FuelDetailFragment();
        return fragment;
    }


    public static FuelDetailFragment newInstance(int id) {
        FuelDetailFragment fragment = new FuelDetailFragment();
        Bundle args = new Bundle();
        args.putInt("id", id);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            id = getArguments().getInt("id");
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_fuel_detail, container, false);
        initialize(view);
        return view;
    }

    int REQUEST_CODE = 99;
    private ImageView imgScanned;
    String[] trailers = {"", ""};

    private void initialize(View view) {
        rbLitre = (RadioButton) view.findViewById(R.id.rbLitre);
        rbGallon = (RadioButton) view.findViewById(R.id.rbGallon);

        if (Utility._appSetting.getUnit() == 2) {
            rbGallon.setChecked(true);
        } else {
            rbLitre.setChecked(true);
        }

        ivAddImage = (ImageView) view.findViewById(R.id.ivAddImage);
        imgScanned = (ImageView) view.findViewById(R.id.imgScanned);
        ivDelete = (ImageView) view.findViewById(R.id.ivDelete);
        tvDriver = (TextView) view.findViewById(R.id.tvDriver);
        tvPowerUnit = (TextView) view.findViewById(R.id.tvPowerUnit);
        tvTrailer = (TextView) view.findViewById(R.id.tvTrailer);
        tvDuration = (TextView) view.findViewById(R.id.tvDuration);
        spFuelCardNo = (Spinner) view.findViewById(R.id.spFuelCardNo);
        /*spFuelCardNo.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                cardId = cardList.get(i).getCardId();

            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {

            }
        });*/
        etQuantity = (EditText) view.findViewById(R.id.etQuantity);
        etPrice = (EditText) view.findViewById(R.id.etPrice);
        etDEFFuelQuantity = (EditText) view.findViewById(R.id.etDEFFuelQuantity);
        etDEFFuelPrice = (EditText) view.findViewById(R.id.etDEFFuelPrice);
        etReeferFuelQuantity = (EditText) view.findViewById(R.id.etReeferFuelQuantity);
        etReeferFuelPrice = (EditText) view.findViewById(R.id.etReeferFuelPrice);
        etCashAdvance = (EditText) view.findViewById(R.id.etCashAdvance);
        btnAttach = (Button) view.findViewById(R.id.btnAttach);
        ivAddImage.setOnClickListener(this);
        btnSave = (ImageButton) view.findViewById(R.id.btnSave);
        btnAttach.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                if (attachedFg) {
                    if (!_fileName.isEmpty()) {
                        File file = new File(_fileName);

                        if (file.exists()) {
                            Uri path = FileProvider.getUriForFile(getContext(), getContext().getApplicationContext().getPackageName() + ".provider", file);

                            //Uri path = Uri.fromFile(file);
                            Intent intent = new Intent(Intent.ACTION_VIEW);
                            intent.setDataAndType(path, "application/pdf");
                            intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                            intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
                            startActivity(intent);
                        }
                    }
                } else {

                    _fileName = Utility.GetDocumentName(DocumentType.DOCUMENT_FUEL);
                    // if Flag_Scan_Dynmsoft = true Document Scan using dynmsoft sdk

                    Intent intent = new Intent(getContext(), ScanActivity.class);
                    intent.putExtra(MediaStore.EXTRA_OUTPUT, _fileName);
                    startActivityForResult(intent, REQUEST_CODE);

                }
            }
        });

        btnSave.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                cardId = cardList.get(spFuelCardNo.getSelectedItemPosition()).getCardId();
                if (isValidate()) {
                    FuelDetailBean bean = new FuelDetailBean();
                    bean.set_id(id);
                    bean.setVehicleId(Utility.vehicleId);
                    bean.setDriverId(Utility.onScreenUserId);
                    bean.setDuration(duration);
                    bean.setQuantity(etQuantity.getText().toString());
                    bean.setDEFFuelQuantity(etDEFFuelQuantity.getText().toString());
                    bean.setReferFuelQuantity(etReeferFuelQuantity.getText().toString());
                    /*if (cardId == 0) {
                        cardId = cardList.get(spFuelCardNo.getSelectedItemPosition()).getCardId();
                    }*/
                    bean.setCardId(cardId);
                    bean.setFuelDateTime(fuelDateTime);
                    bean.setPrice(etPrice.getText().toString());
                    bean.setDEFFuelPrice(etDEFFuelPrice.getText().toString());
                    bean.setReferFuelPrice(etReeferFuelPrice.getText().toString());
                    bean.setCashAdvance(etCashAdvance.getText().toString());
                    bean.setTrailerNo(trailers[1]);
                    bean.setFuelUnit(rbLitre.isChecked() ? 1 : 2);
                    if (attachedFg) {
                        String filename = _fileName.substring(_fileName.lastIndexOf("/") + 1);
                        bean.setImages(filename);
                    } else
                        bean.setImages("");

                    boolean status = FuelDetailDB.Save(bean);
                    if (status && mListener != null) {
                        // If annotaion is selected as Fueling after finishing fueling form open Daily Log Screen
                        if (MainActivity.iSFueling) {
                            mListener.finishFuelDetail();
                        } else {
                            mListener.onLoadFuelDetail();
                        }
                        if (attachedFg)
                            mListener.onUploadDocument(DocumentType.DOCUMENT_FUEL, _fileName);
                    }

                }
            }
        });

        UserBean user;
        if (Utility.onScreenUserId == Utility.user1.getAccountId()) {
            user = Utility.user1;
        } else
            user = Utility.user2;
        tvDriver.setText(getString(R.string.driver) + ": " + user.getFirstName() + " " + user.getLastName());
        tvPowerUnit.setText(getString(R.string.power_unit) + ": " + Utility.UnitNo);
        // tvTrailer.setText(getString(R.string.trailer) + ": " + Utility.TrailerNumber);

        if (id > 0) {

            FuelDetailBean bean = FuelDetailDB.getFuelDetailById(id);
            fuelDateTime = bean.getFuelDateTime();
            duration = bean.getDuration();
            etQuantity.setText(bean.getQuantity());
            cardId = bean.getCardId();
            etDEFFuelQuantity.setText(bean.getDEFFuelQuantity());
            etReeferFuelQuantity.setText(bean.getReferFuelQuantity());
            etPrice.setText(bean.getPrice());

            etDEFFuelPrice.setText(bean.getDEFFuelPrice());
            etReeferFuelPrice.setText(bean.getReferFuelPrice());
            etCashAdvance.setText(bean.getCashAdvance());
            if (bean.getImages().isEmpty()) {
                attachedFg = false;


                attachedFg = false;
                btnAttach.setEnabled(false);
                btnAttach.setText(R.string.no_document_attach);

                imgScanned.setVisibility(View.GONE);
                ivDelete.setVisibility(View.GONE);
            } else {


                _fileName = Utility.GetDocumentFullPath(DocumentType.DOCUMENT_FUEL, bean.getImages());
                btnAttach.setText(R.string.view_document);
                attachedFg = true;


                if (!_fileName.isEmpty()) {
                    scaleBitmap = BitmapUtility.decodeSampledBitmapFromFile(_fileName, 128, 72);
                    imgScanned.setImageBitmap(scaleBitmap);
                    imgScanned.setVisibility(View.VISIBLE);
                    ivDelete.setVisibility(View.VISIBLE);
                } else {
                    imgScanned.setVisibility(View.GONE);
                    ivDelete.setVisibility(View.GONE);
                }

            }
            tvDuration.setText(Utility.getTimeFromMinute(duration));
            trailers = new String[]{"", bean.getTrailerNo()}; //TrailerDB.getTrailers(bean.getTrailerNo());
            rbGallon.setChecked(bean.getFuelUnit() == 2);
            rbLitre.setChecked(bean.getFuelUnit() == 1);
        } else {

            fuelDateTime = Utility.getCurrentDateTime();
            duration = 0;
            trailers = new String[]{"", Utility.TrailerNumber};
            startThread();
        }
        tvTrailer.setText(getString(R.string.trailer) + ": " + trailers[1]);
        populateCard();

        imgScanned.setOnClickListener(this);
        ivDelete.setOnClickListener(this);
    }

    ArrayList<CardBean> cardList;

    private void populateCard() {
        try {
            cardList = FuelDetailDB.CardGet();
            boolean cashCardFg = (Utility.getPreferences("cashcard", "0").equals("1"));
            if (!cashCardFg) {
                CardBean bean = new CardBean();
                bean.setCardId(0);
                bean.setCardNo("Cash");
                bean.setVehicleId(Utility.vehicleId);
                cardList.add(0, bean);
                FuelDetailDB.Save(cardList);
                Utility.savePreferences("cashcard", "1");
            }

            ArrayList<String> data = new ArrayList<>();
            int selected = 0;
            for (int i = 0; i < cardList.size(); i++) {
                CardBean bean = cardList.get(i);
                data.add(bean.getCardNo());
                if (bean.getCardId() == cardId) {
                    selected = i;
                }
            }

            //ArrayAdapter<CardBean> adapter = new ArrayAdapter<>(getContext(), R.layout.spinner_item, cardList);
            ArrayAdapter<String> adapter = new ArrayAdapter<>(getContext(), R.layout.spinner_item, data);
            spFuelCardNo.setAdapter(adapter);
            spFuelCardNo.setSelection(selected);
        } catch (Exception exe) {
            Log.i("error", exe.getMessage());
        }
    }

    int duration = 0;
    private Thread thVehicleInfo;

    private void startThread() {
        if (thVehicleInfo != null) {
            thVehicleInfo.interrupt();
            thVehicleInfo = null;
        }
        duration = 0;
        thVehicleInfo = new Thread(new Runnable() {
            @Override
            public void run() {
                while (true) {
                    try {
                        if (Thread.interrupted())
                            break;
                        getActivity().runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                tvDuration.setText(Utility.getTimeFromMinute(duration));
                            }
                        });
                        Thread.sleep(1000 * 60);
                        duration++;
                    } catch (Exception exe) {
                        break;
                    }
                }
            }
        });

        thVehicleInfo.setName("thVehicleInfo");
        thVehicleInfo.start();
    }

    private void stopThread() {
        if (thVehicleInfo != null) {
            thVehicleInfo.interrupt();
            thVehicleInfo = null;
        }
    }

    private boolean isEmpty(EditText et) {
        return et.getText().toString().isEmpty();
    }

    private boolean isValidate() {
        boolean status = true;
        String message = "";
        if (cardId == -1) {
            status = false;
            message = getString(R.string.fuel_card_alert);
        }/* else if (isEmpty(etQuantity) && isEmpty(etPrice)) {
            status = false;
            //message = getString(R.string.fuel_quantity_alert);
            message = getString(R.string.fuel_price_alert);

        } else if (!isEmpty(etDEFFuelQuantity) && isEmpty(etDEFFuelPrice)) {

            status = false;
            message = getString(R.string.def_fuel_alert);
        } else if (!isEmpty(etReeferFuelQuantity) && isEmpty(etReeferFuelPrice)) {

            status = false;
            message = getString(R.string.reefer_fuel_price_alert);
        } */ else if ((isEmpty(etQuantity) && isEmpty(etDEFFuelQuantity) && isEmpty(etReeferFuelQuantity))) {
            status = false;
            message = "Please enter quantity for at least one field";
        }

        if (!status)
            Utility.showMsg(message);
        return status;
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
        stopThread();
    }

    @Override
    public void onClick(View v) {

        switch (v.getId()) {

            case R.id.ivAddImage:

                final String dir = Utility.GetDocFullPath(DocumentType.DOCUMENT_FUEL);
                File newdir = new File(dir);
                if (!newdir.exists()) {
                    newdir.mkdirs();
                }


                // Here, the counter will be incremented each time, and the
                // picture taken by camera will be stored as 1.jpg,2.jpg
                // and likewise.

                _fileName = newdir + "/" + Utility.getStringTime(Utility.getCurrentDateTime()) + ".jpg";
                File newfile = new File(_fileName);
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
               /* if (attachedFg) {
                    if (!_fileName.isEmpty()) {
                        File file = new File(_fileName);

                        if (file.exists()) {
                            Uri path = Uri.fromFile(file);
                            Intent intent = new Intent(Intent.ACTION_VIEW);
                            intent.setDataAndType(path, "application/pdf");
                            intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                            startActivity(intent);
                        }
                    }
                } else {


                    _fileName = Utility.GetDocumentName(DocumentType.DOCUMENT_FUEL);


                }*/

                break;
            case R.id.imgScanned:
                if (attachedFg) {
                    //view image
                    if (viewImageDialog == null) {
                        viewImageDialog = new ViewImageDialog();
                    }
                    if (viewImageDialog.isAdded()) {
                        viewImageDialog.dismiss();
                    }
                    viewImageDialog.setImagePath(_fileName, true);
                    viewImageDialog.show(getFragmentManager(), "viewImage_dialog");
                }
                break;

            case R.id.ivDelete:
                //delete image
                scaleBitmap = null;
                attachedFg = false;
                _fileName = "";
                imgScanned.setVisibility(View.GONE);
                ivDelete.setVisibility(View.GONE);
                break;
        }
    }

    public interface OnFragmentInteractionListener {
        // TODO: Update argument type and name
        void onFragmentInteraction(Uri uri);

        void onLoadFuelDetail();

        void finishFuelDetail();

        void onUploadDocument(String type, String path);
    }

    boolean attachedFg = false;
    String _fileName = "";

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == REQUEST_CODE && resultCode == Activity.RESULT_OK) {

            try {

                _fileName = data.getExtras().getString(MediaStore.EXTRA_OUTPUT);
                if (!_fileName.isEmpty()) {
                    attachedFg = true;
                    btnAttach.setText(getString(R.string.view_document));

                    /*if (attachedFg)
                        mListener.onUploadDocument(DocumentType.DOCUMENT_FUEL, _fileName);*/
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        } else if (requestCode == TAKE_PHOTO_CODE && resultCode == Activity.RESULT_OK) {


            // isBlackWhite true, then save and upload black and white
            BitmapUtility.compressAndSaveBitmap(_fileName, true);
            getCameraPhotoOrientation(getContext(), null, _fileName);
            //Configuration config = getResources().getConfiguration();
            //scaleBitmap = BitmapUtility.decodeSampledBitmapFromFileWithOrientation(_fileName, 128, 72, config.orientation);


            scaleBitmap = BitmapUtility.decodeSampledBitmapFromFile(_fileName, 128, 72);

            attachedFg = true;
            btnAttach.setText("View image");
            imgScanned.setVisibility(View.VISIBLE);
            ivDelete.setVisibility(View.VISIBLE);

            imgScanned.setImageBitmap(scaleBitmap);
            imgScanned.setScaleType(ImageView.ScaleType.FIT_XY);

            //add delete button for current captured image
            Button butDelete = new Button(getActivity());
            butDelete.setText("Delete");

            LinearLayout layout = new LinearLayout(getActivity());
            layout.setOrientation(LinearLayout.VERTICAL);


        }


    }

    public static Bitmap createBlackAndWhite(Bitmap src) {
        int width = src.getWidth();
        int height = src.getHeight();

        Bitmap bmOut = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(bmOut);
        ColorMatrix ma = new ColorMatrix();
        ma.setSaturation(0);
        Paint paint = new Paint();
        paint.setColorFilter(new ColorMatrixColorFilter(ma));
        canvas.drawBitmap(src, 0, 0, paint);

        Matrix mat = new Matrix();
        mat.postRotate(90);
        Bitmap bMapRotate = Bitmap.createBitmap(bmOut, 0, 0, bmOut.getWidth(), bmOut.getHeight(), mat, true);
        bmOut.recycle();

        return bMapRotate;
    }

    public int getCameraPhotoOrientation(Context context, Uri imageUri, String imagePath) {
        int rotate = 0;
        try {
            //context.getContentResolver().notifyChange(imageUri, null);
            File imageFile = new File(imagePath);

            ExifInterface exif = new ExifInterface(imageFile.getAbsolutePath());
            int orientation = exif.getAttributeInt(ExifInterface.TAG_ORIENTATION, ExifInterface.ORIENTATION_NORMAL);

            switch (orientation) {
                case ExifInterface.ORIENTATION_ROTATE_270:
                    rotate = 270;
                    break;
                case ExifInterface.ORIENTATION_ROTATE_180:
                    rotate = 180;
                    break;
                case ExifInterface.ORIENTATION_ROTATE_90:
                    rotate = 90;
                    break;
            }

            Log.i("RotateImage", "Exif orientation: " + orientation);
            Log.i("RotateImage", "Rotate value: " + rotate);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return rotate;
    }
}
