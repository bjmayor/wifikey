package com.seo.app.services;

import java.util.Iterator;
import java.util.List;
import java.util.Timer;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import android.annotation.SuppressLint;
import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.NetworkInfo;
import android.net.wifi.SupplicantState;
import android.net.wifi.WifiInfo;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Looper;
import android.os.Message;
import android.text.TextUtils;

import com.seo.app.notification.NotificationUtil;
import com.seo.app.receiver.HiwifiBroadcastReceiver;
import com.seo.app.receiver.HiwifiBroadcastReceiver.WifiEventHandler;
import com.seo.app.task.BackupRunnable;
import com.seo.app.task.BlacklistGetDaemonTaskRunner;
import com.seo.app.task.DaemonTaskRunner;
import com.seo.app.task.InsertRecentApplicationTask;
import com.seo.app.task.SaveApListRunnable;
import com.seo.app.task.SaveCmccLogRunnable;
import com.seo.app.task.UpdateConfRunnable;
import com.seo.app.task.WifiConfigToDBRunnable;
import com.seo.wifikey.Gl;
import com.seo.model.log.HWFLog;
import com.seo.model.speedtest.WebPageTester;
import com.seo.model.speedtest.WebPageTester.WebpageTestAction;
import com.seo.model.wifi.AccessPoint;
import com.seo.model.wifi.WifiAdmin;
import com.seo.model.wifi.adapter.CMCCConnectAdapter;
import com.seo.model.wifi.adapter.ChinanetConnectAdapter;
import com.seo.model.wifi.adapter.ConnectAdapter;
import com.seo.model.wifi.state.Account;
import com.seo.store.AccessPointModel;
import com.seo.utils.NetWorkConnectivity;

public class DaemonService extends Service {

    public final static String EXTRA_COMMAND = "com.hiwifi.hiwifi.command";
    private final String TAG = "DaemonService";
    public static final int actionType_init = 0;
    public static final int actionType_connectShouldChange = 1;
    public static final int actionType_connectWifi = 2;
    public static final int actionType_openWifi = 3;
    public static final int actionType_cmccLogin = 4;
    public static final int actionType_cmccLogout = 5;
    public static final int actionType_refreshCmccTimer = 6;
    public static final int actionType_undefined = 7;
    public static final int actionType_closeWifi = 8;
    public static final int actionType_closeNotify = 9;
    private static final long PERIODTIME = 60 * 60 * 1000;
    private AccessPoint notifyedAccessPoint = null;
    private boolean hasNotified = false;
    private Timer mTimer;
    private ExecutorService threadPool = null;
    private Handler mHandler;
    private IdleTask mIdleTask = new IdleTask();
    private ConnectAdapter adapter;
    private long wifiDealedLatestTime = -1;

    public static void execCommand(Context context, int actionType) {
        execCommand(context, actionType, null);
    }

    public static final String EXTRA_BUNDLE = "EXTRABUNDLE";
    public static final String EXTRA_Adapter = "EXTRA_Adapter";

    public static void execCommand(Context context, int actionType,
                                   Bundle bundle) {
        Intent service = new Intent();
        service.putExtra(EXTRA_COMMAND, actionType);
        if (bundle != null) {
            service.putExtra(EXTRA_BUNDLE, bundle);
        }
        service.setClass(context, DaemonService.class);
        context.startService(service);
    }

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onCreate() {
        HWFLog.e(TAG, "onCreate");
        super.onCreate();
        notificationUtil = new NotificationUtil(DaemonService.this);
        if (threadPool == null) {
            threadPool = Executors.newSingleThreadExecutor();
        }
        mHandler = new ServiceHandler();
        adapter = new CMCCConnectAdapter(getApplicationContext());
        if (WifiAdmin.sharedInstance().isChinaNetConnected()) {
            adapter = new ChinanetConnectAdapter(getApplicationContext());
        }
        addWifiListner();
        startScreenBroadcastReceiver();
        Looper.myQueue().addIdleHandler(mIdleTask);
        initNotification();

    }

