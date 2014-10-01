package com.hiwifi.model.wifi.adapter;

import java.io.Serializable;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.regex.MatchResult;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.http.Header;
import org.apache.http.HttpStatus;

import android.content.Context;
import android.os.Bundle;
import android.text.TextUtils;

import com.hiwifi.app.services.DaemonService;
import com.hiwifi.hiwifi.Gl;
import com.hiwifi.model.log.HWFLog;
import com.hiwifi.model.wifi.WifiAdmin;
import com.hiwifi.model.wifi.state.Account;
import com.hiwifi.model.wifi.state.Account.Type;
import com.hiwifi.model.wifi.state.CommerceState;
import com.hiwifi.support.http.AsyncHttpClient;
import com.hiwifi.support.http.AsyncHttpResponseHandler;
import com.hiwifi.support.http.RequestParams;
import com.hiwifi.support.http.TextHttpResponseHandler;
import com.hiwifi.utils.FileUtil;

public class CMCCConnectAdapter extends ConnectAdapter implements Serializable {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1227645161418251648L;
	/**
	 * 
	 */
	protected static ArrayList<SerializedNameAndValuepair> mList;
	private final transient int timeout = 30000;
	private static final transient String CMCCFileName = "formlist";
	private static final transient String AccountFileName = "account";
	private final String TAG = "CMCCConnectAdapter";
	private String portalBody = "";
	private ParseResult parseResult;

	protected AsyncHttpClient getClient() {
		if (client == null) {
			client = new AsyncHttpClient();
			client.addHeader("User-Agent", "G3WLAN");
			client.addHeader("Accept-Charset", "GBK");
			client.addHeader("Connection", "close");
			client.addHeader("Cache-Control", "no-cache");
			client.setTimeout(timeout);
			client.setEnableRedirects(false);
		}
		return client;
	}

	public CMCCConnectAdapter(Context context) {
		super(context);
		load();
	}

	private void setAccountWhenNeed() {
		if (CMCCConnectAdapter.account == null
				&& WifiAdmin.sharedInstance().isConnected()) {
			CMCCConnectAdapter.account = CommerceState
					.getAccount(Type.TypeIsCmcc);
		}
	}

	private void login(String username, String password) {

		// step1
		getClient().get(ConnectAdapter.PORTALTESTURL,
				new AsyncHttpResponseHandler() {

					@Override
					public void onSuccess(int statusCode, Header[] headers,
							byte[] responseBody) {
						mLoginStatus = true;
						onLoginSuccess();
						Bundle bundle = new Bundle();
						bundle.putSerializable(DaemonService.EXTRA_Adapter,
								CMCCConnectAdapter.this);
						DaemonService.execCommand(Gl.Ct(),
								DaemonService.actionType_cmccLogin, bundle);
					}

					@Override
					public void onFailure(int statusCode, Header[] headers,
							byte[] responseBody, Throwable error) {
						if (statusCode == HttpStatus.SC_MOVED_TEMPORARILY) {
							String locationString = null;
							for (int i = 0; i < headers.length; i++) {
								Header header = headers[i];
								if (header.getName().equalsIgnoreCase(
										"location")) {
									locationString = header.getValue();
									break;
								}
							}
							if (locationString != null) {
								getIndex(locationString);
							}
							return;
						}
						onLoginFail(ConnectAdapter.erorrCodeProgram,
								"cmcc exception on access baidu.com");
					}
				});
	}

	@Override
	public void logout() {
		load();
		Iterator<SerializedNameAndValuepair> iterator = mList.iterator();
		String urlString = "";
		Map<String, String> paramsMap = new HashMap<String, String>();
		while (iterator.hasNext()) {
			SerializedNameAndValuepair pair = (SerializedNameAndValuepair) iterator
					.next();
			if (pair.getName().equalsIgnoreCase("action")) {
				urlString = pair.getValue();
			} else {
				paramsMap.put(pair.getName(), pair.getValue());
			}

		}
		if (TextUtils.isEmpty(urlString)) {
			onLogoutFail(ConnectAdapter.erorrCodeProgram,
					"no action, cann't logout");
			return;
		}
		getClient().post(urlString, new RequestParams(paramsMap),
				new TextHttpResponseHandler("GB2312") {

					@Override
					public void onSuccess(int statusCode, Header[] headers,
							String responseString) {
						parseResult = pasreLogout(responseString);
						if (parseResult.status) {
							mLoginStatus = false;
							removeList();
							onLogoutSuccess();
						} else {
							onLogoutFail(parseResult.code, parseResult.msg);
						}
					}

					@Override
					public void onFailure(int statusCode, Header[] headers,
							String responseString, Throwable throwable) {
						onLogoutFail(ConnectAdapter.erorrCodeProgram,
								"network exception");
					}
				});
	}

