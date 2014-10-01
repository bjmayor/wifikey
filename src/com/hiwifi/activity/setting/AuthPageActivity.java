package com.hiwifi.activity.setting;

import java.util.ArrayList;
import java.util.HashMap;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.os.Handler;
import android.os.Handler.Callback;
import android.os.Message;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.BaseAdapter;
import android.widget.CheckedTextView;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.Toast;
import cn.sharesdk.framework.Platform;
import cn.sharesdk.framework.PlatformActionListener;
import cn.sharesdk.framework.ShareSDK;
import cn.sharesdk.framework.utils.UIHandler;
import cn.sharesdk.onekeyshare.ShareCore;

import com.hiwifi.app.views.SildingFinishLayout;
import com.hiwifi.app.views.SildingFinishLayout.OnSildingFinishListener;
import com.hiwifi.hiwifi.R;
import com.hiwifi.model.log.LogUtil;
import com.hiwifi.utils.NetworkUtil;
import com.hiwifi.utils.Utils;
import com.umeng.analytics.MobclickAgent;

public class AuthPageActivity extends Activity implements OnClickListener,
		Callback, PlatformActionListener {

	protected Handler handler = new Handler(this);
	private AuthAdapter adapter;
	private ListView lvPlats;
	private ShareReceiver shareReceiver;
	private SildingFinishLayout mSildingFinishLayout;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);
		setContentView(R.layout.auth_page_activity);
		ShareSDK.initSDK(this);
