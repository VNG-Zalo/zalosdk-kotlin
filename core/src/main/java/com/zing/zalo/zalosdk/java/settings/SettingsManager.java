package com.zing.zalo.zalosdk.java.settings;

import android.content.Context;

public class SettingsManager {

    private static SettingsManager instance= new SettingsManager();
    private SettingsManager(){}
    private com.zing.zalo.zalosdk.kotlin.core.settings.SettingsManager kotlinSettingManager = com.zing.zalo.zalosdk.kotlin.core.settings.SettingsManager.getInstance();

    public static SettingsManager getInstance()
    {
        return instance;
    }

    @Deprecated
    public long getWakeUpInterval(Context context) {
        return kotlinSettingManager.getWakeUpInterval();
    }

    @Deprecated
    public Boolean getWakeupSetting(Context context) {
        return kotlinSettingManager.getWakeUpSetting();
    }

    @Deprecated
    public long getLastTimeWakeup(Context context) {
        return kotlinSettingManager.getLastTimeWakeup();
    }

    @Deprecated
    public void saveLastTimeWakeUp(Context context, long value) {
        kotlinSettingManager.saveLastTimeWakeup(value);
    }

    @Deprecated
    public Boolean isUseWebViewLoginZalo(Context context) {
        return kotlinSettingManager.isUseWebViewLoginZalo();
    }

    @Deprecated
    public Boolean isLoginViaBrowser(Context context) {
        return kotlinSettingManager.isLoginViaBrowser();
    }

    @Deprecated
    public Long getExpiredTime(Context context) {
        return kotlinSettingManager.getExpiredTime();
    }

    ///////
    public long getWakeUpInterval() {
        return kotlinSettingManager.getWakeUpInterval();
    }

    public Boolean getWakeupSetting() {
        return kotlinSettingManager.getWakeUpSetting();
    }

    public long getLastTimeWakeup() {
        return kotlinSettingManager.getLastTimeWakeup();
    }

    public void saveLastTimeWakeUp(long value) {
        kotlinSettingManager.saveLastTimeWakeup(value);
    }

    public Boolean isUseWebViewLoginZalo() {
        return kotlinSettingManager.isUseWebViewLoginZalo();
    }

    public Boolean isLoginViaBrowser() {
        return kotlinSettingManager.isLoginViaBrowser();
    }

    public Long getExpiredTime() {
        return kotlinSettingManager.getExpiredTime();
    }
}
