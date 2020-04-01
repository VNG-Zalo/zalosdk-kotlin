package com.zing.zalo.zalosdk.demo

import android.app.AlertDialog
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.zing.zalo.zalosdk.kotlin.oauth.model.ErrorResponse
import com.zing.zalo.zalosdk.oauth.AuthenticateExtention
import com.zing.zalo.zalosdk.oauth.LoginForm
import com.zing.zalo.zalosdk.oauth.OAuthCompleteListener
import com.zing.zalo.zalosdk.oauth.OauthResponse

class AuthextActivity : AppCompatActivity() {

private lateinit var authenticateExtension : AuthenticateExtention
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

    private fun showToast(msg:String) {
        Toast.makeText(this,msg,Toast.LENGTH_SHORT).show()
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        loginForm.onActivityResult(this, requestCode, resultCode, data)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContentView(R.layout.activity_authext)


        bindUI()
        configureLogic()
    }

    private fun bindUI() {
        loginForm = findViewById(R.id.LoginForm)
        callbackTextView = findViewById(R.id.callback_text_view)


        loginForm.setOAuthCompleteListener(listener)
    }

    private fun configureLogic() {
        authenticateExtension = AuthenticateExtention(this)
    }

    fun loginFacebookButton(view: View) {
        authenticateExtension.authenticateWithFacebook(this,listener)
    }
    fun loginGoogleButton(view: View) {
        authenticateExtension.authenticateWithGooglePlus(this,listener)
    }

    fun loginZingMeButton(view: View) {
        showDialogLoginZingMe()
    }
    fun loginGuestButton(view: View) {
        authenticateExtension.authenticateWithGuest(this,listener)
    }

    private fun showDialogLoginZingMe() { //
        val dialogBuilder = AlertDialog.Builder(this)
        val inflater = this.layoutInflater
        val dialogView: View =
            inflater.inflate(R.layout.layout_login_zingme_dialog,null)
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

}
