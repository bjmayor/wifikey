package com.hiwifi.app.adapter;

import java.net.URI;
import java.util.ArrayList;

import com.hiwifi.activity.SettingFragment.RouterAdapter.ViewHolder;
import com.hiwifi.hiwifi.R;
import com.hiwifi.model.DiscoverItem;
import com.hiwifi.utils.ViewUtil;
import com.nostra13.universalimageloader.core.DisplayImageOptions;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.display.RoundedBitmapDisplayer;

import android.content.Context;
import android.graphics.Bitmap;
import android.text.TextUtils;
import android.text.format.DateFormat;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AbsListView.LayoutParams;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

public class DiscoverAdapter extends BaseAdapter {
	private ArrayList<DiscoverItem> data;
	private Context mContext;
	private LayoutInflater mInflater;
	protected ImageLoader imageLoader = ImageLoader.getInstance();
	private DisplayImageOptions options;

	// private
	public DiscoverAdapter(Context context, ArrayList<DiscoverItem> list) {
		data = list;
		mContext = context;
		mInflater = LayoutInflater.from(mContext);
		options = new DisplayImageOptions.Builder().cacheOnDisc()
				.displayer(new RoundedBitmapDisplayer(0))
				.bitmapConfig(Bitmap.Config.RGB_565).build();
	}

	@Override
	public int getCount() {
		return data == null ? 0 : data.size();
	}

	@Override
	public Object getItem(int position) {
		return data == null ? null : data.get(position);
	}

	@Override
	public long getItemId(int position) {
		return 0;
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		DiscoverItem item = (DiscoverItem) getItem(position);
		ViewHolder holder = null;
		View view = null;
		if (convertView != null) {
			view = convertView;
			holder = (ViewHolder) convertView.getTag();
		} else {
			view = mInflater.inflate(R.layout.item_discover, null);
			int height = (int) (ViewUtil.getScreenWidth() * (320.0 / 720.0));
			LayoutParams params = new LayoutParams(ViewUtil.getScreenWidth(),
					height);

			view.setLayoutParams(params);
			holder = new ViewHolder(view);
			view.setTag(holder);
		}
		holder.configByData(item);
		return view;
	}

	public class ViewHolder {
		TextView titleTextView;
		TextView timeTextView;
		ImageView imageView;

		public ViewHolder(View rootView) {
			titleTextView = (TextView) rootView
					.findViewById(R.id.tv_discover_title);
			timeTextView = (TextView) rootView
					.findViewById(R.id.tv_discover_time);
			imageView = (ImageView) rootView
					.findViewById(R.id.iv_discover_logo);
		}

		public void configByData(DiscoverItem item) {
			titleTextView.setText(item.getTitle());
			timeTextView.setText(new DateFormat().format("yyyy-MM-dd",
					item.getCreateTime()));
			imageView.setImageResource(R.drawable.discovery_default);
			if (!TextUtils.isEmpty(item.getImgUrl())) {
				imageLoader.displayImage(item.getImgUrl(), imageView, options);
			}
		}

	}
}
