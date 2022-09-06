package com.ithema.reggie.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.ithema.reggie.common.CustomException;
import com.ithema.reggie.common.R;
import com.ithema.reggie.dto.DishDto;
import com.ithema.reggie.entity.Dish;
import com.ithema.reggie.entity.DishFlavor;
import com.ithema.reggie.entity.Setmeal;
import com.ithema.reggie.entity.SetmealDish;
import com.ithema.reggie.mapper.DishMapper;
import com.ithema.reggie.service.DishFlavorService;
import com.ithema.reggie.service.DishService;
import com.ithema.reggie.service.SetmealDishService;
import com.ithema.reggie.service.SetmealService;
import net.sf.jsqlparser.expression.operators.arithmetic.BitwiseRightShift;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.RequestParam;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * @version 1.0
 * @Author LQH02
 * @Description
 * @CreateDate 2022/8/30 15:39
 */
@Service
public class DishServiceImpl extends ServiceImpl<DishMapper, Dish>
implements DishService {

    @Resource
    private DishMapper dishMapper;

    @Resource
    private DishService dishService;

    @Resource
    private DishFlavorService dishFlavorService;

    @Resource
    private SetmealService setmealService;

    @Resource
    private SetmealDishService setmealDishService;

    /**
     * 【新增菜品】,保存【菜品】,且包含【菜品口味】;涉及2张表，需要开启事务管理
     * @param dishDto 封装类
     *
     */
    @Override
    @Transactional
    public void saveWithFlavor(DishDto dishDto) {

        //保存菜品的基本信息到【菜品表dish】
        this.save(dishDto);

        //菜品口味id与菜品id是必须关联的
        Long dishId = dishDto.getId();//菜品id

        //将菜品口味和这道菜品一一对应映射（防止串味）
        List<DishFlavor> flavors = dishDto.getFlavors();
        flavors = flavors.stream().map((item) -> {
            item.setDishId(dishId);
            return item;
        }).collect(Collectors.toList());

        //保存菜品口味数据到菜品口味表dish_flavor
        dishFlavorService.saveBatch(flavors);

    }

    /**
     * 查询【菜品】信息和【分类】表里的分类名称
     * @return
     */
    @Override
    public List<Dish> selectDishAndCategoryName() {
        List<Dish> dishes = dishMapper.selectDishAndCategoryName();
        return dishes;
    }

    /**
     * 根据id查询【菜品信息】和【菜品口味】
     * @param id
     */
    @Override
    public DishDto getByIdWithFlavor(Long id) {
        //1.先查询【菜品信息】
        Dish dish = this.getById(id);
        //2.封装进DishDto中
        DishDto dishDto = new DishDto();
        BeanUtils.copyProperties(dish,dishDto);

        //3.查询【菜品口味】
        LambdaQueryWrapper<DishFlavor> lambdaQueryWrapper = new LambdaQueryWrapper<>();
        lambdaQueryWrapper.eq(DishFlavor::getDishId,dish.getId());
        List<DishFlavor> flavors = dishFlavorService.list(lambdaQueryWrapper);
        //4.封装进dishDto中
        dishDto.setFlavors(flavors);

        return dishDto;
    }

    /**
     * 修改菜品
     * @param dishDto
     */
    @Override
    @Transactional
    public void updateWithFlavor(DishDto dishDto) {
        //1.修改【菜品表】基本信息
        this.updateById(dishDto);
        //2.修改【菜品口味】表
        //2.1需要先清除之前的口味
        LambdaQueryWrapper<DishFlavor> lambdaQueryWrapper = new LambdaQueryWrapper<>();
        lambdaQueryWrapper.eq(DishFlavor::getDishId,dishDto.getId());
        dishFlavorService.remove(lambdaQueryWrapper);
        //2.2插入新口味
        List<DishFlavor> flavors = dishDto.getFlavors();
        flavors.stream().map((item) -> {
            item.setDishId(dishDto.getId());
            return item;
        }).collect(Collectors.toList());

        dishFlavorService.saveBatch(flavors);
    }

    /**
     * 删除菜品及菜品口味
     * @param ids 菜品id
     * 菜品批量删除和单个删除
     *      * 1.判断要删除的菜品在不在售卖的套餐中，如果在那不能删除
     *      * 2.要先判断要删除的菜品是否在售卖，如果在售卖也不能删除
     */
    @Override
    public void deleteByIds(List<Long> ids) {

        //1.查询套餐中是否包含要删除的菜品,有则不能删除
        LambdaQueryWrapper<SetmealDish> setmealDishLambdaQueryWrapper = new LambdaQueryWrapper<>();
        setmealDishLambdaQueryWrapper.in(ids != null, SetmealDish::getDishId,ids);
        List<SetmealDish> setmealDishList = setmealDishService.list();
        //如果菜品没有关联套餐,直接删除即可
        if (setmealDishList.size() == 0) {
            //1.1先根据id查询出要删除的菜品
            //构造条件 ( ids in (1,2,3) )
            LambdaQueryWrapper<Dish> dishLambdaQueryWrapper = new LambdaQueryWrapper<>();
            dishLambdaQueryWrapper.in(ids != null,Dish::getId,ids);
            List<Dish> dishes = this.list(dishLambdaQueryWrapper);
            for (Dish dish : dishes) {
                if (dish != null) {
                    //对状态进行判断
                    Integer status = dish.getStatus();
                    if (status == 0) {//停售，可以删除
                        this.removeById(dish.getId());
                    }else {
                        //此时需要回滚,因为可能前面的停售的删除了,后面在售不能删除
                        throw new CustomException("删除菜品中有正在售卖菜品,不能删除");
                    }
                }
            }
            //1.2对菜品口味进行删除
            LambdaQueryWrapper<DishFlavor> dishFlavorLambdaQueryWrapper = new LambdaQueryWrapper<>();
            dishFlavorLambdaQueryWrapper.in(DishFlavor::getDishId,ids);
            dishFlavorService.remove(dishFlavorLambdaQueryWrapper);
            return ;
        }

        //如果菜品有关联套餐,并且在售卖,不能删除
        ArrayList<Long> setmeal_idList = new ArrayList<>();
        for (SetmealDish setmealDish  : setmealDishList) {
            Long setmealId = setmealDish.getSetmealId();
            setmeal_idList.add(setmealId);
        }

        //查询出与删除菜品相关联的套餐
        LambdaQueryWrapper<Setmeal> setmealLambdaQueryWrapper = new LambdaQueryWrapper<>();
        setmealLambdaQueryWrapper.in(Setmeal::getId,setmeal_idList);
        List<Setmeal> setmealList = setmealService.list(setmealLambdaQueryWrapper);
        //对拿到的所有套餐进行遍历，然后拿到套餐的售卖状态，如果有套餐正在售卖那么删除失败
        for (Setmeal setmeal : setmealList) {
            Integer status = setmeal.getStatus();
            if (status == 1){

            }
        }

        //要删除的菜品关联的套餐没有在售，可以删除
        //这下面的代码并不一定会执行,因为如果前面的for循环中出现status == 1,那么下面的代码就不会再执行
        dishService.deleteByIds(ids);
        LambdaQueryWrapper<DishFlavor> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.in(DishFlavor::getDishId,ids);
        dishFlavorService.remove(queryWrapper);

    }
}
