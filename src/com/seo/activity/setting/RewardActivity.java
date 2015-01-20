package com.seo.activity.setting;

import android.content.Intent;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;

import com.seo.activity.base.BaseActivity;
import com.seo.wifikey.R;

public class RewardActivity extends BaseActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

    }

    @Override
    protected void onClickEvent(View paramView) {

    }

    @Override
    protected void findViewById() {

    }

    @Override
    protected void loadViewLayout() {
        setContentView(R.layout.activity_reward);
    }

    @Override
    protected void processLogic() {

    }

    @Override
    protected void setListener() {
        findViewById(R.id.ll_score_3).setOnClickListener(this);
        findViewById(R.id.ll_score_5).setOnClickListener(this);
        findViewById(R.id.ll_score_10).setOnClickListener(this);
        findViewById(R.id.ll_score_20).setOnClickListener(this);
        findViewById(R.id.ll_score_50).setOnClickListener(this);
        findViewById(R.id.ll_score_100).setOnClickListener(this);
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_reward, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            Intent intent = new Intent();
            intent.setClass(RewardActivity.this, RewardHistoryActivity.class);
            startActivity(intent);
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void updateView() {

    }
}
