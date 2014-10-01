package com.hiwifi.app.receiver;

import java.io.File;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.widget.Toast;

import com.hiwifi.app.utils.RecentApplicatonUtil;
import com.hiwifi.downloadproviders.DownloadManager;
import com.hiwifi.downloadproviders.DownloadManager.Query;
import com.hiwifi.model.log.LogUtil;

public class CompleteReceiver extends BroadcastReceiver {

	@Override
	public void onReceive(Context context, Intent intent) {

		String action = intent.getAction();
		if (action.equals(DownloadManager.ACTION_DOWNLOAD_COMPLETE)) {
			Toast.makeText(context, "下载完成了!", Toast.LENGTH_LONG).show();
			long id = intent.getLongExtra(DownloadManager.EXTRA_DOWNLOAD_ID, 0); // TODO
			Query query = new Query();
			query.setFilterById(id);
			DownloadManager downloadManager = new DownloadManager(
					context.getContentResolver(), context.getPackageName());
			Cursor cursor = downloadManager.query(query);
			if (cursor!=null && cursor.moveToFirst()) {// 移动到最新下载的文件
				String fileName = cursor.getString(cursor
						.getColumnIndex(DownloadManager.COLUMN_LOCAL_URI));
				LogUtil.d("column:", fileName);
				System.out.println("======文件名称=====" + fileName);
				File f = new File(fileName.replace("file://", ""));// 过滤路径
				cursor.close();
				RecentApplicatonUtil.install(context, f.getAbsolutePath());//
				// 开始安装apk
			}
		} else if (action.equals(DownloadManager.ACTION_NOTIFICATION_CLICKED)) {
			Toast.makeText(context, "点击下载条", Toast.LENGTH_LONG).show();
		}
	}
}
