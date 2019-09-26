package com.zing.zalo.zalosdk.core.log

import android.content.Context
import java.util.*


object Log {
    private const val LOG_TAG = "ZDK"

    private const val VERBOSE = android.util.Log.VERBOSE
    private const val DEBUG = android.util.Log.DEBUG
    private const val INFO = android.util.Log.INFO
    private const val WARN = android.util.Log.WARN
    private const val ERROR = android.util.Log.ERROR

    private var logLevel = android.util.Log.VERBOSE

    fun setLogLevel(context: Context) {
        logLevel = android.util.Log.VERBOSE
    }

    fun d(msg: String) {
        d(LOG_TAG, msg)
    }

    fun d(format: String, vararg args: Any) {
        d(LOG_TAG, format, *args)
    }

    fun d(tag: String, msg: String) {
        log(DEBUG, tag, msg)
    }

    fun d(tag: Class<*>, msg: String) {
        log(DEBUG, tag.simpleName, msg)
    }

    fun d(tag: String, format: String, vararg args: Any) {
        log(DEBUG, tag, format, *args)
    }

    fun d(tag: Class<*>, format: String, vararg args: Any) {
        log(DEBUG, tag.simpleName, format, *args)
    }

    fun e(msg: String) {
        e(LOG_TAG, msg)
    }

    fun e(format: String, vararg args: Any) {
        e(LOG_TAG, format, *args)
    }

    fun e(tag: String, msg: String) {
        log(ERROR, tag, msg)
    }

    fun e(tag: Class<*>, msg: String) {
        log(ERROR, tag.simpleName, msg)
    }

    fun e(tag: String, format: String, vararg args: Any) {
        log(ERROR, tag, format, *args)
    }

    fun e(tag: Class<*>, format: String, vararg args: Any) {
        log(ERROR, tag.simpleName, format, *args)
    }

    fun e(ex: Exception) {
        e(LOG_TAG, ex)
    }

    fun e(tag: String, ex: Exception) {
        val msg = android.util.Log.getStackTraceString(ex)
        log(ERROR, tag, msg)
    }

    fun e(tag: Class<*>, ex: Exception) {
        val msg = android.util.Log.getStackTraceString(ex)
        log(ERROR, tag.simpleName, msg)
    }

    fun i(msg: String) {
        i(LOG_TAG, msg)
    }

    fun i(format: String, vararg args: Any) {
        i(LOG_TAG, format, *args)
    }

    fun i(tag: String, msg: String) {
        log(INFO, tag, msg)
    }

    fun i(tag: Class<*>, msg: String) {
        log(INFO, tag.simpleName, msg)
    }

    fun i(tag: String, format: String, vararg args: Any) {
        log(INFO, tag, format, *args)
    }

    fun i(tag: Class<*>, format: String, vararg args: Any) {
        log(INFO, tag.simpleName, format, *args)
    }

    fun v(msg: String) {
        v(LOG_TAG, msg)
    }

    fun v(format: String, vararg args: Any) {
        v(LOG_TAG, format, *args)
    }

    fun v(tag: String, msg: String) {
        log(VERBOSE, tag, msg)
    }

    fun v(tag: Class<*>, msg: String) {
        log(VERBOSE, tag.simpleName, msg)
    }

    fun v(tag: String, format: String, vararg args: Any) {
        log(VERBOSE, tag, format, *args)
    }

    fun v(tag: Class<*>, format: String, vararg args: Any) {
        log(VERBOSE, tag.simpleName, format, *args)
    }

    fun w(msg: String) {
        w(LOG_TAG, msg)
    }

    fun w(format: String, vararg args: Any) {
        w(LOG_TAG, format, *args)
    }

    fun w(tag: String, ex: Exception) {
        log(WARN, tag, ex.message.toString())
    }

    fun w(tag: String, msg: String) {
        log(WARN, tag, msg)
    }

    fun w(tag: Class<*>, msg: String) {
        log(WARN, tag.simpleName, msg)
    }

    fun w(tag: String, format: String, vararg args: Any) {
        log(WARN, tag, format, *args)
    }

    fun w(tag: Class<*>, format: String, vararg args: Any) {
        log(WARN, tag.simpleName, format, *args)
    }

    fun w(e: Exception?) {
        if (e != null) {
            w(e.toString())
        }
    }

    private fun log(priority: Int, tag: String, format: String?, vararg args: Any) {
        if (format == null) return

        val msg = String.format(Locale.getDefault(), format, *args)
        log(priority, tag, msg)
    }

    private fun log(priority: Int, tag: String, msg: String) {
        if (priority < logLevel) return

        val newTag = if (tag != "ZDK") "ZDK - $tag" else tag

        android.util.Log.println(priority, newTag, msg)
    }
}
