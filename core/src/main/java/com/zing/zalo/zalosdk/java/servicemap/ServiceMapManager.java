package com.zing.zalo.zalosdk.java.servicemap;

public class ServiceMapManager {
    public static String KEY_URL_OAUTH = com.zing.zalo.zalosdk.kotlin.core.servicemap.ServiceMapManager.KEY_URL_OAUTH;
    public static String KEY_URL_OPENAPI = com.zing.zalo.zalosdk.kotlin.core.servicemap.ServiceMapManager.KEY_URL_OPENAPI;
    public static String KEY_URL_GRAPH = com.zing.zalo.zalosdk.kotlin.core.servicemap.ServiceMapManager.KEY_URL_GRAPH;
    public static String KEY_URL_CENTRALIZED = com.zing.zalo.zalosdk.kotlin.core.servicemap.ServiceMapManager.KEY_URL_CENTRALIZED;
    private static ServiceMapManager instance = new ServiceMapManager();
    private com.zing.zalo.zalosdk.kotlin.core.servicemap.ServiceMapManager kotlinServiceMap = com.zing.zalo.zalosdk.kotlin.core.servicemap.ServiceMapManager.getInstance();
    private ServiceMapManager() {
    }

    public static ServiceMapManager getInstance() {
        return instance;
    }

    public String urlFor(String key, String path) {
        return kotlinServiceMap.urlFor(key, path);
    }

    public String urlFor(String key) {
        return kotlinServiceMap.urlFor(key);
    }
}
