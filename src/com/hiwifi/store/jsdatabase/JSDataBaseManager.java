package com.hiwifi.store.jsdatabase;

import java.util.ArrayList;
import java.util.HashSet;

import android.database.sqlite.SQLiteDatabase;
import android.text.TextUtils;

import com.hiwifi.hiwifi.Gl;
import com.hiwifi.store.jsdatabase.base.AbstractDatabaseHelper;
import com.hiwifi.store.jsdatabase.tables.DownloadList;
import com.hiwifi.store.jsdatabase.tables.JavaScriptMapTable;
import com.hiwifi.store.jsdatabase.tables.JavaScriptTable;

public class JSDataBaseManager {
	private static DatabaseHelper dbhelper;
	private static JSDataBaseManager self;
	public js_map Ssid_Map_JS;
	public js_db JS_Database;
	public dw_list DownloadTaskList;

	private JSDataBaseManager() {
		dbhelper = new DatabaseHelper(Gl.Ct());
		JSDataBaseManager.self = this;
		Ssid_Map_JS = new js_map();
		JS_Database = new js_db();
		DownloadTaskList = new dw_list();
	}

	public void closeDBM() {
		dbhelper.close();
	}

	public static JSDataBaseManager getJSDataBaseManager() {
		if (JSDataBaseManager.self == null) {
			return new JSDataBaseManager();
		}
		dbhelper.close();
		return JSDataBaseManager.self;
	}

	// java script map management
	public class js_map {
		public void clearJSMap() {
			synchronized (AbstractDatabaseHelper._writeLock) {
				SQLiteDatabase db = dbhelper.getWritableDatabase();
				try {
					JavaScriptMapTable.clearTable(db);
				} catch (Exception e) {
					e.printStackTrace();
				}
				dbhelper.close();
			}
		}

		public void addItemInJSMap(String ssid, String js) {
			synchronized (AbstractDatabaseHelper._writeLock) {
				SQLiteDatabase db = dbhelper.getWritableDatabase();
				JavaScriptMapTable.addNew(db, ssid, js);
				dbhelper.close();
			}
		}

		public ArrayList<String> getJSArray(String ssid, boolean is_fuzz) {
			synchronized (AbstractDatabaseHelper._writeLock) {
				ArrayList<String> ret = null;
				SQLiteDatabase db = dbhelper.getReadableDatabase();
				ret = JavaScriptMapTable.getJSArray(db, ssid, is_fuzz);
				dbhelper.close();
				return ret;
			}
		}
	}

	// java script management
	public class js_db {
		public void addJS(String name, String js) {
			if (TextUtils.isEmpty(name) || TextUtils.isEmpty(js)) {
				return;
			}
			boolean temp = isJSExist(name);
			synchronized (AbstractDatabaseHelper._writeLock) {
				SQLiteDatabase db = dbhelper.getWritableDatabase();
				if (!temp) {
					JavaScriptTable.addJS(db, name, js);
				} else {
					JavaScriptTable.updateJS(db, name, js);
				}
				dbhelper.close();
			}
		}

		public boolean isEmpty() {
			SQLiteDatabase db = dbhelper.getReadableDatabase();
			return JavaScriptTable.isEmpty(db);
		}

		public String getJS(String name) {
			boolean temp = isJSExist(name);
			String ret = "";
			if (temp) {
				synchronized (AbstractDatabaseHelper._writeLock) {
					SQLiteDatabase db = dbhelper.getReadableDatabase();
					ret = JavaScriptTable.getJS(db, name);
				}
			}
			dbhelper.close();
			return ret;
		}

		public boolean isJSExist(String name) {
			synchronized (AbstractDatabaseHelper._writeLock) {
				boolean ret;
				SQLiteDatabase db = dbhelper.getReadableDatabase();
				ret = JavaScriptTable.isExist(db, name);
				dbhelper.close();
				return ret;
			}
		}

		public void clearJavaScriptDatabase() {
			synchronized (AbstractDatabaseHelper._writeLock) {
				SQLiteDatabase db = dbhelper.getWritableDatabase();
				JavaScriptTable.clearDB(db);
			}
		}

		public ArrayList<String> deleteUselessJSAndGetUnDownloadList(
				HashSet<String> js_name_set) {
			synchronized (AbstractDatabaseHelper._writeLock) {
				ArrayList<String> ret = new ArrayList<String>();
				SQLiteDatabase db = dbhelper.getWritableDatabase();
				JavaScriptTable.setAllUselessWithoutCloseDB(db);
				for (String js_name : js_name_set) {
					if (JavaScriptTable.isExistWithoutCloseDB(db, js_name)) {
						JavaScriptTable.setUsefulWithoutCloseDB(db, js_name);
					} else {
						ret.add(js_name);
					}
				}
				JavaScriptTable.deleteUselessJS(db);
				dbhelper.close();
				return ret;
			}
		}
	}

	// java script download task list
	public class dw_list {
		@SuppressWarnings("finally")
		public ArrayList<DownloadUnitItem> getDownloadTaskList() {
			ArrayList<DownloadUnitItem> ret = null;
			synchronized (AbstractDatabaseHelper._writeLock) {
				try {
					SQLiteDatabase db = dbhelper.getReadableDatabase();
					ret = DownloadList.getDownloadList(db);
					dbhelper.close();
				} catch (Exception e) {
				} finally {
					return ret;
				}
			}

		}

		public void addNewDownloadTask(String js_name, String path) {
			synchronized (AbstractDatabaseHelper._writeLock) {
				try {
					SQLiteDatabase db = dbhelper.getWritableDatabase();
					DownloadList.addDownloadTask(db, js_name, path);
					dbhelper.close();
				} catch (Exception e) {
				}
			}

		}

		public void removeDownloadTaskWithoutCloseDB(String fullDownloadPath) {
			synchronized (AbstractDatabaseHelper._writeLock) {
				try {
					SQLiteDatabase db = dbhelper.getWritableDatabase();
					DownloadList.deleteDownloadTask(db, fullDownloadPath);
				} catch (Exception e) {
				}
			}
		}
	}
}
