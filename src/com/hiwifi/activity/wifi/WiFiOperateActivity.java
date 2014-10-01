package com.hiwifi.activity.wifi;

import android.content.Intent;
import android.net.NetworkInfo;
import android.net.wifi.SupplicantState;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.os.Handler;
import android.view.KeyEvent;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import com.hiwifi.activity.MainTabActivity;
import com.hiwifi.activity.base.BaseActivity;
import com.hiwifi.app.receiver.HiwifiBroadcastReceiver;
import com.hiwifi.app.receiver.HiwifiBroadcastReceiver.WifiEventHandler;
import com.hiwifi.hiwifi.R;
import com.hiwifi.model.wifi.WifiAdmin;

/**
 * @filename WiFiOperateActivity.java
 * @packagename com.hiwifi.activity
 * @projectname hiwifi1.0.1
 * @author jack at 2014-8-30
 */
public class WiFiOperateActivity extends BaseActivity {

	private Button wifiBtnOpen;
	private String TAG = null;
	public static final String KEYTAG = "TAG";
	private Handler handler = new Handler();

	@Override
	protected void onClickEvent(View paramView) {
		if (paramView == wifiBtnOpen) {
			if (WifiAdmin.sharedInstance().isWifiEnable()) {
				intentToMain();
			} else {
				WifiAdmin.sharedInstance().openNetCard();
				handler.postDelayed(timeoutRunnable, 5000);
			}

		}
	}

	@Override
	protected void findViewById() {
		wifiBtnOpen = (Button) findViewById(R.id.open_btn_wifi);
		Intent intent = getIntent();
		if (intent != null) {
			TAG = intent.getStringExtra(KEYTAG);
		}

	}

	@Override
	protected void loadViewLayout() {
		setContentView(R.layout.wifioperate);
	}

	@Override
	protected void processLogic() {
	}

	@Override
	protected void setListener() {
		wifiBtnOpen.setOnClickListener(this);
	}

	@Override
	public void onResume() {
		HiwifiBroadcastReceiver.addListener(wifiEventHandler);
		super.onResume();
	}

	@Override
	public void onPause() {
		HiwifiBroadcastReceiver.removeListener(wifiEventHandler);
		super.onPause();
	}

	Runnable timeoutRunnable = new Runnable() {

		@Override
		public void run() {
			Toast.makeText(WiFiOperateActivity.this, "开启超时，请进入系统设置开启",
					Toast.LENGTH_SHORT).show();
			wifiBtnOpen.setEnabled(true);
		}
	};

	WifiEventHandler wifiEventHandler = new WifiEventHandler() {

		@Override
		public void onWifiStatusChange(int state, int preState) {
			handler.removeCallbacks(timeoutRunnable);
			switch (state) {
			case WifiManager.WIFI_STATE_ENABLED:
				closeMyDialog();
				intentToMain();
				break;
			case WifiManager.WIFI_STATE_ENABLING:
				showMyDialog("正在开启WiFi");
				wifiBtnOpen.setEnabled(false);
				break;
			case WifiManager.WIFI_STATE_DISABLED:
				wifiBtnOpen.setEnabled(true);
				break;

			default:
				break;
			}
		}

		@Override
		public void onWifiConnectChange(NetworkInfo networkInfo, String BSSID,
				WifiInfo wifiInfo) {

		}

		@Override
		public void onSupStatusChange(SupplicantState newState, int error) {

		}

		@Override
		public void onSupConnectChange(Boolean isConnected) {

		}

		@Override
		public void onScanResultAvaiable() {

		}

		@Override
		public void onRssiChange(int newRssi) {

		}

		@Override
		public void onNetworkIdChange() {

		}
	};

	private void intentToMain() {
		Intent intent = new Intent(WiFiOperateActivity.this,
				MainTabActivity.class);
		intent.addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP);
		intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
		startActivity(intent);
		finish();
	}

	public boolean onKeyDown(int keyCode, android.view.KeyEvent event) {
		if (keyCode == KeyEvent.KEYCODE_BACK) {
			if (WifiAdmin.sharedInstance().isWifiEnable()) {
				intentToMain();
			} else {
				Toast.makeText(this, "请点击开启WiFi", Toast.LENGTH_SHORT).show();
			}
			return true;
		}
		return super.onKeyDown(keyCode, event);
	}

	@Override
	protected void updateView() {
		
	};
}
