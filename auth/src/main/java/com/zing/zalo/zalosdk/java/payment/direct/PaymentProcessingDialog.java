package com.zing.zalo.zalosdk.java.payment.direct;


import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnDismissListener;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.zing.zalo.zalosdk.kotlin.oauth.R;

public class PaymentProcessingDialog extends PaymentDialog implements OnDismissListener {

    public boolean isShow = false;
    String LOG_TAG = PaymentProcessingDialog.class.getSimpleName();
    String zalosdk_processing;
    String zalosdk_success;
    String zalosdk_unsuccess;
    boolean isTimeOut = false;
    int viewIndex = 0;
    Handler handler;
    OnCloseListener listener;
    Runnable runnable = new Runnable() {
        @Override
        public void run() {
            if (viewIndex > 0 && isShowing()) {
                hideView();
                if (listener != null) {
                    listener.onClose();
                }
            }
        }
    };

    public PaymentProcessingDialog(Context context, OnCloseListener listener) {
        super(context);
        handler = new Handler();
        this.listener = listener;
        setOnDismissListener(this);
        zalosdk_processing = StringResource.getString("zalosdk_processing");
        zalosdk_success = StringResource.getString("zalosdk_success");
        zalosdk_unsuccess = StringResource.getString("zalosdk_unsuccess");
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.zalosdk_activity_processing);
        //showProcessingView();
    }

    public void updateProcessingTransactionView(String text) {
        ((TextView) findViewById(R.id.zalosdk_message_ctl)).setText(text);
    }

    private void showProcessingView() {
        Log.i("THREAD BEGIN", "PROCESSING!!");
        ((TextView) findViewById(R.id.zalosdk_message_ctl)).setText(zalosdk_processing);
        findViewById(R.id.zalosdk_status_ctl).setVisibility(View.GONE);
        findViewById(R.id.zalosdk_indicator_ctl).setVisibility(View.VISIBLE);
        setCancelable(false);
        viewIndex = 0;
    }

    private void showSuccessView() {
        Log.i(LOG_TAG, "Show success dialog");
        ((TextView) findViewById(R.id.zalosdk_message_ctl)).setText(zalosdk_success);
        findViewById(R.id.zalosdk_indicator_ctl).setVisibility(View.GONE);
        ImageView status = (ImageView) findViewById(R.id.zalosdk_status_ctl);
        status.setImageResource(R.drawable.zalosdk_ic_success);
        status.setVisibility(View.VISIBLE);
        setCancelable(true);
        viewIndex = 1;
        autoClose();
    }

    private void showUnSuccessView() {
        ((TextView) findViewById(R.id.zalosdk_message_ctl)).setText(zalosdk_unsuccess);
        findViewById(R.id.zalosdk_indicator_ctl).setVisibility(View.GONE);
        ImageView status = (ImageView) findViewById(R.id.zalosdk_status_ctl);
        status.setImageResource(R.drawable.zalosdk_ic_fail);
        status.setVisibility(View.VISIBLE);
        setCancelable(true);
        viewIndex = 1;
        autoClose();
    }

    private void showTimeOutView() {
        Log.i("debuglog", "time out showview");
        findViewById(R.id.zalosdk_process_dialog_ctl).setVisibility(View.GONE);
        setCancelable(true);
        viewIndex = 1;
        handler.postDelayed(runnable, 1);
        isTimeOut = true;
    }

    private void autoClose() {
        handler.postDelayed(runnable, 3000);
    }

    public void showView(Status status) {
        show();
        isShow = true;
        switch (status) {
            case PROCESSING:
                showProcessingView();
                break;
            case SUCCESS:
                showSuccessView();
                break;
            case FAILED:
                showUnSuccessView();
                break;
            case TIMEOUT:
                showTimeOutView();
                break;
        }
    }

    @Override
    public void onDismiss(DialogInterface dialog) {
        if (listener != null) {
            Log.i(getClass().getName(), "cancel");
            listener.onClose();
        }
    }

    @Override
    public void onBackPressed() {
        //cancel();
    }

    public void hideView() {
        isShow = false;
        hide();
    }

    @Override
    public void show() {
        Log.i(LOG_TAG, "show loading..");
        try {
            super.show();
        } catch (Exception ex) {
            Log.i(LOG_TAG, "error can not show loading");
        }

    }

    @Override
    public void hide() {
        Log.i(LOG_TAG, "hide loading!");
        super.hide();
    }

    public enum Status {
        PROCESSING,
        SUCCESS,
        FAILED,
        TIMEOUT;
    }

    public interface OnCloseListener {
        void onClose();
    }
}
