package com.hiwifi.utils;

import java.io.File;

import android.content.Context;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.Bitmap.Config;
import android.graphics.BitmapFactory;
import android.graphics.BitmapFactory.Options;
import android.graphics.Canvas;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffXfermode;
import android.graphics.Rect;
import android.graphics.RectF;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.util.Log;

/**
 * Utility functions on image.
 * 
 * @author umbalaconmeogia
 * 
 */
public class ImageUtil {

	/**
	 * Load a picture from the resource.
	 * 
	 * @param resources
	 * @param resourceId
	 * @return
	 */
	public static Bitmap loadBitmapFromResource(Resources resources,
			int resourceId) {
		// Not scale the bitmap. This will turn the bitmap in raw pixel unit.
		Options options = new BitmapFactory.Options();
		options.inScaled = false;
		// Load the bitmap object.
		Bitmap bitmap = BitmapFactory.decodeResource(resources, resourceId,
				options);
		// Set the density to NONE. This is needed for the ImageView to not
		// scale.
		bitmap.setDensity(Bitmap.DENSITY_NONE);

		return bitmap;
	}

	/**
	 * Calculate ratio to scale an object to fit the max size (width or height). <br/>
	 * Usage example: <br/>
	 * 
	 * <pre>
	 * float ratio = sizeFitRatio(width, height, maxWidth, maxHeight);
	 * // Calculate size to fit maximum width or height.
	 * int newWidth = ratio * width;
	 * int newHeight = ratio * height;
	 * </pre>
	 * 
	 * @param objectWidth
	 * @param objectHeight
	 * @param maxWidth
	 * @param maxHeight
	 * @return
	 */
	public static float sizeFitRatio(float objectWidth, float objectHeight,
			float maxWidth, float maxHeight) {
		float ratio = 1f; // Default ratio to 1.
		if (objectWidth != 0 && objectHeight != 0) {
			float ratioWidth = maxWidth / objectWidth;
			float ratioHeight = maxHeight / objectHeight;
			float minRatio = (ratioWidth < ratioHeight) ? ratioWidth
					: ratioHeight;
			if (minRatio > 0) {
				ratio = minRatio;
			}
		}
		return ratio;
	}

	/**
	 * Scale an bitmap to fit frame size.
	 * 
	 * @param bitmap
	 * @param frameWidth
	 * @param frameHeight
	 * @return Scaled bitmap, or the bitmap itself if scaling is unnecessary.
	 */
	public static Bitmap scaleToFitFrame(Bitmap bitmap, float frameWidth,
			float frameHeight) {
		if (bitmap != null) {
			int bitmapWidth = bitmap.getWidth();
			int bitmapHeight = bitmap.getHeight();
			float ratio = sizeFitRatio(bitmapWidth, bitmapHeight, frameWidth,
					frameHeight);
			return scaleImage(bitmap, ratio);
		} else {
			return null;
		}

	}

	/**
	 * Scale a bitmap.
	 * 
	 * @param bitmap
	 * @param ratio
	 * @return
	 */
	public static Bitmap scaleImage(Bitmap bitmap, float ratio) {
		Bitmap result = bitmap;
		if (ratio != 1f) {
			int newWidth = (int) (bitmap.getWidth() * ratio);
			int newHeight = (int) (bitmap.getHeight() * ratio);
			try {
				result = Bitmap.createScaledBitmap(bitmap, newWidth, newHeight,
						true);
			} catch (IllegalArgumentException e) {
				e.printStackTrace();
				return null;
			} catch (OutOfMemoryError e) {
				e.printStackTrace();
				System.gc();
				return null;
			}
		}
		return result;
	}

