package com.hiwifi.activity;

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.view.animation.AnimationUtils;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.Toast;
import android.widget.ViewSwitcher;

import com.hiwifi.app.adapter.DiscoverAdapter;
import com.hiwifi.constant.RequestConstant.RequestTag;
import com.hiwifi.hiwifi.R;
import com.hiwifi.model.DiscoverItem;
import com.hiwifi.model.log.HWFLog;
import com.hiwifi.model.request.RequestFactory;
import com.hiwifi.model.request.RequestManager.ResponseHandler;
import com.hiwifi.model.request.ServerResponseParser;
import com.umeng.analytics.MobclickAgent;

/**
 * @filename ConnectFragment.java
 * @packagename com.hiwifi.activity
 * @projectname hiwifi1.0.1
 * @author jack at 2014-8-25
 */
public class DiscoverFragment extends Fragment implements ResponseHandler,
		OnItemClickListener, OnClickListener {
	private View rootView;
	private ListView listView;
	private DiscoverAdapter adapter;
	ViewSwitcher switcher;
	ImageView loadingImageView;
	View networktipView;

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		if (rootView == null) {
			rootView = inflater.inflate(R.layout.tab_discover, null);
		}
		// 缓存的rootView需判断是否已经被加过parent，如果有parent需要从parent删除，否则报错
		ViewGroup parent = (ViewGroup) rootView.getParent();
		if (parent != null) {
			parent.removeView(rootView);
		}
		listView = (ListView) rootView.findViewById(R.id.lv_discover);

		switcher = (ViewSwitcher) rootView.findViewById(R.id.vs_network_tips);
		loadingImageView = (ImageView) rootView.findViewById(R.id.iv_loading);
		networktipView = rootView.findViewById(R.id.ll_network_tips);
		networktipView.setOnClickListener(this);
		return rootView;
	}

	@Override
	public void onActivityCreated(Bundle savedInstanceState) {
		super.onActivityCreated(savedInstanceState);
		adapter = new DiscoverAdapter(getActivity(), DiscoverItem.getList());
		listView.setAdapter(adapter);
		listView.setOnItemClickListener(this);
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
			if (DiscoverItem.getList().size() == 0) {
				RequestFactory.getDiscoverList(getActivity(), this);
			} else {
				adapter = new DiscoverAdapter(getActivity(),
						DiscoverItem.getList());
				listView.setAdapter(adapter);
				adapter.notifyDataSetChanged();
			}
		}
		else
		{
			MobclickAgent.onPageEnd(this.getClass().getSimpleName());
		}
	}

	@Override
	public void onStart(RequestTag tag, Code code) {
		HWFLog.e("debug", tag.toString() + code.toString());
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
		loadingImageView.clearAnimation();
		switcher.setVisibility(View.GONE);
		new DiscoverItem().parse(tag, responseParser);
		adapter = new DiscoverAdapter(getActivity(), DiscoverItem.getList());
		listView.setAdapter(adapter);
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
	public void onItemClick(AdapterView<?> parent, View view, int position,
			long id) {
		DiscoverItem item = (DiscoverItem) adapter.getItem(position);
		HWFLog.e("debug", item.toString());
		Intent intent = new Intent();
		intent.setClass(getActivity(), CommonWebviewActivity.class);
		intent.putExtra(CommonWebviewActivity.bnlKeyTitle, item.getTitle());
		intent.putExtra(CommonWebviewActivity.bnlKeyUrl, item.getDetailUrl());
		intent.putExtra(CommonWebviewActivity.bnlKeyFrom, "discover");
		intent.putExtra("position", position);
		getActivity().startActivity(intent);
	}

	@Override
	public void onClick(View v) {
		switch (v.getId()) {
		case R.id.ll_network_tips:
			RequestFactory.getDiscoverList(getActivity(), this);
			break;

		default:
			break;
		}
	}

}
