/**
 * ConfigConstant.java
 * com.hiwifi.constant
 * hiwifiKoala
 * shunping.liu create at 20142014年8月5日上午11:03:47
 */
package com.hiwifi.constant;

import java.io.File;

import android.os.Environment;

import com.hiwifi.hiwifi.Gl;
import com.hiwifi.model.log.HWFLog;

/**
 * @author shunping.liu@hiwifi.tw
 * 
 */
public class ConfigConstant {

	public static final String APP_SRC = "hiwififree";
	public static final String ENCODING_GB2312 = "GB2312";
	public static final String ENCODING_8859_1 = "8859_1";
	public static final String ENCODING_UTF_8 = "utf-8";

	public static final String HIWIFI_ADMIN_PACKAGE = "com.hiwifi";

	public static int SCORE = 0;
	public static final int REQUEST_LOGIN_CODE = 0x200;
	public static final int LOGIN_RESULT = 0x201;
	public static final int SETTING_RESULT = 0x202;
	public static final int SETTING_REQUEST_LOGIN = 0x203;
	public static final int REQUEST_SETTING_CODE = 0x300;
	public static final int SETTING_REQUEST_EXIT = 0x301;

	public static final int HEATH__REQUEST = 0x400;
	public static final int HEATH_WIFI_RESULT = 0x401;
	public static final int HEATH_LED_RESULT = 0x402;
	public static boolean EVER_FREE = false;
	public static long START_ONLINE_TIME = 0;
	public static boolean ONLINE_BY_CMCC = true; // 标记通过cmcc链接上网
	public static boolean NO_FREE_TIME = false; // 标记免费上网时间是否用完

	public static final int BUFFER_SIZE = 8192;
	public static final int JOIN_THREAD_TIME_OUT = 1000; // ms
	public static final int JOIN_PLAY_VOICE_THREAD_TIME_OUT = 30 * 1000; // ms

	public static final int CONNECTION_TIMEOUT = 30 * 1000; // ms
	public static final int CONNECTION_TIMEOUT_10 = 10 * 1000; // ms
	public static final int HTTP_PORT = 80;
	public static final int RESPONSE_STATUS_CODE = 200;

	public static final String SD_HOME_DIRECTORY = "/hiwifi/";
	public static final String SD_CRASH_DIRECTORY = SD_HOME_DIRECTORY
			+ "crash/";
	public static final String SD_DATA_DIRECTORY = SD_HOME_DIRECTORY + "data/";
	public static final String SD_TMP_DIRECTORY = SD_HOME_DIRECTORY + "tmp/";

	public static String getCameraFileName() {
		String fileDirectroy = "";
		if (Environment.MEDIA_MOUNTED.equals(Environment
				.getExternalStorageState())
				&& Environment.getExternalStorageDirectory().exists()) {
			fileDirectroy = Environment.getExternalStorageDirectory()
					.getAbsolutePath() + SD_TMP_DIRECTORY;
		} else {
			fileDirectroy = Gl.Ct().getFilesDir().getAbsolutePath()
					+ SD_TMP_DIRECTORY;
		}

		File file = new File(fileDirectroy);
		if (!file.exists()) {
			file.mkdirs();
		}
		return "file:///" + Environment.getExternalStorageDirectory().getPath()
				+ "/" + SD_TMP_DIRECTORY + "temp.jpg";
	}

	/** 分享图片名称 */
	public static final String FILE_NAME = SD_TMP_DIRECTORY + "/wifi_pic"
			+ Gl.getAppVersionName() + ".jpg";
	public static String IMAGE_PATH = "pwd_share";

	public static final String URL_SCHEMA_HTTP = "http";
	public static final String URL_HTTP = "http://";
	public static final String HTTP_BOUNDARY = "--www.hiwifi.com--";

	/** 第一次请求数据 */
	public static final int LOAD_DATA_FIRST = 0;
	/** 轮询 */
	public static final int LOAD_DATA_POLL = 1;
	/** 下拉刷新 */
	public static final int LOAD_DATA_PULL_DOWN = 2;

	public static final int LOAD_DATA_PULL_DOWN_ = 3;

	// 以下变量主要 用在410时循环请求数据是，避免离开页面也进行网络数据请求
	// 统一指定 true为可以请求数据 false为已离开页面 不再请求数据
	public static boolean loginFlag = true;
	public static boolean update = true;
	public static boolean router_list = true;
	public static boolean router_detail = true;
	public static boolean router_detail_get_swich = true;
	public static boolean router_detail_set_swich = true;
	public static boolean devices_list = true;
	public static boolean devices_WifiCloseDeviceBlock = true;
	public static boolean devices_list_wifi_set_device_block = true;
	public static boolean check_new = true;
	public static boolean router_reboot = true;
	public static boolean getDeviceCountTask = true;
	public static boolean router_bind = true;
	public static boolean router_detail_get_led_status = true;
	public static boolean router_detail_set_led_status = true;
	public static boolean check_router_upgrade = true;
	public static boolean do_router_upgrade = true;
	public static boolean get_plugs = true;
	public static boolean set_app_switch = true;

