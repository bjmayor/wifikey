package com.hiwifi.app.task;

import com.hiwifi.hiwifi.Gl;
import com.hiwifi.model.log.HWFLog;

/**
 * @description the asynchronous task
 * @author jack
 * @date 2013-7-1
 */
public abstract class DaemonTask {
	private static final String TAG = "DaemonTask";
	protected Boolean mCancel = false;
	protected DaemonTaskRunner relativeTaskRunner = null;

	/**
	 * 任务执行方法。需要同步执行，否则队列就没有意义了。
	 */
	public abstract void execute();
	
	public void execute(Boolean async) {
		if (async) {
			new Thread(new Runnable() {
				@Override
				public void run() {
					HWFLog.e(TAG, "called executed"+this.getClass().getSimpleName() + "thread id "+Thread.currentThread().getId());
					execute();
				}
			}).start();
		} else {
			execute();
		}
	}
	
	public void setTaskRunner(DaemonTaskRunner runner) {
		if (runner != null) {
			this.relativeTaskRunner = runner;
		}
	}

	public void onFinished(Boolean finished) {
		Gl.TaskRecord.setTaskExcutedToday(this.getClass(), finished);
	}

	public void cancel() {
		mCancel = true;
		onFinished(false);
		if (this.relativeTaskRunner != null) {
			this.relativeTaskRunner.onTaskCancel(this);
		}
	}

	public Boolean isCanceled() {
		return mCancel;
	}

}
