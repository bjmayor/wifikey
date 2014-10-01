/**
 * RequestManager.java
 * com.hiwifi.model
 * hiwifiKoala
 * shunping.liu create at 20142014年8月5日上午10:42:20
 */
package com.hiwifi.model.request;

import java.io.UnsupportedEncodingException;
import java.util.HashMap;
import java.util.Map;

import org.apache.http.Header;
import org.apache.http.HttpEntity;
import org.apache.http.HttpStatus;
import org.apache.http.entity.StringEntity;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.content.Context;
import android.os.Looper;

import com.hiwifi.constant.ReleaseConstant;
import com.hiwifi.constant.RequestConstant;
import com.hiwifi.constant.RequestConstant.RequestTag;
import com.hiwifi.hiwifi.Gl;
import com.hiwifi.model.ClientInfo;
import com.hiwifi.model.User;
import com.hiwifi.model.log.HWFLog;
import com.hiwifi.model.log.LogUtil;
import com.hiwifi.model.request.RequestManager.ResponseHandler.Code;
import com.hiwifi.model.router.RouterManager;
import com.hiwifi.support.http.AsyncHttpClient;
import com.hiwifi.support.http.JsonHttpResponseHandler;
import com.hiwifi.support.http.PersistentCookieStore;
import com.hiwifi.support.http.RequestParams;
import com.hiwifi.support.http.SyncHttpClient;
import com.hiwifi.utils.NetWorkConnectivity;
import com.hiwifi.utils.encode.MD5Util;

/**
 * @author shunping.liu@hiwifi.tw
 * 
 */
public class RequestManager {
	public interface ResponseHandler {
		public enum Code {
			ok(0, "OK"), errorNoNetwork(1, "网络异常"), errorNotLoginOrExpired(2,
					"未登录，请先登录"), errorNoRouter(3, "没有绑定路由器，请先绑定"), errorFileNotExists(
					4, "文件不存在"), errorUnkown(999, "未知错误");
			private int code;
			private String msg;

			private Code(int code, String msg) {
				this.code = code;
				this.msg = msg;
			}

			public String getMsg() {
				return this.msg;
			}

			public int getCode() {
				return this.code;
			}
		}

		public void onStart(RequestTag tag, Code code);

		public void onSuccess(RequestTag tag,
				ServerResponseParser responseParser);

		public void onFailure(RequestTag tag, Throwable error);

		public void onFinish(RequestTag tag);
	}

	private static final String TAG = "RequestManager";
	public static final String key_wrap = "clientwrap";
	public static final String key_code = "code";
	public static final String key_msg = "msg";
	public static final String key_json = "data";

	private static AsyncHttpClient asyncHttpClient = null;
	private static SyncHttpClient synchttpClient = null;

	private static AsyncHttpClient getHttpClient() {
		ClientModel model = clientPool.get(Thread.currentThread().getId() + "");
		if (model != null) {
			return model.getHttpClient();
		} else {
			if (asyncHttpClient == null) {
				asyncHttpClient = new AsyncHttpClient();
				asyncHttpClient.setCookieStore(new PersistentCookieStore(Gl
						.Ct()));
			}
			return asyncHttpClient;
		}
	}

	private static boolean isMainThread() {
		return Thread.currentThread().getId() == Looper.getMainLooper()
				.getThread().getId();
	}

	private static class ClientModel {
		private boolean isAsync = true;

		public void setSyncModel(boolean isSync) {
			isAsync = !isSync;
		}

		private AsyncHttpClient getHttpClient() {
			if (isAsync || isMainThread()) {
				if (asyncHttpClient == null) {
					asyncHttpClient = new AsyncHttpClient();
					asyncHttpClient.setCookieStore(new PersistentCookieStore(Gl
							.Ct()));
				}
				return asyncHttpClient;
			} else {
				if (synchttpClient == null) {
					synchttpClient = new SyncHttpClient();
					synchttpClient.setCookieStore(new PersistentCookieStore(Gl
							.Ct()));
				}
				isAsync = true;
				return synchttpClient;
			}

		}
	}

	public static HashMap<String, ClientModel> clientPool = new HashMap<String, RequestManager.ClientModel>();
	static {
		clientPool.put(Looper.getMainLooper().getThread().getId() + "",
				new ClientModel());
	}

