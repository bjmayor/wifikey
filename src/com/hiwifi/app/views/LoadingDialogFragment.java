/**
 * LoadingDialogFragment.java
 * com.hiwifi.app.views
 * hiwifi_lsp
 * shunping.liu create at 20142014年7月27日下午5:38:44
 */
package com.hiwifi.app.views;

import com.hiwifi.hiwifi.R;
import com.hiwifi.utils.ViewUtil;

import android.annotation.SuppressLint;
import android.app.Dialog;
import android.app.DialogFragment;
import android.app.FragmentManager;
import android.graphics.Rect;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.Gravity;
import android.view.Window;
import android.view.WindowManager;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.ImageView;
import android.widget.TextView;

/**
 * @author shunping.liu@hiwifi.tw
 * 
 */
@SuppressLint("ValidFragment")
public class LoadingDialogFragment extends DialogFragment {
	private static final String KMESSAGE = "key_message";

	public static LoadingDialogFragment newInstance(String message) {
		LoadingDialogFragment fragment = new LoadingDialogFragment();
		Bundle args = new Bundle();
		if (!TextUtils.isEmpty(message)) {
			args.putString(KMESSAGE, message);
		}
		fragment.setArguments(args);
		return fragment;
	}

	@Override
	public Dialog onCreateDialog(Bundle savedInstanceState) {
		Dialog dialog = new Dialog(getActivity(), R.style.BasicRequestDialog);
		dialog.setContentView(R.layout.dialog_loading);
		dialog.setCanceledOnTouchOutside(false);
		dialog.setCancelable(true);
		
		// 调整对话框位置
		Rect frame = new Rect();
		Window dialogWindow = dialog.getWindow();
		dialogWindow.getDecorView().getWindowVisibleDisplayFrame(frame);
		int statusBarHeight = frame.top;
		WindowManager.LayoutParams lp = dialogWindow.getAttributes();
		lp.y = (ViewUtil.avaiableScreenHeight() - statusBarHeight) / 4;
		// lp.horizontalMargin = 45;
		dialogWindow.setGravity(Gravity.TOP | Gravity.CENTER_HORIZONTAL);
		dialogWindow.setAttributes(lp);

		ImageView loadingview = (ImageView) dialog
				.findViewById(R.id.iv_loading);
		TextView statusTextView = (TextView) dialog
				.findViewById(R.id.tv_laoding_status);
		String message = getArguments().getString(KMESSAGE);

		if (!TextUtils.isEmpty(message)) {
			statusTextView.setText(message);
		} else {
			statusTextView.setText("");
		}
		Animation rotateAnimation = AnimationUtils.loadAnimation(getActivity(),
				R.anim.rotate_anim);
		dialog.setCancelable(true);
		dialog.setCanceledOnTouchOutside(false);
		dialog.show();
		loadingview.startAnimation(rotateAnimation);
		return dialog;
	}
}
