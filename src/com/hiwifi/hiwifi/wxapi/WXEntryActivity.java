/*
 * 官网地站:http://www.ShareSDK.cn
 * 技术支持QQ: 4006852216
 * 官方微信:ShareSDK   （如果发布新版本的话，我们将会第一时间通过微信将版本更新内容推送给您。如果使用过程中有任何问题，也可以通过微信与我们取得联系，我们将会在24小时内给予回复）
 *
 * Copyright (c) 2013年 ShareSDK.cn. All rights reserved.
 */

package com.hiwifi.hiwifi.wxapi;

import cn.sharesdk.wechat.utils.WXAppExtendObject;
import cn.sharesdk.wechat.utils.WXMediaMessage;
import cn.sharesdk.wechat.utils.WechatHandlerActivity;

/** 微信客户端回调activity示例 */
public class WXEntryActivity extends WechatHandlerActivity{

	/**
	 * 处理微信发出的向第三方应用请求app message
	 * <p>
	 * 在微信客户端中的聊天页面有“添加工具”，可以将本应用的图标添加到其中
	 * 此后点击图标，下面的代码会被执行。Demo仅仅只是打开自己而已，但你可
	 * 做点其他的事情，包括根本不打开任何页面
	 */
	public void onGetMessageFromWXReq(WXMediaMessage msg) {
	}

	/**
	 * 处理微信向第三方应用发起的消息
	 * <p>
	 * 此处用来接收从微信发送过来的消息，比方说本demo在wechatpage里面分享
	 * 应用时可以不分享应用文件，而分享一段应用的自定义信息。接受方的微信
	 * 客户端会通过这个方法，将这个信息发送回接收方手机上的本demo中，当作
	 * 回调。
	 * <p>
	 * 本Demo只是将信息展示出来，但你可做点其他的事情，而不仅仅只是Toast
	 */
	public void onShowMessageFromWXReq(WXMediaMessage msg) {
//		if (msg != null && msg.mediaObject != null
//				&& (msg.mediaObject instanceof WXAppExtendObject)) {
//			WXAppExtendObject obj = (WXAppExtendObject) msg.mediaObject;
//			Toast.makeText(this, obj.extInfo, Toast.LENGTH_SHORT).show();
			// System.out.println("-----------");
//		}
	}
//发送到微信请求的响应结果将回调到onResp方法
//	@Override
//	public void onResp(BaseResp resp) {
//		switch (resp.errCode) {
// 		case BaseResp.ErrCode.ERR_OK:
// 			Toast.makeText(this, "分享成功", Toast.LENGTH_LONG).show();
// 			//result = R.string.errcode_success;
// 			break;
// 		case BaseResp.ErrCode.ERR_USER_CANCEL:
// 			//result = R.string.errcode_cancel;
// 			Toast.makeText(this, "分享被取消", Toast.LENGTH_LONG).show();
// 			break;
// 		case BaseResp.ErrCode.ERR_AUTH_DENIED:
// 			Toast.makeText(this, "分享失败", Toast.LENGTH_LONG).show();
// 			//result = R.string.errcode_deny;
// 			break;
// 		default:
// 			Toast.makeText(this, "分享失败", Toast.LENGTH_LONG).show();
// 			//result = R.string.errcode_unknown;
// 			break;
// 		}
//	}
//	private IWXAPI api;
//	private String APP_ID="wx04317dd6d8892ad7";
//	protected void onCreate(Bundle arg0) {
//		super.onCreate(arg0);
////	        setContentView(R.layout.test);
////	        Button   gotoBtn = (Button) findViewById(R.id.gotowx);
//	       //注册微信API
////	        api = WXAPIFactory.createWXAPI(this,APP_ID, false);
//////		       api=WxUtils.registWxApi(WXEntryActivity.this);
////	        gotoBtn.setOnClickListener(new View.OnClickListener() {
////				
////				
//				public void onClick(View v) {
////					
//				   String url = "http://img1.motou.com/haiwai/20110119/20110119210529576.jpg";
////					    //发布文本
////						//WxUtils.sendWx("程序分享微博成功啦......");
////				        //发布图片
////						//WxUtils.sendImageWx(null, null, url, 120, 120,SendMessageToWX.Req.WXSceneSession);
//						Bitmap bmp=null;
//						try {
//							bmp = BitmapFactory.decodeStream(new URL(url).openStream());
//							//发布连接
////							WxUtils.sendWebPageWx("http://www.baidu.com", "http://www.baidu.com", "zkyc应用上线啦，点此观看", bmp, 120, 120, SendMessageToWX.Req.WXSceneSession);
//						} catch (Exception e) {
//							e.printStackTrace();
//						} 			
//					}
//			});
//	    	
//	    	api.handleIntent(getIntent(), this);
//	}


}
