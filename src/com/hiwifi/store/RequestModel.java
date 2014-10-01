package com.hiwifi.store;

import com.hiwifi.constant.RequestConstant;
import com.hiwifi.constant.RequestConstant.RequestTag;
import com.hiwifi.utils.encode.Security;

public class RequestModel {

	public int id;

	public int requestid;
	public static final int RequestID_CMCCLOG = 1;
	private String params;// 加密过的

	public RequestModel() {
		this.id = -1;
		requestid = RequestID_CMCCLOG;
		params = "";
	}

	public final String getUrl() {
		switch (this.requestid) {
		case RequestID_CMCCLOG:
			return RequestConstant.getUrl(RequestTag.HIWIFI_OPLOG_SEND);
		default:
			return RequestConstant.getUrl(RequestTag.HIWIFI_OPLOG_SEND);
		}
	}
	
	public void setParams(String params, Boolean hasEncoded)
	{
		if (hasEncoded) {
			this.params = params;
		}
		else
		{
			this.params = Security.encode_string(params);
		}
	}
	
	public String getDecodeParams()
	{
		return Security.decode_string(this.params);
	}
	
	public String getEncodeParams()
	{
		return this.params;
	}

	public int delete() {
		RequestDbMgr mgr = RequestDbMgr.shareInstance();
		return mgr.delete(this);
	}

	public int add() {
		RequestDbMgr mgr = RequestDbMgr.shareInstance();
		this.id = mgr.insert(this);
		return this.id;
	}
}
