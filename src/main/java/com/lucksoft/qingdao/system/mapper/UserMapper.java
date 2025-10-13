package com.lucksoft.qingdao.system.mapper;

import com.lucksoft.qingdao.system.entity.User;
import org.apache.ibatis.annotations.*;

import java.util.List;
import java.util.Map;

/**
 * 用户数据访问层接口 (使用注解)
 *
 * @author Gemini
 */
@Mapper
public interface UserMapper {

    /**
     * 定义数据库列名到Java实体属性的映射关系。
     * 这是注解方式的 <resultMap>
     */
    @Results(id = "userResultMap", value = {
            @Result(property = "id", column = "ID", id = true),
            @Result(property = "loginid", column = "LOGINID"),
            @Result(property = "passwd", column = "PASSWD"),
            @Result(property = "name", column = "NAME"),
            @Result(property = "email", column = "EMAIL"),
            @Result(property = "status", column = "STATUS"),
            @Result(property = "descn", column = "DESCN"),
            @Result(property = "vscopecode", column = "VSCOPECODE"),
            @Result(property = "vplantno", column = "VPLANTNO"),
            @Result(property = "itype", column = "ITYPE"),
            @Result(property = "vpyidex", column = "VPYIDEX"),
            @Result(property = "vutype", column = "VUTYPE"),
            @Result(property = "nqsxz", column = "NQSXZ"),
            @Result(property = "ngw", column = "NGW"),
            @Result(property = "gh", column = "GH"),
            @Result(property = "ngwname", column = "NGWNAME"),
            @Result(property = "zc", column = "ZC"),
            @Result(property = "zz", column = "ZZ"),
            @Result(property = "whcd", column = "WHCD"),
            @Result(property = "ccny", column = "CCNY"),
            @Result(property = "telephone", column = "TELEPHONE"),
            @Result(property = "sregid", column = "SREGID"),
            @Result(property = "sregnm", column = "SREGNM"),
            @Result(property = "dregt", column = "DREGT"),
            @Result(property = "smodid", column = "SMODID"),
            @Result(property = "smodnm", column = "SMODNM"),
            @Result(property = "dmodt", column = "DMODT"),
            @Result(property = "idel", column = "IDEL")
    })
    /**
     * 根据动态条件查询用户列表，并支持分页。
     * @param params 查询参数Map。
     * 可包含过滤条件如: name, loginid, status 等。
     * 可包含分页参数如: offset (起始位置), pageSize (每页数量)。
     * 如果不传入分页参数，则查询所有符合条件的记录。
     * @return 用户列表
     */
    @Select("<script>" +
            "SELECT * FROM (" +
            "    SELECT u.*, ROWNUM as rnum FROM (" +
            "        SELECT * FROM USERS" +
            "        <where>" +
            "            IDEL = 0" +
            "            <if test=\"params.name != null and params.name != ''\">" +
            "                AND NAME LIKE '%' || #{params.name} || '%'" +
            "            </if>" +
            "            <if test=\"params.loginid != null and params.loginid != ''\">" +
            "                AND LOGINID = #{params.loginid}" +
            "            </if>" +
            "            <if test=\"params.status != null\">" +
            "                AND STATUS = #{params.status}" +
            "            </if>" +
            "        </where>" +
            "        ORDER BY ID DESC" +
            "    ) u" +
            "    <if test=\"params.pageSize != null\">" +
            "        WHERE ROWNUM &lt;= #{params.offset} + #{params.pageSize}" +
            "    </if>" +
            ")" +
            "<if test=\"params.pageSize != null\">" +
            "    WHERE rnum &gt; #{params.offset}" +
            "</if>" +
            "</script>")
    List<User> findAll(@Param("params") Map<String, Object> params);

    /**
     * 根据动态条件查询用户总数。
     * @param params 查询参数Map，与findAll方法中的过滤条件一致。
     * @return 记录总数
     */
    @Select("<script>" +
            "SELECT count(*) FROM USERS" +
            "        <where>" +
            "            IDEL = 0" +
            "            <if test=\"params.name != null and params.name != ''\">" +
            "                AND NAME LIKE '%' || #{params.name} || '%'" +
            "            </if>" +
            "            <if test=\"params.loginid != null and params.loginid != ''\">" +
            "                AND LOGINID = #{params.loginid}" +
            "            </if>" +
            "            <if test=\"params.status != null\">" +
            "                AND STATUS = #{params.status}" +
            "            </if>" +
            "        </where>" +
            "</script>")
    long countAll(@Param("params") Map<String, Object> params);


    @ResultMap("userResultMap") // 复用上面定义的映射
    @Select("SELECT * FROM USERS WHERE ID = #{id} AND IDEL = 0")
    User findById(@Param("id") Long id);

    @ResultMap("userResultMap")
    @Select("SELECT * FROM USERS WHERE LOGINID = #{loginid} AND IDEL = 0")
    User findByLoginId(@Param("loginid") String loginid);

    /**
     * 注意: 对于Oracle的序列自增主键，注解方式相对繁琐。
     * 如果ID需要从序列获取，通常还是推荐在XML中使用 <selectKey>。
     * 这里的示例假设ID是手动设置或有其他机制处理。
     */
    @Insert("INSERT INTO USERS (ID, LOGINID, PASSWD, NAME, EMAIL, STATUS, DREGT, IDEL) " +
            "VALUES (#{id}, #{loginid}, #{passwd}, #{name}, #{email}, #{status}, SYSDATE, 0)")
    int insert(User user);

    /**
     * 使用 <script> 标签可以在注解中编写动态SQL
     */
    @Update("<script>" +
            "UPDATE USERS " +
            "<set>" +
            "<if test='loginid != null'>LOGINID = #{loginid},</if>" +
            "<if test='passwd != null'>PASSWD = #{passwd},</if>" +
            "<if test='name != null'>NAME = #{name},</if>" +
            "<if test='email != null'>EMAIL = #{email},</if>" +
            "<if test='status != null'>STATUS = #{status},</if>" +
            "DMODT = SYSDATE " +
            "</set>" +
            "WHERE ID = #{id}" +
            "</script>")
    int update(User user);

    /**
     * 逻辑删除
     */
    @Update("UPDATE USERS SET IDEL = 1 WHERE ID = #{id}")
    int deleteById(@Param("id") Long id);
}
