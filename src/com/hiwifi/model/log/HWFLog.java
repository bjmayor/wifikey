package com.hiwifi.model.log;

import android.util.Log;

import com.hiwifi.constant.ReleaseConstant;
import com.hiwifi.constant.ReleaseConstant.DebugLevel;

public final class HWFLog {
	
	private static final boolean mDevelopMode = ReleaseConstant.debugLevel==DebugLevel.DebugLevelConsole;
	 
	private HWFLog(){		 
	}

	public static boolean isDevelopMode() {
		return mDevelopMode;
		
	}
	
	public static int v(String tag,String msg){
		if(mDevelopMode){		
			return Log.v(tag, msg);
		}
		else {
			log2Activity(tag, msg);
			return 0;
		}
	}
	
	
	public static void log2Activity(String tag, String msg)
	{
		if(ReleaseConstant.debugLevel == DebugLevel.DebugLevelActivity){
			Main_log.put_log(tag, msg);
		}
	}
	
	public static int v(String tag,String msg,Throwable tr){
		if(mDevelopMode){		
			return Log.v(tag, msg, tr);
		}
		else {	
			log2Activity(tag, msg);
			return 0;
		}
	}
	
	public static int d(String tag,String msg){
		if(mDevelopMode){		
			return Log.d(tag, msg);
		}
		else {
			log2Activity(tag, msg);
			return 0;
		}
	}
	
	public static int d(String tag,String msg,Throwable tr){
		if(mDevelopMode){		
			return Log.d(tag, msg, tr);
		}
		else {
			log2Activity(tag, msg);
			return 0;
		}
	}
	
	public static int i(String tag,String msg){
		if(mDevelopMode){		
			return Log.i(tag, msg);
		}
		else {
			log2Activity(tag, msg);
			return 0;
		}
	}
	
	public static int i(String tag,String msg,Throwable tr){
		if(mDevelopMode){		
			return Log.i(tag, msg, tr);
		}
		else {
			log2Activity(tag, msg);
			return 0;
		}
	}
	
	public static int w(String tag,String msg){
		log2Activity(tag, msg);
		return Log.w(tag, msg );
	}
	
	
	public static int w(String tag,String msg,Throwable tr){
		log2Activity(tag, msg);
		return Log.w(tag, msg, tr);
	}
	
	public static int w(String tag,Throwable tr){
		return Log.w(tag, tr);
	}
	
	public static int e(String tag,String msg){
		UserLog.e(tag, msg);
		log2Activity(tag, msg);
		return Log.e(tag, msg==null?"":msg );
	}
	
	public static int e(String tag,String msg,Throwable tr){
		UserLog.e(tag, msg, tr);
		log2Activity(tag, msg);
		return Log.e(tag, msg, tr);
	}

}
