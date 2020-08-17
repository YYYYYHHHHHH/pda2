package com.example.jpda.ui.lists;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.device.ScanManager;
import android.graphics.Color;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import com.example.jpda.R;
import com.example.jpda.base.BaseListActivity;
import com.example.jpda.bean.SingleNumberBean;
import com.example.jpda.commpont.MyContent;
import com.google.gson.Gson;
import com.zyao89.view.zloading.ZLoadingDialog;
import com.zyao89.view.zloading.Z_TYPE;

import org.xutils.view.annotation.ViewInject;

import okhttp3.Request;

public class ListFiveActivity extends BaseListActivity {
    private String quantityPicked;
    private SingleNumberBean singleNumberBean = new SingleNumberBean();
    private boolean isClock;

    private Button submit;
    private TextView inputCode;
    private TextView cDLCode;
    private TextView ddate;
    private TextView cCusName;
    private TextView cWhName;
    private TextView cInvName;
    private TextView cInvStd;
    private TextView cFree1;
    private TextView inum;
    private Button clear;
    private TextView cSOCode;
    private TextView cmemo;
    private Button inputButton;
    @Override
    protected void init() {
        setContentView(R.layout.activity_listtwo);
        submit = findViewById(R.id.submit);
        inputCode = findViewById(R.id.inputCode);
        cDLCode = findViewById(R.id.cDLCode);
        ddate = findViewById(R.id.ddate);
        cCusName = findViewById(R.id.cCusName);
        cWhName = findViewById(R.id.cWhName);
        cInvName = findViewById(R.id.cInvName);
        cInvStd = findViewById(R.id.cInvStd);
        cFree1 = findViewById(R.id.cFree1);
        inum = findViewById(R.id.inum);
        clear = findViewById(R.id.clear);
        cSOCode = findViewById(R.id.cSOCode);
        cmemo = findViewById(R.id.cmemo);
        inputButton = findViewById(R.id.inputButton);
        //设置好提交请求和checkBar的url
        this.checkBarUrl = "GetDispatchInfo";
        //dialog需要对应当前页面对象
        this.dialog = new ZLoadingDialog(this);
        dialog.setLoadingBuilder(Z_TYPE.DOUBLE_CIRCLE)//设置类型
                .setLoadingColor(Color.BLACK);//颜色
        initInputButton();
        initSubmit();
        initClaer();
    }
    @Override
    protected void initInputButton() {
        inputButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String code = inputCode.getText().toString();
                if ("".equals(code)) {
                    toast.setText("不能添加空的订单号");
                    toast.show();
                } else if (isClock) {
                    toast.setText("请先清空再添加");
                    toast.show();
                } else {
                    checkBarCode(code);
                }
            }
        });
    }
    @Override
    protected void initClaer() {
        clear.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                isClock = false;
                render(new SingleNumberBean());
            }
        });
    }
    @Override
    protected void initSubmit() {
        submit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (singleNumberBean.equals(new SingleNumberBean())) {
                    toast.setText("请先扫描单号");
                    toast.show();
                } else {
                    Intent intent = new Intent(ListFiveActivity.this, ListSixActivity.class);
                    intent.putExtra("autoid", barcodeStr);
                    intent.putExtra("quantityPicked", quantityPicked);
                    startActivity(intent);
                }
            }
        });
    }
    @Override
    protected void checkBarCode(String barcodeStr) {
        final Request request = new Request.Builder()
                .url("http://" + setinfo.getString("Ip", "") + "/MeiliPDAServer/home/" + checkBarUrl + "?autoid=" + barcodeStr)
                .get()
                .build();
        dialog.setHintText("加载数据中");
        dialog.show();
        this.barcodeStr = barcodeStr;
        threadPool.execute(new CheckBarCodeRunable(request));
    }
    @Override
    protected void CheckBarPostProcessing(String ReturnMessage) {
        SingleNumberBean bean = new Gson().fromJson(ReturnMessage, SingleNumberBean.class);
        int status = Integer.parseInt(bean.getStatus());
        if (status != 0) {
            String mesg = bean.get备注();
            toast.setText(mesg);
            toast.show();
        } else {
            isClock = true;
            render(bean);
        }
    }

    @Override
    protected void ScanReceiverRun(Context context, Intent intent) {
        soundpool.play(soundid, 1, 1, 0, 0, 1);
        byte[] barcode = intent.getByteArrayExtra(ScanManager.DECODE_DATA_TAG);
        int barcodelen = intent.getIntExtra(ScanManager.BARCODE_LENGTH_TAG, 0);
        byte temp = intent.getByteExtra(ScanManager.BARCODE_TYPE_TAG, (byte) 0);
        android.util.Log.i("debug", "----codetype--" + temp);
        barcodeStr = new String(barcode, 0, barcodelen);
        android.util.Log.i("debug", "----code--" + barcodeStr);
        if (isClock) {
            toast.setText("请先清空再重新扫描");
            toast.show();
            return;
        }
        if (!isScaning) {
            isScaning = true;
            checkBarCode(barcodeStr);
        }
    }

    private void render(SingleNumberBean bean) {
        singleNumberBean = bean;
        cDLCode.setText(bean.getCDLCode());
        ddate.setText(bean.getDdate());
        cCusName.setText(bean.getCCusName());
        cWhName.setText(bean.getCWhName());
        cInvName.setText(bean.getCInvName());
        cInvStd.setText(bean.getCInvStd());
        cFree1.setText(bean.getCFree1());
        inum.setText(bean.getInum());
        cSOCode.setText(bean.getCSOCode());
        cmemo.setText(bean.getCmemo());
        quantityPicked = bean.getInum();
    }
}
