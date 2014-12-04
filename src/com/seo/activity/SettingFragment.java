package com.seo.activity;

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.CompoundButton;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.seo.activity.setting.AboutAppActivity;
import com.seo.activity.setting.TermsOfServiceActivity;
import com.seo.activity.test.TestCenterActivity;
import com.seo.app.notification.NotificationUtil;
import com.seo.app.views.SwitchButton;
import com.seo.constant.ConfigConstant;
import com.seo.constant.ReleaseConstant;
import com.seo.constant.RequestConstant;
import com.seo.model.ClientInfo;
import com.seo.utils.ResUtil;
import com.seo.wifikey.Gl;
import com.seo.wifikey.R;
import com.umeng.analytics.MobclickAgent;
import com.umeng.update.UmengUpdateAgent;

import cn.sharesdk.framework.Platform;
import cn.sharesdk.framework.ShareSDK;
import cn.sharesdk.onekeyshare.OnekeyShare;
import cn.sharesdk.onekeyshare.ShareContentCustomizeCallback;
import cn.sharesdk.sina.weibo.SinaWeibo;
import cn.sharesdk.wechat.moments.WechatMoments;


/**
 * @author jack at 2014-8-25
 * @filename ConnectFragment.java
 * @packagename com.hiwifi.activity
 * @projectname hiwifi1.0.1
 */
