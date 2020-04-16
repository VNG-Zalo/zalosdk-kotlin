package com.zing.zalo.zalosdk.java.payment.direct;

import android.content.Context;
import android.util.Log;

import java.util.Locale;

/**
 * Created by CPU10329-local on 7/6/2017.
 */

public class Utils {

    private static String language = null;

    public static String getLanguage(Context context) {
//        Locale current = context.getResources().getConfiguration().locale;
        if (language != null) {
//            Log.i("debuglog", "AAAA----current.getLanguage() : " + current.getLanguage());
            Log.i("debuglog", "default-------locale langauge : " + Locale.getDefault().getLanguage());
            if (!Locale.getDefault().getLanguage().equalsIgnoreCase("vi")) {
                return "my";
            } else {
                return "vi";
            }
        }
        return Locale.getDefault().getLanguage();
    }

    public static void showAlertDialog(Context context, String mess, String okTile, PaymentAlertDialog.OnOkListener listener,
                                       PaymentAlertDialog.OnCancelListener cancelListener) {
        PaymentAlertDialog alertDlg = new PaymentAlertDialog(context, listener, cancelListener);
        alertDlg.setOkButtonTitle(okTile);
        alertDlg.showAlert(mess);
    }

    public static void showAlertDialog(Context context, String mess, PaymentAlertDialog.OnOkListener listener) {
        PaymentAlertDialog alertDlg = new PaymentAlertDialog(context, listener);
        alertDlg.showAlert(mess);

    }


    public static void setLanguage(Context context, String _language) {
        language = _language;
    }
}
