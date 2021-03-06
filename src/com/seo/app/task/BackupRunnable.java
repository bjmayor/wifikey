package com.seo.app.task;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import com.seo.constant.RequestConstant;
import com.seo.wifikey.Gl;
import com.seo.model.log.HWFLog;
import com.seo.model.request.RequestFactory;
import com.seo.model.request.RequestManager;
import com.seo.model.request.RequestManager.ResponseHandler;
import com.seo.model.request.ServerResponseParser;
import com.seo.model.wifi.AccessPoint;
import com.seo.model.wifi.WifiAdmin;
import com.seo.store.AccessPointModel.PasswordSource;

public class BackupRunnable extends DaemonTask implements ResponseHandler {
	public static final String TAG = "BackupRunnable";

	@Override
	public void execute() {
		onFinished(true);

		if (!mCancel) {
			RequestManager.setSyncModel(true);
			RequestFactory.sendMyApList(Gl.Ct(), this);
		}

	}

	public static void syncBackupResponseToLocal(JSONArray response) {
		if (response == null || response.length() == 0) {
			return;
		}
		HWFLog.i(TAG, "syncbackup count:" + response.length());
		for (int i = 0; i < response.length(); i++) {
			try {
				JSONObject itemJsonObject = response.getJSONObject(i);
				AccessPoint accessPoint = WifiAdmin.sharedInstance()
						.getAccessPointByBSSID(
								itemJsonObject.getString("bssid"));
				if (accessPoint != null) {
//					accessPoint.getDataModel().setPassword(
//							itemJsonObject.getString("password"), true,
//							PasswordSource.PasswordSourceLocal);
					accessPoint.getDataModel().setUserCount(itemJsonObject.getString("auth_username"), itemJsonObject.getString("password"), true, PasswordSource.PasswordSourceLocal);
					accessPoint.getDataModel().sync();
				} else {
					HWFLog.i(TAG, "no matched accesspoint");
				}
			} catch (JSONException e) {
				e.printStackTrace();
			}

		}
	}

	@Override
	public void onStart(RequestConstant.RequestIdentify identify, Code code) {

	}

	@Override
	public void onSuccess(RequestConstant.RequestIdentify identify, ServerResponseParser responseParser) {
		if (responseParser != null) {
			JSONArray response;
			try {
				response = responseParser.originResponse
						.getJSONArray(RequestManager.key_wrap);
				Gl.GlConf.setMacCount(response.length());
				syncBackupResponseToLocal(response);
			} catch (JSONException e) {
				e.printStackTrace();
			}

		}
	}

	@Override
	public void onFailure(RequestConstant.RequestIdentify identify, Throwable error) {

	}

	@Override
	public void onFinish(RequestConstant.RequestIdentify identify) {

	}

}
