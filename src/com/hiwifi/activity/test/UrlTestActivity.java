package com.hiwifi.activity.test;

import java.net.MalformedURLException;
import java.util.HashMap;
import java.util.Map;

import android.annotation.SuppressLint;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import com.hiwifi.constant.RequestConstant.RequestTag;
import com.hiwifi.hiwifi.R;
import com.hiwifi.model.log.LogUtil;
import com.hiwifi.model.request.RequestFactory;
import com.hiwifi.model.request.RequestManager;
import com.hiwifi.model.request.RequestManager.ResponseHandler;
import com.hiwifi.model.request.ServerResponseParser;
import com.hiwifi.model.router.RouterManager;
import com.hiwifi.model.router.WifiInfo.SignalMode;
import com.hiwifi.model.wifi.WifiAdmin;
import com.hiwifi.store.AccessPointDbMgr;
import com.hiwifi.support.http.RequestParams;

public class UrlTestActivity extends FragmentActivity {

	private static final String TAG = "UrlTestActivity";
	private String title;
	public RequestTag tag;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_url_test);

		if (savedInstanceState == null) {
			getSupportFragmentManager().beginTransaction()
					.add(R.id.container, new PlaceholderFragment()).commit();
		}
		title = getIntent().getStringExtra("title");
		setTitle(title);
		tag = (RequestTag) getIntent().getSerializableExtra("tag");
		PlaceholderFragment.tag = tag;
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {

		// getMenuInflater().inflate(R.menu.url_test, menu);
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		// Handle action bar item clicks here. The action bar will
		// automatically handle clicks on the Home/Up button, so long
		// as you specify a parent activity in AndroidManifest.xml.
		int id = item.getItemId();
		if (id == R.id.action_settings) {
			return true;
		}
		return super.onOptionsItemSelected(item);
	}

	/**
	 * A placeholder fragment containing a simple view.
	 */
	@SuppressLint("ValidFragment")
	public static class PlaceholderFragment extends Fragment implements
			OnClickListener, ResponseHandler {

		TextView resultView;
		public static RequestTag tag;

		public PlaceholderFragment() {

		}

		@Override
		public View onCreateView(LayoutInflater inflater, ViewGroup container,
				Bundle savedInstanceState) {
			View rootView = inflater.inflate(R.layout.fragment_url_test,
					container, false);
			resultView = (TextView) rootView.findViewById(R.id.tv_result);
			rootView.findViewById(R.id.btn_test).setOnClickListener(this);
			return rootView;
		}

		@Override
		public void onClick(View v) {
			RequestParams params = new RequestParams();
			if (tag == RequestTag.URL_USER_LOGIN) {
				RequestFactory.loginByPhoneOrEmail(getActivity(), this,
						"blueachaog@hotmail.com", "123!@#");
			} else if (tag == RequestTag.URL_USER_LOGIN_BY_PHONE) {
				RequestFactory.loginByPhone(getActivity(), this, "18611370939",
						"GeekGeek");
			} else if (tag == RequestTag.URL_USER_VERYCODE_SEND) {
				RequestFactory.sendVerycode(getActivity(), this, "18611370939");
			} else if (tag == RequestTag.URL_APP_UPDATE_CHECK) {
				RequestFactory.checkAppUpGrade(getActivity(), this);
			} else if (tag == RequestTag.URL_ROUTER_UPGRADE_CHECK) {
				RequestFactory.checkRomUpgrade(getActivity(), this);
			} else if (tag == RequestTag.OPENAPI_CLINET_LED_STATUS_SET_OFF) {
				RequestFactory.setLedStatus(getActivity(), this, false);
			} else if (tag == RequestTag.OPENAPI_CLINET_LED_STATUS_SET_ON) {
				RequestFactory.setLedStatus(getActivity(), this, true);
			} else if (tag == RequestTag.OPENAPI_CLINET_LED_STATUS_GET) {
				RequestFactory.getLedStatus(getActivity(), this);
			} else if (tag == RequestTag.OPENAPI_CLIENT_WIFI_SWITCH_GET) {
				RequestFactory.getWiFiStatus(getActivity(), this);
			} else if (tag == RequestTag.OPENAPI_CLIENT_WIFI_SWITCH_SET) {
				RequestFactory.setWiFiStatus(getActivity(), this, false);
			} else if (tag == RequestTag.URL_PLUGIN_INSTALLED_LIST_GET) {
				RequestFactory.getIntalledPlugins(getActivity(), this);
			} else if (tag == RequestTag.URL_ROUTER_NAME_SET) {
				RequestFactory.setRouterName(getActivity(), this, "测试名字");
			} else if (tag == RequestTag.PATH_NETWORK_BLOCKED_LIST_GET) {
				RequestFactory.getBlockedDevices(getActivity(), this);
			} else if (tag == RequestTag.API_OPEN_BIND_SET) {
				RequestFactory.bindRouters(getActivity(), this, true);
			} else if (tag == RequestTag.OPENAPI_WIFI_CHANNEL_GET) {
				RequestFactory.getChannel(getActivity(), this);
			} else if (tag == RequestTag.OPENAPI_WIFI_CHANNEL_SET) {
				RequestFactory.setChannel(getActivity(), this, 4);
			} else if (tag == RequestTag.OPENAPI_WIFI_CHANNEL_RANK_GET) {
				RequestFactory.getChannelRank(getActivity(), this);
			} else if (tag == RequestTag.URL_ROUTER_REBOOT) {
				RequestFactory.rebootCurrentRouter(getActivity(), this);
			} else if (tag == RequestTag.OPENAPI_CLIENT_ROUTER_CROSS_STATUS_SET) {
				RequestFactory.setSignalMode(getActivity(), this,
						SignalMode.Crossed);
			} else if (tag == RequestTag.OPENAPI_CLIENT_ROUTER_CROSS_STATUS_GET) {
				RequestFactory.getSignalMode(getActivity(), this);
			} else if (tag == RequestTag.OPENAPI_CLIENT_WIFI_SLEEP_GET) {
				RequestFactory.getSleepTime(getActivity(), this);
			} else if (tag == RequestTag.OPENAPI_CLIENT_WIFI_SLEEP_SET) {
				RequestFactory.setSleepTime(getActivity(), this, "2320", "900");
			} else if (tag == RequestTag.OPENAPI_CLINET_QOS_GET) {
				RequestFactory.getQos(getActivity(), this);
			} else if (tag == RequestTag.OPENAPI_CLINET_QOS_SET) {
				RequestFactory.setQos(getActivity(), this, "abdsfad", "2320",
						"900", "bb");
			} else if (tag == RequestTag.API_MESSAGE_VIEW_GET) {
				RequestFactory.getMessageDetail(getActivity(), this, 11366399);
			} else if (tag == RequestTag.HIWIFI_APP_RECOMMEND_GET) {
				RequestFactory.getRecommendApps(getActivity(), this);
			} else if (tag == RequestTag.HIWIFI_PWD_GET) {
				RequestFactory.getPasswords(getActivity(), this, WifiAdmin
						.sharedInstance().getMergedAccessPoints());
			} else if (tag == RequestTag.HIWIFI_OPONE_GET) {
				Toast.makeText(getActivity(), "已废弃", Toast.LENGTH_SHORT).show();
			} else if (tag == RequestTag.HIWIFI_OPLOG_SEND) {
				Toast.makeText(getActivity(), "已废弃", Toast.LENGTH_SHORT).show();
			} else if (tag == RequestTag.HIWIFI_APLIST_SEND) {
				RequestFactory.sendApList(getActivity(), this, AccessPointDbMgr
						.shareInstance().getUnUnploadAPList(20));
			} else if (tag == RequestTag.HIWIFI_CONFIG_GET) {
				RequestFactory.getConfig(getActivity(), this);
			} else if (tag == RequestTag.HIWIFI_MYAPLIST_SEND) {
				RequestFactory.sendMyApList(getActivity(), this);
			} else if (tag == RequestTag.HIWIFI_TIME_GET) {
				Toast.makeText(getActivity(), "已废弃", Toast.LENGTH_SHORT).show();
			} else if (tag == RequestTag.HIWIFI_BLOCKEDWIFI_GET) {
				RequestFactory.getSSIDThanNotAutoConnected(getActivity(), this);
			} else if (tag == RequestTag.HIWIFI_STATUS_SHAREDAPP_GET) {
				Toast.makeText(getActivity(), "已废弃", Toast.LENGTH_SHORT).show();
			} else if (tag == RequestTag.HIWIFI_SHAREDAPP_REPORT) {
				Toast.makeText(getActivity(), "已废弃", Toast.LENGTH_SHORT).show();
			} else if (tag == RequestTag.HIWIFI_ROUTER_SHARE_SET) {
				Toast.makeText(getActivity(), "已废弃", Toast.LENGTH_SHORT).show();
			} else if (tag == RequestTag.HIWIFI_CONFIG_BUYROUTER_GET) {
				Toast.makeText(getActivity(), "已废弃", Toast.LENGTH_SHORT).show();
			} else if (tag == RequestTag.HIWIFI_RECENTAPP_SEND) {
				RequestFactory.sendRecentOpenedAppList(getActivity(), this);
			} else if (tag == RequestTag.HIWIFI_ALLAPP_SEND) {
				RequestFactory.sendInstalledAppList(getActivity(), this);
			} else if (tag == RequestTag.HIWIFI_CRASH_SEND) {
				RequestFactory.sendCrashLog(getActivity(), this,
						"test center tested");
			} else if (tag == RequestTag.HIWIFI_PORTAL_SEND) {
				Toast.makeText(getActivity(), "未实现", Toast.LENGTH_SHORT).show();
			} else if (tag == RequestTag.HIWIFI_PWD_VIEWTIMES_GET) {
				RequestFactory.getPasswordViewTimes(getActivity(), this);
			} else if (tag == RequestTag.HIWIFI_PWD_VIEWD_SET) {
				RequestFactory.sendPasswordHasViewed(getActivity(),
						"aa:ff:ss:bb", this);
			} else if (tag == RequestTag.URL_USER_INFO_GET) {
				RequestFactory.getUserInfo(getActivity(), this);
			} else if (tag == RequestTag.URL_USER_AVATAR_EDIT) {
				RequestFactory.uploadUserPhoto(getActivity(), this,
						R.drawable.wifilist_action_refresh_pressed);
			} else if (tag == RequestTag.URL_USER_NAME_EDIT) {
				RequestFactory.modifyUserInfo(getActivity(), this, "urltest");
			} else {
				RequestManager.requestByTag(getActivity(), tag, params, this);
			}

		}

		@Override
		public void onSuccess(RequestTag tag,
				ServerResponseParser responseParser) {
			resultView.setText(responseParser.originResponse.toString());
			LogUtil.e(TAG, tag + "");
			LogUtil.e(TAG, responseParser.originResponse.toString());
			LogUtil.e(TAG, tag.getUri().toString());
			// if (tag == RequestTag.URL_USER_LOGIN) {
			// User.shareInstance().onLogin(
			// responseParser.originResponse.optString("uid", "0"),
			// "blueachaog@hotmail.com",
			// responseParser.originResponse.optString("token", "0"),
			// responseParser.originResponse.optInt("expire", 100));
			// }
			// if (tag == RequestTag.URL_USER_LOGIN_BY_PHONE) {
			// User.shareInstance().onLogin(
			// responseParser.originResponse.optString("uid", "0"),
			// "18611370939",
			// responseParser.originResponse.optString("token", "0"),
			// responseParser.originResponse.optInt("expire", 100));
			// }
			if (tag == RequestTag.URL_ROUTER_LIST_GET) {
				RouterManager.shareInstance().setRouters(
						responseParser.originResponse);
			}
		}

		@Override
		public void onFailure(RequestTag tag, Throwable error) {
			LogUtil.e(TAG, tag + "");
			try {
				LogUtil.e(TAG, error != null ? error.getMessage() : "");
			} catch (Exception e) {
			}
			try {
				LogUtil.e(TAG, tag.getUri().toURL().toString());
			} catch (MalformedURLException e) {
				e.printStackTrace();
			}
		}

		@Override
		public void onFinish(RequestTag tag) {
			if (getActivity() != null) {
				Toast.makeText(getActivity(), "请求结束", Toast.LENGTH_SHORT)
						.show();
			}
		}

		@Override
		public void onStart(RequestTag tag, Code code) {
			if (code == Code.ok) {
				Toast.makeText(getActivity(), "正在请求，请稍后", Toast.LENGTH_SHORT)
						.show();
			} else {
				Toast.makeText(getActivity(), code.getMsg(), Toast.LENGTH_SHORT)
						.show();
			}
		}
	}

}
