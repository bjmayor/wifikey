package com.hiwifi.app.task;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import com.hiwifi.constant.RequestConstant.RequestTag;
import com.hiwifi.hiwifi.Gl;
import com.hiwifi.model.log.HWFLog;
import com.hiwifi.model.request.RequestFactory;
import com.hiwifi.model.request.RequestManager;
import com.hiwifi.model.request.RequestManager.ResponseHandler;
import com.hiwifi.model.request.ServerResponseParser;
import com.hiwifi.model.wifi.AccessPoint;
import com.hiwifi.model.wifi.WifiAdmin;
import com.hiwifi.store.AccessPointModel.PasswordSource;

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
	public void onStart(RequestTag tag, Code code) {

	}

	@Override
	public void onSuccess(RequestTag tag, ServerResponseParser responseParser) {
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
	public void onFailure(RequestTag tag, Throwable error) {

	}

	@Override
	public void onFinish(RequestTag tag) {

	}

}
