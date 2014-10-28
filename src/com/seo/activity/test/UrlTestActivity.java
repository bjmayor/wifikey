package com.seo.activity.test;

import java.net.MalformedURLException;

import android.annotation.SuppressLint;
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

import com.seo.constant.RequestConstant.RequestTag;
import com.seo.model.log.LogUtil;
import com.seo.model.request.RequestFactory;
import com.seo.model.request.RequestManager;
import com.seo.model.request.RequestManager.ResponseHandler;
import com.seo.model.request.ServerResponseParser;
import com.seo.model.wifi.WifiAdmin;
import com.seo.store.AccessPointDbMgr;
import com.seo.support.http.RequestParams;
import com.seo.wifikey.R;

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
		   if (tag == RequestTag.HIWIFI_PWD_GET) {
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
			} else{
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
