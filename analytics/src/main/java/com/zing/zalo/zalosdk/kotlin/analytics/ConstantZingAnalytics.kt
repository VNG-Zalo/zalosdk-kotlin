package com.zing.zalo.zalosdk.kotlin.analytics

import androidx.annotation.Keep

@Keep
object ConstantZingAnalytics {
    const val DEFAULT_MAX_EVENTS_STORED = 1000;
    const val DEFAULT_DISPATCH_EVENTS_INTERVAL = 120 * 1000.toLong()
    const val MIN_DISPATCH_EVENTS_INTERVAL = 10 * 1000.toLong()
    const val DEFAULT_STORE_EVENTS_INTERVAL = 60 * 1000.toLong()
    const val MIN_STORE_EVENTS_INTERVAL = 10 * 1000.toLong()
    const val DEFAULT_DISPATCH_MAX_COUNT_EVENT = 100L
    const val DEFAULT_VALID_EVENTS = 2 * 24 * 60 * 1000.toLong()

}