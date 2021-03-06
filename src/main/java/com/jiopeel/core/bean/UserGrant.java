package com.jiopeel.core.bean;

import lombok.Builder;
import lombok.Data;
import lombok.experimental.Tolerate;

import java.io.Serializable;

/**
 * @Description :授权登陆数据
 * @auhor:lyc
 * @Date:2019/12/12 21:23
 */
@Data
@Builder
public class UserGrant extends Bean implements Serializable {

    private static final long serialVersionUID = -8465793931947933341L;

    @Tolerate
    public UserGrant() {
    }

    /**
     * 用户id
     */
    private String userid;

    /**
     * 授权类型 github
     */
    private String granttype;

    /**
     * 唯一标识
     */
    private String onlyid;

    /**
     * 通行证 
     */
    private String token;

    /**
     * 头像
     */
    private  String imgurl;

    /**
     * 昵称
     */
    private  String nickname;
}
