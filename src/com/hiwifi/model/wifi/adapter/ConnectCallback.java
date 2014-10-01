package com.hiwifi.model.wifi.adapter;


public  interface ConnectCallback  {


	public abstract void onLoginFail(ConnectAdapter connectAdapter, String code, String msg);

	public abstract void onLoginSuccess(ConnectAdapter connectAdapter);

	public abstract void onLogoutFail(ConnectAdapter connectAdapter, String code, String msg);

	public abstract void onLogoutSuccess(ConnectAdapter connectAdapter);

}
