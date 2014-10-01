package com.hiwifi.app.utils;

import java.lang.reflect.Method;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import android.content.Context;
import android.content.res.Configuration;
import android.telephony.TelephonyManager;
import android.text.TextUtils;

public class RegUtil {

	// 正则验证邮箱格式
	public static boolean checkMail(String mail) {
		// 需求:校验邮件地址
		String format = "^\\w+@\\w+\\.(\\w{2,3}|\\w{2,3}\\.\\w{2,3})$";
		if (mail.matches(format)) {
			return true;
		}
		return false;
	}

	// 数字格式验证
	public static boolean checkPhone(String phone) {
//		Pattern pattern = Pattern
//				.compile("^((13[0-9])|(15[^4,\\D])|(18[0,1,5-9]))\\d{8}$");
		Pattern pattern = Pattern
				.compile("^1\\d{10}$");
		Matcher matcher = pattern.matcher(phone);
		if (matcher.matches()) {
			return true;
		}
		return false;
	}

	public static Boolean isPskPasswordValid(String password) {
		Pattern pattern = Pattern.compile("^.{8,63}$");
		Matcher matcher = pattern.matcher(password);
		return matcher.matches();
	}
	
	public static Boolean isUserNameValid(String userName)
	{
		Boolean contentBoolean = false;
		Pattern pattern = Pattern.compile("^[0-9a-zA-Z_[\u4e00-\u9fa5]]{2,20}$");
		Matcher matcher = pattern.matcher(userName);
		contentBoolean =  matcher.matches();
		if (!contentBoolean) {
			return contentBoolean;
		}
		String newString = userName.replaceAll("[[\u4e00-\u9fa5]]", "aa");
		if (newString.length()<4 || newString.length()>20) {
			return false;
		}
		return true;
	}
	
	public static Boolean isUserPasswordValid(String password)
	{
		Pattern pattern = Pattern.compile("^.{6,12}$");
		Matcher matcher = pattern.matcher(password);
		return matcher.matches();
	}

	public static Boolean isWepPasswordValid(String password) {
		Pattern pattern = Pattern.compile("^.{1,64}$");
		Matcher matcher = pattern.matcher(password);
		return matcher.matches();
	}
	
	public static Boolean isEapPasswordValid(String password){
		Pattern pattern = Pattern.compile("^.{1,192}$");
		Matcher matcher = pattern.matcher(password);
		return matcher.matches();
	}

	static String regEx = "[\u4e00-\u9fa5]";
	static Pattern pat = Pattern.compile(regEx);

	public static boolean isContainsChinese(String str) {
		Matcher matcher = pat.matcher(str);
		boolean flg = false;
		if (matcher.find()) {
			flg = true;
		}
		return flg;
	}

	public static String ToDBC(String input) {
		char[] c = input.toCharArray();
		for (int i = 0; i < c.length; i++) {
			if (c[i] == 12288) {
				c[i] = (char) 32;
				continue;
			}
			if (c[i] > 65280 && c[i] < 65375)
				c[i] = (char) (c[i] - 65248);
		}
		return new String(c);
	}

	public static String secont2Time(int time) {
		// TODO
		// int oneDay = 24 * 60 * 60 ;
		int hour = 60 * 60;
		int minute = 60;
		StringBuffer mTime = new StringBuffer();
		if (time == -1) {
			return "永久免费";
		}
		// if(time >= oneDay){
		// mTime.append(time/oneDay+"天");
		// time=time % oneDay;
		// }
		if (time >= hour) {
			mTime.append(time / hour + "小时");
			time = time % hour;
		}
		if (time >= minute) {
			mTime.append(time / minute + "分钟");
			time = time % minute;
		}
		if (time < minute) {
			mTime.append(time + "秒");
		}
		return mTime.toString();
	}

	public static boolean isEmail(String str) {
		Pattern pattern = Pattern
				.compile("^([a-zA-Z0-9\\.\\-])+\\@(([a-zA-Z0-9\\-])+\\.)+([a-zA-Z0-9]{2,4})+$");
		return pattern.matcher(str).matches();
	}

	public static String formateTimeToSecond(int time) {
		String second;
		time = time % 60;
		if (time <= 9) {
			second = "0" + time;
		} else {
			second = time + "";
		}
		return second;
	}

	public static String formateTimeToMinute(int time) {
		String minute;
		if (time < 60) {
			minute = "00";
		} else {
			time = time / 60;
			if (time <= 9) {
				minute = "0" + time;
			} else {
				minute = time + "";
			}
		}
		return minute;
	}

	
}
