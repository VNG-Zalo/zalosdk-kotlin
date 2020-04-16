package com.zing.zalo.zalosdk.kotlin.core.helper

import io.mockk.every
import io.mockk.mockkObject

object AppInfoHelper {
    const val appId = "appId_123456"
    const val scanId = "scanId_3"
    const val appName = "appName"
    const val versionName = "2"
    const val applicationHashKey = "applicationHashKey"
    const val advertiserId = "advertiserId"
    const val packageName = "package_name"
    const val referrer = "referrer"

    fun setup() {
        DeviceInfo.advertiserId = advertiserId


        mockkObject(AppInfo)
        every { AppInfo.getInstance().getAppId() } returns appId
        every { AppInfo.getInstance().getAppName() } returns appName
        every { AppInfo.getInstance().getVersionName() } returns versionName
        every { AppInfo.getInstance().getApplicationHashKey() } returns applicationHashKey
        every { AppInfo.getInstance().getPackageName() } returns packageName
        every { AppInfo.getInstance().getReferrer() } returns referrer

        every { AppInfo.getInstance().extracted } returns true
//        AppInfoClone.getInstance().extracted = true
//        AppInfoClone.getInstance().appId = appId
//        AppInfoClone.getInstance().appName = appName
//        AppInfoClone.getInstance().versionName = versionName
//        AppInfoClone.getInstance().applicationHashKey = applicationHashKey

    }
}