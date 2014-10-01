package com.hiwifi.store.jsdatabase.base;

import java.util.List;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

/**
 * 数据库的管理,对数据库进行创建和升级。扩展的时候，将需要创建的表用覆盖getTables的方式传入
 * 
 */
public abstract class AbstractDatabaseHelper extends SQLiteOpenHelper {
	public static final String DB_NAME = "JavaScript.db";
	public static final int VERSION = 1;
	private final List<Table> tableList;
	public final static byte[] _writeLock = new byte[0];

	/**
	 * 获取需要加入到数据库中的表的实例
	 * */
	protected abstract List<Table> getTables();

	/**
	 * @param tableList
	 *            需要创建的表的列表
	 * */
	public AbstractDatabaseHelper(Context context) {
		super(context, DB_NAME, null, VERSION);
		this.tableList = getTables();
	}

	@Override
	public final void onCreate(SQLiteDatabase db) {
		for (Table table : tableList) {
			table.onCreate(db);
		}
	}

	@Override
	public final void onUpgrade(SQLiteDatabase db, int oldVersion,
			int newVersion) {
		for (Table table : tableList) {
			table.onUpgrade(db, oldVersion, newVersion);
		}
	}

}