	// 设置为同步模式
	public static void setSyncModel(boolean isSync) {
		ClientModel model = clientPool.get(Thread.currentThread().getId() + "");
		if (model == null) {
			model = new ClientModel();
			clientPool.put(Thread.currentThread().getId() + "", model);
		}
		model.setSyncModel(isSync);
	}

	/**
	 * @description 请求的唯一入口，不可在其它地方使用async等框架发请求
	 * @param tag
	 * @param params
	 * @param context
	 * @return
	 */
	public static boolean requestByTag(Context context, RequestTag tag,
			RequestParams params, final ResponseHandler responseHandler) {
		if (!canRequest(tag, params, responseHandler)) {
			return false;
		} else {
			if (params == null) {
				params = new RequestParams();
			}
			if (tag.getType() == RequestConstant.TAG_TYPE_TWX
					|| tag.getType() == RequestConstant.TAG_TYPE_M) {
				params.put("token", User.shareInstance().getToken());
				if (RouterManager.shareInstance().currentRouter() != null) {
					params.put("rid", RouterManager.shareInstance()
							.currentRouter().getRouterId()
							+ "");
				}
			} else if (tag.getType() == RequestConstant.TAG_TYPE_TWX_PATH) {
				params.put("token", User.shareInstance().getToken());
				params.put("rid", RouterManager.shareInstance().currentRouter()
						.getRouterId()
						+ "");
				params.put("path", tag.value());
			} else if (tag.getType() == RequestConstant.TAG_TYPE_OPEN_CLIENT) {
				params.put("token", User.shareInstance().getToken());
				params.put("rid", RouterManager.shareInstance().currentRouter()
						.getRouterId()
						+ "");
			} else if (tag.getType() == RequestConstant.TAG_TYPE_USER) {
				if (User.shareInstance().hasLogin()) {
					params.put("token", User.shareInstance().getToken());
				}
			}
			tag.setParams(params);
			if (ReleaseConstant.ISDEBUG) {
				HWFLog.e(TAG, params.toString());
			}
			if (tag.getMethod().equals(RequestConstant.GET)) {
				doGet(tag, params, responseHandler);
			} else if (tag.getMethod().equals(RequestConstant.POST)) {
				if (tag.getType() == RequestConstant.TAG_TYPE_OPEN_CLIENT) {
					doPostOpenApi(context, tag, params, responseHandler);
				} else {
					doPost(tag, params, responseHandler);
				}
			} else if (tag.getMethod().equals(RequestConstant.JSON)) {
				try {
					doPost(context, tag,
							new StringEntity(params.get(key_json)),
							RequestConstant.ContentType.JSON.toString(),
							responseHandler);
				} catch (UnsupportedEncodingException e) {
					responseHandler.onStart(tag, Code.errorUnkown);
					return false;
				}
			} else if (tag.getMethod().equals(RequestConstant.BINARY)) {
				doPost(tag, params, responseHandler);
			}
			return true;
		}

	}

	private static String buildSign(RequestTag tag, JSONObject jsonBody) {
		return md5(tag.value()
				+ jsonBody.toString()
				+ RouterManager.shareInstance().currentRouter()
						.getClientSecret());
	}

	private static JSONObject buildOpenApiHttpBody(String method,
			RequestParams data) {
		try {
			JSONObject jsonbody = new JSONObject();
			JSONObject subJson = new JSONObject();
			jsonbody.put("app_id", RequestConstant.APP_ID);
			jsonbody.put("dev_id", RouterManager.shareInstance()
					.currentRouter().getMac());
			jsonbody.put("client_id", ClientInfo.shareInstance().getClientId());
			jsonbody.put("method", method);
			jsonbody.put("timeout", "30");

			if (data != null) {
				for (Map.Entry<String, String> o : data.getUrlParams()
						.entrySet()) {
					subJson.put(o.getKey(), o.getValue());
				}
			}
			jsonbody.put("data", subJson);
			LogUtil.d("body:", jsonbody.toString());
			return jsonbody;
		} catch (JSONException e) {
			e.printStackTrace();
		}
		return null;
	}

	private static String md5(String in) {
		return MD5Util.encryptToMD5(in);
	}

