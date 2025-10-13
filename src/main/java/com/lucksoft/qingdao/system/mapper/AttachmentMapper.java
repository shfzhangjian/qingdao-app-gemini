package com.lucksoft.qingdao.system.mapper;

import com.lucksoft.qingdao.system.entity.Attachment;
import org.apache.ibatis.annotations.*;

import java.util.List;

/**
 * 附件数据访问层接口 (使用注解)
 *
 * @author Gemini
 */
@Mapper
public interface AttachmentMapper {

    @Results(id = "attachmentResultMap", value = {
            @Result(property = "attId", column = "ATT_ID", id = true),
            @Result(property = "stitle", column = "STITLE"),
            @Result(property = "spath", column = "SPATH"),
            @Result(property = "docno", column = "DOCNO"),
            @Result(property = "doctype", column = "DOCTYPE"),
            @Result(property = "attState", column = "ATT_STATE"),
            @Result(property = "attType", column = "ATT_TYPE"),
            @Result(property = "doctypeid", column = "DOCTYPEID"),
            @Result(property = "attSize", column = "ATT_SIZE"),
            @Result(property = "sversion", column = "SVERSION"),
            @Result(property = "snotes", column = "SNOTES"),
            @Result(property = "created", column = "CREATED"),
            @Result(property = "sregid", column = "SREGID"),
            @Result(property = "sregnm", column = "SREGNM")
    })
    @Select("SELECT * FROM ATTACHMENT WHERE DOCNO = #{docno} AND DOCTYPE = #{doctype} AND ATT_STATE = 0")
    List<Attachment> findByDoc(@Param("docno") Long docno, @Param("doctype") String doctype);

    @ResultMap("attachmentResultMap")
    @Select("SELECT * FROM ATTACHMENT WHERE ATT_ID = #{id} AND ATT_STATE = 0")
    Attachment findById(@Param("id") Long id);

    @Insert("INSERT INTO ATTACHMENT (ATT_ID, STITLE, SPATH, DOCNO, DOCTYPE, ATT_STATE, ATT_TYPE, DOCTYPEID, ATT_SIZE, SVERSION, SNOTES, CREATED, SREGID, SREGNM) " +
            "VALUES (#{attId}, #{stitle}, #{spath}, #{docno}, #{doctype}, 0, #{attType}, #{doctypeid}, #{attSize}, #{sversion}, #{snotes}, SYSDATE, #{sregid}, #{sregnm})")
    int insert(Attachment attachment);

    // 逻辑删除
    @Update("UPDATE ATTACHMENT SET ATT_STATE = 1 WHERE ATT_ID = #{id}")
    int deleteById(@Param("id") Long id);
}
