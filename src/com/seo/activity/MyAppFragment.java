package com.seo.activity;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ListView;

import com.nostra13.universalimageloader.core.ImageLoader;
import com.seo.app.adapter.RecommendAdapter;
import com.seo.wifikey.Gl;
import com.seo.wifikey.R;

import cn.waps.AppConnect;

public class MyAppFragment extends Fragment implements AdapterView.OnItemClickListener {

    ListView listView;
    View rootView;
    RecommendAdapter recommendAdapter;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        AppConnect.getInstance(Gl.Ct()).initAdInfo();
    }

    @Override
    public void onResume() {
        super.onResume();
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        rootView = inflater.inflate(R.layout.activity_my_app, container, false);
        listView = (ListView) findViewById(R.id.lv_app);
        return rootView;
    }

    private View findViewById(int id) {
        return rootView.findViewById(id);
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        recommendAdapter = new RecommendAdapter(getActivity(), AppConnect.getInstance(getActivity()).getAdInfoList());
        recommendAdapter.setImageLoader(ImageLoader.getInstance());
        listView.setAdapter(recommendAdapter);
        listView.setOnItemClickListener(this);
    }


    @Override
    public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
        AppConnect.getInstance(getActivity()).clickAd(getActivity(), recommendAdapter.getItem(i).getAdId());
    }
}
