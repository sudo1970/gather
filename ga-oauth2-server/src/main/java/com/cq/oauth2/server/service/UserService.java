package com.cq.oauth2.server.service;

import cn.hutool.json.JSONUtil;
import com.cq.commons.model.domain.SignInIdentity;
import com.cq.commons.model.pojo.AccountManager;
import com.cq.commons.utils.AssertUtil;
import com.cq.oauth2.server.mapper.AccountManagerMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;

/**
 * 登录校验--添加UserService实现UserDetailsService接口，用于加载用户信息
 */
@Slf4j
@Service
public class UserService implements UserDetailsService {
    @Resource
    private AccountManagerMapper accountManagerMapper;

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        AssertUtil.isNotEmpty(username, "请输入用户名");
        AccountManager accountManager = accountManagerMapper.selectByAccountInfo(username);
        if (accountManager == null) {
            throw new UsernameNotFoundException("用户名或者密码错误，请重新输入");
        }
        log.info("accountManager:::{}", JSONUtil.toJsonStr(accountManager));
        // 初始化登录认证对象
        SignInIdentity signInIdentity = new SignInIdentity();

        // 拷贝属性
        BeanUtils.copyProperties(accountManager, signInIdentity);
        return signInIdentity;
    }
}
