package me.zalo.startuphelper;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

public class StartupHelperReceiver extends BroadcastReceiver {
    public static final boolean DEBUG = StartupHelperUtil.DEBUG;
    private static final String TAG = "StartupHelperReceiver";

    @Override
    public void onReceive(final Context context, Intent intent) {
        if (DEBUG) Log.d(TAG, "action: " + intent.getAction());
        if (intent.getAction() == null) return;
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.KITKAT
                && Intent.ACTION_PACKAGE_ADDED.equals(intent.getAction())) {
            final int uid = intent.getIntExtra(Intent.EXTRA_UID, 0);
            if (uid == 0) return;
            TaskExecutor.queueRunnable(new Runnable() {
                @Override
                public void run() {
                    StartupHelperUtil.startHelperProvider(context, uid);
                }
            });
        }
    }
}
