package com.ithema.reggie.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.ithema.reggie.common.R;
import com.ithema.reggie.dto.SetmealDto;
import com.ithema.reggie.entity.Category;
import com.ithema.reggie.entity.Setmeal;
import com.ithema.reggie.entity.SetmealDish;
import com.ithema.reggie.service.CategoryService;
import com.ithema.reggie.service.DishService;
import com.ithema.reggie.service.SetmealDishService;
import com.ithema.reggie.service.SetmealService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import java.security.acl.LastOwnerException;
import java.util.List;
import java.util.stream.Collectors;

/**
 * @version 1.0
 * @Author LQH02
 * @Description 套餐管理
 * @CreateDate 2022/8/31 11:20
 */
@RestController
@Slf4j
@RequestMapping("/setmeal")
public class SetmealController {

    @Resource
    private SetmealService setmealService;

    @Resource
    private SetmealDishService setmealDishService;

    @Resource
    private CategoryService categoryService;


    /**
     * 保存套餐
     * @param setmealDto 封装了套餐 和套餐菜品
     * @return
     * @CacheEvict(value = "setmealCache",allEntries = true) 新增套餐时,也要清除缓存;保证数据一致性
     */
    @PostMapping
    @CacheEvict(value = "setmealCache",allEntries = true)
    public R<String> saveWithDish(@RequestBody SetmealDto setmealDto) {
        log.info("套餐信息={}",setmealDto);
        setmealService.saveWithDish(setmealDto);
        return R.success("新增套餐成功");
    }

    /**
     * 套餐 分页 + 条件
     * @param page
     * @param pageSize
     * @param name
     * @return
     */
    @GetMapping("/page")
    public R<Page> page(@RequestParam("page") Integer page,
                        @RequestParam("pageSize") Integer pageSize,
                        String name) {
        //1.构造分页器
        Page<Setmeal> setmealPage = new Page<>(page, pageSize);
        //添加条件(模糊+修改时间倒序)
        LambdaQueryWrapper<Setmeal> setmealLambdaQueryWrapper = new LambdaQueryWrapper<>();
        setmealLambdaQueryWrapper.like(name != null,Setmeal::getName,name);
        setmealLambdaQueryWrapper.orderByDesc(Setmeal::getUpdateTime);
        //查询
        setmealService.page(setmealPage,setmealLambdaQueryWrapper);

        //2.构造分页器
        Page<SetmealDto> setmealDtoPage = new Page<>(page, pageSize);
        //3.把setmealPage基本信息拷贝进setmealDtoPage
        BeanUtils.copyProperties(setmealPage,setmealDtoPage,"records");

        //4.获取records(该属性存储了我们需要的数据)
        List<Setmeal> records = setmealPage.getRecords();
        log.info("records={}",records);
        List<SetmealDto> list = records.stream().map((item) -> {
            //将每一项都拷贝进setmealDto对象中
            SetmealDto setmealDto = new SetmealDto();
            BeanUtils.copyProperties(item, setmealDto);
            //查询菜品分类id
            Long categoryId = item.getCategoryId();
            Category category = categoryService.getById(categoryId);
            if (category != null) {
                String categoryName = category.getName();
                setmealDto.setCategoryName(categoryName);//设置菜品分类
            }
            return setmealDto;//返回该对象
        }).collect(Collectors.toList());
        log.info("list={}",list);

        //5.设置records
        setmealDtoPage.setRecords(list);
        //6.返回
        return R.success(setmealPage);

    }

    /**
     * 删除套餐
     * @param ids
     * @return
     * @CacheEvict(value = "setmealCache",allEntries = true) 清除setmealCache缓存
     */
    @DeleteMapping
    @CacheEvict(value = "setmealCache",allEntries = true)
    public R<String> delete(@RequestParam("ids") List<Long> ids) {
        log.info("ids:{}",ids);

        setmealService.deleteWithDish(ids);

        return R.success("套餐删除成功");
    }


    /**
     * 批量停售/起售套餐
     * @param status
     * @param ids
     * @return
     */
    @PostMapping("/status/{status}")
    public R<String> saleStop(@PathVariable("status") Integer status,
                              @RequestParam("ids")List<Long> ids) {
        log.info("批量停售/起售的套餐 状态={} id={}",status,ids);
        setmealService.updateSetmealStatusById(status,ids);
        return R.success("套餐状态修改成功");
    }

    /**
     * 修改套餐时 回显数据
     * @param id 套餐id
     * @return
     */
    @GetMapping("/{id}")
    public R<SetmealDto> getSetmealDtoData(@PathVariable("id") Long id) {

        SetmealDto setmealDto = setmealService.getSetmealDtoData(id);

        return R.success(setmealDto);
    }

    /**
     * 修改套餐
     * @param setmealDto
     * @return
     */
    @PutMapping
    public R<String> updateSetmeal(@RequestBody SetmealDto setmealDto) {
        if (setmealDto == null) {
            return R.error("请求异常");
        }
        //通过setmealDto获取套餐中所有套餐菜品集合
        List<SetmealDish> setmealDishes = setmealDto.getSetmealDishes();
        log.info("套餐菜品集合={}",setmealDishes);
        if (setmealDishes.size() == 0) {
            return R.success("套餐中没有菜品,请添加菜品");
        }

        //获取本次修改套餐id
        Long setmealId = setmealDto.getId();

        //1.先清除回显的套餐菜品数据(delete from setmeal_dish where setmeal_id = setmealId)
        LambdaQueryWrapper<SetmealDish> lambdaQueryWrapper = new LambdaQueryWrapper<>();
        lambdaQueryWrapper.eq(SetmealDish::getSetmealId,setmealId);
        setmealDishService.remove(lambdaQueryWrapper);

        //2.再重新插入套餐菜品(先赋id,保证是当前套餐id)
        for (SetmealDish setmealDish : setmealDishes) {
            setmealDish.setSetmealId(setmealId);
        }
        //批量把setmealDish保存到setmeal_dish表
        setmealDishService.saveBatch(setmealDishes);
        setmealService.updateById(setmealDto);

        return R.success("套餐修改成功");
    }


    /**
     * 根据条件查询套餐
     * @param setmeal 封装的对象
     * @return 套餐集合
     * @Cacheable 如果缓存中有该数据,则直接返回缓存中的数据,如果没有,则继续执行程序去数据库中查询
     */
    @GetMapping("/list")
    @Cacheable(value = "setmealCache", key = "#setmeal.categoryId + '_' + #setmeal.status")
    public R<List<Setmeal>> list(Setmeal setmeal) {
        log.info("套餐信息={}",setmeal);
        //根据前端传递过来的categoryId和status查询
        LambdaQueryWrapper<Setmeal> setmealLambdaQueryWrapper = new LambdaQueryWrapper<>();
        setmealLambdaQueryWrapper.eq(Setmeal::getCategoryId,setmeal.getCategoryId());
        setmealLambdaQueryWrapper.eq(Setmeal::getStatus,setmeal.getStatus());
        setmealLambdaQueryWrapper.orderByDesc(Setmeal::getUpdateTime);

        //查询
        List<Setmeal> list = setmealService.list(setmealLambdaQueryWrapper);

        return R.success(list);
    }
}
