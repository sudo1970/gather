package com.cq.accountmanger.controller;

import com.cq.accountmanger.service.UserService;
import com.cq.commons.dto.AccountManagerDTO;
import com.cq.commons.model.domain.ResultInfo;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;

@RestController
@RequestMapping("accountMange")
public class AccountMangeController {

    @Resource
    private UserService userService;

    @Resource
    private HttpServletRequest request;
    /**
     * 登录
     *
     * @param account 账号
     * @param password 密码
     * @return
     */
    @GetMapping("signin")
    public ResultInfo signIn(String account, String password) {
        return userService.signIn(account, password, request.getServletPath());
    }
    /**
     * 注册
     *
     * @param dinersDTO
     * @return
     */
    @PostMapping("register")
    public ResultInfo register(@RequestBody AccountManagerDTO dinersDTO) {
        return userService.register(dinersDTO, request.getServletPath());
    }
}
