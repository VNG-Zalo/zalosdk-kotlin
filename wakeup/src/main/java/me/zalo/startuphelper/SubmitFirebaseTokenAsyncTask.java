package me.zalo.startuphelper;

import android.os.AsyncTask;

import com.zing.zalo.zalosdk.java.servicemap.ServiceMapManager;
import com.zing.zalo.zalosdk.kotlin.core.log.Log;

import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;

import javax.net.ssl.HttpsURLConnection;

public class SubmitFirebaseTokenAsyncTask extends AsyncTask<Void, Void, Boolean> {
    private static final String CENTRALIZED_FIREBASE_SUBMIT_NOTIFICATION_PATH = "/firebase/submit/notification";
    private static final String CENTRALIZED_FIREBASE_SUBMIT_WAKEUP_PATH = "/firebase/submit/wakeup";
    private static final String CENTRALIZED_FIREBASE_SUBMIT_OPENAPP_PATH = "/firebase/submit/openapp";
    private final Type type;
    private final String params;
    private final String sourceFrom;
    private final String firebaseToken;
    private final Callback callback;

    SubmitFirebaseTokenAsyncTask(Type type, String params, String firebaseToken, String _sourceFrom, Callback callback) {
        this.type = type;
        this.params = params;
        this.firebaseToken = firebaseToken;
        this.sourceFrom = _sourceFrom;
        this.callback = callback;
    }

    @Override
    protected Boolean doInBackground(Void... voids) {
        try {
            final String requestURL;
            if (type == Type.NOTIF) {
//                requestURL = "https://centralized.zaloapp.com/firebase/submit/notification";
                requestURL = ServiceMapManager.getInstance().urlFor(ServiceMapManager.KEY_URL_CENTRALIZED, CENTRALIZED_FIREBASE_SUBMIT_NOTIFICATION_PATH);
            } else if (type == Type.WAKE_UP) {
//                requestURL = "https://centralized.zaloapp.com/firebase/submit/wakeup";
                requestURL = ServiceMapManager.getInstance().urlFor(ServiceMapManager.KEY_URL_CENTRALIZED, CENTRALIZED_FIREBASE_SUBMIT_WAKEUP_PATH);
            } else {
//                requestURL = "https://centralized.zaloapp.com/firebase/submit/openapp";
                requestURL = ServiceMapManager.getInstance().urlFor(ServiceMapManager.KEY_URL_CENTRALIZED, CENTRALIZED_FIREBASE_SUBMIT_OPENAPP_PATH);
            }


            URL url;
            String response = "";

            url = new URL(requestURL);
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setReadTimeout(2 * 60000);
            conn.setConnectTimeout(2 * 60000);
            conn.setRequestMethod("POST");
            conn.setDoInput(true);
            conn.setDoOutput(true);

            OutputStream os = conn.getOutputStream();
            BufferedWriter writer = new BufferedWriter(
                    new OutputStreamWriter(os, "UTF-8"));
            String postDataParams = "firebaseToken=" + URLEncoder.encode(firebaseToken, "UTF-8") + "&" +
                    "deviceData=" + URLEncoder.encode(params.toString(), "UTF-8") + "&";//
//                    "sourceFrom=" + URLEncoder.encode(sourceFrom, "UTF-8") + "&";
            if (sourceFrom != null) {
                postDataParams += "sourceFrom=" + URLEncoder.encode(sourceFrom, "UTF-8") + "&";
            }

            writer.write(postDataParams);
            writer.flush();
            writer.close();
            os.close();
            int responseCode = conn.getResponseCode();
            if (responseCode == HttpsURLConnection.HTTP_OK) {
                String line;
                BufferedReader br = new BufferedReader(new InputStreamReader(conn.getInputStream()));
                while ((line = br.readLine()) != null) {
                    response += line;
                }

                try {
                    JSONObject jsonObject = new JSONObject(response);
                    if (jsonObject.has("error")) {
                        int errorCode = jsonObject.getInt("error");
                        if (errorCode == 1) {//SUCC
                            return true;
                        }
                    }
                } catch (Exception ex) {
                    ex.printStackTrace();
                }
            }
            return false;
        } catch (Exception ex) {
            Log.e(ex);
        }
        return false;
    }

    @Override
    protected void onPostExecute(Boolean success) {
        super.onPostExecute(success);
        if (callback != null) {
            callback.onCompleted(success);
        }
    }

    public enum Type {
        OPEN_APP,
        NOTIF,
        WAKE_UP
    }

    interface Callback {
        void onCompleted(boolean success);
    }
}
