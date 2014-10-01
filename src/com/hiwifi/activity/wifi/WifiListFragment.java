package com.hiwifi.activity.wifi;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.Observable;
import java.util.Observer;
import java.util.Timer;
import java.util.TimerTask;

import org.json.JSONArray;
import org.json.JSONException;

import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.graphics.drawable.AnimationDrawable;
import android.graphics.drawable.Drawable;
import android.net.NetworkInfo;
import android.net.NetworkInfo.DetailedState;
import android.net.TrafficStats;
import android.net.wifi.ScanResult;
import android.net.wifi.SupplicantState;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.text.Selection;
import android.text.Spannable;
import android.text.TextUtils;
import android.text.method.HideReturnsTransformationMethod;
import android.text.method.PasswordTransformationMethod;
import android.util.Log;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.view.ViewTreeObserver.OnGlobalLayoutListener;
import android.view.Window;
import android.view.animation.AlphaAnimation;
import android.view.animation.Animation;
import android.view.animation.Animation.AnimationListener;
import android.view.animation.AnimationUtils;
import android.view.animation.Transformation;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.PopupWindow;
import android.widget.RelativeLayout;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import com.baidu.location.BDLocation;
import com.baidu.location.BDLocationListener;
import com.baidu.location.LocationClient;
import com.hiwifi.activity.PortalPageActivity;
import com.hiwifi.app.receiver.CmccStateBroadcastReceiver;
import com.hiwifi.app.receiver.CmccStateBroadcastReceiver.CmccStateEventHandler;
import com.hiwifi.app.receiver.HiwifiBroadcastReceiver;
import com.hiwifi.app.receiver.HiwifiBroadcastReceiver.WifiEventHandler;
import com.hiwifi.app.utils.CommonDialogUtil;
import com.hiwifi.app.utils.CommonDialogUtil.DialogActionListener;
import com.hiwifi.app.utils.HintDialog;
import com.hiwifi.app.utils.ToastUtil;
import com.hiwifi.app.views.CancelableEditText;
import com.hiwifi.app.views.MyListView;
import com.hiwifi.app.views.MyScrollView;
import com.hiwifi.app.views.MyScrollView.OnScrollListener;
import com.hiwifi.app.views.PullToRefreshView;
import com.hiwifi.app.views.PullToRefreshView.OnHeaderRefreshListener;
import com.hiwifi.app.views.SelectPicPopupWindow;
import com.hiwifi.app.views.SelectPicPopupWindow.IPopwindowCallback;
import com.hiwifi.constant.RequestConstant.RequestTag;
import com.hiwifi.hiwifi.Gl;
import com.hiwifi.hiwifi.Gl.GlConf;
import com.hiwifi.hiwifi.R;
import com.hiwifi.model.log.HWFLog;
import com.hiwifi.model.log.LogUtil;
import com.hiwifi.model.request.RequestFactory;
import com.hiwifi.model.request.RequestManager;
import com.hiwifi.model.request.RequestManager.ResponseHandler;
import com.hiwifi.model.request.ServerResponseParser;
import com.hiwifi.model.speedtest.WebPageTester;
import com.hiwifi.model.wifi.AccessPoint;
import com.hiwifi.model.wifi.AccessPoint.WebpageTestListener;
import com.hiwifi.model.wifi.AccessPoint.WiFiChangeState;
import com.hiwifi.model.wifi.AccessPoint.WifiConnectState;
import com.hiwifi.model.wifi.WifiAdmin;
import com.hiwifi.model.wifi.WifiSortable;
import com.hiwifi.model.wifi.adapter.ConnectAdapter;
import com.hiwifi.model.wifi.adapter.ConnectCallback;
import com.hiwifi.store.AccessPointModel;
import com.hiwifi.store.AccessPointModel.PasswordSource;
import com.hiwifi.store.AccessPointModel.PasswordStatus;
import com.hiwifi.utils.ViewUtil;
import com.umeng.analytics.MobclickAgent;

