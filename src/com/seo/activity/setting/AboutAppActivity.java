package com.seo.activity.setting;

import android.content.Intent;
import android.util.EventLogTags;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import com.seo.activity.CommonWebviewActivity;
import com.seo.activity.base.BaseActivity;
import com.seo.app.views.UINavigationView;
import com.seo.constant.RequestConstant;
import com.seo.constant.RequestConstant.RequestTag;
import com.seo.wifikey.Gl;
import com.seo.wifikey.R;

public class AboutAppActivity extends BaseActivity {

    private Button offiWebsite;
    private TextView versionTextView;

    @Override
    protected void onClickEvent(View v) {
        switch (v.getId()) {
            case R.id.bt_offi_website:
                Intent website = new Intent(this, CommonWebviewActivity.class);
                website.putExtra("type", "webset");
                website.putExtra("title", "官方网站");
                website.putExtra("url", RequestConstant
                        .getUrl(RequestTag.HIWIFI_PAGE_OFFICE_WEBSITE));
                startActivity(website);
                break;

            default:
                break;
        }
    }

    @Override
    protected void findViewById() {
        offiWebsite = (Button) findViewById(R.id.bt_offi_website);
        versionTextView = (TextView) findViewById(R.id.tv_app_version);
        setTitle("关于" + getResources().getString(R.string.app_name));
        versionTextView.setText("版本:" + Gl.getAppVersionName());
    }

    @Override
    protected void loadViewLayout() {
        setContentView(R.layout.activity_about_app);
    }

    @Override
    protected void processLogic() {
    }

    @Override
    protected void setListener() {
        offiWebsite.setOnClickListener(this);
    }

    @Override
    protected void updateView() {

    }

}
