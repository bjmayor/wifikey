package com.hiwifi.store;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import com.hiwifi.hiwifi.Gl;
import com.hiwifi.model.log.HWFLog;
import com.hiwifi.model.wifi.AccessPoint;
import com.hiwifi.store.jsdatabase.base.Table;

public class AccessPointDbMgr implements Table {

	private static final String TAG = AccessPointDbMgr.class.getSimpleName();

	private static AccessPointDbHelper mDbHelper;
	private static AccessPointDbMgr instance;

	public static class AccessPointTable {
		public static final String TBL_NAME = "AccessPoint";
		public static final String FLD_ID = "id";
		public static final String FLD_SSID = "SSID";// 名称
		public static final String FLD_BSSID = "BSSID";// mac地址
		public static final String FLD_longitude = "longitude";// 经度
		public static final String FLD_latitude = "latitude";
		public static final String FLD_createTime = "createTime";
		public static final String FLD_updateTime = "updateTime";
		public static final String FLD_password = "password";
		public static final String FLD_passwordType = "passwordType";
		public static final String FLD_syncUpTime = "syncUpTime";
		public static final String FLD_syncDownTime = "syncDownTime";
		public static final String FLD_useTimes = "useTimes";
		public static final String FLD_level = "level";
		public static final String FLD_capability = "capability";
		public static final String FLD_channel = "channel";
		public static final String FLD_account = "account";
		public static final String FLD_passwordstatus = "status";
		public static final String FLD_isPortal = "isPortal";

		public static final String CREATESQL_STRING = "CREATE TABLE IF NOT EXISTS "
				+ AccessPointTable.TBL_NAME
				+ "("
				+ AccessPointTable.FLD_ID
				+ " integer PRIMARY KEY AUTOINCREMENT NOT NULL,"
				+ AccessPointTable.FLD_SSID
				+ " varchar(128),"
				+ AccessPointTable.FLD_BSSID
				+ " varchar(128),"
				+ AccessPointTable.FLD_longitude
				+ " FLOAT,"
				+ AccessPointTable.FLD_latitude
				+ " FLOAT,"
				+ AccessPointTable.FLD_createTime
				+ " TIMESTAMP,"
				+ AccessPointTable.FLD_updateTime
				+ " TIMESTAMP,"
				+ AccessPointTable.FLD_password
				+ " varchar(128),"
				+ AccessPointTable.FLD_passwordType
				+ " int,"
				+ AccessPointTable.FLD_syncUpTime
				+ " TIMESTAMP,"
				+ AccessPointTable.FLD_syncDownTime
				+ " TIMESTAMP,"
				+ AccessPointTable.FLD_useTimes
				+ " int,"
				+ AccessPointTable.FLD_level
				+ " int,"
				+ AccessPointTable.FLD_capability
				+ " varchar(128),"
				+ AccessPointTable.FLD_account
				+ " varchar(64),"
				+ AccessPointTable.FLD_channel
				+ " int,"
				+ AccessPointTable.FLD_passwordstatus
				+ " int, "
				+ AccessPointTable.FLD_isPortal + " int" + ")";
	}

	private AccessPointDbMgr() {
		mDbHelper = new AccessPointDbHelper(Gl.Ct());
	}

	public static AccessPointDbMgr shareInstance() {
		if (instance == null) {
			instance = new AccessPointDbMgr();
		}
		return instance;
	}

	public void insert(AccessPointModel model) {
		synchronized (AccessPointDbHelper.DB_Lock) {
			try {
				SQLiteDatabase db = mDbHelper.getWritableDatabase();
				db.execSQL(
						"insert into " + AccessPointTable.TBL_NAME + "("
								+ AccessPointTable.FLD_SSID + ","
								+ AccessPointTable.FLD_BSSID + ","
								+ AccessPointTable.FLD_longitude + ","
								+ AccessPointTable.FLD_latitude + ","
								+ AccessPointTable.FLD_createTime + ","
								+ AccessPointTable.FLD_updateTime + ","
								+ AccessPointTable.FLD_password + ","
								+ AccessPointTable.FLD_isPortal
								+ ") values(?,?,?,?,?,?,?,?)",
						new Object[] {
								model.SSID,
								model.BSSID,
								model.longitude,
								model.latitude,
								System.currentTimeMillis(),
								System.currentTimeMillis(),
								model.getPassword(true) == null ? "" : model
										.getPassword(true),
								model.isPortal() ? AccessPointModel.PortalTrue
										: AccessPointModel.PortalFalse });
				db.close();
			} catch (Exception e) {
				e.printStackTrace();
			}

		}
	}

