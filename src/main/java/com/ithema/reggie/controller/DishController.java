package com.ithema.reggie.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.ithema.reggie.common.R;
import com.ithema.reggie.dto.DishDto;
import com.ithema.reggie.entity.Category;
import com.ithema.reggie.entity.Dish;
import com.ithema.reggie.entity.DishFlavor;
import com.ithema.reggie.service.CategoryService;
import com.ithema.reggie.service.DishFlavorService;
import com.ithema.reggie.service.DishService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import java.util.List;
import java.util.stream.Collectors;

/**
 * @version 1.0
 * @Author LQH02
 * @Description 菜品分类管理
 * @CreateDate 2022/8/30 19:05
 */
@RestController
@Slf4j
@RequestMapping("/dish")
public class DishController {

    @Resource
    private CategoryService categoryService;

    @Resource
    private DishFlavorService dishFlavorService;

    @Resource
    private DishService dishService;


    /**
     * 保存【菜品】
     * @param dishDto 涉及【菜品】表和【菜品口味】表,属性用Dto封装
     * @return
     */
    @PostMapping
    public R<String> save(@RequestBody DishDto dishDto) {
        log.info("保存【菜品】DishDto封装的属性信息={}",dishDto);

        dishService.saveWithFlavor(dishDto);

        return R.success("新增菜品成功");
    }

    /**
     * 菜品信息分页 + 条件
     * @param page
     * @param pageSize
     * @param name
     * @return
     */
    @GetMapping("/page")
    public R<Page> page(@RequestParam("page") Integer page,
                        @RequestParam("pageSize") Integer pageSize,
                        String name) {
        //这是mybatis的pageHelper插件
        /*PageHelper.startPage(page,pageSize);
        List<Dish> dishes = dishService.selectDishAndCategoryName();
        PageInfo<Dish> dishPageInfo = new PageInfo<>(dishes, pageSize);
        return R.success(dishPageInfo);*/


        //这是MP(没有联表)
        /*Page<Dish> pageInfo = new Page<>(page, pageSize);
        LambdaQueryWrapper<Dish> lambdaQueryWrapper = new LambdaQueryWrapper<>();
        lambdaQueryWrapper.like(name != null,Dish::getName,name);
        lambdaQueryWrapper.orderByDesc(Dish::getUpdateTime);
        dishService.page(pageInfo,lambdaQueryWrapper);
        return R.success(pageInfo);*/


        //深拷贝(联表)
        //1.构造分页器
        Page<Dish> dishPage = new Page<>(page,pageSize);
        Page<DishDto> dishDtoPage = new Page<>(page,pageSize);

        //2.添加条件(模糊+倒序)
        LambdaQueryWrapper<Dish> lambdaQueryWrapper = new LambdaQueryWrapper<>();
        lambdaQueryWrapper.like(name != null,Dish::getName,name);
        lambdaQueryWrapper.orderByDesc(Dish::getUpdateTime);
        dishService.page(dishPage,lambdaQueryWrapper);

        //3.将Page<Dish>查询出来的结果拷贝给Page<DishDto> (这里需要需略参数records,我们要查询的集合就在这里面,先把其他信息拷贝过来)
        BeanUtils.copyProperties(dishPage,dishDtoPage,"records");

        //4.拷贝records
        List<Dish> records = dishPage.getRecords();
        List<DishDto> list = records.stream().map((item) -> {
            //将每一项都拷贝进DishDto对象中
            DishDto dishDto = new DishDto();
            BeanUtils.copyProperties(item,dishDto);
            //查询分类id
            Long categoryId = item.getCategoryId();
            Category category = categoryService.getById(categoryId);
            if (category != null) {//分类不为空,即有分类名称,就把分类名称赋给dishDto对象
                String categoryName = category.getName();
                dishDto.setCategoryName(categoryName);
            }
            return dishDto;
        }).collect(Collectors.toList());
        //5.设置records
        dishDtoPage.setRecords(list);
        //6.返回
        return R.success(dishDtoPage);
    }

    /**
     * 根据id查询【菜品信息】和【菜品口味】
     * @param id
     * @return
     */
    @GetMapping("/{id}")
    public R<DishDto> get(@PathVariable("id") Long id) {
        DishDto dishDto = dishService.getByIdWithFlavor(id);

        return R.success(dishDto);
    }

    /**
     * 修改菜品成功
     * @param dishDto
     * @return
     */
    @PutMapping
    public R<String> update(@RequestBody DishDto dishDto) {
        dishService.updateWithFlavor(dishDto);

        return R.success("修改菜品成功");
    }

