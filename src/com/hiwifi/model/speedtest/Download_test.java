package com.hiwifi.model.speedtest;

import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;

public class Download_test extends Thread {

	private URL url = null;
	private String url_str = null;
	private String test_id = null;
	private SpeedTestAction listener = null;
	private boolean is_running = false;
	private byte[] input_byte = new byte[1024];
	private long overflow;
	private long start_time;

	public Download_test(String test_id) {
		this.test_id = test_id;
	}

	public Download_test(String test_id, SpeedTestAction listener) {
		this.test_id = test_id;
		this.listener = listener;
	}

	public void setSpeedTestActionListener(SpeedTestAction listener) {
		this.listener = listener;
	}

	/**
	 * ���URL�õ������� ����������������ز���
	 * 
	 * @param urlStr
	 * @return
	 */
	private int download_file(String urlStr) {
		// *
		// System.out.println(urlStr);
		InputStream inputStream = null;
		HttpURLConnection urlConn = null;
		try {
			url = new URL(urlStr);
			urlConn = (HttpURLConnection) url.openConnection();
			inputStream = urlConn.getInputStream();
			if (inputStream == null) {
				if (listener != null) {
					listener.error_download(test_id,
							"Cannot get stream from URL");
				}
				return -1;
			} else {
				if (listener != null) {
					listener.start_download(test_id);
				}
				while (is_running && inputStream != null
						&& (System.currentTimeMillis() - start_time < overflow)) {
					inputStream.read(input_byte);
				}
			}
			if (listener != null) {
				listener.finish_download(test_id);
			}
		} catch (MalformedURLException e) {
			if (listener != null) {
				listener.error_download(test_id, "URL connection error");
			}
			return -1;
		} catch (Exception e) {
			if (listener != null) {
				listener.error_download(test_id, "Stream read error");
			}
			return -1;
		} finally {
			try {
				if (inputStream != null) {
					inputStream.close();
				}
				urlConn.disconnect();
			} catch (IOException e) {
				e.printStackTrace();
			}
			is_running = false;
		}// */
		return 0;
	}

	@Override
	public void run() {
		if (url_str != null) {
			download_file(url_str);
		}
	}

	public void stop_test() {
		is_running = false;
	}

	public boolean start_test(String url_str, int overflow) {
		if (url_str != null) {
			this.url_str = url_str;
			this.overflow = overflow;
			start_time = System.currentTimeMillis();
			is_running = true;
			this.start();
			return true;
		} else {
			return false;
		}
	}

	public interface SpeedTestAction {
		public void finish_download(String test_id);

		public void start_download(String test_id);

		public void error_download(String test_id, String error_message);
	}
}
