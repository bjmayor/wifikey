package com.seo.activity;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.ColorDrawable;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.text.TextUtils;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup.LayoutParams;
import android.view.inputmethod.InputMethodManager;
import android.webkit.CookieManager;
import android.webkit.CookieSyncManager;
import android.webkit.HttpAuthHandler;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.PopupWindow;
import android.widget.PopupWindow.OnDismissListener;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;

import com.seo.activity.base.BaseActivity;
import com.seo.constant.RequestConstant;
import com.seo.model.request.RequestManager.ResponseHandler;
import com.seo.model.request.ServerResponseParser;
import com.seo.support.http.AsyncHttpClient;
import com.seo.utils.HttpRequestParser;
import com.seo.utils.ImageUtil;
import com.seo.utils.ViewUtil;
import com.seo.wifikey.R;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URL;
import java.net.URLConnection;

import javax.servlet.http.HttpServletRequest;

public class CommonWebviewActivity extends BaseActivity implements
        ResponseHandler {

    public static String bnlKeyUrl = "url";
    public static String bnlKeyType = "type";
    public static String bnlKeyPostData = "postData";
    public static String bnlKeyTitle = "title";
    public final static String bnlKeyFrom = "from";

    public enum WebType {
        unkown(""), // 未定义
        bindRouter("bindRouter"), // 绑定路由器
        push("push"), // 推送wap
        webset("webset"), // 官网
        forget_password("forget_password"), // 忘记密码
        email_register("email_register"), // 邮箱注册
        buy_router("buy_router");// 购买路由器
        private String type;

        private WebType(String type) {
            this.type = type;
        }

        @Override
        public String toString() {
            return type;
        }
    }

    private ProgressBar mProgressBar;
    private WebView mWebView;
    private AsyncHttpClient asyncHttpClient;

    private WebSettings mWebSettings;
    private String url = "", postData = "", title = "", from = "";
    private WebType type = WebType.unkown;
    public SharedPreferences loginData;
    private Bundle bundle;
    private BitmapDrawable bitlogo;
    private Handler handler = new Handler() {
        public void handleMessage(android.os.Message msg) {
        }

        ;
    };


    private InputMethodManager im;

    private void hideInput() {
        if (im != null && im.isActive()) {
            im.hideSoftInputFromWindow(this.getCurrentFocus().getWindowToken(),
                    0);
        }
    }

    @Override
    protected void onClickEvent(View paramView) {

    }

    @Override
    protected void findViewById() {
        im = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
        bundle = getIntent().getExtras();
        try {
            type = WebType.valueOf(bundle.getString(bnlKeyType));
        } catch (Exception e) {
            type = WebType.unkown;
        }
        url = bundle.getString(bnlKeyUrl);
        postData = bundle.getString(bnlKeyPostData);
        title = bundle.getString(bnlKeyTitle);
        from = bundle.getString(bnlKeyFrom);
        mWebView = (WebView) findViewById(R.id.webView);
        mProgressBar = (ProgressBar) findViewById(R.id.progressBar_comm);

        View page_container = findViewById(R.id.webview_rootlayout);
        // ViewTreeObserver vto = mTvTitle.getViewTreeObserver();
        // vto.addOnPreDrawListener(new ViewTreeObserver.OnPreDrawListener() {
        // public boolean onPreDraw() {
        // // System.out.println("---------------+"
        // // + mTvTitle.getMeasuredHeight());
        // logoHeight = (int) (mTvTitle.getMeasuredHeight());
        // int width = mTvTitle.getMeasuredWidth();
        // return true;
        // }
        // });
        shadow = (RelativeLayout) findViewById(R.id.shadow);
    }




    @Override
    protected void loadViewLayout() {
        setContentView(R.layout.activity_comm_webview);
    }

    @Override
    protected void processLogic() {

        mWebView.loadUrl(url);
    }

    private Bundle resultBundle = null;
    private Intent resultIntent = null;
    private int logoHeight;

    @Override
    protected void setListener() {

        mWebSettings = mWebView.getSettings();
        mWebSettings.setJavaScriptEnabled(true);
        mWebSettings.setUseWideViewPort(true);
        mWebSettings.setLoadWithOverviewMode(true);
        mWebSettings.setDisplayZoomControls(false);
        mWebSettings.setBuiltInZoomControls(true);
        mWebSettings.setJavaScriptCanOpenWindowsAutomatically(true);
        mWebView.setWebViewClient(new WebViewClient() {

            @Override
            public void onPageFinished(WebView view, String url) {
                // 解析html
                view.loadUrl("javascript:window.local_obj.showSource('<head>'+"
                        + "document.getElementsByTagName('html')[0].innerHTML+'</head>');");
                closeMyDialog();
                // mProgressBar.setVisibility(View.INVISIBLE);
                super.onPageFinished(view, url);

            }

            @Override
            public void onReceivedError(WebView view, int errorCode,
                                        String description, String failingUrl) {
                super.onReceivedError(view, errorCode, description, failingUrl);
                if (failingUrl != null && !failingUrl.startsWith("file:")
                        && errorCode == ERROR_HOST_LOOKUP) {
                    view.loadUrl("file:///android_asset/wifikey_error.html");
                    view.loadUrl("javascript:setRiderct(" + failingUrl + ")");
                }

            }

            @Override
            public void onPageStarted(WebView view, String url, Bitmap favicon) {
                super.onPageStarted(view, url, favicon);
                // mProgressBar.setVisibility(View.VISIBLE);
                showMyDialog("正在加载");
            }

            @Override
            public void onReceivedHttpAuthRequest(WebView view,
                                                  HttpAuthHandler handler, String host, String realm) {
                super.onReceivedHttpAuthRequest(view, handler, host, realm);
            }

            // @Override
            // public void onReceivedLoginRequest(WebView view, String realm,
            // String account, String args) {
            // super.onReceivedLoginRequest(view, realm, account, args);
            // }

            @Override
            public boolean shouldOverrideUrlLoading(WebView view, String url) {
                // System.out.println("url===" + url);
                if (url.contains("app://callback")) {
                    resultIntent = new Intent();
                    resultBundle = new Bundle();
                    HttpServletRequest request = HttpRequestParser.parse(url);
                    if (type == WebType.email_register) { // 注册的回调
                        if (url.contains("uid")) {
                            resultBundle.putString("uid",
                                    request.getParameter("uid"));
                            resultBundle.putString("token",
                                    request.getParameter("token"));
                            resultBundle.putString("email",
                                    request.getParameter("email"));
                            resultBundle.putString("registType", "email");
                            resultIntent.putExtras(resultBundle);
                            CommonWebviewActivity.this.setResult(0x101,
                                    resultIntent);
                            CommonWebviewActivity.this.finish();
                        } else if (url.contains("tophoneregister")) {
                            CommonWebviewActivity.this.setResult(0x101);
                            CommonWebviewActivity.this.finish();
                        } else {
                            // app://callback?code=645&msg=请使用刚才注册的邮箱登录&action=login&email=blueachaog@hotmail123.com
                            String email = url.substring(
                                    url.lastIndexOf("=") + 1, url.length());
                            resultBundle.putString("uid", "");
                            resultBundle.putString("token", "");
                            resultBundle.putString("email",
                                    request.getParameter("email"));
                            resultBundle.putString("registType", "email");
                            resultIntent.putExtras(resultBundle);
                            CommonWebviewActivity.this.setResult(0,
                                    resultIntent);
                            CommonWebviewActivity.this.finish();
                        }

                        // app://callback?action=client_bind&code=0&msg=OK&mac=D4EE0703E18C

                    } else /* if ("set_app".equalsIgnoreCase(type)) */ {
                        String code = request.getParameter("code");
                        String msg = request.getParameter("msg");
                        resultBundle.putString("code", code);
                        resultBundle.putString("msg", msg);
                        resultBundle.putString("action",
                                request.getParameter("action"));
                        resultIntent.putExtras(resultBundle);
                        setResult(0, resultIntent);
                        CommonWebviewActivity.this.finish();
                        return true;
                    }

                    return true;
                } else {
                    if (url.endsWith(".apk")) {
                        Uri uri = Uri.parse(url);
                        Intent viewIntent = new Intent(Intent.ACTION_VIEW, uri);
                        CommonWebviewActivity.this.startActivity(viewIntent);
                        return true;
                    } else {
                        return super.shouldOverrideUrlLoading(view, url);

                    }
                }
            }
        });

    }

    private void autoOtherLog() {
        CookieSyncManager.createInstance(this);
        CookieManager cookieManager = CookieManager.getInstance();
        cookieManager.removeSessionCookie();
        cookieManager.removeAllCookie();
        cookieManager.removeExpiredCookie();
        mWebView.loadUrl(url);
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
    }


    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if ((keyCode == KeyEvent.KEYCODE_BACK) && mWebView.canGoBack()) {
            mWebView.goBack();
            return true;
        } else {
            if ("AdvertisementActivity".equals(from)) {
                Intent intent = new Intent();
                intent.setClass(CommonWebviewActivity.this,
                        MainTabActivity.class);
                startActivity(intent);
                finish();
            }
        }
        return super.onKeyDown(keyCode, event);
    }


    private void setLogoImage() {
        File filesDir = CommonWebviewActivity.this.getFilesDir();
        String absolutePath = filesDir.getAbsolutePath();
        String path = absolutePath + "jindong.png";
        File f = new File(path);
        if (f.exists()) {
            Bitmap file = BitmapFactory.decodeFile(path);
            if (file != null) {
                BitmapDrawable drawable = new BitmapDrawable(file);
            }
        }
    }

    @Override
    public void onStart(RequestConstant.RequestIdentify identify, Code code) {

    }

    @Override
    public void onSuccess(RequestConstant.RequestIdentify identify, ServerResponseParser responseParser) {
        try {
            final String logo = responseParser.originResponse.optString("logo",
                    "");
            String buyTitle = responseParser.originResponse.optString("title",
                    "");
            if (!TextUtils.isEmpty(buyTitle)) {
            }
            if (!TextUtils.isEmpty(logo)) {

                Thread thread = new Thread() {
                    public void run() {
                        try {
                            URL url = new URL(logo);
                            URLConnection conn = url.openConnection();
                            conn.connect();
                            InputStream is = conn.getInputStream();
                            conn.setConnectTimeout(30000);
                            BufferedInputStream bis = new BufferedInputStream(
                                    is);
                            Bitmap bm = BitmapFactory.decodeStream(bis);
                            int dip2px = ViewUtil.dip2px(
                                    CommonWebviewActivity.this, logoHeight);
                            Bitmap adaptiveH = ImageUtil.adaptiveH(bm, dip2px);
                            bitlogo = new BitmapDrawable(adaptiveH);
                            handler.sendEmptyMessage(0);
                            File filesDir = CommonWebviewActivity.this
                                    .getFilesDir();
                            String absolutePath = filesDir.getAbsolutePath();
                            String path = absolutePath + "jindong.png";
                            File f = new File(path);
                            OutputStream out = new FileOutputStream(f);
                            adaptiveH.compress(Bitmap.CompressFormat.PNG, 100,
                                    out);
                            bis.close();
                            is.close();
                        } catch (Exception e) {
                            e.printStackTrace();
                        }

                    }

                    ;
                };
                thread.start();
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public void onFailure(RequestConstant.RequestIdentify identify, Throwable error) {
        closeMyDialog();
        CommonWebviewActivity.this.finish();
    }

    @Override
    public void onFinish(RequestConstant.RequestIdentify identify) {

    }



    private PopupWindow popupWindow;
    private RelativeLayout shadow;

    public void showSharePopup() {
        // TODO
        int displayWidth = this.getWindowManager().getDefaultDisplay()
                .getWidth();
        int displayHeight = this.getWindowManager().getDefaultDisplay()
                .getHeight();
        View view = LayoutInflater.from(this)
                .inflate(R.layout.share_menu, null);
        view.findViewById(R.id.share_cancle).setOnClickListener(this);
        view.findViewById(R.id.share_to_weixin).setOnClickListener(this);
        view.findViewById(R.id.share_to_moments).setOnClickListener(this);
        view.findViewById(R.id.share_to_qq).setOnClickListener(this);
        view.findViewById(R.id.share_to_weibo).setOnClickListener(this);
        popupWindow = new PopupWindow(this);
        popupWindow.setContentView(view);
        popupWindow.setWidth(displayWidth);
        popupWindow.setHeight(LayoutParams.WRAP_CONTENT);
        ColorDrawable dw = new ColorDrawable(0xb0000000);
        popupWindow.setBackgroundDrawable(dw);
        popupWindow.setOutsideTouchable(false);
        popupWindow.setAnimationStyle(R.style.PopupWindowAnimation2);
        popupWindow.setFocusable(true);
        popupWindow.setOnDismissListener(new OnDismissListener() {

            @Override
            public void onDismiss() {
                shadow.setVisibility(View.INVISIBLE);
            }
        });
        popupWindow.showAtLocation(findViewById(R.id.webview_rootlayout),
                Gravity.BOTTOM | Gravity.CENTER_HORIZONTAL, 0, 0); //
        // 设置layout在PopupWindow中显示的位置
        shadow.setVisibility(View.VISIBLE);

    }

    @Override
    protected void updateView() {

    }

}
