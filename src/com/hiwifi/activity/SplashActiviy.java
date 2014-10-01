package com.hiwifi.activity;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URL;
import java.net.URLConnection;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.os.Handler;
import android.text.TextUtils;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.RelativeLayout.LayoutParams;
import android.widget.TextView;
import cn.sharesdk.framework.ShareSDK;

import com.hiwifi.activity.base.BaseActivity;
import com.hiwifi.activity.wifi.WiFiOperateActivity;
import com.hiwifi.constant.RequestConstant.RequestTag;
import com.hiwifi.hiwifi.Gl;
import com.hiwifi.hiwifi.R;
import com.hiwifi.model.ClientInfo;
import com.hiwifi.model.DiscoverItem;
import com.hiwifi.model.RecommendInfo;
import com.hiwifi.model.User;
import com.hiwifi.model.log.LogUtil;
import com.hiwifi.model.request.RequestFactory;
import com.hiwifi.model.request.RequestManager;
import com.hiwifi.model.request.RequestManager.ResponseHandler;
import com.hiwifi.model.request.ServerResponseParser;
import com.hiwifi.model.wifi.WifiAdmin;
import com.hiwifi.utils.ImageUtil;
import com.hiwifi.utils.NetworkUtil;
import com.hiwifi.utils.ViewUtil;

public class SplashActiviy extends BaseActivity implements ResponseHandler {

	private ImageView map;
	private boolean hasUpgrate = false;
	private boolean isFirstOpen;

	// private boolean loadedAdImge = false;

	@Override
	protected void onClickEvent(View paramView) {

	}

	@Override
	protected void findViewById() {
		ShareSDK.initSDK(this);
		map = (ImageView) findViewById(R.id.splash_map);
		TextView version = (TextView) findViewById(R.id.version);
		version.setText(Gl.getAppVersionName());
		// initView();
		isFirstOpen = ClientInfo.shareInstance().isFirstStarted();
		LogUtil.d("hehe", User.shareInstance().getUid());
	}

	private void initView() {
		Bitmap bitmap = BitmapFactory.decodeResource(getResources(),
				R.drawable.startup_map);
		Bitmap adaptiveW = ImageUtil.adaptiveW(bitmap, screenWidth);
		BitmapDrawable d = new BitmapDrawable(adaptiveW);
		RelativeLayout.LayoutParams lp = (LayoutParams) map.getLayoutParams();
		lp.width = adaptiveW.getWidth();
		lp.height = adaptiveW.getHeight();
		map.setLayoutParams(lp);
		map.setBackgroundDrawable(d);

	}

	@Override
	protected void loadViewLayout() {
		setContentView(R.layout.activity_splash);
	}

	@Override
	protected void processLogic() {
		postDelay();

		hasUpgreatOfApp();
		RequestFactory.getRecommendApps(this, this);
		RequestFactory.getDiscoverList(this, this);

		// if (DiscoverItem.showAdImage
		// && !TextUtils.isEmpty(DiscoverItem.showedImageUrl)) {
		// loadAdImage();
		// }
	}

