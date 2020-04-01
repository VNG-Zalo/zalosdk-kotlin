package com.zing.zalo.zalosdk.oauth;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;

import com.zing.zalo.zalosdk.kotlin.core.helper.AppInfo;
import com.zing.zalo.zalosdk.kotlin.oauth.ZaloSDK;
import com.zing.zalo.zalosdk.kotlin.oauth.model.ErrorResponse;

import org.jetbrains.annotations.NotNull;

/**
 * Oauth authenticate callback
 */
public class OAuthCompleteListener {

    /**
     * This method will be called before SDK call a service.
     * You can show progress dialog here
     */
    public void onStartLoading() {

    }

    /**
     * This method will be called after SDK call a service
     * You can hide progress dialog here
     */
    public void onFinishLoading() {

    }

    public void onProtectAccComplete(int errorCode, String message, Dialog dialog) {

    }

    public void onSkipProtectAcc(Dialog dialog) {

    }


    /**
     * This method will be called after authenticate success
     *
     * @param response response object
     */

    public void onGetOAuthComplete(OauthResponse response) {
        onGetOAuthComplete(response.getuId(), response.getOauthCode(), response.getChannel().toString());
    }

    /**
     * This method will be called after authenticate success. Use method onGetOAuthComplete(OauthResponse response) instead
     *
     * @param uId       Zalo user's id
     * @param oauthCode The oauth code
     * @param channel   channel type: Zalo, Fb, Zing ...
     */
    @Deprecated
    public void onGetOAuthComplete(long uId, String oauthCode, String channel) {
        onGetOAuthComplete(uId, oauthCode);
    }

    /**
     * This method will be called after authenticate success. Use method onGetOAuthComplete(OauthResponse response) instead
     *
     * @param uId       Zalo user's id
     * @param oauthCode The oauth code
     */
    @Deprecated
    public void onGetOAuthComplete(long uId, String oauthCode) {

    }

    /**
     * This method would be called if user submit email protection on login form
     *
     * @param errorCode Reference {@link com.zing.zalo.zalosdk.ZaloOAuthResultCode}
     * @param errorMsg  Message return
     */
    protected void onRequestAccountProtect(int errorCode, String errorMsg) {

    }

    /**
     * This method would be called if error happen while authenticating
     *
     * @param errorCode Reference {@link com.zing.zalo.zalosdk.oauth.ZaloOAuthResultCode}
     * @param message   Error message
     */
    public void onAuthenError(int errorCode, String message) {
        onAuthenError(errorCode);
    }

    /**
     * This method would be called if error happen while authenticating
     * Should use method onAuthenError(int errorCode, String message) instead
     *
     * @param errorCode Reference {@link com.zing.zalo.zalosdk.oauth.ZaloOAuthResultCode}
     */
    @Deprecated
    public void onAuthenError(int errorCode) {

    }

    /**
     * This method would be called if error happen while authenticating
     * Should use method onAuthenError(int errorCode, String message) instead
     * @param errorCode Reference {@link com.zing.zalo.zalosdk.oauth.ZaloOAuthResultCode}
     * @param errorResponse Reference {@link com.zing.zalo.zalosdk.kotlin.oauth.model.ErrorResponse}
     */
    public void onAuthenError(int errorCode, String errorMsg,@NotNull ErrorResponse errorResponse){

    }


    /**
     * This method will be called after request permission success
     *
     * @param code Reference {@link com.zing.zalo.zalosdk.oauth.ZaloOAuthResultCode}
     */
    public void onGetPermissionData(int code) {

    }

    /**
     * This method will be called if current installed Zalo version is out of date
     *
     * @param context
     */
    @SuppressWarnings("deprecation")
    public void onZaloOutOfDate(final Context context) {
        ZaloSDK zaloSDK = new ZaloSDK(context);
        AlertDialog.Builder alertBuilder = new AlertDialog.Builder(context);
        alertBuilder.setMessage(zaloSDK.getLocalizedString().getZaloOauthOfDateMessage())
                .setPositiveButton(zaloSDK.getLocalizedString().getUpdateMessage(), new OnClickListener() {

                    @Override
                    public void onClick(DialogInterface arg0, int arg1) {
                        AppInfo.getInstance().launchMarketApp(context, "com.zing.zalo");
                    }
                });
        alertBuilder.setNegativeButton(zaloSDK.getLocalizedString().getCancelMessage(), new OnClickListener() {

            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
            }
        });
        alertBuilder.setCancelable(false).show();
    }

    /**
     * This method will be called if Zalo hasn't installed yet.
     *
     * @param context
     */
    @SuppressWarnings("deprecation")
    public void onZaloNotInstalled(final Context context) {
        ZaloSDK zaloSDK = new ZaloSDK(context);
        AlertDialog.Builder alertBuilder = new AlertDialog.Builder(context);
        alertBuilder.setMessage(zaloSDK.getLocalizedString().getZaloNotInstalledMessage())
                .setPositiveButton(zaloSDK.getLocalizedString().getInstallMessage(), new OnClickListener() {

                    @Override
                    public void onClick(DialogInterface arg0, int arg1) {
                        AppInfo.getInstance().launchMarketApp(context, "com.zing.zalo");
                    }
                });
        alertBuilder.setNegativeButton(zaloSDK.getLocalizedString().getCancelMessage(), new OnClickListener() {

            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
            }
        });
        alertBuilder.setCancelable(false).show();
    }

}
