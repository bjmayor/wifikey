package com.hiwifi.utils;

import java.util.Timer;
import java.util.TimerTask;

import android.content.Intent;
import android.net.TrafficStats;
import android.os.Handler;
import android.os.Message;

public class CheckFlow {

	private Timer flowTimer;
	
	private static CheckFlow flow = new CheckFlow();
	
	
	private Handler handler;
	public static CheckFlow getInstance(){
		return flow;
	}

	private CheckFlow() {
	}
	public void setHandler(Handler handler) {
		this.handler = handler;
	}

	public void startCheckCurrentFlow() {
		if (flowTimer == null) {
			flowTimer = new Timer();
		}
		flowTimer.schedule(new TimerTask() {

			@Override
			public void run() {
//				currentFlow = currentFlow();
				if (isSurportTraffic() && handler != null) {
					Message message = Message.obtain();
					message.what = 11;
					Intent intent = new Intent();
					intent.putExtra("flow", currentFlow());
					message.obj = intent;
					handler.sendMessage(message);
				}
			}

		}, 0, 1000);

	}

	private void stopCheckFlow() {
		flowTimer.cancel();
		flowTimer = null;
	}

	private long last_flow, last_mobile_flow;
	private long currentFlow;
	private boolean isMobile;
	

	public long getCurrentFlow() {
		return currentFlow;
	}

	public boolean isMobile() {
		return isMobile;
	}
	
	public boolean isSurportTraffic(){
		if (TrafficStats.getTotalRxBytes() +TrafficStats.getTotalRxBytes() == -1 ) {
			return false;
		}
		return true;
	}

	private long currentFlow() {
		long current_mobile_flow = TrafficStats.getMobileTxBytes()
				+ TrafficStats.getMobileRxBytes();
		long current_total_flow = TrafficStats.getTotalRxBytes()
				+ TrafficStats.getTotalTxBytes();
		long mobile_speed = current_mobile_flow - last_mobile_flow;
		long total_speed = current_total_flow - last_flow;
		if (mobile_speed >= total_speed * 0.9) {
			isMobile = true;
		}
		last_flow = current_total_flow;
		last_mobile_flow = current_mobile_flow;
		return (total_speed - mobile_speed) << 1;
	}

}
