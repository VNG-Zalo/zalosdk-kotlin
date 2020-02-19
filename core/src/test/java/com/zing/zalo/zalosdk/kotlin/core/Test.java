package com.zing.zalo.zalosdk.kotlin.core;

import java.text.SimpleDateFormat;
import java.util.Date;

public class Test {
    @org.junit.Test
    public void Abc() {

        String date = new SimpleDateFormat("MM-dd").format(new Date());
        String day = date.split("-")[0];
        String month = date.split("-")[1];
        System.out.println(day);
        System.out.println(month);
    }
}
