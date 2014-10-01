package com.hiwifi.app.utils;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnDismissListener;
import android.graphics.Rect;
import android.graphics.drawable.AnimationDrawable;
import android.text.Html;
import android.text.TextUtils;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.hiwifi.app.utils.HintDialog.IHintDialog;
import com.hiwifi.hiwifi.R;
import com.hiwifi.utils.DeviceUtil;
import com.hiwifi.utils.ViewUtil;

public class CommonDialogUtil {

	private static Dialog dialog;
	public static int NONE = 0;
	public static int HARDWORK = 1;
	public static int SORROW = 2;
	public static int SUCCESS = 3;
	ProgressBar a;

	public abstract interface CancelLoading {
		public abstract void cancel();
	}

	public static void ShowCloseWifiDialog(Context context,
			final SureCloseWifi sure) {
		// DismissCloseWifiDialog();
		dialog = new Dialog(context, R.style.BasicRequestDialog);
		dialog.setContentView(R.layout.dialog_close_shared_caution);
		dialog.setCanceledOnTouchOutside(false);
		dialog.setCancelable(true);
		TextView tv_close_share = (TextView) dialog
				.findViewById(R.id.tv_close_share);
		TextView tv_cancel = (TextView) dialog.findViewById(R.id.tv_cancel);
		LinearLayout dialog_ancelbtn = (LinearLayout) dialog
				.findViewById(R.id.dialog_ancelbtn);
		LinearLayout dialog_surebtn = (LinearLayout) dialog
				.findViewById(R.id.dialog_surebtn);
		ImageView iv_icon = (ImageView) dialog.findViewById(R.id.iv_icon);
		AnimationDrawable animationDrawable = (AnimationDrawable) context
				.getResources().getDrawable(R.drawable.anim_sorrow);
		iv_icon.setImageDrawable(animationDrawable);
		dialog_ancelbtn.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				if (sure != null) {
					sure.cancel();
				}
				if (dialog != null) {
					dialog.dismiss();
					dialog = null;
				}
			}
		});
		dialog_surebtn.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				if (sure != null) {
					sure.sureClose();
				}
				if (dialog != null) {
					dialog.dismiss();
					dialog = null;
				}
			}
		});
		dialog.setOnDismissListener(new OnDismissListener() {

			@Override
			public void onDismiss(DialogInterface dialog1) {
				if (sure != null) {
					sure.cancel();
				}
				dialog = null;
			}
		});
		dialog.show();
		animationDrawable.start();
	}

	public static void DismissCloseWifiDialog() {
		if (dialog != null && dialog.isShowing()) {
			try {
				dialog.dismiss();
			} catch (Exception e) {
				e.printStackTrace();
			} finally {
				dialog = null;
			}
		}

	}

	public abstract interface SureCloseWifi {
		public abstract void sureClose();

		public abstract void cancel();
	}

	public static void showCommonDialog(final Activity context,
			final String text, final String sureText, final String cancelText,
			final CommonDialogListener listener) {

		if (context != null && !context.isFinishing()) {
			final HintDialog hd = new HintDialog(context);
			hd.showDialog(R.layout.common_dialog, new IHintDialog() {
				@Override
				public void showWindowDetail(Window window) {
					LinearLayout surebtn = (LinearLayout) window
							.findViewById(R.id.dialog_ancelbtn);
					TextView tv_cancel = (TextView) window
							.findViewById(R.id.tv_cancel);
					TextView tv_sure = (TextView) window
							.findViewById(R.id.tv_close_share);
					if (!TextUtils.isEmpty(sureText)) {
						tv_sure.setText(sureText);
					}
					surebtn.setOnClickListener(new OnClickListener() {

						@Override
						public void onClick(View v) {
							hd.dissmissDialog();
							if (listener != null) {
								listener.positiveAction();
							}
						}
					});
					LinearLayout cancelbtn = (LinearLayout) window
							.findViewById(R.id.dialog_ancelbtn);
					if (!TextUtils.isEmpty(cancelText)) {
						tv_cancel.setText(cancelText);
					}
					cancelbtn.setOnClickListener(new OnClickListener() {

						@Override
						public void onClick(View v) {
							if (listener != null) {
								listener.negativeAction();
							}
							hd.dissmissDialog();
						}
					});
					TextView tv = (TextView) window.findViewById(R.id.text);
					if (!TextUtils.isEmpty(text)) {
						tv.setTextSize(TypedValue.COMPLEX_UNIT_PX,
								ViewUtil.dip2px(context, 15));
						tv.setText(Html.fromHtml(text));
					}
				}

				@Override
				public void onKeyDown(int keyCode, KeyEvent event) {
					hd.dissmissDialog();
				}
			});

		}
	}

	public abstract interface CommonDialogListener {
		public abstract void positiveAction();

		public abstract void negativeAction();
	}

	public static void showCommonDialog(int layout, final Activity context,
			final DialogActionListener listener) {
		if (context != null && !context.isFinishing()) {
			final HintDialog hd = new HintDialog(context);
			hd.showDialog(layout, new IHintDialog() {
				@Override
				public void showWindowDetail(Window window) {
					if (listener != null) {
						listener.dialogAction(window, hd);
					}
				}

				@Override
				public void onKeyDown(int keyCode, KeyEvent event) {
					hd.dissmissDialog();
					if (listener != null) {
						listener.dialogDismiss();
					}
				}
			});

		}
	}

	public abstract interface DialogActionListener {
		public abstract void dialogAction(Window window, HintDialog hd);

		public abstract void dialogDismiss();
	}

	public static void showCryDialog(final Activity context, final String text,
			final String sureText, final String cancelText,
			final CommonDialogListener listener) {
		if (context != null && !context.isFinishing()) {
			final HintDialog hd = new HintDialog(context);
			hd.showDialog(R.layout.dialog_common_cry, new IHintDialog() {
				@Override
				public void showWindowDetail(Window window) {
					Button btn_sure = (Button) window
							.findViewById(R.id.btn_sure);

					if (!TextUtils.isEmpty(sureText)) {
						btn_sure.setText(sureText);
					}
					btn_sure.setOnClickListener(new OnClickListener() {

						@Override
						public void onClick(View v) {
							hd.dissmissDialog();
							if (listener != null) {
								listener.positiveAction();
							}
						}
					});
					Button btn_cancel = (Button) window
							.findViewById(R.id.btn_cancel);
					if (!TextUtils.isEmpty(cancelText)) {
						btn_cancel.setText(cancelText);
					}
					btn_cancel.setOnClickListener(new OnClickListener() {

						@Override
						public void onClick(View v) {
							if (listener != null) {
								listener.negativeAction();
							}
							hd.dissmissDialog();
						}
					});
					TextView tv = (TextView) window
							.findViewById(R.id.tv_text_comtent);
					if (!TextUtils.isEmpty(text)) {
						tv.setTextSize(TypedValue.COMPLEX_UNIT_PX,
								ViewUtil.dip2px(context, 15));
						tv.setText(text);
					}
				}

				@Override
				public void onKeyDown(int keyCode, KeyEvent event) {
					hd.dissmissDialog();
				}
			});

		}
	}

	public static void ShowAnimDialog(Context context, int type, String text,
			final String sureText, final String cancelText,
			final CommonDialogListener listener) {
		dialog = new Dialog(context, R.style.BasicRequestDialog);
		dialog.setContentView(R.layout.dialog_common_cry);
		dialog.setCanceledOnTouchOutside(false);
		dialog.setCancelable(true);

		//调整对话框位置
		Rect frame = new Rect();
		Window dialogWindow = dialog.getWindow();
		dialogWindow.getDecorView().getWindowVisibleDisplayFrame(frame);
		int statusBarHeight = frame.top;
		WindowManager.LayoutParams lp = dialogWindow.getAttributes();
		lp.y = (ViewUtil.avaiableScreenHeight() - statusBarHeight)/4;
//		lp.horizontalMargin = 45;
		dialogWindow.setGravity(Gravity.TOP | Gravity.CENTER_HORIZONTAL);
		dialogWindow.setAttributes(lp);
		
		Button btn_sure = (Button) dialog.findViewById(R.id.btn_sure);
		if (!TextUtils.isEmpty(sureText)) {
			btn_sure.setText(sureText);
		}
		Button btn_cancel = (Button) dialog.findViewById(R.id.btn_cancel);
		if (!TextUtils.isEmpty(cancelText)) {
			btn_cancel.setText(cancelText);
		}
		TextView tv = (TextView) dialog.findViewById(R.id.tv_text_comtent);
		if (!TextUtils.isEmpty(text)) {
			tv.setTextSize(TypedValue.COMPLEX_UNIT_PX,
					ViewUtil.dip2px(context, 12));
			tv.setText(text);
		}
		ImageView icon = (ImageView) dialog.findViewById(R.id.iv_icon_dialog);
		AnimationDrawable animationDrawable = null;
		if (type == CommonDialogUtil.HARDWORK) {
			animationDrawable = (AnimationDrawable) context.getResources()
					.getDrawable(R.drawable.anim_hardwork);
		} else if (type == CommonDialogUtil.SORROW) {
			animationDrawable = (AnimationDrawable) context.getResources()
					.getDrawable(R.drawable.anim_sorrow);
		} else if (type == CommonDialogUtil.SUCCESS) {
			animationDrawable = (AnimationDrawable) context.getResources()
					.getDrawable(R.drawable.anim_success);
		} else {
			icon.setVisibility(View.GONE);
		}
		if (animationDrawable != null) {
			icon.setImageDrawable(animationDrawable);
		}
		btn_cancel.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				if (listener != null) {
					listener.negativeAction();
				}
				if (dialog != null) {
					dialog.dismiss();
					dialog = null;
				}
			}
		});
		btn_sure.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				if (listener != null) {
					listener.positiveAction();
				}
				if (dialog != null) {
					dialog.dismiss();
					dialog = null;
				}
			}
		});
		dialog.setOnDismissListener(new OnDismissListener() {

			@Override
			public void onDismiss(DialogInterface dialog21) {
				if (listener != null) {
					listener.negativeAction();
				}
				dialog = null;
			}
		});
		dialog.show();
		if (animationDrawable != null) {
			animationDrawable.start();
		}
	}

	private static IdissmissDiaglog dissDiaglog;

	private static AlertDialog alertDialog;

	public static void setDissDiaglog(IdissmissDiaglog dissDiag) {
		dissDiaglog = dissDiag;
	}

	/**
	 * 事件回调接口
	 * 
	 */
	public interface IdissmissDiaglog {
		public void dissmiss();
	}

	public static void gloablDialog(Context context, int type, String text,
			final String sureText, final String cancelText,
			final CommonDialogListener listener) {
		AlertDialog.Builder builder = new AlertDialog.Builder(context,
				R.style.BasicRequestDialog);
		alertDialog = builder.create();
		alertDialog.setContentView(R.layout.dialog_common_cry);
		alertDialog.getWindow().setType(
				WindowManager.LayoutParams.TYPE_SYSTEM_ALERT);
		alertDialog.setCanceledOnTouchOutside(false);
		// alertDialog.setCancelable(true);
		// Button btn_sure = (Button) alertDialog.findViewById(R.id.btn_sure);
		// if (!TextUtils.isEmpty(sureText)) {
		// btn_sure.setText(sureText);
		// }
		// Button btn_cancel = (Button)
		// alertDialog.findViewById(R.id.btn_cancel);
		// if (!TextUtils.isEmpty(cancelText)) {
		// btn_cancel.setText(cancelText);
		// }
		// TextView tv = (TextView)
		// alertDialog.findViewById(R.id.tv_text_comtent);
		// if (!TextUtils.isEmpty(text)) {
		// tv.setTextSize(TypedValue.COMPLEX_UNIT_PX,
		// ViewUtil.dip2px(context, 15));
		// tv.setText(text);
		// }
		// ImageView icon = (ImageView)
		// alertDialog.findViewById(R.id.iv_icon_dialog);
		// AnimationDrawable animationDrawable = null;
		// if (type == CommonDialogUtil.HARDWORK) {
		// animationDrawable = (AnimationDrawable) context.getResources()
		// .getDrawable(R.drawable.anim_hardwork);
		// } else if (type == CommonDialogUtil.SORROW) {
		// animationDrawable = (AnimationDrawable) context.getResources()
		// .getDrawable(R.drawable.anim_sorrow);
		// } else if (type == CommonDialogUtil.SUCCESS) {
		// animationDrawable = (AnimationDrawable) context.getResources()
		// .getDrawable(R.drawable.anim_success);
		// }else {
		// icon.setVisibility(View.GONE);
		// }
		// if (animationDrawable != null) {
		// icon.setImageDrawable(animationDrawable);
		// }
		// btn_cancel.setOnClickListener(new OnClickListener() {
		//
		// @Override
		// public void onClick(View v) {
		// if (listener != null) {
		// listener.negativeAction();
		// }
		// if (alertDialog != null) {
		// alertDialog.dismiss();
		// alertDialog = null;
		// }
		// }
		// });
		// btn_sure.setOnClickListener(new OnClickListener() {
		//
		// @Override
		// public void onClick(View v) {
		// if (listener != null) {
		// listener.positiveAction();
		// }
		// if (alertDialog != null) {
		// alertDialog.dismiss();
		// alertDialog = null;
		// }
		// }
		// });
		// alertDialog.setOnDismissListener(new OnDismissListener() {
		//
		// @Override
		// public void onDismiss(DialogInterface dialog21) {
		// if (listener != null) {
		// listener.negativeAction();
		// }
		// alertDialog = null;
		// }
		// });
		alertDialog.show();
		// if (animationDrawable != null) {
		// animationDrawable.start();
		// }
	}

}
