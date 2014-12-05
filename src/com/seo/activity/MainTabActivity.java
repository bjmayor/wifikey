package com.seo.activity;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.view.ViewPager;
import android.support.v7.app.ActionBar;
import android.support.v7.app.ActionBarActivity;

import com.eadver.offer.recommendwall.RecommendAdListSDK;
import com.eadver.offer.sdk.YjfSDK;
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
public class MainTabActivity extends ActionBarActivity implements ActionBar.TabListener {
    ActionBar actionBar;
    private ViewPager mViewPager;
    private ViewPagerAdapter mViewPagerAdapter;
    private static final int TAB_INDEX_ONE = 0;
    private static final int TAB_INDEX_TWO = 1;
    private static final int TAB_INDEX_THREE = 2;
    private Fragment fragmentHome = new WifiListFragment();
    private Fragment fragmentDiscover = new MyAppFragment();
    private Fragment fragmentSetting = new SettingFragment();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main_tab_layout);
        AppConnect.getInstance(this);
        AppConnect.getInstance(this).initUninstallAd(this);
        AppConnect.getInstance(this).setWeixinAppId(ConfigConstant.WX_KEY, this);

        YjfSDK.getInstance(this, null).initInstance(ConfigConstant.YJF_APP_ID,
                ConfigConstant.YJF_APP_KEY,
                ConfigConstant.YJF_DEV_ID, Gl.getChannel());

        setUpActionBar();
        setUpViewPager();
        setUpTabs();
    }

    private void setUpActionBar() {
        actionBar = getSupportActionBar();
        actionBar.setNavigationMode(ActionBar.NAVIGATION_MODE_TABS);
        actionBar.setBackgroundDrawable(Gl.Ct().getResources().getDrawable(R.color.nav_background_color));
        actionBar.setLogo(R.drawable.logo_header);
        actionBar.setDisplayUseLogoEnabled(true);
    }

    private void setUpTabs() {
        ActionBar.Tab tabHome = actionBar.newTab().setText(R.string.free_wifi);
        ActionBar.Tab tabDiscover = actionBar.newTab().setText(R.string.discover);
        ActionBar.Tab tabSetting = actionBar.newTab().setText(R.string.setting);


        tabHome.setTabListener(this);
        tabDiscover.setTabListener(this);
        tabSetting.setTabListener(this);

        actionBar.addTab(tabHome);
        actionBar.addTab(tabDiscover);
        actionBar.addTab(tabSetting);
    }

    private void setUpViewPager() {
        mViewPagerAdapter = new ViewPagerAdapter(getSupportFragmentManager());

        mViewPager = (ViewPager) findViewById(R.id.pager);
        mViewPager.setAdapter(mViewPagerAdapter);
        mViewPager.setOnPageChangeListener(new ViewPager.SimpleOnPageChangeListener() {
            @Override
            public void onPageSelected(int position) {
                final ActionBar actionBar = getSupportActionBar();
                actionBar.setSelectedNavigationItem(position);
            }

            @Override
            public void onPageScrollStateChanged(int state) {
                switch (state) {
                    case ViewPager.SCROLL_STATE_IDLE:
                        //TODO
                        break;
                    case ViewPager.SCROLL_STATE_DRAGGING:
                        //TODO
                        break;
                    case ViewPager.SCROLL_STATE_SETTLING:
                        //TODO
                        break;
                    default:
                        //TODO
                        break;
                }
            }
        });
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
        RecommendAdListSDK.getInstance(this).onDestroy();
        YjfSDK.getInstance(this,null).recordAppClose();
    }

    @Override
    public void onTabSelected(ActionBar.Tab tab, FragmentTransaction fragmentTransaction) {
        mViewPager.setCurrentItem(tab.getPosition());
    }

    @Override
    public void onTabUnselected(ActionBar.Tab tab, FragmentTransaction fragmentTransaction) {

    }

    @Override
    public void onTabReselected(ActionBar.Tab tab, FragmentTransaction fragmentTransaction) {

    }


    public class ViewPagerAdapter extends FragmentPagerAdapter {
        public ViewPagerAdapter(FragmentManager fm) {
            super(fm);
        }

        @Override
        public Fragment getItem(int i) {
            switch (i) {
                case TAB_INDEX_ONE:
                    return fragmentHome;
                case TAB_INDEX_TWO:
                    return fragmentDiscover;
                case TAB_INDEX_THREE:
                    return fragmentSetting;
            }
            throw new IllegalStateException("No fragment at position " + i);
        }

        @Override
        public int getCount() {
            return 3;
        }
    }
}
