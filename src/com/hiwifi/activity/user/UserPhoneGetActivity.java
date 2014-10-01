package com.hiwifi.activity.user;

import android.content.Context;
import android.content.Intent;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.view.View;
import android.view.View.OnFocusChangeListener;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;

import com.hiwifi.activity.base.BaseActivity;
import com.hiwifi.activity.user.VeryPhoneActivity.Source;
import com.hiwifi.app.utils.RegUtil;
import com.hiwifi.app.views.CancelableEditText;
import com.hiwifi.app.views.InputMethodRelativeLayout.OnSizeChangedListener;
import com.hiwifi.app.views.UINavigationView;
import com.hiwifi.constant.RequestConstant.RequestTag;
import com.hiwifi.hiwifi.R;
import com.hiwifi.model.AppSession;
import com.hiwifi.model.request.RequestFactory;
import com.hiwifi.model.request.RequestManager.ResponseHandler;
import com.hiwifi.model.request.ServerResponseParser;
import com.hiwifi.model.request.ServerResponseParser.ServerCode;

public class UserPhoneGetActivity extends BaseActivity implements
		OnSizeChangedListener, ResponseHandler {

	private static final String TAG = "RegisterOfPhoneActivity";
	private CancelableEditText phone_edit;
	private UINavigationView navigationView;
	private TextView errorTipTextView;
	private TextView regTipTextView;
	private int recLength = 60;
	public static final String RegTypeIsEmailByWeb = "email_web";
	public static final String RegTypeIsPhoneByClient = "phone_client";

	private void showError(String error) {
		errorTipTextView.setText(error);
		errorTipTextView.setVisibility(View.VISIBLE);
	}

	@Override
	protected void onClickEvent(View v) {
		if (v == navigationView.getRightButton()) {
			// 下一步
			if (TextUtils.isEmpty(phone_edit.getText().toString())) {
				showError("请您输入手机号");
				return;
			}
			if (!RegUtil.checkPhone(phone_edit.getText().toString())) {
				showError("请输入正确的手机号");
				return;
			}
			if (AppSession.source == Source.SourceIsREG) {
				RequestFactory.checkMobileAvailable(this, this, phone_edit
						.getText().toString());
				return;
			}
			Intent intent = new Intent();
			intent.setClass(this, VeryPhoneActivity.class);
			AppSession.userPhone = phone_edit.getText().toString();
			startActivity(intent);
		} else if (v == navigationView.getLeftButton()) {
			finish();
		} else {
			switch (v.getId()) {
			case R.id.name_cancel:
				phone_edit.setText("");
				break;
			default:
				break;
			}
		}

	}

	private InputMethodManager im;

	@Override
	protected void findViewById() {
		im = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
		phone_edit = (CancelableEditText) findViewById(R.id.phone_edit);

		navigationView = (UINavigationView) findViewById(R.id.nav);
		navigationView.getLeftButton().setOnClickListener(this);
		navigationView.getRightButton().setOnClickListener(this);
		errorTipTextView = (TextView) findViewById(R.id.tv_error_tip);
		regTipTextView = (TextView) findViewById(R.id.tv_user_regtip);
		initHeight();

	}

	private void initHeight() {
		// TODO Auto-generated method stub
	}

	@Override
	public void onResume() {
		super.onResume();
		errorTipTextView.setVisibility(View.INVISIBLE);
		if (AppSession.source == Source.SourceIsFindPWD) {
			navigationView.setTitle("找回密码");
			regTipTextView.setVisibility(View.GONE);
		} else {
			navigationView.setTitle("小极帐号注册");
			regTipTextView.setVisibility(View.VISIBLE);
		}
	}

	@Override
	protected void loadViewLayout() {
		setContentView(R.layout.activity_register_phone);
	}

	@Override
	protected void processLogic() {

	}

	@Override
	protected void setListener() {
		// back_btn.setOnClickListener(this);
		// login.setOnClickListener(this);
	}

	@Override
	protected void onDestroy() {
		super.onDestroy();
	}

	@Override
	public void onSizeChange(boolean flag, int h) {

	}

	@Override
	protected void updateView() {

	}

	@Override
	public void onStart(RequestTag tag, Code code) {
		if (code != Code.ok) {
			showError(code.getMsg());
		}
		else {
			showError("正在检查手机号");
		}
	}

	@Override
	public void onSuccess(RequestTag tag, ServerResponseParser responseParser) {
		if (responseParser.code == ServerCode.OK.value()) {
			Intent intent = new Intent();
			intent.setClass(this, VeryPhoneActivity.class);
			AppSession.userPhone = phone_edit.getText().toString();
			startActivity(intent);
		} else {
			showError(responseParser.message);
		}
	}

	@Override
	public void onFailure(RequestTag tag, Throwable error) {
		showError("网络异常，请重试");
	}

	@Override
	public void onFinish(RequestTag tag) {

	}
}
