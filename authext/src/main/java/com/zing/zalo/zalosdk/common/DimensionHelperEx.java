package com.zing.zalo.zalosdk.common;

import android.content.Context;
import android.content.res.Resources;
import android.util.TypedValue;

public class DimensionHelperEx {
    public static float getPixelPadding(Context owner) {
        Resources r = owner.getResources();
        float result = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 1, r.getDisplayMetrics());
        if (result < 1) return 1;

        return result;

    }
}
