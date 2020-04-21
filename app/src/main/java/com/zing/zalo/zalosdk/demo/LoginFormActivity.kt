package com.zing.zalo.zalosdk.demo

import android.content.Intent
import android.os.Bundle
import android.widget.TextView
import com.zing.zalo.zalosdk.kotlin.oauth.model.ErrorResponse
import com.zing.zalo.zalosdk.oauth.LoginForm
import com.zing.zalo.zalosdk.oauth.OAuthCompleteListener
import com.zing.zalo.zalosdk.oauth.OauthResponse

class LoginFormActivity :ZBaseActivity(){

    private lateinit var callbackTextView: TextView
    private lateinit var loginForm: LoginForm
    private var listener = object : OAuthCompleteListener() {
        override fun onGetOAuthComplete(response: OauthResponse?) {
            super.onGetOAuthComplete(response)
            callbackTextView.text = response?.oauthCode
        }

        override fun onAuthenError(errorCode: Int, message: String?) {
            super.onAuthenError(errorCode, message)
            callbackTextView.text = message
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


    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        loginForm.onActivityResult(this, requestCode, resultCode, data)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContentView(R.layout.activity_login_form)
        loginForm = findViewById(R.id.LoginForm)
        callbackTextView = findViewById(R.id.callback_text_view)
        loginForm.setOAuthCompleteListener(listener)
    }
}
