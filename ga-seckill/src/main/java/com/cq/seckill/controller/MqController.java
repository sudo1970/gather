package com.cq.seckill.controller;

import com.alibaba.fastjson.JSON;
import com.cq.commons.model.domain.ResultInfo;
import com.cq.commons.utils.ResultInfoUtil;
import com.cq.seckill.mq.MessageSender;
import lombok.extern.slf4j.Slf4j;
import org.apache.rocketmq.common.message.Message;
import org.apache.rocketmq.remoting.common.RemotingHelper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.io.UnsupportedEncodingException;

@Slf4j
@RestController
public class MqController {
    @Autowired
    private MessageSender messageSender;

    @GetMapping("sendMsgByMq")
    public ResultInfo<String> sendMsgByMq() throws UnsupportedEncodingException {
        log.info("------------sendMsgByMq--------------------------------------");
        OrderMessage orderMessage = new OrderMessage();
        orderMessage.setOrderId("0001");
        // 创建消息实例
        Message msg = new Message("OrderTopic", "TagA", JSON.toJSONString(orderMessage).getBytes(RemotingHelper.DEFAULT_CHARSET));

        messageSender.sendMessage(msg);
        return ResultInfoUtil.buildSuccess("success");
    }
}
