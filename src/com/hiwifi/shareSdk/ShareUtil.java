/*
 * 官网地站:http://www.ShareSDK.cn
 * 技术支持QQ: 4006852216
 * 官方微信:ShareSDK   （如果发布新版本的话，我们将会第一时间通过微信将版本更新内容推送给您。如果使用过程中有任何问题，也可以通过微信与我们取得联系，我们将会在24小时内给予回复）
 *
 * Copyright (c) 2013年 ShareSDK.cn. All rights reserved.
 */

package com.hiwifi.shareSdk;

import java.io.File;
import java.io.FileOutputStream;
import java.util.HashMap;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Bitmap.CompressFormat;
import android.graphics.BitmapFactory;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.text.TextUtils;
import android.view.View.OnClickListener;
import android.widget.Toast;
import cn.sharesdk.framework.Platform;
import cn.sharesdk.framework.PlatformActionListener;
import cn.sharesdk.framework.ShareSDK;
import cn.sharesdk.framework.TitleLayout;
import cn.sharesdk.onekeyshare.OnekeyShare;
import cn.sharesdk.wechat.friends.Wechat;
import cn.sharesdk.wechat.moments.WechatMoments;

import com.hiwifi.constant.ConfigConstant;
import com.hiwifi.constant.RequestConstant;
import com.hiwifi.constant.RequestConstant.RequestTag;
import com.hiwifi.hiwifi.Gl;
import com.hiwifi.hiwifi.R;

/**
 * Share SDK接口演示页面，包括演示使用快捷分享完成图文分享、 无页面直接分享、授权、关注和不同平台的分享等等功能。
 */
