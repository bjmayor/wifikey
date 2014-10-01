package com.hiwifi.app.views;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.drawable.Drawable;
import android.text.Editable;
import android.text.InputType;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.util.AttributeSet;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnFocusChangeListener;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.RelativeLayout;

import com.hiwifi.hiwifi.R;
import com.hiwifi.utils.DensityUtil;

public class CancelableEditText extends RelativeLayout implements
		OnClickListener, OnFocusChangeListener, TextWatcher {

	EditText mEditText;
	ImageView cancelView;
	Context mContext;

	public CancelableEditText(Context context) {
		super(context);
		mContext = context;
	}

	public CancelableEditText(Context context, AttributeSet attrs) {
		super(context, attrs);
		mContext = context;
		initAttributes(attrs);
		initContent();
	}

	private String attrHint;
	private float attrTextSize;
	private int attrTextColor;
	private int attrLeftDrawable;
	private int attrCancelDrawable;
	private String attrInputType = "text";

	private void initAttributes(AttributeSet attrs) {
		if (null != attrs) {

			final int attrIds[] = new int[] { R.attr.cancelabelEditText_hint,
					R.attr.cancelabelEditText_text_size,
					R.attr.cancelabelEditText_text_color,
					R.attr.cancelabelEditText_left_drawable,
					R.attr.cancelabelEditText_cancel_drawable,
					R.attr.cancelabelEditText_inputType };

			TypedArray array = mContext.obtainStyledAttributes(attrs, attrIds);

			CharSequence t1 = array.getText(0);
			attrTextSize = array.getDimension(1,
					DensityUtil.dip2px(mContext, 14));
			attrTextColor = array.getColor(2, R.color.text_color_black);
			attrLeftDrawable = array.getResourceId(3,
					R.drawable.login_icon_mobile);
			attrCancelDrawable = array.getResourceId(4, R.drawable.icon_clear);
			CharSequence t2 = array.getText(5);

			array.recycle();
			if (t1 != null) {
				attrHint = t1.toString();
			}
			if (t2 != null) {
				attrInputType = t2.toString();
			}
		}
	}

	private void initContent() {
		setGravity(Gravity.CENTER_VERTICAL);
		setBackgroundColor(getResources().getColor(R.color.white));
		mEditText = new EditText(mContext);
		Drawable leftDrawable = getResources().getDrawable(attrLeftDrawable);
		leftDrawable.setBounds(0, 0, leftDrawable.getMinimumWidth(),
				leftDrawable.getMinimumHeight());
		mEditText.setCompoundDrawables(leftDrawable, null, null, null);
		mEditText.setPadding(DensityUtil.dip2px(mContext, 5), 0, 0, 0);
		mEditText.setCompoundDrawablePadding(DensityUtil.dip2px(mContext, 5));
		mEditText.setHint(attrHint);
		mEditText.setTextColor(getResources().getColor(attrTextColor));
		mEditText.setTextSize(TypedValue.COMPLEX_UNIT_PX, attrTextSize);
		mEditText.setHintTextColor(getResources().getColor(
				R.color.eidt_text_hint_color));
		mEditText.setSingleLine();
		mEditText.setBackgroundColor(getResources().getColor(R.color.white));
		LayoutParams textParams = new LayoutParams(LayoutParams.MATCH_PARENT,
				LayoutParams.MATCH_PARENT);
		textParams.addRule(CENTER_VERTICAL);
		mEditText.setLayoutParams(textParams);
		addView(mEditText);
		if (attrInputType.equalsIgnoreCase("textPassword")) {
			mEditText.setInputType(InputType.TYPE_CLASS_TEXT
					| InputType.TYPE_TEXT_VARIATION_PASSWORD);
		} else {
			mEditText.setInputType(InputType.TYPE_CLASS_TEXT);
		}

		cancelView = new ImageView(mContext);
		cancelView.setBackgroundResource(attrCancelDrawable);
		cancelView.setVisibility(View.INVISIBLE);
		LayoutParams params = new LayoutParams(LayoutParams.WRAP_CONTENT,
				LayoutParams.WRAP_CONTENT);
		params.setMargins(0, 0, DensityUtil.dip2px(mContext, 5), 0);
		params.addRule(ALIGN_PARENT_RIGHT);
		params.addRule(CENTER_VERTICAL);
		cancelView.setLayoutParams(params);
		cancelView.setOnClickListener(this);
		addView(cancelView);

		textParams.setMargins(0, 0, DensityUtil.dip2px(mContext, 35), 0);
		mEditText.setOnFocusChangeListener(this);
		mEditText.addTextChangedListener(this);
	}

	@Override
	public void onClick(View v) {
		mEditText.setText("");
	}

	public void setText(String text) {
		mEditText.setText(text);
	}

	public Editable getText() {
		return mEditText.getText();
	}

	public EditText getEditText() {
		return mEditText;
	}

	public void showCancel() {
		cancelView.setVisibility(View.VISIBLE);
	}

	public void hideCancel() {
		cancelView.setVisibility(View.INVISIBLE);
	}

	@Override
	public void onFocusChange(View v, boolean hasFocus) {
		if (hasFocus == true) {
			if (TextUtils.isEmpty(mEditText.getText().toString())) {
				hideCancel();
			} else {
				showCancel();
			}
		} else {
			hideCancel();
		}
	}

	@Override
	public void beforeTextChanged(CharSequence s, int start, int count,
			int after) {

	}

	@Override
	public void onTextChanged(CharSequence s, int start, int before, int count) {
		if (TextUtils.isEmpty(mEditText.getText().toString())) {
			hideCancel();
		} else {
			showCancel();
		}
	}

	@Override
	public void afterTextChanged(Editable s) {
		if (TextUtils.isEmpty(mEditText.getText().toString())) {
			hideCancel();
		} else {
			showCancel();
		}
	}

}
