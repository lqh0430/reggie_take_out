package com.ithema.reggie.common;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

import java.sql.SQLIntegrityConstraintViolationException;

/**
 * @version 1.0
 * @Author LQH02
 * @Description 全局异常处理类
 * @CreateDate 2022/8/28 21:05
 */
@Slf4j
@ControllerAdvice(annotations = {RestController.class, Controller.class})
@ResponseBody
public class GlobalExceptionHandler {


    /*@ExceptionHandler(SQLIntegrityConstraintViolationException.class)
    public R exceptionHandler(SQLIntegrityConstraintViolationException ex) {
        log.error(ex.getMessage());
        //Duplicate entry 'zhangsan' for key 'idx_username' 捕获sql唯一性约束异常
        if (ex.getMessage().contains("Duplicate entry")) {
            String[] split = ex.getMessage().split(" ");
            String msg = split[2] + "已存在";
            return R.error(msg);
        }

        return R.error("未知错误");
    }*/

    @ExceptionHandler(CustomException.class)
    public R<String> exceptionHandler(CustomException ex) {
        log.error(ex.getMessage());

        return R.error(ex.getMessage());
    }


}
