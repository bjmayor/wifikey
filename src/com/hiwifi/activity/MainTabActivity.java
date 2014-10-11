package com.hiwifi.activity;

import android.content.res.Configuration;
import android.os.Bundle;
import android.support.v4.app.ActionBarDrawerToggle;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarActivity;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.RelativeLayout;

import com.hiwifi.activity.wifi.WifiListFragment;
import com.hiwifi.hiwifi.Gl;
import com.seo.wifikey.R;
import com.umeng.analytics.MobclickAgent;

import org.adver.score.sdk.YjfSDK;
import org.adver.score.sdk.widget.UpdateScordNotifier;

/**
 * @author jack at 2014-8-25
 * @filename MainTabActivity.java
 * @packagename com.hiwifi.activity
 * @projectname hiwifi1.0.1
 */
public class MainTabActivity extends ActionBarActivity implements
        UpdateScordNotifier {
    private DrawerLayout mDrawerLayout;
    private ActionBarDrawerToggle mDrawerToggle;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main_tab_layout);

        if (savedInstanceState == null) {
            FragmentTransaction transaction = getSupportFragmentManager()
                    .beginTransaction();
            transaction.add(R.id.left_drawer, new SettingFragment());
            transaction.add(R.id.content_frame, new WifiListFragment());
            transaction.commit();
        }
        mDrawerLayout = (DrawerLayout) findViewById(R.id.drawer_layout);
        mDrawerToggle = new ActionBarDrawerToggle(this, mDrawerLayout,
                R.drawable.menu_bg, R.string.drawer_open, R.string.drawer_close) {

            /** Called when a drawer has settled in a completely closed state. */
            public void onDrawerClosed(View view) {
                mMenu.findItem(R.id.action_refresh).setVisible(true);
            }

            /** Called when a drawer has settled in a completely open state. */
            public void onDrawerOpened(View drawerView) {
                mMenu.findItem(R.id.action_refresh).setVisible(false);
            }
        };
        mDrawerLayout.setDrawerListener(mDrawerToggle);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(false);
        getSupportActionBar().setHomeButtonEnabled(true);
//		YjfSDK.getInstance(this, null).setCoopInfo("用户 id"); //如无用户可自行设置 ,需要配置回调地址
        YjfSDK.getInstance(this, this).initInstance("72860", "EMI373QQVGBD2XHY9M24O3T30YTXIXHP81", "82214", Gl.getChannel());

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.main, menu);
        mMenu = menu;
        return super.onCreateOptionsMenu(menu);
    }

    Menu mMenu;
    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        // If the nav drawer is open, hide action items related to the content
        // view
        return super.onPrepareOptionsMenu(menu);
    }

    @Override
    protected void onPostCreate(Bundle savedInstanceState) {
        super.onPostCreate(savedInstanceState);
        // Sync the toggle state after onRestoreInstanceState has occurred.
        mDrawerToggle.syncState();
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        mDrawerToggle.onConfigurationChanged(newConfig);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Pass the event to ActionBarDrawerToggle, if it returns
        // true, then it has handled the app icon touch event
        switch (item.getItemId())
        {
            case R.id.action_refresh:
                WifiListFragment.instance.clickToRefresh();
                break;
        }
        if (mDrawerToggle.onOptionsItemSelected(item)) {
            return true;
        }
        // Handle your other action bar items...

        return super.onOptionsItemSelected(item);
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
        YjfSDK.getInstance(this, null).recordAppClose();//释放内存
        super.onBackPressed();
    }

    @Override
    public void updateScoreFailed(int arg0, int arg1, String arg2) {

    }

    @Override
    public void updateScoreSuccess(int arg0, int arg1, int arg2, String arg3) {

    }
}
