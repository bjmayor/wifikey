/**
 * RequestConstant.java
 * com.hiwifi.constant
 * hiwifiKoala
 * shunping.liu create at 20142014��8��1������11:24:03
 */
package com.seo.constant;

import java.io.Serializable;
import java.net.URI;

import com.seo.support.http.RequestParams;
import com.seo.wifikey.Gl;

/**
 * http 请求管理类
 *
 * @author shunping.liu@hiwifi.tw
 */
public class RequestConstant implements Serializable {
    private static final long serialVersionUID = 1L;

    public static enum ContentType {
        JSON("application/json"), URLENCODE("application/x-www-form-urlencoded"), HTML(
                "text/html"), XML("text/xml"), BINARY(
                "application/octet-stream");
        String type;

        public String getType() {
            return type;
        }

        private ContentType(String type) {
            this.type = type;
        }
    }

    public static final String GET = "get";
    public static final String POST = "post";
    public static final String JSON = "json";
    public static final String BINARY = "binary";

    public static String getUrl(RequestTag tag) {
        if (tag.getType().equals(TAG_TYPE_TWX)) {
            return URL_BASE_TWX + tag.value();
        } else if (tag.getType().equals(TAG_TYPE_TWX_PATH)) {
            return URL_BASE_TWX3;
        } else if (tag.getType().equals(TAG_TYPE_OPEN)) {
            return URL_BASE_TWX + tag.value();
        } else if (tag.getType().equals(TAG_TYPE_M_S)) {
            return HOST_M_S + tag.value();
        } else if (tag.getType().equals(TAG_TYPE_M)) {
            return HOST_M + tag.value();
        } else if (tag.getType().equals(TAG_TYPE_APP)) {
            return HOST_APP_S + tag.value();
        } else if (tag.getType().equals(TAG_TYPE_USER)) {
            // if (ReleaseConstant.ISDEBUG) {TAG_TYPE_HWF
            // return "http://hongbin.liu.user.hiwifi-dev.com/" + tag.value();
            // }
            return HOST_USER_S + tag.value();
        } else if (tag.getType().equals(TAG_TYPE_WEB)) {
            return tag.value();
        } else if (tag.getType().equals(TAG_TYPE_HWF)) {
            return HOST_HIWIFI + tag.value();
        } else if (tag.getType().equals(TAG_TYPE_HWF_S)) {
            return HOST_HIWIFI_HTTPS + tag.value();
        }
        return tag.value();

    }

    public static final String HOST_M = "http://m.hiwifi.com/";
    public static final String HOST_M_S = "https://m.hiwifi.com/";
    public static final String HOST_APP = "http://app.hiwifi.com/";
    public static final String HOST_APP_S = "https://app.hiwifi.com/";
    public static final String HOST_USER_S = "https://user.hiwifi.com/";
    public static final String HOST_OPEN = "http://openapi.hiwifi.com/";
    public static final String HOST_OPEN_CLIENT = "http://client.openapi.hiwifi.com/";
    public static final String URL_BASE_TWX = HOST_APP_S
            + "/router.php?m=json&android_client_ver=" + Gl.getAppVersionCode()
            + "&a=";
    public static final String URL_BASE_TWX3 = HOST_APP_S
            + "/router.php?m=json&android_client_ver=" + Gl.getAppVersionCode()
            + "&a=twx_api";
    public static final String HOST_HIWIFI = "http://hf.hiwifi.com";
    public static final String HOST_HIWIFI_HTTPS = "https://hf.hiwifi.com";

    public static final String TAG_TYPE_TWX = "#twx#";
    public static final String TAG_TYPE_TWX_PATH = "#twx_path#";
    public static final String TAG_TYPE_OPEN = "#open#";
    public static final String TAG_TYPE_OPEN_CLIENT = "#open_client#";
    public static final String TAG_TYPE_M_S = "#ms#";
    public static final String TAG_TYPE_M = "#m#";
    public static final String TAG_TYPE_APP = "#app#";
    public static final String TAG_TYPE_APP_S = "#app_#";
    public static final String TAG_TYPE_USER = "#user#";
    public static final String TAG_TYPE_WEB = "#web#";
    public static final String TAG_TYPE_HWF = "#hiwifi#";
    public static final String TAG_TYPE_HWF_S = "#hiwifi_https#";

    /**
     * localkey
     */
    public static final String LOCALKEY = "oLKmbg1g";
    public static final String APP_ID = "13";
    public static final String APP_SECRET = "srt653fa58ac5c30ccee40b66553a429";

