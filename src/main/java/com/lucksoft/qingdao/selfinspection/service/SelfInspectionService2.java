package com.lucksoft.qingdao.selfinspection.service;

import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import com.lucksoft.qingdao.selfinspection.dto.DeviceKeyDto;
import com.lucksoft.qingdao.selfinspection.dto.GenerateTaskReq;
import com.lucksoft.qingdao.selfinspection.entity.*;
import com.lucksoft.qingdao.selfinspection.mapper.*;
import com.lucksoft.qingdao.system.entity.User;
import com.lucksoft.qingdao.tmis.dto.PageResult;
import com.lucksoft.qingdao.tspm.dto.tims.CreateSelfCheckTaskReq;
import com.lucksoft.qingdao.tspm.service.TimsServiceClient;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFRichTextString;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 自检自控业务逻辑服务 (重构版)
 */
@Service
public class SelfInspectionService2 {

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
     * 生成任务
     */
    @Transactional
    public void generateTasks(GenerateTaskReq req) {
        if (req.getSelectedDevices() == null || req.getSelectedDevices().isEmpty()) {
            throw new RuntimeException("未选择任何设备");
        }

        Date now = new Date();
        SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMddHHmmss");

        for (DeviceKeyDto deviceKey : req.getSelectedDevices()) {
            List<ZjzkTool> tools = toolMapper.selectBySpmCode(deviceKey.getSpmcode());

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
    // [新增] 点检统计查询 (SiStats)
    // ==========================================

    /**
     * 获取点检统计分页数据 (从任务明细维度展开)
     */
    public PageResult<Map<String, Object>> getStatsPage(Map<String, Object> params) {
        int pageNum = Integer.parseInt(params.getOrDefault("pageNum", "1").toString());
        int pageSize = Integer.parseInt(params.getOrDefault("pageSize", "20").toString());

        PageHelper.startPage(pageNum, pageSize);
        // 调用联表查询方法
        List<Map<String, Object>> list = taskDetailMapper.findStatsList(params);
        PageInfo<Map<String, Object>> pageInfo = new PageInfo<>(list);

        return new PageResult<>(pageInfo.getList(), pageInfo.getPageNum(), pageInfo.getPageSize(), pageInfo.getTotal(), pageInfo.getPages());
    }

    /**
     * [新增] 获取点检统计数据用于导出 (不分页)
     */
    public List<Map<String, Object>> getStatsListForExport(Map<String, Object> params) {
        // 不调用 startPage，直接查询所有符合条件的数据
        return taskDetailMapper.findStatsList(params);
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

    public List<ZjzkStandardFile> getAllStandardFiles() {
        return fileMapper.findAll();
    }

    public PageResult<ZjzkTool> getTaskGenerationDeviceList(Map<String, Object> params) {
        int pageNum = Integer.parseInt(params.getOrDefault("pageNum", "1").toString());
        int pageSize = Integer.parseInt(params.getOrDefault("pageSize", "10").toString());

        PageHelper.startPage(pageNum, pageSize);
        List<ZjzkTool> list = toolMapper.selectDeviceGroupList(params);
        PageInfo<ZjzkTool> pageInfo = new PageInfo<>(list);

        return new PageResult<>(pageInfo.getList(), pageInfo.getPageNum(), pageInfo.getPageSize(), pageInfo.getTotal(), pageInfo.getPages());
    }

    @Transactional
    public void uploadStandardFile(MultipartFile file, String uploader) throws IOException {
        if (file.isEmpty()) {
            throw new RuntimeException("文件不能为空");
        }
        String originalFilename = file.getOriginalFilename();
        if (originalFilename == null || !originalFilename.toLowerCase().endsWith(".pdf")) {
            throw new RuntimeException("仅支持上传 PDF 文件");
        }

        String storedFilename = UUID.randomUUID().toString() + ".pdf";
        String storedPath = uploadDir + File.separator + "standards" + File.separator + storedFilename;
        File dest = new File(storedPath);
        if (!dest.getParentFile().exists()) {
            dest.getParentFile().mkdirs();
        }
        file.transferTo(dest);

        ZjzkStandardFile record = new ZjzkStandardFile();
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



    // [新增] 导出用的全量查询接口
    public List<ZjzkTool> getLedgerListForExport(Map<String, Object> params) {
        return toolMapper.findList(params);
    }


    private static final Logger log = LoggerFactory.getLogger(SelfInspectionService2.class);

    // 缓存生成的错误报告文件路径，key为UUID
    private final Map<String, String> errorFileCache = new ConcurrentHashMap<>();


    // ==========================================
    //  Excel 导入与校验逻辑
    // ==========================================

    /**
     * 导入台账数据
     * 校验规则更新：
     * 1. 必填校验: PM编码(SPMCODE), 资产编码(SZCNO)
     * 2. 重复校验: 12个关键字段组合唯一
     */
    public Map<String, Object> importLedgerData(MultipartFile file) {
        Map<String, Object> result = new HashMap<>();
        List<Map<String, Object>> errorDetails = new ArrayList<>();

        Workbook workbook = null;
        InputStream is = null;

        try {
            is = file.getInputStream();
            workbook = new XSSFWorkbook(is);
            Sheet sheet = workbook.getSheetAt(0);

            // 样式准备：红色背景
            CellStyle errorStyle = workbook.createCellStyle();
            errorStyle.setFillForegroundColor(IndexedColors.RED.getIndex());
            errorStyle.setFillPattern(FillPatternType.SOLID_FOREGROUND);
            Font font = workbook.createFont();
            font.setColor(IndexedColors.WHITE.getIndex());
            errorStyle.setFont(font);

            List<ZjzkTool> validTools = new ArrayList<>();
            boolean hasError = false;

            // 缓存文件内的唯一性 key
            Set<String> fileUniqueKeys = new HashSet<>();

            // 假设第一行是表头，从第二行开始
            for (int i = 1; i <= sheet.getLastRowNum(); i++) {
                Row row = sheet.getRow(i);
                if (row == null) continue;

                boolean rowHasError = false;
                StringBuilder rowErrorMsg = new StringBuilder();

                // 读取单元格 (根据用户指定的顺序)
                // 0:车间, 1:名称, 2:所属机型, 3:所属设备, 4:主数据名称, 5:PM编码
                // 6:安装位置, 7:厂家, 8:规格型号, 9:测量原理, 10:资产编码, 11:订单编号

                String dept = getCellValue(row.getCell(0));       // 车间 SDEPT
                String name = getCellValue(row.getCell(1));       // 名称 SNAME
                String model = getCellValue(row.getCell(2));      // 所属机型 SJX
                String deviceName = getCellValue(row.getCell(3)); // 所属设备 SFNAME
                String sbName = getCellValue(row.getCell(4));     // 主数据名称 SBNAME
                String pmCode = getCellValue(row.getCell(5));     // PM编码 SPMCODE
                String location = getCellValue(row.getCell(6));   // 安装位置 SAZWZ
                String factory = getCellValue(row.getCell(7));    // 厂家 SCJ
                String spec = getCellValue(row.getCell(8));       // 规格型号 SXH
                String principle = getCellValue(row.getCell(9));  // 测量原理 SYL
                String assetCode = getCellValue(row.getCell(10)); // 资产编码 SZCNO
                String orderNo = getCellValue(row.getCell(11));   // 订单编号 SDDNO

                // --- 校验逻辑 ---

                // A. 必填校验 (PM编码, 资产编码)
                if (isEmpty(pmCode)) {
                    markCellError(row.getCell(5, Row.MissingCellPolicy.CREATE_NULL_AS_BLANK), errorStyle, "PM编码不能为空");
                    rowHasError = true;
                    rowErrorMsg.append("PM编码缺失; ");
                }

                if (isEmpty(assetCode)) {
                    markCellError(row.getCell(10, Row.MissingCellPolicy.CREATE_NULL_AS_BLANK), errorStyle, "资产编码不能为空");
                    rowHasError = true;
                    rowErrorMsg.append("资产编码缺失; ");
                }

                // B. 组合重复校验 (12个字段)
                // 仅当基本数据不全为空时才校验重复，避免空行干扰
                if (!rowHasError || (!isEmpty(pmCode) || !isEmpty(assetCode))) {
                    String uniqueKey = String.format("%s_%s_%s_%s_%s_%s_%s_%s_%s_%s_%s_%s",
                            dept, name, model, deviceName, sbName, pmCode, location, factory, spec, principle, assetCode, orderNo);

                    if (fileUniqueKeys.contains(uniqueKey)) {
                        // 标记整行或首个单元格为重复
                        markCellError(row.getCell(0, Row.MissingCellPolicy.CREATE_NULL_AS_BLANK), errorStyle, "重复数据: 该行内容与前面行完全一致");
                        rowHasError = true;
                        rowErrorMsg.append("数据重复; ");
                    } else {
                        fileUniqueKeys.add(uniqueKey);
                    }
                }

                if (rowHasError) {
                    hasError = true;
                    Map<String, Object> err = new HashMap<>();
                    err.put("row", i + 1);
                    err.put("msg", rowErrorMsg.toString());
                    errorDetails.add(err);
                } else {
                    // 构建对象
                    ZjzkTool tool = new ZjzkTool();
                    tool.setSdept(dept);
                    tool.setSname(name);
                    tool.setSjx(model);
                    tool.setSfname(deviceName);
                    tool.setSbname(sbName);
                    tool.setSpmcode(pmCode);
                    tool.setSazwz(location);
                    tool.setScj(factory);
                    tool.setSxh(spec);
                    tool.setSyl(principle);
                    tool.setSzcno(assetCode);
                    tool.setSddno(orderNo);

                    tool.setDtime(new Date()); // 默认当前导入时间
                    tool.setSstepstate("导入");
                    validTools.add(tool);
                }
            }

            if (hasError) {
                String errorFileId = UUID.randomUUID().toString();
                String errorFileName = "error_report_" + errorFileId + ".xlsx";
                String savePath = uploadDir + File.separator + "temp" + File.separator + errorFileName;

                File tempDir = new File(uploadDir + File.separator + "temp");
                if (!tempDir.exists()) tempDir.mkdirs();

                try (FileOutputStream fos = new FileOutputStream(savePath)) {
                    workbook.write(fos);
                }
                errorFileCache.put(errorFileId, savePath);

                result.put("success", false);
                result.put("message", "导入中断：发现 " + errorDetails.size() + " 行数据校验不通过。请下载报告修正后重新导入。");
                result.put("errorDetails", errorDetails);
                result.put("errorFileId", errorFileId);
            } else {
                // 执行入库
                int count = 0;
                for (ZjzkTool tool : validTools) {
                    toolMapper.insert(tool);
                    count++;
                }
                result.put("success", true);
                result.put("message", "成功导入 " + count + " 行数据！");
            }

        } catch (Exception e) {
            log.error("Excel导入异常", e);
            result.put("success", false);
            result.put("message", "文件解析异常: " + e.getMessage());
        } finally {
            if (workbook != null) {
                try { workbook.close(); } catch (IOException e) {}
            }
            if (is != null) {
                try { is.close(); } catch (IOException e) {}
            }
        }
        return result;
    }

    /**
     * 下载错误报告文件
     */
    public File getErrorFile(String fileId) {
        String path = errorFileCache.get(fileId);
        if (path == null) return null;
        return new File(path);
    }

    // --- Excel 辅助方法 ---
    private String getCellValue(Cell cell) {
        if (cell == null) return "";
        cell.setCellType(CellType.STRING);
        return cell.getStringCellValue().trim();
    }

    private boolean isEmpty(String str) {
        return str == null || str.trim().isEmpty();
    }

    private void markCellError(Cell cell, CellStyle style, String message) {
        cell.setCellStyle(style); // 标记红色背景
        // 添加批注
        Drawing<?> drawing = cell.getSheet().createDrawingPatriarch();
        // 批注位置
        ClientAnchor anchor = drawing.createAnchor(0, 0, 0, 0,
                cell.getColumnIndex(), cell.getRowIndex(),
                cell.getColumnIndex() + 2, cell.getRowIndex() + 2);
        Comment comment = drawing.createCellComment(anchor);
        comment.setString(new XSSFRichTextString(message));
        cell.setCellComment(comment);
    }

    // [新增] 日期转换辅助方法
    private Date parseDate(String dateStr) {
        if (dateStr == null || dateStr.trim().isEmpty()) return null;
        try {
            // 替换常见分隔符，统一尝试解析
            String fmtDate = dateStr.replace('/', '-').replace('.', '-');
            // 简单的尝试 yyyy-MM-dd
            return new SimpleDateFormat("yyyy-MM-dd").parse(fmtDate);
        } catch (Exception e) {
            // 如果解析失败，返回 null，或者可以尝试其他格式
            return null;
        }
    }

    @Autowired
    private TimsServiceClient timsClient;

    @Autowired
    private ZjzkTaskMemberMapper taskMemberMapper;


    /**
     * [重构] 自动任务生成 Job 入口
     * 调用存储过程生成 -> 获取结果 -> 推送 TIMS
     */
    @Transactional
    public void runAutoGenerationJob(String shiftName) {
        String batchNo = "AUTO_" + new SimpleDateFormat("yyyyMMddHHmmss").format(new Date()) + "_" + shiftName;

        log.info(">>> 启动自动生成任务流程, 批次号: {}, 班次: {}", batchNo, shiftName);

        // 1. 调用 Oracle 存储过程
        Map<String, Object> params = new HashMap<>();
        params.put("shiftName", shiftName);
        params.put("batchNo", batchNo);
        params.put("userName", "SYSTEM_JOB");
        params.put("outCount", 0);
        params.put("outMsg", "");

        try {
            taskMapper.callAutoGenTask(params);
        } catch (Exception e) {
            log.error("调用存储过程失败", e);
            throw new RuntimeException("存储过程执行异常: " + e.getMessage());
        }

        String msg = (String) params.get("outMsg");
        Integer count = (Integer) params.get("outCount");

        if (!"SUCCESS".equals(msg)) {
            log.error("存储过程返回错误: {}", msg);
            return;
        }

        if (count == null || count == 0) {
            log.info("本班次符合条件的设备数为 0，流程结束。");
            return;
        }

        log.info("生成成功，任务数: {}。开始推送 TIMS...", count);

        // 2. 根据批次号查出所有刚生成的任务
        List<ZjzkTask> newTasks = taskMapper.findByBatchNo(batchNo);

        // 3. 构造批量推送请求
        List<CreateSelfCheckTaskReq> timsReqList = new ArrayList<>();
        SimpleDateFormat timeFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        SimpleDateFormat dayFormat = new SimpleDateFormat("yyyy-MM-dd");

        for (ZjzkTask task : newTasks) {
            // [修正] 自动生成时，人员信息已经在存储过程中写入 ZJZK_TASK_MEMBER 表
            // 且 CHECKER 字段已存入拼接的姓名。
            // 虽然 DTO 不再接收 receivers，但我们依然可以检查是否有相关人员以确保业务完整性
            List<ZjzkTaskMember> members = taskMemberMapper.findByTaskId(task.getIndocno());
            boolean hasMembers = members != null && !members.isEmpty();

            if (!hasMembers) {
                log.warn("任务[{}]没有关联执行人员，跳过推送TIMS", task.getTaskNo());
                continue;
            }

            String content = dayFormat.format(task.getTaskTime()) + " " + task.getSfname() + " (三班电气) " + task.getShift();

            CreateSelfCheckTaskReq req = new CreateSelfCheckTaskReq();
            // [修正] 字段名适配: deviceCode -> equipmentCode
            req.setEquipmentCode(task.getSpmcode());
            // [修正] 类型转换: Long -> String
            req.setTaskId(String.valueOf(task.getIndocno()));
            req.setCreateTime(timeFormat.format(task.getCreateTime()));
            req.setContent(content);
            // [修正] 移除 setReceivers 调用，因为 DTO 中不存在该字段

            timsReqList.add(req);
        }

        // 4. 推送到 TIMS
        if (!timsReqList.isEmpty()) {
            try {
                timsClient.createSelfCheckTask(timsReqList);
                log.info("已向 TIMS 推送 {} 条待办任务。", timsReqList.size());
            } catch (Exception e) {
                log.error("推送 TIMS 失败 (不回滚任务生成)", e);
            }
        }
    }



}