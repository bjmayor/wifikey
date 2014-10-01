package com.hiwifi.hiwifi;

import java.io.File;
import java.io.IOException;
import java.net.SocketException;
import java.net.UnknownHostException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import org.apache.http.NoHttpResponseException;
import org.json.JSONObject;
import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;

import android.annotation.SuppressLint;
import android.annotation.TargetApi;
import android.app.Activity;
import android.app.Application;
import android.content.Context;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.content.pm.PackageManager.NameNotFoundException;
import android.content.res.Resources.NotFoundException;
import android.content.res.XmlResourceParser;
import android.graphics.Bitmap;
import android.net.ConnectivityManager;
import android.net.wifi.WifiManager;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.text.TextUtils;

import com.hiwifi.app.receiver.CmccStateBroadcastReceiver;
import com.hiwifi.app.services.DaemonService;
import com.hiwifi.constant.ConfigConstant;
import com.hiwifi.constant.ReleaseConstant;
import com.hiwifi.constant.ReleaseConstant.DebugLevel;
import com.hiwifi.model.ClientInfo;
import com.hiwifi.model.log.CrashHandler;
import com.hiwifi.model.log.HWFLog;
import com.hiwifi.model.log.LogUtil;
import com.hiwifi.model.wifi.WifiAdmin;
import com.hiwifi.model.wifi.state.CommerceState;
import com.hiwifi.store.KeyValueModel;
import com.hiwifi.support.http.AsyncHttpClient;
import com.hiwifi.support.http.SyncHttpClient;
import com.hiwifi.utils.DeviceUtil;
import com.hiwifi.utils.ResUtil;
import com.nostra13.universalimageloader.cache.disc.naming.Md5FileNameGenerator;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.ImageLoaderConfiguration;
import com.umeng.analytics.MobclickAgent;

public final class Gl extends Application {
	private static final String TAG = "Gl";
	private static Context sContext;
	public static int versionCode = 0;
	public static String versionName = "";
	public static String AppName;
	private static WifiAdmin mAdmin;
	public static Bitmap userBitmap;

	public static Bitmap getTest() {
		return userBitmap;
	}

	public static void setTest(Bitmap test) {
		Gl.userBitmap = test;
	}

	private static Map<String, String> errorMap;

	public static Map<String, String> getErrorMap() {
		return errorMap;
	}

	@Override
	public void onCreate() {
		super.onCreate();
		HWFLog.d(TAG, "Gl onCreate");
		init(this.getApplicationContext());
		initErrorMap();
		DaemonService.execCommand(Ct(), DaemonService.actionType_init);
		addCmccStateListner();
		registerActivityLifecycleCallback();
		IntentFilter filter = new IntentFilter();
	}

	@Override
	public void onTerminate() {
		HWFLog.e(TAG, "onTerminate");
		super.onTerminate();
	}

	@Override
	public void onLowMemory() {
		HWFLog.e(TAG, "onLowMemory");
		super.onLowMemory();
	}

