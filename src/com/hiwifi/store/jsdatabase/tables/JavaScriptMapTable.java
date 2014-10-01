package com.hiwifi.store.jsdatabase.tables;

import java.util.ArrayList;

import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import com.hiwifi.store.jsdatabase.base.Table;

public class JavaScriptMapTable implements Table {
	/** 表名 */
	public static final String TName = "java_script_map";
	/** 名字 */
	public static final String SSID = "ssid";
	/** 上次使用时间,long */
	public static final String JS_CONTENT = "JS_CONTENT_list";

	public static final String FLD_ID = "id";

	public static final ArrayList<String> getJSArray(SQLiteDatabase db,
			String ssid, boolean is_fuzz) {
		Cursor cursor = null;
		ArrayList<String> arr = new ArrayList<String>();
		try {
			cursor = db.rawQuery("select " + JS_CONTENT + " from " + TName
					+ " where " + SSID + (is_fuzz ? " like ?" : " = ? "),
					new String[] { ssid });
			while (cursor != null && cursor.moveToNext()) {
				String temp = cursor.getString(0);
				if (temp != null) {
					temp = temp.trim();
					if (temp.length() > 0) {
						arr.add(temp);
					}
				}
			}
			return arr;
		} catch (Exception e) {
			// System.out.println("error from JS map table\n" + e.toString());
		} finally {
			if (cursor != null) {
				cursor.close();
			}
			db.close();
		}
		return arr;
	}

	public static void clearTable(SQLiteDatabase db) {
		db.execSQL("DROP TABLE " + TName);
		creat_teble(db);
		db.close();
	}

	public static int addNew(SQLiteDatabase db, String ssid, String js) {
		ContentValues values = new ContentValues();
		values.put(SSID, ssid);
		values.put(JS_CONTENT, js);
		int newid = (int) db.insert(TName, null, values);
		db.close();
		return newid;
	}

	@Override
	public void onCreate(SQLiteDatabase db) {
		creat_teble(db);
	}

	@Override
	public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
	}

	@Override
	public void onDowngrade(SQLiteDatabase db, int oldVersion, int newVersion) {
		// TODO Auto-generated method stub
		
	}

	private static void creat_teble(SQLiteDatabase db){
		String sql = "create table " + TName + " (" + FLD_ID
				+ " integer PRIMARY KEY AUTOINCREMENT NOT NULL," + SSID
				+ " text," + JS_CONTENT + " text)";
		db.execSQL(sql);
	}
}
