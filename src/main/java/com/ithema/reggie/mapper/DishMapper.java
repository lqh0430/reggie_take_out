package com.ithema.reggie.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.ithema.reggie.entity.Dish;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;

/**
 * @version 1.0
 * @Author LQH02
 * @Description
 * @CreateDate 2022/8/30 15:36
 */
@Mapper
public interface DishMapper extends BaseMapper<Dish> {

    List<Dish> selectDishAndCategoryName();

}
