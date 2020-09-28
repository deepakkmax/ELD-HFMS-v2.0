package com.hutchsystems.hutchconnect.common;

import android.content.Context;
import android.content.DialogInterface;

import com.hutchsystems.hutchconnect.beans.ReportBean;

public class DialogClickListener implements DialogInterface.OnClickListener {
    ReportBean data = null;
    Context context = null;

    public DialogClickListener(ReportBean data, Context context) {
        this.data = data;
        this.context = context;
    }


    public DialogClickListener() {

    }

    @Override
    public void onClick(DialogInterface dialogInterface, int response) {
        Utility.PCDialogFg = false;
        switch (response) {
            case DialogInterface.BUTTON_POSITIVE:
                // driver wants to continue personal use

                break;

            case DialogInterface.BUTTON_NEGATIVE:
                //driver don't want to continue personal use
                // clear personal use here

                break;
        }
    }
}
