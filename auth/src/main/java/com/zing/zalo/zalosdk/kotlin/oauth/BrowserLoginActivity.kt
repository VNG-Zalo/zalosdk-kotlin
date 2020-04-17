package com.zing.zalo.zalosdk.kotlin.oauth

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import com.zing.zalo.zalosdk.kotlin.core.helper.AppInfo
import com.zing.zalo.zalosdk.kotlin.core.log.Log
import org.json.JSONException
import org.json.JSONObject

class BrowserLoginActivity : Activity() {

    private lateinit var zaloSDK: ZaloSDK
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        zaloSDK = ZaloSDK(this)
        if (handleBrowserCallback()) {
            finish()
        }
    }

    private fun handleBrowserCallback(): Boolean {
        val data = intent.data
        if (data == null || data.query == null) return false

        val scheme = data.scheme
        if (scheme == null || !scheme.startsWith("zalo-" + AppInfo.getInstance().getAppId())) {
            return false
        }

        val intent = Intent()
        val extra = JSONObject()
        val extraData = JSONObject()
        val sError = data.getQueryParameter("error")

        if (sError != null && Integer.parseInt(sError) != 0) {

            try {
                intent.putExtra("error", Integer.parseInt(data.getQueryParameter("error")!!))

                val errorMsg = data.getQueryParameter("errorMsg")
                val errorDescription = data.getQueryParameter("error_description")
                val errorReason = data.getQueryParameter("error_reason")

                extraData.put("errorMsg", errorMsg ?: "")
                extraData.put("error_description", errorDescription ?: "")
                extraData.put("error_reason", errorReason ?: "")
                extraData.put("from_source", "browser")
                extra.put("data", extraData)
            } catch (ignored: JSONException) {
                Log.e("handleBrowserCallback", ignored)
            }

            intent.putExtra("data", extra.toString())

        } else if (data.getQueryParameter("code") != null) {
            val uid = data.getQueryParameter("uid") ?: ""
            val code = data.getQueryParameter("code") ?: ""

            intent.putExtra("uid", uid.toLong())
            intent.putExtra("code", code)

            try {
                extraData.put("display_name", data.getQueryParameter("display_name"))
                extraData.put("scope", data.getQueryParameter("scope"))
                extraData.put("socialId", data.getQueryParameter("socialId"))
                extraData.put("dob", data.getQueryParameter("dob"))
                extraData.put("gender", data.getQueryParameter("gender"))
                extraData.put("from_source", "browser")
                extra.put("data", extraData)
            } catch (ignored: JSONException) {
                Log.e("handleBrowserCallback", ignored)
            }

            intent.putExtra("data", extra.toString())
        }

        zaloSDK.onActivityResult(
            this,
            Constant.ZALO_AUTHENTICATE_REQUEST_CODE,
            0,
            intent
        )
        return true
    }
}