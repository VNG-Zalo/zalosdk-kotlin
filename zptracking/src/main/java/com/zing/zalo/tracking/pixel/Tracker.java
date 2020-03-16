package com.zing.zalo.tracking.pixel;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.Context;
import android.location.Location;
import android.location.LocationManager;
import android.os.Bundle;
import android.text.TextUtils;

import com.zing.zalo.tracking.pixel.abstracts.IAdvertiserIdProvider;
import com.zing.zalo.tracking.pixel.abstracts.IGlobalIdDProvider;
import com.zing.zalo.tracking.pixel.abstracts.ILocationProvider;
import com.zing.zalo.tracking.pixel.impl.LogUploader;
import com.zing.zalo.tracking.pixel.impl.Storage;
import com.zing.zalo.tracking.pixel.impl.TrackerImpl;
import com.zing.zalo.zalosdk.java.devicetrackingsdk.DeviceTracking;
import com.zing.zalo.zalosdk.kotlin.core.helper.AppInfo;
import com.zing.zalo.zalosdk.kotlin.core.helper.DeviceInfo;
import com.zing.zalo.zalosdk.kotlin.core.helper.Utils;
import com.zing.zalo.zalosdk.kotlin.core.http.HttpClient;

import java.util.HashMap;
import java.util.Map;


public class Tracker {
    private static final Map<Long, Tracker> sTrackers = new HashMap<>();

    private TrackerImpl mTrackerImpl;
    private Storage mStorage;

    /**
     * Init SDK, must call this method before others
     *
     * @param context Application context
     */
    public Tracker(Context context, long pixelId) {
        Context ctx = context.getApplicationContext();

        mTrackerImpl = new TrackerImpl(ctx);
        mStorage = new Storage(ctx, pixelId);
        mStorage.setAppId(AppInfo.getInstance().getAppId());
        LogUploader logUploader = new LogUploader(new HttpClient(""));
        logUploader.setMobileNetworkCode(DeviceInfo.getMobileNetworkCode(ctx));
        DataProvider dataProvider = new DataProvider(ctx);
        mTrackerImpl.setStorage(mStorage);
        mTrackerImpl.setLogUploader(logUploader);
        mTrackerImpl.setIAdsIdProvider(dataProvider);
        mTrackerImpl.setIGlobalIdDProvider(dataProvider);
        mTrackerImpl.setILocationProvider(dataProvider);
        mTrackerImpl.start();
    }

    public static synchronized Tracker newInstance(Context context, long pixelId) {
        Tracker tracker = sTrackers.get(pixelId);
        if (tracker == null) {
            tracker = new Tracker(context, pixelId);
            sTrackers.put(pixelId, tracker);
        }

        return tracker;
    }

    public static synchronized void destroyInstance(Tracker tracker) {
        for (Long key : sTrackers.keySet()) {
            Tracker val = sTrackers.get(key);
            if (val == tracker) {
                sTrackers.remove(key);
                return;
            }
        }
    }

    /**
     * Set zalo app Id
     * Default: Zalo app id from Zalo SDK
     *
     * @param appId zalo appId
     */
    public void setAppId(String appId) {
        mStorage.setAppId(appId);
    }


    /**
     * Set custom user info, support only primary data types
     *
     * @param userInfo user information:
     *                 "uid": "", //user id
     *                 "account_created_time": 1234, //unix ts,
     *                 "city": "",
     *                 "country": "",
     *                 "currency": "VND", //ISO 4217 currency code
     *                 "gender": "", //m: male, f: female, o: others,
     *                 "install_source": "..",
     *                 "language": "VN", //ISO 639-1 codes,
     *                 "user_type": "",
     */
    public void setUserInfo(Bundle userInfo) {
        mStorage.setUserInfo(userInfo == null ? new Bundle() : userInfo);
    }

    /**
     * Track event
     *
     * @param name name of the event
     */
    public void track(String name) {
        track(name, null);
    }

    /**
     * Track event
     *
     * @param name   name of the event
     * @param params addition info, support only primary data types
     */
    public void track(String name, Map<String, Object> params) {
        mTrackerImpl.track(name, params == null ? new HashMap<String, Object>() : params);
    }

    /**
     * Set max number of event stored
     *
     * @param numberOfEvents default 500
     */
    private void setMaxEventStored(int numberOfEvents) {
        mStorage.setMaxEventStored(numberOfEvents);
    }

    /**
     * Set dispatch event interval (ms)
     *
     * @param interval default 120 * 1000
     */
    private void setDispatchInterval(long interval) {
        mTrackerImpl.setDispatchInterval(interval);
    }

    /**
     * Set store event interval (ms)
     *
     * @param interval default 60 * 1000
     */
    private void setStoreInterval(long interval) {
        mTrackerImpl.setStoreInterval(interval);
    }

    private static class DataProvider implements IGlobalIdDProvider, IAdvertiserIdProvider, ILocationProvider {
        private Context mContext;

        DataProvider(Context context) {
            mContext = context;
        }

        @Override
        public String getAdsId() {
            return DeviceInfo.getAdvertiseID(mContext);
        }

        @Override
        public String globalId() {
            DeviceTracking dt = DeviceTracking.getInstance();
            if (TextUtils.isEmpty(DeviceTracking.getInstance().getDeviceId())) {
//                BaseAppInfoStorage storage = new BaseAppInfoStorage(mContext);
//                String appId = AppInfo.getInstance().getAppId();
//                dt.initDeviceTracking(mContext, storage, appId);
                dt.initDeviceTracking();
            }

            return dt.getDeviceId();
        }

        @SuppressLint("MissingPermission")
        @Override
        public String getLocation() {
            try {
                if (Utils.isPermissionGranted(mContext, Manifest.permission.ACCESS_FINE_LOCATION) ||
                        Utils.isPermissionGranted(mContext, Manifest.permission.ACCESS_COARSE_LOCATION)) {
                    LocationManager locationManager = (LocationManager) mContext
                            .getSystemService(Context.LOCATION_SERVICE);
                    if (locationManager != null) {
                        Location loc = locationManager.getLastKnownLocation(LocationManager.PASSIVE_PROVIDER);
                        if (loc != null) {
                            return loc.getLatitude() + "," + loc.getLatitude();
                        }
                    }
                }
            } catch (Exception ignored) {
            }
            return "0.0:0.0";
        }
    }
}
