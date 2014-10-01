package com.hiwifi.utils;

import android.app.KeyguardManager;
import android.app.KeyguardManager.KeyguardLock;
import android.content.Context;
import android.os.PowerManager;
import android.os.PowerManager.WakeLock;

public class KeyguardMgr {

	private static final String TAG = KeyguardMgr.class.getSimpleName();
	private static KeyguardMgr instances;
	private Context mContext;
	private KeyguardManager mKeyguardManager;
	private KeyguardLock mKeyguardLock;
	private WakeLock mWakeLock;
	private PowerManager mPowerManager;

	public static KeyguardMgr getInstances(Context context) {
		if (instances == null) {
			instances = new KeyguardMgr(context);
		}
		return instances;
	}

	private KeyguardMgr(Context context) {
		mContext = context;
		initKeyguard();
	}

	private void initKeyguard() {

		mKeyguardManager = (KeyguardManager) mContext.getSystemService(Context.KEYGUARD_SERVICE);
		mKeyguardLock = mKeyguardManager.newKeyguardLock(TAG);
		mPowerManager = (PowerManager) mContext.getSystemService(Context.POWER_SERVICE);
		mWakeLock = mPowerManager.newWakeLock(PowerManager.ACQUIRE_CAUSES_WAKEUP | PowerManager.SCREEN_DIM_WAKE_LOCK, TAG);
	}

	/**
	 * light up the screen
	 */
	public void acquireScreen() {
		if (mWakeLock != null) {
			mWakeLock.acquire();
		}
	}
	
//	public void 

	/**
	 * It may turn off shortly after you release it
	 */
	public void releaseScreen() {
		// boolean isScreenOn = mPowerManager.isScreenOn();
		if (mWakeLock != null) {
			mWakeLock.release();
			mWakeLock = null;
		}
	}

	public boolean isLockedScreen() {
		boolean isLocked = false;
		if (mKeyguardManager != null) {
			isLocked = mKeyguardManager.inKeyguardRestrictedInputMode();
		}
		return isLocked;
	}

	public void lockedScreen() {
		if (mKeyguardLock != null) {
			mKeyguardLock.reenableKeyguard();
			
		}
	}

	public void unLockedScreen() {
		if (mKeyguardLock != null) {
			mKeyguardLock.disableKeyguard();
		}
	}

}
