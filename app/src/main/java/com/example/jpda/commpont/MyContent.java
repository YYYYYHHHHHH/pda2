package com.example.jpda.commpont;

import java.util.Objects;

public class MyContent {
    private String content;
    
    private String bTrue;

    public MyContent(String content) {
        this.content = content;
    }

    public MyContent(String content, String bTrue) {
        this.content = content;
        this.bTrue = bTrue;
    }

    public String getbTrue() {
        return bTrue;
    }

    public void setbTrue(String bTrue) {
        this.bTrue = bTrue;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        MyContent myContent = (MyContent) o;
        return content.equals(myContent.content);
    }

    @Override
    public int hashCode() {
        return Objects.hash(content);
    }
}

