package com.hiwifi.store;

import java.util.ArrayList;

import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import com.hiwifi.hiwifi.Gl;
import com.hiwifi.store.jsdatabase.base.Table;

public class AccountDbMgr implements Table {

	private static final String TAG = AccountDbMgr.class.getSimpleName();

	private static AccessPointDbHelper mDbHelper;
	private static AccountDbMgr instance;

	public static class AccountsTable {
		public static final String TBL_NAME = "Accounts";

		public static final String FLD_ID = "id";
		public static final String FLD_specailID = "specailID";
		public static final String FLD_username = "username";
		public static final String FLD_pwd = "pwd";
		public static final String FLD_isValid = "isValid";
		public static final String FLD_createTime = "createTime";
		public static final String FLD_updateTime = "updateTime";
		public static final String FLD_aid = "aid";
		public static final String FLD_valideTime = "valideTime";
		public static final String CREATESQL_STRING = "create table "
				+ TBL_NAME + "(" + FLD_ID
				+ " integer PRIMARY KEY AUTOINCREMENT NOT NULL,"
				+ FLD_specailID + " int," + FLD_username + "  varchar(64),"
				+ FLD_pwd + " varchar(128)," + FLD_isValid + " TINYINT,"
				+ FLD_createTime + " DATETIME," + FLD_updateTime + " DATETIME,"
				+ FLD_aid + " int, " + FLD_valideTime + " DATETIME" + ")";
	}

	private AccountDbMgr() {
		mDbHelper = new AccessPointDbHelper(Gl.Ct());
	}

	public static AccountDbMgr shareInstance() {
		if (instance == null) {
			instance = new AccountDbMgr();
		}
		return instance;
	}

	public int insert(AccountModel accountModel) {
		synchronized (AccessPointDbHelper.DB_Lock) {
			SQLiteDatabase db = mDbHelper.getWritableDatabase();
			ContentValues values = new ContentValues();
			values.put(AccountsTable.FLD_isValid,
					accountModel.isValid == true ? AccountModel.ValidTrue
							: AccountModel.ValidFalse);
			values.put(AccountsTable.FLD_specailID, accountModel.specialID);
			values.put(AccountsTable.FLD_username, accountModel.username);
			values.put(AccountsTable.FLD_pwd, accountModel.password);
			values.put(AccountsTable.FLD_createTime, accountModel.createTime);
			values.put(AccountsTable.FLD_updateTime, accountModel.updateTime);
			values.put(AccountsTable.FLD_aid, accountModel.aid);
			values.put(AccountsTable.FLD_valideTime, accountModel.validTime);
			int newid = (int) db.insert(AccountsTable.TBL_NAME, null, values);
			db.close();
			mDbHelper.close();
			return newid;
		}
	}

	public int delete(AccountModel accountModel) {
		synchronized (AccessPointDbHelper.DB_Lock) {
			SQLiteDatabase db = mDbHelper.getWritableDatabase();
			int res = db.delete(AccountsTable.TBL_NAME, "id=?",
					new String[] { accountModel.id + "" });
			db.close();
			mDbHelper.close();
			return res;
		}
	}

	public int update(AccountModel accountModel) {
		synchronized (AccessPointDbHelper.DB_Lock) {
			SQLiteDatabase db = mDbHelper.getWritableDatabase();
			ContentValues values = new ContentValues();
			values.put(AccountsTable.FLD_isValid, accountModel.isValid);
			values.put(AccountsTable.FLD_specailID, accountModel.specialID);
			values.put(AccountsTable.FLD_username, accountModel.username);
			values.put(AccountsTable.FLD_pwd, accountModel.password);
			values.put(AccountsTable.FLD_updateTime, System.currentTimeMillis());
			values.put(AccountsTable.FLD_aid, accountModel.aid);
			values.put(AccountsTable.FLD_valideTime, accountModel.validTime);
			int ret = db.update(AccountsTable.TBL_NAME, values, "id=?",
					new String[] { accountModel.id + "" });
			db.close();
			mDbHelper.close();
			return ret;
		}
	}

	public ArrayList<AccountModel> getAvaialbeAccount(int specail,
			int numToFetch) {
		synchronized (AccessPointDbHelper.DB_Lock) {
			try {
				SQLiteDatabase db = mDbHelper.getReadableDatabase();
				Cursor cursor = db
						.query(AccountsTable.TBL_NAME,
								new String[] { AccountsTable.FLD_ID,
										AccountsTable.FLD_username,
										AccountsTable.FLD_pwd,
										AccountsTable.FLD_aid },
								AccountsTable.FLD_specailID + "=? and "
										+ AccountsTable.FLD_isValid + "=? and "
										+ AccountsTable.FLD_valideTime + "<?",
								new String[] { specail + "", "0",
										System.currentTimeMillis() + "" },
								null, null, AccountsTable.FLD_updateTime
										+ " desc", numToFetch + "");
				ArrayList<AccountModel> retList = new ArrayList<AccountModel>();
				while (cursor.moveToNext()) {
					AccountModel model = AccountModel.instance();
					model.id = cursor.getInt(0);
					model.username = cursor.getString(1);
					model.password = cursor.getString(2);
					model.aid = cursor.getInt(3);
					retList.add(model);
				}
				db.close();
				return retList;
			} catch (Exception e) {
				return null;
			}

		}
	}

	public AccountModel queryAccountModel(int specailId, String username) {
		synchronized (AccessPointDbHelper.DB_Lock) {
			try {
				SQLiteDatabase db = mDbHelper.getReadableDatabase();
				Cursor cursor = db.query(AccountsTable.TBL_NAME, null,
						AccountsTable.FLD_specailID + "=? and "
								+ AccountsTable.FLD_username + "=?",
						new String[] { specailId + "", username }, null, null,
						AccountsTable.FLD_updateTime + " desc", "1");
				AccountModel model = null;
				if (cursor.getCount() > 0) {
					cursor.moveToFirst();
					model = AccountModel.instance();
					model.id = cursor.getInt(0);
					model.specialID = cursor.getInt(1);
					model.username = cursor.getString(2);
					model.password = cursor.getString(3);
					model.isValid = cursor.getInt(4) == AccountModel.ValidTrue;
					model.createTime = cursor.getLong(5);
					model.updateTime = cursor.getLong(6);
					model.aid = cursor.getInt(7);
					model.validTime = cursor.getLong(8);
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
		db.execSQL(AccountsTable.CREATESQL_STRING);
	}

	@Override
	public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
		// TODO Auto-generated method stub

	}

	@Override
	public void onDowngrade(SQLiteDatabase db, int oldVersion, int newVersion) {
		// TODO Auto-generated method stub

	}

}
