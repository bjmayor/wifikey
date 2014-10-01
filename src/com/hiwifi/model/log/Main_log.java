package com.hiwifi.model.log;

import java.util.ArrayList;

public class Main_log extends ArrayList<Log_item> {

	/**
	 * main log
	 */
	private static final long serialVersionUID = 1024L;
	private static Main_log self;
	private int max_list_size = 100;

	private Main_log() {
		if (Main_log.self == null) {
			Main_log.self = this;
		}
	}

	public static void put_log(String class_name, String text) {
		put_log(class_name, "null", text);
	}

	public static void put_log(Object class_name, String text) {
		put_log(class_name, "null", text);
	}

	public static void put_log(Object class_name, String tag, String text) {
		put_log(class_name.getClass().toString(), tag, text);
	}

	public synchronized static void put_log(String class_name, String tag, String text) {
		if (Main_log.self == null) {
			new Main_log();
		}
		Main_log m = Main_log.self;
		while (m.size() >= m.max_list_size) {
			m.remove(m.size()-1);
		}
		m.add(0, new Log_item(class_name, tag, text));
	}

	public static void set_max_list_size(int max_list_size) {
		Main_log t = Main_log.self;
		t.max_list_size = max_list_size;
	}

	public static Main_log getMainList() {
		if (Main_log.self == null) {
			new Main_log();
		}
		return Main_log.self;
	}
}
