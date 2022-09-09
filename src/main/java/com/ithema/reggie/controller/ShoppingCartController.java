package com.ithema.reggie.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.ithema.reggie.common.BaseContext;
import com.ithema.reggie.common.R;
import com.ithema.reggie.entity.ShoppingCart;
import com.ithema.reggie.service.ShoppingCartService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import java.time.LocalDateTime;
import java.util.List;

/**
 * @version 1.0
 * @Author LQH02
 * @Description
 * @CreateDate 2022/9/1 17:48
 */
@RestController
@Slf4j
@RequestMapping("/shoppingCart")
@Api(tags = "购物车接口")
public class ShoppingCartController {

    @Resource
    private ShoppingCartService shoppingCartService;

    /**
     * 添加购物车
     * @param shoppingCart
     * @return
     */
    @PostMapping("/add")
    @ApiOperation(value = "添加购物车接口")
    public R<ShoppingCart> save(@RequestBody ShoppingCart shoppingCart) {
        log.info("购物车={}",shoppingCart);

        //1.先要知道是哪个用户进行的添加购物车操作
        Long currentId = BaseContext.getCurrentId();
        //购物车设置为当前用户
        shoppingCart.setUserId(currentId);

        LambdaQueryWrapper<ShoppingCart> shoppingCartLambdaQueryWrapper = new LambdaQueryWrapper<>();
        shoppingCartLambdaQueryWrapper.eq(ShoppingCart::getUserId,currentId);

        //2.判断购物车中是菜品还是套餐
        Long dishId = shoppingCart.getDishId();
        if (dishId != null) {//是菜品
            shoppingCartLambdaQueryWrapper.eq(ShoppingCart::getDishId,dishId);
        }else {//是套餐
            shoppingCartLambdaQueryWrapper.eq(ShoppingCart::getSetmealId,shoppingCart.getSetmealId());
        }

        //查询当前菜品或者套餐是否在购物车中
        //SQL:select * from shopping_cart where user_id = ? and dish_id/setmeal_id = ?
        ShoppingCart shoppingCartServiceOne = shoppingCartService.getOne(shoppingCartLambdaQueryWrapper);

        String dishFlavor = shoppingCart.getDishFlavor();
        //判断 如果是同一菜品或套餐 数量+1 （同一菜品但口味不同这里算相同数据&& shoppingCartServiceOne.getDishFlavor().equals(dishFlavor)）
        if (shoppingCartServiceOne != null ) {
            Integer number = shoppingCartServiceOne.getNumber();
            shoppingCartServiceOne.setNumber(number + 1);
            //刷新
            shoppingCartService.updateById(shoppingCartServiceOne);
        }else {//不存在,则直接添加到DB
            shoppingCart.setNumber(1);//默认为1
            shoppingCart.setCreateTime(LocalDateTime.now());
            shoppingCartService.save(shoppingCart);
            shoppingCartServiceOne = shoppingCart;//赋值
        }
        //返回
        return R.success(shoppingCartServiceOne);
    }


    /**
     * 查看购物车
     * @return
     */
    @GetMapping("/list")
    @ApiOperation(value = "查看购物车接口")
    public R<List<ShoppingCart>> list() {
        log.info("查看购物车...");
        //获取当前用户id
        Long currentId = BaseContext.getCurrentId();
        //根据当前用户id去查询购物车
        LambdaQueryWrapper<ShoppingCart> shoppingCartLambdaQueryWrapper = new LambdaQueryWrapper<>();
        shoppingCartLambdaQueryWrapper.eq(ShoppingCart::getUserId,currentId);
        shoppingCartLambdaQueryWrapper.orderByAsc(ShoppingCart::getCreateTime);
        //查询
        List<ShoppingCart> list = shoppingCartService.list(shoppingCartLambdaQueryWrapper);
        return R.success(list);
    }

    /**
     * 清空购物车
     * @return
     */
    @DeleteMapping("/clean")
    @ApiOperation(value = "清空购物车接口")
    public R<String> clean() {
        //获取当前用户id
        Long currentId = BaseContext.getCurrentId();
        //SQL:delete from shopping_cart where user_id = ?
        LambdaQueryWrapper<ShoppingCart> shoppingCartLambdaQueryWrapper = new LambdaQueryWrapper<>();
        shoppingCartLambdaQueryWrapper.eq(ShoppingCart::getUserId,currentId);

        shoppingCartService.remove(shoppingCartLambdaQueryWrapper);
        return R.success("购物车清空成功...");
    }

}
