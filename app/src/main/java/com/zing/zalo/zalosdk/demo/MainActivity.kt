package com.zing.zalo.zalosdk.demo

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.Button
import androidx.appcompat.app.AppCompatActivity
import com.zing.zalo.zalosdk.kotlin.analytics.EventStorage
import com.zing.zalo.zalosdk.kotlin.analytics.EventTracker
import com.zing.zalo.zalosdk.kotlin.analytics.EventTrackerListener
import com.zing.zalo.zalosdk.kotlin.analytics.model.Event
import com.zing.zalo.zalosdk.kotlin.core.log.Log


class MainActivity : AppCompatActivity() {

    private val eventTracker = EventTracker.getInstance()

    private val eventTrackerListener = object : EventTrackerListener {
        override fun dispatchComplete() {
            super.dispatchComplete()
            Log.d("got Main Activity ")
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        Log.setLogLevel()

        eventTracker.setListener(eventTrackerListener)
    }

    fun onClickSettingActivityButton(view: View) {
        val intent = Intent(this, SettingsActivity::class.java)
        startActivity(intent)
    }
    fun onClickEventTrackingButton(view: View) {
        eventTracker.addEvent(mockEvent())

        val eventStorage = EventStorage(this)
        val event = eventStorage.loadEventsFromDevice()
        Log.d("event size", event.size.toString())

    }
    fun onClickOpenApiActivityButton(view: View) {
        val intent = Intent(this, OpenApiActivity::class.java)
        startActivity(intent)
    }
    fun onClickWakeUpActivityButton(view: View) {
        val intent = Intent(this, WakeUpActivity::class.java)
        startActivity(intent)
    }
    fun onClickZpTrackingActivityButton(view: View) {
        val intent = Intent(this, ZPTrackingActivity::class.java)
        startActivity(intent)
    }
    fun onClickAuthextActivityButton(view: View) {
        val intent = Intent(this, AuthextActivity::class.java)
        startActivity(intent)
    }


    fun onClickAuthActivityButton(view: View) {
        val intent = Intent(this, AuthDemoActivity::class.java)
        startActivity(intent)
    }

    private fun mockEvent(): Event {
        val timeStamp = System.currentTimeMillis()
        val action = "action-$timeStamp"
        val params = mutableMapOf<String, String>()

        params["name"] = "datahelper-$timeStamp"
        params["age"] = timeStamp.toString()
        return Event(action, params, timeStamp)
    }
}
