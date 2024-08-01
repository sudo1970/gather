package com.cq.oauth2.server.mapper;

import com.cq.commons.model.pojo.AccountManager;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

/**
 * 客户经理 Mapper
 */
public interface AccountManagerMapper {

    // 根据用户名 or 手机号 or 邮箱查询用户信息
    @Select("select id, username, nickname, phone, email, " +
            "password, avatar_url, roles, is_valid from t_account_manager where " +
            "(username = #{account} or phone = #{account} or email = #{account})")
    AccountManager selectByAccountInfo(@Param("account") String account);
}
