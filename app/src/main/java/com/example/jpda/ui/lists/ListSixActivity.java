package com.example.jpda.ui.lists;

import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Color;
import android.os.Message;
import android.os.Process;
import android.view.View;
import android.view.ViewGroup;

import com.example.jpda.base.BaseListActivity;
import com.example.jpda.bean.BarCodeBean;
import com.example.jpda.bean.BarCodeFourBean;
import com.example.jpda.bean.GetBarDetailsBean;
import com.example.jpda.bean.GetBarDetailsRows;
import com.example.jpda.bean.PDASavedBean;
import com.example.jpda.bean.PDASavedRows;
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

public class ListSixActivity extends BaseListActivity {
    private int allSize = 0;
    private String quantityPicked;
    private String autoid;
    private String trayCode;

    @Override
    protected void init() {
        super.init();
        //设置好提交请求和checkBar的url
        this.submitBarUrl = "SavePDABarsToPVs";
        this.checkBarUrl = "CheckBarInfoAndSave";
        //dialog需要对应当前页面对象
        this.dialog = new ZLoadingDialog(this);
        dialog.setLoadingBuilder(Z_TYPE.DOUBLE_CIRCLE)//设置类型
                .setLoadingColor(Color.BLACK);//颜色
    }

    @Override
    protected void SubmitBarPostProcessing(String ReturnMessage) {
        allSize = 0;
        super.SubmitBarPostProcessing(ReturnMessage);
    }

    @Override
    protected void submitBarCode() {
        String url = "http://" + setinfo.getString("Ip", "") + "/MeiliPDAServer/home/" + submitBarUrl + "?loginuser="
                + userBean.getUserId()
                + "&autoid=" + autoid;
        final Request request = new Request.Builder()
                .url(url)
                .get()
                .build();
        dialog.setHintText("提交中").show();
        threadPool.execute(new SubmitBarRunable(request));
    }

