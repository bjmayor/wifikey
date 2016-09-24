package com.seo.activity.setting;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;

import com.seo.activity.base.BaseActivity;
import com.seo.constant.ConfigConstant;
import com.seo.constant.ReleaseConstant;
import com.seo.model.StatEvent;
import com.seo.wifikey.R;
import com.umeng.analytics.MobclickAgent;

import net.youmi.android.offers.OffersManager;

import cn.waps.AppConnect;

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


        public PlaceholderFragment() {
        }

        @Override
        public View onCreateView(LayoutInflater inflater, ViewGroup container,
                                 Bundle savedInstanceState) {
            View rootView = inflater.inflate(R.layout.fragment_app_recommend, container, false);
            rootView.findViewById(R.id.ll_recommend_by_wanpu).setOnClickListener(this);
            rootView.findViewById(R.id.ll_recommend_by_youmi).setOnClickListener(this);
            return rootView;
        }

        @Override
        public void onActivityCreated(Bundle savedInstanceState) {
            super.onActivityCreated(savedInstanceState);
            View view = getActivity().findViewById(R.id.rlayout1);

        }

        @Override
        public void onClick(View view) {
            switch (view.getId()) {
                case R.id.ll_recommend_by_wanpu:
                    AppConnect.getInstance(getActivity()).showOffers(getActivity());
                    MobclickAgent.onEvent(getActivity(), StatEvent.CLICK_EVT_AD_PLATFORM, ReleaseConstant.ADPLATFORM.ADPLATFORM_WANPU.toString());
                    break;
                case R.id.ll_recommend_by_youmi:
                    OffersManager.getInstance(getActivity()).showOffersWall();
                    MobclickAgent.onEvent(getActivity(), StatEvent.CLICK_EVT_AD_PLATFORM, ReleaseConstant.ADPLATFORM.ADPLATFORM_YOUMI.toString());
                    break;


            }
        }
    }
}
