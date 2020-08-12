package com.example.jpda.bean.globalbean;

import android.app.Dialog;
import android.content.Context;
import android.graphics.Color;
import android.view.View;

import com.zyao89.view.zloading.ZLoadingDialog;
import com.zyao89.view.zloading.Z_TYPE;

public class MyDiaLog {
    private static Object object = new Object();
    private static ZLoadingDialog dialog = null;
    public static ZLoadingDialog getDialog(Context context) {
        if (dialog == null) {
            synchronized (object) {
                if (dialog == null) {
                    dialog = new ZLoadingDialog(context);
                    dialog.setLoadingBuilder(Z_TYPE.DOUBLE_CIRCLE)//设置类型
                            .setLoadingColor(Color.BLACK);//颜色
                }
            }
        }
        return dialog;
    }
}
