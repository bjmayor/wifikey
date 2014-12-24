/**
 * RequestManager.java
 * com.hiwifi.model
 * hiwifiKoala
 * shunping.liu create at 20142014年8月5日上午10:42:20
 */
package com.seo.model.request;

import java.io.UnsupportedEncodingException;
import java.util.HashMap;

import org.apache.http.Header;
import org.apache.http.HttpEntity;
import org.apache.http.HttpStatus;
import org.apache.http.entity.StringEntity;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.content.Context;
import android.os.Looper;

import com.seo.constant.ReleaseConstant;
import com.seo.constant.RequestConstant;
import com.seo.wifikey.Gl;
import com.seo.model.log.HWFLog;
import com.seo.model.log.LogUtil;
import com.seo.model.request.RequestManager.ResponseHandler.Code;
import com.seo.support.http.AsyncHttpClient;
import com.seo.support.http.JsonHttpResponseHandler;
import com.seo.support.http.PersistentCookieStore;
import com.seo.support.http.RequestParams;
import com.seo.support.http.SyncHttpClient;
import com.seo.utils.NetWorkConnectivity;

/**
 * @author shunping.liu@hiwifi.tw
 */
public class RequestManager {
    public interface ResponseHandler {
        public enum Code {
            ok(0, "OK"), errorNoNetwork(1, "网络异常"), errorNotLoginOrExpired(2,
                    "未登录，请先登录"), errorNoRouter(3, "没有绑定路由器，请先绑定"), errorFileNotExists(
                    4, "文件不存在"), errorUnkown(999, "未知错误");
            private int code;
            private String msg;

            private Code(int code, String msg) {
                this.code = code;
                this.msg = msg;
            }

            public String getMsg() {
                return this.msg;
            }

            public int getCode() {
                return this.code;
            }
        }

        public void onStart(RequestConstant.RequestIdentify identify, Code code);

        public void onSuccess(RequestConstant.RequestIdentify identify,
                              ServerResponseParser responseParser);

        public void onFailure(RequestConstant.RequestIdentify identify, Throwable error);

        public void onFinish(RequestConstant.RequestIdentify identify);
    }

    private static final String TAG = "RequestManager";
    public static final String key_wrap = "clientwrap";
    public static final String key_code = "code";
    public static final String key_msg = "msg";
    public static final String key_json = "data";

    private static AsyncHttpClient asyncHttpClient = null;
    private static SyncHttpClient synchttpClient = null;

    private static AsyncHttpClient getHttpClient() {
        ClientModel model = clientPool.get(Thread.currentThread().getId() + "");
        if (model != null) {
            return model.getHttpClient();
        } else {
            if (asyncHttpClient == null) {
                asyncHttpClient = new AsyncHttpClient();
                asyncHttpClient.setCookieStore(new PersistentCookieStore(Gl
                        .Ct()));
            }
            return asyncHttpClient;
        }
    }

    private static boolean isMainThread() {
        return Thread.currentThread().getId() == Looper.getMainLooper()
                .getThread().getId();
    }

    private static class ClientModel {
        private boolean isAsync = true;

        public void setSyncModel(boolean isSync) {
            isAsync = !isSync;
        }

        private AsyncHttpClient getHttpClient() {
            if (isAsync || isMainThread()) {
                if (asyncHttpClient == null) {
                    asyncHttpClient = new AsyncHttpClient();
                    asyncHttpClient.setCookieStore(new PersistentCookieStore(Gl
                            .Ct()));
                }
                return asyncHttpClient;
            } else {
                if (synchttpClient == null) {
                    synchttpClient = new SyncHttpClient();
                    synchttpClient.setCookieStore(new PersistentCookieStore(Gl
                            .Ct()));
                }
                isAsync = true;
                return synchttpClient;
            }

        }
    }

    public static HashMap<String, ClientModel> clientPool = new HashMap<String, RequestManager.ClientModel>();

    static {
        clientPool.put(Looper.getMainLooper().getThread().getId() + "",
                new ClientModel());
    }

    // 设置为同步模式
    public static void setSyncModel(boolean isSync) {
        ClientModel model = clientPool.get(Thread.currentThread().getId() + "");
        if (model == null) {
            model = new ClientModel();
            clientPool.put(Thread.currentThread().getId() + "", model);
        }
        model.setSyncModel(isSync);
    }

