package com.hiwifi.model;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URL;
import java.net.URLConnection;
import java.util.ArrayList;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Environment;
import android.text.TextUtils;

import com.hiwifi.constant.ConfigConstant;
import com.hiwifi.constant.RequestConstant.RequestTag;
import com.hiwifi.hiwifi.Gl;
import com.hiwifi.model.log.HWFLog;
import com.hiwifi.model.log.LogUtil;
import com.hiwifi.model.request.ResponseParserInterface;
import com.hiwifi.model.request.ServerResponseParser;
import com.hiwifi.utils.FileUtil;
import com.hiwifi.utils.encode.MD5Util;

public class DiscoverItem implements ResponseParserInterface {
	private String title;// 标题
	private String detailUrl;// 详情页地址
	private Type type;// 类型
	private long createTime;// 创建时间，单位秒
	private String updateTime;// 更新时间
	private String imgUrl;// 图片地址

	private String shareTitle;// 分享标题
	private String shareImage;// 分享图片地址
	private String shareContent;// 分享内容
	private String shareUrl;// 分享地址
	private String shareImgPath;// 分享图片本地路径

	// 二级splash会使用
	public static boolean showAdImage = false;
	public static String showedImageUrl = "";
	public static int firstAd = -1;
	public static boolean loadedAdImge = false;

	public enum Type {
		typeWeb, typeApp;
	}

	public DiscoverItem() {
		title = "title";
		detailUrl = "detailUrl";
		detailUrl = "";
	}

	@Override
	public String toString() {
		return String
				.format("{DiscoverItem:{title:%s,detailUrl:%s,createTime:%s,updateTime:%s,imgUrl:%s,shareTitle:%s,shareImage:%s,shareContent:%s,shareUrl:%s,shareImgPath:%s}}",
						title, detailUrl, createTime, updateTime, imgUrl,
						shareTitle, shareImage, shareContent, shareUrl,
						shareImgPath);
	}

	public synchronized final String getShareTitle() {
		return shareTitle;
	}

	public synchronized final String getShareImage() {
		return shareImage;
	}

	public synchronized final String getShareContent() {
		return shareContent;
	}

	public synchronized final String getShareUrl() {
		return shareUrl;
	}

	public synchronized final void setShareUrl(String shareUrl) {
		this.shareUrl = shareUrl;
	}

	public synchronized final String getTitle() {
		return title;
	}

	public synchronized final String getDetailUrl() {
		return detailUrl;
	}

	public synchronized final long getCreateTime() {
		return createTime;
	}

	public synchronized final String getUpdateTime() {
		return updateTime;
	}

	public synchronized final String getImgUrl() {
		return imgUrl;
	}

	@Override
	public synchronized void parse(RequestTag tag, ServerResponseParser parser) {
		if (tag == RequestTag.HIWIFI_DISCOVER_LIST_GET) {
			String localListMd5 = MD5Util.encryptToMD5(parser.originResponse
					.toString());
			if (!localListMd5.equalsIgnoreCase(getListMd5())) {
				hasNew = true;
				listMd5 = localListMd5;
			}
			save();
			LogUtil.d("hehe", parser.originResponse.toString());
			pageList.clear();
			if (parser.originResponse.has("list")) {
				try {
					JSONArray listArray = parser.originResponse
							.getJSONArray("list");
					showAdImage = false;
					showedImageUrl = "";
					for (int i = 0; i < listArray.length(); i++) {
						JSONObject itemObject = listArray.getJSONObject(i);
						DiscoverItem item = new DiscoverItem();
						item.title = itemObject.optString("title");
						item.imgUrl = itemObject.optString("image");
						item.detailUrl = itemObject.optString("url");
						// item.type = itemObject.optString("type");
						item.createTime = 1000 * Long.parseLong(itemObject
								.optString("createtime"));
						item.updateTime = itemObject.optString("updatetime");

						item.shareTitle = itemObject.optString("share_title",
								"");
						if (TextUtils.isEmpty(shareTitle)) {
							item.shareTitle = item.title;
						}
						item.shareContent = itemObject.optString("share_text",
								"");
						item.shareUrl = itemObject.optString("share_url", "");
						if (TextUtils.isEmpty(item.shareUrl)) {
							item.shareUrl = item.detailUrl;
						}
						item.shareImage = itemObject.optString("share_image",
								"");// detailUrl
						if (TextUtils.isEmpty(item.shareImage)) {
							item.shareImage = item.imgUrl;
						}

						if (!showAdImage) {
							if (!itemObject.isNull("index_show")
									&& !itemObject.isNull("index_image")) {
								if (itemObject.getString("index_show")
										.equalsIgnoreCase("1")
										&& !TextUtils.isEmpty(itemObject
												.getString("index_image"))) {
									showAdImage = true;
									showedImageUrl = itemObject
											.getString("index_image");
									if (firstAd == -1) {
										firstAd = i;
									}
								}
							}
						}
						loadImage(item);
						pageList.add(item);
					}

				} catch (JSONException e) {
					e.printStackTrace();
				}

			}
		}
	}

