package com.lucksoft.qingdao.selfinspection.mapper;

import com.lucksoft.qingdao.selfinspection.entity.SiStandard;
import org.apache.ibatis.annotations.*;

import java.util.List;

/**
 * 点检标准明细 Mapper
 * 对应表: T_SI_STANDARD
 */
@Mapper
public interface SiStandardMapper {

    @Select("SELECT ID, LEDGER_ID, DEVICE_PART, ITEM_NAME, STANDARD_DESC, EXECUTOR_ROLE, CHECK_CYCLE, NEXT_EXEC_DATE " +
            "FROM T_SI_STANDARD WHERE LEDGER_ID = #{ledgerId} ORDER BY ID ASC")
    @Results({
            @Result(property = "id", column = "ID", id = true),
            @Result(property = "ledgerId", column = "LEDGER_ID"),
            @Result(property = "devicePart", column = "DEVICE_PART"),
            @Result(property = "itemName", column = "ITEM_NAME"),
            @Result(property = "standardDesc", column = "STANDARD_DESC"),
            @Result(property = "executorRole", column = "EXECUTOR_ROLE"),
            @Result(property = "checkCycle", column = "CHECK_CYCLE"),
            @Result(property = "nextExecDate", column = "NEXT_EXEC_DATE")
    })
    List<SiStandard> findByLedgerId(Long ledgerId);

    @Delete("DELETE FROM T_SI_STANDARD WHERE LEDGER_ID = #{ledgerId}")
    int deleteByLedgerId(Long ledgerId);

    @Insert("<script>" +
            "INSERT INTO T_SI_STANDARD (ID, LEDGER_ID, DEVICE_PART, ITEM_NAME, STANDARD_DESC, EXECUTOR_ROLE, CHECK_CYCLE) " +
            "SELECT SEQ_SI_STANDARD.NEXTVAL, A.* FROM (" +
            "<foreach collection='list' item='item' separator='UNION ALL'>" +
            " SELECT #{item.ledgerId}, #{item.devicePart}, #{item.itemName}, #{item.standardDesc}, #{item.executorRole}, #{item.checkCycle} FROM DUAL" +
            "</foreach>" +
            ") A" +
            "</script>")
    int batchInsert(@Param("list") List<SiStandard> list);
}