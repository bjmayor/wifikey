package com.hiwifi.activity;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v7.app.ActionBarActivity;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TabHost.OnTabChangeListener;
import android.widget.TabHost.TabSpec;
import android.widget.TextView;

import com.hiwifi.activity.wifi.WifiListFragment;
import com.hiwifi.app.utils.CommonDialogUtil;
import com.hiwifi.app.utils.CommonDialogUtil.CommonDialogListener;
import com.hiwifi.app.views.FragmentTabHost;
import com.hiwifi.constant.RequestConstant.RequestTag;
import com.hiwifi.hiwifi.R;
import com.hiwifi.model.DiscoverItem;
import com.hiwifi.model.RecommendInfo;
import com.hiwifi.model.log.LogUtil;
import com.hiwifi.model.request.RequestFactory;
import com.hiwifi.model.request.RequestManager.ResponseHandler;
import com.hiwifi.model.request.ServerResponseParser;
import com.umeng.analytics.MobclickAgent;

/**
 * @filename MainTabActivity.java
 * @packagename com.hiwifi.activity
 * @projectname hiwifi1.0.1
 * @author jack at 2014-8-25
 */
public class MainTabActivity extends ActionBarActivity implements
		ResponseHandler {
	// 定义FragmentTabHost对象
	private FragmentTabHost mTabHost;
	// 定义一个布局
	private LayoutInflater layoutInflater;
	// 定义数组来存放Fragment界面
	private Class fragmentArray[] = { /* ConnectFragment.class, */
	WifiListFragment.class, DiscoverFragment.class, BoxFragment.class,
			SettingFragment.class };
	// 定义数组来存放按钮图片
	private int mImageViewArray[] = { R.drawable.selector_tab_connect,
			R.drawable.selector_tab_discover, R.drawable.selector_tab_box,
			R.drawable.selector_tab_setting };
	// Tab选项卡的文字
	private String mTextviewArray[] = { "连 接", "发 现", "百宝箱", "设 置" };
	private String title_connect = "小极WiFi钥匙";
	private TextView top_title;
	// private LinearLayout main_container;
	private RelativeLayout message_container;

	@Override
	protected void onCreate(Bundle arg0) {
		// TODO Auto-generated method stub
		super.onCreate(arg0);
		setContentView(R.layout.main_tab_layout);
		LogUtil.d("Tag:", "oncreate()");
		initView();
		getActionBar().hide();
		// registerReveiver();
	}

	private void initView() {
		// 实例化布局对象
		layoutInflater = LayoutInflater.from(this);
		top_title = (TextView) findViewById(R.id.top_title);
		// main_container = (LinearLayout)findViewById(R.id.main_container);

		// 实例化TabHost对象，得到TabHost
		mTabHost = (FragmentTabHost) findViewById(android.R.id.tabhost);
		mTabHost.setup(this, getSupportFragmentManager(), R.id.realtabcontent);
		// 得到fragment的个数
		int count = fragmentArray.length;
		for (int i = 0; i < count; i++) {
			// 为每一个Tab按钮设置图标、文字和内容
			TabSpec tabSpec = mTabHost.newTabSpec(mTextviewArray[i])
					.setIndicator(getTabItemView(i));
			// 将Tab按钮添加进Tab选项卡中
			mTabHost.addTab(tabSpec, fragmentArray[i], null);
			// 设置Tab按钮的背景
			mTabHost.getTabWidget().getChildAt(i)
					.setBackgroundResource(android.R.color.transparent);
		}
		top_title.setText(title_connect);
		mTabHost.setOnTabChangedListener(new OnTabChangeListener() {

			@Override
			public void onTabChanged(String tabId) {
				if(tabId.equals(mTextviewArray[0])){
					top_title.setText(title_connect);
				}else {
					top_title.setText(tabId);
				}
				if (tabId.equals(mTextviewArray[1])) {
					statusSet(false, 1);
					DiscoverItem.markRead();
				} else if (tabId.equals(mTextviewArray[2])) {
					statusSet(false, 2);
					RecommendInfo.markRead();
				}
				MobclickAgent.onEvent(MainTabActivity.this, "click_in_tab",
						tabId);
			}
		});

		statusSet(RecommendInfo.hasNew(), 2);
		statusSet(DiscoverItem.hasNew(), 1);
		if (RecommendInfo.getList().size() == 0) {
			RequestFactory.getRecommendApps(this, this);
		}
		if (DiscoverItem.getList().size() == 0) {
			RequestFactory.getDiscoverList(this, this);
		}
		setListener();
	}
	private void statusSet(boolean isSet, int position) {
		if (isSet) {
			mTabHost.getTabWidget().getChildTabViewAt(position)
					.findViewById(R.id.red_dot).setVisibility(View.VISIBLE);
		} else {
			mTabHost.getTabWidget().getChildTabViewAt(position)
					.findViewById(R.id.red_dot).setVisibility(View.GONE);
		}
	}

	/**
	 * 给Tab按钮设置图标和文字
	 */
	private View getTabItemView(int index) {
		View view = layoutInflater.inflate(R.layout.tab_item_view, null);
		LogUtil.d("Tag:", mTabHost.getTabWidget().getChildCount() + "");
		ImageView imageView = (ImageView) view.findViewById(R.id.imageview);
		imageView.setImageResource(mImageViewArray[index]);
		TextView textView = (TextView) view.findViewById(R.id.textview);
		textView.setText(mTextviewArray[index].replace(" ", "").trim().toString());
		// textView.setTextSize(TypedValue.COMPLEX_UNIT_PX,
		// ViewUtil.dip2px(this, 14));
		return view;
	}

	private void setListener() {
		FragmentManager fragmentManager = getSupportFragmentManager();
		Fragment connect = fragmentManager.findFragmentByTag(mTextviewArray[0]);
		fragmentManager.beginTransaction().commit();
		LogUtil.d("Tag:", connect + "");
	}

	private Update receiver;

	private void registerReveiver() {
		IntentFilter filter = new IntentFilter("hiwifi.hiwifi.update");
		filter.setPriority(0);
		receiver = new Update();
		registerReceiver(receiver, filter);
	}

	private void unRegisterReceiver() {
		if (receiver != null) {
			unregisterReceiver(receiver);
		}
	}

	class Update extends BroadcastReceiver {

		@Override
		public void onReceive(Context context, Intent intent) {
			if (intent != null) {

			}
		}
	}

	@Override
	protected void onDestroy() {
		super.onDestroy();
		// unRegisterReceiver();
	}

	private void showQuitLoginDialog() {
		MobclickAgent.onEvent(this, "show_exit_hiwifi_dialog", getResources()
				.getString(R.string.stat_exit_hiwifi_dialog));
		String ok = getResources().getString(R.string.quit_msg_ok);
		String cancel = getResources().getString(R.string.quit_msg_cancel);
		CommonDialogUtil.ShowAnimDialog(this, CommonDialogUtil.NONE,
				getResources().getString(R.string.quit_msg_tip), ok, cancel,
				new CommonDialogListener() {

					@Override
					public void positiveAction() {
						MobclickAgent.onEvent(
								MainTabActivity.this,
								"click_exit_hiwifi",
								getResources().getString(
										R.string.stat_exit_hiwifi));
						MainTabActivity.this.finish();
					}

					@Override
					public void negativeAction() {
						MobclickAgent.onEvent(
								MainTabActivity.this,
								"click_continue_service",
								getResources().getString(
										R.string.stat_continue_service));
					}
				});
	}

//	@Override
//	public boolean onKeyDown(int keyCode, KeyEvent event) {
//		if (keyCode == KeyEvent.KEYCODE_BACK) {
//			showQuitLoginDialog();
//		}
//		return super.onKeyDown(keyCode, event);
//	}

	@Override
	public void onStart(RequestTag tag, Code code) {
		// TODO Auto-generated method stub

	}

	@Override
	public void onSuccess(RequestTag tag, ServerResponseParser responseParser) {
		if (tag == RequestTag.HIWIFI_APP_RECOMMEND_GET) {
			new RecommendInfo().parse(tag, responseParser);
			statusSet(RecommendInfo.hasNew(), 2);
		} else if (tag == RequestTag.HIWIFI_DISCOVER_LIST_GET) {
			new DiscoverItem().parse(tag, responseParser);
			statusSet(DiscoverItem.hasNew(), 1);
		}
	}

	@Override
	public void onFailure(RequestTag tag, Throwable error) {

	}

	@Override
	public void onFinish(RequestTag tag) {

	}
	
	@Override
	protected void onResume() {
		super.onResume();
		MobclickAgent.onResume(this);          //统计时长
	}
	
	@Override
	protected void onPause() {
		super.onPause();
		MobclickAgent.onPause(this);
	}
}
