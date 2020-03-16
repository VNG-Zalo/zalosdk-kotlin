package com.zing.zalo.zalosdk.java;

public enum LoginChannel {
    GUEST("GUEST"),
    ZALO("ZALO"),
    FACEBOOK("FACEBOOK"),
    GOOGLE("GOOGLE"),
    ZINGME("ZINGME");

    private final String name;

    private LoginChannel(String s) {
        name = s;
    }

    public boolean equalsName(String otherName) {
        return (otherName == null) ? false : name.equals(otherName);
    }

    public String toString() {
        return name;
    }

}