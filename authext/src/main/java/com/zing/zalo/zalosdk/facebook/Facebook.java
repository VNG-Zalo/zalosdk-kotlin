/*
 * Copyright 2010 Facebook, Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.zing.zalo.zalosdk.facebook;

import android.Manifest;
import android.app.Activity;
import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.text.TextUtils;
import android.webkit.CookieSyncManager;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.lang.ref.WeakReference;
import java.net.MalformedURLException;
import java.util.Arrays;

/**
 * Main Facebook object for interacting with the Facebook developer API.
 * Provides methods to log in and log out a user, make requests using the REST
 * and Graph APIs, and start user interface interactions with the API (such as
 * pop-ups promoting for credentials, permissions, stream posts, etc.)
 *
 * @author Jim Brusstar (jimbru@facebook.com),
 * Yariv Sadan (yariv@facebook.com),
 * Luke Shepard (lshepard@facebook.com)
 */
@SuppressWarnings("deprecation")
public class Facebook {

    // Strings used in the authorization flow
    public static final String REDIRECT_URI = "fbconnect://success";
    public static final String CANCEL_URI = "fbconnect://cancel";
    public static final String TOKEN = "access_token";
    public static final String EXPIRES = "expires_in";
    public static final String SINGLE_SIGN_ON_DISABLED = "service_disabled";

    public static final Uri ATTRIBUTION_ID_CONTENT_URI =
            Uri.parse("content://com.facebook.katana.provider.AttributionIdProvider");
    public static final String ATTRIBUTION_ID_COLUMN_NAME = "aid";

    public static final int FORCE_DIALOG_AUTH = -1;
    // Used as default activityCode by authorize(). See authorize() below.
    public static final int DEFAULT_AUTH_ACTIVITY_CODE = 32665;
    private static final String LOGIN = "oauth";
    // Facebook server endpoints: may be modified in a subclass for testing
    protected static String DIALOG_BASE_URL =
            "https://m.facebook.com/dialog/";
    protected static String GRAPH_BASE_URL =
            "https://graph.facebook.com/";
    protected static String RESTSERVER_URL =
            "https://api.facebook.com/restserver.php";
    private static Context mApplicationContext;
    // If the last time we extended the access token was more than 24 hours ago
    // we try to refresh the access token again.
    final private long REFRESH_TOKEN_BARRIER = 24L * 60L * 60L * 1000L;
    private String mAccessToken = null;
    private long mLastAccessUpdate = 0;
    private long mAccessExpires = 0;
    private String mAppId;
    private WeakReference<Activity> mAuthActivity;
    private String[] mAuthPermissions;
    private int mAuthActivityCode;
    private DialogListener mAuthDialogListener;
    private GetTokenClient getTokenClient;

    /**
     * Constructor for Facebook object.
     *
     * @param appId Your Facebook application ID. Found at
     *              www.facebook.com/developers/apps.php.
     */
    public Facebook(Context applicationContext, String appId) {
        if (appId == null) {
            throw new IllegalArgumentException(
                    "You must specify your application ID when instantiating " +
                            "a Facebook object. See README for details.");
        }
        mAppId = appId;
        mApplicationContext = applicationContext;
    }

    public static Context getApplicationContext() {
        return mApplicationContext;
    }

    /**
     * Default authorize method. Grants only basic permissions.
     * <p>
     * See authorize() below for @params.
     */
    public void authorize(WeakReference<Activity> activity, final DialogListener listener) {
        authorize(activity, new String[]{}, DEFAULT_AUTH_ACTIVITY_CODE,
                listener);
    }

    /**
     * Authorize method that grants custom permissions.
     * <p>
     * See authorize() below for @params.
     */
    public void authorize(WeakReference<Activity> activity, String[] permissions,
                          final DialogListener listener) {
        authorize(activity, permissions, DEFAULT_AUTH_ACTIVITY_CODE, listener);
    }

