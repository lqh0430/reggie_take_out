package com.ithema.reggie.service;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.extension.service.IService;
import com.ithema.reggie.entity.OrderDetail;
import com.ithema.reggie.entity.Orders;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;

/**
 * @version 1.0
 * @Author LQH02
 * @Description
 * @CreateDate 2022/9/1 18:51
 */

public interface OrdersService extends IService<Orders> {
    void submit(Orders orders);

    List<OrderDetail> getOrderDetailListByOrderId(Long orderId);
}
