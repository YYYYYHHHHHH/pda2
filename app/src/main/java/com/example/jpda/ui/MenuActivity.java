package com.example.jpda.ui;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.BaseAdapter;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.TextView;
import android.content.Context;
import android.widget.Toast;

import com.example.jpda.R;
import com.example.jpda.bean.MenuBean;
import com.example.jpda.bean.MenuBgBean;
import com.example.jpda.bean.UserBean;
import com.example.jpda.bean.globalbean.MyOkHttpClient;
import com.example.jpda.bean.globalbean.MyToast;
import com.example.jpda.ui.lists.ListFiveActivity;
import com.example.jpda.ui.lists.ListFourActivity;
import com.example.jpda.ui.lists.ListThreeActivity;
import com.example.jpda.ui.lists.ListTwoActivity;
import com.google.gson.Gson;
import com.zyao89.view.zloading.ZLoadingDialog;

import org.xutils.view.annotation.ContentView;
import org.xutils.view.annotation.ViewInject;
import org.xutils.x;

import androidx.appcompat.app.AppCompatActivity;
import okhttp3.OkHttpClient;

@ContentView(R.layout.activity_menuactivity)
public class MenuActivity extends AppCompatActivity {
    @ViewInject(R.id.gridView)
    GridView gridView;
    final OkHttpClient client = MyOkHttpClient.getOkHttpClient();
    private List<MenuBean> rows;
    private ZLoadingDialog dialog;
    private View view;
    private MyAdapter myAdapter;
    private ArrayList<HashMap<String, Object>> arrayList;
    private UserBean userBean;
    private Toast toast = MyToast.getToast();
    private SharedPreferences setinfo;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        this.requestWindowFeature(Window.FEATURE_NO_TITLE);
        x.view().inject(this);
        setinfo = getSharedPreferences("GlobalData", Context.MODE_PRIVATE);
        userBean = new Gson().fromJson(setinfo.getString("user", ""), UserBean.class);
        toast.setText("欢迎回来：" + userBean.getUser());
        toast.show();
        getMenus();
        gridView.setOnItemClickListener(new OnItemClickListener() {

            @Override
            public void onItemClick(AdapterView<?> arg0, View arg1, int arg2,
                                    long arg3) {
                // TODO Auto-generated method stub
                myAdapter.setSelection(arg2);
                myAdapter.notifyDataSetChanged();
            }
        });
    }

    private void getMenus() {
        String str = "{PDAMenu: 2,Rows: [" +
                "{menuId: 110,menuName: 成品待入库, menuTitle:成品待入库}," +
                "{menuId:411,menuName:条码拆托返工,menuTitle:条码拆托返工}, " +
                "{menuId:412,menuName:条码返工出库,menuTitle:条码返工出库}, " +
                "{menuId:413,menuName:其他出库,menuTitle:其他出库}, " +
                "{menuId:414,menuName:发货拣货,menuTitle:发货拣货}]}";
        mHandler.obtainMessage(1, str).sendToTarget();
    }

    class MyAdapter extends BaseAdapter {
        ArrayList<HashMap<String, Object>> arrayList;
        Context context;
        HashMap<String, Object> hashMap;
        int selectItem = -1;

        public MyAdapter(ArrayList<HashMap<String, Object>> arrayList, Context context) {
            // TODO Auto-generated constructor stub
            this.arrayList = arrayList;
            this.context = context;
        }

        public void setSelection(int position) {
            selectItem = position;
        }

        @Override
        public int getCount() {
            // TODO Auto-generated method stub
            if (null == arrayList) {
                return 0;
            } else {
                return arrayList.size();
            }

        }

        @Override
        public Object getItem(int arg0) {
            // TODO Auto-generated method stub
            return arrayList.get(arg0);
        }

        @Override
        public long getItemId(int arg0) {
            // TODO Auto-generated method stub
            return arg0;
        }

        @SuppressWarnings("unchecked")
        @Override
        public View getView(final int arg0, View arg1, ViewGroup arg2) {
            // TODO Auto-generated method stub
            view = LayoutInflater.from(context).inflate(R.layout.mylayout, arg2, false);
            ImageView imageView = (ImageView) view.findViewById(R.id.imageView);
            final TextView textView = (TextView) view.findViewById(R.id.textView);
            hashMap = (HashMap<String, Object>) getItem(arg0);
            imageView.setImageResource((Integer) hashMap.get("image"));
            textView.setText((CharSequence) hashMap.get("text"));
//            if (selectItem == arg0) {
//                view.setBackgroundColor(Color.GREEN);
//            }
            view.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    String title = textView.getText().toString();
                    if (title.contains("条码拆托返工")) {
                        Intent intent = new Intent(MenuActivity.this, ListTwoActivity.class);
                        startActivity(intent);
                    } else if (title.contains("成品待入库")) {
                        Intent intent = new Intent(MenuActivity.this, ChoiceHouse.class);
                        intent.putExtra("menuid", rows.get(arg0).getMenuId());
                        startActivity(intent);
                    } else if (title.contains("条码返工出库")) {
                        Intent intent = new Intent(MenuActivity.this, ListThreeActivity.class);
                        startActivity(intent);
                    }else if (title.contains("其他出库")) {
                        Intent intent = new Intent(MenuActivity.this, ListFourActivity.class);
                        startActivity(intent);
                    } else if (title.contains("发货拣货")) {
                        Intent intent = new Intent(MenuActivity.this, ListFiveActivity.class);
                        startActivity(intent);
                    }
                }
            });

            return view;
        }//设置适配器或更新适配器调用
    }
    private Handler mHandler = new Handler() {
        @SuppressLint("HandlerLeak")
        @Override
        public void handleMessage(Message msg) {
            if (msg.what == 1) {
                String ReturnMessage = (String) msg.obj;
                Log.i("获取的返回信息", ReturnMessage);
                MenuBgBean menuBgBean = new Gson().fromJson(ReturnMessage, MenuBgBean.class);
                rows = menuBgBean.getRows();
                arrayList = new ArrayList<HashMap<String, Object>>();
                for (int i = 0; i < rows.size(); i++) {
                    HashMap<String, Object> hashMap = new HashMap<String, Object>();
                    hashMap.put("text", rows.get(i).getMenuTitle());
                    Object image = R.mipmap.mima;
                    switch (rows.get(i).getMenuTitle()) {
                        case "条码拆托返工": {
                            image = R.mipmap.fangong;
                            break;
                        }
                        case "成品待入库": {
                            image = R.mipmap.ruku;
                            break;
                        }
                        case "条码不拆托": {
                            image = R.mipmap.buchai;
                            break;
                        }
                        case "其他出库": {
                            image = R.mipmap.chuku;
                            break;
                        }
                        case "发货拣货": {
                            image = R.mipmap.jianhuo;
                            break;
                        }
                    }
                    hashMap.put("image", image);
                    arrayList.add(hashMap);
                    myAdapter = new MyAdapter(arrayList, MenuActivity.this);
                    gridView.setAdapter(myAdapter);
                }

            } else {

            }
        }
    };
}