	private static void doGet(final RequestTag tag, RequestParams params,
			final ResponseHandler responseHandler) {
		getHttpClient().get(RequestConstant.getUrl(tag), params,
				new JsonHttpResponseHandler() {

					@Override
					public void onStart() {
						super.onStart();
						tag.setURI(getRequestURI());
						responseHandler.onStart(tag, ResponseHandler.Code.ok);
					}

					@Override
					public void onSuccess(int statusCode, Header[] headers,
							JSONObject response) {
						super.onSuccess(statusCode, headers, response);
						if (statusCode == HttpStatus.SC_OK) {
							responseHandler.onSuccess(tag,
									new ServerResponseParser(response));
						}
					}

					public void onSuccess(int statusCode, Header[] headers,
							JSONArray response) {
						super.onSuccess(statusCode, headers, response);
						if (statusCode == HttpStatus.SC_OK) {
							JSONObject object = new JSONObject();
							try {
								object.put(key_wrap, response);
								object.put(key_code, 0);
								object.put(key_msg, "OK");
							} catch (JSONException e) {
								e.printStackTrace();
							}
							responseHandler.onSuccess(tag,
									new ServerResponseParser(object));
						}
					}

					@Override
					public void onFailure(int statusCode, Header[] headers,
							String responseString, Throwable throwable) {
						super.onFailure(statusCode, headers, responseString,
								throwable);
						responseHandler.onFailure(tag, throwable);
					}

					@Override
					public void onFailure(int statusCode, Header[] headers,
							Throwable throwable, JSONArray errorResponse) {
						super.onFailure(statusCode, headers, throwable,
								errorResponse);
						responseHandler.onFailure(tag, throwable);
					}

					@Override
					public void onFailure(int statusCode, Header[] headers,
							Throwable throwable, JSONObject errorResponse) {
						super.onFailure(statusCode, headers, throwable,
								errorResponse);
						responseHandler.onFailure(tag, throwable);
					}
				});
	}

	private static void doPost(final RequestTag tag, RequestParams params,
			final ResponseHandler responseHandler) {
//		System.out.println("url:"+RequestConstant.getUrl(tag));
		getHttpClient().post(RequestConstant.getUrl(tag), params,
				new JsonHttpResponseHandler() {
					long startTime = 0;

					@Override
					public void onStart() {
						super.onStart();
						if (ReleaseConstant.ISDEBUG) {
							startTime = System.currentTimeMillis();
						}
						tag.setURI(getRequestURI());
						responseHandler.onStart(tag, Code.ok);
					}

					@Override
					public void onSuccess(int statusCode, Header[] headers,
							JSONObject response) {
						super.onSuccess(statusCode, headers, response);
						if (ReleaseConstant.ISDEBUG) {
							LogUtil.e(
									TAG,
									tag
											+ "use time:"
											+ (System.currentTimeMillis() - startTime));
						}
						if (statusCode == HttpStatus.SC_OK) {
							responseHandler.onSuccess(tag,
									new ServerResponseParser(response));
						}
					}

					public void onSuccess(int statusCode, Header[] headers,
							JSONArray response) {
						super.onSuccess(statusCode, headers, response);
						if (statusCode == HttpStatus.SC_OK) {
							JSONObject object = new JSONObject();
							try {
								object.put(key_wrap, response);
								object.put(key_code, 0);
								object.put(key_msg, "OK");
							} catch (JSONException e) {
								e.printStackTrace();
							}
							responseHandler.onSuccess(tag,
									new ServerResponseParser(object));
						}
					}

					@Override
					public void onFailure(int statusCode, Header[] headers,
							String responseString, Throwable throwable) {
						super.onFailure(statusCode, headers, responseString,
								throwable);
						if (ReleaseConstant.ISDEBUG) {
							LogUtil.d(
									TAG,
									tag
											+ "use time:"
											+ (System.currentTimeMillis() - startTime));
						}
						responseHandler.onFailure(tag, throwable);
					}

					@Override
					public void onFailure(int statusCode, Header[] headers,
							Throwable throwable, JSONArray errorResponse) {
						super.onFailure(statusCode, headers, throwable,
								errorResponse);
						if (ReleaseConstant.ISDEBUG) {
							LogUtil.d(
									TAG,
									tag
											+ "use time:"
											+ (System.currentTimeMillis() - startTime));
						}
						responseHandler.onFailure(tag, throwable);
					}

					@Override
					public void onFailure(int statusCode, Header[] headers,
							Throwable throwable, JSONObject errorResponse) {
						super.onFailure(statusCode, headers, throwable,
								errorResponse);
						if (ReleaseConstant.ISDEBUG) {
							LogUtil.d(
									TAG,
									tag
											+ "use time:"
											+ (System.currentTimeMillis() - startTime));
						}
						responseHandler.onFailure(tag, throwable);
					}

					@Override
					public void onProgress(int bytesWritten, int totalSize) {
						super.onProgress(bytesWritten, totalSize);
						HWFLog.e("debug",
								String.format("%d/%d", bytesWritten, totalSize));
					}
					
					@Override
					public void onFinish() {
						responseHandler.onFinish(tag);
						super.onFinish();
					}
				});
	}