	@Override
	public void cancel() {
		cancelled = true;
	}

	private void formToMap(String portalBody, String formName) {
		mList.clear();
		mList.add(new SerializedNameAndValuepair("action", getPatternValue(
				"<form name=\"" + formName + "\" action=\"([^\"]*)\"",
				portalBody, 1)));
		formToMatchAction(portalBody);

	}

	private void formToMatchAction(String portalBody) {

		Pattern pattern = Pattern
				.compile("<input\\b\\s+type=\"hidden\" name=\"([^\"]*)\" value=\"([^\"]*)\">");
		Matcher matcher = pattern.matcher(portalBody);
		while (matcher.find()) {
			MatchResult result = matcher.toMatchResult();
			mList.add(new SerializedNameAndValuepair(result.group(1), result
					.group(2)));
		}
	}

	private String getPatternValue(String pattern, String content, int i) {
		Pattern patternurl = Pattern.compile(pattern);
		Matcher matcher = patternurl.matcher(content);
		int total = matcher.groupCount();
		if (total > 0) {
			if (matcher.find()) {
				MatchResult result = matcher.toMatchResult();
				return result.group(i);
			}
		}
		return null;
	}

	private void getIndex(final String url) {
		if (TextUtils.isEmpty(url) || !url.startsWith("http")) {
			if (!cancelled) {
				onLoginFail(ConnectAdapter.erorrCodeProgram,
						"index page url not got");
			}
			return;
		}
		getClient().get(url, new TextHttpResponseHandler() {

			@Override
			public void onSuccess(int statusCode, Header[] headers,
					String responseString) {
				portalBody = responseString;
				formToMap(portalBody, "loginform");
				postLogin(url);
			}

			@Override
			public void onFailure(int statusCode, Header[] headers,
					String responseString, Throwable throwable) {
				if (statusCode == 0) {
					if (!cancelled) {
						onLoginFail(ConnectAdapter.erorrCodeProgram,
								"cmcc exception on access index page");
					}
				}
			}
		});
	}

	private void postLogin(String reffer) {
		Iterator<SerializedNameAndValuepair> iterator = mList.iterator();
		String urlString = "";
		getClient().addHeader("REFERER", reffer);
		Map<String, String> paramsMap = new HashMap<String, String>();
		while (iterator.hasNext()) {
			SerializedNameAndValuepair pair = (SerializedNameAndValuepair) iterator
					.next();
			if (pair.getName().equalsIgnoreCase("action")) {
				urlString = pair.getValue();
			} else {
				paramsMap.put(pair.getName(), pair.getValue());
			}

		}
		if (account == null) {
			return;
		}
		paramsMap.put("USER", account.getUsername());
		paramsMap.put("PWD", account.getPassword(false));
		HWFLog.e(TAG, "user---" + account.getUsername());
		HWFLog.e(TAG, "password---" + account.getPassword(false));
		HWFLog.e(TAG, "encode password---" + account.getPassword(true));
		paramsMap.put("forceflag", "1");

		getClient().post(urlString, new RequestParams(paramsMap),
				new AsyncHttpResponseHandler() {

					@Override
					public void onSuccess(int statusCode, Header[] headers,
							byte[] responseBody) {
						String contentString;
						try {
							contentString = new String(responseBody, "GB2312");
							portalBody = contentString;
							parseResult = pasreLogin(contentString);
							if (parseResult.status) {
								// 准备logout数据
								formToMap(contentString, "loginform");
								mLoginStatus = true;
								onLoginSuccess();
							} else {
								onLoginFail(parseResult.code, parseResult.msg);
							}
						} catch (UnsupportedEncodingException e) {
						}
					}

					@Override
					public void onFailure(int statusCode, Header[] headers,
							byte[] responseBody, Throwable error) {
						mLoginStatus = false;
						if (statusCode == 0) {
							if (!cancelled) {
								onLoginFail(ConnectAdapter.erorrCodeProgram,
										"cmcc exception on access eclient.do");
							}
						}
					}
				}

		);
	}

