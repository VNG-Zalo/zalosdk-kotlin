package me.zalo.startuphelper;

import android.content.Context;
import android.os.AsyncTask;
import android.os.Build;
import android.text.TextUtils;

import com.zing.zalo.zalosdk.java.devicetrackingsdk.DeviceTracking;
import com.zing.zalo.zalosdk.kotlin.core.devicetrackingsdk.SdkTracking;
import com.zing.zalo.zalosdk.kotlin.core.devicetrackingsdk.model.PreloadInfo;
import com.zing.zalo.zalosdk.kotlin.core.helper.AppInfo;
import com.zing.zalo.zalosdk.kotlin.core.helper.DeviceInfo;
import com.zing.zalo.zalosdk.kotlin.core.helper.Utils;

import org.json.JSONObject;

import java.util.Date;
import java.util.Locale;

class GenerateTrackingParamsAsyncTask extends AsyncTask<Context, Void, String> {
    private final Callback callback;
    private final String wakerDeviceId;

    GenerateTrackingParamsAsyncTask(String wakerDeviceId, Callback callback) {
        this.callback = callback;
        this.wakerDeviceId = wakerDeviceId;
    }

    @Override
    protected String doInBackground(Context... contexts) {
        JSONObject data = new JSONObject();
        try {
            Context context = contexts[0];

            if (!TextUtils.isEmpty(wakerDeviceId)) {
                data.put("wakeupZdid", wakerDeviceId);
            }

            data.put("pkgName", context.getPackageName());
            data.put("appId", AppInfo.getInstance().getAppId());
            data.put("pl", "android");
            data.put("osv", DeviceInfo.INSTANCE.getOSVersion());

            data.put("sdkv", Utils.getSDKVersion());
            data.put("sdkId", SdkTracking.getInstance().getSDKId());
            data.put("an", AppInfo.getInstance().getAppName());
            data.put("av", AppInfo.getInstance().getVersionName());
            data.put("dId", DeviceInfo.INSTANCE.getAdvertiseID(context));
            data.put("aId", DeviceInfo.INSTANCE.getAndroidId());
            data.put("ser", DeviceInfo.INSTANCE.getSerial());
            data.put("mod", DeviceInfo.INSTANCE.getModel());
            data.put("ss", DeviceInfo.INSTANCE.getScreenSize(context));
            data.put("mac", DeviceInfo.INSTANCE.getWLANMACAddress(context));
            data.put("conn", DeviceInfo.INSTANCE.getConnectionType(context));
            data.put("mno", DeviceInfo.INSTANCE.getMobileNetworkCode(context));
            data.put("zdid", DeviceTracking.getInstance().getDeviceId());
            data.put("adid", DeviceInfo.INSTANCE.getAdvertiseID(context));

            data.put("ts", String.valueOf(new Date().getTime()));
            data.put("brd", DeviceInfo.INSTANCE.getBrand());
            data.put("dev", Build.DEVICE);
            data.put("prd", DeviceInfo.INSTANCE.getProduct());
            data.put("adk_ver", Build.VERSION.SDK_INT);
            data.put("mnft", DeviceInfo.INSTANCE.getManufacturer());
            data.put("dev_type", Build.TYPE);
            data.put("avc", AppInfo.getInstance().getVersionCode());
            data.put("lang", Locale.getDefault().toString());
            data.put("dpi", context.getResources().getDisplayMetrics().density);

            PreloadInfo preloadInfo = DeviceInfo.INSTANCE.getPreloadInfo(context);
            data.put("preload", preloadInfo.getPreload());
            data.put("preloadDefault", AppInfo.getInstance().getPreloadChannel());
            if (!preloadInfo.isPreloaded()) {
                data.put("preloadFailed", preloadInfo.getError());
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
        return data.toString();
    }

    @Override
    protected void onPostExecute(String s) {
        if (callback != null) {
            callback.onCompleted(s);
        }
        super.onPostExecute(s);
    }

    interface Callback {
        void onCompleted(String params);
    }
}
