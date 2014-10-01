package com.hiwifi.app.adapter;

import java.util.ArrayList;
import java.util.List;

import android.content.Context;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Filter;
import android.widget.Filterable;
import android.widget.TextView;

import com.hiwifi.hiwifi.R;


public class AutoTextViewAdapter extends BaseAdapter implements Filterable {  
	  
	public List<String> mList;  
    private Context mContext;  
    private MyFilter mFilter;  
      
    public AutoTextViewAdapter(Context context) {  
        mContext = context;  
        mList = new ArrayList<String>();  
    }  
      
    @Override  
    public int getCount() {  
        return mList == null ? 0 : mList.size();  
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
    public View getView(int position, View convertView, ViewGroup parent) {  
    	String string = mList.get(position);
    	String[] split = string.split("@");
        View inflate = View.inflate(mContext, R.layout.login_dropdown_second, null);
    	TextView tv1 = (TextView) inflate.findViewById(R.id.autotext_1);
    	TextView tv2 = (TextView) inflate.findViewById(R.id.autotext_2);
    	tv1.setText(split[0]);
    	tv2.setText("@"+split[1]);
    	return inflate;
    }  

    public Filter getFilter() {  
        if (mFilter == null) {  
            mFilter = new MyFilter();  
        }  
        return mFilter;  
    }  
    
    private class MyFilter extends Filter {  
    	
    	@Override  
    	protected FilterResults performFiltering(CharSequence constraint) {  
    		FilterResults results = new FilterResults();  
    		if (mList == null) {  
    			mList = new ArrayList<String>();  
    		}  
    		results.values = mList;  
    		results.count = mList.size();  
    		return results;  
    	}  
    	
    	@Override  
    	protected void publishResults(CharSequence constraint, FilterResults results) {  
    		if (results.count > 0) {  
    			notifyDataSetChanged();  
    		} else {  
    			notifyDataSetInvalidated();  
    		}  
    	}  
    	
    }  
}
