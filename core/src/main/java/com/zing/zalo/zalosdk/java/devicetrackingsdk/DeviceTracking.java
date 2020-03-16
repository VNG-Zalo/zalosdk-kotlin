package com.zing.zalo.zalosdk.java.devicetrackingsdk;

import com.zing.zalo.zalosdk.kotlin.core.devicetrackingsdk.DeviceTrackingListener;
import com.zing.zalo.zalosdk.kotlin.core.devicetrackingsdk.IDeviceTracking;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class DeviceTracking implements IDeviceTracking {

    private static DeviceTracking instance = new DeviceTracking();

    private DeviceTracking() { }
    private com.zing.zalo.zalosdk.kotlin.core.devicetrackingsdk.DeviceTracking kotlinDeviceTracking = com.zing.zalo.zalosdk.kotlin.core.devicetrackingsdk.DeviceTracking.getInstance();

    public static DeviceTracking getInstance() {
        return instance;
    }

    @Deprecated()
    @Override
    public void initDeviceTracking() {
    }

    @Override
    public String getDeviceId() {

        return com.zing.zalo.zalosdk.kotlin.core.devicetrackingsdk.DeviceTracking.getInstance().getDeviceId();
    }

    @Override
    public void getDeviceId(@Nullable DeviceTrackingListener listener) {
        com.zing.zalo.zalosdk.kotlin.core.devicetrackingsdk.DeviceTracking.getInstance().getDeviceId(listener);
    }

    @NotNull
    @Override
    public String getVersion() {
        return com.zing.zalo.zalosdk.kotlin.core.devicetrackingsdk.DeviceTracking.getInstance().getVersion();
    }

    public long getDeviceIdExpireTime() {
        return kotlinDeviceTracking.getDeviceIdExpiredTime();
    }

}