public class SettingFragment extends Fragment implements OnClickListener,
        OnItemClickListener, OnCheckedChangeListener {

    TextView versionTextView;
    ImageView appNewImageView;
    SwitchButton shareSwitchButton;
    SwitchButton backupSwitchButton;
    SwitchButton notifySwitchButton;
    View rootView;


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        rootView = inflater.inflate(R.layout.tab_settings, null);

        appNewImageView = (ImageView) rootView.findViewById(R.id.iv_app_new);
        shareSwitchButton = (SwitchButton) rootView
                .findViewById(R.id.sb_auto_share);
        backupSwitchButton = (SwitchButton) rootView
                .findViewById(R.id.sb_auto_backup);
        notifySwitchButton = (SwitchButton) rootView.findViewById(R.id.swt_notify);
        shareSwitchButton.setOnCheckedChangeListener(this);
        backupSwitchButton.setOnCheckedChangeListener(this);
        notifySwitchButton.setOnCheckedChangeListener(this);
        shareSwitchButton.setChecked(ClientInfo.shareInstance().isAutoShared());
        backupSwitchButton
                .setChecked(ClientInfo.shareInstance().isAutoBackup());
        notifySwitchButton.setChecked(ClientInfo.shareInstance().isNotifyOpen());
        versionTextView = (TextView) rootView.findViewById(R.id.tv_version);
        rootView.findViewById(R.id.ll_about).setOnClickListener(this);
        rootView.findViewById(R.id.ll_agreement).setOnClickListener(this);
        rootView.findViewById(R.id.ll_version).setOnClickListener(this);
        rootView.findViewById(R.id.ll_app_recommend).setOnClickListener(this);
        rootView.findViewById(R.id.ll_app_share).setOnClickListener(this);
        if (ReleaseConstant.ISDEBUG) {
            rootView.findViewById(R.id.ll_test).setOnClickListener(this);
        } else {
            rootView.findViewById(R.id.ll_test).setVisibility(View.GONE);
        }
        rootView.findViewById(R.id.feedback).setOnClickListener(this);
        return rootView;
    }

    private static boolean checkUpdated = false;

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        if (!checkUpdated) {
            UmengUpdateAgent.update(Gl.Ct());
            checkUpdated = true;
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        versionTextView.setText("v" + Gl.getAppVersionName());
        MobclickAgent.onPageStart(this.getClass().getSimpleName()); //统计页面
        updateView();

    }

    @Override
    public void onHiddenChanged(boolean hidden) {
        super.onHiddenChanged(hidden);
        if (hidden) {
            MobclickAgent.onPageEnd(this.getClass().getSimpleName());
        } else {
            MobclickAgent.onPageStart(this.getClass().getSimpleName()); //统计页面
            updateView();
        }
    }

    @Override
    public void onPause() {
        super.onPause();
        MobclickAgent.onPageEnd(this.getClass().getSimpleName());
    }

    private void updateView() {

    }

    @Override
    public void onClick(View v) {
        Intent intent = new Intent();
        switch (v.getId()) {
            case R.id.ll_version:
                UmengUpdateAgent.update(Gl.Ct());
                break;
            case R.id.ll_about:
                intent.setClass(getActivity(), AboutAppActivity.class);
                getActivity().startActivity(intent);
                break;

            case R.id.feedback:
                intent.setClass(getActivity(), FeedbackActivity.class);
                getActivity().startActivity(intent);

                break;
            case R.id.ll_agreement:
                intent.setClass(getActivity(), TermsOfServiceActivity.class);
                getActivity().startActivity(intent);
                break;

            case R.id.ll_app_recommend:
                intent.setClass(getActivity(), AppRecommendActivity.class);
                getActivity().startActivity(intent);
                break;

            case R.id.ll_app_share:
                showShare();
                break;

            case R.id.ll_test:
                intent.setClass(getActivity(), TestCenterActivity.class);
                getActivity().startActivity(intent);
                break;
            default:
                break;
        }
    }

    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position,
                            long id) {
    }

    @Override
    public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
        switch (buttonView.getId()) {
            case R.id.sb_auto_share:
                ClientInfo.shareInstance().setAutoShared(isChecked);
                if (!isChecked) {
                    Toast.makeText(getActivity(), "密码已经停止分享",
                            Toast.LENGTH_LONG).show();
                }
                break;

            case R.id.swt_notify:
                ClientInfo.shareInstance().setNotify(isChecked);
                if (!isChecked) {
                    NotificationUtil.sendCancelMessage();
                }
                break;

            case R.id.sb_auto_backup:
                ClientInfo.shareInstance().setAutoBackup(isChecked);
                if (!isChecked) {
                    Toast.makeText(getActivity(), "建议开启该功能，换手机也可自动同步WiFi本地密码",
                            Toast.LENGTH_LONG).show();
                }
                break;

            default:
                break;
        }
    }

    private void showShare() {
        ShareSDK.initSDK(getActivity());
        OnekeyShare oks = new OnekeyShare();
        //关闭sso授权
        oks.disableSSOWhenAuthorize();

        // 分享时Notification的图标和文字
        oks.setNotification(R.drawable.hiwifi_launcher, getString(R.string.app_name));
        // title标题，印象笔记、邮箱、信息、微信、人人网和QQ空间使用
        oks.setTitle(getString(R.string.share));
        // text是分享文本，所有平台都需要这个字段
        oks.setText("在外面也能连上免费wifi了，#" + ResUtil.getStringById(R.string.app_name) + "#还行！我真是一会儿都离不开网啊，推荐你也试试：" + RequestConstant.getUrl(RequestConstant.RequestTag.URL_APP_DOWNLOAD));
        // imagePath是图片的本地路径，Linked-In以外的平台都支持此参数
//        oks.setImagePath("/sdcard/test.jpg");
        // url仅在微信（包括好友和朋友圈）中使用
        oks.setUrl(RequestConstant.getUrl(RequestConstant.RequestTag.URL_APP_DOWNLOAD));
        // comment是我对这条分享的评论，仅在人人网和QQ空间使用
        oks.setComment("真的很赞!");
        // site是分享此内容的网站名称，仅在QQ空间使用
        oks.setSite(getString(R.string.app_name));
        // siteUrl是分享此内容的网站地址，仅在QQ空间使用
        oks.setSiteUrl(RequestConstant.getUrl(RequestConstant.RequestTag.URL_APP_DOWNLOAD));
        oks.setShareContentCustomizeCallback(new ShareContentCustomizeCallback() {
            @Override
            public void onShare(Platform platform, Platform.ShareParams paramsToShare) {
                if (SinaWeibo.NAME.equals(platform.getName())
                        || WechatMoments.NAME.equals(platform.getName())) {
                    paramsToShare.setText("在外面也能连上免费wifi了，#" + ResUtil.getStringById(R.string.app_name) + "#还行！我真是一会儿都离不开网啊，推荐大家也试试：" + RequestConstant.getUrl(RequestConstant.RequestTag.URL_APP_DOWNLOAD));
                }
            }
        });
        // 启动分享GUI
        oks.show(getActivity());
    }
}
