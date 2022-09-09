package com.ithema.reggie.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.ithema.reggie.common.R;
import com.ithema.reggie.entity.Category;
import com.ithema.reggie.service.CategoryService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import java.util.List;

/**
 * @version 1.0
 * @Author LQH02
 * @Description 分类
 * @CreateDate 2022/8/30 13:49
 */
@RestController
@Slf4j
@RequestMapping("/category")
@Api(tags = "分类接口")
public class CategoryController {

    @Resource
    private CategoryService categoryService;


    /**
     * 新增分类
     * @param category
     * @return
     */
    @PostMapping
    @ApiOperation(value = "新增分类接口")
    public R<String> save(@RequestBody Category category) {
        log.info("菜品={}",category);
        categoryService.save(category);
        return R.success("新增菜品成功");
    }

    /**
     * 分类 分页
     * @param page
     * @param pageSize
     * @return
     */
    @GetMapping("/page")
    @ApiOperation(value = "分类分页查询接口")
    public R<Page> page(@RequestParam("page") Integer page,
                        @RequestParam("pageSize") Integer pageSize) {
        log.info("page={} pageSize={}",page,pageSize);
        //构造分页器
        Page<Category> pageInfo = new Page<>(page,pageSize);
        //条件构造器
        LambdaQueryWrapper<Category> lambdaQueryWrapper = new LambdaQueryWrapper<>();
        //排序条件
        lambdaQueryWrapper.orderByAsc(Category::getSort);
        //执行查询
        categoryService.page(pageInfo,lambdaQueryWrapper);
        return R.success(pageInfo);
    }

    /**
     * 删除分类
     * @param ids
     * @return
     */
    @DeleteMapping
    @ApiOperation(value = "删除分类接口")
    public R<String> delete(Long ids) {
        log.info("要删除菜品的id={}",ids);
        /*categoryService.removeById(ids);*/
        categoryService.remove(ids);

        return R.success("菜品删除成功");
    }

    /**
     * 根据id修改菜品
     * @param category
     * @return
     */
    @PutMapping
    @ApiOperation(value = "修改菜品接口")
    public R<String> update(@RequestBody Category category) {
        log.info("要修改的菜品的信息={}",category);

        categoryService.updateById(category);


        return R.success("修改菜品成功");
    }


    /**
     * 查询 【菜品分类】
     * @param category 前端传入的是type参数,这里用对象接收更易于扩展,且由于是GET请求,前端传来的参数不是json格式,不需要使用@RequsetBody封装属性
     * @return 返回【菜品分类】 集合
     */
    @GetMapping("/list")
    @ApiOperation(value = "菜品分类查询接口")
    public R<List<Category>> list(Category category) {
        //条件构造器
        LambdaQueryWrapper<Category> lambdaQueryWrapper = new LambdaQueryWrapper<>();
        //添加条件(当餐品分类不为空时,才能加入过滤条件)
        lambdaQueryWrapper.eq(category.getType() != null,Category::getType,category.getType());
        //添加排序条件
        lambdaQueryWrapper.orderByAsc(Category::getSort).orderByDesc(Category::getUpdateTime);
        //执行查询
        List<Category> categoryList = categoryService.list(lambdaQueryWrapper);
        return R.success(categoryList);
    }
}