    /**
     * Full authorize method.
     * <p>
     * Starts either an Activity or a dialog which prompts the user to log in to
     * Facebook and grant the requested permissions to the given application.
     * <p>
     * This method will, when possible, use Facebook's single sign-on for
     * Android to obtain an access token. This involves proxying a call through
     * the Facebook for Android stand-alone application, which will handle the
     * authentication flow, and return an OAuth access token for making API
     * calls.
     * <p>
     * Because this process will not be available for all users, if single
     * sign-on is not possible, this method will automatically fall back to the
     * OAuth 2.0 User-Agent flow. In this flow, the user credentials are handled
     * by Facebook in an embedded WebView, not by the client application. As
     * such, the dialog makes a network request and renders HTML content rather
     * than a native UI. The access token is retrieved from a redirect to a
     * special URL that the WebView handles.
     * <p>
     * Note that User credentials could be handled natively using the OAuth 2.0
     * Username and Password Flow, but this is not supported by this SDK.
     * <p>
     * See http://developers.facebook.com/docs/authentication/ and
     * http://wiki.oauth.net/OAuth-2 for more details.
     * <p>
     * Note that this method is asynchronous and the callback will be invoked in
     * the original calling thread (not in a background thread).
     * <p>
     * Also note that requests may be made to the API without calling authorize
     * first, in which case only public information is returned.
     * <p>
     * IMPORTANT: Note that single sign-on authentication will not function
     * correctly if you do not include a call to the authorizeCallback() method
     * in your onActivityResult() function! Please see below for more
     * information. single sign-on may be disabled by passing FORCE_DIALOG_AUTH
     * as the activityCode parameter in your call to authorize().
     *
     * @param activity      The Android activity in which we want to display the
     *                      authorization dialog.
     * @param applicationId The Facebook application identifier e.g. "350685531728"
     * @param permissions   A list of permissions required for this application: e.g.
     *                      "read_stream", "publish_stream", "offline_access", etc. see
     *                      http://developers.facebook.com/docs/authentication/permissions
     *                      This parameter should not be null -- if you do not require any
     *                      permissions, then pass in an empty String array.
     * @param activityCode  Single sign-on requires an activity result to be called back
     *                      to the client application -- if you are waiting on other
     *                      activities to return data, pass a custom activity code here to
     *                      avoid collisions. If you would like to force the use of legacy
     *                      dialog-based authorization, pass FORCE_DIALOG_AUTH for this
     *                      parameter. Otherwise just omit this parameter and Facebook
     *                      will use a suitable default. See
     *                      http://developer.android.com/reference/android/
     *                      app/Activity.html for more information.
     * @param listener      Callback interface for notifying the calling application when
     *                      the authentication dialog has completed, failed, or been
     *                      canceled.
     */

    public void authorize(WeakReference<Activity> activity, String[] permissions,
                          int activityCode, final DialogListener listener) {


        mAuthActivity = activity;
        mAuthDialogListener = listener;
        mAuthPermissions = permissions;
        mAuthActivityCode = activityCode;

        if (startGetTokenLoginMethod()) {
            return;
        }

        startLoginWithProxyAuth();

    }

    private void startLoginWithProxyAuth() {
        boolean singleSignOnStarted = false;
        // Prefer single sign-on, where available.
        if (mAuthActivityCode >= 0) {
            singleSignOnStarted = startSingleSignOn(mAuthActivity, mAppId,
                    mAuthPermissions, mAuthActivityCode);
        }
        // Otherwise fall back to traditional dialog.
        if (!singleSignOnStarted) {
            startDialogAuth(mAuthActivity, mAuthPermissions);
        }
    }

    private boolean startGetTokenLoginMethod() {
        getTokenClient = new GetTokenClient(mApplicationContext, mAppId);
        if (!getTokenClient.start()) {
            return false;
        }


        GetTokenClient.CompletedListener callback = new GetTokenClient.CompletedListener() {
            @Override
            public void completed(Bundle result) {
                getTokenCompleted(result);
            }
        };

        getTokenClient.setCompletedListener(callback);
        return true;
    }

    private void getTokenCompleted(Bundle result) {
        if (getTokenClient != null) {
            getTokenClient.setCompletedListener(null);
        }
        getTokenClient = null;

        if (result != null) {

            setAccessToken(result.getString(NativeProtocol.EXTRA_ACCESS_TOKEN));
            setAccessExpires(result.getLong(NativeProtocol.EXTRA_EXPIRES_SECONDS_SINCE_EPOCH));
            if (isSessionValid()) {
                mAuthDialogListener.onComplete(result);
                return;
            }
        }

        startLoginWithProxyAuth();
    }


