package com.hiwifi.shareSdk;

import java.util.HashMap;

import android.content.Context;
import android.os.Handler;
import android.os.Message;
import cn.sharesdk.framework.Platform;
import cn.sharesdk.framework.PlatformActionListener;

public class OneKeyShareCallback implements PlatformActionListener {

	Handler handler ;
	private Context context;

	public OneKeyShareCallback(Handler handl) {
		this.handler = handl;
	}

	public OneKeyShareCallback(Context context) {
		this.context = context;
	}

	@Override
	public void onCancel(Platform palt, int action) {
		// System.out.println("取消的回调" + action);
		 Message msg = new Message();
		 msg.arg1 = 3;
		 msg.arg2 = action;
		 msg.obj = palt;
		 handler.sendMessage(msg);
	}

	@Override
	public void onComplete(Platform plat, int action,
			HashMap<String, Object> arg2) {
		// TODO Auto-generated method stub
		// System.out.println("完成的回调" + plat.toString() + "行为：" + action + "参数"
		// + arg2.toString());
		Message msg = new Message();
		msg.arg1 = 1;
		msg.arg2 = action;
		msg.obj = plat;
		handler.sendMessage(msg);
//		sendShareToServer(plat.toString());
	
	}

	@Override
	public void onError(Platform palt, int action, Throwable arg2) {
		// System.out.println("失败的回调" + action + "e:" + arg2.toString());
		Message msg = new Message();
		msg.arg1 = 2;
		msg.arg2 = action;
		msg.obj = palt;
		handler.sendMessage(msg);
	}


}