    /**
     * @param context
     * @param tag
     * @param params
     * @return
     * @description 请求的唯一入口，不可在其它地方使用async等框架发请求
     */
    public static boolean requestByTag(Context context, RequestConstant.RequestTag tag,
                                       RequestParams params, final ResponseHandler responseHandler) {
        if (params == null) {
            params = new RequestParams();
        }
        if (tag.getType() == RequestConstant.TAG_TYPE_TWX
                || tag.getType() == RequestConstant.TAG_TYPE_M) {
        }
        RequestConstant.RequestIdentify identify = new RequestConstant.RequestIdentify(tag);
        identify.setParams(params);

        if (!canRequest(identify, params, responseHandler)) {
            return false;
        } else {

            if (tag.getMethod().equals(RequestConstant.GET)) {
                doGet(identify, params, responseHandler);
            } else if (tag.getMethod().equals(RequestConstant.POST)) {

                doPost(identify, params, responseHandler);
            } else if (tag.getMethod().equals(RequestConstant.JSON)) {
                try {
                    doPost(context, identify,
                            new StringEntity(params.get(key_json)),
                            RequestConstant.ContentType.JSON.toString(),
                            responseHandler);
                } catch (UnsupportedEncodingException e) {
                    responseHandler.onStart(identify, Code.errorUnkown);
                    return false;
                }
            } else if (tag.getMethod().equals(RequestConstant.BINARY)) {
                doPost(identify, params, responseHandler);
            }
            return true;
        }

    }


    private static void doGet(final RequestConstant.RequestIdentify identify, final RequestParams params,
                              final ResponseHandler responseHandler) {
        getHttpClient().get(RequestConstant.getUrl(identify.getTag()), params,
                new JsonHttpResponseHandler() {
                    private long startTime;

                    @Override
                    public void onStart() {
                        super.onStart();
                        startTime = System.currentTimeMillis();
                        identify.setURI(getRequestURI());
                        responseHandler.onStart(identify, ResponseHandler.Code.ok);
                    }

                    @Override
                    public void onSuccess(int statusCode, Header[] headers,
                                          JSONObject response) {
                        super.onSuccess(statusCode, headers, response);
                        identify.setHttpCode(statusCode);
                        showRequestDebugInfo(identify, params, response, startTime, null);
                        if (statusCode == HttpStatus.SC_OK) {
                            responseHandler.onSuccess(identify,
                                    new ServerResponseParser(response));
                        }
                    }

                    public void onSuccess(int statusCode, Header[] headers,
                                          JSONArray response) {
                        super.onSuccess(statusCode, headers, response);
                        identify.setHttpCode(statusCode);
                        showRequestDebugInfo(identify, params, response, startTime, null);
                        if (statusCode == HttpStatus.SC_OK) {
                            JSONObject object = new JSONObject();
                            try {
                                object.put(key_wrap, response);
                                object.put(key_code, 0);
                                object.put(key_msg, "OK");
                            } catch (JSONException e) {
                                e.printStackTrace();
                            }
                            responseHandler.onSuccess(identify,
                                    new ServerResponseParser(object));
                        }
                    }

                    @Override
                    public void onFailure(int statusCode, Header[] headers,
                                          String responseString, Throwable throwable) {
                        super.onFailure(statusCode, headers, responseString,
                                throwable);
                        identify.setHttpCode(statusCode);
                        showRequestDebugInfo(identify, params, responseString, startTime, throwable);
                        responseHandler.onFailure(identify, throwable);
                    }

                    @Override
                    public void onFailure(int statusCode, Header[] headers,
                                          Throwable throwable, JSONArray errorResponse) {
                        super.onFailure(statusCode, headers, throwable,
                                errorResponse);
                        identify.setHttpCode(statusCode);
                        showRequestDebugInfo(identify, params, errorResponse, startTime, throwable);
                        responseHandler.onFailure(identify, throwable);
                    }

                    @Override
                    public void onFailure(int statusCode, Header[] headers,
                                          Throwable throwable, JSONObject errorResponse) {
                        super.onFailure(statusCode, headers, throwable,
                                errorResponse);
                        identify.setHttpCode(statusCode);
                        showRequestDebugInfo(identify, params, errorResponse, startTime, throwable);
                        responseHandler.onFailure(identify, throwable);
                    }

                    @Override
                    public void onFinish() {
                        responseHandler.onFinish(identify);
                        super.onFinish();
                    }
                });
    }

