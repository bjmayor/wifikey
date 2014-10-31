package com.seo.app.notification;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.widget.RemoteViews;

import com.seo.app.services.DaemonService;
import com.seo.wifikey.Gl;
import com.seo.model.ClientInfo;
import com.seo.wifikey.R;

public class NotificationUtil {

    private String verson = android.os.Build.VERSION.RELEASE;

    private int Notification_ID_BASE = 110;
    private int NOTIFICATION_ID_MAINTAIN = 911;
    private NotificationManager notificationManager;
    private Context mContext;
    public Notification notificaion = null;
    private int notificationId = NOTIFICATION_ID_MAINTAIN;

    public int getNotificationId() {
        return notificationId;
    }

    public Notification getNotification() {
        return notificaion;
    }

    public NotificationUtil(Context mContext) {
        this.mContext = mContext.getApplicationContext();
        notificationManager = (NotificationManager) mContext
                .getSystemService(android.content.Context.NOTIFICATION_SERVICE);
        this.notificaion = new Notification();
        this.notificaion.icon = R.drawable.hiwifilogo288;
        this.notificaion.flags = Notification.FLAG_FOREGROUND_SERVICE | Notification.FLAG_ONGOING_EVENT;
        Intent localIntent1 = new Intent(this.mContext, com.seo.activity.MainTabActivity.class);
        localIntent1.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_RESET_TASK_IF_NEEDED | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        PendingIntent pendingIntent = PendingIntent.getActivity(mContext, 2, localIntent1, PendingIntent.FLAG_UPDATE_CURRENT);
        this.notificaion.contentIntent = pendingIntent;
    }

    public int showConnectedWifiNotNetwork(String SSID) {
        if (!isNotifyEnabled()) {
            return notificationId;
        }
        // 创建一个NotificationManager的引用
        // 定义Notification的各种属性
        RemoteViews expandedView = new RemoteViews(mContext.getPackageName(),
                R.layout.notification_wifi_connect_show);
        expandedView.setTextViewText(R.id.notification_ssid, "WiFi已连接但不能上网");
        expandedView.setTextViewText(R.id.tv_context_title, SSID + " 不能上网");

        Intent intentOpener = new Intent(mContext, DaemonService.class);
        intentOpener.putExtra(DaemonService.EXTRA_COMMAND,
                DaemonService.actionType_closeWifi);
        PendingIntent shit = PendingIntent.getService(mContext, 0,
                intentOpener, PendingIntent.FLAG_UPDATE_CURRENT);
        expandedView.setOnClickPendingIntent(R.id.btn_change, shit);
        this.notificaion.contentView = expandedView;
        this.notificaion.icon = R.drawable.hiwifilogo288;
        this.notificaion.when = System.currentTimeMillis();

        notificationManager.notify(notificationId, this.notificaion);
        return notificationId;
    }

    public int showConnectedWifi(String SSID) {
        return showConnectedWifi(SSID, false);
    }

    public int showConnectedWifi(String SSID, boolean requiredAuth) {
        if (!isNotifyEnabled()) {
            return notificationId;
        }
        String titleText;
        String contentText;
        if (requiredAuth) {
            titleText = SSID + " 需认证";
            contentText = "去认证登录后使用此WiFi上网";
        } else {
            titleText = SSID + " 正在上网";
            contentText = "正在连接此WiFi上网";
        }

        RemoteViews expandedView = new RemoteViews(mContext.getPackageName(),
                R.layout.notification_wifi_connect_show);
        expandedView.setTextViewText(R.id.notification_ssid, contentText);
        expandedView.setTextViewText(R.id.tv_context_title, titleText);

        Intent intentOpener = new Intent(mContext, DaemonService.class);
        intentOpener.putExtra(DaemonService.EXTRA_COMMAND,
                DaemonService.actionType_closeWifi);
        PendingIntent shit = PendingIntent.getService(mContext, 0,
                intentOpener, PendingIntent.FLAG_UPDATE_CURRENT);
        expandedView.setOnClickPendingIntent(R.id.btn_change, shit);
        this.notificaion.contentView = expandedView;
        this.notificaion.icon = R.drawable.hiwifilogo288;
        this.notificaion.when = System.currentTimeMillis();
//		PendingIntent pendingIntent;
//		Intent localIntent1 = new Intent(this.mContext, com.hiwifi.activity.MainActivity.class);
//		localIntent1.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK|Intent.FLAG_ACTIVITY_RESET_TASK_IF_NEEDED|Intent.FLAG_ACTIVITY_CLEAR_TASK);
//		pendingIntent = PendingIntent.getActivity(mContext, 2, localIntent1, PendingIntent.FLAG_UPDATE_CURRENT);
//		this.notificaion.contentIntent = pendingIntent;

        notificationManager.notify(notificationId, this.notificaion);
        return notificationId;
    }