public class ShareUtil extends OnekeyShare implements OnClickListener,
		PlatformActionListener {
	private static final String TAG = "ShareUtil";
	private TitleLayout llTitle;
	public Handler handler;
	public static String IMAGE_PATH;
	private Context context;
	private HashMap<String, Object> reqMap;

	public ShareUtil(Context con) {
		handler = new Handler(this);
		this.context = con;
		ShareSDK.initSDK(this.context);
	}

	public ShareUtil(Context con, Handler handler) {
		this.handler = handler;
		this.context = con;
		ShareSDK.initSDK(this.context);
	}

	public void onekeyShare(boolean silent, HashMap<String, Object> rep) {
		this.reqMap = rep;
		onekeyShare(silent, null, reqMap);
	}

	@Deprecated
	public void onekeyShare(boolean silent, String platform,
			HashMap<String, Object> rep) {
		this.reqMap = rep;
		if (reqMap != null || reqMap.size() == 0) {
			final OnekeyShare oks = new OnekeyShare();
			oks.setNotification(
					(Integer) reqMap.get("icon_id") == null ? 0 : 0,
					(String) reqMap.get("notifi_title"));// 分享时Notification的图标和文字
			String address = (String) reqMap.get("address");
			if (!TextUtils.isEmpty(address))
				oks.setAddress(address);// 接收人的地址--信息和邮件
			String title = (String) reqMap.get("title");
			if (!TextUtils.isEmpty(title))
				oks.setTitle(title);// 微信，好友圈和QQ空间
			String titleUrl = (String) reqMap.get("titleUrl");
			if (!TextUtils.isEmpty(titleUrl))
				oks.setTitleUrl(titleUrl);// 标题的链接QQ空间使用
			String text = (String) reqMap.get("text");
			if (!TextUtils.isEmpty(text))
				oks.setText(text);// 分享文本----大小有限制 QQ40个字符
			String imagePath = (String) reqMap.get("imagePath");
			if (!TextUtils.isEmpty(imagePath))
				oks.setImagePath(imagePath);// 本地文件路径
			String imageUrl = (String) reqMap.get("imageUrl");
			if (!TextUtils.isEmpty(imageUrl))
				oks.setImageUrl(imageUrl);
			String url = (String) reqMap.get("url");
			if (!TextUtils.isEmpty(url))
				oks.setUrl(url);// 微信中可用
			String filePath = (String) reqMap.get("filePath");
			if (!TextUtils.isEmpty(filePath))
				oks.setFilePath(filePath);// 分享应用的本地路径--微信好友可用
			String comment = (String) reqMap.get("comment");
			if (!TextUtils.isEmpty(comment))
				oks.setComment(comment);// 对分享的评价，在QQ空间可用
			String site = (String) reqMap.get("site");
			if (!TextUtils.isEmpty(site))
				oks.setSite(site);// 分享内容的网站名称--QQ空间可用
			String siteUrl = (String) reqMap.get("siteUrl");
			if (!TextUtils.isEmpty(siteUrl))
				oks.setSiteUrl(siteUrl);// QQ空间使用
			String venueName = (String) reqMap.get("venueName");
			if (!TextUtils.isEmpty(venueName))
				oks.setVenueName(venueName);// 分享时的地名
			String venueDescription = (String) reqMap.get("venueDescription");
			if (!TextUtils.isEmpty(venueDescription))
				oks.setVenueDescription(venueDescription);// 分享时地方描述

			float latitude = reqMap.get("latitude") == null ? 0.0f
					: (Float) reqMap.get("latitude");
			if (latitude != 0.0f)
				oks.setLatitude(latitude);
			float longitude = reqMap.get("longitude") == null ? 0.0f
					: (Float) reqMap.get("longitude");
			if (longitude != 0.0f)
				oks.setLongitude(longitude);
			// oks.onKeyEvent(keyCode, event);//授权页面物理按键的回调
			oks.setSilent(true);// 是否直接分享--false
			if (platform != null) {
				oks.setPlatform(platform);
			}

			// 去除注释，可令编辑页面显示为Dialog模式
			// oks.setDialogMode();

			// 去除注释，则快捷分享的操作结果将通过OneKeyShareCallback回调
			oks.setCallback(this);
			// oks.setShareContentCustomizeCallback(new
			// ShareContentCustomizeDemo());

			// 去除注释，演示在九宫格设置自定义的图标
			// Bitmap logo =
			// BitmapFactory.decodeResource(context.getResources(),
			// R.drawable.ic_launcher);
			// String label =
			// context.getResources().getString(R.string.app_name);
			// OnClickListener listener = new OnClickListener() {
			// public void onClick(View v) {
			// String text = "Customer Logo -- Share SDK " +
			// ShareSDK.getSDKVersionName();
			// Toast.makeText(context, text, Toast.LENGTH_SHORT).show();
			// oks.finish();
			// }
			// };
			// oks.setCustomerLogo(logo, label, listener);

			oks.show(context);
		}
	}

	// 使用快捷分享完成分享（请务必仔细阅读位于SDK解压目录下Docs文件夹中OnekeyShare类的JavaDoc）
	public void showShare(boolean silent, String platform, String text,
			int resourceId) {
		if (TextUtils.isEmpty(ConfigConstant.IMAGE_PATH)) {
			initImagePath(resourceId);
		}
		final OnekeyShare oks = new OnekeyShare(handler);
		oks.setNotification(R.drawable.hiwifi_launcher, Gl.Ct().getResources()
				.getString(R.string.app_name));// 分享时Notification的图标和文字
		// oks.setAddress("12345678901");//接收人的地址--信息和邮件
		if (platform.equals(Wechat.NAME)) {
			oks.setTitle(text);
		} else {
			oks.setTitle("用天天WiFi 天天免费上网");
		}
		oks.setTitleUrl(RequestConstant
				.getUrl(RequestTag.HIWIFI_PAGE_DOWNLOADAPP));// 标题的链接QQ空间使用
		oks.setText(text);// 分享文本
		oks.setImagePath(ConfigConstant.IMAGE_PATH);// 本地文件路径
		// System.out.println("aaaaaaaaaaaaaaaapath==="+Constants.IMAGE_PATH);
		// oks.setImageUrl("http://img.appgo.cn/imgs/sharesdk/content/2013/07/25/1374723172663.jpg");
		oks.setUrl(RequestConstant.getUrl(RequestTag.HIWIFI_PAGE_DOWNLOADAPP));// 微信中可用
		oks.setFilePath(ConfigConstant.IMAGE_PATH);// 分享应用的本地路径--微信好友可用
		// oks.setComment("让上网更痛快");// 对分享的评价，在QQ空间可用
		// oks.setSite("hiwifi");// 分享内容的网站名称--QQ空间可用
		// oks.setSiteUrl("http://www.hiwifi.com/mobile");// QQ空间使用
		// oks.setVenueName("Share SDK");//分享时的地名
		// oks.setVenueDescription("This is a beautiful place!");//分享时地方描述
		// oks.setLatitude(23.056081f);
		// oks.setLongitude(113.385708f);
		// oks.onKeyEvent(keyCode, event);授权页面物理按键的回调
		oks.setSilent(silent);// 是否直接分享--false
		if (platform != null) {
			oks.setPlatform(platform);
		}
		// 去除注释，可令编辑页面显示为Dialog模式
		// oks.setDialogMode();

		// 去除注释，则快捷分享的操作结果将通过OneKeyShareCallback回调
		oks.setCallback(this);
		// oks.setShareContentCustomizeCallback(new
		// ShareContentCustomizeDemo());

		// 去除注释，演示在九宫格设置自定义的图标
		// Bitmap logo = BitmapFactory.decodeResource(context.getResources(),
		// R.drawable.ic_launcher);
		// String label = context.getResources().getString(R.string.app_name);
		// OnClickListener listener = new OnClickListener() {
		// public void onClick(View v) {
		// String text = "Customer Logo -- Share SDK " +
		// ShareSDK.getSDKVersionName();
		// Toast.makeText(context, text, Toast.LENGTH_SHORT).show();
		// oks.finish();
		// }
		// };
		// oks.setCustomerLogo(logo, label, listener);

		oks.show(context);
	}

	public void showShare(String platform, String text, String title,String url,String imagePath) {
		final OnekeyShare oks = new OnekeyShare(handler);
		oks.setNotification(R.drawable.hiwifi_launcher, Gl.Ct().getResources()
				.getString(R.string.app_name));// 分享时Notification的图标和文字
		if (!TextUtils.isEmpty(title)) {
			oks.setTitle(title);
		} else {
//			oks.setTitle("分享给好友");//TODO 默认
		}
		if (!TextUtils.isEmpty(url)) {
			oks.setTitleUrl(url);// 标题的链接QQ空间使用
			oks.setUrl(url);
		}else {
			oks.setTitleUrl(RequestConstant
					.getUrl(RequestTag.URL_APP_DOWNLOAD));// 标题的链接QQ空间使用
//			oks.setUrl(RequestConstant.getUrl(RequestTag.HIWIFI_PAGE_DOWNLOADAPP));// 微信中可用
		}
		if (!TextUtils.isEmpty(text)) {
			oks.setText(text);// 分享文本
		}else {
//			oks.setText("让上网更痛快");
		}
		if (!TextUtils.isEmpty(imagePath)) {
			oks.setImagePath(imagePath);// 本地文件路径			
		}else {
//			initImagePath(R.drawable.wifikey_icon_share);
//			oks.setImagePath(ConfigConstant.IMAGE_PATH);// 本地文件路径
		}

		// oks.setFilePath(ConfigConstant.IMAGE_PATH);// 分享应用的本地路径--微信好友可用
		oks.setComment("让上网更痛快");// 对分享的评价，在QQ空间可用
		oks.setSite("小极WiFi钥匙");// 分享内容的网站名称--QQ空间可用
		oks.setSiteUrl("http://www.hiwifi.com/mobile");// QQ空间使用
		oks.setSilent(true);// 是否直接分享--false
		if (platform != null) {
			oks.setPlatform(platform);
		}
		oks.setCallback(this);
		oks.show(context);
	}
	
	

	public void onComplete(Platform plat, int action,
			HashMap<String, Object> res) {
		Message msg = new Message();
		msg.arg1 = 1;
		msg.arg2 = action;
		msg.obj = plat;
		handler.sendMessage(msg);
	}

	public void onCancel(Platform palt, int action) {
		Message msg = new Message();
		msg.arg1 = 3;
		msg.arg2 = action;
		msg.obj = palt;
		handler.sendMessage(msg);
	}

	public void onError(Platform palt, int action, Throwable t) {
		t.printStackTrace();
		Message msg = new Message();
		msg.arg1 = 2;
		msg.arg2 = action;
		msg.obj = palt;
		handler.sendMessage(msg);
	}

	/** 处理操作结果 */
	public boolean handleMessage(Message msg) {
		Platform plat = (Platform) msg.obj;
		String text = "";
		switch (msg.arg1) {
		case 1: {
			// 成功
			text = "分享成功";
		}
			break;
		case 2: {
			// 失败
			text = "分享失败";
		}
			break;
		case 3: {
			// 取消
			// text = plat.getName() + " canceled at " + text;
			text = "取消分享";
		}
			break;
		}
		if (!"QQ".equals(plat.getName())) {
			Toast.makeText(context, text, Toast.LENGTH_SHORT).show();
		} else {
			Toast.makeText(context, "分享到QQ", Toast.LENGTH_SHORT).show();
		}
		// handler.sendMessage(msg);
		return false;
	}

	public void initImagePath(int path) {
		try {
			if (Environment.MEDIA_MOUNTED.equals(Environment
					.getExternalStorageState())
					&& Environment.getExternalStorageDirectory().exists()) {
				ConfigConstant.IMAGE_PATH = Environment
						.getExternalStorageDirectory().getAbsolutePath()
						+ ConfigConstant.FILE_NAME;
			} else {
				ConfigConstant.IMAGE_PATH = context.getFilesDir()
						.getAbsolutePath() + ConfigConstant.FILE_NAME;
			}
			File file = new File(ConfigConstant.IMAGE_PATH);
			if (!file.exists()) {
				file.createNewFile();
				Bitmap pic = BitmapFactory.decodeResource(
						context.getResources(), path);
				FileOutputStream fos = new FileOutputStream(file);
				pic.compress(CompressFormat.JPEG, 100, fos);
				fos.flush();
				fos.close();
			}
		} catch (Throwable t) {
			t.printStackTrace();
			IMAGE_PATH = null;
		}
	}

	/** 将action转换为String */
	public static String actionToString(int action) {
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

}
