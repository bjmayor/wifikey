package com.hiwifi.app.receiver;

import java.util.List;

import org.json.JSONObject;

import android.app.ActivityManager;
import android.app.ActivityManager.RunningTaskInfo;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.text.TextUtils;
import android.util.Log;

import com.hiwifi.activity.CommonWebviewActivity;
import com.hiwifi.app.views.CustomDialog;
import com.hiwifi.hiwifi.R;
import com.hiwifi.support.http.AsyncHttpClient;
import com.hiwifi.utils.ApplicationUtil;
import com.igexin.sdk.PushConsts;

public class GeTuiBroadcastReceiver extends BroadcastReceiver {

	private Context cont;
	private SharedPreferences loginData;
	private String title;
	private String url;
	private String content;
	private String action;
	private String push_data;
	private int rid;
	private Intent notificationIntent;
	private String[] mac;
	private AsyncHttpClient asyncHttpClient;
	private Handler handler = new Handler(Looper.getMainLooper()) {
		public void handleMessage(android.os.Message msg) {

			// AlertDialog.Builder builder = new AlertDialog.Builder(cont,
			// R.style.BasicRequestDialog);
			// builder.setMessage(push_data+"")
			// .setPositiveButton("是", new DialogInterface.OnClickListener() {
			// @Override
			// public void onClick(DialogInterface dialog, int which) {
			//
			// }
			// }).setNegativeButton("否", new OnClickListener() {
			// @Override
			// public void onClick(DialogInterface dialog, int which) {
			// }
			// });
			// // builder.setView(view);
			// AlertDialog ad = builder.create();
			// //
			// ad.getWindow().setType(WindowManager.LayoutParams.TYPE_SYSTEM_DIALOG);
			// //系统中关机对话框就是这个属性
			// ad.getWindow().setType(WindowManager.LayoutParams.TYPE_SYSTEM_ALERT);
			// ad.setCanceledOnTouchOutside(false); //点击外面区域不会让dialog消失
			// ad.show();
			new CustomDialog.Builder(cont)
					.setMessage(push_data + "")
					.setNegativeButton("取消",
							new DialogInterface.OnClickListener() {

								@Override
								public void onClick(DialogInterface dialog,
										int which) {

								}
							})
					.setPositiveButton("查看",
							new DialogInterface.OnClickListener() {

								@Override
								public void onClick(DialogInterface dialog,
										int which) {
									Intent notifi = new Intent(cont
											.getApplicationContext(),
											CommonWebviewActivity.class);
									notifi.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
									notifi.putExtra("type", "push");
									notifi.putExtra("title", title);
									notifi.putExtra("url", url);
									cont.startActivity(notifi);
								}
							}).show();

		};
	};

	@Override
	public void onReceive(Context context, Intent intent) {
		this.cont = context;
		Bundle bundle = intent.getExtras();
		Log.d("GetuiSdkDemo", "onReceive() action=" + bundle.getInt("action"));
		switch (bundle.getInt(PushConsts.CMD_ACTION)) {
		case PushConsts.GET_MSG_DATA:
			// 获取透传数据
			// String appid = bundle.getString("appid");
			byte[] payload = bundle.getByteArray("payload");

			if (payload != null) {
				String data = new String(payload);
				try {
					JSONObject jsonObject = new JSONObject(data);
					if (jsonObject != null) {
						url = jsonObject.isNull("url") ? "" : jsonObject
								.getString("url");
						title = jsonObject.isNull("msg_title") ? ""
								: jsonObject.getString("msg_title");
						content = jsonObject.isNull("msg_content") ? ""
								: jsonObject.getString("msg_content");
						action = jsonObject.isNull("action") ? "" : jsonObject
								.getString("action");
						push_data = jsonObject.isNull("data") ? "" : jsonObject
								.getString("data");
					}

					if (!TextUtils.isEmpty(title)
							&& !TextUtils.isEmpty(content)
							&& !TextUtils.isEmpty(url)) {
						if (ApplicationUtil.isRunningForeground(context)) {
							handler.sendEmptyMessage(0);
						} else {
							showNotification();
						}
					}
				} catch (Exception e) {
					e.printStackTrace();
				}

				String taskid = bundle.getString("taskid");
				String messageid = bundle.getString("messageid");
			}
			break;
		case PushConsts.GET_CLIENTID:
			// 获取ClientID(CID)
			// 第三方应用需要将CID上传到第三方服务器，并且将当前用户帐号和CID进行关联，以便日后通过用户帐号查找CID进行消息推送
			String cid = bundle.getString("clientid");
			break;
		case PushConsts.THIRDPART_FEEDBACK:
			/*
			 * String appid = bundle.getString("appid"); String taskid =
			 * bundle.getString("taskid"); String actionid =
			 * bundle.getString("actionid"); String result =
			 * bundle.getString("result"); long timestamp =
			 * bundle.getLong("timestamp");
			 * 
			 * Log.d("GetuiSdkDemo", "appid = " + appid); Log.d("GetuiSdkDemo",
			 * "taskid = " + taskid); Log.d("GetuiSdkDemo", "actionid = " +
			 * actionid); Log.d("GetuiSdkDemo", "result = " + result);
			 * Log.d("GetuiSdkDemo", "timestamp = " + timestamp);
			 */
			break;
		default:
			break;
		}

	}

