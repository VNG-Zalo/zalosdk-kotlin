package com.zing.zalo.zalosdk.kotlin.openapi.helper

import com.zing.zalo.zalosdk.kotlin.core.helper.AppInfo
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
    const val installerPackageName = "installer_packageName"
    const val installedDate = "installed_date"
    const val firstInstalledDate = "installed_date"
    const val lastUpdate = "last_update"
    const val firstRunDate = "first_run_date"


    fun setup() {
        mockkObject(AppInfo)
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


        every { AppInfo.getInstance().extracted } returns true
//        AppInfoClone.getInstance().extracted = true
//        AppInfoClone.getInstance().appId = appId
//        AppInfoClone.getInstance().appName = appName
//        AppInfoClone.getInstance().versionName = versionName
//        AppInfoClone.getInstance().applicationHashKey = applicationHashKey

    }
}