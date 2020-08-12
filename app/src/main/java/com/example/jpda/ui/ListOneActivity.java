package com.example.jpda.ui;

import android.content.Intent;
import android.os.Bundle;

import com.example.jpda.base.BaseListActivity;
import com.example.jpda.commpont.MyContent;

import androidx.annotation.Nullable;
import okhttp3.Request;

public class ListOneActivity extends BaseListActivity {
    private String cWhCode;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //设置好提交请求和checkBar的url
        this.submitBarUrl = "CommitBarToStock";
        this.checkBarUrl = "GetBarStatus";
    }

    @Override
    protected void onResume() {
        super.onResume();
        Intent intent = getIntent();
        cWhCode = intent.getStringExtra("cWhCode");
    }

    @Override
    protected void submitBarCode() {
        String url = "http://" + setinfo.getString("Ip", "") + "/MeiliPDAServer/home/" + submitBarUrl + "?userName="
                + userBean.getUserId()
                + "&whCode=" + cWhCode
                + "&tDate=" + setinfo.getString("Date", "");
        for (MyContent myContent : strArr) {
            url += "&barcodes=" + myContent.getContent();
        }
        final Request request = new Request.Builder()
                .url(url)
                .get()
                .build();
        dialog.setHintText("提交中").show();
        threadPool.execute(new SubmitBarRunable(request));
    }
}
