package com.zing.zalo.zalosdk.exception;


@SuppressWarnings("serial")
public class InitializedException extends RuntimeException {
    public InitializedException() {
        super("Missing call ZingAnalyticsManager.getInstance().init(application, appID) in Application ");
    }

    public InitializedException(String message) {
        super(message);
    }
}
