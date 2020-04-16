package com.zing.zalo.tracking.pixel;

public class ZPConstants {
    public static final String LOG_TAG = "ZPT";
    public static final int MAX_EVENT_STORED = 500;
    public static final long EVENT_EXPIRE_TIME = 2 * 24 * 60 * 1000;
    public static final long DISPATCH_INTERVAL = 120 * 1000;
    public static final long STORE_INTERVAL = 60 * 1000;
    public static final String UPLOAD_API = "/pixel/android";
}
