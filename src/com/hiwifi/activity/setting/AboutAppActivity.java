package com.hiwifi.activity.setting;

import android.content.Intent;
import android.view.View;
import android.widget.Button;

import com.hiwifi.activity.CommonWebviewActivity;
import com.hiwifi.activity.TutorialActivity;
import com.hiwifi.activity.base.BaseActivity;
import com.hiwifi.app.views.UINavigationView;
import com.hiwifi.constant.RequestConstant;
import com.hiwifi.constant.RequestConstant.RequestTag;
import com.hiwifi.hiwifi.R;

public class AboutAppActivity extends BaseActivity {

	private Button bt_version, offi_website;
	private UINavigationView navigationView;

	@Override
	protected void onClickEvent(View v) {
		if (v == navigationView.getLeftButton()) {
			finish();
			return;
		}
		switch (v.getId()) {
		case R.id.bt_version:
			Intent version = new Intent(this, TutorialActivity.class);
			version.putExtra("from", "about");
			startActivity(version);
			break;
		case R.id.bt_offi_website:
			Intent website = new Intent(this, CommonWebviewActivity.class);
			website.putExtra("type", "webset");
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
		bt_version = (Button) findViewById(R.id.bt_version);
		offi_website = (Button) findViewById(R.id.bt_offi_website);
		navigationView = (UINavigationView) findViewById(R.id.nav);
		navigationView.setTitle("关于 " + getResources().getString(R.string.app_name));
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
		bt_version.setOnClickListener(this);
		offi_website.setOnClickListener(this);
		navigationView.getLeftButton().setOnClickListener(this);
	}

	@Override
	protected void updateView() {
		
	}

}
