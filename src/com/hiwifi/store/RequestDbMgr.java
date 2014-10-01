package com.hiwifi.store;

import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import com.hiwifi.hiwifi.Gl;
import com.hiwifi.store.jsdatabase.base.Table;

public class RequestDbMgr implements Table {

	private static final String TAG = RequestDbMgr.class.getSimpleName();

	private static AccessPointDbHelper mDbHelper;
	private static RequestDbMgr instance;

	public static class RequestTable {
		public static final String TBL_NAME = "Requests";

		public static final String FLD_ID = "id";
		public static final String FLD_requestId = "requestid";
		public static final String FLD_params = "params";
		public static final String CREATESQL_STRING = "create table "
				+ TBL_NAME + "(" + FLD_ID
				+ " integer PRIMARY KEY AUTOINCREMENT NOT NULL,"
				+ FLD_requestId + " int," + FLD_params + "  TEXT)";
	}

	private RequestDbMgr() {
		mDbHelper = new AccessPointDbHelper(Gl.Ct());
	}

	public static RequestDbMgr shareInstance() {
		if (instance == null) {
			instance = new RequestDbMgr();
		}
		return instance;
	}

	public int insert(RequestModel requestModel) {
		ContentValues values = new ContentValues();
		values.put(RequestTable.FLD_requestId, requestModel.requestid);
		values.put(RequestTable.FLD_params, requestModel.getEncodeParams());
		synchronized (AccessPointDbHelper.DB_Lock) {
			try {
				SQLiteDatabase db = mDbHelper.getWritableDatabase();
				int newid = (int) db
						.insert(RequestTable.TBL_NAME, null, values);
				db.close();
				return newid;
			} catch (Exception e) {
				return -1;
			}
		}
	}

	public int delete(RequestModel requestModel) {
		synchronized (AccessPointDbHelper.DB_Lock) {
			try {
				SQLiteDatabase db = mDbHelper.getWritableDatabase();
				int res = db.delete(RequestTable.TBL_NAME, "id=?",
						new String[] { requestModel.id + "" });
				db.close();
				return res;
			} catch (Exception e) {
				return -1;
			}

		}
	}

	public RequestModel queryOlderRequestModel() {
		synchronized (AccessPointDbHelper.DB_Lock) {
			try {
				SQLiteDatabase db = mDbHelper.getReadableDatabase();
				Cursor cursor = db.query(RequestTable.TBL_NAME, null, null,
						null, null, null, RequestTable.FLD_ID + " desc", "1");
				RequestModel model = null;
				if (cursor.getCount() > 0) {
					cursor.moveToFirst();
					model = new RequestModel();
					model.id = cursor.getInt(0);
					model.requestid = cursor.getInt(1);
					model.setParams(cursor.getString(2), true);
				}
				db.close();
				return model;
			} catch (Exception e) {
				return null;
			}

		}
	}

	@Override
	public void onCreate(SQLiteDatabase db) {
		db.execSQL(RequestTable.CREATESQL_STRING);
	}

	@Override
	public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {

	}

	@Override
	public void onDowngrade(SQLiteDatabase db, int oldVersion, int newVersion) {

	}

}
