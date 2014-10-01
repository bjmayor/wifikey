package com.hiwifi.store.jsdatabase.tables;

import java.util.ArrayList;

import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import com.hiwifi.store.jsdatabase.DownloadUnitItem;
import com.hiwifi.store.jsdatabase.base.Table;

public class DownloadList implements Table {

	private static final String TName = "JS_download_list";
	private static final String Name = "js_name";
	private static final String FULL_Path = "full_download_path";

	public static void deleteDownloadTask(SQLiteDatabase db, String full_path) {
		db.delete(TName, FULL_Path + "=?", new String[] { full_path });
		db.close();
	}

	public static ArrayList<DownloadUnitItem> getDownloadList(SQLiteDatabase db) {
		ArrayList<DownloadUnitItem> ret = new ArrayList<DownloadUnitItem>();
		Cursor c;
		try {
			c = db.rawQuery("select * from " + TName, new String[] {});
			if (c != null) {
				while (c.moveToNext()) {
					String name = c.getString(0);
					String fullpath = c.getString(1);
					DownloadUnitItem a = new DownloadUnitItem(name, fullpath);
					ret.add(a);
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			db.close();
		}
		return ret;
	}

	public static void addDownloadTask(SQLiteDatabase db, String name,
			String path) {
		if (isExistWithoutCloseDB(db, name)) {
			// System.out.println("db replace");
			db.execSQL("update " + TName + " set " + FULL_Path + " = ? where "
					+ Name + " = ? ", new String[] { path, name });
			db.close();
		} else {
			// System.out.println("db add new");
			db.execSQL("insert into " + TName + "(" + Name + "," + FULL_Path
					+ ") values(?,?)", new String[] { name, path });
			db.close();
		}
	}

	@Override
	public void onCreate(SQLiteDatabase db) {
		String sql = "create table " + TName + " (" + Name + " text,"
				+ FULL_Path + " text, primary key (" + Name + "))";
		db.execSQL(sql);
	}

	@Override
	public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
	}

	private static boolean isExistWithoutCloseDB(SQLiteDatabase db, String name) {
		Cursor c;
		try {
			c = db.rawQuery("select count(*) from " + TName + " where " + Name
					+ " = ?", new String[] { name });
			if (c != null && c.moveToFirst()) {
				int count = c.getInt(0);
				return count > 0;
			}
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
		}
		return false;
	}

	@Override
	public void onDowngrade(SQLiteDatabase db, int oldVersion, int newVersion) {
		// TODO Auto-generated method stub
		
	}

}