    // 请求地址都在这里定义，禁止出现在其它地方
    public enum RequestTag {
        /**
         * *********** 为hiwifi free定义 ***************
         */
        URL_APP_DOWNLOAD("http://app.mi.com/detail/74544", TAG_TYPE_WEB),
        HIWIFI_APP_RECOMMEND_GET("/hiwififree/box/boxlist", TAG_TYPE_HWF), // 推荐app
        HIWIFI_PWD_GET("/hiwififree/wifi/getPw", TAG_TYPE_HWF_S, JSON), // 获取密码
        HIWIFI_OPONE_GET("/hiwififree/wifi/getOPOne", TAG_TYPE_HWF_S), // 获取运营商密码
        HIWIFI_OPLOG_SEND("/hiwififree/wifi/saveOPLog", TAG_TYPE_HWF_S), // 发送运营商日志
        HIWIFI_APLIST_SEND("/hiwififree/util/saveApScanList", TAG_TYPE_HWF_S,
                JSON), // 发送扫描的wifi列表
        HIWIFI_CONFIG_GET("/hiwififree/wifi/getConfig", TAG_TYPE_HWF), // 获取配置信息
        HIWIFI_MYAPLIST_SEND("/hiwififree/wifi/saveWifiBackup", TAG_TYPE_HWF_S,
                JSON), // 备份wifi
        HIWIFI_TIME_GET("/hiwififree/rank/getRemainTime", TAG_TYPE_HWF_S), // 获取剩余时间
        HIWIFI_BLOCKEDWIFI_GET("/hiwififree/wifi/noAutoLink", TAG_TYPE_HWF), // 获取不自动连接wifi列表
        HIWIFI_STATUS_SHAREDAPP_GET("/hiwififree/rank/getShared",
                TAG_TYPE_HWF_S), // 获取分享状态，指分享微博
        HIWIFI_SHAREDAPP_REPORT("/hiwififree/rank/shareCallback",
                TAG_TYPE_HWF_S), // 分享微博回调
        HIWIFI_PAGE_BUYROUTER(HOST_M
                + "/api/Page/redirect?act=BuyRouterFromHiwifi", TAG_TYPE_WEB), // 购买极路由地址
        HIWIFI_PAGE_SHAREROUTER(HOST_APP
                + "mobile.php?m=service&a=install&rid=", TAG_TYPE_WEB), // 共享wifi页面
        HIWIFI_PAGE_DOWNLOADAPP(HOST_M + "/api/Page/redirect?act=downtiantian",
                TAG_TYPE_WEB), // 下载app
        HIWIFI_PAGE_OFFICE_WEBSITE("http://wifikey.sinaapp.com/", TAG_TYPE_WEB), // 官网
        HIWIFI_ROUTER_SHARE_SET("/hiwififree/rank/setWifiShare", TAG_TYPE_HWF_S), // 共享wifi
        HIWIFI_CONFIG_BUYROUTER_GET("/hiwififree/page/buyRouterConf",
                TAG_TYPE_HWF), // 购买路由器的配置信息
        HIWIFI_RECENTAPP_SEND("/hiwififree/util/saveRecentApps",
                TAG_TYPE_HWF_S, JSON), // 上报最近打开的app列表
        HIWIFI_ALLAPP_SEND("/hiwififree/util/saveAllApps", TAG_TYPE_HWF_S, JSON), // 上报所有安装的app列表
        HIWIFI_CRASH_SEND("/hiwififree/util/saveCrashLog", TAG_TYPE_HWF_S, JSON), // 上报错误日志
        HIWIFI_PORTAL_SEND("/hiwififree/util/saveCrashLog", TAG_TYPE_HWF_S), // 上报有portal的wifi
        HIWIFI_PWD_VIEWTIMES_GET("/hiwififree/pwd/getLeftTimes", TAG_TYPE_HWF), // 获取密码查看次数
        HIWIFI_PWD_VIEWD_SET("/hiwififree/pwd/getOnePwd", TAG_TYPE_HWF), // 上报用户查看了一次密码
        HIWIFI_DISCOVER_LIST_GET("/hiwififree/find/pagelist", TAG_TYPE_HWF), // 获取发现页列表
        URL_NONE("", "", ""), HIWIFI_CHECK_APP_UPGRADE(
                "/router.php?m=json&a=check_android_upgrade", TAG_TYPE_APP),;// /
        private String url;
        private String tagType;
        private String method;
        private boolean needLogin;


        private RequestTag(String url, String tagType) {
            this.url = url;
            this.tagType = tagType;
            this.method = POST;
            this.needLogin = true;
        }

        private RequestTag(String url, String tagType, String method) {
            this.url = url;
            this.tagType = tagType;
            this.method = method;
            this.needLogin = true;
        }

        public String getType() {
            return tagType;
        }

        public String getMethod() {
            return method;
        }


        public String value() {
            return url;
        }

    }

    public static  class RequestIdentify {
        private RequestTag tag;
        private URI requestURI;
        private RequestParams params;

        public RequestIdentify(RequestTag tag) {
            this.tag = tag;
        }

        public RequestTag getTag() {
            return tag;
        }

        public void setTag(RequestTag tag) {
            this.tag = tag;
        }

        public void setURI(URI uri) {
            this.requestURI = uri;
        }

        public URI getUri() {
            return this.requestURI;
        }

        public void setParams(RequestParams params) {
            this.params = params;
        }

        public RequestParams getParams() {
            return this.params;
        }

        @Override
        public String toString() {
            return String.format(
                    "tag:{url:%s,type:%s,method:%s,URI:%s, params:%s}",
                    this.tag.url, this.tag.tagType, this.tag.method,
                    this.requestURI != null ? this.requestURI.toString()
                            : "not set", this.params != null ? this.params.toString() : "no params");
        }
    }

}