    @Override
    protected void initClaer() {
        clear.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                new AlertDialog.Builder(ListSixActivity.this).setTitle("确认要清空吗")
                        .setIcon(android.R.drawable.ic_dialog_info)
                        .setPositiveButton("确定", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                strArr = new ArrayList<>();
                                allSize = 0;
                                renderList();
                                inputCode.setText("");
                                clearAllCode();

                            }
                        })
                        .setNegativeButton("返回", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                // 点击“返回”后的操作,这里不设置没有任何操作
                            }
                        }).show();
            }
        });
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
            CheckBarPostProcessing(ReturnMessage);
        } else if (msg.what == 2) {
            SubmitBarPostProcessing(ReturnMessage);
        } else if (msg.what == 3) {
            InitPickingPostProcessing(ReturnMessage);
        } else if (msg.what == 4) {
            DeletLocalSaveCloudPostProcessing(ReturnMessage);
        } else if (msg.what == 5) {
            ShowBarDetailPostProcessing(ReturnMessage);
        } else if (msg.what == 6) {
            ClearAllCodePostProcessing(ReturnMessage);
        }
    }

    @Override
    protected void CheckBarPostProcessing(String ReturnMessage) {
        BarCodeFourBean barCodeBean = new Gson().fromJson(ReturnMessage, BarCodeFourBean.class);
        int status = Integer.parseInt(barCodeBean.getStatus());
        String mesg = barCodeBean.getMsg();
        if (status != 0) {
            if (status == -100) {
                mesg += "，或者扫描不清晰";
            }
            toast.setText(mesg);
            toast.show();
        } else {
            strArr.add(new MyContent(barcodeStr, barCodeBean.getbTrue()));
            allSize += Integer.parseInt(barCodeBean.getiNum());
            renderList();
        }
    }

    @Override
    protected void onItemDelet(int position, View v, ViewGroup parent, ArrayList<MyContent> datas) {
        MyContent myContent = datas.get(position);
        SlideLayout slideLayout = (SlideLayout) v.getParent();
        slideLayout.closeMenu(); //解决删除item后下一个item变成open状态问题
        datas.remove(myContent);
        deletLocalSaveCloud(myContent.getContent());
    }

    @Override
    protected void onItemClick(int position, View v, ViewGroup parent, ArrayList<MyContent> datas) {
        MyContent myContent = datas.get(position);
        if (Integer.parseInt(myContent.getbTrue()) == 0) {
            Intent intent = new Intent(ListSixActivity.this, ListSevenActivity.class);
            intent.putExtra("autoid", autoid);
            intent.putExtra("barcode", myContent.getContent());
            startActivity(intent);
        } else {
            showBarDetail(myContent.getContent());            
        }
    }

    private void InitPickingPostProcessing(String ReturnMessage) {
        PDASavedBean bean = new Gson().fromJson(ReturnMessage, PDASavedBean.class);
        PDASavedRows[] rows = bean.getRows();
        strArr.clear();
        allSize = 0;
        for (int i = 0; i < rows.length; i++) {
            strArr.add(new MyContent(rows[i].getScancode(), rows[i].getbTrue()));
            allSize += Integer.parseInt(rows[i].getiNum());
        }
        renderList();
    }

    private void DeletLocalSaveCloudPostProcessing(String ReturnMessage) {
        BarCodeBean bean = new Gson().fromJson(ReturnMessage, BarCodeBean.class);
        if (!"0".equals(bean.getStatus())) {
            toast.setText(bean.getMsg());
            toast.show();
        }
        allSize = 0;
        initPicking();
    }

    private void ShowBarDetailPostProcessing(String ReturnMessage) {
        GetBarDetailsBean bean = new Gson().fromJson(ReturnMessage, GetBarDetailsBean.class);
        GetBarDetailsRows[] rows = bean.getRows();
        StringBuilder builder = new StringBuilder();
        for (int i = 0; i < rows.length; i++) {
            builder.append(rows[i].getBarcode()).append("\n");
        }
        new AlertDialog.Builder(this)
                .setTitle("此条码的组托码")
                .setMessage(builder.toString())
                .show();
    }

    private void ClearAllCodePostProcessing(String ReturnMessage) {
        initPicking();
    }

    private void initPicking() {
        final Request request = new Request.Builder()
                .url("http://" + setinfo.getString("Ip", "") + "/MeiliPDAServer/home/GetBarsFromPDASaved?autoId=" + autoid)
                .get()
                .build();
        dialog.setHintText("加载数据中").show();
        threadPool.execute(new InitPickingRunable(request));
    }

    private void deletLocalSaveCloud(String content) {
        final Request request = new Request.Builder()
                .url("http://" + setinfo.getString("Ip", "") + "/MeiliPDAServer/home/DeleteBarFromPDA?autoId="
                        + autoid
                        + "&barcode=" + content
                        + "&LoginUser=" + userBean.getUser())
                .get()
                .build();
        dialog.setHintText("同步中").show();
        threadPool.execute(new DeletLocalSaveCloudRunable(request));
    }

    private void showBarDetail(String barcodeStr) {
        trayCode = barcodeStr;
        final Request request = new Request.Builder()
                .url("http://" + setinfo.getString("Ip", "") + "/MeiliPDAServer/home/GetBarsDetails?barcode=" + barcodeStr + "&bTrue=1")
                .get()
                .build();
        dialog.setHintText("获取详情中").show();
        threadPool.execute(new ShowBarDetailRunable(request));
    }

    private void clearAllCode() {
        final Request request = new Request.Builder()
                .url("http://" + setinfo.getString("Ip", "") + "/MeiliPDAServer/home/DeleteAllBarFromPDA?autoid=" + autoid
                        + "&LoginUser=" + userBean.getUser())
                .get()
                .build();
        threadPool.execute(new ClearAllCodeRunable(request));
    }

    @Override
    protected void initSubmit() {
        submit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (strArr.size() <= 0) {
                    toast.setText("没有要提交的条码");
                    toast.show();
                    return;
                }
                new AlertDialog.Builder(ListSixActivity.this).setTitle("提示")
                        .setMessage("一共有" + strArr.size() + "码，" + allSize + "件\n所需发货件数为：" + quantityPicked + "件\n确认要提交吗")
                        .setIcon(android.R.drawable.ic_dialog_info)
                        .setPositiveButton("确定", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                submitBarCode();
                            }
                        })
                        .setNegativeButton("返回", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                // 点击“返回”后的操作,这里不设置没有任何操作
                            }
                        }).show();

            }
        });
    }

    @Override
    protected void checkBarCode(String barcodeStr) {
        final Request request = new Request.Builder()
                .url("http://" + setinfo.getString("Ip", "") + "/MeiliPDAServer/home/" + checkBarUrl
                        + "?barcode=" + barcodeStr
                        + "&autoid=" + autoid
                        + "&LoginUser=" + userBean.getUser())
                .get()
                .build();
        dialog.setHintText("检查条码中");
        dialog.show();
        this.barcodeStr = barcodeStr;
        threadPool.execute(new CheckBarCodeRunable(request));
    }

    @Override
    protected void renderList() {
        BaseListActivity.MyAdapter myAdapter = new BaseListActivity.MyAdapter(ListSixActivity.this, strArr);
        listView.setAdapter(myAdapter);
        numberText.setText(strArr.size() + "码(" + allSize + "件)");
        goToBottom();
    }

    @Override
    protected void onResume() {
        super.onResume();
        Intent intent = getIntent();
        autoid = intent.getStringExtra("autoid");
        quantityPicked = intent.getStringExtra("quantityPicked");
        trayCode = "";
        initPicking();
    }

    class InitPickingRunable extends BaseRunable {
        public InitPickingRunable(Request request) {
            super(request);
            what = 3;
        }
    }

    class DeletLocalSaveCloudRunable extends BaseRunable {
        public DeletLocalSaveCloudRunable(Request request) {
            super(request);
            what = 4;
        }
    }

    class ShowBarDetailRunable extends BaseRunable {
        public ShowBarDetailRunable(Request request) {
            super(request);
            what = 5;
        }
    }

    class ClearAllCodeRunable extends BaseRunable {
        public ClearAllCodeRunable(Request request) {
            super(request);
            what = 6;
        }
    }
}
