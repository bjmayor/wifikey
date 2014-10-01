package com.hiwifi.app.adapter;

import android.content.Context;
import android.content.pm.ApplicationInfo;
import android.content.pm.ComponentInfo;
import android.content.pm.PackageManager;
import android.content.pm.PackageManager.NameNotFoundException;
import android.content.pm.ResolveInfo;
import android.graphics.drawable.Drawable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.hiwifi.hiwifi.R;
import com.hiwifi.store.AppInfoModel;

public class RecentAdapter extends ArrayListAdapter<AppInfoModel> {

	private PackageManager pm;

	public RecentAdapter(Context context) {
		super(context);
		pm = context.getPackageManager();
	}

	@Override
	public int getCount() {
		// TODO Auto-generated method stub
		if (mList != null)
			return mList.size() < 8 ? mList.size() : 8;
		else
			return 0;
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		// TODO Auto-generated method stub
		ViewHolder viewHolder = null;
		if (convertView == null) {
			viewHolder = new ViewHolder();
			convertView = LayoutInflater.from(mContext).inflate(
					R.layout.griditem, null);
			viewHolder.app_icon = (ImageView) convertView
					.findViewById(R.id.app_icon);
			viewHolder.app_name = (TextView) convertView
					.findViewById(R.id.app_name);
			convertView.setTag(viewHolder);
		} else {
			viewHolder = (ViewHolder) convertView.getTag();
		}

		if (mList != null && mList.size() > 0) {
			// viewHolder.app_icon.setImageDrawable(mList.get(position).loadIcon(
			// pm));
			// viewHolder.app_name.setText(mList.get(position).loadLabel(pm)
			// .toString());
			viewHolder.app_icon.setImageDrawable(getAppIcon(mList.get(position).getPackgename()));
			viewHolder.app_name.setText(mList.get(position).getAppname()
					.toString());
		}
		return convertView;
	}

	/*
	 * 获取程序 图标
	 */
	public Drawable getAppIcon(String packname) {
		try {
			ApplicationInfo info = pm.getApplicationInfo(packname, 0);
			return info.loadIcon(pm);
		} catch (NameNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			return null;
		}
	}

	static class ViewHolder {
		ImageView app_icon;
		TextView app_name;
	}

}
