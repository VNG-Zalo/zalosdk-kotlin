package com.zing.zalo.zalosdk.java.payment.direct;


import android.annotation.TargetApi;
import android.content.Context;
import android.graphics.Point;
import android.os.Build;
import android.util.AttributeSet;
import android.view.Display;
import android.view.WindowManager;
import android.widget.RelativeLayout;

import com.zing.zalo.zalosdk.kotlin.core.helper.DeviceInfo;

public class DynamicLayout extends RelativeLayout {

    int w, mBoundedWidth = 900;

    public DynamicLayout(Context context) {
        super(context);
    }

    public DynamicLayout(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    @TargetApi(Build.VERSION_CODES.HONEYCOMB_MR2)
    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {

        if (DeviceInfo.isTablet(getContext())) {
            int width, height;
            WindowManager wm = (WindowManager) getContext().getSystemService(Context.WINDOW_SERVICE);
            Display display = wm.getDefaultDisplay();
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB_MR2) {
                Point size = new Point();
                display.getSize(size);
                width = size.x;
                height = size.y;
            } else {
                width = display.getWidth();
                height = display.getHeight();
            }

            if (MeasureSpec.getSize(widthMeasureSpec) < MeasureSpec.getSize(heightMeasureSpec)) {
                mBoundedWidth = (int) (width * 0.8);
            } else {
                mBoundedWidth = (int) (height * 0.8);
            }

            if (mBoundedWidth < MeasureSpec.getSize(widthMeasureSpec)) {
                int measureMode = MeasureSpec.getMode(widthMeasureSpec);
                w = MeasureSpec.makeMeasureSpec(mBoundedWidth, measureMode);
            }
        } else {
            if (MeasureSpec.getSize(widthMeasureSpec) < MeasureSpec.getSize(heightMeasureSpec)) {
                w = widthMeasureSpec;
//				android.util.Log.i("debuglog", "DynamicLayout.java-----AAAAAAAAAAAAAA");
            } else {
                w = heightMeasureSpec + 50;
//				android.util.Log.i("debuglog", "DynamicLayout.java-----BBBBBBBBBBBBB");
            }
        }


        super.onMeasure(w, heightMeasureSpec);
    }
}
