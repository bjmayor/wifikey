package com.hiwifi.activity.user;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.graphics.drawable.Drawable;
import android.os.Handler;
import android.text.Editable;
import android.text.Selection;
import android.text.Spannable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.View;
import android.view.View.OnFocusChangeListener;
import android.view.ViewGroup.LayoutParams;
import android.view.WindowManager;
import android.view.animation.Animation;
import android.view.inputmethod.EditorInfo;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.hiwifi.activity.base.BaseActivity;
import com.hiwifi.activity.user.VeryPhoneActivity.Source;
import com.hiwifi.app.adapter.AutoTextViewAdapter;
import com.hiwifi.app.task.BackupWifiPasswordTack;
import com.hiwifi.app.utils.RegUtil;
import com.hiwifi.app.views.CancelableEditText;
import com.hiwifi.app.views.InputMethodRelativeLayout;
import com.hiwifi.app.views.InputMethodRelativeLayout.OnSizeChangedListener;
import com.hiwifi.app.views.UINavigationView;
import com.hiwifi.constant.ConfigConstant;
import com.hiwifi.constant.RequestConstant.RequestTag;
import com.hiwifi.hiwifi.R;
import com.hiwifi.model.AppSession;
import com.hiwifi.model.EventDispatcher;
import com.hiwifi.model.User;
import com.hiwifi.model.log.HWFLog;
import com.hiwifi.model.log.LogUtil;
import com.hiwifi.model.request.RequestFactory;
import com.hiwifi.model.request.RequestManager.ResponseHandler;
import com.hiwifi.model.request.ServerResponseParser;
import com.hiwifi.model.request.ServerResponseParser.ServerCode;
import com.hiwifi.utils.ViewUtil;

