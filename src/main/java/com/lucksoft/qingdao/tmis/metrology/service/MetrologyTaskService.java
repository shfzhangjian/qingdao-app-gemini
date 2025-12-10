package com.lucksoft.qingdao.tmis.metrology.service;

import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import com.lucksoft.qingdao.qdjl.mapper.MetrologyTaskMapper;
import com.lucksoft.qingdao.tmis.dto.PageResult;
import com.lucksoft.qingdao.tmis.metrology.dto.MetrologyTaskDTO;
import com.lucksoft.qingdao.tmis.metrology.dto.TaskQuery;
import com.lucksoft.qingdao.tmis.metrology.dto.UpdateTaskRequestDTO;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class MetrologyTaskService {

    private static final Logger log = LoggerFactory.getLogger(MetrologyTaskService.class);

    private final MetrologyTaskMapper metrologyTaskMapper;

    public MetrologyTaskService(MetrologyTaskMapper metrologyTaskMapper) {
        this.metrologyTaskMapper = metrologyTaskMapper;
    }

    public PageResult<MetrologyTaskDTO> getTasksPage(TaskQuery query) {
        convertFilterParams(query);
        PageHelper.startPage(query.getPageNum(), query.getPageSize());
        List<MetrologyTaskDTO> list = metrologyTaskMapper.findTasksByCriteria(query);
        PageInfo<MetrologyTaskDTO> pageInfo = new PageInfo<>(list);
        return new PageResult<>(
                pageInfo.getList(),
                pageInfo.getPageNum(),
                pageInfo.getPageSize(),
                pageInfo.getTotal(),
                pageInfo.getPages()
        );
    }

    public List<MetrologyTaskDTO> getTasksForExport(TaskQuery query) {
        convertFilterParams(query);
        return metrologyTaskMapper.findTasksByCriteria(query);
    }

    /**
     * 统一的任务更新入口
     */
    @Transactional
    public int updateTasks(UpdateTaskRequestDTO requestDTO) {
        if (requestDTO.getIds() == null || requestDTO.getIds().isEmpty()) {
            return 0;
        }

        String idsStr = String.join(",", requestDTO.getIds());
        String checkResult = requestDTO.getCheckResult();
        String remark = requestDTO.getAbnormalDesc() != null ? requestDTO.getAbnormalDesc() : requestDTO.getCheckRemark();

        // [新增] 路由逻辑：如果是 "异常"，调用专门的异常提报过程
        if ("异常".equals(checkResult)) {
            log.info("检测到异常提报请求，调用 PKG_METROLOGY.P_REPORT_BATCH_EXCEPTION...");

            Map<String, Object> params = new HashMap<>();
            params.put("idsStr", idsStr);
            params.put("reason", remark); // 异常描述
            params.put("loginId", requestDTO.getLoginId());
            params.put("userName", requestDTO.getUserName());
            params.put("outCount", 0);
            params.put("outMsg", "");

            metrologyTaskMapper.callBatchReportException(params);

            String msg = (String) params.get("outMsg");
            if (!"SUCCESS".equals(msg)) {
                log.error("异常提报存储过程执行失败: {}", msg);
                throw new RuntimeException("操作失败: " + msg);
            }

            return (Integer) params.get("outCount");

        } else {
            // 正常的提交逻辑 (调用 tmis.update_jl_task)
            try {
                log.info("正在调用存储过程 tmis.update_jl_task. IDs: {}, User: {}", idsStr, requestDTO.getLoginId());

                metrologyTaskMapper.callUpdateJlTaskProcedure(
                        idsStr,
                        checkResult,
                        remark,
                        requestDTO.getLoginId(),
                        requestDTO.getUserName()
                );
                log.info("存储过程调用成功。");
                return requestDTO.getIds().size();

            } catch (Exception e) {
                log.error("调用存储过程 tmis.update_jl_task 失败", e);
                throw new RuntimeException("存储过程执行失败: " + e.getMessage());
            }
        }
    }

    private void convertFilterParams(TaskQuery query) {
        Map<String, String> statusMap = new HashMap<>();
        statusMap.put("unchecked", "待检");
        statusMap.put("checked", "已检");
        if (query.getTaskStatus() != null && statusMap.containsKey(query.getTaskStatus())) {
            query.setTaskStatus(statusMap.get(query.getTaskStatus()));
        }
    }
}