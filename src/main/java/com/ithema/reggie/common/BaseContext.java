package com.ithema.reggie.common;

/**
 * @version 1.0
 * @Author LQH02
 * @Description 基于ThreadLocal封装的工具类,用于保存和获取当前登录的用户id
 * @CreateDate 2022/8/30 13:19
 */
public class BaseContext {

    private static ThreadLocal<Long> threadLocal = new ThreadLocal<>();

    public static void setCurrentId(Long id) {
        threadLocal.set(id);
    }

    public static Long getCurrentId() {
        return threadLocal.get();
    }

}
