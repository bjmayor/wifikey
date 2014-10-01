package com.hiwifi.utils.encode;

public class Security {

	public static String encode_string(String input) {
		// return input;
		return code2string(stringEncodeJNI(input.toCharArray()));
	}

	public static String decode_string(String input) {
		// return input;
		return code2string(stringDecodeJNI(input.toCharArray()));
	}

	private static String code2string(char[] input) {
		String ret = "";
		for (char i : input) {
			// , key.toCharArray())) {
			ret += i;
		}
		return ret;

	}

	private native static char[] stringEncodeJNI(char[] input);// , char[] key);

	private native static char[] stringDecodeJNI(char[] input);// , char[] key);

	private native static String unimplementedStringFromJNI();

	static {
		System.loadLibrary("Security");
	}
}
