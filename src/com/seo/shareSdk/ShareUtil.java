/*
 * 官网地站:http://www.ShareSDK.cn
 * 技术支持QQ: 4006852216
 * 官方微信:ShareSDK   （如果发布新版本的话，我们将会第一时间通过微信将版本更新内容推送给您。如果使用过程中有任何问题，也可以通过微信与我们取得联系，我们将会在24小时内给予回复）
 *
 * Copyright (c) 2013年 ShareSDK.cn. All rights reserved.
 */

package com.seo.shareSdk;

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

import com.seo.constant.ConfigConstant;
import com.seo.constant.RequestConstant;
import com.seo.constant.RequestConstant.RequestTag;
import com.seo.hiwifi.Gl;
import com.seo.wifikey.R;

/**
 * Share SDK接口演示页面，包括演示使用快捷分享完成图文分享、 无页面直接分享、授权、关注和不同平台的分享等等功能。
 */
public class ShareUtil  {

	public static void initImagePath(int path) {
		try {
			if (Environment.MEDIA_MOUNTED.equals(Environment
					.getExternalStorageState())
					&& Environment.getExternalStorageDirectory().exists()) {
				ConfigConstant.IMAGE_PATH = Environment
						.getExternalStorageDirectory().getAbsolutePath()
						+ ConfigConstant.FILE_NAME;
			} else {
				ConfigConstant.IMAGE_PATH = Gl.Ct().getFilesDir()
						.getAbsolutePath() + ConfigConstant.FILE_NAME;
			}
			File file = new File(ConfigConstant.IMAGE_PATH);
			if (!file.exists()) {
				file.createNewFile();
				Bitmap pic = BitmapFactory.decodeResource(
                        Gl.Ct().getResources(), path);
				FileOutputStream fos = new FileOutputStream(file);
				pic.compress(CompressFormat.JPEG, 100, fos);
				fos.flush();
				fos.close();
			}
		} catch (Throwable t) {
			t.printStackTrace();
		}
	}



}
