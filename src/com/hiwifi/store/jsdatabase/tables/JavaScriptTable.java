package com.hiwifi.store.jsdatabase.tables;

import com.hiwifi.store.jsdatabase.base.Table;

import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

/**
 * 人信息表
 * 
 */
public class JavaScriptTable implements Table {
	/** 表名 */
	public static String TNAME = "java_script";
	/** 名字 */
	public static String NAME = "name";
	/** JavaScript */
	public static String JS = "js";
	/** 是否 */
	public static String IS_USED = "is_used";

	public static int USEFUL = 1;
	public static int USELESS = 0;

	public static void clearDB(SQLiteDatabase db) {
		db.execSQL(" delete from " + TNAME);
		db.close();
	}

	public static void setAllUselessWithoutCloseDB(SQLiteDatabase db) {
		db.execSQL("update " + TNAME + " set " + IS_USED + "= ? ",
				new String[] { String.valueOf(USELESS) });
	}

	public static void setUsefulWithoutCloseDB(SQLiteDatabase db, String name) {
		// ContentValues values = new ContentValues();
		// values.put(IS_USED, USEFUL);
		// db.update(TNAME, values, NAME, new String[] { name });
		db.execSQL("update " + TNAME + " set " + IS_USED + "= ? where " + NAME
				+ " = ?", new String[] { String.valueOf(USEFUL), name });
	}

	public static void deleteUselessJS(SQLiteDatabase db) {
		db.execSQL("delete from " + TNAME + " where " + IS_USED + "= ? ",
				new String[] { String.valueOf(USELESS) });
		db.close();
	}

	public static void addJS(SQLiteDatabase db, String name, String js) {
		ContentValues values = new ContentValues();
		values.put(NAME, name);
		values.put(JS, js);
		values.put(IS_USED, USEFUL);
		db.insert(TNAME, null, values);
		db.close();
	}

	public static void updateJS(SQLiteDatabase db, String name, String js) {
		ContentValues values = new ContentValues();
		values.put(JS, js);
		values.put(IS_USED, USEFUL);
		db.update(TNAME, values, NAME + "=?", new String[] { name });
		db.close();
	}

	public static String getJS(SQLiteDatabase db, String name) {
		String ret = "";
		Cursor c;
		try {
			c = db.rawQuery("select " + JS + " from " + TNAME + " where "
					+ NAME + " = ?", new String[] { name });
			if (c != null && c.moveToFirst()) {
				return c.getString(0);
			}
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			db.close();
		}

		return ret;
	}

	public static boolean isExist(SQLiteDatabase db, String name) {
		boolean ret = isExistWithoutCloseDB(db, name);
		db.close();
		return ret;
	}

	public static boolean isEmpty(SQLiteDatabase db) {
		Cursor c;
		try {
			c = db.rawQuery("select count(*) from " + TNAME, new String[] {});
			if (c != null && c.moveToFirst()) {
				int count = c.getInt(0);
				return count == 0;
			}
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
		}
		return true;
	}

	public static boolean isExistWithoutCloseDB(SQLiteDatabase db, String name) {
		Cursor c;
		try {
			c = db.rawQuery("select count(*) from " + TNAME + " where " + NAME
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
	public void onCreate(SQLiteDatabase db) {
		String sql = "create table " + TNAME + " (" + NAME + " text, " + JS
				+ " text," + IS_USED + " int,  primary key (" + NAME + "))";
		db.execSQL(sql);
	}

	@Override
	public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
	}

	@Override
	public void onDowngrade(SQLiteDatabase db, int oldVersion, int newVersion) {
		// TODO Auto-generated method stub

	}

}