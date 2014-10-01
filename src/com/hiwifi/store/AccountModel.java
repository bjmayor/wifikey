package com.hiwifi.store;

import java.text.ParseException;
import java.text.SimpleDateFormat;

import org.json.JSONObject;

import com.hiwifi.constant.RequestConstant.RequestTag;
import com.hiwifi.model.request.ResponseParserInterface;
import com.hiwifi.model.request.ServerResponseParser;

public class AccountModel implements ResponseParserInterface {

	public int id;
	public String username;
	protected String password;// 密文
	public int specialID;
	public Boolean isValid;
	public static final int ValidTrue = 0;
	public static final int ValidFalse = 1;
	public long createTime;
	public long updateTime;
	public int aid;
	public long validTime;
	// 本地类型,和服务器保持一致
	public static final int SpecialIDCMCC = 1;
	public static final int SpecialIDChinanet = 2;
	public static final int SpecialIDChinaunicom = 3;

	private AccountModel(String username, String password, int specialID,
			Boolean isValid) {
		super();
		this.username = username;
		this.password = password;
		this.specialID = specialID;
		this.isValid = isValid;
		this.createTime = 0;
		this.updateTime = 0;
		this.validTime = 0;
		this.id = -1;
	}

	public static AccountModel instance() {
		return new AccountModel("", "", SpecialIDCMCC, true);
	}

	public void sync() {
		if (this.id == -1) {
			this.id = this.add();
		} else {
			this.update();
		}
	}

	private void update() {
		AccountDbMgr mgr = AccountDbMgr.shareInstance();
		mgr.update(this);
	}

	public void onAccountError() {
		this.delete();
	}

	public int delete() {
		AccountDbMgr mgr = AccountDbMgr.shareInstance();
		return mgr.delete(this);
	}

	private int add() {
		AccountDbMgr mgr = AccountDbMgr.shareInstance();
		AccountModel matchedAccountModel = mgr.queryAccountModel(
				this.specialID, username);
		if (matchedAccountModel != null) {
			matchedAccountModel.password = this.password;
			matchedAccountModel.isValid = true;
			matchedAccountModel.aid = this.aid;
			matchedAccountModel.validTime = this.validTime;
			matchedAccountModel.update();
			return matchedAccountModel.id;
		} else {
			return mgr.insert(this);
		}
	}

	public synchronized final String getPassword() {
		return password;
	}

	public synchronized final void setPassword(String password) {
		this.password = password;
	}

	@Override
	public void parse(RequestTag tag, ServerResponseParser parser) {
		JSONObject response = parser.originResponse;

		try {
			this.username = response.getString("a");
			this.setPassword(response.getString("p"));
			this.aid = response.getInt("aid");
			this.specialID = Integer.parseInt(tag.getParams().get("type"));
			SimpleDateFormat sf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
			long time = sf.parse(response.getString("e")).getTime();
			this.validTime = time;

			this.sync();
		} catch (Exception e) {
			e.printStackTrace();
		}

	}

}
