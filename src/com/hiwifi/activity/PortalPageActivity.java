package com.hiwifi.activity;

import android.content.Intent;
import android.graphics.Bitmap;
import android.text.TextUtils;
import android.view.View;
import android.view.View.OnClickListener;
import android.webkit.WebView;
import android.webkit.WebViewClient;

import com.hiwifi.activity.base.BaseActivity;
import com.hiwifi.activity.protalpage.MockRedirectWebView;
import com.hiwifi.app.views.UINavigationView;
import com.hiwifi.hiwifi.R;

/**
 * @filename PortalPageActivity.java
 * @packagename com.hiwifi.activity
 * @projectname hiwifi1.0.1
 * @author jack at 2014-9-9
 */

public class PortalPageActivity extends BaseActivity {
	public static boolean is_open = false;
	private UINavigationView nav;
	private final String URL = "http://www.baidu.com";
	private MockRedirectWebView webview;

	@Override
	protected void onClickEvent(View paramView) {
	}

	@Override
	protected void findViewById() {
		nav = (UINavigationView) findViewById(R.id.nav);
		webview = (MockRedirectWebView) findViewById(R.id.webview);
		Intent i = getIntent();
		if (i != null) {
			String ssid = getIntent().getStringExtra("ssid");
			String title1 = i.getStringExtra("title");
			if (!TextUtils.isEmpty(title1)) {
				nav.setTitle(title1);
			} else if (!TextUtils.isEmpty(ssid)) {
				nav.setTitle(ssid);
			}
		}
		webview.loadUrl(URL);
	}

	@Override
	protected void loadViewLayout() {
		setContentView(R.layout.activity_portal);
	}

	@Override
	protected void processLogic() {

	}

	@Override
	protected void setListener() {
		nav.getLeftButton().setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				finish();
			}
		});
		webview.setWebViewClient(new WebViewClient(){
			@Override
			public void onPageStarted(WebView view, String url, Bitmap favicon) {
				super.onPageStarted(view, url, favicon);
				showMyDialog("正在加载");
			}
			@Override
			public void onPageFinished(WebView view, String url) {
				super.onPageFinished(view, url);
				closeMyDialog();
			}
		});
	}
	@Override
	protected void onDestroy() {
		// TODO Auto-generated method stub
		super.onDestroy();
		is_open = false;
	}

	@Override
	protected void updateView() {
		// TODO Auto-generated method stub
		
	}
}
