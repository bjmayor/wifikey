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

import com.seo.activity.base.BaseActivity;
import com.seo.constant.ReleaseConstant;
import com.seo.wifikey.R;

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
        getActionBar().show();
        getActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
    }

    @Override
    protected void onClickEvent(View paramView) {

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
        switch (id)
        {
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
            rootView.findViewById(R.id.ll_recommend_by_yjf).setOnClickListener(this);
            rootView.findViewById(R.id.ll_recommend_by_youmi).setOnClickListener(this);
            return rootView;
        }

        @Override
        public void onClick(View view) {
            switch (view.getId()) {
                case R.id.ll_recommend_by_wanpu:
                    AppConnect.getInstance(getActivity()).showOffers(getActivity());
                    break;
                case R.id.ll_recommend_by_yjf:
                    RecommendWallSDK.getInstance(getActivity()).showRecommendWall();
                    break;
                case R.id.ll_recommend_by_youmi:
                    OffersManager.getInstance(getActivity()).showOffersWall();
                    break;
            }
        }
    }
}
