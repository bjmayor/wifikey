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
        if (tag.getType().equals(TAG_TYPE_APP)) {
            return HOST_APP + tag.value();
        }
        return tag.value();

    }

    public static final String HOST_APP = "http://wifi.go2live.cn/";
    public static final String TAG_TYPE_WEB = "#web#";
    public static final String TAG_TYPE_APP = "#wifi_http#";


    // 请求地址都在这里定义，禁止出现在其它地方
    public enum RequestTag {
        /**
         * *********** 为hiwifi free定义 ***************
         */
        URL_APP_DOWNLOAD("http://app.mi.com/detail/74544", TAG_TYPE_WEB),//app在小米市场的下载地址
        HIWIFI_PWD_GET("Wifikey/getPwds", TAG_TYPE_APP, JSON), // 获取密码
        HIWIFI_OPONE_GET("/hiwififree/wifi/getOPOne", TAG_TYPE_APP), // 获取运营商密码
        HIWIFI_OPLOG_SEND("/hiwififree/wifi/saveOPLog", TAG_TYPE_APP), // 发送运营商日志
        HIWIFI_APLIST_SEND("Wifikey/s", TAG_TYPE_APP,
                JSON), // 发送扫描的wifi列表
        HIWIFI_CONFIG_GET("Conf", TAG_TYPE_APP), // 获取配置信息
        HIWIFI_MYAPLIST_SEND("/hiwififree/wifi/saveWifiBackup", TAG_TYPE_APP,
                JSON), // 备份wifi
        HIWIFI_BLOCKEDWIFI_GET("/hiwififree/wifi/noAutoLink", TAG_TYPE_APP), // 获取不自动连接wifi列表
        APP_PAGE_OFFICE_WEBSITE("http://wifi.go2live.cn/", TAG_TYPE_WEB), // 官网
        APP_PAGE_RECOMMEND_READ("http://go2live.cn/", TAG_TYPE_WEB), // 推荐阅读网站
        HIWIFI_RECENTAPP_SEND("App/saverecent",
                TAG_TYPE_APP, JSON), // 上报最近打开的app列表
        HIWIFI_ALLAPP_SEND("App/saveall", TAG_TYPE_APP, JSON), // 上报所有安装的app列表
        URL_NONE("", "", ""), ;// /
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
        private int httpCode = 200;

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

        public int getHttpCode() {
            return httpCode;
        }

        public void setHttpCode(int httpCode) {
            this.httpCode = httpCode;
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
