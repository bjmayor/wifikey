package com.hiwifi.store;

import java.util.ArrayList;
import java.util.concurrent.CopyOnWriteArrayList;

import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteStatement;
import android.util.Log;

import com.hiwifi.app.utils.RecentApplicatonUtil;
import com.hiwifi.hiwifi.Gl;
import com.hiwifi.model.log.HWFLog;
import com.hiwifi.store.jsdatabase.base.Table;

public class RecentApplicationDbMgr implements Table {
	private static final String TAG = RecentApplicationDbMgr.class
			.getSimpleName();
	private static AccessPointDbHelper mDbHelper;
	private static RecentApplicationDbMgr instance;

	
	public static class ApplicationTable {
		public static final String TBL_NAME = "RecentApplication";
		public static final String FLD_ID = "id";
		public static final String FLD_app_name = "appname";
		public static final String FLD_package = "packagename";
		public static final String FLD_START_COUNT = "count";
		public static final String DELETE_TABLE = "DROP TABLE IF EXISTS "
				+ TBL_NAME;
		public static final String CREATESQL_STRING = "create table "
				+ TBL_NAME + "(" + FLD_ID
				+ " integer PRIMARY KEY AUTOINCREMENT NOT NULL," + FLD_app_name
				+ " TEXT," + FLD_package + "  TEXT)"/*
																 * ," +
																 * FLD_START_COUNT
																 * + " integer)"
																 */;
	}

	public RecentApplicationDbMgr() {
		mDbHelper = new AccessPointDbHelper(Gl.Ct());
	}

	public synchronized static RecentApplicationDbMgr shareInstance() {
		if (instance == null) {
			instance = new RecentApplicationDbMgr();
		}
		return instance;
	}

	@Override
	public void onCreate(SQLiteDatabase db) {
		db.execSQL(ApplicationTable.CREATESQL_STRING);
	}

