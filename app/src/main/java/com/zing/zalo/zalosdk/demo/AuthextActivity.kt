package com.zing.zalo.zalosdk.demo

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.zing.zalo.zalosdk.java.settings.SettingsManager
import com.zing.zalo.zalosdk.kotlin.analytics.EventStorage
import com.zing.zalo.zalosdk.oauth.LoginForm
import com.zing.zalo.zalosdk.oauth.OAuthCompleteListener
import com.zing.zalo.zalosdk.oauth.OauthResponse
import com.zing.zalo.zalosdk.oauth.ZingMeLoginView

class AuthextActivity : AppCompatActivity() {


    private lateinit var callbackTextView: TextView
    private lateinit var loginForm: LoginForm
    private lateinit var zingMeLoginView: ZingMeLoginView
    private var authCompleteListener = object : OAuthCompleteListener() {
        override fun onGetOAuthComplete(response: OauthResponse?) {
            super.onGetOAuthComplete(response)
            callbackTextView.text = response?.oauthCode
        }

        override fun onAuthenError(errorCode: Int, message: String?) {
            super.onAuthenError(errorCode, message)
            callbackTextView.text = message
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        loginForm.onActivityResult(this, requestCode, resultCode, data)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContentView(R.layout.activity_authext)
        loginForm = findViewById(R.id.LoginForm)
        zingMeLoginView = findViewById(R.id.ZingMeLoginView)
        callbackTextView = findViewById(R.id.callback_text_view)

        loginForm.setOAuthCompleteListener(authCompleteListener)
        zingMeLoginView.setOAuthCompleteListener(authCompleteListener)

        bindViewListener(this)

    }

    private fun bindViewListener(context: Context) {

    }

}
