package com.ithema.reggie.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.ithema.reggie.entity.DishFlavor;
import com.ithema.reggie.mapper.DishFlavorMapper;
import com.ithema.reggie.service.DishFlavorService;
import com.ithema.reggie.service.DishService;
import org.springframework.stereotype.Service;

/**
 * @version 1.0
 * @Author LQH02
 * @Description
 * @CreateDate 2022/8/30 19:03
 */
@Service
public class DishFlavorServiceImpl extends ServiceImpl<DishFlavorMapper, DishFlavor>
implements DishFlavorService {
}
