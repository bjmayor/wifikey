package com.seo.activity.wifi;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.Timer;
import java.util.TimerTask;

import org.adver.score.scorewall.ScoreWallSDK;
import org.adver.score.sdk.widget.UpdateScordNotifier;

import android.content.Intent;
import android.net.NetworkInfo;
import android.net.TrafficStats;
import android.net.wifi.SupplicantState;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.os.Handler;
import android.text.SpannableString;
import android.text.Spanned;
import android.text.TextUtils;
import android.text.style.ForegroundColorSpan;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup.LayoutParams;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.PopupWindow;
import android.widget.TextView;
import android.widget.Toast;


import com.seo.activity.base.BaseActivity;
import com.seo.app.receiver.HiwifiBroadcastReceiver;
import com.seo.app.receiver.HiwifiBroadcastReceiver.WifiEventHandler;
import com.seo.constant.ReleaseConstant;
import com.seo.constant.RequestConstant;
import com.seo.model.log.LogUtil;
import com.seo.model.wifi.AccessPoint;
import com.seo.model.wifi.WifiAdmin;
import com.seo.utils.ViewUtil;
import com.seo.wifikey.R;
import com.umeng.analytics.MobclickAgent;

import net.youmi.android.offers.OffersManager;
import net.youmi.android.offers.PointsManager;

import cn.sharesdk.framework.Platform;
import cn.sharesdk.framework.PlatformActionListener;
import cn.sharesdk.framework.ShareSDK;
import cn.sharesdk.onekeyshare.OnekeyShare;
import cn.sharesdk.onekeyshare.ShareContentCustomizeCallback;
import cn.sharesdk.sina.weibo.SinaWeibo;
import cn.sharesdk.wechat.moments.WechatMoments;
import cn.waps.AppConnect;
import cn.waps.AppListener;
import cn.waps.UpdatePointsNotifier;


