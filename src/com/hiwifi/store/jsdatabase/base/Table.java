package com.hiwifi.store.jsdatabase.base;

import android.database.sqlite.SQLiteDatabase;

/**
 * 实现这个接口的类可以被统一管理
 * 
 */
public interface Table {
	/**
	 * 创建数据库的代码
	 */
	void onCreate(SQLiteDatabase db);

	/**
	 * 升级数据库的代码
	 * */
	void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion);

	/**
	 * 降级数据库
	 * */
	public void onDowngrade(SQLiteDatabase db, int oldVersion, int newVersion);
}