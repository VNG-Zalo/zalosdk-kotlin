package com.zing.zalo.zalosdk.demo

import android.os.Bundle
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.zing.zalo.zalosdk.kotlin.core.settings.SettingsManager
import java.lang.String
import java.text.SimpleDateFormat
import java.util.*

class SettingsActivity : AppCompatActivity() {
    private lateinit var settingTextView: TextView
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_settings)

        settingTextView = findViewById(R.id.setting_text_view)
        val setting = getData()

        var str= ""
        for (i in setting!!) {
            str+=  "${i.key} - ${i.value} \n\n"
        }
        settingTextView.text = str
    }


    private fun getData(): List<KeyValue>? {
        val mgr: SettingsManager = SettingsManager.getInstance()
        val list: MutableList<KeyValue> =
            ArrayList<KeyValue>()
        list.add(
            KeyValue(
                "Wakeup interval",
                String.valueOf(mgr.getWakeUpInterval())
            )
        )
        list.add(
            KeyValue(
                "Is out app Login",
                String.valueOf(mgr.isLoginViaBrowser())
            )
        )
        list.add(
            KeyValue(
                "Use web view if zalo app not login",
                String.valueOf(mgr.isUseWebViewLoginZalo())
            )
        )
        list.add(
            KeyValue(
                "Expire date",
                formatTime(mgr.getExpiredTime()).toString()
            )
        )
        return list
    }
    class KeyValue(var key: kotlin.String, var value: kotlin.String)

    fun formatTime(timestamp: Long): kotlin.String? {
        try {
            val calendar = Calendar.getInstance()
            val tz = TimeZone.getDefault()
            calendar.timeInMillis = timestamp
            calendar.add(Calendar.MILLISECOND, tz.getOffset(calendar.timeInMillis))
            val sdf =
                SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault())
            val currenTimeZone = calendar.time
            return sdf.format(currenTimeZone)
        } catch (e: Exception) {
        }
        return ""
    }
}