    /**
     * Internal method to handle single sign-on backend for authorize().
     *
     * @param activity      The Android Activity that will parent the ProxyAuth Activity.
     * @param applicationId The Facebook application identifier.
     * @param permissions   A list of permissions required for this application. If you do
     *                      not require any permissions, pass an empty String array.
     * @param activityCode  Activity code to uniquely identify the result Intent in the
     *                      callback.
     */
    private boolean startSingleSignOn(WeakReference<Activity> activity, String applicationId,
                                      String[] permissions, int activityCode) {
        boolean didSucceed = true;
        String e2e = "{init: " + System.currentTimeMillis() + "}";


        Activity act = activity.get();
        if (act == null) return false;

        Intent intent = NativeProtocol.createProxyAuthIntent(mApplicationContext, mAppId, Arrays.asList(permissions), e2e, false, false, "friends");


        mAuthActivity = activity;
        mAuthPermissions = permissions;
        mAuthActivityCode = activityCode;
        try {
            act.startActivityForResult(intent, activityCode);
        } catch (ActivityNotFoundException e) {
            didSucceed = false;
        } catch (NullPointerException ex) {
            didSucceed = false;
        }

        return didSucceed;
    }


    /**
     * Internal method to handle dialog-based authentication backend for
     * authorize().
     *
     * @param activity      The Android Activity that will parent the auth dialog.
     * @param applicationId The Facebook application identifier.
     * @param permissions   A list of permissions required for this application. If you do
     *                      not require any permissions, pass an empty String array.
     */
    private void startDialogAuth(WeakReference<Activity> activity, String[] permissions) {
        Bundle params = new Bundle();
        if (permissions.length > 0) {
            params.putString("scope", TextUtils.join(",", permissions));
        }


        Activity act = activity.get();
        if (act == null) return;
        Context ctx = act.getWindow().getContext();

        CookieSyncManager.createInstance(ctx);
        dialog(act, LOGIN, params, new DialogListener() {

            public void onComplete(Bundle values) {
                // ensure any cookies set by the dialog are saved
                CookieSyncManager.getInstance().sync();
                setAccessToken(values.getString(TOKEN));
                setAccessExpiresIn(values.getString(EXPIRES));
                if (isSessionValid()) {
                    Util.logd("Facebook-authorize", "Login Success! access_token="
                            + getAccessToken() + " expires="
                            + getAccessExpires());
                    mAuthDialogListener.onComplete(values);
                } else {
                    mAuthDialogListener.onFacebookError(new FacebookError(
                            "Failed to receive access token."));
                }
            }

            public void onError(DialogError error) {
                Util.logd("Facebook-authorize", "Login failed: " + error);
                mAuthDialogListener.onError(error);
            }

            public void onFacebookError(FacebookError error) {
                Util.logd("Facebook-authorize", "Login failed: " + error);
                mAuthDialogListener.onFacebookError(error);
            }

            public void onCancel() {
                Util.logd("Facebook-authorize", "Login canceled");
                mAuthDialogListener.onCancel();
            }
        });
    }

