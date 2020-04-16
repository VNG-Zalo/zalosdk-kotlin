package com.zing.zalo.zalosdk.kotlin.core.helper

import android.annotation.SuppressLint
import android.content.ActivityNotFoundException
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.content.pm.Signature
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.Environment
import android.text.TextUtils
import android.util.Base64
import androidx.annotation.Keep
import com.zing.zalo.zalosdk.kotlin.core.Constant
import com.zing.zalo.zalosdk.kotlin.core.log.Log
import com.zing.zalo.zalosdk.kotlin.core.module.BaseModule
import java.io.File
import java.net.URLEncoder
import java.security.MessageDigest

class AppInfo : BaseModule() {

    @Keep
    companion object {
        private val instance = AppInfo()
        @JvmStatic
        fun getInstance(): AppInfo {
            return instance
        }
    }

    private val lock = Any()
    var extracted: Boolean = false
    private var appId: String = ""
    private var applicationHashKey: String = ""
    private var packageName: String = ""
    private var versionName: String = ""
    internal var versionCode: Long = 0
    internal var appName: String = ""
    internal var firstInstallDate: String = ""
    internal var installDate: String = ""
    internal var lastUpdateDate: String = ""
    internal var installerPackageName: String = ""
    internal var preloadChannel: String = ""

    private var isAutoTrackingOpenApp: Boolean = false

    private lateinit var ctx: Context

    override fun onStart(context: Context) {
        super.onStart(context)
        ctx = context
        extractBasicAppInfo(context)
    }

    fun isPackageExists(context: Context, targetPackage: String): Boolean {
        val pm: PackageManager = context.packageManager
        try {
            pm.getPackageInfo(targetPackage, PackageManager.GET_META_DATA)
        } catch (ex: Exception) {
            return false
        }
        return true
    }


