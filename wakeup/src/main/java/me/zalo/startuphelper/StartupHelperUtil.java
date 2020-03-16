package me.zalo.startuphelper;

import android.annotation.TargetApi;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.database.Cursor;
import android.net.Uri;
import android.os.Build;
import android.text.TextUtils;
import android.util.Log;

import com.zing.zalo.zalosdk.java.devicetrackingsdk.DeviceTracking;
import com.zing.zalo.zalosdk.java.settings.SettingsManager;
import com.zing.zalo.zalosdk.kotlin.core.devicetrackingsdk.DeviceTrackingListener;
import com.zing.zalo.zalosdk.kotlin.core.helper.Utils;

import org.jetbrains.annotations.NotNull;

import java.util.List;
import java.util.Map;


/**
 * StartupHelperUtil
 * Created by khanhtm on 3/14/18.
 */

public class StartupHelperUtil {
    public static final String TAG = "wakeuplib";
    static final boolean DEBUG = BuildConfig.DEBUG;
    private static final String ACTION_STARTUP_PROVIDER = "me.zalo.startuphelper.intent.action.STARTUP_HELPER_PROVIDER";
    private static final String START_TYPE_STARTUP = "startup";
    private static final String START_TYPE_INSTALL_EVENT = "install_event";
    private static final String START_TYPE_CALLBACK_EVENT = "callback_event";
    private static final String EXTRA_KEY_START_FROM = "z_startFrom";
    private static final String EXTRA_KEY_START_TYPE = "z_startType";
    private static final String EXTRA_KEY_FIREBASE_TOKEN_TYPE = "z_firebaseToken";
    private static final String EXTRA_KEY_SOURCE_WAKEUP_TYPE = "z_source_wakeup";
    private static final String EXTRA_KEY_SOURCE_DEVICE_DATA = "z_source_deviceData";
    private static final String EXTRA_KEY_Z_DEVICE_ID = "z_deviceid";
    private static final String EXTRA_KEY_Z_DEVICE_ID_EXPIRED_TIME = "z_deviceid_expiredTime";
    private static final String[] SUPPORTED_APPS = {"com.vng.zing.zdice", "com.zing.zalo", "com.epi", "com.zing.mp3", "com.zing.tv3", "com.vng.inputmethod.labankey"};
    private static final Object receiverLocker = new Object();
    private static StartUpTracker startUpTracker;
    private static StartupHelperReceiver startupHelperReceiver;
    private static long lastWakeup = 0;

    public static void onApplicationCreate(final Context context) {
        try {
            getStartUpTracker(context); // init startup tracker
            if (context == null) return;
            TaskExecutor.queueRunnable(new Runnable() {
                @Override
                public void run() {
                    try {
                        lastWakeup = SettingsManager.getInstance().getLastTimeWakeup(context);

                        long wakeupInterval = SettingsManager.getInstance().getWakeUpInterval(context);
                        if (DEBUG) Log.d(TAG, "wakeupInterval " + wakeupInterval);
                        if (Installation.isFirstCall(context) ||
                                (SettingsManager.getInstance().getWakeupSetting(context)
                                        && (wakeupInterval > 0 && lastWakeup + wakeupInterval < System.currentTimeMillis()))) {
                            if (DEBUG) Log.d(TAG, "first launch - broadcast to our friendly apps");
                            DeviceTracking.getInstance().getDeviceId(new DeviceTrackingListener() {
                                @Override
                                public void onComplete(@NotNull String result) {
                                    lastWakeup = System.currentTimeMillis();
                                    SettingsManager.getInstance().saveLastTimeWakeUp(context, lastWakeup);
                                    startHelperProviders(context);
                                }
                            });
                        } else {
                            if (DEBUG) Log.d(TAG, "not is first launch!");
                        }
                    } catch (Exception e) {
                        if (DEBUG) e.printStackTrace();
                    }
                }
            });
            ApplicationInfo applicationInfo = context.getApplicationInfo();
            if (Build.VERSION.SDK_INT < Build.VERSION_CODES.O || applicationInfo.targetSdkVersion < Build.VERSION_CODES.O) {
                return;
            }

            synchronized (receiverLocker) {
                if (startupHelperReceiver != null) return;
                startupHelperReceiver = new StartupHelperReceiver();
                IntentFilter intentFilter = new IntentFilter();
                intentFilter.addAction(Intent.ACTION_PACKAGE_ADDED);
                intentFilter.addDataScheme("package");
                context.registerReceiver(startupHelperReceiver, intentFilter);
            }
        } catch (Exception e) {
            if (DEBUG) e.printStackTrace();
        }
    }

