package com.hiwifi.model.log;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.io.Writer;
import java.text.SimpleDateFormat;
import java.util.Date;

import android.os.Environment;

import com.hiwifi.constant.ConfigConstant;
import com.hiwifi.constant.ReleaseConstant;
import com.hiwifi.constant.ReleaseConstant.DebugLevel;

public class UserLog {
	public static Boolean IsWriteToFile = ReleaseConstant.debugLevel==DebugLevel.DebugLevelSDCard;			//日志写入文件开关
	
	private static char USERLOG_TYPE = 'e';//输入日志类型，用来控制日志信息等级
	private static String USERLOG_PATH_SDCARD_DIR = Environment.getExternalStorageDirectory().getPath() + ConfigConstant.SD_CRASH_DIRECTORY;//日志文件在sdcard中的路径
	private static String USERLOG_FILE_NAME = "UserLog.txt";//本类输出的日志文件名称
	private static final int MAX_LOG_FILE_SIZE = (5 * 1024 * 1024);//sd卡中日志文件的最大大小
	
	private static SimpleDateFormat myLogSdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");// 日志的输出格式   
	
	public static void e(String tag, Object msg){//错误信息
		msg = msg ==null?"":msg;
		log(tag, msg.toString(), 'e');
	}
	
	public static void w(String tag, Object msg){//警告信息
		msg = msg ==null?"":msg;
		log(tag, msg.toString(), 'w');
	}
	
	public static void d(String tag, Object msg){//调试信息
		msg = msg ==null?"":msg;
		log(tag, msg.toString(), 'd');
	}
	
	public static void i(String tag, Object msg){
		msg = msg ==null?"":msg;
		log(tag, msg.toString(), 'i');
	}
	
	public static void v(String tag, Object msg){
		msg = msg ==null?"":msg;
		log(tag, msg.toString(), 'v');
	}
	
	public static void e(String tag, String text, Throwable tr){//错误信息
		log(tag, text + tr, 'e',tr);
	}
	
	public static void w(String tag, String text, Throwable tr){//警告信息
		log(tag, text + tr, 'w');
	}
	
	public static void d(String tag, String text, Throwable tr){//调试信息
		log(tag, text + tr, 'd');
	}
	
	public static void i(String tag, String text, Throwable tr){
		log(tag, text + tr, 'i');
	}
	
	public static void v(String tag, String text, Throwable tr){
		log(tag, text + tr, 'v');
	}
	
	/**
	 * 根据tag，msg和等级，输出日志
	 * 
	 * @param tag
	 * @param msg
	 * @param level
	 * @return void
	 * 
	 */
	private static void log(String tag, String msg, char level){
		if(IsWriteToFile ){
			writeUserLogToFile(String.valueOf(level), tag, msg,null);
		}
		
	}
	
	private static void log(String tag, String msg, char level,Throwable tr){
		if(IsWriteToFile ){
			writeUserLogToFile(String.valueOf(level), tag, msg,tr);
		}
		
	}
	
	
	
	
	/**
	 * 打开日志文件并写入日志
	 */
	private static void writeUserLogToFile(String logtype, String tag, String text, Throwable tr){
		Date nowTime = new Date();
		String needWriteMessage = myLogSdf.format(nowTime) + "  " + logtype + "  " + tag + "  " + text ;
		
		if(tr !=null){
			Writer writer = new StringWriter();
			PrintWriter printWriter = new PrintWriter(writer);
			tr.printStackTrace(printWriter);
			Throwable cause = tr.getCause();
			while (cause != null) {
				cause.printStackTrace(printWriter);
				cause = cause.getCause();
			}
			printWriter.close();
			needWriteMessage += writer.toString();
		}
		
		
		File dir = new File(USERLOG_PATH_SDCARD_DIR);
		if (!dir.exists()) {
			dir.mkdirs();
		}
		File file = new File(USERLOG_PATH_SDCARD_DIR, USERLOG_FILE_NAME);
		if (!file.exists()) {
			try {
				if (!file.createNewFile()) {
					return;
				}
			} catch (Exception e) {
			}
		} 
		FileWriter fileWriter = null;
		BufferedWriter bufWriter = null;
		try{
			fileWriter = new FileWriter(file, true);//后面的参数代表是不是要接上文件中原来的数据，不进行覆盖
			bufWriter = new BufferedWriter(fileWriter);
			bufWriter.write(needWriteMessage);
			bufWriter.newLine();
		}catch (Exception e){
		}finally{
			try {
				if(bufWriter != null){
					bufWriter.close();
				}
			} catch (Exception e) {
				e.printStackTrace();
			}
			try {
				if(fileWriter != null){
					fileWriter.close();
				}
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
	}
	
	/**
	 * 删除指定的日志文件，根据文件大小删除文件
	 */
	public static void deleteFile(){
		File file = new File(USERLOG_PATH_SDCARD_DIR, USERLOG_FILE_NAME);
		long size = file.length();
		if(file.exists() && size > MAX_LOG_FILE_SIZE){
			file.delete();
		}
	}
	
}