    private static void doPost(final RequestConstant.RequestIdentify identify, final RequestParams params,
                               final ResponseHandler responseHandler) {
//		System.out.println("url:"+RequestConstant.getUrl(tag));
        getHttpClient().post(RequestConstant.getUrl(identify.getTag()), params,
                new JsonHttpResponseHandler() {
                    long startTime = 0;

                    @Override
                    public void onStart() {
                        super.onStart();
                        if (ReleaseConstant.ISDEBUG) {
                            startTime = System.currentTimeMillis();
                        }
                        identify.setURI(getRequestURI());
                        responseHandler.onStart(identify, Code.ok);
                    }

                    @Override
                    public void onSuccess(int statusCode, Header[] headers,
                                          JSONObject response) {
                        super.onSuccess(statusCode, headers, response);
                        identify.setHttpCode(statusCode);
                        showRequestDebugInfo(identify, params, response, startTime, null);
                        if (statusCode == HttpStatus.SC_OK) {
                            responseHandler.onSuccess(identify,
                                    new ServerResponseParser(response));
                        }
                    }

                    public void onSuccess(int statusCode, Header[] headers,
                                          JSONArray response) {
                        super.onSuccess(statusCode, headers, response);
                        identify.setHttpCode(statusCode);
                        showRequestDebugInfo(identify, params, response, startTime, null);
                        if (statusCode == HttpStatus.SC_OK) {
                            JSONObject object = new JSONObject();
                            try {
                                object.put(key_wrap, response);
                                object.put(key_code, 0);
                                object.put(key_msg, "OK");
                            } catch (JSONException e) {
                                e.printStackTrace();
                            }
                            responseHandler.onSuccess(identify,
                                    new ServerResponseParser(object));
                        }
                    }

                    @Override
                    public void onFailure(int statusCode, Header[] headers,
                                          String responseString, Throwable throwable) {
                        super.onFailure(statusCode, headers, responseString,
                                throwable);
                        identify.setHttpCode(statusCode);
                        showRequestDebugInfo(identify, params, responseString, startTime, throwable);
                        responseHandler.onFailure(identify, throwable);
                    }

                    @Override
                    public void onFailure(int statusCode, Header[] headers,
                                          Throwable throwable, JSONArray errorResponse) {
                        super.onFailure(statusCode, headers, throwable,
                                errorResponse);
                        identify.setHttpCode(statusCode);
                        showRequestDebugInfo(identify, params, errorResponse, startTime, throwable);
                        responseHandler.onFailure(identify, throwable);
                    }

                    @Override
                    public void onFailure(int statusCode, Header[] headers,
                                          Throwable throwable, JSONObject errorResponse) {
                        super.onFailure(statusCode, headers, throwable,
                                errorResponse);
                        identify.setHttpCode(statusCode);
                        showRequestDebugInfo(identify, params, errorResponse, startTime, throwable);
                        responseHandler.onFailure(identify, throwable);
                    }

                    @Override
                    public void onProgress(int bytesWritten, int totalSize) {
                        super.onProgress(bytesWritten, totalSize);
                        HWFLog.e("debug",
                                String.format("%d/%d", bytesWritten, totalSize));
                    }

                    @Override
                    public void onFinish() {
                        responseHandler.onFinish(identify);
                        super.onFinish();
                    }
                });
    }