    public static void onApplicationTerminate(Context context) {
        try {
            getStartUpTracker(context).onTerminate();
            if (context == null) return;
            synchronized (receiverLocker) {
                if (startupHelperReceiver != null) {
                    context.unregisterReceiver(startupHelperReceiver);
                    startupHelperReceiver = null;
                }
            }
        } catch (Exception e) {
            if (DEBUG) e.printStackTrace();
        }
    }

    @TargetApi(Build.VERSION_CODES.KITKAT)
    private static void startHelperProviders(Context context) {
        try {
            Intent intent = new Intent(ACTION_STARTUP_PROVIDER);
            PackageManager pm = context.getPackageManager();
            List<ResolveInfo> resolveInfoList = null;
            if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.KITKAT) {
                resolveInfoList = pm.queryIntentContentProviders(intent, 0);
            }
            if (resolveInfoList == null) {
                return;
            }
            if (resolveInfoList.size() == 0) {
                return;
            }
            for (ResolveInfo providerInfo : resolveInfoList) {
                try {
                    String packageName = providerInfo.providerInfo.packageName;
                    if (!isSupportedApp(context.getPackageName()) || packageName.equals(context.getPackageName()))
                        continue;

                    String uriStr = "content://" + providerInfo.providerInfo.authority
                            + "?" + EXTRA_KEY_START_FROM + "=" + context.getPackageName() + "&" + EXTRA_KEY_START_TYPE + "=" + START_TYPE_STARTUP
                            + buildParamUriZDeviceID();

                    Uri uri = Uri.parse(uriStr);
                    if (uri == null) continue;

                    Cursor cursor = context.getContentResolver().query(uri, null, null, null, null);
                    if (cursor != null) cursor.close();

                } catch (Exception e) {
                    if (DEBUG) e.printStackTrace();
                }
            }
        } catch (Exception e) {
            if (DEBUG) e.printStackTrace();
        }
    }


    private static boolean isSupportedApp(String packageName) {
        if (DEBUG) {
            Log.d(TAG, "support: " + packageName);
            return true;
        }
        for (String pkg : SUPPORTED_APPS) {
            if (pkg.equals(packageName)) return true;
        }
        return false;
    }

    private static String buildParamUriZDeviceID() {
        String uriStr = "";

        String deviceId = DeviceTracking.getInstance().getDeviceId();
        if (!TextUtils.isEmpty(deviceId)) {
            long expireTime = DeviceTracking.getInstance().getDeviceIdExpireTime();
            uriStr += "&" + EXTRA_KEY_Z_DEVICE_ID + "=" + deviceId;
            uriStr += "&" + EXTRA_KEY_Z_DEVICE_ID_EXPIRED_TIME + "=" + expireTime;
        }
        return uriStr;
    }

    static void startHelperProvider(Context context, int appUID) {
        try {
            PackageManager pm = context.getPackageManager();
            String packageName = pm.getNameForUid(appUID);
            if (packageName == null) return;
            int index = -1;
            if ((index = packageName.indexOf(':')) > 0) {
                packageName = packageName.substring(0, index);
            }
            if (isSupportedApp(packageName)) {
                startHelperProvider(context, packageName);
            }
        } catch (Exception e) {
            if (DEBUG) e.printStackTrace();
        }
    }

    @TargetApi(Build.VERSION_CODES.KITKAT)
    private static void startHelperProvider(Context context, String targetPackage) {
        try {
            Intent intent = new Intent(ACTION_STARTUP_PROVIDER);
            PackageManager pm = context.getPackageManager();
            List<ResolveInfo> resolveInfoList = pm.queryIntentContentProviders(intent, 0);
            if (resolveInfoList == null) {
                return;
            }
            if (resolveInfoList.size() == 0) {
                return;
            }
            for (ResolveInfo providerInfo : resolveInfoList) {
                try {
                    String packageName = providerInfo.providerInfo.packageName;
                    if (packageName.equals(context.getPackageName()) || !packageName.equals(targetPackage))
                        continue;

                    String uriStr = "content://" + providerInfo.providerInfo.authority
                            + "?" + EXTRA_KEY_START_FROM + "=" + context.getPackageName() + "&" + EXTRA_KEY_START_TYPE + "=" + START_TYPE_INSTALL_EVENT
                            + buildParamUriZDeviceID();

                    Uri uri = Uri.parse(uriStr);
                    if (uri == null) continue;

                    Cursor cursor = context.getContentResolver().query(uri, null, null, null, null);
                    if (cursor != null) cursor.close();
                    return;
                } catch (Exception e) {
                    if (DEBUG) e.printStackTrace();
                }
            }
        } catch (Exception e) {
            if (DEBUG) e.printStackTrace();
        }
    }

    /**
     * Set and Submit FirebaseToken to server
     *
     * @param firebaseToken firebaseToken
     */
    public static void setFirebaseToken(Context context, String firebaseToken) {
        getStartUpTracker(context).setFirebaseToken(firebaseToken);
    }

    /**
     * Check is open app data
     *
     * @param stringMap stringMap
     * @return intent
     */
    public static boolean isOpenAppNotificationData(Context context, Map<String, String> stringMap) {
        if (stringMap == null) {
            return false;
        }
        return getStartUpTracker(context).isOpenAppNotificationData(stringMap);
    }

    /**
     * Create intent open app for notification
     *
     * @param stringMap stringMap
     * @return intent
     */
    public static Intent createOpenAppNotificationDataIntent(Context context, Map<String, String> stringMap) {
        if (stringMap == null) {
            return null;
        }

        return getStartUpTracker(context).createOpenAppNotificationDataIntent(context, stringMap);
    }

    /**
     * Call this when your app is open
     *
     * @param intent intent
     */
    public static void setOpenAppSource(Context context, Intent intent) {
        getStartUpTracker(context).onAppOpenedFromNotification(intent);
    }

    /**
     * Check is opendata Intent
     *
     * @param intent intent
     * @return boolean
     */
    public static boolean isOpenNotificationIntent(Context context, Intent intent) {
        return getStartUpTracker(context).isOpenNotificationIntent(intent);
    }

    /**
     * must add this method onCreate() on main Activity of your app
     *
     * @param context ApplicationContext
     * @param params  parameters
     */
    public static void sendEventOpenApp(Context context, Map<String, String> params) {
        getStartUpTracker(context).onAppOpened(params);
    }

    /**
     * StartupHelperUtil.query(Uri uri, String[] projection, String selection,String[] selectionArgs, String sortOrder)
     * Call this function on your ContentProvider
     * public Cursor query(Uri uri, String[] projection, String selection,
     * String[] selectionArgs, String sortOrder)
     *
     * @param context       context
     * @param uri           uri
     * @param projection    projection
     * @param selection     selection
     * @param selectionArgs selectionArgs
     * @param sortOrder     sortOrder
     */
    public static void wakeUpQueryContentProvider(Context context, Uri uri, String[] projection, String selection,
                                                  String[] selectionArgs, String sortOrder) {
        getStartUpTracker(context);
        String query = uri.getQuery();
        if (TextUtils.isEmpty(query)) return;

        StartUpTracker tracker = getStartUpTracker(context);
        String fromPkg = uri.getQueryParameter(EXTRA_KEY_START_FROM);
        if (!query.contains(START_TYPE_CALLBACK_EVENT)) {
            //được wakeup
            String fromDeviceId = null;
            if (query.contains(EXTRA_KEY_Z_DEVICE_ID)) {
                fromDeviceId = uri.getQueryParameter(EXTRA_KEY_Z_DEVICE_ID);
            }
            tracker.onWakeUp(fromPkg, fromDeviceId);
        }
    }

    public static String getVersionZaloSDK() {
        return Utils.getSDKVersion();
    }

    private static synchronized StartUpTracker getStartUpTracker(Context context) {
        if (startUpTracker == null) {
            startUpTracker = new StartUpTracker(context);
        }

        return startUpTracker;
    }
}
