package com.hiwifi.app.task;

import java.io.UnsupportedEncodingException;

import org.apache.http.Header;
import org.apache.http.entity.StringEntity;
import org.json.JSONObject;

import com.hiwifi.constant.RequestConstant;
import com.hiwifi.hiwifi.Gl;
import com.hiwifi.model.request.RequestFactory;
import com.hiwifi.store.RequestDbMgr;
import com.hiwifi.store.RequestModel;
import com.hiwifi.support.http.JsonHttpResponseHandler;
import com.hiwifi.support.http.SyncHttpClient;

public class SaveCmccLogRunnable extends DaemonTask {

	private Boolean hasMore = true;
	private SyncHttpClient client = Gl.sharedSyncClient();
	private RequestModel requestModel = null;
	


	@Override
	public void execute() {
		while (hasMore && !isCanceled()) {
			requestModel = RequestDbMgr.shareInstance()
					.queryOlderRequestModel();
			if (requestModel == null) {
				hasMore = false;
				onFinished(true);
			} else {
				try {
					client.post(
							Gl.Ct(),
							requestModel.getUrl(),
							new StringEntity(requestModel.getDecodeParams()),
							RequestConstant.ContentType.URLENCODE.toString(),
							new JsonHttpResponseHandler() {
								@Override
								public void onSuccess(int statusCode,
										Header[] headers, JSONObject response) {
									requestModel.delete();
									super.onSuccess(statusCode, headers,
											response);
								}
							});
				} catch (UnsupportedEncodingException e) {
					mCancel = true;
					e.printStackTrace();
				}
			}
		}
	}


}
