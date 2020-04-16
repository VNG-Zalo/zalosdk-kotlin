package com.zing.zalo.zalosdk.kotlin.core;

import org.junit.Test;

import java.text.SimpleDateFormat;
import java.util.Date;

public class asv {

    @Test
    public void abc() {

        String date = new SimpleDateFormat("MM-dd").format(new Date());
        String month = date.split("-")[0];
        String day = date.split("-")[1];
        Integer versionCode = Integer.parseInt("25" + month + day);
        String s = null;
        System.out.println(s == null ? "" : s);
    }
}
