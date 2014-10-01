package com.hiwifi.app.adapter;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import android.content.Context;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ListView;

public abstract class ArrayListAdapter<T> extends BaseAdapter{
    
    protected ArrayList<T> mList;
    protected Context mContext;
    protected ListView mListView;
    protected Map<Integer, Boolean> runningMap = new HashMap<Integer, Boolean>();
    
    public ArrayListAdapter(Context context){
        this.mContext = context;
    }

    @Override
    public int getCount() {
        if(mList != null)
            return mList.size();
        else
            return 0;
    }

    @Override
    public Object getItem(int position) {
        return mList == null ? null : mList.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    abstract public View getView(int position, View convertView, ViewGroup parent);
    
    public void setList(ArrayList<T> list){
        this.mList = list;
        for(int i=0;i<this.mList.size();i++){
        	runningMap.put(i, false);
        }
        notifyDataSetChanged();
    }
    
    public ArrayList<T> getList(){
        return mList;
    }
    
    public void setList(T[] list){
        ArrayList<T> arrayList = new ArrayList<T>(list.length);  
        for (T t : list) {  
            arrayList.add(t);  
        }  
//        for(int i=0;i<arrayList.size();i++){
//        	runningMap.put(i, false);
//        }
//        System.out.println("size==>" + runningMap.size());
        setList(arrayList);
    }
    
    public ListView getListView(){
        return mListView;
    }
    
    public void setListView(ListView listView){
        mListView = listView;
    }

}