	private void loadAdImage() {
		File filesDir = SplashActiviy.this.getFilesDir();
		String absolutePath = filesDir.getAbsolutePath();
		String path = absolutePath +"/"+DiscoverItem.showedImageUrl.substring(DiscoverItem.showedImageUrl.lastIndexOf("/")+1, DiscoverItem.showedImageUrl.length()-4)
				+ "ad.png";
		File f = new File(path);
		if (f.exists() && !destroyed) {
			DiscoverItem.loadedAdImge = true;
			return;
		}

		Thread thread = new Thread() {
			public void run() {
				try {
					URL url = new URL(DiscoverItem.showedImageUrl);
					URLConnection conn = url.openConnection();
					conn.connect();
					InputStream is = conn.getInputStream();
					conn.setConnectTimeout(130000);
					BufferedInputStream bis = new BufferedInputStream(is);
					Bitmap bm = BitmapFactory.decodeStream(bis);
					File filesDir = SplashActiviy.this.getFilesDir();
					String absolutePath = filesDir.getAbsolutePath();
					String path =  absolutePath +"/" +DiscoverItem.showedImageUrl.substring(DiscoverItem.showedImageUrl.lastIndexOf("/"), DiscoverItem.showedImageUrl.length()-4)
							+ "ad.png";
					File f = new File(path);
//					if (f.exists()) {
//						f.delete();
//					}
					if (!destroyed) {
						OutputStream out = new FileOutputStream(f);
						bm.compress(Bitmap.CompressFormat.PNG, 100, out);
					}
					bm.recycle();
					bis.close();
					is.close();
					if (f.exists() && !destroyed) {
						DiscoverItem.loadedAdImge = true;
					}
				} catch (Exception e) {
					e.printStackTrace();
				}

			};
		};
		thread.start();

	}

	@Override
	protected void setListener() {

	}

	// 延迟启动
	private void postDelay() {
		new Handler(getMainLooper()).postDelayed(new Runnable() {
			@Override
			public void run() {
				Intent intent = new Intent();
				if (hasUpgrate) {
					intent.setClass(SplashActiviy.this,
							UpgradeAppActivity.class);
				} else {
					if (isFirstOpen) {
						ClientInfo.shareInstance().setFirstStarted(false);
						intent.setClass(SplashActiviy.this,
								TutorialActivity.class);
					} else {
						if (DiscoverItem.showAdImage
								&& DiscoverItem.loadedAdImge) {
							intent.setClass(SplashActiviy.this,
									AdvertisementActivity.class);
						} else {
							if (WifiAdmin.sharedInstance().isWifiEnable()) {
								intent.setClass(SplashActiviy.this,
										MainTabActivity.class);
							} else {
								intent.setClass(SplashActiviy.this,
										WiFiOperateActivity.class);
							}
						}
					}

				}
				startActivity(intent);
				finish();

			}
		}, 3000);

	}

	private void hasUpgreatOfApp() {
		// TODO 检查更新
		RequestFactory.checkAppUpGrade(this, this);
	}

	@Override
	public void onStart(RequestTag tag, Code code) {
		if (code == Code.ok) {
		}

	}

	@Override
	public void onSuccess(RequestTag tag, ServerResponseParser responseParser) {
		LogUtil.d("hehe", responseParser.toString());
		if (tag == RequestTag.HIWIFI_APP_RECOMMEND_GET) {
			new RecommendInfo().parse(tag, responseParser);
		} else if (tag == RequestTag.HIWIFI_DISCOVER_LIST_GET) {
			new DiscoverItem().parse(tag, responseParser);
			LogUtil.d("hehe", DiscoverItem.showAdImage + "--oo");
			LogUtil.d("hehe", DiscoverItem.showedImageUrl);
			if (DiscoverItem.showAdImage
					&& !TextUtils.isEmpty(DiscoverItem.showedImageUrl)) {
				//
				loadAdImage();
			}
		} else {
			ClientInfo.shareInstance().parse(tag, responseParser);
			if (ClientInfo.shareInstance().appNeedUpdate()) {
				hasUpgrate = true;
			}
		}

	}

	@Override
	public void onFailure(RequestTag tag, Throwable error) {
	}

	@Override
	public void onFinish(RequestTag tag) {

	}

	private boolean destroyed = false;

	@Override
	protected void onDestroy() {
		// RequestManager.cancelRequest(this);
		destroyed = true;
		// thread.interrupted();
		super.onDestroy();
	}

	@Override
	protected void updateView() {
		// TODO Auto-generated method stub

	}
	
	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event) {
		if (keyCode == KeyEvent.KEYCODE_BACK) {
			return true;
		}
		return super.onKeyDown(keyCode, event);
	}
	
	
	
}
