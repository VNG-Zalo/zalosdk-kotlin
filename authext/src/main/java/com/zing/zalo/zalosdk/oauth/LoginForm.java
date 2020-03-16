package com.zing.zalo.zalosdk.oauth;

import android.animation.Animator;
import android.animation.ObjectAnimator;
import android.animation.ValueAnimator;
import android.annotation.TargetApi;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.content.res.TypedArray;
import android.graphics.Color;
import android.graphics.Typeface;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Handler;
import android.text.TextUtils;
import android.text.method.PasswordTransformationMethod;
import android.util.AttributeSet;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ImageView.ScaleType;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.zing.zalo.zalosdk.common.DimensionHelperEx;
import com.zing.zalo.zalosdk.common.Utils;
import com.zing.zalo.zalosdk.exception.InitializedException;
import com.zing.zalo.zalosdk.java.LoginChannel;
import com.zing.zalo.zalosdk.java.devicetrackingsdk.DeviceTracking;
import com.zing.zalo.zalosdk.java.payment.direct.PaymentAlertDialog;
import com.zing.zalo.zalosdk.java.payment.direct.PaymentProcessingDialog;
import com.zing.zalo.zalosdk.java.servicemap.ServiceMapManager;
import com.zing.zalo.zalosdk.kotlin.core.helper.AppInfo;
import com.zing.zalo.zalosdk.kotlin.core.helper.DeviceInfo;
import com.zing.zalo.zalosdk.kotlin.core.http.HttpClient;
import com.zing.zalo.zalosdk.kotlin.core.http.HttpGetRequest;
import com.zing.zalo.zalosdk.kotlin.core.log.Log;
import com.zing.zalo.zalosdk.kotlin.oauth.Constant;
import com.zing.zalo.zalosdk.kotlin.oauth.IAuthenticateCompleteListener;
import com.zing.zalo.zalosdk.kotlin.oauth.LoginVia;
import com.zing.zalo.zalosdk.kotlin.oauth.ZaloOAuthResultCode;
import com.zing.zalo.zalosdk.kotlin.oauth.ZaloSDK;
import com.zing.zalo.zalosdk.kotlin.oauth.callback.ValidateOAuthCodeCallback;
import com.zing.zalo.zalosdk.kotlin.oauth.helper.AuthStorage;

import org.jetbrains.annotations.NotNull;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.Date;
import java.util.Locale;
import java.util.Map;

import static android.content.Context.MODE_PRIVATE;

public class LoginForm extends RelativeLayout implements OnClickListener {

    static final String KEY_FBTOKEN = "zfbtoken";
    static final String KEY_FBTOKEN_EXP = "zfbtokenExpire";
    static final String KEY_PREF_NAME = "zfbtokenname";
    private static final String WEB_MOBILE_LOGIN_INFO_PATH = "/v2/mobile/login-info";
    //View lg_zalo, lg_fb, lg_zing, lg_google, lg_guest;
    public static boolean RELOGIN_ZING = true;
    public static String email;
    public static String hash;
    private final String GUARD_GUEST = "GUARD_GUEST";
    private final String RECOVERY_GUEST = "RECOVERY_GUEST";
    private final String RECOVERY_PASS = "RECOVERY_PASS";
    private final String GUARD_GUEST_FROM_SUPPORT = "GUARD_GUEST_FROM_SUPPORT";
    private final String REGIS_CERTIFICATE = "REGIS_CERTIFICATE";
    LoginListener loginListener;
    OAuthCompleteListener listener;
    LoginVia mLoginVia;
    View header_line;
    View back_login_form;
    View loginZingIDForm;
    View back_form;
    View back_login_form_from_support;
    View back_form_cmnd;
    Button submitZingMeButton;
    TextView title;
    RelativeLayout loginContainer;
    View retryContainer;
    View email_guard_container;
    View support_container;
    View cmnd_container;
    View title_forget_pass;
    LinearLayout confirm_cmnd;
    ProgressBar progress_loading;
    ImageView ico_unremind_confirm;
    TextView retryButton;
    int oneDp;
    int viewIndex = 0;
    String guest_channel_title;
    String zingIDUserName;
    int loginGuestCount;
    int numberOfShown;
    long lastShownTime;
    String warningMsg;
    String zing_me_acc;
    String cmnd_number;
    EditText email_guard;
    EditText pass_guard;
    EditText userPass;
    View tt_continue_login;
    long showWarningInSecond;
    long remind_time_guest_login;
    int isGuestCert;
    boolean isGettingIsGuestCertificate;
    Button request_certificate_guest;
    Button custom_service;
    LinearLayout channel_container;
    JSONObject csInfo;
    JSONObject zingInfo;
    SharedPreferences gVar;
    AuthenticateExtention authenticateExtention;
    Context mContext;
    long uId;
    String oauthCode;
    LoginChannel channel;
    String fbAccessToken;
    long fbExpireTime;
    long mLastTimeClick;
    boolean isClicked;
    int loginContainerHeight;
    private ShowProtectGuestAccountListener mProtectGuestAccountListener;
    private ZaloSDK zaloSDK;
    private AuthStorage authStorage;

    public LoginForm(Context context) {
        super(context);
        initZaloSDK(context);
        init(null);
    }

    public LoginForm(Context context, AttributeSet attrs) {
        super(context, attrs);
        initZaloSDK(context);
        init(attrs);
    }

    public LoginForm(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        initZaloSDK(context);
        init(attrs);
    }

    public void setOnShowProtectGuestAccountListener(ShowProtectGuestAccountListener listener) {
        mProtectGuestAccountListener = listener;
    }

    public void setOAuthCompleteListener(OAuthCompleteListener authCompleteListener) {
        this.listener = authCompleteListener;
    }

    public boolean onActivityResult(Activity activity, int requestCode,
                                    int resultCode, Intent data) {
        if (authenticateExtention != null) {
            Log.i("debuglog", "LoginForm-----onActivityResult");
            authenticateExtention.onActivityResult(activity, requestCode, resultCode, data, loginListener);
        }
        return false;
    }

