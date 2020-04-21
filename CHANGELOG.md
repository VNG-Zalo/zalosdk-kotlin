# Changelog

## 2.5.0421 - 2020/04/21

### Added

- Thêm data class ErrorResponse, xử lý trong callback khi xuất hiện lỗi khi đăng nhập web_login, browser và App
- Thêm onAuthenticateError(errorCode: Int, errorMsg: String?, errorResponse: ErrorResponse) trong interface IAuthenticateCompleteListener
- Thêm new method shareMessage, shareFeed với string param
### Deprecated

- Deprecated FeedData
- Deprecated old method shareMessage, shareFeed với FeedData param

### Fixed

- Fix lỗi khi  user đăng nhập bằng browser không back về app
- Fix bug login facebook no facebook id, google no default_web_client
- Fix callback ErrorResponse webview, browser, register zalo khi không có internet
- Fix không nhận data onActivityResult khi login Facebook APP

## 2.5.0316 - 2020/03/16

### Added

- Module Zptracking, Wakeup, Authext
- ZingAnalyticsManager trong analytics module
- LocalizedString

### Fixed

- Openapi trả về json error khi Zalo app chưa được install
- Fixed getSDKVersion trả về version thay vì chuỗi rỗng

### Changed

- Thay đổi static AppInfo sang Singleton AppInfo.getInstance()

## 2.5.0219 - 2020/02/19

### Changed

- Singleton ZaloSDK sang Class ZaloSDK(context:Context, authCode:String)
- Singleton ZaloOpenApi sang Class ZaloOpenApi(context:Context, authCode:String)
- Thay đổi tên bintray repo ZaloSDKKotlin và Organization sang "vng"
- Refactor package "com.zing.zalo.zalosdk" sang "com.zing.zalo.zalosdk.kotlin"
- Thay đổi các method sử dụng asynctask sang coroutine

### Added

- ZaloInitProvider, Abstract class BaseModule
- Service map: tự cập nhật domain của các api

### Remove

- Deprecated method initDeviceTracking() trong class DeviceTracking
