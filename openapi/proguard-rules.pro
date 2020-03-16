-keepattributes MethodParameters,LineNumberTable,LocalVariableTable,LocalVariableTypeTable


-keep class com.zing.zalo.zalosdk.kotlin.openapi.model.** { *; }
-keep interface com.zing.zalo.zalosdk.kotlin.openapi.ZaloOpenApiCallback {*;}
-keep interface com.zing.zalo.zalosdk.kotlin.openapi.ZaloPluginCallback {*;}
-keep interface com.zing.zalo.zalosdk.kotlin.openapi.IZaloOpenApi {*;}
-keep class com.zing.zalo.zalosdk.kotlin.openapi.ZaloOpenApi {*;}
-keep class com.zing.zalo.zalosdk.kotlin.openapi.** {*;}

-keep class com.zing.zalo.zalosdk.kotlin.openapi.exception.OpenApiException { *; }

-keepclasseswithmembers public interface androidx.annotation.Nullable { *;}
# Output a source map file
-printmapping proguard.map