	private static ArrayList<DiscoverItem> pageList = new ArrayList<DiscoverItem>();

	public synchronized static ArrayList<DiscoverItem> getList() {
		return pageList;
	}

	private static String listMd5 = null;
	private static Boolean hasNew = false;
	private static Boolean hasLoaded = false;

	public static Boolean hasNew() {
		if (!hasLoaded) {
			load();
			hasLoaded = true;
		}
		return hasNew;
	}

	private static String getListMd5() {
		if (!hasLoaded) {
			load();
			hasLoaded = true;
		}
		return listMd5;
	}

	public static void markRead() {
		hasNew = false;
		save();
	}

	public void setDetailUrl(String detailUrl) {
		this.detailUrl = detailUrl;
	}

	public void loadImage(DiscoverItem item) {
		final String imagUrl = item.getImgUrl();
		String name = imagUrl.substring(imagUrl.lastIndexOf("/")+1,
				imagUrl.lastIndexOf(".")) +"_share.png";

		String path = "";
		if (Environment.MEDIA_MOUNTED.equals(Environment
				.getExternalStorageState())
				&& Environment.getExternalStorageDirectory().exists()) {
			path = Environment.getExternalStorageDirectory().getAbsolutePath()
					+ ConfigConstant.SD_DATA_DIRECTORY;
		} else {
			path = Gl.Ct().getFilesDir().getAbsolutePath()
					+ ConfigConstant.SD_DATA_DIRECTORY;
		}
		
		File file = new File(path);
		if (!file.exists()) {
			file.mkdirs();
		}
		path += name;
		LogUtil.d("hehe", path);
		final File shareFile = new File(path);
		if (shareFile.exists()) {
			return;
		}

		item.setShareImgPath(path);
		Thread thread = new Thread() {
			public void run() {
				try {
					URL url = new URL(imagUrl);
					URLConnection conn = url.openConnection();
					conn.connect();
					conn.setConnectTimeout(30000);
					InputStream is = conn.getInputStream();
					BufferedInputStream bis = new BufferedInputStream(is);
					Bitmap bm = BitmapFactory.decodeStream(bis);
					OutputStream out = new FileOutputStream(shareFile);
					bm.compress(Bitmap.CompressFormat.PNG, 100, out);
					bis.close();
					is.close();

				} catch (Exception e) {
					e.printStackTrace();
				}

			};
		};
		thread.start();
	}

	public String getShareImgPath() {
		return shareImgPath;
	}

	public void setShareImgPath(String shareImgPath) {
		this.shareImgPath = shareImgPath;
	}

	public static SharedPreferences getSharedPreferences() {
		return Gl.Ct().getSharedPreferences("DiscoverItem", 0);
	}

	private static void save() {
		try {
			SharedPreferences sp = getSharedPreferences();
			sp.edit().putBoolean("hasNew", hasNew).commit();
			sp.edit().putString("listMd5", listMd5).commit();
		} catch (Exception e) {
		}
	}

	private static void load() {
		SharedPreferences sp = getSharedPreferences();
		hasNew = sp.getBoolean("hasNew", false);
		listMd5 = sp.getString("listMd5", "");
		HWFLog.e("debugaaaaaa", "hasNew:" + hasNew + " listmd5" + listMd5);
	}

}
