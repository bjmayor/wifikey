package com.seo.app.task;

import java.util.ArrayList;

import android.content.Context;

import com.seo.app.utils.RecentApplicatonUtil;
import com.seo.constant.RequestConstant.RequestTag;
import com.seo.wifikey.Gl;
import com.seo.model.request.RequestFactory;
import com.seo.model.request.RequestManager;
import com.seo.model.request.RequestManager.ResponseHandler;
import com.seo.model.request.ServerResponseParser;
import com.seo.store.AppInfoModel;
import com.umeng.analytics.MobclickAgent;

public class InsertRecentApplicationTask extends DaemonTask implements
		ResponseHandler {

	private int rencentAppCount = 100;

	@Override
	public void execute() {
		onFinished(true);
		// TODO Auto-generated method stub
		// if(!Gl.GlConf.isCommittedTime()){
		uploadRecentApplicationTask(Gl.Ct());
		uploadAllApplicationTask(Gl.Ct());
		// }
	}

	public void uploadRecentApplicationTask(Context context) {
		ArrayList<AppInfoModel> list = RecentApplicatonUtil
				.getRecentApplication(context);
		if (list != null && list.size() > 0) {
			RequestFactory.sendRecentOpenedAppList(Gl.Ct(), this);
		}
	}

	public void uploadAllApplicationTask(Context context) {
		RequestManager.setSyncModel(true);
		RequestFactory.sendInstalledAppList(Gl.Ct(), this);
		// TODO 插入json数据进数据库
		// MobileInfoDbMgr.shareInstance().insert(jsonObject,
		// CacheConfigure.ALL);
	}

	@Override
	public void onStart(RequestTag tag, Code code) {
		if (code != Code.ok) {
			switch (tag) {
			case HIWIFI_ALLAPP_SEND:
				MobclickAgent
						.onEvent(Gl.Ct(), "stat_upload_app", code.getMsg());
				break;

			default:
				break;
			}
		}
	}

	@Override
	public void onSuccess(RequestTag tag, ServerResponseParser responseParser) {
		switch (tag) {
		case HIWIFI_ALLAPP_SEND:
			MobclickAgent.onEvent(Gl.Ct(), "stat_upload_app", "success");
			break;

		default:
			break;
		}
	}

	@Override
	public void onFailure(RequestTag tag, Throwable error) {
		switch (tag) {
		case HIWIFI_ALLAPP_SEND:
			MobclickAgent.onEvent(Gl.Ct(), "stat_upload_app",
					error != null ? error.getMessage() : "unkown");
			break;

		default:
			break;
		}
	}

	@Override
	public void onFinish(RequestTag tag) {

	}
}
