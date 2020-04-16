package me.zalo.startuphelper;

import android.content.Context;
import android.os.AsyncTask;

import com.zing.zalo.zalosdk.kotlin.core.devicetrackingsdk.model.PreloadInfo;
import com.zing.zalo.zalosdk.kotlin.core.helper.AppInfo;
import com.zing.zalo.zalosdk.kotlin.core.helper.DeviceInfo;
import com.zing.zalo.zalosdk.kotlin.core.helper.Utils;

import java.util.HashMap;
import java.util.Map;

public class SubmitOpenAppEventAsyncTask extends AsyncTask<Context, Void, Void> {
    private final Map<String, String> extra;

    SubmitOpenAppEventAsyncTask(Map<String, String> extra) {
        this.extra = extra;
    }

    @Override
    protected Void doInBackground(Context... contexts) {
        Context context = contexts[0];
        Map<String, String> params = new HashMap<>();
        PreloadInfo preloadInfo = DeviceInfo.INSTANCE.getPreloadInfo(context);

        if (preloadInfo != null) {
            params.put("preloadDefault", AppInfo.getInstance().getPreloadChannel());
            params.put("preload", preloadInfo.getPreload());
            if (!preloadInfo.isPreloaded()) {
                params.put("preloadFailed", preloadInfo.getError());
            }
        }

        String listDeviceId = Utils.INSTANCE.loadListDeviceIDWakeUp(context);
        params.put("wakeupInfo", listDeviceId);

        if (extra != null) {
            params.putAll(extra);
        }

//        ZingAnalyticsManager.getInstance().addEvent("open_app", params);
//        ZingAnalyticsManager.getInstance().dispatchEvents();
        return null;
    }
}