    public int showNoWifiConnected() {
        if (!isNotifyEnabled()) {
            return notificationId;
        }
        // 创建一个NotificationManager的引用
        // 定义Notification的各种属性
        RemoteViews expandedView = new RemoteViews(mContext.getPackageName(),
                R.layout.notification_wifi_no_connected);
        Intent intentno = new Intent(mContext, DaemonService.class);
        intentno.putExtra(DaemonService.EXTRA_COMMAND,
                DaemonService.actionType_closeWifi);
        PendingIntent bool = PendingIntent.getService(mContext, 110,
                intentno, PendingIntent.FLAG_UPDATE_CURRENT);
        expandedView.setOnClickPendingIntent(R.id.btn_check, bool);
        this.notificaion.contentView = expandedView;
        this.notificaion.icon = R.drawable.hiwifilogo288_off;
        this.notificaion.when = System.currentTimeMillis();

//		Intent localIntent1 = new Intent(this.mContext, com.hiwifi.activity.MainActivity.class);
//		localIntent1.setAction("com.hiwifi.hiwifi.main");
//		localIntent1.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK|Intent.FLAG_ACTIVITY_RESET_TASK_IF_NEEDED|Intent.FLAG_ACTIVITY_CLEAR_TASK);
//		PendingIntent pendingIntent = PendingIntent.getActivity(mContext, 2, localIntent1, PendingIntent.FLAG_UPDATE_CURRENT);
//		this.notificaion.contentIntent = pendingIntent;
        // 使用自定义下拉视图时，不需要再调用setLatestEventInfo()方法
        // 但是必须定义 contentIntent
        this.notificaion.when = System.currentTimeMillis();
        notificationManager.notify(notificationId, this.notificaion);
        return notificationId;
    }

    public int showOpenWifiHelper() {
        if (!isNotifyEnabled()) {
            return notificationId;
        }
        RemoteViews expandedView = new RemoteViews(mContext.getPackageName(),
                R.layout.notification_wifi_open_helper);
        Intent intentOpener = new Intent(mContext, DaemonService.class);
//		intentOpener.setFlags(268435456);
        intentOpener.putExtra(DaemonService.EXTRA_COMMAND,
                DaemonService.actionType_openWifi);
        PendingIntent pendingIntentA = PendingIntent.getService(mContext, 0,
                intentOpener, PendingIntent.FLAG_UPDATE_CURRENT);
        expandedView.setOnClickPendingIntent(R.id.btn_open_wifi, pendingIntentA);
        this.notificaion.contentView = expandedView;
        this.notificaion.icon = R.drawable.hiwifilogo288_off;
        this.notificaion.when = System.currentTimeMillis();
        notificationManager.notify(notificationId, this.notificaion);
        return notificationId;
    }

    public void creatR(Context mContext) {

    }

    public  boolean isNotifyEnabled() {
        return ClientInfo.shareInstance().isNotifyOpen();
    }

    public static void sendCancelMessage()
    {
        DaemonService.execCommand(Gl.Ct(), DaemonService.actionType_closeNotify);

    }

    public void cancelNotify()
    {
        this.notificaion.flags = Notification.FLAG_AUTO_CANCEL;
        notificationManager.notify(notificationId, notificaion);
        notificationManager.cancel(notificationId);
    }

}
