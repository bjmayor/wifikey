/**
 * ReleaseConstant.java
 * com.hiwifi.constant
 * hiwifiKoala
 * shunping.liu create at 20142014��8��1������11:23:50
 */
package com.hiwifi.constant;


/**
 * @author shunping.liu@hiwifi.tw
 *
 */
public class ReleaseConstant {
	
	public enum DebugLevel
	{
		DebugLevelConsole,
		DebugLevelSDCard,
		DebugLevelActivity,
		DebugLevelNone;
	}
	public static DebugLevel debugLevel = DebugLevel.DebugLevelSDCard;
	public static final boolean ISDEBUG = false;
	//如果关闭，则不取密码，也不倒计时
	public static boolean isCommerceOpened = false;
}
