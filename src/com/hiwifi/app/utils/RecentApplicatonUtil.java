package com.hiwifi.app.utils;

import java.io.File;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

import android.app.ActivityManager;
import android.app.ActivityManager.RecentTaskInfo;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.PackageManager.NameNotFoundException;
import android.content.pm.ResolveInfo;
import android.database.Cursor;
import android.net.Uri;
import android.text.TextUtils;
import android.util.Log;

import com.hiwifi.downloadproviders.DownloadManager;
import com.hiwifi.hiwifi.Gl;
import com.hiwifi.model.log.HWFLog;
import com.hiwifi.store.AppInfoModel;
import com.hiwifi.store.RecentApplicationDbMgr;
import com.hiwifi.utils.Blacklist;

public class RecentApplicatonUtil {
	private static final String TAG = "RecentApplicatonUtil";
	private static int rencentAppCount = 100;

	// private Context context;
	//
	// private ActivityManager mActivityManager;
	// public RecentApplicatonUtil(Context context) {
	// this.context = context;
	// }

	/**
	 * get recent application info
	 * 
	 * @param context
	 * @return
	 */
	public static ArrayList<AppInfoModel> getRecentApplication(Context context) {
		ArrayList<AppInfoModel> receArrayList = new ArrayList<AppInfoModel>();
		PackageManager packageManager = context.getPackageManager();
		ActivityManager mActivityManager = (ActivityManager) context
				.getSystemService(Context.ACTIVITY_SERVICE);
		List<RecentTaskInfo> appList = mActivityManager.getRecentTasks(
				rencentAppCount, ActivityManager.RECENT_WITH_EXCLUDED);// 参数，前一个是你要取的最大数，后一个是状态

		if (appList.size() > 0) {
			for (int i = appList.size() - 1; i >= 0; i--) {
				Intent intent = appList.get(i).baseIntent;
				ResolveInfo resolveInfo = packageManager.resolveActivity(
						intent, 0);
				if (resolveInfo != null) {
					if ((resolveInfo.activityInfo.applicationInfo.flags & ApplicationInfo.FLAG_SYSTEM) <= 0) {
						// 获取应用包名
						// System.out.println(resolveInfo.activityInfo.packageName);
						// 获取应用名
						// System.out.println(resolveInfo.loadLabel(packageManager).toString());
						// 获取应用头标
						// System.out.println(resolveInfo.loadIcon(pm) + "");
						String packageName = resolveInfo.activityInfo.packageName;
						Intent launchIntent = packageManager
								.getLaunchIntentForPackage(packageName);
						if (launchIntent != null
								&& !Blacklist.is_in_blacklist(packageName)) {
							AppInfoModel model = new AppInfoModel(resolveInfo
									.loadLabel(packageManager).toString(),
									resolveInfo.activityInfo.packageName);
							receArrayList.add(0, model);
							// RecentApplicationDbMgr.shareInstance().insertApp(
							// resolveInfo, packageManager);
						}
					}
				}
			}
		}
		// for (ActivityManager.RecentTaskInfo running : appList) {
		// Intent intent = running.baseIntent;
		// ResolveInfo resolveInfo = packageManager.resolveActivity(intent, 0);
		// if (resolveInfo != null) {
		// if ((resolveInfo.activityInfo.applicationInfo.flags &
		// ApplicationInfo.FLAG_SYSTEM) <= 0) {
		// // 获取应用包名
		// // System.out.println(resolveInfo.activityInfo.packageName);
		// // 获取应用名
		// // System.out.println(resolveInfo.loadLabel(pm).toString());
		// // 获取应用头标
		// // System.out.println(resolveInfo.loadIcon(pm) + "");
		// String packageName = resolveInfo.activityInfo.packageName;
		// Intent launchIntent = packageManager
		// .getLaunchIntentForPackage(packageName);
		// if (launchIntent != null
		// && !Blacklist.is_in_blacklist(packageName)) {
		// // receArrayList.add(resolveInfo);
		// AccessPointDbMgr.shareInstance().insertApp(resolveInfo,
		// packageManager);
		// }
		// }
		// }
		// }

		// receArrayList = RecentApplicationDbMgr.shareInstance().queryApp();

		// TODO syn database
		// if(receArrayList !=null && receArrayList.size()>0){
		// JSONObject jsonObject = new JsonRecentBuilder().build(receArrayList);
		// MobileInfoDbMgr.shareInstance().insert(jsonObject,
		// CacheConfigure.Recent);
		// }
		return receArrayList;
	}

