package com.cq.accountmanger.mapper;

import com.cq.commons.dto.AccountManagerDTO;
import com.cq.commons.model.pojo.AccountManager;
import com.cq.commons.model.vo.ShortAccountManagerInfo;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

/**
 * 用户 Mapper
 */
public interface UserMapper {

    // 根据手机号查询用户信息
    @Select("select id, username, phone, email, is_valid " +
            " from t_account_manager where phone = #{phone}")
    AccountManager selectByPhone(@Param("phone") String phone);

    // 根据用户名查询用户信息
    @Select("select id, username, phone, email, is_valid " +
            " from t_account_manager where username = #{username}")
    AccountManager selectByUsername(@Param("username") String username);

    // 新增用户信息
    @Insert("insert into " +
            " t_account_manager (username, password, phone, roles, is_valid, create_date, update_date) " +
            " values (#{username}, #{password}, #{phone}, \"ROLE_USER\", 1, now(), now())")
    int save(AccountManagerDTO AccountManagerDTO);

    // 根据 ID 集合查询多个用户信息
    @Select("<script> " +
            " select id, nickname, avatar_url from t_account_manager " +
            " where is_valid = 1 and id in " +
            " <foreach item=\"id\" collection=\"ids\" open=\"(\" separator=\",\" close=\")\"> " +
            "   #{id} " +
            " </foreach> " +
            " </script>")
    List<ShortAccountManagerInfo> findByIds(@Param("ids") String[] ids);

}
