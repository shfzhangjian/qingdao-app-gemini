package com.lucksoft.qingdao.system.controller;

import com.lucksoft.qingdao.system.entity.Attachment;
import com.lucksoft.qingdao.system.service.AttachmentService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;

/**
 * 附件接口控制器
 *
 * @author Gemini
 */
@RestController
@RequestMapping("/api/system/attachments")
public class AttachmentController {

    @Autowired
    private AttachmentService attachmentService;

    /**
     * 根据业务单据信息获取附件列表
     * @param docno   业务单据ID
     * @param doctype 业务单据类型
     * @return 附件列表
     */
    @GetMapping
    public ResponseEntity<List<Attachment>> getAttachments(
            @RequestParam Long docno,
            @RequestParam String doctype) {
        List<Attachment> attachments = attachmentService.getAttachmentsByDoc(docno, doctype);
        return ResponseEntity.ok(attachments);
    }

    /**
     * 上传单个文件
     * @param file    上传的文件 (请求参数名为 "file")
     * @param docno   关联的业务单据ID
     * @param doctype 关联的业务单据类型
     * @return 上传成功后的附件信息
     */
    @PostMapping("/upload")
    public ResponseEntity<Attachment> uploadFile(
            @RequestParam("file") MultipartFile file,
            @RequestParam Long docno,
            @RequestParam String doctype) {
        try {
            Attachment attachment = attachmentService.uploadAndSaveAttachment(file, docno, doctype);
            return ResponseEntity.ok(attachment);
        } catch (IOException e) {
            // 在实际应用中，应进行更详细的异常处理
            e.printStackTrace();
            return ResponseEntity.internalServerError().build();
        }
    }

    /**
     * 删除一个附件
     * @param id 附件ID
     * @return 无内容
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteAttachment(@PathVariable Long id) {
        boolean success = attachmentService.deleteAttachment(id);
        if (success) {
            return ResponseEntity.noContent().build();
        } else {
            return ResponseEntity.notFound().build();
        }
    }
}
