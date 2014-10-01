package com.hiwifi.app.utils;

import android.app.Dialog;
import android.content.Context;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.Window;

import com.hiwifi.hiwifi.R;

public class HintDialog {

	Dialog mDialog = null;
	private Context mContext;
	private IHintDialog mDialogInstance = null;
	
	/**
	 * 构造函数
	 * @param context
	 */
	public HintDialog(Context context) {
		this.mContext =context;
		mDialog = new Dialog(mContext,R.style.NotificationDialog){
			public boolean onKeyDown(int keyCode, KeyEvent event) {
				if(keyCode == KeyEvent.KEYCODE_BACK && mDialogInstance !=null){
					mDialogInstance.onKeyDown(keyCode, event);
					return true;
				}
				return super.onKeyDown(keyCode, event);
			};
		};
		mDialog.setCancelable(false);
		mDialog.setCanceledOnTouchOutside(false);
		
	}
	
	
	/**
	 * 构造函数
	 * @param iLayoutResId 此Dialog采用的自定义布局文件
	 *  @param interfaceInstance 此Dialog需要实现的一些接口回调事件
	 */
	public void showDialog(int iLayoutResId,IHintDialog interfaceInstance){
		if(mDialog == null || iLayoutResId == 0){
			return;
		}
		
		mDialogInstance = interfaceInstance;
		mDialog.show();
		mDialog.setContentView(iLayoutResId);
		Window window = mDialog.getWindow();
		window.setGravity(Gravity.CENTER);
		if(mDialogInstance!=null){
			mDialogInstance.showWindowDetail(window);
		}
	}
	
	/**
	 * 销毁Dialog
	 */
	public void dissmissDialog(){
		if(mDialog!=null && mDialog.isShowing()){
			mDialog.dismiss();
			mDialog = null;
		}
	}
	
	/**
	 * 判断Dialog是否显示
	 * @return
	 */
	public boolean isShowing(){
		if(mDialog!=null && mDialog.isShowing()){
			return mDialog.isShowing();
		}
		return false;
	}
	
	
	/**
	 * 事件回调接口
	 *
	 */
	public interface IHintDialog{
		public void onKeyDown(int keyCode,KeyEvent event);
		public void showWindowDetail(Window window);
	}
}
