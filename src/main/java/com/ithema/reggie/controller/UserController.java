package com.ithema.reggie.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.ithema.reggie.common.R;
import com.ithema.reggie.entity.User;
import com.ithema.reggie.service.UserService;
import com.ithema.reggie.utils.ValidateCodeUtils;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang.StringUtils;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;
import java.util.Map;
import java.util.concurrent.TimeUnit;

/**
 * @version 1.0
 * @Author LQH02
 * @Description
 * @CreateDate 2022/9/1 10:41
 */
@RestController
@Slf4j
@RequestMapping("/user")
@Api(tags = "用户接口")
public class UserController {

    @Resource
    private UserService userService;

    @Resource
    private RedisTemplate redisTemplate;

    @PostMapping("/sendMsg")
    @ApiOperation(value = "QQ邮箱发送接口")
    public R<String> sendMsg(HttpSession session, @RequestBody User user) {
        //获取邮箱号(前端邮箱绑定的字段是phone)
        String email = user.getPhone();
        //定义署名
        String subject = "美团外卖";

        if (StringUtils.isNotEmpty(email)) {
            //发送一个四位数的验证码,把验证码变为String类型
            String code = ValidateCodeUtils.generateValidateCode(4).toString();
            String text = "【美团】您好,您的账号在异地登录异常,如果不是您本人亲自操作,请尽快验证登录,您的登录验证码为:"+code;
            log.info("验证码: {}",code);
            //发送短信
            userService.sendMsg(email,subject,text);
            //将验证码保存到session当中
            //session.setAttribute("email",code);

            //将验证码缓存到redis中，并设置有效期
            redisTemplate.opsForValue().set("email",code,5, TimeUnit.MINUTES);

            return R.success("验证码发送成功");
        }
        return R.error("验证码发送异常,请重新发送");
    }

    //登录
    @PostMapping("/login")
    @ApiOperation(value = "用户登录接口")
    public R<User> login(HttpSession session,@RequestBody Map map){    //Map存JSON数据
        //获取邮箱，用户输入的
        String phone = map.get("phone").toString();
        //获取验证码，用户输入的
        String code = map.get("code").toString();
        //获取session中保存的验证码
        //Object sessionCode = session.getAttribute("email");

        //从redis中获取验证码
        Object sessionCode = redisTemplate.opsForValue().get("email");

        //如果session的验证码和用户输入的验证码进行比对,&&同时
        if (sessionCode != null && sessionCode.equals(code)) {
            //要是User数据库没有这个邮箱则自动注册,先看看输入的邮箱是否存在数据库
            LambdaQueryWrapper<User> queryWrapper = new LambdaQueryWrapper<>();
            queryWrapper.eq(User::getPhone,phone);
            //获得唯一的用户，因为手机号是唯一的
            User user = userService.getOne(queryWrapper);
            //要是User数据库没有这个邮箱则自动注册
            if (user == null) {
                user = new User();
                user.setPhone(phone);
                user.setStatus(1);
                //取邮箱的前五位为用户名
                user.setName(phone.substring(0,6));
                userService.save(user);
            }
            //不保存这个用户名就登不上去，因为过滤器需要得到这个user才能放行，程序才知道你登录了
            session.setAttribute("user", user.getId());

            //登录成功后,直接删除缓存验证码
            redisTemplate.delete("email");

            return R.success(user);
        }
        return R.error("登录失败");
    }

    /**
     * 退出
     * @param request
     * @return
     */
    @PostMapping("/loginout")
    @ApiOperation(value = "用户退出接口")
    public R<String> loginout(HttpServletRequest request) {
        request.getSession().removeAttribute("user");
        return R.success("退出成功");
    }
}
