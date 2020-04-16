-repackageclasses com.zing.zalo.zalosdk.authext.internal
-keepattributes MethodParameters,LineNumberTable,LocalVariableTable,LocalVariableTypeTable
-keep class com.zing.zalo.zalosdk.oauth.AuthenticateExtention
-keepclassmembers public class com.zing.zalo.zalosdk.oauth.AuthenticateExtention {
    public static <methods>;
    public <methods>;
    public static <fields>;
    public <fields>;
}

-keep class com.zing.zalo.zalosdk.oauth.LoginForm{ *; }
-keep interface com.zing.zalo.zalosdk.oauth.LoginForm$ShowProtectGuestAccountListener { *;}
-keep class com.zing.zalo.zalosdk.oauth.ZingMeBaseLoginView { *; }
-keep class com.zing.zalo.zalosdk.oauth.ZingMeLoginView { *; }
-keepclasseswithmembernames class com.zing.zalo.zalosdk.oauth.OAuthCompleteListener { *; }
-keep class com.zing.zalo.zalosdk.oauth.OauthResponse { *; }
-keepclassmembers class com.zing.zalo.zalosdk.oauth.LoginForm$LoginListener {
    public *;
}


-keep public class * extends android.app.Activity
-keep public class * extends android.app.Application
-keep public class * extends android.app.Service
-keep public class * extends android.content.BroadcastReceiver
-keep public class * extends android.content.ContentProvider
-keep public class * extends android.app.backup.BackupAgentHelper
-keep public class * extends android.preference.Preference

-keepclasseswithmembernames class * {
    native <methods>;
    public static <methods>;
}

-keepclasseswithmembers class * {
    public <init>(android.content.Context, android.util.AttributeSet);
}

-keepclasseswithmembers class * {
    public <init>(android.content.Context, android.util.AttributeSet, int);
}

-keepclassmembers class * extends android.app.Activity {
    public void *(android.view.View);
}

-keepclassmembers enum * {
    public static **[] values();
    public static ** valueOf(java.lang.String);
}

-keep class * implements android.os.Parcelable {
    public static final android.os.Parcelable$Creator *;
}

# Output a source map file
-printmapping proguard.map