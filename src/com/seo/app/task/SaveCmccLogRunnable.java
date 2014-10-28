package com.seo.app.task;

import java.io.UnsupportedEncodingException;

import org.apache.http.Header;
import org.apache.http.entity.StringEntity;
import org.json.JSONObject;

import com.seo.constant.RequestConstant;
import com.seo.hiwifi.Gl;
import com.seo.store.RequestDbMgr;
import com.seo.store.RequestModel;
import com.seo.support.http.JsonHttpResponseHandler;
import com.seo.support.http.SyncHttpClient;

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