    @SuppressWarnings("deprecation")
    private void init(AttributeSet attrs) {
        if (!(getContext() instanceof Activity))
            throw new RuntimeException("Not Activity!");
        authenticateExtention = new AuthenticateExtention(getContext());
        loginListener = new LoginListener();
        inflate(getContext(), R.layout.login_activity, this);

        guest_channel_title = "Chơi ngay";
        if (attrs != null) {
            TypedArray typedArray = getContext().obtainStyledAttributes(attrs, R.styleable.LoginForm);
            int n = typedArray.getIndexCount();
            for (int i = 0; i < n; i++) {
                int attr = typedArray.getIndex(i);
                if (attr == R.styleable.LoginForm_guestLoginTitle) {
                    guest_channel_title = typedArray.getString(R.styleable.LoginForm_guestLoginTitle);
                } else if (attr == R.styleable.LoginForm_loginFormBackground) {
                    Drawable bg = typedArray.getDrawable(R.styleable.LoginForm_loginFormBackground);
                    View view = findViewById(R.id.login_form);
                    view.setBackgroundDrawable(bg);
                }
            }
            typedArray.recycle();
        }

        mLoginVia = LoginVia.APP;
//		Log.i("AndroidRuntime", "R.id.gateway_root===" + R.id.gateway_root);
        Utils.setupUIHideKeyBoard((Activity) getContext(), findViewById(R.id.gateway_root));

        oneDp = (int) DimensionHelperEx.getPixelPadding(getContext());
        loginContainer = findViewById(R.id.loginContainer);
        retryContainer = findViewById(R.id.retry_container);
        support_container = findViewById(R.id.support_container);
        email_guard_container = findViewById(R.id.email_guard_container);
        cmnd_container = findViewById(R.id.cmnd_container);
        confirm_cmnd = findViewById(R.id.confirm_cmnd);

        email_guard = findViewById(R.id.email_guard);
        pass_guard = findViewById(R.id.emailPass);
        pass_guard.setTypeface(Typeface.DEFAULT);
        pass_guard.setTransformationMethod(new PasswordTransformationMethod());

        userPass = findViewById(R.id.userPass);
        userPass.setTypeface(Typeface.DEFAULT);
        userPass.setTransformationMethod(new PasswordTransformationMethod());

        header_line = findViewById(R.id.header_line);
        back_login_form = findViewById(R.id.back_login_form);
        title = findViewById(R.id.title);
        loginZingIDForm = findViewById(R.id.loginZingIDForm);
        progress_loading = findViewById(R.id.progress_loading);
        custom_service = findViewById(R.id.custom_service);
        request_certificate_guest = findViewById(R.id.request_certificate_guest);
        tt_continue_login = findViewById(R.id.tt_continue_login);
        submitZingMeButton = findViewById(R.id.submit);
        ico_unremind_confirm = findViewById(R.id.ico_unremind_confirm);
        back_login_form_from_support = findViewById(R.id.back_login_form_from_support);
        back_form_cmnd = findViewById(R.id.back_form_cmnd);
        back_form = findViewById(R.id.back_form);
        retryButton = findViewById(R.id.retry);
        title_forget_pass = findViewById(R.id.title_forget_pass);

        channel_container = findViewById(R.id.channel_container);

        ((TextView) findViewById(R.id.zalo_version)).setText(DeviceTracking.getInstance().getVersion());
        findViewById(R.id.submit_email_guard).setOnClickListener(this);
        findViewById(R.id.submit_cmnd_number).setOnClickListener(this);
        findViewById(R.id.regis_acc).setOnClickListener(this);
        findViewById(R.id.unremind_confirm).setOnClickListener(this);
        findViewById(R.id.cancel_submit_cmnd).setOnClickListener(this);
        findViewById(R.id.submit_cmnd).setOnClickListener(this);
        findViewById(R.id.support).setOnClickListener(this);
        findViewById(R.id.recovery_guest).setOnClickListener(this);
        findViewById(R.id.register_identify_number).setOnClickListener(this);

        title_forget_pass.setOnClickListener(this);
        custom_service.setOnClickListener(this);
        request_certificate_guest.setOnClickListener(this);
        retryButton.setOnClickListener(this);
        back_login_form.setOnClickListener(this);
        submitZingMeButton.setOnClickListener(this);
        tt_continue_login.setOnClickListener(this);
        back_login_form_from_support.setOnClickListener(this);
        back_form_cmnd.setOnClickListener(this);
        back_form.setOnClickListener(this);

        gVar = getContext().getSharedPreferences("zacPref", 0);
        long expiredTime = gVar.getLong("login_channel_expiredTime", 0);
        showWarningInSecond = gVar.getLong("showWarningInSecond", 0);

        String channels = gVar.getString("login_channel_array", "[]");
        zingIDUserName = gVar.getString("zing_me_acc", "");

        loginGuestCount = gVar.getInt("guest_login_count", 0);
        numberOfShown = gVar.getInt("numberOfShown", 0);
        lastShownTime = gVar.getLong("lastShownTime", 0);

        remind_time_guest_login = gVar.getLong("remind_time_guest_login", 0);

        warningMsg = gVar.getString("warningMsg", "");
        try {
            csInfo = new JSONObject(gVar.getString("csInfo", "{}"));
            zingInfo = new JSONObject(gVar.getString("zingInfo", "{}"));
        } catch (JSONException e1) {
            Log.e("LoginForm init", e1);
        }

        cmnd_number = gVar.getString("cmnd_number", "");
        String forceLogin = gVar.getString("forceLogin", "");

        if (expiredTime <= System.currentTimeMillis()) {
            GetLoginChannelTask task = new GetLoginChannelTask();
            task.execute();
        } else {
            try {
                setUpLayout(new JSONArray(channels));
                forceLogin(forceLogin);
            } catch (JSONException e) {
                Log.e("LoginForm init", e);
            }
        }
    }

    private void forceLogin(String channel) {

        if (TextUtils.isEmpty(channel)) return;

        View v = channel_container.findViewWithTag(channel);
        if (v == null) return;
        v.performClick();

    }

    private void trapListener(LoginChannel channel) {

        switch (channel) {
            case GUEST:
                authenticateExtention.authenticateWithGuest(mContext, loginListener);
                break;
            case ZALO:
                zaloSDK.authenticate((Activity) getContext(), mLoginVia, loginListener);
                break;
            case FACEBOOK:
                authenticateExtention.authenticateWithFacebook((Activity) getContext(), loginListener);
                break;
            case GOOGLE:
                authenticateExtention.authenticateWithGooglePlus((Activity) getContext(), loginListener);
                break;
            default:
                break;
        }
    }

    private void trapListener(String username, String pass) {
        authenticateExtention.authenticateWithZingMe((Activity) getContext(), username, pass, loginListener);
    }

    private boolean checkShownShowProtectGuestAccForm() {
        loginGuestCount++;
        Editor editor = gVar.edit();
        long currentTime = System.currentTimeMillis();

        if (mProtectGuestAccountListener != null) {
            //APP RULE
            isGettingIsGuestCertificate = mProtectGuestAccountListener.onShowProtectGuestAccount(loginGuestCount, numberOfShown, new Date(lastShownTime));
        } else {
            //OLD RULE
            isGettingIsGuestCertificate = loginGuestCount == 2 || loginGuestCount == 3 || remind_time_guest_login < currentTime;
        }

        if (isGettingIsGuestCertificate) {
            while (remind_time_guest_login < currentTime && showWarningInSecond > 0) {
                remind_time_guest_login += showWarningInSecond;
            }

            editor.putLong("remind_time_guest_login", remind_time_guest_login);
        }

        editor.putInt("guest_login_count", loginGuestCount);
        editor.commit();

        if (isGettingIsGuestCertificate) {
            isGettingIsGuestCertificate = false;

            if (isGuestCert == 0) {
                showProtectAccountGuestForm();
                return true;
            }
        }

        return false;
    }

    public String getString(String key) {
        SharedPreferences prefs = getContext().getSharedPreferences(KEY_PREF_NAME, MODE_PRIVATE);
        String restoredText = prefs.getString(key, "");
        return restoredText;
    }

