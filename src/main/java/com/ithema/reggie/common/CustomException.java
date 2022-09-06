package com.ithema.reggie.common;

/**
 * @version 1.0
 * @Author LQH02
 * @Description 自定义异常类
 * @CreateDate 2022/8/30 15:51
 */
public class CustomException extends RuntimeException{
    public CustomException(String message) {
        super(message);
    }
}
