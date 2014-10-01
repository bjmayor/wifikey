package com.hiwifi.app.views;

import android.content.Context;
import android.graphics.Matrix;

import android.graphics.drawable.Drawable;
import android.util.AttributeSet;
import android.widget.ImageView;

public class TutorialImage extends ImageView {

	private static final float SCALE_ORIGINAL_VALUE = 1;

	// Matrix
	private Matrix mMatrix = new Matrix();

	private float mSaveScale = SCALE_ORIGINAL_VALUE;
	private float mViewWidth;
	private float mViewHeight;
	private float[] mMatrixValues;
	/**
	 * this values is scaled by "setScaleType(ScaleType.MATRIX);"
	 */
	private float mResWidth;
	private float mResHeight;

	public TutorialImage(Context context) {
		super(context);
		initView(context);
	}

	public TutorialImage(Context context, AttributeSet attrs) {
		super(context, attrs);
		initView(context);
	}

	public TutorialImage(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
	}

	private void initView(Context context) {

		// Only when the scale type set used {ScaleType.MATRIX} can zooming
		// by finger
		if (getScaleType() != ScaleType.MATRIX) {
			setScaleType(ScaleType.MATRIX);
		}

		mMatrixValues = new float[9];
	}

	@Override
	protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
		super.onMeasure(widthMeasureSpec, heightMeasureSpec);
		mViewWidth = MeasureSpec.getSize(widthMeasureSpec);
		mViewHeight = MeasureSpec.getSize(heightMeasureSpec);
		setPositionByMeasure();
	}

	private void fixTranslate() {
		mMatrix.getValues(mMatrixValues);
		float transX = mMatrixValues[Matrix.MTRANS_X];
		float transY = mMatrixValues[Matrix.MTRANS_Y];
		float fixTransX = getFixTransOffset(transX, mViewWidth, mResWidth * mSaveScale);
		float fixTransY = getFixTransOffset(transY, mViewHeight, mResHeight * mSaveScale);

		if (fixTransX != 0 || fixTransY != 0)
			mMatrix.postTranslate(fixTransX, fixTransY);
	}

	private float getFixTransOffset(float trans, float viewSize, float contentSize) {
		float minTrans, maxTrans;
		if (contentSize <= viewSize) {
			minTrans = 0;
			maxTrans = viewSize - contentSize;
		} else {
			minTrans = viewSize - contentSize;
			maxTrans = 0;
		}

		if (trans < minTrans)
			return -trans + minTrans;
		if (trans > maxTrans)
			return -trans + maxTrans;
		return 0;
	}

	private void setPositionByMeasure() {

		if (mSaveScale == SCALE_ORIGINAL_VALUE) {
			Drawable drawable = getDrawable();
			if (drawable == null) {
				return;
			} else {
				float drawableW = drawable.getIntrinsicWidth();
				float drawableH = drawable.getIntrinsicHeight();

				float scaleX = (float) mViewWidth / (float) drawableW;

				float scale = scaleX;
				mMatrix.setScale(scale, scale);

				mResWidth = scale * (float) drawableW;
				mResHeight = scale * (float) drawableH;

				// Center the image
				float redundantYSpace = (float) mViewHeight - mResHeight;
				float redundantXSpace = (float) mViewWidth - mResWidth;
				redundantYSpace /= (float) 2;
				redundantXSpace /= (float) 2;

				mMatrix.postTranslate(redundantXSpace, redundantYSpace);
				setImageMatrix(mMatrix);
			}
		}
		fixTranslate();
	}
}
