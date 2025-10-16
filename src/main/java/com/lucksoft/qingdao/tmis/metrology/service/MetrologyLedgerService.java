package com.lucksoft.qingdao.tmis.metrology.service;

import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import com.lucksoft.qingdao.qdjl.mapper.MetrologyLedgerMapper;
import com.lucksoft.qingdao.tmis.dto.PageResult;
import com.lucksoft.qingdao.tmis.metrology.dto.LedgerQuery;
import com.lucksoft.qingdao.tmis.metrology.dto.MetrologyLedgerDTO;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class MetrologyLedgerService {

    private final MetrologyLedgerMapper metrologyLedgerMapper;

    // 使用构造函数注入
    public MetrologyLedgerService(MetrologyLedgerMapper metrologyLedgerMapper) {
        this.metrologyLedgerMapper = metrologyLedgerMapper;
    }

    /**
     * 根据查询条件分页获取计量台账数据
     * @param query 查询条件，包含分页信息
     * @return 分页结果
     */
    public PageResult<MetrologyLedgerDTO> getLedgerPage(LedgerQuery query) {
        // 转换前端 'pills' 筛选器的值为数据库可识别的值
        convertFilterParams(query);

        // 启动分页
        PageHelper.startPage(query.getPageNum(), query.getPageSize());

        // 执行查询
        List<MetrologyLedgerDTO> list = metrologyLedgerMapper.findLedgerByCriteria(query);

        // 用 PageInfo 包装查询结果，以获取总记录数等信息
        PageInfo<MetrologyLedgerDTO> pageInfo = new PageInfo<>(list);

        // 转换为前端需要的 PageResult 格式
        return new PageResult<>(
                pageInfo.getList(),
                pageInfo.getPageNum(),
                pageInfo.getPageSize(),
                pageInfo.getTotal(),
                pageInfo.getPages()
        );
    }

    /**
     * 获取所有符合条件的台账数据（用于导出）
     * @param query 查询条件
     * @return 数据列表
     */
    public List<MetrologyLedgerDTO> getLedgerListForExport(LedgerQuery query) {
        convertFilterParams(query);
        return metrologyLedgerMapper.findLedgerByCriteria(query);
    }

    /**
     * 转换前端传入的筛选参数以匹配数据库中的值
     * @param query 查询对象
     */
    private void convertFilterParams(LedgerQuery query) {
        // 设备状态转换
        Map<String, String> statusMap = new HashMap<>();
        statusMap.put("normal", "在用");
        statusMap.put("repair", "维修");
        statusMap.put("scrapped", "报废");
        // ... 可根据需要添加其他状态映射
        if (query.getDeviceStatus() != null && statusMap.containsKey(query.getDeviceStatus())) {
            query.setDeviceStatus(statusMap.get(query.getDeviceStatus()));
        }

        // ABC 分类，前端传来的已经是大写 A, B, C，无需转换
    }
}
