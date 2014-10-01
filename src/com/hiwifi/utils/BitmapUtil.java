package com.hiwifi.utils;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;

import android.graphics.Bitmap;
import android.graphics.Bitmap.CompressFormat;
import android.graphics.Bitmap.Config;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.Rect;
import android.util.Log;
import android.view.View;

import com.hiwifi.model.log.LogUtil;


/**
 * 对图片处理的工具类
 * 
 * @author wsl
 * 
 */
public class BitmapUtil {
	private static final String TAG = "BitmapUtil";

	public static final int OPTIONS_SCALE_UP = 0x1;
	public static final int OPTIONS_RECYCLE_INPUT = 0x2;
	private static final int MAXLENGTH = 720;
	public static final int UNCONSTRAINED = -1;
	
	private static final float PICTTURE_SD_QUALITY = 720;
	/**
	 *质量图片压缩
	 * @param image
	 * @return Bitmap
	 */
	public static Bitmap compressImage(Bitmap image) {
		return makeBitmap(Bitmap2Bytes(image), MAXLENGTH*MAXLENGTH);
	}
	
	public static File saveBitmapTofile(Bitmap bmp,String path){  
        CompressFormat format= Bitmap.CompressFormat.JPEG;  
        int quality = 100;  
        File file = new File(path);
        FileOutputStream stream = null;  
       try {  
            stream = new FileOutputStream(file);  
            bmp.compress(format, quality, stream);  
            stream.close();
       } catch (Exception e) {  
               // TODO Auto-generated catch block  
            e.printStackTrace();  
       }  
       return file;
    }  
	
	
	/**
	 * 把图片写入流传递
	 * @param bitmap
	 * @return byte[]
	 */
	public static byte[] Bitmap2Bytes(Bitmap bm) {
		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		bm.compress(Bitmap.CompressFormat.JPEG, 100, baos);
		return baos.toByteArray();
	}
	/**
	 * 通过一个url获取一张bitmap
	 * @param url
	 * @return Bitmap
	 */
	public static Bitmap getBitmap(String url){
		Bitmap bitmap =	BitmapFactory.decodeFile(url);
		return bitmap;
	}
	/**
	 * 释放内存
	 * 
	 * @param bitmap
	 */
	public static void recycledBitmap(Bitmap bitmap) {
		if (bitmap != null && !bitmap.isRecycled()) {
			bitmap.recycle();
		}
		bitmap = null;
	}
//	public static void clearCache() {
//		BitmapCache.getInstance().clearCache();
//		BitmapLruCache.getInstance().clearCache();
//	}
	public static boolean isRecycled(Bitmap bitmap) {
		if (bitmap == null || bitmap.isRecycled()) {
			return true;
		}
		return false;
	}

