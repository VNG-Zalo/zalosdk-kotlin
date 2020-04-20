package com.zing.zalo.zalosdk.demo

import android.annotation.SuppressLint
import android.app.AlertDialog
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.text.TextUtils
import android.view.View
import android.widget.TextView
import com.zing.zalo.zalosdk.kotlin.analytics.EventTracker
import com.zing.zalo.zalosdk.kotlin.analytics.EventTrackerListener
import com.zing.zalo.zalosdk.kotlin.core.helper.AppInfo
import com.zing.zalo.zalosdk.kotlin.core.log.Log
import com.zing.zalo.zalosdk.kotlin.oauth.Constant
import com.zing.zalo.zalosdk.kotlin.oauth.IAuthenticateCompleteListener
import com.zing.zalo.zalosdk.kotlin.oauth.LoginVia
import com.zing.zalo.zalosdk.kotlin.oauth.ZaloSDK
import com.zing.zalo.zalosdk.kotlin.oauth.callback.GetZaloLoginStatus
import com.zing.zalo.zalosdk.kotlin.oauth.callback.ValidateOAuthCodeCallback
import com.zing.zalo.zalosdk.kotlin.oauth.helper.AuthStorage
import com.zing.zalo.zalosdk.kotlin.oauth.model.ErrorResponse
import com.zing.zalo.zalosdk.oauth.AuthenticateExtention
import com.zing.zalo.zalosdk.oauth.OAuthCompleteListener
import com.zing.zalo.zalosdk.oauth.OauthResponse

@SuppressLint("SetTextI18n")
class AuthDemoActivity : ZBaseActivity(), ValidateOAuthCodeCallback, GetZaloLoginStatus {


    private lateinit var loginStatusTextView: TextView
    private lateinit var resultTextView: TextView

    private lateinit var mStorage: AuthStorage
    private lateinit var authenticateExtension: AuthenticateExtention
    private lateinit var zaloSDK: ZaloSDK

    private val eventTracker = EventTracker.getInstance()

    private val authenticateListener = object : IAuthenticateCompleteListener {
        override fun onAuthenticateSuccess(uid: Long, code: String, data: Map<String, Any>) {
            val displayName = data[Constant.user.DISPLAY_NAME].toString()
            resultTextView.text = "Auth code: $code \n" +
                    "User: $displayName \nUID: $uid"
            showToast("Login Success")
        }

        override fun onAuthenticateError(errorCode: Int, message: String) {
            if (!TextUtils.isEmpty(message)) {
                showAlertDialog(message)
                resultTextView.text = null
            }
        }

        override fun onAuthenticateError(
            errorCode: Int,
            errorMsg: String?,
            errorResponse: ErrorResponse
        ) {
            super.onAuthenticateError(errorCode, errorMsg, errorResponse)
            val msg = "ErrorCode: ${errorResponse.errorCode} \n" +
                    "Error Message: ${errorResponse.errorMsg} \n" +
                    "Error description: ${errorResponse.errorDescription} \n" +
                    "Error reason: ${errorResponse.errorReason} \n" +
                    "Error fromSource: ${errorResponse.fromSource} "
            showToast(msg)
        }

    }


    private val eventTrackerListener = object : EventTrackerListener {
        override fun dispatchComplete() {
            super.dispatchComplete()
            Log.d("got Main Activity ")
        }
    }

