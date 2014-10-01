package com.hiwifi.activity.user;

import java.util.Timer;
import java.util.TimerTask;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.app.Fragment;
import android.support.v7.app.ActionBarActivity;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import android.widget.ViewSwitcher;

import com.hiwifi.app.views.CancelableEditText;
import com.hiwifi.app.views.UINavigationView;
import com.hiwifi.constant.RequestConstant.RequestTag;
import com.hiwifi.hiwifi.R;
import com.hiwifi.model.AppSession;
import com.hiwifi.model.User;
import com.hiwifi.model.request.RequestFactory;
import com.hiwifi.model.request.RequestManager.ResponseHandler;
import com.hiwifi.model.request.ServerResponseParser;
import com.hiwifi.model.request.ServerResponseParser.ServerCode;

public class VeryPhoneActivity extends ActionBarActivity {
	public static String SourceIsFindPWD = "findpassword";
	public static String SourceIsREG = "register";

	public enum Source {
		SourceIsFindPWD("findpassword"), SourceIsREG("register"), SourceIsViewInfo(
				"viewinfo");

		private String sourceType;

		private Source(String source) {
			this.sourceType = source;
		}
	}

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_very_phone);

		if (savedInstanceState == null) {
			getSupportFragmentManager().beginTransaction()
					.add(R.id.container, new PlaceholderFragment()).commit();
		}
		getSupportActionBar().hide();
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {

		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.very_phone, menu);
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		// Handle action bar item clicks here. The action bar will
		// automatically handle clicks on the Home/Up button, so long
		// as you specify a parent activity in AndroidManifest.xml.
		int id = item.getItemId();
		if (id == R.id.action_settings) {
			return true;
		}
		return super.onOptionsItemSelected(item);
	}

	/**
	 * A placeholder fragment containing a simple view.
	 */
	public static class PlaceholderFragment extends Fragment implements
			OnClickListener, ResponseHandler {

		UINavigationView navigationView;
		TextView phoneTextView;
		TextView timeTextView;
		TextView errorTextView;
		CancelableEditText veryCodeEditText;
		TextView processTipTextView;
		ViewSwitcher switcher;
		Button sendButton;
		private Handler handler = new Handler();

		public PlaceholderFragment() {
		}

		@Override
		public View onCreateView(LayoutInflater inflater, ViewGroup container,
				Bundle savedInstanceState) {
			View rootView = inflater.inflate(R.layout.fragment_very_phone,
					container, false);
			phoneTextView = (TextView) rootView.findViewById(R.id.tv_phone_tip);
			phoneTextView.setText(AppSession.userPhone);
			timeTextView = (TextView) rootView.findViewById(R.id.tv_time_tip);
			errorTextView = (TextView) rootView.findViewById(R.id.tv_error_tip);
			veryCodeEditText = (CancelableEditText) rootView
					.findViewById(R.id.et_verycode);
			processTipTextView = (TextView) rootView
					.findViewById(R.id.tv_process_tip);
			switcher = (ViewSwitcher) rootView.findViewById(R.id.vs_process);
			sendButton = (Button) rootView.findViewById(R.id.btn_resend);
			sendButton.setVisibility(View.GONE);
			sendButton.setOnClickListener(this);

			navigationView = (UINavigationView) rootView.findViewById(R.id.nav);
			navigationView.getLeftButton().setOnClickListener(this);
			navigationView.getRightButton().setOnClickListener(this);
			return rootView;
		}

		@Override
		public void onResume() {
			super.onResume();
			if (AppSession.source == Source.SourceIsFindPWD) {
				navigationView.setTitle("找回密码");
			} else {
				navigationView.setTitle("小极帐号注册");
			}
		}

		@Override
		public void onActivityCreated(Bundle savedInstanceState) {
			super.onActivityCreated(savedInstanceState);
			RequestFactory.sendVerycode(getActivity(), this,
					AppSession.userPhone);
		}

		public static int timeLeft = 60;

		public void showTime() {
			if (timeLeft > 0) {
				switcher.setDisplayedChild(0);
				timeTextView.setText("" + timeLeft);
			} else {
				showProcess("没有收到验证码？");
			}
		}

		public void showProcess(String msg) {
			switcher.setDisplayedChild(1);
			processTipTextView.setText(msg);
		}

		public void showError(String error) {
			errorTextView.setVisibility(View.VISIBLE);
			errorTextView.setText(error);
		}

		@Override
		public void onClick(View v) {
			if (v == navigationView.getLeftButton()) {
				getActivity().finish();
				return;
			} else if (v == navigationView.getRightButton()) {
				switch (AppSession.source) {
				case SourceIsFindPWD:
					RequestFactory.resetUserPassword(getActivity(), this,
							AppSession.userPhone, veryCodeEditText.getText()
									.toString());
					break;
				case SourceIsREG:
					if (TextUtils
							.isEmpty(veryCodeEditText.getText().toString())) {
						showError("验证码不能为空");
						return;
					}
					RequestFactory.regByPhone(getActivity(), this,
							AppSession.userPhone, veryCodeEditText.getText()
									.toString());
					break;
				default:
					break;
				}

			} else {
				switch (v.getId()) {

				case R.id.btn_resend:
					RequestFactory.sendVerycode(getActivity(), this,
							AppSession.userPhone);
					sendButton.setVisibility(View.GONE);
					break;

				default:
					break;
				}
			}
		}

		@Override
		public void onStart(RequestTag tag, Code code) {
			if (code == Code.ok) {
				switch (tag) {
				case URL_USER_VERYCODE_SEND:
					showProcess("正在获取验证码");
					timeLeft = 60;
					break;
				case URL_USER_REG_BY_PHONE:
					showProcess("正在注册...");
					break;
				default:
					break;
				}

			} else {
				showError(code.getMsg());
				switch (tag) {
				case URL_USER_VERYCODE_SEND:
					phoneTextView.setText(code.getMsg());
					sendButton.setVisibility(View.VISIBLE);
					break;

				default:
					break;
				}

			}
		}

		@Override
		public void onSuccess(RequestTag tag,
				ServerResponseParser responseParser) {
			switch (tag) {
			case URL_USER_VERYCODE_SEND:
				showProcess("验证码已经发送到" + AppSession.userPhone);
				final Timer timer = new Timer();
				timer.scheduleAtFixedRate(new TimerTask() {

					@Override
					public void run() {
						timeLeft--;
						handler.post(new Runnable() {

							@Override
							public void run() {
								showTime();
								if (timeLeft <= 0) {
									sendButton.setVisibility(View.VISIBLE);
									timer.cancel();
								}
							}
						});

					}
				}, 0, 1000);
				showTime();
				break;
			case URL_USER_REG_BY_PHONE:
				if (responseParser.code == ServerCode.OK.value()) {
					User.shareInstance().parse(tag, responseParser);
					Intent intent = new Intent();
					intent.setClass(getActivity(), UserInfoActivity.class);
					getActivity().startActivity(intent);
				} else if (responseParser.code > 9999) {
					showError(responseParser.message);
				} else {
					showError(responseParser.message);
				}
				break;
			case URL_USER_PWD_RESET:
				if (responseParser.code == 0) {
					showProcess("密码已重置，并发送到您的手机");
				} else {
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
			case URL_USER_LOGIN_BY_PHONE:

				break;

			default:
				showError("网络异常");
				break;
			}
		}

		@Override
		public void onFinish(RequestTag tag) {

		}

	}

}
