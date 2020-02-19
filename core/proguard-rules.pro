#-verbose
-keepattributes MethodParameters,LineNumberTable,LocalVariableTable,LocalVariableTypeTable

#Android's module
-dontwarn androidx.**
-keep class androidx.** { *; }
-keep interface androidx.** { *;}

-dontwarn kotlinx.**
-keep class kotlinx.** { *; }
-keep interface kotlinx.** { *;}

#Device Tracking
-keep class com.zing.zalo.zalosdk.kotlin.core.devicetrackingsdk.** { *; }
-keep class com.zing.zalo.devicetrackingsdk.** {*;}
#-keepclasseswithmembers class com.zing.zalo.zalosdk.kotlin.core.devicetrackingsdk.model.PreloadInfo {*;}

#App Tracking
-keep interface com.zing.zalo.zalosdk.kotlin.core.apptracking.AppTrackerListener { *;}
-keep interface com.zing.zalo.zalosdk.kotlin.core.apptracking.IAppTracker { *;}
-keep class com.zing.zalo.zalosdk.kotlin.core.apptracking.** { *;}

#Helper
-keepclassmembers class com.zing.zalo.zalosdk.kotlin.core.helper.DeviceInfo { *;}
-keep class com.zing.zalo.zalosdk.kotlin.core.helper.** {  *;}

#Log
-keep class com.zing.zalo.zalosdk.kotlin.core.log.Log { *;}

#Module
-keep class com.zing.zalo.zalosdk.kotlin.core.module.** {
 *;
}

#Http
-keepclasseswithmembers class com.zing.zalo.zalosdk.kotlin.core.http.** { *;}

#Service Map
-keep class com.zing.zalo.zalosdk.kotlin.core.servicemap.** { *;}

#SettingManager
-keep class com.zing.zalo.zalosdk.kotlin.core.settings.** {*;}

# -keep public class * extends android.content.BroadcastReceiver
#Android"s module
#-keep public class * extends android.app.Activity
# -keep public class * extends android.app.Application
# -keep public class * extends android.app.Service
# -keep public class * extends android.content.BroadcastReceiver
#-keep class android.content.ContentProvider { *;}


#print mapping
-printmapping proguard.map