    @SuppressLint("PackageManagerGetSignatures")
    @Suppress("DEPRECATION")
    fun getApplicationHashKey(): String? {
        if (!TextUtils.isEmpty(applicationHashKey)) return applicationHashKey

        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
                val signInfo =
                    ctx.packageManager.getPackageInfo(
                        ctx.packageName,
                        PackageManager.GET_SIGNING_CERTIFICATES
                    )
                        .signingInfo

                if (signInfo.hasMultipleSigners()) {
                    signInfo.apkContentsSigners.map { signature ->
                        encodeSignature(signature)
                    }
                } else {
                    signInfo.signingCertificateHistory.map { signature ->
                        encodeSignature(signature)
                    }
                }
            } else {
                val info = ctx.packageManager.getPackageInfo(
                    ctx.packageName,
                    PackageManager.GET_SIGNATURES
                )
                info.signatures.map { signature ->
                    encodeSignature(signature)
                }
            }

        } catch (e: Exception) {
            Log.e("AppInfo: getApplicationHashKey()", e)
        }

        return applicationHashKey
    }

    fun isPackageExists(targetPackage: String): Boolean {
        val pm: PackageManager = ctx.packageManager
        try {
            pm.getPackageInfo(targetPackage, PackageManager.GET_META_DATA)
        } catch (ex: Exception) {
            return false
        }
        return true
    }

    fun getAppIdLong(): Long {
        val str = getAppId()
        return try {
            java.lang.Long.parseLong(str)
        } catch (ex: Exception) {
            Log.w("getAppIdLong", ex)
            return 0L
        }

    }

    fun getReferrer(): String {
        return try {
            ctx.getSharedPreferences("zacCookie", 0).getString("referrer", "") ?: ""
        } catch (ex: Exception) {
            ""
        }
    }

    fun getPackageName(): String {
        return getPropertyAsT(ctx, "packageName") ?: ""
    }

    fun getAppId(): String {
        return getPropertyAsT(ctx, "appId") ?: ""
    }

    fun getAppName(): String {
        return getPropertyAsT(ctx, "appName") ?: ""
    }

    fun getSDKVersion(): String {
        return Constant.VERSION
    }

    fun getVersionName(): String {
        return getPropertyAsT(ctx, "versionName") ?: ""
    }

    fun getVersionCode(): Long {
        return getPropertyAsT(ctx, "versionCode") ?: 0L
    }

    fun getInstallerPackageName(): String {
        return getPropertyAsT(ctx, "installerPackageName") ?: ""
    }

    fun getInstallDate(): String {
        return getPropertyAsT(ctx, "installDate") ?: ""
    }

    fun getFirstInstallDate(): String {

        return getPropertyAsT(ctx, "firstInstallDate") ?: ""
    }

    fun getLastUpdate(): String {

        return getPropertyAsT(ctx, "installDate") ?: ""
    }

    fun getFirstRunDate(): String {
        return getPropertyAsT(ctx, "firstInstallDate") ?: ""
    }

    fun getPreloadChannel(): String {
        return getPropertyAsT(ctx, "preloadChannel") ?: ""
    }

    fun isPreInstalled(): Boolean {
        try {
            if (Utils.isExternalStorageReadable(ctx)) {
                val file = prepareFileInExternalStore(ctx.packageName, false)
                return if (file.exists()) {
                    true
                } else {
                    file.createNewFile()
                    false
                }
            }
        } catch (e: Exception) {
            return false
        }

        return false
    }

    fun launchMarketApp(
        context: Context,
        targetPackage: String
    ) {
        try {
            val intent =
                Intent(Intent.ACTION_VIEW)
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            intent.data = Uri.parse("market://details?id=$targetPackage")
            context.startActivity(intent)
        } catch (e: ActivityNotFoundException) {
            val intent = Intent(
                Intent.ACTION_VIEW,
                Uri.parse("https://play.google.com/store/apps/details?id=$targetPackage")
            )
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            context.startActivity(intent)
        }
    }


    //#region private supportive method
    private fun prepareFileInExternalStore(fileName: String, clearIfExists: Boolean): File {
        val path =
            Environment.getExternalStorageDirectory().absolutePath + "/Android/data/com.google.android.zdt.data/" + fileName
        val f = File(path)
        f.parentFile.mkdirs()

        if (clearIfExists && f.exists()) {
            f.delete()
        }

        return f
    }

    @Suppress("UNCHECKED_CAST")
    private fun <T> getPropertyAsT(context: Context, key: String): T? {
        synchronized(lock) {
            if (!extracted)
                extractBasicAppInfo(context)
        }
        return findPropertyValue(key) as T?
    }


    private fun findPropertyValue(key: String): Any? {
        return when (key) {
            "appId" -> appId
            "versionName" -> versionName
            "versionCode" -> versionCode
            "appName" -> appName
            "applicationHashKey" -> applicationHashKey
            "firstInstallDate" -> firstInstallDate
            "installDate" -> installDate
            "lastUpdateDate" -> lastUpdateDate
            "installerPackageName" -> installerPackageName
            "preloadChannel" -> preloadChannel
            "isAutoTrackingOpenApp" -> isAutoTrackingOpenApp
            "packageName" -> packageName
            else -> null
        }
    }

    private fun encodeSignature(signature: Signature) {
        val md = MessageDigest.getInstance("SHA")
        md.update(signature.toByteArray())
        applicationHashKey = Base64.encodeToString(md.digest(), Base64.DEFAULT).trim { it <= ' ' }
    }


    private fun getBoolean(bundle: Bundle, key: String, def: Boolean): Boolean {
        return if (bundle.containsKey(key)) {
            bundle.getBoolean(key)
        } else {
            return def
        }
    }


    private fun getString(bundle: Bundle, key: String, def: String?): String? {
        return if (bundle.containsKey(key)) {
            bundle.getString(key)
        } else {
            return def
        }
    }

    private fun extractBasicAppInfo(ctx: Context) {
        synchronized(lock) {
            if (extracted) return

            try {
                val pm = ctx.packageManager
                packageName = ctx.packageName

                val pInfo = pm.getPackageInfo(packageName, PackageManager.GET_PERMISSIONS)
                val appInfo = pm.getApplicationInfo(packageName, PackageManager.GET_META_DATA)
                versionName = pInfo.versionName

                versionCode = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
                    pInfo.longVersionCode
                } else {
                    pInfo.versionCode.toLong()
                }

                appName = URLEncoder.encode(pInfo.applicationInfo.loadLabel(pm).toString(), "UTF-8")
                installerPackageName = pm.getInstallerPackageName(packageName) ?: ""


                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.ICE_CREAM_SANDWICH) {
                    installDate = pInfo.firstInstallTime.toString()
                    firstInstallDate = pInfo.firstInstallTime.toString()
                    lastUpdateDate = pInfo.lastUpdateTime.toString()
                } else {
                    installDate = ""
                    firstInstallDate = ""
                    lastUpdateDate = ""
                }


                val bundle = appInfo.metaData

                appId = getString(bundle, "com.zing.zalo.zalosdk.appID", null) ?: ""

                if (TextUtils.isEmpty(appId)) {
                    appId = getString(bundle, "appID", "") ?: ""
                }

                isAutoTrackingOpenApp =
                    getBoolean(bundle, "com.zing.zalosdk.configAutoTrackingActivity", false)

                preloadChannel = getString(bundle, "com.zing.zalo.sdk.preloadChannel", "") ?: ""

            } catch (ex: Exception) {
                Log.e("extractBasicAppInfo", ex)
            }

            extracted = true
        }
    }
    //#endregion
}