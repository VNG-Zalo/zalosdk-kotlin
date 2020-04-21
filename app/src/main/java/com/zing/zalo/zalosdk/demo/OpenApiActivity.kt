package com.zing.zalo.zalosdk.demo

import android.annotation.SuppressLint
import android.os.Bundle
import android.text.TextUtils
import android.view.View
import android.widget.Button
import android.widget.LinearLayout
import android.widget.TextView
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import com.zing.zalo.zalosdk.demo.OpenApiAction.*
import com.zing.zalo.zalosdk.kotlin.oauth.ZaloSDK
import com.zing.zalo.zalosdk.kotlin.openapi.ZaloOpenApi
import com.zing.zalo.zalosdk.kotlin.openapi.ZaloOpenApiCallback
import com.zing.zalo.zalosdk.kotlin.openapi.ZaloPluginCallback
import org.json.JSONObject

class OpenApiActivity : AppCompatActivity(), ZaloOpenApiCallback, ZaloPluginCallback {


    private lateinit var getProfileButton: Button
    private lateinit var getFriendListUsedAppButton: Button
    private lateinit var getFriendListInvitableButton: Button
    private lateinit var inviteFriendUseAppButton: Button
    private lateinit var postToWallButton: Button
    private lateinit var sendMsgToFriendButton: Button
    private lateinit var sendMessageViaApp: Button
    private lateinit var sharePostViaApp: Button

    private lateinit var callBackTextView: TextView

    private lateinit var zaloSDK: ZaloSDK
    private lateinit var zaloOpenApi: ZaloOpenApi

    private val friendID = arrayOf("1491696566623706686")

    @SuppressLint("SetTextI18n")
    override fun onResult(
        isSuccess: Boolean,
        error_code: Int,
        message: String?,
        jsonData: String?
    ) {
        callBackTextView.text = "${message.toString()}\n\n${jsonData.toString()}"
    }

    override fun onResult(data: JSONObject?) {
        callBackTextView.text = data.toString()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_open_api)
        bindUI()
        configureLogic()
        configureUI()
        bindViewsListener()


    }

    //#region private supportive method
    private fun bindUI() {
        getProfileButton = findViewById(R.id.get_profile_button)
        getFriendListUsedAppButton = findViewById(R.id.get_friend_list_used_app_button)
        getFriendListInvitableButton = findViewById(R.id.get_friend_list_invitable_button)
        inviteFriendUseAppButton = findViewById(R.id.invite_friend_use_app_button)
        postToWallButton = findViewById(R.id.post_to_wall_button)
        sendMsgToFriendButton = findViewById(R.id.send_message_to_friend_button)
        sendMessageViaApp = findViewById(R.id.send_message_via_app_button)
        sharePostViaApp = findViewById(R.id.share_post_via_app)

        callBackTextView = findViewById(R.id.callback_text_view)
    }

    private fun configureUI() {

    }

    private fun configureLogic() {
        zaloSDK = ZaloSDK(this)
        zaloOpenApi = ZaloOpenApi(
            this,
            zaloSDK.getOauthCode()
        )
    }

    private fun bindViewsListener() {
        getProfileButton.setOnClickListener {
            val fields = arrayOf("id", "birthday", "gender", "picture", "name")
            zaloOpenApi.getProfile(fields, this)
        }
        getFriendListUsedAppButton.setOnClickListener {
            val fields = arrayOf("id", "name", "gender", "picture")
            zaloOpenApi.getFriendListUsedApp(fields, 0, 999, this)
        }
        getFriendListInvitableButton.setOnClickListener {
            val fields = arrayOf("id", "name", "gender", "picture")
            zaloOpenApi.getFriendListInvitable(fields, 0, 999, this)
        }

        inviteFriendUseAppButton.setOnClickListener {
            zaloOpenApi.inviteFriendUseApp(friendID, "Hello!", this)
        }

        postToWallButton.setOnClickListener {
            showDialogInputMessage(PostToWall)
        }

        sendMsgToFriendButton.setOnClickListener {
            showDialogInputMessage(SendMessageToFriend)
        }

        sendMessageViaApp.setOnClickListener {
            showDialogInputMessage(ShareMessageViaApp)
        }

        sharePostViaApp.setOnClickListener {
            showDialogInputMessage(ShareFeedViaApp)
        }
    }

    private fun showDialogInputMessage(case: OpenApiAction) { //

        val dialogBuilder = AlertDialog.Builder(this)
        val inflater = this.layoutInflater
        val dialogView: View = inflater.inflate(R.layout.layout_input_url, null)

        dialogBuilder.setView(dialogView)
        dialogBuilder.setMessage("Share content via Zalo App")
            .setCancelable(true)
            .setPositiveButton("OK") { dialog, id ->
                val url =
                    dialogView.findViewById<TextView>(R.id.input_url_text_view).text.toString()
                val msg =
                    dialogView.findViewById<TextView>(R.id.input_message_text_view).text.toString()
                handleClickButtonApi(case, msg, url)
            }
        dialogBuilder.create().show()

        setOnClickListenerButton(dialogView)
        configDialogUILogic(case, dialogView)

    }

    private fun handleClickButtonApi(case: OpenApiAction, msg: String, url: String) {

//        val msg = if (TextUtils.isEmpty(msg)) "" else msg
        when (case) {
            ShareMessageViaApp -> {
                zaloOpenApi.shareMessage(msg, this)
            }
            ShareFeedViaApp -> {
                zaloOpenApi.shareFeed(url, this)
            }
            SendMessageToFriend -> {
                zaloOpenApi.sendMsgToFriend(friendID[0], msg, url, this)
            }
            PostToWall -> {
                zaloOpenApi.postToWall(url,msg, this)
            }
        }
    }

    private fun configDialogUILogic(case: OpenApiAction, view: View) {
        val inputMessageTextView = view.findViewById<TextView>(R.id.input_message_text_view)
        val inputUrlTextView = view.findViewById<TextView>(R.id.input_url_text_view)
        val buttonLayout = view.findViewById<LinearLayout>(R.id.set_default_link_button_linear_layout)
        val defaultLinkTextView = view.findViewById<TextView>(R.id.default_link_text_view)

        when (case) {
            ShareMessageViaApp -> {
                inputUrlTextView.visibility = View.GONE
                buttonLayout.visibility = View.GONE
                defaultLinkTextView.visibility = View.GONE
            }
            ShareFeedViaApp -> {
                inputMessageTextView.visibility = View.GONE
            }
            SendMessageToFriend,PostToWall -> return
        }
    }

    private fun setOnClickListenerButton(view: View) {
        val inputUrlTextView = view.findViewById<TextView>(R.id.input_url_text_view)
        view.findViewById<Button>(R.id.linkButton).setOnClickListener {
            inputUrlTextView.text = "https://zingnews.vn/"
        }

        view.findViewById<Button>(R.id.imageUrlButton).setOnClickListener {
            inputUrlTextView.text = "https://homepages.cae.wisc.edu/~ece533/images/boat.png"
        }
        view.findViewById<Button>(R.id.videoUrlButton).setOnClickListener {
            inputUrlTextView.text = "https://www.radiantmediaplayer.com/media/bbb-360p.mp4"
        }

    }

    //#endregion
}

enum class OpenApiAction {
    ShareMessageViaApp,
    SendMessageToFriend,
    ShareFeedViaApp,
    PostToWall
}