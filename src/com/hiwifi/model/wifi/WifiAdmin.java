package com.hiwifi.model.wifi;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.BitSet;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.wifi.ScanResult;
import android.net.wifi.WifiConfiguration;
import android.net.wifi.WifiEnterpriseConfig;
import android.net.wifi.WifiConfiguration.Status;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.net.wifi.WifiManager.WifiLock;
import android.os.Build;
import android.text.TextUtils;
import android.util.Log;

import com.hiwifi.constant.ReleaseConstant;
import com.hiwifi.hiwifi.Gl;
import com.hiwifi.hiwifi.R;
import com.hiwifi.model.log.HWFLog;
import com.hiwifi.model.log.LogUtil;
import com.hiwifi.model.wifi.AccessPoint.SECURITY_TYPE;
import com.hiwifi.utils.ResUtil;

public class WifiAdmin {
	private final static String TAG = "WifiAdmin";
	private StringBuffer mStringBuffer = new StringBuffer();
	private List<AccessPoint> mListResult;// 全局使用，不让外面方法可以修改。
	private AccessPoint mAccessPoint;
	private WifiManager mWifiManager;

	private WifiInfo mWifiInfo;
	private Context mContext;
	private List<WifiConfiguration> mWifiConfiguration;
	WifiLock mWifiLock;

	private BitSet flagBitSet;
	private static final int WifiListChangeFlag = 0;
	private static final int WifiConnectChangeFlag = 1;

	private Boolean wifiConnectHasChange() {
		return flagBitSet.get(WifiConnectChangeFlag);
	}

	private Boolean wifiListHasChange() {
		return flagBitSet.get(WifiListChangeFlag);
	}

	private void resetWifiConnectFlag() {
		flagBitSet.set(WifiConnectChangeFlag, false);
	}

	private void resetWifiListFlag() {
		flagBitSet.set(WifiListChangeFlag, false);
	}

	public void onWificonnectChanged() {
		flagBitSet.set(WifiConnectChangeFlag, true);
	}

	public void onWifiListChanged() {
		flagBitSet.set(WifiListChangeFlag, true);
	}

	public void onWifiStateChanged() {
		onWificonnectChanged();
		onWifiListChanged();
	}

	private static WifiAdmin instance = null;

	private WifiAdmin(Context context) {
		mContext = context;
		mWifiManager = (WifiManager) context
				.getSystemService(Context.WIFI_SERVICE);
		mWifiInfo = mWifiManager.getConnectionInfo();
		flagBitSet = new BitSet(2);
		flagBitSet.set(WifiListChangeFlag, true);
		flagBitSet.set(WifiConnectChangeFlag, true);
	}

	public static WifiAdmin sharedInstance() {
		if (instance == null) {
			instance = new WifiAdmin(Gl.Ct());
		}
		return instance;
	}

	public WifiManager getWifiManager() {
		return mWifiManager;
	}

	public void openNetCard() {
		if (!mWifiManager.isWifiEnabled()) {
			mWifiManager.setWifiEnabled(true);
		}
	}

	public void closeNetCard() {
		if (mWifiManager.isWifiEnabled()) {
			mWifiManager.setWifiEnabled(false);
		}
	}
	
	public boolean isWifiEnable(){
		if(mWifiManager.isWifiEnabled()){
			return true;
		}
		return false;
	}

	public String checkNetCardState() {
		String statusString = "";
		switch (mWifiManager.getWifiState()) {
		case 0:
			statusString = ResUtil
					.getStringById(R.string.netcard_disconnecting);
			break;
		case 1:
			statusString = ResUtil.getStringById(R.string.netcard_disconnected);
			break;
		case 2:
			statusString = ResUtil.getStringById(R.string.netcard_connecting);
			break;
		case 3:
			statusString = ResUtil.getStringById(R.string.netcard_connected);
			break;
		default:
			statusString = ResUtil.getStringById(R.string.netcard_unkonw);
			break;
		}
		Log.i(TAG, statusString);
		return statusString;
	}

