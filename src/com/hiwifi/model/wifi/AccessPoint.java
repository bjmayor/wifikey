package com.hiwifi.model.wifi;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Observable;
import java.util.Observer;

import android.annotation.SuppressLint;
import android.annotation.TargetApi;
import android.graphics.drawable.Drawable;
import android.net.NetworkInfo.DetailedState;
import android.net.wifi.ScanResult;
import android.net.wifi.WifiConfiguration;
import android.net.wifi.WifiConfiguration.Status;
import android.net.wifi.WifiEnterpriseConfig;
import android.net.wifi.WifiInfo;
import android.os.Build;
import android.os.Handler;
import android.os.Parcel;
import android.os.Parcelable;
import android.text.TextUtils;

import com.hiwifi.app.utils.RegUtil;
import com.hiwifi.hiwifi.Gl;
import com.hiwifi.hiwifi.R;
import com.hiwifi.model.log.HWFLog;
import com.hiwifi.model.log.LogUtil;
import com.hiwifi.model.speedtest.WebPageTester;
import com.hiwifi.model.speedtest.WebPageTester.WebpageTestAction;
import com.hiwifi.model.wifi.adapter.CMCCConnectAdapter;
import com.hiwifi.store.AccessPointDbMgr;
import com.hiwifi.store.AccessPointModel;
import com.hiwifi.store.AccessPointModel.PasswordSource;
import com.hiwifi.store.AccessPointModel.PasswordStatus;
import com.hiwifi.utils.FileUtil;

@TargetApi(Build.VERSION_CODES.JELLY_BEAN_MR2)
public class AccessPoint extends Observable implements Parcelable, Observer {
	private static final String TAG = "AccessPoint";
	private ScanResult mScanResult;
	private APType type = APType.APTypeNormal;
	private SECURITY_TYPE securityType;
	private int networkId;
	private Boolean hasStored = false;
	private Boolean isConfiged = false;
	private Boolean isConfigedPasswordCorrect = false;
	private Boolean isConfigedInited = false;
	private Boolean hasWebTested = false;
	private WebPageTester mWebpageTester = null;
	private Handler mHandler = null;
	private WifiConnectState connectState = WifiConnectState.connectState_unknown;
	private WiFiChangeState changeState = WiFiChangeState.connectState_unknown;
	private WifiConfiguration mWifiConfiguration = null;
	private long netTime = Long.MAX_VALUE;
	private long speed;

	private List<String> groupedBssidList;

	// 由于相同ssid组合起来了。
	public synchronized List<String> getGroupedBssid() {
		if (groupedBssidList == null) {
			groupedBssidList = new ArrayList<String>();
			groupedBssidList.add(this.getScanResult().BSSID);
		}
		return groupedBssidList;
	}

	public synchronized void addBssid(String BSSID) {
		if (BSSID != null && !getGroupedBssid().contains(BSSID)) {
			getGroupedBssid().add(BSSID);
		}
	}

	public synchronized void setSpeed(long speed) {
		this.speed = speed;
	}

	public synchronized long getSpeed() {
		return speed;
	}

	public synchronized final WifiConfiguration getWifiConfiguration() {
		return mWifiConfiguration;
	}

	public synchronized final long getNetTime() {
		return netTime;
	}

	@Override
	public synchronized void writeToParcel(Parcel dest, int flags) {
		dest.writeParcelable(mScanResult, PARCELABLE_WRITE_RETURN_VALUE);
		dest.writeValue(type);
		dest.writeValue(securityType);
		dest.writeInt(networkId);
		dest.writeValue(connectState);
	}

	private static final int maxPoolSize = 1000;
	private static Map<String, ReuseAccessPoint> mapPoolMap = new HashMap<String, AccessPoint.ReuseAccessPoint>();

	public synchronized final ScanResult getScanResult() {
		return mScanResult;
	}

	public synchronized final void setScanResult(ScanResult scanResult) {
		groupedBssidList = null;
		resetCachedFlags();
		initByScanresult(scanResult);
	}

	public synchronized final void resetCachedFlags() {
		isConfigedInited = false;
		lockDrawableInited = false;
	}

	@Override
	public int describeContents() {
		return 0;
	}

	/*
	 * API解释: Interface that must be implemented and provided as a public
	 * CREATOR field that generates instances of your Parcelable class from a
	 * Parcel.
	 * 
	 * 实际上用来对象的反序列化
	 */
	public static final Parcelable.Creator<AccessPoint> CREATOR = new Parcelable.Creator<AccessPoint>() {

		@Override
		public AccessPoint createFromParcel(Parcel source) {
			AccessPoint accessPoint = new AccessPoint(
					(ScanResult) source.readParcelable(ScanResult.class
							.getClassLoader()));
			accessPoint.type = (APType) source.readValue(APType.class
					.getClassLoader());
			accessPoint.securityType = (SECURITY_TYPE) source
					.readValue(SECURITY_TYPE.class.getClassLoader());
			accessPoint.networkId = source.readInt();
			accessPoint.setConnectState((WifiConnectState) source
					.readValue(WifiConnectState.class.getClassLoader()));
			return accessPoint;
		}

		@Override
		public AccessPoint[] newArray(int size) {
			return new AccessPoint[size];
		}
	};

