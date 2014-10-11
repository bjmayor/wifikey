package com.hiwifi.activity;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.os.Handler;
import android.view.KeyEvent;
import android.view.View;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.RelativeLayout.LayoutParams;
import android.widget.TextView;

import com.hiwifi.activity.base.BaseActivity;
import com.hiwifi.activity.wifi.WiFiOperateActivity;
import com.hiwifi.hiwifi.Gl;
import com.hiwifi.model.ClientInfo;
import com.hiwifi.model.log.LogUtil;
import com.hiwifi.model.wifi.WifiAdmin;
import com.hiwifi.utils.ImageUtil;
import com.seo.wifikey.R;

import cn.sharesdk.framework.ShareSDK;

public class SplashActiviy extends BaseActivity  {

    private ImageView map;
    private boolean hasUpgrate = false;
    private boolean isFirstOpen;

    // private boolean loadedAdImge = false;

    @Override
    protected void onClickEvent(View paramView) {

    }

    @Override
    protected void findViewById() {
        ShareSDK.initSDK(this);
        map = (ImageView) findViewById(R.id.splash_map);
        TextView version = (TextView) findViewById(R.id.version);
        version.setText(Gl.getAppVersionName());
        // initView();
        isFirstOpen = ClientInfo.shareInstance().isFirstStarted();
    }

    private void initView() {
        Bitmap bitmap = BitmapFactory.decodeResource(getResources(),
                R.drawable.startup_map);
        Bitmap adaptiveW = ImageUtil.adaptiveW(bitmap, screenWidth);
        BitmapDrawable d = new BitmapDrawable(adaptiveW);
        RelativeLayout.LayoutParams lp = (LayoutParams) map.getLayoutParams();
        lp.width = adaptiveW.getWidth();
        lp.height = adaptiveW.getHeight();
        map.setLayoutParams(lp);
        map.setBackgroundDrawable(d);

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
