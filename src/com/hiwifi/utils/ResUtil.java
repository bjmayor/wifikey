package com.hiwifi.utils;

import java.io.InputStream;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.NinePatch;
import android.graphics.drawable.Drawable;

import com.hiwifi.hiwifi.Gl;

public class ResUtil {
	
	public static int getResIdentifier(String id, String type) {
		return Gl.Ct().getResources().getIdentifier(id, type, Gl.Ct().getPackageName());
	}
	
	public static Bitmap getBitmapById(int resId) {
		InputStream is = Gl.Ct().getResources().openRawResource(resId);
		return BitmapFactory.decodeStream(is, null, null);
	}
	
	public static NinePatch getNinePatchBmpByID(int resId) {
		Bitmap tmpBmp = getBitmapById(resId);
		return new NinePatch(tmpBmp, tmpBmp.getNinePatchChunk(), null);
	}
	
	public static Drawable getDrawableByID(int resId){
		return Gl.Ct().getResources().getDrawable(resId);
	}
	
	public static String getStringById(int resId) {
		return Gl.Ct().getResources().getString(resId);
	}
	
	
	public static int getColorById(int resId) {
		return Gl.Ct().getResources().getColor(resId);
	}
	
	public static float getDeminVal(int redId){
		return Gl.Ct().getResources().getDimension(redId);
	}
	
	public static float getDensity(){
		return Gl.Ct().getResources().getDisplayMetrics().density;
	}
}
