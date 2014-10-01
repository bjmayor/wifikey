package com.hiwifi.activity;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;

import com.hiwifi.activity.wifi.WiFiOperateActivity;
import com.hiwifi.constant.RequestConstant;
import com.hiwifi.constant.RequestConstant.RequestTag;
import com.hiwifi.hiwifi.R;
import com.hiwifi.model.ClientInfo;
import com.hiwifi.model.DiscoverItem;
import com.hiwifi.model.wifi.WifiAdmin;
import com.umeng.analytics.MobclickAgent;

public class UpgradeAppActivity extends Activity implements OnClickListener {

	private boolean isFirstOpen = true;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_update_app);
		// getSupportActionBar()
		getActionBar().hide();
		initListener();
		isFirstOpen = ClientInfo.shareInstance().isFirstStarted();

	}

	private void initListener() {
		findViewById(R.id.start_update).setOnClickListener(this);
		findViewById(R.id.skip_update).setOnClickListener(this);
	}

	@Override
	public void onClick(View v) {
		switch (v.getId()) {
		case R.id.start_update:
			// UmengUpdateAgent.setUpdateOnlyWifi(false);
			// UmengUpdateAgent.update(this);
			Intent upgrate = new Intent();
			upgrate.setAction("android.intent.action.VIEW");
//			String share = "http://m.hiwifi.com/xiazai/app?type=hiwififree";
			 Uri content_url = Uri.parse(RequestConstant
			 .getUrl(RequestTag.URL_APP_DOWNLOAD));
			upgrate.setData(content_url);
			startActivity(upgrate);
			break;

		case R.id.skip_update:
			Intent intent = new Intent();
			if (isFirstOpen) {
				ClientInfo.shareInstance().setFirstStarted(false);
				intent.setClass(this.getApplicationContext(),
						TutorialActivity.class);
			} else {
				if (DiscoverItem.showAdImage && DiscoverItem.loadedAdImge) {
					intent.setClass(this, AdvertisementActivity.class);
				} else {
					if (WifiAdmin.sharedInstance().isWifiEnable()) {
						intent.setClass(this.getApplicationContext(),
								MainTabActivity.class);
					} else {
						intent.setClass(this.getApplicationContext(),
								WiFiOperateActivity.class);
					}
				}
			}
			startActivity(intent);
			finish();
			break;

		default:
			break;
		}
	}
	
	@Override
	protected void onResume() {
		super.onResume();
		MobclickAgent.onPageStart(this.getClass().getSimpleName()); //统计页面
		MobclickAgent.onResume(this);          //统计时长
	}
	
	@Override
	protected void onPause() {
		super.onPause();
		MobclickAgent.onPageEnd(this.getClass().getSimpleName());
		MobclickAgent.onPause(this);
	}

}