	public synchronized void startWebTest(long timeout,
			final WebpageTestListener listener) {
		mHandler = new Handler();
		mWebpageTester = new WebPageTester(new WebpageTestAction() {

			@Override
			public void webpage_finish_download(final long time) {
				// hasWebTested = true;
				CMCCConnectAdapter.setLoginStatus(true);
				netTime = time;
				setChangeState(WiFiChangeState.connectState_netok);
				getDataModel().setIsPortal(false);
				mHandler.post(new Runnable() {
					@Override
					public void run() {
						listener.onTestFinished(time);
					}
				});
			}

			@Override
			public void webpage_error_download(final int errorCode,
					final String message) {
				// hasWebTested = false;
				if(errorCode == WebPageTester.ErrorCodeCaptured){
					getDataModel().setIsPortal(true);
				}else {
					getDataModel().setIsPortal(false);
				}
				mHandler.post(new Runnable() {
					@Override
					public void run() {
						listener.onTestFailed(errorCode, message);
					}
				});
			}
		});
		mWebpageTester.start_test(timeout);
	}

	public void stopWebTest(){
		if(mWebpageTester!=null){
			mWebpageTester.stop_test();
		}
	}
	public synchronized final Boolean hasWebTested() {
		return hasWebTested;
	}

	public synchronized Boolean isNeedAuth() {
		return connectState == WifiConnectState.connectState_portal
				|| changeState == WiFiChangeState.connectState_needauth
				|| changeState == WiFiChangeState.connectState_net_exception
				|| isSpecialNet();
	}

	public synchronized final void setHasWebTested(Boolean hasWebTested) {
		this.hasWebTested = hasWebTested;
	}

	public interface WebpageTestListener {
		public void onTestFinished(long usetime);

		public void onTestFailed(int code, String reason);
	}

	public synchronized final Boolean getHasStored() {
		return hasStored;
	}

	public synchronized final void setHasStored(Boolean hasStored) {
		this.hasStored = hasStored;
	}

	public static class ReuseAccessPoint {
		private AccessPoint accessPoint;
		public long useTime;

		public ReuseAccessPoint(AccessPoint accessPoint) {
			this.accessPoint = accessPoint;
			this.useTime = System.currentTimeMillis();
		}

		public AccessPoint getAccessPoint() {
			this.useTime = System.currentTimeMillis();
			return accessPoint;
		}

	}

	public static enum AvaiableLevel {
		AvaiableLevel_unkown, AvaiableLevel_well, AvaiableLevel_bad, AvaiableLevel_normal, AvaiableLevel_detecting, AvaiableLevel_unavaiable

	}

	public static enum WifiConnectState {
		connectState_unknown/*
							 * , connectState_connecting, connectState_needauth,
							 * connectState_net_exception,
							 */
		/* connectState_netok, connectState_connected */, connectState_canconnect, connectState_needpassword,
		/* connectState_connectfailedPasswordError, */connectState_needUserCount, connectState_local_restore, connectState_unlock, connectState_chinaUnicom, connectState_chinaNet, connectState_chinaCmcc, connectState_chinaCmcc_edu, connectState_chinaCmcc_auto, connectState_portal
	}

	public static enum WiFiChangeState {
		connectState_unknown, connectState_connecting, connectState_needauth, connectState_net_exception, connectState_netok, connectState_connected, connectState_connectfailedPasswordError, connectState_connectNoConnect

	}

	public synchronized final int getNetworkId() {
		return networkId;
	}

	public synchronized final void setNetworkId(int networkId) {
		this.networkId = networkId;
	}

	// ui surport
	private Boolean isExpanded = false;

	// sort surport 1.影响排序，降低排序权值。2.是从configNetwork中移除，不自动连网。
	private Boolean isIgnored = false;
	private int priority = 5;// 优先级0~10, 默认5

	private AccessPointModel dataModel = null;
	private Boolean isConnected = false;

	public static enum APType {
		APTypeNormal, APTypeCmcc, APTypeCmccEdu, APTypeCmccAuto, APTypeHiwifiOpen, APTypeHiwifiCommerce, APTypeChinaUnicom, APTyoeChinaNet
	}

	// CMCC - 中国移动
	// CMCC-AUTO - 中国移动
	// CMCC-EDU - 中国移动
	// ChinaUnicom - 中国联通
	// ChinaNet - 中国电信
	public static enum APTypeString {
		T_APTypeCmcc("CMCC"), T_APTypeCmccEdu("CMCC-EDU"), T_APTypeCmccAuto(
				"CMCC-AUTO"), T_APTypeChinaUnicom("ChinaUnicom"), T_APTyoeChinaNet(
				"ChinaNet");
		String t;

		private APTypeString(String t) {
			this.t = t;
		}

		public String getValue() {
			return this.t;
		}
	}

	public static enum SECURITY_TYPE {
		SECURITY_NONE, SECURITY_WEP, SECURITY_PSK, SECURITY_EAP
	}

	public synchronized Boolean isExpanded() {
		return isExpanded;
	}

	public synchronized Boolean isConnected() {
		return isConnected;
	}

	// public Boolean isStateConnected() {
	// return this.connectState == WifiConnectState.connectState_connected
	// || this.connectState == WifiConnectState.connectState_needauth
	// || this.connectState == WifiConnectState.connectState_netok
	// || this.connectState == WifiConnectState.connectState_net_exception;
	// }

	public Boolean isStateConnected() {
		return this.changeState == WiFiChangeState.connectState_connected
				|| this.changeState == WiFiChangeState.connectState_needauth
				|| this.changeState == WiFiChangeState.connectState_netok
				|| this.changeState == WiFiChangeState.connectState_net_exception;
	}

	public synchronized void setIsConnected(Boolean isConnected) {
		this.isConnected = isConnected;
	}

	public synchronized void setIsExpanded(Boolean isExpanded) {
		this.isExpanded = isExpanded;
	}

	public synchronized final Boolean IsIgnored() {
		return isIgnored;
	}

