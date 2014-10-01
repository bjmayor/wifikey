package com.hiwifi.model.wifi.adapter;


import java.io.Serializable;

import android.content.Context;

import com.hiwifi.hiwifi.Gl;
import com.hiwifi.model.log.HWFLog;
import com.hiwifi.model.wifi.state.Account;
import com.hiwifi.support.http.AsyncHttpClient;

public abstract class ConnectAdapter implements Serializable {

	/**
	 * 
	 */
	private static final long serialVersionUID = -3341868080108274106L;
	protected transient ConnectCallback mCallback;
	public static final String PORTALTESTURL = "http://www.baidu.com/favicon.ico";
	protected static final String TAG = "ConnectAdapter";
	protected Boolean cancelled = false;
	protected static Boolean mLoginStatus = false;
	protected static Account account;
	protected transient AsyncHttpClient client;
	protected transient Context mContext;

	protected AsyncHttpClient getClient() {
		if (client == null) {
			client = new AsyncHttpClient();
			client.setEnableRedirects(false);
		}
		return client;
	}

	public Account getAccount() {
		return ConnectAdapter.account;
	}

	public static synchronized final void setLoginStatus(Boolean pLoginStatu) {
		mLoginStatus = pLoginStatu;
	}

	public static final String erorrCodeProgram = "-1";
	public static final String SucessCodeProgram = "0";

	public ConnectAdapter(Context context) {
		super();
		cancelled = false;
		if (context != null) {
			mContext = context;
		} else {
			mContext = Gl.Ct();
		}
	}

	public Context getContext() {
		if (mContext == null) {
			mContext = Gl.Ct();
		}
		return mContext;
	}

	public Boolean isCanceled() {
		return cancelled;
	}

	public Boolean isLogin() {
		return mLoginStatus;
	}

	public synchronized final ConnectCallback getCallback() {
		if (mCallback == null) {
			mCallback = new ConnectCallback() {

				@Override
				public void onLoginFail(ConnectAdapter connectAdapter,
						String code, String msg) {

				}

				@Override
				public void onLoginSuccess(ConnectAdapter connectAdapter) {

				}

				@Override
				public void onLogoutFail(ConnectAdapter connectAdapter,
						String code, String msg) {

				}

				@Override
				public void onLogoutSuccess(ConnectAdapter connectAdapter) {

				}

			};
		}
		return mCallback;
	}

	public synchronized final void setCallback(ConnectCallback mCallback) {
		this.mCallback = mCallback;
	}

	public abstract void login();

	public abstract void logout();

	public abstract void cancel();

	public abstract Boolean supportAutoAuth();

	public void onLogoutSuccess() {
		if (!isCanceled() && this.getCallback() != null) {
			this.getCallback().onLogoutSuccess(this);
		}
	}

	public void onLogoutFail(String code, String msg) {
		if (!isCanceled() && this.getCallback() != null) {
			this.getCallback().onLogoutFail(this, code, msg);
		}
	}

	public void onLoginSuccess() {
		if (!isCanceled() && this.getCallback() != null) {
			this.getCallback().onLoginSuccess(this);
		}
	}

	public void onLoginFail(String code, String msg) {
		HWFLog.e(TAG, code + "===" + msg);
		if (!code.equals(ConnectAdapter.erorrCodeProgram) && account != null
				&& msg.contains("密码")) {
			account.delete();
			account = null;
		}

		if (!isCanceled() && this.getCallback() != null) {
			this.getCallback().onLoginFail(this, code, msg);
		}
	}

}
