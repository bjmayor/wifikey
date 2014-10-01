package com.hiwifi.activity.base;

import android.annotation.TargetApi;
import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.support.v7.app.ActionBar;
import android.support.v7.app.ActionBarActivity;
import android.util.DisplayMetrics;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.ImageView;
import android.widget.TextView;
import cn.sharesdk.framework.ShareSDK;

import com.hiwifi.activity.user.UserLoginActivity;
import com.hiwifi.app.views.LoadingDialogFragment;
import com.hiwifi.model.EventDispatcher;
import com.hiwifi.model.User;
import com.hiwifi.model.log.HWFLog;
import com.hiwifi.model.log.LogUtil;
import com.hiwifi.utils.ApplicationUtil;
import com.hiwifi.utils.ViewUtil;
import com.umeng.analytics.MobclickAgent;

public abstract class BaseActivity extends ActionBarActivity implements
		OnClickListener {

	protected final String TAG = "BaseActivity";
	protected ImageView leftBtn, rightBtn;
	protected TextView commontitle, right_text;
	public static int screenWidth;
	public static int screenHeight;
	protected Handler mHandler = new Handler();
	public DisplayMetrics displayMetrics;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		displayMetrics = new DisplayMetrics();
		getWindowManager().getDefaultDisplay().getMetrics(displayMetrics);
		screenWidth = displayMetrics.widthPixels;
		screenHeight = displayMetrics.heightPixels;
		LogUtil.d("屏幕size：", screenWidth + "*" + screenHeight);
		if (ViewUtil.hasSmartBar()) {
			LogUtil.d("nott->", "smart");
			screenHeight -= 120;
		}
		ActionBar bar = getSupportActionBar();
		if (bar != null) {
			bar.setDisplayHomeAsUpEnabled(true);
		}
		ShareSDK.initSDK(this);
		// HiwifiApplication.context = this;
		// HiwifiApplication hiwifiApplication =
		// HiwifiApplication.getInstance();
		// hiwifiApplication.getActivities().add(this);
		// loginData = getSharedPreferences("loginData", 0); // 保存登陆信息
		// versionData = getSharedPreferences("versionData", 0);
		// qiutTimeData = getSharedPreferences("qiutTimeData1", 0);
		// pushData = getSharedPreferences("pushData", 0);
		// upgradeData = getSharedPreferences("upgrade", 0);
		initialize();
		getActionBar().hide();
	}

	private void initialize() {
		loadViewLayout();
		initBaseProperty();
		findViewById();
		setListener();
		processLogic();
	}

	private void initBaseProperty() {
		// TODO
		// leftBtn = (ImageView) findViewById(R.id.back_btn);
		// rightBtn = (ImageView) findViewById(R.id.right_btn);
		// commontitle = (TextView) findViewById(R.id.center_title);
		// right_text = (TextView) findViewById(R.id.right_text);
		if (leftBtn != null) {
			leftBtn.setOnClickListener(new OnClickListener() {

				@Override
				public void onClick(View v) {
					// TODO Auto-generated method stub
					finish();
				}
			});
		}

		if (rightBtn != null) {
			rightBtn.setOnClickListener(new OnClickListener() {

				@Override
				public void onClick(View v) {
					// TODO callback
					if (rightBtnClickListener != null) {
						// rightBtnClickListener
						// .RightClick(R.id.right_btn);
					}
				}
			});
		}
		if (right_text != null) {
			right_text.setOnClickListener(new OnClickListener() {

				@Override
				public void onClick(View v) {
					// TODO Auto-generated method stub
					if (rightBtnClickListener != null) {
						// rightBtnClickListener
						// .RightClick(CCResource.id.right_text);
					}
				}
			});
		}

	}

	protected void setCenterTitle(int title) {
		if (commontitle != null) {
			commontitle.setText(title);
		}
	}

	protected void setCenterTitle(String title) {
		if (commontitle != null) {
			commontitle.setText(title);
		}
	}

	protected void setRightImage(int rId) {
		if (rightBtn != null) {
			rightBtn.setVisibility(View.VISIBLE);
			right_text.setVisibility(View.GONE);
			rightBtn.setImageResource(rId);
		}
	}

	protected void setRightText(String righttext) {
		if (right_text != null) {
			right_text.setVisibility(View.VISIBLE);
			rightBtn.setVisibility(View.GONE);
			right_text.setText(righttext);
		}
	}

	protected void setRightEnable(boolean isEnable) {
		if (right_text != null && right_text.getVisibility() == View.VISIBLE) {
			right_text.setEnabled(isEnable);
		} else if (rightBtn != null && rightBtn.getVisibility() == View.VISIBLE) {
			rightBtn.setEnabled(isEnable);
		}
	}

	/**
	 * 处理点击事件
	 * 
	 * @param paramView
	 */
	protected abstract void onClickEvent(View paramView);

	/**
	 * 寻找控件
	 */
	protected abstract void findViewById();

	/**
	 * 加载布局文件
	 */
	protected abstract void loadViewLayout();

	/**
	 * 向后台请求数据
	 */
	protected abstract void processLogic();

	/**
	 * 设置监听事件
	 */
	protected abstract void setListener();

	protected IRightBtnClickListener rightBtnClickListener;

	public void setRightBtnClickListener(
			IRightBtnClickListener rightBtnClickListener) {
		this.rightBtnClickListener = rightBtnClickListener;
	}

	protected abstract interface IRightBtnClickListener {
		abstract void RightClick(int rid);
	}

	@Override
	protected void onDestroy() {
		// TODO Auto-generated method stub
		LogUtil.d("onde===>", "ondestory");
		// ShareSDK.stopSDK(this);
		// MessageManager.getInstance().stopService(this.getApplicationContext());
		// HiwifiApplication app = (HiwifiApplication) getApplication();//
		// 获取应用程序全局的实例引用
		this.finish();
		// app.activities.remove(this); // 把当前Activity从集合中移除
		super.onDestroy();
	}

	@Override
	public void onClick(View v) {
		onClickEvent(v);
	}

	@Override
	public void onResume() {
		super.onResume();
		MobclickAgent.onPageStart(this.getClass().getSimpleName());
		MobclickAgent.onResume(this);
		
		if (needRedirectToLoginPage() || needRefreshView()) {
			IntentFilter filter = new IntentFilter();
			filter.addAction(EventDispatcher.ACTION_USER);
			registerReceiver(receiver, filter);
		}
	}

	@Override
	public void onPause() {
		super.onPause();
		MobclickAgent.onPause(this);
		MobclickAgent.onPageEnd(this.getClass().getSimpleName());
		if (needRedirectToLoginPage() || needRefreshView()) {
			IntentFilter filter = new IntentFilter();
			filter.addAction(EventDispatcher.ACTION_USER);
			unregisterReceiver(receiver);
		}
	}

	LoadingDialogFragment loadingDialogFragment = null;

	protected void showMyDialog(final String message) {
		if (this.isFinishing()) {
			return;
		}
		mHandler.post(new Runnable() {

			@Override
			public void run() {
				synchronized (BaseActivity.this) {
					if (loadingDialogFragment != null) {
						if (loadingDialogFragment.isAdded()) {
							getFragmentManager().beginTransaction()
									.show(loadingDialogFragment)
									.commitAllowingStateLoss();
						} else {
							getFragmentManager().beginTransaction()
									.remove(loadingDialogFragment)
									.commitAllowingStateLoss();
							loadingDialogFragment = LoadingDialogFragment
									.newInstance(message);
							getFragmentManager().beginTransaction()
									.show(loadingDialogFragment)
									.commitAllowingStateLoss();
						}
					} else {
						loadingDialogFragment = LoadingDialogFragment
								.newInstance(message);
						loadingDialogFragment.show(getFragmentManager(), TAG);
					}
				}

			}
		});
	}

	protected void closeMyDialog() {
		if (this.isFinishing()) {
			return;
		}
		mHandler.post(new Runnable() {

			@TargetApi(Build.VERSION_CODES.HONEYCOMB_MR1)
			@Override
			public void run() {
				synchronized (BaseActivity.this) {
					if (loadingDialogFragment != null) {
						if (loadingDialogFragment.isAdded()) {
							if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB_MR1) {
								loadingDialogFragment
										.dismissAllowingStateLoss();
							} else {
								loadingDialogFragment.dismiss();
							}
						}
						loadingDialogFragment = null;
					}
				}

			}
		});

	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
		case android.R.id.home:
			finish();
			return true;
		default:
			break;
		}
		return super.onOptionsItemSelected(item);
	}

	public boolean needRedirectToLoginPage() {
		return false;
	}

	public boolean needRefreshView() {
		return false;
	}

	protected abstract void updateView();

	private final int REQUEST_LOGIN = 0x01;

	protected void redirect2LoginPage() {
		MobclickAgent.onEvent(this, "click_user_login", this.getClass()
				.getSimpleName());
		Intent intent = new Intent(this, UserLoginActivity.class);
		intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
		startActivityForResult(intent, REQUEST_LOGIN);
	}

	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		if (requestCode == REQUEST_LOGIN && User.shareInstance().hasLogin()) {
			updateView();
		}
	}

	BroadcastReceiver receiver = new BroadcastReceiver() {

		@Override
		public void onReceive(Context context, Intent intent) {
			if (intent != null && intent.getAction() != null) {
				if (ApplicationUtil.isTopActivity(BaseActivity.this)) {
					String action = intent.getAction();
					if (action.equalsIgnoreCase(EventDispatcher.ACTION_USER)) {
						if (User.shareInstance().hasLogin()) {
							if (needRefreshView()) {
								updateView();
							}
						} else {
							if (needRedirectToLoginPage()) {
								redirect2LoginPage();
							} else if (needRefreshView()) {
								updateView();
							}
						}
					}
				}

			}

		}
	};
}
