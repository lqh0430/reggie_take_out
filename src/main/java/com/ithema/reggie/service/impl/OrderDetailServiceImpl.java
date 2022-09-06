package com.ithema.reggie.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.ithema.reggie.entity.OrderDetail;
import com.ithema.reggie.mapper.OrderDetailMapper;
import com.ithema.reggie.service.OrderDetailService;
import org.springframework.stereotype.Service;

/**
 * @version 1.0
 * @Author LQH02
 * @Description
 * @CreateDate 2022/9/1 18:59
 */
@Service
public class OrderDetailServiceImpl extends ServiceImpl<OrderDetailMapper, OrderDetail> implements OrderDetailService {
}
