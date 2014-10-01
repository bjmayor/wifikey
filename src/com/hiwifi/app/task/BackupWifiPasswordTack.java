package com.hiwifi.app.task;

import java.io.UnsupportedEncodingException;
import java.util.List;

import org.apache.http.Header;
import org.apache.http.HttpStatus;
import org.apache.http.entity.StringEntity;
import org.json.JSONArray;
import org.json.JSONException;

import android.content.Context;
import android.content.Intent;

import com.hiwifi.constant.RequestConstant.RequestTag;
import com.hiwifi.hiwifi.Gl;
import com.hiwifi.model.log.LogUtil;
import com.hiwifi.model.request.RequestBodyBuilder;
import com.hiwifi.model.request.RequestFactory;
import com.hiwifi.model.request.RequestManager;
import com.hiwifi.model.request.RequestManager.ResponseHandler;
import com.hiwifi.model.request.ServerResponseParser;
import com.hiwifi.store.AccessPointDbMgr;
import com.hiwifi.store.AccessPointModel;
import com.hiwifi.support.http.AsyncHttpClient;
import com.hiwifi.support.http.JsonHttpResponseHandler;
import com.hiwifi.utils.NetworkUtil;

public class BackupWifiPasswordTack {
	
	private static  ResponseHandler handler = new ResponseHandler()
	{

		@Override
		public void onStart(RequestTag tag, Code code) {
			
		}

		@Override
		public void onSuccess(RequestTag tag,
				ServerResponseParser responseParser) {
			if (responseParser!=null) {
				JSONArray response;
				try {
					response = responseParser.originResponse.getJSONArray(RequestManager.key_wrap);
					if (response != null && response.length() > 0) {
						Gl.GlConf.setMacCount(response.length());
						Intent backup = new Intent();
						backup.setAction("hiwifi.hiwifi.login");
						backup.putExtra("backup", "backup");
						Gl.Ct().sendBroadcast(backup);
						BackupRunnable
								.syncBackupResponseToLocal(response);
					}
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
		
	};
	
	public static void backupPwdServer(final Context context, boolean changeUI)
			throws UnsupportedEncodingException {
		if (!NetworkUtil.checkConnection(context)) {
			// Utils.showToast(context, -1, "网络不畅", 0, Utils.Level.ERROR);
			return;
		}
		RequestManager.setSyncModel(true);
		RequestFactory.sendMyApList(Gl.Ct(), handler);
	}
}
