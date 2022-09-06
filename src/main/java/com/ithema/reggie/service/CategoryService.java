package com.ithema.reggie.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.ithema.reggie.entity.Category;

/**
 * @version 1.0
 * @Author LQH02
 * @Description
 * @CreateDate 2022/8/30 13:47
 */
public interface CategoryService extends IService<Category> {

    //删除分类
    void remove(Long id);

}
