package com.hiwifi.app.views;

import android.content.Context;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.ColorDrawable;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnTouchListener;
import android.view.ViewGroup.LayoutParams;
import android.view.animation.AlphaAnimation;
import android.view.animation.Animation;
import android.view.animation.Animation.AnimationListener;
import android.widget.PopupWindow;
import android.widget.RelativeLayout;

import com.hiwifi.hiwifi.R;

public class SelectPicPopupWindow extends PopupWindow {

	private View mMenuView;

	public SelectPicPopupWindow(Context context, int layout,
			IPopwindowCallback ip) {
		super(context);
		LayoutInflater inflater = (LayoutInflater) context
				.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		mMenuView = inflater.inflate(layout, null);
		// 设置按钮监听
		// 设置SelectPicPopupWindow的View
		this.setContentView(mMenuView);
		// 设置SelectPicPopupWindow弹出窗体的宽
		this.setWidth(LayoutParams.FILL_PARENT);
		// 设置SelectPicPopupWindow弹出窗体的高
		this.setHeight(LayoutParams.FILL_PARENT);
		// 设置SelectPicPopupWindow弹出窗体可点击
		this.setFocusable(true);
		// 设置SelectPicPopupWindow弹出窗体动画效果
		this.setAnimationStyle(R.style.AnimBottom);
		// 实例化一个ColorDrawable颜色为半透明
		ColorDrawable dw = new ColorDrawable(0xb0000000);
		// 设置SelectPicPopupWindow弹出窗体的背景
		this.setBackgroundDrawable(new BitmapDrawable());
		this.setOutsideTouchable(true);
		if (ip != null) {
			ip.showWindowDetail(mMenuView);
		}
	}

	public SelectPicPopupWindow(Context context, final View layout,
			IPopwindowCallback ip) {
		super(context);
		// 设置按钮监听
		// 设置SelectPicPopupWindow的View
		this.setContentView(layout);
		// 设置SelectPicPopupWindow弹出窗体的宽
		this.setWidth(LayoutParams.FILL_PARENT);
		// 设置SelectPicPopupWindow弹出窗体的高
		this.setHeight(LayoutParams.FILL_PARENT);
		// 设置SelectPicPopupWindow弹出窗体可点击
		this.setFocusable(true);
		// 设置SelectPicPopupWindow弹出窗体动画效果
		this.setAnimationStyle(R.style.PopAnimStyle);
		// 实例化一个ColorDrawable颜色为半透明
		ColorDrawable dw = new ColorDrawable(0xb0000000);
		// 设置SelectPicPopupWindow弹出窗体的背景
		this.setBackgroundDrawable(new BitmapDrawable());
		this.setOutsideTouchable(true);
		if (ip != null) {
			ip.showWindowDetail(mMenuView);
		}
//		final RelativeLayout content_container = (RelativeLayout) layout.findViewById(R.id.content_container);
//		final View view = layout.findViewById(R.id.shadow);
//		Animation animation = new AlphaAnimation(0f, 1f);
//		animation.setDuration(300);
//		animation.setFillAfter(true);
//		final Animation animation2 = new AlphaAnimation(1f, 0f);
//		animation2.setDuration(100);
//		animation2.setFillAfter(true);
//		layout.startAnimation(animation);
//		animation.setAnimationListener(new AnimationListener() {
//			
//			@Override
//			public void onAnimationStart(Animation animation) {
////				view.setVisibility(View.VISIBLE);
////				content_container.setVisibility(View.VISIBLE);
//			}
//			
//			@Override
//			public void onAnimationRepeat(Animation animation) {
//				// TODO Auto-generated method stub
//				
//			}
//			
//			@Override
//			public void onAnimationEnd(Animation animation) {
////				layout.clearAnimation();
//			}
//		});
		
//		Animation content_anim_in = AnimationUtils.loadAnimation(context, R.anim.popup_anim_in);
////		Animation content_anim_out = AnimationUtils.loadAnimation(context, R.anim.popup_anim_out);
//		content_anim_in.setDuration(300);
//		content_anim_in.setFillAfter(true);
//		content_container.startAnimation(content_anim_in);
		
		layout.setOnTouchListener(new OnTouchListener() {

			@Override
			public boolean onTouch(View v, MotionEvent event) {

				int height = layout.findViewById(R.id.myscroll).getTop();
				int y = (int) event.getY();
				if (event.getAction() == MotionEvent.ACTION_UP) {
					if (y < height) {
						dismiss();
					}
				}
				return true;
			}
		});
		this.setOnDismissListener(new OnDismissListener() {
			
			@Override
			public void onDismiss() {
//				layout.startAnimation(animation2);
//				view.setVisibility(View.GONE);
//				content_container.setVisibility(View.GONE);
//				layout.clearAnimation();
			}
		});
		
	}

	public interface IPopwindowCallback {
		public void showWindowDetail(View window);
	}
}
