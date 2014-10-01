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
import org.apache.http.entity.StringEntity;

import android.content.Context;
import android.os.Bundle;
import android.text.TextUtils;

import com.hiwifi.app.services.DaemonService;
import com.hiwifi.constant.ConfigConstant;
import com.hiwifi.constant.RequestConstant;
import com.hiwifi.hiwifi.Gl;
import com.hiwifi.model.log.HWFLog;
import com.hiwifi.model.wifi.AccessPoint.WebpageTestListener;
import com.hiwifi.model.wifi.WifiAdmin;
import com.hiwifi.model.wifi.state.Account;
import com.hiwifi.model.wifi.state.Account.Type;
import com.hiwifi.model.wifi.state.CommerceState;
import com.hiwifi.support.http.AsyncHttpClient;
import com.hiwifi.support.http.AsyncHttpResponseHandler;
import com.hiwifi.support.http.RequestParams;
import com.hiwifi.support.http.TextHttpResponseHandler;
import com.hiwifi.utils.FileUtil;

/**
 * chinanet有三类卡，1.中国电信天翼手机号码；2.宽带帐号 3.WiFi时长卡帐号 我们使用的是第3类，时长卡。过程如下： step1: get
 * www.baidu.com step2:
 * 获取重定向地址:https://wlan.ct10000.com/portal/Huawei.redirect?NasName
 * =BJ-DW-SR-1.M.ME60 step3:
 * 打开上页，进入index页：https://wlan.ct10000.com/portal/index.
 * do?NasType=Huawei&NasName=BJ-DW-SR-1.M.ME60 step4:登录 post 表单
 * https://wlan.ct10000.com/portal/login4V2.do ping一下判断是否成功。 step5:登出 get 表单
 * https
 * ://wlan.ct10000.com/portal/Logout.do?NasType=Huawei&NasName=BJ-DW-SR-1.M.ME60
 * 第一类卡支持cdma+wlan的方式 step1: get www.baidu.com 获取到xml文件 step2: xml文件里获取到loginurl
 * , 补充账号等信息，post 表单。返回xml认证结果。包括登出url。 step3: get 登出url
 * 
 * @author shunpingliu
 * 
 */
