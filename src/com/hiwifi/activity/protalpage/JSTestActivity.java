package com.hiwifi.activity.protalpage;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.Locale;
import java.util.Timer;
import java.util.TimerTask;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.os.Handler;
import android.telephony.SmsMessage;
import android.view.Gravity;
import android.view.Menu;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.view.inputmethod.InputMethodManager;
import android.webkit.JavascriptInterface;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.hiwifi.app.task.UpdateConfRunnable;
import com.hiwifi.app.utils.RegUtil;
import com.hiwifi.app.views.TextViewAdvance;
import com.hiwifi.hiwifi.R;
import com.hiwifi.model.User;
import com.hiwifi.store.jsdatabase.JSDataBaseManager;
import com.hiwifi.utils.PhoneNumberCapturer;
import com.hiwifi.utils.encode.Security;
import com.hiwifi.utils.jsdownloader.JS_downloader.DownloadFinishListener;
import com.umeng.analytics.MobclickAgent;

public class JSTestActivity extends Activity implements OnClickListener,
		DownloadFinishListener {
	public static boolean loading = false;
	public static boolean is_open = false;
	private int wait_timer;
	private int js_index;
	private int wait_time = 20;
	private int input_type = 0;
	private boolean running = false;
	private boolean js_running = false;
	private boolean is_callback = false;
	private boolean has_jqurey = false;
	private String js = "";
	private String sms = "";
	private String url = "http://www.baidu.com/";
	private String ssid = "";
	private String title_str = "";
	private String jqurey;
	private ArrayList<String> js_list;
	private ImageView back;
	private Button btn_close;
	private Button btn_run;
	private Button btn_db;
	private TextView title;
	private TextViewAdvance js_switch;
	private MySMSMonitor receiver;
	private JSDataBaseManager jsdbm;
	private UpdateConfRunnable ucr;
	private InputMethodManager im;
	private MockRedirectWebView webView = null;
	private View myDialog;
	private View myDialogBG;

	private void runJS(String js_str) {
		if (has_jqurey) {
			webView.execjs(js + "\n" + js_str);
		} else {
			webView.execjs(jqurey + "\n" + js + "\n" + js_str);
		}
	}

	@SuppressLint("HandlerLeak")
	private Handler h = new Handler() {
		public void handleMessage(android.os.Message msg) {
			try {
				if (msg.what == -1) {
					is_callback = false;
					// System.out.println("load js");
					// webView.execjs(jqurey);
					runJS("HI_check();");
					new Thread(new Runnable() {
						@Override
						public void run() {
							int timer = 0;
							while (!is_callback) {
								try {
									Thread.sleep(1);
								} catch (InterruptedException e) {
									e.printStackTrace();
								}
								if (timer++ > 1000) {
									loadJS();
									break;
								}
							}
						}
					}).start();
				} else if (msg.what == 0) {
					is_callback = false;
					webView.execjs("hiwifi.has_jquery(typeof(jQuery)==\"function\")");
					new Thread(new Runnable() {
						@Override
						public void run() {
							int timer = 0;
							while (!is_callback) {
								try {
									Thread.sleep(1);
								} catch (InterruptedException e) {
									e.printStackTrace();
								}
								if (timer++ > 1000) {
									has_jqurey = false;
									h.sendEmptyMessage(-1);
									break;
								}
							}
						}
					}).start();
				} else if (msg.what == 1) {
					title.setText(title_str);
					String ret = "";
					String num = PhoneNumberCapturer
							.getNumber(JSTestActivity.this);
					if (num.length() == 11) {
						ret = num;
					} else {
						String name = User.shareInstance().getPassport().trim();
						if ((name.length() == 11)
								&& RegUtil.checkPhone(name)) {
							ret = name;
						}
					}
					js_switch.setEnabled(true);
					registerRecevier();
					js_running = true;
					js_switch.setBackgroundColor(138, 215, 226);
					js_switch.show_new_str("关闭");
					disable_loading();
					runJS("HI_login_show('" + ret + "');");
					// webView.execjs(jqurey + "\n" + js + "\nHI_login_show('"
					// + ret + "');");
				} else if (msg.what == 2) {
					runJS("smslogin('" + sms + "');");
					// webView.execjs(jqurey + "\n" + js + "\nsmslogin('" + sms
					// + "');");
				} else if (msg.what == 3) {
					btn_close.setEnabled(false);
					btn_db.setEnabled(false);
					btn_run.setEnabled(false);
				} else if (msg.what == 4) {
					btn_close.setEnabled(true);
					btn_db.setEnabled(true);
					btn_run.setEnabled(true);
				} else if (msg.what == 5) {
					js_switch.show_new_str("自动认证");
					js_switch.setBackgroundColor(93, 195, 209);
					runJS("HI_login_close();");
					// webView.execjs(jqurey + "\n" + js +
					// "\nHI_login_close();");
				} else if (msg.what == 6) {
					js_switch.setEnabled(true);
				} else if (msg.what == 7) {
					title.setText(title_str);
					webView.clearCache(true);
					loading = true;
					webView.loadUrl(url);
					try {
						Thread.sleep(500);
					} catch (InterruptedException e) {
						e.printStackTrace();
					}
					new Thread(new Runnable() {
						@Override
						public void run() {
							wait_timer = 0;
							while (loading && wait_timer++ < wait_time) {
								try {
									Thread.sleep(500);
								} catch (InterruptedException e) {
									e.printStackTrace();
								}
							}
							h.sendEmptyMessage(8);
							check_timeout(0);
						}
					}).start();
				} else if (msg.what == 8) {
					disable_loading();
				} else if (msg.what == 9) {
					InputMethodPanel(input_type);
				} else if (msg.what == 10) {
					hideInput();
				}
			} catch (Exception e) {
				// System.out
				// .println("jstest : error in handler\n" + e.toString());
			}
		}
	};

	@JavascriptInterface
	public void has_jquery(boolean input) {
		cancleCallbackCheck();
		has_jqurey = input;
		h.sendEmptyMessage(-1);
	}

	@JavascriptInterface
	public void available(boolean hehe) {
		cancleCallbackCheck();
		// System.out.println("enter case 1");
		if (hehe) {
			// System.out.println("send 1");
			h.sendEmptyMessage(1);
		} else {
			// System.out.println("send 2");
			loadJS();
		}
	}

	@JavascriptInterface
	public void available(boolean hehe, String title) {
		cancleCallbackCheck();
		// System.out.println("enter case 2");
		if (hehe) {
			// System.out.println("send 1");
			title_str = title;
			h.sendEmptyMessage(1);
		} else {
			// System.out.println("send 2");
			loadJS();
		}
	}

	@JavascriptInterface
	public void available(boolean hehe, String title, String act) {
		cancleCallbackCheck();
		// System.out.println("enter case 3");
		if (hehe) {
			title_str = title;
			int location = act.indexOf(":");
			// String action = act.substring(0, location);
			String url = act.substring(location + 1);
			if (url != null) {
				url = url.trim();
				if (url.length() > 0) {
					this.url = url;
				}
			}
			// System.out.println("act = " + action + "\nurl = " + url);
			h.sendEmptyMessage(7);
		}
	}

	@JavascriptInterface
	public void openKeyboard(int type) {
		// 2 mobile
		// 1 number
		// 0 normal
		input_type = type;
		h.sendEmptyMessage(9);
	}

	@JavascriptInterface
	public void closeKeyboard() {
		h.sendEmptyMessage(10);
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.main, menu);
		return true;
	}

	public void unRegisterReveier() {
		if (receiver != null) {
			unregisterReceiver(receiver);
		}
	}

	@Override
	public void onClick(View arg0) {
		finish();
	}

	@Override
	public void download_js_finish() {
		h.sendEmptyMessage(4);
	}

	public void refreshDB(View view) {
		if (!running) {
			jsdbm.JS_Database.clearJavaScriptDatabase();
			h.sendEmptyMessage(3);
			refreshjsdb();
		}
	}

	public void get_JS(View view) {
		if (loading) {
			return;
		}
		enable_loading();
		h.sendEmptyMessage(10);
		boolean fuzz = false;
		String ssid = Security.encode_string(this.ssid);
		js_list = jsdbm.Ssid_Map_JS.getJSArray(ssid, fuzz);
		js_index = 0;
		loading = true;
		wait_timer = 0;
		webView.loadUrl(url);
		new Thread(new Runnable() {

			@Override
			public void run() {
				while (loading && wait_timer++ < wait_time) {
					try {
						Thread.sleep(500);
					} catch (InterruptedException e) {
						e.printStackTrace();
					}
				}
				check_timeout(1);
			}
		}).start();
	}

	@Override
	protected void onDestroy() {
		super.onDestroy();
		is_open = false;
		webView.stopLoading();
		disable_loading();
		jsdbm.closeDBM();
		unRegisterReveier();
	}

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		getActionBar().hide();
		MobclickAgent.onEvent(this, "stat_portal_enter",
				getResources()
						.getString(R.string.stat_portal_enter));
		// load JQuery
		String Result = "";
		try {
			InputStreamReader inputReader = new InputStreamReader(
					getResources().getAssets().open(
							"jquery.mobile-1.11.1.min.js"));
			BufferedReader bufReader = new BufferedReader(inputReader);
			String line = "";
			while ((line = bufReader.readLine()) != null) {
				Result += line;
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		jqurey = Result;
		// jqurey = "";
		//
		Animation rotateAnimation = AnimationUtils.loadAnimation(this,
				R.anim.rotate_anim);
		is_open = true;
		im = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
		setContentView(R.layout.js_test_activity);
		webView = (MockRedirectWebView) findViewById(R.id.webview);

		myDialog = findViewById(R.id.dialog);
		myDialogBG = findViewById(R.id.dialog_bg);
		ImageView loadingview = (ImageView) myDialog
				.findViewById(R.id.iv_loading);
		loadingview.startAnimation(rotateAnimation);
		Intent i = getIntent();
		jsdbm = JSDataBaseManager.getJSDataBaseManager();
		ucr = new UpdateConfRunnable();
		ucr.setJSDownloadFinishListener(this);
		ssid = i.getStringExtra("ssid").trim();
		btn_close = (Button) findViewById(R.id.js_close);
		if (i.getBooleanExtra("istest", false)) {
			btn_db = (Button) findViewById(R.id.js_get_db);
			btn_db.setVisibility(View.VISIBLE);
			btn_run = (Button) findViewById(R.id.js_run);
			btn_run.setVisibility(View.VISIBLE);
			btn_close.setVisibility(View.GONE);
			url = "http://115.182.0.172/dash/radcheck/?res=notyet&uamip=192.168.60.1&uamport=3990&challenge=6561d5ac43787ecc29b9044be98f213b&mac=48-74-6E-89-A7-71&ip=192.168.60.57&ssid=Starbucks&called=00-1F-7A-EC-00-DB&nasid=Open-Mesh&userurl=http%3a%2f%2ftieba.baidu.com%2f%3fmo_device%3d1%26itj%3d43%26ssid%3d0%26from%3d844b%26bd_page_type%3d1%26uid%3d0%26pu%3dsz%25401320_2001%252Cta%2540iphone_1_7.0_3_537";
			// url = "http://m.baidu.com";
			// url = "http://www.eastmoney.com/";
			// ssid = "AIRPORT-FREE-WIFI";
		}
		btn_close.setOnClickListener(this);
//		title = (TextView) findViewById(R.id.tv_titile);
//		back = (ImageView) findViewById(R.id.close_page);
		back.setOnClickListener(this);
		String title1 = i.getStringExtra("title");
		String btn_text = i.getStringExtra("close_btn");
		if (title1 == null || btn_text == null) {
			title1 = "认证登录";
			btn_text = "退出认证";
		}
		btn_close.setText(btn_text);
		title.setText(title1);
		if (ssid == null) {
			ssid = "";
		}
		ssid = ssid.toLowerCase(Locale.CHINA);
		webView.addJavascriptInterface(this, "hiwifi");

		get_JS(null);
		js_switch = (TextViewAdvance) findViewById(R.id.show_js_button);
		js_switch.setBackgroundColor(93, 195, 209);
		js_switch.setEnabled(false);
		js_switch.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View arg0) {
				js_switch.setEnabled(false);
				if (js_running) {
					h.sendEmptyMessage(5);
				} else {
					h.sendEmptyMessage(1);
				}
				js_running = !js_running;
				h.sendEmptyMessageDelayed(6, 700);
			}

		});
	}

	private void cancleCallbackCheck() {
		is_callback = true;
		try {
			Thread.sleep(2);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
	}

	private void disable_loading() {
		myDialog.setVisibility(View.INVISIBLE);
		myDialogBG.setVisibility(View.INVISIBLE);
	}

	private void enable_loading() {
		myDialog.setVisibility(View.VISIBLE);
		myDialogBG.setVisibility(View.VISIBLE);
	}

	private void hideInput() {
		if (im != null && im.isActive()) {
			im.hideSoftInputFromWindow(webView.getWindowToken(), 0);
		}
	}

	private void loadJS() {
		if (js_index < js_list.size()) {
			String name = js_list.get(js_index++);
			// System.out.println("jsname " + Security.decode_string(name));
			String temp = jsdbm.JS_Database.getJS(name);
			// System.out.println("js length " + temp.length());
			js = delete_note(temp);
			h.sendEmptyMessage(0);
		} else {
			h.sendEmptyMessage(8);
		}
	}

	private String delete_note(String input) {
		int loc;
		int loc_end;
		String buffer = "";
		do {
			loc = input.indexOf("/*");
			loc_end = input.indexOf("*/");
			if (loc_end > loc) {
				if (loc > 0) {
					buffer += input.substring(0, loc);
				}
				if (loc_end > 0) {
					input = input.substring(loc_end + 2);
				}
			} else {
				loc = -1;
			}
		} while (loc > 0);
		buffer += input;
		BufferedReader bufferedReader = new BufferedReader(new StringReader(
				buffer));
		String ret = "";
		try {
			String temp;
			while ((temp = bufferedReader.readLine()) != null) {
				temp = temp.trim();
				loc = temp.indexOf("//");
				if (loc > 0) {
					temp = temp.substring(0, loc);
					if (temp.length() > 0) {
						ret += temp + "\r\n";
					}
				} else if ((loc < 0) && (temp.length() > 0)) {
					ret += temp + "\r\n";
				}
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
		return ret;
	}

	private void registerRecevier() {
		IntentFilter filter = new IntentFilter(
				"android.provider.Telephony.SMS_RECEIVED");
		receiver = new MySMSMonitor();
		registerReceiver(receiver, filter);
	}

	private void check_timeout(int action) {
		if (wait_timer >= wait_time) {
			h.post(new Runnable() {
				@Override
				public void run() {
					Toast toast = Toast.makeText(JSTestActivity.this,
							"网络不通,请重新进入认证", Toast.LENGTH_LONG);
					toast.setGravity(Gravity.CENTER, 0, 0);
					toast.show();
					finish();
				}
			});
		} else {
			if (action == 0) {
				h.sendEmptyMessage(0);
			} else if (action == 1) {
				loadJS();
			}
		}
	}

	private void refreshjsdb() {
		if (!running) {
			new Thread(new Runnable() {

				@Override
				public void run() {
					running = true;
					ucr.execute();
					running = false;
				}

			}).start();
		}
	}
	
	@Override
	protected void onResume() {
		super.onResume();
		MobclickAgent.onPageStart(this.getClass().getSimpleName()); //统计页面
	    MobclickAgent.onResume(this);          //统计时长
	}
	
	@Override
	protected void onPause() {
		super.onPause();
		MobclickAgent.onPageEnd(this.getClass().getSimpleName());
		MobclickAgent.onPause(this);
	}

	private void InputMethodPanel(int type) {
		Timer timer = new Timer();
		webView.requestFocus();
		timer.schedule(new TimerTask() {
			@Override
			public void run() {
				if (im.isActive()) {
					im.toggleSoftInput(InputMethodManager.HIDE_IMPLICIT_ONLY, 0);
				}
			}
		}, 300);
	}

	class MySMSMonitor extends BroadcastReceiver {
		private static final String ACTION = "android.provider.Telephony.SMS_RECEIVED";

		@Override
		public void onReceive(Context context, Intent intent) {
			if (intent != null && intent.getAction() != null
					&& ACTION.compareToIgnoreCase(intent.getAction()) == 0) {
				Object[] pduArray = (Object[]) intent.getExtras().get("pdus");
				SmsMessage[] messages = new SmsMessage[pduArray.length];
				for (int i = 0; i < pduArray.length; i++) {
					messages[i] = SmsMessage
							.createFromPdu((byte[]) pduArray[i]);
				}
				for (SmsMessage cur : messages) {
					sms = cur.getDisplayMessageBody();
					h.sendEmptyMessage(2);
				}
			}
		}
	}
}
