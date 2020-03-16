@file:JvmName("Constants")

package com.zing.zalo.zalosdk.kotlin.core

import androidx.annotation.Keep


@Keep
object Constant {
    const val VERSION = BuildConfig.VERSION_NAME
    //    const val DEV_MODE = true
    var DEV_MODE = false
    const val ZALO_PACKAGE_NAME = "com.zing.zalo"

    val sharedPreference = SharedPreferenceConstant
    val api = Api
    val key = SecretKey
}

@Keep
object SharedPreferenceConstant {

    //Prefs Auth
    const val PREF_OAUTH_CODE = "PREFERECE_ZALO_SDK_OAUTH_CODE"
    const val PREF_ZALO_ID = "PREFERECE_ZALO_SDK_ZALO_ID"
    const val PREF_ZALO_DISPLAY_NAME = "PREFERECE_ZALO_SDK_ZALO_DISPLAY_NAME"
    const val PREF_ACCESS_TOKEN_NEW_API = "PREF_NEW_API_ACCESSTOKEN"
    const val PREF_OAUTH_CODE_CHANNEL = "PREFERECE_ZALO_SDK_OAUTH_CODE_CHANNEL"
    const val PREF_GUEST_DEVICE_ID = "PREF_GUEST_DEVICE_ID"
    const val PREF_GUEST_IS_CERT = "PREF_GUEST_IS_CERTIFICATE"
    const val PREF_ACESS_TOKEN = "PREF_ACESS_TOKEN"
    const val PREF_IS_PROTECTED = "PREF_IS_PROTECTED"
    const val PREF_SOCIAL_ID = "PREFERECE_SOCIAL_ID"

    //Prefs ServiceMapManager
    const val PREF_KEY_URL_AUTH = "PREFERECE_KEY_URL_OAUTH"
    const val PREF_KEY_URL_CENTRALIZED = "PREFERECE_KEY_URL_CENTRALIZED"
    const val PREF_KEY_URL_GRAPH = "PREFERECE_KEY_URL_GRAPH"
    const val PREF_EXPIRE_TIME = "PREFERCE_EXPIRE_TIME"

    //Prefs SettingsManager
    const val PREF_NAME_WAKEUP = "com.zing.zalo.sdk.preload.wakeup"
    const val PREF_NAME_PRELOAD = "com.zing.zalo.sdk.preload"
    const val PREF_ADVERTISE_ID = "adsid"

    //Prefs DeviceTrackingSdk
    const val PREF_SDK_ID = "PREFERECE_SDK_ID"
    const val PREF_PRIVATE_KEY = "PREFERECE_PRIVATE_KEY"

    const val PREF_KEY_LIST_DEVICEID_WAKE_UP = "com.zing.zalo.sdk.wakeup.listdeviceid_wk"
}

@Keep
object Api {
    const val API_HARDWARE_ID_URL = "id/mobile/android"
    const val API_SDK_ID = "/sdk/mobile/android"
    const val API_GET_SETTING = "/sdk/mobile/setting"
    const val API_TRACKING_URL = "/apps/mobile/android"
    const val API_APP_TRACKING_ID_URL = "/apps/mobile/explore/android"

    const val AUTH_MOBILE_ACCESS_TOKEN_PATH = "/v3/mobile/access_token"
    const val GRAPH_ME_FRIENDS_PATH = "/v2.0/me/friends"
    const val GRAPH_V2_ME_PATH = "/v2.0/me"
    const val GRAPH_ME_INVITABLE_FRIENDS_PATH = "/v2.0/me/invitable_friends"
    const val GRAPH_ME_FEED_PATH = "/v2.0/me/feed"
    const val GRAPH_ME_MESSAGE_PATH = "/v2.0/me/message"
    const val GRAPH_APP_REQUESTS_PATH = "/v2.0/apprequests"
}

@Keep
object SecretKey {
    const val TRK_SECRET_KEY = "@#centralize#@"
}
