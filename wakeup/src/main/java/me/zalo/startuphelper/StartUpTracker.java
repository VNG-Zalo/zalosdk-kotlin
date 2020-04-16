package me.zalo.startuphelper;

import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.text.TextUtils;

import com.zing.zalo.zalosdk.java.devicetrackingsdk.DeviceTracking;
import com.zing.zalo.zalosdk.kotlin.analytics.ZingAnalyticsManager;
import com.zing.zalo.zalosdk.kotlin.core.devicetrackingsdk.DeviceTrackingListener;
import com.zing.zalo.zalosdk.kotlin.core.helper.PrivateSharedPreferenceInterface;
import com.zing.zalo.zalosdk.kotlin.core.helper.Storage;
import com.zing.zalo.zalosdk.kotlin.core.helper.Utils;
import com.zing.zalo.zalosdk.kotlin.core.log.Log;

import org.jetbrains.annotations.NotNull;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;

import static com.zing.zalo.zalosdk.kotlin.core.SharedPreferenceConstant.PREF_KEY_LIST_DEVICEID_WAKE_UP;
import static com.zing.zalo.zalosdk.kotlin.core.SharedPreferenceConstant.PREF_NAME_WAKEUP;

/**
 * Created by ThuanNM on 1/3/2019.
 * StartupHelper
 */
class StartUpTracker {

    public static final String WK_PGK_NAME = "wk_pgk_name";
    public static final String WK_NOTIF = "wk_notif";
    public static final String WK_SOURCE = "wk_source";
    public static final String WK_URL = "wk_url";
    public static final String WK_OPEN_PS = "wk_open_ps";
    private static final String KEY_FIREBASE_TOKEN = "com.zing.zalo.sdk.wakeup.firebasetoken";
    private static final String KEY_CHECK_SUBMIT_TOKEN = "com.zing.zalo.sdk.wakeup.isSubmitFirebaseToken";
    private static final String WK_GLOBAL_ID = "wk_globalid";
    private String wakerPkgName;
    private String wakerDeviceId;
    private boolean isNotifOpen = false;
    private boolean isWakeUp = false;
    private boolean isOpenApp = false;
    private boolean isSubmittingToken = false;
    private Context context;


    StartUpTracker(Context context) {
        this.context = context;
    }

    void onAppOpenedFromNotification(Intent intent) {
        if (intent == null || !intent.hasExtra(WK_SOURCE)) return;

        String sourceNotif = intent.getStringExtra(WK_SOURCE);
        if (TextUtils.isEmpty(sourceNotif)) {
            return;
        }

        //add event
        Map<String, String> params = new HashMap<>();
        params.put("sourceFrom", sourceNotif);
        params.put("wakeupInfo", Utils.loadListDeviceIDWakeUp(context));

        if (intent.hasExtra(WK_GLOBAL_ID)) {
            String globalIDReceived = intent.getStringExtra(WK_GLOBAL_ID);
            params.put("sourceGid", globalIDReceived);
        }

        ZingAnalyticsManager.getInstance().addEvent("open_app_by_notification", params);
        ZingAnalyticsManager.getInstance().dispatchEvents();

        isNotifOpen = true;
        wakerPkgName = sourceNotif;
        String token = getSavedFirebaseToken();
        if (!TextUtils.isEmpty(token) && !isFirebaseTokenSubmitted()) {
            submitFirebaseToken(token);
        }
    }


    void onAppOpened(Map<String, String> extra) {
        isOpenApp = true;
        wakerPkgName = "";
        SubmitOpenAppEventAsyncTask task = new SubmitOpenAppEventAsyncTask(extra);
        task.execute(context);

        String token = getSavedFirebaseToken();
        if (!TextUtils.isEmpty(token) && !isFirebaseTokenSubmitted()) {
            submitFirebaseToken(token);
        }
    }

    //được wakeup từ app khác
    void onWakeUp(String fromPkg, String fromDeviceId) {
        isWakeUp = true;
        wakerPkgName = fromPkg;
        wakerDeviceId = fromDeviceId;
        saveListDeviceIdWakeUp(fromDeviceId, System.currentTimeMillis(), fromPkg);

        String token = getSavedFirebaseToken();
        if (!TextUtils.isEmpty(token) && !isFirebaseTokenSubmitted()) {
            submitFirebaseToken(token);
        }
    }

