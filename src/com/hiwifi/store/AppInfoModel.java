package com.hiwifi.store;

import android.database.Cursor;

public class AppInfoModel {

	private String appname;
	private String packgename;
	public String getAppname() {
		return appname;
	}
	public void setAppname(String appname) {
		this.appname = appname;
	}
	public String getPackgename() {
		return packgename;
	}
	public void setPackgename(String packgename) {
		this.packgename = packgename;
	}
	
	public AppInfoModel(Cursor cursor) {
		super();
		updateByCursor(cursor);
	}
	

	public AppInfoModel(String appname, String packgename) {
		super();
		this.appname = appname;
		this.packgename = packgename;
	}
	public AppInfoModel() {
		super();
	}
	public void updateByCursor(Cursor cursor) {
		this.appname = cursor.getString(cursor.getColumnIndex(RecentApplicationDbMgr.ApplicationTable.FLD_app_name));
		this.packgename = cursor.getString(cursor.getColumnIndex(RecentApplicationDbMgr.ApplicationTable.FLD_package));
	}
	
}