	/**
	 * Create rounded corner bitmap from original bitmap.
	 * <p>
	 * Reference
	 * http://stackoverflow.com/questions/2459916/how-to-make-an-imageview
	 * -to-have-rounded-corners
	 * 
	 * @param input
	 *            Original bitmap.
	 * @param cornerRadius
	 *            Corner radius in pixel.
	 * @param w
	 * @param h
	 * @param cornerTopLeft
	 * @param cornerTopRight
	 * @param cornerBottomLeft
	 * @param cornerBottomRight
	 * @return
	 */
	public static Bitmap getRoundedCornerBitmap(Bitmap input,
			float cornerRadius, float w, float h, boolean cornerTopLeft,
			boolean cornerTopRight, boolean cornerBottomLeft,
			boolean cornerBottomRight) {

		Bitmap output = null;
		try {
			output = Bitmap.createBitmap((int) w, (int) h, Config.ARGB_8888);
		} catch (IllegalArgumentException e) {
			e.printStackTrace();
			return null;
		} catch (OutOfMemoryError e) {
			e.printStackTrace();
			System.gc();
			return null;
		}

		Canvas canvas = new Canvas(output);

		final Paint paint = new Paint(Paint.ANTI_ALIAS_FLAG);
		final RectF rectF = new RectF(0, 0, w, h);

		// make sure that our rounded corner is scaled appropriately
		final float roundPx = cornerRadius;

		canvas.drawARGB(0, 0, 0, 0);
		paint.setColor(0xff424242);
		canvas.drawRoundRect(rectF, roundPx, roundPx, paint);

		// draw rectangles over the corners we want to be square
		if (cornerTopLeft == false) {
			canvas.drawRect(0, 0, w / 2, h / 2, paint);
		}
		if (cornerTopRight == false) {
			canvas.drawRect(w / 2, 0, w, h / 2, paint);
		}
		if (cornerBottomLeft == false) {
			canvas.drawRect(0, h / 2, w / 2, h, paint);
		}
		if (cornerBottomRight == false) {
			canvas.drawRect(w / 2, h / 2, w, h, paint);
		}

		paint.setXfermode(new PorterDuffXfermode(PorterDuff.Mode.SRC_IN));
		Matrix matrix = new Matrix();
		matrix.postScale(w / input.getWidth(), h / input.getHeight());
		canvas.drawBitmap(input, matrix, paint);

		return output;
	}

	/**
	 * 截取方图
	 * 
	 * @param bitmap
	 * @return
	 */
	public static Bitmap centerCrop(Bitmap bitmap) {
		int h = bitmap.getHeight();
		int w = bitmap.getWidth();
		int l;
		int startX, startY;
		if (h == w) {
			return bitmap;
		}
		if (h > w) {
			l = w;
			startX = 0;
			startY = (h - l) / 2;
		} else {
			l = h;
			startX = (w - l) / 2;
			startY = 0;
		}

		Bitmap bitmap1 = null;
		try {
			bitmap1 = Bitmap.createBitmap(bitmap, startX, startY, l, l);
		} catch (Exception exception) {
			exception.printStackTrace();
		}

		return bitmap1;
	}

	/**
	 * 按比例截图
	 * 
	 * @param bitmap
	 * @param width
	 * @param height
	 * @return
	 */
	public static Bitmap centerCrop(Bitmap bitmap, float width, float height) {
		int h = bitmap.getHeight();
		int w = bitmap.getWidth();
		double targetScale = width / (double) height;
		double sourceScale = w / (double) h;
		int startX, startY, xLength, yLength;

		if (targetScale == sourceScale) {
			return bitmap;
		} else if (targetScale > sourceScale) { // x = 0， 获取y位置,目标图的宽度全要
			xLength = w;
			yLength = (int) (xLength / targetScale);
			startX = 0;
			startY = (h - yLength) / 2;
		} else { // y = 0,获取x位置，目标图的高度全要
			yLength = h;
			xLength = (int) (targetScale * yLength);
			startY = 0;
			startX = (w - xLength) / 2;
		}

		Bitmap bitmap1 = null;
		try {
			bitmap1 = Bitmap.createBitmap(bitmap, startX, startY, xLength,
					yLength);
		} catch (Exception exception) {
			exception.printStackTrace();
		}

		return bitmap1;
	}

	/**
	 * 只能压缩图片
	 * 
	 * @param path
	 * @return
	 */
	public static Bitmap fitSizePic(String path) {
		File f = new File(path);
		Bitmap resizeBmp = null;
		BitmapFactory.Options opts = new BitmapFactory.Options();
		// 数字越大读出的图片占用的heap越小 不然总是溢出
		if (f.length() < 20480) { // 0-20k
			opts.inSampleSize = 1;
		} else if (f.length() < 51200) { // 20-50k
			opts.inSampleSize = 1;
		} else if (f.length() < 307200) { // 50-300k
			opts.inSampleSize = 1;
		} else if (f.length() < 819200) { // 300-800k
			opts.inSampleSize = 3;
		} else if (f.length() < 1048576) { // 800-1024k
			opts.inSampleSize = 4;
		} else {
			opts.inSampleSize = 10;
		}
		resizeBmp = BitmapFactory.decodeFile(f.getPath(), opts);
		return resizeBmp;
	}