    public void saveString(String nameKey, String value) {
        SharedPreferences.Editor editor = getContext().getSharedPreferences(KEY_PREF_NAME, MODE_PRIVATE).edit();
        editor.putString(nameKey, value);
        editor.commit();
    }

    private void submitCMND(OauthResponse response) {
        if (TextUtils.isEmpty(cmnd_number)) return;

        email_guard_container.setTag(REGIS_CERTIFICATE);
        authenticateExtention.protectAcc((Activity) getContext(), cmnd_number, null);

    }

    private void authenComplete(OauthResponse response) {
        if (listener == null) {
            Log.i("debuglog", "You must set OAuthCompleteListener for LoginForm");
            Log.e("ZaloLoginForm", "You must set OAuthCompleteListener for LoginForm");
            return;
        }
        //submit khi zcert=true and guest channel || orther channel
        isGuestCert = authenticateExtention.getIsGuestCertificated();
        if (isGuestCert == 1 && LoginChannel.GUEST.equals(response.getChannel()) || !LoginChannel.GUEST.equals(response.getChannel())) {
            submitCMND(response);
        }

        listener.onGetOAuthComplete(response);
    }

    private void showSupportForm() {
        title.setText("Hỗ trợ");
        support_container.setVisibility(View.VISIBLE);

        Button recoveryGuestButton = findViewById(R.id.recovery_guest);

        if (isChannelEnabled(LoginChannel.GUEST)) {
            recoveryGuestButton.setText("Khôi phục tài khoản " + guest_channel_title.toLowerCase(Locale.getDefault()));
            recoveryGuestButton.setVisibility(View.VISIBLE);
        } else {
            recoveryGuestButton.setVisibility(View.GONE);
        }


        String zgId = authenticateExtention.getGuestDeviceId();
        int cer = authenticateExtention.getIsGuestCertificated();
        boolean isNeedRequestCertificateGuest = TextUtils.isEmpty(zgId) == false && cer == 0;

        if (isNeedRequestCertificateGuest) {
            request_certificate_guest.setVisibility(View.VISIBLE);
            request_certificate_guest.setText("Bảo vệ tài khoản " + guest_channel_title.toLowerCase(Locale.getDefault()));
        } else {
            request_certificate_guest.setVisibility(View.GONE);
        }

        if (csInfo != null && csInfo.optBoolean("enable", false)) {
            String title = TextUtils.isEmpty(csInfo.optString("name")) ? "Chăm sóc khách hàng" : csInfo.optString("name");
            String link = csInfo.optString("link");
            custom_service.setText(title);
            custom_service.setTag(link);
            custom_service.setVisibility(View.VISIBLE);
        } else {
            custom_service.setVisibility(View.GONE);
        }

    }

