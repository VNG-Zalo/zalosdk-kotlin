package com.zing.zalo.zalosdk.facebook;

import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.ProviderInfo;
import android.content.pm.ResolveInfo;
import android.content.pm.Signature;
import android.database.Cursor;
import android.net.Uri;
import android.os.Build;
import android.text.TextUtils;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.TreeSet;
import java.util.concurrent.atomic.AtomicBoolean;

public final class NativeProtocol {

    public static final String FACEBOOK_PROXY_AUTH_PERMISSIONS_KEY = "scope";
    public static final String FACEBOOK_PROXY_AUTH_APP_ID_KEY = "client_id";
    public static final String FACEBOOK_PROXY_AUTH_E2E_KEY = "e2e";
    public static final String DIALOG_PARAM_RESPONSE_TYPE = "response_type";
    public static final String DIALOG_PARAM_RETURN_SCOPES = "return_scopes";
    public static final String DIALOG_PARAM_SCOPE = "scope";
    public static final String DIALOG_RESPONSE_TYPE_TOKEN_AND_SIGNED_REQUEST = "token,signed_request";
    public static final String DIALOG_RETURN_SCOPES_TRUE = "true";
    public static final String DIALOG_PARAM_DEFAULT_AUDIENCE = "default_audience";
    public static final String DIALOG_REREQUEST_AUTH_TYPE = "rerequest";
    public static final String DIALOG_PARAM_LEGACY_OVERRIDE = "legacy_override";
    public static final String GRAPH_API_VERSION = "v2.3";
    public static final String DIALOG_PARAM_AUTH_TYPE = "auth_type";
    public static final int NO_PROTOCOL_AVAILABLE = -1;
    public static final int PROTOCOL_VERSION_20121101 = 20121101;
    public static final int PROTOCOL_VERSION_20130502 = 20130502;
    public static final int PROTOCOL_VERSION_20130618 = 20130618;
    public static final int PROTOCOL_VERSION_20131107 = 20131107;
    public static final int PROTOCOL_VERSION_20140204 = 20140204;
    public static final int PROTOCOL_VERSION_20140324 = 20140324;
    public static final int PROTOCOL_VERSION_20140701 = 20140701;
    public static final int PROTOCOL_VERSION_20141001 = 20141001;
    public static final int PROTOCOL_VERSION_20141028 = 20141028;
    public static final int PROTOCOL_VERSION_20141107 = 20141107; // Bucketed Result Intents
    public static final int PROTOCOL_VERSION_20141218 = 20141218;
    public static final String EXTRA_PERMISSIONS = "com.facebook.platform.extra.PERMISSIONS";
    public static final String EXTRA_ACCESS_TOKEN = "com.facebook.platform.extra.ACCESS_TOKEN";
    public static final String EXTRA_EXPIRES_SECONDS_SINCE_EPOCH = "com.facebook.platform.extra.EXPIRES_SECONDS_SINCE_EPOCH";
    static final String INTENT_ACTION_PLATFORM_SERVICE = "com.facebook.platform.PLATFORM_SERVICE";
    private static final String FACEBOOK_PROXY_AUTH_ACTIVITY = "com.facebook.katana.ProxyAuth";
    private static final String PLATFORM_PROVIDER_VERSION_COLUMN = "version";
    private static final String CONTENT_SCHEME = "content://";
    private static final String PLATFORM_PROVIDER = ".provider.PlatformProvider";
    private static final String PLATFORM_PROVIDER_VERSIONS = PLATFORM_PROVIDER + "/versions";
    // Note: be sure this stays sorted in descending order; add new versions at the beginning
    private static final List<Integer> KNOWN_PROTOCOL_VERSIONS =
            Arrays.asList(
                    PROTOCOL_VERSION_20141218,
                    PROTOCOL_VERSION_20141107,
                    PROTOCOL_VERSION_20141028,
                    PROTOCOL_VERSION_20141001,
                    PROTOCOL_VERSION_20140701,
                    PROTOCOL_VERSION_20140324,
                    PROTOCOL_VERSION_20140204,
                    PROTOCOL_VERSION_20131107,
                    PROTOCOL_VERSION_20130618,
                    PROTOCOL_VERSION_20130502,
                    PROTOCOL_VERSION_20121101
            );
    private static final NativeAppInfo FACEBOOK_APP_INFO = new KatanaAppInfo();
    private static List<NativeAppInfo> facebookAppInfoList = buildFacebookAppList();
    private static AtomicBoolean protocolVersionsAsyncUpdating = new AtomicBoolean(false);

