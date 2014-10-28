package com.seo.app.task;

import java.io.UnsupportedEncodingException;

import org.json.JSONArray;
import org.json.JSONException;

import android.content.Context;
import android.content.Intent;

import com.seo.constant.RequestConstant.RequestTag;
import com.seo.hiwifi.Gl;
import com.seo.model.request.RequestFactory;
import com.seo.model.request.RequestManager;
import com.seo.model.request.RequestManager.ResponseHandler;
import com.seo.model.request.ServerResponseParser;
import com.seo.utils.NetworkUtil;

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
