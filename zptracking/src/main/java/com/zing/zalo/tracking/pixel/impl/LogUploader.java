package com.zing.zalo.tracking.pixel.impl;

import android.os.AsyncTask;
import android.os.Bundle;

import com.zing.zalo.tracking.pixel.BuildConfig;
import com.zing.zalo.tracking.pixel.Utils;
import com.zing.zalo.tracking.pixel.abstracts.ILogUploader;
import com.zing.zalo.tracking.pixel.abstracts.ILogUploaderCallback;
import com.zing.zalo.tracking.pixel.model.Event;
import com.zing.zalo.zalosdk.kotlin.core.helper.DeviceInfo;
import com.zing.zalo.zalosdk.kotlin.core.http.HttpClient;
import com.zing.zalo.zalosdk.kotlin.core.http.HttpMultipartRequest;
import com.zing.zalo.zalosdk.kotlin.core.log.Log;

import org.json.JSONObject;

import java.util.List;

import static com.zing.zalo.tracking.pixel.ZPConstants.LOG_TAG;
import static com.zing.zalo.zalosdk.kotlin.core.helper.Utils.gzipCompress;

public class LogUploader implements ILogUploader {
    public static final String API_URL = "https://px.za.zalo.me/m/tr";
    private HttpClient mHttpClient;
    private String mMobileNetworkCode;

    public LogUploader(HttpClient httpClientFactory) {
        mHttpClient = httpClientFactory;
    }

    @Override
    public void upload(List<Event> events, String appId, long pixelId, String globalId, String adsId,
                       Bundle userInfo, String packageName, String connectionType, String location, ILogUploaderCallback callback) {
        if (events.size() == 0) {
            Log.v(LOG_TAG, "No events to submit");
            return;
        }

        Task task = new Task(mHttpClient, events, appId, pixelId, globalId, adsId, userInfo,
                packageName, connectionType, mMobileNetworkCode, location, callback);
        task.execute();

    }

    public void setMobileNetworkCode(String mobileNetworkCode) {
        mMobileNetworkCode = mobileNetworkCode;
    }

    private static class Task extends AsyncTask<Void, Void, Boolean> {
        String location;
        List<Event> events;
        String appId;
        long pixelId;
        String globalId;
        String adsId;
        Bundle userInfo;
        String packageName;
        String connectionType;
        ILogUploaderCallback callback;
        HttpClient mHttpClient;
        String mobileNetworkCode;

        Task(HttpClient httpClientFactory, List<Event> events, String appId, long pixelId,
             String globalId, String adsId, Bundle userInfo, String packageName, String connectionType, String mobileNetworkCode, String location, ILogUploaderCallback callback) {
            this.mHttpClient = httpClientFactory;
            this.events = events;
            this.appId = appId;
            this.pixelId = pixelId;
            this.globalId = globalId;
            this.adsId = adsId;
            this.userInfo = userInfo;
            this.callback = callback;
            this.packageName = packageName;
            this.connectionType = connectionType;
            this.location = location;
            this.mobileNetworkCode = mobileNetworkCode;
        }

        @Override
        protected Boolean doInBackground(Void... voids) {
            try {
                JSONObject json = new JSONObject();
                json.put("pkg", packageName);
                json.put("app_id", appId);
                json.put("vid", globalId);
                json.put("ads_id", adsId);
                json.put("model", DeviceInfo.getModel());
                json.put("brd", DeviceInfo.getBrand());
                json.put("pl", "2002");
                json.put("net", connectionType);
                json.put("osv", DeviceInfo.getOSVersion());
                json.put("sdkv", BuildConfig.VERSION_NAME);
                json.put("loc", location);
                json.put("mnc", mobileNetworkCode);
                json.put("events", Utils.eventsToJSON(events));
                json.put("user_info", Utils.eventsToJSON(userInfo));

                String src = json.toString();
                byte[] body = gzipCompress(src);

                String url = API_URL + "?id=" + pixelId;
//                IHttpRequest req = mHttpClient.newRequest(HttpClientRequest.Type.POST, url);
                HttpMultipartRequest req = new HttpMultipartRequest(url);
                req.addHeader("Content-Encoding", "gzip");
                req.addHeader("Content-Type", "application/json");
                req.addHeader("Content-Length", String.valueOf(body.length));
                req.setBody(body);

                return true;
            } catch (Exception e) {
                Log.w(LOG_TAG, "Upload err: %s", e);
                return false;
            }
        }

        @Override
        protected void onPostExecute(Boolean result) {
            super.onPostExecute(result);
            if (callback != null) {
                callback.onCompleted(events, result);
            }
        }
    }

}