public class UserLoginActivity extends BaseActivity implements
		OnSizeChangedListener, ResponseHandler {

	public static final String TAG = "UserLoginActivity";

	private UINavigationView navigationView;
	private int recLength = 60;
	public AutoCompleteTextView passport;
	private CancelableEditText password;
	private Button loginBtn, /* introBtn, */registerBtn;
	private ImageView name_cancel;
	private RelativeLayout loginLayout;
	private TextView forget_password;
	private AutoTextViewAdapter adapter;
	private Drawable img_off;
	private static String[] autoCompleteMails;

	private InputMethodRelativeLayout in;
	private RelativeLayout loginLayout_container, relativeLayout1;
	private int totalheight;

	private boolean btn_loc = false;

	@SuppressLint("HandlerLeak")
	private Handler h = new Handler() {
		public void handleMessage(android.os.Message msg) {
			if (msg.what == 1) {
				btn_loc = false;
			}
		};
	};

	private void setButtonLock(final int wait_time) {
		btn_loc = true;
		h.sendEmptyMessageDelayed(1, wait_time);
	}

	@Override
	protected void onClickEvent(View v) {
		if (btn_loc) {
			return;
		}
		if (v == navigationView.getLeftButton()) {
			finish();
			return;
		}
		if (v == loginBtn) {// 登录click_login
			// MobclickAgent.onEvent(this, "click_login", getResources()
			// .getString(R.string.tongji_click_login));
			requestLogin();
		} else if (v == name_cancel) {
			passport.setText("");
		} else if (v == registerBtn) {
			// MobclickAgent.onEvent(this, "click_register", getResources()
			// .getString(R.string.tongji_click_register));
			setButtonLock(1000);
			Intent intent = new Intent(this, UserPhoneGetActivity.class);
			AppSession.source = Source.SourceIsREG;
			startActivity(intent);
		} else if (v == forget_password) { // forget password
			// MobclickAgent.onEvent(this, "click_find_pwd","点击找回密码");
			setButtonLock(1000);
			Intent forget = new Intent(this, UserPhoneGetActivity.class);
			AppSession.source = Source.SourceIsFindPWD;
			startActivity(forget);
		}

	}

	private void requestLogin() {
		requestLoginServer(passport.getText().toString(), password.getText()
				.toString().trim());
	}

	@Override
	protected void findViewById() {
		errorTipsTextView = (TextView) findViewById(R.id.tv_error_tip);
		loginLayout_container = (RelativeLayout) findViewById(R.id.loginLayout);
		relativeLayout1 = (RelativeLayout) findViewById(R.id.relativeLayout1);
		in = (InputMethodRelativeLayout) findViewById(R.id.login_root_layout);
		passport = (AutoCompleteTextView) findViewById(R.id.login_et_user);
		password = (CancelableEditText) findViewById(R.id.login_et_password);
		passport.setHintTextColor(getResources().getColor(
				R.color.text_color_gray));
		loginBtn = (Button) findViewById(R.id.login_btn);
		// introBtn = (Button) findViewById(R.id.intro_btn);
		// progressBar = (ProgressBar) findViewById(R.id.progress);
		loginLayout = (RelativeLayout) findViewById(R.id.loginLayout);
		name_cancel = (ImageView) findViewById(R.id.name_cancel);
		registerBtn = (Button) findViewById(R.id.reg_btn);
		forget_password = (TextView) findViewById(R.id.forget_password);
		navigationView = (UINavigationView) findViewById(R.id.nav);
		View sv = findViewById(R.id.login_sv);
		double physicSize = ViewUtil.getPhysicSize(this);
		// System.out.println("size=================="+physicSize);
	}

	@Override
	protected void loadViewLayout() {
		setContentView(R.layout.activity_user_login);
	}

	@Override
	protected void processLogic() {
	}

	@Override
	protected void setListener() {
		in.setOnSizeChangedListener(this);
		forget_password.setOnClickListener(this);
		loginBtn.setOnClickListener(this);
		// introBtn.setOnClickListener(this);
		name_cancel.setOnClickListener(this);
		registerBtn.setOnClickListener(this);
		adapter = new AutoTextViewAdapter(getApplicationContext());
		passport.setAdapter(adapter);
		passport.setThreshold(1);
		navigationView.getLeftButton().setOnClickListener(this);

		Selection.setSelection((Spannable) passport.getText(), passport
				.getText().length());
		String normal = User.shareInstance().getPassport();
		autoCompleteMails = getResources().getStringArray(R.array.email);
		passport.setText(normal);

		if (passport.isFocused() && !passport.getText().toString().equals("")
				&& passport.getText().toString() != null) {
			passport.setCompoundDrawables(passport.getCompoundDrawables()[0],
					null, img_off, null);
		} else {
			name_cancel.setVisibility(View.INVISIBLE);
			passport.setCompoundDrawables(passport.getCompoundDrawables()[0],
					null, null, null);
		}
		textChangedListener();
	}

	private void textChangedListener() {

		passport.setOnFocusChangeListener(new OnFocusChangeListener() {
			@Override
			public void onFocusChange(View v, boolean hasFocus) {
				if (hasFocus == true) {
					showCancel();
				} else {
					name_cancel.setVisibility(View.INVISIBLE);
				}
			}
		});

		passport.addTextChangedListener(new TextWatcher() {

			@Override
			public void onTextChanged(CharSequence s, int start, int before,
					int count) {
				showCancel();

			}

			@Override
			public void beforeTextChanged(CharSequence s, int start, int count,
					int after) {

			}

			@Override
			public void afterTextChanged(Editable s) {
				if (UserLoginActivity.this.isFinishing() || !isResumedCalled) {
					return;
				}
				showCancel();
				String input = s.toString().trim();
				// passport.setSelection(input.length());
				adapter.mList.clear();
				autoAddEmails(input);
				adapter.notifyDataSetChanged();
				passport.showDropDown();
				if (RegUtil.checkPhone(input)) {
					if (recLength == 60) {
					}
				} else {
				}
			}
		});

		passport.setOnEditorActionListener(new EditText.OnEditorActionListener() {
			public boolean onEditorAction(TextView v, int actionId,
					KeyEvent event) {
				if (actionId == EditorInfo.IME_ACTION_SEND
						|| actionId == EditorInfo.IME_ACTION_DONE
						|| actionId == EditorInfo.IME_ACTION_GO) {
					// 在这里编写自己想要实现的功能
					requestLogin();
				}
				return false;
			}
		});
		password.getEditText().setOnEditorActionListener(
				new EditText.OnEditorActionListener() {

					@Override
					public boolean onEditorAction(TextView v, int actionId,
							KeyEvent event) {
						if (actionId == EditorInfo.IME_ACTION_SEND
								|| actionId == EditorInfo.IME_ACTION_DONE
								|| actionId == EditorInfo.IME_ACTION_GO) {
							// 在这里编写自己想要实现的功能
							requestLogin();
						}
						return false;
					}
				});

	}

	WindowManager al;

	protected void showCancel() {
		if (passport.isFocused() && !passport.getText().toString().equals("")
				&& passport.getText().toString() != null) {
			name_cancel.setVisibility(View.VISIBLE);
		} else if (!passport.isFocused()
				|| passport.getText().toString().equals("")
				|| passport.getText().toString() == null) {
			name_cancel.setVisibility(View.INVISIBLE);
		}
	}

	/**
	 * 用户名/邮箱登录请求
	 * 
	 * @param name
	 * @param pwd
	 */
	private void requestLoginServer(final String name, String pwd) {
		if (TextUtils.isEmpty(name)) {
			showError("请输入用户名");
			return;
		}
		if (TextUtils.isEmpty(pwd)) {
			showError("密码不能为空");
			return;
		}
		hideError();
		RequestFactory.loginByPhoneOrEmail(this, this, name, pwd);
	}

	private TextView errorTipsTextView;

	private void showError(String error) {
		errorTipsTextView.setVisibility(View.VISIBLE);
		errorTipsTextView.setText(error);
	}

	private void hideError() {
		errorTipsTextView.setVisibility(View.INVISIBLE);
	}

	private void resetLayoutParams() {
		i++;
		if (i >= 2) {
			LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
					LayoutParams.MATCH_PARENT, ViewUtil.dip2px(
							UserLoginActivity.this, 60));
			params.setMargins(0, 0, 0, 0);
			params.gravity = Gravity.CENTER_VERTICAL;
			params.weight = 0.4f;
			LinearLayout.LayoutParams bparams = new LinearLayout.LayoutParams(
					LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT);
			bparams.weight = 0.6f;

		}
	}

	@Override
	public void onSizeChange(boolean flag, int h) {
		// if (flag) {
		//
		// LogUtil.d("state:", "弹起了" + h);
		// // move_height = totalheight - screenHeight/2;
		// LinearLayout.LayoutParams layoutParams =
		// (android.widget.LinearLayout.LayoutParams) loginLayout
		// .getLayoutParams();
		// layoutParams.width = LayoutParams.FILL_PARENT;
		// layoutParams.height = LayoutParams.WRAP_CONTENT;
		// layoutParams.topMargin = totalheight;
		// loginLayout.setLayoutParams(layoutParams);
		// } else {
		// LinearLayout.LayoutParams layoutParams =
		// (android.widget.LinearLayout.LayoutParams) loginLayout
		// .getLayoutParams();
		// layoutParams.width = LayoutParams.FILL_PARENT;
		// layoutParams.height = LayoutParams.WRAP_CONTENT;
		// layoutParams.topMargin = 0;
		// loginLayout.setLayoutParams(layoutParams);
		// LogUtil.d("state:", "未弹起了" + h);
		// }
	}

	private void autoAddEmails(String input) {
		String autoEmail = "";
		if (input.length() > 0) {
			for (int i = 0; i < autoCompleteMails.length; ++i) {
				if (input.contains("@")) {
					String filter = input.substring(input.indexOf("@") + 1,
							input.length());
					if (autoCompleteMails[i].contains(filter)) {
						autoEmail = input.substring(0, input.indexOf("@"))
								+ autoCompleteMails[i];

						// String sb = input.substring(0, input.indexOf("@"));
						// sb ="<font color='#00000000'>"+sb+"</font>";
						// autoEmail=sb+AUTO_EMAILS[i];

						adapter.mList.add(autoEmail);
					}
				} else {
					// autoEmail = input + AUTO_EMAILS[i];
					// adapter.mList.add(autoEmail);
				}
			}
		}
	}

	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event) {
		if (keyCode == KeyEvent.KEYCODE_BACK) {
			finish();
			// this.overridePendingTransition(R.anim.config_show,
			// R.anim.config_item_down);
		}
		return super.onKeyDown(keyCode, event);
	}

	@Override
	public void onPause() {
		closeMyDialog();
		super.onPause();
	}

	@Override
	protected void onDestroy() {
		String mName = passport.getText().toString();
		User.shareInstance().setPassport(mName);
		super.onDestroy();
	}

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		if (requestCode == ConfigConstant.REQUEST_CODE_REGISTER
				&& resultCode == 0x111 && !this.isFinishing()) {
			if (data != null && data.getExtras() != null) {
				String registType = data.getExtras().getString("registType");
				if ("phone".equalsIgnoreCase(registType)) {
					// 手机号注册
					String phone = data.getExtras().getString("phone");
					passport.setText(phone);
					passport.setSelection(phone.length());
				} else {
					// 邮箱注册
					String name = data.getExtras().getString("name");
					if (!TextUtils.isEmpty(name)) {
						passport.setText(name);
					}

				}
			}
		} else if (requestCode == ConfigConstant.REQUEST_CODE_REGISTER
				&& resultCode == 0x200 && !this.isFinishing()) {
			setResult(ConfigConstant.LOGIN_RESULT, data);
			finish();
		}

	}

	int i = 0;
	private Animation.AnimationListener inAnimListener = new Animation.AnimationListener() {

		@Override
		public void onAnimationEnd(Animation animation) {
			// rl_msg_conf.setVisibility(View.VISIBLE);
			// animation.setFillAfter(true);// 让动画执行完毕之后停住

			LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
					LayoutParams.MATCH_PARENT, ViewUtil.dip2px(
							UserLoginActivity.this, 60));
			params.setMargins(0, 0, 0, 0);
			params.gravity = Gravity.CENTER_VERTICAL;
			params.weight = 0.4f;
			LinearLayout.LayoutParams bparams = new LinearLayout.LayoutParams(
					LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT);
			bparams.weight = 0.6f;
		}

		@Override
		public void onAnimationStart(Animation animation) {
			// resetLayoutParams();

		}

		@Override
		public void onAnimationRepeat(Animation animation) {
		}

	};

	private Animation.AnimationListener outAnimListener = new Animation.AnimationListener() {

		@Override
		public void onAnimationEnd(Animation animation) {

		}

		@Override
		public void onAnimationStart(Animation animation) {
		}

		@Override
		public void onAnimationRepeat(Animation animation) {
		}

	};

	Boolean isResumedCalled = false;

	@Override
	public void onResume() {
		super.onResume();

	}

	@Override
	public void onAttachedToWindow() {
		super.onAttachedToWindow();
		isResumedCalled = true;
	}

	@Override
	public void onStart(RequestTag tag, Code code) {
		if (code == Code.ok) {
			showMyDialog("正在登录中...");
		}

	}

	@Override
	public void onSuccess(RequestTag tag, ServerResponseParser responseParser) {
		LogUtil.e(TAG, tag.toString());
		LogUtil.e(TAG, responseParser.originResponse.toString());
		switch (tag) {
		case URL_USER_LOGIN:
			if (responseParser.code == ServerCode.OK.value()) {
				closeMyDialog();
				User.shareInstance().parse(tag, responseParser);
				User.shareInstance().setPassport(tag.getParams().get("mobile"));
				finish();
				try {
					BackupWifiPasswordTack.backupPwdServer(
							UserLoginActivity.this, true);
				} catch (Exception e) {
					e.printStackTrace();
				}
				// overridePendingTransition(R.anim.config_show,
				// R.anim.config_item_down);
			} else if (responseParser.code > 9999) {
				closeMyDialog();
				showError(responseParser.message);
			} else {
				closeMyDialog();
				showError(responseParser.message);
			}
			break;
		default:
			break;
		}

	}

	@Override
	public void onFailure(RequestTag tag, Throwable error) {
		switch (tag) {
		default:
			closeMyDialog();
			showError("网络不畅");
			break;
		}

	}

	@Override
	public void onFinish(RequestTag tag) {

	}

	@Override
	protected void updateView() {

	}

}
