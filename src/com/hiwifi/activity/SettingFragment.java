package com.hiwifi.activity;

import java.util.ArrayList;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.view.animation.AnimationUtils;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.BaseAdapter;
import android.widget.CompoundButton;
import android.widget.Toast;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.ImageView;
import android.widget.TextView;

import com.hiwifi.activity.setting.AboutAppActivity;
import com.hiwifi.activity.setting.TermsOfServiceActivity;
import com.hiwifi.activity.test.TestCenterActivity;
import com.hiwifi.activity.user.UserInfoActivity;
import com.hiwifi.activity.user.UserLoginActivity;
import com.hiwifi.activity.user.VeryPhoneActivity.Source;
import com.hiwifi.app.utils.RecentApplicatonUtil;
import com.hiwifi.app.views.CircleImageView;
import com.hiwifi.app.views.MyListView;
import com.hiwifi.app.views.SwitchButton;
import com.hiwifi.constant.ConfigConstant;
import com.hiwifi.constant.ReleaseConstant;
import com.hiwifi.constant.RequestConstant;
import com.hiwifi.constant.RequestConstant.RequestTag;
import com.hiwifi.hiwifi.Gl;
import com.hiwifi.hiwifi.R;
import com.hiwifi.model.AppSession;
import com.hiwifi.model.ClientInfo;
import com.hiwifi.model.User;
import com.hiwifi.model.log.LogUtil;
import com.hiwifi.model.request.RequestFactory;
import com.hiwifi.model.request.RequestManager.ResponseHandler;
import com.hiwifi.model.request.ServerResponseParser;
import com.hiwifi.model.router.Router;
import com.hiwifi.model.router.RouterManager;
import com.nostra13.universalimageloader.core.DisplayImageOptions;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.display.RoundedBitmapDisplayer;
import com.umeng.analytics.MobclickAgent;

/**
 * @filename ConnectFragment.java
 * @packagename com.hiwifi.activity
 * @projectname hiwifi1.0.1
 * @author jack at 2014-8-25
 */
