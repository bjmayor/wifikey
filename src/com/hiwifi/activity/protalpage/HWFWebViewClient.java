package com.hiwifi.activity.protalpage;

import android.webkit.WebView;
import android.webkit.WebViewClient;

final class HWFWebViewClient extends WebViewClient {

	public HWFWebViewClient() {
		super();
	}

	@Override
	public void onPageFinished(WebView view, String url) {
		// view.loadUrl("javascript:alert('load sucess');");
//		JSTestActivity.loading = false;
		super.onPageFinished(view, url);
	}

}
