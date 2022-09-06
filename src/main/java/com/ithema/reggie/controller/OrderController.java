package com.ithema.reggie.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.ithema.reggie.common.BaseContext;
import com.ithema.reggie.common.R;
import com.ithema.reggie.dto.OrdersDto;
import com.ithema.reggie.entity.OrderDetail;
import com.ithema.reggie.entity.Orders;
import com.ithema.reggie.service.OrdersService;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang.StringUtils;
import org.omg.PortableInterceptor.INACTIVE;
import org.springframework.beans.BeanUtils;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import java.util.List;
import java.util.stream.Collectors;

/**
 * @version 1.0
 * @Author LQH02
 * @Description
 * @CreateDate 2022/9/1 19:00
 */
@RestController
@Slf4j
@RequestMapping("/order")
public class OrderController {

    @Resource
    private OrdersService ordersService;

    /**
     * 生成订单
     * @param orders
     * @return
     */
    @PostMapping("/submit")
    public R<String> submit(@RequestBody Orders orders) {
        log.info("订单信息={}",orders);
        ordersService.submit(orders);
        return R.success("下单成功");
    }

    /**
     * 后台 订单分页 + 日期条件 + 订单号查询
     * @param page
     * @param pageSize
     * @param number 订单号
     * @param beginTime 开始日期
     * @param endTime 终止日期
     * @return
     */
    @GetMapping("/page")
    public R<Page> page(@RequestParam("page") Integer page,
                            @RequestParam("pageSize") Integer pageSize,
                            String number,
                            String beginTime,
                            String endTime) {
        log.info("查询订单分页信息={} {}",page,pageSize);
        //构造分页器
        Page<Orders> ordersPage = new Page<>(page, pageSize);
        //添加条件
        LambdaQueryWrapper<Orders> ordersLambdaQueryWrapper = new LambdaQueryWrapper<>();
        ordersLambdaQueryWrapper.like(number != null,Orders::getNumber,number)
                .gt(StringUtils.isNotEmpty(beginTime), Orders::getOrderTime,beginTime)
                .lt(StringUtils.isNotEmpty(endTime), Orders::getOrderTime,endTime);
        //倒序
        ordersLambdaQueryWrapper.orderByDesc(Orders::getOrderTime);
        //查询
        ordersService.page(ordersPage,ordersLambdaQueryWrapper);
        return R.success(ordersPage);
    }

    /**
     * 前台 分页查询订单
     * @param page
     * @param pageSize
     * @return
     */
    @GetMapping("/userPage")
    public R<Page> userPage(@RequestParam("page") Integer page,
                            @RequestParam("pageSize") Integer pageSize) {
        log.info("查询订单分页信息={} {}",page,pageSize);

        //获取当前用户id
        Long currentId = BaseContext.getCurrentId();

        //1.构造分页器
        Page<Orders> ordersPage = new Page<>(page, pageSize);
        Page<OrdersDto> ordersDtoPage = new Page<>(page, pageSize);
        //2.添加条件
        LambdaQueryWrapper<Orders> ordersLambdaQueryWrapper = new LambdaQueryWrapper<>();
        ordersLambdaQueryWrapper.eq(Orders::getUserId,currentId);
        ordersLambdaQueryWrapper.orderByAsc(Orders::getOrderTime);
        //查询
        ordersService.page(ordersPage,ordersLambdaQueryWrapper);

        //3.拷贝对象(给orderDtoPage赋第一个值ordersPage)(先忽略records集合,因为此时是空的)
        BeanUtils.copyProperties(ordersPage,ordersDtoPage,"records");

        //4.拷贝records对象(该对象里面包含了订单明细)
        List<Orders> records = ordersPage.getRecords();
        List<OrdersDto> list = records.stream().map((item) -> {
            //构造ordersDto空对象
            OrdersDto ordersDto = new OrdersDto();
            //获取订单id
            Long orderId = item.getId();
            //根据订单id查询订单明细
            List<OrderDetail> orderDetailList = ordersService.getOrderDetailListByOrderId(orderId);

            //通过父类orders继承下来的属性 给ordersDto对象拷贝赋值
            BeanUtils.copyProperties(item,ordersDto);
            //(给ordersDto对象的第2个属性List<OrderDetail>拷贝赋值)
            ordersDto.setOrderDetails(orderDetailList);
            return ordersDto;
        }).collect(Collectors.toList());

        //5.设置records值
        ordersDtoPage.setRecords(list);

        return R.success(ordersDtoPage);
    }

}
