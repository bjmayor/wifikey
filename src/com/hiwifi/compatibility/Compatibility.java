package com.hiwifi.compatibility;

import android.annotation.TargetApi;
import android.os.Build;
import android.view.View;
import android.view.animation.AlphaAnimation;
import android.widget.FrameLayout;

public class Compatibility {
	
	@TargetApi(Build.VERSION_CODES.HONEYCOMB)
	public static void setAlpha(float alpha, View view)
	{
		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB) {
			view.setAlpha(alpha);
		}
		else {
			AlphaAnimation alphaAnimation = new AlphaAnimation(alpha, alpha);
			alphaAnimation.setDuration(0); // Make animation instant
			alphaAnimation.setFillAfter(true); // Tell it to persist after the animation ends
			view.startAnimation(alphaAnimation);
		}
	}
}
