package com.ithema.reggie.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.ithema.reggie.common.CustomException;
import com.ithema.reggie.entity.Category;
import com.ithema.reggie.entity.Dish;
import com.ithema.reggie.entity.Setmeal;
import com.ithema.reggie.mapper.CategoryMapper;
import com.ithema.reggie.service.CategoryService;
import com.ithema.reggie.service.DishService;
import com.ithema.reggie.service.SetmealService;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;

/**
 * @version 1.0
 * @Author LQH02
 * @Description
 * @CreateDate 2022/8/30 13:48
 */
@Service
public class CategoryServiceImpl extends ServiceImpl<CategoryMapper, Category>
implements CategoryService {

    @Resource
    private DishService dishService;

    @Resource
    private SetmealService setmealService;



    /**
     * 删除分类(在菜品表和套餐表中存在分类,有关联关系，不能随便删除,需要进行判断)
     * @param id 分类id
     */
    @Override
    public void remove(Long id) {
        LambdaQueryWrapper<Dish> dishLambdaQueryWrapper = new LambdaQueryWrapper<>();
        //根据分类id进行查询
        dishLambdaQueryWrapper.eq(Dish::getCategoryId,id);
        //查询当前分类是否关联了菜品,如果关联了,需要抛出异常
        int count1 = dishService.count(dishLambdaQueryWrapper);
        if (count1 > 0) {//关联
            //抛出异常
            throw new CustomException("当前分类下关联了菜品,不能删除");
        }

        //查询当前分类是否关联了套餐,如果已经关联,需要抛出异常
        LambdaQueryWrapper<Setmeal> setmealLambdaQueryWrapper = new LambdaQueryWrapper<>();
        setmealLambdaQueryWrapper.eq(Setmeal::getCategoryId,id);
        int count2 = setmealService.count(setmealLambdaQueryWrapper);
        if (count2 > 0) {//关联
            //抛出异常
            throw new CustomException("当前分类下关联了套餐,不能删除");
        }

        //正常删除
        super.removeById(id);
    }
}