    private void disableZingIDForm() {
        title.setText("Đăng nhập");
        header_line.setBackgroundColor(Color.parseColor("#2196F3"));

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB) {
            collapse(loginZingIDForm, loginContainer, loginContainerHeight);
        } else {
            loginContainer.setVisibility(View.VISIBLE);
            loginZingIDForm.setVisibility(View.GONE);
        }
    }

    @TargetApi(Build.VERSION_CODES.HONEYCOMB)
    private void enableZingIDForm() {
        title.setText("Đăng nhập bằng Zing ID");
        header_line.setBackgroundColor(Color.parseColor("#009ddc"));//"#019A42"));

        loginZingIDForm.setVisibility(View.VISIBLE);
        String v = ((EditText) findViewById(R.id.userName)).getText().toString().trim();
        if (TextUtils.isEmpty(v)) {
            ((EditText) findViewById(R.id.userName)).setText(zingIDUserName);
        }

        loginContainerHeight = loginContainer.getHeight();
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB) {
            if (loginContainerHeight < (int) DeviceInfo.pxFromDp(getContext(), 225)) {
                ValueAnimator mAnimator = slideAnimator(loginZingIDForm, loginContainer.getHeight(), (int) DeviceInfo.pxFromDp(getContext(), 225));
                mAnimator.start();
            }

        }
        loginContainer.setVisibility(View.GONE);
    }

    private void showProtectAccountGuestForm() {
        numberOfShown++;
        lastShownTime = System.currentTimeMillis();

        gVar.edit().putInt("numberOfShown", numberOfShown).putLong("lastShownTime", lastShownTime).commit();

        email_guard_container.setTag(GUARD_GUEST);
        loginContainer.setVisibility(View.GONE);
        email_guard_container.setVisibility(View.VISIBLE);
        findViewById(R.id.title_sent_email_confirm).setVisibility(View.VISIBLE);
        email_guard.setText("");
        pass_guard.setText("");
        title.setText("Bảo vệ tài khoản");
        tt_continue_login.setVisibility(View.VISIBLE);
        back_form.setVisibility(View.GONE);
        title_forget_pass.setVisibility(View.GONE);
    }

    public boolean canBackPressed() {
        return (loginZingIDForm.isShown() || back_form.isShown() || back_login_form_from_support.isShown() || back_form_cmnd.isShown());
    }

    public void onBackPressed() {
        if (loginZingIDForm.isShown()) {
            disableZingIDForm();
        } else if (back_form.isShown()) {
            back_form.performClick();
        } else if (back_login_form_from_support.isShown()) {
            back_login_form_from_support.performClick();
        } else if (back_form_cmnd.isShown()) {
            back_form_cmnd.performClick();
        }
    }

    public void setZaloLoginVia(LoginVia loginVia) {
        mLoginVia = loginVia;
    }

    private View setUpEmptyCell() {
        RelativeLayout layout = new RelativeLayout(getContext());
        LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT);
        layoutParams.weight = 1.0f;
        layout.setMinimumWidth(oneDp * 110);
        layout.setPadding(oneDp * 5, oneDp * 5, oneDp * 5, oneDp * 5);
        layout.setLayoutParams(layoutParams);
        return layout;
    }

    private View setUpCellLogin(String channel, CellType cellType) {

        int colorBackground = 0;
        int icon = 0;

        if (channel.equals("zalo")) {
            channel = "Zalo";
            colorBackground = R.drawable.zalosdk_zalo;
            icon = R.drawable.ic_play_zalo;
        } else if (channel.equals("facebook")) {
            channel = "Facebook";
            colorBackground = R.drawable.zalosdk_fb;
            icon = R.drawable.ic_play_fb;
        } else if (channel.equals("google")) {
            channel = "Google";
            colorBackground = R.drawable.zalosdk_google;
            icon = R.drawable.ic_play_google;
        } else if (channel.equals("zing")) {
            channel = "Zing ID";
            colorBackground = R.drawable.zalosdk_zing;
            icon = R.drawable.ic_play_zing;
        } else if (channel.equals("guest")) {
            channel = guest_channel_title;
            colorBackground = R.drawable.zalosdk_guest;
            icon = R.drawable.ic_play;
        }

        LinearLayout groupLayout = null;
        if (cellType == CellType.ALL_CENTER) {
            groupLayout = new LinearLayout(getContext());
            LayoutParams params = new LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT);
            params.addRule(RelativeLayout.CENTER_IN_PARENT);

            groupLayout.setOrientation(LinearLayout.HORIZONTAL);
            groupLayout.setLayoutParams(params);
            groupLayout.setGravity(Gravity.CENTER_VERTICAL);

            RelativeLayout layout = new RelativeLayout(getContext());
            LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT);
            layoutParams.weight = 1.0f;
            layout.setMinimumWidth(oneDp * 110);
            layout.setBackgroundResource(colorBackground);
            int padding = (int) getResources().getDimension(R.dimen.sdk_login_channel_padding);
            layout.setPadding(padding, padding, padding, padding);
            layout.setLayoutParams(layoutParams);

            int ico_size = (int) getResources().getDimension(R.dimen.sdk_icon_channel_size);

            ImageView ico = new ImageView(getContext());
            LinearLayout.LayoutParams layoutParams2 = new LinearLayout.LayoutParams(ico_size, ico_size);
            ico.setImageResource(icon);
            ico.setScaleType(ScaleType.FIT_CENTER);
            ico.setLayoutParams(layoutParams2);

            TextView label = new TextView(getContext());
            layoutParams2 = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT);
            layoutParams2.setMargins(padding * 2, 0, 0, 0);
            label.setSingleLine(true);
            label.setEllipsize(TextUtils.TruncateAt.MARQUEE);
            label.setSelected(true);
            label.setTextColor(Color.parseColor("white"));
            label.setTextSize(TypedValue.COMPLEX_UNIT_DIP, (int) getResources().getDimension(R.dimen.sdk_title_channel_size) / getResources().getDisplayMetrics().density);
            label.setText(channel);
            label.setLayoutParams(layoutParams2);

            groupLayout.addView(ico);
            groupLayout.addView(label);
            layout.addView(groupLayout);

            return layout;
        } else {

            RelativeLayout layout = new RelativeLayout(getContext());
            LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT);
            layoutParams.weight = 1.0f;
            layout.setMinimumWidth(oneDp * 110);
            layout.setBackgroundResource(colorBackground);
            int padding = (int) getResources().getDimension(R.dimen.sdk_login_channel_padding);
            layout.setPadding(padding, padding, padding, padding);

            layout.setLayoutParams(layoutParams);
            int ico_size = (int) getResources().getDimension(R.dimen.sdk_icon_channel_size);

            ImageView ico = new ImageView(getContext());
            LayoutParams layoutParams2 = new LayoutParams(ico_size, ico_size);
            layoutParams2.addRule(RelativeLayout.CENTER_VERTICAL);
            ico.setImageResource(icon);
            ico.setScaleType(ScaleType.FIT_CENTER);
            ico.setLayoutParams(layoutParams2);

            View divider = new View(getContext());
            layoutParams2 = new LayoutParams(1, (int) (ico_size * 0.4f));
            layoutParams2.addRule(RelativeLayout.CENTER_VERTICAL);
            layoutParams2.setMargins(ico_size, 0, 0, 0);
            divider.setLayoutParams(layoutParams2);
            divider.setBackgroundColor(Color.parseColor("#cccccc"));
            divider.setVisibility(cellType == CellType.TEXT_CENTER ? View.GONE : View.VISIBLE);

            TextView label = new TextView(getContext());
            layoutParams2 = new LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT);

            if (cellType == CellType.TEXT_CENTER) {
                layoutParams2.addRule(RelativeLayout.CENTER_IN_PARENT);
            } else {
                layoutParams2.addRule(RelativeLayout.CENTER_VERTICAL);
                layoutParams2.setMargins((int) getResources().getDimension(R.dimen.sdk_title_channel_margin_left), 0, 0, 0);
            }
            label.setSingleLine(true);
            label.setEllipsize(TextUtils.TruncateAt.MARQUEE);
            label.setSelected(true);
            label.setTextColor(Color.parseColor("white"));
            label.setTextSize(TypedValue.COMPLEX_UNIT_DIP, (int) getResources().getDimension(R.dimen.sdk_title_channel_size) / getResources().getDisplayMetrics().density);
            label.setText(channel);
            label.setLayoutParams(layoutParams2);

            layout.addView(ico);
            layout.addView(divider);
            layout.addView(label);
            return layout;
        }

    }

    private View setUpColumnMidle() {
        View view = new View(getContext());
        LinearLayout.LayoutParams rowParams = new LinearLayout.LayoutParams(oneDp * 20, oneDp);
        view.setLayoutParams(rowParams);
        return view;
    }

    private LinearLayout setUpRowLayout() {
        LinearLayout rowLayout = new LinearLayout(getContext());
        LinearLayout.LayoutParams rowParams = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);
        rowLayout.setLayoutParams(rowParams);
        rowLayout.setOrientation(LinearLayout.HORIZONTAL);
        int padding = (int) getResources().getDimension(R.dimen.sdk_row_channel_padding);
        rowLayout.setPadding(padding, 0, padding, padding);

        return rowLayout;
    }

    private void setUpOneRowLayout(JSONArray arrayChannel) throws JSONException {
        LinearLayout rowLayout = setUpRowLayout();

        String channel = arrayChannel.getString(0);

        View cellLayout = null;
        cellLayout = setUpCellLogin(channel, CellType.TEXT_CENTER);

        cellLayout.setTag(channel);
        cellLayout.setOnClickListener(new LoginFormClickListener());
        rowLayout.addView(cellLayout);

        channel_container.addView(rowLayout, viewIndex);
        viewIndex++;
    }

    private void setUpTwoRowLayout(JSONArray arrayChannel) throws JSONException {
        for (int i = 0; i < 2; i++) {
            LinearLayout rowLayout = setUpRowLayout();

            String channel = arrayChannel.getString(i);

            View cellLayout = null;
            cellLayout = setUpCellLogin(channel, CellType.TEXT_CENTER);

            cellLayout.setTag(channel);
            cellLayout.setOnClickListener(new LoginFormClickListener());
            rowLayout.addView(cellLayout);

            channel_container.addView(rowLayout, viewIndex);
            viewIndex++;
        }
    }

    private void setUpMultiRowLayout(JSONArray arrayChannel) throws JSONException {
        int size = arrayChannel.length();
        int padding = (int) getResources().getDimension(R.dimen.sdk_row_channel_padding);

        String channelFirst = arrayChannel.getString(0);
        JSONArray a = new JSONArray();
        for (int i = 1; i < size; i++) {
            a.put(arrayChannel.get(i));
        }

        size = a.length();
        int num_row = size / 2 + size % 2;
        for (int row = 0; row < num_row; row++) {
            LinearLayout rowLayout = setUpRowLayout();
            rowLayout.setPadding(padding, 0, padding, padding / 2);

            int cell = 0;
            for (int i = row * 2; i < row * 2 + 2 && i < size; i++) {
                cell++;
                String channel = a.getString(i);

                View cellLayout = null;
                cellLayout = setUpCellLogin(channel, CellType.NORMAL);
                cellLayout.setTag(channel);
                cellLayout.setOnClickListener(new LoginFormClickListener());
                rowLayout.addView(cellLayout);

                if (i % 2 == 0) {
                    rowLayout.addView(setUpColumnMidle());
                }
            }
            if (cell % 2 != 0) {
                rowLayout.addView(setUpEmptyCell());
            }

            channel_container.addView(rowLayout, viewIndex);
            viewIndex++;
        }

        LinearLayout rowLayout = setUpRowLayout();
        String channel = channelFirst;

        View cellLayout = null;
        cellLayout = setUpCellLogin(channel, CellType.ALL_CENTER);
        cellLayout.setTag(channel);
        cellLayout.setOnClickListener(new LoginFormClickListener());
        rowLayout.addView(cellLayout);

        channel_container.addView(rowLayout, 0);
    }

    private void setUpLayout(JSONArray arrayChannel) {
        try {
            if (arrayChannel == null || arrayChannel.length() == 0) return;


            int size = arrayChannel.length();
            if (size == 1) {
                setUpOneRowLayout(arrayChannel);
                //forceLogin(arrayChannel.getString(0));
            } else if (size == 2) {
                setUpTwoRowLayout(arrayChannel);
            } else {
                setUpMultiRowLayout(arrayChannel);
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @TargetApi(Build.VERSION_CODES.HONEYCOMB)
    private ValueAnimator slideAnimator(final View endView, int start, int end) {

        ValueAnimator animator = ObjectAnimator.ofInt(start, end);

        animator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator valueAnimator) {
                //Update Height
                int value = (Integer) valueAnimator.getAnimatedValue();
                ViewGroup.LayoutParams layoutParams = endView.getLayoutParams();
                layoutParams.height = value;
                endView.setLayoutParams(layoutParams);
            }
        });
        return animator;
    }

    @TargetApi(Build.VERSION_CODES.HONEYCOMB)
    private void collapse(final View startView, final View endView, int end) {
        int finalHeight = startView.getHeight();

        ValueAnimator mAnimator = slideAnimator(startView, Math.max(finalHeight, end), Math.min(finalHeight, end));

        mAnimator.addListener(new Animator.AnimatorListener() {
            @Override
            public void onAnimationEnd(Animator animator) {
                startView.setVisibility(View.GONE);
                endView.setVisibility(View.VISIBLE);
            }

            @Override
            public void onAnimationCancel(Animator animation) {

            }

            @Override
            public void onAnimationRepeat(Animator animation) {

            }

            @Override
            public void onAnimationStart(Animator animation) {

            }
        });
        mAnimator.start();
    }

    @SuppressWarnings("deprecation")
    @TargetApi(Build.VERSION_CODES.HONEYCOMB)
    @Override
    public void onClick(View v) {
        if (isClicked) return;

        isClicked = true;
        Handler handler = new Handler();
        handler.postDelayed(new Runnable() {

            @Override
            public void run() {
                isClicked = false;
            }
        }, 150);

        int id = v.getId();
        if (id == R.id.submit_email_guard) {

            String phase = (String) email_guard_container.getTag();
            String email = email_guard.getText().toString().trim();
            String pass = pass_guard.getText().toString();

            if (TextUtils.isEmpty(phase) == false) {

                if (TextUtils.isEmpty(email)) {
                    Utils.showAlertDialog(getContext(), "Chưa nhập email", null);
                } else if (phase.equals(RECOVERY_PASS)) {
                    authenticateExtention.recoveryPassProtectAccountGuest((Activity) getContext(), email, loginListener);
                } else if (TextUtils.isEmpty(pass)) {
                    Utils.showAlertDialog(getContext(), "Chưa nhập password", null);
                } else if (phase.equals(GUARD_GUEST) || phase.equals(GUARD_GUEST_FROM_SUPPORT)) {
                    authenticateExtention.requestCertificateGuest((Activity) getContext(), email, pass, loginListener);
                } else if (phase.equals(RECOVERY_GUEST)) {
                    authenticateExtention.recoveryGuest((Activity) getContext(), email, pass, loginListener);
                }

            }

        } else if (id == R.id.submit_cmnd_number) {

            String cmnd = ((EditText) findViewById(R.id.cmnd_number)).getText().toString().trim();
            if (TextUtils.isEmpty(cmnd)) {
                Utils.showAlertDialog(getContext(), "Chưa nhập số CMND", null);
            } else if (cmnd.length() < 9) {
                Utils.showAlertDialog(getContext(), "Số CMND tối thiểu 9 ký tự", null);
            } else {
                cmnd_number = cmnd;
                gVar.edit().putString("cmnd_number", cmnd).commit();
                Utils.showAlertDialog(getContext(), "Khai báo số CMND thành công", null);
                loginContainer.setVisibility(View.VISIBLE);
                cmnd_container.setVisibility(View.GONE);
                title.setText("Đăng nhập");
            }

        } else if (id == R.id.regis_acc) {

            if (!Utils.isOnline(getContext())) {
                Utils.showAlertDialog(getContext(), "Mạng không ổn định. Vui lòng thử lại sau", null);
            } else if (zingInfo != null) {
                String link = zingInfo.optString("registerURL");
                if (TextUtils.isEmpty(link) == false) {
                    Intent browserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse(link));
                    getContext().startActivity(browserIntent);
                }
            }

        } else if (id == R.id.custom_service) {

            if (custom_service != null) {
                String link = (String) custom_service.getTag();

                if (TextUtils.isEmpty(link) == false) {
                    Intent browserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse(link));
                    getContext().startActivity(browserIntent);
                }
            }

        } else if (id == R.id.request_certificate_guest) {
            ViewGroup.LayoutParams params = email_guard_container.getLayoutParams();
            params.height = support_container.getHeight();

            support_container.setVisibility(View.GONE);

            email_guard_container.setVisibility(View.VISIBLE);
            email_guard_container.setLayoutParams(params);
            findViewById(R.id.title_sent_email_confirm).setVisibility(View.VISIBLE);
            email_guard_container.setTag(GUARD_GUEST_FROM_SUPPORT);
            email_guard.setText("");
            pass_guard.setText("");
            title.setText("Bảo vệ tài khoản");
            tt_continue_login.setVisibility(View.GONE);
            back_form.setVisibility(View.VISIBLE);
            title_forget_pass.setVisibility(View.GONE);
        } else if (id == R.id.retry) {

            retryButton.setEnabled(false);
            GetLoginChannelTask task = new GetLoginChannelTask();
            task.execute();

        } else if (id == R.id.back_login_form) {

            disableZingIDForm();

        } else if (id == R.id.submit) {

            String u = ((EditText) findViewById(R.id.userName)).getText().toString().trim();
            String p = userPass.getText().toString();
            if (TextUtils.isEmpty(u) || TextUtils.isEmpty(p)) {
                Utils.showAlertDialog(getContext(), "Vui lòng nhập tên tài khoản và mật khẩu", null);
            } else {

                zing_me_acc = u;
                trapListener(u, p);
            }

        } else if (id == R.id.tt_continue_login) {

            try {
                authenComplete(new OauthResponse(uId, oauthCode, channel, fbAccessToken, fbExpireTime));
            } catch (Exception e) {
            }

        } else if (id == R.id.support) {

            if (System.currentTimeMillis() - mLastTimeClick < 600) {
                return;
            }

            mLastTimeClick = System.currentTimeMillis();

            showSupportForm();

            loginContainerHeight = loginContainer.getHeight();
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB) {
                if (loginContainerHeight < (int) DeviceInfo.pxFromDp(getContext(), 225)) {
                    ValueAnimator mAnimator = slideAnimator(support_container, loginContainer.getHeight(), (int) DeviceInfo.pxFromDp(getContext(), 225));
                    mAnimator.start();
                }

            } else {
                ViewGroup.LayoutParams params = support_container.getLayoutParams();
                boolean isSmallScreen = getResources().getBoolean(R.bool.isSmallScreen);
                if (isSmallScreen) {
                    params.height = (int) DeviceInfo.pxFromDp(getContext(), 190);
                } else {
                    params.height = (int) DeviceInfo.pxFromDp(getContext(), 225);
                }
                support_container.setLayoutParams(params);

            }

            loginContainer.setVisibility(View.GONE);

        } else if (id == R.id.unremind_confirm) {

            String tag = (String) ico_unremind_confirm.getTag();
            if (tag != null) {
                if (tag.equals("uncheck")) {
                    ico_unremind_confirm.setImageResource(R.drawable.ic_checked);
                    ico_unremind_confirm.setTag("check");
                } else if (tag.equals("check")) {
                    ico_unremind_confirm.setImageResource(R.drawable.ic_uncheck);
                    ico_unremind_confirm.setTag("uncheck");
                }
            }

        } else if (id == R.id.cancel_submit_cmnd) {

            String tag = (String) ico_unremind_confirm.getTag();
            Editor editor = gVar.edit();
            if (tag != null && tag.equals("check")) {
                editor.putBoolean("ignore_protect_" + authStorage.getZaloId(), true);
                editor.commit();
            }
            try {
                authenComplete(new OauthResponse(uId, oauthCode, channel, fbAccessToken, fbExpireTime));
            } catch (Exception e) {
            }

        } else if (id == R.id.submit_cmnd) {

            authenticateExtention.protectAcc((Activity) getContext(), cmnd_number, loginListener);

        } else if (id == R.id.recovery_guest) {
            ViewGroup.LayoutParams params = email_guard_container.getLayoutParams();
            params.height = support_container.getHeight();
            support_container.setVisibility(View.GONE);
            title.setText("Khôi phục tài khoản");

            findViewById(R.id.title_sent_email_confirm).setVisibility(View.GONE);
            email_guard_container.setVisibility(View.VISIBLE);
            email_guard_container.setLayoutParams(params);
            email_guard_container.setTag(RECOVERY_GUEST);
            email_guard.setText("");
            pass_guard.setText("");
            tt_continue_login.setVisibility(View.GONE);
            back_form.setVisibility(View.VISIBLE);
            title_forget_pass.setVisibility(View.VISIBLE);
        } else if (id == R.id.register_identify_number) {
            ViewGroup.LayoutParams params = cmnd_container.getLayoutParams();
            params.height = support_container.getHeight();
            support_container.setVisibility(View.GONE);
            title.setText("Khai báo số CMND");

            cmnd_container.setVisibility(View.VISIBLE);
            cmnd_container.setLayoutParams(params);
            ((EditText) findViewById(R.id.cmnd_number)).setText(cmnd_number);

        } else if (id == R.id.back_form) {
            String tag = (String) email_guard_container.getTag();
            if (TextUtils.isEmpty(tag) == false && tag.equals(RECOVERY_PASS)) {
                title.setText("Khôi phục tài khoản");
                findViewById(R.id.title_sent_email_confirm).setVisibility(View.GONE);
                title_forget_pass.setVisibility(View.VISIBLE);
                email_guard_container.setVisibility(View.VISIBLE);
                email_guard_container.setTag(RECOVERY_GUEST);
                email_guard.setText("");
                pass_guard.setText("");
                findViewById(R.id.form_email_container).setBackgroundDrawable(null);
                findViewById(R.id.form_id_container).setBackgroundResource(R.drawable.zalosdk_white_border_rectangle_corner_partial_transparent);

                findViewById(R.id.form_devider).setVisibility(View.VISIBLE);
                findViewById(R.id.form_pass_container).setVisibility(View.VISIBLE);

                //tt_continue_login.setVisibility(View.GONE);
                //back_form.setVisibility(View.VISIBLE);

            } else {
                email_guard_container.setVisibility(View.GONE);
                showSupportForm();
            }


        } else if (id == R.id.back_form_cmnd) {

            cmnd_container.setVisibility(View.GONE);
            showSupportForm();

        } else if (id == R.id.back_login_form_from_support) {


            title.setText("Đăng nhập");

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB) {
                collapse(support_container, loginContainer, loginContainerHeight);
            } else {
                support_container.setVisibility(View.GONE);
                loginContainer.setVisibility(View.VISIBLE);
            }
        } else if (id == R.id.title_forget_pass) {
            title.setText("Quên mật khẩu");
            title_forget_pass.setVisibility(View.GONE);
            findViewById(R.id.title_sent_email_confirm).setVisibility(View.VISIBLE);
            findViewById(R.id.form_id_container).setBackgroundDrawable(null);
            findViewById(R.id.form_email_container).setBackgroundResource(R.drawable.zalosdk_white_border_rectangle_corner_partial_transparent);
            findViewById(R.id.form_devider).setVisibility(View.GONE);
            findViewById(R.id.form_pass_container).setVisibility(View.GONE);

            email_guard_container.setTag(RECOVERY_PASS);
            email_guard.setText("");

        }

    }

    public boolean isChannelEnabled(LoginChannel channel) {
        SharedPreferences sp = getContext().getSharedPreferences("zacPref", 0);
        String channels = sp.getString("login_channel_array", "[]");
        try {
            JSONArray array = new JSONArray(channels);
            for (int i = 0; i < array.length(); i++) {
                String s = array.getString(i).toUpperCase();
                if (channel.equalsName(s)) {
                    return true;
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }


        return false;
    }

    private void initZaloSDK(Context context) {
        mContext = context;
        zaloSDK = new ZaloSDK(context);
        authStorage = new AuthStorage(context);
    }

    public enum CellType {
        ALL_CENTER,
        TEXT_CENTER,
        NORMAL
    }
    public interface ShowProtectGuestAccountListener {
        boolean onShowProtectGuestAccount(int loginGuestCount, int numberOfShown, Date lastShownTime);
    }

    class GetLoginChannelTask extends AsyncTask<Void, Void, JSONObject> {

        String URL = ServiceMapManager.getInstance().urlFor(ServiceMapManager.KEY_URL_OAUTH);
        HttpClient httpClient = new HttpClient(URL);

        @Override
        protected void onPreExecute() {
            progress_loading.setVisibility(View.VISIBLE);
            loginContainer.setVisibility(View.GONE);
            retryContainer.setVisibility(View.GONE);
        }

        @Override
        protected JSONObject doInBackground(Void... params) {
            try {
                String applicationHashKey = AppInfo.getInstance().getApplicationHashKey();

                HttpGetRequest request = new HttpGetRequest(WEB_MOBILE_LOGIN_INFO_PATH);
                request.addQueryStringParameter(Constant.PARAM_APP_ID, AppInfo.getInstance().getAppId());
                request.addQueryStringParameter("version", DeviceTracking.getInstance().getVersion());
                request.addQueryStringParameter("sign_key", applicationHashKey != null ? applicationHashKey : "");
                request.addQueryStringParameter("pkg_name", AppInfo.getInstance().getPackageName());
                request.addQueryStringParameter("frm", "sdk");
                request.addQueryStringParameter("uid", "" + authStorage.getZaloId());
                request.addQueryStringParameter("av", AppInfo.getInstance().getPackageName());

                Log.d("GetLoginChannel Request", request.getMQueryParams().toString());
                return httpClient.send(request).getJSON();

            } catch (Exception e) {
                return null;
            }

        }

        @Override
        protected void onPostExecute(JSONObject result) {
            progress_loading.setVisibility(View.GONE);
            retryButton.setEnabled(true);

            if (result != null) {
                Log.d("GetLoginChannel Result", result.toString());

                try {
                    int error_code = result.getInt("error");
                    if (error_code == 0) {
                        loginContainer.setVisibility(View.VISIBLE);

                        JSONObject data = result.getJSONObject("data");
                        JSONArray channels = data.getJSONArray("channels");

                        long expiredTime = data.getLong("expiredTime") + System.currentTimeMillis();
                        String forceLogin = data.getString("forceLogin");
                        showWarningInSecond = data.getJSONObject("guestInfo").getLong("showWarningInSecond") * 1000;

                        csInfo = data.optJSONObject("csInfo");
                        zingInfo = data.optJSONObject("zingInfo");

                        warningMsg = result.optString("warningMsg");
                        Editor editor = getContext().getSharedPreferences("zacPref", 0).edit();
                        editor.putLong("showWarningInSecond", showWarningInSecond);
                        if (remind_time_guest_login < System.currentTimeMillis()) {
                            remind_time_guest_login = System.currentTimeMillis() + showWarningInSecond;
                            editor.putLong("remind_time_guest_login", remind_time_guest_login);
                        }

                        editor.putLong("login_channel_expiredTime", expiredTime);
                        editor.putString("login_channel_array", channels.toString());
                        editor.putString("forceLogin", forceLogin);
                        editor.putString("warningMsg", warningMsg);
                        editor.putString("csInfo", csInfo.toString());
                        editor.putString("zingInfo", zingInfo.toString());
                        editor.commit();
                        setUpLayout(channels);
                        forceLogin(forceLogin);
                    } else {
                        String errorMsg = result.optString("errorMsg");
                        if (TextUtils.isEmpty(errorMsg))
                            errorMsg = "Có lỗi xảy ra, vui lòng thử lại sau.";

                        Utils.showAlertDialog(getContext(), errorMsg, null);
                        //listener.onAuthenError(error_code, errorMsg);
                        retryContainer.setVisibility(View.VISIBLE);
                    }
                } catch (Exception e) {
                    retryContainer.setVisibility(View.VISIBLE);
                    e.printStackTrace();

                }
            } else {
                retryContainer.setVisibility(View.VISIBLE);
            }
        }
    }

    private class LoginListener extends OAuthCompleteListener implements IAuthenticateCompleteListener {

        private PaymentProcessingDialog progressDialog;

        @Override
        public void onRequestAccountProtect(int errorCode, String errorMsg) {

            String tag = (String) email_guard_container.getTag();
            Log.d("LoginForm " + tag, "e: " + errorCode + " msg: " + errorMsg);
            if (errorCode == 0) {
                try {
                    if (TextUtils.isEmpty(tag) == false && tag.equals(GUARD_GUEST_FROM_SUPPORT)) {
                        loginContainer.setVisibility(View.VISIBLE);
                        title.setText("Đăng nhập");
                        email_guard_container.setVisibility(View.GONE);

                        if (TextUtils.isEmpty(errorMsg))
                            errorMsg = "Đăng ký bảo vệ tài khoản thành công";

                        Utils.showAlertDialog(getContext(), errorMsg, null);
                    } else if (TextUtils.isEmpty(tag) == false && tag.equals(RECOVERY_PASS)) {

                        title_forget_pass.setVisibility(View.VISIBLE);
                        findViewById(R.id.form_email_container).setBackgroundDrawable(null);
                        findViewById(R.id.form_id_container).setBackgroundResource(R.drawable.zalosdk_white_border_rectangle_corner_partial_transparent);

                        findViewById(R.id.form_devider).setVisibility(View.VISIBLE);
                        findViewById(R.id.form_pass_container).setVisibility(View.VISIBLE);

                        email_guard_container.setVisibility(View.GONE);
                        showSupportForm();

                        if (TextUtils.isEmpty(errorMsg))
                            errorMsg = "Thông tin khôi phục tài khoản đã được gửi";

                        Utils.showAlertDialog(getContext(), errorMsg, null);
                    } else {
                        Utils.showAlertDialog(getContext(), errorMsg, new PaymentAlertDialog.OnOkListener() {

                            @Override
                            public void onOK() {
                                authenComplete(new OauthResponse(uId, oauthCode, channel, fbAccessToken, fbExpireTime));
                            }
                        });
                    }
                } catch (Exception e) {
                    Log.e("onRequestAccountProtect", e);
                }

            } else {
                if (TextUtils.isEmpty(tag) == false && !tag.equals(REGIS_CERTIFICATE)) {
                    if (TextUtils.isEmpty(errorMsg))
                        errorMsg = "Có lỗi xảy ra, vui lòng thử lại sau.";
                    Utils.showAlertDialog(getContext(), errorMsg, null);
                } else {
                    if (!TextUtils.isEmpty(errorMsg))
                        Utils.showAlertDialog(getContext(), errorMsg, null);

                    authenComplete(new OauthResponse(uId, oauthCode, channel, fbAccessToken, fbExpireTime));
                }
            }
        }

        @Override
        public void onAuthenError(int errorCode, String message) {
            Log.i("debuglog", "onAuthenError: errorCode: " + errorCode + " message: " + message);
            switch (errorCode) {
                case ZaloOAuthResultCode.RESULTCODE_USER_REJECT:
                case ZaloOAuthResultCode.RESULTCODE_USER_BACK:
                    return;
                case -1004:
                    try {
                        zaloSDK.unAuthenticate();
                    } catch (InitializedException e) {
                        Log.e("onAuthenError", e);
                    }
                    break;
                default:
                    if (TextUtils.isEmpty(message)) {
                        message = "Có lỗi xảy ra, vui lòng thử lại sau.";
                    }
                    break;
            }

            Utils.showAlertDialog(getContext(), message, null);
        }

        @Override
        public void onGetOAuthComplete(OauthResponse response) {
            Log.i("debuglog", "onGetOAuthComplete: response: " + response.getFacebookAccessToken());
            LoginForm.this.uId = response.getuId();
            LoginForm.this.oauthCode = response.getOauthCode();
            LoginForm.this.channel = response.getChannel();
            LoginForm.this.fbAccessToken = response.getFacebookAccessToken();
            LoginForm.this.fbExpireTime = response.getFacebookExpireTime();
            Log.i("debuglog", "aaa: fbAccessToken: " + LoginForm.this.fbAccessToken);
            saveString(KEY_FBTOKEN, LoginForm.this.fbAccessToken);
            saveString(KEY_FBTOKEN_EXP, String.valueOf(LoginForm.this.fbExpireTime));

            Log.i("debuglog", "aaa: " + getString(KEY_FBTOKEN));
            Editor editor = gVar.edit();

            if (LoginChannel.ZINGME.equals(channel)) {
                editor.putString("zing_me_acc", zing_me_acc).commit();
            } else {
                editor.putString("zing_me_acc", "").commit();
            }

            isGuestCert = authenticateExtention.getIsGuestCertificated();

            if (isGuestCert == 0 && LoginChannel.GUEST.equals(response.getChannel())) {
                if (checkShownShowProtectGuestAccForm()) {
                    return;
                }
            }

            authenComplete(response);

        }

        @Override
        public void onStartLoading() {
            try {
                if (progressDialog == null) {
                    progressDialog = new PaymentProcessingDialog(getContext(), new PaymentProcessingDialog.OnCloseListener() {
                        @Override
                        public void onClose() {

                        }
                    });
                    progressDialog.setTitle("");
                    progressDialog.setCancelable(false);
//					progressDialog.setMessage("Đang xử lý");
                }

                if (getContext() != null && !((Activity) getContext()).isFinishing() && !progressDialog.isShowing()) {
                    progressDialog.show();
                }

            } catch (Exception e) {

            }

        }

        @Override
        public void onFinishLoading() {
            try {
                if (getContext() != null && !((Activity) getContext()).isFinishing() && progressDialog.isShowing()) {
                    progressDialog.dismiss();
                }
            } catch (Exception e) {

            }

        }

        @Override
        public void onAuthenticateSuccess(long uid, @NotNull String code, @NotNull Map<String, ?> data) {

        }

        @Override
        public void onAuthenticateError(int errorCode, @NotNull String message) {

        }
    }

    class LoginFormClickListener implements OnClickListener {
        @Override
        public void onClick(View v) {
            Log.i("debuglog", "go 11111");
            if (System.currentTimeMillis() - mLastTimeClick < 600) {
                return;
            }

            mLastTimeClick = System.currentTimeMillis();

            if (!Utils.isOnline(getContext())) {
                Toast.makeText(getContext(), "Mạng không ổn định. Vui lòng thử lại sau.", Toast.LENGTH_SHORT).show();
                return;
            }

            String tag = (String) v.getTag();
            String lastLogin = authStorage.getLastLoginChannel();
            lastLogin = lastLogin == null ? "" : lastLogin;
            Log.i("debuglog", "go tag: " + tag);
            if (tag.equals("guest")) {

                if ("GUEST".equals(lastLogin)) {
                    zaloSDK.isAuthenticate(new ValidateOAuthCodeCallback() {
                        @Override
                        public void onValidateComplete(boolean validated, int errorCode,
                                                       long userId, String oauthCode) {
                            if (validated) {
                                OauthResponse response = new OauthResponse(userId, oauthCode, LoginChannel.GUEST);
                                LoginForm.this.uId = response.getuId();
                                LoginForm.this.oauthCode = response.getOauthCode();
                                LoginForm.this.channel = response.getChannel();
                                LoginForm.this.fbAccessToken = response.getFacebookAccessToken();
                                LoginForm.this.fbExpireTime = response.getFacebookExpireTime();

                                isGuestCert = authenticateExtention.getIsGuestCertificated();
                                if (isGuestCert == 0) {
                                    if (checkShownShowProtectGuestAccForm()) {
                                        return;
                                    }
                                }
                                Log.i("debuglog", "go thiss00000");
                                authenComplete(response);

                            } else {
                                trapListener(LoginChannel.GUEST);
                            }

                        }
                    });
                } else {
                    trapListener(LoginChannel.GUEST);
                }

            } else if (tag.equals("zalo")) {

                if ("ZALO".equals(lastLogin)) {
                    zaloSDK.isAuthenticate(new ValidateOAuthCodeCallback() {

                        @Override
                        public void onValidateComplete(boolean validated, int errorCode,
                                                       long userId, String oauthCode) {
                            if (validated) {
                                authenComplete(new OauthResponse(userId, oauthCode, LoginChannel.ZALO));
                            } else {
                                trapListener(LoginChannel.ZALO);
                            }

                        }
                    });
                } else {
                    trapListener(LoginChannel.ZALO);
                }
            } else if (tag.equals("facebook")) {

                if ("FACEBOOK".equals(lastLogin)) {
                    zaloSDK.isAuthenticate(new ValidateOAuthCodeCallback() {

                        @Override
                        public void onValidateComplete(boolean validated, int errorCode,
                                                       long userId, String oauthCode) {
                            if (validated) {
                                OauthResponse res = new OauthResponse(userId, oauthCode, LoginChannel.FACEBOOK);
                                try {
//									Log.i("debuglog", "check: " + getString(KEY_FBTOKEN));
//									Log.i("debuglog", "KEY_FBTOKEN_EXP: " + getString(KEY_FBTOKEN_EXP));
                                    res.setFacebookAccessToken(getString(KEY_FBTOKEN));
                                    if (getString(KEY_FBTOKEN_EXP) != null)
                                        res.setFacebookExpireTime(Long.parseLong(getString(KEY_FBTOKEN_EXP)));
                                } catch (Exception ex) {
                                    ex.printStackTrace();
                                }
                                authenComplete(res);
                            } else {
                                trapListener(LoginChannel.FACEBOOK);
                            }

                        }
                    });
                } else {
                    trapListener(LoginChannel.FACEBOOK);
                }
            } else if (tag.equals("zing")) {

                if (RELOGIN_ZING) {
                    zaloSDK.unAuthenticate();
                    enableZingIDForm();
                } else {
                    if ("ZINGME".equals(lastLogin)) {

                        zaloSDK.isAuthenticate(new ValidateOAuthCodeCallback() {

                            @Override
                            public void onValidateComplete(boolean validated, int errorCode,
                                                           long userId, String oauthCode) {
                                if (validated) {
                                    authenComplete(new OauthResponse(userId, oauthCode, LoginChannel.ZINGME));

                                } else {
                                    enableZingIDForm();
                                }

                            }
                        });
                    } else {
                        enableZingIDForm();
                    }
                }
            } else if (tag.equals("google")) {
                if ("GOOGLE".equals(lastLogin)) {
                    zaloSDK.isAuthenticate(new ValidateOAuthCodeCallback() {

                        @Override
                        public void onValidateComplete(boolean validated, int errorCode,
                                                       long userId, String oauthCode) {
                            if (validated) {
                                authenComplete(new OauthResponse(userId, oauthCode, LoginChannel.GOOGLE));

                            } else {
                                trapListener(LoginChannel.GOOGLE);
                            }

                        }
                    });
                } else {
                    trapListener(LoginChannel.GOOGLE);
                }

            }

        }
    }
}