public class WifiListFragment extends WifiFragment implements
		OnItemClickListener,/* OnItemLongClickListener, */OnClickListener,
		OnScrollListener, OnHeaderRefreshListener, /* OnItemLongClickListener, */
		ResponseHandler {
	public final static String TAG = "WifiListFragment";
	private ListAdapter listAdapter;
	private Comparator<AccessPoint> mCurrentComparator;

	private LinearLayout connectedWifi, checkPassword;
	private MyListView mListView;
	private List<AccessPoint> mDisplayedList = new ArrayList<AccessPoint>();
	private List<AccessPoint> mAvaibleList = new ArrayList<AccessPoint>();
	private AccessPoint mAttempAccessPoint;
	private AccessPoint mConnectedAccessPoint = null;
	private AccessPoint mBestAccessPoint = null;
	// private CMCCConnectAdapter adapter;
	private Handler mHandler;
	private final int detectTimeout = 9 * 1000;

	private final int detectFrozeonTime = 1000;
	private final int openWifiTimeout = 3000;
	private final int CURRENT_TRACFFIC = 0x12;

	private Boolean shouldDetectSpeedAfterConnected = false;
	private PopupWindow menuWindow;
	private Dialog detectMaskDialog;
	private Boolean programScan = false;
	private Boolean scanTriggerByPull = false;
	private ImageView menuView;
	private TopViewHolder topViewHolder = new TopViewHolder();
	private View currentView = null;
	private int position;
	private int CLICKCODE = CLOSE;
	private static int OPEN = 0x100;
	private static int CLOSE = 0x101;
	private int topHeight;
	private MyScrollView myScrollView;
	private ImageView top_layout, top;

	private PullToRefreshView refresh;

	private boolean isResume = false;
	private ConnectAdapter adapter;

	// nowifi ui
	// private ImageView eyeImageView;

	private ImageView searchingImageView, wifiSwitch, refreshWifiPullpwd;
	// private ImageView iv_action_more;

	// private ImageButton refreshButton;
	// private ViewSwitcher viewSwitcher;
	private HintDialog hdDialog;
	private int selected_position;
	private boolean isLongClick = false;
	private TextView /* netState, */wifiState;
	private FrameLayout main_container;
	private LinearLayout alpha_layout;
	private AlphaAnimation animation, animation2;
	private SelectPicPopupWindow pop;
	private ImageView wifi_icon;
	private TextView current_net_status;
	private LinearLayout sub_container;
	private View rootView;
	private TextView info_show_tx;
	private Animation anim_in, anim_out;
	private boolean isConnectedByUser = false; // 是否用户主动连接

	// 定位
	public LocationClient mLocationClient = null;
	public BDLocationListener myListener = new MyLocationListener();

	public synchronized final WifiListListener getListener() {
		return listener;
	}

	public synchronized final void setListener(WifiListListener listener) {
		this.listener = listener;
	}

	private WifiListListener listener;

	private void startScan() {
		programScan = true;
		mWifiAdmin.getWifiManager().startScan();
	}

	private Dialog passwordDialog;

	private synchronized void showPasswordDialog(final AccessPoint accessPoint) {
		if (accessPoint == null) {
			return;
		}
		LayoutInflater factory = LayoutInflater.from(this.getActivity());
		View textEntryView = factory.inflate(R.layout.wifi_password_pop, null);
		// textEntryView.setLayoutParams(new LayoutParams((int)
		// (ViewUtil.getScreenWidth() * 0.9), LayoutParams.WRAP_CONTENT));
		mPasswordDialogViewHolder = new PasswordDialogViewHolder();
		mPasswordDialogViewHolder.initWithParent(textEntryView);
		mPasswordDialogViewHolder.ssidEditText.setText(mAttempAccessPoint
				.getScanResult().SSID);
		mPasswordDialogViewHolder.connectButton.setOnClickListener(this);
		mPasswordDialogViewHolder.cancelButton.setOnClickListener(this);
		mPasswordDialogViewHolder.close.setOnClickListener(this);
		// 使View滑动到ScrollView底部
		handler.post(new Runnable() {

			@Override
			public void run() {
				// TODO Auto-generated method stub
				mPasswordDialogViewHolder.myscrollview
						.fullScroll(View.FOCUS_DOWN);
			}
		});
		if (accessPoint.isDotxType()) {
			MobclickAgent
					.onEvent(getActivity(), "show_password_type", "802.1x");
			mPasswordDialogViewHolder.edit_username.getEditText()
					.requestFocus();
			(textEntryView.findViewById(R.id.username_container))
					.setVisibility(View.VISIBLE);
			(textEntryView.findViewById(R.id.password_container))
					.setVisibility(View.VISIBLE);
		} else {
			MobclickAgent.onEvent(getActivity(), "show_password_type",
					"password");
			mPasswordDialogViewHolder.passwordEditText.requestFocus();
			(textEntryView.findViewById(R.id.username_container))
					.setVisibility(View.GONE);
			(textEntryView.findViewById(R.id.password_container))
					.setVisibility(View.VISIBLE);
		}

		if (GlConf.isShowPassword()) {
			mPasswordDialogViewHolder.passwordEditText
					.setTransformationMethod(HideReturnsTransformationMethod
							.getInstance());
			mPasswordDialogViewHolder.show_password_checked.setChecked(true);

		} else {
			mPasswordDialogViewHolder.passwordEditText
					.setTransformationMethod(PasswordTransformationMethod
							.getInstance());
			mPasswordDialogViewHolder.show_password_checked.setChecked(false);
		}
		mPasswordDialogViewHolder.show_password_checked
				.setOnCheckedChangeListener(new OnCheckedChangeListener() {

					@Override
					public void onCheckedChanged(CompoundButton buttonView,
							boolean isChecked) {
						GlConf.setShowPassword(isChecked);
						if (isChecked) {
							mPasswordDialogViewHolder.passwordEditText
									.setTransformationMethod(HideReturnsTransformationMethod
											.getInstance());
						} else {
							mPasswordDialogViewHolder.passwordEditText
									.setTransformationMethod(PasswordTransformationMethod
											.getInstance());
						}
						focusOnEnd();

					}

					private void focusOnEnd() {
						CharSequence textCharSequence = mPasswordDialogViewHolder.passwordEditText
								.getText();
						if (textCharSequence instanceof Spannable) {
							Spannable spannable = (Spannable) textCharSequence;
							Selection.setSelection(spannable,
									textCharSequence.length());
						}
					}
				});

		if (pop != null && pop.isShowing()) {
			return;
		}
		pop = new SelectPicPopupWindow(getActivity(), textEntryView,
				new IPopwindowCallback() {

					@Override
					public void showWindowDetail(View window) {
						// TODO Auto-generated method stub

					}
				});

		pop.showAtLocation(main_container, Gravity.BOTTOM, 0, 0);
		pop.setOutsideTouchable(true);
		handler.postDelayed(new Runnable() {

			@Override
			public void run() {
				InputMethodManager inputManager = (InputMethodManager) Gl.Ct()
						.getSystemService(Context.INPUT_METHOD_SERVICE);
				if (accessPoint.isDotxType()) {
					inputManager.showSoftInput(
							mPasswordDialogViewHolder.edit_username
									.getEditText(), 0);
				} else {
					inputManager.showSoftInput(
							mPasswordDialogViewHolder.passwordEditText, 0);
				}
			}
		}, 100);
	}

	private void closeInputPop() {
		if (pop != null && pop.isShowing()) {
			pop.dismiss();
		}
	}

	private PasswordDialogViewHolder mPasswordDialogViewHolder = null;

	private class PasswordDialogViewHolder {
		EditText passwordEditText;
		CancelableEditText edit_username;
		TextView ssidEditText;
		ImageButton close;
		Button connectButton;
		Button cancelButton;
		ScrollView myscrollview;
		TextView error_info;
		CheckBox show_password_checked;

		public void initWithParent(View parentView) {
			passwordEditText = (EditText) parentView
					.findViewById(R.id.edit_password);
			passwordEditText.requestFocus();
			ssidEditText = (TextView) parentView.findViewById(R.id.ssid);
			edit_username = (CancelableEditText) parentView
					.findViewById(R.id.edit_username);
			error_info = (TextView) parentView.findViewById(R.id.error_info);
			close = (ImageButton) parentView.findViewById(R.id.dlg_close);
			connectButton = (Button) parentView
					.findViewById(R.id.btn_password_connect);
			cancelButton = (Button) parentView
					.findViewById(R.id.btn_password_cancel);
			myscrollview = (ScrollView) parentView.findViewById(R.id.myscroll);
			show_password_checked = (CheckBox) parentView
					.findViewById(R.id.show_password_checked);
		}

	}

	private WifiEventHandler wifiEventHandler = new WifiEventHandler() {

		@Override
		public void onWifiStatusChange(int state, int preState) {
			mHandler.removeCallbacks(openTimeoutRunnable);
			if (state == WifiManager.WIFI_STATE_ENABLED) {
				isPause = false;
				if(refresh.isActivated()){
					refresh.onHeaderRefreshComplete(new Date().toLocaleString());
				}
				wifiSwitch
						.setBackgroundResource(R.drawable.selector_wifi_switch);
				wifiState.setText("已开启");
				wifiState.setTextColor(Gl.Ct().getResources().getColor(
						R.color.wifilist_normal_color));
				// if (isEmptyPage()) {
				startScan();
				// }
			} else if (state == WifiManager.WIFI_STATE_DISABLED) {
				isPause = true;
				
				wifiSwitch.setBackgroundResource(R.drawable.icon_wifioff);
				wifiState.setText("已关闭");
				wifiState.setTextColor(Gl.Ct().getResources().getColor(
						R.color.wifilist_itembg_detecting));
				Intent intent = new Intent(getActivity(),
						WiFiOperateActivity.class);
				intent.putExtra(WiFiOperateActivity.KEYTAG, "main");
				startActivity(intent);
				// stop animation
				// if (isEmptyPage()) {
				// showSearchError();
				// } else {
				// showEmpty();
				// }
			}

		}

		private Boolean isSystemAutoConnect(String BSSID) {
			return mAttempAccessPoint == null
					|| (BSSID != null && !mAttempAccessPoint.getGroupedBssid()
							.contains(BSSID));
		}

		@Override
		public void onWifiConnectChange(NetworkInfo networkInfo, String BSSID,
				WifiInfo wifiInfo) {
			HWFLog.e(TAG, (networkInfo != null ? networkInfo.getDetailedState()
					: "no networkinfo")
					+ "--"
					+ BSSID
					+ "--"
					+ (wifiInfo != null ? wifiInfo.getSupplicantState()
							: "no wifiinfo"));
			if (networkInfo != null) {
				// onConnectingDetailChanged(networkInfo.getDetailedState());
				// 连上wifi并获取到ip
				if (networkInfo.getDetailedState() == DetailedState.CONNECTED) {
					if (isSystemAutoConnect(BSSID)) {
						if (mAttempAccessPoint != null) {
							if (mAttempAccessPoint.needPassword()) {
								mAttempAccessPoint
										.setConnectState(WifiConnectState.connectState_local_restore);
							} else {
								mAttempAccessPoint
										.setConnectState(WifiConnectState.connectState_canconnect);
							}
						}
						if (mWifiAdmin.getActiveAccessPoint() != null) {
							mAttempAccessPoint = mWifiAdmin
									.getActiveAccessPoint();
						}
						mHandler.sendEmptyMessage(DisplayListChangeHandler.actiontypeConnectedSystem);
					} else {
						mHandler.sendEmptyMessage(DisplayListChangeHandler.actiontypeConnectedProgram);
						MobclickAgent.onEvent(
								getActivity(),
								"linked_by_click",
								Gl.Ct().getResources().getString(
										R.string.stat_conn_by_click));

					}

					// 登录成功并且未配置过
					if (mAttempAccessPoint != null) {
						if (mAttempAccessPoint.getDataModel().passwordType == PasswordSource.PasswordSourceLocal
								.ordinal() && !mAttempAccessPoint.isConfiged()) {
							mAttempAccessPoint.saveConfig();
						}
						testSpeedOnAccesspoint(mAttempAccessPoint, false);
					}

				}

			}

		}

		@Override
		public void onSupStatusChange(SupplicantState newState, int error) {
			HWFLog.e(TAG, newState + " sdasda");
			if (newState != SupplicantState.SCANNING) {
				mHandler.removeCallbacks(connectTimeoutRunnable);
			}
			MobclickAgent.onEvent(getActivity(), "stat_wifi_state", newState
					+ "");
			switch (newState) {
			case DISCONNECTED:
				if (error != 0) {
					if (error == WifiManager.ERROR_AUTHENTICATING) {
						// showError(getResources().getString(
						// R.string.ap_password_error));
						MobclickAgent.onEvent(getActivity(),
								"error_authenticating", Gl.Ct().getResources()
										.getString(R.string.stat_error_auth));
						mHandler.sendEmptyMessage(DisplayListChangeHandler.actiontypeConnectPasswordError);
					}
					CLICKCODE = CLOSE;
				} else {
					mHandler.sendEmptyMessageDelayed(
							DisplayListChangeHandler.actiontypeDisConnected,
							200);
				}
				break;
			case DORMANT:
			case UNINITIALIZED:
			case INVALID:
			case INTERFACE_DISABLED:
			case INACTIVE:
				mHandler.sendEmptyMessage(DisplayListChangeHandler.actiontypeDisConnected);
				break;
			case SCANNING:
			case AUTHENTICATING:
			case ASSOCIATING:
			case ASSOCIATED:
			case FOUR_WAY_HANDSHAKE:
			case GROUP_HANDSHAKE:
			case COMPLETED:
				mHandler.removeMessages(DisplayListChangeHandler.actiontypeDisConnected);
				break;
			default:
				break;
			}

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
			// System.out.println("-----");
			if (programScan) {
				programScan = false;
				onScanResultChanged(false);
			}
			mLocationClient.requestLocation();
		}

	};

	private void showAnimation(AccessPoint accessPoint) {
		if (isConnectedByUser) {
			info_show_tx.setText(accessPoint.getChangeStateString());
			info_show_tx.startAnimation(anim_in);
			isConnectedByUser = false;
		}
	}

	public void testSpeedOnAccesspoint(final AccessPoint accessPoint,
			final Boolean isOnresume) {
		accessPoint.startWebTest(Gl.GlConf.getPingTimeout(),
				new WebpageTestListener() {
					@Override
					public void onTestFinished(long usetime) {
						setConnectedAccessPoint(accessPoint,
								WiFiChangeState.connectState_netok, false);
						// if (!isOnresume) {
						// gotoSuccessPage();
						// }
						if (WifiAdmin.sharedInstance().connectedAccessPoint() != null) {
							showProgressing(accessPoint.getChangeStateString());
							sub_container.setVisibility(View.VISIBLE);
							showAnimation(accessPoint);
						}
						if (((AnimationDrawable) wifi_icon.getDrawable())
								.isRunning()) {
							((AnimationDrawable) wifi_icon.getDrawable())
									.stop();
							((AnimationDrawable) wifi_icon.getDrawable())
									.selectDrawable(0);
						}
					}

					@Override
					public void onTestFailed(int code, String reason) {
						if (((AnimationDrawable) wifi_icon.getDrawable())
								.isRunning()) {
							((AnimationDrawable) wifi_icon.getDrawable())
									.stop();
							((AnimationDrawable) wifi_icon.getDrawable())
									.selectDrawable(0);
						}
						if (code == WebPageTester.ErrorCodeCaptured) {
							LogUtil.d("tag:", "需认证");
							setConnectedAccessPoint(accessPoint,
									WiFiChangeState.connectState_needauth,
									false);
							// adapter = ConnectAdapterFactory.createAdapter(
							// accessPoint, getActivity());
							// if (adapter != null) {
							// adapter.setCallback(new CmccConnectCallback());
							// if (adapter.supportAutoAuth()) {
							// showProgressing("HiWiFi帮助您自动登录中");
							// } else {
							// CommonDialogUtil.ConnectDialog
							// .dismiss(false);
							// }
							//
							// adapter.login();
							// } else {
							// gotoSuccessPage();
							// }
							// }
						} else if (code == WebPageTester.ErrorCodeNetException) {
							setConnectedAccessPoint(accessPoint,
									WiFiChangeState.connectState_net_exception,
									false);
						}
						if (WifiAdmin.sharedInstance().connectedAccessPoint() != null) {
							sub_container.setVisibility(View.GONE);
							showAnimation(mConnectedAccessPoint);
							if (!isPause) {
								if ((code == WebPageTester.ErrorCodeCaptured || code == WebPageTester.ErrorCodeNetException)) {
									enterPortal(accessPoint);
								}
							}
						}
					}

				});
	}

	private void enterPortal(AccessPoint accessPoint) {
		if (!PortalPageActivity.is_open) {
			PortalPageActivity.is_open = true;
			Intent intent = new Intent(Gl.Ct(), PortalPageActivity.class);
			AccessPoint pString = WifiAdmin.sharedInstance()
					.connectedAccessPoint();
			intent.putExtra("ssid",
					pString == null ? "" : pString.getPrintableSsid());
			intent.putExtra("from", this.getClass().toString());
			if (accessPoint.isShowPortal()) {

			} else if (accessPoint.isShowNetException()) {
				intent.putExtra("title", "极路由诊断");
				intent.putExtra("close_btn", "退出诊断");
			}
			startActivity(intent);
		}
	}

	private void onScanResultChanged(Boolean isOnResume) {
		if (isOnResume) {
			mHandler.sendEmptyMessage(DisplayListChangeHandler.actiontypeInit);
		} else {
			mHandler.sendEmptyMessage(DisplayListChangeHandler.actiontypeRefresh);
		}

	}

	public class CmccConnectCallback implements ConnectCallback {

		@Override
		public void onLogoutSuccess(ConnectAdapter adapter) {
		}

		@Override
		public void onLogoutFail(ConnectAdapter adapter, String code, String msg) {
		}

		@Override
		public void onLoginSuccess(ConnectAdapter adapter) {
		}

		@Override
		public void onLoginFail(ConnectAdapter adapter, String code, String msg) {
		}
	}

	private void setConnectedAccessPoint(AccessPoint accessPoint,
			WiFiChangeState state, boolean isConnect) {
		if (getActivity() == null) {
			return;
		}
		if (accessPoint == null) {
			// if (mConnectedAccessPoint != null) {
			// if (state == WiFiChangeState.connectState_connected) {
			// mConnectedAccessPoint
			// .setConnectState(WifiConnectState.connectState_canconnect);
			// } else {
			// mConnectedAccessPoint.setChangeState(state);
			// // mConnectedAccessPoint.setConnectState(state);
			// }
			// mConnectedAccessPoint = null;
			// }
			// topViewHolder.stopTimer();
			connectedWifi.setVisibility(View.GONE);
			if (isConnect) {
				mConnectedAccessPoint.setChangeState(state);
				showProgressing(mConnectedAccessPoint.getChangeStateString());
			} else {
				if (mAttempAccessPoint != null) {
					mAttempAccessPoint.setChangeState(state);
				}
			}
		} else {
			mConnectedAccessPoint = accessPoint;
			// mConnectedAccessPoint.setConnectState(state);
			mConnectedAccessPoint.setChangeState(state);
			connectedWifi.setVisibility(View.VISIBLE);
			topViewHolder.configByAccessPoint(mConnectedAccessPoint);
			showProgressing(mConnectedAccessPoint.getChangeStateString());
		}
	}

	private void onConnectingDetailChanged(DetailedState state) {
		if (mConnectedAccessPoint != null && connectedWifi.isShown()) {
			String detailString = AccessPoint.getDetailStateDescription(state);
			if (detailString != null) {
				showProgressing(detailString);
			}
		}

	}

	public class DisplayListChangeHandler extends Handler {
		@Override
		public void handleMessage(Message msg) {
			super.handleMessage(msg);
			if (msg.what < msg_connectWifi) {
				displayListChangeOnAction(msg.what);
			} else {
				switch (msg.what) {
				case msg_connectWifi:
					startConnectInNewThread();
					break;
				case msg_init_wifi_status:
					initWifiState();
					break;
				default:
					break;
				}
			}
		}

		public static final int actiontypeInit = 0;
		public static final int actiontypeRefresh = 1;
		public static final int actiontypeStartConnect = 2;
		public static final int actiontypeConnectPasswordError = 3;
		public static final int actiontypeDisConnected = 4;
		public static final int actiontypeConnectTimeout = 5;
		public static final int actiontypeConnectedProgram = 6;
		public static final int actiontypeConnectedSystem = 7;
		public static final int actiontypeConnectedCancelByUser = 8;
		public static final int msg_connectWifi = 9;
		public static final int msg_init_wifi_status = 10;

		private synchronized void displayListChangeOnAction(final int actiontype) {
			HWFLog.e(TAG, "displayListChangeOnAction actiontype:" + actiontype);
			switch (actiontype) {
			case actiontypeStartConnect:
				// //TODO WIFI ICON 旋转效果
				// AnimationUtil.setFlickerAnimation(wifi_icon);
				((AnimationDrawable) wifi_icon.getDrawable()).start();
				sub_container.setVisibility(View.GONE);
				if (mConnectedAccessPoint != null) {
					if (!mDisplayedList.contains(mConnectedAccessPoint)) {
						mDisplayedList.add(0, mConnectedAccessPoint);
					}
					setConnectedAccessPoint(null,
							WiFiChangeState.connectState_netok, false);
				}
				if (mDisplayedList != null && mAttempAccessPoint != null) {
					mAttempAccessPoint.onDetailedStateChanged(
							DetailedState.CONNECTING, mDisplayedList);
				}
				setConnectedAccessPoint(mAttempAccessPoint,
						WiFiChangeState.connectState_connecting, false);
				if (mDisplayedList != null && mConnectedAccessPoint != null) {
					mDisplayedList.remove(mConnectedAccessPoint);
				}
				// showProgressing(R.string.wifi_status_connectting);
				showProgressing(mAttempAccessPoint.getChangeStateString());

				break;
			case actiontypeInit:
			case actiontypeRefresh: {
				LogUtil.d("Tag:",
						"mmmmmmmmmmmmmmmmmmmmmmmmmmmmmmmmmmmmmmmmmmmmmmmmmmm");
				AccessPoint accessPoint = mWifiAdmin.connectedAccessPoint();
				mDisplayedList = mWifiAdmin.getMergedAccessPoints();
				if (accessPoint != null) {
					setConnectedAccessPoint(accessPoint,
							accessPoint.getChangeState(), false);
					if (mDisplayedList != null) {
						mDisplayedList.remove(mConnectedAccessPoint);
						LogUtil.d("Tag:", "remove");
						if (!isPause && actiontype == actiontypeInit) {
							mConnectedAccessPoint.onDetailedStateChanged(
									DetailedState.CONNECTED, mDisplayedList);
						}
					}
					if (actiontype == actiontypeInit) {
						LogUtil.d("Tag:", "ping");
						testSpeedOnAccesspoint(accessPoint, true);
					}
				} else {
					if (mConnectedAccessPoint != null) {
						LogUtil.d("i:", "当前连接断开");
						setConnectedAccessPoint(null,
								mConnectedAccessPoint.getChangeState(), false);
					}
				}
				if (mDisplayedList != null) {
					Collections.sort(mDisplayedList, mCurrentComparator);
				}
				if (mWifiAdmin.isConnected()) {
					startPullPassword(mDisplayedList);
				} else {
					refresh.onHeaderRefreshComplete(new Date().toLocaleString());
					if (mWifiAdmin.isWifiEnable()) {
						if(listAdapter == null){
							listAdapter = new ListAdapter();
						}
						int count = listAdapter.getFree() == null?0:listAdapter.getFree().size();
						if (count != 0) {
							info_show_tx.setText("发现了" + count + "个免费WiFi");
							info_show_tx.startAnimation(anim_in);
						}
					}
				}
			}
				break;
			case actiontypeConnectedProgram:
			case actiontypeConnectedSystem:
				HWFLog.e(TAG, "mAttempAccessPoint" + mAttempAccessPoint);
				if (mDisplayedList != null && mAttempAccessPoint != null) {
					mAttempAccessPoint
							.getDataModel()
							.setPasswordStatus(
									PasswordStatus.PasswordStatusValid).sync();
					mAttempAccessPoint.onDetailedStateChanged(
							DetailedState.CONNECTED, mDisplayedList);
				}
				setConnectedAccessPoint(mAttempAccessPoint,
						WiFiChangeState.connectState_connected, false);
				if (mDisplayedList != null && mConnectedAccessPoint != null) {
					mDisplayedList.remove(mConnectedAccessPoint);
				}
				// if (passwordDialog != null && passwordDialog.isShowing()) {
				// passwordDialog.dismiss();
				// }
				closeInputPop();
				// showProgressing(mConnectedAccessPoint.getChangeStateString());
				((AnimationDrawable) wifi_icon.getDrawable()).stop();
				((AnimationDrawable) wifi_icon.getDrawable()).selectDrawable(0);
				// sub_container.setVisibility(View.VISIBLE);
				info_show_tx.setText(mConnectedAccessPoint
						.getChangeStateString());
				// info_show_tx.startAnimation(anim_in);
				break;

			case actiontypeConnectTimeout:
				if (mConnectedAccessPoint != null) {
					if (!mDisplayedList.contains(mConnectedAccessPoint)) {
						mDisplayedList.add(0, mConnectedAccessPoint);
					}
					setConnectedAccessPoint(null,
							WiFiChangeState.connectState_net_exception, true);
					showAnimation(mConnectedAccessPoint);
				}
				closeInputPop();
				break;
			case actiontypeConnectPasswordError:
				if (mConnectedAccessPoint != null) {
					mConnectedAccessPoint
							.getDataModel()
							.setPasswordStatus(
									PasswordStatus.PasswordStatusInvalid)
							.sync();
					if (mConnectedAccessPoint.isDotxType()) {
						mConnectedAccessPoint
								.setConnectState(WifiConnectState.connectState_needUserCount);
					} else {
						if (mConnectedAccessPoint.needPassword()) {
							mConnectedAccessPoint
									.setConnectState(WifiConnectState.connectState_needpassword);
						}
					}
					// 删除数据库数据
					mConnectedAccessPoint.getDataModel().resetAp();
					if (!mDisplayedList.contains(mConnectedAccessPoint)) {
						mDisplayedList.add(0, mConnectedAccessPoint);
					}
					setConnectedAccessPoint(
							null,
							WiFiChangeState.connectState_connectfailedPasswordError,
							true);
					showAnimation(mConnectedAccessPoint);

				}
				if (mAttempAccessPoint != null
						&& mAttempAccessPoint.needPassword()) {
					showPasswordDialog(mAttempAccessPoint);
				}

				break;
			case actiontypeDisConnected:
				LogUtil.d("Tag:",
						"lmmmmmmmmmmmmmmmmmmmmmmmmmmmmmmmmmmmmmmmmmmmmmmmmmmm");
				if (mConnectedAccessPoint != null) {
					LogUtil.d("Tag:",
							"Mmmmmmmmmmmmmmmmmmmmmmmmmmmmmmmmmmmmmmmmmmmmmmmmmmmm");
					mConnectedAccessPoint.onDetailedStateChanged(
							DetailedState.DISCONNECTED, mDisplayedList);
					if (!mDisplayedList.contains(mConnectedAccessPoint)) {
						mDisplayedList.add(0, mConnectedAccessPoint);
					}
					if (!isPause) {
						setConnectedAccessPoint(null,
								WiFiChangeState.connectState_net_exception,
								true);
					} else {
						setConnectedAccessPoint(null,
								WiFiChangeState.connectState_connectNoConnect,
								true);
					}
					showAnimation(mConnectedAccessPoint);
				}
				break;
			case actiontypeConnectedCancelByUser:
				break;
			default:
				break;
			}
			if(listAdapter == null){
				listAdapter = new ListAdapter();
			}
			listAdapter.setScanList(mDisplayedList);
			listAdapter.notifyDataSetChanged();
		}

	}

	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		mHandler = new DisplayListChangeHandler();
		mCurrentComparator = WifiSortable.sortByWifiType();
		mLocationClient = new LocationClient(Gl.Ct()); // 声明LocationClient类
		mLocationClient.registerLocationListener(myListener); // 注册监听函数
	}

	@Override
	public void onActivityCreated(Bundle savedInstanceState) {
		initView();
		initEvent();
		if (listAdapter == null) {
			listAdapter = new ListAdapter();
		}
		listAdapter.setScanList(mDisplayedList);
		mListView.setAdapter(listAdapter);

		resetCurrentConfig();
		mHandler.sendEmptyMessage(DisplayListChangeHandler.msg_init_wifi_status);
		// listAdapter.notifyDataSetChanged();
		super.onActivityCreated(savedInstanceState);
	}

	@Override
	public void onActivityResult(int requestCode, int resultCode, Intent data) {
		if (requestCode == 0x100 && resultCode == 0x200) {
			mHandler.sendEmptyMessage(DisplayListChangeHandler.msg_init_wifi_status);
			HiwifiBroadcastReceiver.addListener(wifiEventHandler);
		}
		super.onActivityResult(requestCode, resultCode, data);
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		if (rootView == null) {
			rootView = inflater.inflate(R.layout.fragment_wifi_list, null);
		}
		// 缓存的rootView需判断是否已经被加过parent，如果有parent需要从parent删除，否则报错
		ViewGroup parent = (ViewGroup) rootView.getParent();
		if (parent != null) {
			parent.removeView(rootView);
		}
		return rootView;
	}

	@Override
	public void onHiddenChanged(boolean hidden) {
		// TODO Auto-generated method stub
		super.onHiddenChanged(hidden);
		if (hidden) {
			isPause = true;
		} else {
			isPause = false;
		}
	}

	public synchronized void resetCurrentConfig() {

		if (mConnectedAccessPoint != null) {
			mConnectedAccessPoint.resetConfigFlag();
		}
		if (mDisplayedList != null && mDisplayedList.size() > 0) {
			Iterator<AccessPoint> iterator = mDisplayedList.iterator();
			while (iterator.hasNext()) {
				AccessPoint accessPoint = (AccessPoint) iterator.next();
				accessPoint.resetConfigFlag();
			}
		}
	}

	
	@Override
	public void onResume() {
		super.onResume();
		MobclickAgent.onPageStart(this.getClass().getSimpleName()); //统计页面
		HiwifiBroadcastReceiver.addListener(wifiEventHandler);
		if (WifiAdmin.sharedInstance().isWifiEnable()) {
			wifiSwitch.setBackgroundResource(R.drawable.selector_wifi_switch);
			wifiState.setText("已开启");
			wifiState.setTextColor(Gl.Ct().getResources().getColor(
					R.color.wifilist_normal_color));
			// if ((mDisplayedList != null && mDisplayedList.size() == 0)
			// || (mWifiAdmin.getActiveAccessPoint() != null
			// && mConnectedAccessPoint != null && mWifiAdmin
			// .getActiveAccessPoint().getPrintableSsid()
			// .equals(mConnectedAccessPoint.getPrintableSsid()))) {
			// mHandler.sendEmptyMessage(DisplayListChangeHandler.msg_init_wifi_status);
			// }
		} else {
			wifiSwitch.setBackgroundResource(R.drawable.icon_wifioff);
			wifiState.setText("已关闭");
			wifiState.setTextColor(Gl.Ct().getResources().getColor(
					R.color.wifilist_itembg_detecting));
		}
		startCheckCurrentFlow();
		resetCurrentConfig();
		mHandler.sendEmptyMessage(DisplayListChangeHandler.msg_init_wifi_status);
		if (listAdapter != null) {
			listAdapter.setScanList(mDisplayedList);
			listAdapter.notifyDataSetChanged();
		}
		mLocationClient.start();
	}

	private boolean isPause = false;

	@Override
	public void onPause() {
		super.onPause();
		MobclickAgent.onPageEnd(this.getClass().getSimpleName());
		HiwifiBroadcastReceiver.removeListener(wifiEventHandler);
		stopCheckFlow();
		mLocationClient.stop();
		isPause = true;
	}

	@Override
	public void onStart() {
		super.onStart();
	}

	@Override
	public void onDestroy() {
		mAttempAccessPoint = null;
		super.onDestroy();
	}

	@Override
	public void onDestroyView() {
		super.onDestroyView();
	}

	@Override
	public void onStop() {
		super.onStop();
	}

	@Override
	public void onDetach() {
		super.onDetach();
		HiwifiBroadcastReceiver.removeListener(wifiEventHandler);
	}

	private void initView() {
		sub_container = (LinearLayout) getActivity().findViewById(
				R.id.sub_container);
		wifi_icon = (ImageView) getActivity().findViewById(R.id.wifi_icon_anim);
		current_net_status = (TextView) getActivity().findViewById(
				R.id.current_net_status);
		info_show_tx = (TextView) getActivity().findViewById(R.id.info_show_tx);
		anim_in = AnimationUtils.loadAnimation(getActivity(),
				R.anim.activity_uptodown_in);
		anim_out = AnimationUtils.loadAnimation(getActivity(),
				R.anim.activity_downtoup_out);
		anim_in.setAnimationListener(new AnimationListener() {

			@Override
			public void onAnimationStart(Animation animation) {
				info_show_tx.setVisibility(View.VISIBLE);
			}

			@Override
			public void onAnimationRepeat(Animation animation) {
			}

			@Override
			public void onAnimationEnd(Animation animation) {
				mHandler.postDelayed(new Runnable() {

					@Override
					public void run() {
						info_show_tx.startAnimation(anim_out);
					}
				}, 3000);
			}
		});
		anim_out.setAnimationListener(new AnimationListener() {

			@Override
			public void onAnimationStart(Animation animation) {
			}

			@Override
			public void onAnimationRepeat(Animation animation) {
			}

			@Override
			public void onAnimationEnd(Animation animation) {
				info_show_tx.setVisibility(View.GONE);
			}
		});
		alpha_layout = (LinearLayout) getActivity().findViewById(R.id.shadow);
		animation = new AlphaAnimation(0f, 1f);
		animation.setDuration(300);
		animation.setFillAfter(true);
		animation2 = new AlphaAnimation(1f, 0f);
		animation2.setDuration(300);
		animation2.setFillAfter(true);
		mListView = (MyListView) getActivity().findViewById(R.id.listView);

		refresh = (PullToRefreshView) getActivity().findViewById(R.id.refresh);
		main_container = (FrameLayout) getActivity().findViewById(
				R.id.main_container);
		myScrollView = (MyScrollView) getActivity().findViewById(
				R.id.myscrollview);
		top_layout = (ImageView) getActivity().findViewById(R.id.top_layout);
		top = (ImageView) getActivity().findViewById(R.id.top);
		myScrollView.setOnScrollListener(this);
		// 当布局的状态或者控件的可见性发生改变回调的接口
		getActivity().findViewById(R.id.parent_layout).getViewTreeObserver()
				.addOnGlobalLayoutListener(new OnGlobalLayoutListener() {

					@Override
					public void onGlobalLayout() {
						onScroll(myScrollView.getScrollY());
					}
				});
		connectedWifi = (LinearLayout) getActivity().findViewById(
				R.id.connected_wifi);
		topViewHolder.initByParent(connectedWifi);
		connectedWifi.measure(0, 0);
		topHeight = connectedWifi.getMeasuredHeight();
		// TODO

		wifiSwitch = (ImageView) getActivity().findViewById(R.id.wifi_switch);
		refreshWifiPullpwd = (ImageView) getActivity().findViewById(
				R.id.refresh_wifi_pullpwd);
		wifiState = (TextView) getActivity()
				.findViewById(R.id.wifiswitch_state);

	}

	@SuppressWarnings("deprecation")
	private void initEvent() {
		mListView.setOnItemClickListener(WifiListFragment.this);
		// mListView.setOnItemLongClickListener(WifiListFragment.this);
		// mListView.setOnItemLongClickListener(WifiListFragment.this);
		// connectedWifi.setOnLongClickListener(new OnLongClickListener() {
		//
		// @Override
		// public boolean onLongClick(View v) {
		// topLongClickListener();
		// return false;
		// }
		// });
		connectedWifi.setOnClickListener(this);

		// iv_action_more.setOnClickListener(this);
		refresh.setOnHeaderRefreshListener(this);
		refresh.onHeaderRefreshComplete(new Date().toLocaleString());
		// refreshButton.setOnClickListener(this);
		wifiSwitch.setOnClickListener(this);
		refreshWifiPullpwd.setOnClickListener(this);
		alpha_layout.setOnClickListener(this);
	}

	private synchronized void showProgressing(String text) {
		if (mConnectedAccessPoint != null) {
			current_net_status.setText(text);
		}
	}

	private synchronized void showProgressing(int stringId) {
		showProgressing(getString(stringId));
	}

	private Runnable connectTimeoutRunnable = new Runnable() {
		@Override
		public void run() {
			mHandler.sendEmptyMessage(DisplayListChangeHandler.actiontypeConnectTimeout);
		}
	};

	private Runnable openTimeoutRunnable = new Runnable() {
		@Override
		public void run() {
			// if (isEmptyPage()) {
			// showError(R.string.open_error);
			// // showSearchError();
			// }
		}
	};

	/**
	 * WIFI_STATE_DISABLING 0 WIFI_STATE_DISABLED 1 WIFI_STATE_ENABLING 2
	 * WIFI_STATE_ENABLED 3
	 */
	private void initWifiState() {
		WifiInfo wifiInfo = mWifiAdmin.getWifiManager().getConnectionInfo();
		if (wifiInfo != null && !TextUtils.isEmpty(wifiInfo.getBSSID())) {
			mAttempAccessPoint = mWifiAdmin.getAccessPointByBSSID(wifiInfo
					.getBSSID());
		}
		if (mWifiAdmin.connectedAccessPoint() == null) {
			// 之前有连接，后来在其它app里断开了
			if (mConnectedAccessPoint != null) {
				if (mConnectedAccessPoint.isConfiged()) {
					if (mConnectedAccessPoint.needPassword()) {
						if (mConnectedAccessPoint.hasRemotePassword()) {
							mConnectedAccessPoint
									.setConnectState(WifiConnectState.connectState_unlock);
						} else {
							mConnectedAccessPoint
									.setConnectState(WifiConnectState.connectState_local_restore);
						}
					} else {
						mConnectedAccessPoint
								.setConnectState(WifiConnectState.connectState_canconnect);
					}
				} else {
					mConnectedAccessPoint
							.setConnectState(WifiConnectState.connectState_unknown);
				}
			}
		}
		List<AccessPoint> displayedList = mWifiAdmin.getMergedAccessPoints();
		if (displayedList != null && displayedList.size() > 0) {
			onScanResultChanged(true);
			HWFLog.e(TAG, "initWifiState6");
		} else {
			if (mWifiAdmin.getWifiManager().isWifiEnabled()) {
				startScan();
				HWFLog.e(TAG, "initWifiState7");
			} else {
				onScanResultChanged(true);
				HWFLog.e(TAG, "initWifiState8");
			}
		}

	}

	public class ListAdapter extends BaseAdapter {

		LayoutInflater inflater;
		private int freeCount, needPwdCount;
		final int TYPE_TITLE_ONE = 0;
		final int TYPE_TITLE_TWO = 1;
		private List<AccessPoint> scanList;
		private List<AccessPoint> free = new ArrayList<AccessPoint>();
		private List<AccessPoint> needPassword = new ArrayList<AccessPoint>();
		int count = 0;
		private boolean isEmptyOfFree = false, isEmptyOfNeedPass = false;

		public ListAdapter() {
			this.inflater = LayoutInflater.from(getActivity());
		}

		public void setScanList(List<AccessPoint> scanList) {
			this.scanList = scanList;
			classifyWifiCategory(scanList);
		}

		public List<AccessPoint> getFree() {
			return free;
		}

		public void classifyWifiCategory(List<AccessPoint> scanList) {
			if (scanList != null && scanList.size() >= 0) {
				free.clear();
				needPassword.clear();
				for (int i = 0; i < scanList.size(); i++) {
					AccessPoint accessPoint = scanList.get(i);
					// if ((!accessPoint.needPassword() ||
					// accessPoint.isConfiged()
					// || accessPoint.hasPassword() ||
					// accessPoint.hasRemotePassword()
					// || accessPoint.hasUserCount())&&
					// !accessPoint.getDataModel().isPortal()) {
					if (accessPoint.isFree()) {
						free.add(accessPoint);
						// LogUtil.d("Tag:",
						// "free:" + accessPoint.getPrintableSsid()
						// + accessPoint.getSecurityType() + "");
					} else {
						needPassword.add(accessPoint);
						// LogUtil.d("Tag:",
						// "needpass:" + accessPoint.getPrintableSsid()
						// + accessPoint.getSecurityType() + "");
					}

				}

				// for (AccessPoint accessPoint : needPassword) {
				// LogUtil.d("TAG:", accessPoint.getPrintableSsid() + "  "
				// + accessPoint.getSecurityType());
				// }

				count = 0;
				freeCount = free.size();
				System.out.println("freeCount=" + freeCount);
				if (freeCount == 0) {
					freeCount = 1;
					// count += 1;
					isEmptyOfFree = true;
				} else {
					isEmptyOfFree = false;
				}
				count += freeCount;

				needPwdCount = needPassword.size();
				if (needPwdCount == 0) {
					needPwdCount = 0;
					isEmptyOfNeedPass = true;
				} else {
					isEmptyOfNeedPass = false;
				}
				count += needPwdCount;

			}
		}

		@Override
		public int getCount() {
			if (getActivity() == null) {
				return 0;
			}
			if (mDisplayedList != null && mDisplayedList.size() > 0) {
				return count;
			} else {
				return count;
			}

		}

		@Override
		public Object getItem(int position) {
			return getAccessPoint(position);
		}

		@Override
		public long getItemId(int position) {
			return position;
		}

		public int getItemViewType(int position) {
			if (position == 0) {
				return TYPE_TITLE_ONE;
			} else if (position == freeCount) {
				return TYPE_TITLE_TWO;
			} else if (position > 0 && position < freeCount) {
				return 2;
			} else if (position > freeCount && position < count) {
				return 3;
			}
			return -1;
		}

		private AccessPoint getAccessPoint(int position) {
			AccessPoint accessPoint = null;
			int type = getItemViewType(position);

			switch (type) {
			case 0:
			case 2:
				if (free.size() > 0 && position < free.size()) {
					accessPoint = free.get(position);
				}
				break;
			case 1:
			case 3:
				if (needPassword.size() > 0) {
					accessPoint = needPassword.get(position - freeCount);
				}
				break;

			default:
				break;
			}
			return accessPoint;
		}

		@Override
		public View getView(final int position, View convertView,
				ViewGroup parent) {
			ViewHolder viewHolder = null;
			final View view;

			AccessPoint accessPoint = null;
			int type = getItemViewType(position);

			switch (type) {
			case 0:
			case 2:
				if (free.size() > 0 && position < free.size()) {
					accessPoint = free.get(position);
				}
				break;
			case 1:
			case 3:
				if (needPassword.size() > 0) {
					accessPoint = needPassword.get(position - freeCount);
				}
				break;

			default:
				break;
			}
			if (convertView == null) {
				view = inflater.inflate(R.layout.item_wifi_list_item, parent,
						false);
				setViewHolder(view);
			} else if (((ViewHolder) convertView.getTag()).needInflate) {
				view = inflater.inflate(R.layout.item_wifi_list_item, parent,
						false);
				setViewHolder(view);
			} else {
				view = convertView;
			}
			viewHolder = (ViewHolder) view.getTag();

			if (type == TYPE_TITLE_ONE) {
				viewHolder.title.setVisibility(View.VISIBLE);
				viewHolder.category.setText("免费上网");
				viewHolder.titileIcon.setImageDrawable(Gl.Ct().getResources().getDrawable(R.drawable.list_key_icon));
				if (free.size() == 0) {
					viewHolder.noFree.setVisibility(View.VISIBLE);
					viewHolder.basicInfo.setVisibility(View.GONE);
					return view;
				} else {
					viewHolder.noFree.setVisibility(View.GONE);
					viewHolder.basicInfo.setVisibility(View.VISIBLE);
				}
			} else if (type == TYPE_TITLE_TWO) {
				viewHolder.title.setVisibility(View.VISIBLE);
				viewHolder.noFree.setVisibility(View.GONE);
				viewHolder.category.setText("需要密码");
				viewHolder.titileIcon.setImageDrawable(Gl.Ct().getResources().getDrawable(R.drawable.list_lock_icon));
				if (needPassword.size() == 0) {
					viewHolder.title.setVisibility(View.INVISIBLE);
					viewHolder.noFree.setVisibility(View.INVISIBLE);
					viewHolder.basicInfo.setVisibility(View.GONE);
					return view;
				} else {
					viewHolder.noFree.setVisibility(View.GONE);
					viewHolder.basicInfo.setVisibility(View.VISIBLE);
				}
			} else {
				viewHolder.title.setVisibility(View.GONE);
				viewHolder.noFree.setVisibility(View.GONE);
				viewHolder.basicInfo.setVisibility(View.VISIBLE);
			}
			if (accessPoint != null) {
				viewHolder.configByAccessPoint(accessPoint);
			}
			// viewHolder.actionMoreImageView
			// .setOnClickListener(new OnClickListener() {
			//
			// @Override
			// public void onClick(View v) {
			//
			// onItemLongClick(null, view, position, position);
			// }
			// });
			return view;

		}

		private boolean isEmptyOfFreeAp() {
			return isEmptyOfFree;
		}

		private boolean isEmptyOfANeedPass() {
			return isEmptyOfNeedPass;
		}

		private void setViewHolder(View convertView) {
			ViewHolder vh = new ViewHolder();
			vh.initByParent(convertView);
			convertView.setTag(vh);
		}

	}

	public class ViewHolder implements Observer {
		TextView textView, signaleStrength;
		ImageView iconImageView;
		ImageView lockStatusImageView;
		ImageView signalImageView;
		TextView stateTextView;
		View availabilityBgView;
		Drawable wifi5GDrawable;
		public boolean needInflate;
		LinearLayout basicInfo, title, noFree;
		ImageView titileIcon;
		TextView category;

		public void initByParent(View parentView) {
			this.textView = (TextView) parentView.findViewById(R.id.textView);
			this.iconImageView = (ImageView) parentView
					.findViewById(R.id.lockerImageView);
			this.signalImageView = (ImageView) parentView
					.findViewById(R.id.iv_action_more);

			this.lockStatusImageView = (ImageView) parentView
					.findViewById(R.id.iv_lock_state);
			this.lockStatusImageView = (ImageView) parentView
					.findViewById(R.id.iv_lock_state);
			this.stateTextView = (TextView) parentView
					.findViewById(R.id.wifi_state_textview);
			this.availabilityBgView = parentView
					.findViewById(R.id.availability_bg);

			this.titileIcon = (ImageView) parentView
					.findViewById(R.id.item_titile_icon);
			this.category = (TextView) parentView
					.findViewById(R.id.item_category);

			this.title = (LinearLayout) parentView
					.findViewById(R.id.category_title);
			this.basicInfo = (LinearLayout) parentView
					.findViewById(R.id.basic_info);
			this.noFree = (LinearLayout) parentView
					.findViewById(R.id.no_free_contain);
			this.signaleStrength = (TextView) parentView
					.findViewById(R.id.signal_strength);
		}

		public void configByAccessPoint(AccessPoint accessPoint) {
			if (accessPoint == null) {
				HWFLog.d(TAG, "error, no access point provide");
				return;
			}

			if (accessPoint != null) {
				accessPoint.deleteObservers();
				accessPoint.addObserver(this);
				this.textView.setText(accessPoint.getPrintableSsid());
				this.signalImageView.setImageDrawable(accessPoint
						.getSignalDrawable());
				// this.signalImageView.setImageLevel(WifiManager
				// .calculateSignalLevel(
				// accessPoint.getScanResult().level, 5));
				signalImageView.setImageLevel(accessPoint
						.calculateSignalLevel());
				this.lockStatusImageView.setVisibility(View.INVISIBLE);
				if (accessPoint.getLockDrawable() != null) {
					this.lockStatusImageView.setImageDrawable(accessPoint
							.getLockDrawable());
					this.lockStatusImageView.setVisibility(View.VISIBLE);
				} else {
					this.lockStatusImageView.setVisibility(View.INVISIBLE);
				}
				configItemState(accessPoint);
			}
		}

		protected void configItemState(AccessPoint accessPoint) {
			// if (accessPoint.is5g()) {
			// if (wifi5GDrawable == null && getActivity() != null) {
			// wifi5GDrawable = Gl.Ct().getResources()
			// .getDrawable(R.drawable.icon_5g_unlink);
			// wifi5GDrawable.setBounds(0, 0,
			// wifi5GDrawable.getMinimumWidth(),
			// wifi5GDrawable.getMinimumHeight());
			// }
			// textView.setCompoundDrawables(null, null, wifi5GDrawable, null);
			// } else {
			// textView.setCompoundDrawables(null, null, null, null);
			// }
			updateStatus(accessPoint);
		}

		public void updateStatus(AccessPoint accessPoint) {
			if (TextUtils.isEmpty(accessPoint.getConnectStateString())) {
				stateTextView.setVisibility(View.GONE);
			} else {
				stateTextView.setVisibility(View.VISIBLE);
				stateTextView.setText(accessPoint.getConnectStateString());
				stateTextView.setTextColor(accessPoint.getConnectStateColor());
			}
			LogUtil.d("Tag:", "ssid:" + accessPoint.getPrintableSsid()
					+ " state:" + accessPoint.getConnectStateString());
			signaleStrength.setText(accessPoint.getSignalPersent() + "%");
		}

		@Override
		public void update(Observable observable, Object data) {
			AccessPoint accessPoint = (AccessPoint) data;
			if (getActivity() != null) {
				updateStatus(accessPoint);
			}
		}

	}

	public class TopViewHolder {
		// private ViewGroup leftTimeContainerGroup;
		// private TextView timeLefTextView;
		private TextView joinedStatus;
		private TextView joinedBssid;

		private ImageView stateImageView;
		private int leftTime = 0;
		private AccessPoint mAccessPoint;
		private TextView currentTraffic, currentSignal;

		// private LinearLayout checkPassword;
		// private RelativeLayout successOnlineWifi;

		public void initByParent(View parentView) {

			this.stateImageView = (ImageView) parentView
					.findViewById(R.id.joined_state_imageview);
			// this.leftTimeContainerGroup = (ViewGroup) parentView
			// .findViewById(R.id.release_time_container);
			// this.tipsTextView = (TextView) parentView
			// .findViewById(R.id.release_time);
			this.joinedStatus = (TextView) parentView
					.findViewById(R.id.joined_wifi_state);
			this.joinedBssid = (TextView) parentView
					.findViewById(R.id.joined_wifi_bssid);
			this.currentTraffic = (TextView) parentView
					.findViewById(R.id.current_traffic);
			this.currentSignal = (TextView) parentView
					.findViewById(R.id.current_signal);
			// this.checkPassword = (LinearLayout) parentView
			// .findViewById(R.id.check_password_container);
			// this.successOnlineWifi = (RelativeLayout) parentView
			// .findViewById(R.id.success_online_wifi);
			//
			// setListener();

		}

		private void setListener() {

			successOnlineWifi.setOnClickListener(new OnClickListener() {

				@Override
				public void onClick(View v) {
					if (!isLongClick) {

						if (mConnectedAccessPoint != null
								&& mConnectedAccessPoint.isStateConnected()) {
							isResume = true;
							// TODO
							// Intent intent = new Intent(getActivity(),
							// SuccessOnlineActivity.class);
							// intent.putExtra("accessPoint",
							// mConnectedAccessPoint.getScanResult());
							// startActivity(intent);

						}
					}
				}
			});
		}

		private CmccStateEventHandler cmccStateEventHandler = new CmccStateEventHandler() {
			@Override
			public void onCmccStateLogout(int leftTime, Boolean isVip) {
				topViewHolder.leftTime = leftTime;
				refreshTimerView();
			}

			@Override
			public void onCmccStateLogin(int leftTime, Boolean isVip) {
				topViewHolder.leftTime = leftTime;
				refreshTimerView();
			}

			public void onCmccStateClick(int leftTime, Boolean isVip) {
				topViewHolder.leftTime = leftTime;
				refreshTimerView();
			}

			@Override
			public void onCmccStateVipChanged(int leftTime, Boolean isVip) {
				topViewHolder.leftTime = leftTime;
				refreshTimerView();
			}
		};

		public void startTimer() {
			CmccStateBroadcastReceiver.addListener(cmccStateEventHandler);
		}

		public void stopTimer() {
			CmccStateBroadcastReceiver.removeListener(cmccStateEventHandler);
		}

		public void showEarntime() {
			// this.foreverFreeTx.setVisibility(View.GONE);
			// this.leftTimeContainerGroup.setVisibility(View.VISIBLE);
			// this.timeLefTextView.setVisibility(View.INVISIBLE);
			// this.tipsTextView.setVisibility(View.VISIBLE);
			//
			// this.tipsTextView.setText(getString(R.string.click_get_more_time));
			// this.tipsTextView.setClickable(true);
			// this.tipsTextView.getPaint().setFlags(Paint.UNDERLINE_TEXT_FLAG);
		}

		public void setCurrentTracffic() {
			double f1;
			float f = (float) (currentFlow / 1024.0);
			BigDecimal bg = new BigDecimal(f);
			if (currentFlow < 1024) {
				f1 = bg.setScale(2, BigDecimal.ROUND_HALF_UP).doubleValue();
				currentTraffic.setText("实时 " + f1 + "k/s");
			} else if (currentFlow < 10240) {
				f1 = bg.setScale(1, BigDecimal.ROUND_HALF_UP).doubleValue();
				currentTraffic.setText("实时 " + f1 + "k/s");
			} else {
				currentTraffic.setText("实时 " + (int) f + "k/s");
			}

		}

		public void setCurrentSignal(AccessPoint accessPoint) {
			currentSignal.setText("信号 " + accessPoint.getSignalPersent() + "%");
		}

		public void refreshTimerView() {
			if (!connectedWifi.isShown()) {
				stopTimer();
				return;
			}
			if (mAccessPoint.supportAutoAuth()) {
				if (Gl.cmccHasLeftTime()
						&& mAccessPoint.getChangeState() == WiFiChangeState.connectState_connected) {
					if (Gl.GlConf.cmccIsVip()) {
						// this.foreverFreeTx.setVisibility(View.VISIBLE);
						// this.leftTimeContainerGroup.setVisibility(View.GONE);
					} else {

						if (leftTime > 0) {
							// this.foreverFreeTx.setVisibility(View.GONE);
							// this.leftTimeContainerGroup
							// .setVisibility(View.VISIBLE);
							// this.tipsTextView.setText("剩余");
							// this.tipsTextView.getPaint().setFlags(
							// this.tipsTextView.getPaint().getFlags()
							// & ~Paint.UNDERLINE_TEXT_FLAG);
							// this.tipsTextView.setClickable(false);
							// this.timeLefTextView.setVisibility(View.VISIBLE);
							// this.timeLefTextView
							// .setText(new DecimalFormat("00")
							// .format(leftTime / 60)
							// + "分"
							// + new DecimalFormat("00")
							// .format(leftTime % 60)
							// + "秒");
						} else {
							stopTimer();
							showEarntime();
						}
					}
				} else {
					stopTimer();
					showEarntime();
				}

			} else {
				// this.leftTimeContainerGroup.setVisibility(View.GONE);
				// this.foreverFreeTx.setVisibility(View.GONE);
			}
			if (!mWifiAdmin.isConnected()) {
				stopTimer();
			}
		}

		public void configByAccessPoint(AccessPoint accessPoint) {
			mAccessPoint = accessPoint;

			// super.configByAccessPoint(accessPoint);
			this.joinedBssid.setText(accessPoint.getPrintableSsid());
			// if(accessPoint!=null ){ // 需要密码连接状态修改
			// accessPoint.getLockDrawable();
			// }
			if (TextUtils.isEmpty(accessPoint.getConnectStateString())) {
				this.joinedStatus.setVisibility(View.GONE);
			} else {
				this.joinedStatus.setText(accessPoint.getConnectStateString());
				this.joinedStatus.setVisibility(View.VISIBLE);
			}
			this.stateImageView.setImageDrawable(accessPoint
					.getChangeStateDrawable());
			setCurrentSignal(accessPoint);
			// setCurrentTracffic(accessPoint);

			// this.signalImageView.setImageDrawable(accessPoint
			// .getSignalDrawable());
			// this.signalImageView.setImageLevel(WifiManager
			// .calculateSignalLevel(
			// accessPoint.getScanResult().level, 5));
			//
			// if (accessPoint.getLockDrawable() != null) {
			// this.lockStatusImageView.setImageDrawable(accessPoint
			// .getLockDrawable());
			// this.lockStatusImageView.setVisibility(View.VISIBLE);
			// } else {
			// this.lockStatusImageView.setVisibility(View.INVISIBLE);
			// }

			// TODO

			// refreshTimerView();
			// // this.lockStatusImageView.setVisibility(View.INVISIBLE);
			// if (mWifiAdmin.isConnected()) {
			// startTimer();
			// }
		}

		protected void configItemState(AccessPoint accessPoint) {
			if (getActivity() == null) {
				return;
			}
			// if (accessPoint.is5g()) {
			// if (wifi5GDrawable == null) {
			// wifi5GDrawable = Gl.Ct().getResources()
			// .getDrawable(R.drawable.icon_5g);
			// wifi5GDrawable.setBounds(0, 0,
			// wifi5GDrawable.getMinimumWidth(),
			// wifi5GDrawable.getMinimumHeight());
			// }
			// MobclickAgent.onEvent(getActivity(), "stat_wifi_is5g",
			// getResources().getString(R.string.stat_boolean_yes));
			// textView.setCompoundDrawables(null, null, wifi5GDrawable, null);
			// } else {
			// textView.setCompoundDrawables(null, null, null, null);
			// MobclickAgent.onEvent(getActivity(), "stat_wifi_is5g",
			// getResources().getString(R.string.stat_boolean_no));
			// }
			//
			// stateTextView.setText(accessPoint.getConnectStateString());
			// stateTextView.setTextColor(accessPoint.getConnectStateColor());
			// updateStatus(accessPoint);
			// super.configItemState(accessPoint);
		}

		public void updateStatus(AccessPoint accessPoint) {
			// super.updateStatus(accessPoint);
			stateImageView.setVisibility(View.VISIBLE);
			stateImageView.setImageDrawable(accessPoint
					.getConnectStateDrawable());
			if (accessPoint.getChangeState() == WiFiChangeState.connectState_connecting) {
				Animation rotateAnimation = AnimationUtils
						.loadAnimation(WifiListFragment.this.getActivity(),
								R.anim.rotate_anim);
				stateImageView.startAnimation(rotateAnimation);
			} else {
				stateImageView.clearAnimation();
			}
		}
	}

	private void startConnectInNewThread() {
		int ret = -1;
		HWFLog.d(TAG, "startConnect");
		assert (mAttempAccessPoint != null);
		// 已连接过
		if (mAttempAccessPoint.isConfiged()
				&& mAttempAccessPoint.isConfigedPasswordCorrect()) {
			if (mAttempAccessPoint.needPassword()) {
				ret = mWifiAdmin.addConfigedNetwork(this.mAttempAccessPoint);
				if (mAttempAccessPoint.isConfiged()) {
					MobclickAgent
							.onEvent(
									getActivity(),
									"stat_wifi_password_type",
									Gl.Ct().getResources()
											.getString(
													R.string.stat_wifi_password_type_is_stored));
				}

				HWFLog.e(TAG, "已经连接过，直接连接");
			} else {
				HWFLog.e(TAG, "不需要密码，直接连接");
				MobclickAgent.onEvent(
						getActivity(),
						"stat_wifi_password_type",
						Gl.Ct().getResources().getString(
								R.string.stat_wifi_password_type_is_noneed));
				ret = mWifiAdmin.addNetwork(mWifiAdmin.createWifiConf(
						this.mAttempAccessPoint, null, mAttempAccessPoint
								.getDataModel().getPassword(false)));

			}
		} else {
			if (this.mAttempAccessPoint.isDotxType()) {
				// TODO 802.1X {username & password}
				AccessPointModel accessPointModel = mAttempAccessPoint
						.getDataModel();
				ret = mWifiAdmin.addNetwork(mWifiAdmin.createWifiConf(
						this.mAttempAccessPoint,
						accessPointModel.getUserName(),
						accessPointModel.getPassword(false)));

			} else {
				ret = mWifiAdmin.addNetwork(mWifiAdmin.createWifiConf(
						this.mAttempAccessPoint, null, mAttempAccessPoint
								.getDataModel().getPassword(false)));
			}
			if (mAttempAccessPoint.getDataModel().passwordType == PasswordSource.PasswordSourceLocal
					.ordinal()) {
				MobclickAgent.onEvent(
						getActivity(),
						"stat_wifi_password_type",
						Gl.Ct().getResources().getString(
								R.string.stat_wifi_password_type_is_useinput));
			} else {
				MobclickAgent.onEvent(
						getActivity(),
						"stat_wifi_password_type",
						Gl.Ct().getResources().getString(
								R.string.stat_wifi_password_type_is_useserver));
			}

			HWFLog.e(TAG, "需要密码，使用本地密码连接"
					+ mAttempAccessPoint.getDataModel().getPassword(false));
		}
		if (ret == -1) {
			showError("连接wifi失败");
		} else {
			mHandler.sendEmptyMessage(DisplayListChangeHandler.actiontypeStartConnect);
			mHandler.postDelayed(connectTimeoutRunnable, detectTimeout);
		}
		HWFLog.i(TAG, "ret" + ret);
	}
 
	private void startConnect() {
		mHandler.sendEmptyMessage(DisplayListChangeHandler.msg_connectWifi);
	}

	private void showError(int resId) {
		showError(getString(resId));
	}

	private void showError(String errorMsg) {
		ToastUtil.makeImageToast(getActivity(), ToastUtil.ERROR, errorMsg,
				null, Toast.LENGTH_LONG).show();
	}

	private void showSuccess(String okMsg) {
		ToastUtil.makeImageToast(getActivity(), ToastUtil.SUCCESS, okMsg, null,
				Toast.LENGTH_LONG).show();
	}

	// @Override
	// public boolean onItemLongClick(AdapterView<?> parent, final View view,
	// final int position, long id) {
	// HWFLog.i(TAG, "long click on position");
	// CustomDialog dialog = new CustomDialog.Builder(getActivity())
	// .setMessage("忽略此网络么?")
	// .setPositiveButton(R.string.btn_ok,
	// new DialogInterface.OnClickListener() {
	//
	// @Override
	// public void onClick(DialogInterface dialog,
	// int which) {
	// AccessPoint accessPoint = WifiListFragment.this.mDisplayedList
	// .get(position);
	// accessPoint.setIsIgnored(true);
	// mWifiAdmin.removeConfig(accessPoint
	// .getScanResult().SSID);
	// deleteCell(view, position, false,false);
	// }
	// })
	// .setNegativeButton(R.string.btn_cancel,
	// new DialogInterface.OnClickListener() {
	//
	// @Override
	// public void onClick(DialogInterface dialog,
	// int which) {
	//
	// }
	// }).create();
	// dialog.show();
	// return true;
	// }

	public interface WifiListListener {
		public void onMenuClicked();

	}

	public void intentToShowApplist() {

	}

	public void intentToTest() {

	}

	// private IcallBackListener icallBackListener;

	// public interface IcallBackListener {
	// public void successOnline(AccessPoint accessPoint);
	// }
	//
	// @Override
	// public void onAttach(Activity activity) {
	// try {
	// icallBackListener = (IcallBackListener) activity;
	// } catch (Exception e) {
	// e.printStackTrace();
	// // throw new ClassCastException(activity.toString()
	// // + " must implement OnResolveTelsCompletedListener");
	// }
	// super.onAttach(activity);
	// }

	@Override
	public void onClick(View v) {
		try {
			switch (v.getId()) {
			case R.id.dlg_close:
				passwordDialog.dismiss();
				break;
			case R.id.btn_cancel: {
				if (detectMaskDialog != null && detectMaskDialog.isShowing()) {
					detectMaskDialog.dismiss();
				}
			}
				break;
			case R.id.btn_password_connect:
				// TODO 表单验证
				if (!isFormValid(mAttempAccessPoint)) {
					return;
				}
				// 802.1x类型 输入username & password
				if (mAttempAccessPoint.isDotxType()) {
					// TODO
					mAttempAccessPoint.getDataModel().setUserCount(
							mPasswordDialogViewHolder.edit_username.getText()
									.toString(),
							mPasswordDialogViewHolder.passwordEditText
									.getText().toString(), false,
							PasswordSource.PasswordSourceLocal);
				} else {
					// mAttempAccessPoint.getDataModel().setPassword(
					// mPasswordDialogViewHolder.passwordEditText
					// .getText().toString(), false,
					// PasswordSource.PasswordSourceLocal);
					mAttempAccessPoint.getDataModel().setUserCount(
							"",
							mPasswordDialogViewHolder.passwordEditText
									.getText().toString(), false,
							PasswordSource.PasswordSourceLocal);
				}
				if(mConnectedAccessPoint!=null){
					mConnectedAccessPoint.stopWebTest();
				}
				CLICKCODE = OPEN;
				startConnect();
				closeInputPop();
				// passwordDialog.dismiss();
				break;
			case R.id.btn_password_cancel:
				closeInputPop();
				// if(pop!=null && pop.isShowing()){
				// pop.dismiss();
				// }
				break;
			// case R.id.refresh_button:
			//
			// if (mWifiAdmin.getWifiManager().isWifiEnabled()) {
			// startScan();
			// } else {
			// mWifiAdmin.openNetCard();
			// mHandler.postDelayed(openTimeoutRunnable, openWifiTimeout);
			// }
			// showSearching();
			// break;
			case R.id.wifi_switch:
				if (mWifiAdmin.getWifiManager().isWifiEnabled()) {
					wifiSwitch.setBackgroundResource(R.drawable.icon_wifioff);
					wifiState.setText("已经关闭");
					wifiState.setTextColor(Gl.Ct().getResources().getColor(
							R.color.wifilist_itembg_detecting));
					mWifiAdmin.closeNetCard();
				}
				Intent intent = new Intent(getActivity(),
						WiFiOperateActivity.class);
				intent.putExtra(WiFiOperateActivity.KEYTAG, "main");
				startActivity(intent);
				break;
			case R.id.refresh_wifi_pullpwd:
				if (mWifiAdmin.isWifiEnable()) {
					refresh.clickToRefresh();
					scanTriggerByPull = true;
					startScan();
				}
				break;
			case R.id.shadow:
				closeInputPop();
				break;
			case R.id.connected_wifi:
				// TODO 是否要判断已经连上网
				if (mConnectedAccessPoint != null
						&& mConnectedAccessPoint.isConnected()) {
					if (mConnectedAccessPoint.isShowPortal()) {
						enterPortal(mConnectedAccessPoint);
					} else {
						MobclickAgent.onEvent(
								getActivity(),
								"enter_wifi_detail",
								Gl.Ct().getResources().getString(
										R.string.enter_wifi_detail));
						Intent lookPwd = new Intent(getActivity(),
								CheckPasswordActivity.class);
						startActivity(lookPwd);
						getActivity().overridePendingTransition(
								R.anim.activity_uptodown_in, R.anim.shit);
					}
				}
				break;
			default:
				break;
			}
		} catch (Exception e) {
			e.printStackTrace();
		}

	}

	private boolean isFormValid(AccessPoint mAttempAccessPoint) {
		String userName = mPasswordDialogViewHolder.edit_username.getEditText()
				.getText().toString();
		String password = mPasswordDialogViewHolder.passwordEditText.getText()
				.toString();
		if (mAttempAccessPoint.isDotxType()) {
			if (TextUtils.isEmpty(userName)) {
				mPasswordDialogViewHolder.error_info.setText(Gl.Ct().getResources()
						.getString(R.string.empty_username));
				return false;
			} else if (TextUtils.isEmpty(password)) {
				mPasswordDialogViewHolder.error_info.setText(Gl.Ct().getResources()
						.getString(R.string.empty_pass));
				return false;
			} else if (!mAttempAccessPoint.isPasswordValid(password)) {
				mPasswordDialogViewHolder.error_info.setText(Gl.Ct().getResources()
						.getString(R.string.invalid_count));
				return false;
			}
		} else {
			LogUtil.d(TAG, mAttempAccessPoint.getPrintableSsid());
			if (TextUtils.isEmpty(password)) {
				mPasswordDialogViewHolder.error_info.setText(Gl.Ct().getResources()
						.getString(R.string.empty_pass));
				return false;
			} else if (!mAttempAccessPoint.isPasswordValid(password)) {
				mPasswordDialogViewHolder.error_info.setText(Gl.Ct().getResources()
						.getString(R.string.invalid_pass));
				LogUtil.d(TAG, mAttempAccessPoint.getPrintableSsid() + " 不符合");
				return false;
			}else {
				return true;
			}
		}
		return true;
	}

	static final int ANIMATION_DURATION = 200;

	AnimationListener al;
	private RelativeLayout successOnlineWifi;

	private void deleteCell(final View v, final AccessPoint point,
			final boolean isScan, boolean isShow) {
		al = new AnimationListener() {

			@Override
			public void onAnimationEnd(Animation arg0) {
				mDisplayedList.remove(point);
				ViewHolder vh = (ViewHolder) v.getTag();
				vh.needInflate = true;
				listAdapter.setScanList(mDisplayedList);
				listAdapter.notifyDataSetChanged();
				if (isScan) {
					startConnect();
				}
				// animHideShowView(connectedWifi, null, 60, true,
				// ANIMATION_DURATION);
			}

			@Override
			public void onAnimationRepeat(Animation animation) {
			}

			@Override
			public void onAnimationStart(Animation animation) {
				// animHideShowView(connectedWifi, null, 0, false,
				// ANIMATION_DURATION);
			}
		};

		animHideShowView(v, al, 0, isShow, ANIMATION_DURATION);
	}

	/**
	 * 防止动画执行过程中背景空白，屏幕跳；边执行，边控制view高度
	 * 
	 * 欢迎加入QQ讨论群：88130145
	 * 
	 * @Description:
	 * @param v
	 *            要执行动画的View
	 * @param al
	 *            动画监听器，可为空
	 * @param measureHeight
	 *            view的实际高度，可不传，但显示时需要保证此高度不为0
	 * @param show
	 *            是显示还是隐藏
	 * @param ainmTime
	 *            动画时间
	 * @see:
	 * @since:
	 */
	public static void animHideShowView(final View v, AnimationListener al,
			int measureHeight, final boolean show, int ainmTime) {

		if (measureHeight == 0) {
			measureHeight = v.getMeasuredHeight();
		}
		final int heightMeasure = measureHeight;
		Animation anim = new Animation() {

			@Override
			protected void applyTransformation(float interpolatedTime,
					Transformation t) {

				if (interpolatedTime == 1) {

					v.setVisibility(show ? View.VISIBLE : View.GONE);
				} else {
					int height;
					if (show) {
						height = (int) (heightMeasure * interpolatedTime);
					} else {
						height = heightMeasure
								- (int) (heightMeasure * interpolatedTime);
					}
					v.getLayoutParams().height = height;
					v.requestLayout();
				}
			}

			@Override
			public boolean willChangeBounds() {
				return true;
			}
		};

		if (al != null) {
			anim.setAnimationListener(al);
		}
		anim.setDuration(ainmTime);
		v.startAnimation(anim);
	}

	@Override
	public void onScroll(int scrollY) {
		int mBuyLayout2ParentTop = Math.max(scrollY, mListView.getTop());
		if (mBuyLayout2ParentTop <= 0) {
			top_layout.setVisibility(View.INVISIBLE);
		} else {
			top_layout.setVisibility(View.VISIBLE);
		}
		top_layout.layout(0, mBuyLayout2ParentTop, top_layout.getWidth(),
				mBuyLayout2ParentTop + top_layout.getHeight());
	}

	@Override
	public void onHeaderRefresh(PullToRefreshView view) {
		MobclickAgent.onEvent(
				getActivity(),
				"stat_list_refresh_type",
				Gl.Ct().getResources().getString(
						R.string.stat_list_refresh_type_is_pulldown));
		scanTriggerByPull = true;
		startScan();
	}

	private Boolean startPullPassword(List<AccessPoint> list) {
		// if (list == null || list.size() == 0) {
		// refresh.onHeaderRefreshComplete(new Date().toLocaleString());
		// return false;
		// }
		// if (refresh != null && scanTriggerByPull) {
		// refresh.setMessage("已成功获取" + mDisplayedList.size() + "个WiFi");
		// }
		RequestFactory.getPasswords(getActivity(), this, list);
		return true;
	}

	// private void showList() {
	// viewSwitcher.setDisplayedChild(1);
	// }
	//
	// private void showEmpty() {
	// viewSwitcher.setDisplayedChild(0);
	// showSearchError();
	// }

	// private void showSearchError() {
	// searchingImageView.setBackgroundResource(R.drawable.nowifi_sorry);
	// AnimationDrawable animation = (AnimationDrawable) eyeImageView
	// .getBackground();
	// if (animation != null) {
	// animation.stop();
	// }
	// }

	// private void showSearching() {
	// searchingImageView.setBackgroundResource(R.drawable.nowifi_searching);
	// AnimationDrawable animation = (AnimationDrawable) eyeImageView
	// .getBackground();
	// animation.stop();
	// animation.start();
	// }

	// private Boolean isEmptyPage() {
	// return viewSwitcher.getDisplayedChild() == 0;
	// }

	@Override
	public void onSaveInstanceState(Bundle outState) {
		super.onSaveInstanceState(outState);
	}

	public void onBackPressed() {
	}

	@Override
	public void onItemClick(AdapterView<?> arg0, View arg1, int arg2, long arg3) {
		itemAction(arg1, arg2);
	}

	private void itemAction(View arg1, int arg2) {
		// if (!isLongClick) {
		isPause = false;
		isConnectedByUser = true;
		MobclickAgent.onEvent(getActivity(), "lick_wifi_item", Gl.Ct().getResources()
				.getString(R.string.stat_wifi_item));
		// this.mAttempAccessPoint = mDisplayedList.get(arg2);
		this.mAttempAccessPoint = (AccessPoint) listAdapter.getItem(arg2);
		// change wifi clear speed
		if (this.mAttempAccessPoint == null) {
			return;
		}
		this.mAttempAccessPoint.setHasWebTested(false);
		this.mAttempAccessPoint.setSpeed(0);
		ScanResult scanRet = this.mAttempAccessPoint.getScanResult();
		Log.e(TAG, "click item:ssid " + scanRet.SSID + " capabilities "
				+ scanRet.capabilities);

		LogUtil.d("Tag:", mWifiAdmin.getAccessPointByBSSID(scanRet.BSSID) + " ");
		if (mWifiAdmin.getAccessPointByBSSID(scanRet.BSSID) == null) {
			LogUtil.d("Tag:", "无此项");
			showError(Gl.Ct().getResources().getString(R.string.list_not_in_range));
			return;
		}
		currentView = arg1;
		position = arg2;
		if (mAttempAccessPoint.canAutoConnnect()) {
			if(mConnectedAccessPoint!=null){
				mConnectedAccessPoint.stopWebTest();
			}
			CLICKCODE = OPEN;
			startConnect();
			// deleteCell(arg1, this.mAttempAccessPoint, true,false);
			return;
		} else {
			showPasswordDialog(mAttempAccessPoint);
		}
		// }
	}

	@Override
	public void onStart(RequestTag tag, Code code) {
		if (code != Code.ok) {
			MobclickAgent.onEvent(getActivity(), "stat_getpwd_stats",
					Gl.Ct().getResources().getString(R.string.stat_boolean_no));
			mHandler.postDelayed(new Runnable() {
				@Override
				public void run() {
					refresh.onHeaderRefreshComplete(new Date().toLocaleString());
				}
			}, 2000);
		}
	}

	@Override
	public void onSuccess(RequestTag tag, ServerResponseParser responseParser) {
		JSONArray response = null;
		int count = 0;
		if (responseParser.originResponse != null) {
			AccessPoint ap = null;
			try {
				response = responseParser.originResponse
						.getJSONArray(RequestManager.key_wrap);
				LogUtil.d("Password:", response.toString());
				if (response.length() == 0) {
					MobclickAgent.onEvent(getActivity(), "stat_getpwd_stats",
							Gl.Ct().getResources().getString(R.string.stat_boolean_no));
				} else {
					MobclickAgent
							.onEvent(
									getActivity(),
									"stat_getpwd_stats",
									Gl.Ct().getResources().getString(
											R.string.stat_boolean_yes));
				}
				count = response.length();
				for (int i = 0; i < response.length(); i++) {
					ap = mWifiAdmin.getAccessPointByBSSID(response
							.getJSONObject(i).getString("b"));
					if (ap != null) {
						// ap.getDataModel().setPassword(
						// response.getJSONObject(i).getString("p"), true,
						// PasswordSource.PasswordSourceRemote);
						if (ap.isDotxType()
								&& (TextUtils.isEmpty(response.getJSONObject(i)
										.getString("auth_username")) || TextUtils
										.isEmpty(response.getJSONObject(i)
												.getString("p")))) {
							count -= 1;
						}
						ap.getDataModel().setUserCount(
								response.getJSONObject(i).getString(
										"auth_username"),
								response.getJSONObject(i).getString("p"), true,
								PasswordSource.PasswordSourceRemote);
						ap.getDataModel()
								.setIsPortal(
										response.getJSONObject(i).getInt(
												"is_portal") == AccessPointModel.PortalTrue);
						ap.getDataModel().syncDownTime = (int) new Date()
								.getTime() / 1000;
						ap.sync();
					}
				}
				listAdapter.setScanList(mDisplayedList);
				listAdapter.notifyDataSetChanged();

			} catch (JSONException e) {
				e.printStackTrace();
			}
		}
		if (scanTriggerByPull) {
			scanTriggerByPull = false;
			final int length = count;
			mHandler.postDelayed(new Runnable() {
				@Override
				public void run() {
					if (length == 0) {
						refresh.setMessage("要更多密码？换地方刷刷");
					} else {
						refresh.setMessage("获取了" + length + "个小极共享WiFi");
						info_show_tx.setText("获取了" + length + "个小极共享WiFi");
						info_show_tx.startAnimation(anim_in);
					}
				}
			}, 1000);
			mHandler.postDelayed(new Runnable() {

				@Override
				public void run() {
					refresh.onHeaderRefreshComplete(new Date().toLocaleString());
				}
			}, 2000);
		}
	}

	@Override
	public void onFailure(RequestTag tag, Throwable error) {
		refresh.onHeaderRefreshComplete(new Date().toLocaleString());
	}

	@Override
	public void onFinish(RequestTag tag) {
	}

	public void getCurrentTraffic() {

	}

	private Handler handler = new Handler() {
		public void handleMessage(Message msg) {
			if (msg.what == CURRENT_TRACFFIC) {
				topViewHolder.setCurrentTracffic();
			}
		};
	};
	private Timer flowTimer;
	private long last_flow, last_mobile_flow;
	private long currentFlow;
	private boolean isMobile;

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
				if (isSurportTraffic()) {
					handler.sendEmptyMessage(CURRENT_TRACFFIC);
				}
			}

		}, 0, 1000);

	}

	private void stopCheckFlow() {
		flowTimer.cancel();
		flowTimer = null;
	}

	public boolean isSurportTraffic() {
		if (TrafficStats.getTotalRxBytes() + TrafficStats.getTotalRxBytes() == -1) {
			return false;
		}
		return true;
	}

	private long currentFlow() {
		long current_mobile_flow = TrafficStats.getMobileRxBytes();
		long current_total_flow = TrafficStats.getTotalRxBytes();
		// + TrafficStats.getTotalTxBytes();
		long mobile_speed = current_mobile_flow - last_mobile_flow;
		long total_speed = current_total_flow - last_flow;
		if (mobile_speed >= total_speed * 0.9) {
			isMobile = true;
		}
		last_flow = current_total_flow;
		last_mobile_flow = current_mobile_flow;
		return (total_speed - mobile_speed);
	}

	// 选择第一个wifi连接
	public void pickOneToConnect() {
		HWFLog.e(TAG, "try pick one to connect");
		if (WifiAdmin.sharedInstance().getActiveAccessPoint() == null) {
			List<AccessPoint> mAvailableList = listAdapter.getFree();
			if (mAvailableList != null && mAvailableList.size() > 0) {
				mAttempAccessPoint = mAvailableList.get(0);
				HWFLog.e(TAG, "has avaiable wifi,go to connect");
				startConnect();
			}
		}
	}

	public class MyLocationListener implements BDLocationListener {
		@Override
		public void onReceiveLocation(BDLocation location) {
			if (location == null)
				return;
			StringBuffer sb = new StringBuffer(256);
			sb.append("time : ");
			sb.append(location.getTime());
			sb.append("\nerror code : ");
			sb.append(location.getLocType());
			sb.append("\nlatitude : ");
			sb.append(location.getLatitude());
			sb.append("\nlontitude : ");
			sb.append(location.getLongitude());
			sb.append("\nradius : ");
			sb.append(location.getRadius());
			// 精度高，可记录
			if (location.getLocType() == BDLocation.TypeGpsLocation) {
				sb.append("\nspeed : ");
				sb.append(location.getSpeed());
				sb.append("\nsatellite : ");
				sb.append(location.getSatelliteNumber());
				Iterator<AccessPoint> iterator = WifiAdmin.sharedInstance()
						.getMergedAccessPoints().iterator();
				while (iterator.hasNext()) {
					AccessPoint ap = iterator.next();
					ap.getDataModel().longitude = (float) location
							.getLongitude();
					ap.getDataModel().latitude = (float) location.getLatitude();
					ap.getDataModel().syncUpTime = 0;
					ap.sync();
				}
			} else if (location.getLocType() == BDLocation.TypeNetWorkLocation) {
				// 精度不够，没有数据才写
				sb.append("\naddr : ");
				sb.append(location.getAddrStr());
				Iterator<AccessPoint> iterator = WifiAdmin.sharedInstance()
						.getMergedAccessPoints().iterator();
				while (iterator.hasNext()) {
					AccessPoint ap = iterator.next();
					if (ap.getDataModel().longitude == 0) {
						ap.getDataModel().longitude = (float) location
								.getLongitude();
						ap.getDataModel().latitude = (float) location
								.getLatitude();
						ap.getDataModel().syncUpTime = 0;
					}

					ap.sync();
				}
			}

			LogUtil.d(TAG, sb.toString());
		}
	}
}
