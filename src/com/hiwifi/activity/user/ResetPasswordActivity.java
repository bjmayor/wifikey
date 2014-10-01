package com.hiwifi.activity.user;

import android.content.Context;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.app.Fragment;
import android.support.v7.app.ActionBar;
import android.support.v7.app.ActionBarActivity;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewTreeObserver.OnWindowAttachListener;
import android.view.inputmethod.InputMethodManager;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import com.hiwifi.activity.base.BaseActivity;
import com.hiwifi.app.utils.RegUtil;
import com.hiwifi.app.views.CancelableEditText;
import com.hiwifi.app.views.UINavigationView;
import com.hiwifi.constant.RequestConstant.RequestTag;
import com.hiwifi.hiwifi.Gl;
import com.hiwifi.hiwifi.R;
import com.hiwifi.model.User;
import com.hiwifi.model.request.RequestFactory;
import com.hiwifi.model.request.RequestManager.ResponseHandler;
import com.hiwifi.model.request.ServerResponseParser;
import com.hiwifi.model.request.ServerResponseParser.ServerCode;

public class ResetPasswordActivity extends BaseActivity {

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_reset_password);

		if (savedInstanceState == null) {
			getSupportFragmentManager().beginTransaction()
					.add(R.id.container, new PlaceholderFragment()).commit();
		}
		ActionBar actionBar = getSupportActionBar();
		if (actionBar != null) {
			actionBar.hide();
		}
	}

	@Override
	public void onAttachedToWindow() {
		super.onAttachedToWindow();

	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {

		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.reset_password, menu);
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

		CancelableEditText oldPasswordTextView;
		CancelableEditText newPasswordTextView;
		TextView errorTipTextView;
		Button saveButton;
		UINavigationView navigationView;

		public PlaceholderFragment() {

		}

		@Override
		public View onCreateView(LayoutInflater inflater, ViewGroup container,
				Bundle savedInstanceState) {
			View rootView = inflater.inflate(R.layout.fragment_reset_password,
					container, false);
			oldPasswordTextView = (CancelableEditText) rootView
					.findViewById(R.id.et_user_old_password);
			newPasswordTextView = (CancelableEditText) rootView
					.findViewById(R.id.et_user_new_password);
			errorTipTextView = (TextView) rootView
					.findViewById(R.id.tv_error_tip);
			saveButton = (Button) rootView.findViewById(R.id.btn_user_save);
			saveButton.setOnClickListener(this);
			navigationView = (UINavigationView) rootView.findViewById(R.id.nav);
			navigationView.getLeftButton().setOnClickListener(this);
			return rootView;
		}

		public void showError(String error) {
			errorTipTextView.setText(error);
			errorTipTextView.setVisibility(View.VISIBLE);
		}

		@Override
		public void onResume() {
			super.onResume();
			showKeyboard();
		}

		Handler mHandler = new Handler();

		private void showKeyboard() {
			mHandler.postDelayed(new Runnable() {

				@Override
				public void run() {
					InputMethodManager inputManager = (InputMethodManager) Gl
							.Ct()
							.getSystemService(Context.INPUT_METHOD_SERVICE);
					inputManager.showSoftInput(
							oldPasswordTextView.getEditText(), 0);
				}
			}, 500);

		}

		@Override
		public void onClick(View v) {
			if (v == navigationView.getLeftButton()) {
				getActivity().finish();
			}
			switch (v.getId()) {
			case R.id.btn_user_save:
				if (!RegUtil.isUserPasswordValid(newPasswordTextView.getText()
						.toString())) {
					showError("密码需6-12位");
					return;
				}
				RequestFactory.modifyPassword(getActivity(), this,
						oldPasswordTextView.getText().toString(),
						newPasswordTextView.getText().toString());
				break;
			default:
				break;
			}
		}

		@Override
		public void onStart(RequestTag tag, Code code) {
			if (code != Code.ok) {
				showError(code.getMsg());
			} else {
				showError("正在修改");
			}
		}

		@Override
		public void onSuccess(RequestTag tag,
				ServerResponseParser responseParser) {
			if (responseParser.code == ServerCode.OK.value()) {
				User.shareInstance().parse(tag, responseParser);
				showError("修改成功");
			} else if (responseParser.isLoginStatusValid()) {
				showError(responseParser.message);
			}
		}

		@Override
		public void onFailure(RequestTag tag, Throwable error) {

		}

		@Override
		public void onFinish(RequestTag tag) {

		}

	}

	@Override
	protected void onClickEvent(View paramView) {
		// TODO Auto-generated method stub

	}

	@Override
	protected void findViewById() {

	}

	@Override
	protected void loadViewLayout() {

	}

	@Override
	protected void processLogic() {

	}

	@Override
	protected void setListener() {

	}

	@Override
	public boolean needRedirectToLoginPage() {
		return true;
	}

	@Override
	protected void updateView() {

	}

}
