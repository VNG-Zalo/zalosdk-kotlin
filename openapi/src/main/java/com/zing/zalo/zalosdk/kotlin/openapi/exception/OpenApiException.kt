package com.zing.zalo.zalosdk.kotlin.openapi.exception



class OpenApiException (var error_code: Int, message: String):Exception(message) {
}