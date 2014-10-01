package com.hiwifi.app.task;

import java.io.UnsupportedEncodingException;
import java.util.List;

import org.apache.http.Header;
import org.apache.http.entity.StringEntity;
import org.json.JSONException;
import org.json.JSONObject;

import com.hiwifi.constant.ConfigConstant;
import com.hiwifi.constant.RequestConstant.RequestTag;
import com.hiwifi.hiwifi.Gl;
import com.hiwifi.model.request.RequestBodyBuilder;
import com.hiwifi.model.request.RequestFactory;
import com.hiwifi.model.request.RequestManager;
import com.hiwifi.model.request.RequestManager.ResponseHandler;
import com.hiwifi.model.request.ServerResponseParser;
import com.hiwifi.store.AccessPointDbMgr;
import com.hiwifi.store.AccessPointModel;
import com.hiwifi.support.http.AsyncHttpClient;
import com.hiwifi.support.http.JsonHttpResponseHandler;

public class SaveApListRunnable extends DaemonTask implements ResponseHandler {

	private Boolean hasMore = true;
	private List<AccessPointModel> listToUpload = null;
	private AsyncHttpClient client = Gl.sharedSyncClient();

	@Override
	public void execute() {
		while (hasMore && !isCanceled()) {
			RequestManager.setSyncModel(true);
			listToUpload = AccessPointDbMgr.shareInstance().getUnUnploadAPList(
					20);
			if (listToUpload != null && listToUpload.size() > 0
					&& !isCanceled()) {
				RequestFactory.sendApList(Gl.Ct(), this, listToUpload);
			} else {
				hasMore = false;
				onFinished(true);

			}
		}
	}

	@Override
	public void onStart(RequestTag tag, Code code) {
		// TODO Auto-generated method stub

	}

	@Override
	public void onSuccess(RequestTag tag, ServerResponseParser responseParser) {
		try {
			if (responseParser.code == 0) {
				for (AccessPointModel model : listToUpload) {
					model.syncUpTime = (int) System.currentTimeMillis() / 1000;
					model.sync();
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	@Override
	public void onFailure(RequestTag tag, Throwable error) {

	}

	@Override
	public void onFinish(RequestTag tag) {

	}

}
