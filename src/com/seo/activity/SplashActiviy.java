package com.seo.activity;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.os.Bundle;
import android.os.Handler;
import android.support.v7.app.ActionBarActivity;
import android.view.KeyEvent;
import android.view.View;

import com.seo.activity.base.BaseActivity;
import com.seo.activity.wifi.WiFiOperateActivity;
import com.seo.constant.ConfigConstant;
import com.seo.constant.ReleaseConstant;
import com.seo.model.ClientInfo;
import com.seo.model.wifi.WifiAdmin;
import com.seo.utils.ImageUtil;
import com.seo.utils.ViewUtil;
import com.seo.wifikey.Gl;
import com.seo.wifikey.R;

import net.youmi.android.AdManager;
import net.youmi.android.offers.OffersManager;

import cn.waps.AppConnect;


public class SplashActiviy extends ActionBarActivity {

    private boolean hasUpgrate = false;
    private boolean isFirstOpen;

    // private boolean loadedAdImge = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_splash);
        isFirstOpen = ClientInfo.shareInstance().isFirstStarted();
        if (ReleaseConstant.getAdPlatform() == ReleaseConstant.ADPLATFORM.ADPLATFORM_YOUMI) {
            AdManager.getInstance(this).init(ConfigConstant.YOUMI_PUBLISH_ID, ConfigConstant.YOUMI_APP_SECRET, false);
            OffersManager.getInstance(this).onAppLaunch();
        }
        Bitmap bitmap = BitmapFactory.decodeResource(getResources(),
                R.drawable.startup_map);
        Bitmap adaptiveW = ImageUtil.adaptiveW(bitmap, ViewUtil.getScreenWidth());
        BitmapDrawable d = new BitmapDrawable(adaptiveW);
        postDelay();
        getSupportActionBar().hide();
    }


    // 延迟启动
    private void postDelay() {

        new Handler(getMainLooper()).postDelayed(new Runnable() {
            @Override
            public void run() {
                Intent intent = new Intent();

                if (WifiAdmin.sharedInstance().isWifiEnable()) {
                    intent.setClass(SplashActiviy.this,
                            MainTabActivity.class);
                } else {
                    intent.setClass(SplashActiviy.this,
                            WiFiOperateActivity.class);
                }
                startActivity(intent);
                finish();

            }
        }, 3000);
        AppConnect.getInstance(Gl.Ct()).initAdInfo();
    }


    private boolean destroyed = false;

    @Override
    protected void onDestroy() {
        // RequestManager.cancelRequest(this);
        destroyed = true;
        // thread.interrupted();
        super.onDestroy();
    }


    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_BACK) {
            return true;
        }
        return super.onKeyDown(keyCode, event);
    }


}
