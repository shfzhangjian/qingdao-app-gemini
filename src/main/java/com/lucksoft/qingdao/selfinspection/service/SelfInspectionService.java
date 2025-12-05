package com.lucksoft.qingdao.selfinspection.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import com.lucksoft.qingdao.selfinspection.dto.DeviceKeyDto;
import com.lucksoft.qingdao.selfinspection.dto.GenerateTaskReq;
import com.lucksoft.qingdao.selfinspection.entity.*;
import com.lucksoft.qingdao.selfinspection.mapper.*;
import com.lucksoft.qingdao.system.entity.User;
import com.lucksoft.qingdao.tmis.dto.PageResult;
import com.lucksoft.qingdao.tspm.dto.tims.CreateSelfCheckTaskReq;
import com.lucksoft.qingdao.tspm.dto.tims.GetAvgSpeedReq;
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
import java.util.stream.Collectors;

/**
 * 自检自控业务逻辑服务 (重构版)
 */
@Service
public class SelfInspectionService {

    private static final Logger log = LoggerFactory.getLogger(SelfInspectionService.class);

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

    @Autowired
    private ZjzkTaskMemberMapper taskMemberMapper;

    @Autowired
    private TimsServiceClient timsClient;

    @Autowired
    private ObjectMapper objectMapper; // Jackson

    // ==========================================
    // 任务管理 (Generation) - 新增逻辑
    // ==========================================

    @Autowired
    private ZjzkSpeedCheckMapper speedCheckMapper; // [新增]

    // 定义需要检查车速的设备列表
    private static final List<String> SPEED_CHECK_DEVICES = Arrays.asList("JB000483", "JB000484", "JB000488", "JB000487");


    /**
     * [新增] 获取车速并记录到 ZJZK_SPEED_CHECK 表
     */
    private void checkAndRecordSpeed(String batchNo) {
        Date now = new Date();
        // startTime = now - 11 minutes
        Date startTime = new Date(now.getTime() - 11 * 60 * 1000);

        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        String startStr = sdf.format(startTime);
        String endStr = sdf.format(now);

        List<Map<String, Object>> records = new ArrayList<>();

        for (String spmcode : SPEED_CHECK_DEVICES) {
            try {
                GetAvgSpeedReq req = new GetAvgSpeedReq();
                req.setEquipmentCode(spmcode);
                req.setStartTime(startStr);
                req.setEndTime(endStr);

                // 调用 TIMS 接口 (接口7)
                Double avgSpeed = timsClient.getAverageSpeed(req);

                if (avgSpeed != null) {
                    Map<String, Object> record = new HashMap<>();
                    record.put("batchNo", batchNo);
                    record.put("spmcode", spmcode);
                    record.put("avgSpeed", avgSpeed);
                    record.put("startTime", startTime);
                    record.put("endTime", now);
                    records.add(record);

                    log.info("设备[{}]车速检测: {} (Batch: {})", spmcode, avgSpeed, batchNo);
                }
            } catch (Exception e) {
                // 接口调用失败不阻断流程，但该设备可能因无数据在存储过程中被过滤
                log.error("获取设备[{}]车速失败: {}", spmcode, e.getMessage());
            }
        }

        if (!records.isEmpty()) {
            speedCheckMapper.batchInsert(records);
        }
    }

    /**
     * [重构] 自动任务生成 Job 入口
     * 调用存储过程生成 -> 获取结果 -> 推送 TIMS
     */
    @Transactional
    public void runAutoGenerationJob(String shiftName) {
        String batchNo = "AUTO_" + new SimpleDateFormat("yyyyMMddHHmmss").format(new Date()) + "_" + shiftName;

        log.info(">>> 启动自动生成任务流程, 批次号: {}, 班次: {}", batchNo, shiftName);

        // 1. 自动生成时，可能需要预先检查车速 (如果是特定设备)
        checkAndRecordSpeed(batchNo); // 如果需要开启车速检查请取消注释并确保相关表存在

        // 2. 调用 Oracle 存储过程 (P_AUTO_GEN_SHIFT_TASKS)
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

        handleTaskGenerationResult(params, batchNo);
    }