	/**
	 * 获取缩放后的bitmap（较大）
	 * 
	 * @param path
	 * @return
	 */
	public static Bitmap fitBigSizePic(String path) {
		File f = new File(path);
		Bitmap resizeBmp = null;
		BitmapFactory.Options opts = new BitmapFactory.Options();
		// 数字越大读出的图片占用的heap越小 不然总是溢出
		if ((f.length() > 1048576 * 5)) {
			opts.inSampleSize = 10;
		} else if ((f.length() > 1048576 * 4)) {
			opts.inSampleSize = 8;
		} else if ((f.length() > 1048576 * 3)) {
			opts.inSampleSize = 6;
		} else if ((f.length() > 1048576 * 2)) {
			opts.inSampleSize = 4;
		} else if ((f.length() > 1048576)) {
			opts.inSampleSize = 2;
		} else if ((f.length() > 524288)) {
			opts.inSampleSize = 2;
		}
		opts.inPurgeable = true;
		resizeBmp = BitmapFactory.decodeFile(f.getPath(), opts);
		return resizeBmp;
	}

	public static Bitmap drawableToBitamp(Drawable drawable) {
		BitmapDrawable bd = (BitmapDrawable) drawable;
		return bd.getBitmap();
	}

	/*
	 * public static Bitmap centerCrop(Bitmap bitmap, float ivW, float ivH) {
	 * 
	 * int h = bitmap.getHeight(); int w = bitmap.getWidth(); int width,height;
	 * int startX,startY; float ratio;
	 * 
	 * if (ivW == ivH) { if (h == w) { return bitmap; } if (h > w) { width = w;
	 * startX = 0; startY = (h - width)/2; }else { width = h; startX = (w -
	 * width)/2; startY = 0; } }else {
	 * 
	 * if(ivH/h - ivW/w > 0){ ratio = ivW/w; }else { ratio = ivH/h; } width =
	 * (int) (w*ratio); height = (int) (h*ratio);
	 * 
	 * 
	 * }
	 * 
	 * 
	 * Bitmap bitmap1 = Bitmap.createBitmap(bitmap, startX, startY, width,
	 * width);
	 * 
	 * return bitmap1; }
	 */

	/**
	 * 根据控件宽度等比例缩放图片
	 * 
	 * @param bitmap
	 * @param screenWidth
	 * @return
	 */
	public static Bitmap adaptiveW(Bitmap bitmap, float screenWidth) {
		float scale;
		Matrix matrix = new Matrix();
		int width = bitmap.getWidth();// 获取资源位图的宽
		int height = bitmap.getHeight();// 获取资源位图的高
		// System.out.println(width + "width=====height" + height +
		// "screenWidth"
		// + screenWidth);
		if (width > screenWidth) {
			float w = screenWidth / bitmap.getWidth();
			matrix.postScale(w, w);// 获取缩放比例
			// 根据缩放比例获取新的位图
			Bitmap newbmp = Bitmap.createBitmap(bitmap, 0, 0, width, height,
					matrix, true);
			return newbmp;

		}
		return bitmap;
	}

	/**
	 * 根据控件高度等比例缩放图片
	 * 
	 * @param bitmap
	 * @param screenHeight
	 * @return
	 */
	public static Bitmap adaptiveH(Bitmap bitmap, float screenHeight) {
		float scale;
		Matrix matrix = new Matrix();
		int width = bitmap.getWidth();// 获取资源位图的宽
		int height = bitmap.getHeight();// 获取资源位图的高
		if (height>0 && width >0 && height > screenHeight) {
			float w = screenHeight / bitmap.getHeight();
			matrix.postScale(w, w);// 获取缩放比例
			// 根据缩放比例获取新的位图
			Bitmap newbmp = Bitmap.createBitmap(bitmap, 0, 0, width, height,
					matrix, true);
			return newbmp;

		}
		return bitmap;
	}

