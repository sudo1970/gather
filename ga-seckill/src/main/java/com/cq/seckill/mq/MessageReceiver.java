package com.cq.seckill.mq;

import org.springframework.cloud.stream.annotation.EnableBinding;
import org.springframework.cloud.stream.annotation.StreamListener;
import org.springframework.cloud.stream.messaging.Sink;

@EnableBinding(Sink.class)
public class MessageReceiver {

    @StreamListener(Sink.INPUT)
    public void receive(String message) {
        System.out.println("Received message: " + message);
    }
}
