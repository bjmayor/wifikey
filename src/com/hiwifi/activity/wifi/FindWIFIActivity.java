package com.hiwifi.activity.wifi;

import java.util.ArrayList;
import java.util.Timer;
import java.util.TimerTask;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Intent;
import android.media.AudioManager;
import android.media.SoundPool;
import android.media.SoundPool.OnLoadCompleteListener;
import android.net.wifi.ScanResult;
import android.net.wifi.WifiManager;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.Gravity;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.hiwifi.app.views.Radar;
import com.hiwifi.app.views.RadarButton;
import com.hiwifi.app.views.SildingFinishLayout;
import com.hiwifi.app.views.SildingFinishLayout.OnSildingFinishListener;
import com.hiwifi.hiwifi.Gl;
import com.hiwifi.hiwifi.R;
import com.umeng.analytics.MobclickAgent;

public class FindWIFIActivity extends Activity implements
		Radar.RadarValueChangeActionListener, OnLoadCompleteListener {

	private TextView ssid_tv;
	private TextView rssi_tv;
	private Radar radar;
	private RadarButton radar_btn;
	private ImageView close_btn;
	private Timer mTimer;
	private Timer wifi_scaner;
	private WifiManager wifi;
	private SoundPool soundpool;
	private ArrayList<Integer> soundPoolMap;
	private String ssid;
	private String bssid;
	private int rssi = -100;
	private int play_id;
	private int load_cnt;
	private int timer;
	private int play_timer;
	private int play_time;
	private int play_max_time = 22;
	private int start_timer;
	private int start_time_max = 2;
	private float sound_level;
	private float sound_level_min = 0.1f;
	private float sound_level_max = 0.5f;
	private float start_angle = (float) (-Math.PI * 0.395);
	private float end_angle = (float) (Math.PI * 1.395);
	private float angle_range = end_angle - start_angle;
	private boolean sound_loading;

	@SuppressLint("HandlerLeak")
	private Handler h = new Handler() {
		public void handleMessage(android.os.Message msg) {
			if (msg.what == 1) {
				rssi_tv.setText(String.valueOf(rssi) + " dbm");
				float p = rssi;
				if (p > -40) {
					p = -40;
				} else if (p < -100) {
					p = -100;
				}
				p += 100;
				int value = (int) (p / 0.6);
				if (value > 99) {
					value = 99;
				}
				// radar.set_angle(start_angle - 0.01f, true);
				if (p == 0) {
					radar.set_angle(start_angle - 0.01f, true, value);
				} else {
					p /= 60f;
					radar.set_angle(angle_range * p + start_angle, false, value);
				}
			}
			if (msg.what == 2) {
				float value = (Float) msg.obj;
				float ret = (value - start_angle) / angle_range;
				int out = (int) (ret * 100);
				if (out > 99) {
					out = 99;
				} else if (out < 0) {
					out = 0;
				}
				if (value < start_angle) {
					value = start_angle;
				} else if (value > end_angle) {
					value = end_angle;
				}
				int id = (int) (out / 4.5f);
				if (id > 21) {
					id = 21;
				}
				int time = (int) (21 - out / 4.95f);
				if (time > 20) {
					time = 20;
				}
				play_time = time;
				if (play_id != id) {
					timer = play_time + 1;
				}
				play_id = id;
				int lable_id = out == 0 ? 0 : (out + 26) / 25;
				if (lable_id > 4) {
					lable_id = 4;
				}
				radar.setValue(out);
				radar.setCenterImgId(lable_id);
			}
		};
	};
	private SildingFinishLayout mSildingFinishLayout;

	@Override
	public void onLoadComplete(SoundPool arg0, int arg1, int arg2) {
		if (++load_cnt == play_max_time) {
			sound_loading = false;
			startPlay();
			radar.startCheck();
			run_check();
		}
	}

	@Override
	public void onTouchGetAngle(float angle) {

	}

	@Override
	public void valueChanging(float value) {
		Message msg = new Message();
		msg.what = 2;
		msg.obj = value;
		h.sendMessage(msg);
	}

	public void run_check() {
		if (wifi_scaner != null) {
			wifi_scaner.cancel();
		}
		if (wifi == null) {
			wifi = (WifiManager) getSystemService(WIFI_SERVICE);
		}
		wifi_scaner = new Timer();
		wifi_scaner.schedule(new TimerTask() {
			@Override
			public void run() {
				try {
					wifi.startScan();
					int output = -100;
					for (ScanResult i : wifi.getScanResults()) {
						/* ==== base on BSSID ==== */
						// if (i.BSSID.equals(bssid)) {
						// output = i.level;
						// break;
						// }
						/* ==== end ==== */
						/* ==== base on SSID ==== */
						if (i.SSID.equals(ssid)) {
							int temp = i.level;
							if (temp > output) {
								output = temp;
							}
						}
						/* ==== end ==== */
					}
					rssi = output;
					h.sendEmptyMessage(1);
				} catch (Exception e) {
				}

			}
		}, 0, 2000);
	}

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
		setContentView(R.layout.find_wifi);
		ssid_tv = (TextView) findViewById(R.id.find_ssid);
		rssi_tv = (TextView) findViewById(R.id.find_rssi);
//		close_btn = (ImageView) findViewById(R.id.iv_close_page);
//		((TextView) findViewById(R.id.center_titile)).setText(getResources()
//				.getString(R.string.wifi_single));
		close_btn.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View arg0) {
				FindWIFIActivity.this.finish();
			}
		});
		Intent intent = getIntent();
		if (intent != null) {
			ssid = intent.getStringExtra("ssid");
			bssid = intent.getStringExtra("bssid");
		} else {
			ssid = null;
		}
		if (ssid == null) {
			Toast toast = Toast.makeText(this, "未找到ssid参数", Toast.LENGTH_LONG);
			toast.setGravity(Gravity.CENTER, 0, 0);
			toast.show();
			finish();
		}
		radar_btn = (RadarButton) findViewById(R.id.sound_btn);
		ssid_tv.setText(ssid);
		rssi_tv.setText(String.valueOf(rssi) + "[" + bssid + "]");
		View content = findViewById(R.id.radar_content);
		mSildingFinishLayout = (SildingFinishLayout) findViewById(R.id.find_wifi_sildingFinishLayout);
		mSildingFinishLayout.setTouchView(content);
		mSildingFinishLayout
				.setOnSildingFinishListener(new OnSildingFinishListener() {
					@Override
					public void onSildingFinish() {
						finish();
						overridePendingTransition(0, 0);
					}
				});
		radar = (Radar) findViewById(R.id.cycle);
		radar.setRadarValueChangeActionListener(this);
		// radar_btn.sound_on = Gl.GlConf.getRadarSound();
		radar_btn.sound_on = false;
		load_sound();
		radar_btn.setStop();
	}

	@Override
	protected void onResume() {
		super.onResume();
		run_check();
		startPlay();
		MobclickAgent.onPageStart(this.getClass().getSimpleName()); //统计页面
		MobclickAgent.onResume(this);          //统计时长
	}

	@Override
	protected void onPause() {
		super.onPause();
		stopPlay();
		MobclickAgent.onPageEnd(this.getClass().getSimpleName());
		MobclickAgent.onPause(this);
	}

	@Override
	protected void onDestroy() {
		stopPlay();
		if (soundpool != null) {
			soundpool.release();
			soundpool = null;
		}
		if (soundPoolMap != null) {
			soundPoolMap.clear();
			soundPoolMap = null;
		}
		Gl.GlConf.setRadarSound(radar_btn.sound_on);
		super.onDestroy();
	}

	private void startPlay() {
		if (!sound_loading) {
			if (mTimer != null) {
				mTimer.cancel();
			}
			play_timer = 0;
			timer = play_time;
			mTimer = new Timer();
			mTimer.schedule(new TimerTask() {
				@Override
				public void run() {
					try {
						if (play_timer++ > (play_time >> 1)) {
							radar_btn.setStop();
						}
						if (radar_btn.sound_on) {
							if (sound_level < sound_level_max) {
								if (start_timer++ > start_time_max) {
									start_timer = 0;
									sound_level += 0.01f;
								}
							}
							if (timer++ > play_time) {
								timer = 0;
								radar_btn.setPlay();
								play_timer = 0;
								int id = soundPoolMap.get(play_id);
								soundpool.play(id, sound_level, sound_level, 1,
										0, 1f);
							}
						} else {
							sound_level = sound_level_min;
							timer = play_time;
						}
					} catch (Exception e) {
					}
				}
			}, 0, 50);
			radar.startCheck();
		}
	}

	private void stopPlay() {
		if (mTimer != null) {
			mTimer.cancel();
		}
		if (wifi_scaner != null) {
			wifi_scaner.cancel();
		}
		radar.stopCheck();
	}

	private void load_sound() {
		new Thread(new Runnable() {
			@Override
			public void run() {
				try {

					load_cnt = 0;
					sound_level = sound_level_min;
					radar.set_init_angle(start_angle - 0.01f);
					sound_loading = true;
					soundpool = new SoundPool(play_max_time,
							AudioManager.STREAM_MUSIC, 0);
					soundPoolMap = new ArrayList<Integer>();
					soundpool.setOnLoadCompleteListener(FindWIFIActivity.this);
					play_time = 100;
					soundPoolMap.add(soundpool.load(FindWIFIActivity.this,
							R.raw.p1, 1));
					soundPoolMap.add(soundpool.load(FindWIFIActivity.this,
							R.raw.p2, 1));
					soundPoolMap.add(soundpool.load(FindWIFIActivity.this,
							R.raw.p3, 1));
					soundPoolMap.add(soundpool.load(FindWIFIActivity.this,
							R.raw.p4, 1));
					soundPoolMap.add(soundpool.load(FindWIFIActivity.this,
							R.raw.p5, 1));
					soundPoolMap.add(soundpool.load(FindWIFIActivity.this,
							R.raw.p6, 1));
					soundPoolMap.add(soundpool.load(FindWIFIActivity.this,
							R.raw.p7, 1));
					soundPoolMap.add(soundpool.load(FindWIFIActivity.this,
							R.raw.p8, 1));
					soundPoolMap.add(soundpool.load(FindWIFIActivity.this,
							R.raw.p9, 1));
					soundPoolMap.add(soundpool.load(FindWIFIActivity.this,
							R.raw.p10, 1));
					soundPoolMap.add(soundpool.load(FindWIFIActivity.this,
							R.raw.p11, 1));
					soundPoolMap.add(soundpool.load(FindWIFIActivity.this,
							R.raw.p12, 1));
					soundPoolMap.add(soundpool.load(FindWIFIActivity.this,
							R.raw.p13, 1));
					soundPoolMap.add(soundpool.load(FindWIFIActivity.this,
							R.raw.p14, 1));
					soundPoolMap.add(soundpool.load(FindWIFIActivity.this,
							R.raw.p15, 1));
					soundPoolMap.add(soundpool.load(FindWIFIActivity.this,
							R.raw.p16, 1));
					soundPoolMap.add(soundpool.load(FindWIFIActivity.this,
							R.raw.p17, 1));
					soundPoolMap.add(soundpool.load(FindWIFIActivity.this,
							R.raw.p18, 1));
					soundPoolMap.add(soundpool.load(FindWIFIActivity.this,
							R.raw.p19, 1));
					soundPoolMap.add(soundpool.load(FindWIFIActivity.this,
							R.raw.p20, 1));
					soundPoolMap.add(soundpool.load(FindWIFIActivity.this,
							R.raw.p21, 1));
					soundPoolMap.add(soundpool.load(FindWIFIActivity.this,
							R.raw.p22, 1));

				} catch (Exception e) {
				}
			}
		}).start();
	}
}
