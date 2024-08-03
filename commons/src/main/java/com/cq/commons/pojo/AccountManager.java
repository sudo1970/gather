package com.cq.commons.pojo;


import com.cq.commons.model.base.BaseModel;
import lombok.Getter;
import lombok.Setter;

/**
 * 客户经理实体类
 */
@Getter
@Setter
public class AccountManager extends BaseModel {

    // 主键
    private Integer id;
    // 用户名
    private String username;
    // 昵称
    private String nickname;
    // 密码
    private String password;
    // 手机号
    private String phone;
    // 邮箱
    private String email;
    // 头像
    private String avatarUrl;
    // 角色
    private String roles;

}