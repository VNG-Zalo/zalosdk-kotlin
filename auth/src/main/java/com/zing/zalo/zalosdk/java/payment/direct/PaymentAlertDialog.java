package com.zing.zalo.zalosdk.java.payment.direct;


import android.content.Context;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.TextView;

import com.zing.zalo.zalosdk.kotlin.oauth.R;

public class PaymentAlertDialog extends PaymentDialog implements View.OnClickListener {
    OnCancelListener cancelListener = null;
    OnOkListener listener = null;
    String okTitle;
    boolean hideOkButton = false;
    String title;

    public PaymentAlertDialog(Context context) {
        super(context);
    }
    public PaymentAlertDialog(Context context, OnOkListener listener) {
        super(context);
        this.listener = listener;
    }

    public PaymentAlertDialog(Context context, OnOkListener listener, OnCancelListener cancelListener) {
        super(context);
        this.listener = listener;
        this.cancelListener = cancelListener;
    }

    public void setOnOkListener(OnOkListener listener) {
        this.listener = listener;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.zalosdk_activity_alert);
        findViewById(R.id.zalosdk_ok_ctl).setOnClickListener(this);
        if (!TextUtils.isEmpty(okTitle))
            ((TextView) findViewById(R.id.zalosdk_ok_ctl)).setText(okTitle);
        if (cancelListener == null) {
            findViewById(R.id.button_devider).setVisibility(View.GONE);
            findViewById(R.id.zalosdk_cancel_ctl).setVisibility(View.GONE);
            findViewById(R.id.zalosdk_cancel_ctl).setOnClickListener(null);
        } else {
            findViewById(R.id.button_devider).setVisibility(View.VISIBLE);
            findViewById(R.id.zalosdk_cancel_ctl).setVisibility(View.VISIBLE);
            findViewById(R.id.zalosdk_cancel_ctl).setOnClickListener(this);
        }
    }

    public void setTitle(String _title) {

        title = _title;

    }

    public void setOkButtonTitle(String title) {
        this.okTitle = title;


    }

    public void hideOkButton(boolean isHide) {
        hideOkButton = isHide;
    }

    public void showAlert(String message) {
        show();
        ((TextView) findViewById(R.id.zalosdk_message_ctl)).setText(message);
        View ok = findViewById(R.id.zalosdk_ok_ctl);
        if (hideOkButton) {
            ok.setVisibility(View.GONE);
        } else {
            ok.setVisibility(View.VISIBLE);
        }
        setCancelable(true);
        TextView extTitle = (TextView) findViewById(R.id.text_title_alert);
        if (extTitle != null)
            extTitle.setText(title);
    }

    @Override
    public void onClick(View v) {
        int id = v.getId();
        if (id == R.id.zalosdk_ok_ctl) {
            dismiss();
            if (listener != null) {
                listener.onOK();
            }
        } else if (id == R.id.zalosdk_cancel_ctl) {
            dismiss();
            if (cancelListener != null) {
                cancelListener.onCancel();
            }
        }
//		could not use switch case since adt-v14
//		switch (v.getId()) {
//		case R.id.ok_ctl:
//			hide();
//			if(listener != null) {
//				listener.onOK();
//			}
//			break;
//		}
    }

    public static interface OnOkListener {
        public void onOK();
    }

    public static interface OnCancelListener {
        public void onCancel();
    }
}