    private static void doPost(Context context, final RequestConstant.RequestIdentify identify,
                               final HttpEntity entity, String contentType,
                               final ResponseHandler responseHandler) {

        getHttpClient().post(context, RequestConstant.getUrl(identify.getTag()), entity,
                contentType, new JsonHttpResponseHandler() {
                    long startTime = 0;

                    @Override
                    public void onStart() {
                        super.onStart();
                        if (ReleaseConstant.ISDEBUG) {
                            startTime = System.currentTimeMillis();
                        }
                        identify.setURI(getRequestURI());
                        responseHandler.onStart(identify, Code.ok);
                    }

                    @Override
                    public void onSuccess(int statusCode, Header[] headers,
                                          JSONObject response) {
                        super.onSuccess(statusCode, headers, response);
                        identify.setHttpCode(statusCode);
                        showRequestDebugInfo(identify, entity, response, startTime, null);
                        if (statusCode == HttpStatus.SC_OK) {
                            responseHandler.onSuccess(identify,
                                    new ServerResponseParser(response));
                        }
                    }

                    public void onSuccess(int statusCode, Header[] headers,
                                          JSONArray response) {
                        super.onSuccess(statusCode, headers, response);
                        identify.setHttpCode(statusCode);
                        showRequestDebugInfo(identify, entity, response, startTime, null);
                        if (statusCode == HttpStatus.SC_OK) {
                            JSONObject object = new JSONObject();
                            try {
                                object.put(key_wrap, response);
                                object.put(key_code, 0);
                                object.put(key_msg, "OK");
                            } catch (JSONException e) {
                                e.printStackTrace();
                            }
                            responseHandler.onSuccess(identify,
                                    new ServerResponseParser(object));
                        }
                    }

                    @Override
                    public void onFailure(int statusCode, Header[] headers,
                                          String responseString, Throwable throwable) {
                        super.onFailure(statusCode, headers, responseString,
                                throwable);
                        identify.setHttpCode(statusCode);
                        showRequestDebugInfo(identify, entity, responseString, startTime, throwable);
                        responseHandler.onFailure(identify, throwable);
                    }

                    @Override
                    public void onFailure(int statusCode, Header[] headers,
                                          Throwable throwable, JSONArray errorResponse) {
                        super.onFailure(statusCode, headers, throwable,
                                errorResponse);
                        identify.setHttpCode(statusCode);
                        showRequestDebugInfo(identify, entity, errorResponse, startTime, throwable);
                        responseHandler.onFailure(identify, throwable);
                    }

                    @Override
                    public void onFailure(int statusCode, Header[] headers,
                                          Throwable throwable, JSONObject errorResponse) {
                        super.onFailure(statusCode, headers, throwable,
                                errorResponse);
                        identify.setHttpCode(statusCode);
                        showRequestDebugInfo(identify, entity, errorResponse, startTime, throwable);
                        responseHandler.onFailure(identify, throwable);
                    }

                    @Override
                    public void onFinish() {
                        // TODO Auto-generated method stub
                        responseHandler.onFinish(identify);
                        super.onFinish();
                    }
                });
    }

    public static void showRequestDebugInfo(RequestConstant.RequestIdentify identify, Object requestBody, Object response, long startTime, Throwable throwable) {
        if (ReleaseConstant.ISDEBUG) {
            StringBuffer buffer = new StringBuffer();
            buffer.append((identify != null ? identify.toString() : ""));
            buffer.append("\r\n" + "---------requestBody---------\r\n");
            buffer.append(requestBody != null ? requestBody.toString() : "no body");
            buffer.append("\r\n---------response http code--------\r\n");
            buffer.append(identify.getHttpCode());
            buffer.append("\r\n---------response--------\r\n");
            buffer.append(response != null ? response.toString() : "no response" + "\r\n");
            if (throwable != null) {
                buffer.append("throwabel:\r\n" + throwable.toString() + "\r\n");
            }
            buffer.append("usetime:" + (System.currentTimeMillis() - startTime));
            LogUtil.w(TAG, buffer.toString());
        }
    }

    public static void cancelRequest(Context context) {
        getHttpClient().cancelRequests(context, true);
    }

    public static boolean canRequest(RequestConstant.RequestIdentify identify, RequestParams params,
                                     ResponseHandler responseHandler) {
        if (!NetWorkConnectivity.hasNetwork(Gl.Ct())) {
            responseHandler.onStart(identify, Code.errorNoNetwork);
            return false;
        }
        if (identify.getTag().getType() != RequestConstant.TAG_TYPE_USER
                && identify.getTag().getType() != RequestConstant.TAG_TYPE_WEB
                && identify.getTag().getType() != RequestConstant.TAG_TYPE_HWF_S
                && identify.getTag().getType() != RequestConstant.TAG_TYPE_HWF
                ) {
        }

        return true;
    }
}
