package com.zing.zalo.zalosdk.java;

/**
 * Store all messages used in the SDK. You can subclass this to customize the messages.
 */
public class LocalizedString {
    protected String loadingMessage;
    protected String noNetworkMessage;
    protected String zaloOauthOfDateMessage;
    protected String zaloNotInstalledMessage;
    protected String updateMessage;
    protected String installMessage;
    protected String cancelMessage;
    protected String errorWebViewMessage = "";


    public LocalizedString() {
        setDefault();
        customizeString();
    }

    /**
     * Default text is "Đang tải..."
     *
     * @return
     */
    public String getLoadingMessage() {
        return loadingMessage;
    }

    /**
     * Default text is "Mạng không ổn định, vui lòng thử lại sau"
     *
     * @return
     */
    public String getNoNetworkMessage() {
        return noNetworkMessage;
    }

    /**
     * Default text is "Bản Zalo hiện tại không tương thích!"
     *
     * @return
     */
    public String getZaloOauthOfDateMessage() {
        return zaloOauthOfDateMessage;
    }

    /**
     * Default text is "Bạn chưa cài Zalo!"
     *
     * @return
     */
    public String getZaloNotInstalledMessage() {
        return zaloNotInstalledMessage;
    }

    /**
     * Default text is "Cập nhật"
     *
     * @return
     */
    public String getUpdateMessage() {
        return updateMessage;
    }

    /**
     * Default text is "Cài đặt"
     *
     * @return
     */
    public String getInstallMessage() {
        return installMessage;
    }

    /**
     * Default text is "Bỏ qua"
     *
     * @return
     */
    public String getCancelMessage() {
        return cancelMessage;
    }

    public String getErrorWebViewMessage() {
        return errorWebViewMessage;
    }

    /**
     * Override this methd if you want to customize the message
     */
    protected void customizeString() {
    }

    private void setDefault() {
        loadingMessage = "Đang tải...";
        noNetworkMessage = "Mạng không ổn định, vui lòng thử lại sau";
        zaloOauthOfDateMessage = "Bản Zalo hiện tại không tương thích!";
        zaloNotInstalledMessage = "Bạn chưa cài Zalo!";
        updateMessage = "Cập nhật";
        cancelMessage = "Bỏ qua";
        installMessage = "Cài đặt";
        errorWebViewMessage = "Lỗi WebView. Vui lòng cài đặt Zalo để đăng nhập.";
    }
}
