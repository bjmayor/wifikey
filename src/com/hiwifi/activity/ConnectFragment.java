package com.hiwifi.activity;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.hiwifi.hiwifi.R;

/**
 * @filename ConnectFragment.java
 * @packagename com.hiwifi.activity
 * @projectname hiwifi1.0.1
 * @author jack at 2014-8-25
 */
public class ConnectFragment extends Fragment{
	
	private View rootView;
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		if(rootView == null){
			rootView = inflater.inflate(R.layout.tab_connect, null);  
		}
		//缓存的rootView需判断是否已经被加过parent，如果有parent需要从parent删除，否则报错
		ViewGroup parent = (ViewGroup) rootView.getParent();
		if(parent !=null){
			parent.removeView(rootView);
		}
		return  rootView;
	}
}
