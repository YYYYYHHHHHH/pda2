package com.example.jpda.bean;

public class UserBean {
    private String Status;
    private  String Msg;
    private String User;
    private String UserId;

    public String getUserId() {
        return UserId;
    }

    public void setUserId(String userId) {
        UserId = userId;
    }

    public String getStatus() {
        return Status;
    }

    public void setStatus(String status) {
        Status = status;
    }

    public String getMsg() {
        return Msg;
    }

    public void setMsg(String msg) {
        Msg = msg;
    }

    public String getUser() {
        return User;
    }

    public void setUser(String user) {
        User = user;
    }
}