	@TargetApi(Build.VERSION_CODES.ICE_CREAM_SANDWICH)
	private void registerActivityLifecycleCallback() {
		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.ICE_CREAM_SANDWICH) {

			registerActivityLifecycleCallbacks(new ActivityLifecycleCallbacks() {

				@Override
				public void onActivityStopped(Activity activity) {

				}

				@Override
				public void onActivityStarted(Activity activity) {

				}

				@Override
				public void onActivitySaveInstanceState(Activity activity,
						Bundle outState) {

				}

				@Override
				public void onActivityResumed(Activity activity) {

				}

				@Override
				public void onActivityPaused(Activity activity) {

				}

				@Override
				public void onActivityDestroyed(Activity activity) {

				}

				@Override
				public void onActivityCreated(Activity activity,
						Bundle savedInstanceState) {

				}
			});
		}
	}

	/**
	 * 实例化ErrorMap
	 */
	private void initErrorMap() {
		try {
			// test
			XmlResourceParser pullParser = getResources().getXml(
					R.xml.exception);
			pullParser.getProperty("");
			// 取得事件
			int event = pullParser.getEventType();
			// 若为解析到末尾
			String key = "";
			String value = "";
			while (event != XmlPullParser.END_DOCUMENT) // 文档结束
			{
				// 节点名称
				String nodeName = pullParser.getName();

				switch (event) {
				case XmlPullParser.START_DOCUMENT: // 文档开始
					errorMap = new HashMap<String, String>();
					break;
				case XmlPullParser.START_TAG: // 标签开始
					if ("key".equals(nodeName)) {
						key = pullParser.nextText();
						value = "";
						LogUtil.d("key:", key);
					}
					if ("string".equals(nodeName)) {
						value = pullParser.nextText();
						LogUtil.d("value:", value);
					}
					if (!key.equals("") && !value.equals("")) {
						errorMap.put(key, value);
						key = "";
						value = "";
					}
					break;
				case XmlPullParser.END_TAG: // 标签结束
					break;
				}
				event = pullParser.next(); // 下一个标签
			}
		} catch (NotFoundException e) {
			e.printStackTrace();
		} catch (XmlPullParserException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	public void init(Context context) {
		sContext = context;
		mAdmin = WifiAdmin.sharedInstance();
		MobclickAgent.openActivityDurationTrack(false);
		if (Gl.isDevelopMode()) {
			Thread.setDefaultUncaughtExceptionHandler(CrashHandler
					.getInstance().init(sContext));
		} else {
			MobclickAgent.setCatchUncaughtExceptions(true);
		}
		try {
			versionCode = context.getPackageManager().getPackageInfo(
					context.getPackageName(), 0).versionCode;
			versionName = context.getPackageManager().getPackageInfo(
					context.getPackageName(), 0).versionName;
			AppName = context.getResources().getString(R.string.app_name);

			ImageLoaderConfiguration config = new ImageLoaderConfiguration.Builder(
					sContext)
					.threadPoolSize(3)
					.threadPriority(Thread.NORM_PRIORITY - 2)
					.memoryCacheSize(1500000)
					// 1.5 Mb
					.denyCacheImageMultipleSizesInMemory()
					.discCacheFileNameGenerator(new Md5FileNameGenerator())
					.enableLogging() // Not necessary in common
					.build();
			// Initialize ImageLoader with configuration.
			ImageLoader.getInstance().init(config);
		} catch (NameNotFoundException e) {
			e.printStackTrace();
		}
		prepareForFileCreate();
	}

	// 创建数据目录
	private void prepareForFileCreate() {
		String path[] = { ConfigConstant.SD_HOME_DIRECTORY,
				ConfigConstant.SD_CRASH_DIRECTORY,
				ConfigConstant.SD_DATA_DIRECTORY,
				ConfigConstant.SD_TMP_DIRECTORY };
		for (int i = 0; i < path.length; i++) {
			String fileDirectroy = "";
			if (Environment.MEDIA_MOUNTED.equals(Environment
					.getExternalStorageState())
					&& Environment.getExternalStorageDirectory().exists()) {
				fileDirectroy = Environment.getExternalStorageDirectory()
						.getAbsolutePath() + path[i];
			} else {
				fileDirectroy = Gl.Ct().getFilesDir().getAbsolutePath()
						+ path[i];
			}

			File file = new File(fileDirectroy);
			if (!file.exists()) {
				file.mkdirs();
			}
		}
	}

	public static final WifiManager wifiManager() {
		return (WifiManager) Ct().getSystemService(Context.WIFI_SERVICE);
	}

	public static final ConnectivityManager connectivityManager() {
		return (ConnectivityManager) Ct().getSystemService(
				Context.CONNECTIVITY_SERVICE);
	}

	/**
	 * 
	 * Ct
	 * 
	 * @description: get the application context, it can be used most where the
	 *               Context is needed.
	 * @return the application context (comes from
	 *         Application.getApplicationContext())
	 */
	public static Context Ct() {
		return sContext;
	}

	public static Boolean isDevelopMode() {
		return ReleaseConstant.debugLevel != DebugLevel.DebugLevelSDCard;
	}

	public static class TaskRecord {
		private static final String CONFNAME_STRING = "taskRecord";
		private static final SharedPreferences SP_PREFERENCES = sContext
				.getSharedPreferences(CONFNAME_STRING, Context.MODE_PRIVATE);

		public static SharedPreferences sp() {
			return SP_PREFERENCES;
		}

		public static Boolean isTaskExcutedToday(Class<?> cls) {
			return sp().getBoolean(keyOfTask(cls), false);
		}

		public static void setTaskExcutedToday(Class<?> cls, Boolean excuted) {
			Editor editor = sp().edit();
			editor.putBoolean(keyOfTask(cls), excuted);
			editor.commit();
		}

		@SuppressLint("SimpleDateFormat")
		private static String keyOfTask(Class<?> cls) {
			LogUtil.d("get:", cls.getName()
					+ new SimpleDateFormat("yyyy_MM_dd").format(new Date()));
			return cls.getName()
					+ new SimpleDateFormat("yyyy_MM_dd").format(new Date());
		}

	}

	public static class GlConf {
		private static final String CONFNAME_STRING = "apConf";
		private static final String CONF_KEY_SHOW_PASSWORD = "showPassword";
		private static final String CONF_KEY_REMEMBER_PASSWORD = "rememberPassword";
		private static final String mac_count = "mac_count";
		private static final String AP_KEY_forceUpGrade = "forceupgrade";
		private static final String AP_KEY_cmccIsVip = "cmccIsVip";
		private static final String AP_KEY_leftTime = "leftTime";
		private static final String AP_KEY_joinedPeoples = "joinedPeoples";
		private static final String AP_KEY_macAddress = "macAddress";
		private static final String AP_KEY_IMEI = "imei";
		private static final String COMMITTED_TIME = "time";
		private static final String AUTO_SHARE_PASS = "autosharepass";
		private static final String RECONMEND_APPS = "reconmend_apps";
		private static final String HIWIFIAD = "Advertisement";
		private static final String HIWIFIADURL = "Advertisementurl";
		private static final String RADAR_SOUND = "RadarSound";
		private static final String SPEED_TEST_URLS = "speed_test_urls";
		private static final String SPEED_TEST_TIMEOUT = "speed_test_timeout";
		private static final String AP_KEY_PING_URL = "ap_key_ping_url";
		private static final String AP_KEY_PING_EXPECTCONTENT = "ap_key_ping_expectcontent";
		public static final String DEFAULT_PING_URL = "http://m.baidu.com";
		public static final String DEFAULT_PING_EXPECTCONTENT = "030173";

		private static SharedPreferences spPreferences = null;

		// 考虑到两个进程都
		public static SharedPreferences sp() {
			if (spPreferences == null) {
				spPreferences = sContext.getSharedPreferences(CONFNAME_STRING,
						Context.MODE_PRIVATE);
			}
			return spPreferences;
		}

		public static int getSpeedTestTimeout() {
			return sp().getInt(SPEED_TEST_TIMEOUT, 5000);
		}

		public static void setSpeedTestTimeout(int timeout) {
			Editor editor = sp().edit();
			editor.putInt(SPEED_TEST_TIMEOUT, timeout);
			editor.apply();
		}

		public static void setSpeedTestUrls(ArrayList<String> urls) {
			String output = "";
			if (urls != null) {
				for (String i : urls) {
					if (i != null) {
						String t = i.trim();
						if (t.length() > 0) {
							if (output.length() > 0) {
								output += "\n" + t;
							} else {
								output = t;
							}
						}
					}
				}
				if (output.length() > 0) {
					Editor editor = sp().edit();
					editor.putString(SPEED_TEST_URLS, output);
					editor.apply();
				}
			}
		}

		public static String[] getSpeedTestUrls() {
			String ret = sp().getString(SPEED_TEST_URLS, null);
			if (ret != null) {
				return ret.split("\n");
			} else {
				return null;
			}
		}

		public static void setRadarSound(boolean sound) {
			Editor editor = sp().edit();
			editor.putBoolean(RADAR_SOUND, sound);
			editor.apply();
		}

		public static boolean getRadarSound() {
			return sp().getBoolean(RADAR_SOUND, false);
		}

		public static String[] getAdvertisment() {
			String ad = sp().getString(HIWIFIAD, null);
			String url = sp().getString(HIWIFIADURL, null);
			return new String[] { ad, url };
		}

		public static void setAdvertisement(String text, String url) {
			Editor editor = sp().edit();
			editor.putString(HIWIFIAD, text);
			editor.putString(HIWIFIADURL, url);
			editor.apply();
		}

		public static int setMacCount(int count) {
			KeyValueModel.setIntValue(mac_count, count);
			return count;
		}

		public static int getMacCount() {
			return KeyValueModel.getIntValue(mac_count, 0);
		}

		public static String getJoinedPeoples() {
			return sp().getString(AP_KEY_joinedPeoples, "1,000");
		}

		public static void setJoinedPeoples(String count) {
			Editor editor = sp().edit();
			editor.putString(AP_KEY_joinedPeoples, count);
			editor.apply();
		}

		public static Boolean isRememberPassword() {
			return sp().getBoolean(CONF_KEY_REMEMBER_PASSWORD, true);
		}

		public static void setRememberPassword(Boolean remBoolean) {
			Editor editor = sp().edit();
			editor.putBoolean(CONF_KEY_REMEMBER_PASSWORD, remBoolean);
			editor.apply();
		}

		public static Boolean isShowPassword() {
			return sp().getBoolean(CONF_KEY_SHOW_PASSWORD, true);
		}

		public static void setShowPassword(Boolean ShowBoolean) {
			Editor editor = sp().edit();
			editor.putBoolean(CONF_KEY_SHOW_PASSWORD, ShowBoolean);
			editor.apply();
		}

		public static Boolean isAutoSharePass() {
			return sp().getBoolean(AUTO_SHARE_PASS, true);
		}

		public static void setAutoSharePass(Boolean shareBoolean) {
			Editor editor = sp().edit();
			editor.putBoolean(AUTO_SHARE_PASS, shareBoolean);
			editor.apply();
		}

		public static String getReconmendApps() {
			return sp().getString(RECONMEND_APPS, null);
		}

		public static void setReconmendApps(JSONObject jsonObject) {
			Editor editor = sp().edit();
			editor.putString(RECONMEND_APPS, jsonObject.toString());
			editor.apply();
		}

		public static boolean isCommittedTime() {
			long currentTime = System.currentTimeMillis();
			long agoTime = sp().getLong(COMMITTED_TIME, 0);
			if (agoTime != 0) {
				if (currentTime - agoTime > 86400000) {
					return false;
				} else {
					return true;
				}
			} else {
				return false;
			}
		}

		public static void setCommitted(long time) {
			Editor editor = sp().edit();
			editor.putLong(COMMITTED_TIME, time);
			editor.apply();
		}

		public static long getPingTimeout() {
			return 30000;
		}

		public static void setIsForceUpgrade(Boolean flag) {
			KeyValueModel.setBooleanValue(AP_KEY_forceUpGrade, flag);
		}

		public static Boolean isForceUpgrade() {
			return KeyValueModel.getBooleanValue(AP_KEY_forceUpGrade, false);
		}

		public static synchronized int getLeftTime() {
			return KeyValueModel.getIntValue(AP_KEY_leftTime, 600);
		}

		public static synchronized void setLeftTime(int leftTime) {
			if (cmccIsVip()) {
				HWFLog.e(TAG, "is vip , return ");
				return;
			}
			HWFLog.e(TAG, "cmcc new left time:" + leftTime);
			KeyValueModel.setIntValue(AP_KEY_leftTime, leftTime);
		}

		public static Boolean cmccIsVip() {
			return sp().getBoolean(AP_KEY_cmccIsVip, false);
		}

		private static void setCmccIsVip(Boolean isVip) {
			Editor editor = sp().edit();
			editor.putBoolean(AP_KEY_cmccIsVip, isVip);
			editor.apply();
		}

		public static void onCmccVipStateChanged(Boolean isVip) {
			setCmccIsVip(isVip);
		}

		public static String getMadAddress() {
			String macAddress = sp().getString(AP_KEY_macAddress, "");
			if (TextUtils.isEmpty(macAddress)) {
				macAddress = DeviceUtil.getMacAddress();
				setMacAddress(macAddress);
			}
			return macAddress == null ? "" : macAddress;
		}

		private static void setMacAddress(String macAddress) {
			if (TextUtils.isEmpty(macAddress)) {
				return;
			}
			Editor editor = sp().edit();
			editor.putString(AP_KEY_macAddress, macAddress);
			editor.apply();
		}

		public static void setDeivceImei(String imei) {
			if (TextUtils.isEmpty(imei)) {
				return;
			}
			Editor editor = sp().edit();
			editor.putString(AP_KEY_IMEI, imei);
			editor.apply();
		}

		public static String getDeviceImei() {
			String imei = sp().getString(AP_KEY_IMEI, "");
			if (TextUtils.isEmpty(imei)) {
				imei = DeviceUtil.getImei();
				setDeivceImei(imei);
			}
			return imei == null ? "" : imei;
		}

		public static String getPingUrl() {
			return KeyValueModel.getStringValue(AP_KEY_PING_URL,
					DEFAULT_PING_URL);
		}

		public static String getPingExpectContent() {
			return KeyValueModel.getStringValue(AP_KEY_PING_EXPECTCONTENT,
					DEFAULT_PING_EXPECTCONTENT);
		}

		public static void setPingUrl(String url, String expectContent) {
			if (!TextUtils.isEmpty(url) && !TextUtils.isEmpty(expectContent)) {
				KeyValueModel.setStringValue(AP_KEY_PING_URL, url);
				KeyValueModel.setStringValue(AP_KEY_PING_EXPECTCONTENT,
						expectContent);
			}
		}

	}

	public static class ApConf {
		private static final String APNAME_STRING = "apConf";
		private static final String AP_KEY_Password = "apPassword";

		private static final SharedPreferences SP_PREFERENCES = sContext
				.getSharedPreferences(APNAME_STRING, Context.MODE_PRIVATE);

		public static SharedPreferences sp() {
			return SP_PREFERENCES;
		}

		public static void setApPassword(String password) {
			Editor editor = sp().edit();
			editor.putString(AP_KEY_Password, password);
			editor.commit();
		}

		public static String getApPassword() {
			return sp().getString(AP_KEY_Password,
					ResUtil.getStringById(R.string.ap_default_password));
		}

	}

	public static String uniqueId() {
		return ClientInfo.shareInstance().getUUID().toString();
	}

	private static AsyncHttpClient sClient = null;

	public static AsyncHttpClient sharedAsyncClient() {
		if (sClient == null) {
			sClient = new AsyncHttpClient();
			AsyncHttpClient
					.blockRetryExceptionClass(NoHttpResponseException.class);
			AsyncHttpClient.blockRetryExceptionClass(SocketException.class);
			AsyncHttpClient
					.blockRetryExceptionClass(UnknownHostException.class);
		}
		return sClient;
	}

	private static SyncHttpClient sSyncHttpClient = null;

	public static SyncHttpClient sharedSyncClient() {
		if (sSyncHttpClient == null) {
			sSyncHttpClient = new SyncHttpClient();
		}
		return sSyncHttpClient;
	}

	public static Boolean cmccHasLeftTime() {
		return GlConf.cmccIsVip() || GlConf.getLeftTime() > 0;
	}

	private static void addCmccStateListner() {
		CmccStateBroadcastReceiver receiver = new CmccStateBroadcastReceiver();
		IntentFilter filter = new IntentFilter();
		filter.addAction(CommerceState.ACTION_CLICK);
		filter.addAction(CommerceState.ACTION_LOGIN);
		filter.addAction(CommerceState.ACTION_LOGIN);
		filter.addAction(CommerceState.ACTION_VIP_STATE_CHANGED);
		Ct().registerReceiver(receiver, filter);
	}

	public static int getAppVersionCode() {
		try {
			return Ct().getPackageManager().getPackageInfo(
					Ct().getPackageName(), 0).versionCode;
		} catch (NameNotFoundException e) {
			return 0;
		}
	}

	public static String getAppVersionName() {
		try {
			return Ct().getPackageManager().getPackageInfo(
					Ct().getPackageName(), 0).versionName;
		} catch (NameNotFoundException e) {
			return "";
		}
	}

	private static String channel = null;

	public static String getChannel() {
		if (TextUtils.isEmpty(channel)) {
			channel = getMetaDataValue("UMENG_CHANNEL", "develope");
		}
		return channel;
	}

	private static String getMetaDataValue(String name, String def) {

		String value = getMetaDataValue(name);

		return (value == null) ? def : value;

	}

	private static String getMetaDataValue(String name) {

		Object value = null;

		PackageManager packageManager = Ct().getPackageManager();

		ApplicationInfo applicationInfo;

		try {

			applicationInfo = packageManager.getApplicationInfo(Ct()

			.getPackageName(), 128);

			if (applicationInfo != null && applicationInfo.metaData != null) {

				value = applicationInfo.metaData.get(name);

			}

		} catch (NameNotFoundException e) {

			throw new RuntimeException(

			"Could not read the name in the manifest file.", e);

		}

		if (value == null) {

			throw new RuntimeException("The name '" + name

			+ "' is not defined in the manifest file's meta data.");

		}

		return value.toString();

	}
}
