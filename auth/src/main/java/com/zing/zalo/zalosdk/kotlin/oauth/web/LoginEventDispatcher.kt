package com.zing.zalo.zalosdk.kotlin.oauth.web

import android.content.Context
import android.graphics.Bitmap
import android.net.Uri
import android.view.View
import android.webkit.WebResourceError
import android.webkit.WebResourceRequest
import android.webkit.WebView
import android.webkit.WebViewClient
import com.zing.zalo.zalosdk.kotlin.core.log.Log
import com.zing.zalo.zalosdk.kotlin.oauth.Constant
import com.zing.zalo.zalosdk.kotlin.oauth.ZaloOAuthResultCode
import java.lang.ref.WeakReference

class LoginEventDispatcher(
    var context: Context?,
    var that: WeakReference<ZaloWebLoginBaseFragment>,
    var callbackUrl: String
) : WebViewClient() {

    override fun onPageFinished(view: WebView?, url: String?) {
        super.onPageFinished(view, url)
        if (that.get() != null) that.get()!!.progressBar.visibility = View.GONE
    }

    override fun onPageStarted(view: WebView?, url: String?, favicon: Bitmap?) {
        super.onPageStarted(view, url, favicon)
        if (that.get() != null) that.get()!!.progressBar.visibility = View.VISIBLE
    }

    override fun onReceivedError(
        view: WebView?,
        request: WebResourceRequest?,
        error: WebResourceError?
    ) {
        super.onReceivedError(view, request, error)

        if (that.get() != null) {
            that.get()!!.progressBar.visibility = View.GONE
            that.get()!!.onLoginCompleted(-1, 0, "", 0, "", false)
        }

    }

    @Suppress("DEPRECATION")
    override fun shouldOverrideUrlLoading(view: WebView?, url: String?): Boolean {
        if (processCallbackUrl(url!!)) return true
        return super.shouldOverrideUrlLoading(view, url)

    }

    private fun processCallbackUrl(url: String): Boolean {
        if (url.indexOf(callbackUrl) != 0) return false

        val uri = Uri.parse(url)
        var error = Constant.RESULT_CODE_SUCCESSFUL
        var uid: Long = 0
        var code: String? = ""
        var name: String? = ""
        var zProtect = 0
        var errorFlag = false

        //response data for error
        var errorReason = ""
        var errorDescription = ""
        var errorMsg = ""
        var fromSource = ""
        try {
            if (uri.getQueryParameter("error") != null) {
                error = Integer.parseInt(uri.getQueryParameter("error").toString())
                errorReason = uri.getQueryParameter("error_reason")
                errorDescription = uri.getQueryParameter("error_description")
                if ( context != null) {
                    errorMsg = ZaloOAuthResultCode.findErrorMessageByID(context!!, error)
                }
                errorFlag = true
            } else {
                uid = (uri.getQueryParameter("uid").toString()).toLong()
                code = uri.getQueryParameter("code")
                name = uri.getQueryParameter("display_name")
                val tmp = uri.getQueryParameter("zprotect")
                if (tmp != null) zProtect = Integer.parseInt(tmp)
            }
        } catch (e: Exception) {
            Log.e("processCallbackUrl", e)
            error = ZaloOAuthResultCode.RESULTCODE_UNEXPECTED_ERROR
            errorMsg = e.toString()
            errorFlag = true
        }


        if (that.get() != null) {
            if (errorFlag) {
                fromSource = "web_login"
                that.get()!!.onLoginFailed(error, errorMsg, errorReason, errorDescription, fromSource)
            } else {
                that.get()!!.onLoginCompleted(error, uid, code!!, zProtect, name!!, false)
            }
        }
        return true
    }
}