package com.hiwifi.activity.protalpage;

import android.annotation.SuppressLint;
import android.content.Context;
import android.util.AttributeSet;
import android.webkit.WebChromeClient;
import android.webkit.WebSettings;
import android.webkit.WebView;

@SuppressLint("SetJavaScriptEnabled")
public class MockRedirectWebView extends WebView {

	private String mJSString;

	public MockRedirectWebView(Context context) {
		super(context);
		init();
	}

	public MockRedirectWebView(Context context, AttributeSet attrs) {
		super(context, attrs);
		init();
	}

	public MockRedirectWebView(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
		init();
	}

	//
	// public static final String checkUrl() {
	// return "http://check.51y5.net/check.html?t="
	// + System.currentTimeMillis();
	// }

	private void init() {
		WebSettings webSettings = getSettings();
		webSettings.setJavaScriptEnabled(true);
		webSettings.setJavaScriptCanOpenWindowsAutomatically(true);
		webSettings.setDefaultTextEncodingName("UTF-8");
		webSettings.setUseWideViewPort(true);
		webSettings.setLoadWithOverviewMode(true);
		webSettings.setBuiltInZoomControls(true);
		setWebViewClient(new HWFWebViewClient());
		setWebChromeClient(new WebChromeClient());
	}

	//
	// public void login(String username, String password)
	// {
	// this.mExcuted = true;
	// loadUrl("javascript: wifilocating_auto_login(\"" + username + "\", \"" +
	// password + "\"); ");
	// this.mHandler.sendEmptyMessageDelayed(2, 5000L);
	// }
	//
	public void setJsString(String jsString) {
		mJSString = "javascript:" + jsString;
	}

	public void runJS() {
		loadUrl("javascript:");
		// loadData("<b>123</b>", "text/html", "UTF-8");
	}

	public void execjs(String jscode) {
		// loadData("<img src='http://www.2cto.com/daili/images/daili.gif'>",
		// "text/html", "UTF-8");
		setJsString(jscode);
		loadUrl(this.mJSString);
	}

}
