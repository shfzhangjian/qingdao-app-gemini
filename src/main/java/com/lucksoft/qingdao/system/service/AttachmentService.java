package com.lucksoft.qingdao.system.service;

import com.lucksoft.qingdao.system.entity.Attachment;
import com.lucksoft.qingdao.system.mapper.AttachmentMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.util.Date;
import java.util.List;
import java.util.UUID;

/**
 * 附件服务类
 *
 * @author Gemini
 */
@Service
public class AttachmentService {

    @Autowired
    private AttachmentMapper attachmentMapper;

    // 从 application.properties 中读取文件上传路径
    @Value("${file.upload-dir}")
    private String uploadDir;

    /**
     * 根据业务单据查询附件列表
     * @param docno   业务单据ID
     * @param doctype 业务单据类型
     * @return 附件列表
     */
    public List<Attachment> getAttachmentsByDoc(Long docno, String doctype) {
        return attachmentMapper.findByDoc(docno, doctype);
    }

    /**
     * 上传文件并保存附件信息
     * @param file    上传的文件
     * @param docno   关联的业务单据ID
     * @param doctype 关联的业务单据类型
     * @return 保存后的附件信息
     * @throws IOException 文件保存异常
     */
    public Attachment uploadAndSaveAttachment(MultipartFile file, Long docno, String doctype) throws IOException {
        // 1. 将文件保存到服务器磁盘
        String originalFilename = file.getOriginalFilename();
        String fileExtension = originalFilename.substring(originalFilename.lastIndexOf("."));
        String storedFilename = UUID.randomUUID().toString() + fileExtension;
        String storedPath = uploadDir + File.separator + storedFilename;

        File dest = new File(storedPath);
        // 确保目录存在
        if (!dest.getParentFile().exists()) {
            dest.getParentFile().mkdirs();
        }
        file.transferTo(dest);

        // 2. 创建附件实体并保存到数据库
        Attachment attachment = new Attachment();
        attachment.setStitle(originalFilename);
        attachment.setSpath(storedPath); // 存储完整路径或相对路径
        attachment.setDocno(docno);
        attachment.setDoctype(doctype);
        attachment.setAttType(file.getContentType());
        attachment.setAttSize(file.getSize());
        attachment.setCreated(new Date());
        // attachment.setSregid(...); // 在实际应用中，从当前登录用户获取
        // attachment.setSregnm(...);

        attachmentMapper.insert(attachment);

        return attachment;
    }

    /**
     * 根据ID删除附件 (逻辑删除)
     * @param id 附件ID
     * @return 是否成功
     */
    public boolean deleteAttachment(Long id) {
        // 在实际应用中，您可能还想删除磁盘上的物理文件
        return attachmentMapper.deleteById(id) > 0;
    }
}
