package com.seo.activity;

import android.content.Intent;
import android.graphics.Bitmap;
import android.text.TextUtils;
import android.view.View;
import android.view.View.OnClickListener;
import android.webkit.WebView;
import android.webkit.WebViewClient;

import com.seo.activity.base.BaseActivity;
import com.seo.activity.protalpage.MockRedirectWebView;
import com.seo.app.views.UINavigationView;
import com.seo.wifikey.R;

/**
 * @filename PortalPageActivity.java
 * @packagename com.hiwifi.activity
 * @projectname hiwifi1.0.1
 * @author jack at 2014-9-9
 */

public class PortalPageActivity extends BaseActivity {
	public static boolean is_open = false;
	private final String URL = "http://www.baidu.com";
	private MockRedirectWebView webview;

	@Override
	protected void onClickEvent(View paramView) {
	}

	@Override
	protected void findViewById() {
		webview = (MockRedirectWebView) findViewById(R.id.webview);
		Intent i = getIntent();
		if (i != null) {
			String ssid = getIntent().getStringExtra("ssid");
			String title1 = i.getStringExtra("title");
				setTitle(title1);
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
