package com.hiwifi.utils;

import android.view.animation.AlphaAnimation;
import android.view.animation.Animation;
import android.view.animation.LinearInterpolator;
import android.view.animation.RotateAnimation;
import android.widget.ImageView;

/**
 * @filename AnimationUtil.java
 * @packagename com.hiwifi.utils
 * @projectname hiwifi1.0.1
 * @author jack at 2014-8-29
 */
public class AnimationUtil {
	/**
	 * @description ImageView旋转效果
	 * @return void
	 * @param iv_chat_head
	 */
	public static  void setFlickerAnimation(ImageView iv_chat_head) {
		final Animation animation = new RotateAnimation(0f, 360f,Animation.RELATIVE_TO_SELF, 
				0.5f,Animation.RELATIVE_TO_SELF,0.5f); // Change alpha from fully visible to invisible
		animation.setDuration(500); // duration - half a second
		animation.setInterpolator(new LinearInterpolator()); // do not alter animation rate
		animation.setRepeatCount(Animation.INFINITE); // Repeat animation infinitely
//		animation.setRepeatMode(Animation.REVERSE); // 
		iv_chat_head.setAnimation(animation);
		animation.startNow();
	}
}