//		((TextView) findViewById(R.id.tv_titile)).setText(getResources()
//				.getString(R.string.oauth_share));
//		((ImageView) findViewById(R.id.close_page)).setOnClickListener(this);
		// back_btn = (ImageView) findViewById(R.id.back_btn);
		// common_title = (TextView) findViewById(R.id.center_title);
		// common_title.setText("分享设置");
		lvPlats = (ListView) findViewById(R.id.lvPlats);
		lvPlats.setSelector(new ColorDrawable());
		adapter = new AuthAdapter(this);
		lvPlats.setAdapter(adapter);
		lvPlats.setOnItemClickListener(adapter);
		View page_container = findViewById(R.id.auth_content);
		mSildingFinishLayout = (SildingFinishLayout) findViewById(R.id.auth_sildingFinishLayout);
		mSildingFinishLayout.setTouchView(page_container);
		mSildingFinishLayout.setTouchView(lvPlats);
		setListener();
	}

	private void setListener() {
		// back_btn.setOnClickListener(this);

		IntentFilter filter = new IntentFilter("com.hiwifi.share.broadcast");
		shareReceiver = new ShareReceiver();
		registerReceiver(shareReceiver, filter);
		mSildingFinishLayout
				.setOnSildingFinishListener(new OnSildingFinishListener() {

					@Override
					public void onSildingFinish() {
						AuthPageActivity.this.finish();
						AuthPageActivity.this.overridePendingTransition(0, 0);
					}
				});

	}

	/**
	 * 处理操作结果
	 * <p>
	 * 如果获取到用户的名称，则显示名称；否则如果已经授权，则显示 平台名称
	 */
	public boolean handleMessage(Message msg) {
		Platform plat = (Platform) msg.obj;
		// String text = actionToString(msg.arg2);
		String text = "";
		switch (msg.arg1) {
		case 1: {
			// 成功
			// text = plat.getName() + " completed at " + text;
			text = "绑定成功";
			Toast.makeText(this, text, Toast.LENGTH_SHORT).show();
			// ToastUtil.showMessage(this,text, Toast.LENGTH_SHORT);
			break;
		}
		case 2: {
			// 失败
			// text = plat.getName() + " caught error at " + text;
			text = "绑定失败";
			Toast.makeText(this, text, Toast.LENGTH_SHORT).show();
			// ToastUtil.showMessage(this,text, Toast.LENGTH_SHORT);
			return false;
		}
		case 3: {
			// 取消
			// text = plat.getName() + " canceled at " + text;
			// text = "";
			return false;
		}
		}
		adapter.notifyDataSetChanged();

		return false;
	}

	/** 将action转换为String */
	public String actionToString(int action) {
		switch (action) {
		case Platform.ACTION_AUTHORIZING:
			return "ACTION_AUTHORIZING";
		case Platform.ACTION_GETTING_FRIEND_LIST:
			return "ACTION_GETTING_FRIEND_LIST";
		case Platform.ACTION_FOLLOWING_USER:
			return "ACTION_FOLLOWING_USER";
		case Platform.ACTION_SENDING_DIRECT_MESSAGE:
			return "ACTION_SENDING_DIRECT_MESSAGE";
		case Platform.ACTION_TIMELINE:
			return "ACTION_TIMELINE";
		case Platform.ACTION_USER_INFOR:
			return "ACTION_USER_INFOR";
		case Platform.ACTION_SHARE:
			return "ACTION_SHARE";
		default: {
			return "UNKNOWN";
		}
		}
	}

	
	
	
	@Override
	public void onComplete(Platform plat, int action,
			HashMap<String, Object> arg2) {
		// TODO Auto-generated method stub
		Message msg = new Message();
		msg.arg1 = 1;
		msg.arg2 = action;
		msg.obj = plat;
//		handler.sendMessage(msg);
		UIHandler.sendMessage(msg, this);
	}

	@Override
	public void onError(Platform plat, int action, Throwable t) {
		// TODO Auto-generated method stub
		t.printStackTrace();
		Message msg = new Message();
		msg.arg1 = 2;
		msg.arg2 = action;
		msg.obj = plat;
//		handler.sendMessage(msg);
		UIHandler.sendMessage(msg, this);
	}

	public void onCancel(Platform plat, int action) {
		Message msg = new Message();
		msg.arg1 = 3;
		msg.arg2 = action;
		msg.obj = plat;
//		handler.sendMessage(msg);
		UIHandler.sendMessage(msg, this);
	}
	
	@Override
	/** 授权和取消授权的逻辑代码 */
	public void onClick(View v) {
	}

	private class AuthAdapter extends BaseAdapter implements
			OnItemClickListener {
		private Context page;
		private ArrayList<Platform> platforms;

		public AuthAdapter(Context page) {
			this.page = page;

			// 获取平台列表
			Platform[] tmp = ShareSDK.getPlatformList(page);
			platforms = new ArrayList<Platform>();
			for (Platform p : tmp) {
				String name = p.getName();
				if (!ShareCore.canAuthorize(p.getContext(), name)) {
					continue;
				}
				platforms.add(p);
			}
		}

		public int getCount() {
			return platforms == null ? 0 : platforms.size();
		}

		public Platform getItem(int position) {
			return platforms.get(position);
		}

		public long getItemId(int position) {
			return position;
		}

		public View getView(int position, View convertView, ViewGroup parent) {
			if (convertView == null) {
				convertView = LayoutInflater.from(page).inflate(
						R.layout.auth_page_item, null);
			}

			int count = getCount();
			View llItem = convertView.findViewById(R.id.llItem);
			int dp_10 = cn.sharesdk.framework.utils.R.dipToPx(
					parent.getContext(), 10);
			if (count == 1) {
				llItem.setBackgroundResource(R.drawable.list_item_single_normal);
				llItem.setPadding(0, 0, 0, 0);
				convertView.setPadding(dp_10, dp_10, dp_10, dp_10);
			} else if (position == 0) {
				llItem.setBackgroundResource(R.drawable.list_item_first_normal);
				llItem.setPadding(0, 0, 0, 0);
				convertView.setPadding(dp_10, dp_10, dp_10, 0);
			} else if (position == count - 1) {
				llItem.setBackgroundResource(R.drawable.list_item_last_normal);
				llItem.setPadding(0, 0, 0, 0);
				convertView.setPadding(dp_10, 0, dp_10, dp_10);
			} else {
				llItem.setBackgroundResource(R.drawable.list_item_middle_normal);
				llItem.setPadding(0, 0, 0, 0);
				convertView.setPadding(dp_10, 0, dp_10, 0);
			}

			Platform plat = getItem(position);
			ImageView ivLogo = (ImageView) convertView
					.findViewById(R.id.ivLogo);
			Bitmap logo = getIcon(plat);
			if (logo != null && !logo.isRecycled()) {
				ivLogo.setImageBitmap(logo);
			}
			CheckedTextView ctvName = (CheckedTextView) convertView
					.findViewById(R.id.ctvName);
			ctvName.setChecked(plat.isValid());
			if (plat.isValid()) {
				String userName = plat.getDb().get("nickname");
				if (userName == null || userName.length() <= 0
						|| "null".equals(userName)) {
					// 如果平台已经授权却没有拿到帐号名称，则自动获取用户资料，以获取名称
					userName = getName(plat);
					plat.setPlatformActionListener(AuthPageActivity.this);
					plat.authorize();
					plat.showUser(null);
				}
				ctvName.setText(userName);
			} else {
				ctvName.setText(R.string.not_yet_authorized);
			}
			return convertView;
		}

		private Bitmap getIcon(Platform plat) {
			if (plat == null) {
				return null;
			}

			String name = plat.getName();
			if (name == null) {
				return null;
			}

			String resName = "logo_" + plat.getName();
			LogUtil.d("image_icon:", resName);
			int resId = cn.sharesdk.framework.utils.R.getResId(
					R.drawable.class, resName);
			return BitmapFactory.decodeResource(page.getResources(), resId);
		}

		private String getName(Platform plat) {
			if (plat == null) {
				return "";
			}

			String name = plat.getName();
			if (name == null) {
				return "";
			}

			int resId = cn.sharesdk.framework.utils.R.getStringRes(page,
					plat.getName());
			return page.getString(resId);
		}

		@Override
		public void onItemClick(AdapterView<?> arg0, View arg1, int arg2,
				long arg3) {

			Platform plat = getItem(arg2);
			CheckedTextView ctvName = (CheckedTextView) arg1
					.findViewById(R.id.ctvName);
			if (plat == null) {
				ctvName.setChecked(false);
				ctvName.setText(R.string.not_yet_authorized);
				return;
			}

			if (plat.isValid()) {
				plat.removeAccount();
				ctvName.setChecked(false);
				ctvName.setText(R.string.not_yet_authorized);
				return;
			}

			if (!NetworkUtil.checkConnection(page)) {
				Utils.showToast(page, -1, "网络不畅", 0, Utils.Level.ERROR);
				return;
			}
			plat.setPlatformActionListener(AuthPageActivity.this);
			plat.authorize();
//			plat.SSOSetting(true);
			plat.showUser(null);

		}
	}

	private class ShareReceiver extends BroadcastReceiver {

		@Override
		public void onReceive(Context context, Intent intent) {
			adapter.notifyDataSetChanged();
		}
	}

	@Override
	protected void onDestroy() {
		unregisterReceiver(shareReceiver);
		super.onDestroy();
	}
	
	@Override
	protected void onResume() {
		super.onResume();
		MobclickAgent.onPageStart(this.getClass().getSimpleName()); //统计页面
		MobclickAgent.onResume(this);          //统计时长
	}
	
	@Override
	protected void onPause() {
		super.onPause();
		MobclickAgent.onPageEnd(this.getClass().getSimpleName());
		MobclickAgent.onPause(this);
	}
}
