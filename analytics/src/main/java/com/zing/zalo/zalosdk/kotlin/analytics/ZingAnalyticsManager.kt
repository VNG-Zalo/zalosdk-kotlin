package com.zing.zalo.zalosdk.kotlin.analytics

import android.content.Context
import com.zing.zalo.zalosdk.kotlin.core.helper.AppInfo
import com.zing.zalo.zalosdk.kotlin.core.helper.DeviceInfo
import com.zing.zalo.zalosdk.kotlin.core.helper.Utils
import com.zing.zalo.zalosdk.kotlin.core.module.BaseModule
import com.zing.zalo.zalosdk.kotlin.core.module.ModuleManager
import java.util.*

class ZingAnalyticsManager private constructor() : BaseModule() {


    companion object {
        private val instance = ZingAnalyticsManager()

        @JvmStatic
        fun getInstance(): ZingAnalyticsManager {
            return instance
        }

        init {
            ModuleManager.addModule(instance)
        }
    }

    private var isInitialized = false

    private val eventStorage: EventStorage? = null
    private lateinit var ctx: Context
    private val zaAutoActivityTracking = true
    private var pendingEvents: MutableList<PendingEvent> = mutableListOf()
    private var pendingDispatch = false

    override fun onStart(context: Context) {
        super.onStart(context)
        ctx = context
        isInitialized = true
    }

    fun addEvent(
        action: String?,
        params: MutableMap<String, String>?
    ) {
        synchronized(this) {

            if (!isInitialized) {
                pendingEvents.add(PendingEvent(action!!, params))
            } else {
                var _params: MutableMap<String, String> = HashMap()
                if (params == null) {
                    _params = HashMap()
                }
                fillPreloadParams(_params)
                EventTracker.getInstance()
                    .addEvent(action ?: "", _params, System.currentTimeMillis())
            }
        }
    }

    fun addEvent(
        action: String?,
        category: String,
        label: String,
        value: Long
    ) {
        val params: MutableMap<String, String> =
            HashMap()
        params["category"] = category
        params["label"] = label
        params["value"] = "" + value
        addEvent(action, params)
    }


    fun setMaxEventsStored(num: Int) {
        if (isInitialized)
            EventTracker.getInstance().setMaxEventsStored(num)
        else
            EventTracker.tempMaxEventStored = num
    }

    fun dispatchEvents() {
        if (isInitialized) {
            EventTracker.getInstance().dispatchEvent()
        } else {
            pendingDispatch = true
        }
    }

    //#region private supportive method
    private fun fillPreloadParams(params: MutableMap<String, String>?) {
        try {
            val _params: MutableMap<String, String> = HashMap()
            val preloadInfo = DeviceInfo.getPreloadInfo(ctx)

            _params["preloadDefault"] = AppInfo.getInstance().getPreloadChannel()
            _params["preload"] = preloadInfo.preload
            if (!preloadInfo.isPreloaded()) {
                _params["preloadFailed"] = preloadInfo.error
            }

            val listDeviceId =
                Utils.loadListDeviceIDWakeUp(ctx) ?: ""
            _params["wakeupInfo"] = listDeviceId
            _params.putAll(params as Map<String, String>)
        } catch (ex: Exception) {
            ex.printStackTrace()
        }
    }
    //#endregion

}

private class PendingEvent internal constructor(
    var action: String,
    var params: MutableMap<String, String>?
)