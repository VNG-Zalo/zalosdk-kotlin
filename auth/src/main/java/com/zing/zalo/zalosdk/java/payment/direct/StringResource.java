package com.zing.zalo.zalosdk.java.payment.direct;

import java.util.HashMap;

public class StringResource {
    public static HashMap<String, String> mapStringResource = new HashMap<String, String>();


    public static String getString(Object key) {
        String result = null;
        if (((String) key).equals("zalosdk_transaction_timeOut_mess")) {
            result = StringResource.mapStringResource.get(key);
            return result == null ? "Giao dịch đã quá thời gian cho phép" : result;
        }
        if (((String) key).equals("get_status_timeOut")) {
            result = StringResource.mapStringResource.get(key);
            return result == null ? "30000" : result;
        }
        if (((String) key).equals("OAuthCodeParam")) {
            result = StringResource.mapStringResource.get(key);
            return result == null ? "code" : result;
        }
        if (((String) key).equals("zalosdk_no_internet")) {
            result = StringResource.mapStringResource.get(key);
            return result == null ? "Mạng không ổn định, vui lòng thử lại sau" : result;
        }
        if (((String) key).equals("durationTimeForAsync")) {
            result = StringResource.mapStringResource.get(key);
            return result == null ? "1800000" : result;
        }
        if (((String) key).equals("zalosdk_maintance")) {
            result = StringResource.mapStringResource.get(key);
            return result == null ? "Hệ thống đang bảo trì, bạn vui lòng thử lại sau" : result;
        }
        if (((String) key).equals("zalosdk_processing")) {
            result = StringResource.mapStringResource.get(key);
            return result == null ? "Đang xử lý" : result;
        }


        return result;
    }

}