    /**
     * IMPORTANT: This method must be invoked at the top of the calling
     * activity's onActivityResult() function or Facebook authentication will
     * not function properly!
     * <p>
     * If your calling activity does not currently implement onActivityResult(),
     * you must implement it and include a call to this method if you intend to
     * use the authorize() method in this SDK.
     * <p>
     * For more information, see
     * http://developer.android.com/reference/android/app/
     * Activity.html#onActivityResult(int, int, android.content.Intent)
     */
    public void authorizeCallback(int requestCode, int resultCode, Intent data) {
        if (requestCode == mAuthActivityCode) {

            // Successfully redirected.
            if (resultCode == Activity.RESULT_OK) {

                // Check OAuth 2.0/2.10 error code.
                String error = data.getStringExtra("error");
                if (error == null) {
                    error = data.getStringExtra("error_type");
                }

                // A Facebook error occurred.
                if (error != null) {
                    if (error.equals(SINGLE_SIGN_ON_DISABLED)
                            || error.equals("AndroidAuthKillSwitchException")) {
                        Util.logd("debuglog", "Hosted auth currently "
                                + "disabled. Retrying dialog auth...");
                        startDialogAuth(mAuthActivity, mAuthPermissions);
                    } else if (error.equals("access_denied")
                            || error.equals("OAuthAccessDeniedException")) {
                        Util.logd("debuglog", "Login canceled by user.");
                        mAuthDialogListener.onCancel();
                    } else {
                        String description = data.getStringExtra("error_description");
                        if (description != null) {
                            error = error + ":" + description;
                        }
                        Util.logd("debuglog", "Facebook Login failed: " + error);
                        mAuthDialogListener.onFacebookError(
                                new FacebookError(error));
                    }

                    // No errors.
                } else {
                    setAccessToken(data.getStringExtra(TOKEN));
                    setAccessExpiresIn(data.getStringExtra(EXPIRES));
                    if (isSessionValid()) {
                        Util.logd("debuglog",
                                "Login Success! access_token="
                                        + getAccessToken() + " expires="
                                        + getAccessExpires());
                        mAuthDialogListener.onComplete(data.getExtras());
                    } else {
                        mAuthDialogListener.onFacebookError(new FacebookError(
                                "Failed to receive access token."));
                    }
                }

                // An error occurred before we could be redirected.
            } else if (resultCode == Activity.RESULT_CANCELED) {

                // An Android error occured.
                if (data != null) {
                    Util.logd("debuglog",
                            "Login failed AAAA: " + data.getStringExtra("error"));
                    Object aa = data.getExtras().get("error_code");
                    if (aa instanceof String) {
                        Util.logd("debuglog",
                                "Login failed AAAA: CCCC" + aa);
                        mAuthDialogListener.onError(
                                new DialogError(
                                        data.getStringExtra("error"),
                                        data.getIntExtra("error_code", -1),
                                        data.getStringExtra("failing_url")));
                    } else {
                        mAuthDialogListener.onError(
                                new DialogError(
                                        data.getStringExtra("error"),
                                        data.getIntExtra("error_code", -1),
                                        data.getStringExtra("failing_url")));
                    }

                    // User pressed the 'back' button.
                } else {
                    Util.logd("debuglog", "Login canceled by user.");
                    mAuthDialogListener.onCancel();
                }
            }
        }
    }


    /**
     * Check if the access token requires refreshing.
     *
     * @return true if the last time a new token was obtained was over 24 hours ago.
     */
    public boolean shouldExtendAccessToken() {
        return isSessionValid() &&
                (System.currentTimeMillis() - mLastAccessUpdate >= REFRESH_TOKEN_BARRIER);
    }


    /**
     * Invalidate the current user session by removing the access token in
     * memory, clearing the browser cookie, and calling auth.expireSession
     * through the API.
     * <p>
     * Note that this method blocks waiting for a network response, so do not
     * call it in a UI thread.
     *
     * @param context The Android context in which the logout should be called: it
     *                should be the same context in which the login occurred in
     *                order to clear any stored cookies
     * @return JSON string representation of the auth.expireSession response
     * ("true" if successful)
     * @throws IOException
     * @throws MalformedURLException
     */
    public String logout(Context context)
            throws MalformedURLException, IOException {
        Util.clearCookies(context);
        Bundle b = new Bundle();
        b.putString("method", "auth.expireSession");
        String response = request(b);
        setAccessToken(null);
        setAccessExpires(0);
        return response;
    }

    /**
     * Make a request to Facebook's old (pre-graph) API with the given
     * parameters. One of the parameter keys must be "method" and its value
     * should be a valid REST server API method.
     * <p>
     * See http://developers.facebook.com/docs/reference/rest/
     * <p>
     * Note that this method blocks waiting for a network response, so do not
     * call it in a UI thread.
     * <p>
     * Example:
     * <code>
     * Bundle parameters = new Bundle();
     * parameters.putString("method", "auth.expireSession");
     * String response = request(parameters);
     * </code>
     *
     * @param parameters Key-value pairs of parameters to the request. Refer to the
     *                   documentation: one of the parameters must be "method".
     * @return JSON string representation of the response
     * @throws IOException              if a network error occurs
     * @throws MalformedURLException    if accessing an invalid endpoint
     * @throws IllegalArgumentException if one of the parameters is not "method"
     */
    public String request(Bundle parameters)
            throws MalformedURLException, IOException {
        if (!parameters.containsKey("method")) {
            throw new IllegalArgumentException("API method must be specified. "
                    + "(parameters must contain key \"method\" and value). See"
                    + " http://developers.facebook.com/docs/reference/rest/");
        }
        return request(null, parameters, "GET");
    }

