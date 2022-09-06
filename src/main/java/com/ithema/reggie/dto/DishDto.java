package com.ithema.reggie.dto;

import com.ithema.reggie.entity.Dish;
import com.ithema.reggie.entity.DishFlavor;
import lombok.Data;

import java.util.ArrayList;
import java.util.List;

/**
 * @version 1.0
 * @Author LQH02
 * @Description 前端传给后端的属性,涉及多张表的字段,我们就用DTO将其封装起来
 * @CreateDate 2022/8/30 19:51
 */
@Data
public class DishDto extends Dish {

    private List<DishFlavor> flavors = new ArrayList<>();

    private String categoryName;

    private Integer copies;

}
