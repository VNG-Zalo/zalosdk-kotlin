package com.zing.zalo.zalosdk.common;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Build;
import android.os.Bundle;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnTouchListener;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;

import com.zing.zalo.zalosdk.java.payment.direct.PaymentAlertDialog;
import com.zing.zalo.zalosdk.kotlin.core.log.Log;

public class Utils {

    public static void showAlertDialog(Context context, String mess, PaymentAlertDialog.OnOkListener listener) {
        PaymentAlertDialog alertDlg = new PaymentAlertDialog(context, listener);
        alertDlg.showAlert(mess);

    }

    public static boolean isPermissionGranted(Context context, String permission) {
        int permissionCheck = -1;
        if (Build.VERSION.SDK_INT >= 23) {
            java.lang.reflect.Method method;
            try {
                method = context.getClass().getMethod("checkSelfPermission", String.class);
                if (method != null) {
                    permissionCheck = (Integer) method.invoke(context, permission);
                } else {
                    permissionCheck = context.getPackageManager().checkPermission(permission, context.getPackageName());
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        } else {
            permissionCheck = context.getPackageManager().checkPermission(permission, context.getPackageName());
        }
        if (permissionCheck == PackageManager.PERMISSION_GRANTED) {
            return true;
        }
        return false;
    }

    public static void hideSoftKeyboard(Activity activity) {
        if (activity != null) {
            InputMethodManager inputMethodManager = (InputMethodManager) activity
                    .getSystemService(Activity.INPUT_METHOD_SERVICE);
            if (inputMethodManager != null) {
                View focus = activity.getCurrentFocus();
                if (focus != null)
                    inputMethodManager.hideSoftInputFromWindow(focus.getWindowToken(),
                            InputMethodManager.HIDE_NOT_ALWAYS);
                else
                    activity.getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN);
            }

        }
    }

    public static void setupUIHideKeyBoard(final Activity activity, View view) {

        // Set up touch listener for non-text box views to hide keyboard.
        if (!(view instanceof EditText)) {

            view.setOnTouchListener(new OnTouchListener() {

                @SuppressLint("ClickableViewAccessibility")
                public boolean onTouch(View v, MotionEvent event) {
                    Utils.hideSoftKeyboard(activity);
                    return false;
                }

            });
        }

        // If a layout container, iterate over children and seed recursion.
        if (view instanceof ViewGroup) {

            for (int i = 0; i < ((ViewGroup) view).getChildCount(); i++) {

                View innerView = ((ViewGroup) view).getChildAt(i);

                setupUIHideKeyBoard(activity, innerView);
            }
        }
    }

    public static boolean isOnline(Context ctx) {
        try {
            if (!isPermissionGranted(ctx, Manifest.permission.ACCESS_WIFI_STATE)) return true;
            ConnectivityManager cm = (ConnectivityManager) ctx.getSystemService(Context.CONNECTIVITY_SERVICE);
            @SuppressLint("MissingPermission") NetworkInfo netInfo = cm.getActiveNetworkInfo();
            if (netInfo != null && netInfo.isConnectedOrConnecting()) {
                return true;
            }
        } catch (Exception e) {
            return true;
        }

        return false;
    }

    /**
     * Utils.getResourceId(this, "zalosdk_activity_zalo_web_login", "layout")
     *
     * @param context
     * @param variableName
     * @param resourcename
     * @return
     */

    public static int getResourceId(Context context, String variableName, String resourcename) {
        try {
            return context.getResources().getIdentifier(variableName, resourcename, context.getPackageName());
        } catch (Exception e) {
            Log.e("getResourceId", e);
            return 0;
        }
    }


    public static String getFacebookAppId(Context ctx) {
        PackageManager pm = ctx.getPackageManager();
        String packageName = ctx.getPackageName();
        try {
            PackageInfo pInfo = pm.getPackageInfo(packageName, PackageManager.GET_PERMISSIONS);
            ApplicationInfo appInfo = pm.getApplicationInfo(packageName, PackageManager.GET_META_DATA);
            Bundle bundle = appInfo.metaData;
            return getStringValue(bundle, "com.facebook.sdk.ApplicationId");
        } catch (PackageManager.NameNotFoundException e) {
            Log.e("getFacebookAppId", e);
        }

        return "";
    }

    private static String getStringValue(Bundle bundle, String key) {
        try {
            if (bundle.containsKey(key)) {
                return bundle.getString(key);
            }
        } catch (Exception ex) {
            Log.v("getStringValue", ex.toString());
        }
        return "";
    }

}
