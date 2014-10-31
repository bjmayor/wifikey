package com.seo.app.task;

import java.util.List;

import com.seo.constant.RequestConstant.RequestTag;
import com.seo.wifikey.Gl;
import com.seo.model.request.RequestFactory;
import com.seo.model.request.RequestManager;
import com.seo.model.request.RequestManager.ResponseHandler;
import com.seo.model.request.ServerResponseParser;
import com.seo.store.AccessPointDbMgr;
import com.seo.store.AccessPointModel;
import com.seo.support.http.AsyncHttpClient;

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
