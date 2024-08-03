package com.cq.accountmanger.vo;

import lombok.Getter;
import lombok.Setter;

import java.io.Serializable;

@Setter
@Getter
public class LoginUserInfo implements Serializable {

    private String nickname;
    private String token;
    private String avatarUrl;

}