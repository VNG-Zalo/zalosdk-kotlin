-keepattributes MethodParameters,LineNumberTable,LocalVariableTable,LocalVariableTypeTable

#-keep class com.zing.zalo.zalosdk.kotlin.analytics.EventTracker { *; }
-keepclasseswithmembers class com.zing.zalo.zalosdk.kotlin.analytics.EventTrackerListener { *; }
-keepclasseswithmembers class com.zing.zalo.zalosdk.kotlin.analytics.IEventTracker { *; }
-keepclasseswithmembers class com.zing.zalo.zalosdk.kotlin.analytics.model.Event {*;}
-keepclasseswithmembers class com.zing.zalo.zalosdk.kotlin.analytics.EventTracker$Companion { com.zing.zalo.zalosdk.kotlin.analytics.EventTracker getInstance(); }

-keepnames class com.zing.zalo.zalosdk.kotlin.analytics.sqlite.**{*;}
-keepnames class com.zing.zalo.zalosdk.kotlin.analytics.EventStorage {*;}

# Output a source map file
-printmapping proguard.map