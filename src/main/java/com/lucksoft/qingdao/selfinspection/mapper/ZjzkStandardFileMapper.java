package com.lucksoft.qingdao.selfinspection.mapper;

import com.lucksoft.qingdao.selfinspection.entity.ZjzkStandardFile;
import org.apache.ibatis.annotations.*;

import java.util.List;

/**
 * 标准附件 Mapper
 * 对应表: ZJZK_STANDARD_FILE
 */
@Mapper
public interface ZjzkStandardFileMapper {

    // [修复] 添加 ResultMap 映射，将数据库的下划线字段映射到 Java 的驼峰属性
    @Results(id = "zjzkStandardFileMap", value = {
            @Result(property = "id", column = "ID", id = true),
            @Result(property = "fileName", column = "FILE_NAME"),
            @Result(property = "filePath", column = "FILE_PATH"),
            @Result(property = "uploadTime", column = "UPLOAD_TIME"),
            @Result(property = "uploader", column = "UPLOADER")
    })
    @Select("SELECT * FROM ZJZK_STANDARD_FILE ORDER BY UPLOAD_TIME DESC")
    List<ZjzkStandardFile> findAll();

    @ResultMap("zjzkStandardFileMap") // 复用上面的映射
    @Select("SELECT * FROM ZJZK_STANDARD_FILE WHERE ID = #{id}")
    ZjzkStandardFile findById(Long id);

    @Insert("INSERT INTO ZJZK_STANDARD_FILE (ID, FILE_NAME, FILE_PATH, UPLOAD_TIME, UPLOADER) " +
            "VALUES (SEQ_ZJZK_STANDARD_FILE.NEXTVAL, #{fileName}, #{filePath}, SYSDATE, #{uploader})")
    int insert(ZjzkStandardFile file);

    @Delete("DELETE FROM ZJZK_STANDARD_FILE WHERE ID = #{id}")
    int deleteById(Long id);
}