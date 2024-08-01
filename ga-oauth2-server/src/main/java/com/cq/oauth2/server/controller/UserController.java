package com.cq.oauth2.server.controller;

import com.cq.commons.model.domain.ResultInfo;
import com.cq.commons.model.domain.SignInIdentity;
import com.cq.commons.model.vo.SignInAcountManagerInfo;
import com.cq.commons.utils.ResultInfoUtil;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.common.OAuth2AccessToken;
import org.springframework.security.oauth2.common.OAuth2RefreshToken;
import org.springframework.security.oauth2.provider.token.store.redis.RedisTokenStore;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import java.util.Locale;

/**
 * 用户中心
 */
@RestController
public class UserController {
    @Resource
    private HttpServletRequest request;

    @Resource
    private RedisTokenStore redisTokenStore;

    @GetMapping("user/me")
    public ResultInfo getCurrentUser(Authentication authentication) {
        // 获取登录用户的信息，然后设置
        SignInIdentity signInIdentity = (SignInIdentity) authentication.getPrincipal();
        // 转为前端可用的视图对象
        SignInAcountManagerInfo dinerInfo = new SignInAcountManagerInfo();
        BeanUtils.copyProperties(signInIdentity, dinerInfo);
        return ResultInfoUtil.buildSuccess(request.getServletPath(), dinerInfo);
    }

    @GetMapping("user/logout")
    public ResultInfo logout(String access_token, String authorization) {
        // 判断access_token是否为空，为空将authorization赋值给access_token
        if (StringUtils.isBlank(access_token)) {
            access_token = authorization;
        }
        // 判断authorization是否为空
        if (StringUtils.isBlank(access_token)) {
            return ResultInfoUtil.buildSuccess(request.getServletPath(), "退出成功");
        }
        // 判断bearer token是否为空
        if (access_token.toLowerCase(Locale.ROOT).contains("bearer ".toLowerCase())) {
            access_token = access_token.toLowerCase(Locale.ROOT).replace("bearer ", "");
        }
        // 清除redis token 信息
        OAuth2AccessToken oAuth2AccessToken = redisTokenStore.readAccessToken(access_token);
        if (oAuth2AccessToken != null) {
            redisTokenStore.removeAccessToken(oAuth2AccessToken);
            OAuth2RefreshToken refreshToken = oAuth2AccessToken.getRefreshToken();
            redisTokenStore.removeRefreshToken(refreshToken);
        }
        return ResultInfoUtil.buildSuccess(request.getServletPath(), "退出成功");
    }

}
