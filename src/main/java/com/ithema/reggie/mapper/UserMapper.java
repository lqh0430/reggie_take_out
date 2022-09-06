package com.ithema.reggie.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.ithema.reggie.entity.User;
import org.apache.ibatis.annotations.Mapper;

/**
 * @version 1.0
 * @Author LQH02
 * @Description
 * @CreateDate 2022/9/1 10:39
 */
@Mapper
public interface UserMapper extends BaseMapper<User> {
}
