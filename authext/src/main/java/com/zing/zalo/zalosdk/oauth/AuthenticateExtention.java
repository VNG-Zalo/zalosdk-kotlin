package com.zing.zalo.zalosdk.oauth;

import android.app.Activity;
import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.graphics.Point;
import android.graphics.Typeface;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.text.TextUtils;
import android.text.method.PasswordTransformationMethod;
import android.view.Display;
import android.view.LayoutInflater;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.TextView;

import com.google.android.gms.auth.api.Auth;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.auth.api.signin.GoogleSignInResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.zing.zalo.zalosdk.common.Utils;
import com.zing.zalo.zalosdk.facebook.DialogError;
import com.zing.zalo.zalosdk.facebook.Facebook;
import com.zing.zalo.zalosdk.facebook.FacebookError;
import com.zing.zalo.zalosdk.facebook.SessionEvents;
import com.zing.zalo.zalosdk.facebook.SessionStore;
import com.zing.zalo.zalosdk.java.LoginChannel;
import com.zing.zalo.zalosdk.java.devicetrackingsdk.DeviceTracking;
import com.zing.zalo.zalosdk.java.payment.direct.PaymentProcessingDialog;
import com.zing.zalo.zalosdk.java.servicemap.ServiceMapManager;
import com.zing.zalo.zalosdk.kotlin.core.helper.AppInfo;
import com.zing.zalo.zalosdk.kotlin.core.helper.DeviceInfo;
import com.zing.zalo.zalosdk.kotlin.core.http.HttpClient;
import com.zing.zalo.zalosdk.kotlin.core.http.HttpUrlEncodedRequest;
import com.zing.zalo.zalosdk.kotlin.core.log.Log;
import com.zing.zalo.zalosdk.kotlin.oauth.Constant;
import com.zing.zalo.zalosdk.kotlin.oauth.ZaloOAuthResultCode;
import com.zing.zalo.zalosdk.kotlin.oauth.ZaloSDK;
import com.zing.zalo.zalosdk.kotlin.oauth.helper.AuthStorage;
import com.zing.zalo.zalosdk.kotlin.oauth.model.ErrorResponse;

import org.json.JSONObject;

import java.lang.ref.WeakReference;

/**
 * Created by CPU10329-local on 6/28/2017.
 */

public class AuthenticateExtention {

    private static final String AUTH_MOBILE_GUEST_PATH = "/v2/mobile/guest";
    private static final String AUTH_MOBILE_ZING_PATH = "/v2/mobile/zing";
    private static final String AUTH_MOBILE_GOOGLE_PATH = "/v3/mobile/google";
    private static final String AUTH_MOBILE_FACEBOOK_PATH = "/v2/mobile/facebook";
    GoogleApiClient mGoogleApiClient;
    int w, mBoundedWidth = 900;
    int width, height;
    private Facebook mFacebook;
    private Context mContext;
    private ZaloSDK zaloSDK;
    private AuthStorage authStorage;
    private OAuthCompleteListener listener;

    public AuthenticateExtention(Context context) {
        mContext = context;
        authStorage = new AuthStorage(context);
        zaloSDK = new ZaloSDK(context);
    }

    //***************Guest**************
    public void authenticateWithGuest(Context context, OAuthCompleteListener listener) {
//        zaloSDK.checkInitialize();
        _authenticateWithGuest(context, listener);
    }

    protected void requestCertificateGuest(Activity context, String email, String pass, OAuthCompleteListener listener) {
        _requestCertificateGuest(context, email, pass, listener);
    }

    protected void protectAcc(Activity context, String cmnd, OAuthCompleteListener listener) {
        _protectAcc(context, cmnd, listener);
    }

    public void openProtectGuestDialog(Activity context, OAuthCompleteListener listener) {
        _openProtectGuestDialog(context, listener);
    }

    void _openProtectGuestDialog(final Activity activity, final OAuthCompleteListener listener) {
        String lastLogin = authStorage.getLastLoginChannel();
        if (TextUtils.isEmpty(lastLogin) || !"GUEST".equals(lastLogin)) {
            return;
        }
        LayoutInflater inflater = activity.getLayoutInflater();
        View dialoglayout = inflater.inflate(R.layout.protect_acc_guest, null);
        final Dialog dialog = new Dialog(activity, R.style.ProtectAccDialogTheme);
        dialog.setContentView(dialoglayout);
        dialog.setCancelable(false);
        Window window = dialog.getWindow();
        WindowManager wm = (WindowManager) activity.getSystemService(Context.WINDOW_SERVICE);
        Display display = wm.getDefaultDisplay();
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB_MR2) {
            Point size = new Point();
            display.getSize(size);
            width = size.x;
            height = size.y;
        } else {
            width = display.getWidth();
            height = display.getHeight();
        }