public class ChinanetConnectAdapter extends ConnectAdapter implements
		Serializable {

	/**
	 * 
	 */
	private static final long serialVersionUID = -9150099743271269697L;
	protected static ArrayList<SerializedNameAndValuepair> mList;
	private final transient int timeout = 30000;
	private static final transient String ChinanetFileName = "chinanet_formlist";
	private static final transient String AccountFileName = "chinanet_account";
	private final String TAG = "ChinanetConnectAdapter";
	private String portalBody = "";
	private ParseResult parseResult;
	private static final String KEY_LOGOFF = "logoff";

	protected AsyncHttpClient getClient() {
		if (client == null) {
			client = new AsyncHttpClient();
			// client.addHeader("User-Agent", "CDMA+WLAN");
			client.addHeader("Accept-Charset", "GBK");
			client.addHeader("Connection", "close");
			client.addHeader("Cache-Control", "no-cache");
			client.setTimeout(timeout);
			client.setEnableRedirects(false);
		}
		return client;
	}

	public ChinanetConnectAdapter(Context context) {
		super(context);
		load();
	}

	private void setAccountWhenNeed() {
		if (ChinanetConnectAdapter.account == null
				&& WifiAdmin.sharedInstance().isConnected()) {
			ChinanetConnectAdapter.account = CommerceState
					.getAccount(Type.TypeIsChinanet);
		}
	}

	private void login(String username, String password) {
		// step1
		expectRedirect(ConnectAdapter.PORTALTESTURL, false);
	}

	private void expectRedirect(String url, final Boolean expectedIsIndexUrl) {
		getClient().get(url, new AsyncHttpResponseHandler() {

			@Override
			public void onSuccess(int statusCode, Header[] headers,
					byte[] responseBody) {
				mLoginStatus = true;
				onLoginSuccess();
				Bundle bundle = new Bundle();
				bundle.putSerializable(DaemonService.EXTRA_Adapter,
						ChinanetConnectAdapter.this);
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
						if (header.getName().equalsIgnoreCase("location")) {
							locationString = header.getValue();
							break;
						}
					}
					if (locationString != null) {
						if (expectedIsIndexUrl) {
							// step3
							getIndex(locationString);
						} else {
							// step2
							expectRedirect(locationString, true);
						}
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
		Map<String, String> paramsMap = new HashMap<String, String>();
		while (iterator.hasNext()) {
			SerializedNameAndValuepair pair = (SerializedNameAndValuepair) iterator
					.next();
			paramsMap.put(pair.getName(), pair.getValue());
		}
		final String urlString = paramsMap.get("basePath")==null?"https://wlan.ct10000.com/portal/Logout.do?NasType=Huawei&NasName=BJ-DW-SR-1.M.ME60":( paramsMap.get("basePath") + "Logout.do?"
				+ "NasType=" + paramsMap.get("NasType") + "&NasName="
				+ paramsMap.get("NasName"));
		getClient().post(urlString, new RequestParams(paramsMap),
				new TextHttpResponseHandler("GB2312") {

					@Override
					public void onSuccess(int statusCode, Header[] headers,
							String responseString) {
						if (WifiAdmin.sharedInstance().connectedAccessPoint() != null) {
							WifiAdmin
									.sharedInstance()
									.connectedAccessPoint()
									.startWebTest(5000,
											new WebpageTestListener() {

												@Override
												public void onTestFinished(
														long usetime) {
													parseResult = new ParseResult(
															ConnectAdapter.erorrCodeProgram,
															false,
															"ping success");
													mLoginStatus = true;
													onLogoutFail(
															parseResult.code,
															parseResult.msg);
												}

												@Override
												public void onTestFailed(
														int code, String reason) {
													parseResult = new ParseResult(
															ConnectAdapter.erorrCodeProgram,
															true, "ping fail");
													onLoginFail(
															parseResult.code,
															parseResult.msg);
													mLoginStatus = false;
													removeList();
													onLogoutSuccess();
												}
											});
						} else {
							parseResult = new ParseResult(
									ConnectAdapter.erorrCodeProgram, false,
									"wifi disconnected");
							onLogoutFail(parseResult.code, parseResult.msg);
						}
					}

					@Override
					public void onFailure(int statusCode, Header[] headers,
							String responseString, Throwable throwable) {
						onLogoutFail(ConnectAdapter.erorrCodeProgram,
								"network exception:"+urlString);
					}
				});
	}

	@Override
	public void cancel() {
		cancelled = true;
	}

	@Deprecated
	private String getLoginUrl(String portalBody) {
		if (portalBody != null) {
			Matcher responseMatcher = Pattern.compile(
					"<LoginURL>(.*)</LoginURL>").matcher(portalBody);
			if (responseMatcher.find()) {
				return responseMatcher.group(1);
			} else {
				return null;
			}
		}
		return null;
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

	// step3
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
				String action = getPatternValue(
						"<form id=\"loginForm\" name=\"loginForm\" method=\"post\" action=\"([^\"]*)\">",
						portalBody, 1);
				String baseUrl = getPatternValue(
						"<input id=basePath type=hidden value=\"([^\"]*)\" name=basePath>",
						portalBody, 1);
				String loginValue = getPatternValue(
						"<INPUT id=loginvalue type=hidden value=\"([^\"]*)\" name=loginvalue>",
						portalBody, 1);
				String language = getPatternValue(
						"<input id=language type=hidden value=\"([^\"]*)\" name=language>",
						portalBody, 1);
				String longNameLength = getPatternValue(
						"<INPUT id=longNameLength type=hidden value=(.*) name=longNameLength>",
						portalBody, 1);
				String NasType = getPatternValue(
						"<INPUT id=nasType type=hidden name=NasType value=\"([^\"]*)\">",
						portalBody, 1);
				String NasName = getPatternValue(
						"<INPUT id=nasName type=hidden name=NasName value=\"([^\"]*)\">",
						portalBody, 1);
				String OrgURL = getPatternValue(
						"<INPUT id=orgURL type=hidden name=OrgURL value=\"([^\"]*)\">",
						portalBody, 1);
				String isMobileRand = getPatternValue(
						"<INPUT id=isMobileRand  type=hidden name=isMobileRand value=\"([^\"]*)\">",
						portalBody, 1);
				mList.clear();
				mList.add(new SerializedNameAndValuepair("action", action));
				mList.add(new SerializedNameAndValuepair("validateCode", ""));
				mList.add(new SerializedNameAndValuepair("postfix",
						"@wlan.sh.chntel.com"));
				mList.add(new SerializedNameAndValuepair("address", "sh"));
				mList.add(new SerializedNameAndValuepair("loginvalue",
						loginValue));
				mList.add(new SerializedNameAndValuepair("basePath", baseUrl));
				mList.add(new SerializedNameAndValuepair("language", language));
				mList.add(new SerializedNameAndValuepair("longNameLength",
						longNameLength));
				mList.add(new SerializedNameAndValuepair("NasType", NasType));
				mList.add(new SerializedNameAndValuepair("NasName", NasName));
				mList.add(new SerializedNameAndValuepair("OrgURL", OrgURL));
				mList.add(new SerializedNameAndValuepair("isMobileRand",
						isMobileRand));
				mList.add(new SerializedNameAndValuepair("isNeedValidateCode",
						"false"));
				mList.add(new SerializedNameAndValuepair("phone", ""));
				mList.add(new SerializedNameAndValuepair("pwd_phone", ""));
				mList.add(new SerializedNameAndValuepair("validateCode_phone",
						""));
				mList.add(new SerializedNameAndValuepair(
						"validateCode_otherUser", ""));

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
		String baseurl = "";
		String loginUrl = "";
		getClient().addHeader("REFERER", reffer);
		Map<String, String> paramsMap = new HashMap<String, String>();
		while (iterator.hasNext()) {
			SerializedNameAndValuepair pair = (SerializedNameAndValuepair) iterator
					.next();
			if (pair.getName().equalsIgnoreCase("action")) {
				loginUrl = pair.getValue();
			} else if (pair.getName().equalsIgnoreCase("basePath")) {
				baseurl = pair.getValue();
			} else {
				paramsMap.put(pair.getName(), pair.getValue());
			}

		}
		if (account == null) {
			return;
		}
		// paramsMap.put("UserName", account.getUsername());
		// paramsMap.put("Password", account.getPassport(false));
		// paramsMap.put("button", "Login");
		// paramsMap.put("FNAME", "0");
		// paramsMap.put("OriginatingServer", "www.baidu.com");

		paramsMap.put("username", account.getUsername());
		paramsMap.put("password", account.getPassword(false));
		paramsMap.put("loginvalue", "1");
		paramsMap.put("otherUser", account.getUsername());
		paramsMap.put("otherUserPwd", account.getPassword(false));
		paramsMap.put("select2", "-Select+Service+Provider-");

		HWFLog.e(TAG, "user---" + account.getUsername());
		HWFLog.e(TAG, "password---" + account.getPassword(false));
		HWFLog.e(TAG, "encode password---" + account.getPassword(true));
		try {
			getClient().post(
					getContext(),
					baseurl + loginUrl,
					new StringEntity(new RequestParams(paramsMap).toString(),
							ConfigConstant.ENCODING_GB2312),
					RequestConstant.ContentType.URLENCODE.toString(),
					new AsyncHttpResponseHandler() {

						@Override
						public void onSuccess(int statusCode, Header[] headers,
								byte[] responseBody) {
							String contentString;
							try {
								contentString = new String(responseBody,
										ConfigConstant.ENCODING_UTF_8);
								portalBody = contentString;
								HWFLog.e(TAG, portalBody);
								if (WifiAdmin.sharedInstance()
										.connectedAccessPoint() != null) {
									WifiAdmin
											.sharedInstance()
											.connectedAccessPoint()
											.startWebTest(5000,
													new WebpageTestListener() {

														@Override
														public void onTestFinished(
																long usetime) {
															parseResult = new ParseResult(
																	ConnectAdapter.SucessCodeProgram,
																	true,
																	"ping success");
															mLoginStatus = true;
															onLoginSuccess();
														}

														@Override
														public void onTestFailed(
																int code,
																String reason) {
															parseResult = new ParseResult(
																	ConnectAdapter.erorrCodeProgram,
																	false,
																	"ping fail");
															onLoginFail(
																	parseResult.code,
																	parseResult.msg);
														}
													});
								} else {
									parseResult = new ParseResult(
											ConnectAdapter.erorrCodeProgram,
											false, "wifi disconnected");
									onLoginFail(parseResult.code,
											parseResult.msg);
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
									onLoginFail(
											ConnectAdapter.erorrCodeProgram,
											"cmcc exception on access eclient.do");
								}
							}
						}
					});
		} catch (UnsupportedEncodingException e1) {
			onLoginFail(ConnectAdapter.erorrCodeProgram, "create login failer");
			e1.printStackTrace();
		}

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

	@Deprecated
	public ParseResult pasreLogin(String body) {
		ParseResult result = new ParseResult(ConnectAdapter.erorrCodeProgram,
				false, "");
		if (body.contains("您本次上网时间")) {
			result.status = true;
			result.code = "0";
			result.msg = "sucess";
		} else {
			result.code = "-100";
			result.msg = "unknown";
		}
		// String responseCodeString = getPatternValue(
		// "<ResponseCode>(.*)</ResponseCode>", body, 1);
		// String messageTypeString = getPatternValue(
		// "<MessageType>(.*)</MessageType>", body, 1);
		// String replyMessage = getPatternValue(
		// "<MessageType>(.*)</MessageType>", body, 1);
		// String logoutUrl = getPatternValue("<MessageType>(.*)</MessageType>",
		// body, 1);
		// if (messageTypeString.equalsIgnoreCase("120")
		// && responseCodeString.equalsIgnoreCase("50")) {
		// result.code = "0";
		// result.status = true;
		// result.msg = replyMessage;
		// mList.add(new SerializedNameAndValuepair(KEY_LOGOFF, logoutUrl));
		// } else {
		// try {
		// result.code = responseCodeString;
		// result.msg = replyMessage;
		// } catch (Exception e) {
		// }
		// }
		return result;
	}

	@Deprecated
	public ParseResult parseLogout(String body) {
		ParseResult result = new ParseResult(ConnectAdapter.SucessCodeProgram,
				true, "");
		// String responseCodeString = getPatternValue(
		// "<ResponseCode>(.*)</ResponseCode>", body, 1);
		// String messageTypeString = getPatternValue(
		// "<MessageType>(.*)</MessageType>", body, 1);
		// result.code = responseCodeString;
		// if (responseCodeString.equalsIgnoreCase("150")
		// && messageTypeString.equalsIgnoreCase("130")) {
		// result.status = true;
		// result.msg = "success";
		// } else {
		// result.status = false;
		// result.msg = "fail";
		// }
		return result;
	}

	// 文件序列化
	public void save() {
		try {
			FileUtil.saveObject2File(ChinanetFileName, mList);
			FileUtil.saveObject2File(AccountFileName, account);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	private void load() {
		try {
			mList = (ArrayList<SerializedNameAndValuepair>) FileUtil
					.readObjectFromFile(ChinanetFileName);
		} catch (Exception e) {
		}
		if (mList == null) {
			mList = new ArrayList<SerializedNameAndValuepair>();
		}

		try {
			account = (Account) FileUtil.readObjectFromFile(AccountFileName);
		} catch (Exception e) {
			
		}
		if(account==null)
		{
			setAccountWhenNeed();
		}
	}

	private void removeList() {
		FileUtil.delFile(ChinanetFileName);
	}

	@Override
	public void login() {
		if (WifiAdmin.sharedInstance().isChinaNetConnected()) {
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
		HWFLog.e(TAG, "onLoginFail code " + code + "----msg " + msg);
		if (parseResult != null) {
			CommerceState.notifyAccountState(parseResult.code, parseResult.msg,
					account, CommerceState.LOG_ACTION_LOGIN);
		}

		super.onLoginFail(code, msg);
	}

	private void heartBeat() {

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
		HWFLog.e(TAG, "onLogoutFail code " + code + "----msg " + msg);
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
