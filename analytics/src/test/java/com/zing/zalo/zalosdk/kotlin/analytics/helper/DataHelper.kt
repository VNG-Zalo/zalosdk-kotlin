package com.zing.zalo.zalosdk.kotlin.analytics.helper


import com.zing.zalo.zalosdk.kotlin.analytics.model.Event
import com.zing.zalo.zalosdk.kotlin.core.helper.AppInfo
import io.mockk.every
import io.mockk.mockkObject

object DataHelper {
    const val EVENT_STORED_IN_DEVICE =
        "{\"events\":[{\"params\":{\"name\":\"Luke\",\"age\":\"0\"},\"action\":\"0\"},{\"params\":{\"name\":\"Luke\",\"age\":\"1\"},\"action\":\"1\"},{\"params\":{\"name\":\"Luke\",\"age\":\"2\"},\"action\":\"2\"},{\"params\":{\"name\":\"Luke\",\"age\":\"3\"},\"action\":\"3\"},{\"params\":{\"name\":\"Luke\",\"age\":\"4\"},\"action\":\"4\"},{\"params\":{\"name\":\"Luke\",\"age\":\"5\"},\"action\":\"5\"},{\"params\":{\"name\":\"Luke\",\"age\":\"6\"},\"action\":\"6\"},{\"params\":{\"name\":\"Luke\",\"age\":\"7\"},\"action\":\"7\"},{\"params\":{\"name\":\"Luke\",\"age\":\"8\"},\"action\":\"8\"},{\"params\":{\"name\":\"Luke\",\"age\":\"9\"},\"action\":\"9\"},{\"params\":{\"name\":\"Luke\",\"age\":\"10\"},\"action\":\"10\"}]}"

    const val preloadInfo = "preload_info"
    fun mockEvent(): Event {
        val timeStamp = System.currentTimeMillis()
        val action = "action-$timeStamp"
        val params = mutableMapOf<String, String>()


        params["name"] = "datahelper-$timeStamp"
        params["age"] = timeStamp.toString()
        return Event(action, params, timeStamp)
    }
}

object DeviceHelper {
    const val deviceId = "device_id"
    const val adsId = "ads_id"
}


object AppInfoHelper {
    const val appId = "appId_123456"
    const val scanId = "scanId_3"
    const val appName = "appName"
    const val versionName = "2"
    const val applicationHashKey = "applicationHashKey"
    const val advertiserId = "advertiserId"
    const val packageName = "package_name"
    const val referrer = "referrer"
    const val installerPackageName = "installer_packageName"
    const val installedDate = "installed_date"
    const val firstInstalledDate = "installed_date"
    const val lastUpdate = "last_update"
    const val firstRunDate = "first_run_date"
    const val versionCode = 4L
    fun setup() {
        mockkObject(AppInfo, recordPrivateCalls = true)

        every { AppInfo.getInstance().getAppId() } returns appId
        every { AppInfo.getInstance().getAppName() } returns appName
        every { AppInfo.getInstance().getVersionName() } returns versionName
        every { AppInfo.getInstance().getApplicationHashKey() } returns applicationHashKey
        every { AppInfo.getInstance().getPackageName() } returns packageName
        every { AppInfo.getInstance().getReferrer() } returns referrer
        every { AppInfo.getInstance().getInstallerPackageName() } returns installerPackageName
        every { AppInfo.getInstance().getInstallDate() } returns installedDate
        every { AppInfo.getInstance().getFirstInstallDate() } returns firstInstalledDate
        every { AppInfo.getInstance().getLastUpdate() } returns lastUpdate
        every { AppInfo.getInstance().getFirstRunDate() } returns firstRunDate
        every { AppInfo.getInstance().getVersionCode() } returns versionCode
        every { AppInfo.getInstance().isPreInstalled() } returns true
        every { AppInfo.getInstance().getPreloadChannel() } returns "preloadChanel"


        every { AppInfo.getInstance().extracted } returns true
    }
}