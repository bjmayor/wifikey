package com.hiwifi.app.views;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.res.ColorStateList;
import android.view.Gravity;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.FrameLayout.LayoutParams;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.hiwifi.app.views.CustomDialog.AlertParams.ButtonType;
import com.hiwifi.constant.ConfigConstant;
import com.hiwifi.hiwifi.R;
import com.hiwifi.model.log.HWFLog;
import com.hiwifi.utils.ResUtil;
import com.hiwifi.utils.ViewUtil;

/**
 * 自定义dialog
 * 
 * @author
 * 
 */
public class CustomDialog extends Dialog {

	private static String TAG = CustomDialog.class.getName();

	public static final int OKANDCANCLE = 0;
	public static final int OK = 1;

	private TextView textView;
	private TextView titleView;
	private LinearLayout btnLayout;
	private LinearLayout viewLayout;

	public CustomDialog(Context context) {
		super(context, R.style.dialog);
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		this.init();
	}
	
	public CustomDialog(Context context,View view) {
		super(context, R.style.dialog);
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		this.setContentView(R.layout.dialog_custom);
		this.setCancelable(true);
		btnLayout = (LinearLayout) this.findViewById(R.id.btnLayout);
		viewLayout = (LinearLayout) this.findViewById(R.id.viewLayout);
		viewLayout.removeAllViews();
		LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
				LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT);
		viewLayout.addView(view, params);
	}

	private void init() {
		this.setContentView(R.layout.dialog_custom);
		this.setCancelable(true);
		btnLayout = (LinearLayout) this.findViewById(R.id.btnLayout);
	}

	public void setMsg(int resId) {
		textView = (TextView) this.findViewById(R.id.dialogtext);
		if (textView != null) {
			textView.setText(resId);
		}
	}

	public void setMsg(CharSequence str) {
		textView = (TextView) this.findViewById(R.id.dialogtext);
		if (textView != null) {
			textView.setText(str);
		}
	}

	public void setView(View view) {
		viewLayout = (LinearLayout) this.findViewById(R.id.viewLayout);
		viewLayout.removeAllViewsInLayout();
		LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
				LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT);
		viewLayout.addView(view, params);
	}

	public void setTitle(CharSequence title) {
		titleView = (TextView) this.findViewById(R.id.dialogtext_title);
		titleView.setVisibility(View.VISIBLE);
		titleView.setText(title);
	}

	public void setTitle(int titleId) {
		titleView = (TextView) this.findViewById(R.id.dialogtext_title);
		titleView.setVisibility(View.VISIBLE);
		titleView.setText(titleId);
	}

	private int btnCount;

	private Button addButton(int msgId, final OnClickListener linster) {
		final CharSequence str = this.getContext().getResources()
				.getString(msgId);
		return addButton(str, linster, ButtonType.ButtonTypeNeutral);
	}

	private ColorStateList createColorStateList(int normal, int pressed,
			int focused, int unable) {
		int[] colors = new int[] { pressed, focused, normal, focused, unable,
				normal };
		int[][] states = new int[6][];
		states[0] = new int[] { android.R.attr.state_pressed,
				android.R.attr.state_enabled };
		states[1] = new int[] { android.R.attr.state_enabled,
				android.R.attr.state_focused };
		states[2] = new int[] { android.R.attr.state_enabled };
		states[3] = new int[] { android.R.attr.state_focused };
		states[4] = new int[] { android.R.attr.state_window_focused };
		states[5] = new int[] {};
		ColorStateList colorList = new ColorStateList(states, colors);
		return colorList;
	}

	/**
	 * 创建button
	 * 
	 * @return
	 */
	@SuppressLint("NewApi")
	private Button addButton(final CharSequence msgId,
			final OnClickListener linster, ButtonType type) {
		Button btn = new Button(this.getContext());
		btn.setGravity(Gravity.CENTER);
		btn.setTextSize(16);
		btn.setText(msgId);
		btn.setTextColor(createColorStateList(
				ResUtil.getColorById(R.color.text_color_black),
				ResUtil.getColorById(R.color.white),
				ResUtil.getColorById(R.color.white),
				ResUtil.getColorById(R.color.white)));
		if (type == ButtonType.ButtonTypePositive) {
			btn.setBackgroundResource(R.drawable.selector_right_btn_bg);
		} else if (type == ButtonType.ButtonTypeNegative) {
			btn.setBackgroundResource(R.drawable.selector_left_btn_bg);
		} else {
			btn.setBackgroundResource(R.drawable.manual_rl_click);
		}

		// if (btnCount > 0) {
		// ImageView imageView = new ImageView(this.getContext());
		// imageView.setBackgroundResource(R.drawable.skin_vertical_divider);
		// LinearLayout.LayoutParams params = new
		// LinearLayout.LayoutParams(LayoutParams.WRAP_CONTENT,
		// LayoutParams.MATCH_PARENT);
		// params.setMargins(0, 10, 0, 10);
		// imageView.setLayoutParams(params);
		// btnLayout.addView(imageView);
		// }

		LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
				LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT);
		params.weight = 1;
		btnLayout.addView(btn, params);
		final int index = btnCount;
		btn.setOnClickListener(new android.view.View.OnClickListener() {

			@Override
			public void onClick(View v) {
				linster.onClick(CustomDialog.this, index);
				CustomDialog.this.dismiss();
			}
		});
		btnCount++;

		return btn;
	}

	public static class AlertParams {
		public AlertParams(Context context) {
			this.mContext = context;
		}

		public static enum ButtonType {
			ButtonTypePositive, ButtonTypeNegative, ButtonTypeNeutral
		}

		private Context mContext;
		private DialogInterface mDialogInterface;
		private CharSequence mMessage;
		private CharSequence mPositiveButtonText;
		private OnClickListener mPositiveButtonListener;
		private CharSequence mTitle = null;
		private CharSequence mNegativeButtonText;
		private OnClickListener mNegativeButtonListener;
		private boolean mCancelable = true;
		private boolean mCanceledOnTouchOutside = false;

		private CharSequence mNeutralButtonText;
		private OnClickListener mNeutralButtonListener;

		private OnCancelListener mOnCancelListener;
		private View mView;
	}

	public static class Builder {
		private final AlertParams P;
		private CustomDialog dialog;

		public Builder setTitle(CharSequence title) {
			P.mTitle = title;
			return this;
		}

		public Builder setTitle(int titleId) {
			P.mTitle = P.mContext.getText(titleId);
			return this;
		}

		/**
		 * Constructor using a context for this builder and the
		 * {@link AlertDialog} it creates.
		 */
		public Builder(Context context) {
			P = new AlertParams(context);
		}

		/**
		 * Set the message to display using the given resource id.
		 * 
		 * @return This Builder object to allow for chaining of calls to set
		 *         methods
		 */
		public Builder setMessage(int messageId) {
			P.mMessage = P.mContext.getText(messageId);
			return this;
		}

		/**
		 * Set the message to display.
		 * 
		 * @return This Builder object to allow for chaining of calls to set
		 *         methods
		 */
		public Builder setMessage(CharSequence message) {
			P.mMessage = message;
			return this;
		}

		/**
		 * Set a listener to be invoked when the positive button of the dialog
		 * is pressed.
		 * 
		 * @param textId
		 *            The resource id of the text to display in the positive
		 *            button
		 * @param listener
		 *            The {@link DialogInterface.OnClickListener} to use.
		 * 
		 * @return This Builder object to allow for chaining of calls to set
		 *         methods
		 */
		public Builder setPositiveButton(int textId,
				final OnClickListener listener) {
			P.mPositiveButtonText = P.mContext.getText(textId);
			P.mPositiveButtonListener = listener;
			return this;
		}

		/**
		 * Set a listener to be invoked when the positive button of the dialog
		 * is pressed.
		 * 
		 * @param text
		 *            The text to display in the positive button
		 * @param listener
		 *            The {@link DialogInterface.OnClickListener} to use.
		 * 
		 * @return This Builder object to allow for chaining of calls to set
		 *         methods
		 */
		public Builder setPositiveButton(CharSequence text,
				final OnClickListener listener) {
			P.mPositiveButtonText = text;
			P.mPositiveButtonListener = listener;
			return this;
		}

		/**
		 * Set a listener to be invoked when the negative button of the dialog
		 * is pressed.
		 * 
		 * @param textId
		 *            The resource id of the text to display in the negative
		 *            button
		 * @param listener
		 *            The {@link DialogInterface.OnClickListener} to use.
		 * 
		 * @return This Builder object to allow for chaining of calls to set
		 *         methods
		 */
		public Builder setNegativeButton(int textId,
				final OnClickListener listener) {
			P.mNegativeButtonText = P.mContext.getText(textId);
			P.mNegativeButtonListener = listener;
			return this;
		}

		/**
		 * Set a listener to be invoked when the negative button of the dialog
		 * is pressed.
		 * 
		 * @param text
		 *            The text to display in the negative button
		 * @param listener
		 *            The {@link DialogInterface.OnClickListener} to use.
		 * 
		 * @return This Builder object to allow for chaining of calls to set
		 *         methods
		 */
		public Builder setNegativeButton(CharSequence text,
				final OnClickListener listener) {
			P.mNegativeButtonText = text;
			P.mNegativeButtonListener = listener;
			return this;
		}

		/**
		 * Set a listener to be invoked when the neutral button of the dialog is
		 * pressed.
		 * 
		 * @param textId
		 *            The resource id of the text to display in the neutral
		 *            button
		 * @param listener
		 *            The {@link DialogInterface.OnClickListener} to use.
		 * 
		 * @return This Builder object to allow for chaining of calls to set
		 *         methods
		 */
		public Builder setNeutralButton(int textId,
				final OnClickListener listener) {
			P.mNeutralButtonText = P.mContext.getText(textId);
			P.mNeutralButtonListener = listener;
			return this;
		}

		/**
		 * Set a listener to be invoked when the neutral button of the dialog is
		 * pressed.
		 * 
		 * @param text
		 *            The text to display in the neutral button
		 * @param listener
		 *            The {@link DialogInterface.OnClickListener} to use.
		 * 
		 * @return This Builder object to allow for chaining of calls to set
		 *         methods
		 */
		public Builder setNeutralButton(CharSequence text,
				final OnClickListener listener) {
			P.mNeutralButtonText = text;
			P.mNeutralButtonListener = listener;
			return this;
		}

		/**
		 * Sets whether the dialog is cancelable or not default is true.
		 * 
		 * @return This Builder object to allow for chaining of calls to set
		 *         methods
		 */
		public Builder setCancelable(boolean cancelable) {
			P.mCancelable = cancelable;
			return this;
		}

		/**
		 * Sets whether this dialog is canceled when touched outside the
		 * window's bounds. If setting to true, the dialog is set to be
		 * cancelable if not already set.
		 * 
		 * @param cancel
		 *            Whether the dialog should be canceled when touched outside
		 *            the window.
		 */
		public Builder setCanceledOnTouchOutside(boolean canceledOnTouchOutside) {
			P.mCanceledOnTouchOutside = canceledOnTouchOutside;
			return this;
		}

		/**
		 * Sets the callback that will be called if the dialog is canceled.
		 * 
		 * @see #setCancelable(boolean)
		 * 
		 * @return This Builder object to allow for chaining of calls to set
		 *         methods
		 */
		public Builder setOnCancelListener(OnCancelListener onCancelListener) {
			P.mOnCancelListener = onCancelListener;
			return this;
		}

		public Builder setView(View view) {
			P.mView = view;
			return this;
		}

		/**
		 * Creates a {@link AlertDialog} with the arguments supplied to this
		 * builder. It does not {@link Dialog#show()} the dialog. This allows
		 * the user to do any extra processing before displaying the dialog. Use
		 * {@link #show()} if you don't have any other processing to do and want
		 * this to be created and displayed.
		 */
		public CustomDialog create() {
			final CustomDialog dialog = new CustomDialog(P.mContext);
			dialog.setCancelable(P.mCancelable);
			dialog.setCanceledOnTouchOutside(P.mCanceledOnTouchOutside);
			dialog.setOnCancelListener(P.mOnCancelListener);
			if (P.mTitle != null) {
				dialog.setTitle(P.mTitle);
			}

			HWFLog.d(TAG, "===============" + P.mPositiveButtonListener + ","
					+ P.mNegativeButtonListener + ","
					+ P.mNeutralButtonListener);

			if (P.mNegativeButtonListener != null) {
				dialog.addButton(P.mNegativeButtonText,
						P.mNegativeButtonListener,
						ButtonType.ButtonTypeNegative);
			}

			if (P.mPositiveButtonListener != null) {
				dialog.addButton(P.mPositiveButtonText,
						P.mPositiveButtonListener,
						ButtonType.ButtonTypePositive);
			}

			if (P.mNeutralButtonListener != null) {
				dialog.addButton(P.mNeutralButtonText,
						P.mNeutralButtonListener, ButtonType.ButtonTypeNeutral);
			}

			if (P.mPositiveButtonListener == null
					&& P.mNegativeButtonListener == null
					&& P.mNeutralButtonListener == null) {
				dialog.addButton(R.string.btn_ok, new OnClickListener() {

					@Override
					public void onClick(DialogInterface dialog, int which) {
						dialog.dismiss();
					}
				});
			}

			if (P.mView != null) {
				dialog.setView(P.mView);
			}
			if (P.mTitle != null) {

			}

			dialog.setMsg(P.mMessage);
			dialog.getWindow().setType(WindowManager.LayoutParams.TYPE_SYSTEM_ALERT);
			dialog.getWindow()
					.setLayout(
							(int) (ViewUtil.getScreenWidth() * ConfigConstant.DIALOG_KINDNOTE_WIDTH),
							android.view.WindowManager.LayoutParams.WRAP_CONTENT);

			return dialog;
		}

		/**
		 * Creates a {@link AlertDialog} with the arguments supplied to this
		 * builder and {@link Dialog#show()}'s the dialog.
		 */
		public CustomDialog show() {
			if(dialog == null){
				dialog = create();
				dialog.show();
			}else {
				if(!dialog.isShowing()){
					dialog.show();
				}
			}

			dialog.getWindow()
					.setLayout(
							(int) (ViewUtil.getScreenWidth() * ConfigConstant.DIALOG_KINDNOTE_WIDTH),
							android.view.WindowManager.LayoutParams.WRAP_CONTENT);
			return dialog;
		}

	}

}