    /**
     * Make a request to the Facebook Graph API without any parameters.
     * <p>
     * See http://developers.facebook.com/docs/api
     * <p>
     * Note that this method blocks waiting for a network response, so do not
     * call it in a UI thread.
     *
     * @param graphPath Path to resource in the Facebook graph, e.g., to fetch data
     *                  about the currently logged authenticated user, provide "me",
     *                  which will fetch http://graph.facebook.com/me
     * @return JSON string representation of the response
     * @throws IOException
     * @throws MalformedURLException
     */
    public String request(String graphPath)
            throws MalformedURLException, IOException {
        return request(graphPath, new Bundle(), "GET");
    }

    /**
     * Make a request to the Facebook Graph API with the given string parameters
     * using an HTTP GET (default method).
     * <p>
     * See http://developers.facebook.com/docs/api
     * <p>
     * Note that this method blocks waiting for a network response, so do not
     * call it in a UI thread.
     *
     * @param graphPath  Path to resource in the Facebook graph, e.g., to fetch data
     *                   about the currently logged authenticated user, provide "me",
     *                   which will fetch http://graph.facebook.com/me
     * @param parameters key-value string parameters, e.g. the path "search" with
     *                   parameters "q" : "facebook" would produce a query for the
     *                   following graph resource:
     *                   https://graph.facebook.com/search?q=facebook
     * @return JSON string representation of the response
     * @throws IOException
     * @throws MalformedURLException
     */
    public String request(String graphPath, Bundle parameters)
            throws MalformedURLException, IOException {
        return request(graphPath, parameters, "GET");
    }

    /**
     * Synchronously make a request to the Facebook Graph API with the given
     * HTTP method and string parameters. Note that binary data parameters
     * (e.g. pictures) are not yet supported by this helper function.
     * <p>
     * See http://developers.facebook.com/docs/api
     * <p>
     * Note that this method blocks waiting for a network response, so do not
     * call it in a UI thread.
     *
     * @param graphPath  Path to resource in the Facebook graph, e.g., to fetch data
     *                   about the currently logged authenticated user, provide "me",
     *                   which will fetch http://graph.facebook.com/me
     * @param params     Key-value string parameters, e.g. the path "search" with
     *                   parameters {"q" : "facebook"} would produce a query for the
     *                   following graph resource:
     *                   https://graph.facebook.com/search?q=facebook
     * @param httpMethod http verb, e.g. "GET", "POST", "DELETE"
     * @return JSON string representation of the response
     * @throws IOException
     * @throws MalformedURLException
     */
    public String request(String graphPath, Bundle params, String httpMethod)
            throws FileNotFoundException, MalformedURLException, IOException {
        params.putString("format", "json");
        if (isSessionValid()) {
            params.putString(TOKEN, getAccessToken());
        }
        String url = (graphPath != null) ? GRAPH_BASE_URL + graphPath
                : RESTSERVER_URL;
        return Util.openUrl(url, httpMethod, params);
    }

    /**
     * Generate a UI dialog for the request action in the given Android context.
     * <p>
     * Note that this method is asynchronous and the callback will be invoked in
     * the original calling thread (not in a background thread).
     *
     * @param context  The Android context in which we will generate this dialog.
     * @param action   String representation of the desired method: e.g. "login",
     *                 "stream.publish", ...
     * @param listener Callback interface to notify the application when the dialog
     *                 has completed.
     */
    public void dialog(Context context, String action,
                       DialogListener listener) {
        dialog(context, action, new Bundle(), listener);
    }

    /**
     * Generate a UI dialog for the request action in the given Android context
     * with the provided parameters.
     * <p>
     * Note that this method is asynchronous and the callback will be invoked in
     * the original calling thread (not in a background thread).
     *
     * @param context    The Android context in which we will generate this dialog.
     * @param action     String representation of the desired method: e.g. "feed" ...
     * @param parameters String key-value pairs to be passed as URL parameters.
     * @param listener   Callback interface to notify the application when the dialog
     *                   has completed.
     */
    public void dialog(Context context, String action, Bundle parameters,
                       final DialogListener listener) {

        String endpoint = DIALOG_BASE_URL + action;
        parameters.putString("display", "touch");
        parameters.putString("redirect_uri", REDIRECT_URI);

        if (action.equals(LOGIN)) {
            parameters.putString("type", "user_agent");
            parameters.putString("client_id", mAppId);
        } else {
            parameters.putString("app_id", mAppId);
        }

        if (isSessionValid()) {
            parameters.putString(TOKEN, getAccessToken());
        }
        String url = endpoint + "?" + Util.encodeUrl(parameters);
        if (context.checkCallingOrSelfPermission(Manifest.permission.INTERNET)
                != PackageManager.PERMISSION_GRANTED) {
            Util.showAlert(context, "Error",
                    "Application requires permission to access the Internet");
        } else {
            new FbDialog(context, url, listener).show();
        }
    }

