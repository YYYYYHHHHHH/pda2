package com.example.jpda.bean;

import java.util.Objects;

import androidx.annotation.Nullable;

public class SingleNumberBean {
    private String status;
    private String cDLCode;
    private String cCusName;
    private String ddate;
    private String cInvName;
    private String cInvStd;
    private String cFree1;
    private String iNum;
    private String cWhName;
    private String cSOCode;
    private String cmemo;
    private String 备注;

    public String get备注() {
        return 备注;
    }

    public void set备注(String 备注) {
        this.备注 = 备注;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        SingleNumberBean bean = (SingleNumberBean) o;
        return Objects.equals(status, bean.status) &&
                Objects.equals(cDLCode, bean.cDLCode) &&
                Objects.equals(cCusName, bean.cCusName) &&
                Objects.equals(ddate, bean.ddate) &&
                Objects.equals(cInvName, bean.cInvName) &&
                Objects.equals(cInvStd, bean.cInvStd) &&
                Objects.equals(cFree1, bean.cFree1) &&
                Objects.equals(iNum, bean.iNum) &&
                Objects.equals(cWhName, bean.cWhName) &&
                Objects.equals(cSOCode, bean.cSOCode) &&
                Objects.equals(cmemo, bean.cmemo);
    }

    @Override
    public int hashCode() {
        return Objects.hash(status, cDLCode, cCusName, ddate, cInvName, cInvStd, cFree1, iNum, cWhName, cSOCode, cmemo);
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getCDLCode() {
        return cDLCode;
    }

    public void setCDLCode(String cDLCode) {
        this.cDLCode = cDLCode;
    }

    public String getCCusName() {
        return cCusName;
    }

    public void setCCusName(String cCusName) {
        this.cCusName = cCusName;
    }

    public String getDdate() {
        return ddate;
    }

    public void setDdate(String ddate) {
        this.ddate = ddate;
    }

    public String getCInvName() {
        return cInvName;
    }

    public void setCInvName(String cInvName) {
        this.cInvName = cInvName;
    }

    public String getCInvStd() {
        return cInvStd;
    }

    public void setCInvStd(String cInvStd) {
        this.cInvStd = cInvStd;
    }

    public String getCFree1() {
        return cFree1;
    }

    public void setCFree1(String cFree1) {
        this.cFree1 = cFree1;
    }

    public String getInum() {
        return iNum;
    }

    public void setINum(String iNum) {
        this.iNum = iNum;
    }

    public String getCWhName() {
        return cWhName;
    }

    public void setCWhName(String cWhName) {
        this.cWhName = cWhName;
    }

    public String getCSOCode() {
        return cSOCode;
    }

    public void setCSOCode(String cSOCode) {
        this.cSOCode = cSOCode;
    }

    public String getCmemo() {
        return cmemo;
    }

    public void setCmemo(String cmemo) {
        this.cmemo = cmemo;
    }
}