	/**
	 * launcher application by packagename
	 * 
	 * @param packageName
	 * @param context
	 */
	public static void startAppByPackageName(String packageName, Context context) {
		// PackageManager packageManager = context.getPackageManager();
		// Intent intent = new Intent();
		// try {
		// intent = packageManager.getLaunchIntentForPackage(packageName);
		// } catch (Exception e) {
		// // TODO: handle exception
		// }
		// context.startActivity(intent);
		PackageInfo pi = null;
		try {
			pi = context.getPackageManager().getPackageInfo(packageName, 0);
		} catch (NameNotFoundException e) {
			e.printStackTrace();
			return;
		}

		// Intent resolveIntent = new Intent(Intent.ACTION_MAIN, null);
		// resolveIntent.addCategory(Intent.CATEGORY_LAUNCHER);
		// resolveIntent.setPackage(pi.packageName);
		//
		// List<ResolveInfo> apps = context.getPackageManager()
		// .queryIntentActivities(resolveIntent, 0);
		//
		// ResolveInfo ri = (apps.iterator()!=null &&
		// apps.iterator().hasNext())?apps.iterator().next():null;
		// if (ri != null) {
		// String packageName1 = ri.activityInfo.packageName;
		// String className = ri.activityInfo.name;
		//
		// Intent intent = new Intent();
		// intent.setAction(Intent.ACTION_MAIN);
		// intent.addCategory(Intent.CATEGORY_LAUNCHER);
		//
		// ComponentName cn = new ComponentName(packageName1, className);
		//
		// intent.setComponent(cn);
		// context.startActivity(intent);
		// }

		Intent intent = new Intent();
		intent = context.getPackageManager().getLaunchIntentForPackage(
				packageName);
		intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK
				| Intent.FLAG_ACTIVITY_CLEAR_TOP);
		context.startActivity(intent);
	}

	/**
	 * 判断应用是否已安装
	 * 
	 * @param packageName
	 * @return
	 */

	public static boolean judgePackageExist(Context context, String packageName) {
		boolean isAvaliable = false;
		PackageInfo packageInfo;
		try {
			packageInfo = context.getPackageManager().getPackageInfo(
					packageName, 0);
		} catch (NameNotFoundException e) {
			packageInfo = null;
			e.printStackTrace();
		}
		if (packageInfo == null) {
			isAvaliable = false;
		} else {
			isAvaliable = true;
		}
		return isAvaliable;
	}

	/**
	 * 获得应用程序的启动次数
	 * 
	 * @param context
	 * @param packageName
	 * @return
	 */

	public static long isThisAppUsed(Context context, String packageName) {
		long aUseTime = 0;
		int aLaunchCount = 0;
		PackageManager pm = context.getPackageManager();
		Intent i = pm.getLaunchIntentForPackage(packageName);
		ComponentName aName = i.getComponent();
		Log.i("TAG", aName.toString());
		try {
			// ComponentName aName = new ComponentName(packageName, getActivity(
			// context, packageName));
			// 获得ServiceManager类
			Class<?> ServiceManager = Class
					.forName("android.os.ServiceManager");
			// 获得ServiceManager的getService方法
			Method getService = ServiceManager.getMethod("getService",
					java.lang.String.class);
			// 调用getService获取RemoteService
			Object oRemoteService = getService.invoke(null, "usagestats");
			// 获得IUsageStats.Stub类
			Class<?> cStub = Class
					.forName("com.android.internal.app.IUsageStats$Stub");
			// 获得asInterface方法
			Method asInterface = cStub.getMethod("asInterface",
					android.os.IBinder.class);
			// 调用asInterface方法获取IUsageStats对象
			Object oIUsageStats = asInterface.invoke(null, oRemoteService);
			// 获得getPkgUsageStats(ComponentName)方法
			Method getPkgUsageStats = oIUsageStats.getClass().getMethod(
					"getPkgUsageStats", ComponentName.class);
			// 调用getPkgUsageStats 获取PkgUsageStats对象
			Object aStats = getPkgUsageStats.invoke(oIUsageStats, aName);
			// 获得PkgUsageStats类
			Class<?> PkgUsageStats = Class
					.forName("com.android.internal.os.PkgUsageStats");
			aLaunchCount = PkgUsageStats.getDeclaredField("launchCount")
					.getInt(aStats);
			aUseTime = PkgUsageStats.getDeclaredField("usageTime").getLong(

			aStats);
		} catch (Exception e) {

			Log.e("###", e.toString(), e);
			// e.printStackTrace();

		}

		// return aUseTime;

		return aLaunchCount;

	}

	public static ArrayList<AppInfoModel> getAllInstallApplications() {
		ArrayList<AppInfoModel> appList = new ArrayList<AppInfoModel>(); // 用来存储获取的应用信息数据

		List<PackageInfo> packages = Gl.Ct().getPackageManager()
				.getInstalledPackages(0);

		for (int i = 0; i < packages.size(); i++) {
			PackageInfo packageInfo = packages.get(i);
			AppInfoModel tmpInfo = new AppInfoModel();
			tmpInfo.setAppname(packageInfo.applicationInfo.loadLabel(
					Gl.Ct().getPackageManager()).toString());
			tmpInfo.setPackgename(packageInfo.packageName);
			// tmpInfo.setStart_count(start_count);
			// Only display the non-system app info
			if ((packageInfo.applicationInfo.flags & ApplicationInfo.FLAG_SYSTEM) == 0) {
				appList.add(tmpInfo);// 如果非系统应用，则添加至appList
			}

		}
		return appList;

	}

	public static ArrayList<AppInfoModel> getRecentUsedApp(Context context) {
		ArrayList<AppInfoModel> recent = getRecentApplication(context);// 最近使用的列表
		CopyOnWriteArrayList<AppInfoModel> receArrayList = RecentApplicationDbMgr
				.shareInstance().queryAll();// 所有的App
		if (receArrayList != null && receArrayList.size() > 0 && recent != null) {
			for (int i = 0; i < recent.size(); i++) {
				for (int j = 0; j < receArrayList.size(); j++) {
					if (receArrayList.get(j).getPackgename()
							.equals(recent.get(i).getPackgename())) {
						receArrayList.remove(j);
						break;
					}
				}
			}
		}
		final ArrayList<AppInfoModel> newRecent = new ArrayList<AppInfoModel>(
				recent);
		newRecent.addAll(receArrayList);
		new Thread(new Runnable() {

			@Override
			public void run() {
				RecentApplicationDbMgr.shareInstance().clearTable();
				RecentApplicationDbMgr.shareInstance().insertApp(newRecent);

			}
		}).start();

		return newRecent;
	}

	/*
	 * public static void downLoadApk(Context context, String url) {
	 * ToastUtil.showMessage(context, "开始下载"); DownloadManager downloadManager =
	 * (DownloadManager) context .getSystemService(Context.DOWNLOAD_SERVICE);
	 * 
	 * Uri uri = Uri.parse(url); Request request = new Request(uri);
	 * 
	 * // 设置允许使用的网络类型，这里是移动网络和wifi都可以
	 * request.setAllowedNetworkTypes(DownloadManager.Request.NETWORK_MOBILE |
	 * DownloadManager.Request.NETWORK_WIFI);
	 * 
	 * // 禁止发出通知，既后台下载，如果要使用这一句必须声明一个权限：android.permission.
	 * DOWNLOAD_WITHOUT_NOTIFICATION //
	 * request.setShowRunningNotification(true); //
	 * request.setMimeType("application/vnd.android.package-archive");
	 * 
	 * // 不显示下载界面 request.setVisibleInDownloadsUi(true);
	 * request.setNotificationVisibility
	 * (DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED); //
	 * request.setDestinationInExternalFilesDir(this, null, "tar.apk"); long id
	 * = downloadManager.enqueue(request); // TODO
	 * 把id保存好，在接收者里面要用，最好保存在Preferences里面 }
	 */

	/**
	 * install app
	 * 
	 * @param context
	 * @param filePath
	 * @return whether apk exist
	 */
	public static boolean install(Context context, String filePath) {
		Intent i = new Intent(Intent.ACTION_VIEW);
		File file = new File(filePath);
		if (file != null && file.length() > 0 && file.exists() && file.isFile()) {
			i.setDataAndType(Uri.parse("file://" + filePath),
					"application/vnd.android.package-archive");
			i.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
			context.startActivity(i);
			return true;
		}
		return false;
	}

	public static void downLoadApk2(Context ctx, String url) {
		ToastUtil.showMessage(ctx, "开始下载");
		DownloadManager manager = (DownloadManager) ctx
				.getSystemService(Context.DOWNLOAD_SERVICE); // 初始化下载管理器

		DownloadManager.Request request = new DownloadManager.Request(
				Uri.parse(url));// 创建请求

		// 设置允许使用的网络类型，这里是移动网络和wifi都可以
		request.setAllowedNetworkTypes(DownloadManager.Request.NETWORK_MOBILE
				| DownloadManager.Request.NETWORK_WIFI);

		// 禁止发出通知，既后台下载
		request.setShowRunningNotification(true);

		// 显示下载界面
		request.setVisibleInDownloadsUi(true);
		request.setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED);
		String[] names = TextUtils.split(url, "/");
		request.setDestinationInExternalFilesDir(ctx, null,
				names[names.length - 1]);// 设置下载后文件存放的位置--如果目标位置已经存在这个文件名，则不执行下载。
		request.setMimeType("application/vnd.android.package-archive");
		manager.enqueue(request);// 将下载请求放入队列
	}

	public static Boolean hasStartDownload(Context ctx, String url) {
		DownloadManager downloadManager = new DownloadManager(
				ctx.getContentResolver(), ctx.getPackageName());
		DownloadManager.Query query = new DownloadManager.Query();
		query.setFilterByStatus(~DownloadManager.STATUS_FAILED);// 设置过滤状态：成功
		Cursor c = downloadManager.query(query);
		while (c.moveToNext()) {
			String uriString = c.getString(c
					.getColumnIndex(DownloadManager.COLUMN_URI));
			if (uriString.equalsIgnoreCase(url)) {
				HWFLog.e(
						TAG,
						c.getInt(c
								.getColumnIndexOrThrow(DownloadManager.COLUMN_STATUS))
								+ "");
				HWFLog.e(
						TAG,
						c.getString(c
								.getColumnIndexOrThrow(DownloadManager.COLUMN_LOCAL_URI))
								+ "");
				try {
					if (c.getInt(c
							.getColumnIndexOrThrow(DownloadManager.COLUMN_STATUS)) == DownloadManager.STATUS_PAUSED) {
						downloadManager
								.resumeDownload(c
										.getColumnIndexOrThrow(DownloadManager.COLUMN_ID));
					}
				} catch (Exception e) {
					e.printStackTrace();
				}

				return true;
			}
		}
		return false;
	}

	public static String hasDownloaded(Context ctx, String url) {
		DownloadManager downloadManager = new DownloadManager(
				ctx.getContentResolver(), ctx.getPackageName());

		DownloadManager.Query query = new DownloadManager.Query();
		query.setFilterByStatus(DownloadManager.STATUS_SUCCESSFUL);// 设置过滤状态：成功
		Cursor c = downloadManager.query(query);
		while (c.moveToNext()) {
			String uriString = c.getString(c
					.getColumnIndex(DownloadManager.COLUMN_URI));
			HWFLog.e(TAG, uriString);
			if (uriString.equalsIgnoreCase(url)) {
				File f = new File(c.getString(
						c.getColumnIndex(DownloadManager.COLUMN_LOCAL_URI))
						.replace("file://", ""));// 过滤路径
				return f.exists() ? f.getAbsolutePath() : null;
			}
		}
		return null;
	}

	public static void downLoadApk3(Context ctx, String url, String title) {
		DownloadManager downloadManager = new DownloadManager(
				ctx.getContentResolver(), ctx.getPackageName());
		try {
			DownloadManager.Request request = new DownloadManager.Request(
					Uri.parse(url));
			request.setAllowedNetworkTypes(DownloadManager.Request.NETWORK_MOBILE
					| DownloadManager.Request.NETWORK_WIFI);
			request.setShowRunningNotification(true);
			request.setVisibleInDownloadsUi(true);
			request.setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED);
			request.setTitle(title);
			// request.setDestinationInExternalPublicDir(
			// Environment.DIRECTORY_DOWNLOADS, null);
			request.setMimeType("application/vnd.android.package-archive");
			downloadManager.enqueue(request);// 将下载请求放入队列
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
}
