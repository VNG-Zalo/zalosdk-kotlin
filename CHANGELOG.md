# Changelog

All notable changes to this project will be documented in this file.

## [2.5.0219] - 2020/02/19

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