    private static List<NativeAppInfo> buildFacebookAppList() {
        List<NativeAppInfo> list = new ArrayList<NativeAppInfo>();

        // Katana needs to be the first thing in the list since it will get selected as the default
        // FACEBOOK_APP_INFO
        list.add(FACEBOOK_APP_INFO);
        list.add(new WakizashiAppInfo());

        return list;
    }

    private static TreeSet<Integer> fetchAllAvailableProtocolVersionsForAppInfo(
            NativeAppInfo appInfo) {
        TreeSet<Integer> allAvailableVersions = new TreeSet<>();

        Context appContext = Facebook.getApplicationContext();
        ContentResolver contentResolver = appContext.getContentResolver();

        String[] projection = new String[]{PLATFORM_PROVIDER_VERSION_COLUMN};
        Uri uri = buildPlatformProviderVersionURI(appInfo);
        Cursor c = null;
        try {
            // First see if the base provider exists as a check for whether the native app is
            // installed. We do this prior to querying, to prevent errors from being output to
            // logcat saying that the provider was not found.
            PackageManager pm = Facebook.getApplicationContext().getPackageManager();
            String contentProviderName = appInfo.getPackage() + PLATFORM_PROVIDER;
            ProviderInfo pInfo = pm.resolveContentProvider(contentProviderName, 0);
            if (pInfo != null) {
                c = contentResolver.query(uri, projection, null, null, null);
                if (c != null) {
                    while (c.moveToNext()) {
                        int version = c.getInt(c.getColumnIndex(PLATFORM_PROVIDER_VERSION_COLUMN));
                        allAvailableVersions.add(version);
                    }
                }
            }

            return allAvailableVersions;
        } finally {
            if (c != null) {
                c.close();
            }
        }
    }

    private static Uri buildPlatformProviderVersionURI(NativeAppInfo appInfo) {
        return Uri.parse(CONTENT_SCHEME + appInfo.getPackage() + PLATFORM_PROVIDER_VERSIONS);
    }

    public static Intent createProxyAuthIntent(
            Context context,
            String applicationId,
            Collection<String> permissions,
            String e2e,
            boolean isRerequest,
            boolean isForPublish,
            String defaultAudience) {
        for (NativeAppInfo appInfo : facebookAppInfoList) {
            Intent intent = new Intent()
                    .setClassName(appInfo.getPackage(), FACEBOOK_PROXY_AUTH_ACTIVITY)
                    .putExtra(FACEBOOK_PROXY_AUTH_APP_ID_KEY, applicationId);

            if (permissions != null && permissions.size() > 0) {
                intent.putExtra(
                        FACEBOOK_PROXY_AUTH_PERMISSIONS_KEY, TextUtils.join(",", permissions));
            }
            if (!TextUtils.isEmpty(e2e)) {
                intent.putExtra(FACEBOOK_PROXY_AUTH_E2E_KEY, e2e);
            }

            intent.putExtra(
                    DIALOG_PARAM_RESPONSE_TYPE,
                    DIALOG_RESPONSE_TYPE_TOKEN_AND_SIGNED_REQUEST);
            intent.putExtra(
                    DIALOG_PARAM_RETURN_SCOPES,
                    DIALOG_RETURN_SCOPES_TRUE);
            if (isForPublish) {
                intent.putExtra(
                        DIALOG_PARAM_DEFAULT_AUDIENCE, defaultAudience);
            }

            // Override the API Version for Auth
            intent.putExtra(DIALOG_PARAM_LEGACY_OVERRIDE, GRAPH_API_VERSION);

            // Set the re-request auth type for requests
            if (isRerequest) {
                intent.putExtra(DIALOG_PARAM_AUTH_TYPE, DIALOG_REREQUEST_AUTH_TYPE);
            }

            intent = validateActivityIntent(context, intent, appInfo);

            if (intent != null) {
                return intent;
            }
        }
        return null;
    }

