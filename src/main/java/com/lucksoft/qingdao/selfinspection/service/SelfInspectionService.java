package com.lucksoft.qingdao.selfinspection.service;

import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import com.lucksoft.qingdao.selfinspection.dto.DeviceKeyDto;
import com.lucksoft.qingdao.selfinspection.dto.GenerateTaskReq;
import com.lucksoft.qingdao.selfinspection.entity.ZjzkStandardFile;
import com.lucksoft.qingdao.selfinspection.entity.ZjzkTask;
import com.lucksoft.qingdao.selfinspection.entity.ZjzkTaskDetail;
import com.lucksoft.qingdao.selfinspection.entity.ZjzkTool;
import com.lucksoft.qingdao.selfinspection.mapper.ZjzkStandardFileMapper;
import com.lucksoft.qingdao.selfinspection.mapper.ZjzkTaskDetailMapper;
import com.lucksoft.qingdao.selfinspection.mapper.ZjzkTaskMapper;
import com.lucksoft.qingdao.selfinspection.mapper.ZjzkToolMapper;
import com.lucksoft.qingdao.system.entity.User;
import com.lucksoft.qingdao.tmis.dto.PageResult;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.stream.Collectors;

/**
 * 自检自控业务逻辑服务 (重构版)
 */
@Service
public class SelfInspectionService {

    @Autowired
    private ZjzkToolMapper toolMapper;

    @Autowired
    private ZjzkStandardFileMapper fileMapper;

    @Value("${file.upload-dir}")
    private String uploadDir;

    @Autowired
    private ZjzkTaskMapper taskMapper;

    @Autowired
    private ZjzkTaskDetailMapper taskDetailMapper;

    // ==========================================
    // 任务管理 (Generation) - 新增逻辑
    // ==========================================

    /**
     * 生成任务 (基于联合主键)
     */
    @Transactional
    public void generateTasks(GenerateTaskReq req) {
        if (req.getSelectedDevices() == null || req.getSelectedDevices().isEmpty()) {
            throw new RuntimeException("未选择任何设备");
        }

        Date now = new Date();
        SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMddHHmmss");

        for (DeviceKeyDto deviceKey : req.getSelectedDevices()) {
            // 1. 根据联合主键查询该设备下的所有明细
            List<ZjzkTool> tools = toolMapper.selectByDeviceKey(deviceKey.getSpmcode(), deviceKey.getSname());
            if (tools == null || tools.isEmpty()) continue;

            ZjzkTool first = tools.get(0);

            // 2. 生成主表 (设备级)
            ZjzkTask task = new ZjzkTask();
            task.setTaskNo("T" + sdf.format(now) + "-" + UUID.randomUUID().toString().substring(0, 4));
            task.setTaskTime(req.getTaskTime() != null ? req.getTaskTime() : now);
            task.setTaskType(req.getTaskType());
            task.setProdStatus(req.getProdStatus());
            task.setShiftType(req.getShiftType());
            task.setShift(req.getShift());

            task.setSjx(first.getSjx());
            task.setSfname(first.getSfname());
            task.setSbname(first.getSbname());
            task.setSpmcode(first.getSpmcode());

            task.setCheckStatus("待检");
            task.setConfirmStatus("待确认");
            task.setIsOverdue("否");

            taskMapper.insert(task);

            // 3. 生成子表 (明细级 - SAZWZ 安装位置)
            List<ZjzkTaskDetail> details = new ArrayList<>();
            for (ZjzkTool tool : tools) {
                ZjzkTaskDetail detail = new ZjzkTaskDetail();
                detail.setTaskId(task.getIndocno());
                detail.setToolId(tool.getIndocno());
                detail.setItemName(tool.getSazwz());
                details.add(detail);
            }

            if (!details.isEmpty()) {
                taskDetailMapper.batchInsert(details);
            }
        }
    }

    // ==========================================
    // 任务查询 & 执行
    // ==========================================

    public PageResult<ZjzkTask> getTaskPage(Map<String, Object> params) {
        int pageNum = Integer.parseInt(params.getOrDefault("pageNum", "1").toString());
        int pageSize = Integer.parseInt(params.getOrDefault("pageSize", "20").toString());
        PageHelper.startPage(pageNum, pageSize);
        List<ZjzkTask> list = taskMapper.findList(params);
        PageInfo<ZjzkTask> pageInfo = new PageInfo<>(list);
        return new PageResult<>(pageInfo.getList(), pageInfo.getPageNum(), pageInfo.getPageSize(), pageInfo.getTotal(), pageInfo.getPages());
    }

    public List<ZjzkTaskDetail> getTaskDetails(Long taskId) {
        return taskDetailMapper.findByTaskId(taskId);
    }