public class CheckPasswordActivity extends BaseActivity implements
        OnClickListener, UpdateScordNotifier, UpdatePointsNotifier, PlatformActionListener {

    private LinearLayout unloginView, loginedView, showPwdView, shareContainer;
    private ImageView backWifilist, stateImage;
    private TextView currentSignal, currentTraffic, joinedBssid, joinedState,
            password, remain, achieve, title_save, shareTitleView, sharePrompt,
            unloginSharePrompt;
    private Button checkPassword, login, breakConnect;

    private final int SET_TRAFFIC = 1;
    private final int REQUEST_LOGIN = 1001;
    private final int SCORE_PER_VIEW = 10;
    private String wifiPwd = "";

    public final String APP_URL = "http://wifikey.sinaapp.com";

    private Handler handler = new Handler() {
        public void handleMessage(android.os.Message msg) {
            if (msg.what == SET_TRAFFIC) {
                setCurrentTracffic();
            }
        }

        ;
    };

    private void initView() {

        getActionBar().setDisplayHomeAsUpEnabled(true);
        getActionBar().show();
        if (mAttempAccessPoint != null) {
            this.joinedBssid.setText(mAttempAccessPoint.getScanResult().SSID);
            this.joinedState
                    .setText(mAttempAccessPoint.getConnectStateString());
            setSignal(mAttempAccessPoint.getSignalPersent());
            this.stateImage.setImageDrawable(mAttempAccessPoint
                    .getChangeStateDrawable());
        }

    }


    private void setRemainChance() {
        int color = getResources().getColor(R.color.text_blue);
        String shit = "今日还可以查看";
        String times = String.valueOf(leftTimes / SCORE_PER_VIEW);// "12";
        SpannableString shitshit = new SpannableString(shit + times + "次");
        shitshit.setSpan(new ForegroundColorSpan(color), shit.length(),
                shit.length() + times.length(),
                Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
        remain.setText(shitshit);
    }

    private void setAchieveTextColor() {
        int color = getResources().getColor(R.color.text_blue);
        SpannableString prompt = new SpannableString("（分享给好友，可获得更多查看次数）");
        achieve.setText(prompt);
    }

    private void showShareByWp() {
        AppConnect.getInstance(this).showShareOffers(this);
    }

    private void showShare() {
        ShareSDK.initSDK(this);
        OnekeyShare oks = new OnekeyShare();
        //关闭sso授权
        oks.disableSSOWhenAuthorize();

        // 分享时Notification的图标和文字
        oks.setNotification(R.drawable.hiwifi_launcher, getString(R.string.app_name));
        // title标题，印象笔记、邮箱、信息、微信、人人网和QQ空间使用
        oks.setTitle(getString(R.string.share));
        // text是分享文本，所有平台都需要这个字段
        oks.setText("在外面也能连上免费wifi了，这软件还行！我真是一会儿都离不开网啊，推荐你也试试：" + RequestConstant.getUrl(RequestConstant.RequestTag.URL_APP_DOWNLOAD));
        // imagePath是图片的本地路径，Linked-In以外的平台都支持此参数
//        oks.setImagePath("/sdcard/test.jpg");
        oks.setShareContentCustomizeCallback(new ShareContentCustomizeCallback() {
            @Override
            public void onShare(Platform platform, Platform.ShareParams paramsToShare) {
                if (SinaWeibo.NAME.equals(platform.getName())
                        || WechatMoments.NAME.equals(platform.getName())) {
                    paramsToShare.setText("在外面也能连上免费wifi了，这软件还行！我真是一会儿都离不开网啊，推荐大家也试试：" + RequestConstant.getUrl(RequestConstant.RequestTag.URL_APP_DOWNLOAD));
                }
            }
        });
        // url仅在微信（包括好友和朋友圈）中使用
        oks.setUrl(RequestConstant.getUrl(RequestConstant.RequestTag.URL_APP_DOWNLOAD));
        // comment是我对这条分享的评论，仅在人人网和QQ空间使用
        oks.setComment("真的很赞!");
        // site是分享此内容的网站名称，仅在QQ空间使用
        oks.setSite(getString(R.string.app_name));
        // siteUrl是分享此内容的网站地址，仅在QQ空间使用
        oks.setSiteUrl(RequestConstant.getUrl(RequestConstant.RequestTag.URL_APP_DOWNLOAD));
        oks.setCallback(this);
        // 启动分享GUI
        oks.show(this);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        // TODO 登录回来显示 如果剩余查看次数 如何处理
        LogUtil.d("hehe", "onActivityResult");
        requestServerRemainChance();
    }

    // TODO 如果退到后台 网络状况发生了变化 怎么显示？？
    // 流量 信号

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_BACK) {
            closePage();
        }
        return super.onKeyDown(keyCode, event);
    }

    @Override
    protected void onDestroy() {
        if (handler != null) {
            handler = null;
        }
        HiwifiBroadcastReceiver.removeListener(wifiEventHandler);
        stopCheckFlow();
        super.onDestroy();
    }


    public void onResume() {
        super.onResume();
        startCheckCurrentFlow();
        setStatus();
//        ScoreWallSDK.getInstance(CheckPasswordActivity.this).getScore(
//                CheckPasswordActivity.this, CheckPasswordActivity.this);
        HiwifiBroadcastReceiver.addListener(wifiEventHandler);
//        AppConnect.getInstance(this).getConfig("showad", "no", new AppListener() {
//            @Override
//            public void onGetConfig(String value) {
//                if (value.equals("no")) {
//                    showAd = false;
//                } else {
//                    showAd = true;
//                }
//            }
//        });
    }

    @Override
    protected void onRestart() {
        System.out.println("onRestart");
        super.onRestart();
    }

    public void setSignal(int signal) {
        currentSignal.setText("信号 " + signal + "%");
    }

    public void setCurrentTracffic() {
        double f1;
        float f = (float) (currentFlow / 1024.0);
        BigDecimal bg = new BigDecimal(f);
        if (currentTraffic != null) {
            if (currentFlow == 0) {
                currentTraffic.setText("实时 " + (int) f + "k/s");
            } else if (currentFlow < 1024) {
                f1 = bg.setScale(2, BigDecimal.ROUND_HALF_UP).doubleValue();
                currentTraffic.setText("实时 " + f1 + "k/s");
            } else if (currentFlow < 10240) {
                f1 = bg.setScale(1, BigDecimal.ROUND_HALF_UP).doubleValue();
                currentTraffic.setText("实时 " + f1 + "k/s");
            } else {
                currentTraffic.setText("实时 " + (int) f + "k/s");
            }
        }
    }

    public void onPause() {
        stopCheckFlow();

        super.onPause();
    }

    private void stopCheckFlow() {
        if (flowTimer != null) {
            flowTimer.cancel();
            flowTimer = null;
        }
    }

    private Timer flowTimer;
    private long last_flow, last_mobile_flow;
    private long currentFlow;
    private boolean isMobile;
    private AccessPoint mAttempAccessPoint = WifiAdmin.sharedInstance().getActiveAccessPoint();

    public void startCheckCurrentFlow() {
        if (flowTimer == null) {
            flowTimer = new Timer();
        } else {
            flowTimer.cancel();
            flowTimer = new Timer();
        }
        flowTimer.schedule(new TimerTask() {

            @Override
            public void run() {
                currentFlow = currentFlow();
                if (currentFlow > 102400) {
                    currentFlow = 0;
                }
                handler.sendEmptyMessage(SET_TRAFFIC);
            }

        }, 0, 1000);

    }

    // TODO 实时流量 wifi 关闭是停止
    private long currentFlow() {
        long current_mobile_flow = TrafficStats.getMobileRxBytes();
        long current_total_flow = TrafficStats.getTotalRxBytes();
        long mobile_speed = current_mobile_flow - last_mobile_flow;
        long total_speed = current_total_flow - last_flow;
        last_flow = current_total_flow;
        last_mobile_flow = current_mobile_flow;
        return (total_speed - mobile_speed);
    }

    private WifiEventHandler wifiEventHandler = new WifiEventHandler() {

        @Override
        public void onWifiStatusChange(int state, int preState) {
            if (state == WifiManager.WIFI_STATE_DISABLED) {
                closePage();
            }
        }

        @Override
        public synchronized void onWifiConnectChange(NetworkInfo networkInfo,
                                                     String BSSID, WifiInfo wifiInfo) {
            if (wifiInfo != null) {
                mWifiAdmin = WifiAdmin.sharedInstance();
                // mAttempAccessPoint =
                // mWifiAdmin.getAccessPointByBSSID(wifiInfo
                // .getBSSID());
                if (mAttempAccessPoint != null) {
                    joinedBssid
                            .setText(mAttempAccessPoint.getScanResult().SSID);
                    joinedState.setText(mAttempAccessPoint
                            .getConnectStateString());
                    setSignal(mAttempAccessPoint.getSignalPersent());
                    stateImage.setImageDrawable(mAttempAccessPoint
                            .getChangeStateDrawable());
                } else {
                    closePage();
                }
            } else {
                closePage();
            }
        }

        @Override
        public void onSupStatusChange(SupplicantState newState, int error) {

        }

        @Override
        public void onSupConnectChange(Boolean isConnected) {
            // TODO Auto-generated method stub

        }

        @Override
        public void onRssiChange(int newRssi) {
            // TODO Auto-generated method stub

        }

        @Override
        public void onNetworkIdChange() {
            // TODO Auto-generated method stub

        }

        @Override
        public void onScanResultAvaiable() {
            // TODO Auto-generated method stub

        }

    };
    private static int leftTimes = 0;
    private WifiAdmin mWifiAdmin;

    private void requestServerRemainChance() {
//        // 获取查看次数
//        if (ReleaseConstant.getAdPlatform() == ReleaseConstant.ADPLATFORM.ADPLATFORM_YJF) {
//            ScoreWallSDK.getInstance(CheckPasswordActivity.this).getScore(
//                    CheckPasswordActivity.this, CheckPasswordActivity.this);
//        } else if (ReleaseConstant.getAdPlatform() == ReleaseConstant.ADPLATFORM.ADPLATFORM_WANPU) {
//            AppConnect.getInstance(this).getPoints(this);
//        } else {
//            int myPointBalance = PointsManager.getInstance(this).queryPoints();
//            leftTimes = myPointBalance;
//            setStatus();
//        }


    }

    private void consume() {
        leftTimes -= SCORE_PER_VIEW;
//        if (ReleaseConstant.getAdPlatform() == ReleaseConstant.ADPLATFORM.ADPLATFORM_YJF) {
//            ScoreWallSDK.getInstance(CheckPasswordActivity.this).consumeScore(
//                    CheckPasswordActivity.this, CheckPasswordActivity.this, SCORE_PER_VIEW);
//        } else if (ReleaseConstant.getAdPlatform() == ReleaseConstant.ADPLATFORM.ADPLATFORM_WANPU) {
//            AppConnect.getInstance(this).spendPoints(SCORE_PER_VIEW, this);
//        } else {
//            PointsManager.getInstance(this).spendPoints(SCORE_PER_VIEW);
//        }

    }

    private final int status_not_enought_score = 0;
    private final int status_can_view_password = 2;
    private final int status_password_viewed = 3;

    private void switchViewStatus(int status) {
        switch (status) {
            case status_not_enought_score:
                unloginView.setVisibility(View.VISIBLE);
                loginedView.setVisibility(View.GONE);
                showPwdView.setVisibility(View.GONE);
                break;
            case status_can_view_password:
                unloginView.setVisibility(View.GONE);
                loginedView.setVisibility(View.VISIBLE);
                showPwdView.setVisibility(View.GONE);
                break;
            case status_password_viewed:
                unloginView.setVisibility(View.GONE);
                loginedView.setVisibility(View.GONE);
                showPwdView.setVisibility(View.VISIBLE);
                break;
        }
    }

    @Override
    protected void onClickEvent(View v) {
        switch (v.getId()) {
            case R.id.login:
                // 引导赚取积分
//                if (ReleaseConstant.getAdPlatform() == ReleaseConstant.ADPLATFORM.ADPLATFORM_YJF) {
//                    ScoreWallSDK.getInstance(CheckPasswordActivity.this)
//                            .showScoreWall();
//                } else if (ReleaseConstant.getAdPlatform() == ReleaseConstant.ADPLATFORM.ADPLATFORM_WANPU) {
//
//                    AppConnect.getInstance(this).setOffersCloseListener(new AppListener() {
//                        @Override
//                        public void onOffersClose() {
//                            AppConnect.getInstance(CheckPasswordActivity.this).close();
//                            requestServerRemainChance();
//                        }
//                    });
//                    AppConnect.getInstance(this).showOffers(this);
//                } else {
//                    OffersManager.getInstance(this).showOffersWall();
//                }
                showShare();

                break;
            case R.id.check_pwd:
                MobclickAgent
                        .onEvent(this, "click_password_in_detail", getResources()
                                .getString(R.string.click_password_in_detail));
                if (leftTimes <= 0) {
                    setStatus();
                    return;
                }
                switchViewStatus(status_password_viewed);
                if (mAttempAccessPoint != null) {
                    wifiPwd = mAttempAccessPoint.getDataModel().getPassword(false);
                    if (!TextUtils.isEmpty(wifiPwd) && !wifiPwd.equals("*")) {
                        password.setText(wifiPwd);
                        shareTitleView.setVisibility(View.VISIBLE);
                        shareContainer.setVisibility(View.VISIBLE);
                    } else {
                        wifiPwd = WiFiLocalManager.getWifiInfo(mAttempAccessPoint.getPrintableSsid()).password;
                    }
                    consume();
                    if (!TextUtils.isEmpty(wifiPwd)) {
                        password.setText(wifiPwd);
                    } else {
                        password.setText("请重新进入页面");
                    }
                } else {
                    password.setText("当前没有连接WiFi,请连接WiFi后再查看");
                }

                break;
            case R.id.break_connection:
                MobclickAgent.onEvent(this, "click_disconnect_wifi", getResources()
                        .getString(R.string.stat_disconnect_wifi));
                if (WifiAdmin.sharedInstance().disconnect()) {
                    closePage();
                } else {
                    Toast.makeText(this, "WiFi断开失败", Toast.LENGTH_SHORT).show();
                }
                break;

            case R.id.back_wifilist:
                closePage();
                break;
            case R.id.btn_share_to_friends:
                showShare();
                break;


            case R.id.show_pwd:
                showPwdPopupWind(mAttempAccessPoint.getDataModel().getPassword(
                        false));
                break;

            default:
                break;
        }

    }


    private void closePage() {
        MobclickAgent.onEvent(this, "close_wifi_detail", getResources()
                .getString(R.string.close_wifi_detail));
        finish();
        overridePendingTransition(R.anim.shit, R.anim.activity_downtoup_out);
    }

    @Override
    protected void findViewById() {
        try {
            unloginView = (LinearLayout) findViewById(R.id.user_unlogin);
            loginedView = (LinearLayout) findViewById(R.id.user_logined);
            showPwdView = (LinearLayout) findViewById(R.id.user_check_pwd);

            breakConnect = (Button) findViewById(R.id.break_connection);
            backWifilist = (ImageView) findViewById(R.id.back_wifilist);
            login = (Button) findViewById(R.id.login);
            checkPassword = (Button) findViewById(R.id.check_pwd);

            currentSignal = (TextView) findViewById(R.id.current_signal);
            currentTraffic = (TextView) findViewById(R.id.current_traffic);
            joinedBssid = (TextView) findViewById(R.id.joined_wifi_bssid);
            joinedState = (TextView) findViewById(R.id.joined_wifi_state);
            stateImage = (ImageView) findViewById(R.id.joined_state_imageview);
            password = (TextView) findViewById(R.id.show_pwd);
            remain = (TextView) findViewById(R.id.times_of_checkpwd);
            achieve = (TextView) findViewById(R.id.achieve_time);
            title_save = (TextView) findViewById(R.id.title_save_check);
            sharePrompt = (TextView) findViewById(R.id.share_friend_prompt);
            unloginSharePrompt = (TextView) findViewById(R.id.unlogin_share_prompt);

            shareContainer = (LinearLayout) findViewById(R.id.share_container);
            shareTitleView = (TextView) findViewById(R.id.share_wifi_wpd_title);
            initView();
        } catch (Exception e) {
            closePage();
            e.printStackTrace();
        }
    }

    @Override
    protected void loadViewLayout() {
        setContentView(R.layout.activity_check_wifipassword);
    }

    @Override
    protected void processLogic() {

        requestServerRemainChance();
    }

    @Override
    protected void setListener() {
        breakConnect.setOnClickListener(this);
        backWifilist.setOnClickListener(this);
        login.setOnClickListener(this);
        checkPassword.setOnClickListener(this);
        password.setOnClickListener(this);
        this.findViewById(R.id.btn_share_to_friends).setOnClickListener(this);
    }

    private PopupWindow popupWindow;

    private void showPwdPopupWind(String pwd) {
        int displayWidth = this.getWindowManager().getDefaultDisplay()
                .getWidth();
        int displayHeight = this.getWindowManager().getDefaultDisplay()
                .getHeight();
        View view = LayoutInflater.from(this).inflate(
                R.layout.show_wifipwd_pop, null);
        TextView wifiPwd = (TextView) view.findViewById(R.id.popup_wifi_pwd);
        wifiPwd.setText(pwd);
        view.findViewById(R.id.popup_sure).setOnClickListener(
                new OnClickListener() {

                    @Override
                    public void onClick(View v) {
                        popupWindow.dismiss();
                    }
                });
        popupWindow = new PopupWindow(this);
        popupWindow.setContentView(view);
        popupWindow.setWidth(displayWidth - ViewUtil.dip2px(this, 80));
        popupWindow.setHeight(LayoutParams.WRAP_CONTENT);
        // ColorDrawable dw = new ColorDrawable(0x50000000);
        popupWindow.setBackgroundDrawable(getResources().getDrawable(
                R.drawable.shape_bg_pwd_popup));
        popupWindow.setOutsideTouchable(false);
        // popupWindow.setAnimationStyle(R.style.PopupWindowAnimation2);
        popupWindow.setFocusable(true);
        popupWindow.showAtLocation(findViewById(R.id.main_contain),
                Gravity.CENTER, 0, 0); //
        // 设置layout在PopupWindow中显示的位置

    }

    @Override
    protected void updateView() {
        setStatus();
    }

    /**
     * **********易积分广告回调**********
     */
    @Override
    public void updateScoreFailed(int arg0, int arg1, String arg2) {
        closeMyDialog();
    }

    /**
     * 积分回调
     *
     * @param arg0 接口类型
     * @param arg1 当前可用积分
     * @param arg2 当前增加/减少的积分数
     * @param arg3 积分单位
     */
    @Override
    public void updateScoreSuccess(int arg0, int arg1, int arg2, String arg3) {
        LogUtil.e("updateScoreSuccess", "arg0:" + arg0 + " arg1:" + arg1 + " arg2:" + arg2 + " arg3:" + arg3);
        switch (arg0) {
            case 1:// 查询
                leftTimes = arg1;
                break;
            case 2:// 消费

                break;
            case 3:// 增加

                break;

            default:
                break;
        }
        closeMyDialog();
        setStatus();
    }

    private void setStatus() {
        setRemainChance();
        setAchieveTextColor();
        if (leftTimes >= SCORE_PER_VIEW) {
            switchViewStatus(status_can_view_password);
        } else {
            switchViewStatus(status_not_enought_score);
        }
    }

    /**
     * **********万普广告回调**********
     */
    @Override
    public void getUpdatePoints(String s, int i) {
        LogUtil.e(TAG, s + ";i=" + i);
        leftTimes = i;
        closeMyDialog();
        setStatus();
    }

    @Override
    public void getUpdatePointsFailed(String s) {

    }

    @Override
    public void onComplete(Platform platform, int i, HashMap<String, Object> stringObjectHashMap) {
        LogUtil.e(platform.getName(), i + "---" + stringObjectHashMap);
        Toast.makeText(this, "分享成功，+2次查看机会", Toast.LENGTH_SHORT).show();
        leftTimes += SCORE_PER_VIEW * 2;
    }

    @Override
    public void onError(Platform platform, int i, Throwable throwable) {
        Toast.makeText(this, "分享失败", Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onCancel(Platform platform, int i) {
        Toast.makeText(this, "取消分享", Toast.LENGTH_SHORT).show();
    }
}
