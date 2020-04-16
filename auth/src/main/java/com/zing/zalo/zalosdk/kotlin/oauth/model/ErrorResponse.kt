package com.zing.zalo.zalosdk.kotlin.oauth.model


data class ErrorResponse(
    val errorCode: Int,
    val errorMsg: String,
    val errorReason: String,
    val errorDescription: String,
    val fromSource: String
)
