package com.hiwifi.model.speedtest;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.TimeoutException;

import com.hiwifi.hiwifi.Gl;

import android.annotation.SuppressLint;
import android.net.TrafficStats;
import android.os.Handler;
import android.os.Message;

public class Speed_test implements Download_test.SpeedTestAction {
	// ======================for net speed test=============================
	// the URL of the test
	private String[] test_urls = new String[] {
			"http://down.sandai.net/thundervip/ThunderVIP_Setup.exe",
			"http://dldir1.qq.com/qqfile/qq/QQ5.3/10702/QQ5.3.exe" };
	// the name of the test
	private final String[] test_names = new String[] { "thunder", "qq" };
	// ======================================================================
	private ArrayList<Download_test> test_list = new ArrayList<Download_test>();
	private int timer01;
	private int timer02;
	private int connection_overflow = 20;
	private int test_overflow;
	private int wait_second;
	private int speed_cnt;
	private long last_flow;
	private long speed_sum;
	private long last_mobile_flow;
	private long current_speed;
	private boolean start_test;
	private boolean is_success;
	private boolean is_mobile;
	private boolean is_test;
	private String message;
	private final int msg_start_test = 0;
	private final int msg_tick_speed = 1;
	private final int msg_finish_test = 2;
	// speed timer
	private Timer mTimer;
	private Test_result_listener listener;

	@SuppressLint("HandlerLeak")
	private Handler handler = new Handler() {
		@Override
		public void handleMessage(Message msg) {
			if (listener != null) {
				if (msg.what == msg_start_test) {
					listener.start_test();
				} else if (msg.what == msg_tick_speed) {
					listener.current_speed(current_speed);
				} else if (msg.what == msg_finish_test) {
					stop_test();
					if (mTimer != null) {
						mTimer.cancel();
					}
					listener.finish_test(is_success, current_speed, message);
				}
			}
		}
	};

	public Speed_test(Test_result_listener listener) {
		this.listener = listener;
		String[] urls = Gl.GlConf.getSpeedTestUrls();
		if (urls != null) {
			this.test_urls = urls;
		}
		wait_second = Gl.GlConf.getSpeedTestTimeout();
		is_test = false;
	}

	public void startTest(boolean pingFirst) {
		if (is_test) {
			return;
		}
		is_test = true;
		int res = -1, cnt = 0;
		last_mobile_flow = TrafficStats.getMobileTxBytes()
				+ TrafficStats.getMobileRxBytes();
		current_speed();
		is_mobile = false;
		if (pingFirst) {

			while (res != 0 && cnt < 20) {
				cnt++;
				try {
					res = Speed_test.executeCommand(
							"ping -c 1 -w 10 www.baidu.com", 1000);
					break;
				} catch (IOException e) {
					res = 100;
				} catch (InterruptedException e) {
					res = 200;
				} catch (TimeoutException e) {
					res = 300;
				}
			}
		}
		test_overflow = (wait_second << 1) / 1000;
		if (res == 0 || !pingFirst) {
			for (int i = 0; i < test_urls.length; i++) {
				Download_test test = new Download_test(test_names[i], this);
				test_list.add(test);
				test.start_test(test_urls[i], wait_second);
			}
			start_test = false;
			speed_cnt = 0;
			speed_sum = 0;
			timer02 = 0;
			start_timer();
		} else {
			is_success = false;
			message = "newwork cannot use [code:" + String.valueOf(res) + "]";
			current_speed = 0;
			handler.sendEmptyMessage(msg_finish_test);
		}
	}

	@Override
	public void start_download(String test_id) {
		if (!start_test) {
			timer01 = 0;
			start_test = true;
			handler.sendEmptyMessage(msg_start_test);
		}

	}

	@Override
	public void error_download(String test_id, String error_message) {

	}

	@Override
	public void finish_download(String test_id) {
		if (timer01 > test_overflow)
			return;
		if (speed_cnt < 1) {
			speed_cnt = 1;
		}
		current_speed = speed_sum / speed_cnt;
		is_success = true;
		message = "test finish [" + test_id + "]";
		handler.sendEmptyMessage(msg_finish_test);
	}

	private void start_timer() {
		if (mTimer != null) {
			mTimer.cancel();
		}
		timer01 = 0;
		mTimer = new Timer();
		mTimer.schedule(new TimerTask() {
			@Override
			public void run() {
				if (is_mobile) {
					is_success = false;
					message = "working in 3g";
					current_speed = 0;
					handler.sendEmptyMessage(msg_finish_test);
				} else if (start_test) {
					if (timer01 > test_overflow) {
						if (speed_cnt < 1) {
							speed_cnt = 1;
						}
						current_speed = speed_sum / speed_cnt;
						is_success = true;
						message = "test finish [time overflow]";
						handler.sendEmptyMessage(msg_finish_test);

					} else {
						current_speed = current_speed();
						speed_sum += current_speed;
						speed_cnt++;
						if (timer02 > 4) {
							timer02 = 0;
							handler.sendEmptyMessage(msg_tick_speed);
						}
						timer02++;
					}
				} else {
					if (timer01 > connection_overflow) {
						is_success = false;
						message = "newwork cannot download";
						current_speed = 0;
						handler.sendEmptyMessage(msg_finish_test);
					}
				}
				timer01++;
			}
		}, 0, 500);
	}

	public void stop_test() {
		is_test = false;
		if (test_list != null && test_list.size() > 0) {
			for (Download_test i : test_list) {
				if (i != null) {
					i.stop_test();
				}
			}
		}
	}

	private long current_speed() {
		// get the flow without mobile network
		long current_mobile_flow = TrafficStats.getMobileTxBytes()
				+ TrafficStats.getMobileRxBytes();
		long current_total_flow = TrafficStats.getTotalRxBytes()
				+ TrafficStats.getTotalTxBytes();
		long mobile_speed = current_mobile_flow - last_mobile_flow;
		long total_speed = current_total_flow - last_flow;
		if (mobile_speed >= total_speed * 0.9) {
			is_mobile = true;
		}
		last_flow = current_total_flow;
		last_mobile_flow = current_mobile_flow;
		return (total_speed - mobile_speed) << 1;
	}

	public static int executeCommand(final String command, final long timeout)
			throws IOException, InterruptedException, TimeoutException {
		Process process = Runtime.getRuntime().exec(command);
		Worker worker = new Worker(process);
		worker.start();
		try {
			worker.join(timeout);
			if (worker.exit != null) {
				return worker.exit;
			} else {
				throw new TimeoutException();
			}
		} catch (InterruptedException ex) {
			worker.interrupt();
			Thread.currentThread().interrupt();
			throw ex;
		} finally {
			process.destroy();
		}
	}

	private static class Worker extends Thread {
		private final Process process;
		public Integer exit;

		private Worker(Process process) {
			this.process = process;
		}

		public void run() {
			try {
				exit = process.waitFor();
			} catch (InterruptedException ignore) {
				return;
			}
		}
	}

	public interface Test_result_listener {
		// one of the thread have been starting download
		public void start_test();

		// when test ends
		public void finish_test(boolean is_success, long speed, String message);

		// set current speed
		public void current_speed(long current_speed);
	}
}
