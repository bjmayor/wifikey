package com.hiwifi.activity;

import android.annotation.TargetApi;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.PixelFormat;
import android.os.Bundle;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.Window;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.ImageView;

import com.hiwifi.activity.wifi.WiFiOperateActivity;
import com.hiwifi.app.views.HorizontalScroller;
import com.hiwifi.hiwifi.R;
import com.hiwifi.model.ClientInfo;
import com.hiwifi.model.DiscoverItem;
import com.hiwifi.model.wifi.WifiAdmin;
import com.hiwifi.utils.ActivitySplitAnimationUtil;
import com.hiwifi.utils.NetworkUtil;
import com.hiwifi.utils.ResUtil;
import com.hiwifi.utils.SmartBarUtils;
import com.hiwifi.utils.ViewUtil;
import com.umeng.analytics.MobclickAgent;

public class TutorialActivity extends Activity {
	private static final String TAG = TutorialActivity.class.getSimpleName();
	private static final int TUTORIAL_IMAGE_COUNT = 4;
	private static final String TOURIAL_IMAGE_NAME = "wifikey_guide";
	private static final String RESOURES_DRAWABLE = "drawable";
	private HorizontalScroller mGallery = null;
	private LayoutInflater mLayoutInflater;
	public static boolean isFirst;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		ClientInfo.shareInstance().setFirstStarted(false);
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		getWindow().setFormat(PixelFormat.TRANSPARENT);
		setContentView(R.layout.tutorial);
		SmartBarUtils.hide(getWindow().getDecorView());

		mGallery = (HorizontalScroller) findViewById(R.id.view_group_id);
		mLayoutInflater = (LayoutInflater) getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		addView();
		// if (!ShortcutUtil.hasShortcut(this)) {
		// ShortcutUtil.addShortcut(this);
		// }
	}

	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event) {
		if (keyCode == KeyEvent.KEYCODE_BACK) {
			intent2Tab();

		}
		return super.onKeyDown(keyCode, event);
	}

	private void addView() {

		for (int i = 0; i < TUTORIAL_IMAGE_COUNT; i++) {
			FrameLayout itemLayout = (FrameLayout) mLayoutInflater.inflate(
					R.layout.tutorial_gallery, null);
			ImageView imageView = (ImageView) itemLayout
					.findViewById(R.id.ImageView_content);
			imageView.setImageResource(ResUtil.getResIdentifier(
					TOURIAL_IMAGE_NAME + (i + 1), RESOURES_DRAWABLE));
			if (i == TUTORIAL_IMAGE_COUNT - 1) {
				imageView.setOnClickListener(new OnClickListener() {

					@Override
					public void onClick(View v) {
						intent2Tab();
					}
				});
			}
			mGallery.addView(itemLayout);
		}
	}

	private void intent2Tab() {
		ClientInfo.shareInstance().setFirstStarted(false);
		Intent mainIntent = new Intent();
		Intent fromIntent = getIntent();
		if (fromIntent != null) {
			String from = fromIntent.getStringExtra("from");
			if (from != null && from.equalsIgnoreCase("about")) {
				finish();
				return;
			}
		}
		if (DiscoverItem.showAdImage && DiscoverItem.loadedAdImge) {
			mainIntent.setClass(TutorialActivity.this,
					AdvertisementActivity.class);
		} else {
			if (WifiAdmin.sharedInstance().isWifiEnable()) {
				mainIntent.setClass(this.getApplicationContext(),
						MainTabActivity.class);
			} else {
				mainIntent.setClass(this.getApplicationContext(),
						WiFiOperateActivity.class);
			}
		}
		startActivity(mainIntent);
		finish();
		if (android.os.Build.VERSION.SDK_INT > android.os.Build.VERSION_CODES.DONUT) {
			new AnimationModel(TutorialActivity.this)
					.overridePendingTransition(R.anim.fade, R.anim.hold);
		}
	}

	class AnimationModel {
		private Activity context;

		public AnimationModel(Activity context) {
			this.context = context;
		}

		/**
		 * call overridePendingTransition() on the supplied Activity.
		 */
		@TargetApi(5)
		public void overridePendingTransition(int a, int b) {
			context.overridePendingTransition(a, b);
		}
	}

	@Override
	protected void onResume() {
		super.onResume();
		MobclickAgent.onResume(this);
	}

	@Override
	protected void onPause() {
		super.onPause();
		MobclickAgent.onPause(this);
	}
}