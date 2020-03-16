package com.zing.zalo.zalosdk.kotlin.oauth.helper

import android.content.Context
import androidx.annotation.Keep
import com.zing.zalo.zalosdk.kotlin.core.SharedPreferenceConstant.PREF_ACCESS_TOKEN_NEW_API
import com.zing.zalo.zalosdk.kotlin.core.SharedPreferenceConstant.PREF_ACESS_TOKEN
import com.zing.zalo.zalosdk.kotlin.core.SharedPreferenceConstant.PREF_GUEST_DEVICE_ID
import com.zing.zalo.zalosdk.kotlin.core.SharedPreferenceConstant.PREF_GUEST_IS_CERT
import com.zing.zalo.zalosdk.kotlin.core.SharedPreferenceConstant.PREF_IS_PROTECTED
import com.zing.zalo.zalosdk.kotlin.core.SharedPreferenceConstant.PREF_OAUTH_CODE_CHANNEL
import com.zing.zalo.zalosdk.kotlin.core.SharedPreferenceConstant.PREF_SOCIAL_ID
import com.zing.zalo.zalosdk.kotlin.core.SharedPreferenceConstant.PREF_ZALO_DISPLAY_NAME
import com.zing.zalo.zalosdk.kotlin.core.SharedPreferenceConstant.PREF_ZALO_ID
import com.zing.zalo.zalosdk.kotlin.core.helper.Storage

@Keep
class AuthStorage(ctx: Context) : Storage(ctx) {


    fun getZaloId(): Long? {
        return getLong(PREF_ZALO_ID)
    }

    fun setZaloId(id: Long) {
        setLong(PREF_ZALO_ID, id)
    }

    fun getZaloDisplayName(): String? {
        return getString(PREF_ZALO_DISPLAY_NAME)
    }

    fun setZaloDisplayName(displayname: String) {
        setString(PREF_ZALO_DISPLAY_NAME, displayname)
    }

    fun setAccessTokenNewAPI(token: String) {
        setString(PREF_ACCESS_TOKEN_NEW_API, token)
    }

    fun setLastLoginChannel(channel: String) {
        setString(PREF_OAUTH_CODE_CHANNEL, channel)
    }

    fun getLastLoginChannel(): String? {
        return getString(PREF_OAUTH_CODE_CHANNEL)
    }

    fun getGuestDeviceId(): String? {
        return getString(PREF_GUEST_DEVICE_ID)
    }

    fun setGuestDeviceId(id: String?) {
        setString(PREF_GUEST_DEVICE_ID, id!!)
    }

    fun getIsGuestCertificated(): Int {
        return getInt(PREF_GUEST_IS_CERT)
    }

    fun setIsGuestCertificated(isCert: Int) {
        setInt(PREF_GUEST_IS_CERT, isCert)
    }

    fun getAccessToken(): String? {
        return getString(PREF_ACESS_TOKEN)
    }

    fun setAccessToken(token: String?) {
        setString(PREF_ACESS_TOKEN, token!!)
    }

    fun getIsProtected(): Int {
        return getInt(PREF_IS_PROTECTED)
    }

    fun setIsProtected(isProtected: Int) {
        setInt(PREF_IS_PROTECTED, isProtected)
    }

    fun setSocialId(socialId: String?) {
        setString(PREF_SOCIAL_ID, socialId!!)
    }

    fun getSocialId(): String? {
        return getString(PREF_SOCIAL_ID)
    }
}