        if (DeviceInfo.isTablet(activity)) {
            if (width > height) {
                mBoundedWidth = (int) (width * 0.8);
            } else {
                mBoundedWidth = (int) (height * 0.8);
            }
            if (mBoundedWidth > 900) {
                mBoundedWidth = 900;
            }
        } else {
            if (width > height) {
                mBoundedWidth = height + 50;
            } else {
                mBoundedWidth = width - 40;
            }
        }
        window.setLayout(mBoundedWidth, WindowManager.LayoutParams.WRAP_CONTENT);
        final TextView email_guard = dialoglayout.findViewById(R.id.email_guard);
        final TextView pass_guard = dialoglayout.findViewById(R.id.emailPass);
        pass_guard.setTypeface(Typeface.DEFAULT);
        pass_guard.setTransformationMethod(new PasswordTransformationMethod());
        dialog.show();
        dialoglayout.findViewById(R.id.tt_continue_login).setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                if (listener != null)
                    listener.onSkipProtectAcc(dialog);
            }
        });
        dialoglayout.findViewById(R.id.submit_email_guard).setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {

                String email = email_guard.getText().toString().trim();
                String pass = pass_guard.getText().toString();

                if (TextUtils.isEmpty(email)) {
                    Utils.showAlertDialog(activity, "Chưa nhập email", null);
                } else if (TextUtils.isEmpty(pass)) {
                    Utils.showAlertDialog(activity, "Chưa nhập password", null);
                } else {
                    OAuthCompleteListener authCompleteListener = new OAuthCompleteListener() {

                        PaymentProcessingDialog progressDialog;

                        @Override
                        public void onAuthenError(int errorCode, String message) {
                            super.onAuthenError(errorCode, message);
                            if (listener != null)
                                listener.onProtectAccComplete(errorCode, message, dialog);
                        }

                        @Override
                        public void onRequestAccountProtect(final int errorCode, final String errorMsg) {
                            if (listener != null)
                                listener.onProtectAccComplete(errorCode, errorMsg, dialog);
                        }

                        @Override
                        public void onStartLoading() {
                            try {

                                progressDialog = new PaymentProcessingDialog(activity, new PaymentProcessingDialog.OnCloseListener() {
                                    @Override
                                    public void onClose() {

                                    }
                                });
                                progressDialog.setTitle("");
                                progressDialog.setCancelable(false);
//								progressDialog.setMessage("Đang xử lý");

                                if (activity != null && !activity.isFinishing()) {
                                    progressDialog.show();
                                }

                            } catch (Exception e) {
                                Log.e("onStartLoading", e);
                            }

                        }

                        @Override
                        public void onFinishLoading() {
                            try {
                                if (activity != null && !activity.isFinishing() && progressDialog.isShowing()) {
                                    progressDialog.dismiss();
                                }
                            } catch (Exception e) {
                                Log.e("onFinishLoading", e);
                            }

                        }
                    };

                    AuthenticateExtention.this.requestCertificateGuest(activity, email, pass, authCompleteListener);
                }
            }
        });
    }

    public boolean isGuestAccProtected() {
        int isGuestCert = getIsGuestCertificated();
        String loginChannel = authStorage.getLastLoginChannel();

        return !TextUtils.isEmpty(loginChannel) && "GUEST".equals(loginChannel) && isGuestCert == 1;
    }

    protected void recoveryGuest(Activity context, String email, String pass, OAuthCompleteListener listener) {
        _recoveryGuest(context, email, pass, listener);
    }

    protected void recoveryPassProtectAccountGuest(Activity context, String email, OAuthCompleteListener listener) {
        _recoveryPassGuest(context, email, listener);
    }

