package com.hiwifi.app.services;

import java.util.ArrayList;

import org.json.JSONObject;

import android.app.Service;
import android.content.Intent;
import android.os.Handler;
import android.os.IBinder;

import com.hiwifi.hiwifi.Gl;
import com.hiwifi.model.wifi.AccessPoint;
import com.hiwifi.support.http.AsyncHttpClient;
import com.hiwifi.utils.NetWorkConnectivity;

public class UpApService extends Service {
	Thread syncThread = null;
	SyncApToServer syncApToServer = null;
	private Handler mHandler = new Handler();
	private int timeinterval = 30*60*1000;
	public UpApService() {
	}

	@Override
	public IBinder onBind(Intent intent) {
		return null;
	}
	
	@Override
	public int onStartCommand(Intent intent, int flags, int startId) {
		
		if (syncApToServer == null) {
			syncApToServer = new SyncApToServer();
		}
		if(syncThread.isInterrupted() || syncThread==null)
		{
			syncThread = new Thread(syncApToServer);
			syncThread.start();
		}
		return super.onStartCommand(intent, flags, startId);
	}
	
	//wifi列表对表，存到本地
	public class FoundNewWifi implements Runnable
	{

		@Override
		public void run() {
			
		}
		
	}

	// 同步到服务器
	public class SyncApToServer implements Runnable {

		AsyncHttpClient asyncHttpClient = new AsyncHttpClient();
		ArrayList<AccessPoint> listToUpload = null;
		final int numOfdatasToUpload = 10;
		Boolean isRunning = false;

		// 获取最新的没有上传服务器的数据
		public Boolean checkNewerThatNotSynced(int numToUpload) {
			// 设置listToUpload

			return false;
		}

		public Boolean isNetAvaiable() {
			// wifi情况下才传
			return NetWorkConnectivity.isWifi(NetWorkConnectivity.checkNetworkType(Gl
					.Ct()));
		}
		
		public JSONObject buildJson()
		{
			return null;
		}

		public void markAsUploaded() {
		}

		public void upload(JSONObject jsonObject) {

		}

		@Override
		public void run() {
			isRunning = true;
			Boolean hasLeft = checkNewerThatNotSynced(numOfdatasToUpload);
			if (hasLeft && isNetAvaiable()) {
				upload(buildJson());
			} else {
				isRunning = false;
			}
			mHandler.postDelayed(this, timeinterval);
		}
	}

}
