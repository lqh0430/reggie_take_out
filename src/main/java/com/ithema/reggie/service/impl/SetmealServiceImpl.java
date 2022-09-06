package com.ithema.reggie.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.ithema.reggie.common.CustomException;
import com.ithema.reggie.dto.SetmealDto;
import com.ithema.reggie.entity.Setmeal;
import com.ithema.reggie.entity.SetmealDish;
import com.ithema.reggie.mapper.SetmealMapper;
import com.ithema.reggie.service.SetmealDishService;
import com.ithema.reggie.service.SetmealService;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.PostMapping;

import javax.annotation.Resource;
import java.util.List;
import java.util.stream.Collectors;

/**
 * @version 1.0
 * @Author LQH02
 * @Description
 * @CreateDate 2022/8/30 15:40
 */
@Service
public class SetmealServiceImpl extends ServiceImpl<SetmealMapper, Setmeal>
implements SetmealService {

    @Resource
    private SetmealDishService setmealDishService;

    /**
     * 保存套餐
     * @param setmealDto
     */
    @Override
    public void saveWithDish(SetmealDto setmealDto) {
        //1.保存套餐基础信息
        this.save(setmealDto);

        //2.插入套餐菜品
        List<SetmealDish> setmealDishes = setmealDto.getSetmealDishes();
        //注意上面拿到的setmealDishes是没有setmeanlId这个的值的，通过debug可以发现
        setmealDishes.stream().map((item) -> {
            item.setSetmealId(setmealDto.getId());//给每项 套餐菜品 设置套餐id(保证对应)
            return item;
        }).collect(Collectors.toList());

        //3.保存套餐和菜品的关联关系,操作setmeal_dish表
        setmealDishService.saveBatch(setmealDishes);
    }

    /**
     * 删除套餐,同时需要删除套餐和菜品的关联数据
     * @param ids
     */
    @Override
    @Transactional
    public void deleteWithDish(List<Long> ids) {
        //select count(*) from setmeal where id in (1,2,3) and status
        //查询套餐状态,起售时不可以删除
        LambdaQueryWrapper<Setmeal> lambdaQueryWrapper = new LambdaQueryWrapper<>();
        lambdaQueryWrapper.in(Setmeal::getId,ids);
        lambdaQueryWrapper.eq(Setmeal::getStatus,1);

        int count = this.count(lambdaQueryWrapper);
        if (count > 0) {//不能删除
            throw new CustomException("套餐正在售卖中,不能删除");
        }

        //不再售卖中,可以删除,需要先删除套餐表中的数据--setmeal
        //delete from setmeal dish where setmeal id in (1,2,3)
        LambdaQueryWrapper<SetmealDish> dishLambdaQueryWrapper = new LambdaQueryWrapper<>();
        dishLambdaQueryWrapper.in(SetmealDish::getSetmealId,ids);
        //删除关系表中的数据 setmeal_dish
        setmealDishService.remove(dishLambdaQueryWrapper);
    }

    @Override
    public void updateSetmealStatusById(Integer status, List<Long> ids) {
        //构造条件(where id in (1,2,3,ids) )
        LambdaQueryWrapper<Setmeal> lambdaQueryWrapper = new LambdaQueryWrapper<>();
        lambdaQueryWrapper.in(ids != null,Setmeal::getId,ids);
        List<Setmeal> list = this.list(lambdaQueryWrapper);

        for (Setmeal setmeal : list) {
            if (setmeal != null) {
                //修改状态
                setmeal.setStatus(status);
                //刷新
                this.updateById(setmeal);
            }
        }
    }

    /**
     * 回显套餐数据
     * @param id 套餐id
     * @return
     */
    @Override
    public SetmealDto getSetmealDtoData(Long id) {

        //1.通过id获取套餐基本信息
        Setmeal setmeal = this.getById(id);
        //2.构造SetmealDto对象(该对象要封装setmeal属性和setmealDish属性)
        SetmealDto setmealDto = new SetmealDto();
        //3.查询setmealDish关系表
        LambdaQueryWrapper<SetmealDish> lambdaQueryWrapper = new LambdaQueryWrapper<>();
        lambdaQueryWrapper.eq(id != null,SetmealDish::getSetmealId,id);

        if (setmeal != null) {
            //封装setmeal属性
            BeanUtils.copyProperties(setmeal,setmealDto);
            List<SetmealDish> list = setmealDishService.list(lambdaQueryWrapper);
            //封装setmealDish属性
            setmealDto.setSetmealDishes(list);
            return setmealDto;
        }

        return null;
    }


}
