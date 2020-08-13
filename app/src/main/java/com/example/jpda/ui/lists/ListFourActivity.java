package com.example.jpda.ui.lists;

import android.graphics.Color;
import android.os.Bundle;

import com.example.jpda.base.BaseListActivity;
import com.example.jpda.bean.BarCodeBean;
import com.google.gson.Gson;
import com.zyao89.view.zloading.ZLoadingDialog;
import com.zyao89.view.zloading.Z_TYPE;

import androidx.annotation.Nullable;

public class ListFourActivity extends BaseListActivity {
    @Override
    protected void init() {
        super.init();
        //设置好提交请求和checkBar的url
        this.submitBarUrl = "othReturnBarfromStock";
        this.checkBarUrl = "CheckBarStatus";
        //dialog需要对应当前页面对象
        this.dialog = new ZLoadingDialog(this);
        dialog.setLoadingBuilder(Z_TYPE.DOUBLE_CIRCLE)//设置类型
                .setLoadingColor(Color.BLACK);//颜色
    }
}
