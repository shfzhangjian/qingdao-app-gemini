package com.lucksoft.qingdao.tmis.metrology;


public class ExportColumn {
    private String key;
    private String title;

    // Getters and Setters
    public String getKey() {
        return key;
    }

    // 无参构造函数
    public ExportColumn() {
    }

    // [新增] 全参构造函数，方便 new ExportColumn(key, title) 调用
    public ExportColumn(String key, String title) {
        this.key = key;
        this.title = title;
    }

    public void setKey(String key) {
        this.key = key;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }
}

