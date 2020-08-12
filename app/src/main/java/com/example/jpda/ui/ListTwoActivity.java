package com.example.jpda.ui;

import android.graphics.Color;
import android.os.Bundle;

import com.example.jpda.base.BaseListActivity;
import com.zyao89.view.zloading.ZLoadingDialog;
import com.zyao89.view.zloading.Z_TYPE;

import androidx.annotation.Nullable;

public class ListTwoActivity extends BaseListActivity {
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //设置好提交请求和checkBar的url
        this.submitBarUrl = "ReturnBarFromStock";
        this.checkBarUrl = "CheckBarStatus";
        //dialog需要对应当前页面对象
        this.dialog = new ZLoadingDialog(this);
        dialog.setLoadingBuilder(Z_TYPE.DOUBLE_CIRCLE)//设置类型
                .setLoadingColor(Color.BLACK);//颜色
    }
}