	public void deleteAP(String BSSID) {
		synchronized (AccessPointDbHelper.DB_Lock) {
			try {
				SQLiteDatabase db = mDbHelper.getWritableDatabase();
				db.execSQL("delete from " + AccessPointTable.TBL_NAME
						+ " where BSSID = ? ", new Object[] { BSSID });
				db.close();
			} catch (Exception e) {
				e.printStackTrace();
			}

		}
	}

	public void update(String BSSID, float longitude, float latitude) {
		synchronized (AccessPointDbHelper.DB_Lock) {
			try {
				SQLiteDatabase db = mDbHelper.getWritableDatabase();
				db.execSQL("update " + AccessPointTable.TBL_NAME + " set "
						+ AccessPointTable.FLD_longitude + " = ?, "
						+ AccessPointTable.FLD_latitude + "=?,"
						+ AccessPointTable.FLD_updateTime
						+ "=?  where BSSID = ?", new Object[] { longitude,
						latitude, System.currentTimeMillis(), BSSID });
				db.close();
			} catch (Exception e) {
				e.printStackTrace();
			}

		}

	}

	public void update(String BSSID, String password) {
		synchronized (AccessPointDbHelper.DB_Lock) {
			try {
				SQLiteDatabase db = mDbHelper.getWritableDatabase();
				db.execSQL("update " + AccessPointTable.TBL_NAME + " set "
						+ AccessPointTable.FLD_password + " = ?, "
						+ AccessPointTable.FLD_updateTime
						+ "=?  where BSSID = ?", new Object[] { password,
						System.currentTimeMillis(), BSSID });
				db.close();
			} catch (Exception e) {
				e.printStackTrace();
			}

		}
	}

	public void updateByMap(int id, Map<String, String> otherFileds) {
		synchronized (AccessPointDbHelper.DB_Lock) {
			try {
				SQLiteDatabase db = mDbHelper.getWritableDatabase();
				otherFileds.remove(AccessPointTable.FLD_ID);
				Object[] valueSet = new Object[otherFileds.size() + 1];
				String innerSqlString = " set ";
				Set<String> keySet = otherFileds.keySet();
				Iterator<String> iterator = keySet.iterator();
				int i = 0;
				while (iterator.hasNext()) {
					String key = (String) iterator.next();
					innerSqlString += " " + key + "=?,";
					valueSet[i++] = otherFileds.get(key);
				}
				valueSet[i] = id + "";
				innerSqlString = innerSqlString.substring(0,
						innerSqlString.length() - 1);
				String updateSqlString = "update " + AccessPointTable.TBL_NAME
						+ innerSqlString + " where " + AccessPointTable.FLD_ID
						+ " = ?";
				db.execSQL(updateSqlString, valueSet);
				db.close();
			} catch (Exception e) {
				e.printStackTrace();
			}

		}
	}

	public AccessPointModel getAccessPointByBSSID(String BSSID) {
		synchronized (AccessPointDbHelper.DB_Lock) {
			try {
				AccessPointModel ap = null;
				SQLiteDatabase db = mDbHelper.getReadableDatabase();
				Cursor cursor = null;
				ap = new AccessPointModel();
				cursor = db.rawQuery("select * from "
						+ AccessPointTable.TBL_NAME + " where "
						+ AccessPointTable.FLD_BSSID + " = ?  ",
						new String[] { BSSID });
				if (cursor.moveToFirst()) {
					ap.updateByCursor(cursor);
					cursor.close();
					db.close();
					return ap;
				} else {
					return null;
				}

			} catch (Exception e) {
				return null;
			}

		}
	}

	public List<AccessPointModel> getLatestAPList(float longitude,
			float latitude, float filterDistance) {
		synchronized (AccessPointDbHelper.DB_Lock) {
			List<AccessPointModel> retList = new ArrayList<AccessPointModel>();
			SQLiteDatabase db = mDbHelper.getReadableDatabase();
			Cursor cursor = null;
			try {

				cursor = db.rawQuery("select * from "
						+ AccessPointTable.TBL_NAME + " where ("
						+ AccessPointTable.FLD_longitude + " > ? and "
						+ AccessPointTable.FLD_longitude + " < ?) and "
						+ "and  (" + AccessPointTable.FLD_latitude
						+ " > ? and " + AccessPointTable.FLD_latitude
						+ " < ?) ", new String[] {
						longitude - filterDistance + "",
						longitude + filterDistance + "",
						latitude - filterDistance + "",
						latitude + filterDistance + "" });
				while (cursor.moveToNext()) {
					AccessPointModel ap = new AccessPointModel(cursor);
					retList.add(ap);
				}

			} catch (Exception e) {
				HWFLog.e(TAG, "", e);
			} finally {
				cursor.close();
				db.close();
			}
			return retList;
		}
	}