	// 这里会合并SSID
	public List<AccessPoint> getMergedAccessPoints() {
		List<AccessPoint> resultAccessPoints = new ArrayList<AccessPoint>();
		List<AccessPoint> accessPoints = getAccessPoints();
		List<String> ssidList = new ArrayList<String>();
		AccessPoint activeAccessPoint = getActiveAccessPoint();
		if (accessPoints != null) {
			Iterator<AccessPoint> iterator = accessPoints.iterator();
			while (iterator.hasNext()) {
				AccessPoint accessPoint = (AccessPoint) iterator.next();
				if (ssidList.contains(accessPoint.getPrintableSsid())) {
					continue;
				}
				if (activeAccessPoint != null
						&& activeAccessPoint.getPrintableSsid().equals(
								accessPoint.getPrintableSsid())) {
					if (activeAccessPoint == accessPoint) {
						resultAccessPoints.add(accessPoint);
						ssidList.add(accessPoint.getPrintableSsid());
					}
				} else {
					resultAccessPoints.add(accessPoint);
					ssidList.add(accessPoint.getPrintableSsid());
				}
			}
		}
		return resultAccessPoints;
	}

	// 原始列表，不合并SSID
	public List<AccessPoint> getAccessPoints() {
		if (mListResult != null && !wifiListHasChange()) {
			return getWifiManager().isWifiEnabled() ? new ArrayList<AccessPoint>(
					mListResult) : null;
		}
		List<ScanResult> listResult = mWifiManager.getScanResults();
		if (listResult == null) {
			return null;
		}

		setConfigMap();
		Iterator<ScanResult> iterator = listResult.iterator();
		mListResult = new ArrayList<AccessPoint>();
		while (iterator.hasNext()) {
			ScanResult scanResult = (ScanResult) iterator.next();
			if (TextUtils.isEmpty(scanResult.SSID)
					/*|| scanResult.SSID.equals("CMCC-AUTO")*/) {
				continue;
			} else {
				mListResult.add(AccessPoint.instanceByScanResult(scanResult));
			}

		}
		resetWifiListFlag();
		return getWifiManager().isWifiEnabled() ? new ArrayList<AccessPoint>(
				mListResult) : null;
	}

	public String getApSSID() {
		try {
			Method localMethod = this.mWifiManager.getClass()
					.getDeclaredMethod("getWifiApConfiguration", new Class[0]);
			if (localMethod == null)
				return null;
			Object localObject1 = localMethod.invoke(this.mWifiManager,
					new Object[0]);
			if (localObject1 == null)
				return null;
			WifiConfiguration localWifiConfiguration = (WifiConfiguration) localObject1;
			if (localWifiConfiguration.SSID != null)
				return localWifiConfiguration.SSID;
			Field localField1 = WifiConfiguration.class
					.getDeclaredField("mWifiApProfile");
			if (localField1 == null)
				return null;
			localField1.setAccessible(true);
			Object localObject2 = localField1.get(localWifiConfiguration);
			localField1.setAccessible(false);
			if (localObject2 == null)
				return null;
			Field localField2 = localObject2.getClass()
					.getDeclaredField("SSID");
			localField2.setAccessible(true);
			Object localObject3 = localField2.get(localObject2);
			if (localObject3 == null)
				return null;
			localField2.setAccessible(false);
			String str = (String) localObject3;
			return str;
		} catch (Exception localException) {
		}
		return null;
	}

	public Boolean addConfig(WifiConfiguration config) {
		if (config != null) {
			this.mWifiManager.addNetwork(config);
			return this.mWifiManager.saveConfiguration();
		}
		return false;
	}

