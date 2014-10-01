package com.hiwifi.model.request;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.annotation.SuppressLint;

import com.hiwifi.hiwifi.Gl;
import com.hiwifi.model.wifi.AccessPoint;
import com.hiwifi.model.wifi.WifiAdmin;
import com.hiwifi.store.AccessPointModel;
import com.hiwifi.store.AppInfoModel;

public class RequestBodyBuilder {

	public RequestBodyBuilder() {
	}

	@SuppressLint("SimpleDateFormat")
	public static JSONObject buildSyncUpJsonObject(List<AccessPointModel> list) {
		JSONObject j = new JSONObject();
		try {
			j.put("source_type", "android_app");
			j.put("key", "il9hHyY53hGRBW9j");
			j.put("client_id", Gl.uniqueId());
			JSONArray jsonArray = new JSONArray();
			int size = list.size();
			for (int i = 0; i < size; i++) {
				AccessPointModel model = list.get(i);
				JSONObject jsonObject = new JSONObject();
				jsonObject.put("time", new SimpleDateFormat(
						"yyyy-MM-dd HH:mm:ss").format(new Date()));
				jsonObject.put("longitude", model.longitude);
				jsonObject.put("latitude", model.latitude);
				jsonObject.put("bssid", model.BSSID);
				jsonObject.put("ssid", model.SSID);
				jsonObject.put("rssi", model.level);
				int channel = (model.channel > 2484 ? (model.channel - 5000) / 5
						: (model.channel - 2407) / 5);
				jsonObject.put("channel", channel);
				jsonObject.put("securitytype",
						httpSecuritytype(model.capability));
				jsonObject.put("password", model.getPassword(true));
				jsonObject.put("status_pwd", model.passwordStatus);
				jsonObject.put("auth_username", model.account);
				jsonObject.put("is_portal",
						model.isPortal() ? AccessPointModel.PortalTrue
								: AccessPointModel.PortalFalse);
				jsonArray.put(jsonObject);
			}
			j.putOpt("data", jsonArray);
		} catch (JSONException e) {
			e.printStackTrace();
		}
		// System.out.println("ok===" + j.toString());
		return j;
	}

	public static JSONObject buildGetConfJsonObject() {
		JSONObject ret = new JSONObject();
		try {
			ret.put("link_ssid", "");
			ret.put("link_mac", "");
			AccessPoint accessPoint = WifiAdmin.sharedInstance()
					.getActiveAccessPoint();
			if (accessPoint != null) {
				ret.put("link_ssid", accessPoint.getPrintableSsid());
				ret.put("link_mac", accessPoint.getScanResult().BSSID);
			}
		} catch (JSONException e) {
			e.printStackTrace();
		}
		return ret;
	}

	@SuppressLint("SimpleDateFormat")
	public static JSONArray buildSaveWifiBackupJsonObject(
			List<AccessPointModel> list) {
		if (list == null) {
			return new JSONArray();
		}
		JSONArray jsonArray = new JSONArray();
		try {
			int size = list.size();
			for (int i = 0; i < size; i++) {
				AccessPointModel model = list.get(i);
				JSONObject jsonObject = new JSONObject();
				jsonObject.put("act_date", new SimpleDateFormat(
						"yyyy-MM-dd HH:mm:ss").format(new Date()));
				jsonObject.put("bssid", model.BSSID);
				jsonObject.put("ssid", model.SSID);
				jsonObject.put("auth_username", model.account);
				jsonObject.put("password", model.getPassword(true));
				jsonArray.put(jsonObject);
			}
		} catch (JSONException e) {
			e.printStackTrace();
		}
		return jsonArray;
	}

	private static String httpSecuritytype(String securitytype) {
		if (securitytype != null) {
			if ("".equals(securitytype)) {
				return "OPEN";
			} else if (securitytype.contains("WPA")
					|| securitytype.contains("WPA2")) {
				return "PSK";
			} else if (securitytype.contains("WEP")) {
				return "WEP";
			} else if (securitytype.contains("EAP")) {
				return "8021X";
			} else {
				return "OPEN";
			}
		} else {
			return "OPEN";
		}
	}

	public static JSONArray buildGetPwdJsonObject(List<AccessPoint> list) {
		JSONArray jsonArray = new JSONArray();
		JSONArray itemArray = null;
		int size = list.size();
		if (list != null && list.size() > 0) {
			for (int i = 0; i < size; i++) {
				itemArray = new JSONArray();
				itemArray.put(list.get(i).getScanResult().BSSID);
				itemArray.put(list.get(i).getScanResult().SSID);
				if (!list.get(i).needPassword()) {
					itemArray.put(0);
				}
				jsonArray.put(itemArray);
			}
		}
		return jsonArray;
	}

	public static JSONObject buildUploadAppJsonObject(ArrayList<AppInfoModel> t) {
		try {
			JSONArray array = new JSONArray();
			JSONObject object = new JSONObject();
			int length = t.size();
			for (int i = 0; i < length; i++) {
				AppInfoModel stone = t.get(i);
				String name = stone.getAppname();
				String packagename = stone.getPackgename();
				// int count = stone.getStart_count();
				JSONObject stoneObject = new JSONObject();
				stoneObject.put("name", name);
				stoneObject.put("packagename", packagename);
				// stoneObject.put("startcount", count);
				array.put(stoneObject);
			}
			object.put("data", array);
			return object;
		} catch (JSONException e) {
			e.printStackTrace();
		}
		return null;
	}
}