	/**
	 * 按角度旋转图片
	 * 
	 * @param context
	 * @param id
	 * @param angle
	 * @return
	 */
	public static Bitmap rotateBitMap(Context context, int id, int angle) {
		Resources res = context.getResources();
		Bitmap img = BitmapFactory.decodeResource(res, id);
		Matrix matrix = new Matrix();
		matrix.postRotate(angle);
		int width = img.getWidth();
		int height = img.getHeight();
		return Bitmap.createBitmap(img, 0, 0, width, height, matrix, true);
	}

	public static Bitmap adaptiveWidth(Bitmap bitmap, float screenWidth,
			float screanHight) {
		float scale;
		Matrix matrix = new Matrix();
		int width = bitmap.getWidth();// 获取资源位图的宽
		int height = bitmap.getHeight();// 获取资源位图的高
		if (width > screenWidth) {
			float w = screenWidth / bitmap.getWidth();
			float h = height * w;
			int cutW = 0;
			if (h > screanHight) {
				cutW = (int) ((h - screanHight) * 1.5);
			}
			matrix.postScale(w, w);// 获取缩放比例
			// 根据缩放比例获取新的位图
			Bitmap newbmp = Bitmap.createBitmap(bitmap, 0, cutW, width, height
					- cutW, matrix, true);
			return newbmp;
		} else {
			// TODO
			float w = screenWidth / width;
			matrix.postScale(w, w);// 获取缩放比例
			// 根据缩放比例获取新的位图
			Bitmap newbmp = Bitmap.createBitmap(bitmap, 0, 0, width, height,
					matrix, true);
			return newbmp;
		}
		// return bitmap;
	}

	/**
	 * 根据控件宽度等比例缩放图片
	 * 
	 * @param bitmap
	 * @param screenWidth
	 * @return
	 */
	public static Bitmap adaptive(Bitmap bitmap, float w) {
		float scale;
		Matrix matrix = new Matrix();
		int width = bitmap.getWidth();// 获取资源位图的宽
		int height = bitmap.getHeight();// 获取资源位图的高
		matrix.postScale(w, w);// 获取缩放比例
		// 根据缩放比例获取新的位图
		Bitmap newbmp = Bitmap.createBitmap(bitmap, 0, 0, width, height,
				matrix, true);
		return bitmap;
	}
	
	
	public static Bitmap makeRoundCorner(Bitmap bitmap)
    {
        int width = bitmap.getWidth();
        int height = bitmap.getHeight();
        int left = 0, top = 0, right = width, bottom = height;
        float roundPx = height/2;
        if (width > height) {
            left = (width - height)/2;
            top = 0;
            right = left + height;
            bottom = height;
        } else if (height > width) {
            left = 0;
            top = (height - width)/2;
            right = width;
            bottom = top + width;
            roundPx = width/2;
        }
        Log.i("hehe", "ps:"+ left +", "+ top +", "+ right +", "+ bottom);

        Bitmap output = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(output);
        int color = 0xff424242;
        Paint paint = new Paint();
        Rect rect = new Rect(left, top, right, bottom);
        RectF rectF = new RectF(rect);

        paint.setAntiAlias(true);
        canvas.drawARGB(0, 0, 0, 0);
        paint.setColor(color);
        canvas.drawRoundRect(rectF, roundPx, roundPx, paint);
        paint.setXfermode(new PorterDuffXfermode(PorterDuff.Mode.SRC_IN));
        canvas.drawBitmap(bitmap, rect, rect, paint);
        return output;
    }
	
	public static Bitmap makeRoundCorner(Bitmap bitmap, int px)
    {
        int width = bitmap.getWidth();
        int height = bitmap.getHeight();
        Bitmap output = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(output);
        int color = 0xff424242;
        Paint paint = new Paint();
        Rect rect = new Rect(0, 0, width, height);
        RectF rectF = new RectF(rect);
        paint.setAntiAlias(true);
        canvas.drawARGB(0, 0, 0, 0);
        paint.setColor(color);
        canvas.drawRoundRect(rectF, px, px, paint);
        paint.setXfermode(new PorterDuffXfermode(PorterDuff.Mode.SRC_IN));
        canvas.drawBitmap(bitmap, rect, rect, paint);
        return output;
    }


}
