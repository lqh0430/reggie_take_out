package com.ithema.reggie.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.ithema.reggie.entity.Employee;
import com.ithema.reggie.mapper.EmployeeMapper;
import com.ithema.reggie.service.EmployeeService;
import org.springframework.stereotype.Service;

/**
 * @version 1.0
 * @Author LQH02
 * @Description
 * @CreateDate 2022/8/28 18:35
 */
@Service
public class EmployeeServiceImpl extends ServiceImpl<EmployeeMapper,Employee> implements EmployeeService{

}
