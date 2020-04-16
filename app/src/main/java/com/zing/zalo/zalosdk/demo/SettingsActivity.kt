package com.zing.zalo.zalosdk.demo

import android.os.Bundle
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.zing.zalo.zalosdk.kotlin.core.settings.SettingsManager
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
                "Wakeup interval", mgr.getWakeUpInterval().toString()
            )
        )
        list.add(
            KeyValue(
                "Is Login via Browser: ",
                mgr.isLoginViaBrowser().toString()
            )
        )
        list.add(
            KeyValue(
                "Is use web view login: ",
                mgr.isUseWebViewLoginZalo().toString()
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