public class SettingFragment extends Fragment implements OnClickListener,
		ResponseHandler, OnItemClickListener, OnCheckedChangeListener {

	public TextView userLoginTextView;
	View routerContainer;
	TextView firstRouterTextView;
	TextView versionTextView;
	ImageView routerArrowView;
	ImageView appNewImageView;
	CircleImageView avatarImageView;
	View loginedView;
	View unloginView;
	MyListView routerListView;
	RouterAdapter routerAdapter;
	SwitchButton shareSwitchButton;
	SwitchButton backupSwitchButton;
	ArrayList<Router> routers = new ArrayList<Router>();
	View rootView;

	private DisplayImageOptions options = new DisplayImageOptions.Builder()
			.cacheInMemory().cacheOnDisc()
			.displayer(new RoundedBitmapDisplayer(0))
			.bitmapConfig(Bitmap.Config.RGB_565).build();

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		rootView = inflater.inflate(R.layout.tab_settings, null);
		userLoginTextView = (TextView) rootView
				.findViewById(R.id.tv_user_status);
		routerContainer = rootView.findViewById(R.id.ll_router);
		loginedView = rootView.findViewById(R.id.ll_user_logined);
		unloginView = rootView.findViewById(R.id.ll_user_unlogin);
		avatarImageView = (CircleImageView) rootView
				.findViewById(R.id.iv_avatar);

		firstRouterTextView = (TextView) rootView
				.findViewById(R.id.tv_router_first);
		routerArrowView = (ImageView) rootView
				.findViewById(R.id.iv_router_arrow);
		appNewImageView = (ImageView) rootView.findViewById(R.id.iv_app_new);
		routerListView = (MyListView) rootView.findViewById(R.id.ll_routers);
		shareSwitchButton = (SwitchButton) rootView
				.findViewById(R.id.sb_auto_share);
		backupSwitchButton = (SwitchButton) rootView
				.findViewById(R.id.sb_auto_backup);
		shareSwitchButton.setOnCheckedChangeListener(this);
		backupSwitchButton.setOnCheckedChangeListener(this);
		shareSwitchButton.setChecked(ClientInfo.shareInstance().isAutoShared());
		backupSwitchButton
				.setChecked(ClientInfo.shareInstance().isAutoBackup());
		versionTextView = (TextView) rootView.findViewById(R.id.tv_version);
		routerListView.setOnItemClickListener(this);
		rootView.findViewById(R.id.ll_about).setOnClickListener(this);
		rootView.findViewById(R.id.ll_agreement).setOnClickListener(this);
		rootView.findViewById(R.id.ll_version).setOnClickListener(this);
		rootView.findViewById(R.id.ll_router).setOnClickListener(this);
		rootView.findViewById(R.id.ll_user_status).setOnClickListener(this);
		if (ReleaseConstant.ISDEBUG) {
			rootView.findViewById(R.id.ll_test).setOnClickListener(this);
		} else {
			rootView.findViewById(R.id.ll_test).setVisibility(View.GONE);
		}
		rootView.findViewById(R.id.feedback).setOnClickListener(this);
		return rootView;
	}

	@Override
	public void onActivityCreated(Bundle savedInstanceState) {
		super.onActivityCreated(savedInstanceState);
		routerListView.setAdapter(routerAdapter = new RouterAdapter(
				this.routers));
	}

	@Override
	public void onResume() {
		super.onResume();
		versionTextView.setText("v" + Gl.getAppVersionName());
		MobclickAgent.onPageStart(this.getClass().getSimpleName()); //统计页面
		if (TextUtils.isEmpty(User.shareInstance().getAvatarUrl())) {
			RequestFactory.getUserInfo(getActivity(), this);
		}
		updateView();
		RequestFactory.checkAppUpGrade(getActivity(), this);
	}
	
	@Override
	public void onHiddenChanged(boolean hidden) {
		super.onHiddenChanged(hidden);
		if (hidden) {
			MobclickAgent.onPageEnd(this.getClass().getSimpleName());
		}
		else
		{
			MobclickAgent.onPageStart(this.getClass().getSimpleName()); //统计页面
			if (TextUtils.isEmpty(User.shareInstance().getAvatarUrl())) {
				RequestFactory.getUserInfo(getActivity(), this);
			}
			updateView();
			RequestFactory.checkAppUpGrade(getActivity(), this);
		}
	}

	@Override
	public void onPause() {
		super.onPause();
		MobclickAgent.onPageEnd(this.getClass().getSimpleName());
	}

	private void updateView() {
		if (User.shareInstance().hasLogin()) {
			RequestFactory.getRouters(getActivity(), this);

			userLoginTextView.setText(User.shareInstance().getUserName());
			loginedView.setVisibility(View.VISIBLE);
			unloginView.setVisibility(View.GONE);
			if (!TextUtils.isEmpty(User.shareInstance().getAvatarUrl())) {
				ImageLoader.getInstance().displayImage(
						User.shareInstance().getAvatarUrl(), avatarImageView,
						options);
			}
		} else {
			loginedView.setVisibility(View.GONE);
			unloginView.setVisibility(View.VISIBLE);
			this.routers = null;
			this.routerAdapter.setRouters(this.routers);
		}
	}

	@Override
	public void onClick(View v) {
		Intent intent = new Intent();
		switch (v.getId()) {
		case R.id.ll_user_status:
			if (!User.shareInstance().hasLogin()) {
				MobclickAgent.onEvent(getActivity(), "click_user_login",
						"setting");
				intent.setClass(getActivity(), UserLoginActivity.class);
				getActivity().startActivity(intent);
			} else {
				MobclickAgent.onEvent(getActivity(), "click_user_info",
						getString(R.string.click_user_info));
				intent.setClass(getActivity(), UserInfoActivity.class);
				AppSession.source = Source.SourceIsViewInfo;
				getActivity().startActivity(intent);
			}
			break;
		case R.id.ll_version:
			if (ClientInfo.shareInstance().appNeedUpdate()) {
				Intent upgrate = new Intent();
				upgrate.setAction("android.intent.action.VIEW");
				Uri content_url = Uri.parse(RequestConstant
						.getUrl(RequestTag.URL_APP_DOWNLOAD));
				upgrate.setData(content_url);
				startActivity(upgrate);
			} else {
				RequestFactory.checkAppUpGrade(getActivity(), this);
			}

			break;
		case R.id.ll_about:
			intent.setClass(getActivity(), AboutAppActivity.class);
			getActivity().startActivity(intent);
			break;

		case R.id.feedback:
			intent.setClass(getActivity(), FeedbackActivity.class);
			getActivity().startActivity(intent);

			break;
		case R.id.ll_agreement:
			intent.setClass(getActivity(), TermsOfServiceActivity.class);
			getActivity().startActivity(intent);
			break;
		case R.id.ll_router:
			MobclickAgent.onEvent(getActivity(), "click_routers",
					routerListView.isShown() ? "收起" : "打开");
			if (routerListView.isShown()) {
				routerListView.setVisibility(View.GONE);
				routerArrowView.startAnimation(AnimationUtils.loadAnimation(
						getActivity(), R.anim.arrow_anim_toright));
			} else {
				routerListView.setVisibility(View.VISIBLE);
				routerArrowView.startAnimation(AnimationUtils.loadAnimation(
						getActivity(), R.anim.arrow_anim_tobottom));
			}
			break;

		case R.id.ll_test:
			intent.setClass(getActivity(), TestCenterActivity.class);
			getActivity().startActivity(intent);
			break;
		default:
			break;
		}
	}

	public class RouterAdapter extends BaseAdapter {
		LayoutInflater mInflater;
		ArrayList<Router> routers;

		public RouterAdapter(ArrayList<Router> routers) {
			if (routers != null) {
				LogUtil.e("bbbbb", routers.toString());
			} else {
				LogUtil.e("bbbbb", "no data");
			}
			this.routers = routers;
			mInflater = (LayoutInflater) Gl.Ct().getSystemService(
					Context.LAYOUT_INFLATER_SERVICE);
		}

		public void setRouters(ArrayList<Router> routers) {
			this.routers = routers;
			notifyDataSetChanged();
		}

		@Override
		public int getCount() {
			return this.routers != null ? this.routers.size() + 1 : 1;
		}

		@Override
		public Object getItem(int position) {
			return null;
		}

		@Override
		public long getItemId(int position) {
			return 0;
		}

		@Override
		public View getView(int position, View convertView, ViewGroup parent) {
			Router router = null;
			if (position > 0) {
				position--;
				router = this.routers.get(position);
			}
			View view = null;
			ViewHolder holder = null;
			if (convertView != null) {
				view = convertView;
				holder = (ViewHolder) view.getTag();
			} else {
				view = mInflater.inflate(R.layout.item_router, null);
				holder = new ViewHolder(view);
				view.setTag(holder);
			}
			holder.configByRouter(router);
			return view;
		}

		public class ViewHolder {
			View rootView;
			TextView nameTextView;
			TextView statusTextView;

			public ViewHolder(View rootView) {
				this.rootView = rootView;
				nameTextView = (TextView) rootView
						.findViewById(R.id.tv_router_name);
				statusTextView = (TextView) rootView
						.findViewById(R.id.tv_router_status);
			}

			public void configByRouter(Router router) {
				statusTextView.setText("");
				if (router != null) {
					nameTextView.setText(router.getAliasName());
				} else {
					if (User.shareInstance().hasLogin()) {
						nameTextView.setText(String.format("已绑定%d台，每天可查看%d次",
								getCount() - 1, (getCount() - 1) * 3 + 1));
					} else {
						nameTextView.setText("每绑定1台，每天可增加3次查看机会");
					}
				}

			}
		}

	}

	@Override
	public void onStart(RequestTag tag, Code code) {

	}

	@Override
	public void onSuccess(RequestTag tag, ServerResponseParser responseParser) {
		switch (tag) {
		case URL_ROUTER_LIST_GET:
			if (!tag.getParams().get("token")
					.equalsIgnoreCase(User.shareInstance().getToken())) {
				return;
			}
			this.routers = RouterManager.shareInstance().setRouters(
					responseParser.originResponse);
			LogUtil.e("aaaaa", responseParser.originResponse.toString());
			this.routerListView.setAdapter(routerAdapter = new RouterAdapter(
					this.routers));
			break;
		case URL_APP_UPDATE_CHECK:
			ClientInfo.shareInstance().parse(tag, responseParser);
			if (ClientInfo.shareInstance().appNeedUpdate()) {
				appNewImageView.setVisibility(View.VISIBLE);
			} else {
				appNewImageView.setVisibility(View.INVISIBLE);
			}
			break;

		case URL_USER_INFO_GET:
			User.shareInstance().parse(tag, responseParser);
			updateView();
			break;
		default:
			break;
		}
	}

	@Override
	public void onFailure(RequestTag tag, Throwable error) {

	}

	@Override
	public void onFinish(RequestTag tag) {

	}

	@Override
	public void onItemClick(AdapterView<?> parent, View view, int position,
			long id) {
		MobclickAgent.onEvent(getActivity(), "click_router", getResources()
				.getString(R.string.click_router));
		if (RecentApplicatonUtil.judgePackageExist(Gl.Ct(),
				ConfigConstant.HIWIFI_ADMIN_PACKAGE)) {
			RecentApplicatonUtil.startAppByPackageName(
					ConfigConstant.HIWIFI_ADMIN_PACKAGE, getActivity());
		} else {
			Intent intent = new Intent();
			intent.setAction("android.intent.action.VIEW");
			Uri content_url = Uri.parse(RequestConstant
					.getUrl(RequestTag.URL_APP_DOWNLOAD));
			intent.setData(content_url);
			startActivity(intent);
		}
	}

	@Override
	public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
		switch (buttonView.getId()) {
		case R.id.sb_auto_share:
			ClientInfo.shareInstance().setAutoShared(isChecked);
			break;
		case R.id.sb_auto_backup:
			ClientInfo.shareInstance().setAutoBackup(isChecked);
			if (!isChecked) {
				Toast.makeText(getActivity(), "建议开启该功能，换手机也可自动同步WiFi本地密码",
						Toast.LENGTH_LONG).show();
			}
			break;

		default:
			break;
		}
	}

}
