package com.hiwifi.app.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageSwitcher;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.hiwifi.app.utils.RecentApplicatonUtil;
import com.hiwifi.hiwifi.Gl;
import com.hiwifi.hiwifi.R;
import com.hiwifi.model.RecommendInfo;
import com.hiwifi.model.log.HWFLog;
import com.hiwifi.utils.ViewUtil;
import com.nostra13.universalimageloader.core.DisplayImageOptions;
import com.nostra13.universalimageloader.core.ImageLoader;

public class RecommendAdapter extends ArrayListAdapter<RecommendInfo> {
	protected static ImageLoader imageLoader;
	private static DisplayImageOptions options;

	public RecommendAdapter(Context context) {
		super(context);
	}

	public void setImageLoader(ImageLoader imageLoader) {
		this.imageLoader = imageLoader;
	}

	public void setOptions(DisplayImageOptions options) {
		this.options = options;
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		ViewHolder viewHolder = null;
		if (convertView == null) {
			convertView = LayoutInflater.from(mContext).inflate(
					R.layout.griditem, null);
			viewHolder = new ViewHolder(convertView);
			convertView.setTag(viewHolder);
		} else {
			viewHolder = (ViewHolder) convertView.getTag();
		}
		viewHolder.configByItem(mList.get(position));
		return convertView;
	}

	static class ViewHolder {
		ImageView appIcon;
		ImageView appType;
		TextView appName;
		ImageSwitcher isDownOrOpenImageSwitcher;
		TextView downloadCounTextView;
		LinearLayout startLevel;

		public ViewHolder(View rootView) {
			appIcon = (ImageView) rootView.findViewById(R.id.app_icon);
			appIcon.getLayoutParams().width = (int) (((ViewUtil
					.getScreenWidth() - 8) / 3 - 20) * 0.8);
			appIcon.getLayoutParams().height = appIcon.getLayoutParams().width;
			appType = (ImageView) rootView.findViewById(R.id.app_type);
			appName = (TextView) rootView.findViewById(R.id.app_name);
			isDownOrOpenImageSwitcher = (ImageSwitcher) rootView
					.findViewById(R.id.is_down_or_open);
			downloadCounTextView = (TextView) rootView
					.findViewById(R.id.tv_download_count);
			startLevel = (LinearLayout) rootView
					.findViewById(R.id.ll_start_level);
		}

		public void configByItem(RecommendInfo item) {
			appIcon.setImageResource(R.drawable.ic_recommend_default);
			if (item.getIcon() != null && !"".equals(item.getIcon())) {
				imageLoader.displayImage(item.getIcon(), appIcon, options);
			}
			appName.setText(item.getName());
			downloadCounTextView.setText(item.getDownLoadNumber());
			if (RecentApplicatonUtil.judgePackageExist(Gl.Ct(),
					item.getPackagename())) {
				isDownOrOpenImageSwitcher.setDisplayedChild(0);
			} else {
				isDownOrOpenImageSwitcher.setDisplayedChild(1);
			}
			int whole = item.getStartLevel() / 2;
			int left = item.getStartLevel() % 2;
			for (int i = 0; i < startLevel.getChildCount(); i++) {
				ImageView imageView = (ImageView) startLevel.getChildAt(i);
				if (i < whole) {
					imageView.setImageResource(R.drawable.istar_light);
				} else if (i > whole) {
					imageView.setImageResource(R.drawable.istar_grey);
				}
				if (i == whole) {
					if (left == 1) {
						imageView.setImageResource(R.drawable.istar_half);
					} else {
						imageView.setImageResource(R.drawable.istar_grey);
					}
				}
			}
			HWFLog.e("debug", item.getStartLevel() + "");
			switch (item.getType()) {
			case None:
				appType.setVisibility(View.INVISIBLE);
				break;
			case Recommend:
				appType.setVisibility(View.VISIBLE);
				appType.setImageResource(R.drawable.tag_push);
				break;
			case Hot:
				appType.setVisibility(View.VISIBLE);
				appType.setImageResource(R.drawable.tag_hottest);
				break;
			case New:
				appType.setVisibility(View.VISIBLE);
				appType.setImageResource(R.drawable.tag_newest);
				break;
			case Basic:
				appType.setVisibility(View.VISIBLE);
				appType.setImageResource(R.drawable.tag_musthave);
				break;
			default:
				break;
			}
		}

	}
}
