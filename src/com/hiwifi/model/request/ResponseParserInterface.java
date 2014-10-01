/**
 * ResponseParserInterface.java
 * com.hiwifi.model.request
 * hiwifiKoala
 * shunping.liu create at 20142014年8月18日下午2:42:58
 */
package com.hiwifi.model.request;

import com.hiwifi.constant.RequestConstant.RequestTag;

/**
 * @author shunping.liu@hiwifi.tw
 *
 */
public interface ResponseParserInterface {
	public  void parse(RequestTag tag, ServerResponseParser parser);
}
