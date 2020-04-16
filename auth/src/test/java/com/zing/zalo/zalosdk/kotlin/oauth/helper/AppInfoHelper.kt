package com.zing.zalo.zalosdk.kotlin.oauth.helper

import com.zing.zalo.zalosdk.kotlin.core.Constant
import com.zing.zalo.zalosdk.kotlin.core.helper.AppInfo
import io.mockk.every
import io.mockk.mockkObject

object AppInfoHelper {
    const val appId = "12345"
    const val appIdLong = 12345L
    const val scanId = "scanId_3"
    const val appName = "appName"
    const val versionName = "2"
    const val applicationHashKey = "applicationHashKey"
    const val advertiserId = "advertiserId"
    const val packageName = "package_name"
    const val referrer = "referrer"
    const val sdkVersion = Constant.VERSION

    fun setup() {
        mockkObject(AppInfo)
        every { AppInfo.getInstance().getAppId() } returns appId
        every { AppInfo.getInstance().getAppName() } returns appName
        every { AppInfo.getInstance().getVersionName() } returns versionName
        every { AppInfo.getInstance().getApplicationHashKey() } returns applicationHashKey
        every { AppInfo.getInstance().getPackageName() } returns packageName
        every { AppInfo.getInstance().getReferrer() } returns referrer

        every { AppInfo.getInstance().extracted } returns true
        every {
            AppInfo.getInstance()
                .isPackageExists(com.zing.zalo.zalosdk.kotlin.oauth.Constant.core.ZALO_PACKAGE_NAME)
        } returns true
        every { AppInfo.getInstance().getAppIdLong() } returns appIdLong
        every { AppInfo.getInstance().getSDKVersion() } returns sdkVersion
//        AppInfoClone.getInstance().extracted = true
//        AppInfoClone.getInstance().appId = appId
//        AppInfoClone.getInstance().appName = appName
//        AppInfoClone.getInstance().versionName = versionName
//        AppInfoClone.getInstance().applicationHashKey = applicationHashKey

    }

}