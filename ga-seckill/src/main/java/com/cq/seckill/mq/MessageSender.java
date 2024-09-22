package com.cq.seckill.mq;


import org.apache.rocketmq.common.message.Message;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cloud.stream.annotation.EnableBinding;
import org.springframework.cloud.stream.messaging.Source;
import org.springframework.messaging.support.MessageBuilder;

@EnableBinding(Source.class)
public class MessageSender {
    @Autowired
    private Source source;

    public void sendMessage(Message msg) {
        this.source.output().send(MessageBuilder.withPayload(msg).build());
    }
}
