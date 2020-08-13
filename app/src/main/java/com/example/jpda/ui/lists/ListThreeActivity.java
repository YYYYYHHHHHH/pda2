package com.example.jpda.ui.lists;

import android.content.DialogInterface;
import android.graphics.Color;
import android.os.Bundle;

import com.example.jpda.base.BaseListActivity;
import com.example.jpda.bean.BarCodeBean;
import com.google.gson.Gson;
import com.zyao89.view.zloading.ZLoadingDialog;
import com.zyao89.view.zloading.Z_TYPE;

public class ListThreeActivity extends BaseListActivity {
    @Override
    protected void init() {
        super.init();
        //设置好提交请求和checkBar的url
        this.submitBarUrl = "ReturnBarFromStockOri";
        this.checkBarUrl = "CheckBarStatus";
        //dialog需要对应当前页面对象
        this.dialog = new ZLoadingDialog(this);
        dialog.setLoadingBuilder(Z_TYPE.DOUBLE_CIRCLE)//设置类型
                .setLoadingColor(Color.BLACK);//颜色
    }

    @Override
    protected void SubmitBarPostProcessing(String ReturnMessage) {
        BarCodeBean barCodeBean = new Gson().fromJson(ReturnMessage, BarCodeBean.class);
        int status = Integer.parseInt(barCodeBean.getStatus());
        String mesg = barCodeBean.getMsg();
        if (status != 0) {
            toast.setText(mesg);
            toast.show();
        } else {
            toast.setText("返工出库成功");
            toast.show();
            strArr.clear();
            renderList();
        }
    }
}
