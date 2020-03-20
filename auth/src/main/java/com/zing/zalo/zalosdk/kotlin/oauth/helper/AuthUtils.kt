package com.zing.zalo.zalosdk.kotlin.oauth.helper

import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.text.TextUtils
import com.zing.zalo.zalosdk.kotlin.core.helper.AppInfo
import com.zing.zalo.zalosdk.kotlin.core.log.Log
import com.zing.zalo.zalosdk.kotlin.core.settings.SettingsManager
import com.zing.zalo.zalosdk.kotlin.oauth.BrowserLoginActivity

object AuthUtils {
    @SuppressLint("StaticFieldLeak")
    lateinit var settingsManager: SettingsManager

    internal fun canUseBrowserLogin(context: Context): Boolean {

        if (!AuthUtils::settingsManager.isInitialized)
            settingsManager = SettingsManager.getInstance()

        if (!settingsManager.isLoginViaBrowser()) return false

        val appId = AppInfo.getInstance().getAppId()
        val pkgName = context.packageName
        if (TextUtils.isEmpty(appId)) return false

        val pkgMgr = context.packageManager
        val intent = Intent()
        intent.setPackage(pkgName)
        intent.data = Uri.parse("zalo-$appId://")
        val componentName = intent.resolveActivity(pkgMgr)
        if (componentName != null &&
            BrowserLoginActivity::class.java.name.equals(componentName.className, ignoreCase = true)
        ) {
            return true
        }

        return false
    }
}