	public int addNetwork(WifiConfiguration wcg) {
		if (wcg.networkId != -1) {
			this.mWifiManager.enableNetwork(wcg.networkId, true);
			Boolean reconnect = this.mWifiManager.reassociate();
			HWFLog.e(TAG, "reconnect 1  " + reconnect + "SSID:" + wcg.SSID);
			this.mWifiManager.saveConfiguration();
			return wcg.networkId;
		}

		int wcgID = mWifiManager.addNetwork(wcg);
		if (wcgID != -1) {
			boolean status = mWifiManager.enableNetwork(wcgID, true);
			HWFLog.e(TAG, status + "");
			Boolean reconnect = this.mWifiManager.reconnect();
			HWFLog.e(TAG, "reconnect 2 " + reconnect + "SSID:" + wcg.SSID);
		} else {
			HWFLog.e(TAG, "reconnect 2 " + wcgID + "SSID:" + wcg.SSID);
		}

		return wcgID;
	}

	public void saveConf() {
		this.mWifiManager.saveConfiguration();
	}

	public int addConfigedNetwork(AccessPoint accessPoint) {
		WifiConfiguration configuration = accessPoint.getWifiConfiguration();
		if (configuration == null) {
			HWFLog.e(TAG, "-1 not found config");
			return -1;
		}
		// 魅族 实际不是current,代码显示是current，估计是系统bug.故去掉这个逻辑.
		// if (configuration.status == Status.CURRENT) {
		// getWifiManager().reassociate();
		// return 0;
		// } else {
		configuration.status = Status.ENABLED;
		this.mWifiManager.disconnect();
		return addNetwork(configuration);
		// }

	}

	private Map<String, WifiConfiguration> configMap = new HashMap<String, WifiConfiguration>();

	public Map<String, WifiConfiguration> getConfMap() {
		return configMap;
	}

	private synchronized void setConfigMap() {
		List<WifiConfiguration> existingConfigs = mWifiManager
				.getConfiguredNetworks();
		if (existingConfigs != null) {
			Iterator<WifiConfiguration> iterator = existingConfigs
					.listIterator();
			configMap.clear();
			while (iterator.hasNext()) {
				WifiConfiguration config = (WifiConfiguration) iterator.next();
				String SSID = config.SSID;
				final int length = config.SSID.length();
				if (length > 2 && (config.SSID.charAt(0) == '"')
						&& config.SSID.charAt(length - 1) == '"') {
					SSID = config.SSID.substring(1, length - 1);
				}
				configMap.put(SSID, config);
			}
		}
	}

	public WifiConfiguration exsitedWifiConf(String SSID) {
		if (configMap == null || SSID == null) {
			return null;
		}
		return configMap.get(SSID);
	}

	public boolean isConnected() {
		ConnectivityManager connMgr = (ConnectivityManager) mContext
				.getSystemService(Context.CONNECTIVITY_SERVICE);
		NetworkInfo networkInfo = connMgr.getActiveNetworkInfo();
		return networkInfo != null && networkInfo.isConnected()
				&& networkInfo.isAvailable();
	}

	public WifiConfiguration createWifiConf(AccessPoint accessPoint,String userName,
			String password) {
		return createWifiConf(accessPoint, userName,password, false);
	}
	
	public WifiConfiguration createDotxWifiConf(AccessPoint accessPoint){
		return createWifiConf(accessPoint.getPrintableSsid(), "", "", accessPoint.getSecurityType(), false);
	}

