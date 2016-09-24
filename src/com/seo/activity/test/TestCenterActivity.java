package com.seo.activity.test;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import com.seo.constant.RequestConstant;
import com.seo.constant.RequestConstant.RequestTag;
import com.seo.wifikey.R;

public class TestCenterActivity extends FragmentActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_testcenter);

        if (savedInstanceState == null) {
            getSupportFragmentManager().beginTransaction()
                    .add(R.id.container, new PlaceholderFragment()).commit();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {

        // Inflate the menu; this adds items to the action bar if it is present.
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();
        if (id == R.id.action_settings) {
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    /**
     * A placeholder fragment containing a simple view.
     */
    public static class PlaceholderFragment extends Fragment implements
            OnItemClickListener {

        public ListView listView;
        public List<Map<String, TestItem>> listData = getData();
        TestAdatapter adapter;

        public PlaceholderFragment() {
            super();
        }

        @Override
        public View onCreateView(LayoutInflater inflater, ViewGroup container,
                                 Bundle savedInstanceState) {
            View rootView = inflater.inflate(R.layout.fragment_testcenter,
                    container, false);
            listView = (ListView) rootView.findViewById(R.id.lv_test_url);
            return rootView;
        }

        public class TestItem {
            public String name;
            public RequestTag tag;
            public boolean hasTestedOk;

            public TestItem(String name, RequestTag tag, boolean hasTestedOk) {
                this.name = name;
                this.tag = tag;
                this.hasTestedOk = hasTestedOk;
            }

            @Override
            public String toString() {
                return this.name;
            }
        }

        private List<Map<String, TestItem>> getData() {
            ArrayList<Map<String, TestItem>> list = new ArrayList<Map<String, TestItem>>();

            list.add(makeItem("test_name", new TestItem("获取wifi密码",
                    RequestTag.HIWIFI_PWD_GET, true)));
            list.add(makeItem("test_name", new TestItem("获取运营商密码(deprecated)",
                    RequestTag.HIWIFI_OPONE_GET, false)));
            list.add(makeItem("test_name", new TestItem("上报运营商日志(deprecated)",
                    RequestTag.HIWIFI_OPLOG_SEND, false)));
            list.add(makeItem("test_name", new TestItem("上报扫描的wifi列表",
                    RequestTag.HIWIFI_APLIST_SEND, true)));
            list.add(makeItem("test_name", new TestItem("获取配置信息",
                    RequestTag.HIWIFI_CONFIG_GET, true)));
            list.add(makeItem("test_name", new TestItem("备份wifi",
                    RequestTag.HIWIFI_MYAPLIST_SEND, true)));

            list.add(makeItem("test_name", new TestItem("获取不自动连接的wifi列表",
                    RequestTag.HIWIFI_BLOCKEDWIFI_GET, true)));




            list.add(makeItem("test_name", new TestItem("官方网站",
                    RequestTag.APP_PAGE_OFFICE_WEBSITE, true)));
            list.add(makeItem("test_name", new TestItem("上报最近打开的app列表",
                    RequestTag.HIWIFI_RECENTAPP_SEND, true)));
            list.add(makeItem("test_name", new TestItem("上报用户安装的app列表",
                    RequestTag.HIWIFI_ALLAPP_SEND, true)));



            return list;
        }

        private Map<String, TestItem> makeItem(String name, TestItem testItem) {
            Map<String, TestItem> map = new HashMap<String, TestItem>();
            map.put(name, testItem);
            return map;
        }

        @Override
        public void onAttach(Activity activity) {
            super.onAttach(activity);
            Log.e("debug", "onAttach");
        }

        @Override
        public void onActivityCreated(Bundle savedInstanceState) {
            super.onActivityCreated(savedInstanceState);
            Log.e("debug", "onActivityCreated");
            adapter = new TestAdatapter(getActivity(), listData);
            listView.setAdapter(adapter);
            listView.setOnItemClickListener(this);
        }

        @Override
        public void onResume() {
            super.onResume();
            adapter.notifyDataSetChanged();
        }

        public class TestAdatapter extends BaseAdapter {

            LayoutInflater mInflater;
            List<? extends Map<String, ?>> mData;


            public TestAdatapter(Context context,
                                 List<? extends Map<String, ?>> data) {
                super();
                mData = data;
                mInflater = (LayoutInflater) context
                        .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            }

            @Override
            public View getView(int position, View convertView, ViewGroup parent) {
                View itemView = mInflater.inflate(R.layout.item_test_url, null,
                        false);
                TextView titleTextView = (TextView) itemView
                        .findViewById(R.id.tv_test_name);
                ImageView testResultImageView = (ImageView) itemView
                        .findViewById(R.id.iv_test_status);
                TestItem testItem = (TestItem) mData.get(position).get(
                        "test_name");
                if (testItem.hasTestedOk) {
                    testResultImageView.setImageResource(R.drawable.test_ok);
                }
                if (testItem.tag.getType() != RequestConstant.TAG_TYPE_WEB) {

                    titleTextView.setTextColor(getResources().getColor(
                            R.color.red));
                } else {
                    titleTextView.setTextColor(getResources().getColor(
                            R.color.green));
                }

                titleTextView.setText(testItem.name);
                return itemView;
            }

            @Override
            public int getCount() {
                return mData.size();
            }

            @Override
            public Object getItem(int position) {
                return null;
            }

            @Override
            public long getItemId(int position) {
                return 0;
            }

        }

        @Override
        public void onItemClick(AdapterView<?> parent, View view, int position,
                                long id) {
            TestItem testItem = listData.get(position).get("test_name");
            if (testItem.tag.getType() == RequestConstant.TAG_TYPE_WEB) {
                Intent intent = new Intent();
                intent.setAction("android.intent.action.VIEW");
                Uri content_url = Uri.parse(RequestConstant
                        .getUrl(testItem.tag));
                intent.setData(content_url);
                getActivity().startActivity(intent);
            } else {
                Intent intent = new Intent();
                intent.setClass(getActivity(), UrlTestActivity.class);
                intent.putExtra("title", testItem.name);
                intent.putExtra("tag", testItem.tag);
                getActivity().startActivity(intent);
            }
        }
    }

}