    /**
     * Submit FirebaseToken to server
     *
     * @param firebaseToken firebaseToken
     */
    void setFirebaseToken(String firebaseToken) {
        String oldFirebaseToken = getSavedFirebaseToken();
        if (!TextUtils.isEmpty(firebaseToken) &&
                (!firebaseToken.equals(oldFirebaseToken) || !isFirebaseTokenSubmitted())) {
            saveFirebaseToken(firebaseToken);
            setFirebaseTokenSubmitted(false);
            submitFirebaseToken(firebaseToken);
        }
    }

    private void submitFirebaseToken(final String token) {
        boolean isSubmitFirebaseBefore = isFirebaseTokenSubmitted();
        if (isSubmitFirebaseBefore || isSubmittingToken) {
            return;
        }

        isSubmittingToken = true;

        //get device id
        DeviceTracking.getInstance().getDeviceId(new DeviceTrackingListener() {
            @Override
            public void onComplete(@NotNull String deviceId) {
                if (TextUtils.isEmpty(deviceId)) {
                    Log.e("submitFirebaseToken", "Submit fb token: Can't get device id");
                    isSubmittingToken = false;
                    return;
                }

                //get params
                GenerateTrackingParamsAsyncTask task = new GenerateTrackingParamsAsyncTask(wakerDeviceId,
                        new GenerateTrackingParamsAsyncTask.Callback() {
                            @Override
                            public void onCompleted(String params) {
                                //get params completed
                                SubmitFirebaseTokenAsyncTask.Type type = SubmitFirebaseTokenAsyncTask.Type.OPEN_APP;
//                        if (isWakeUp){
//                            type = SubmitFirebaseTokenAsyncTask.Type.WAKE_UP;
//                            StartupHelperUtil.callbackToWaker(context, params, token, wakerPkgName);
//                        } else if (isNotifOpen){
//                            type = SubmitFirebaseTokenAsyncTask.Type.NOTIF;
//                        }
                                //thuannm: 1. NOTIF is highest priority
                                //2. if app open first, wakeup after open_app -> it will be open_app
                                // Example: app1 open -> wakeup app2 -> app2 wakeup app1->app1 must subtmit open_app
                                if (isNotifOpen) {
                                    type = SubmitFirebaseTokenAsyncTask.Type.NOTIF;
                                } else if (!isOpenApp && isWakeUp) {
                                    type = SubmitFirebaseTokenAsyncTask.Type.WAKE_UP;
                                }
                                //submit token
                                SubmitFirebaseTokenAsyncTask task = new SubmitFirebaseTokenAsyncTask(type, params,
                                        token, wakerPkgName, new SubmitFirebaseTokenAsyncTask.Callback() {
                                    @Override
                                    public void onCompleted(boolean success) {
                                        isSubmittingToken = false;
                                        if (success) {
                                            setFirebaseTokenSubmitted(true);
                                        }
                                    }
                                });
                                task.execute();
                            }
                        });
                task.execute(context);
            }
        });
    }

    void onTerminate() {
    }

    boolean isOpenAppNotificationData(Map<String, String> stringMap) {
        return stringMap.containsKey(WK_NOTIF) && stringMap.containsKey(WK_SOURCE) &&
                (stringMap.containsKey(WK_URL) || stringMap.containsKey(WK_PGK_NAME));
    }

    private boolean isResolveIntent(Context context, Intent intent) {
        PackageManager packageManager = context.getPackageManager();
        return intent != null && intent.resolveActivity(packageManager) != null;
    }

    public void putDeviceIDIntoIntent(Intent intent) {
        if (intent != null) {
            String deviceID = DeviceTracking.getInstance().getDeviceId();
            if (!TextUtils.isEmpty(deviceID)) {
                intent.putExtra(WK_GLOBAL_ID, deviceID);
            }
        }
    }

    private Intent createIntentOpenPlayStore(Context context, Map<String, String> stringMap) {
        Intent intent = null;
        if (stringMap != null && stringMap.containsKey(WK_OPEN_PS)) {
            String packageName = stringMap.get(WK_OPEN_PS);
            intent = new Intent(Intent.ACTION_VIEW, Uri.parse("market://details?id=" + packageName));
            boolean isResolveIntent = isResolveIntent(context, intent);
            if (!isResolveIntent) {
                intent = new Intent(Intent.ACTION_VIEW, Uri.parse("https://play.google.com/store/apps/details?id=" + packageName));
            }
            isResolveIntent = isResolveIntent(context, intent);
            if (!isResolveIntent) {
                return null;
            }
            if (stringMap.containsKey(WK_PGK_NAME)) {
                if (intent != null) {
                    intent.putExtra(WK_NOTIF, stringMap.get(WK_NOTIF));
                    intent.putExtra(WK_SOURCE, stringMap.get(WK_SOURCE));
                    putDeviceIDIntoIntent(intent);
                }
            } else if (stringMap.containsKey(WK_URL)) {
                intent.putExtra(WK_NOTIF, stringMap.get(WK_NOTIF));
                intent.putExtra(WK_SOURCE, stringMap.get(WK_SOURCE));
                putDeviceIDIntoIntent(intent);
            }
        }
        return intent;
    }

