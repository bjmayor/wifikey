package com.seo.activity;

import android.support.v7.app.ActionBarActivity;
import android.support.v7.app.ActionBar;
import android.support.v4.app.Fragment;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.os.Build;

import com.qq.e.appwall.GdtAppwall;
import com.seo.activity.base.BaseActivity;
import com.seo.app.views.UINavigationView;
import com.seo.constant.ConfigConstant;
import com.seo.constant.ReleaseConstant;
import com.seo.wifikey.R;
import com.taobao.newxp.common.AlimmContext;
import com.taobao.newxp.common.ExchangeConstants;
import com.taobao.newxp.controller.ExchangeDataService;
import com.taobao.newxp.view.ExchangeViewManager;
import com.umeng.analytics.MobclickAgent;
import com.wandoujia.ads.sdk.Ads;

import net.youmi.android.offers.OffersManager;

import org.adver.score.recommendwall.RecommendWallSDK;
import org.adver.score.scorewall.ScoreWallSDK;

import cn.waps.AppConnect;
import cn.waps.AppListener;

public class AppRecommendActivity extends BaseActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_app_recommend);
        if (savedInstanceState == null) {
            getSupportFragmentManager().beginTransaction()
                    .add(R.id.container, new PlaceholderFragment())
                    .commit();
        }

        try {
            Ads.init(this, ConfigConstant.WDJ_AD_APPID, ConfigConstant.WDJ_AD_APPSECRET);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    protected void onClickEvent(View paramView) {

    }


    @Override
    public void onResume() {
        super.onResume();
        MobclickAgent.onResume(this);          //统计时长
    }


    @Override
    public void onPause() {
        super.onPause();
        MobclickAgent.onPause(this);
    }

    @Override
    protected void findViewById() {

    }

    @Override
    protected void loadViewLayout() {

    }

    @Override
    protected void processLogic() {

    }

    @Override
    protected void setListener() {

    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.app_recommend, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();
        switch (id) {
            case android.R.id.home:
                finish();
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void updateView() {

    }

    /**
     * A placeholder fragment containing a simple view.
     */
    public static class PlaceholderFragment extends Fragment implements View.OnClickListener {

        GdtAppwall appwall;
        private UINavigationView navigationView;

        public PlaceholderFragment() {
        }

        @Override
        public View onCreateView(LayoutInflater inflater, ViewGroup container,
                                 Bundle savedInstanceState) {
            View rootView = inflater.inflate(R.layout.fragment_app_recommend, container, false);
            rootView.findViewById(R.id.ll_recommend_by_wanpu).setOnClickListener(this);
            rootView.findViewById(R.id.ll_recommend_by_yjf).setOnClickListener(this);
            rootView.findViewById(R.id.ll_recommend_by_youmi).setOnClickListener(this);
            rootView.findViewById(R.id.ll_recommend_by_qq).setOnClickListener(this);
            rootView.findViewById(R.id.ll_recommend_by_wandoujia).setOnClickListener(this);
            navigationView = (UINavigationView) rootView.findViewById(R.id.nav);
            navigationView.getLeftButton().setOnClickListener(this);
            return rootView;
        }

        @Override
        public void onActivityCreated(Bundle savedInstanceState) {
            super.onActivityCreated(savedInstanceState);
            AlimmContext.getAliContext().init(getActivity());//必须保证这段代码最先执行
            View view = getActivity().findViewById(R.id.rlayout1);
            new ExchangeViewManager(getActivity(), new ExchangeDataService(ConfigConstant.SLOT_ID))
                    .addView(ExchangeConstants.type_list_curtain, view);
            appwall = new GdtAppwall(getActivity(), ConfigConstant.QQ_AD_APPID, ConfigConstant.QQ_AD_POSID, false);

        }

        @Override
        public void onClick(View view) {
            switch (view.getId()) {
                case R.id.nav_left_btn:
                    getActivity().finish();
                    break;
                case R.id.ll_recommend_by_wanpu:
                    AppConnect.getInstance(getActivity()).showOffers(getActivity());
                    break;
                case R.id.ll_recommend_by_yjf:
                    RecommendWallSDK.getInstance(getActivity()).showRecommendWall();
                    break;
                case R.id.ll_recommend_by_youmi:
                    OffersManager.getInstance(getActivity()).showOffersWall();
                    break;
                case R.id.ll_recommend_by_qq:
                    appwall.doShowAppWall();
                    break;
                case R.id.ll_recommend_by_wandoujia:
                    Ads.showAppWall(getActivity(), ConfigConstant.WDJ_AD_POSID);
                    break;
            }
        }
    }
}