	public WifiConfiguration createWifiConf(String SSID,String userName,String password,
			SECURITY_TYPE type, Boolean isAp) {
		WifiConfiguration config = new WifiConfiguration();
		config.SSID = "\"" + SSID + "\"";
		if (isAp) {
			config.SSID = SSID;
			config.allowedAuthAlgorithms.set(1);
			config.allowedGroupCiphers.set(WifiConfiguration.GroupCipher.CCMP);
			config.allowedGroupCiphers.set(WifiConfiguration.GroupCipher.TKIP);
			config.allowedGroupCiphers.set(WifiConfiguration.GroupCipher.WEP40);
			config.allowedGroupCiphers
					.set(WifiConfiguration.GroupCipher.WEP104);
			config.allowedKeyManagement.set(0);
			config.wepTxKeyIndex = 0;
			if (type == SECURITY_TYPE.SECURITY_NONE) {
				config.wepKeys[0] = "";
				config.allowedKeyManagement.set(0);
				config.wepTxKeyIndex = 0;
			} else if (type == SECURITY_TYPE.SECURITY_WEP) {
				config.wepKeys[0] = password;
			} else if (type == SECURITY_TYPE.SECURITY_PSK) {
				config.preSharedKey = password;
				config.allowedAuthAlgorithms.set(0);
				config.allowedProtocols.set(1);
				config.allowedProtocols.set(0);
				config.allowedKeyManagement.set(1);
				config.allowedPairwiseCiphers.set(2);
				config.allowedPairwiseCiphers.set(1);
			}
		} else {
			if (type == SECURITY_TYPE.SECURITY_NONE) {
				// config.wepKeys[0] = "";
				config.allowedKeyManagement.set(WifiConfiguration.KeyMgmt.NONE);
				config.wepTxKeyIndex = 0;
			}
			if (type == SECURITY_TYPE.SECURITY_WEP) {
				config.wepKeys[0] = "\"" + password + "\"";
				config.allowedAuthAlgorithms
						.set(WifiConfiguration.AuthAlgorithm.SHARED);
				config.allowedGroupCiphers
						.set(WifiConfiguration.GroupCipher.CCMP);
				config.allowedGroupCiphers
						.set(WifiConfiguration.GroupCipher.TKIP);
				config.allowedGroupCiphers
						.set(WifiConfiguration.GroupCipher.WEP40);
				config.allowedGroupCiphers
						.set(WifiConfiguration.GroupCipher.WEP104);
				config.allowedKeyManagement.set(WifiConfiguration.KeyMgmt.NONE);
				config.wepTxKeyIndex = 0;
			}
			if (type == SECURITY_TYPE.SECURITY_PSK) {
				config.preSharedKey = "\"" + password + "\"";
				config.allowedAuthAlgorithms
						.set(WifiConfiguration.AuthAlgorithm.OPEN);
				config.allowedGroupCiphers
						.set(WifiConfiguration.GroupCipher.TKIP);
				config.allowedKeyManagement
						.set(WifiConfiguration.KeyMgmt.WPA_PSK);
				config.allowedPairwiseCiphers
						.set(WifiConfiguration.PairwiseCipher.TKIP);
				// config.allowedProtocols.set(WifiConfiguration.Protocol.WPA);
				config.allowedGroupCiphers
						.set(WifiConfiguration.GroupCipher.CCMP);
				config.allowedPairwiseCiphers
						.set(WifiConfiguration.PairwiseCipher.CCMP);
			}
			
			//TODO 802.1X  EAP METHOD{PEAP,TLS,LEAP,TTLS,EAP-FAST}
			if(type == SECURITY_TYPE.SECURITY_EAP){
				config = createDot1xWifiConfiguration(SSID, userName,password,PEAPTYPEMETHOD.PEAP.getValue(),config);
			}
			
		}
		return config;
	}

