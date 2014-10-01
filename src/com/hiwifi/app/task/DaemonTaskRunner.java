package com.hiwifi.app.task;

import java.util.Iterator;
import java.util.LinkedList;

import android.os.Handler;
import android.os.Looper;
import android.util.Log;

import com.hiwifi.model.log.HWFLog;

/**
 * @description the asynchronous task queue
 * @author jack
 * @date 2013-7-1
 */
public class DaemonTaskRunner implements Runnable {
	private final String TAG = "DaemonTaskRunner";
	private volatile boolean runFlag = true;
	private Handler handler = null;
	private DaemonTask task = null;
	private Thread thread;
	private int sleeptime;

	/**
	 * 异步任务队列
	 */
	protected LinkedList<DaemonTask> taskList = new LinkedList<DaemonTask>();

	public DaemonTaskRunner() {
		init();
	}
	
	public int getSleeptime() {
		return sleeptime;
	}
	
	public void setSleeptime(int sleeptime) {
		this.sleeptime = sleeptime;
	}
	

	public boolean isRunFlag() {
		return runFlag;
	}

	public void setRunFlag(boolean runFlag) {
		this.runFlag = runFlag;
	}

	/**
	 * 初始化方法，启动后台异步任务管理器
	 */
	public void init() {
		if (Looper.myLooper()!=null) {
			handler  = new Handler();
		}
		else
		{
			HWFLog.e(TAG, "not looper.prepare, force to sync mode");
		}
		this.runFlag = true;
		thread = new Thread(this);
		thread.start();
	}
	public void finish() {
		synchronized (this.taskList) {
			this.runFlag = false;
			this.taskList.clear();
			this.taskList.notify();
		}
	}

	
	public void notifityThread() {
		synchronized (this.taskList) {
			this.runFlag = true;
			if(thread == null){
				thread = new Thread(this);
			}
			thread.start();
		}
	}

	public void waitThread() {
		synchronized (this.taskList) {
			this.runFlag = false;
		}
	}

	
	/**
	 * 线程执行方法，覆盖Runnable的方法 检查任务队列里面是否有任务，如果有就执行；如果没有就进入休眠状态。
	 */
	public void run() {
		Looper.prepare();
		while (this.runFlag) {
			// 从taskList队尾取出任务并执行
			try {
				synchronized (this.taskList) {
					if (this.taskList.size() != 0) {
						task = this.taskList.removeLast();
						if (task.isCanceled()) {
							task = null;
						}
						if (task != null) {
							task.execute();
							task = null;
						}
					} else {
						// 如果没有任务，就休眠。
						this.taskList.wait();
					}
				}
				
			} catch (Exception e) {
				Log.e("DaemonTaskRunner",
						"DaemonTaskRunner get and run task failed!", e);
			}
		}
		Looper.loop();
	}

	/**
	 * 添加一个异步任务到执行队列 添加后唤醒执行线程
	 * 
	 * @param task
	 *            DaemonTask
	 */
	public void addTask(final DaemonTask task) {
		if (this.runFlag) {
			if(handler!=null)
			{
				handler.postDelayed(new Runnable() {
					@Override
					public void run() {
						addOneTask(task);
					}
				}, getSleeptime());
			}
			else {
				addOneTask(task);
			}
			
		} else {
			return;
		}
	}
	
	private void addOneTask(final DaemonTask task){
		synchronized (DaemonTaskRunner.this.taskList) {
			DaemonTaskRunner.this.taskList.addLast(task);
			task.setTaskRunner(this);
			DaemonTaskRunner.this.taskList.notifyAll();
			HWFLog.e(TAG, "add task"+task.getClass().getSimpleName());
		}
	}
	
	public void onTaskCancel(final DaemonTask task)
	{
		synchronized (this.taskList) {
			if (this.taskList.contains(task)) {
				this.taskList.remove(task);
			}
		}
	}
	
	public void removeAllTask()
	{
		synchronized (this.taskList) {
			Iterator<DaemonTask> iterator = this.taskList.iterator();
			while (iterator.hasNext()) {
				DaemonTask daemonTask = (DaemonTask) iterator.next();
				daemonTask.cancel();
				iterator.remove();
			}
		}
	}
	
	
}