	public class ParseResult implements Serializable {
		/**
		 * 
		 */
		private static final long serialVersionUID = -3214909900059415404L;
		public String code = ConnectAdapter.erorrCodeProgram;
		public Boolean status = false;
		public String msg = "";

		public ParseResult(String code, Boolean status, String msg) {
			super();
			this.code = code;
			this.status = status;
			this.msg = msg;
		}

	}

	public ParseResult pasreLogin(String body) {
		ParseResult result = new ParseResult(ConnectAdapter.erorrCodeProgram,
				false, "");
		String[] arrayOfString = body.split("\\|");
		if ((arrayOfString.length <= 3)
				|| (!(arrayOfString[1].equalsIgnoreCase("login_res")))
				|| (!(arrayOfString[2].equalsIgnoreCase("0")))) {
			result.status = false;
			result.code = arrayOfString[2];
			result.msg = arrayOfString[3];
		} else {
			result.status = true;
			result.code = arrayOfString[2];
			result.msg = arrayOfString[3];

		}
		return result;
	}

	public ParseResult pasreLogout(String body) {
		ParseResult result = new ParseResult(ConnectAdapter.erorrCodeProgram,
				false, "");
		String[] arrayOfString = body.split("\\|");
		if ((arrayOfString.length <= 3)
				|| (!(arrayOfString[1].equalsIgnoreCase("offline_res")))
				|| (!(arrayOfString[2].equalsIgnoreCase("0")))) {
			result.status = false;
			result.code = arrayOfString[2];
			result.msg = arrayOfString[3];
		} else {
			result.status = true;
			result.code = arrayOfString[2];
			result.msg = arrayOfString[3];
		}
		return result;
	}

	// 文件序列化
	public void save() {
		try {
			FileUtil.saveObject2File(CMCCFileName, mList);
			FileUtil.saveObject2File(AccountFileName, account);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	private void load() {
		try {
			mList = (ArrayList<SerializedNameAndValuepair>) FileUtil
					.readObjectFromFile(CMCCFileName);
		} catch (Exception e) {
		}
		if (mList == null) {
			mList = new ArrayList<SerializedNameAndValuepair>();
		}

		try {
			account = (Account) FileUtil.readObjectFromFile(AccountFileName);
		} catch (Exception e) {

		}
		if (account == null) {
			setAccountWhenNeed();
		}
	}

	private void removeList() {
		FileUtil.delFile(CMCCFileName);
	}

	@Override
	public void login() {
		if (WifiAdmin.sharedInstance().isCmccConnected()) {
			if (account != null) {
				login(account.getUsername(), account.getPassword(false));
			} else {
				onLoginFail(ConnectAdapter.erorrCodeProgram, "no valid accout");
				setAccountWhenNeed();
			}
		} else {
			onLoginFail(ConnectAdapter.erorrCodeProgram, "current is not cmcc");
		}

	}

	@Override
	public void onLoginFail(String code, String msg) {
		save();
		if (parseResult != null) {
			CommerceState.notifyAccountState(parseResult.code, parseResult.msg,
					account, CommerceState.LOG_ACTION_LOGIN);
		}

		super.onLoginFail(code, msg);
	}

	public void onLoginSuccess() {
		save();
		if (parseResult != null) {
			CommerceState.notifyAccountState(parseResult.code, parseResult.msg,
					account, CommerceState.LOG_ACTION_LOGIN);
		}
		super.onLoginSuccess();
	}

	@Override
	public void onLogoutFail(String code, String msg) {
		save();
		HWFLog.e(TAG, "code " + code + "----msg " + msg);
		if (parseResult != null) {
			CommerceState.notifyAccountState(parseResult.code, parseResult.msg,
					account, CommerceState.LOG_ACTION_LOGOUT);
		}
		super.onLogoutFail(code, msg);
	}

	public void onLogoutSuccess() {
		save();
		if (parseResult != null) {
			CommerceState.notifyAccountState(parseResult.code, parseResult.msg,
					account, CommerceState.LOG_ACTION_LOGOUT);
		}
		super.onLogoutSuccess();
	}

	@Override
	public Boolean supportAutoAuth() {
		return true;
	}

}
