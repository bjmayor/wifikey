/**
 * User.java
 * com.hiwifi.model
 * hiwifiKoala
 * shunping.liu create at 
 */
package com.hiwifi.model;

import java.io.Serializable;
import java.util.ArrayList;

import org.json.JSONException;
import org.json.JSONObject;

import android.text.TextUtils;
import android.text.format.DateFormat;

import com.hiwifi.constant.RequestConstant.RequestTag;
import com.hiwifi.model.log.HWFLog;
import com.hiwifi.model.request.ResponseParserInterface;
import com.hiwifi.model.request.ServerResponseParser;
import com.hiwifi.model.request.ServerResponseParser.ServerCode;
import com.hiwifi.utils.FileUtil;

/**
 * @author shunping.liu@hiwifi.tw
 * 
 */
public class User implements Serializable, ResponseParserInterface {

	private static final long serialVersionUID = -5593366178039937505L;
	private static String TAG = "User";
	private String token = "";// 登录token，标识登录状态
	private long tokenExpiredTime = -1;// token过期时间,单位毫秒，如果过期了，需要重新登录,0为永不过期
	private String uid = null;// 用户id, 唯一标识。
	private String passport = "";// 用户登录名，可以是邮箱，手机号，用户名
	private String username = "";// 用户名
	private String avatarUrl = "";// 用户头像地址
	private String email = "";// 用户邮箱
	private String mobile = "";// 用户手机

	private UserPreference userPreference;// 用户偏好设置
	private static User userInstance;
	private static ArrayList<User> logoutQueue = new ArrayList<User>();

	public synchronized static User shareInstance() {
		if (userInstance == null) {
			userInstance = new User();
			userInstance.load();
		}
		return userInstance;
	}

	private User() {
	}

	private void clearData() {
		this.token = "";
		this.tokenExpiredTime = -1;
		this.uid = null;
		this.username = "";
		this.avatarUrl = "";
		this.email = "";
		this.mobile = "";
	}

	public synchronized final String getUid() {
		return uid;
	}

	public synchronized final String getAvatarUrl() {
		return this.avatarUrl;
	}

	public synchronized final String getUserName() {
		return this.username;
	}

	public synchronized final String getPassport() {
		return this.passport;
	}

	public synchronized final String getMobile() {
		return this.mobile;
	}

	public synchronized final void setPassport(String username) {
		this.passport = username;
		save();
	}

	public synchronized final void setUserName(String username) {
		this.username = username;
		save();
	}

	public synchronized final String getToken() {
		return token;
	}

	private synchronized final void setTokenExpired(long serverProvideExpired) {
		if (serverProvideExpired == 0) {
			this.tokenExpiredTime = System.currentTimeMillis() + 12 * 31 * 24
					* 3600 * 1000;
		} else {
			this.tokenExpiredTime = System.currentTimeMillis()
					+ serverProvideExpired * 1000;
		}
	}

	public UserPreference getPreference() {
		return userPreference;
	}

	public synchronized ArrayList<User> getLogouQueue() {
		return logoutQueue;
	}

	public synchronized void onLogout(Boolean requestSuccess) {
		// 进入登出队列
		if (!requestSuccess && !logoutQueue.contains(this)) {
			logoutQueue.add(this);
		}
		clearData();
		ClientInfo.shareInstance().setCurrentUserId("");
		EventDispatcher.dispatchUserStatusChanged();
	}

	public String toString() {
		return super.toString()
				+ String.format(
						"{uid:%s, passport:%s, token:%s,expiredTime:%s,avatarUrl:%s，username:%s}",
						uid, passport, token, new DateFormat().format(
								"yyyy-MM-dd hh:mm:ss", this.tokenExpiredTime),
						this.avatarUrl, this.username);
	}

	@Override
	public boolean equals(Object o) {
		User otherUser = (User) o;
		return uid.equals(otherUser.uid);
	}

	public void onTokenExpired() {
		clearData();
		EventDispatcher.dispatchUserStatusChanged();
	}

	public boolean hasLogin() {
		return !TextUtils.isEmpty(token) && !isTokenExpired();
	}

	private boolean isTokenExpired() {
		boolean ret = !TextUtils.isEmpty(token) && tokenExpiredTime > 0
				&& System.currentTimeMillis() > tokenExpiredTime;
		if (ret) {
			EventDispatcher.dispatchUserStatusChanged();
		}
		return ret;
	}

	private String getFilePath() {
		String path = ClientInfo.shareInstance().getCurrentUserId()
				+ "userInfo";
		HWFLog.d(TAG, path);
		return path;
	}

