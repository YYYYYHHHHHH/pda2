package com.example.jpda.ui.lists;

import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Color;
import android.os.Message;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ListView;
import android.widget.ScrollView;
import android.widget.TextView;

import com.example.jpda.R;
import com.example.jpda.base.BaseListActivity;
import com.example.jpda.bean.BarCodeBean;
import com.example.jpda.bean.GetBarDetailsBean;
import com.example.jpda.bean.GetBarDetailsRows;
import com.example.jpda.commpont.MyContent;
import com.example.jpda.commpont.SlideLayout;
import com.google.gson.Gson;
import com.zyao89.view.zloading.ZLoadingDialog;
import com.zyao89.view.zloading.Z_TYPE;

import java.util.ArrayList;
import java.util.HashMap;

import androidx.appcompat.app.AlertDialog;
import okhttp3.Request;
import okhttp3.Response;

public class ListSevenActivity extends BaseListActivity {
    private String autoid;
    private String barcode;
    @Override
    protected void init() {
        setContentView(R.layout.activity_listthree);
        numberText = findViewById(R.id.numberText);
        listView = findViewById(R.id.codeitem);
        submit = findViewById(R.id.submit);
        scrollView = findViewById(R.id.scrollview);
        //重写onCreate的时候要记得给strArr赋值
        strArr = new ArrayList<>();
        initSubmit();
        //dialog需要对应当前页面对象
        this.dialog = new ZLoadingDialog(this);
        dialog.setLoadingBuilder(Z_TYPE.DOUBLE_CIRCLE)//设置类型
                .setLoadingColor(Color.BLACK);//颜色
    }

    @Override
    protected void initSubmit() {
        submit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ListSevenActivity.this.finish();
            }
        });
    }

    @Override
    protected void onItemDelet(int position, View v, ViewGroup parent, ArrayList<MyContent> datas) {
        MyContent myContent = datas.get(position);
        SlideLayout slideLayout = (SlideLayout) v.getParent();
        slideLayout.closeMenu(); //解决删除item后下一个item变成open状态问题
        datas.remove(myContent);
        initDeleteBarInTray(myContent.getContent());
    }

    private void initPicking() {
        final Request request = new Request.Builder()
                .url("http://" + setinfo.getString("Ip", "") + "/MeiliPDAServer/home/GetBarsDetails?barcode=" + barcode + "&bTrue=0")
                .get()
                .build();
        dialog.setHintText("加载数据中").show();
        threadPool.execute(new InitPickingRunable(request));
    }
    private void initDeleteBarInTray(String trayCode) {
        final Request request = new Request.Builder()
                .url("http://" + setinfo.getString("Ip", "") + "/MeiliPDAServer/home/DeleteBarInTray?barcode=" + trayCode
                        + "&autoid=" + autoid
                        + "&trayCode=" + barcode
                        + "&loginuser=" + userBean.getUser())
                .get()
                .build();
        threadPool.execute(new DeleteBarInTrayRunable(request));
    }

    @Override
    protected void HandlerProcessing(Message msg) {
        HashMap hashMap = (HashMap) msg.obj;
        Response response = (Response) hashMap.get("response");
        String ReturnMessage = (String) hashMap.get("resStr");
        if (!response.isSuccessful()) {
            toast.setText("服务器出错");
            toast.show();
            return;
        }
        if (msg.what == 1) {
            GetBarsDetailsPostProcessing(ReturnMessage);
        } else if (msg.what == 2) {
            DeleteBarInTrayPostProcessing(ReturnMessage);
        }
    }

    public void GetBarsDetailsPostProcessing(String ReturnMessage) {
        GetBarDetailsBean bean = new Gson().fromJson(ReturnMessage, GetBarDetailsBean.class);
        GetBarDetailsRows[] rows = bean.getRows();
        strArr.clear();
        for (GetBarDetailsRows row : rows) {
            strArr.add(new MyContent(row.getBarcode()));
        }
        renderList();
    }
    
    public void DeleteBarInTrayPostProcessing(String ReturnMessage) {
        BarCodeBean barCodeBean = new Gson().fromJson(ReturnMessage, BarCodeBean.class);
        int status = Integer.parseInt(barCodeBean.getStatus());
        String mesg = barCodeBean.getMsg();
        if (status != 0) {
            toast.setText(mesg);
            toast.show();
        }
        renderList();
    }
    @Override
    protected void onResume() {
        super.onResume();
        Intent intent = getIntent();
        autoid = intent.getStringExtra("autoid");
        barcode = intent.getStringExtra("barcode");
        initPicking();
    }
    class InitPickingRunable extends BaseRunable {
        public InitPickingRunable(Request request) {
            super(request);
            what = 1;
        }
    }
    class DeleteBarInTrayRunable extends BaseRunable {
        public DeleteBarInTrayRunable(Request request) {
            super(request);
            what = 2;
        }
    }
}
