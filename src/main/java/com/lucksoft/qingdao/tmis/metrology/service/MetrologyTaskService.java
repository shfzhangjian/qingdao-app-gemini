package com.lucksoft.qingdao.tmis.metrology.service;

import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import com.lucksoft.qingdao.qdjl.mapper.MetrologyTaskMapper;
import com.lucksoft.qingdao.tmis.dto.PageResult;
import com.lucksoft.qingdao.tmis.metrology.dto.MetrologyTaskDTO;
import com.lucksoft.qingdao.tmis.metrology.dto.TaskQuery;
import com.lucksoft.qingdao.tmis.metrology.dto.UpdateTaskRequestDTO;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class MetrologyTaskService {

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
        return metrologyTaskMapper.updateTask(requestDTO);
    }

    private void convertFilterParams(TaskQuery query) {
        Map<String, String> statusMap = new HashMap<>();
        statusMap.put("unchecked", "待检");
        statusMap.put("checked", "已检");
        // "abnormal" is a result status, not a plan status, so we don't map it here.
        // The query will filter by IDJSTATE.
        if (query.getTaskStatus() != null && statusMap.containsKey(query.getTaskStatus())) {
            query.setTaskStatus(statusMap.get(query.getTaskStatus()));
        }
    }
}