	// 从系统配置里忽略掉
	public synchronized void ignore() {
		setIsIgnored(true);
		if (this.getWifiConfiguration() != null) {
			WifiAdmin.sharedInstance().getWifiManager()
					.removeNetwork(this.getWifiConfiguration().networkId);
			WifiAdmin.sharedInstance().saveConf();
		}
	}

	public synchronized final void setIsIgnored(Boolean isIgnored) {
		this.isIgnored = isIgnored;
		if (isIgnored) {
			setPriority(getPriority() - 5);
		}
	}

	public synchronized final int getPriority() {
		return priority;
	}

	public synchronized final void setPriority(int priority) {
		if (priority < 0) {
			priority = 0;
		}
		if (priority > 10) {
			priority = 10;
		}
		this.priority = priority;
	}

	private AccessPoint(ScanResult scanResult) {
		super();
		initByScanresult(scanResult);
	}

	private void initByScanresult(ScanResult scanResult) {
		mScanResult = scanResult;
		securityType = SECURITY_TYPE.SECURITY_NONE;
		if (mScanResult.capabilities != null) {
			if (mScanResult.capabilities.contains("PSK")) {
				securityType = SECURITY_TYPE.SECURITY_PSK;
			} else if (mScanResult.capabilities.contains("WEP")) {
				securityType = SECURITY_TYPE.SECURITY_WEP;
			} else if (mScanResult.capabilities.contains("EAP")) {
				securityType = SECURITY_TYPE.SECURITY_EAP;
			}else {
				if (getPrintableSsid().equals(APTypeString.T_APTypeCmcc.getValue())) {
					setType(APType.APTypeCmcc);
				} else if (getPrintableSsid().equals(
						APTypeString.T_APTypeCmccAuto.getValue())) {
					setType(APType.APTypeCmccAuto);
				} else if (getPrintableSsid().equals(
						APTypeString.T_APTypeCmccEdu.getValue())) {
					setType(APType.APTypeCmccEdu);
				} else if (getPrintableSsid().equals(
						APTypeString.T_APTypeChinaUnicom.getValue())) {
					setType(APType.APTypeChinaUnicom);
				} else if (getPrintableSsid().equals(
						APTypeString.T_APTyoeChinaNet.getValue())) {
					setType(APType.APTyoeChinaNet);
				}
			}
			HWFLog.e(TAG, "bssid:" + mScanResult.SSID + " securityType:"
					+ securityType);
		}

	}

	private boolean isInited = false;

	private void initStatus() {
		if (!isInited) {
			calculateWifiConnectState();
			HWFLog.e(TAG, "init called");
			isInited = true;
		}
	}

	public synchronized Boolean canAutoConnnect() {
		if (isConfiged()) {
			return isConfigedPasswordCorrect();
		} else {
			if (isDotxType()) {
				return hasUserCount() || hasRemotePassword();
			} else {
				return !needPassword() || hasPassword();
			}

		}

	}

	private static List<BlockAlg> commerceWifiList;

	@SuppressLint("DefaultLocale")
	public synchronized boolean isBlockAutoLink() {
		if (commerceWifiList == null) {
			loadBlockList();
		}
		for (BlockAlg blckAlg : commerceWifiList) {
			if (blckAlg.isBlocked(getPrintableSsid())) {
				return true;
			}
		}
		return false;
	}

	public synchronized static void loadBlockList() {
		try {
			commerceWifiList = (List<BlockAlg>) FileUtil
					.readObjectFromFile(BLACKLISTFILENAME_STRING);
		} catch (Exception e) {
			commerceWifiList = null;
			e.printStackTrace();
		}
		if (commerceWifiList == null) {
			commerceWifiList = new ArrayList<BlockAlg>();
			commerceWifiList.add(new BlockAlg(APTypeString.T_APTypeCmcc
					.getValue(), BlockAlg.ruleWhole));
			commerceWifiList.add(new BlockAlg(APTypeString.T_APTypeCmccAuto
					.getValue(), BlockAlg.ruleWhole));
			commerceWifiList.add(new BlockAlg(APTypeString.T_APTypeCmccEdu
					.getValue(), BlockAlg.ruleWhole));
			commerceWifiList.add(new BlockAlg(APTypeString.T_APTyoeChinaNet
					.getValue(), BlockAlg.ruleWhole));
			commerceWifiList.add(new BlockAlg(APTypeString.T_APTypeChinaUnicom
					.getValue(), BlockAlg.ruleWhole));
			commerceWifiList.add(new BlockAlg("16wifi", BlockAlg.ruleWhole));
			commerceWifiList.add(new BlockAlg("Print", BlockAlg.ruleBlur));
		}
	}

	private static final String BLACKLISTFILENAME_STRING = "blacklist";

	public synchronized static void saveList(List<BlockAlg> blackList) {
		if (blackList == null) {
			return;
		}
		try {
			FileUtil.saveObject2File(BLACKLISTFILENAME_STRING, blackList);
		} catch (Exception e) {
			e.printStackTrace();
		}

	}

	public Boolean supportAutoAuth() {
		// 暂时停用cmcc等的自动认证
		return false;
		// return getPrintableSsid().equalsIgnoreCase("CMCC")
		// || getPrintableSsid().equalsIgnoreCase("ChinaNet");
	}

	public synchronized String getPrintableSsid() {
		if (getScanResult().SSID == null)
			return "";
		final int length = getScanResult().SSID.length();
		if (length > 2 && (getScanResult().SSID.charAt(0) == '"')
				&& getScanResult().SSID.charAt(length - 1) == '"') {
			return getScanResult().SSID.substring(1, length - 1);
		}
		return getScanResult().SSID;
	}

	public Boolean isChinaUnicom() {
		return getPrintableSsid().equalsIgnoreCase("chinaunicom");
	}

