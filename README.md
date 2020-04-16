# **ZALO&#39;S KOTLIN SDK**

ZaloSDK for Android

1. Cách sử dụng

- Import module từ jfrog/jCenter

```
        def sdkVersion = sdk_version
        implementation "com.zing.zalo.zalosdk.kotlin:core:$sdkVersion"
        implementation "com.zing.zalo.zalosdk.kotlin:auth:$sdkVersion"
        implementation "com.zing.zalo.zalosdk.kotlin:openapi:$sdkVersion"
        implementation "com.zing.zalo.zalosdk.kotlin:analytics:$sdkVersion"
```

- Lấy application hash key

```
        AppInfo.getInstance().getApplicationHashKey(this);
```

- Đăng ký app trên web
  [https://developers.zalo.me](https://developers.zalo.me/) để lấy appID
- Tạo ứng dụng android với package name và appID

- Thêm meta-data vào Android Manifest

```
        <meta-data
            android:name="com.zing.zalo.zalosdk.appID"
            android:value="@string/appID" />
```

- Thêm thẻ string vào string.xml

```
        <string name="zalosdk_login_protocol_schema"
                translatable="false">zalo-$appID</string>
        <string name="zalosdk_app_id" translatable="false">$appID</string>
```

2. Gọi phương thức ZaloSdk trong Java - Kotlin

```
        Java:   ZaloSDK zaloSDK = new ZaloSDK(Context context, String authCode);
        Kotlin: val zaloSDK = ZaloSDK(context:Context) 
```

3. Đăng nhập Zalo bằng Zalo's SDK
- Có thể đăng nhập Zalo bằng Application, Webview ( hoặc Browser) bằng enum LoginVia.APP, LoginVia.WEB, LoginVia.APP_OR_WEB
- Java Project
```
        ZaloSDK zaloSDK;
        zaloSDK.authenticate(MainActivity.this, LoginVia.APP, listener);
        
        @Override
        protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
            super.onActivityResult(requestCode, resultCode, data);
            zaloSDK.onActivityResult(this, requestCode, resultCode, data);
        }   
```

- Kotlin project

```
        val zaloSDK = ZaloSDK(context:Context)
        
        private val authenticateListener = object : IAuthenticateCompleteListener {
            override fun onAuthenticateSuccess(uid: Long, code: String, data: Map<String, Any>) {
                TODO("not implemented")
            }
    
            override fun onAuthenticateError(errorCode: Int, message: String) {
                TODO("not implemented")
            }
        }
    
        zaloSDK.authenticate(this, LoginVia.APP_OR_WEB, authenticateListener)
        
```

4. Get Profile bằng Zalo's SDK
- Có thể getProfile user theo thông tin như fields : id, birthday, gender, picture, name
- Java project
```
        ZaloOpenApi zaloOpenApi = new ZaloOpenApi(this, zaloSDK.getOauthCode());
        private ZaloOpenApiCallback callback = new ZaloOpenApiCallback() {
            @Override
            public void onResult(JSONObject jSONObject) {
                String data = jSONObject.toString();
                String result = "Data: " + data;
                resultTextView.setText(result);
            }
        };
        
        String[] fields = {"name", "id"};
        zaloOpenApi.getProfile(fields, callback);
```
- Kotlin project
```
        val zaloOpenApi = ZaloOpenApi(this,zaloSDK.getOauthCode())
        private ZaloOpenApiCallback callback = new ZaloOpenApiCallback() {
            @Override
            public void onResult(JSONObject jSONObject) {
                String data = jSONObject.toString();
                String result = "Data: " + data;
                resultTextView.setText(result);
            }
        };
        
        val fields = arrayOf("id", "birthday", "gender", "picture", "name")
        zaloOpenApi.getProfile(fields, callback)
```

