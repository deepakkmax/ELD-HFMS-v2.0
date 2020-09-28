package com.hutchsystems.hutchconnect.fragments;


import android.content.res.Configuration;
import android.graphics.Bitmap;
import android.os.Bundle;
import androidx.fragment.app.DialogFragment;

import android.util.DisplayMetrics;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.ImageButton;
import android.widget.ImageView;

import com.hutchsystems.hutchconnect.R;
import com.hutchsystems.hutchconnect.common.BitmapUtility;
import com.hutchsystems.hutchconnect.common.LogFile;
import com.hutchsystems.hutchconnect.common.Utility;
import com.hutchsystems.hutchconnect.db.LogDB;

public class ViewImageDialog extends DialogFragment implements View.OnClickListener{
    String TAG = ViewImageDialog.class.getName();

    ImageView imageView;
    ImageButton imgCancel;
    Bitmap bitmap;

    String path = "";
    boolean isBlackWhite;
    public ViewImageDialog(){

    }

    public void setImagePath(String imagePath,boolean isBlackWhite) {
        path = imagePath;
        this.isBlackWhite = isBlackWhite;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setStyle(DialogFragment.STYLE_NORMAL, R.style.dialog_theme);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.activity_view_image, container);
        try {

            initialize(view);
            //getDialog().setTitle("Defect Image");
            getDialog().getWindow().requestFeature(Window.FEATURE_NO_TITLE);
            this.setCancelable(false);

            if (path.equals("")) {
                dismiss();
            } else {
                if (bitmap != null) {
                    bitmap.recycle();
                    bitmap = null;
                }
                Configuration config = getResources().getConfiguration();

                DisplayMetrics displayMetrics = new DisplayMetrics();
                getActivity().getWindowManager().getDefaultDisplay().getMetrics(displayMetrics);
                int height = displayMetrics.heightPixels;
                int width = displayMetrics.widthPixels;
                //call to get bitmap with small size
                bitmap = BitmapUtility.decodeSampledBitmapFromFileWithOrientation(path, width, height, config.orientation);


               /* if(isBlackWhite){
                    bitmap = FuelDetailFragment.createBlackAndWhite(bitmap);
                }*/

                imageView.setPadding(2, 2, 2, 2);
                imageView.setImageBitmap(bitmap);
                //imageView.setScaleType(ImageView.ScaleType.FIT_XY);
                Log.i(TAG, bitmap.getWidth() + "x" + bitmap.getHeight());
            }
        } catch (Exception e) {
            LogFile.write(DefectSelectionDialog.class.getName() + "::onCreateView Error: " + e.getMessage(), LogFile.USER_INTERACTION, LogFile.ERROR_LOG);
            LogDB.writeLogs(DefectSelectionDialog.class.getName(),"onCreateView",e.getMessage(), Utility.printStackTrace(e));

        }
        return view;
    }


    @Override
    public void onResume() {
//        ViewGroup.LayoutParams params = getDialog().getWindow().getAttributes();
//        params.width =WindowManager.LayoutParams.MATCH_PARENT;
//        params.height = WindowManager.LayoutParams.MATCH_PARENT;
//        getDialog().getWindow().setAttributes((android.view.WindowManager.LayoutParams) params);

        super.onResume();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (bitmap != null) {
            bitmap.recycle();
            bitmap = null;
        }
    }

    private void initialize(View view) {
        try {
            imageView = (ImageView) view.findViewById(R.id.ivImage);
            imgCancel = (ImageButton) view.findViewById(R.id.imgCancel);
            imgCancel.setOnClickListener(this);
        } catch (Exception e) {
            LogFile.write(ViewImageDialog.class.getName() + "::initialize Error:" + e.getMessage(), LogFile.USER_INTERACTION, LogFile.ERROR_LOG);
            LogDB.writeLogs(ViewImageDialog.class.getName(),"initialize",e.getMessage(), Utility.printStackTrace(e));

        }
    }

    @Override
    public void onClick(View view) {
        try {
            switch (view.getId()) {
                case R.id.imgCancel:
                    imageView.setImageBitmap(null);
                    bitmap.recycle();
                    dismiss();
                    break;
            }
        } catch (Exception e) {
            LogFile.write(ViewImageDialog.class.getName() + "::onClick Error:" + e.getMessage(), LogFile.USER_INTERACTION, LogFile.ERROR_LOG);
        }
    }

}
