package com.zing.zalo.zalosdk.demo

import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity

abstract class ZBaseActivity: AppCompatActivity() {


    fun showToast(msg: String) {
        Toast.makeText(this, msg, Toast.LENGTH_SHORT).show()
    }
}