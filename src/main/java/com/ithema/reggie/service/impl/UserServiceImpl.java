package com.ithema.reggie.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.ithema.reggie.entity.User;
import com.ithema.reggie.mapper.UserMapper;
import com.ithema.reggie.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;

/**
 * @version 1.0
 * @Author LQH02
 * @Description
 * @CreateDate 2022/9/1 10:40
 */
@Service
public class UserServiceImpl extends ServiceImpl<UserMapper, User> implements UserService {

    @Value("${spring.mail.username}")//把yml配置的邮箱号赋值到from
    private String from;

    @Resource
    private JavaMailSender javaMailSender;//发送邮件需要的对象

    /**
     * 发送邮件
     * @param to 收件人
     * @param subject 主题
     * @param text 文本
     */
    @Override
    public void sendMsg(String to, String subject, String text) {
        //发送简单邮件,简单邮件不包括附件等别的
        SimpleMailMessage message = new SimpleMailMessage();
        message.setFrom(from);
        message.setTo(to);
        message.setSubject(subject);
        message.setText(text);
        //发送
        javaMailSender.send(message);
    }
}
