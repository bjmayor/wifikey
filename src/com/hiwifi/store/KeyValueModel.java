package com.hiwifi.store;

public class KeyValueModel {

	public String key=null;
	public String value=null;

	public static String getStringValue(String key, String defaultValue) {
		KeyValueModel model = get(key);
		try {
			return model == null ? defaultValue : model.value;
		} catch (Exception e) {
			return defaultValue;
		}
	}

	public static void setStringValue(String key, String value) {
		update(key, value);
	}

	public static int getIntValue(String key, int defaultValue) {
		KeyValueModel model = get(key);
		try {
			return model == null ? defaultValue : Integer.parseInt(model.value);
		} catch (Exception e) {
			return defaultValue;
		}
	}

	public static void setIntValue(String key, int value) {
		update(key, value + "");
	}

	public static Boolean getBooleanValue(String key, Boolean defaultValue) {
		KeyValueModel model = get(key);
		try {
			return model == null ? defaultValue : Boolean.parseBoolean(model.value);
		} catch (Exception e) {
			return defaultValue;
		}
	}

	public static void setBooleanValue(String key, Boolean value) {
		update(key, value + "");
	}

	public static void delete(String key) {
		KeyValueDbMgr.shareInstance().delete(key);
	}

	private static KeyValueModel get(String key) {
		return KeyValueDbMgr.shareInstance().getModel(key);
	}

	private static void add(String key, String value) {
		KeyValueModel model = new KeyValueModel();
		model.key = key;
		model.value = value;
		KeyValueDbMgr.shareInstance().insert(model);
	}

	private static void update(String key, String value) {
		KeyValueModel model = KeyValueDbMgr.shareInstance().getModel(key);
		if (model == null) {
			add(key, value);
		} else {
			model.value =  value;
			KeyValueDbMgr.shareInstance().update(model);
		}
	}
}
