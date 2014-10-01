package com.hiwifi.model;

import java.io.Serializable;
import java.util.ArrayList;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.content.SharedPreferences;

import com.hiwifi.app.task.SaveApListRunnable;
import com.hiwifi.constant.RequestConstant.RequestTag;
import com.hiwifi.hiwifi.Gl;
import com.hiwifi.model.log.HWFLog;
import com.hiwifi.model.request.ResponseParserInterface;
import com.hiwifi.model.request.ServerResponseParser;
import com.hiwifi.model.request.ServerResponseParser.ServerCode;
import com.hiwifi.utils.encode.MD5Util;

public class RecommendInfo implements ResponseParserInterface, Serializable {
	private static final long serialVersionUID = -5415290809736595898L;
	private String downLoadUrl;// 应用下载地址
	private String name;// 应用名字
	private String icon;// 应用icon
	private String packagename;// 应用包名
	private int startLevel;// 应用星级
	private String downloadNumber;// 下载人数
	private Type type;// 应用类型

	public enum Type {
		None(0), // 无
		Recommend(3), // 推荐
		New(2), // 最新
		Hot(1), // 最热
		Basic(4);// 必备
		int type;

		private Type(int i) {
			this.type = i;
		}

		public static Type valueOf(int value) {
			switch (value) {
			case 1:
				return Hot;
			case 2:
				return New;
			case 3:
				return Recommend;
			case 4:
				return Basic;
			default:
				return None;
			}
		}
	}

	public String getDownLoadUrl() {
		return downLoadUrl;
	}

	public String getDownLoadNumber() {
		return downloadNumber;
	}

	public int getStartLevel() {
		return startLevel;
	}

	public void setDownLoadUrl(String url_dl) {
		this.downLoadUrl = url_dl;
	}

	public String getName() {
		return name;
	}

	public Type getType() {
		return type;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getIcon() {
		return icon;
	}

	public void setIcon(String icon) {
		this.icon = icon;
	}

	public String getPackagename() {
		return packagename;
	}

	public void setPackagename(String packagename) {
		this.packagename = packagename;
	}

	@Override
	public String toString() {
		return "ReonmendInfo [url_dl=" + downLoadUrl + ", name=" + name
				+ ", icon=" + icon + ", packagename=" + packagename + "]";
	}

	public RecommendInfo() {

	}

	public RecommendInfo(String url_dl, String name, String icon,
			String packagename) {
		super();
		this.downLoadUrl = url_dl;
		this.name = name;
		this.icon = icon;
		this.packagename = packagename;
	}

	@Override
	public void parse(RequestTag tag, ServerResponseParser parser) {
		if (parser.code == ServerCode.OK.value()) {
			JSONObject response = parser.originResponse;
			String localListMd5 = MD5Util.encryptToMD5(parser.originResponse
					.toString());
			if (!localListMd5.equalsIgnoreCase(getListMd5())) {
				hasNew = true;
				listMd5 = localListMd5;
			}
			save();
			JSONArray array;
			try {
				array = response.isNull("list") ? null : response
						.getJSONArray("list");

				recommendList.clear();
				if (array != null && array.length() > 0) {
					for (int i = 0; i < array.length(); i++) {
						JSONObject object = array.optJSONObject(i);
						String url_dl = object.optString("download_url", "");
						String name = object.optString("title", "");
						String icon = object.optString("icon", "");
						String downloadCount = object.optString(
								"download_count", "");
						int startLevel = object.optInt("star_score", 0);
						String packagename = object.isNull("package") ? ""
								: object.getString("package");
						RecommendInfo info = new RecommendInfo(url_dl, name,
								icon, packagename);

						recommendList.add(info);
						info.downloadNumber = downloadCount;
						info.startLevel = startLevel;
						try {
							info.type = Type
									.valueOf(object.optInt("typeid", 0));
						} catch (Exception e) {
							info.type = Type.None;
						}

					}
				}
			} catch (JSONException e) {
				e.printStackTrace();
				HWFLog.e("debug", "load data error:" + e.getMessage());
			}
		}
	}

	private static String listMd5 = null;
	private static Boolean hasNew = false;
	private static ArrayList<RecommendInfo> recommendList = new ArrayList<RecommendInfo>();

	public static ArrayList<RecommendInfo> getList() {
		return recommendList;
	}

	private static String getListMd5() {
		if (!hasLoaded) {
			load();
			hasLoaded = true;
		}
		return listMd5;
	}

	public static void markRead() {
		hasNew = false;
		save();
	}

	private static Boolean hasLoaded = false;

	public static Boolean hasNew() {
		if (!hasLoaded) {
			load();
			hasLoaded = true;
		}
		return hasNew;
	}

	public static SharedPreferences getSharedPreferences() {
		return Gl.Ct().getSharedPreferences("RecommendInfo", 0);
	}

	private static void save() {
		try {
			SharedPreferences sp = getSharedPreferences();
			sp.edit().putBoolean("hasNew", hasNew).commit();
			sp.edit().putString("listMd5", listMd5).commit();
		} catch (Exception e) {
		}
	}

	private static void load() {
		SharedPreferences sp = getSharedPreferences();
		hasNew = sp.getBoolean("hasNew", false);
		listMd5 = sp.getString("listMd5", "");
	}

}