	public List<AccessPointModel> getLocalAPList() {
		synchronized (AccessPointDbHelper.DB_Lock) {
			try {
				List<AccessPointModel> retList = new ArrayList<AccessPointModel>();
				SQLiteDatabase db = mDbHelper.getReadableDatabase();
				Cursor cursor = null;
				try {

					cursor = db
							.rawQuery(
									"select * from "
											+ AccessPointTable.TBL_NAME
											+ " where "
											+ AccessPointTable.FLD_passwordType
											+ "=? and "
											+ AccessPointTable.FLD_password
											+ "!=?",
									new String[] {
											AccessPointModel.PasswordSource.PasswordSourceLocal
													.ordinal() + "", "" });
					while (cursor.moveToNext()) {
						AccessPointModel ap = new AccessPointModel(cursor);
						retList.add(ap);
					}

				} catch (Exception e) {
					HWFLog.e(TAG, "", e);
				} finally {
					cursor.close();
					db.close();
				}
				return retList;
			} catch (Exception e) {
				return null;
			}

		}
	}

	public boolean hasAccessPoint(String BSSID) {
		synchronized (AccessPointDbHelper.DB_Lock) {
			try {
				SQLiteDatabase db = mDbHelper.getReadableDatabase();
				int count = db.rawQuery(
						"select * from " + AccessPointTable.TBL_NAME
								+ " where " + AccessPointTable.FLD_BSSID
								+ " = ? ", new String[] { BSSID }).getCount();
				db.close();
				if (count > 0) {
					return true;
				}
				return false;
			} catch (Exception e) {
				return false;
			}

		}
	}

	public List<AccessPointModel> findAccessPointModels(List<AccessPoint> list) {
		synchronized (AccessPointDbHelper.DB_Lock) {

			if (list == null) {
				return null;
			} else {
				try {
					String sqlString = "select * from "
							+ AccessPointTable.TBL_NAME + " where "
							+ AccessPointTable.FLD_BSSID + " in (?)";
					SQLiteDatabase db = mDbHelper.getReadableDatabase();
					HWFLog.d(TAG, sqlString);
					HWFLog.d(TAG, joinedByCommaForBSSIDInList(list));
					Cursor cursor = db.rawQuery(sqlString,
							new String[] { joinedByCommaForBSSIDInList(list) });
					if (cursor.getCount() == 0) {
						return null;
					}
					List<AccessPointModel> retList = new ArrayList<AccessPointModel>();
					while (cursor.moveToNext()) {
						retList.add(new AccessPointModel(cursor));
					}
					return retList;
				} catch (Exception e) {
					return null;
				}

			}
		}
	}

	public String joinedByCommaForBSSIDInList(List<AccessPoint> list) {
		if (list == null || list.size() == 0) {
			return "";
		}
		StringBuilder builder = new StringBuilder();
		for (int i = 0; i < list.size(); i++) {
			builder.append(list.get(i).getScanResult().BSSID + ",");
		}
		return builder.toString().substring(0, builder.toString().length() - 1);
	}

	public List<AccessPointModel> getUnUnploadAPList(int numToGet) {
		synchronized (AccessPointDbHelper.DB_Lock) {
			try {
				List<AccessPointModel> retList = new ArrayList<AccessPointModel>();
				SQLiteDatabase db = mDbHelper.getReadableDatabase();
				Cursor cursor = null;
				try {
					cursor = db.query(AccessPointTable.TBL_NAME, null,
							AccessPointTable.FLD_syncUpTime + "=?",
							new String[] { "0" }, null, null,
							AccessPointTable.FLD_updateTime + " desc", ""
									+ numToGet);
					while (cursor.moveToNext()) {
						AccessPointModel ap = new AccessPointModel(cursor);
						retList.add(ap);
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
			} catch (Exception e) {
				return null;
			}

		}
	}

	@Override
	public void onCreate(SQLiteDatabase db) {
		db.execSQL(AccessPointTable.CREATESQL_STRING);
	}

	@Override
	public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
		try {
			if (oldVersion < 2) {
				db.beginTransaction();
				db.execSQL("alter table " + AccessPointTable.TBL_NAME + " add "
						+ AccessPointTable.FLD_isPortal + " int");
				db.setTransactionSuccessful();
			}
		} catch (Exception e) {
			throw new RuntimeException(
					"Database upgrade error! Please contact the support or developer.",
					e);
		} finally {
			db.endTransaction();
		}
	}

	@Override
	public void onDowngrade(SQLiteDatabase db, int oldVersion, int newVersion) {
		// TODO Auto-generated method stub

	}

}
