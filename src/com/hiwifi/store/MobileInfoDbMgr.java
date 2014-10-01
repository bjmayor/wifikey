package com.hiwifi.store;

import org.json.JSONObject;

import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import com.hiwifi.hiwifi.Gl;
import com.hiwifi.store.jsdatabase.base.Table;

public class MobileInfoDbMgr implements Table {

	public String TAG = MobileInfoDbMgr.class.getSimpleName();
	private static AccessPointDbHelper mDbHelper;
	private static MobileInfoDbMgr instance;

	public static class MobileTable {
		public static final String TBL_NAME = "MobileInfo";
		public static final String FLD_ID = "id";
		public static final String FLD_app_data = "data";
		public static final String FLD_TYPE = "type";
		public static final String FLD_TIME = "time";
		public static final String CREATESQL_STRING = "create table "
				+ TBL_NAME + "(" + FLD_ID
				+ " integer PRIMARY KEY AUTOINCREMENT NOT NULL," + FLD_app_data
				+ " TEXT," + FLD_TYPE + "  TEXT," + FLD_TIME + " long)";
	}

	public MobileInfoDbMgr() {
		mDbHelper = new AccessPointDbHelper(Gl.Ct());
	}

	public static MobileInfoDbMgr shareInstance() {
		if (instance == null) {
			instance = new MobileInfoDbMgr();
		}
		return instance;
	}

	@Override
	public void onCreate(SQLiteDatabase db) {
		db.execSQL(MobileTable.CREATESQL_STRING);
	}

	@Override
	public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
		// TODO Auto-generated method stub

	}

	@Override
	public void onDowngrade(SQLiteDatabase db, int oldVersion, int newVersion) {
		// TODO Auto-generated method stub

	}

	public void insert(JSONObject jsonObject, CacheConfigure cacheConfigure) {
		synchronized (AccessPointDbHelper.DB_Lock) {
			SQLiteDatabase db = mDbHelper.getWritableDatabase();
			// db.execSQL("delete from "+MobileTable.TBL_NAME);
			Cursor cursor = db.rawQuery("select * from " + MobileTable.TBL_NAME
					+ " where " + MobileTable.FLD_TYPE + "=?",
					new String[] { cacheConfigure.getCacheName() });
			if (cursor != null) {
				if (cursor.moveToFirst()) {
					cursor.close();
					db.execSQL("update " + MobileTable.TBL_NAME + " set "
							+ MobileTable.FLD_app_data + "=? , "
							+ MobileTable.FLD_TIME + "=? where "
							+ MobileTable.FLD_TYPE + " =?", new Object[] {
							jsonObject.toString(), System.currentTimeMillis(),
							cacheConfigure.getCacheName() });
				} else {
					db.execSQL(
							"insert into " + MobileTable.TBL_NAME + "("
									+ MobileTable.FLD_app_data + ","
									+ MobileTable.FLD_TYPE + ","
									+ MobileTable.FLD_TIME + ") "
									+ " values(?,?,?)",
							new Object[] { jsonObject.toString(),
									cacheConfigure.getCacheName(),
									System.currentTimeMillis() });
				}
			}
			db.close();
		}
	}

	public enum CacheConfigure {

		/**
		 * @share
		 * @database 缓存文件列表
		 */
		ALL("all"),

		Recent("recently");

		String cacheName;

		public String getCacheName() {
			return cacheName;
		}

		private CacheConfigure(String cacheName) {
			this.cacheName = cacheName;
		}
	}
}
