package com.hiwifi.activity.wifi;

import java.math.BigDecimal;
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

import cn.sharesdk.sina.weibo.SinaWeibo;
import cn.sharesdk.tencent.qq.QQ;
import cn.sharesdk.wechat.friends.Wechat;
import cn.sharesdk.wechat.moments.WechatMoments;

import com.hiwifi.activity.base.BaseActivity;
import com.hiwifi.app.receiver.HiwifiBroadcastReceiver;
import com.hiwifi.app.receiver.HiwifiBroadcastReceiver.WifiEventHandler;
import com.hiwifi.constant.ConfigConstant;
import com.hiwifi.model.log.LogUtil;
import com.hiwifi.model.wifi.AccessPoint;
import com.hiwifi.model.wifi.AccessPoint.WifiConnectState;
import com.hiwifi.model.wifi.WifiAdmin;
import com.hiwifi.shareSdk.ShareUtil;
import com.hiwifi.utils.ViewUtil;
import com.seo.wifikey.R;
import com.umeng.analytics.MobclickAgent;

public class CheckPasswordActivity extends BaseActivity implements
        OnClickListener, UpdateScordNotifier {

    private LinearLayout unloginView, loginedView, showPwdView, shareContainer;
    private ImageView backWifilist, stateImage, shareToWechat, shareToMoments,
            shareToQQ, shareToWeibo;
    private TextView currentSignal, currentTraffic, joinedBssid, joinedState,
            password, remain, achieve, title_save, shareTitleView, sharePrompt,
            unloginSharePrompt;
    private Button checkPassword, login, breakConnect;

    private final int SET_TRAFFIC = 1;
    private final int REQUEST_LOGIN = 1001;
    private String wifiPwd = "";

    public final String APP_URL = "http://www.hiwifi.com/app";

    private Handler handler = new Handler() {
        public void handleMessage(android.os.Message msg) {
            if (msg.what == SET_TRAFFIC) {
                setCurrentTracffic();
            }
        }

        ;
    };

    private void initView() {
        setRemainChance();
        setAchieveTextColor();
        hasLoginedView();
        if (mAttempAccessPoint != null) {
            this.joinedBssid.setText(mAttempAccessPoint.getScanResult().SSID);
            this.joinedState
                    .setText(mAttempAccessPoint.getConnectStateString());
            setSignal(mAttempAccessPoint.getSignalPersent());
            this.stateImage.setImageDrawable(mAttempAccessPoint
                    .getChangeStateDrawable());
        }

    }

    private void hasLoginedView() {
        mWifiAdmin = WifiAdmin.sharedInstance();
        mAttempAccessPoint = mWifiAdmin.getActiveAccessPoint();
        if (leftTimes > 5) {
            switchViewStatus(status_can_view_password);
        } else {
            switchViewStatus(status_not_enought_score);
        }
        if (mAttempAccessPoint != null) {
            wifiPwd = mAttempAccessPoint.getDataModel().getPassword(false);
            if (mAttempAccessPoint.getConnectState() == WifiConnectState.connectState_canconnect) {
                unloginSharePrompt.setVisibility(View.INVISIBLE);
            } else if (mAttempAccessPoint.getConnectState() == WifiConnectState.connectState_local_restore) {
                unloginSharePrompt.setVisibility(View.INVISIBLE);
            }
        }

    }

    private void noWifiPwd() {
        remain.setVisibility(View.GONE);
        achieve.setVisibility(View.GONE);
        sharePrompt.setVisibility(View.GONE);
        checkPassword.setClickable(false);
        checkPassword.setEnabled(false);
        checkPassword.setBackgroundResource(R.drawable.selector_btn_gray);
        title_save.setText("无密码");
        if (mAttempAccessPoint != null) {
            if (mAttempAccessPoint.getConnectState() == WifiConnectState.connectState_canconnect) {
                title_save.setText("无密码");
            } else if (mAttempAccessPoint.getConnectState() == WifiConnectState.connectState_local_restore) {
                title_save.setText("本地密码，无法查看");
            }
        } else {
            closePage();
        }

    }

    private void setRemainChance() {
        int color = getResources().getColor(R.color.text_blue);
        String shit = "今日还可以查看";
        String times = String.valueOf(leftTimes / 5);// "12";
        SpannableString shitshit = new SpannableString(shit + times + "次");
        shitshit.setSpan(new ForegroundColorSpan(color), shit.length(),
                shit.length() + times.length(),
                Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
        remain.setText(shitshit);
    }

    private void setAchieveTextColor() {
        int color = getResources().getColor(R.color.text_blue);
        SpannableString prompt = new SpannableString("（做任务赚积分，可获得更多查看次数）");
        prompt.setSpan(new ForegroundColorSpan(color), 12, 13,
                Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
        achieve.setText(prompt);
    }

    String title = "小极WiFi钥匙";

    private void shareToWeibo() {
        // TODO Auto-generated method stub
        ShareUtil share = new ShareUtil(this);
        String ssid = mAttempAccessPoint.getScanResult().SSID;
        // String shareText = "小极WIFI钥匙告诉你，" + ssid + "的连接密码为" + wifiPwd + "。"
        // + getResources().getString(R.string.share_moments_content);
        share.initImagePath(R.drawable.wifikey_icon_share);
        // oks.setImagePath(ConfigConstant.IMAGE_PATH);
        share.showShare(SinaWeibo.NAME, shareText, title, APP_URL,
                ConfigConstant.IMAGE_PATH);
    }

    private void shareToWechat() {
        if (TextUtils.isEmpty(wifiPwd)) {
            return;
        }
        ShareUtil share = new ShareUtil(this);
        String ssid = mAttempAccessPoint.getScanResult().SSID;
        String shareText = ssid + "的连接密码为" + wifiPwd + "。"
                + getResources().getString(R.string.share_moments_content);
        share.showShare(Wechat.NAME, shareText, title, null, null);
    }

    private void shareToQQ() {
        ShareUtil share = new ShareUtil(this);
        String ssid = mAttempAccessPoint.getScanResult().SSID;
        // String shareText = "小极WiFi钥匙今天帮我连上了" + ssid + "，这个WiFi的连接密码为" +
        // wifiPwd
        // + "，你要是在附近也可以来试一试。"
        // + getResources().getString(R.string.share_moments_content);
        String shareText = ssid + "的连接密码为" + wifiPwd + "。"
                + getResources().getString(R.string.share_qq_moments_content);
        share.showShare(QQ.NAME, shareText, title, null, null);
    }

    String shareText = "小极WiFi钥匙今天帮我免费上网了，您也来试试免费上网吧。http://www.hiwifi.com/app";// "我用\"小极WiFi钥匙\"成功免费上网";

    private void shareToMoments() {
        // WIFI名字 xxxxxxx ，你要是在附近也可以来试一试。上www.hiwifi.com
        ShareUtil share = new ShareUtil(this);
        // String ssid = mAttempAccessPoint.getScanResult().SSID;
        // String shareText = "极WIFI钥匙今天帮我连上了" + ssid + "，这个WIFI的连接密码为" +
        // wifiPwd
        // + "，你要是在附近也可以来试一试。"
        // + getResources().getString(R.string.share_moments_content);
        share.initImagePath(R.drawable.wifikey_icon_share);
        share.showShare(WechatMoments.NAME, shareText, shareText, APP_URL,
                ConfigConstant.IMAGE_PATH);
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
        startCheckCurrentFlow();
        ScoreWallSDK.getInstance(CheckPasswordActivity.this).getScore(
                CheckPasswordActivity.this, CheckPasswordActivity.this);
        HiwifiBroadcastReceiver.addListener(wifiEventHandler);
        super.onResume();
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
    private AccessPoint mAttempAccessPoint = null;

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
                hasLoginedView();
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
    private int leftTimes = 0;
    private WifiAdmin mWifiAdmin;

    private void requestServerRemainChance() {
        // 获取查看次数
        ScoreWallSDK.getInstance(CheckPasswordActivity.this).getScore(
                CheckPasswordActivity.this, CheckPasswordActivity.this);
    }

    private void comsume() {
        ScoreWallSDK.getInstance(CheckPasswordActivity.this).consumeScore(
                CheckPasswordActivity.this, CheckPasswordActivity.this, 5);
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
                ScoreWallSDK.getInstance(CheckPasswordActivity.this)
                        .showScoreWall();
                break;
            case R.id.check_pwd:
                MobclickAgent
                        .onEvent(this, "click_password_in_detail", getResources()
                                .getString(R.string.click_password_in_detail));
                if (leftTimes <= 0) {
                    Toast.makeText(this, "积分不够了，明天再试", Toast.LENGTH_SHORT).show();
                    return;
                }
                switchViewStatus(status_password_viewed);
                if (mAttempAccessPoint != null) {
                    wifiPwd = mAttempAccessPoint.getDataModel().getPassword(false);
                    comsume();
                    if (!TextUtils.isEmpty(wifiPwd) && !wifiPwd.equals("*")) {
                        password.setText(wifiPwd);
                        shareTitleView.setVisibility(View.VISIBLE);
                        shareContainer.setVisibility(View.VISIBLE);
                    }
                }
                if (!TextUtils.isEmpty(wifiPwd)) {
                    password.setText(wifiPwd);
                } else {
                    // 不可分享
                    shareTitleView.setVisibility(View.INVISIBLE);
                    shareContainer.setVisibility(View.INVISIBLE);
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
            case R.id.share_to_weixin:
                shareToWechat();
                break;

            case R.id.share_to_moments:
                shareToMoments();

                break;
            case R.id.share_to_qq:
                shareToQQ();

                break;
            case R.id.share_to_weibo:
                shareToWeibo();

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
            shareToWechat = (ImageView) findViewById(R.id.share_to_weixin);
            shareToMoments = (ImageView) findViewById(R.id.share_to_moments);
            shareToQQ = (ImageView) findViewById(R.id.share_to_qq);
            shareToWeibo = (ImageView) findViewById(R.id.share_to_weibo);

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
        shareToWechat.setOnClickListener(this);
        shareToMoments.setOnClickListener(this);
        checkPassword.setOnClickListener(this);
        shareToQQ.setOnClickListener(this);
        shareToWeibo.setOnClickListener(this);
        password.setOnClickListener(this);
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
        hasLoginedView();
    }

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
        LogUtil.e("updateScoreSuccess","arg0:"+arg0+" arg1:"+arg1+" arg2:"+arg2+" arg3:"+arg3);
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
        setRemainChance();
        switchViewStatus(status_can_view_password);
    }
}
