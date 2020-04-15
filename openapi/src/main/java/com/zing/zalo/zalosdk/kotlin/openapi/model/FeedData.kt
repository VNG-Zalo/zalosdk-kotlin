package com.zing.zalo.zalosdk.kotlin.openapi.model

data class FeedData(
    var msg: String,
    var link: String
) {
    constructor() : this("", "")
}