	public Boolean isChinaNet() {
		return getPrintableSsid().equalsIgnoreCase("chinaNet");
	}

	public synchronized static AccessPoint instanceByScanResult(
			ScanResult scanResult) {
		ReuseAccessPoint resuAccessPoint = mapPoolMap.get(scanResult.BSSID);
		if (resuAccessPoint != null) {
			AccessPoint accessPoint = resuAccessPoint.getAccessPoint();
			accessPoint.setScanResult(scanResult);
			accessPoint.calculateWifiConnectState();
			return accessPoint;
		} else {
			AccessPoint accessPoint = new AccessPoint(scanResult);
			putToMapPool(accessPoint);
			return accessPoint;
		}
	}

	public synchronized static AccessPoint findByBSSID(String BSSID) {
		if (mapPoolMap.get(BSSID) != null) {
			AccessPoint accessPoint = mapPoolMap.get(BSSID).getAccessPoint();
			return accessPoint;
		}
		return null;
	}

	public synchronized static void clearCache() {
		mapPoolMap.clear();
	}

	private synchronized static void putToMapPool(AccessPoint accessPoint) {
		synchronized (mapPoolMap) {
			if (mapPoolMap.size() == maxPoolSize) {
				// 移除一小时前的缓存数据
				Iterator<String> iterator = mapPoolMap.keySet().iterator();
				while (iterator.hasNext()) {
					String key = (String) iterator.next();
					ReuseAccessPoint apAccessPoint = mapPoolMap.get(key);
					if (System.currentTimeMillis() - apAccessPoint.useTime > 3600 * 1000) {
						mapPoolMap.remove(apAccessPoint);
					}
				}
			}
			mapPoolMap.put(accessPoint.getScanResult().BSSID,
					new ReuseAccessPoint(accessPoint));
		}

	}

	public synchronized void resetConfigFlag() {
		this.isConfigedInited = false;
	}

	public synchronized boolean needPassword() {
		return securityType != SECURITY_TYPE.SECURITY_NONE;
	}

	public synchronized boolean isDotxType() {
		return securityType == SECURITY_TYPE.SECURITY_EAP;
	}

	public synchronized Boolean isConfiged() {
		if (isConfigedInited) {
			return isConfiged;
		} else {
			isConfigedInited = true;
			mWifiConfiguration = WifiAdmin.sharedInstance().exsitedWifiConf(
					getPrintableSsid());
			// 为三星加强判断。 TODO 判断系统用户名、密码
			isConfiged = mWifiConfiguration != null
					&& (!TextUtils.isEmpty(configedPassword()));
			if (getScanResult().frequency > 2500) {
				is5g = true;
			}
			if (isConfiged) {
				isConfigedPasswordCorrect = disableReason() != 3;
			}
			return isConfiged;
		}
	}

	private Boolean is5g = false;

	public synchronized Boolean is5g() {
		return is5g;
	}

