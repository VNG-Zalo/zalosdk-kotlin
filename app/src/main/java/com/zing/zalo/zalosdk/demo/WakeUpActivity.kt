package com.zing.zalo.zalosdk.demo

import android.os.Bundle
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import me.zalo.startuphelper.StartupHelperUtil
import java.util.*

class WakeUpActivity : AppCompatActivity() {

    private lateinit var openZaloButton: Button
    private lateinit var zingMp3Button: Button
    private lateinit var baoMoiButton: Button
    private lateinit var labanButton: Button

    private lateinit var resultTextView: TextView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_wake_up)

        bindUI()
        bindViewsListener()
    }


    private fun bindUI() {
        openZaloButton = findViewById(R.id.btn_open_zalo)
        zingMp3Button = findViewById(R.id.btn_open_zingmp3)
        baoMoiButton = findViewById(R.id.btn_open_baomoi)
        labanButton = findViewById(R.id.btn_open_laban)

        resultTextView = findViewById(R.id.result_text_view)
    }

    private fun configureUI() {

    }

    private fun configureLogic() {
    }

    private fun bindViewsListener() {
        openZaloButton.setOnClickListener {
            val params: MutableMap<String, String> =
                HashMap()
            params["wk_notif"] = "1"
            params["wk_pgk_name"] = "com.zing.zalo"
            params["wk_open_ps"] = "com.zing.zalo"
            params["wk_source"] = "com.vng.zing.zdice"
            startUpHelperUtilFunction(params)

        }

        baoMoiButton.setOnClickListener {
            val params: MutableMap<String, String> =
                HashMap()
            params["wk_notif"] = "2"
            params["wk_url"] = "baomoi://"
            params["wk_open_ps"] = "com.epi"
            params["wk_source"] = "com.vng.zing.zdice"

            startUpHelperUtilFunction(params)
        }

        zingMp3Button.setOnClickListener {
            val params: MutableMap<String, String> =
                HashMap()
            params["wk_notif"] = "2"
            params["wk_url"] = "zingmp3://home"
            params["wk_open_ps"] = "com.zing.mp3"
            params["wk_source"] = "com.vng.zing.zdice"
            startUpHelperUtilFunction(params)

        }

        labanButton.setOnClickListener {
            val params: MutableMap<String, String> =
                HashMap()
            params["wk_notif"] = "2"
            params["wk_url"] = "labanapp://com.vng.inputmethod.labankey/themesettingsactivity"
            params["wk_open_ps"] = "com.vng.inputmethod.labankey"
            params["wk_source"] = "com.vng.zing.zdice"

            startUpHelperUtilFunction(params)
        }

    }

    private fun startUpHelperUtilFunction(params: MutableMap<String, String>) {
        if (StartupHelperUtil.isOpenAppNotificationData(this, params)) {
            val intent =
                StartupHelperUtil.createOpenAppNotificationDataIntent(this, params)
            startActivity(intent)
        } else {
            showToast("Create intent failed!")
        }
    }

    private fun showToast(msg: String?) {
        Toast.makeText(this, msg, Toast.LENGTH_LONG).show()
    }
}
