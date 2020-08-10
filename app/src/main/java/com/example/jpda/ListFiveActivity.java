package com.example.jpda;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.device.ScanManager;
import android.device.scanner.configuration.PropertyID;
import android.graphics.Color;
import android.media.AudioManager;
import android.media.SoundPool;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.example.jpda.bean.BarCodeBean;
import com.example.jpda.bean.SingleNumberBean;
import com.example.jpda.bean.globalbean.MyOkHttpClient;
import com.example.jpda.bean.globalbean.MyToast;
import com.example.jpda.commpont.MyContent;
import com.google.gson.Gson;
import com.zyao89.view.zloading.ZLoadingDialog;
import com.zyao89.view.zloading.Z_TYPE;

import org.xutils.view.annotation.ContentView;
import org.xutils.view.annotation.Event;
import org.xutils.view.annotation.ViewInject;
import org.xutils.x;

import java.io.IOException;
import java.net.ConnectException;
import java.net.SocketTimeoutException;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

@ContentView(R.layout.activity_listtwo)
public class ListFiveActivity extends AppCompatActivity {
    @ViewInject(R.id.submit)
    private Button submit;
    @ViewInject(R.id.inputCode)
    private TextView inputCode;
    @ViewInject(R.id.cDLCode)
    private TextView cDLCode;
    @ViewInject(R.id.ddate)
    private TextView ddate;
    @ViewInject(R.id.cCusName)
    private TextView cCusName;
    @ViewInject(R.id.cWhName)
    private TextView cWhName;
    @ViewInject(R.id.cInvName)
    private TextView cInvName;
    @ViewInject(R.id.cInvStd)
    private TextView cInvStd;
    @ViewInject(R.id.cFree1)
    private TextView cFree1;
    @ViewInject(R.id.inum)
    private TextView inum;
    @ViewInject(R.id.clear)
    private Button clear;
    @ViewInject(R.id.cSOCode)
    private TextView cSOCode;

    private final static String SCAN_ACTION = ScanManager.ACTION_DECODE;//default action
    private OkHttpClient okHttpClient = MyOkHttpClient.getOkHttpClient();
    private SharedPreferences setinfo;
    private ZLoadingDialog dialog;
    private String barcodeStr;
    private Toast toast = MyToast.getToast();
    private boolean isScaning;
    private boolean isClock;
    private SoundPool soundpool = null;
    private ScanManager mScanManager;
    private int soundid;


    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        x.view().inject(this);
        setinfo = getSharedPreferences("GlobalData", Context.MODE_PRIVATE);

    }


    @Event(R.id.inputButton)
    private void ClickInputButton(View view) {
        String code = inputCode.getText().toString();
        if ("".equals(code)) {
            toast.setText("不能添加空的订单号");
            toast.show();
        } else if (isClock) {
            toast.setText("请先清空再添加");
            toast.show();
        } else {
            getMes(code);
        }
    }

    @Event(R.id.clear)
    private void ClickClear(View view) {
        isClock = false;
        render(new SingleNumberBean());
    }

    @Event(R.id.submit)
    private void ClickSubmit(View view) {

    }

    private void getMes(String barcodeStr) {
        final Request request = new Request.Builder()
                .url("http://" + setinfo.getString("Ip", "") + "/MeiliPDAServer/home/GetDispatchInfo?autoId=" + barcodeStr)
                .get()
                .build();
        dialog = new ZLoadingDialog(ListFiveActivity.this);
        dialog.setLoadingBuilder(Z_TYPE.DOUBLE_CIRCLE)//设置类型
                .setLoadingColor(Color.BLACK)//颜色
                .setHintText("检查条码中")
                .show();
        this.barcodeStr = barcodeStr;
        new Thread(new Runnable() {
            @Override
            public void run() {
                Response response = null;
                try {
                    response = okHttpClient.newCall(request).execute();
                    mHandler.obtainMessage(1, response).sendToTarget();
                } catch (IOException e) {
                    dialog.cancel();
                    isScaning = false;
                    e.printStackTrace();
                    if (e instanceof SocketTimeoutException) {
                        toast.setText("请求超时！");
                        toast.show();
                    }
                    if (e instanceof ConnectException) {
                        toast.setText("和服务器连接异常！");
                        toast.show();

                    }
                }
            }
        }).start();
    }
    private BroadcastReceiver mScanReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            // TODO Auto-generated method stub
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
                getMes(barcodeStr);
            }
        }
    };

    private void initScan() {
        // TODO Auto-generated method stub
        mScanManager = new ScanManager();
        mScanManager.openScanner();

        mScanManager.switchOutputMode(0);
        soundpool = new SoundPool(1, AudioManager.STREAM_NOTIFICATION, 100); // MODE_RINGTONE
        soundid = soundpool.load("/etc/Scan_new.ogg", 1);
    }

    private void render(SingleNumberBean bean) {
        cDLCode.setText(bean.getCDLCode());
        ddate.setText(bean.getDdate());
        cCusName.setText(bean.getCCusName());
        cWhName.setText(bean.getCWhName());
        cInvName.setText(bean.getCInvName());
        cInvStd.setText(bean.getCInvStd());
        cFree1.setText(bean.getCFree1());
        inum.setText(bean.getInum());
        cSOCode.setText(bean.getCSOCode());
    }

    private Handler mHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            dialog.cancel();
            isScaning = false;
            Response response = (Response)msg.obj;
            if (!response.isSuccessful()) {
                toast.setText("服务器出错");
                toast.show();
                return;
            }
            if (msg.what == 1) {
                String ReturnMessage = null;
                try {
                    ReturnMessage = response.body().string();
                } catch (IOException e) {
                    e.printStackTrace();
                }
                Log.i("获取的返回信息", ReturnMessage);
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
        }
    };
    @Override
    protected void onResume() {
        // TODO Auto-generated method stub
        super.onResume();
        initScan();
//        showScanResult.setText("");
        IntentFilter filter = new IntentFilter();
        int[] idbuf = new int[]{PropertyID.WEDGE_INTENT_ACTION_NAME, PropertyID.WEDGE_INTENT_DATA_STRING_TAG};
        String[] value_buf = mScanManager.getParameterString(idbuf);
        if (value_buf != null && value_buf[0] != null && !value_buf[0].equals("")) {
            filter.addAction(value_buf[0]);
        } else {
            filter.addAction(SCAN_ACTION);
        }
        registerReceiver(mScanReceiver, filter);
    }

    @Override
    protected void onPause() {
        // TODO Auto-generated method stub
        super.onPause();
        if (mScanManager != null) {
            mScanManager.stopDecode();
            isScaning = false;
        }
        unregisterReceiver(mScanReceiver);
    }
}