//    protected int getIsProtected() {
//        if (ZaloSDK.Instance != null &&
//                ZaloSDK.Instance.getBaseAppInfo() != null)
//            return ZaloSDK.Instance.getBaseAppInfo().getIsProtected();
//        return 0;
//    }

    protected int getIsGuestCertificated() {
        if (authStorage != null)
            return authStorage.getIsGuestCertificated();
        return 0;
    }

    protected String getGuestDeviceId() {
        if (authStorage != null)
            return authStorage.getGuestDeviceId();
        return "";
    }

    void _requestCertificateGuest(Context context, String email, String pass,
                                  final OAuthCompleteListener listener) {

        Context ctx = context.getApplicationContext();
        if (!Utils.isOnline(ctx)) {
            listener.onAuthenError(ZaloOAuthResultCode.RESULTCODE_UNEXPECTED_ERROR, "Mạng không ổn định, vui lòng thử lại sau");
            return;
        }

        String zgId = authStorage.getGuestDeviceId();
        RequestCertificateGuestTask task = new RequestCertificateGuestTask(ctx, email, pass, zgId, listener);
        task.execute();
    }

    void _protectAcc(Context context, String cmnd,
                     final OAuthCompleteListener listener) {

        Context ctx = context.getApplicationContext();
        if (!Utils.isOnline(ctx)) {
            listener.onAuthenError(ZaloOAuthResultCode.RESULTCODE_UNEXPECTED_ERROR, "Mạng không ổn định, vui lòng thử lại sau");
            return;
        }
        ProtectAccTask task = new ProtectAccTask(ctx, cmnd, listener);
        task.execute();

    }

    void _recoveryGuest(Context context, String email, String pass,
                        final OAuthCompleteListener listener) {

        Context ctx = context.getApplicationContext();
        if (!Utils.isOnline(ctx)) {
            listener.onAuthenError(ZaloOAuthResultCode.RESULTCODE_UNEXPECTED_ERROR, "Mạng không ổn định, vui lòng thử lại sau");
            return;
        }
        RecoveryGuestTask task = new RecoveryGuestTask(ctx, email, pass, listener);
        task.execute();
    }

    void _recoveryPassGuest(Context context, String email, final OAuthCompleteListener listener) {

        Context ctx = context.getApplicationContext();
        if (!Utils.isOnline(ctx)) {
            listener.onAuthenError(ZaloOAuthResultCode.RESULTCODE_UNEXPECTED_ERROR, "Mạng không ổn định, vui lòng thử lại sau");
            return;
        }
        RecoveryPassGuestTask task = new RecoveryPassGuestTask(ctx, email, listener);
        task.execute();
    }

    void _authenticateWithGuest(Context context,
                                final OAuthCompleteListener listener) {
        if (listener == null)
            throw new IllegalArgumentException(
                    "OAuthCompleteListener must be set.");

        Context ctx = context.getApplicationContext();
        if (!Utils.isOnline(ctx)) {
            if (listener != null)
                listener.onAuthenError(ZaloOAuthResultCode.RESULTCODE_UNEXPECTED_ERROR, "Mạng không ổn định, vui lòng thử lại sau");
            return;
        }
        String deviceId = authStorage.getGuestDeviceId();
        Log.i("debuglog", "authenticate with guest deviceId: " + deviceId);
        AuthenticateWithGuestTask task = new AuthenticateWithGuestTask(
                ctx, deviceId, listener);
        task.execute();
    }

    //****************
    public void authenticateWithZingMe(Context context, String username, String password, OAuthCompleteListener listener) {
        _authenticateWithZingMe(context, username, password, listener);

    }

    void _authenticateWithZingMe(Context context, String username,
                                 String password, OAuthCompleteListener listener) {
        if (listener == null)
            throw new IllegalArgumentException(
                    "OAuthCompleteListener must be set.");

        Context ctx = context.getApplicationContext();
        if (!Utils.isOnline(ctx)) {
            if (listener != null)
                listener.onAuthenError(ZaloOAuthResultCode.RESULTCODE_UNEXPECTED_ERROR, "Mạng không ổn định, vui lòng thử lại sau");
            return;
        }

        AuthenticateWithZingMeTask task = new AuthenticateWithZingMeTask(
                ctx, username, password, listener);
        task.execute();
    }

    public void authenticateWithGooglePlus(Activity context, OAuthCompleteListener listener) {
        mContext = context;
        _authenticateWithGooglePlus(context, listener, 0);

    }

    void _authenticateWithGooglePlus(Activity activity,
                                     OAuthCompleteListener listener, int overrideTheme) {
        this.listener = listener;
        if (listener == null)
            throw new IllegalArgumentException(
                    "OAuthCompleteListener must be set.");

        Context ctx = activity.getApplicationContext();
        if (!Utils.isOnline(ctx)) {
            if (listener != null)
                listener.onAuthenError(ZaloOAuthResultCode.RESULTCODE_UNEXPECTED_ERROR, "Mạng không ổn định, vui lòng thử lại sau");
            return;
        }
        //new
        Integer defaultWebClientId = Utils.getResourceId(activity, "default_web_client_id", "string");
        if (defaultWebClientId == 0) {
            listener.onAuthenError(1, "\"Invalid or missing DefaultWebClientId for Google SignIn. Please check again file google-service.json or firebase google signin\"");
            return;
        }
        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken(activity.getString(defaultWebClientId))
                .requestEmail()
                .build();
        mGoogleApiClient = new GoogleApiClient.Builder(activity)
                .addApi(Auth.GOOGLE_SIGN_IN_API, gso)
                .build();
        Intent signInIntent = Auth.GoogleSignInApi.getSignInIntent(mGoogleApiClient);

        listener.onStartLoading();
        activity.startActivityForResult(signInIntent, Constant.GOOGLE_AUTHENTICATE_REQUEST_CODE);
        Log.i("debuglog", "---_authenticateWithGooglePlus-----");
    }

    /**
     * Authenticate by using facebook account
     *
     * @param context  Activity the login activity
     * @param listener OAuthCompleteListener listener to receive authenticate event
     */
    public void authenticateWithFacebook(Activity context, OAuthCompleteListener listener) {
        mContext = context;
        _authenticateWithFacebook(context, listener);
    }

    public boolean onActivityResult(Activity activity, int requestCode,
                                    int resultCode, Intent data, OAuthCompleteListener listener) {
        Context ctx = activity.getApplicationContext();
        if (requestCode == Facebook.DEFAULT_AUTH_ACTIVITY_CODE) {
            if (mFacebook != null) {
                mFacebook.authorizeCallback(requestCode, resultCode, data);
            }
        }
        else if (requestCode == Constant.ZALO_AUTHENTICATE_REQUEST_CODE) {
            zaloSDK.onActivityResult(activity, requestCode, resultCode, data);
        }
        else { onGoogleSignInSActivityResult(activity,requestCode,resultCode,data);}

        return false;
    }

    public boolean onGoogleSignInSActivityResult(Activity activity, int requestCode,
                                                 int resultCode, Intent data) {

        if (requestCode == Constant.GOOGLE_AUTHENTICATE_REQUEST_CODE) {
            listener.onFinishLoading();
            if (data != null) {
                GoogleSignInResult result = Auth.GoogleSignInApi.getSignInResultFromIntent(data);
                if (result.isSuccess()) {
                    GoogleSignInAccount acct = result.getSignInAccount();
                    String idToken = acct.getIdToken();
                    Log.i("debuglog", "Authenticator.java-----378-----idToken: " + idToken);
                    AuthenticateWithGooglePlusTask task = new AuthenticateWithGooglePlusTask(activity, idToken, listener);
                    task.execute();
                    return true;
                } else {
                    Log.i("debuglog", "Authenticator.java ----line 378 --- result.isSuccess() failed");
                    listener.onAuthenError(ZaloOAuthResultCode.RESULTCODE_CANT_LOGIN_GOOGLE, "Không thể đăng nhập Google.");
                }
            } else {
                if (resultCode == Activity.RESULT_CANCELED) {
                    listener.onAuthenError(ZaloOAuthResultCode.RESULTCODE_USER_REJECT, "");
                } else {
                    listener.onAuthenError(ZaloOAuthResultCode.RESULTCODE_CANT_LOGIN_GOOGLE, "Không thể đăng nhập Google.");
                }
            }
        }
        return false;
    }

    public void unauthenticateExtension() {
        //embed fb
        SessionStore.clear(mContext);
    }

    void _authenticateWithFacebook(Activity context,
                                   final OAuthCompleteListener listener) {
        if (listener == null)
            throw new IllegalArgumentException(
                    "OAuthCompleteListener must be set.");
//        ZaloSDK.Instance.getAuthenticator().nameActivtyCheckAuthen = context.getClass().getSimpleName();
//        ZaloSDK.Instance.getAuthenticator().setOAuthCompleteListener(listener);
        final Context ctx = context.getApplicationContext();
        if (!Utils.isOnline(ctx)) {
            listener.onAuthenError(ZaloOAuthResultCode.RESULTCODE_UNEXPECTED_ERROR, "Mạng không ổn định, vui lòng thử lại sau");
            return;
        }

        if (TextUtils.isEmpty(Utils.getFacebookAppId(ctx))) {
            listener.onAuthenError(ZaloOAuthResultCode.RESULTCODE_CANT_LOGIN_FACEBOOK, "Không tìm thấy meta-data com.facebook.sdk.ApplicationId");
        }

        if (mFacebook == null) {
            mFacebook = new Facebook(context.getApplicationContext(), Utils.getFacebookAppId(context));
            SessionStore.restore(mFacebook, context);
        }

        mFacebook.authorize(new WeakReference<Activity>(context), new String[]{"public_profile", "email", "user_friends",
                "user_birthday"}, Facebook.DEFAULT_AUTH_ACTIVITY_CODE, new Facebook.DialogListener() {

            @Override
            public void onFacebookError(FacebookError error) {
                SessionEvents.onLoginError(error.getMessage());
                listener.onAuthenError(ZaloOAuthResultCode.RESULTCODE_CANT_LOGIN_FACEBOOK, "Không thể đăng nhập Facebook.");
            }

            @Override
            public void onError(DialogError error) {
                SessionEvents.onLoginError(error.getMessage());
                listener.onAuthenError(ZaloOAuthResultCode.RESULTCODE_CANT_LOGIN_FACEBOOK, "Không thể đăng nhập Facebook.");
            }

            @Override
            public void onComplete(Bundle values) {
                SessionEvents.onLoginSuccess();
                Log.i("debuglog", "accessToken facebook: " + mFacebook.getAccessToken());
                AuthenticateWithFacebookTask task = new AuthenticateWithFacebookTask(
                        ctx, mFacebook.getAccessToken(), listener);
                task.execute();
            }

            @Override
            public void onCancel() {
                SessionEvents.onLoginError("Action Canceled");
                listener.onAuthenError(ZaloOAuthResultCode.RESULTCODE_USER_REJECT, "");
            }
        });
    }

    private class AuthenticateWithGuestTask extends AuthenticateTask {
        private String deviceId;

        public AuthenticateWithGuestTask(Context context, String deviceId, OAuthCompleteListener listener) {
            super(context, LoginChannel.GUEST, listener);
            this.deviceId = deviceId;
        }

        @Override
        protected String authenticateUrl() {
//            return Constant.IS_DEV ? "https://dev.oauth.zaloapp.com/v2/mobile/guest" : "https://oauth.zaloapp.com/v2/mobile/guest";
            return AUTH_MOBILE_GUEST_PATH;
        }

        @Override
        protected void customizeParam(HttpUrlEncodedRequest request) {
            Log.i("debuglog", "login with guest------deviceId: " + deviceId);
            request.addParameter("zgId", deviceId);
        }
    }

    private class AuthenticateWithZingMeTask extends AuthenticateTask {
        private String username;
        private String password;

        AuthenticateWithZingMeTask(Context context, String username,
                                   String password, OAuthCompleteListener listener) {
            super(context, LoginChannel.ZINGME, listener);
            this.username = username;
            this.password = password;
        }

        @Override
        protected String authenticateUrl() {
//            return Constant.IS_DEV ?"https://dev.oauth.zaloapp.com/v2/mobile/zing" : "https://oauth.zaloapp.com/v2/mobile/zing";
            return AUTH_MOBILE_ZING_PATH;
        }

        @Override
        protected void customizeParam(HttpUrlEncodedRequest request) {
            request.addParameter("u", username);
            request.addParameter("p", password);
        }
    }

    private class AuthenticateWithGooglePlusTask extends AuthenticateTask {
        private String accessToken;

        public AuthenticateWithGooglePlusTask(Context context,
                                              String accessToken, OAuthCompleteListener listener) {
            super(context, LoginChannel.GOOGLE, listener);
            this.accessToken = accessToken;
        }

        @Override
        protected String authenticateUrl() {
//            return Constant.IS_DEV ? "https://dev.oauth.zaloapp.com/v3/mobile/google" : "https://oauth.zaloapp.com/v3/mobile/google";
            return AUTH_MOBILE_GOOGLE_PATH;
        }

        @Override
        protected void customizeParam(HttpUrlEncodedRequest request) {
            request.addParameter("idToken", accessToken);
        }
    }

    private class AuthenticateWithFacebookTask extends AuthenticateTask {
        private String accessToken;

        public AuthenticateWithFacebookTask(Context context, String accessToken, OAuthCompleteListener listener) {
            super(context, LoginChannel.FACEBOOK, listener);
            this.accessToken = accessToken;
        }

        @Override
        protected String authenticateUrl() {
            return AUTH_MOBILE_FACEBOOK_PATH;
        }

        @Override
        protected void customizeParam(HttpUrlEncodedRequest request) {
            request.addParameter("access_token", accessToken);
        }

        @Override
        protected void customizeResponse(OauthResponse response) {
            response.setFacebookAccessToken(mFacebook.getAccessToken());
            response.setFacebookExpireTime(mFacebook.getAccessExpires());
        }
    }

    protected abstract class AuthenticateTask extends AsyncTask<Void, Void, String> {
        protected Context context;
        protected LoginChannel channel;
        protected OAuthCompleteListener listener;

        private String baseUrl = ServiceMapManager.getInstance().urlFor(ServiceMapManager.KEY_URL_OAUTH);
        private HttpClient httpClient = new HttpClient(baseUrl);

        protected AuthenticateTask(Context context, LoginChannel channel, OAuthCompleteListener listener) {
            this.context = context;
            this.channel = channel;
            this.listener = listener;
        }

        @Override
        protected void onPreExecute() {
            //super.onPreExecute();
            listener.onStartLoading();
        }

        protected abstract String authenticateUrl();

        protected abstract void customizeParam(HttpUrlEncodedRequest request);

        protected void customizeResponse(OauthResponse response) {
        }

        @Override
        protected String doInBackground(Void... params) {
            Log.i("debuglog", "Authenticator.java-----1243----AuthenticateTask--->doInBackground---");
            String applicationHashKey = AppInfo.getInstance().getApplicationHashKey() != null ? AppInfo.getInstance().getApplicationHashKey() : "";
            HttpUrlEncodedRequest request = new HttpUrlEncodedRequest(authenticateUrl());

            request.addParameter(Constant.PARAM_APP_ID, AppInfo.getInstance().getAppId());
            request.addParameter("version", DeviceTracking.getInstance().getVersion());
            request.addParameter("sign_key", applicationHashKey);
            request.addParameter("pkg_name", AppInfo.getInstance().getPackageName());
            request.addParameter("frm", "sdk");
            customizeParam(request);

            Log.i("debuglog", "authenticateUrl(): " + authenticateUrl());
            Log.i("debuglog", Constant.PARAM_APP_ID + " : " + AppInfo.getInstance().getAppId());
            Log.i("debuglog", "version: " + DeviceTracking.getInstance().getVersion());
            Log.i("debuglog", "sign_key: " + AppInfo.getInstance().getApplicationHashKey());
            Log.i("debuglog", "pkg_name: " + AppInfo.getInstance().getPackageName());
            Log.i("debuglog", "frm: sdk");
            return httpClient.send(request).getText();
        }

        @Override
        protected void onPostExecute(String result) {
            //super.onPostExecute(result);
            Log.i("debuglog", "Authenticator.java ---1262---AuthenticateTask--onPostResult-----: " + result);
            try {
                listener.onFinishLoading();
            } catch (Exception e) {
                Log.e("AuthenticateTask", e);
            }

            try {
                JSONObject object = new JSONObject(result);
                int errorCode = object.getInt("error");
                String errorMsg = object.getString("errorMsg");
                if (errorCode == Constant.RESULT_CODE_SUCCESSFUL) {
                    String zgId = object.optString("zgId");
                    JSONObject data = object.getJSONObject("data");
                    String oauth = data.getString("code");
                    long uid = data.getLong("uid");
                    String name = data.getString("display_name");
                    int zcert = object.optInt("zcert");
                    int zprotect = data.optInt("zprotect");
                    String socialId = data.optString("socialId");

                    authStorage.setLastLoginChannel(channel.toString());
                    authStorage.setAuthCode(oauth);
                    authStorage.setAccessToken("");
                    authStorage.setAccessTokenNewAPI("");
                    authStorage.setZaloId(uid);
                    authStorage.setZaloDisplayName(name);
                    authStorage.setIsProtected(zprotect);
                    authStorage.setSocialId(socialId);

                    if (this instanceof AuthenticateWithGuestTask) {
                        authStorage.setGuestDeviceId(zgId);
                        authStorage.setIsGuestCertificated(zcert);
                    }

                    OauthResponse response = new OauthResponse();
                    response.setuId(uid);
                    response.setOauthCode(oauth);
                    response.setChannel(channel);

                    response.setSocialId(socialId);
                    customizeResponse(response);
                    listener.onGetOAuthComplete(response);
                } else {
                    int e = ZaloOAuthResultCode.findById(errorCode);
                    ErrorResponse errorResponse = new ErrorResponse(e, "", "", "", "");
                    listener.onAuthenError(e, errorMsg);
                    listener.onAuthenError(e, errorMsg, errorResponse);
                }
            } catch (Exception ex) {
                Log.e("AuthenticateTask", ex);
                listener.onAuthenError(ZaloOAuthResultCode.RESULTCODE_UNEXPECTED_ERROR, "Có lỗi xảy ra. Xin vui lòng thử lại.");
            }
            listener = null;
        }
    }

    //task for guest
    private class RecoveryPassGuestTask extends AsyncTask<Void, Void, String> {

        //        private String url = Constant.IS_DEV ? "https://dev.oauth.zaloapp.com/v2/mobile/forgot-passwd-guest" : "https://oauth.zaloapp.com/v2/mobile/forgot-passwd-guest";
        final String AUTH_FORGOT_PASSWORD_GUEST_PATH = "/v2/mobile/forgot-passwd-guest";
        String email;
        private Context context;
        private OAuthCompleteListener listener;
        private String url = ServiceMapManager.getInstance().urlFor(ServiceMapManager.KEY_URL_OAUTH);
        private HttpClient httpClient = new HttpClient(url);

        public RecoveryPassGuestTask(Context context, String email, OAuthCompleteListener listener) {
            this.context = context;
            this.email = email;
            this.listener = listener;
        }

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            listener.onStartLoading();
        }

        @Override
        protected String doInBackground(Void... params) {
            String applicationHashKey = AppInfo.getInstance().getApplicationHashKey();
            if (applicationHashKey == null) applicationHashKey = "";

            HttpUrlEncodedRequest request = new HttpUrlEncodedRequest(AUTH_FORGOT_PASSWORD_GUEST_PATH);
            request.addParameter(Constant.PARAM_APP_ID, AppInfo.getInstance().getAppId());
            request.addParameter("sign_key", applicationHashKey);
            request.addParameter("pkg_name", AppInfo.getInstance().getPackageName());
            request.addParameter("email", email);

            return httpClient.send(request).getText();
        }

        @Override
        protected void onPostExecute(String result) {
            super.onPostExecute(result);
            try {
                listener.onFinishLoading();
            } catch (Exception e) {
                Log.e("RecoveryPassGuestTask", e);
            }

            try {
                JSONObject object = new JSONObject(result);
                int errorCode = object.getInt("error");
                String errorMsg = object.getString("errorMsg");
                if (errorCode == Constant.RESULT_CODE_SUCCESSFUL) {
                    listener.onRequestAccountProtect(errorCode, errorMsg);
                } else {
                    listener.onAuthenError(errorCode, errorMsg);
                }
            } catch (Exception ex) {
                Log.e("RecoveryPassGuestTask", ex);
                listener.onAuthenError(ZaloOAuthResultCode.RESULTCODE_UNEXPECTED_ERROR, "Có lỗi xảy ra. Xin vui lòng thử lại.");
            }
        }

    }


    private class ProtectAccTask extends AsyncTask<Void, Void, String> {

        //        private String url = Constant.IS_DEV ? "https://dev.oauth.zaloapp.com/v2/mobile/protect-account" : "https://oauth.zaloapp.com/v2/mobile/protect-account";
        final String AUTH_PROTECT_ACCOUNT_PATH = "/v2/mobile/protect-account";
        String cmnd;
        private Context context;
        private OAuthCompleteListener listener;
        private String url = ServiceMapManager.getInstance().urlFor(ServiceMapManager.KEY_URL_OAUTH);
        private HttpClient httpClient = new HttpClient(url);

        public ProtectAccTask(Context context, String cmnd, OAuthCompleteListener listener) {
            this.context = context;
            this.cmnd = cmnd;
            this.listener = listener;
        }

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            listener.onStartLoading();
        }

        @Override
        protected String doInBackground(Void... params) {
            HttpUrlEncodedRequest request = new HttpUrlEncodedRequest(AUTH_PROTECT_ACCOUNT_PATH);
            String applicationHashKey = AppInfo.getInstance().getApplicationHashKey() != null ? AppInfo.getInstance().getApplicationHashKey() : "";
            String authCode = authStorage.getOAuthCode() != null ? authStorage.getOAuthCode() : "";

            request.addParameter(Constant.PARAM_APP_ID, AppInfo.getInstance().getAppId());
            request.addParameter("sign_key", applicationHashKey);
            request.addParameter("pkg_name", AppInfo.getInstance().getPackageName());
            request.addParameter("code", authCode);
            request.addParameter("govId", cmnd);
            return httpClient.send(request).getText();
        }

        @Override
        protected void onPostExecute(String result) {
            super.onPostExecute(result);
            try {
                listener.onFinishLoading();
            } catch (Exception e) {
                Log.e("ProtectAccTask", e);
            }

            try {
                JSONObject object = new JSONObject(result);
                int errorCode = object.getInt("error");
                String errorMsg = object.getString("errorMsg");
                listener.onRequestAccountProtect(errorCode, errorMsg);
            } catch (Exception ex) {
                Log.e("ProtectAccTask", ex);
                listener.onAuthenError(ZaloOAuthResultCode.RESULTCODE_UNEXPECTED_ERROR, "Có lỗi xảy ra. Xin vui lòng thử lại.");
            }
        }

    }

    private class RequestCertificateGuestTask extends AsyncTask<Void, Void, String> {
        //        private String url = Constant.IS_DEV ? "https://dev.oauth.zaloapp.com/v2/mobile/req-cert-guest" : "https://oauth.zaloapp.com/v2/mobile/req-cert-guest";
        final String AUTH_REQ_CERT_GUEST_PATH = "/v2/mobile/req-cert-guest";
        String email;
        String zgId;
        String pass;
        private Context context;
        private OAuthCompleteListener listener;
        private String url = ServiceMapManager.getInstance().urlFor(ServiceMapManager.KEY_URL_OAUTH);
        private HttpClient httpClient = new HttpClient(url);

        public RequestCertificateGuestTask(Context context, String email, String pass, String zgId, OAuthCompleteListener listener) {
            this.context = context;
            this.email = email;
            this.zgId = zgId;
            this.pass = pass;
            this.listener = listener;
        }

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            listener.onStartLoading();
        }

        @Override
        protected String doInBackground(Void... params) {
            HttpUrlEncodedRequest request = new HttpUrlEncodedRequest(AUTH_REQ_CERT_GUEST_PATH);

            String applicationHashKey = AppInfo.getInstance().getApplicationHashKey() != null ? AppInfo.getInstance().getApplicationHashKey() : "";
            request.addParameter(Constant.PARAM_APP_ID, AppInfo.getInstance().getAppId());
            request.addParameter("sign_key", applicationHashKey);
            request.addParameter("pkg_name", AppInfo.getInstance().getPackageName());
            request.addParameter("email", email);
            request.addParameter("zgId", zgId);
            request.addParameter("passwd", pass);

            return httpClient.send(request).getText();
        }

        @Override
        protected void onPostExecute(String result) {
            super.onPostExecute(result);
            try {
                listener.onFinishLoading();
            } catch (Exception e) {
                Log.e("RequestCertificateGuestTask", e);
            }

            try {
                JSONObject object = new JSONObject(result);
                int errorCode = object.getInt("error");
                String errorMsg = object.getString("errorMsg");
                listener.onRequestAccountProtect(errorCode, errorMsg);
            } catch (Exception ex) {
                Log.e("RequestCertificateGuestTask", ex);
                listener.onAuthenError(ZaloOAuthResultCode.RESULTCODE_UNEXPECTED_ERROR, "Có lỗi xảy ra. Xin vui lòng thử lại.");
            }
        }
    }

    private class RecoveryGuestTask extends AsyncTask<Void, Void, String> {

        //        private String url = Constant.IS_DEV ?  "https://dev.oauth.zaloapp.com/v2/mobile/recover-guest" : "https://oauth.zaloapp.com/v2/mobile/recover-guest";
        final String AUTH_RECOVER_GUEST_PATH = "/v2/mobile/recover-guest";
        String email;
        String pass;
        private Context context;
        private OAuthCompleteListener listener;
        private String url = ServiceMapManager.getInstance().urlFor(ServiceMapManager.KEY_URL_OAUTH);
        private HttpClient httpClient = new HttpClient(url);

        public RecoveryGuestTask(Context context, String email, String pass, OAuthCompleteListener listener) {
            this.context = context;
            this.email = email;
            this.pass = pass;
            this.listener = listener;
        }

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            listener.onStartLoading();
        }

        @Override
        protected String doInBackground(Void... params) {
            HttpUrlEncodedRequest request = new HttpUrlEncodedRequest(AUTH_RECOVER_GUEST_PATH);

            String applicationHashKey = AppInfo.getInstance().getApplicationHashKey() != null ? AppInfo.getInstance().getApplicationHashKey() : "";
            request.addParameter(Constant.PARAM_APP_ID, AppInfo.getInstance().getAppId());
            request.addParameter("sign_key", applicationHashKey);
            request.addParameter("pkg_name", AppInfo.getInstance().getPackageName());
            request.addParameter("email", email);
            request.addParameter("passwd", pass);

            return httpClient.send(request).getText();
        }

        @Override
        protected void onPostExecute(String result) {
            super.onPostExecute(result);
            try {
                listener.onFinishLoading();
            } catch (Exception e) {
                Log.e("RecoveryGuestTask", e);
            }

            try {
                JSONObject object = new JSONObject(result);
                int errorCode = object.getInt("error");
                String errorMsg = object.getString("errorMsg");
                if (errorCode == Constant.RESULT_CODE_SUCCESSFUL) {

                    String zgId = object.optString("zgId");
                    JSONObject data = object.getJSONObject("data");
                    String oauth = data.getString("code");
                    long uid = data.getLong("uid");
                    String name = data.getString("display_name");
                    int zcert = object.optInt("zcert");
                    int zprotect = data.optInt("zprotect");

                    authStorage.setLastLoginChannel(LoginChannel.GUEST.toString());
                    authStorage.setAuthCode(oauth);
                    authStorage.setAccessToken("");
                    authStorage.setAccessTokenNewAPI("");
                    authStorage.setZaloId(uid);
                    authStorage.setZaloDisplayName(name);
                    authStorage.setIsProtected(zprotect);

                    authStorage.setGuestDeviceId(zgId);
                    authStorage.setIsGuestCertificated(zcert);

                    listener.onGetOAuthComplete(new OauthResponse(uid, oauth, LoginChannel.GUEST));
                } else {
                    listener.onAuthenError(errorCode, errorMsg);
                }
            } catch (Exception ex) {
                Log.e("RecoveryGuestTask", ex);
                listener.onAuthenError(ZaloOAuthResultCode.RESULTCODE_UNEXPECTED_ERROR, "Có lỗi xảy ra. Xin vui lòng thử lại.");
            }
        }

    }
}
