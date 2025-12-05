package com.lucksoft.qingdao.selfinspection.entity;


import java.io.Serializable;

/**
 * 自检自控任务执行人员表 (子表)
 * 用于解决单任务对应多个人员的情况
 */
public class ZjzkTaskMember implements Serializable {
    private Long indocno;
    private Long taskId;      // 关联 ZJZK_TASK.INDOCNO
    private String userCode;  // 工号
    private String userName;  // 姓名
    private String userType;  // 类型: 主操/副操/成员 (可选)

    public Long getIndocno() {
        return indocno;
    }

    public void setIndocno(Long indocno) {
        this.indocno = indocno;
    }

    public Long getTaskId() {
        return taskId;
    }

    public void setTaskId(Long taskId) {
        this.taskId = taskId;
    }

    public String getUserCode() {
        return userCode;
    }

    public void setUserCode(String userCode) {
        this.userCode = userCode;
    }

    public String getUserName() {
        return userName;
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }

    public String getUserType() {
        return userType;
    }

    public void setUserType(String userType) {
        this.userType = userType;
    }
}