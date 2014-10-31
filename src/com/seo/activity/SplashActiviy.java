package com.seo.activity;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.os.Handler;
import android.view.KeyEvent;
import android.view.View;

import com.seo.activity.base.BaseActivity;
import com.seo.activity.wifi.WiFiOperateActivity;
import com.seo.model.ClientInfo;
import com.seo.model.wifi.WifiAdmin;
import com.seo.utils.ImageUtil;
import com.seo.wifikey.R;


public class SplashActiviy extends BaseActivity  {

    private boolean hasUpgrate = false;
    private boolean isFirstOpen;

    // private boolean loadedAdImge = false;

    @Override
    protected void onClickEvent(View paramView) {

    }

    @Override
    protected void findViewById() {
        // initView();
        isFirstOpen = ClientInfo.shareInstance().isFirstStarted();
    }

    private void initView() {
        Bitmap bitmap = BitmapFactory.decodeResource(getResources(),
                R.drawable.startup_map);
        Bitmap adaptiveW = ImageUtil.adaptiveW(bitmap, screenWidth);
        BitmapDrawable d = new BitmapDrawable(adaptiveW);

    }

    @Override
    protected void loadViewLayout() {
        setContentView(R.layout.activity_splash);
    }

    @Override
    protected void processLogic() {
        postDelay();


        // if (DiscoverItem.showAdImage
        // && !TextUtils.isEmpty(DiscoverItem.showedImageUrl)) {
        // loadAdImage();
        // }
    }


    @Override
    protected void setListener() {

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
    protected void updateView() {
        // TODO Auto-generated method stub

    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_BACK) {
            return true;
        }
        return super.onKeyDown(keyCode, event);
    }


}