    /**
     * 生成任务 (简单版，仅用于手动生成)
     */
    @Transactional
    public void generateTasks(GenerateTaskReq req) {
        generateTasksWithChecker(req, null);
    }

    /**
     * [修复] 生成任务，支持指定人员列表 (用于自动任务或手动带人)
     * 关键修复：将请求转换为 XML 格式字符串，而不是 JSON，以匹配 Oracle 11g 存储过程的要求。
     */
    @Transactional
    public void generateTasksWithChecker(GenerateTaskReq req, Map<String, List<Map<String, String>>> deviceUserMap) {
        if (req.getSelectedDevices() == null || req.getSelectedDevices().isEmpty()) {
            if (deviceUserMap == null) throw new RuntimeException("未选择任何设备");
            else return;
        }

        String batchNo = "MANUAL_" + new SimpleDateFormat("yyyyMMddHHmmss").format(new Date()) + "_" + UUID.randomUUID().toString().substring(0, 4);
        SimpleDateFormat dayFormat = new SimpleDateFormat("yyyy-MM-dd");

        // 1. [Fix] 构建 XML 数据结构 (替代 JSON)
        // 存储过程 P_MANUAL_GEN_TASK 使用 XMLTYPE 解析
        StringBuilder xml = new StringBuilder();
        xml.append("<root>");
        xml.append("<batchNo>").append(escapeXml(batchNo)).append("</batchNo>");
        xml.append("<taskTime>").append(dayFormat.format(req.getTaskTime())).append("</taskTime>");
        xml.append("<taskType>").append(escapeXml(req.getTaskType())).append("</taskType>");
        xml.append("<prodStatus>").append(escapeXml(req.getProdStatus())).append("</prodStatus>");
        xml.append("<shiftType>").append(escapeXml(req.getShiftType())).append("</shiftType>");
        xml.append("<shift>").append(escapeXml(req.getShift())).append("</shift>");

        xml.append("<devices>");
        for (DeviceKeyDto deviceKey : req.getSelectedDevices()) {
            String spmCode = deviceKey.getSpmcode();
            // 简单处理：checkerName 拼接，users 列表嵌套
            List<Map<String, String>> users = (deviceUserMap != null) ? deviceUserMap.get(spmCode) : null;
            String mainCheckerName = "";
            if (users != null && !users.isEmpty()) {
                mainCheckerName = users.stream().map(u -> u.get("name")).collect(Collectors.joining(","));
            }

            xml.append("<device>");
            xml.append("<spmcode>").append(escapeXml(spmCode)).append("</spmcode>");
            xml.append("<checkerName>").append(escapeXml(mainCheckerName)).append("</checkerName>");

            xml.append("<users>");
            if (users != null) {
                for (Map<String, String> u : users) {
                    xml.append("<user>");
                    xml.append("<code>").append(escapeXml(u.get("code"))).append("</code>");
                    xml.append("<name>").append(escapeXml(u.get("name"))).append("</name>");
                    xml.append("</user>");
                }
            }
            xml.append("</users>");

            xml.append("</device>");
        }
        xml.append("</devices>");
        xml.append("</root>");

        // 2. 调用存储过程
        try {
            log.info("调用人工生成任务存储过程(XML), BatchNo: {}", batchNo);
            // log.debug("XML Payload: {}", xml.toString()); // 调试用

            Map<String, Object> params = new HashMap<>();
            // 注意：Mapper 中参数名为 jsonParams (为了兼容性保留 key 名)，但传的值是 XML
            params.put("jsonParams", xml.toString());
            params.put("outCount", 0);
            params.put("outMsg", "");

            taskMapper.callManualGenTask(params);

            handleTaskGenerationResult(params, batchNo);

        } catch (Exception e) {
            log.error("任务生成失败", e);
            throw new RuntimeException("任务生成失败: " + e.getMessage());
        }
    }

    /**
     * XML 转义辅助方法 (防止特殊字符破坏 XML 结构)
     */
    private String escapeXml(String val) {
        if (val == null) return "";
        return val.replace("&", "&amp;")
                .replace("<", "&lt;")
                .replace(">", "&gt;")
                .replace("\"", "&quot;")
                .replace("'", "&apos;");
    }

