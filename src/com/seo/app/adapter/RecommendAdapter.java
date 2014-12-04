package com.seo.app.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageSwitcher;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;


import com.nostra13.universalimageloader.core.DisplayImageOptions;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.seo.wifikey.R;

import java.util.List;

import cn.waps.AdInfo;
import cn.waps.AppConnect;

/*
String adId = adInfo.getAdId(); //广告 id
//            String adName = adInfo.getAdName(); //广告标题
//            String adText = adInfo.getAdText(); //广告诧文字
//            Bitmap adIcon = adInfo.getAdIcon(); //广告图标(48*48 像素)
//            int adPoint = adInfo.getAdPoints(); //广告积分
//            String description = adInfo.getDescription(); //应用描述
//            String version = adInfo.getVersion(); //程序版本
//            String filesize = adInfo.getFilesize(); //安装包大小
//            String provider = adInfo.getProvider();//应用提供商
//            String[] imageUrls = adInfo.getImageUrls(); //应用截图癿 url 数组，每个应用 2 张截图
//            String adPackage = adInfo.getAdPackage();//广告应用包名
//            String action = adInfo.getAction(); //用亍存储“安装”或“注册”癿字段
 */
public class RecommendAdapter extends BaseAdapter {
    protected static ImageLoader imageLoader;
    private static DisplayImageOptions options;
    private Context mContext;
    private List<AdInfo> mData;

    public RecommendAdapter(Context context, List<AdInfo> mData) {
        this.mContext = context;
        this.mData = mData;
    }

    public void setImageLoader(ImageLoader imageLoader) {
        this.imageLoader = imageLoader;
    }

    public void setOptions(DisplayImageOptions options) {
        this.options = options;
    }

    @Override
    public int getCount() {
        if (mData != null) {
            return mData.size();
        } else {
            return 0;
        }
    }

    @Override
    public AdInfo getItem(int i) {
        return mData.get(i);
    }

    @Override
    public long getItemId(int i) {
        return 0;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        ViewHolder viewHolder = null;
        if (convertView == null) {
            convertView = LayoutInflater.from(mContext).inflate(
                    R.layout.item_app, null);
            viewHolder = new ViewHolder(convertView);
            convertView.setTag(viewHolder);
        } else {
            viewHolder = (ViewHolder) convertView.getTag();
        }
        viewHolder.configByItem(getItem(position));
        return convertView;
    }

    static class ViewHolder implements View.OnClickListener {
        ImageView appIcon;
        TextView appName;
        TextView appDescription;
        TextView appSize;
        ImageView downloadIcon;
        AdInfo item;

        public ViewHolder(View rootView) {
            appIcon = (ImageView) rootView.findViewById(R.id.app_icon);
            downloadIcon = (ImageView) rootView.findViewById(R.id.iv_download);
            appName = (TextView) rootView.findViewById(R.id.app_name);
            appDescription = (TextView) rootView.findViewById(R.id.app_description);
            appSize = (TextView) rootView.findViewById(R.id.app_size);
            downloadIcon.setOnClickListener(this);
        }

        public void configByItem(AdInfo item) {
            this.item = item;
            appName.setText(item.getAdName());
            appIcon.setImageBitmap(item.getAdIcon());
            appDescription.setText(item.getAdText());
            appSize.setText(item.getFilesize() + "M");
        }

        @Override
        public void onClick(View view) {
            AppConnect.getInstance(view.getContext()).downloadAd(view.getContext(), this.item.getAdId());
        }
    }
}
