package com.hiwifi.activity.setting;

import android.view.View;

import com.hiwifi.activity.base.BaseActivity;
import com.hiwifi.app.views.UINavigationView;
import com.hiwifi.hiwifi.R;

public class TermsOfServiceActivity extends BaseActivity{

	private UINavigationView nav;
	
	@Override
	protected void onClickEvent(View paramView) {
		if(paramView == nav.getLeftButton()){
			this.finish();
		}
	}

	@Override
	protected void findViewById() {
		nav = (UINavigationView) findViewById(R.id.nav);
	}

	@Override
	protected void loadViewLayout() {
		setContentView(R.layout.activity_terms);
	}

	@Override
	protected void processLogic() {
		
	}

	@Override
	protected void setListener() {
		nav.getLeftButton().setOnClickListener(this);
	}

	@Override
	protected void updateView() {
		// TODO Auto-generated method stub
		
	}

}
