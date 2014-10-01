package com.hiwifi.store;

import java.util.ArrayList;
import java.util.List;

import com.hiwifi.store.jsdatabase.base.Table;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

public class AccessPointDbHelper extends SQLiteOpenHelper {

	private static final String DB_NAME = "AccessPoints.db";
	//1.0~1.03 是1
	//2.0开始是2
	private static final int DB_VERSION = 2;
	public final static byte[] DB_Lock = new byte[0];
	private List<Table> tableList;

	public List<Table> getTables() {
		if (tableList == null) {
			tableList = new ArrayList<Table>();
			tableList.add(AccessPointDbMgr.shareInstance());
			tableList.add(AccountDbMgr.shareInstance());
			tableList.add(RequestDbMgr.shareInstance());
			tableList.add(RecentApplicationDbMgr.shareInstance());
//			tableList.add(MobileInfoDbMgr.shareInstance());
			tableList.add(KeyValueDbMgr.shareInstance());
		}
		return tableList;
	}
	public AccessPointDbHelper(Context context) {
		super(context, DB_NAME, null, DB_VERSION);
	}

	@Override
	public void onCreate(SQLiteDatabase db) {
		// db.beginTransaction();
		for (Table table : getTables()) {
			table.onCreate(db);
		}
		// db.endTransaction();
	}

	@Override
	public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
		for (Table table : getTables()) {
			table.onUpgrade(db, oldVersion, newVersion);
		}
	}

	@Override
	public void onDowngrade(SQLiteDatabase db, int oldVersion, int newVersion) {
		for (Table table : getTables()) {
			table.onDowngrade(db, oldVersion, newVersion);
		}
	}

}
