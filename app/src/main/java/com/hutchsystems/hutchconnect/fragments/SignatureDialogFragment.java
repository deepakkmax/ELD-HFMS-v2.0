package com.hutchsystems.hutchconnect.fragments;

import android.app.Dialog;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.RectF;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import androidx.fragment.app.DialogFragment;
import android.util.AttributeSet;
import android.util.Base64;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.LinearLayout;

import com.hutchsystems.hutchconnect.R;
import com.hutchsystems.hutchconnect.common.LogFile;
import com.hutchsystems.hutchconnect.common.Utility;
import com.hutchsystems.hutchconnect.db.LogDB;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;

public class SignatureDialogFragment extends DialogFragment implements View.OnClickListener {
    UserSignature mSignature;
    Button btnSave, btnReset;
    LinearLayout mContent;
    ImageButton imgCancel;

    private void initialize(View view) {
        btnSave = (Button) view.findViewById(R.id.btnSave);
        btnSave.setEnabled(false);
        btnSave.setOnClickListener(this);

        btnReset = (Button) view.findViewById(R.id.btnReset);
        btnReset.setOnClickListener(this);

        imgCancel = (ImageButton) view.findViewById(R.id.imgCancel);
        imgCancel.setOnClickListener(this);
        mContent = (LinearLayout) view.findViewById(R.id.mContent);
        mSignature = new UserSignature(getActivity().getApplicationContext(), null);
        // Dynamically generating Layout through java code
        mContent.addView(mSignature);

        drawBackground();
// CTPAT Inspection after open dialoug clear signature
        try {
            if (getArguments() != null) {
                if (getArguments().getBoolean("Clear", false)) {
                    mSignature.clear();
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    @Override
    public void onStart() {
        super.onStart();

        Dialog dialog = getDialog();
        if (dialog != null) {
            dialog.getWindow().setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
            //dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        }
    }

    private void drawBackground() {
        try {
            String filePath = Utility.GetSignaturePath();
            File file = new File(filePath);
            if (file.exists()) {
                Bitmap bitmap = BitmapFactory.decodeFile(filePath);
                BitmapDrawable bitmapDrawable = new BitmapDrawable(getResources(), bitmap);
                mContent.setBackground(bitmapDrawable);
            }
        } catch (Exception exe) {
        }
    }

    public SignatureDialogFragment() {
    }

    public static SignatureDialogFragment newInstance() {
        SignatureDialogFragment fragment = new SignatureDialogFragment();

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
        View view = inflater.inflate(R.layout.fragment_signature_dialog, container, false);
        initialize(view);
        return view;
    }

    @Override
    public void onClick(View view) {
        try {
            switch (view.getId()) {
                case R.id.imgCancel:
                    dismiss();
                    break;
                case R.id.btnSave:
                    mSignature.save();
                    dismiss();
                    break;
                case R.id.btnReset:
                    mSignature.clear();
                    break;
            }
        } catch (Exception e) {
            LogFile.write(SignatureDialogFragment.class.getName() + "::onClick Error:" + e.getMessage(), LogFile.USER_INTERACTION, LogFile.ERROR_LOG);
            LogDB.writeLogs(SignatureDialogFragment.class.getName(),"onClick",e.getMessage(), Utility.printStackTrace(e));

        }
    }


    @Override
    public void onAttach(Context context) {
        super.onAttach(context);

    }

    @Override
    public void onDetach() {
        super.onDetach();
    }

    OnFragmentInteractionListener mListner;

    public interface OnFragmentInteractionListener {
        // TODO: Update argument type and name
        void onSignatureUpload(String data, String path);
    }

    public class UserSignature extends View {
        static final float STROKE_WIDTH = 10f;
        static final float HALF_STROKE_WIDTH = STROKE_WIDTH / 2;
        Paint paint = new Paint();
        Path path = new Path();

        float lastTouchX;
        float lastTouchY;
        final RectF dirtyRect = new RectF();

        public UserSignature(Context context, AttributeSet attrs) {
            super(context, attrs);
            paint.setAntiAlias(true);
            paint.setColor(Color.BLACK);
            paint.setStyle(Paint.Style.STROKE);
            paint.setStrokeJoin(Paint.Join.ROUND);
            paint.setStrokeWidth(STROKE_WIDTH);
        }

        public void clear() {
            mContent.setBackgroundColor(Color.TRANSPARENT);
            path.reset();
            invalidate();
            btnSave.setEnabled(false);
        }

        public void save() {
            try {

                Bitmap returnedBitmap = Bitmap.createBitmap(mContent.getWidth(),
                        mContent.getHeight(), Bitmap.Config.ARGB_8888);
                Canvas canvas = new Canvas(returnedBitmap);
                Drawable bgDrawable = mContent.getBackground();
                if (bgDrawable != null)
                    bgDrawable.draw(canvas);
                else
                    canvas.drawColor(Color.WHITE);
                mContent.draw(canvas);

                ByteArrayOutputStream bs = new ByteArrayOutputStream();
                returnedBitmap.compress(Bitmap.CompressFormat.PNG, 50, bs);
                byte[] byteArrayImage = bs.toByteArray();
                String data = Base64.encodeToString(byteArrayImage, Base64.DEFAULT);

                String filePath = Utility.GetSignaturePath();
                FileOutputStream out = new FileOutputStream(filePath);
                out.write(byteArrayImage);
                out.close();
                if (mListner != null) {
                    mListner.onSignatureUpload(data, filePath);
                }

            } catch (Exception exe) {
                exe.printStackTrace();
            }
        }

        @Override
        protected void onDraw(Canvas canvas) {
            // TODO Auto-generated method stub
            canvas.drawPath(path, paint);
        }

        @Override
        public boolean onTouchEvent(MotionEvent event) {
            float eventX = event.getX();
            float eventY = event.getY();
            btnSave.setEnabled(true);

            switch (event.getAction()) {
                case MotionEvent.ACTION_DOWN:
                    path.moveTo(eventX, eventY);
                    lastTouchX = eventX;
                    lastTouchY = eventY;
                    return true;

                case MotionEvent.ACTION_MOVE:

                case MotionEvent.ACTION_UP:

                    resetDirtyRect(eventX, eventY);
                    int historySize = event.getHistorySize();
                    for (int i = 0; i < historySize; i++) {
                        float historicalX = event.getHistoricalX(i);
                        float historicalY = event.getHistoricalY(i);
                        path.lineTo(historicalX, historicalY);
                    }
                    path.lineTo(eventX, eventY);
                    break;
            }

            invalidate((int) (dirtyRect.left - HALF_STROKE_WIDTH),
                    (int) (dirtyRect.top - HALF_STROKE_WIDTH),
                    (int) (dirtyRect.right + HALF_STROKE_WIDTH),
                    (int) (dirtyRect.bottom + HALF_STROKE_WIDTH));

            lastTouchX = eventX;
            lastTouchY = eventY;

            return true;
        }

        private void resetDirtyRect(float eventX, float eventY) {
            dirtyRect.left = Math.min(lastTouchX, eventX);
            dirtyRect.right = Math.max(lastTouchX, eventX);
            dirtyRect.top = Math.min(lastTouchY, eventY);
            dirtyRect.bottom = Math.max(lastTouchY, eventY);
        }
    }
}
