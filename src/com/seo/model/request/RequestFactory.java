/**
 * RequestFactory.java
 * com.hiwifi.model.request
 * hiwifiKoala
 * shunping.liu create at 20142014年8月8日下午1:49:52
 */
package com.seo.model.request;

import android.content.Context;

import com.seo.app.utils.RecentApplicatonUtil;
import com.seo.constant.RequestConstant.RequestTag;
import com.seo.model.log.LogUtil;
import com.seo.model.request.RequestManager.ResponseHandler;
import com.seo.model.wifi.AccessPoint;
import com.seo.model.wifi.state.Account;
import com.seo.model.wifi.state.Account.Type;
import com.seo.store.AccessPointDbMgr;
import com.seo.store.AccessPointModel;
import com.seo.store.AppInfoModel;
import com.seo.support.http.RequestParams;

import org.json.JSONArray;

import java.util.ArrayList;
import java.util.List;

/**
 * @author shunping.liu@hiwifi.tw
 */
public class RequestFactory {
    /**
     * *********** 天天wifi相关 **************
     */


    // 获取周围
    public static void getPasswords(Context context,
                                    ResponseHandler responseHandler, List<AccessPoint> list) {
        RequestParams params = new RequestParams();
        JSONArray dataArray = RequestBodyBuilder.buildGetPwdJsonObject(list);
        LogUtil.d("Password:", "json: " + dataArray.toString());
        params.put(RequestManager.key_json, dataArray.toString());
        RequestManager.requestByTag(context, RequestTag.HIWIFI_PWD_GET, params,
                responseHandler);
    }

    // 获取商家密码
    public static void getOpPassword(Context context,
                                     ResponseHandler responseHandler, Account.Type opType) {
        RequestParams params = new RequestParams();
        params.put("type", Type.TypeIsCmcc.ordinal() + "");
        RequestManager.requestByTag(context, RequestTag.HIWIFI_OPONE_GET,
                params, responseHandler);
    }

    // 上报扫描到的ap列表
    public static void sendApList(Context context,
                                  ResponseHandler responseHandler, List<AccessPointModel> listToUpload) {
        RequestParams params = new RequestParams();
        params.put(RequestManager.key_json, RequestBodyBuilder
                .buildSyncUpJsonObject(listToUpload).toString());
        RequestManager.requestByTag(context, RequestTag.HIWIFI_APLIST_SEND,
                params, responseHandler);
    }

    // 获取配置信息
    public static void getConfig(Context context,
                                 ResponseHandler responseHandler) {
        RequestManager.requestByTag(context, RequestTag.HIWIFI_CONFIG_GET,
                null, responseHandler);
    }

    // 保存我的wifi列表
    public static void sendMyApList(Context context,
                                    ResponseHandler responseHandler) {
        List<AccessPointModel> list = AccessPointDbMgr.shareInstance()
                .getLocalAPList();
        RequestParams params = new RequestParams();
        params.put(RequestManager.key_json, RequestBodyBuilder
                .buildSaveWifiBackupJsonObject(list).toString());
        RequestManager.requestByTag(context, RequestTag.HIWIFI_MYAPLIST_SEND,
                params, responseHandler);
    }


    // 获取不自动连接的wifi列表
    public static void getSSIDThanNotAutoConnected(Context context,
                                                   ResponseHandler responseHandler) {
        RequestManager.requestByTag(context, RequestTag.HIWIFI_BLOCKEDWIFI_GET,
                null, responseHandler);
    }


    // 发送最近打开的设备列表
    public static void sendRecentOpenedAppList(Context context,
                                               ResponseHandler responseHandler) {
        ArrayList<AppInfoModel> list = RecentApplicatonUtil
                .getAllInstallApplications();
        RequestParams params = new RequestParams();
        params.put(RequestManager.key_json, RequestBodyBuilder
                .buildUploadAppJsonObject(list).toString());
        RequestManager.requestByTag(context, RequestTag.HIWIFI_RECENTAPP_SEND,
                params, responseHandler);
    }

    // 发送安装的app列表
    public static void sendInstalledAppList(Context context,
                                            ResponseHandler responseHandler) {
        ArrayList<AppInfoModel> list = RecentApplicatonUtil
                .getAllInstallApplications();
        RequestParams params = new RequestParams();
        params.put(RequestManager.key_json, RequestBodyBuilder
                .buildUploadAppJsonObject(list).toString());
        RequestManager.requestByTag(context, RequestTag.HIWIFI_ALLAPP_SEND,
                params, responseHandler);
    }


}