    @Transactional
    public void submitTaskDetails(Long taskId, List<ZjzkTaskDetail> details, String userRole, User currentUser) {
        boolean isInspector = "inspector".equals(userRole);
        Date now = new Date();

        // 1. 更新明细
        for (ZjzkTaskDetail detail : details) {
            if (isInspector && detail.getCheckResult() != null) {
                detail.setOpTime(now);
                if (currentUser != null) detail.setOperatorName(currentUser.getName());
            }
            if (!isInspector && detail.getIsConfirmed() != null && detail.getIsConfirmed() == 1) {
                detail.setConfirmOpTime(now);
                if (currentUser != null) detail.setConfirmName(currentUser.getName());
            }
            taskDetailMapper.update(detail);
        }

        // 2. 更新主表状态
        ZjzkTask task = taskMapper.findById(taskId);
        if (task != null) {
            if (isInspector) {
                task.setCheckStatus("已检");
                task.setCheckTime(now);
                if(currentUser != null) task.setChecker(currentUser.getName());
            } else {
                // 简单判断：所有都确认才算确认完成
                boolean allConfirmed = details.stream().allMatch(d -> d.getIsConfirmed() != null && d.getIsConfirmed() == 1);
                if (allConfirmed) {
                    task.setConfirmStatus("已确认");
                    task.setConfirmTime(now);
                    if(currentUser != null) task.setConfirmer(currentUser.getName());
                }
            }
            taskMapper.updateStatus(task);
        }
    }

    // ==========================================
    // 台账管理 (ZJZK_TOOL)
    // ==========================================

    public PageResult<ZjzkTool> getLedgerPage(Map<String, Object> params) {
        int pageNum = Integer.parseInt(params.getOrDefault("pageNum", "1").toString());
        int pageSize = Integer.parseInt(params.getOrDefault("pageSize", "15").toString());

        PageHelper.startPage(pageNum, pageSize);
        List<ZjzkTool> list = toolMapper.findList(params);
        PageInfo<ZjzkTool> pageInfo = new PageInfo<>(list);

        return new PageResult<>(pageInfo.getList(), pageInfo.getPageNum(), pageInfo.getPageSize(), pageInfo.getTotal(), pageInfo.getPages());
    }

    @Transactional
    public void saveLedger(ZjzkTool tool) {
        if (tool.getIndocno() == null) {
            tool.setSstepstate("草稿");
            toolMapper.insert(tool);
        } else {
            toolMapper.update(tool);
        }
    }

    @Transactional
    public void deleteLedger(Long id) {
        toolMapper.deleteById(id);
    }

    public List<String> getLedgerOptions(String field) {
        return toolMapper.findDistinctValues(field);
    }

    // ==========================================
    // 附件管理 (ZJZK_STANDARD_FILE)
    // ==========================================

    // 修改：不再依赖 linkId，而是返回所有文件（或者分页）
    public List<ZjzkStandardFile> getAllStandardFiles() {
        return fileMapper.findAll();
    }


    /**
     * 获取用于生成任务的设备列表 (分组去重)
     */
    public PageResult<ZjzkTool> getTaskGenerationDeviceList(Map<String, Object> params) {
        int pageNum = Integer.parseInt(params.getOrDefault("pageNum", "1").toString());
        int pageSize = Integer.parseInt(params.getOrDefault("pageSize", "10").toString());

        PageHelper.startPage(pageNum, pageSize);
        List<ZjzkTool> list = toolMapper.selectDeviceGroupList(params);
        PageInfo<ZjzkTool> pageInfo = new PageInfo<>(list);

        return new PageResult<>(pageInfo.getList(), pageInfo.getPageNum(), pageInfo.getPageSize(), pageInfo.getTotal(), pageInfo.getPages());
    }

    /**
     * 上传标准附件 (仅支持PDF)
     * 移除 linkId 参数
     */
    @Transactional
    public void uploadStandardFile(MultipartFile file, String uploader) throws IOException {
        if (file.isEmpty()) {
            throw new RuntimeException("文件不能为空");
        }
        String originalFilename = file.getOriginalFilename();
        if (originalFilename == null || !originalFilename.toLowerCase().endsWith(".pdf")) {
            throw new RuntimeException("仅支持上传 PDF 文件");
        }

        // 1. 保存文件到物理路径
        String storedFilename = UUID.randomUUID().toString() + ".pdf";
        String storedPath = uploadDir + File.separator + "standards" + File.separator + storedFilename;
        File dest = new File(storedPath);
        if (!dest.getParentFile().exists()) {
            dest.getParentFile().mkdirs();
        }
        file.transferTo(dest);

        // 2. 保存记录到数据库 (不存 linkId)
        ZjzkStandardFile record = new ZjzkStandardFile();
        // record.setLinkId(linkId);
        record.setFileName(originalFilename);
        record.setFilePath(storedPath);
        record.setUploader(uploader);

        fileMapper.insert(record);
    }

    @Transactional
    public void deleteStandardFile(Long id) {
        ZjzkStandardFile file = fileMapper.findById(id);
        if (file != null) {
            fileMapper.deleteById(id);
        }
    }

    public ZjzkStandardFile getFileById(Long id) {
        return fileMapper.findById(id);
    }
}