package com.cq.seckill.controller;

import com.cq.commons.model.domain.ResultInfo;
import com.cq.commons.pojo.SeckillVouchers;
import com.cq.commons.utils.ResultInfoUtil;
import com.cq.seckill.service.SeckillService;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;

/**
 * 秒杀控制层
 */
@RestController
public class SeckillController {

    @Resource
    private SeckillService seckillService;
    @Resource
    private HttpServletRequest request;

    /**
     * 秒杀下单
     *
     * @param voucherId
     * @param access_token
     * @return
     */
    @PostMapping("seckill/doSeckill/{voucherId}")
    public ResultInfo<String> doSeckill(@PathVariable Integer voucherId, String access_token) {
        ResultInfo resultInfo = seckillService.doSeckill(voucherId, access_token, request.getServletPath());
        return resultInfo;
    }

    /**
     * 新增秒杀活动
     *
     * @param seckillVouchers
     * @return
     */
    @PostMapping("seckill/add")
    public ResultInfo<String> addSeckillVouchers(@RequestBody SeckillVouchers seckillVouchers) {
        seckillService.addSeckillVouchers(seckillVouchers);
        return ResultInfoUtil.buildSuccess(request.getServletPath(),
                "添加成功");
    }

    @GetMapping("seckill/hello")
    public ResultInfo<String> hello() {
        return ResultInfoUtil.buildSuccess(request.getServletPath(),
                "hello");
    }
}