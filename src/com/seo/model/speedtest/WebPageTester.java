package com.seo.model.speedtest;

import org.apache.http.Header;

import android.annotation.SuppressLint;
import android.os.Handler;
import android.os.Message;

import com.seo.wifikey.Gl;
import com.seo.wifikey.Gl.GlConf;
import com.seo.model.log.HWFLog;
import com.seo.model.log.LogUtil;
import com.seo.support.http.AsyncHttpResponseHandler;
import com.seo.support.http.SyncHttpClient;
import com.seo.wifikey.R;
import com.umeng.analytics.MobclickAgent;

public class WebPageTester implements Runnable {
    private final String TAG = this.getClass().getSimpleName();
    private String webTestUrl = GlConf.getPingUrl();
    private String webTestExpectContent = GlConf.getPingExpectContent();
    private int timeout;// 毫秒单位
    private String message;
    private int errorCode;
    private long time;
    private WebpageTestAction listener = null;
    private boolean is_running = false;
    private boolean isSendMessage = true;
    private final int msg_ok = 0;
    private final int msg_error_download = 1;

    public static final int ErrorCodeCaptured = 1;
    public static final int ErrorCodeNetException = 2;
    public static final int ErrorCodeTimeout = 3;

    @SuppressLint("HandlerLeak")
    private Handler handler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            if (listener != null && isSendMessage) {
                switch (msg.what) {
                    case msg_error_download:
                        LogUtil.d("failure:", "msg_error_download");

                        listener.webpageErrorDownload(errorCode, message);
                        break;
                    case msg_ok:
                        errorCode = 0;
                        listener.webpageFinishDownload(time);
                        break;
                    default:
                        break;
                }
                is_running = false;
                isSendMessage = false;
                try {
                    MobclickAgent
                            .onEvent(
                                    Gl.Ct(),
                                    "stat_ping_result",
                                    Gl.Ct()
                                            .getResources()
                                            .getStringArray(
                                                    R.array.stat_ping_result)[errorCode]);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }
    };

    public WebPageTester() {
    }

    public WebPageTester(WebpageTestAction listener) {
        this.listener = listener;
        this.timeout = 5000;
    }

    SyncHttpClient client = new SyncHttpClient();

    private String loadPage(String urlStr) {
        long startTime = System.currentTimeMillis();
        client.setTimeout(this.timeout);
        client.setEnableRedirects(false);
        final StringBuffer buffer = new StringBuffer();
        Boolean isErrorBoolean = false;
        client.get(Gl.Ct(), urlStr, new AsyncHttpResponseHandler() {
            @Override
            public void onSuccess(int statusCode, Header[] headers,
                                  byte[] responseBody) {
                if (responseBody != null) {
                    buffer.append(new String(responseBody));
                    if (!buffer.toString().trim()
                            .contains(webTestExpectContent)) {
                        setError(ErrorCodeCaptured, "no matched content");
                        // handler.sendEmptyMessage(msg_error_download);
                    }
                }

            }

            @Override
            public void onFailure(int statusCode, Header[] headers,
                                  byte[] responseBody, Throwable error) {
                if (responseBody != null) {
                    buffer.append(new String(responseBody));
                }
                if (isHiwifi(getLocation(headers))) {
                    LogUtil.d("location:", "进入诊断页");
                    setError(ErrorCodeNetException, "net off");
                } else {
                    if (statusCode == 0) {
                        setError(ErrorCodeTimeout, "time out");
                        // handler.sendEmptyMessage(msg_error_download);
                    }
                    if (statusCode > 300 && statusCode < 400) {
                        setError(ErrorCodeCaptured, "3xx capture");
                        // handler.sendEmptyMessage(msg_error_download);
                    }
                }

            }

            private Boolean isHiwifi(String location) {
                return location != null && location.contains("net_detect");
            }

            private String getLocation(Header[] headers) {
                String locationString = "";
                if (headers == null || headers.length == 0) {
                    return locationString;
                }
                for (int i = 0; i < headers.length; i++) {
                    Header header = headers[i];
                    if (header.getName().equalsIgnoreCase("location")) {
                        return header.getValue();
                    }
                }
                return locationString;
            }
        });
        if (isErrorBoolean) {
            is_running = false;
        }
        time = System.currentTimeMillis() - startTime;
        return buffer.toString();
    }

    private void setError(int code, String msg) {
        if (isSendMessage) {
            message = msg;
            errorCode = code;
            HWFLog.e(TAG, errorCode + ":" + message);
            is_running = false;
            handler.sendEmptyMessage(msg_error_download);
        }
    }

    @Override
    public void run() {
        long total_time = 0;
        if (is_running && webTestUrl != null) {
            loadPage(webTestUrl);
            total_time += time;
        }
        if (is_running && isSendMessage) {
            time = total_time;
            handler.sendEmptyMessage(msg_ok);
        }
    }

    public void stopTest() {
        is_running = false;
        client.cancelRequests(Gl.Ct(), true);
        isSendMessage = false;
    }

    public boolean startTest(long time_overflow_in_million_second) {
        boolean ret = true;
        this.timeout = (int) time_overflow_in_million_second;
        if (ret) {
            is_running = true;
            new Thread(this).start();
        }
        return ret;
    }

    public interface WebpageTestAction {
        public void webpageErrorDownload(int errorCode, String message);

        public void webpageFinishDownload(long avgTime);
    }
}
