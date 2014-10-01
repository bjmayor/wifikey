package com.hiwifi.app.task;

import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Locale;

import org.apache.http.Header;
import org.apache.http.HttpStatus;
import org.apache.http.entity.StringEntity;
import org.json.JSONArray;
import org.json.JSONObject;

import com.hiwifi.constant.ConfigConstant;
import com.hiwifi.constant.RequestConstant.RequestTag;
import com.hiwifi.hiwifi.Gl;
import com.hiwifi.hiwifi.Gl.GlConf;
import com.hiwifi.model.log.LogUtil;
import com.hiwifi.model.request.RequestBodyBuilder;
import com.hiwifi.model.request.RequestFactory;
import com.hiwifi.model.request.RequestManager;
import com.hiwifi.model.request.RequestManager.ResponseHandler;
import com.hiwifi.model.request.ServerResponseParser;
import com.hiwifi.store.jsdatabase.JSDataBaseManager;
import com.hiwifi.support.http.AsyncHttpClient;
import com.hiwifi.support.http.JsonHttpResponseHandler;
import com.hiwifi.utils.encode.Security;
import com.hiwifi.utils.jsdownloader.JS_downloader;
import com.hiwifi.utils.jsdownloader.JS_downloader.DownloadFinishListener;

public class UpdateConfRunnable extends DaemonTask implements ResponseHandler {

	private DownloadFinishListener js_download_finish_listener;
	private AsyncHttpClient client = null;
	private static Boolean isRunning = false;

	public void setJSDownloadFinishListener(DownloadFinishListener listener) {
		this.js_download_finish_listener = listener;
	}

	@Override
	public void execute() {
		onFinished(true);
		if (isRunning) {
			return;
		}
		isRunning = true;
		RequestManager.setSyncModel(true);
		RequestFactory.getConfig(Gl.Ct(), this);

	}

	@Override
	public void onStart(RequestTag tag, Code code) {

	}

	@Override
	public void onSuccess(RequestTag tag, ServerResponseParser responseParser) {
		JSDataBaseManager jsdb = JSDataBaseManager.getJSDataBaseManager();
		JSONObject response = null;
		try {
			response = responseParser.originResponse;
			if (!response.isNull("ping") && !response.isNull("ping_inc")) {
				Gl.GlConf.setPingUrl(response.optString("ping",
						GlConf.DEFAULT_PING_URL), response.optString(
						"ping_inc", GlConf.DEFAULT_PING_EXPECTCONTENT));
			}
			if (!response.isNull("force_upgrade")) {
				Gl.GlConf
						.setIsForceUpgrade(response.getInt("force_upgrade") == 1);
			}
			// System.out.println("shareusers:===="
			// + response.getString("shareusers"));
			if (!response.isNull("shareusers")) {
				Gl.GlConf.setJoinedPeoples(response.getString("shareusers"));
			}
			String path = response.optString("jspath", "");

			// System.out.println(jslist.toString());
			// dl 下载地址数组
			// dl_time=5000

			if (!response.isNull("dl")) {
				JSONArray urls = response.getJSONArray("dl");
				int l = urls.length();
				ArrayList<String> url = new ArrayList<String>();
				for (int i1 = 0; i1 < l; i1++) {
					String t = urls.optString(i1, null);
					if (t != null) {
						url.add(t);
					}
				}
				Gl.GlConf.setSpeedTestUrls(url);
			}
			int overtime = response.optInt("dl_time", 5000);
			Gl.GlConf.setSpeedTestTimeout(overtime);
			if (!response.isNull("jsbackup")) {
				JSONArray jslist = response.getJSONArray("jsbackup");
				int max = jslist.length();
				// System.out.println(max);
				HashSet<String> js_set = new HashSet<String>();
				if (max > 0) {
					jsdb.Ssid_Map_JS.clearJSMap();
				}
				// System.out.println(jslist.toString());
				for (int it = 0; it < max; it++) {
					JSONArray items = jslist.getJSONArray(it);
					// System.out.println("item "
					// + items.toString());
					String key = items.getString(0);
					key = Security.decode_string(key);
					key = key.toLowerCase(Locale.CHINA);
					key = Security.encode_string(key);
					// System.out.println("key " + key);
					int m = items.length();
					for (int x = 1; x < m; x++) {
						String tt = items.optString(x, "");
						if (tt.length() > 0) {
							js_set.add(tt);
							// System.out.println(key + ","
							// +
							// tt);
							jsdb.Ssid_Map_JS.addItemInJSMap(key, tt);
						}
					}
				}
				ArrayList<String> download_list = jsdb.JS_Database
						.deleteUselessJSAndGetUnDownloadList(js_set);
				// System.out.println("finish");
				if (download_list != null && download_list.size() > 0) {
					System.out.println("add download list");
					for (String i : download_list) {
						System.out.println(i + "," + path);
						String ipath = path + Security.decode_string(i);
						jsdb.DownloadTaskList.addNewDownloadTask(i, ipath);
					}
					JS_downloader jsd = new JS_downloader();
					if (js_download_finish_listener != null) {
						jsd.setDownloadFinishListener(js_download_finish_listener);
					}
					jsd.refresh_JSDB();
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
			System.out.println(e.toString());
		}
		isRunning = false;
	}

	@Override
	public void onFailure(RequestTag tag, Throwable error) {
		isRunning = false;
	}

	@Override
	public void onFinish(RequestTag tag) {

	}

}
