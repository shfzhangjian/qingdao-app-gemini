package com.lucksoft.qingdao.tmis.metrology.dto;

import java.io.Serializable;

/**
 * 点检原始统计数据DTO，用于接收MyBatis从数据库查询出的初步聚合结果。
 * 字段（F1, F1A1...）直接对应A/B/C类统计SQL中的别名。
 */
public class PointCheckRawStatsDTO implements Serializable {
    private static final long serialVersionUID = 1L;
    // 部门ID
    private Long iusedept;
    // 部门名称
    private String susedept;

    // --- A类统计周期 (双月) ---
    // 1-2月应检
    private long f1;
    // 1-2月已检
    private long f1a1;
    // 1-2月正常
    private long f1a2;
    // 1-2月异常
    private long f1a3;

    // 3-4月
    private long f2;
    private long f2a1;
    private long f2a2;
    private long f2a3;

    // 5-6月
    private long f3;
    private long f3a1;
    private long f3a2;
    private long f3a3;

    // 7-8月
    private long f4;
    private long f4a1;
    private long f4a2;
    private long f4a3;

    // 9-10月
    private long f5;
    private long f5a1;
    private long f5a2;
    private long f5a3;

    // 11-12月
    private long f6;
    private long f6a1;
    private long f6a2;
    private long f6a3;

    // Getters and Setters
    public Long getIusedept() { return iusedept; }
    public void setIusedept(Long iusedept) { this.iusedept = iusedept; }
    public String getSusedept() { return susedept; }
    public void setSusedept(String susedept) { this.susedept = susedept; }
    public long getF1() { return f1; }
    public void setF1(long f1) { this.f1 = f1; }
    public long getF1a1() { return f1a1; }
    public void setF1a1(long f1a1) { this.f1a1 = f1a1; }
    public long getF1a2() { return f1a2; }
    public void setF1a2(long f1a2) { this.f1a2 = f1a2; }
    public long getF1a3() { return f1a3; }
    public void setF1a3(long f1a3) { this.f1a3 = f1a3; }
    public long getF2() { return f2; }
    public void setF2(long f2) { this.f2 = f2; }
    public long getF2a1() { return f2a1; }
    public void setF2a1(long f2a1) { this.f2a1 = f2a1; }
    public long getF2a2() { return f2a2; }
    public void setF2a2(long f2a2) { this.f2a2 = f2a2; }
    public long getF2a3() { return f2a3; }
    public void setF2a3(long f2a3) { this.f2a3 = f2a3; }
    public long getF3() { return f3; }
    public void setF3(long f3) { this.f3 = f3; }
    public long getF3a1() { return f3a1; }
    public void setF3a1(long f3a1) { this.f3a1 = f3a1; }
    public long getF3a2() { return f3a2; }
    public void setF3a2(long f3a2) { this.f3a2 = f3a2; }
    public long getF3a3() { return f3a3; }
    public void setF3a3(long f3a3) { this.f3a3 = f3a3; }
    public long getF4() { return f4; }
    public void setF4(long f4) { this.f4 = f4; }
    public long getF4a1() { return f4a1; }
    public void setF4a1(long f4a1) { this.f4a1 = f4a1; }
    public long getF4a2() { return f4a2; }
    public void setF4a2(long f4a2) { this.f4a2 = f4a2; }
    public long getF4a3() { return f4a3; }
    public void setF4a3(long f4a3) { this.f4a3 = f4a3; }
    public long getF5() { return f5; }
    public void setF5(long f5) { this.f5 = f5; }
    public long getF5a1() { return f5a1; }
    public void setF5a1(long f5a1) { this.f5a1 = f5a1; }
    public long getF5a2() { return f5a2; }
    public void setF5a2(long f5a2) { this.f5a2 = f5a2; }
    public long getF5a3() { return f5a3; }
    public void setF5a3(long f5a3) { this.f5a3 = f5a3; }
    public long getF6() { return f6; }
    public void setF6(long f6) { this.f6 = f6; }
    public long getF6a1() { return f6a1; }
    public void setF6a1(long f6a1) { this.f6a1 = f6a1; }
    public long getF6a2() { return f6a2; }
    public void setF6a2(long f6a2) { this.f6a2 = f6a2; }
    public long getF6a3() { return f6a3; }
    public void setF6a3(long f6a3) { this.f6a3 = f6a3; }
}
