package com.hiwifi.model.log;

import java.text.SimpleDateFormat;
import java.util.HashMap;
import java.util.Locale;

public class Log_item extends HashMap<String, String> {

	/**
	 * the data unit of the log
	 */
	private static final long serialVersionUID = 512L;

	public Log_item(String class_name, String tag, String text) {
		SimpleDateFormat sDateFormat = new SimpleDateFormat(
				"yyyy-MM-dd   hh:mm:ss", Locale.CHINA);
		String time = sDateFormat.format(new java.util.Date());
		put("class_name", class_name);
		put("tag", tag);
		put("text", text);
		put("time", time);
	}

}
