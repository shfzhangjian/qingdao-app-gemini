package com.lucksoft.qingdao.qdjl.mapper;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;

import java.util.Map;

/**
 * 一个用于演示的 Mapper 接口.
 * 它位于新的 'com.lucksoft.qingdao.qdjl.mapper' 包下，将被 QdjlDataSourceConfig 扫描。
 * 您可以在此包下创建您所有需要访问 qdjl 数据源的 Mapper 接口。
 */
@Mapper
public interface QdjlPlaceholderMapper {

    /**
     * 测试与 qdjl 数据源的连接。
     * @return 一个包含测试信息 Map.
     */
    @Select("SELECT 'Hello from QDJL DataSource' as message FROM DUAL")
    Map<String, Object> testConnection();

}
