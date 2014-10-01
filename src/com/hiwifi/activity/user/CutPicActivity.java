package com.hiwifi.activity.user;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;

import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Bitmap.CompressFormat;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.Paint.Style;
import android.graphics.PointF;
import android.net.Uri;
import android.os.Environment;
import android.util.FloatMath;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnTouchListener;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.hiwifi.activity.base.BaseActivity;
import com.hiwifi.app.views.UINavigationView;
import com.hiwifi.constant.ConfigConstant;
import com.hiwifi.constant.RequestConstant.RequestTag;
import com.hiwifi.hiwifi.Gl;
import com.hiwifi.hiwifi.R;
import com.hiwifi.model.User;
import com.hiwifi.model.log.LogUtil;
import com.hiwifi.model.request.RequestFactory;
import com.hiwifi.model.request.RequestManager.ResponseHandler;
import com.hiwifi.model.request.ServerResponseParser;
import com.hiwifi.model.request.ServerResponseParser.ServerCode;
import com.hiwifi.utils.ViewUtil;

public class CutPicActivity extends BaseActivity {

	private ImageView imageView;
	private ImageView image2;
	private LinearLayout cropLayout;
	private static int height;
	private static int width;
	private static int line;
	private Matrix matrix = new Matrix();
	private static float startX, startY;
	private float zoomDegree = 1;
	private static Bitmap img;
	private View myView;
	private Uri cameraImgUri = Uri.parse(ConfigConstant.getCameraFileName());
	private byte[] mContent;

	@Override
	protected void onDestroy() {
		if (img != null && !img.isRecycled()) {
			img.recycle();
		}
		mContent = null;
		super.onDestroy();
	}