	public synchronized String configedPassword() {
		if (mWifiConfiguration == null) {
			return null;
		} else {
			if (securityType == SECURITY_TYPE.SECURITY_PSK) {
				LogUtil.d("Tag:", getPrintableSsid() + "  pre:"
						+ mWifiConfiguration.preSharedKey);
				return mWifiConfiguration.preSharedKey;
			} else if (securityType == SECURITY_TYPE.SECURITY_WEP) {
				return mWifiConfiguration.wepKeys[0];
			} else if (securityType == SECURITY_TYPE.SECURITY_EAP) {
				if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR2) {
					LogUtil.d("Tag:", "init:" + Build.VERSION.SDK_INT + "  "
							+ Build.VERSION_CODES.JELLY_BEAN_MR2);
					WifiEnterpriseConfig config = mWifiConfiguration.enterpriseConfig;
					if (config != null) {
						return config.getPassword();
					}
					return null;
				} else {
					return null;
				}
			} else {
				return null;
			}
		}
	}

	public synchronized String enterConfigUsername() {
		if (mWifiConfiguration == null) {
			return null;
		} else {
			if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR2) {
				WifiEnterpriseConfig config = mWifiConfiguration.enterpriseConfig;
				if (config != null) {
					return config.getIdentity();
				}
				return null;
			}
			return null;
		}
	}

	public synchronized String enterConfigPassword() {
		if (mWifiConfiguration == null) {
			return null;
		} else {
			if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR2) {
				WifiEnterpriseConfig config = mWifiConfiguration.enterpriseConfig;
				if (config != null) {
					return config.getPassword();
				}
				return null;
			}
			return null;
		}
	}

	// 通过反射的可能不可靠
	public synchronized int disableReason() {
		WifiConfiguration configuration = this.mWifiConfiguration;
		try {
			Class localClass = configuration.getClass();
			Field localField = localClass.getField("disableReason");
			if (localField == null) {
				return 0;
			}
			Object val = localField.getInt(configuration);
			return Integer.parseInt(val.toString());
		} catch (Exception e) {
			e.printStackTrace();
			return 0;
		}
	}

	public synchronized void saveConfig() {
		WifiAdmin.sharedInstance().saveConf();
		isConfigedInited = true;
		isConfiged = true;
	}

	public synchronized boolean hasRemotePassword() {
		if (isDotxType()) {
			return !TextUtils.isEmpty(getDataModel().getPassword(false))
					&& !TextUtils.isEmpty(getDataModel().getUserName())
					&& getDataModel().passwordType != PasswordSource.PasswordSourceLocal
							.ordinal() /* && !isStoredPasswordInValid() */;
		} else {
			return !TextUtils.isEmpty(getDataModel().getPassword(false))
					&& getDataModel().passwordType != PasswordSource.PasswordSourceLocal
							.ordinal() /* && !isStoredPasswordInValid() */;
		}
	}

	public synchronized Boolean hasPassword() {
		return !TextUtils.isEmpty(getDataModel().getPassword(false))
				&& !isStoredPasswordInValid();
	}

	public Boolean isCanShowPassWord() {
		return isConnected() && needPassword();
	}

	public Boolean isShowPortal() {
		return isNeedAuth();
	}

	public Boolean isShowNetException() {
		return changeState == WiFiChangeState.connectState_net_exception;
	}

	public synchronized Boolean hasUserCount() {
		return !TextUtils.isEmpty(getDataModel().getUserName())
				&& !TextUtils.isEmpty(getDataModel().getPassword(false))
				&& getDataModel().passwordType == PasswordSource.PasswordSourceLocal
						.ordinal() && !isStoredPasswordInValid();
	}

	public Boolean isStoredPasswordInValid() {
		return getDataModel().passwordStatus == PasswordStatus.PasswordStatusInvalid
				.ordinal();
	}

	public synchronized Drawable getSignalDrawable() {
		initStatus();
		int drawable = R.drawable.wifi_signal_open;
		Drawable signalDrawable = Gl.Ct().getResources().getDrawable(drawable);
		return signalDrawable;
	}

	private Drawable lockDrawable;
	private Boolean lockDrawableInited = false;

	public synchronized Drawable getLockDrawable() {
		initStatus();
		if (lockDrawableInited) {
			return lockDrawable;
		}
		int drawable = R.drawable.list_lock_icon;
		if (needPassword()) {
			if (connectState == WifiConnectState.connectState_unlock) {
				drawable = R.drawable.key_icon_blue;
			} else if (connectState == WifiConnectState.connectState_local_restore) {
				return null;
			}else{
				if (isSpecialNet()) {
					return null;
				} else {
					drawable = R.drawable.list_lock_icon;
				}
			}
			// if (hasRemotePassword() || hasUserCount()) {
			// drawable = R.drawable.key_icon_blue;
			// } else if ((isConfiged() && isConfigedPasswordCorrect())
			// || hasPassword()) {
			// // drawable = R.drawable.wifi_saved;// TODO
			// return null;
			// } else {
			// drawable = R.drawable.list_lock_icon;
			// }

		} else {
			return null;
		}
		lockDrawable = Gl.Ct().getResources().getDrawable(drawable);
		return lockDrawable;
	}

	public synchronized APType getType() {
		LogUtil.d("Tag:", "get:" + getPrintableSsid() + "  " + this.type);
		return this.type;
	}

	public void setType(APType type) {
		this.type = type;
		LogUtil.d("Tag:", "set:" + getPrintableSsid() + "  " + this.type);
	}

	public synchronized SECURITY_TYPE getSecurityType() {
		return securityType;
	}

	@Override
	public boolean equals(Object o) {
		AccessPoint anotherAccessPoint = (AccessPoint) o;
		return anotherAccessPoint.getPrintableSsid().equals(
				this.getPrintableSsid());
	}

	public synchronized final AccessPointModel getDataModel() {
		if (dataModel == null) {
			AccessPointDbMgr mgr = AccessPointDbMgr.shareInstance();
			dataModel = mgr.getAccessPointByBSSID(getScanResult().BSSID);

			if (dataModel == null) {
				mgr.insert(new AccessPointModel(getScanResult()));
				dataModel = mgr.getAccessPointByBSSID(getScanResult().BSSID);
				dataModel.addObserver(this);
			} else {
				dataModel.addObserver(this);
			}
		}
		hasStored = true;
		return dataModel;
	}

	public synchronized final void setDataModel(AccessPointModel dataModel) {
		this.dataModel = dataModel;
	}

	public synchronized final void calculateWifiConnectState() {
		if (isConfiged()) { // 系统配置
			if (isConfigedPasswordCorrect()) { // 配置正确
				if (needPassword()) {
					if (hasRemotePassword()) {
						setConnectState(WifiConnectState.connectState_unlock);
					} else {
						setConnectState(WifiConnectState.connectState_local_restore);
						LogUtil.d("Tag:", "系统" + getPrintableSsid());
					}
				} else {
					if (isSpecialNet()) {
						portalPageStatus();
					} else {
						setConnectState(WifiConnectState.connectState_canconnect);
					}
				}
			} else {
				if (isDotxType()) {
					setConnectState(WifiConnectState.connectState_needUserCount);
				} else {
					setConnectState(WifiConnectState.connectState_needpassword);
				}
			}
		} else {
			if (isSpecialNet()) {
				LogUtil.d("Tag:", "特殊网络：" + getPrintableSsid());
				portalPageStatus();
			} else if (isDotxType()) {
				if (hasRemotePassword()) {
					setConnectState(WifiConnectState.connectState_unlock);
				} else if (hasUserCount()) {
					setConnectState(WifiConnectState.connectState_local_restore);
				} else {
					setConnectState(WifiConnectState.connectState_needUserCount);
				}
			} else if (hasRemotePassword()) {
				setConnectState(WifiConnectState.connectState_unlock);
			} else if (getDataModel().isPortal()) {
				setConnectState(WifiConnectState.connectState_portal);
			} else {
				if (needPassword()) {
					if (hasPassword()) {
						setConnectState(WifiConnectState.connectState_local_restore);
						LogUtil.d("Tag:", "非系统" + getPrintableSsid());
					} else {
						setConnectState(WifiConnectState.connectState_needpassword);
					}
				} else {
					setConnectState(WifiConnectState.connectState_canconnect);
				}
			}
		}
	}

	public Boolean isFree() {
		return (this.connectState == WifiConnectState.connectState_canconnect
				|| this.connectState == WifiConnectState.connectState_local_restore || this.connectState == WifiConnectState.connectState_unlock)
				&& !isSpecialNet();
	}

	public Boolean isSpecialNet() {
		return this.type == APType.APTyoeChinaNet
				|| this.type == APType.APTypeChinaUnicom
				|| this.type == APType.APTypeCmcc
				|| this.type == APType.APTypeCmccAuto
				|| this.type == APType.APTypeCmccEdu;
	}

	public Boolean hasLocalOrRemotePassword() {
		return hasPassword() || hasRemotePassword() || hasUserCount();
	}

	public void portalPageStatus() {
		if (this.type == APType.APTypeChinaUnicom) {
			setConnectState(WifiConnectState.connectState_chinaUnicom);
		} else if (this.type == APType.APTyoeChinaNet) {
			setConnectState(WifiConnectState.connectState_chinaNet);
		} else if (this.type == APType.APTypeCmcc) {
			setConnectState(WifiConnectState.connectState_chinaCmcc);
		} else if (this.type == APType.APTypeCmccAuto) {
			setConnectState(WifiConnectState.connectState_chinaCmcc_auto);
		} else if (this.type == APType.APTypeCmccEdu) {
			setConnectState(WifiConnectState.connectState_chinaCmcc_edu);
		}
	}

	public synchronized final WifiConnectState getConnectState() {
		initStatus();
		return connectState;
	}

	public synchronized final WiFiChangeState getChangeState() {
		return changeState;
	}

	private String connectStateString;
	private int connectStateColor;
	private Drawable connectStateDrawable;

	private String changeStateString;
	private Drawable changeStateDrawable;

	public synchronized final void setChangeState(WiFiChangeState changeState) {
		this.changeState = changeState;
		switch (this.changeState) {
		case connectState_connected:
			changeStateString = Gl.Ct().getString(R.string.wifi_status_success);
			changeStateDrawable = connectStateDrawable = Gl.Ct().getResources()
					.getDrawable(R.drawable.joined_icon);
			isConnected = false;
			if (connectState == WifiConnectState.connectState_needUserCount
					|| connectState == WifiConnectState.connectState_needpassword) {
				connectState = WifiConnectState.connectState_local_restore;
			}
			break;
		case connectState_netok:
			changeStateString = Gl.Ct().getString(R.string.wifi_status_success);
			changeStateDrawable = connectStateDrawable = Gl.Ct().getResources()
					.getDrawable(R.drawable.joined_icon);
			isConnected = true;
			break;
		case connectState_connecting:
			if (connectState == WifiConnectState.connectState_unlock) {
				changeStateString = Gl.Ct().getString(
						R.string.wifi_status_pull_password);
			} else {
				changeStateString = Gl.Ct().getString(
						R.string.wifi_status_connectting);
			}
			isConnected = false;
			// connectStateDrawable = Gl.Ct().getResources()
			// .getDrawable(R.drawable.joined_icon);
			changeStateDrawable = null;
			break;
		case connectState_connectfailedPasswordError:
			if (connectState == WifiConnectState.connectState_unlock) {
				changeStateString = Gl.Ct().getString(
						R.string.wifi_status_failure_unlock);
				if (isDotxType()) {
					connectState = WifiConnectState.connectState_needUserCount;
				} else {
					connectState = WifiConnectState.connectState_needpassword;
				}
			} else {
				changeStateString = Gl.Ct().getString(
						R.string.wifi_status_failure);
			}
			changeStateDrawable = connectStateDrawable = Gl.Ct().getResources()
					.getDrawable(R.drawable.join_error_icon);
			isConnected = false;
			break;
		case connectState_needauth:
			changeStateString = Gl.Ct().getString(
					R.string.wifi_statue_success_portal);
			changeStateDrawable = connectStateDrawable = Gl.Ct().getResources()
					.getDrawable(R.drawable.mark_icon);
			isConnected = true;
			break;
		case connectState_net_exception:
			if (connectState == WifiConnectState.connectState_unlock) {
				changeStateString = Gl.Ct().getString(
						R.string.wifi_status_failure_unlock);
			} else {
				changeStateString = Gl.Ct().getString(
						R.string.wifi_status_failure);
			}
			changeStateDrawable = connectStateDrawable = Gl.Ct().getResources()
					.getDrawable(R.drawable.join_error_icon);
			isConnected = true;
			break;
		case connectState_connectNoConnect:
			changeStateString = Gl.Ct().getString(
					R.string.wifi_status_no_connect);
			changeStateDrawable = null;
			isConnected = false;
			break;
		}
	}

	public synchronized final void setConnectState(WifiConnectState connectState) {
		this.connectState = connectState;
		switch (this.connectState) {
		case connectState_local_restore:
			connectStateString = Gl.Ct().getString(
					R.string.wifi_state_localrestore);
			connectStateColor = Gl.Ct().getResources()
					.getColor(R.color.wifilist_itembg_detecting);
			connectStateDrawable = Gl.Ct().getResources()
					.getDrawable(R.drawable.wifi_connected);
			break;
		case connectState_chinaNet: // 中国电信
			connectStateString = Gl.Ct()
					.getString(R.string.wifi_state_chinaNet);
			connectStateColor = Gl.Ct().getResources()
					.getColor(R.color.wifilist_itembg_detecting);
			connectStateDrawable = Gl.Ct().getResources()
					.getDrawable(R.drawable.shape_bg_transparent);
			break;
		case connectState_chinaUnicom:// 中国联通
			connectStateString = Gl.Ct().getString(
					R.string.wifi_state_chinaUnicom);
			connectStateColor = Gl.Ct().getResources()
					.getColor(R.color.wifilist_itembg_detecting);
			connectStateDrawable = Gl.Ct().getResources()
					.getDrawable(R.drawable.shape_bg_transparent);
			break;
		case connectState_chinaCmcc:
			connectStateString = Gl.Ct().getString(
					R.string.wifi_state_chinaCmcc);
			connectStateColor = Gl.Ct().getResources()
					.getColor(R.color.wifilist_itembg_detecting);
			connectStateDrawable = Gl.Ct().getResources()
					.getDrawable(R.drawable.shape_bg_transparent);
			break;
		case connectState_chinaCmcc_edu:
			connectStateString = Gl.Ct().getString(
					R.string.wifi_state_chinaCmcc_edu);
			connectStateColor = Gl.Ct().getResources()
					.getColor(R.color.wifilist_itembg_detecting);
			connectStateDrawable = Gl.Ct().getResources()
					.getDrawable(R.drawable.shape_bg_transparent);
			break;
		case connectState_chinaCmcc_auto:
			connectStateString = Gl.Ct().getString(
					R.string.wifi_state_chinaCmcc_auto);
			connectStateColor = Gl.Ct().getResources()
					.getColor(R.color.wifilist_itembg_detecting);
			connectStateDrawable = Gl.Ct().getResources()
					.getDrawable(R.drawable.shape_bg_transparent);
			break;
		case connectState_portal: // chinaUnicome、chinanet、portal
			connectStateString = Gl.Ct().getString(R.string.wifi_state_portal);
			connectStateColor = Gl.Ct().getResources()
					.getColor(R.color.wifilist_itembg_detecting);
			connectStateDrawable = Gl.Ct().getResources()
					.getDrawable(R.drawable.shape_bg_transparent);
			break;
		case connectState_unlock:
			connectStateString = Gl.Ct().getString(
					R.string.wifi_state_hasremotepass_unlock);
			connectStateColor = Gl.Ct().getResources()
					.getColor(R.color.wifilist_itembg_detecting);
			connectStateDrawable = Gl.Ct().getResources()
					.getDrawable(R.drawable.wifi_connected);
			break;
		/*
		 * case connectState_connected: connectStateString = Gl.Ct().getString(
		 * R.string.wifi_state_connected); connectStateColor =
		 * Gl.Ct().getResources() .getColor(R.color.wifilist_emphasize_color);
		 * connectStateDrawable = Gl.Ct().getResources()
		 * .getDrawable(R.drawable.wifi_connected); break; case
		 * connectState_netok: connectStateString =
		 * Gl.Ct().getString(R.string.wifi_state_netok); connectStateColor =
		 * Gl.Ct().getResources() .getColor(R.color.wifilist_emphasize_color);
		 * connectStateDrawable = Gl.Ct().getResources()
		 * .getDrawable(R.drawable.wifi_connected); break;
		 */
		case connectState_needpassword:
			connectStateString = Gl.Ct().getString(
					R.string.wifi_state_needpassword);
			connectStateColor = Gl.Ct().getResources()
					.getColor(R.color.wifilist_itembg_detecting);
			connectStateDrawable = Gl.Ct().getResources()
					.getDrawable(R.drawable.shape_bg_transparent);
			break;
		case connectState_needUserCount:
			connectStateString = Gl.Ct().getString(
					R.string.wifi_state_needusercount);
			connectStateColor = Gl.Ct().getResources()
					.getColor(R.color.wifilist_itembg_detecting);
			connectStateDrawable = Gl.Ct().getResources()
					.getDrawable(R.drawable.shape_bg_transparent);
			break;

		case connectState_canconnect:
			connectStateString = Gl.Ct().getString(
					R.string.wifi_state_canconnect);
			connectStateColor = Gl.Ct().getResources()
					.getColor(R.color.wifilist_itembg_detecting);
			connectStateDrawable = Gl.Ct().getResources()
					.getDrawable(R.drawable.wifi_connected);
			break;

		/*
		 * case connectState_connecting: connectStateString = Gl.Ct().getString(
		 * R.string.wifi_state_connecting); connectStateColor =
		 * Gl.Ct().getResources() .getColor(R.color.wifilist_ignore_color);
		 * connectStateDrawable = Gl.Ct().getResources()
		 * .getDrawable(R.drawable.wifi_state_loading); break; case
		 * connectState_connectfailedPasswordError: connectStateString =
		 * Gl.Ct().getString( R.string.wifi_state_passworderror);
		 * connectStateColor = Gl.Ct().getResources()
		 * .getColor(R.color.wifilist_ignore_color); connectStateDrawable =
		 * Gl.Ct().getResources() .getDrawable(R.drawable.shape_bg_transparent);
		 * break;
		 */
		case connectState_unknown:
			connectStateString = Gl.Ct().getString(R.string.wifi_state_unkown);
			connectStateColor = Gl.Ct().getResources()
					.getColor(R.color.wifilist_ignore_color);
			connectStateDrawable = Gl.Ct().getResources()
					.getDrawable(R.drawable.shape_bg_transparent);
			break;
		/*
		 * case connectState_needauth: connectStateString = Gl.Ct()
		 * .getString(R.string.wifi_state_needauth); connectStateColor =
		 * Gl.Ct().getResources() .getColor(R.color.wifilist_ignore_color);
		 * connectStateDrawable = Gl.Ct().getResources()
		 * .getDrawable(R.drawable.i_alert); break; case
		 * connectState_net_exception: connectStateString = Gl.Ct().getString(
		 * R.string.wifi_state_netexception); connectStateColor =
		 * Gl.Ct().getResources() .getColor(R.color.wifilist_ignore_color);
		 * connectStateDrawable = Gl.Ct().getResources()
		 * .getDrawable(R.drawable.wifi_net_error); break;
		 */

		default:
			break;
		}
	}

	public synchronized final String getConnectStateString() {
		initStatus();
		return connectStateString;
	}

	public String getChangeStateString() {
		return changeStateString;
	}

	public synchronized final int getConnectStateColor() {
		initStatus();
		return connectStateColor;
	}

	public synchronized final Drawable getConnectStateDrawable() {
		initStatus();
		return connectStateDrawable;
	}

	public synchronized final Drawable getChangeStateDrawable() {
		return changeStateDrawable;
	}

	public synchronized void sync() {
		getDataModel().sync();
	}

	public synchronized void onDetailedStateChanged(DetailedState state,
			List<AccessPoint> currentList) {
		switch (state) {
		case SCANNING:
		case CONNECTING:
		case AUTHENTICATING:
		case OBTAINING_IPADDR:
			setChangeState(WiFiChangeState.connectState_connecting);
			// setConnectState(WifiConnectState.connectState_connecting);
			break;
		case FAILED:
			setChangeState(WiFiChangeState.connectState_connectfailedPasswordError);
			// setConnectState(WifiConnectState.connectState_connectfailedPasswordError);
			break;
		case CONNECTED:
			setChangeState(WiFiChangeState.connectState_connected);
			// setConnectState(WifiConnectState.connectState_connected);
			break;
		case DISCONNECTED:
			setChangeState(WiFiChangeState.connectState_unknown);
			// setConnectState(WifiConnectState.connectState_unknown);
			break;

		default:
			break;
		}
		// resetOthersState(currentList);
	}

	public synchronized void resetOthersState(List<AccessPoint> currentList) {
		if (currentList != null) {
			Iterator<AccessPoint> iterator = currentList.iterator();
			while (iterator.hasNext()) {
				AccessPoint accessPoint = (AccessPoint) iterator.next();
				if (accessPoint != this) {
					switch (accessPoint.changeState) {
					case connectState_connected:
						accessPoint
								.setConnectState(WifiConnectState.connectState_canconnect);
						break;
					case connectState_connecting:
						accessPoint
								.setConnectState(WifiConnectState.connectState_unknown);
					default:
						break;
					}

				}
			}
		}
	}

	public synchronized Boolean isPasswordValid(String password) {
		switch (this.securityType) {
		case SECURITY_PSK:// 8~63
			return RegUtil.isPskPasswordValid(password);
		case SECURITY_WEP:// 5 or 13
			return RegUtil.isWepPasswordValid(password);
		case SECURITY_EAP:
			return RegUtil.isEapPasswordValid(password);
		default:
			break;
		}
		return true;
	}

	public synchronized Boolean isConfigedPasswordCorrect() {
		return isConfigedPasswordCorrect;
	}

	public synchronized void enable() {
		if (isConfiged() && isConfigedPasswordCorrect()) {
			getWifiConfiguration().status = Status.ENABLED;
		}
	}

	@TargetApi(Build.VERSION_CODES.JELLY_BEAN_MR1)
	public synchronized static String getDetailStateDescription(
			DetailedState state) {
		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
			if (state == DetailedState.VERIFYING_POOR_LINK) {
				return Gl.Ct().getString(
						R.string.wifi_detailstatus_verifying_poor_link);
			}
		}
		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR1) {
			if (state == DetailedState.CAPTIVE_PORTAL_CHECK) {
				return Gl.Ct().getString(
						R.string.wifi_detailstatus_captive_portal_check);
			}
		}
		switch (state) {
		case AUTHENTICATING:
			return Gl.Ct().getString(R.string.wifi_detailstatus_authenticating);
		case OBTAINING_IPADDR:
			return Gl.Ct().getString(
					R.string.wifi_detailstatus_obtaining_ipaddr);
		default:
			break;
		}
		return null;

	}

	public synchronized boolean isConnecting(WifiInfo wifiInfo) {
		switch (wifiInfo.getSupplicantState()) {
		case AUTHENTICATING:
		case ASSOCIATING:
		case ASSOCIATED:
		case FOUR_WAY_HANDSHAKE:
		case GROUP_HANDSHAKE:
			setChangeState(WiFiChangeState.connectState_connecting);
			// setConnectState(WifiConnectState.connectState_connecting);
			return true;
		case COMPLETED:
			// setChangeState(WiFiChangeState.connectState_connected);
			LogUtil.d("Tag:", "判断连接");
			// setConnectState(WifiConnectState.connectState_connected);
			return true;
		case DISCONNECTED:
		case INTERFACE_DISABLED:
		case INACTIVE:
		case SCANNING:
		case DORMANT:
		case UNINITIALIZED:
		case INVALID:
			return false;
		default:
			throw new IllegalArgumentException("Unknown supplicant state");
		}
	}

	@Override
	public void update(Observable observable, Object data) {
		// setConnectState(WifiConnectState.connectState_canconnect);
		// setChanged();
		// notifyObservers(this);
	}

	public int getSignalPersent() {
		// -100 -55
		// if (mScanResult != null) {
		// int signalLevel = WifiManager.calculateSignalLevel(mScanResult.level,
		// 100);
		// }
		if (mScanResult.level <= -100) {
			return 0;
		} else if (mScanResult.level >= -55) {
			return 100;
		} else {
			float inputRange = 45;
			float outputRange = 100;
			return (int) ((float) (mScanResult.level + 100) * outputRange / inputRange);
		}
	}

	public int calculateSignalLevel() {
		double signalPersent = getSignalPersent();
		return (int) Math.ceil(signalPersent / 20);
	}

	@Override
	public String toString() {
		return "{AccessPoint:{SSID:" + this.getPrintableSsid()
				+ ", changestate: " + this.changeState + "}}";
	}
}