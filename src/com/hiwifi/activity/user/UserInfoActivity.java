package com.hiwifi.activity.user;

import android.content.ContentResolver;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.ColorDrawable;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.support.v4.app.Fragment;
import android.support.v7.app.ActionBar;
import android.support.v7.app.ActionBarActivity;
import android.text.TextUtils;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.view.ViewGroup.LayoutParams;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.PopupWindow;
import android.widget.PopupWindow.OnDismissListener;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.ViewSwitcher;

import com.hiwifi.activity.FeedbackActivity;
import com.hiwifi.activity.MainTabActivity;
import com.hiwifi.activity.base.BaseActivity;
import com.hiwifi.activity.user.VeryPhoneActivity.Source;
import com.hiwifi.app.utils.RegUtil;
import com.hiwifi.app.views.CancelableEditText;
import com.hiwifi.app.views.CircleImageView;
import com.hiwifi.app.views.UINavigationView;
import com.hiwifi.constant.ConfigConstant;
import com.hiwifi.constant.RequestConstant.RequestTag;
import com.hiwifi.hiwifi.Gl;
import com.hiwifi.hiwifi.R;
import com.hiwifi.model.AppSession;
import com.hiwifi.model.EventDispatcher;
import com.hiwifi.model.User;
import com.hiwifi.model.log.HWFLog;
import com.hiwifi.model.request.RequestFactory;
import com.hiwifi.model.request.RequestManager.ResponseHandler;
import com.hiwifi.model.request.ServerResponseParser;
import com.hiwifi.model.request.ServerResponseParser.ServerCode;
import com.nostra13.universalimageloader.core.DisplayImageOptions;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.display.RoundedBitmapDisplayer;
import com.umeng.analytics.MobclickAgent;