    private var listener = object : OAuthCompleteListener() {

        override fun onGetOAuthComplete(response: OauthResponse?) {
            super.onGetOAuthComplete(response)
            resultTextView.text = "Auth code: ${response?.oauthCode}"
        }

        override fun onAuthenError(errorCode: Int, message: String?) {
            super.onAuthenError(errorCode, message)
            resultTextView.text = message
        }

        override fun onAuthenError(
            errorCode: Int,
            errorMsg: String?,
            errorResponse: ErrorResponse
        ) {
            super.onAuthenError(errorCode, errorMsg, errorResponse)

            val msg = "ErrorCode: ${errorResponse.errorCode} \n" +
                    "Error Message: ${errorResponse.errorMsg} \n" +
                    "Error description: ${errorResponse.errorDescription} \n" +
                    "Error reason: ${errorResponse.errorReason} \n" +
                    "Error fromSource: ${errorResponse.fromSource} "
            showToast(msg)
        }

    }


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_auth_demo)

        Log.setLogLevel()
        resultTextView = findViewById(R.id.result_text_view)
        loginStatusTextView = findViewById(R.id.login_status_text_view)
        zaloSDK = ZaloSDK(this)
        mStorage = AuthStorage(this)
        authenticateExtension = AuthenticateExtention(this)
        eventTracker.setListener(eventTrackerListener)


        resultTextView.text = "Auth code: ${zaloSDK.getOauthCode()} \n" +
                "App ID: ${AppInfo.getInstance().getAppId()} \n" +
                "User ID: ${mStorage.getZaloDisplayName()} \n"
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        zaloSDK.onActivityResult(this, requestCode, resultCode, data)
        authenticateExtension.onActivityResult(this,requestCode,resultCode,data)
    }

    override fun onValidateComplete(
        validated: Boolean,
        errorCode: Int,
        userId: Long,
        authCode: String?
    ) {
        showToast("validated: $validated - errorCode: $errorCode")
        resultTextView.text = "Authcode: $authCode \n" +
                "UserId: $userId"
    }

    override fun onGetZaloLoginStatusCompleted(status: Int) {
        runOnUiThread {
            when (status) {
                1 -> {
                    loginStatusTextView.text = "Zalo login: yes"
                    showToast("Zalo login: yes")
                }
                0 -> {
                    loginStatusTextView.text = "Zalo login: no"
                    showToast("Zalo login: no")
                }
                else -> {
                    loginStatusTextView.text = "Error: $status"
                    showToast("Error: $status")
                }
            }

        }
    }

    //#region onClickBindButton
    fun onClickZaloLoginMobileButton(view: View) {
        zaloSDK.unAuthenticate()
        zaloSDK.authenticate(this, LoginVia.APP, authenticateListener)
    }
    fun onClickZaloLoginViaWebAppButton(view: View) {
        zaloSDK.unAuthenticate()
        zaloSDK.authenticate(this, LoginVia.APP_OR_WEB, authenticateListener)
    }
    fun onClickZaloLoginWebButton(view: View) {
        zaloSDK.unAuthenticate()
        zaloSDK.authenticate(this, LoginVia.WEB, authenticateListener)
    }

    fun onClickLoginFacebookButton(view: View) {
        authenticateExtension.authenticateWithFacebook(this, listener)
    }
    fun onClickLoginGoogleButton(view: View) {
        authenticateExtension.authenticateWithGooglePlus(this, listener)
    }

    fun onClickLoginZingmeButton(view: View) {
        showDialogLoginZingMe()
    }
    fun onClickLoginGuestButton(view: View) {
        authenticateExtension.authenticateWithGuest(this, listener)
    }

    fun onClickRegisterButton(view: View) {
        zaloSDK.registerZalo(this, authenticateListener)
    }
    fun onClickValidateOAuthCodeButton(view: View) {
        zaloSDK.isAuthenticate(this)
    }

    fun onClickCheckAppLoginButton(view: View) {
        zaloSDK.getZaloLoginStatus(this)
    }
    //#endregion

    //#region private supportive function
    private fun showAlertDialog(message: String) {
        AlertDialog.Builder(this).setTitle(R.string.app_name).setMessage(message)
            .setPositiveButton(android.R.string.yes, null).show()
    }
    private fun showDialogLoginZingMe() { //
        val dialogBuilder = AlertDialog.Builder(this)
        val inflater = this.layoutInflater
        val dialogView: View =
            inflater.inflate(R.layout.layout_login_zingme_dialog, null)
        val context: Context = this
        dialogBuilder.setView(dialogView)
        dialogBuilder.setMessage("Look at this dialog!")
            .setCancelable(false)
            .setPositiveButton("OK") { dialog, id ->
                val accountTextView =
                    dialogView.findViewById<TextView>(R.id.zingme_account_text_view)
                val passwordTextView =
                    dialogView.findViewById<TextView>(R.id.password_text_view)
                val account = accountTextView.text.toString()
                val password = passwordTextView.text.toString()
                authenticateExtension.authenticateWithZingMe(
                    context,
                    account,
                    password,
                    listener
                )
            }
        val alertDialog = dialogBuilder.create()
        dialogView.findViewById<View>(R.id.zingme_account_text_view)
            .setOnClickListener { alertDialog.dismiss() }
        alertDialog.show()
    }
    //#endregion
}
