package com.seo.store.jsdatabase;

import java.util.ArrayList;
import java.util.List;

import com.seo.store.jsdatabase.base.AbstractDatabaseHelper;
import com.seo.store.jsdatabase.base.Table;
import com.seo.store.jsdatabase.tables.DownloadList;
import com.seo.store.jsdatabase.tables.JavaScriptMapTable;
import com.seo.store.jsdatabase.tables.JavaScriptTable;

import android.content.Context;

/**
 * 添加表的代码写在这里
 * 
 */
public class DatabaseHelper extends AbstractDatabaseHelper {
	private static List<Table> tables = new ArrayList<Table>();
	/**
	 * 将添加表的代码写在这里
	 * */
	static {
		// @ table 列表
		tables.add(new JavaScriptTable());
		tables.add(new JavaScriptMapTable());
		tables.add(new DownloadList());
	}

	public DatabaseHelper(Context context) {
		super(context);
	}

	@Override
	protected List<Table> getTables() {
		return tables;
	}

}