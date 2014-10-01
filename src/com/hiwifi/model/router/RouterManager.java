/**
 * RouterManager.java
 * com.hiwifi.model.router
 * hiwifiKoala
 * shunping.liu create at 20142014年8月1日下午12:56:56
 */
package com.hiwifi.model.router;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import com.hiwifi.model.ClientInfo;
import com.hiwifi.model.User;
import com.hiwifi.model.log.HWFLog;
import com.hiwifi.model.request.RequestManager;
import com.hiwifi.model.request.ServerResponseParser;

/**
 * @author shunping.liu@hiwifi.tw
 * 
 */
public class RouterManager {
	private static final String TAG = "RouterManager";

	private RouterManager() {
	}

	private static RouterManager manager = null;

	public static RouterManager shareInstance() {
		if (manager == null) {
			manager = new RouterManager();
		}
		return manager;
	}

	private Map<String, ArrayList<Router>> routerListMap = new HashMap<String, ArrayList<Router>>();

	public synchronized ArrayList<Router> getRouterList() {
		if (!User.shareInstance().hasLogin()) {
			return new ArrayList<Router>();
		} else {
			ArrayList<Router> routerList = routerListMap.get(User
					.shareInstance().getUid());
			if (routerList == null) {
				routerList = new ArrayList<Router>();
				routerListMap.put(User.shareInstance().getUid(), routerList);
			}
			return routerList;
		}

	}

	public synchronized ArrayList<Router> setRouters(JSONObject object) {
		try {
			ArrayList<Router> routerList = getRouterList();
			routerList.clear();
			if (object.getJSONArray("routers") != null) {
				JSONArray routers = object.getJSONArray("routers");
				for (int i = 0; i < routers.length(); i++) {
					JSONObject routerObject = routers.getJSONObject(i);
					int rid = routerObject.optInt("rid", 0);
					Router router = getRouterById(rid);
					if (router == null) {
						router = new Router(rid);
						routerList.add(router);
					}
					router.setAppCount(routerObject.optInt("app_cnt", 0));
					router.setOnline(routerObject.optInt("is_online", 0) == 1);
					router.setMac(routerObject.optString("mac", ""));
					router.setAliasName(routerObject.optString("name", ""));
					if (currentRouter() == null && i == 0) {
						ClientInfo.shareInstance().setCurrentRouterId(
								router.getRouterId());
					}
				}
				return routerList;
			}
		} catch (JSONException e) {
			e.printStackTrace();
		}
		return null;
	}

	public synchronized void onBindSuccess(JSONObject object) {
		try {
			JSONArray bindListArray = object
					.getJSONArray(RequestManager.key_wrap);
			for (int i = 0; i < bindListArray.length(); i++) {
				JSONObject jsonObject = bindListArray.getJSONObject(i);
				int rid = Integer.parseInt(jsonObject.getString("rid"));
				if (jsonObject.getInt("code") == ServerResponseParser.ServerCode.OK
						.value()) {
					getRouterById(rid).setClientSecret(
							jsonObject.getString("client_secret"));
				}
			}
		} catch (JSONException e) {
			e.printStackTrace();
		}
	}

	private Router currentRouter = null;

	public Router currentRouter() {
		if (currentRouter == null
				|| currentRouter.getRouterId() != ClientInfo.shareInstance()
						.getCurrentRouterId()) {
			currentRouter = getRouterById(ClientInfo.shareInstance()
					.getCurrentRouterId());
		}
		return currentRouter;
	}

	public Router getRouterById(int routerId) {
		for (Router router : getRouterList()) {
			if (router.getRouterId() == routerId) {
				return router;
			}
		}
		return null;
	}

	public Router getRouterbyMac(String routerMac) {
		return null;
	}

}