	public static Bitmap extractThumbnail(Bitmap source, int width, int height, int options) {
		if (source == null) {
			return null;
		}

		float scale;
		if (source.getWidth() < source.getHeight()) {
			scale = width / (float) source.getWidth();
		} else {
			scale = height / (float) source.getHeight();
		}
		Matrix matrix = new Matrix();
		matrix.setScale(scale, scale);
		Bitmap thumbnail = transform(matrix, source, width, height, OPTIONS_SCALE_UP | options);
		return thumbnail;
	}
	/**
	 * Transform source Bitmap to targeted width and height.
	 */
	private static Bitmap transform(Matrix scaler, Bitmap source, int targetWidth, int targetHeight, int options) {
		boolean scaleUp = (options & OPTIONS_SCALE_UP) != 0;
		boolean recycle = (options & OPTIONS_RECYCLE_INPUT) != 0;

		int deltaX = source.getWidth() - targetWidth;
		int deltaY = source.getHeight() - targetHeight;
		if (!scaleUp && (deltaX < 0 || deltaY < 0)) {
			/*
			 * In this case the bitmap is smaller, at least in one dimension,
			 * than the target. Transform it by placing as much of the image as
			 * possible into the target and leaving the top/bottom or left/right
			 * (or both) black.
			 */
			Bitmap b2 = Bitmap.createBitmap(targetWidth, targetHeight, Bitmap.Config.ARGB_8888);
			Canvas c = new Canvas(b2);

			int deltaXHalf = Math.max(0, deltaX / 2);
			int deltaYHalf = Math.max(0, deltaY / 2);
			Rect src = new Rect(deltaXHalf, deltaYHalf, deltaXHalf + Math.min(targetWidth, source.getWidth()), deltaYHalf + Math.min(targetHeight, source.getHeight()));
			int dstX = (targetWidth - src.width()) / 2;
			int dstY = (targetHeight - src.height()) / 2;
			Rect dst = new Rect(dstX, dstY, targetWidth - dstX, targetHeight - dstY);
			c.drawBitmap(source, src, dst, null);
			if (recycle) {
				source.recycle();
			}
			return b2;
		}
		float bitmapWidthF = source.getWidth();
		float bitmapHeightF = source.getHeight();

		float bitmapAspect = bitmapWidthF / bitmapHeightF;
		float viewAspect = (float) targetWidth / targetHeight;

		if (bitmapAspect > viewAspect) {
			float scale = targetHeight / bitmapHeightF;
			if (scale < .9F || scale > 1F) {
				scaler.setScale(scale, scale);
			} else {
				scaler = null;
			}
		} else {
			float scale = targetWidth / bitmapWidthF;
			if (scale < .9F || scale > 1F) {
				scaler.setScale(scale, scale);
			} else {
				scaler = null;
			}
		}

		Bitmap b1;
		if (scaler != null) {
			// this is used for minithumb and crop, so we want to filter here.
			b1 = Bitmap.createBitmap(source, 0, 0, source.getWidth(), source.getHeight(), scaler, true);
		} else {
			b1 = source;
		}

		if (recycle && b1 != source) {
			source.recycle();
		}

		int dx1 = Math.max(0, b1.getWidth() - targetWidth);
		int dy1 = Math.max(0, b1.getHeight() - targetHeight);

		Bitmap b2 = Bitmap.createBitmap(b1, dx1 / 2, dy1 / 2, targetWidth, targetHeight);

		if (b2 != b1) {
			if (recycle || b1 != source) {
				b1.recycle();
			}
		}

		return b2;
	}
	/**
	 * 
	 * 计算图片采样率 Calculate an inSampleSize for use in a
	 * {@link BitmapFactory.Options} object when decoding bitmaps using the
	 * decode* methods from {@link BitmapFactory}. This implementation
	 * calculates the closest inSampleSize that will result in the final decoded
	 * bitmap having a width and height equal to or larger than the requested
	 * width and height. This implementation does not ensure a power of 2 is
	 * returned for inSampleSize which can be faster when decoding but results
	 * in a larger bitmap which isn't as useful for caching purposes.
	 * 
	 * @param options
	 *            An options object with out* params already populated (run
	 *            through a decode* method with inJustDecodeBounds==true
	 * @param reqWidth
	 *            The requested width of the resulting bitmap
	 * @param reqHeight
	 *            The requested height of the resulting bitmap
	 * @return The value to be used for inSampleSize
	 */
	public static int calculateInSampleSize(BitmapFactory.Options options, int reqWidth, int reqHeight) {
		// Raw height and width of image
		final int height = options.outHeight;
		final int width = options.outWidth;
		int inSampleSize = 1;

		if (height > reqHeight || width > reqWidth) {

			// Calculate ratios of height and width to requested height and
			// width
			int heightRatio = 1;
			if (reqHeight != 0) {
				heightRatio = Math.round((float) height / (float) reqHeight);
			}
			int widthRatio = 1;
			if (reqWidth != 0) {
				widthRatio = Math.round((float) width / (float) reqWidth);
			}

			// Choose the smallest ratio as inSampleSize value, this will
			// guarantee a final image
			// with both dimensions larger than or equal to the requested height
			// and width.
			inSampleSize = heightRatio < widthRatio ? heightRatio : widthRatio;

			/*
			 * // This offers some additional logic in case the image has a
			 * strange // aspect ratio. For example, a panorama may have a much
			 * larger // width than height. In these cases the total pixels
			 * might still // end up being too large to fit comfortably in
			 * memory, so we should // be more aggressive with sample down the
			 * image (=larger // inSampleSize).
			 * 
			 * final float totalPixels = width * height;
			 * 
			 * // Anything more than 2x the requested pixels we'll sample down
			 * // further final float totalReqPixelsCap = reqWidth * reqHeight *
			 * 2;
			 * 
			 * while (totalPixels / (inSampleSize * inSampleSize) >
			 * totalReqPixelsCap) { inSampleSize++; }
			 */
		}
		return inSampleSize;
	}

	//图片合成
    public static Bitmap toCoformBitmap(Bitmap background, Bitmap foreground) {
        if( background == null ) {   
           return null;   
        }   
        int bgWidth = background.getWidth();   
        int bgHeight = background.getHeight();   
        int fgWidth = foreground.getWidth();   
        int fgHeight = foreground.getHeight();   
        if (bgWidth!=fgWidth || bgHeight!=fgHeight) {
			LogUtil.e(TAG, "combined two not matched image ,may error.");
		}
        //create the new blank bitmap 创建一个新的和SRC长度宽度一样的位图    
        Bitmap newbmp = Bitmap.createBitmap(bgWidth, bgHeight, Config.ARGB_8888);  
        Canvas cv = new Canvas(newbmp);   
        //draw bg into   
        cv.drawBitmap(background, 0, 0, null);//在 0，0坐标开始画入bg   
        //draw fg into   
        cv.drawBitmap(foreground, 0, 0, null);//在 0，0坐标开始画入fg ，可以从任意位置画入
        //save all clip   
        cv.save(Canvas.ALL_SAVE_FLAG);//保存   
        //store   
        cv.restore();//存储   
        return newbmp;   
   }
    /**
     * view->bitmap
     * @param v
     * @return
     */
    public static Bitmap getViewBitmap(View v, int x, int y, int width, int height) {
        v.clearFocus();
        v.setPressed(false);

        boolean willNotCache = v.willNotCacheDrawing();
        v.setWillNotCacheDrawing(false);

        // Reset the drawing cache background color to fully transparent
        // for the duration of this operation
        int color = v.getDrawingCacheBackgroundColor();
        v.setDrawingCacheBackgroundColor(0);

        if (color != 0) {
            v.destroyDrawingCache();
        }
        v.buildDrawingCache(true);
        Bitmap cacheBitmap = v.getDrawingCache(true);
        if (cacheBitmap == null) {
            Log.e("TTTTTTTTActivity", "failed getViewBitmap(" + v + ")", new RuntimeException());
            return null;
        }

        Bitmap bitmap = Bitmap.createBitmap(cacheBitmap,x ,y, width, height);

        // Restore the view
        v.destroyDrawingCache();
        v.setWillNotCacheDrawing(willNotCache);
        v.setDrawingCacheBackgroundColor(color);

        return bitmap;
    }
    
