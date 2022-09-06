package com.ithema.reggie.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.fasterxml.jackson.databind.ser.Serializers;
import com.ithema.reggie.entity.DishFlavor;
import org.apache.ibatis.annotations.Mapper;

/**
 * @version 1.0
 * @Author LQH02
 * @Description
 * @CreateDate 2022/8/30 19:01
 */
@Mapper
public interface DishFlavorMapper extends BaseMapper<DishFlavor> {
}