    public static Intent createPlatformServiceIntent(Context context) {
        for (NativeAppInfo appInfo : facebookAppInfoList) {
            Intent intent = new Intent(INTENT_ACTION_PLATFORM_SERVICE)
                    .setPackage(appInfo.getPackage())
                    .addCategory(Intent.CATEGORY_DEFAULT);
            intent = validateServiceIntent(context, intent, appInfo);
            if (intent != null) {
                return intent;
            }
        }
        return null;
    }

    static Intent validateActivityIntent(Context context, Intent intent, NativeAppInfo appInfo) {
        if (intent == null) {
            return null;
        }

        ResolveInfo resolveInfo = context.getPackageManager().resolveActivity(intent, 0);
        if (resolveInfo == null) {
            return null;
        }

        if (!appInfo.validateSignature(context, resolveInfo.activityInfo.packageName)) {
            return null;
        }

        return intent;
    }

    static Intent validateServiceIntent(Context context, Intent intent, NativeAppInfo appInfo) {
        if (intent == null) {
            return null;
        }

        ResolveInfo resolveInfo = context.getPackageManager().resolveService(intent, 0);
        if (resolveInfo == null) {
            return null;
        }

        if (!appInfo.validateSignature(context, resolveInfo.serviceInfo.packageName)) {
            return null;
        }

        return intent;
    }

    public static void updateAllAvailableProtocolVersionsAsync() {
        if (!protocolVersionsAsyncUpdating.compareAndSet(false, true)) {
            return;
        }

//        FacebookSdk.getExecutor().execute(new Runnable() {
//            @Override
//            public void run() {
//                try {
//                    for (NativeAppInfo appInfo : facebookAppInfoList) {
//                        appInfo.fetchAvailableVersions(true);
//                    }
//                } finally {
//                    protocolVersionsAsyncUpdating.set(false);
//                }
//            }
//        });
    }

    private static int getLatestAvailableProtocolVersionForAppInfoList(
            List<NativeAppInfo> appInfoList,
            int[] versionSpec) {
        // Kick off an update
        updateAllAvailableProtocolVersionsAsync();

        if (appInfoList == null) {
            return NO_PROTOCOL_AVAILABLE;
        }

        // Could potentially cache the NativeAppInfo to latestProtocolVersion
        for (NativeAppInfo appInfo : appInfoList) {
            int protocolVersion =
                    computeLatestAvailableVersionFromVersionSpec(
                            appInfo.getAvailableVersions(),
                            getLatestKnownVersion(),
                            versionSpec);

            if (protocolVersion != NO_PROTOCOL_AVAILABLE) {
                return protocolVersion;
            }
        }

        return NO_PROTOCOL_AVAILABLE;
    }

    public static int getLatestAvailableProtocolVersionForService(final int minimumVersion) {
        // Services are currently always against the Facebook App
        return getLatestAvailableProtocolVersionForAppInfoList(facebookAppInfoList, new int[]{minimumVersion});
    }

