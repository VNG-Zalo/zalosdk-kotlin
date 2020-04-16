package com.zing.zalo.zalosdk.oauth;


import com.zing.zalo.zalosdk.java.LoginChannel;

public class OauthResponse {
    private int errorCode;
    private String errorMessage;
    private long uId;
    private String oauthCode;
    private LoginChannel channel;
    private String facebookAccessToken;
    private long facebookExpireTime;
    private String socialId;
    private boolean isRegister;

    public OauthResponse() {
        uId = 0;
        errorCode = 0;
        channel = LoginChannel.ZALO;
    }

    public OauthResponse(long uId, String oauthCode, LoginChannel channel) {
        this.uId = uId;
        this.oauthCode = oauthCode;
        this.channel = channel;
    }

    public OauthResponse(long uId, String oauthCode, LoginChannel channel, String facebookAccessToken, long facebookExpireTime) {
        this.uId = uId;
        this.oauthCode = oauthCode;
        this.channel = channel;
        this.facebookAccessToken = facebookAccessToken;
        this.facebookExpireTime = facebookExpireTime;
    }

    public boolean isRegister() {
        return isRegister;
    }

    public void setRegister(boolean register) {
        isRegister = register;
    }

    public String getSocialId() {
        return socialId;
    }

    public void setSocialId(String socialId) {
        this.socialId = socialId;
    }

    public int getErrorCode() {
        return errorCode;
    }

    public void setErrorCode(int errorCode) {
        this.errorCode = errorCode;
    }

    public String getErrorMessage() {
        return errorMessage;
    }

    public void setErrorMessage(String errorMessage) {
        this.errorMessage = errorMessage;
    }

    public long getuId() {
        return uId;
    }

    public void setuId(long uId) {
        this.uId = uId;
    }

    public String getOauthCode() {
        return oauthCode;
    }

    public void setOauthCode(String oauthCode) {
        this.oauthCode = oauthCode;
    }

    public LoginChannel getChannel() {
        return channel;
    }

    public void setChannel(LoginChannel channel) {
        this.channel = channel;
    }

    public String getFacebookAccessToken() {
        return facebookAccessToken;
    }

    public void setFacebookAccessToken(String facebookAccessToken) {
        this.facebookAccessToken = facebookAccessToken;
    }

    public long getFacebookExpireTime() {
        return facebookExpireTime;
    }

    public void setFacebookExpireTime(long facebookExpireTime) {
        this.facebookExpireTime = facebookExpireTime;
    }
}
