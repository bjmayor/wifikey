package com.hiwifi.utils.jsdownloader;

import java.util.ArrayList;

import com.hiwifi.store.jsdatabase.DownloadUnitItem;
import com.hiwifi.store.jsdatabase.JSDataBaseManager;

public class JS_downloader implements JS_download_thread.DownloadAction {
	JSDataBaseManager db;
	private int max_thread = 2;
	private int running_thread = 0;
	private boolean running = false;
	private ArrayList<Download_Cache> cache;
	private ArrayList<DownloadUnitItem> list;
	private DownloadFinishListener listener;

	private Thread looking = new Thread(new Runnable() {
		@Override
		public void run() {
			while (running) {
				while (cache.size() > 0) {
					Download_Cache i = cache.get(0);
					db.DownloadTaskList
							.removeDownloadTaskWithoutCloseDB(i.path);
					wait_time(10);
					db.JS_Database.addJS(i.name, i.js);
					cache.remove(0);
					wait_time(10);
				}
			}
			if (listener != null) {
				listener.download_js_finish();
			}
		}
	});

	private Thread download_manager = new Thread(new Runnable() {
		@Override
		public void run() {
			running = true;
			for (DownloadUnitItem i : list) {
				// System.out.println(i.name + "," + i.fullpath);
				while (running_thread >= max_thread) {
					wait_time(500);
				}
				if (!running) {
					break;
				}
				new JS_download_thread(JS_downloader.this).start_download(
						i.name, i.fullpath);
				running_thread++;
			}
			while (running_thread > 0 && running) {
				wait_time(500);
			}
			running = false;
		}
	});

	public void setDownloadFinishListener(DownloadFinishListener listener) {
		this.listener = listener;
	}

	public void stopDownload() {
		running = false;
	}

	public JS_downloader() {
		db = JSDataBaseManager.getJSDataBaseManager();
		cache = new ArrayList<Download_Cache>();
		list = db.DownloadTaskList.getDownloadTaskList();
	}

	public void refresh_JSDB() {
		download_manager.start();
		looking.start();
	}

	@Override
	public void download_finish(String name, String js, String path) {
		// System.out.println(path);
		cache.add(new Download_Cache(name, js, path));
		running_thread--;
	}

	@Override
	public void download_error(String name, String message) {
		// System.out.println("failed " + name);
		running_thread--;
	}

	private void wait_time(int time) {
		try {
			Thread.sleep(time);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
	}

	public interface DownloadFinishListener {
		public void download_js_finish();
	}

	private class Download_Cache {
		public String name;
		public String js;
		public String path;

		public Download_Cache(String name, String js, String path) {
			this.name = name;
			this.js = js;
			this.path = path;
		}
	}
}