	public static int REQUEST_CODE_REGISTER = 100;
	public static int REQUEST_CODE_GET_ROUTER_INFO = 101;
	public static int REQUEST_CODE_SET_APP = 102;
	public static int REQUEST_CODE_INSTALL_APP = 103;

	//
	// the simple date pattern
	//
	public static final String DATE_FORMAT_SLASH_YMD = "yyyy/MM/dd";
	public static final String DATE_FORMAT_HM = "HH:mm";
	public static final String DATE_FORMAT_MINUS_YMD = "yyyy-MM-dd";
	public static final String DATE_FORMAT_MINUS_YMDHM = "yyyy-MM-dd HH:mm";
	public static final String DATE_FORMAT_MINUS_YMDHMG = "yyyy-MM-dd HH:mm 'GMT'";
	public static final String DATE_FORMAT_TPYE1 = "yyyy/MM/dd HH:mm";
	public static final String DATE_FORMAT_TYPE2 = "M月d日 HH:mm";
	public static final String DATE_FORMAT_TPYE3 = "yyyyMMdd_HHmmss";
	public static final String DATE_SIMPLE_PATTERN_FOR_HTTP = "EEE, dd MMM yyyy HH:mm:ss 'GMT'";
	public static final String DATE_DAYBREAK = "00:00";

	// sql lite
	public static final String AP_DB_NAME = "hwf_access_point";

	public static final String STRING_FILE_SPLIT = "/";
	public static final String STRING_FILE_SPLIT1 = "~";

	public static final String STRING_SPACE = "  ";
	public static final String STRING_INTERVAL_SYMBOL = "~";
	public static final String STRING_SEMICOLON = ";";
	public static final String STRING_SPLIT_SYMBOL = "\\|";
	public static final String STRING_XML_POSTFIX = ".xml";
	public static final String STRING_JPG_POSTFIX = ".jpg";
	public static final String STRING_TXT_POSTFIX = ".txt";
	public static final String STRING_PNG_POSTFIX = ".png";
	public static final String STRING_NEXT_LINE = "\r\n";
	public static final String STRING_NEWLINE = "\n";
	public static final String STRING_LINE_LAND = "-";
	public static final String STRING_COMMA = ",";
	public static final String STRING_COLON = ":";
	public static final String STRING_UNDERLINE = "_";
	public static final String STRING_WENHAO = "?";

	/**
	 * About carrier
	 */

	public static final String CMCC_APERATOR_CODE3 = "46007";
	public static final String CMCC_NAME = "CMCC";
	public static final String UNICOM_NAME = "UNICOM";
	public static final String TELECOM_NAME = "TELECOM";

	//
	// TAG and attributes.
	//
	public static final String NEW_VERSION_TAG = "supd";
	public static final String PUSH_INFO_TAG = "adv";
	public static final String UPDATE_KEY_INFO = "info";
	public static final String UPDATE_KEY_URL = "url";
	public static final String MOBILE = "Mobile";
	public static final String UNICOME = "Unicom";
	public static final String TELECOM = "Telecom";
	public static final String FALLBACK = "Fallback";
	public static final String MOBILE_NUM_46000 = "46000";
	public static final String MOBILE_NUM_46002 = "46002";
	public static final String UNICOME_NUM = "46001";
	public static final String TELECOM_NUM = "46003";

	public static final String ANDROID_MARKET_PACKAGE_NAME = "com.android.vending";

	/**
	 * About the date type
	 */
	public static final String FAKE_IMEI = "35278404110901160";

	public static final String DEFTYPE_ID = "id";
	public static final String DEFTYPE_STRING = "string";
	public static final String DEFTYPE_DRAWABLE = "drawable";

	//
	// the width of kind note dialog
	//
	public static final double DIALOG_KINDNOTE_WIDTH = 0.91;
	public static final double DIALOG_KINDNOTE_HEIGHT = 0.65;
	public static final int PUSH_DOMAIN_SCORE_MAX = 3;
	public static final int PUSH_DOMAIN_SCORE_MIN = 0;

	public static final String KEY_BAIDUMAP = "R133KR7GeZNupLlBR4698Duc";
}
