package com.seo.activity;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.ActionBar;
import android.support.v7.app.ActionBarActivity;

import com.seo.activity.wifi.WifiListFragment;
import com.seo.constant.ConfigConstant;
import com.seo.wifikey.Gl;
import com.seo.wifikey.R;
import com.umeng.analytics.MobclickAgent;


import cn.waps.AppConnect;

/**
 * @author jack at 2014-8-25
 * @filename MainTabActivity.java
 * @packagename com.hiwifi.activity
 * @projectname hiwifi1.0.1
 */
public class MainTabActivity extends ActionBarActivity {
    ActionBar actionBar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main_tab_layout);
        AppConnect.getInstance(this);
        AppConnect.getInstance(this).initUninstallAd(this);
        AppConnect.getInstance(this).setWeixinAppId(ConfigConstant.WX_KEY, this);
        AppConnect.getInstance(Gl.Ct()).initAdInfo();
        actionBar = getSupportActionBar();
        actionBar.setNavigationMode(ActionBar.NAVIGATION_MODE_TABS);
        actionBar.setBackgroundDrawable(Gl.Ct().getResources().getDrawable(R.color.nav_background_color));
        actionBar.setLogo(R.drawable.logo_header);
        actionBar.setDisplayUseLogoEnabled(true);
        ActionBar.Tab tabHome = actionBar.newTab().setText(R.string.free_wifi);
        ActionBar.Tab tabDiscover = actionBar.newTab().setText(R.string.discover);
        ActionBar.Tab tabSetting = actionBar.newTab().setText(R.string.setting);

        Fragment fragmentHome = new WifiListFragment();
        Fragment fragmentDiscover = new MyAppFragment();
        Fragment fragmentSetting = new SettingFragment();

        tabHome.setTabListener(new MyTabListener(fragmentHome));
        tabDiscover.setTabListener(new MyTabListener(fragmentDiscover));
        tabSetting.setTabListener(new MyTabListener(fragmentSetting));

        actionBar.addTab(tabHome);
        actionBar.addTab(tabDiscover);
        actionBar.addTab(tabSetting, true);

//        YjfSDK.getInstance(this, this).initInstance("72860", "EMI373QQVGBD2XHY9M24O3T30YTXIXHP81", "82214", Gl.getChannel());

    }


    @Override
    protected void onDestroy() {
        super.onDestroy();
    }


    @Override
    protected void onResume() {
        super.onResume();
        MobclickAgent.onResume(this);          //统计时长
    }

    @Override
    protected void onPause() {
        super.onPause();
        MobclickAgent.onPause(this);
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
    }


    public class MyTabListener implements ActionBar.TabListener {
        private Fragment fragment;

        public MyTabListener(Fragment fragment) {
            this.fragment = fragment;
        }

        @Override
        public void onTabSelected(ActionBar.Tab tab, FragmentTransaction fragmentTransaction) {
            fragmentTransaction.replace(R.id.fl_content, this.fragment, null);
        }

        @Override
        public void onTabUnselected(ActionBar.Tab tab, FragmentTransaction fragmentTransaction) {

        }

        @Override
        public void onTabReselected(ActionBar.Tab tab, FragmentTransaction fragmentTransaction) {

        }
    }
}
