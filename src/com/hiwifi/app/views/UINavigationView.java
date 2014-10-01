package com.hiwifi.app.views;

import junit.runner.Version;
import android.annotation.TargetApi;
import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.text.TextUtils;
import android.text.TextUtils.TruncateAt;
import android.util.AttributeSet;
import android.util.Log;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.hiwifi.hiwifi.R;
import com.hiwifi.model.log.HWFLog;
import com.hiwifi.utils.ResUtil;

public class UINavigationView extends RelativeLayout {

	private Button leftButton;
	private Button rightButton;
	private TextView titleTextView;

	private String leftText = null;
	private String titleText = null;
	private String rightText = null;
	private int leftDrawable = 0;
	private int rightDrawable = 0;

	public UINavigationView(Context context) {
		super(context);
		initContent();
	}

	public UINavigationView(Context context, AttributeSet attrs) {
		super(context, attrs);
		initAttributes(attrs);
		initContent();
	}

	private void initAttributes(AttributeSet attributeSet) {

		if (null != attributeSet) {

			final int attrIds[] = new int[] { R.attr.btn_leftText,
					R.attr.btn_rightText, R.attr.tv_title,
					R.attr.left_drawable, R.attr.right_drawable };

			Context context = getContext();

			TypedArray array = context.obtainStyledAttributes(attributeSet,
					attrIds);

			CharSequence t1 = array.getText(0);
			CharSequence t2 = array.getText(1);
			CharSequence t3 = array.getText(2);
			leftDrawable = array.getResourceId(3, 0);
			rightDrawable = array.getResourceId(4, 0);

			array.recycle();
			if (t1 != null) {
				leftText = t1.toString();
			}
			if (t2 != null) {
				rightText = t2.toString();
			}
			if (t3 != null) {
				titleText = t3.toString();
			}
		}

	}

	@TargetApi(Build.VERSION_CODES.JELLY_BEAN)
	private void initContent() {

		Log.i("coder", "-----initContent----");
		// 设置水平方向
		setGravity(Gravity.CENTER_VERTICAL);

		Context context = getContext();

		leftButton = new Button(context);
		leftButton.setVisibility(View.INVISIBLE);// 设置设置不可见
		leftButton.setGravity(Gravity.CENTER_VERTICAL);
		leftButton.setMinHeight(0);
		leftButton.setMinWidth(0);
		leftButton.setPadding(0, 0, 0, 0);

		LayoutParams btnLeftParams = new LayoutParams(
				LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT);
		 btnLeftParams.setMargins(10, 0, 0, 0);
		 btnLeftParams.addRule(RelativeLayout.CENTER_VERTICAL);
		if (null != leftText && !TextUtils.isEmpty(leftText)) {
			 btnLeftParams.setMargins(25, 0, 0, 0);
			leftButton.setLayoutParams(btnLeftParams);
			leftButton.setText(leftText);
			leftButton.setTextColor(Color.WHITE);// 字体颜色
			leftButton.setTextSize(TypedValue.COMPLEX_UNIT_PX, getResources()
					.getDimension(R.dimen.nav_left_title_size));
			leftButton.setVisibility(View.VISIBLE);
			leftButton.setBackgroundResource(R.color.transparent);// 设置背景
		} else {
			if (leftDrawable != 0) {
				Drawable drawable = getResources().getDrawable(leftDrawable);
				if(Build.VERSION.SDK_INT < android.os.Build.VERSION_CODES.JELLY_BEAN) {
					leftButton.setBackgroundDrawable(drawable);
				} else {
					leftButton.setBackground(drawable);
				}
				leftButton.setVisibility(View.VISIBLE);
				btnLeftParams.width = drawable.getMinimumWidth();
				btnLeftParams.height = drawable.getMinimumHeight();
			}
		}
		leftButton.setLayoutParams(btnLeftParams);
		// 添加这个按钮
		addView(leftButton);

		//
		titleTextView = new TextView(context);
		LayoutParams centerParam = new LayoutParams(LayoutParams.MATCH_PARENT,
				LayoutParams.MATCH_PARENT);

		centerParam.addRule(RelativeLayout.CENTER_IN_PARENT);
		centerParam.setMargins(150, 0, 150, 0);
		titleTextView.setLayoutParams(centerParam);
		titleTextView.setSingleLine();
		titleTextView.setEllipsize(TruncateAt.END);
		titleTextView.setTextColor(Color.WHITE);
		titleTextView.setTextSize(TypedValue.COMPLEX_UNIT_PX, getResources()
				.getDimension(R.dimen.nav_title_size));
		if (null != titleText) {
			titleTextView.setText(titleText);
		}

		titleTextView.setGravity(Gravity.CENTER);

		// 添加这个标题
		addView(titleTextView);

		rightButton = new Button(context);
		rightButton.setVisibility(View.INVISIBLE);// 设置设置不可见
		rightButton.setTextColor(Color.WHITE);// 字体颜色
		rightButton.setTextSize(15);
		rightButton.setMinHeight(0);
		rightButton.setMinWidth(0);

		if (rightDrawable != 0) {
			rightButton.setBackgroundResource(rightDrawable);
			rightButton.setVisibility(View.VISIBLE);
		} else {
			rightButton.setBackgroundResource(R.color.transparent);// 设置背景
		}
		LayoutParams btnRightParams = new LayoutParams(
				LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT);
		btnRightParams.setMargins(0, 0, 10, 0);
		btnRightParams.addRule(RelativeLayout.ALIGN_PARENT_RIGHT);
		btnRightParams.addRule(RelativeLayout.CENTER_VERTICAL);
		rightButton.setLayoutParams(btnRightParams);

		if (null != rightText && !TextUtils.isEmpty(rightText)) {
			rightButton.setText(rightText);
			rightButton.setTextSize(TypedValue.COMPLEX_UNIT_PX, getResources()
					.getDimension(R.dimen.nav_right_title_size));
			rightButton.setVisibility(View.VISIBLE);
		} else {
			if (rightDrawable != 0) {
				Drawable drawableRight = getResources().getDrawable(
						rightDrawable);
				if(Build.VERSION.SDK_INT < android.os.Build.VERSION_CODES.JELLY_BEAN) {
					rightButton.setBackgroundDrawable(drawableRight);
				} else {
					rightButton.setBackground(drawableRight);
				}
				rightButton.setVisibility(View.VISIBLE);
				// rightButton.setLayoutParams(new
				// LayoutParams(drawable1.getMinimumWidth()
				// , drawable1.getMinimumHeight()));
				btnRightParams.width = drawableRight.getMinimumWidth();
				btnRightParams.height = drawableRight.getMinimumHeight();
			}
			// rightButton.setLayoutParams(new LayoutParams(
			// LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT));

		}
		btnRightParams.addRule(RelativeLayout.ALIGN_PARENT_RIGHT);
		btnRightParams.addRule(RelativeLayout.CENTER_VERTICAL);
		rightButton.setLayoutParams(btnRightParams);

		// 添加这个按钮
		addView(rightButton);

	}

	public Button getLeftButton() {
		return leftButton;
	}

	public Button getRightButton() {
		return rightButton;
	}

	public TextView getTitleTextView() {
		return titleTextView;
	}

	public void setTitle(String title) {
		if (title != null) {
			getTitleTextView().setText(title);
		}
	}

	public String getRightText() {
		return rightText;
	}

	public void setRightText(String rightText) {
		this.rightText = rightText;
	}

}
