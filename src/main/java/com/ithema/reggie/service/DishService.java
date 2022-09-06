package com.ithema.reggie.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.ithema.reggie.dto.DishDto;
import com.ithema.reggie.entity.Dish;

import java.util.List;

/**
 * @version 1.0
 * @Author LQH02
 * @Description
 * @CreateDate 2022/8/30 15:38
 */
public interface DishService extends IService<Dish> {

    //添加【菜品】,且要包含【菜品口味】
    void saveWithFlavor(DishDto dishDto);

    //查询【菜品】信息和【分类】表里的分类名称
    List<Dish> selectDishAndCategoryName();

    //根据id查询【菜品信息】和【菜品口味】
    DishDto getByIdWithFlavor(Long id);

    void updateWithFlavor(DishDto dishDto);

    void deleteByIds(List<Long> ids);
}
