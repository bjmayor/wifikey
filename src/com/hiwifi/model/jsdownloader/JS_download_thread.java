package com.hiwifi.model.jsdownloader;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.StringReader;

import org.apache.http.Header;

//import android.annotation.SuppressLint;
//import android.os.Handler;
import android.os.Looper;
import android.text.TextUtils;

import com.hiwifi.support.http.AsyncHttpResponseHandler;
import com.hiwifi.support.http.SyncHttpClient;

public class JS_download_thread implements Runnable {

	private DownloadAction listener = null;
	private String message = "";
	private String js_path = "";
	private String name = "";
	private String js = "";
	private boolean need_delete_note = true;

	// @SuppressLint("HandlerLeak")
	// private Handler handler = new Handler() {
	// public void handleMessage(android.os.Message msg) {
	// if (listener != null) {
	// if (msg.what == 1) {
	// listener.download_finish(name, js, js_path);
	// } else if (msg.what == 2) {
	// listener.download_error(name, message);
	// }
	// }
	// }
	// };

	public JS_download_thread(DownloadAction listener) {
		this.listener = listener;
	}

	public JS_download_thread(DownloadAction listener, boolean need_delete_note) {
		this.listener = listener;
		this.need_delete_note = need_delete_note;
	}

	public void start_download(String js_name, String js_path) {
		if (TextUtils.isEmpty(js_name) || TextUtils.isEmpty(js_path)) {
			return;
		}
		name = js_name;
		this.js_path = js_path;
		new Thread(this).start();
	}

	@Override
	public void run() {
		Looper.prepare();
		int msg = 0;
		try {
			js = loadJS(js_path);
			if (js == null || js.trim().length() == 0) {
				message = "cannot get java script";
				msg = 2;
				// handler.sendEmptyMessage(2);
			} else {
				msg = 1;
			}
			// handler.sendEmptyMessage(1);
		} catch (Exception e) {
			message = e.toString();
			msg = 2;
			// handler.sendEmptyMessage(2);
		}
		if (listener != null) {
			if (msg == 1) {
				listener.download_finish(name, js, js_path);
			} else if (msg == 2) {
				listener.download_error(name, message);
			}
		}
		Looper.loop();
	}

	private String delete_note(String input) {
		int loc;
		int loc_end;
		String buffer = "";
		do {
			loc = input.indexOf("/*");
			loc_end = input.indexOf("*/");
			if (loc_end > loc) {
				if (loc > 0) {
					buffer += input.substring(0, loc);
				}
				if (loc_end > 0) {
					input = input.substring(loc_end + 2);
				}
			} else {
				loc = -1;
			}
		} while (loc > 0);
		buffer += input;
		BufferedReader bufferedReader = new BufferedReader(new StringReader(
				buffer));
		String ret = "";
		try {
			String temp;
			while ((temp = bufferedReader.readLine()) != null) {
				temp = temp.trim();
				loc = temp.indexOf("//");
				if (loc > 0) {
					temp = temp.substring(0, loc);
					if (temp.length() > 0) {
						ret += temp + "\r\n";
					}
				} else if ((loc < 0) && (temp.length() > 0)) {
					ret += temp + "\r\n";
				}
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
		return ret;
	}

	private String loadJS(String urlStr) {
		SyncHttpClient client = new SyncHttpClient();
		client.getHttpClient().getParams()
				.setParameter("http.socket.timeout", 30000);
		final StringBuffer buffer = new StringBuffer();
		client.get(urlStr, new AsyncHttpResponseHandler() {
			@Override
			public void onSuccess(int statusCode, Header[] headers,
					byte[] responseBody) {
				if (responseBody != null) {
					buffer.append(new String(responseBody));
				}
			}

			@Override
			public void onFailure(int statusCode, Header[] headers,
					byte[] responseBody, Throwable error) {
				if (responseBody != null) {
					message += new String(responseBody);
				}
				// System.out.println("error " + message);
			}
		});

		// System.out.println("start download " + buffer.toString());
		if (need_delete_note) {
			return delete_note(buffer.toString());
		} else {
			return buffer.toString();
		}
	}

	public interface DownloadAction {
		public void download_finish(String name, String js, String path);

		public void download_error(String name, String message);
	}
}