public class UserInfoActivity extends BaseActivity implements ResponseHandler {
	private static InputMethodManager im;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_user_info);

		if (savedInstanceState == null) {
			getSupportFragmentManager().beginTransaction()
					.add(R.id.container, new PlaceholderFragment()).commit();
		}
		ActionBar actionBar = getSupportActionBar();
		if (actionBar != null) {
			actionBar.hide();
		}
		im = (InputMethodManager) getSystemService("input_method");
	}

	@Override
	public void onResume() {
		super.onResume();
		RequestFactory.getUserInfo(this, this);
	}

	@Override
	public void onBackPressed() {
		Intent intent = new Intent();
		intent.setClass(this, MainTabActivity.class);
		intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
		startActivity(intent);
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {

		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.user_info, menu);
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		// Handle action bar item clicks here. The action bar will
		// automatically handle clicks on the Home/Up button, so long
		// as you specify a parent activity in AndroidManifest.xml.
		int id = item.getItemId();
		switch (id) {
		case R.id.action_settings_logout:
			RequestFactory.logout(this, this);
			return true;
		case android.R.id.home:
			finish();
			return true;
		default:
			break;
		}
		return super.onOptionsItemSelected(item);
	}

	/**
	 * A placeholder fragment containing a simple view.
	 */
	public static class PlaceholderFragment extends Fragment implements
			OnClickListener, ResponseHandler {

		UINavigationView navigationView;
		EditText mobileEditText;
		CancelableEditText usernickEditText;
		CancelableEditText passwordEditText;
		CircleImageView photoImageView;
		Button saveButton;
		ViewSwitcher viewSwitcher;
		TextView resetPasswordTextView;
		TextView errorTextView;
		View rootView;

		public static PlaceholderFragment instance;

		public PlaceholderFragment() {
			instance = this;
		}

		@Override
		public View onCreateView(LayoutInflater inflater, ViewGroup container,
				Bundle savedInstanceState) {
			rootView = inflater.inflate(R.layout.fragment_user_info, container,
					false);

			navigationView = (UINavigationView) rootView.findViewById(R.id.nav);
			navigationView.getLeftButton().setOnClickListener(this);
			navigationView.getRightButton().setOnClickListener(this);
			mobileEditText = (EditText) rootView
					.findViewById(R.id.et_user_phone);
			usernickEditText = (CancelableEditText) rootView
					.findViewById(R.id.et_user_nick);
			passwordEditText = (CancelableEditText) rootView
					.findViewById(R.id.et_user_password);
			errorTextView = (TextView) rootView.findViewById(R.id.tv_error_tip);
			photoImageView = (CircleImageView) rootView
					.findViewById(R.id.iv_user_photo);
			LayoutParams layou = photoImageView.getLayoutParams();
			layou.width = 200;
			layou.height = 200;
			viewSwitcher = (ViewSwitcher) rootView
					.findViewById(R.id.vs_password);
			resetPasswordTextView = (TextView) rootView
					.findViewById(R.id.tv_reset_password);
			resetPasswordTextView.setOnClickListener(this);
			saveButton = (Button) rootView.findViewById(R.id.btn_user_save);
			saveButton.setOnClickListener(this);
			shadow = (RelativeLayout) rootView.findViewById(R.id.shadow);
			photoImageView.setOnClickListener(this);
			return rootView;
		}

		@Override
		public void onResume() {
			super.onResume();
			updateView();
			if (TextUtils.isEmpty(User.shareInstance().getAvatarUrl())) {
				RequestFactory.getUserInfo(getActivity(), this);
			} else {
				ImageLoader.getInstance().displayImage(
						User.shareInstance().getAvatarUrl(),
						photoImageView,
						new DisplayImageOptions.Builder().cacheInMemory()
								.cacheOnDisc()
								.displayer(new RoundedBitmapDisplayer(90))
								.bitmapConfig(Bitmap.Config.RGB_565).build());
			}
		}

		public void updateView() {
			if (!TextUtils.isEmpty(User.shareInstance().getMobile())) {
				this.mobileEditText.setText(User.shareInstance().getMobile()
						.replaceFirst("(...)(....)(....)", "$1****$3"));
				this.mobileEditText.setVisibility(View.VISIBLE);
			} else {
				this.mobileEditText.setVisibility(View.GONE);
			}
			if (!TextUtils.isEmpty(User.shareInstance().getAvatarUrl())) {
				ImageLoader.getInstance().displayImage(
						User.shareInstance().getAvatarUrl(), photoImageView);
			}
			this.usernickEditText.setText(User.shareInstance().getUserName());
			if (AppSession.source == Source.SourceIsREG) {
				viewSwitcher.setDisplayedChild(1);
			} else {
				viewSwitcher.setDisplayedChild(0);
			}
		}

		private PopupWindow popupWindow;

		private void hideInput() {
			if (im != null && im.isActive()) {
				im.hideSoftInputFromWindow(getActivity().getCurrentFocus()
						.getWindowToken(), 0);
			}
		}

		public void showChoicePopup() {
			// TODO
			hideInput();
			int displayWidth = getActivity().getWindowManager()
					.getDefaultDisplay().getWidth();
			int displayHeight = getActivity().getWindowManager()
					.getDefaultDisplay().getHeight();
			View view = LayoutInflater.from(getActivity()).inflate(
					R.layout.pic_choose_pop, null);
			view.findViewById(R.id.popup_cancle).setOnClickListener(this);
			view.findViewById(R.id.popup_photo).setOnClickListener(this);
			view.findViewById(R.id.popup_tackpic).setOnClickListener(this);
			popupWindow = new PopupWindow(getActivity());
			popupWindow.setContentView(view);
			popupWindow.setWidth(displayWidth);
			popupWindow.setHeight(LayoutParams.WRAP_CONTENT);
			ColorDrawable dw = new ColorDrawable(0xb0000000);
			popupWindow.setBackgroundDrawable(dw);
			popupWindow.setOutsideTouchable(false);
			popupWindow.setAnimationStyle(R.style.PopupWindowAnimation2);
			popupWindow.setFocusable(true);
			popupWindow.setOnDismissListener(new OnDismissListener() {

				@Override
				public void onDismiss() {
					shadow.setVisibility(View.INVISIBLE);
				}
			});
			popupWindow.showAtLocation(rootView, Gravity.BOTTOM
					| Gravity.CENTER_HORIZONTAL, 0, 0); //
			// 设置layout在PopupWindow中显示的位置
			shadow.setVisibility(View.VISIBLE);
			// popupWindow.showAsDropDown(rootView);

		}

		private final int PHOTO_PICKED_WITH_DATA = 1002;
		private final int ReQuestCode_CUT_PIC = 111;

		private void getPhotograph() {
			Intent openAlbumIntent = new Intent(Intent.ACTION_PICK);
			openAlbumIntent.setDataAndType(
					MediaStore.Images.Media.EXTERNAL_CONTENT_URI, "image/*");
			getActivity().startActivityForResult(openAlbumIntent,
					PHOTO_PICKED_WITH_DATA);
		}

		// private Uri cameraImgUri = Uri.parse(IMAGE_FILE_LOCATION);
		private Uri cameraImgUri = Uri.parse(ConfigConstant.getCameraFileName());
		private final int CAMERA_WITH_DATA = 1001;

		private void takePic() {
			// TODO Auto-generated method stub
			if (Environment.getExternalStorageState().equals(
					Environment.MEDIA_MOUNTED)) {
				Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
				intent.putExtra(MediaStore.EXTRA_OUTPUT, cameraImgUri);
				getActivity().startActivityForResult(intent, CAMERA_WITH_DATA);
			} else {
				Toast.makeText(getActivity(), "没有SD卡！请选择本地图片",
						Toast.LENGTH_LONG).show();
			}
		}

		@Override
		public void onActivityResult(int requestCode, int resultCode,
				Intent data) {
			super.onActivityResult(requestCode, resultCode, data);
			HWFLog.e("afdsfsafds", "on ac");
			ContentResolver resolver = getActivity().getContentResolver();
			switch (requestCode) {
			case PHOTO_PICKED_WITH_DATA:
				try {
					Uri originalUri = data.getData();
					Intent intent = new Intent(getActivity(),
							CutPicActivity.class);
					intent.putExtra("uri", originalUri);
					intent.putExtra("picSource", "photo");
					getActivity().startActivityForResult(intent, ReQuestCode_CUT_PIC);
				}

				catch (Exception e) {
					e.printStackTrace();
				}
				break;
			case ReQuestCode_CUT_PIC:
				// Bitmap makeRoundCorner =
				// ImageUtil.makeRoundCorner(Gl.getTest());
				BitmapDrawable drawable = new BitmapDrawable(Gl.getTest());
				photoImageView.setBackgroundDrawable(drawable);
				break;

			case CAMERA_WITH_DATA:

				// 将图片内容解析成字节数组
				try {
					Intent intent = new Intent(getActivity(),
							CutPicActivity.class);
					intent.putExtra("picSource", "camera");
					getActivity().startActivityForResult(intent, ReQuestCode_CUT_PIC);
				}

				catch (Exception e) {
					e.printStackTrace();
				}

			default:
				break;
			}
		}

		private byte[] mContent;
		private RelativeLayout shadow;

		public void showError(String msg) {
			errorTextView.setText(msg);
		}

		@Override
		public void onClick(View v) {
			if (v == navigationView.getLeftButton()) {
				Intent intent = new Intent();
				intent.setClass(getActivity(), MainTabActivity.class);
				intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
				getActivity().startActivity(intent);
			} else if (v == navigationView.getRightButton()) {
				User.shareInstance().onLogout(true);
				EventDispatcher.dispatchUserStatusChanged();
				getActivity().finish();
			} else {
				switch (v.getId()) {
				case R.id.iv_user_photo:
					// TODO
					showChoicePopup();
					break;
				case R.id.popup_tackpic:
					if (popupWindow != null && popupWindow.isShowing()) {
						popupWindow.dismiss();
					}
					takePic();

					break;
				case R.id.popup_cancle:
					if (popupWindow != null && popupWindow.isShowing()) {
						popupWindow.dismiss();
					}
					break;
				case R.id.tv_reset_password:
					MobclickAgent.onEvent(getActivity(),
							"click_reset_password",
							getString(R.string.click_reset_password));
					Intent intent = new Intent();
					intent.setClass(getActivity(), ResetPasswordActivity.class);
					getActivity().startActivity(intent);
					break;

				case R.id.popup_photo:
					MobclickAgent.onEvent(getActivity(), "click_user_photo",
							getString(R.string.click_user_photo));
					if (popupWindow != null && popupWindow.isShowing()) {
						popupWindow.dismiss();
					}
					getPhotograph();

					break;
				case R.id.btn_user_save:
					MobclickAgent.onEvent(getActivity(), "click_info_save",
							getString(R.string.click_info_save));
					if (!RegUtil.isUserNameValid(usernickEditText.getText()
							.toString())) {
						showError("用户名需4-20位字符 (中文占2字符)");
						return;
					}

					if (AppSession.source == Source.SourceIsREG) {
						if (!RegUtil.isUserPasswordValid(passwordEditText
								.getText().toString())) {
							showError("密码需6-20字符");
							return;
						}
					}

					if (TextUtils
							.isEmpty(passwordEditText.getText().toString())
							&& usernickEditText
									.getText()
									.toString()
									.equalsIgnoreCase(
											User.shareInstance().getUserName())) {
						Toast.makeText(getActivity(), "保存成功",
								Toast.LENGTH_SHORT).show();
						return;
					}
					if (AppSession.source == Source.SourceIsREG) {
						RequestFactory.modifyUserInfo(getActivity(), this,
								usernickEditText.getText().toString(),
								passwordEditText.getText().toString());
					} else {
						RequestFactory.modifyUserInfo(getActivity(), this,
								usernickEditText.getText().toString());
					}

					break;
				default:
					break;
				}
			}
		}

		@Override
		public void onStart(RequestTag tag, Code code) {
			switch (tag) {
			case URL_USER_NAME_EDIT:
				showError("正在修改");
				break;

			default:
				break;
			}
		}

		@Override
		public void onSuccess(RequestTag tag,
				ServerResponseParser responseParser) {
			if (responseParser.code == ServerCode.OK.value()) {
				switch (tag) {
				case URL_USER_INFO_GET:
					User.shareInstance().parse(tag, responseParser);
					updateView();
					break;
				case URL_USER_NAME_EDIT:
					if (responseParser.code == ServerCode.OK.value()) {
						User.shareInstance().parse(tag, responseParser);
						showError("修改成功");
						User.shareInstance().setUserName(
								tag.getParams().get("newusername"));
					} else {
						showError(responseParser.message);
					}
					break;
				case URL_USER_INFO_EDIT:
					User.shareInstance().parse(tag, responseParser);
					showError("修改成功");
					User.shareInstance().setUserName(
							tag.getParams().get("newusername"));
					break;
				default:
					break;
				}
			} else {
				showError(responseParser.message);
			}

		}

		@Override
		public void onFailure(RequestTag tag, Throwable error) {
			// TODO Auto-generated method stub

		}

		@Override
		public void onFinish(RequestTag tag) {
			// TODO Auto-generated method stub

		}
	}

	@Override
	public void onStart(RequestTag tag, Code code) {
		// TODO Auto-generated method stub

	}

	@Override
	public void onSuccess(RequestTag tag, ServerResponseParser responseParser) {
		switch (tag) {
		case API_OPEN_LOGOUT:
			User.shareInstance().onLogout(false);
			finish();
			break;

		default:
			break;
		}

	}

	@Override
	public void onFailure(RequestTag tag, Throwable error) {

	}

	@Override
	public void onFinish(RequestTag tag) {

	}

	private void hideInput() {
		if (im != null && im.isActive()) {
			im.hideSoftInputFromWindow(this.getCurrentFocus().getWindowToken(),
					0);
		}
	}

	@Override
	public boolean needRedirectToLoginPage() {
		return true;
	}

	@Override
	protected void onClickEvent(View paramView) {

	}

	@Override
	protected void findViewById() {

	}

	@Override
	protected void loadViewLayout() {

	}

	@Override
	protected void processLogic() {

	}

	@Override
	protected void setListener() {

	}

	@Override
	protected void updateView() {

	}

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		super.onActivityResult(requestCode, resultCode, data);
		PlaceholderFragment.instance.onActivityResult(requestCode, resultCode,
				data);
	}

}