    /**
     * @return boolean - whether this object has an non-expired session token
     */
    public boolean isSessionValid() {
        return (getAccessToken() != null) &&
                ((getAccessExpires() == 0) ||
                        (System.currentTimeMillis() < getAccessExpires()));
    }

    /**
     * Retrieve the OAuth 2.0 access token for API access: treat with care.
     * Returns null if no session exists.
     *
     * @return String - access token
     */
    public String getAccessToken() {
        return mAccessToken;
    }

    /**
     * Set the OAuth 2.0 access token for API access.
     *
     * @param token - access token
     */
    public void setAccessToken(String token) {
        mAccessToken = token;
        mLastAccessUpdate = System.currentTimeMillis();
    }

    /**
     * Retrieve the current session's expiration time (in milliseconds since
     * Unix epoch), or 0 if the session doesn't expire or doesn't exist.
     *
     * @return long - session expiration time
     */
    public long getAccessExpires() {
        return mAccessExpires;
    }

    /**
     * Set the current session's expiration time (in milliseconds since Unix
     * epoch), or 0 if the session doesn't expire.
     *
     * @param time - timestamp in milliseconds
     */
    public void setAccessExpires(long time) {
        mAccessExpires = time;
    }

    /**
     * Retrieve the last time the token was updated (in milliseconds since
     * the Unix epoch), or 0 if the token has not been set.
     *
     * @return long - timestamp of the last token update.
     */
    public long getLastAccessUpdate() {
        return mLastAccessUpdate;
    }

    /**
     * Restore the token, expiration time, and last update time from cached values.
     * These should be values obtained from getAccessToken(), getAccessExpires, and
     * getLastAccessUpdate() respectively.
     *
     * @param accessToken      - access token
     * @param accessExpires    - access token expiration time
     * @param lastAccessUpdate - timestamp of the last token update
     */
    public void setTokenFromCache(String accessToken, long accessExpires, long lastAccessUpdate) {
        mAccessToken = accessToken;
        mAccessExpires = accessExpires;
        mLastAccessUpdate = lastAccessUpdate;
    }

    /**
     * Set the current session's duration (in seconds since Unix epoch), or "0"
     * if session doesn't expire.
     *
     * @param expiresIn - duration in seconds (or 0 if the session doesn't expire)
     */
    public void setAccessExpiresIn(String expiresIn) {
        if (expiresIn != null) {
            long expires = expiresIn.equals("0")
                    ? 0
                    : System.currentTimeMillis() + Long.parseLong(expiresIn) * 1000L;
            setAccessExpires(expires);
        }
    }

    public String getAppId() {
        return mAppId;
    }

    public void setAppId(String appId) {
        mAppId = appId;
    }


    /**
     * Callback interface for dialog requests.
     */
    public static interface DialogListener {

        /**
         * Called when a dialog completes.
         * <p>
         * Executed by the thread that initiated the dialog.
         *
         * @param values Key-value string pairs extracted from the response.
         */
        public void onComplete(Bundle values);

        /**
         * Called when a Facebook responds to a dialog with an error.
         * <p>
         * Executed by the thread that initiated the dialog.
         */
        public void onFacebookError(FacebookError e);

        /**
         * Called when a dialog has an error.
         * <p>
         * Executed by the thread that initiated the dialog.
         */
        public void onError(DialogError e);

        /**
         * Called when a dialog is canceled by the user.
         * <p>
         * Executed by the thread that initiated the dialog.
         */
        public void onCancel();

    }

    /**
     * Callback interface for service requests.
     */
    public static interface ServiceListener {

        /**
         * Called when a service request completes.
         *
         * @param values Key-value string pairs extracted from the response.
         */
        public void onComplete(Bundle values);

        /**
         * Called when a Facebook server responds to the request with an error.
         */
        public void onFacebookError(FacebookError e);

        /**
         * Called when a Facebook Service responds to the request with an error.
         */
        public void onError(Error e);

    }
}
