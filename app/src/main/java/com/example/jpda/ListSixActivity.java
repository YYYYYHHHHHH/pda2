package com.example.jpda;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
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
import android.os.Vibrator;
import android.util.Log;
import android.view.Display;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.ArrayAdapter;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import com.example.jpda.bean.BarCodeBean;
import com.example.jpda.bean.BarCodeFourBean;
import com.example.jpda.bean.GetBarDetailsBean;
import com.example.jpda.bean.GetBarDetailsRows;
import com.example.jpda.bean.PDASavedBean;
import com.example.jpda.bean.PDASavedRows;
import com.example.jpda.bean.UserBean;
import com.example.jpda.bean.globalbean.MyOkHttpClient;
import com.example.jpda.bean.globalbean.MyToast;
import com.example.jpda.commpont.MyContent;
import com.example.jpda.commpont.SlideLayout;
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
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Set;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

@ContentView(R.layout.activity_list)
public class ListSixActivity extends AppCompatActivity {
    @ViewInject(R.id.numberText)
    private TextView numberText;
    @ViewInject(R.id.inputCode)
    private EditText inputCode;
    @ViewInject(R.id.codeitem)
    private ListView listView;
    @ViewInject(R.id.inputButton)
    private Button inputButton;
    @ViewInject(R.id.clear)
    private Button clear;
    @ViewInject(R.id.submit)
    private Button submit;
    @ViewInject(R.id.scrollview)
    private ScrollView scrollView;
    private final static String SCAN_ACTION = ScanManager.ACTION_DECODE;//default action
    private boolean isScaning = false;
    private SoundPool soundpool = null;
    private String barcodeStr;
    private ScanManager mScanManager;
    private ZLoadingDialog dialog;
    private UserBean userBean;
    private String autoid;
    private Set<SlideLayout> sets = new HashSet();
    private Toast toast = MyToast.getToast();
    private int soundid;
    private final OkHttpClient client = MyOkHttpClient.getOkHttpClient();
    private ArrayList<MyContent> strArr = null;
    private SharedPreferences setinfo;
    private Vibrator vibrator;
    private final int MAX_BAR = 100;
    private int allSize = 0;
    private String quantityPicked;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Window window = getWindow();
        window.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        x.view().inject(this);
        vibrator = (Vibrator) getSystemService(VIBRATOR_SERVICE);
        setinfo = getSharedPreferences("GlobalData", Context.MODE_PRIVATE);
        userBean = new Gson().fromJson(setinfo.getString("user", ""), UserBean.class);
        this.listView();
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
            if (strArr.contains(new MyContent(barcodeStr))) {
                toast.setText("不能重复扫码！");
                toast.show();
                return;
            }
            if (!isScaning) {
                if (strArr.size() >= MAX_BAR) {
                    toast.setText("一次扫入的条码不能超过【" + MAX_BAR + "】条");
                } else {
                    isScaning = true;
                    checkBarCode(barcodeStr);
                }
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

    private void goToBottom() {
        vibrator.vibrate(200);
        scrollView.post(new Runnable() {
            public void run() {
                scrollView.fullScroll(View.FOCUS_DOWN);
            }
        });
    }

    private void initPicking() {
        final Request request = new Request.Builder()
                .url("http://" + setinfo.getString("Ip", "") + "/MeiliPDAServer/home/GetBarsFromPDASaved?autoId=" + autoid)
                .get()
                .build();
        dialog = new ZLoadingDialog(ListSixActivity.this);
        dialog.setLoadingBuilder(Z_TYPE.DOUBLE_CIRCLE)//设置类型
                .setLoadingColor(Color.BLACK)//颜色
                .setHintText("加载数据中")
                .show();
        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    Response response = client.newCall(request).execute();
                    mHandler.obtainMessage(3, response).sendToTarget();
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

    private void deletLocalSaveCloud(String barcodeStr) {
        final Request request = new Request.Builder()
                .url("http://" + setinfo.getString("Ip", "") + "/MeiliPDAServer/home/DeleteBarFromPDA?autoId="
                        + autoid
                        + "&barcode=" + barcodeStr
                        + "&LoginUser" + userBean.getUser())
                .get()
                .build();
        dialog = new ZLoadingDialog(ListSixActivity.this);
        dialog.setLoadingBuilder(Z_TYPE.DOUBLE_CIRCLE)//设置类型
                .setLoadingColor(Color.BLACK)//颜色
                .setHintText("同步中")
                .show();
        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    Response response = client.newCall(request).execute();
                    mHandler.obtainMessage(4, response).sendToTarget();
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

    @Override
    protected void onResume() {
        // TODO Auto-generated method stub
        super.onResume();
        Intent intent = getIntent();
        autoid = intent.getStringExtra("autoid");
        quantityPicked = intent.getStringExtra("quantityPicked");
        initScan();
        initPicking();
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

    @Event(R.id.submit)
    private void initSubmit(View view) {
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

    @Event(R.id.inputButton)
    private void initInputButton(View view) {
        final String code = inputCode.getText().toString();
        if ("".equals(code)) {
            toast.setText("不能添加空条码");
            toast.show();
        } else {
            if (strArr.contains(new MyContent(code))) {
                toast.setText("不能重复扫码");
                toast.show();
                return;
            }
            checkBarCode(code);
        }
    }

    @Event(R.id.clear)
    private void initClaer(View view) {
        new AlertDialog.Builder(ListSixActivity.this).setTitle("确认要清空吗")
                .setIcon(android.R.drawable.ic_dialog_info)
                .setPositiveButton("确定", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        strArr = new ArrayList<>();
                        MyAdapter myAdapter = new ListSixActivity.MyAdapter(ListSixActivity.this, strArr);
                        listView.setAdapter(myAdapter);
                        allSize = 0;
                        numberText.setText(strArr.size() + "码(" + allSize + "件)");
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

    @Event(R.id.inputCode)
    private void inputCode(View view) {
        inputCode.setFocusable(true);
        inputCode.setFocusableInTouchMode(true);
        inputCode.requestFocus();
    }

    private void clearAllCode() {
        final Request request = new Request.Builder()
                .url("http://" + setinfo.getString("Ip", "") + "/MeiliPDAServer/home/DeleteAllBarFromPDA?autoid=" + autoid
                        + "&LoginUser=" + userBean.getUser())
                .get()
                .build();
        new Thread(new Runnable() {
            @Override
            public void run() {
                Response response = null;
                try {
                    //回调
                    response = client.newCall(request).execute();
                    mHandler.obtainMessage(6, response).sendToTarget();
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


    private void checkBarCode(String barcodeStr) {
        final Request request = new Request.Builder()
                .url("http://" + setinfo.getString("Ip", "") + "/MeiliPDAServer/home/CheckBarInfoAndSave?barcode=" + barcodeStr
                        + "&autoid=" + autoid
                        + "&LoginUser=" + userBean.getUser())
                .get()
                .build();
        dialog = new ZLoadingDialog(ListSixActivity.this);
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
                    //回调
                    response = client.newCall(request).execute();
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

    private void submitBarCode() {
        String url = "http://" + setinfo.getString("Ip", "") + "/MeiliPDAServer/home/SavePDABarsToPVs?loginuser="
                + userBean.getUserId()
                + "&autoid=" + autoid;
        for (MyContent myContent : strArr) {
            url += "&barcodes=" + myContent.getContent();
        }
        final Request request = new Request.Builder()
                .url(url)
                .get()
                .build();
        dialog = new ZLoadingDialog(ListSixActivity.this);
        dialog.setLoadingBuilder(Z_TYPE.DOUBLE_CIRCLE)//设置类型
                .setLoadingColor(Color.BLACK)//颜色
                .setHintText("提交中")
                .show();
        new Thread(new Runnable() {
            @Override
            public void run() {
                Response response = null;
                try {
                    //回调
                    response = client.newCall(request).execute();
                    mHandler.obtainMessage(2, response).sendToTarget();
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

    private void showBarDetail(String barcodeStr) {
        final Request request = new Request.Builder()
                .url("http://" + setinfo.getString("Ip", "") + "/MeiliPDAServer/home/GetBarsDetails?barcode=" + barcodeStr)
                .get()
                .build();
        dialog = new ZLoadingDialog(ListSixActivity.this);
        dialog.setLoadingBuilder(Z_TYPE.DOUBLE_CIRCLE)//设置类型
                .setLoadingColor(Color.BLACK)//颜色
                .setHintText("获取件中")
                .show();
        new Thread(new Runnable() {
            @Override
            public void run() {
                Response response = null;
                try {
                    //回调
                    response = client.newCall(request).execute();
                    mHandler.obtainMessage(5, response).sendToTarget();
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

    private void listView() {
        strArr = new ArrayList<>();
        MyAdapter myAdapter = new ListSixActivity.MyAdapter(this, strArr);
        listView.setAdapter(myAdapter);
    }

    private Handler mHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            isScaning = false;
            dialog.cancel();
            Response response = (Response) msg.obj;
            if (!response.isSuccessful()) {
                toast.setText("服务器出错");
                toast.show();
                return;
            }
            String ReturnMessage = null;
            try {
                ReturnMessage = (String) response.body().string();
            } catch (IOException e) {
                e.printStackTrace();
            }
            if (msg.what == 1) {
                Log.i("获取的返回信息", ReturnMessage);
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
                    strArr.add(new MyContent(barcodeStr));
                    MyAdapter myAdapter = new ListSixActivity.MyAdapter(ListSixActivity.this, strArr);
                    allSize += Integer.parseInt(barCodeBean.getiNum());
                    listView.setAdapter(myAdapter);
                    numberText.setText(strArr.size() + "码(" + allSize + "件)");
                    goToBottom();
                }
            } else if (msg.what == 2) {
                Log.i("获取的返回信息", ReturnMessage);
                BarCodeBean barCodeBean = new Gson().fromJson(ReturnMessage, BarCodeBean.class);
                int status = Integer.parseInt(barCodeBean.getStatus());
                String mesg = barCodeBean.getMsg();
                if (status != 0) {
                    toast.setText(mesg);
                    toast.show();
                } else {
                    new AlertDialog.Builder(ListSixActivity.this).setTitle("拣货单号为：【" + mesg + "】")
                            .setIcon(android.R.drawable.ic_dialog_info)
                            .setPositiveButton("确定", new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                }
                            })
                            .setNegativeButton("返回", new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                }
                            }).show();
                    strArr.clear();
                    allSize = 0;
                    MyAdapter myAdapter = new ListSixActivity.MyAdapter(ListSixActivity.this, strArr);
                    listView.setAdapter(myAdapter);
                    numberText.setText(strArr.size() + "码(" + allSize + "件)");
                }
            } else if (msg.what == 3) {
                handlerThree(ReturnMessage);
            } else if (msg.what == 4) {
                handlerFour(ReturnMessage);
            } else if (msg.what == 5) {
                handlerFive(ReturnMessage);
            } else if (msg.what == 6) {
                handlerSix(ReturnMessage);
            }
        }
    };

    private void handlerSix(String ReturnMessage) {
        initPicking();
    }

    private void handlerFive(String ReturnMessage) {
        GetBarDetailsBean bean = new Gson().fromJson(ReturnMessage, GetBarDetailsBean.class);
        GetBarDetailsRows[] rows = bean.getRows();
        String[] sr = new String[rows.length];
        for (int i = 0; i < rows.length; i++) {
            sr[i] = rows[i].getBarcode();
        }
        final AlertDialog.Builder builder =new AlertDialog.Builder(this);
        builder.setTitle("共【" + bean.get条码明细() + "条】明细");
        builder.setItems(sr, null);
        builder.setPositiveButton("确定", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.cancel();
            }
        });
        builder.setCancelable(true);
        builder.create().show();
    }

    private void handlerThree(String ReturnMessage) {
        PDASavedBean bean = new Gson().fromJson(ReturnMessage, PDASavedBean.class);
        PDASavedRows[] rows = bean.getRows();
        for (int i = 0; i < rows.length; i++) {
            strArr.add(new MyContent(rows[i].getScancode()));
            allSize += Integer.parseInt(rows[i].getiNum());
        }
        MyAdapter myAdapter = new ListSixActivity.MyAdapter(this, strArr);
        listView.setAdapter(myAdapter);
        numberText.setText(strArr.size() + "码(" + allSize + "件)");
    }

    private void handlerFour(String ReturnMessage) {
        BarCodeBean bean = new Gson().fromJson(ReturnMessage, BarCodeBean.class);
        if (!"0".equals(bean.getStatus())) {
            toast.setText(bean.getMsg());
            toast.show();
        }
        allSize = 0;
        initPicking();
    }

    class MyAdapter extends BaseAdapter {
        private Context content;
        private ArrayList<MyContent> datas;

        private MyAdapter(Context context, ArrayList<MyContent> datas) {
            this.content = context;
            this.datas = datas;
        }

        @Override
        public int getCount() {
            return datas.size();
        }

        @Override
        public Object getItem(int position) {
            return datas.get(position);
        }

        @Override
        public long getItemId(int position) {
            return position;
        }


        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            testActivity.ViewHolder viewHolder = null;
            if (convertView == null) {
                convertView = LayoutInflater.from(content).inflate(R.layout.item_slide, null);
                viewHolder = new testActivity.ViewHolder();
                viewHolder.contentView = (TextView) convertView.findViewById(R.id.content);
                viewHolder.menuView = (TextView) convertView.findViewById(R.id.menu);
                convertView.setTag(viewHolder);
            } else {
                viewHolder = (testActivity.ViewHolder) convertView.getTag();
            }
            viewHolder.contentView.setText(datas.get(position).getContent());
            final MyContent myContent = datas.get(position);
            viewHolder.contentView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    showBarDetail(myContent.getContent());
                }
            });

            viewHolder.menuView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    SlideLayout slideLayout = (SlideLayout) v.getParent();
                    slideLayout.closeMenu(); //解决删除item后下一个item变成open状态问题
                    datas.remove(myContent);
                    deletLocalSaveCloud(myContent.getContent());
//                    numberText.setText(strArr.size() + "件");
//                    notifyDataSetChanged();
                }
            });

            SlideLayout slideLayout = (SlideLayout) convertView;
            slideLayout.setOnStateChangeListener(new ListSixActivity.MyAdapter.MyOnStateChangeListener());


            return convertView;
        }

        public SlideLayout slideLayout = null;

        class MyOnStateChangeListener implements SlideLayout.OnStateChangeListener {
            /**
             * 滑动后每次手势抬起保证只有一个item是open状态，加入sets集合中
             **/
            @Override
            public void onOpen(SlideLayout layout) {
                slideLayout = layout;
                if (sets.size() > 0) {
                    for (SlideLayout s : sets) {
                        s.closeMenu();
                        sets.remove(s);
                    }
                }
                sets.add(layout);
            }

            @Override
            public void onMove(SlideLayout layout) {
                if (slideLayout != null && slideLayout != layout) {
                    slideLayout.closeMenu();
                }
            }

            @Override
            public void onClose(SlideLayout layout) {
                if (sets.size() > 0) {
                    sets.remove(layout);
                }
                if (slideLayout == layout) {
                    slideLayout = null;
                }
            }
        }
    }

    static class ViewHolder {
        public TextView contentView;
        public TextView menuView;
    }
}