	private void save() {
		boolean ret = true;
		try {
			FileUtil.saveObject2File(getFilePath(), this);
		} catch (Exception e) {
			ret = false;
			e.printStackTrace();
		}
		if (ret) {
			HWFLog.d(TAG, "save ok");
		} else {
			HWFLog.d(TAG, "save error");
		}
	}

	private void load() {
		boolean ret = true;
		try {
			User loadUser = (User) FileUtil.readObjectFromFile(getFilePath());
			this.uid = loadUser.uid;
			this.passport = loadUser.passport;
			this.username = loadUser.username;
			this.avatarUrl = loadUser.avatarUrl;
			this.userPreference = loadUser.userPreference;
			this.tokenExpiredTime = loadUser.tokenExpiredTime;
			this.token = loadUser.token;
			this.email = loadUser.email;
			this.mobile = loadUser.mobile;
			HWFLog.d(TAG, loadUser.toString());
		} catch (Exception e) {
			e.printStackTrace();
			ret = false;
		}
		if (ret) {
			HWFLog.d(TAG, "load user  ok");
		} else {
			HWFLog.d(TAG, "load user error");
		}
	}

	/**
	 * 用户偏好信息设置
	 * 
	 * @author shunping.liu@hiwifi.tw
	 * 
	 */
	public class UserPreference implements Serializable {

		private static final long serialVersionUID = 207804567038699640L;

	}

	@Override
	public void parse(RequestTag tag, ServerResponseParser parser) {
		if (parser.code == ServerCode.OK.value()) {
			switch (tag) {
			case URL_USER_INFO_GET:// 获取用户信息
				try {
					JSONObject data = parser.originResponse
							.getJSONObject("profile");
					if (data.getString("uid").equalsIgnoreCase(this.uid)) {
						this.avatarUrl = data.getJSONObject("avatars")
								.getString("b");
						this.username = data.optString("username", "N/A");
						this.email = data.optString("email", "");
						this.mobile = data.optString("mobile", "");
						if (!this.mobile.startsWith("1")) {
							this.mobile = "";
						}
						save();
					}
				} catch (JSONException e) {
					e.printStackTrace();
				}
				break;

			case URL_USER_LOGIN:// 登录
				try {
					clearData();
					String uid = parser.originResponse.getString("uid");
					String token = parser.originResponse.getString("token");
					long tokenExpiredTime = parser.originResponse
							.getLong("expire");
					this.username = parser.originResponse.optString("username",
							"N/A");
					this.uid = uid;
					this.token = token;
					setTokenExpired(tokenExpiredTime);
					EventDispatcher.dispatchUserStatusChanged();
					ClientInfo.shareInstance().setCurrentUserId(
							User.shareInstance().getUid());
					if (logoutQueue.contains(this)) {
						logoutQueue.remove(this);
					}
					save();
				} catch (JSONException e) {
					e.printStackTrace();
				}

				break;
			case URL_USER_REG_BY_PHONE:
			case URL_USER_LOGIN_BY_PHONE:// 注册
				this.username = parser.originResponse.optString("username",
						"N/A");
				try {
					String uid = parser.originResponse.getString("uid");
					String token = parser.originResponse.getString("token");
					long tokenExpiredTime = parser.originResponse
							.getLong("expire");
					this.username = parser.originResponse.optString("username",
							"N/A");
					this.uid = uid;
					this.token = token;
					this.mobile = AppSession.userPhone;
					this.passport = this.mobile;
					setTokenExpired(tokenExpiredTime);
					EventDispatcher.dispatchUserStatusChanged();
					if (logoutQueue.contains(this)) {
						logoutQueue.remove(this);
					}
					save();
				} catch (JSONException e) {
					e.printStackTrace();
				}
				break;
			case URL_USER_NAME_EDIT:// 编辑昵称
				break;
			case URL_USER_INFO_EDIT:// 编辑昵称和密码
				break;
			case URL_USER_PWD_EDIT:
				String token;
				try {
					token = parser.originResponse.getString("token");
					this.token = token;
					long tokenExpiredTime = parser.originResponse
							.getLong("expire");
					setTokenExpired(tokenExpiredTime);
					save();
				} catch (JSONException e1) {
					e1.printStackTrace();
				}

				break;

			case URL_USER_AVATAR_EDIT:// 修改用户头像
				try {
					this.avatarUrl = parser.originResponse.getJSONObject("url")
							.getString("b");
					save();
				} catch (JSONException e) {
					e.printStackTrace();
				}
				break;

			default:
				break;
			}
		}

	}

	// 用户头像切图本地路径
	private String userSculpturePath;

	public synchronized String getUserSculpturePath() {
		return userSculpturePath;
	}

	public synchronized void setUserSculpturePath(String userSculpturePath) {
		this.userSculpturePath = userSculpturePath;
		save();
	}

}
