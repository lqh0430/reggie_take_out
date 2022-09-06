package com.ithema.reggie.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.ithema.reggie.entity.User;

/**
 * @version 1.0
 * @Author LQH02
 * @Description
 * @CreateDate 2022/9/1 10:40
 */
public interface UserService extends IService<User> {

    //发送邮件
    void sendMsg(String to,String subject,String text);

}