	public enum PEAPTYPEMETHOD{
		PEAP("PEAP"),TLS("TLS"),LEAP("LEAP"),TTLS("TTLS"),EAPFAST("EAP-FAST");
		private String type;
		private PEAPTYPEMETHOD(String type){
			this.type = type;
		}
		public String getValue(){
			return this.type;
		}
	}
	public void createWifiAP(WifiConfiguration paramWifiConfiguration,
			boolean enabled) {
		try {
			Class localClass = this.mWifiManager.getClass();
			Class[] arrayOfClass = new Class[2];
			arrayOfClass[0] = WifiConfiguration.class;
			arrayOfClass[1] = Boolean.TYPE;
			Method localMethod = localClass.getMethod("setWifiApEnabled",
					arrayOfClass);
			WifiManager localWifiManager = this.mWifiManager;
			Object[] arrayOfObject = new Object[2];
			arrayOfObject[0] = paramWifiConfiguration;
			arrayOfObject[1] = Boolean.valueOf(enabled);
			localMethod.invoke(localWifiManager, arrayOfObject);
			return;
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public WifiConfiguration createWifiConf(AccessPoint accessPoint,String userName,
			String password, boolean isAp) {
		return createWifiConf(accessPoint.getScanResult().SSID, userName,password,
				accessPoint.getSecurityType(), isAp);
	}

	public int getWifiApState() {
		try {
			int i = ((Integer) this.mWifiManager.getClass()
					.getMethod("getWifiApState", new Class[0])
					.invoke(this.mWifiManager, new Object[0])).intValue();
			return i;
		} catch (Exception localException) {
		}
		return 4;
	}

	public Boolean disconnect() {
		onWifiStateChanged();
		return getWifiManager().disconnect();
	}

	private AccessPoint mConnectedAccessPoint = null;

	// 正在连接或连接成功的
	public AccessPoint connectedAccessPoint() {
		AccessPoint accessPoint = getActiveAccessPoint();
		WifiInfo wifiInfo = mWifiManager.getConnectionInfo();
		if (wifiInfo == null || accessPoint == null) {
			return null;
		} else {
			return accessPoint.isConnecting(wifiInfo) ? accessPoint : null;
		}
	}

	// 当前激活的，可能是断开连接
	public AccessPoint getActiveAccessPoint() {
		if (mConnectedAccessPoint != null && !wifiConnectHasChange()
				&& !wifiListHasChange()) {
			return mConnectedAccessPoint;
		}
		WifiInfo wifiInfo = mWifiManager.getConnectionInfo();
		if (wifiInfo != null) {
			AccessPoint accessPoint = getAccessPointByBSSID(wifiInfo.getBSSID());
			if (accessPoint != null) {
				mConnectedAccessPoint = accessPoint;
			} else {
				mConnectedAccessPoint = null;
			}
		} else {
			mConnectedAccessPoint = null;
		}
		resetWifiConnectFlag();
		return getWifiManager().isWifiEnabled() ? mConnectedAccessPoint : null;
	}

	public AccessPoint getAccessPointByBSSID(String BSSID) {
		// init pool
		getAccessPoints();
		AccessPoint accessPoint = AccessPoint.findByBSSID(BSSID);
		return accessPoint;
	}

	public Boolean isCmccConnected() {
		return isCommercenceConnected("CMCC");
	}

	public Boolean isChinaNetConnected() {
		return isCommercenceConnected("ChinaNet");
	}

	public Boolean isCommercenceConnected() {
		if (!ReleaseConstant.isCommerceOpened) {
			return false;
		}
		AccessPoint accessPoint = connectedAccessPoint();
		if (accessPoint != null) {
			if (accessPoint.isStateConnected()) {
				try {
					return accessPoint.supportAutoAuth();
				} catch (Exception e) {
					HWFLog.e(TAG, "exception, return false");
					return false;
				}

			}
			HWFLog.e(TAG, "no conntected, return false");
			return false;
		}
		HWFLog.e(TAG, "no connectedAccessPoint, return false");
		return false;
	}

	public Boolean isCommercenceConnected(String commercenName) {
		AccessPoint accessPoint = connectedAccessPoint();
		if (accessPoint != null) {
			if (accessPoint.isStateConnected()) {
				try {
					return accessPoint.getPrintableSsid().trim()
							.equalsIgnoreCase(commercenName);
				} catch (Exception e) {
					HWFLog.e(TAG, "exception, return false");
					return false;
				}

			}
			HWFLog.e(TAG, "no conntected, return false");
			return false;
		}
		HWFLog.e(TAG, "no connectedAccessPoint, return false");
		return false;
	}

	/*
	 * connectEduroam method configure security and user parametres and 
	 * establishes an eduroam connection. This method uses java reflection 
	 * api to set parametres.
	 */
	private WifiConfiguration createDot1xWifiConfiguration(String ssid,String userName,/*String hiddenName,*/String password,String eapType,WifiConfiguration selectedConfig) {
		//some constants for java reflection api
		final String INT_PRIVATE_KEY = "private_key";
	    final String INT_PHASE2 = "phase2";
	    final String INT_PASSWORD = "password";
	    final String INT_IDENTITY = "identity";
	    final String INT_EAP = "eap";
	    final String INT_CLIENT_CERT = "client_cert";
	    final String INT_CA_CERT = "ca_cert";
	    final String INT_ANONYMOUS_IDENTITY = "anonymous_identity";
	    final String INT_ENTERPRISEFIELD_NAME = "android.net.wifi.WifiConfiguration$EnterpriseField";
	    final String INT_ENTERPRISEFIELD_NAME_NEW = "android.net.wifi.WifiEnterpriseConfig";//>=4.3 sdk
	    //user credentials and ssid
//	    String username = "lanshaoze";
//	    String passWord = "w13463235585"; //password maked.
//	    String SSID = "HiWiFi-Office";
	    
	    //connection properties
	    final String ENTERPRISE_EAP = eapType;//"PEAP";
        final String ENTERPRISE_CLIENT_CERT = null;
        final String ENTERPRISE_PRIV_KEY = null;        
        
        final String ENTERPRISE_PHASE2 = "auth=PAP";
        final String ENTERPRISE_ANON_IDENT = null;
        
        
        
//        WifiConfiguration selectedConfig = new WifiConfiguration();
        
        selectedConfig.SSID = "\""+ssid+"\"";
        selectedConfig.priority = 48;
        selectedConfig.hiddenSSID = false;

        selectedConfig.allowedKeyManagement.clear();
        selectedConfig.allowedKeyManagement.set(WifiConfiguration.KeyMgmt.IEEE8021X);
        selectedConfig.allowedKeyManagement.set(WifiConfiguration.KeyMgmt.WPA_EAP);
        
        selectedConfig.allowedGroupCiphers.clear();
        selectedConfig.allowedGroupCiphers.set(WifiConfiguration.GroupCipher.CCMP);
        selectedConfig.allowedGroupCiphers.set(WifiConfiguration.GroupCipher.TKIP);
        selectedConfig.allowedGroupCiphers.set(WifiConfiguration.GroupCipher.WEP104);
        selectedConfig.allowedGroupCiphers.set(WifiConfiguration.GroupCipher.WEP40);

        
        selectedConfig.allowedPairwiseCiphers.clear();
        selectedConfig.allowedPairwiseCiphers.set(WifiConfiguration.PairwiseCipher.CCMP);
        selectedConfig.allowedPairwiseCiphers.set(WifiConfiguration.PairwiseCipher.TKIP);

        
        selectedConfig.allowedProtocols.clear();
        selectedConfig.allowedProtocols.set(WifiConfiguration.Protocol.RSN);
        selectedConfig.allowedProtocols.set(WifiConfiguration.Protocol.WPA);
        
        selectedConfig.allowedAuthAlgorithms.set(WifiConfiguration.AuthAlgorithm.OPEN);  
        selectedConfig.status = WifiConfiguration.Status.ENABLED;

        try {
            Class[] wcClasses = WifiConfiguration.class.getClasses();
            for (Class class1 : wcClasses) {
    			Log.d("Tag1:", class1.getName());
    		}
            Class wcEnterpriseField = null;

            for (Class wcClass : wcClasses)
            	if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR2){
            		if (wcClass.getName().equals(INT_ENTERPRISEFIELD_NAME_NEW)) 
            		{
            			wcEnterpriseField = wcClass;
            			break;
            		}
            	}else {
            		if (wcClass.getName().equals(INT_ENTERPRISEFIELD_NAME)) 
            		{
            			wcEnterpriseField = wcClass;
            			break;
            		}
				}
            
            boolean noEnterpriseFieldType = false; 
            

            Field wcefAnonymousId = null, wcefCaCert = null, wcefClientCert = null, wcefEap = null, wcefIdentity = null, wcefPassword = null, wcefPhase2 = null, wcefPrivateKey = null;
            Field[] wcefFields = WifiConfiguration.class.getFields();
            
            for (Field field : wcefFields) {
            	Log.d("Tag1:", "field:" + field.getName());
			}
            //Get fields from hidden api
            for (Field wcefField : wcefFields) 
            {
                if (wcefField.getName().equals(INT_ANONYMOUS_IDENTITY))
                    wcefAnonymousId = wcefField;
                else if (wcefField.getName().equals(INT_CA_CERT))
                    wcefCaCert = wcefField;
                else if (wcefField.getName().equals(INT_CLIENT_CERT))
                    wcefClientCert = wcefField;
                else if (wcefField.getName().equals(INT_EAP))
                    wcefEap = wcefField;
                else if (wcefField.getName().equals(INT_IDENTITY))
                    wcefIdentity = wcefField;
                else if (wcefField.getName().equals(INT_PASSWORD))
                    wcefPassword = wcefField;
                else if (wcefField.getName().equals(INT_PHASE2))
                    wcefPhase2 = wcefField;
                else if (wcefField.getName().equals(INT_PRIVATE_KEY))
                    wcefPrivateKey = wcefField;
            }
            
            //fix: 适配三星 ，还有问题
            if(wcEnterpriseField == null){
            	noEnterpriseFieldType = true;
            	if(wcefEap!=null){
            		LogUtil.d("Tag:", "wcefEap");
            		wcefEap.set(selectedConfig, ENTERPRISE_EAP);
            	}
            }


            Method wcefSetValue = null;
            if(!noEnterpriseFieldType)
            {
            for(Method m: wcEnterpriseField.getMethods())
                if(m.getName().trim().equals("setValue"))
                    wcefSetValue = m;
            }
            if(!noEnterpriseFieldType)
                wcefSetValue.invoke(wcefEap.get(selectedConfig), ENTERPRISE_EAP);
            else
            	wcefEap.set(selectedConfig, ENTERPRISE_EAP);
            
            if(!noEnterpriseFieldType)
            	wcefSetValue.invoke(wcefPhase2.get(selectedConfig), ENTERPRISE_PHASE2);
            else
            	wcefPhase2.set(selectedConfig, ENTERPRISE_PHASE2);

            if(!noEnterpriseFieldType)
                wcefSetValue.invoke(wcefAnonymousId.get(selectedConfig), ENTERPRISE_ANON_IDENT);
            else
            	wcefAnonymousId.set(selectedConfig, ENTERPRISE_ANON_IDENT);

            if(!noEnterpriseFieldType)
                wcefSetValue.invoke(wcefPrivateKey.get(selectedConfig), ENTERPRISE_PRIV_KEY);
            else
            	wcefPrivateKey.set(selectedConfig, ENTERPRISE_PRIV_KEY);

            if(!noEnterpriseFieldType)
                wcefSetValue.invoke(wcefIdentity.get(selectedConfig), userName);
            else
            	wcefIdentity.set(selectedConfig, userName);

            if(!noEnterpriseFieldType)
                wcefSetValue.invoke(wcefPassword.get(selectedConfig), password);
            else
            	wcefPassword.set(selectedConfig, password);

            if(!noEnterpriseFieldType)
                wcefSetValue.invoke(wcefClientCert.get(selectedConfig), ENTERPRISE_CLIENT_CERT);
            else
            	wcefClientCert.set(selectedConfig, ENTERPRISE_CLIENT_CERT);

            try{
            Field wcAdhoc = WifiConfiguration.class.getField("adhocSSID");
            Field wcAdhocFreq = WifiConfiguration.class.getField("frequency");

            wcAdhoc.setBoolean(selectedConfig, false);
            int freq = 2462;  
            wcAdhocFreq.setInt(selectedConfig, freq); 
            } catch (Exception e)
            {
                e.printStackTrace();
            }

        }catch (Exception e){
            e.printStackTrace();
        }

       return selectedConfig; 
	}
}
