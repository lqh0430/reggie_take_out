package com.ithema.reggie.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.ithema.reggie.entity.ShoppingCart;
import com.ithema.reggie.mapper.ShoppingCartMapper;
import com.ithema.reggie.service.ShoppingCartService;
import org.springframework.stereotype.Service;

/**
 * @version 1.0
 * @Author LQH02
 * @Description
 * @CreateDate 2022/9/1 17:47
 */
@Service
public class ShoppingCartServiceImpl extends ServiceImpl<ShoppingCartMapper, ShoppingCart>
implements ShoppingCartService {
}
