package com.seo.activity;

import android.content.DialogInterface;
import android.graphics.Bitmap;
import android.preference.DialogPreference;
import android.support.v4.app.Fragment;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ListView;

import com.nostra13.universalimageloader.core.ImageLoader;
import com.seo.app.adapter.RecommendAdapter;
import com.seo.app.views.CustomDialog;
import com.seo.wifikey.Gl;
import com.seo.wifikey.R;

import net.youmi.android.diy.DiyManager;

import java.util.List;

import cn.waps.AdInfo;
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
        CustomDialog dialog = new CustomDialog.Builder(getActivity()).setMessage(recommendAdapter.getItem(i).getAdText()+",是否下载？")
                .setPositiveButton("确定", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        AppConnect.getInstance(getActivity()).downloadAd(getActivity(), recommendAdapter.getItem(i).getAdId());
                    }
                })
                .setNegativeButton("取消", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {

                    }
                })
                .create();
        dialog.show();

    }
}
