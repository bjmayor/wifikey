package com.hiwifi.app.task;

import java.util.ArrayList;
import java.util.List;

import org.json.JSONArray;
import org.json.JSONObject;

import android.annotation.SuppressLint;

import com.hiwifi.constant.RequestConstant.RequestTag;
import com.hiwifi.hiwifi.Gl;
import com.hiwifi.model.request.RequestFactory;
import com.hiwifi.model.request.RequestManager;
import com.hiwifi.model.request.RequestManager.ResponseHandler;
import com.hiwifi.model.request.ServerResponseParser;
import com.hiwifi.model.wifi.AccessPoint;
import com.hiwifi.model.wifi.BlockAlg;

@SuppressLint("DefaultLocale")
public class BlacklistGetDaemonTaskRunner extends DaemonTask implements
		ResponseHandler {
	public static final String TAG = "BlacklistGetDaemonTaskRunner";

	@Override
	public void execute() {
		onFinished(true);

		if (!mCancel) {
			RequestManager.setSyncModel(true);
			RequestFactory.getSSIDThanNotAutoConnected(Gl.Ct(), this);
		}

	}

	@Override
	public void onStart(RequestTag tag, Code code) {

	}

	@Override
	public void onSuccess(RequestTag tag, ServerResponseParser responseParser) {
		try {
			JSONArray response = responseParser.originResponse
					.getJSONArray(RequestManager.key_wrap);
			if (response != null && response.length() > 0) {
				List<BlockAlg> blackList = new ArrayList<BlockAlg>();
				for (int i = 0; i < response.length(); i++) {
					JSONObject object = response.getJSONObject(i);
					blackList.add(new BlockAlg(object.getString("s"), object
							.getInt("r")));
				}
				AccessPoint.saveList(blackList);
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
