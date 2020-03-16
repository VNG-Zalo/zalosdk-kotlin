package com.zing.zalo.zalosdk.oauth;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.text.InputType;
import android.util.AttributeSet;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;

public class ZingMeBaseLoginView extends RelativeLayout {

    protected OAuthCompleteListener listener;
    private AuthenticateExtention authenticateExtention;
    private Button submit;
    private EditText zingId, pass;
    private LinearLayout inputContainer;

    @SuppressWarnings("deprecation")
    public ZingMeBaseLoginView(final Context context, AttributeSet attrs) {
        super(context, attrs);

        int color = Color.BLACK;
        float textsize = 12;
        int textStyle = 0;
        Drawable id_background = null, pass_background = null, btn_background = null;
        String text = null, zingIDhint = null, passhint = null;
        TypedArray typedArray = getContext().obtainStyledAttributes(attrs, R.styleable.ZingMeLoginView);
        int n = typedArray.getIndexCount();
        for (int i = 0; i < n; i++) {
            int attr = typedArray.getIndex(i);
            if (attr == R.styleable.ZingMeLoginView_android_textColor) {
                color = typedArray.getColor(R.styleable.ZingMeLoginView_android_textColor, Color.BLACK);
            } else if (attr == R.styleable.ZingMeLoginView_android_textSize) {
                textsize = typedArray.getDimension(R.styleable.ZingMeLoginView_android_textSize, 12);
            } else if (attr == R.styleable.ZingMeLoginView_android_textStyle) {
                textStyle = typedArray.getInt(R.styleable.ZingMeLoginView_android_textStyle, 0);
            } else if (attr == R.styleable.ZingMeLoginView_inputBackground) {
                id_background = typedArray.getDrawable(R.styleable.ZingMeLoginView_inputBackground);
                pass_background = typedArray.getDrawable(R.styleable.ZingMeLoginView_inputBackground);
            } else if (attr == R.styleable.ZingMeLoginView_buttonBackground) {
                btn_background = typedArray.getDrawable(R.styleable.ZingMeLoginView_buttonBackground);
            } else if (attr == R.styleable.ZingMeLoginView_buttonText) {
                text = typedArray.getString(R.styleable.ZingMeLoginView_buttonText);
            } else if (attr == R.styleable.ZingMeLoginView_passwordHint) {
                passhint = typedArray.getString(R.styleable.ZingMeLoginView_passwordHint);
            } else if (attr == R.styleable.ZingMeLoginView_usernameHint) {
                zingIDhint = typedArray.getString(R.styleable.ZingMeLoginView_usernameHint);
            }
        }
        LayoutParams params = new LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT);
        params.setMargins(0, 0, 0, 0);
        inputContainer = new LinearLayout(context);
        inputContainer.setOrientation(LinearLayout.VERTICAL);
        inputContainer.setLayoutParams(params);
        inputContainer.setId(Integer.MAX_VALUE);

        //-- Input Zing ID
        LayoutParams params1 = new LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT);
        params1.setMargins(0, 0, 0, 10);
        zingId = new EditText(context);//(EditText) view.findViewById(R.id.input_zing_id);
        zingId.setLayoutParams(params1);
        zingId.setTextColor(color);
        if (zingIDhint != null)
            zingId.setHint(zingIDhint);
        else
            zingId.setHint(getResources().getString(R.string.hint_zing_id));
        zingId.setTextSize(getOriginalDimension(textsize));
        zingId.setTypeface(null, textStyle);
        zingId.setSingleLine(true);
        if (id_background != null)
            zingId.setBackgroundDrawable(id_background);
//		else
//			zingId.setBackgroundDrawable(getResources().getDrawable(R.drawable.textfield_background));

        LayoutParams params2 = new LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT);
        params2.setMargins(0, 0, 0, 10);
        //-- Input password
        pass = new EditText(context);// (EditText) view.findViewById(R.id.input_pass);
        pass.setLayoutParams(params2);
        pass.setTextColor(color);
        pass.setSingleLine(true);
        pass.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_PASSWORD);
        if (passhint != null)
            pass.setHint(passhint);
        else
            pass.setHint(getResources().getString(R.string.hint_password));
        pass.setTextSize(getOriginalDimension(textsize));
        if (pass_background != null)
            pass.setBackgroundDrawable(pass_background);
//		else
//			pass.setBackgroundDrawable(getResources().getDrawable(R.drawable.textfield_background));

        //-- Button login via zing me
        LayoutParams params3 = new LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT);
        params3.setMargins(0, 0, 0, 10);
        params3.addRule(RelativeLayout.BELOW, Integer.MAX_VALUE);
        submit = new Button(context);//(Button) view.findViewById(R.id.btn_login_via_zingme);
        submit.setId(Integer.MAX_VALUE - 1);
        if (text != null && text.trim().length() > 0)
            submit.setText(text);
        else
            submit.setText(getResources().getString(R.string.btn_login));

        if (btn_background != null)
            submit.setBackgroundDrawable(btn_background);
//		else
//			submit.setBackgroundDrawable(getResources().getDrawable(R.drawable.login_bg));
        submit.setLayoutParams(params3);

        submit.setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(View v) {
                String id = zingId.getText().toString().trim();
                String p = pass.getText().toString().trim();
                if (authenticateExtention == null) {
                    authenticateExtention = new AuthenticateExtention(context);
                }
                authenticateExtention.authenticateWithZingMe(getContext(), id, p, listener);
            }
        });
        inputContainer.addView(zingId);
        inputContainer.addView(pass);
        addView(inputContainer);
        addView(submit);

        typedArray.recycle();
    }

    public Button getLoginZingMe() {
        return submit;
    }

    public EditText getZingId() {
        return zingId;
    }

    public EditText getPass() {
        return pass;
    }

    public void setOAuthCompleteListener(OAuthCompleteListener listener) {
        this.listener = listener;
    }

    private float getOriginalDimension(float dimension) {
        float scaleRatio = getResources().getDisplayMetrics().density;
        return (dimension - 0.5f) / scaleRatio;
    }
}