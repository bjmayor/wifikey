package com.seo.activity;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;

import com.seo.activity.wifi.WiFiOperateActivity;
import com.seo.model.ClientInfo;
import com.seo.model.wifi.WifiAdmin;
import com.seo.wifikey.R;
import com.umeng.analytics.MobclickAgent;
import com.umeng.update.UmengUpdateAgent;

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
                UmengUpdateAgent.setUpdateOnlyWifi(false);
                UmengUpdateAgent.update(this);
                break;

            case R.id.skip_update:
                Intent intent = new Intent();
                if (isFirstOpen) {
                    ClientInfo.shareInstance().setFirstStarted(false);
                    intent.setClass(this.getApplicationContext(),
                            TutorialActivity.class);
                } else {
                    if (WifiAdmin.sharedInstance().isWifiEnable()) {
                        intent.setClass(this.getApplicationContext(),
                                MainTabActivity.class);
                    } else {
                        intent.setClass(this.getApplicationContext(),
                                WiFiOperateActivity.class);
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
