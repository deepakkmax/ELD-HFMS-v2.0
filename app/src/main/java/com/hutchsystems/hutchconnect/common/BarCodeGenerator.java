package com.hutchsystems.hutchconnect.common;

import android.app.Dialog;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.view.View;
import android.view.Window;
import android.widget.ImageView;
import android.widget.TextView;

import com.google.zxing.BarcodeFormat;
import com.google.zxing.EncodeHintType;
import com.google.zxing.WriterException;
import com.google.zxing.common.BitMatrix;
import com.google.zxing.qrcode.QRCodeWriter;
import com.google.zxing.qrcode.decoder.ErrorCorrectionLevel;
import com.hutchsystems.hutchconnect.R;

import java.util.Hashtable;

public class BarCodeGenerator {



    // generate qrcode
    public static void generateQrCode(Context context, String title, String documentType, String id,String date)  {
        String myCodeText = getFormat(documentType,id,date);

        //Toast.makeText(context,myCodeText,Toast.LENGTH_LONG).show();

        Hashtable<EncodeHintType, ErrorCorrectionLevel> hintMap = new Hashtable<EncodeHintType, ErrorCorrectionLevel>();
        hintMap.put(EncodeHintType.ERROR_CORRECTION, ErrorCorrectionLevel.H); // H = 30% damage

        QRCodeWriter qrCodeWriter = new QRCodeWriter();

        int size = 256;

        BitMatrix bitMatrix = null;
        try {
            bitMatrix = qrCodeWriter.encode(myCodeText, BarcodeFormat.QR_CODE, size, size, hintMap);
        } catch (WriterException e) {
            e.printStackTrace();
        }
        int width = bitMatrix.getWidth();
        Bitmap bmp = Bitmap.createBitmap(width, width, Bitmap.Config.RGB_565);
        for (int x = 0; x < width; x++) {
            for (int y = 0; y < width; y++) {

                bmp.setPixel(y, x,  bitMatrix.get(x, y)? Color.BLACK : Color.WHITE);
            }
        }
        showDialog(context,bmp,title);
    }
    //get text format for generate the qr code
    public static String getFormat(String documentType,String id,String date){
        String str;
        str = GetDocumentFileName(documentType,id,date)+","+documentType+","+Utility.companyId;
        return str;

    }
    public static String GetDocumentFileName(String type, String value,String date) {

        return "/DOC-" + type + "-" + value + "-"
                + Utility.format(date, CustomDateFormat.dt7) // time stamp
                + ".pdf";
    }
    public static  void showDialog(Context context, Bitmap bmp, String title){
        final Dialog dialog = new Dialog(context);
        dialog.setCancelable(true);

        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialog.setCancelable(false);
        dialog.setContentView(R.layout.barcode_generator);
        dialog.getWindow().setBackgroundDrawableResource(android.R.color.transparent);
        ImageView imageView = (ImageView) dialog.findViewById(R.id.ivBarCode);
        ImageView ivClose = (ImageView) dialog.findViewById(R.id.ivBarCodeClose);

        TextView tvTitle = (TextView) dialog.findViewById(R.id.tvBarCodeTitle);
        tvTitle.setText(title);
        imageView.setImageBitmap(bmp);
        ivClose.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                dialog.dismiss();
            }
        });
        dialog.show();

    }
}

