package com.hiwifi.model.log;

import org.json.JSONArray;
import org.json.JSONObject;

import android.util.Log;


public class LogUtil {
	public static boolean isDebug = /*Constants.logIsDebug*/ true;

	public static void i(String tag, String message) {
		if (isDebug) {
			Log.i(tag, message+"");
		}

	}

	public static void e(String tag, String message) {
		if (isDebug) {
			Log.e(tag, message+"");
		}

	}

	public static void d(String tag, String message) {
		if (isDebug) {
			Log.d(tag, message+"");
		}

	}
	
	public static  void dialogLog(Object response,String result){
			try {
				if(response!=null){
					org.json.JSONObject jsonObject = null;
					if(response instanceof JSONObject){
						jsonObject = (org.json.JSONObject) response;
					}else if(response instanceof String){
						jsonObject = new org.json.JSONObject((String)response);
					}else if(response instanceof JSONArray){
						
					}
					int code = jsonObject.isNull("code")?-3:jsonObject.getInt("code");
					if(code != 0){
						if(isDebug){
							//TODO 添加开关限制
//								DialogTool.createMessageDialog(HiwifiApplication.context, "Log日志", result, "确定", null, DialogTool.NO_ICON).show();
						}
//						if(isDebug){
////							JSONObject response = HiwifiApplication.ERROR_LOG;
//							//TODO 添加开关限制
//							StringBuffer errorString = new StringBuffer();
//							errorString.append(!response.containsKey("REQUEST->")?"":response.getString("REQUEST->").toString()+"\n" +(!response.containsKey("PARAMS->")?"":response.getString("PARAMS->")).toString()+ "\n" +(!response.containsKey("ERROR->")?"": response.getString("ERROR->").toString()));
//							if(HiwifiApplication.context !=null && errorString!=null && !"".equals(errorString)){
//								DialogTool.createMessageDialog(HiwifiApplication.context, "Log日志", errorString.toString(), "确定", null, DialogTool.NO_ICON).show();
//							}
//						}
//						ToastUtil.showMessage(HiwifiApplication.context, HiwifiApplication.getErrorMap().get(String.valueOf(code)));
					}
				}/*else{
					ToastUtil.showMessage(HiwifiApplication.context, "网络不畅");
				}*/
			} catch (Exception e) {
				// TODO: handle exception
				e.printStackTrace();
			}
		}

}
