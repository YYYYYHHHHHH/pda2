package com.example.pda.bean;

public class WhBean {
        private String cWhCode;
        private String cWhName;

    @Override
    public String toString() {
        return cWhName;
    }

    public String getcWhCode() {
        return cWhCode;
    }

    public void setcWhCode(String cWhCode) {
        this.cWhCode = cWhCode;
    }

    public String getcWhName() {
        return cWhName;
    }

    public void setcWhName(String cWhName) {
        this.cWhName = cWhName;
    }
}