    public static int computeLatestAvailableVersionFromVersionSpec(
            TreeSet<Integer> allAvailableFacebookAppVersions,
            int latestSdkVersion,
            int[] versionSpec) {
        // Remember that these ranges are sorted in ascending order and can be unbounded. So we are
        // starting from the end of the version-spec array and working backwards, to try get the
        // newest possible version
        int versionSpecIndex = versionSpec.length - 1;
        Iterator<Integer> fbAppVersionsIterator =
                allAvailableFacebookAppVersions.descendingIterator();
        int latestFacebookAppVersion = -1;

        while (fbAppVersionsIterator.hasNext()) {
            int fbAppVersion = fbAppVersionsIterator.next();

            // We're holding on to the greatest fb-app version available.
            latestFacebookAppVersion = Math.max(latestFacebookAppVersion, fbAppVersion);

            // If there is a newer version in the versionSpec, throw it away, we don't have it
            while (versionSpecIndex >= 0 && versionSpec[versionSpecIndex] > fbAppVersion) {
                versionSpecIndex--;
            }

            if (versionSpecIndex < 0) {
                // There was no fb app version that fell into any range in the versionSpec - or -
                // the versionSpec was empty, which means that this action is not supported.
                return NO_PROTOCOL_AVAILABLE;
            }

            // If we are here, we know we are within a range specified in the versionSpec. We should
            // see if it is a disabled or enabled range.

            if (versionSpec[versionSpecIndex] == fbAppVersion) {
                // if the versionSpecIndex is even, it is enabled; if odd, disabled
                return (
                        versionSpecIndex % 2 == 0 ?
                                Math.min(latestFacebookAppVersion, latestSdkVersion) :
                                NO_PROTOCOL_AVAILABLE
                );
            }
        }

        return NO_PROTOCOL_AVAILABLE;
    }

    public static final int getLatestKnownVersion() {
        return KNOWN_PROTOCOL_VERSIONS.get(0);
    }

    private static abstract class NativeAppInfo {
        private static final String FBI_HASH = "a4b7452e2ed8f5f191058ca7bbfd26b0d3214bfc";
        private static final String FBL_HASH = "5e8f16062ea3cd2c4a0d547876baa6f38cabf625";
        private static final String FBR_HASH = "8a3c4b262d721acd49a4bf97d5213199c86fa2b9";
        private static final HashSet<String> validAppSignatureHashes = buildAppSignatureHashes();
        private TreeSet<Integer> availableVersions;

        private static HashSet<String> buildAppSignatureHashes() {
            HashSet<String> set = new HashSet<String>();
            set.add(FBR_HASH);
            set.add(FBI_HASH);
            set.add(FBL_HASH);
            return set;
        }

        abstract protected String getPackage();

        public boolean validateSignature(Context context, String packageName) {
            String brand = Build.BRAND;
            int applicationFlags = context.getApplicationInfo().flags;
            if (brand.startsWith("generic") &&
                    (applicationFlags & ApplicationInfo.FLAG_DEBUGGABLE) != 0) {
                // We are debugging on an emulator, don't validate package signature.
                return true;
            }

            PackageInfo packageInfo = null;
            try {
                packageInfo = context.getPackageManager().getPackageInfo(packageName,
                        PackageManager.GET_SIGNATURES);
            } catch (PackageManager.NameNotFoundException e) {
                return false;
            }

            for (Signature signature : packageInfo.signatures) {
                String hashedSignature = Util.sha1hash(signature.toByteArray());
                if (validAppSignatureHashes.contains(hashedSignature)) {
                    return true;
                }
            }

            return false;
        }

        public TreeSet<Integer> getAvailableVersions() {
            if (availableVersions == null) {
                fetchAvailableVersions(false);
            }
            return availableVersions;
        }

        private synchronized void fetchAvailableVersions(boolean force) {
            if (force || availableVersions == null) {
                availableVersions = fetchAllAvailableProtocolVersionsForAppInfo(this);
            }
        }
    }

    private static class KatanaAppInfo extends NativeAppInfo {
        static final String KATANA_PACKAGE = "com.facebook.katana";

        @Override
        protected String getPackage() {
            return KATANA_PACKAGE;
        }
    }

    private static class MessengerAppInfo extends NativeAppInfo {
        static final String MESSENGER_PACKAGE = "com.facebook.orca";

        @Override
        protected String getPackage() {
            return MESSENGER_PACKAGE;
        }
    }

    private static class WakizashiAppInfo extends NativeAppInfo {
        static final String WAKIZASHI_PACKAGE = "com.facebook.wakizashi";

        @Override
        protected String getPackage() {
            return WAKIZASHI_PACKAGE;
        }
    }
}