    /**
     * 【菜品】 单个停售or起售
     * @param status (前端做了校验,将状态相反传过来,所以可以直接赋值,就不用取反了)
     * @param ids
     * @return
     */
    /*@PostMapping("/status/{status}")
    public R<String> saleStop(@PathVariable("status") Integer status,
                              @RequestParam("ids") Long ids) {
        log.info("停售的状态={} 停售的菜品id={}",status,ids);
        //根据id获取菜品
        Dish dish = dishService.getById(ids);
        if (dish != null) {
            //设置状态
            dish.setStatus(status);
            //根据id修改,修改完调用刷新
            dishService.updateById(dish);
            return R.success("菜品状态修改成功");
        }
        return R.error("菜品状态设置异常");
    }*/

    /**
     * 【菜品】 批量起售or停售
     * @param status
     * @param ids
     * @return
     */
    @PostMapping("/status/{status}")
    public R<String> saleStopBatch(@PathVariable("status") Integer status,
                                   @RequestParam("ids") List<Long> ids) {
        log.info("批量停售的菜品状态={} 菜品id={}", status, ids);
        //构造条件( ids in (1,2,3) )
        LambdaQueryWrapper<Dish> lambdaQueryWrapper = new LambdaQueryWrapper<>();
        lambdaQueryWrapper.in(ids != null, Dish::getId, ids);
        //查询
        List<Dish> dishes = dishService.list(lambdaQueryWrapper);
        for (Dish dish : dishes) {
            if (dish != null) {
                dish.setStatus(status);
                dishService.updateById(dish);
            }
        }
        return R.success("批量起售/停售成功");
    }

    /**
     * 批量删除
     * @param ids
     * @return
     */
    @DeleteMapping
    public R<String> delete(@RequestParam("ids") List<Long> ids) {
        log.info("批量删除的菜品id={}",ids);
        //删除菜品(逻辑删除)
        dishService.deleteByIds(ids);

        return R.success("菜品批量删除成功");
    }


    /**
     * 根据条件查询对应的菜品数据
     * @param dish
     * @return
     */
    /*@GetMapping("/list")
    public R<List<Dish>> list(Dish dish) {
        log.info("菜品分类查询哪些菜品={}",dish);
        //构造查询条件
        LambdaQueryWrapper<Dish> lambdaQueryWrapper = new LambdaQueryWrapper<>();
        lambdaQueryWrapper.eq(dish.getCategoryId() != null,Dish::getCategoryId,dish.getCategoryId());
        lambdaQueryWrapper.eq(Dish::getStatus,1);//在售中的菜品
        lambdaQueryWrapper.orderByAsc(Dish::getSort).orderByDesc(Dish::getUpdateTime);
        List<Dish> list = dishService.list(lambdaQueryWrapper);
        return R.success(list);
    }*/


    /**
     * 改造扩展属性
     * @param dish
     * @return
     */
    @GetMapping("/list")
    public R<List<DishDto>> list(Dish dish) {
        log.info("菜品分类查询哪些菜品={}",dish);
        //构造查询条件
        LambdaQueryWrapper<Dish> lambdaQueryWrapper = new LambdaQueryWrapper<>();
        lambdaQueryWrapper.eq(dish.getCategoryId() != null,Dish::getCategoryId,dish.getCategoryId());
        lambdaQueryWrapper.eq(Dish::getStatus,1);//在售中的菜品
        lambdaQueryWrapper.orderByAsc(Dish::getSort).orderByDesc(Dish::getUpdateTime);
        List<Dish> list = dishService.list(lambdaQueryWrapper);

        //构造一个新对象(拷贝原属性值+设置新属性值)
        List<DishDto> dishDtoList = list.stream().map((item) -> {
            DishDto dishDto = new DishDto();
            //1.拷贝菜品表的基本信息
            BeanUtils.copyProperties(item,dishDto);
            //查询菜品分类表
            Category category = categoryService.getById(dish.getCategoryId());
            if (category != null) {
                String categoryName = category.getName();
                //2.设置菜品分类名称
                dishDto.setCategoryName(categoryName);
            }
            //查询菜品口味表
            Long dishId = item.getId();//获取当前每项的菜品id
            //根据菜品id去查询对应的菜品口味 SQL: select * from dish_flavor where dish_id = dishId
            LambdaQueryWrapper<DishFlavor> dishFlavorLambdaQueryWrapper = new LambdaQueryWrapper<>();
            dishFlavorLambdaQueryWrapper.eq(DishFlavor::getDishId,dishId);
            List<DishFlavor> dishFlavorList = dishFlavorService.list(dishFlavorLambdaQueryWrapper);

            //3.设置菜品口味
            dishDto.setFlavors(dishFlavorList);

            //4.返回该对象
            return dishDto;
        }).collect(Collectors.toList());

        return R.success(dishDtoList);
    }
}
