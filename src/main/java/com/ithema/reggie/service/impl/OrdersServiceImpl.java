package com.ithema.reggie.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.IdWorker;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.ithema.reggie.common.BaseContext;
import com.ithema.reggie.common.CustomException;
import com.ithema.reggie.entity.*;
import com.ithema.reggie.mapper.OrdersMapper;
import com.ithema.reggie.service.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

/**
 * @version 1.0
 * @Author LQH02
 * @Description
 * @CreateDate 2022/9/1 18:58
 */
@Service
public class OrdersServiceImpl extends ServiceImpl<OrdersMapper, Orders> implements OrdersService {

    @Resource
    private UserService userService;

    @Resource
    private ShoppingCartService shoppingCartService;

    @Resource
    private AddressBookService addressBookService;

    @Resource
    private OrderDetailService orderDetailService;

    /**
     * 生成订单
     * @param orders 前端传来的参数有address_book_id ,remark,payMethod等三个参数 封装成orders对象
     */
    @Override
    @Transactional
    public void submit(Orders orders) {
        //1.首先要获取当前用户id
        Long currentId = BaseContext.getCurrentId();
        //获取当前用户
        User user = userService.getById(currentId);

        //2.根据当前用户去查询 购物车 表 select * from shopping_cart where user_id = currentId;
        LambdaQueryWrapper<ShoppingCart> shoppingCartLambdaQueryWrapper = new LambdaQueryWrapper<>();
        shoppingCartLambdaQueryWrapper.eq(ShoppingCart::getUserId,currentId);
        List<ShoppingCart> cartList = shoppingCartService.list(shoppingCartLambdaQueryWrapper);
        //2.1对查询出来的购物车集合判断
        if (cartList == null || cartList.size() == 0) {
            throw new CustomException("购物车为空,不能下单");
        }

        //3.根据地址id去查询地址
        Long addressBookId = orders.getAddressBookId();
        AddressBook addressBook = addressBookService.getById(addressBookId);
        //判断(如果地址为空,就要跳转到地址填写页面)
        if (addressBook == null) {
            throw new CustomException("用户地址为空,不能下单");
        }

        //生成订单号
        long orderId = IdWorker.getId();
        //原子类 保证线程安全,线程中开辟空间存储数据 用于计算总价
        AtomicInteger amount = new AtomicInteger(0);

        //4.为订单明细对象 赋值(购物车和订单明细是对应的,所以把购物车里的属性值拷贝给订单明细)
        List<OrderDetail> orderDetailList = cartList.stream().map((item) -> {
            OrderDetail orderDetail = new OrderDetail();
            orderDetail.setOrderId(orderId);
            orderDetail.setNumber(item.getNumber());
            orderDetail.setDishFlavor(item.getDishFlavor());
            orderDetail.setDishId(item.getDishId());
            orderDetail.setSetmealId(item.getSetmealId());
            orderDetail.setName(item.getName());
            orderDetail.setImage(item.getImage());
            orderDetail.setAmount(item.getAmount());
            //合计 计算
            amount.addAndGet(item.getAmount().multiply(new BigDecimal(item.getNumber())).intValue());
            //返回订单明细对象
            return orderDetail;
        }).collect(Collectors.toList());

        //5.为 订单对象 赋值
        orders.setId(orderId);
        orders.setUserName(user.getName());
        orders.setUserId(currentId);
        orders.setNumber(String.valueOf(orderId));
        orders.setStatus(2);//待派送
        orders.setUserId(currentId);
        //orders.setAddressBookId();
        orders.setOrderTime(LocalDateTime.now());
        orders.setCheckoutTime(LocalDateTime.now());
        //orders.setPayMethod();
        orders.setAmount(new BigDecimal(amount.get()));//总金额
        orders.setPhone(addressBook.getPhone());
        orders.setConsignee(addressBook.getConsignee());
        //地址
        orders.setAddress((addressBook.getProvinceName() == null ? "" : addressBook.getProvinceName())
                + (addressBook.getCityName() == null ? "" : addressBook.getCityName())
                + (addressBook.getDistrictName() == null ? "" : addressBook.getDistrictName())
                + (addressBook.getDetail() == null ? "" : addressBook.getDetail()));

        //6.向订单表插入 一条订单数据
        this.save(orders);

        //7.向订单明细表插入多条数据
        orderDetailService.saveBatch(orderDetailList);

        //8.清空购物车
        shoppingCartService.remove(shoppingCartLambdaQueryWrapper);
    }

    /**
     * 根据订单id查询订单明细表
     * @param orderId 订单id
     * @return 订单明细集合
     */
    @Override
    public List<OrderDetail> getOrderDetailListByOrderId(Long orderId) {

        LambdaQueryWrapper<OrderDetail> orderDetailLambdaQueryWrapper = new LambdaQueryWrapper<>();
        orderDetailLambdaQueryWrapper.eq(orderId != null,OrderDetail::getOrderId,orderId);
        List<OrderDetail> orderDetailList = orderDetailService.list(orderDetailLambdaQueryWrapper);
        return orderDetailList;
    }

}
