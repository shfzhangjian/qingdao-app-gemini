package com.lucksoft.qingdao.selfinspection.entity;

import com.fasterxml.jackson.annotation.JsonFormat;
import java.io.Serializable;
import java.util.Date;

/**
 * 标准附件表实体 (对应 ZJZK_STANDARD_FILE)
 */
public class ZjzkStandardFile implements Serializable {
    private Long id;
    // private Long linkId; // 移除关联字段
    private String fileName;    // 文件名
    private String filePath;    // 物理路径

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private Date uploadTime;    // 上传时间

    private String uploader;    // 上传人

    // Getters and Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getFileName() { return fileName; }
    public void setFileName(String fileName) { this.fileName = fileName; }
    public String getFilePath() { return filePath; }
    public void setFilePath(String filePath) { this.filePath = filePath; }
    public Date getUploadTime() { return uploadTime; }
    public void setUploadTime(Date uploadTime) { this.uploadTime = uploadTime; }
    public String getUploader() { return uploader; }
    public void setUploader(String uploader) { this.uploader = uploader; }
}