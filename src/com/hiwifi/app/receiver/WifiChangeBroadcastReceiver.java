package com.hiwifi.app.receiver;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;

public class WifiChangeBroadcastReceiver extends BroadcastReceiver{

	
	private WifiListener listener;
	
	public WifiChangeBroadcastReceiver(WifiListener listener) {
		this.listener = listener;
	}

	@Override
	public void onReceive(Context context, Intent intent) {
		 String action = intent.getAction();
         if (action.equals(ConnectivityManager.CONNECTIVITY_ACTION)) {
             ConnectivityManager connectivityManager = (ConnectivityManager)context.getSystemService(Context.CONNECTIVITY_SERVICE);
              NetworkInfo info = connectivityManager.getActiveNetworkInfo();  
             if(info != null && info.isAvailable()) {
             } else {
            	 if(listener != null){
            		 listener.onWifiClosed();
            	 }
			}
         }
	}
	
	public abstract interface WifiListener {
		public abstract void onWifiClosed();
	}

}
