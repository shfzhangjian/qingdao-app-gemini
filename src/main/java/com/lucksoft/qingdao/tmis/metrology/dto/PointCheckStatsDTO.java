package com.lucksoft.qingdao.tmis.metrology.dto;

public class PointCheckStatsDTO {
    private String dept;

    // 上半年
    private long yingJianShu1;
    private long yiJianShu1;
    private long weiJianShu1;
    private String zhiXingLv1;
    private long zhengChangShu1;
    private long yiChangShu1;

    // 下半年
    private long yingJianShu2;
    private long yiJianShu2;
    private long weiJianShu2;
    private String zhiXingLv2;
    private long zhengChangShu2;
    private long yiChangShu2;

    public PointCheckStatsDTO(String dept, long yingJianShu1, long yiJianShu1, long weiJianShu1, String zhiXingLv1, long zhengChangShu1, long yiChangShu1, long yingJianShu2, long yiJianShu2, long weiJianShu2, String zhiXingLv2, long zhengChangShu2, long yiChangShu2) {
        this.dept = dept;
        this.yingJianShu1 = yingJianShu1;
        this.yiJianShu1 = yiJianShu1;
        this.weiJianShu1 = weiJianShu1;
        this.zhiXingLv1 = zhiXingLv1;
        this.zhengChangShu1 = zhengChangShu1;
        this.yiChangShu1 = yiChangShu1;
        this.yingJianShu2 = yingJianShu2;
        this.yiJianShu2 = yiJianShu2;
        this.weiJianShu2 = weiJianShu2;
        this.zhiXingLv2 = zhiXingLv2;
        this.zhengChangShu2 = zhengChangShu2;
        this.yiChangShu2 = yiChangShu2;
    }

    // Getters
    public String getDept() { return dept; }
    public long getYingJianShu1() { return yingJianShu1; }
    public long getYiJianShu1() { return yiJianShu1; }
    public long getWeiJianShu1() { return weiJianShu1; }
    public String getZhiXingLv1() { return zhiXingLv1; }
    public long getZhengChangShu1() { return zhengChangShu1; }
    public long getYiChangShu1() { return yiChangShu1; }
    public long getYingJianShu2() { return yingJianShu2; }
    public long getYiJianShu2() { return yiJianShu2; }
    public long getWeiJianShu2() { return weiJianShu2; }
    public String getZhiXingLv2() { return zhiXingLv2; }
    public long getZhengChangShu2() { return zhengChangShu2; }
    public long getYiChangShu2() { return yiChangShu2; }
}