	private static void doPostOpenApi(Context context, final RequestTag tag,
			RequestParams data, final ResponseHandler responseHandler) {

		JSONObject bodyJsonObject = buildOpenApiHttpBody(tag.value(), data);
		StringEntity entity = null;
		try {
			entity = new StringEntity(bodyJsonObject.toString(), "utf-8");
		} catch (UnsupportedEncodingException e1) {
			e1.printStackTrace();
		}
		LogUtil.e(TAG, bodyJsonObject.toString());
		getHttpClient().post(
				context,
				RequestConstant.getUrl(tag)
						+ "?sign="
						+ buildSign(tag, bodyJsonObject)
						+ "&dev_id="
						+ RouterManager.shareInstance().currentRouter()
								.getMac(), entity,
				RequestConstant.ContentType.JSON.getType(),
				new JsonHttpResponseHandler() {
					long startTime = 0;

					@Override
					public void onStart() {
						super.onStart();
						if (ReleaseConstant.ISDEBUG) {
							startTime = System.currentTimeMillis();
						}
						tag.setURI(getRequestURI());
						responseHandler.onStart(tag, Code.ok);
					}

					@Override
					public void onSuccess(int statusCode, Header[] headers,
							JSONObject response) {
						super.onSuccess(statusCode, headers, response);
						if (ReleaseConstant.ISDEBUG) {
							LogUtil.e(
									TAG,
									tag
											+ "use time:"
											+ (System.currentTimeMillis() - startTime));
						}
						if (statusCode == HttpStatus.SC_OK) {
							responseHandler.onSuccess(tag,
									new ServerResponseParser(response));
						}
					}

					public void onSuccess(int statusCode, Header[] headers,
							JSONArray response) {
						super.onSuccess(statusCode, headers, response);
						if (statusCode == HttpStatus.SC_OK) {
							JSONObject object = new JSONObject();
							try {
								object.put(key_wrap, response);
								object.put(key_code, 0);
								object.put(key_msg, "OK");
							} catch (JSONException e) {
								e.printStackTrace();
							}
							responseHandler.onSuccess(tag,
									new ServerResponseParser(object));
						}
					}

					@Override
					public void onFailure(int statusCode, Header[] headers,
							String responseString, Throwable throwable) {
						super.onFailure(statusCode, headers, responseString,
								throwable);
						if (ReleaseConstant.ISDEBUG) {
							LogUtil.d(
									TAG,
									tag
											+ "use time:"
											+ (System.currentTimeMillis() - startTime));
						}
						responseHandler.onFailure(tag, throwable);
					}

					@Override
					public void onFailure(int statusCode, Header[] headers,
							Throwable throwable, JSONArray errorResponse) {
						super.onFailure(statusCode, headers, throwable,
								errorResponse);
						if (ReleaseConstant.ISDEBUG) {
							LogUtil.d(
									TAG,
									tag
											+ "use time:"
											+ (System.currentTimeMillis() - startTime));
						}
						responseHandler.onFailure(tag, throwable);
					}

					@Override
					public void onFailure(int statusCode, Header[] headers,
							Throwable throwable, JSONObject errorResponse) {
						super.onFailure(statusCode, headers, throwable,
								errorResponse);
						if (ReleaseConstant.ISDEBUG) {
							LogUtil.d(
									TAG,
									tag
											+ "use time:"
											+ (System.currentTimeMillis() - startTime));
						}
						responseHandler.onFailure(tag, throwable);
					}
				});
	}

