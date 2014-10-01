package com.hiwifi.model.wifi.webauth;

import android.webkit.JsPromptResult;
import android.webkit.JsResult;
import android.webkit.WebChromeClient;
import android.webkit.WebView;

final class HWFWebChromeClient extends WebChromeClient {

	public HWFWebChromeClient() {
	}

	@Override
	public final boolean onJsAlert(WebView view, String url, String message,
			JsResult result) {
		result.confirm();
		return true;
	}

	@Override
	public final boolean onJsConfirm(WebView view, String url, String message,
			JsResult result) {
		return true;
	}

	@Override
	public final boolean onJsPrompt(WebView view, String url, String message,
			String defaultValue, JsPromptResult result) {
		return true;
	}

}