	@SuppressWarnings("deprecation")
	private void showNotification() {

		// 创建一个NotificationManager的引用
		NotificationManager notificationManager = (NotificationManager) cont
				.getSystemService(android.content.Context.NOTIFICATION_SERVICE);

		// 定义Notification的各种属性
		Notification notification = new Notification();
		notification.icon = R.drawable.hiwifi_launcher;
		notification.tickerText = "极路由";
		// notification.largeIcon = BitmapFactory.decodeResource(
		// cont.getResources(), R.drawable.ic_launcher);
		if (url.startsWith("http://")) {
			notificationIntent = new Intent(cont, CommonWebviewActivity.class);
			notificationIntent.putExtra("type", "push");
			notificationIntent.putExtra("title", title);
			notificationIntent.putExtra("url", url);
		} else if (url.startsWith("hiwifi://")) {

		}
		PendingIntent contentItent = null;
		contentItent = PendingIntent.getActivity(cont, 0, notificationIntent,
				PendingIntent.FLAG_UPDATE_CURRENT);
		notification.flags |= Notification.FLAG_AUTO_CANCEL;
		notification.defaults = Notification.DEFAULT_SOUND
				| Notification.DEFAULT_VIBRATE;
		CharSequence contentTitle = title; // 通知栏标题
		CharSequence contentText = content; // 通知栏内容
		notification.setLatestEventInfo(cont, contentTitle, contentText,
				contentItent);
		notificationManager.notify(0, notification);
	}

	/**
	 * 判断指定activity是否在任务栈的最前端 需要权限：android.permission.GET_TASKS
	 * 
	 * @param context
	 * @param activityName
	 * @return
	 */
	public boolean isTopActivity(Context context, String activityName) {
		ActivityManager am = (ActivityManager) context
				.getSystemService(Context.ACTIVITY_SERVICE);
		List<RunningTaskInfo> runningTasks = am.getRunningTasks(100);
		for (RunningTaskInfo task : runningTasks) {
			if (task.topActivity.getClassName().equals(activityName))
				return true;
		}
		return false;
	}

	// 删除通知
	private void clearNotification() {
		// 启动后删除之前我们定义的通知
		NotificationManager notificationManager = (NotificationManager) cont
				.getSystemService(android.content.Context.NOTIFICATION_SERVICE);
		notificationManager.cancel(0);
	}

	protected boolean isTopActivity(Context context) {

		String packageName = context.getPackageName();

		ActivityManager activityManager = (ActivityManager) context
				.getSystemService(Context.ACTIVITY_SERVICE);

		List<RunningTaskInfo> tasksInfo = activityManager.getRunningTasks(1);

		if (tasksInfo.size() > 0) {
			// 应用程序不在堆栈的顶层
			if (packageName.equals(tasksInfo.get(0).topActivity
					.getPackageName())) {
				SharedPreferences loginData = context.getSharedPreferences(
						"loginData", 0);
				if (Boolean.valueOf(loginData.getBoolean("setting_push", true))) {
					return true;
				}

			}
		}
		return false;

	}

}
