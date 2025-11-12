package com.lucksoft.qingdao.oracle.mapper;

import com.lucksoft.qingdao.oracle.dto.VFaultReportCodeDTO;
import org.apache.ibatis.annotations.*;

import java.util.List;

/**
 * [新] MyBatis Mapper 接口
 * 专门用于查询 V_TMIS_REPORT_CODE 视图 (对应接口 12)。
 * 此 Mapper 假设连接的是主数据源 (PrimaryDataSource)。
 */
@Mapper
public interface VFaultReportCodeMapper {

    /**
     * 根据 TIMS ID 查询故障报告编码。
     * 视图 V_TMIS_REPORT_CODE 应该已经按最新的日期过滤。
     *
     * @param timsId TIMS系统报告数据记录主键
     * @return VFaultReportCodeDTO 对象，如果未找到则为 null
     */
    @Results(id = "vFaultReportCodeResultMap", value = {
            @Result(property = "type", column = "type"),
            @Result(property = "id", column = "id"),
            @Result(property = "code", column = "code")
    })
    @Select("SELECT * FROM V_TMIS_REPORT_CODE WHERE \"id\" = #{timsId}")
    VFaultReportCodeDTO findByTimsId(@Param("timsId") Integer timsId);

}