	private static void doPost(Context context, final RequestTag tag,
			HttpEntity entity, String contentType,
			final ResponseHandler responseHandler) {

		getHttpClient().post(context, RequestConstant.getUrl(tag), entity,
				contentType, new JsonHttpResponseHandler() {
					long startTime = 0;

					@Override
					public void onStart() {
						super.onStart();
						if (ReleaseConstant.ISDEBUG) {
							startTime = System.currentTimeMillis();
						}
						tag.setURI(getRequestURI());
						responseHandler.onStart(tag, Code.ok);
					}

					@Override
					public void onSuccess(int statusCode, Header[] headers,
							JSONObject response) {
						super.onSuccess(statusCode, headers, response);
						if (ReleaseConstant.ISDEBUG) {
							LogUtil.e(
									TAG,
									tag
											+ "use time:"
											+ (System.currentTimeMillis() - startTime));
						}
						if (statusCode == HttpStatus.SC_OK) {
							responseHandler.onSuccess(tag,
									new ServerResponseParser(response));
						}
					}

					public void onSuccess(int statusCode, Header[] headers,
							JSONArray response) {
						super.onSuccess(statusCode, headers, response);
						if (statusCode == HttpStatus.SC_OK) {
							JSONObject object = new JSONObject();
							try {
								object.put(key_wrap, response);
								object.put(key_code, 0);
								object.put(key_msg, "OK");
							} catch (JSONException e) {
								e.printStackTrace();
							}
							responseHandler.onSuccess(tag,
									new ServerResponseParser(object));
						}
					}

					@Override
					public void onFailure(int statusCode, Header[] headers,
							String responseString, Throwable throwable) {
						super.onFailure(statusCode, headers, responseString,
								throwable);
						if (ReleaseConstant.ISDEBUG) {
							LogUtil.d(
									TAG,
									tag
											+ "use time:"
											+ (System.currentTimeMillis() - startTime));
						}
						responseHandler.onFailure(tag, throwable);
					}

					@Override
					public void onFailure(int statusCode, Header[] headers,
							Throwable throwable, JSONArray errorResponse) {
						super.onFailure(statusCode, headers, throwable,
								errorResponse);
						if (ReleaseConstant.ISDEBUG) {
							LogUtil.d(
									TAG,
									tag
											+ "use time:"
											+ (System.currentTimeMillis() - startTime));
						}
						responseHandler.onFailure(tag, throwable);
					}

					@Override
					public void onFailure(int statusCode, Header[] headers,
							Throwable throwable, JSONObject errorResponse) {
						super.onFailure(statusCode, headers, throwable,
								errorResponse);
						if (ReleaseConstant.ISDEBUG) {
							LogUtil.d(
									TAG,
									tag
											+ "use time:"
											+ (System.currentTimeMillis() - startTime));
						}
						responseHandler.onFailure(tag, throwable);
					}
					
					@Override
					public void onFinish() {
						// TODO Auto-generated method stub
						responseHandler.onFinish(tag);
						super.onFinish();
					}
				});
	}

	public static void cancelRequest(Context context) {
		getHttpClient().cancelRequests(context, true);
	}

	public static boolean canRequest(RequestTag tag, RequestParams params,
			ResponseHandler responseHandler) {
		if (!NetWorkConnectivity.hasNetwork(Gl.Ct())) {
			responseHandler.onStart(tag, Code.errorNoNetwork);
			return false;
		}
		if (tag.getType() != RequestConstant.TAG_TYPE_USER
				&& tag.getType() != RequestConstant.TAG_TYPE_WEB
				&& tag.getType() != RequestConstant.TAG_TYPE_HWF_S
				&& tag.getType() != RequestConstant.TAG_TYPE_HWF
				&& tag !=  RequestTag.URL_APP_UPDATE_CHECK) {
			if (!User.shareInstance().hasLogin()) {
				responseHandler.onStart(tag, Code.errorNotLoginOrExpired);
				return false;
			}
		}
		if (tag.getType() == RequestConstant.TAG_TYPE_OPEN_CLIENT) {
			if (RouterManager.shareInstance().currentRouter() != null) {
				if (!RouterManager.shareInstance().currentRouter().isBinded()) {
					LogUtil.e(TAG, "router has not bind yet!");
				} else {
					LogUtil.e(TAG, "ok, router has  binded!");
				}
			} else {
				LogUtil.e(TAG, "no router selected");
			}
		}
		return true;
	}
}
