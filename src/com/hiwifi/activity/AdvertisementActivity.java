package com.hiwifi.activity;

import java.io.File;

import android.R.bool;
import android.annotation.SuppressLint;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.os.Handler;
import android.service.dreams.DreamService;
import android.view.View;
import android.view.animation.AlphaAnimation;
import android.view.animation.Animation;
import android.widget.ImageView;
import android.widget.RelativeLayout;

import com.hiwifi.activity.base.BaseActivity;
import com.hiwifi.activity.wifi.WiFiOperateActivity;
import com.hiwifi.app.views.PullDoorView;
import com.hiwifi.app.views.PullDoorView.ClickListener;
import com.hiwifi.constant.RequestConstant.RequestTag;
import com.hiwifi.hiwifi.Gl;
import com.hiwifi.hiwifi.R;
import com.hiwifi.model.ClientInfo;
import com.hiwifi.model.DiscoverItem;
import com.hiwifi.model.request.RequestFactory;
import com.hiwifi.model.request.RequestManager.ResponseHandler;
import com.hiwifi.model.request.ServerResponseParser;
import com.hiwifi.model.wifi.WifiAdmin;
import com.hiwifi.utils.ActivitySplitAnimationUtil;
import com.hiwifi.utils.ViewUtil;
import com.umeng.analytics.MobclickAgent;

public class AdvertisementActivity extends BaseActivity implements
		ResponseHandler, ClickListener {

	private ImageView shit, close;
	boolean hasUpgrate = false;
	boolean isFirstOpen = false;
	private Bitmap decodeFile;
	private Handler handler = new Handler();

	@Override
	protected void onClickEvent(View v) {
		switch (v.getId()) {
		case R.id.ad_image:
			showDetail();
			break;

		case R.id.btn_close_ad:
			rederect();
			// overridePendingTransition(R.anim.shit,
			// R.anim.activity_downtoup_out);

			break;

		default:
			break;
		}
	}

	@Override
	protected void findViewById() {
		shit = (ImageView) findViewById(R.id.ad_image);
		close = (ImageView) findViewById(R.id.btn_close_ad);
		isFirstOpen = ClientInfo.shareInstance().isFirstStarted();
		// pullDoorView = (PullDoorView) findViewById(R.id.mypull_view);
		// main = (RelativeLayout) findViewById(R.id.ad_main);

		 initView();//TODO 白页？？？
	}

	private void initView() {
		try {
			System.out.println("adadadad-------------------");
			File filesDir = AdvertisementActivity.this.getFilesDir();
			String absolutePath = filesDir.getAbsolutePath();
			String path = absolutePath +"/"+DiscoverItem.showedImageUrl.substring(DiscoverItem.showedImageUrl.lastIndexOf("/"), DiscoverItem.showedImageUrl.length()-4)
					+ "ad.png";
			File file = new File(path);
			if (!file.exists()) {
				rederect();
				return;
			}
			Bitmap bitmap = BitmapFactory.decodeFile(path);
			if (bitmap == null) {
				rederect();
				return;
			}
			// Bitmap adaptiveW = ImageUtil.adaptiveW(bitmap, screenWidth);
			BitmapDrawable d = new BitmapDrawable(bitmap);
			// RelativeLayout.LayoutParams lp = (LayoutParams)
			// shit.getLayoutParams();
			//
			// lp.width = adaptiveW.getWidth();
			// lp.height = adaptiveW.getHeight();
			// shit.setLayoutParams(lp);
			if (d == null) {
				rederect();
				return;
			}
			shit.setBackgroundDrawable(d);
		} catch (Exception e) {
			rederect();
			e.printStackTrace();
		}
	}

	@Override
	protected void loadViewLayout() {
		setContentView(R.layout.activity_advertisement);
	}

	@Override
	protected void processLogic() {
		RequestFactory.checkAppUpGrade(this, this);
		postDelay();
	}

	@Override
	protected void setListener() {
		 shit.setOnClickListener(this);
		// pullDoorView.setListener(this);

		close.setOnClickListener(this);
		Animation ani = new AlphaAnimation(0.2f, 1f);
		ani.setDuration(1500);
		ani.setRepeatMode(Animation.REVERSE);
		ani.setRepeatCount(Animation.INFINITE);
		close.startAnimation(ani);
	}

	private Runnable runnable = new Runnable() {

		@Override
		public void run() {
			rederect();
		}
	};

	// private PullDoorView pullDoorView;
	// private RelativeLayout main;

	private void postDelay() {
		handler.postDelayed(runnable, 3000);
	}

	private void rederect() {
		MobclickAgent.onEvent(this, "close_splash_ad", getResources()
				.getString(R.string.close_splash_ad));
		Intent intent = new Intent();

		if (WifiAdmin.sharedInstance().isWifiEnable()) {
			intent.setClass(AdvertisementActivity.this, MainTabActivity.class);
		} else {
			intent.setClass(AdvertisementActivity.this,
					WiFiOperateActivity.class);
		}

		// if (!click) {
		startActivity(intent);
		// overridePendingTransition(R.anim.shit, R.anim.activity_downtoup_out);
		// } else {
		// intent.putExtra("fromAD", "FromAD");
		// ActivitySplitAnimationUtil.startActivity(
		// AdvertisementActivity.this, intent);
		// }
		finish();
	}

	@Override
	public void onStart(RequestTag tag, Code code) {
		// TODO Auto-generated method stub

	}

	@Override
	protected void onDestroy() {
		if (Gl.userBitmap != null) {
			Gl.userBitmap = null;
		}
		super.onDestroy();
	}

	@Override
	public void onSuccess(RequestTag tag, ServerResponseParser responseParser) {
		ClientInfo.shareInstance().parse(tag, responseParser);
		if (ClientInfo.shareInstance().appNeedUpdate()) {
			hasUpgrate = true;
		}
	}

	@Override
	public void onFailure(RequestTag tag, Throwable error) {
		// TODO Auto-generated method stub

	}

	@Override
	public void onFinish(RequestTag tag) {

	}

	@Override
	public void showDetail() {
		MobclickAgent.onEvent(this, "enter_splash_ad", getResources()
				.getString(R.string.enter_splash_ad));
		handler.removeCallbacks(runnable);
		Intent ad = new Intent(this, CommonWebviewActivity.class);
		if (DiscoverItem.firstAd != -1) {
			DiscoverItem discover = DiscoverItem.getList().get(
					DiscoverItem.firstAd);
			ad.putExtra("from", "AdvertisementActivity");
			ad.putExtra("title", discover.getTitle());
			ad.putExtra("type", "push");
			ad.putExtra("url", discover.getDetailUrl());
		} else {
			return;
		}
		startActivity(ad);
		finish();
	}

	@Override
	public void skip() {
		rederect();
	}

	@Override
	protected void updateView() {
		
	}

}