	@Override
	public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
		// TODO Auto-generated method stub

	}

	@Override
	public void onDowngrade(SQLiteDatabase db, int oldVersion, int newVersion) {
		// TODO Auto-generated method stub

	}

	int rowCount = 0;

	public void insertApp(ResolveInfo resolveInfo, PackageManager pm) {
		synchronized (AccessPointDbHelper.DB_Lock) {
			try {
				SQLiteDatabase db = mDbHelper.getWritableDatabase();
				// db.execSQL("delete from " + ApplicationTable.TBL_NAME);
				Cursor cursor = db.rawQuery("select * from "
						+ ApplicationTable.TBL_NAME + " where "
						+ ApplicationTable.FLD_package + "=?",
						new String[] { resolveInfo.activityInfo.packageName });
				if (cursor != null) {
					Log.d("insert:", "size:" + cursor.getCount());
					if (cursor.moveToFirst()
							&& cursor
									.getString(
											cursor.getColumnIndex(ApplicationTable.FLD_package))
									.equals(resolveInfo.activityInfo.packageName)) {
						Log.d("insert:", "含有"
								+ resolveInfo.activityInfo.packageName);
						// int id = cursor.getInt(0);
						// Log.d("insert:", "id:" + id);
						// // TODO DELETE INSERT
						// deleteAndReSortIndex(db, id,
						// resolveInfo.activityInfo.packageName);
						cursor.close();
					} else {

						// if (cursor.moveToNext()
						// && cursor
						// .getString(
						// cursor.getColumnIndex(ApplicationTable.FLD_PACKGE_NAME_STRING))
						// .equals(resolveInfo.activityInfo.packageName)) {
						// Log.d("insert:", "含有"
						// + resolveInfo.activityInfo.packageName);
						// int id = cursor.getInt(cursor.getColumnIndex("id"));
						// cursor.close();
						// // TODO DELETE INSERT
						// deleteAndReSortIndex(db, id);
						// } else {
						// Log.d("insert:", "没有"
						// + resolveInfo.activityInfo.packageName);
						// }
						// 获取行数
						Cursor cursor2 = db.rawQuery("select * from "
								+ ApplicationTable.TBL_NAME, null);
						if (cursor2 != null && cursor2.moveToFirst()) {
							rowCount = cursor2.getCount();
							Log.d("insert:", "==" + rowCount);
							cursor2.close();
						} else {
							rowCount = 0;
						}
						db.execSQL("insert into " + ApplicationTable.TBL_NAME
								+ "(" + ApplicationTable.FLD_ID + ","
								+ ApplicationTable.FLD_app_name + ","
								+ ApplicationTable.FLD_package/*
																		 * +","
																		 * +
																		 * ApplicationTable
																		 * .
																		 * FLD_START_COUNT
																		 */
								+ ") values(?,?,?)", new Object[] { ++rowCount,
								resolveInfo.loadLabel(pm).toString(),
								resolveInfo.activityInfo.packageName /*
																	 * ,
																	 * RecentApplicatonUtil
																	 * .
																	 * isThisAppUsed
																	 * (Gl.
																	 * Ct(),
																	 * resolveInfo
																	 * .
																	 * activityInfo
																	 * .
																	 * packageName
																	 * )
																	 */});

						Log.d("insert:", "succecss:" + rowCount);
					}
				}
				db.close();
			} catch (Exception e) {
				e.printStackTrace();
			}

		}
	}

	public void deleteAndReSortIndex(SQLiteDatabase db, int id,
			String packagename) {
		if (db != null) {
			synchronized (AccessPointDbHelper.DB_Lock) {
				// TODO RESORT
				db.execSQL("delete from " + ApplicationTable.TBL_NAME
						+ " where " + ApplicationTable.FLD_ID + " = " + id);
				Cursor cursor = db.rawQuery("select * from "
						+ ApplicationTable.TBL_NAME + " where id >" + id, null);
				if (cursor != null) {
					while (cursor.moveToNext()) {
						Log.d("insert:", "update:" + id);
						db.execSQL("update " + ApplicationTable.TBL_NAME
								+ " set id=" + (id++));
					}
					cursor.close();
				}
				Log.d("insert:", "delete");
			}

		}
	}

	public ArrayList<AppInfoModel> queryAppNoLock() {
		ArrayList<AppInfoModel> retList = new ArrayList<AppInfoModel>();
		SQLiteDatabase db = null;
		Cursor cursor = null;
		synchronized (AccessPointDbHelper.DB_Lock) {
			try {
				db = mDbHelper.getReadableDatabase();
				// cursor = db.rawQuery("select " +
				// RecentApplication.FLD_app_name + "," +
				// RecentApplication.FLD_PACKGE_NAME_STRING + " from "
				// + RecentApplication.TBL_NAME /*+ " order by id desc"*/,null);
				cursor = db
						.rawQuery("select * from " + ApplicationTable.TBL_NAME
								+ " order by id desc", null);
				while (cursor.moveToNext()) {
					AppInfoModel ap = new AppInfoModel(cursor);
					if (RecentApplicatonUtil.judgePackageExist(Gl.Ct(),
							ap.getPackgename())) {
						retList.add(ap);
					}
				}

			} catch (Exception e) {
				HWFLog.e(TAG, "", e);
			} finally {
				if (cursor != null) {
					cursor.close();
				}
				db.close();
			}
			return retList;
		}

	}

	public ArrayList<AppInfoModel> queryApp() {
		synchronized (AccessPointDbHelper.DB_Lock) {
			ArrayList<AppInfoModel> retList = new ArrayList<AppInfoModel>();
			SQLiteDatabase db = null;
			Cursor cursor = null;
			try {
				db = mDbHelper.getReadableDatabase();
				// cursor = db.rawQuery("select " +
				// RecentApplication.FLD_app_name + "," +
				// RecentApplication.FLD_PACKGE_NAME_STRING + " from "
				// + RecentApplication.TBL_NAME /*+ " order by id desc"*/,null);
				cursor = db.rawQuery("select * from "
						+ ApplicationTable.TBL_NAME + " order by id desc ",
						null);
				while (cursor.moveToNext()) {
					Log.d("insert:", "cursor:" + cursor.getCount());
					AppInfoModel ap = new AppInfoModel(cursor);
					if (RecentApplicatonUtil.judgePackageExist(Gl.Ct(),
							ap.getPackgename())) {
						retList.add(ap);
					}
				}

			} catch (Exception e) {
				HWFLog.e(TAG, "", e);
			} finally {
				if (cursor != null) {
					cursor.close();
				}
				db.close();
			}
			return retList;
		}
	}

	public CopyOnWriteArrayList<AppInfoModel> queryAll() {
		CopyOnWriteArrayList<AppInfoModel> retList = new CopyOnWriteArrayList<AppInfoModel>();
		SQLiteDatabase db = mDbHelper.getReadableDatabase();
		Cursor cursor = null;
		try {
			cursor = db.rawQuery("select * from " + ApplicationTable.TBL_NAME,
					null);
			while (cursor.moveToNext()) {
				AppInfoModel ap = new AppInfoModel(cursor);
				if (RecentApplicatonUtil.judgePackageExist(Gl.Ct(),
						ap.getPackgename())) {
					retList.add(ap);
				}
			}
		} catch (Exception e) {
			HWFLog.e(TAG, "", e);
		} finally {
			if (cursor != null) {
				cursor.close();
			}
			db.close();
		}
		return retList;
	}

	public void clearTable() {
		synchronized (AccessPointDbHelper.DB_Lock) {
			SQLiteDatabase db = null;
			try {
				db = mDbHelper.getReadableDatabase();
				// 开始事务
				db.beginTransaction();
				db.execSQL(ApplicationTable.DELETE_TABLE);
				db.execSQL(ApplicationTable.CREATESQL_STRING);
				db.setTransactionSuccessful();
				// System.out.println("更新成功！！");
			} catch (Exception e) {
				e.printStackTrace();
			} finally {
				// 关闭事务
				if(db!=null)
				{
					db.endTransaction();
					db.close();
				}
				
			}
		}
		
	}

	public void insertApp(ArrayList<AppInfoModel> list) {
		if(list==null || list.size()==0)
		{
			return;
		}
		synchronized (AccessPointDbHelper.DB_Lock) {
			SQLiteDatabase db = null;
			try {
				db = mDbHelper.getWritableDatabase();
				db.beginTransaction();
				String sql = "INSERT INTO "+ApplicationTable.TBL_NAME+" ("+ApplicationTable.FLD_app_name+","+ApplicationTable.FLD_package+") VALUES (?,?)";
				SQLiteStatement cs = db.compileStatement(sql);
				for (int i = 0; i < list.size(); i++) {
					cs.bindString(1, list.get(i).getAppname());
					cs.bindString(2, list.get(i).getPackgename());
					cs.execute();
					cs.clearBindings();
				}
				db.setTransactionSuccessful();
				db.endTransaction();
			} catch (Exception e) {
				e.printStackTrace();
			} finally {
				if(db!=null)
				{
					db.close();
				}
			}
		}

	}

}
