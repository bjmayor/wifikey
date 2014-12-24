package com.seo.model.wifi.state;

import java.util.ArrayList;
import java.util.HashMap;

import org.json.JSONException;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;

import com.seo.app.services.DaemonService;
import com.seo.constant.ReleaseConstant;
import com.seo.constant.RequestConstant;
import com.seo.constant.RequestConstant.RequestTag;
import com.seo.wifikey.Gl;
import com.seo.model.request.RequestFactory;
import com.seo.model.request.RequestManager.ResponseHandler;
import com.seo.model.request.ServerResponseParser;
import com.seo.model.request.ServerResponseParser.ServerCode;
import com.seo.model.wifi.WifiAdmin;
import com.seo.model.wifi.adapter.CMCCConnectAdapter;
import com.seo.model.wifi.adapter.ChinanetConnectAdapter;
import com.seo.model.wifi.adapter.ConnectAdapter;
import com.seo.model.wifi.state.Account.Type;
import com.seo.store.AccountDbMgr;
import com.seo.store.AccountModel;
import com.seo.store.RequestModel;

public class CommerceState {

	private static HashMap<String, Account> accountHashMap = new HashMap<String, Account>();
	private static final String TYPE_CMCC_STRING = "cmcc";
	private static final String TYPE_CHINANET_STRING = "chinanet";
	private static final String TYPE_CHINAUNICOM_STRING = "chinaunicom";
	private static String TAG = "CmccState";
	private static final int SVR_RESPONSE_OK = 0;
	public static final String LOG_ACTION_LOGIN = "login";
	public static final String LOG_ACTION_LOGOUT = "logout";
	public static final String LOG_ACTION_KEEPLIVE = "keep";
	public static final String LOG_ACTION_DISCONNECT = "disconnect";

	// broadcast
	public static final String ACTION_CLICK = "com.hiwifi.hiwifi.commerce.refresh";
	public static final String ACTION_LOGOUT = "com.hiwifi.hiwifi.commerce.logout";
	public static final String ACTION_LOGIN = "com.hiwifi.hiwifi.commerce.login";
	public static final String ACTION_VIP_STATE_CHANGED = "com.hiwifi.hiwifi.commerce.vip";

	public static final String EXTRA_LEFTTIME = "lefttime";
	public static final String EXTRA_ISVIP = "isvip";

	private CommerceState() {
	}

	public static void sendBroadcast(String actiontype, int lefttime) {
		Intent intent = new Intent(actiontype);
		intent.putExtra(EXTRA_LEFTTIME, lefttime);
		Gl.GlConf.setLeftTime(lefttime);
		intent.putExtra(EXTRA_ISVIP, Gl.GlConf.cmccIsVip());
		Gl.Ct().sendBroadcast(intent);// 传递过去
	}

	public static void sendBroadcast(String actiontype, Boolean isvip) {
		Intent intent = new Intent(actiontype);
		intent.putExtra(EXTRA_LEFTTIME, Gl.GlConf.getLeftTime());
		intent.putExtra(EXTRA_ISVIP, isvip);
		Gl.GlConf.onCmccVipStateChanged(isvip);
		Gl.Ct().sendBroadcast(intent);// 传递过去
	}

	@SuppressLint("SimpleDateFormat")
	public static void notifyAccountState(final String code,
			final String reason, final Account account, final String action) {
		if (action == LOG_ACTION_LOGIN && code.equalsIgnoreCase("0")) {
			Bundle bundle = new Bundle();
			ConnectAdapter adapter = new CMCCConnectAdapter(null);
			if (account != null
					&& account.getType() == Type.TypeIsChinanet.ordinal()) {
				adapter = new ChinanetConnectAdapter(null);
			}
			bundle.putSerializable(DaemonService.EXTRA_Adapter, adapter);
			DaemonService.execCommand(Gl.Ct(),
					DaemonService.actionType_cmccLogin, bundle);
		}
		if (action == LOG_ACTION_LOGOUT && code.equalsIgnoreCase("0")) {
			DaemonService.execCommand(Gl.Ct(),
					DaemonService.actionType_cmccLogout);
		}

	}

	private static String type2Key(Type type) {
		switch (type) {
		case TypeIsCmcc:
			return TYPE_CMCC_STRING;
		case TypeIsChinanet:
			return TYPE_CHINANET_STRING;
		case TypeIsChinaunicom:
			return TYPE_CHINAUNICOM_STRING;
		default:
			break;
		}
		return "";
	}

	public static Account getAccount(Type type) {
		if (accountHashMap.get(type2Key(type)) == null) {
			AccountDbMgr mgr = AccountDbMgr.shareInstance();
			ArrayList<AccountModel> list = mgr.getAvaialbeAccount(
					type.ordinal(), 1);
			if (list != null && list.size() > 0) {
				AccountModel model = list.get(0);
				Account account = new Account(model);
				accountHashMap.put(type2Key(type), account);
			} else {
				if (WifiAdmin.sharedInstance().isConnected()) {
					getAccountFromServer(type);
				}
			}
		}
		return accountHashMap.get(type2Key(type));
	}

	public static ResponseHandler handler = new ResponseHandler() {

		@Override
		public void onStart(RequestConstant.RequestIdentify identify, Code code) {

		}

		@Override
		public void onSuccess(RequestConstant.RequestIdentify identify,
				ServerResponseParser responseParser) {
			if (responseParser.code == ServerCode.OK.value()) {

				if (identify.getTag() == RequestTag.HIWIFI_OPONE_GET) {
					Account account = new Account();
					try {
						if (responseParser.code == ServerCode.OK.value()) {
							AccountModel model = AccountModel.instance();
							model.parse(identify, responseParser);
							account = new Account(model);
							accountHashMap.put(type2Key(Type.valueOf(Integer
									.parseInt(identify.getParams().get("type")))),
									account);
						}

					} catch (Exception e) {
						e.printStackTrace();
					}
				} else if (identify.getTag() == RequestTag.HIWIFI_OPLOG_SEND) {
					try {
						if (!responseParser.originResponse.isNull("rtime")) {
							sendBroadcast(CommerceState.ACTION_LOGIN,
									responseParser.originResponse.getInt("rtime"));
						}
					} catch (JSONException e) {
						e.printStackTrace();
					}
				}
			}
		}

		@Override
		public void onFailure(RequestConstant.RequestIdentify identify, Throwable error) {
			if (identify.getTag() == RequestTag.HIWIFI_OPLOG_SEND) {
				RequestModel model = new RequestModel();
				model.requestid = RequestModel.RequestID_CMCCLOG;
				model.setParams(identify.getParams().toString(), false);
				model.add();
			}

		}

		@Override
		public void onFinish(RequestConstant.RequestIdentify identify) {

		}

	};

	public static void getAccountFromServer(final Type type) {
		if (!ReleaseConstant.isCommerceOpened
				|| !WifiAdmin.sharedInstance().isConnected()) {
			return;
		}
		RequestFactory.getOpPassword(Gl.Ct(), handler, type);
	}
}
