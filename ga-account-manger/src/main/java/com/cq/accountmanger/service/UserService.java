package com.cq.accountmanger.service;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.crypto.digest.DigestUtil;
import com.cq.commons.constant.ApiConstant;
import com.cq.commons.dto.AccountManagerDTO;
import com.cq.commons.model.domain.ResultInfo;
import com.cq.commons.model.pojo.AccountManager;
import com.cq.commons.utils.AssertUtil;
import com.cq.commons.utils.ResultInfoUtil;
import com.cq.accountmanger.config.OAuth2ClientConfiguration;
import com.cq.accountmanger.domain.OAuthUserInfo;
import com.cq.accountmanger.mapper.UserMapper;
import com.cq.accountmanger.vo.LoginUserInfo;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.http.client.support.BasicAuthenticationInterceptor;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;

import javax.annotation.Resource;
import java.util.LinkedHashMap;

@Slf4j
@Service
public class UserService {
    @Resource
    private RestTemplate restTemplate;
    @Value("${service.name.ga-oauth-server}")
    private String oauthServerName;
    @Resource
    private OAuth2ClientConfiguration oAuth2ClientConfiguration;
    @Resource
    private SendVerifyCodeService sendVerifyCodeService;
    
    @Resource
    private UserMapper userMapper;

    /**
     * 注册
     * @param AccountManagerDTO
     * @param path
     * @return
     */
    public ResultInfo register(AccountManagerDTO AccountManagerDTO, String path) {
        // 参数非空校验
        String username = AccountManagerDTO.getUsername();
        AssertUtil.isNotEmpty(username, "请输入用户名");
        String password = AccountManagerDTO.getPassword();
        AssertUtil.isNotEmpty(password, "请输入密码");
        String phone = AccountManagerDTO.getPhone();
        AssertUtil.isNotEmpty(phone, "请输入手机号");
        String verifyCode = AccountManagerDTO.getVerifyCode();
        AssertUtil.isNotEmpty(verifyCode, "请输入验证码");
        // 获取验证码
        String code = sendVerifyCodeService.getCodeByPhone(phone);
        // 验证是否过期
        AssertUtil.isNotEmpty(code, "验证码已过期，请重新发送");
        // 验证码一致性校验
        AssertUtil.isTrue(!AccountManagerDTO.getVerifyCode().equals(code), "验证码不一致，请重新输入");
        // 验证用户名是否已注册
        AccountManager accountManager = userMapper.selectByUsername(username.trim());
        AssertUtil.isTrue(accountManager != null, "用户名已存在，请重新输入");
        // 注册
        // 密码加密
        AccountManagerDTO.setPassword(DigestUtil.md5Hex(password.trim()));
        userMapper.save(AccountManagerDTO);
        // 自动登录
        return signIn(username.trim(), password.trim(), path);
    }

    /**
     * 登录
     * 访问oauth2的auth/token接口获取令牌
     *
     * @param account 账号名称
     * @param password 账号密码
     * @param path 请求路径
     * @return
     */
    public ResultInfo signIn(String account, String password, String path) {
        // 参数校验
        AssertUtil.isNotEmpty(account, "请输入登录账号");
        AssertUtil.isNotEmpty(password, "请输入登录密码");
        // 构建请求头
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);
        // 构建请求头（请求参数）
        MultiValueMap<String, Object> body = new LinkedMultiValueMap<>();
        body.add("username", account);
        body.add("password", password);
        body.setAll(BeanUtil.beanToMap(oAuth2ClientConfiguration));
        HttpEntity<MultiValueMap<String, Object>> entity = new HttpEntity<>(body, headers);
        // 设置 Authorization
        restTemplate.getInterceptors().add(new BasicAuthenticationInterceptor(oAuth2ClientConfiguration.getClientId(),
                oAuth2ClientConfiguration.getSecret()));
        // 发送请求
        ResponseEntity<ResultInfo> result = restTemplate.postForEntity(oauthServerName + "oauth/token", entity, ResultInfo.class);
        // 处理返回结果
        AssertUtil.isTrue(result.getStatusCode() != HttpStatus.OK, "登录失败");
        ResultInfo resultInfo = result.getBody();
        if (resultInfo.getCode() != ApiConstant.SUCCESS_CODE) {
            // 登录失败
            resultInfo.setData(resultInfo.getMessage());
            return resultInfo;
        }
        OAuthUserInfo dinerInfo = BeanUtil.fillBeanWithMap((LinkedHashMap)resultInfo.getData(),
                new OAuthUserInfo(), false);
        // 根据业务需求返回视图对象
        LoginUserInfo loginUserInfo = new LoginUserInfo();
        loginUserInfo.setToken(dinerInfo.getAccessToken());
        loginUserInfo.setAvatarUrl(dinerInfo.getAvatarUrl());
        loginUserInfo.setNickname(dinerInfo.getNickname());
        return ResultInfoUtil.buildSuccess(path, loginUserInfo);
    }
}