    public static Bitmap getNormnalBitmap(File file){
    	BitmapFactory.Options bfOptions = new BitmapFactory.Options();
		bfOptions.inDither = false;
		bfOptions.inPurgeable = true;
		bfOptions.inTempStorage=new byte[12 * 1024]; 
		try {
			FileInputStream fs= new FileInputStream(file);
			Bitmap bmp = null;
			if(fs != null){
				  bmp = BitmapFactory.decodeFileDescriptor(fs.getFD(), null, bfOptions);
			}
			fs.close();
			return bmp;
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			return null;
		}
    }
    /**
     * view->bitmap
     * @param v
     * @return
     */
    public static Bitmap getViewBitmap(View v) {
    	return getViewBitmap(v, 0, 0, v.getWidth(), v.getHeight());
    }
    
    
    public static Bitmap getBitmapById(int resId) {
		return ResUtil.getBitmapById(resId);
	}
    
	public static int getTextWidth(Paint paint, String str) {
		int iRet = 0;
		if (str != null && str.length() > 0) {
			int len = str.length();
			float[] widths = new float[len];
			paint.getTextWidths(str, widths);
			for (int j = 0; j < len; j++) {
				iRet += (int) Math.ceil(widths[j]);
			}
		}
		return iRet;
	}
	
	//根据byte生成最大像素为maxNumOfPixels的图片
	 public static Bitmap makeBitmap(byte[] jpegData, int maxNumOfPixels) {
	        try {
	            BitmapFactory.Options options = new BitmapFactory.Options();
	            options.inJustDecodeBounds = true;
	            BitmapFactory.decodeByteArray(jpegData, 0, jpegData.length,
	                    options);
	            if (options.mCancel || options.outWidth == -1
	                    || options.outHeight == -1) {
	                return null;
	            }
	            options.inSampleSize = computeSampleSize(
	                    options, -1, maxNumOfPixels);
	            options.inJustDecodeBounds = false;

	            options.inDither = false;
	            options.inPreferredConfig = Bitmap.Config.ARGB_8888;
	            return BitmapFactory.decodeByteArray(jpegData, 0, jpegData.length,
	                    options);
	        } catch (OutOfMemoryError ex) {
	            Log.e(TAG, "Got oom exception ", ex);
	            return null;
	        }
	    }
	 
	 public static int computeSampleSize(BitmapFactory.Options options,
	            int minSideLength, int maxNumOfPixels) {
	        int initialSize = computeInitialSampleSize(options, minSideLength,
	                maxNumOfPixels);

	        int roundedSize;
	        if (initialSize <= 8) {
	            roundedSize = 1;
	            while (roundedSize < initialSize) {
	                roundedSize <<= 1;
	            }
	        } else {
	            roundedSize = (initialSize + 7) / 8 * 8;
	        }

	        return roundedSize;
	    }
	 
	 private static int computeInitialSampleSize(BitmapFactory.Options options,
	            int minSideLength, int maxNumOfPixels) {
	        double w = options.outWidth;
	        double h = options.outHeight;

	        int lowerBound = (maxNumOfPixels == UNCONSTRAINED) ? 1 :
	                (int) Math.ceil(Math.sqrt(w * h / maxNumOfPixels));
	        int upperBound = (minSideLength == UNCONSTRAINED) ? 128 :
	                (int) Math.min(Math.floor(w / minSideLength),
	                Math.floor(h / minSideLength));

	        if (upperBound < lowerBound) {
	            // return the larger one when there is no overlapping zone.
	            return lowerBound;
	        }

	        if ((maxNumOfPixels == UNCONSTRAINED) &&
	                (minSideLength == UNCONSTRAINED)) {
	            return 1;
	        } else if (minSideLength == UNCONSTRAINED) {
	            return lowerBound;
	        } else {
	            return upperBound;
	        }
	    }
	
}
