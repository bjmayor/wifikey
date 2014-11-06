/**
 * ReleaseConstant.java
 * com.hiwifi.constant
 * hiwifiKoala
 * shunping.liu create at 20142014��8��1������11:23:50
 */
package com.seo.constant;


import com.seo.wifikey.Gl;

import java.util.Random;

/**
 * @author shunping.liu@hiwifi.tw
 */
public class ReleaseConstant {

    public enum DebugLevel {
        DebugLevelConsole,
        DebugLevelSDCard,
        DebugLevelActivity,
        DebugLevelNone;
    }

    public static DebugLevel debugLevel = DebugLevel.DebugLevelSDCard;
    public static final boolean ISDEBUG = false;
    //如果关闭，则不取密码，也不倒计时
    public static boolean isCommerceOpened = false;

    public enum ADPLATFORM {
        ADPLATFORM_UNDEFINED,//未定义
        ADPLATFORM_WANPU,//万普
        ADPLATFORM_YOUMI,//有米
        ADPLATFORM_YJF;//意积分

        public static ADPLATFORM valueOf(int value) {
            switch (value) {
                case 0:
                    return ADPLATFORM_UNDEFINED;
                case 1:
                    return ADPLATFORM_WANPU;
                case 2:
                    return ADPLATFORM_YOUMI;
                case 3:
                    return ADPLATFORM_YJF;
                default:
                    return ADPLATFORM_YOUMI;
            }
        }
    }

    private static ADPLATFORM adplatform = ADPLATFORM.ADPLATFORM_UNDEFINED;

    /**
     * 产生随机的广告平台 28：70：2
     * 一个端固定使用一个广告平台
     *
     * @return
     */
    public static ADPLATFORM getAdPlatform() {
        if (adplatform != ADPLATFORM.ADPLATFORM_UNDEFINED) {
            return adplatform;
        } else {
            if (Gl.GlConf.isAdSetted()) {
                adplatform = Gl.GlConf.getAdPlatForm();
            } else {
                int rand = new Random().nextInt(100);
                if (rand > 72) {
                    adplatform = ADPLATFORM.ADPLATFORM_YJF;
                } else if (rand > 2) {
                    adplatform = ADPLATFORM.ADPLATFORM_YOUMI;
                } else {
                    adplatform = ADPLATFORM.ADPLATFORM_WANPU;
                }
            }
        }
        return adplatform;
    }
}
