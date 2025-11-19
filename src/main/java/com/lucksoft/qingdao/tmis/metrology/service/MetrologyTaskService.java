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

    @Transactional
    public int updateTasks(UpdateTaskRequestDTO requestDTO) {
        if (requestDTO.getIds() == null || requestDTO.getIds().isEmpty()) {
            return 0;
        }

        // [修改] 移除本地 updateTask 调用，直接调用存储过程
        // int rows = metrologyTaskMapper.updateTask(requestDTO);
        // log.info("本地任务状态更新完成，影响行数: {}", rows);

        try {
            // 将 ID 列表转换为逗号分隔的字符串
            String idsStr = String.join(",", requestDTO.getIds());
            String remark = requestDTO.getAbnormalDesc() != null ? requestDTO.getAbnormalDesc() : requestDTO.getCheckRemark();

            log.info("正在调用存储过程 tmis.update_jl_task. IDs: {}, User: {}", idsStr, requestDTO.getLoginId());

            metrologyTaskMapper.callUpdateJlTaskProcedure(
                    idsStr,
                    requestDTO.getCheckResult(),
                    remark,
                    requestDTO.getLoginId(),
                    requestDTO.getUserName()
            );
            log.info("存储过程调用成功。");

            // 由于存储过程不直接返回影响行数，这里返回处理的ID数量作为近似值
            return requestDTO.getIds().size();

        } catch (Exception e) {
            log.error("调用存储过程 tmis.update_jl_task 失败", e);
            throw new RuntimeException("存储过程执行失败: " + e.getMessage());
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