	public Bitmap getPicFromBytes(byte[] bytes, BitmapFactory.Options opts) {
		try {
			if (bytes != null) {
				if (opts != null) {
					return BitmapFactory.decodeByteArray(bytes, 0,
							bytes.length, opts);
				} else {
					return BitmapFactory
							.decodeByteArray(bytes, 0, bytes.length);
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
			finish();
		}
		return null;
	}

	public byte[] readStream(InputStream inStream) throws Exception {
		byte[] data = null;
		try {
			byte[] buffer = new byte[1024];
			int len = -1;
			ByteArrayOutputStream outStream = new ByteArrayOutputStream();
			while ((len = inStream.read(buffer)) != -1) {
				outStream.write(buffer, 0, len);
			}
			data = outStream.toByteArray();
			outStream.close();
			inStream.close();

		} catch (Exception e) {
			e.printStackTrace();
		}
		return data;
	}

	private void initView() {
		imageView = (ImageView) findViewById(R.id.becutted_img);
		imageView.setOnTouchListener(new TounchListener());
		image2 = (ImageView) findViewById(R.id.becutted_img);
		cropLayout = (LinearLayout) findViewById(R.id.cropLayout);
		bar = (ProgressBar) findViewById(R.id.progress_bar);
		navigationView = (UINavigationView) findViewById(R.id.nav);

		myView = new MyView(this);
		cropLayout.addView(myView);

	}

	/**
	 * 监听缩放、平移
	 * 
	 * @author lcy
	 * 
	 */
	private class TounchListener implements OnTouchListener {

		private PointF startPoint = new PointF();

		private Matrix currentMaritx = new Matrix();

		private int mode = 0;// 用于标记模式
		private static final int DRAG = 1;// 拖动
		private static final int ZOOM = 2;// 放大
		private float startDis = 0;
		private PointF midPoint;// 中心点

		public boolean onTouch(View v, MotionEvent event) {
			switch (event.getAction() & MotionEvent.ACTION_MASK) {
			case MotionEvent.ACTION_DOWN:
				mode = DRAG;
				currentMaritx.set(imageView.getImageMatrix());// 记录ImageView当期的移动位置
				startPoint.set(event.getX(), event.getY());// 开始点
				break;

			case MotionEvent.ACTION_MOVE:// 移动事件
				if (mode == DRAG) {// 图片拖动事件
					float dx = event.getX() - startPoint.x;// x轴移动距离
					float dy = event.getY() - startPoint.y;

					int x = (int) (dx / 50);
					int y = (int) (dy / 50);

					int[] start = new int[2];
					imageView.getLocationInWindow(start);
					matrix.set(currentMaritx);// 在当前的位置基础上移动
					matrix.postTranslate(dx, dy);
					// }
				} else if (mode == ZOOM) {// 图片放大事件
					float endDis = distance(event);// 结束距离
					if (endDis > 10f) {
						float scale = endDis / startDis;// 放大倍数
						matrix.set(currentMaritx);
						matrix.postScale(scale, scale, midPoint.x, midPoint.y);
					}

				}

				break;

			case MotionEvent.ACTION_UP:
				float dx = event.getX() - startPoint.x;// x轴移动距离
				float dy = event.getY() - startPoint.y;
				// startX -= dx / zoomDegree;
				// startY -= dy / zoomDegree;
				mode = 0;
				break;
			// 有手指离开屏幕，但屏幕还有触点(手指)
			case MotionEvent.ACTION_POINTER_UP:
				float[] f = new float[9];
				matrix.getValues(f);
				zoomDegree = f[0];
				mode = 0;
				break;
			// 当屏幕上已经有触点（手指）,再有一个手指压下屏幕
			case MotionEvent.ACTION_POINTER_DOWN:
				mode = ZOOM;
				startDis = distance(event);

				if (startDis > 10f) {// 避免手指上有两个茧
					midPoint = mid(event);
					currentMaritx.set(imageView.getImageMatrix());// 记录当前的缩放倍数
				}

				break;

			}
			imageView.setImageMatrix(matrix);
			return true;
		}
	}

	/**
	 * 两点之间的距离
	 * 
	 * @param event
	 * @return
	 */
	private static float distance(MotionEvent event) {
		// 两根线的距离
		float dx = event.getX(1) - event.getX(0);
		float dy = event.getY(1) - event.getY(0);
		return FloatMath.sqrt(dx * dx + dy * dy);
	}

	/**
	 * 获得屏幕中心点
	 * 
	 * @param event
	 * @return
	 */
	private static PointF mid(MotionEvent event) {
		return new PointF(width / 2, height / 2);
	}

	private class MyView extends View {

		public MyView(Context context) {
			super(context);
		}

		@Override
		protected void onDraw(Canvas canvas) {
			// TODO Auto-generated method stub
			super.onDraw(canvas);
			height = getHeight();
			width = getWidth();
			line = Math.min(ViewUtil.getScreenWidth(),
					ViewUtil.getScreenWidth()) / 2;// width / 2;
			if (line < 200) {
				line = 200;
			}
			startX = width / 2 - line / 2;
			startY = height / 2 - line / 2;

			Paint p = new Paint();
			p.setColor(Color.RED);
			p.setStyle(Style.STROKE);
			p.setStrokeWidth(2);
			canvas.drawRect(width / 2 - line / 2, height / 2 - line / 2, width
					/ 2 + line / 2, height / 2 + line / 2, p);
			Paint p2 = new Paint();
			p2.setColor(Color.BLACK);
			p2.setAlpha(100);
			canvas.drawRect(0, 0, width / 2 - line / 2, height, p2);
			canvas.drawRect(width / 2 + line / 2, 0, width, height, p2);
			canvas.drawRect(width / 2 - line / 2, height / 2 + line / 2, width
					/ 2 + line / 2, height, p2);
			canvas.drawRect(width / 2 - line / 2, 0, width / 2 + line / 2,
					height / 2 - line / 2, p2);

			int h = img.getHeight();
			int w = img.getWidth();
			float zoom1 = (float) height / (float) h;
			float zoom2 = (float) width / (float) w;
			if (zoom1 < 1 || zoom2 < 1) {
				Matrix m = new Matrix();
				float size = zoom1 > zoom2 ? zoom1 : zoom2;
				m.setScale(size, size);

				img = Bitmap.createBitmap(img, 0, 0, img.getWidth(),
						img.getHeight(), m, true);
			}
			imageView.setImageBitmap(img);

		}

	}

	private void buildAndsavePic() {
		Bitmap bmp = img;
		imageView.setDrawingCacheEnabled(false);
		float[] f = new float[9];
		matrix.getValues(f);
		startX = (-f[2] / zoomDegree + ((float) (width - line) / 2)
				/ zoomDegree);
		startY = (-f[5] / zoomDegree + ((float) (height - line) / 2)
				/ zoomDegree);
		try {
			Bitmap bitmap = Bitmap.createBitmap(bmp, (int) startX,
					(int) startY, (int) (line / zoomDegree),
					(int) (line / zoomDegree), matrix, true);
			// TODO 上传图片 并且缓存图片 username_time.jpg;
			sendUserPhotoServer(bitmap);
			// image2.setImageBitmap(bitmap);
			// imageView.setVisibility(View.GONE);
			// image2.setVisibility(View.VISIBLE);
		} catch (IllegalArgumentException e) {
			Toast.makeText(this, "请正确选择图片区域", Toast.LENGTH_LONG).show();
		}
	}

	public String IMAGE_PATH = "";
	private String picFrom;
	private Uri originalUri;
	private UINavigationView navigationView;
	private ProgressBar bar;

	// TODO 缓存头像图片
	private void storeBitmap(Bitmap bitmap) {
		// try {
		if (Environment.MEDIA_MOUNTED.equals(Environment
				.getExternalStorageState())
				&& Environment.getExternalStorageDirectory().exists()) {
			IMAGE_PATH = Environment.getExternalStorageDirectory()
					.getAbsolutePath() + "cut.jpg";
		} else {
			IMAGE_PATH = getFilesDir().getAbsolutePath() + "cut.jpg";
		}
		File file = new File(IMAGE_PATH);
		if (file.exists()) {
			file.delete();
		}
		try {
			file.createNewFile();
			FileOutputStream fos = new FileOutputStream(file);
			bitmap.compress(CompressFormat.JPEG, 100, fos);
			fos.flush();
			fos.close();
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			System.out.println("-=----------");
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			System.out.println("-=----------111");
			e.printStackTrace();
		}

		// } catch (Throwable t) {
		// System.out.println("-=----------");
		// t.printStackTrace();
		// IMAGE_PATH = null;
		// }
	}

	private void sendUserPhotoServer(final Bitmap bitmap) {
		RequestFactory.uploadUserPhoto(this, new ResponseHandler() {

			@Override
			public void onStart(RequestTag tag, Code code) {
				showMyDialog("正在上传");
			}

			@Override
			public void onSuccess(RequestTag tag,
					ServerResponseParser responseParser) {
				LogUtil.d("hehe", responseParser.toString());
				if (responseParser.code == ServerCode.OK.value()) {
					User.shareInstance().parse(tag, responseParser);
					Gl.setTest(bitmap);
					Toast.makeText(CutPicActivity.this, "上传成功", 0).show();
					finish();
				} else {
					Toast.makeText(CutPicActivity.this, responseParser.message,
							0).show();
				}
			}

			@Override
			public void onFailure(RequestTag tag, Throwable error) {

			}

			@Override
			public void onFinish(RequestTag tag) {
				closeMyDialog();
			}

		}, bitmap);
	}

	@Override
	protected void onClickEvent(View paramView) {

	}

	@Override
	protected void findViewById() {
		ContentResolver resolver = getContentResolver();
		Intent intent = getIntent();
		picFrom = intent.getStringExtra("picSource");
		if ("camera".equals(picFrom)) {
			originalUri = cameraImgUri;
		} else if ("photo".equals(picFrom)) {
			originalUri = intent.getParcelableExtra("uri");
		}
		try {
			BitmapFactory.Options bmpFactoryOptions = new BitmapFactory.Options();
			bmpFactoryOptions.inJustDecodeBounds = true;
			BitmapFactory.decodeStream(
					getContentResolver().openInputStream(originalUri), null,
					bmpFactoryOptions);

			double visiableHeight = screenHeight
					- ViewUtil.dip2px(Gl.Ct(),
							getResources().getDimension(R.dimen.nav_height));
			double heightRatio = (double) Math.max(bmpFactoryOptions.outHeight,
					bmpFactoryOptions.outWidth)
					/ Math.max(visiableHeight, screenWidth);
			double widthRatio = (double) Math.min(bmpFactoryOptions.outHeight,
					bmpFactoryOptions.outWidth)
					/ Math.min(visiableHeight, screenWidth);
			if (heightRatio > 1.5 || widthRatio > 1.5) {
				bmpFactoryOptions.inSampleSize = (int) Math.round(Math.max(
						heightRatio, widthRatio));
			}
			bmpFactoryOptions.inJustDecodeBounds = false;
			img = BitmapFactory.decodeStream(getContentResolver()
					.openInputStream(originalUri), null, bmpFactoryOptions);
			// mContent = readStream(resolver.openInputStream(Uri
			// .parse(originalUri.toString())));
		} catch (Exception e) {
			finish();
		}
		// img = getPicFromBytes(mContent, null);
		initView();

	}

	@Override
	protected void loadViewLayout() {
		setContentView(R.layout.activity_cut_photo);
	}

	@Override
	protected void processLogic() {
		// TODO Auto-generated method stub

	}

	@Override
	protected void setListener() {
		navigationView.getRightButton().setOnClickListener(
				new OnClickListener() {

					@Override
					public void onClick(View v) {
						buildAndsavePic();
					}
				});
		navigationView.getLeftButton().setOnClickListener(
				new OnClickListener() {

					@Override
					public void onClick(View v) {
						finish();
					}
				});

	}

	@Override
	protected void updateView() {

	}

	@Override
	public boolean needRedirectToLoginPage() {
		return true;
	}

}
