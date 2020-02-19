-keepattributes MethodParameters,LineNumberTable,LocalVariableTable,LocalVariableTypeTable

-dontwarn androidx.**
-keep class androidx.** { *; }
-keep interface androidx.** { *;}

#-keep class com.zing.zalo.zalosdk.kotlin.oauth.helper.AuthStorage { *; }
-keep class com.zing.zalo.zalosdk.kotlin.oauth.ZaloSDK {
    public *;
 }

-keep enum  com.zing.zalo.zalosdk.kotlin.oauth.LoginVia { *;}
-keep class com.zing.zalo.zalosdk.kotlin.oauth.callback.** { *;}

#print mapping
-printmapping proguard.map