    /**
     * 处理生成结果：检查返回值，查询生成任务，推送到 TIMS
     */
    private void handleTaskGenerationResult(Map<String, Object> params, String batchNo) {
        String msg = (String) params.get("outMsg");
        Integer count = (Integer) params.get("outCount");

        if (!"SUCCESS".equals(msg)) {
            log.error("存储过程返回错误: {}", msg);
            throw new RuntimeException("数据库生成失败: " + msg);
        }

        if (count == null || count == 0) {
            log.info("批次 {} 未生成任何任务 (可能已存在或无符合条件数据)。", batchNo);
            return;
        }

        log.info("批次 {} 生成成功，任务数: {}。开始推送 TIMS...", batchNo, count);

        // 3. 根据批次号查出所有刚生成的任务
        List<ZjzkTask> newTasks = taskMapper.findByBatchNo(batchNo);

        // 4. 构造批量推送请求
        List<CreateSelfCheckTaskReq> timsReqList = new ArrayList<>();
        SimpleDateFormat timeFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        SimpleDateFormat dayFormat = new SimpleDateFormat("yyyy-MM-dd");

        for (ZjzkTask task : newTasks) {
            // 从数据库获取人员工号
            List<ZjzkTaskMember> members = taskMemberMapper.findByTaskId(task.getIndocno());

            // 即使没有特定人员，也要推送待办（可能分配给班组）
            String content = dayFormat.format(task.getTaskTime()) + " " + task.getSfname() + " (三班电气) " + task.getShift();

            // 如果有具体人员，为每个人生成一条请求（TIMS接口目前是按人头还是按任务？假设 CreateSelfCheckTaskReq 是一条任务，TIMS负责分发）
            // 根据 TimsServiceClient，它接受 List<Req>。如果 req 里没 receivers，那怎么发？
            // 之前的 CreateSelfCheckTaskReq DTO 被回滚为无 receivers 字段。
            // 这意味着：要么 TIMS 接口根据 equipmentCode 自动找人，要么我们需要为每个人构造一个 Req 对象。
            // 假设需要为每个人构造一个 Req (因为 Req 里没有 List<Receiver>)

            if (members != null && !members.isEmpty()) {
                // 场景A: 为每个人推送一条 (如果TIMS不支持单条任务多接收人)
                // 但通常 taskId 是一样的，重复推可能会报错。
                // 回看 `CreateSelfCheckTaskReq`，它确实很简单。
                // 假设我们只推一次，TIMS 自己去关联班组/机台人员。
                // 或者，如果必须指定人，那之前的 DTO 修改是必要的。但既然被驳回，我们按现有逻辑：只推任务信息。

                CreateSelfCheckTaskReq req = new CreateSelfCheckTaskReq();
                req.setEquipmentCode(task.getSpmcode());
                req.setTaskId(String.valueOf(task.getIndocno()));
                req.setCreateTime(timeFormat.format(task.getCreateTime()));
                req.setContent(content);
                timsReqList.add(req);

                // *注*：如果 TIMS 真的需要显式接收人，而 Req 对象里没有字段，那说明 TIMS 设计上是“认设备不认人”或者“去各自系统认领”。
                // 我们这里只负责把任务“抛”过去。

                // 为防止重复添加 (虽然这个循环是按 Task 来的)，Break 并不是必须，因为这里只添加一次。
            } else {
                // 没人的情况也推一下试试
                CreateSelfCheckTaskReq req = new CreateSelfCheckTaskReq();
                req.setEquipmentCode(task.getSpmcode());
                req.setTaskId(String.valueOf(task.getIndocno()));
                req.setCreateTime(timeFormat.format(task.getCreateTime()));
                req.setContent(content);
                timsReqList.add(req);
            }
        }

        // 5. 推送到 TIMS
        if (!timsReqList.isEmpty()) {
            try {
                timsClient.createSelfCheckTask(timsReqList);
                log.info("已向 TIMS 推送 {} 条待办任务。", timsReqList.size());
            } catch (Exception e) {
                log.error("推送 TIMS 失败 (不回滚任务生成)", e);
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
    // 统计查询 (SiStats)
    // ==========================================

    public PageResult<Map<String, Object>> getStatsPage(Map<String, Object> params) {
        int pageNum = Integer.parseInt(params.getOrDefault("pageNum", "1").toString());
        int pageSize = Integer.parseInt(params.getOrDefault("pageSize", "20").toString());

        PageHelper.startPage(pageNum, pageSize);
        // 调用联表查询方法
        List<Map<String, Object>> list = taskDetailMapper.findStatsList(params);
        PageInfo<Map<String, Object>> pageInfo = new PageInfo<>(list);

        return new PageResult<>(pageInfo.getList(), pageInfo.getPageNum(), pageInfo.getPageSize(), pageInfo.getTotal(), pageInfo.getPages());
    }

    public List<Map<String, Object>> getStatsListForExport(Map<String, Object> params) {
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

    public List<ZjzkTool> getLedgerListForExport(Map<String, Object> params) {
        return toolMapper.findList(params);
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

    @Transactional
    public void importLedger(MultipartFile file) {
        try (Workbook workbook = WorkbookFactory.create(file.getInputStream())) {
            Sheet sheet = workbook.getSheetAt(0);
            if (sheet == null) throw new RuntimeException("Excel 文件为空");

            for (int i = 1; i <= sheet.getLastRowNum(); i++) {
                Row row = sheet.getRow(i);
                if (row == null) continue;

                ZjzkTool tool = new ZjzkTool();
                tool.setSdept(getCellValue(row.getCell(0)));
                tool.setSname(getCellValue(row.getCell(1)));
                tool.setSjx(getCellValue(row.getCell(2)));
                tool.setSfname(getCellValue(row.getCell(3)));
                tool.setSbname(getCellValue(row.getCell(4)));
                tool.setSpmcode(getCellValue(row.getCell(5)));
                tool.setSazwz(getCellValue(row.getCell(6)));
                tool.setScj(getCellValue(row.getCell(7)));
                tool.setSxh(getCellValue(row.getCell(8)));
                tool.setSyl(getCellValue(row.getCell(9)));
                tool.setSddno(getCellValue(row.getCell(10)));
                tool.setSzcno(getCellValue(row.getCell(11)));
                tool.setDtime(parseDate(getCellValue(row.getCell(12))));
                tool.setSsm(getCellValue(row.getCell(13)));

                if (isEmpty(tool.getSpmcode()) || isEmpty(tool.getSname())) {
                    continue;
                }

                List<ZjzkTool> existing = toolMapper.findByUniqueKey(tool);
                if (existing != null && !existing.isEmpty()) {
                    ZjzkTool exist = existing.get(0);
                    tool.setIndocno(exist.getIndocno());
                    tool.setSstepstate(exist.getSstepstate());
                    toolMapper.update(tool);
                } else {
                    tool.setSstepstate("草稿");
                    toolMapper.insert(tool);
                }
            }
        } catch (IOException e) {
            throw new RuntimeException("Excel 解析失败: " + e.getMessage());
        }
    }

    private String getCellValue(Cell cell) {
        if (cell == null) return "";
        DataFormatter formatter = new DataFormatter();
        return formatter.formatCellValue(cell).trim();
    }

    private Date parseDate(String dateStr) {
        if (dateStr == null || dateStr.trim().isEmpty()) return null;
        try {
            String fmtDate = dateStr.replace('/', '-').replace('.', '-');
            return new SimpleDateFormat("yyyy-MM-dd").parse(fmtDate);
        } catch (Exception e) {
            return null;
        }
    }

    private boolean isEmpty(String str) {
        return str == null || str.trim().isEmpty();
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


    // 缓存生成的错误报告文件路径，key为UUID
    private final Map<String, String> errorFileCache = new ConcurrentHashMap<>();


    /**
     * 下载错误报告文件
     */
    public File getErrorFile(String fileId) {
        String path = errorFileCache.get(fileId);
        if (path == null) return null;
        return new File(path);
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

}