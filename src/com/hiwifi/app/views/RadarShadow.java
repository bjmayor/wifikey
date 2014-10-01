package com.hiwifi.app.views;

import com.hiwifi.hiwifi.R;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Rect;
import android.util.AttributeSet;
import android.view.View;

public class RadarShadow extends View {

	private Bitmap img;
	private int img_left;
	private int img_top;

	public RadarShadow(Context context) {
		super(context);
	}

	public RadarShadow(Context context, AttributeSet attrs) {
		super(context, attrs);
	}

	public RadarShadow(Context context, AttributeSet attrs, int defStyleAttr) {
		super(context, attrs, defStyleAttr);
	}

	@SuppressLint("DrawAllocation")
	@Override
	protected void onLayout(boolean changed, int left, int top, int right,
			int bottom) {
		if (changed) {
			load_img(right - left, bottom - top);
		}
		super.onLayout(changed, left, top, right, bottom);
	}

	@Override
	protected void onDraw(Canvas canvas) {
		super.onDraw(canvas);
		if (img != null) {
			canvas.drawBitmap(img, img_left, img_top, null);
		}
	}

	private void load_img(int width, int height) {
		// get the location to draw the shadow
		img_left = (int) (width * 0.5f - width * 0.356375f * Radar.zoom);
		img_top = (int) (width * 0.098119f * Radar.zoom);
		// load the original shadow image and get the size
		Bitmap org_img = BitmapFactory.decodeResource(getResources(),
				R.drawable.radar_shadow);
		int org_height = org_img.getHeight();
		int org_width = org_img.getWidth();
		// calculate the max space to draw the image
		int max_width = width - img_left;
		int max_height = height - img_top;
		// calculate the zoom rate so that radar and shadow have the same size
		float img_zoom = (float) width / org_width * 1.124552f * Radar.zoom;
		// calculate the size of shadow
		int img_height = (int) (org_height * img_zoom);
		int img_width = (int) (org_width * img_zoom);
		// if the shadow's width is out of max space
		// cut the original shadow image's width
		if (img_width > max_width) {
			float width_zoom = (float) max_width / img_width;
			org_width = (int) (org_width * width_zoom);
			img_width = max_width;
		}
		// if the shadow's height is out of the max space
		// cut the original shadow image's height
		if (img_height > max_height) {
			float height_zoom = (float) max_height / img_height;
			org_height = (int) (org_height * height_zoom);
			img_height = max_height;
		}
		// create a area to capture image form original image
		Rect org_area = new Rect(0, 0, org_width, org_height);
		// create a area to draw image to background
		Rect img_area = new Rect(0, 0, img_width, img_height);
		// if background is not null then clean background
		if (img != null) {
			img.recycle();
		}
		// create a new background and draw the original shadow image
		img = Bitmap.createBitmap(img_width, img_height,
				Bitmap.Config.ARGB_8888);
		Canvas c = new Canvas(img);
		c.drawBitmap(org_img, org_area, img_area, null);
		// clean the original shadow image and template variables
		org_img.recycle();
		org_img = null;
		org_area = null;
		img_area = null;
	}
}
