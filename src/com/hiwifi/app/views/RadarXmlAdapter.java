package com.hiwifi.app.views;

import android.content.Context;
import android.util.AttributeSet;
import android.view.View;

/**
 * 
 * @author ZhiHao.Kang this View is use for adjust the height between radar view
 *         and radar button
 */
public class RadarXmlAdapter extends View {

	public RadarXmlAdapter(Context context) {
		super(context);
	}

	public RadarXmlAdapter(Context context, AttributeSet attrs) {
		super(context, attrs);
	}

	public RadarXmlAdapter(Context context, AttributeSet attrs, int defStyleAttr) {
		super(context, attrs, defStyleAttr);
	}

	/**
	 * the height is calculated by the width
	 */
	@Override
	protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
		float w = MeasureSpec.getSize(widthMeasureSpec);
		float h = w * 0.025f * Radar.zoom;
		setMeasuredDimension((int) w, (int) h);
	}
}
