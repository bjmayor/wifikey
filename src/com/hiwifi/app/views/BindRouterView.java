package com.hiwifi.app.views;

import com.hiwifi.hiwifi.R;

import android.content.Context;
import android.util.AttributeSet;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

public class BindRouterView extends RelativeLayout {


	private TextView tv_router_name;
	private LinearLayout ll_router_name;
	private ImageView iv_router_icon;
	private ImageView iv_status_icon;
	private TextView tv_router_status;
	private Context context;
	private boolean isAddButton;
	private LinearLayout ll_status_contain;
	
	public void setAddButton(boolean isAddButton) {
		this.isAddButton = isAddButton;
	}

	public BindRouterView(Context context) {
		super(context);
		this.context = context;
		initView(context);
	}

	public BindRouterView(Context context, AttributeSet attrs) {
		super(context, attrs);
		this.context = context;
		initView(context);
		if(isAddButton){
			setViewAsAddButton();
		}
	}

	public BindRouterView(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
		this.context = context;
		initView(context);
	}

	public void initView(Context context) {
		View.inflate(context, R.layout.item_binded_router, this);
		ll_router_name = (LinearLayout) this.findViewById(R.id.ll_router_name);
		tv_router_name = (TextView) this.findViewById(R.id.tv_router_name);
		iv_router_icon = (ImageView) this.findViewById(R.id.iv_router_icon);
		iv_status_icon = (ImageView) this.findViewById(R.id.iv_status_icon);
		tv_router_status = (TextView) this.findViewById(R.id.tv_router_status);
		ll_status_contain = (LinearLayout) this.findViewById(R.id.ll_status_contain);
		
	}
	
	public void setViewAsAddButton(){
		ll_router_name.setVisibility(View.GONE);
		ll_status_contain.setVisibility(View.GONE);
		iv_router_icon.setBackgroundColor(R.drawable.icon_add);
	}
	
	public void setRouterName(String name){
		tv_router_name.setText(name+"");
	}
	
	public void setRouterNameBg(int id){
		ll_router_name.setBackgroundResource(id);
	}
	
	public void setRouterStatus(boolean sharing){
		if(sharing){
			tv_router_status.setText("共享中");
			tv_router_status.setTextColor(context.getResources().getColor(R.color.text_color_green));
			iv_status_icon.setBackgroundResource(R.drawable.icon_sharing);
		}else {
			tv_router_status.setText("未共享");
			tv_router_status.setTextColor(context.getResources().getColor(R.color.text_color_yellow));
			iv_status_icon.setBackgroundResource(R.drawable.icon_no_share);
		}
	}
	
	public void setRouterIcon(int status){
		if (0==status) { // offline
			iv_router_icon.setBackgroundResource(R.drawable.j1s_offline);
		}else if(1==status){ //online
			iv_router_icon.setBackgroundResource(R.drawable.j1s_share_nothing);
		}else if (2==status) { //sharing
			iv_router_icon.setBackgroundResource(R.drawable.j1s_sharing);
		}
	}
	
	
	public void setViewStatus(int status){
		if (0==status) { // offline
			iv_router_icon.setBackgroundResource(R.drawable.j1s_offline);
			tv_router_status.setText("不在线");
			tv_router_status.setTextColor(context.getResources().getColor(R.color.text_color_gray));
			iv_status_icon.setBackgroundResource(R.drawable.icon_offline);
			ll_router_name.setBackgroundResource(R.drawable.shape_bg_icon_gray);
		}else if(1==status){ //online
			iv_router_icon.setBackgroundResource(R.drawable.j1s_share_nothing);
			tv_router_status.setText("未共享");
			tv_router_status.setTextColor(context.getResources().getColor(R.color.text_color_yellow));
			iv_status_icon.setBackgroundResource(R.drawable.icon_no_share);
			ll_router_name.setBackgroundResource(R.drawable.shape_bg_icon_yellow);
		}else if (2==status) { //sharing
			iv_router_icon.setBackgroundResource(R.drawable.j1s_sharing);
			tv_router_status.setText("共享中");
			tv_router_status.setTextColor(context.getResources().getColor(R.color.text_color_green));
			iv_status_icon.setBackgroundResource(R.drawable.icon_sharing);
			ll_router_name.setBackgroundResource(R.drawable.shape_bg_icon_green);
		}
	}
}