    private final int msg_wifi_status_change = 0;
    private final int msg_wifi_connect_change = 1;
    private final int msg_sup_status_change = 2;

    @SuppressLint("HandlerLeak")
    public class ServiceHandler extends Handler {
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case msg_wifi_status_change:
                    if (!WifiAdmin.sharedInstance().getWifiManager()
                            .isWifiEnabled()) {
                        notificationUtil.showOpenWifiHelper();
                    } else {
                        AccessPoint accessPoint = WifiAdmin.sharedInstance()
                                .connectedAccessPoint();
                        if (accessPoint != null
                                && !TextUtils.isEmpty(accessPoint
                                .getPrintableSsid())) {
                            notificationUtil.showConnectedWifi(accessPoint
                                    .getPrintableSsid());
                        } else {
                            notificationUtil.showNoWifiConnected();
                        }
                    }
                    break;
                case msg_wifi_connect_change: {
                    Object[] objects = (Object[]) msg.obj;
                    if (objects.length == 3) {
                        new WifiConnectOnChangeRunnable((NetworkInfo) objects[0],
                                (String) objects[1], (WifiInfo) objects[2]).start();
                    }
                }
                break;

                case msg_sup_status_change: {
                    Object[] objects = (Object[]) msg.obj;
                    if (objects.length == 2) {
                        new SupStatusOnChangeRunnable((SupplicantState) objects[0],
                                (Integer) objects[1]).start();
                    }
                }
                break;
                default:
                    break;
            }
            super.handleMessage(msg);
        }
    }

    private ScreenBroadcastReceiver screenBroadcastReceiver = new ScreenBroadcastReceiver();

    private void startScreenBroadcastReceiver() {
        IntentFilter filter = new IntentFilter();
        filter.addAction(Intent.ACTION_SCREEN_ON);
        filter.addAction(Intent.ACTION_SCREEN_OFF);
        filter.addAction(Intent.ACTION_USER_PRESENT);
        registerReceiver(screenBroadcastReceiver, filter);
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        if (intent != null) {
            int actionType = intent.getIntExtra(EXTRA_COMMAND,
                    actionType_undefined);
            HWFLog.e(TAG, "service actiontype:" + actionType);
            switch (actionType) {
                case actionType_cmccLogin:
                    adapter = (ConnectAdapter) intent.getBundleExtra(EXTRA_BUNDLE)
                            .getSerializable(EXTRA_Adapter);
                    break;
                case actionType_cmccLogout:
                    break;
                case actionType_openWifi:
                    WifiAdmin.sharedInstance().openNetCard();
                    break;
                case actionType_refreshCmccTimer:
                    break;
                case actionType_closeWifi:
                    WifiAdmin.sharedInstance().closeNetCard();
                    break;
                case actionType_closeNotify:
                    stopForeground(true);
                    notificationUtil.cancelNotify();
                    break;
                default:
                    break;
            }
        }
        if (flags == Service.START_FLAG_REDELIVERY) {
            stopSelf(startId);
        }
        if (!wifiListerStarted()) {
            addWifiListner();
        }
        // 如果进程死了，会重启进程，并把intent重新传过去
        return START_REDELIVER_INTENT;
    }

    public Boolean wifiListerStarted() {
        return wifiListernerStarted;
    }

    private Boolean wifiListernerStarted = false;
    private NotificationUtil notificationUtil;


    public void addWifiListner() {
        wifiListernerStarted = true;
        HiwifiBroadcastReceiver.addListener(new DaemonWifiEventHandler());

    }

    public class DaemonWifiEventHandler extends WifiEventHandler {

        @Override
        public void onWifiStatusChange(int state, int preState) {
            HWFLog.e(TAG, "onWifiStatusChange");
            mHandler.removeMessages(msg_wifi_status_change);
            mHandler.sendEmptyMessageDelayed(msg_wifi_status_change, 200);
        }

        @Override
        public void onWifiConnectChange(NetworkInfo networkInfo, String BSSID,
                                        WifiInfo wifiInfo) {
            HWFLog.e(TAG, "onWifiConnectChange");
            mHandler.removeMessages(msg_wifi_connect_change);
            Message msg = mHandler.obtainMessage(msg_wifi_connect_change,
                    new Object[]{networkInfo, BSSID, wifiInfo});
            mHandler.sendMessageDelayed(msg, 200);
        }

        @Override
        public void onSupStatusChange(SupplicantState newState, int error) {
            HWFLog.e(TAG, "onSupStatusChange:" + newState);
            mHandler.removeMessages(msg_sup_status_change);
            Message msg = mHandler.obtainMessage(msg_sup_status_change,
                    new Object[]{newState, error});
            mHandler.sendMessageDelayed(msg, 200);
        }

        @Override
        public void onSupConnectChange(Boolean isConnected) {

        }

        @Override
        public void onRssiChange(int newRssi) {

        }

        @Override
        public void onNetworkIdChange() {

        }

        @Override
        public void onScanResultAvaiable() {
            HWFLog.e(TAG, "onScanResultAvaiable");
            long currentTime = System.currentTimeMillis();
            if (wifiDealedLatestTime < 0
                    || currentTime - wifiDealedLatestTime > PERIODTIME) {
                try {
                    saveAptoLocal();
                } catch (Exception e) {
                    e.printStackTrace();
                }
                threadPool.submit(new AutoChooseRunnable());
            }
        }

    }

    public Account getAccount() {
        return adapter == null ? null : adapter.getAccount();
    }


    private Boolean testFinished = true;
    private Runnable pingRunnable = new Runnable() {
        @Override
        public void run() {
            startPingPageService();
        }
    };

    private void startPingPageService() {
        mHandler.removeCallbacks(pingRunnable);
        WebPageTester tester = new WebPageTester(new WebpageTestAction() {
            @Override
            public void webpageFinishDownload(long avgTime) {
                AccessPoint accessPoint = WifiAdmin.sharedInstance()
                        .connectedAccessPoint();
                if (accessPoint != null) {
                    if (!TextUtils.isEmpty(accessPoint.getPrintableSsid())) {
                        notificationUtil.showConnectedWifi(accessPoint
                                .getPrintableSsid());
                    }

                } else {
                    if (WifiAdmin.sharedInstance().getWifiManager()
                            .isWifiEnabled()) {
                        notificationUtil.showNoWifiConnected();
                    } else {
                        notificationUtil.showOpenWifiHelper();
                    }
                }
            }

            @Override
            public void webpageErrorDownload(int errorCode, String message) {
                mHandler.postDelayed(pingRunnable, 5000);
                AccessPoint accessPoint = WifiAdmin.sharedInstance()
                        .connectedAccessPoint();
                if (accessPoint != null) {
                    if (!TextUtils.isEmpty(accessPoint.getPrintableSsid())) {
                        if (WebPageTester.ErrorCodeCaptured == errorCode) {
                            notificationUtil.showConnectedWifi(
                                    accessPoint.getPrintableSsid(), true);
                        } else {
                            notificationUtil
                                    .showConnectedWifiNotNetwork(accessPoint
                                            .getPrintableSsid());
                        }
                    }

                } else {
                    if (WifiAdmin.sharedInstance().getWifiManager()
                            .isWifiEnabled()) {
                        notificationUtil.showNoWifiConnected();
                    } else {
                        notificationUtil.showOpenWifiHelper();
                    }
                }
            }

        });
        tester.startTest(1000);
    }

    private synchronized void startCmccTimer() {
        if (!testFinished) {
            return;
        }
        if (!NetWorkConnectivity.isWifi(getApplicationContext())) {
            return;
        }
        WebPageTester tester = new WebPageTester(new WebpageTestAction() {
            @Override
            public void webpageFinishDownload(long avgTime) {
                testFinished = true;
                AccessPoint accessPoint = WifiAdmin.sharedInstance()
                        .connectedAccessPoint();
                if (accessPoint != null) {
                    if (!TextUtils.isEmpty(accessPoint.getPrintableSsid())) {
                        notificationUtil.showConnectedWifi(accessPoint
                                .getPrintableSsid());
                    }

                } else {
                    if (WifiAdmin.sharedInstance().getWifiManager()
                            .isWifiEnabled()) {
                        notificationUtil.showNoWifiConnected();
                    } else {
                        notificationUtil.showOpenWifiHelper();
                    }
                }
            }

            @Override
            public void webpageErrorDownload(int errorCode, String message) {
                testFinished = true;
                AccessPoint accessPoint = WifiAdmin.sharedInstance()
                        .connectedAccessPoint();
                if (accessPoint != null) {
                    if (!TextUtils.isEmpty(accessPoint.getPrintableSsid())) {
                        notificationUtil
                                .showConnectedWifiNotNetwork(accessPoint
                                        .getPrintableSsid());
                    }

                } else {
                    if (WifiAdmin.sharedInstance().getWifiManager()
                            .isWifiEnabled()) {
                        notificationUtil.showNoWifiConnected();
                    } else {
                        notificationUtil.showOpenWifiHelper();
                    }
                }
            }
        });
        tester.startTest(1000);
    }

    private void saveAptoLocal() throws Exception {
        List<AccessPoint> list = WifiAdmin.sharedInstance().getAccessPoints();
        if (list != null && list.size() > 0) {
            SaveAptoLocalRunnable runnable = new SaveAptoLocalRunnable(
                    list.iterator());
            threadPool.submit(runnable);
        }


    }

    public class SaveAptoLocalRunnable implements Runnable {
        private Iterator<AccessPoint> iterable;

        public SaveAptoLocalRunnable(Iterator<AccessPoint> iterator) {
            this.iterable = iterator;
        }

        @Override
        public void run() {
            while (this.iterable.hasNext()) {
                AccessPoint accessPoint = (AccessPoint) this.iterable.next();
                if (!accessPoint.getHasStored()) {
                    accessPoint.getDataModel();
                }
            }
        }

    }

    private void initNotification() {
        AccessPoint accessPoint = WifiAdmin.sharedInstance()
                .connectedAccessPoint();
        if (accessPoint != null) {
            if (!TextUtils.isEmpty(accessPoint.getPrintableSsid())) {
                notificationUtil.showConnectedWifi(accessPoint
                        .getPrintableSsid());
            }

        } else {
            if (WifiAdmin.sharedInstance().getWifiManager().isWifiEnabled()) {
                notificationUtil.showNoWifiConnected();
            } else {
                notificationUtil.showOpenWifiHelper();
            }
        }
        if (notificationUtil.isNotifyEnabled()) {
            startForeground(notificationUtil.getNotificationId(),
                    notificationUtil.getNotification());
        }

    }

    class AutoChooseRunnable implements Runnable {
        private WifiAdmin mWifiAdmin = WifiAdmin.sharedInstance();

        public void run() {
            if (mWifiAdmin.getWifiManager().getConfiguredNetworks() != null) {
                // Gl.GlConf.setMacCount(mWifiAdmin.getWifiManager()
                // .getConfiguredNetworks().size());
            }
            // List<AccessPoint> list = mWifiAdmin.getAccessPoints();
            // Iterator<AccessPoint> iterator = list.iterator();
            // while (iterator.hasNext()) {
            // AccessPoint accessPoint = (AccessPoint) iterator.next();
            // if (!accessPoint.isConfiged()) {
            // iterator.remove();
            // }
            // }
            // Collections.sort(list, WifiSortable.sortBySignal());
            // if (list.size() > 0) {
            // AccessPoint bestAccessPoint = list.get(0);
            // // check
            // try {
            // WifiInfo wifiInfo = mWifiAdmin.getWifiManager()
            // .getConnectionInfo();
            // // 已有连接的
            // if (wifiInfo != null && wifiInfo.getNetworkId() != -1) {
            // // 已是最好的wifi
            // if (wifiInfo.getSSID().equals(
            // bestAccessPoint.getScanResult().SSID)
            // || wifiInfo.getSSID().equals(
            // "\""
            // + bestAccessPoint
            // .getScanResult().SSID
            // + "\"")) {
            // return;
            // // 之前没有通知过，或者最好的不是之前通知的
            // } else if (notifyedAccessPoint == null
            // || !notifyedAccessPoint.equals(bestAccessPoint)) {
            // // 信号差是否在20以上
            // if ((bestAccessPoint.getScanResult().level - notifyedAccessPoint
            // .getScanResult().level) > 20) {
            // new HWFNotification().notifyMessage(
            // Gl.Ct(),
            // "hiwifi tips",
            // "found a better wifi:"
            // + bestAccessPoint
            // .getScanResult().SSID);
            // notifyedAccessPoint = bestAccessPoint;
            // }
            //
            // }
            // } else// 当前没有链接
            // {
            // if (notifyedAccessPoint == null
            // || !notifyedAccessPoint.equals(bestAccessPoint)) {
            // // 信号差是否在20以上
            // if ((bestAccessPoint.getScanResult().level - notifyedAccessPoint
            // .getScanResult().level) > 20) {
            // new HWFNotification().notifyMessage(
            // Gl.Ct(),
            // "hiwifi tips",
            // "found a avaiable wifi:"
            // + bestAccessPoint
            // .getScanResult().SSID);
            // notifyedAccessPoint = bestAccessPoint;
            // }
            //
            // }
            //
            // }
            // } catch (Exception e) {
            // e.printStackTrace();
            // }
            //
            // }
        }
    }

    class SupStatusOnChangeRunnable implements Runnable {
        SupplicantState state;
        int error;

        public void start() {
            mHandler.post(this);
        }

        public SupStatusOnChangeRunnable(SupplicantState newState, int error) {
            this.state = newState;
            this.error = error;
        }

        @Override
        public void run() {
            switch (this.state) {
                case DISCONNECTED:
                case DORMANT:
                case UNINITIALIZED:
                case INVALID:
                case INACTIVE:
                    if (WifiAdmin.sharedInstance().getWifiManager().isWifiEnabled()) {
                        notificationUtil.showNoWifiConnected();
                    } else {
                        notificationUtil.showOpenWifiHelper();
                    }
                    break;
                case SCANNING:
                    break;
                case ASSOCIATING:
                case ASSOCIATED:
                case FOUR_WAY_HANDSHAKE:
                case GROUP_HANDSHAKE:
                case COMPLETED:
                    break;
                default:
                    break;
            }

        }

    }

    class WifiConnectOnChangeRunnable implements Runnable {
        private Boolean shouldStart = false;
        private NetworkInfo mNetworkInfo;
        private String mBSSID;

        public void start() {
            if (shouldStart) {
                mHandler.post(this);
            }
        }

        public WifiConnectOnChangeRunnable(NetworkInfo networkInfo,
                                           String BSSID, WifiInfo wifiInfo) {
            shouldStart = true;
            mNetworkInfo = networkInfo;
            mBSSID = BSSID;
        }

        @Override
        public void run() {
            AccessPoint accessPoint = WifiAdmin.sharedInstance()
                    .connectedAccessPoint();
            if (accessPoint != null) {
                if (!TextUtils.isEmpty(accessPoint.getPrintableSsid())) {
                    notificationUtil.showConnectedWifi(accessPoint
                            .getPrintableSsid());
                    startPingPageService();
                }
                accessPoint.getDataModel().useTimes += 1;

                if (accessPoint.needPassword()) {
                    accessPoint.resetConfigFlag();
                    if (accessPoint.isConfiged()
                            && !accessPoint.hasRemotePassword()) {
                        if (accessPoint.isDotxType()) {
                            String username = "";
                            String pasString = "";
                            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR2) {
                                username = accessPoint.enterConfigUsername();
                                pasString = accessPoint.enterConfigPassword();
                            }
                            if (!TextUtils.isEmpty(username) && !TextUtils.isEmpty(pasString)) {
                                accessPoint.getDataModel().setUserCount(username, pasString, false, AccessPointModel.PasswordSource.PasswordSourceLocal);
                            }
                        } else {
                            String password = accessPoint.configedPassword();
                            if (!TextUtils.isEmpty(password)
                                    && !password.equals("*")) {
                                accessPoint.getDataModel().setUserCount("", password, false, AccessPointModel.PasswordSource.PasswordSourceLocal);
                            }
                        }

                    }
                }
                accessPoint.getDataModel().sync();
            } else {
                if (WifiAdmin.sharedInstance().getWifiManager().isWifiEnabled()) {
                    notificationUtil.showNoWifiConnected();
                } else {
                    notificationUtil.showOpenWifiHelper();
                }
            }
        }
    }


    @Override
    public void onDestroy() {
        super.onDestroy();
        unregisterReceiver(screenBroadcastReceiver);
        stopForeground(false);
        execCommand(getApplicationContext(), actionType_init);
    }

    public class IdleTask implements android.os.MessageQueue.IdleHandler {
        private Boolean isScreenLocked = false;
        private DaemonTaskRunner taskRunner;

        public synchronized final Boolean getIsScreenLock() {
            return isScreenLocked;
        }

        public synchronized final void setIsScreenLock(Boolean isScreenLocked) {
            this.isScreenLocked = isScreenLocked;
            if (!isScreenLocked) {
                cancelTask();
            }
        }

        public IdleTask() {
            super();
            initTaskList();
        }

        private Boolean isNetOk() {
            return WifiAdmin.sharedInstance().isConnected()
                    && NetWorkConnectivity.isWifi(getApplicationContext());
        }

        @Override
        public boolean queueIdle() {
            // 有网并且锁屏才执行后台任务
            if (isNetOk() && getIsScreenLock()) {
                execTask();
            }
            mHandler.postDelayed(new Runnable() {
                @Override
                public void run() {
                    Looper.myQueue().addIdleHandler(IdleTask.this);
                }
            }, PERIODTIME);
            return false;
        }

        public void initTaskList() {
            taskRunner = new DaemonTaskRunner();
            taskRunner.setSleeptime(0);
        }

        private void execTask() {
            if (!Gl.TaskRecord.isTaskExcutedToday(UpdateConfRunnable.class)) {
                taskRunner.addTask(new UpdateConfRunnable());
            }

            if (!Gl.TaskRecord
                    .isTaskExcutedToday(BlacklistGetDaemonTaskRunner.class)) {
                taskRunner.addTask(new BlacklistGetDaemonTaskRunner());
            }

            if (!Gl.TaskRecord.isTaskExcutedToday(SaveApListRunnable.class)) {
                taskRunner.addTask(new SaveApListRunnable());
            }

            if (!Gl.TaskRecord.isTaskExcutedToday(SaveCmccLogRunnable.class)) {
                taskRunner.addTask(new SaveCmccLogRunnable());
            }

            if (!Gl.TaskRecord.isTaskExcutedToday(WifiConfigToDBRunnable.class)) {
                taskRunner.addTask(new WifiConfigToDBRunnable());
            }
            if (!Gl.TaskRecord.isTaskExcutedToday(BackupRunnable.class)) {
                taskRunner.addTask(new BackupRunnable());
            }

            if (!Gl.TaskRecord
                    .isTaskExcutedToday(InsertRecentApplicationTask.class)) {
                taskRunner.addTask(new InsertRecentApplicationTask());
            }

        }

        private void cancelTask() {
            try {
                if (taskRunner != null && taskRunner.isRunFlag()) {
                    taskRunner.waitThread();
                }
                taskRunner.removeAllTask();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

    }

    /**
     * 监听屏幕锁开屏状态
     */
    private class ScreenBroadcastReceiver extends BroadcastReceiver {
        private String action = null;

        @Override
        public void onReceive(Context context, Intent intent) {
            action = intent.getAction();
            if (Intent.ACTION_SCREEN_ON.equals(action)) {
                // 开屏
                mIdleTask.setIsScreenLock(false);
            } else if (Intent.ACTION_SCREEN_OFF.equals(action)) {
                // 锁屏
                mIdleTask.setIsScreenLock(true);

            } else if (Intent.ACTION_USER_PRESENT.equals(action)) {
                mIdleTask.setIsScreenLock(false);
            }
        }
    }


}
