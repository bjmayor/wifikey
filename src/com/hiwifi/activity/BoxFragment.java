package com.hiwifi.activity;

import java.util.ArrayList;

import android.content.Context;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.view.animation.AnimationUtils;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.Toast;
import android.widget.ViewSwitcher;

import com.hiwifi.app.adapter.RecommendAdapter;
import com.hiwifi.app.utils.RecentApplicatonUtil;
import com.hiwifi.app.utils.ToastUtil;
import com.hiwifi.constant.RequestConstant.RequestTag;
import com.hiwifi.hiwifi.Gl;
import com.hiwifi.hiwifi.R;
import com.hiwifi.model.RecommendInfo;
import com.hiwifi.model.request.RequestFactory;
import com.hiwifi.model.request.RequestManager.ResponseHandler;
import com.hiwifi.model.request.ServerResponseParser;
import com.hiwifi.model.request.ServerResponseParser.ServerCode;
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
public class BoxFragment extends Fragment implements ResponseHandler,
		OnClickListener {
	private View rootView;
	private GridView reconmendGridView;
	private ArrayList<RecommendInfo> reconmendInfos = RecommendInfo.getList();
	private RecommendAdapter recommendAdapter;
	protected ImageLoader imageLoader = ImageLoader.getInstance();
	private DisplayImageOptions options;

	ViewSwitcher switcher;
	ImageView loadingImageView;
	View networktipView;

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		if (rootView == null) {
			rootView = inflater.inflate(R.layout.tab_box, null);
		}
		// 缓存的rootView需判断是否已经被加过parent，如果有parent需要从parent删除，否则报错
		ViewGroup parent = (ViewGroup) rootView.getParent();
		if (parent != null) {
			parent.removeView(rootView);
		}
		reconmendGridView = (GridView) rootView
				.findViewById(R.id.reconmend_grid);
		switcher = (ViewSwitcher) rootView.findViewById(R.id.vs_network_tips);
		loadingImageView = (ImageView) rootView.findViewById(R.id.iv_loading);
		networktipView = rootView.findViewById(R.id.ll_network_tips);
		networktipView.setOnClickListener(this);
		return rootView;
	}

	@Override
	public void onActivityCreated(Bundle savedInstanceState) {
		super.onActivityCreated(savedInstanceState);
		options = new DisplayImageOptions.Builder().cacheInMemory()
				.cacheOnDisc().displayer(new RoundedBitmapDisplayer(0))
				.bitmapConfig(Bitmap.Config.RGB_565).build();
		recommendAdapter = new RecommendAdapter(getActivity());
		recommendAdapter.setOptions(options);
		recommendAdapter.setImageLoader(imageLoader);
		recommendAdapter.setList(reconmendInfos);
		reconmendGridView.setAdapter(recommendAdapter);
		reconmendGridView.setOnItemClickListener(new OnItemClickListener() {

			@Override
			public void onItemClick(AdapterView<?> parent, View view,
					int position, long id) {
				MobclickAgent.onEvent(getActivity(),
						"click_app_reconmend_open",
						Gl.Ct().getResources().getString(R.string.stat_reconmend_item));
				reconemndOpAction(reconmendInfos.get(position));
			}
		});
	}

	@Override
	public void onResume() {
		super.onResume();
	}

	@Override
	public synchronized void onHiddenChanged(boolean hidden) {
		super.onHiddenChanged(hidden);
		if (!hidden) {
			MobclickAgent.onPageStart(this.getClass().getSimpleName()); //统计页面
			if (RecommendInfo.getList().size() == 0) {
				RequestFactory.getRecommendApps(getActivity(), this);
			} else {
				recommendAdapter = new RecommendAdapter(getActivity());
				recommendAdapter.setOptions(options);
				recommendAdapter.setImageLoader(imageLoader);
				recommendAdapter.setList(RecommendInfo.getList());
				reconmendGridView.setAdapter(recommendAdapter);
			}
		}
		else {
			MobclickAgent.onPageEnd(this.getClass().getSimpleName());
		}
	}

	private void reconemndOpAction(RecommendInfo recommendApp) {
		Context aContext = getActivity();
		if (RecentApplicatonUtil.judgePackageExist(aContext,
				recommendApp.getPackagename())) {
			RecentApplicatonUtil.startAppByPackageName(
					recommendApp.getPackagename(), aContext);
		} else {
			String pathString;
			if ((pathString = RecentApplicatonUtil.hasDownloaded(aContext,
					recommendApp.getDownLoadUrl())) != null) {
				RecentApplicatonUtil.install(aContext, pathString);
			} else if (RecentApplicatonUtil.hasStartDownload(aContext,
					recommendApp.getDownLoadUrl())) {
				ToastUtil.showMessage(aContext, "正在下载");
			} else {
				ToastUtil.showMessage(aContext, "开始下载");
				RecentApplicatonUtil.downLoadApk3(aContext,
						recommendApp.getDownLoadUrl(), recommendApp.getName());
			}
		}
	}

	@Override
	public void onStart(RequestTag tag, Code code) {
		switcher.setVisibility(View.VISIBLE);
		if (code == Code.ok) {
			switcher.setDisplayedChild(1);
			loadingImageView.startAnimation(AnimationUtils.loadAnimation(
					getActivity(), R.anim.rotate_anim));
		} else {
			switcher.setDisplayedChild(0);
			Toast.makeText(getActivity(), "网络异常", Toast.LENGTH_SHORT).show();
		}
	}

	@Override
	public void onSuccess(RequestTag tag, ServerResponseParser responseParser) {
		switch (tag) {
		case HIWIFI_APP_RECOMMEND_GET:
			loadingImageView.clearAnimation();
			switcher.setVisibility(View.GONE);
			if (responseParser.code == ServerCode.OK.value()) {
				RecommendInfo recommendInfo = new RecommendInfo();
				recommendInfo.parse(tag, responseParser);
				// reconmendInfos.clear();
				// reconmendInfos.addAll(RecommendInfo.getList());
				// recommendAdapter.notifyDataSetChanged();
				recommendAdapter = new RecommendAdapter(getActivity());
				recommendAdapter.setOptions(options);
				recommendAdapter.setImageLoader(imageLoader);
				recommendAdapter.setList(RecommendInfo.getList());
				reconmendGridView.setAdapter(recommendAdapter);
			}
			break;

		default:
			break;
		}
	}

	@Override
	public void onFailure(RequestTag tag, Throwable error) {
		switcher.setVisibility(View.VISIBLE);
		switcher.setDisplayedChild(0);
	}

	@Override
	public void onFinish(RequestTag tag) {

	}

	@Override
	public void onClick(View v) {
		switch (v.getId()) {
		case R.id.ll_network_tips:
			RequestFactory.getRecommendApps(getActivity(), this);
			break;

		default:
			break;
		}
	}
}
