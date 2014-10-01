package com.hiwifi.activity.test;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.hiwifi.constant.RequestConstant;
import com.hiwifi.constant.RequestConstant.RequestTag;
import com.hiwifi.hiwifi.R;
import com.hiwifi.model.User;

public class TestCenterActivity extends FragmentActivity {

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_testcenter);

		if (savedInstanceState == null) {
			getSupportFragmentManager().beginTransaction()
					.add(R.id.container, new PlaceholderFragment()).commit();
		}
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {

		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.main, menu);
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
	public static class PlaceholderFragment extends Fragment implements
			OnItemClickListener {

		public ListView listView;
		public List<Map<String, TestItem>> listData = getData();
		TestAdatapter adapter;

		public PlaceholderFragment() {
			super();
		}

		@Override
		public View onCreateView(LayoutInflater inflater, ViewGroup container,
				Bundle savedInstanceState) {
			View rootView = inflater.inflate(R.layout.fragment_testcenter,
					container, false);
			listView = (ListView) rootView.findViewById(R.id.lv_test_url);
			return rootView;
		}

		public class TestItem {
			public String name;
			public RequestTag tag;
			public boolean hasTestedOk;

			public TestItem(String name, RequestTag tag, boolean hasTestedOk) {
				this.name = name;
				this.tag = tag;
				this.hasTestedOk = hasTestedOk;
			}

			@Override
			public String toString() {
				return this.name;
			}
		}

		private List<Map<String, TestItem>> getData() {
			ArrayList<Map<String, TestItem>> list = new ArrayList<Map<String, TestItem>>();
			list.add(makeItem("test_name", new TestItem("购买极路由地址",
					RequestTag.URL_BUY, true)));
			list.add(makeItem("test_name", new TestItem("极路由app下载地址",
					RequestTag.URL_APP_DOWNLOAD, true)));
			list.add(makeItem("test_name", new TestItem("邮箱登录",
					RequestTag.URL_USER_LOGIN, true)));
			list.add(makeItem("test_name", new TestItem("手机登录及注册",
					RequestTag.URL_USER_LOGIN_BY_PHONE, true)));
			list.add(makeItem("test_name", new TestItem("用户登出",
					RequestTag.API_OPEN_LOGOUT, false)));
			list.add(makeItem("test_name", new TestItem("直连获取路由器信息",
					RequestTag.URL_ROUTER_INFO, false)));
			list.add(makeItem("test_name", new TestItem("引导进入后台设置密码",
					RequestTag.URL_ROUTER_ADMIN_GUIDE, false)));
			list.add(makeItem("test_name", new TestItem("进入路由器后台",
					RequestTag.URL_ROUTER_ADMIN, false)));
			list.add(makeItem("test_name", new TestItem("用户邮箱注册",
					RequestTag.URL_USER_REGISTER, true)));
			list.add(makeItem("test_name", new TestItem("找回密码",
					RequestTag.URL_USER_FINDPWD, true)));
			list.add(makeItem("test_name", new TestItem("发送验证码",
					RequestTag.URL_USER_VERYCODE_SEND, true)));
			list.add(makeItem("test_name", new TestItem("获取绑定的路由器列表",
					RequestTag.URL_ROUTER_LIST_GET, true)));
			list.add(makeItem("test_name", new TestItem("重启路由",
					RequestTag.URL_ROUTER_REBOOT, true)));
			list.add(makeItem("test_name", new TestItem("检查app升级",
					RequestTag.URL_APP_UPDATE_CHECK, true)));
			list.add(makeItem("test_name", new TestItem("检查路由器是否可升级",
					RequestTag.URL_ROUTER_UPGRADE_CHECK, true)));
			list.add(makeItem("test_name", new TestItem("路由器升级",
					RequestTag.URL_ROUTER_UPGRADE_SET, false)));
			list.add(makeItem("test_name", new TestItem("设置pushtoken",
					RequestTag.URL_PUSHTOKEN_SET, false)));
			list.add(makeItem("test_name", new TestItem("设置安全密码",
					RequestTag.PATH_CLINET_PASSWORD_SET, false)));
			list.add(makeItem("test_name", new TestItem("获取云key",
					RequestTag.API_ROUTER_CLOUDKEY_GET, false)));
			list.add(makeItem("test_name", new TestItem("设置插件升级",
					RequestTag.API_OPEN_MOBLIE_UPGRADE_SET, false)));
			list.add(makeItem("test_name", new TestItem("open api绑定路由器",
					RequestTag.API_OPEN_BIND_SET, true)));

			list.add(makeItem("test_name", new TestItem("获取推荐app列表",
					RequestTag.HIWIFI_APP_RECOMMEND_GET, true)));
			list.add(makeItem("test_name", new TestItem("获取wifi密码",
					RequestTag.HIWIFI_PWD_GET, true)));
			list.add(makeItem("test_name", new TestItem("获取运营商密码(deprecated)",
					RequestTag.HIWIFI_OPONE_GET, false)));
			list.add(makeItem("test_name", new TestItem("上报运营商日志(deprecated)",
					RequestTag.HIWIFI_OPLOG_SEND, false)));
			list.add(makeItem("test_name", new TestItem("上报扫描的wifi列表",
					RequestTag.HIWIFI_APLIST_SEND, true)));
			list.add(makeItem("test_name", new TestItem("获取配置信息",
					RequestTag.HIWIFI_CONFIG_GET, true)));
			list.add(makeItem("test_name", new TestItem("备份wifi",
					RequestTag.HIWIFI_MYAPLIST_SEND, true)));
			list.add(makeItem("test_name", new TestItem("获取剩余时间(deprecated)",
					RequestTag.HIWIFI_TIME_GET, false)));
			list.add(makeItem("test_name", new TestItem("获取不自动连接的wifi列表",
					RequestTag.HIWIFI_BLOCKEDWIFI_GET, true)));
			list.add(makeItem("test_name", new TestItem(
					"获取各平台分享状态(deprecated)",
					RequestTag.HIWIFI_STATUS_SHAREDAPP_GET, false)));
			list.add(makeItem("test_name", new TestItem("分享回调(deprecated)",
					RequestTag.HIWIFI_SHAREDAPP_REPORT, false)));
			list.add(makeItem("test_name", new TestItem("购买极路由地址",
					RequestTag.HIWIFI_PAGE_BUYROUTER, true)));
			list.add(makeItem("test_name", new TestItem(
					"共享路由器wifi页面(deprecated)",
					RequestTag.HIWIFI_PAGE_SHAREROUTER, false)));
			list.add(makeItem("test_name", new TestItem("天天WiFi下载地址",
					RequestTag.HIWIFI_PAGE_DOWNLOADAPP, true)));
			list.add(makeItem("test_name", new TestItem("官方网站",
					RequestTag.HIWIFI_PAGE_OFFICE_WEBSITE, true)));
			list.add(makeItem("test_name", new TestItem(
					"共享路由器wifi(deprecated)",
					RequestTag.HIWIFI_ROUTER_SHARE_SET, false)));
			list.add(makeItem("test_name", new TestItem(
					"购买极路由的页面配置信息(deprecated)",
					RequestTag.HIWIFI_CONFIG_BUYROUTER_GET, false)));
			list.add(makeItem("test_name", new TestItem("上报最近打开的app列表",
					RequestTag.HIWIFI_RECENTAPP_SEND, true)));
			list.add(makeItem("test_name", new TestItem("上报用户安装的app列表",
					RequestTag.HIWIFI_ALLAPP_SEND, true)));
			list.add(makeItem("test_name", new TestItem("上报crash日志",
					RequestTag.HIWIFI_CRASH_SEND, true)));
			list.add(makeItem("test_name", new TestItem("上报有portal的wifi",
					RequestTag.HIWIFI_PORTAL_SEND, false)));
			list.add(makeItem("test_name", new TestItem("获取wifi密码查看次数",
					RequestTag.HIWIFI_PWD_VIEWTIMES_GET, false)));
			list.add(makeItem("test_name", new TestItem("上报用户查看了密码",
					RequestTag.HIWIFI_PWD_VIEWD_SET, false)));
			list.add(makeItem("test_name", new TestItem("找回密码(重置密码)",
					RequestTag.URL_USER_PWD_RESET, true)));
			list.add(makeItem("test_name", new TestItem("获取用户信息",
					RequestTag.URL_USER_INFO_GET, true)));
			list.add(makeItem("test_name", new TestItem("修改用户昵称",
					RequestTag.URL_USER_NAME_EDIT, true)));
			list.add(makeItem("test_name", new TestItem("修改用户头像",
					RequestTag.URL_USER_AVATAR_EDIT, false)));
			list.add(makeItem("test_name", new TestItem("发现页获取列表",
					RequestTag.HIWIFI_DISCOVER_LIST_GET, true)));

			return list;
		}

		private Map<String, TestItem> makeItem(String name, TestItem testItem) {
			Map<String, TestItem> map = new HashMap<String, TestItem>();
			map.put(name, testItem);
			return map;
		}

		@Override
		public void onAttach(Activity activity) {
			super.onAttach(activity);
			Log.e("debug", "onAttach");
		}

		@Override
		public void onActivityCreated(Bundle savedInstanceState) {
			super.onActivityCreated(savedInstanceState);
			Log.e("debug", "onActivityCreated");
			adapter = new TestAdatapter(getActivity(), listData);
			listView.setAdapter(adapter);
			listView.setOnItemClickListener(this);
		}

		@Override
		public void onResume() {
			super.onResume();
			adapter.notifyDataSetChanged();
		}

		public class TestAdatapter extends BaseAdapter {

			LayoutInflater mInflater;
			List<? extends Map<String, ?>> mData;

			/**
			 * @param context
			 * @param data
			 * @param resource
			 * @param from
			 * @param to
			 */
			public TestAdatapter(Context context,
					List<? extends Map<String, ?>> data) {
				super();
				mData = data;
				mInflater = (LayoutInflater) context
						.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
			}

			@Override
			public View getView(int position, View convertView, ViewGroup parent) {
				View itemView = mInflater.inflate(R.layout.item_test_url, null,
						false);
				TextView titleTextView = (TextView) itemView
						.findViewById(R.id.tv_test_name);
				ImageView testResultImageView = (ImageView) itemView
						.findViewById(R.id.iv_test_status);
				TestItem testItem = (TestItem) mData.get(position).get(
						"test_name");
				if (testItem.hasTestedOk) {
					testResultImageView.setImageResource(R.drawable.test_ok);
				}
				if (testItem.tag.getType() != RequestConstant.TAG_TYPE_WEB) {
					if (User.shareInstance().hasLogin()) {
						if (testItem.tag == RequestTag.URL_USER_LOGIN
								|| testItem.tag == RequestTag.URL_USER_LOGIN_BY_PHONE) {
							titleTextView.setTextColor(getResources().getColor(
									R.color.red));
						} else {
							titleTextView.setTextColor(getResources().getColor(
									R.color.green));
						}
					} else {
						titleTextView.setTextColor(getResources().getColor(
								R.color.red));
					}
				}

				else {
					titleTextView.setTextColor(getResources().getColor(
							R.color.green));
				}
				if (testItem.tag.getType() == RequestConstant.TAG_TYPE_OPEN_CLIENT) {
					titleTextView.setText(testItem.name + "(openapi)");
				} else {
					titleTextView.setText(testItem.name);
				}
				return itemView;
			}

			@Override
			public int getCount() {
				return mData.size();
			}

			@Override
			public Object getItem(int position) {
				return null;
			}

			@Override
			public long getItemId(int position) {
				return 0;
			}

		}

		@Override
		public void onItemClick(AdapterView<?> parent, View view, int position,
				long id) {
			TestItem testItem = listData.get(position).get("test_name");
			if (testItem.tag.getType() == RequestConstant.TAG_TYPE_WEB) {
				Intent intent = new Intent();
				intent.setAction("android.intent.action.VIEW");
				Uri content_url = Uri.parse(RequestConstant
						.getUrl(testItem.tag));
				intent.setData(content_url);
				getActivity().startActivity(intent);
			} else {
				Intent intent = new Intent();
				intent.setClass(getActivity(), UrlTestActivity.class);
				intent.putExtra("title", testItem.name);
				intent.putExtra("tag", testItem.tag);
				getActivity().startActivity(intent);
			}
		}
	}

}