    Intent createOpenAppNotificationDataIntent(Context context, Map<String, String> stringMap) {
        if (stringMap.containsKey(WK_PGK_NAME)) {
            Intent intent = context.getPackageManager().getLaunchIntentForPackage(stringMap.get(WK_PGK_NAME));
            if (intent != null) {
                intent.putExtra(WK_NOTIF, stringMap.get(WK_NOTIF));
                intent.putExtra(WK_SOURCE, stringMap.get(WK_SOURCE));
                putDeviceIDIntoIntent(intent);
            }
            boolean isResolveIntent = isResolveIntent(context, intent);
            stringMap.put("can_open", String.valueOf(isResolveIntent));
            ZingAnalyticsManager.getInstance().addEvent("receive_notification", stringMap);
            ZingAnalyticsManager.getInstance().dispatchEvents();
            if (!isResolveIntent) {
                intent = createIntentOpenPlayStore(context, stringMap);
                return intent;
            }
            return intent;
        } else if (stringMap.containsKey(WK_URL)) {
            Intent intent = new Intent(Intent.ACTION_VIEW);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            String uriStr = stringMap.get(WK_URL);
            if (uriStr.contains("?")) {
                uriStr += "&";
            } else {
                uriStr += "?";
            }
            uriStr += WK_SOURCE + "=" + stringMap.get(WK_SOURCE)
                    + "&" + WK_NOTIF + "=" + stringMap.get(WK_NOTIF);

            intent.setData(Uri.parse(uriStr));
            intent.putExtra(WK_NOTIF, stringMap.get(WK_NOTIF));
            intent.putExtra(WK_SOURCE, stringMap.get(WK_SOURCE));
            putDeviceIDIntoIntent(intent);
            boolean isResolveIntent = isResolveIntent(context, intent);
            stringMap.put("can_open", String.valueOf(isResolveIntent));
            ZingAnalyticsManager.getInstance().addEvent("receive_notification", stringMap);
            ZingAnalyticsManager.getInstance().dispatchEvents();
            if (!isResolveIntent) {
                intent = createIntentOpenPlayStore(context, stringMap);
                return intent;
            }
            return intent;
        }
        return null;
    }

    boolean isOpenNotificationIntent(Intent intent) {
        return intent != null && intent.hasExtra(WK_SOURCE) && intent.hasExtra(WK_NOTIF);
    }

    private void saveListDeviceIdWakeUp(String deviceIDWakeUp, long timeStamp, String packageName) {
        try {
            String str = Utils.loadListDeviceIDWakeUp(context);
            JSONObject oldObj;

            try {
                oldObj = new JSONObject(str);
            } catch (Exception ex) {
                oldObj = new JSONObject();
            }

            JSONObject jsonObject = new JSONObject();
            jsonObject.put("zdid", deviceIDWakeUp);
            jsonObject.put("ts", timeStamp);
            oldObj.put(packageName, jsonObject);
            getPrivateStorage(PREF_NAME_WAKEUP).setString(PREF_KEY_LIST_DEVICEID_WAKE_UP, oldObj.toString());
        } catch (Exception ex) {
            Log.e(ex);
        }
    }

    private PrivateSharedPreferenceInterface getPrivateStorage(String storageName) {
        return new Storage(context).createPrivateStorage(storageName);
    }

    //static utils
    private void saveFirebaseToken(String firebaseToken) {
        getPrivateStorage(PREF_NAME_WAKEUP).setString(KEY_FIREBASE_TOKEN, firebaseToken);
    }

    private String getSavedFirebaseToken() {
        return getPrivateStorage(PREF_NAME_WAKEUP).getString(KEY_FIREBASE_TOKEN);
    }

    private boolean isFirebaseTokenSubmitted() {
        return getPrivateStorage(PREF_NAME_WAKEUP).getBoolean(KEY_CHECK_SUBMIT_TOKEN);
    }

    private void setFirebaseTokenSubmitted(boolean isSubmit) {
        getPrivateStorage(PREF_NAME_WAKEUP).setBoolean(KEY_CHECK_SUBMIT_TOKEN, isSubmit);
    }
}
