package com.ithema.reggie.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.ithema.reggie.common.R;
import com.ithema.reggie.entity.Employee;
import com.ithema.reggie.service.EmployeeService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang.StringUtils;
import org.springframework.util.DigestUtils;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import java.time.LocalDateTime;

/**
 * @version 1.0
 * @Author LQH02
 * @Description
 * @CreateDate 2022/8/28 18:41
 */
@Slf4j
@RestController
@RequestMapping("/employee")
@Api(tags = "员工接口")
public class EmployeeController {

    @Resource
    private EmployeeService employeeService;

    /**
     * 员工登录
     * @param employee @RequestBody将前端传来的json数据封装成employee对象
     * @param request 可以将用户信息存入session中
     * @return
     */
    @PostMapping("/login")
    @ApiOperation(value = "员工登录接口")
    public R<Employee> login(@RequestBody Employee employee, HttpServletRequest request) {

        //1.将页面提交的密码password进行md5加密处理
        String password = employee.getPassword();
        password = DigestUtils.md5DigestAsHex(password.getBytes());

        //2.根据提交的用户名name去查询 员工
        LambdaQueryWrapper<Employee> lambdaQueryWrapper = new LambdaQueryWrapper<>();
        lambdaQueryWrapper.eq(Employee::getUsername,employee.getUsername());
        Employee emp = employeeService.getOne(lambdaQueryWrapper);

        //3.判断员工是否存在
        if (emp == null) {
            return R.error("登录失败");
        }
        //4.员工存在,比对密码
        if (!emp.getPassword().equals(password)) {
            return R.error("登录失败");
        }

        //5.查看员工状态,是否被禁用
        if (emp.getStatus() == 0) {
            return R.error("账号已禁用");
        }

        //6.登录成功,将员工id存入session中
        request.getSession().setAttribute("employee",emp.getId());

        return R.success(emp);
    }


    /**
     * 登出
     * @param request
     * @return
     */
    @PostMapping("/logout")
    @ApiOperation(value = "员工退出接口")
    public R<String> logout(HttpServletRequest request) {
        request.getSession().removeAttribute("employee");
        return R.success("退出成功");
    }

    /**
     * 管理员保存员工
     * @param employee
     * @param request
     * @return
     */
    @PostMapping
    @ApiOperation(value = "保存员工接口")
    public R save(@RequestBody Employee employee,HttpServletRequest request) {
        log.info("新增员工信息: {}",employee.toString());
        //给员工设置初始密码123456
        employee.setPassword(DigestUtils.md5DigestAsHex("123456".getBytes()));
        //设置创建时间和修改时间
        employee.setCreateTime(LocalDateTime.now());
        employee.setUpdateTime(LocalDateTime.now());

        //获取session域中的管理员id
        Long empId = (Long) request.getSession().getAttribute("employee");
        //管理员设置创建人和修改人
        employee.setCreateUser(empId);
        employee.setUpdateUser(empId);

        employeeService.save(employee);

        return R.success("新增员工成功");

    }

    /**
     * 分页 + 带条件
     * @param page
     * @param pageSize
     * @param name
     * @return
     */
    @GetMapping("/page")
    @ApiOperation("员工带条件分页查询接口")
    public R<Page> page(@RequestParam("page") Integer page,
                        @RequestParam("pageSize") Integer pageSize,
                        String name) {

        log.info("page={} pageSize={} name={}",page,pageSize,name);

        //1.构造分页构造器
        Page pageInfo = new Page(page, pageSize);

        //2.构造条件构造器
        LambdaQueryWrapper<Employee> lambdaQueryWrapper = new LambdaQueryWrapper<>();
        //动态sql, 相当于当name不为null时(if(name!=null)), 我们再进行模糊查询 (like name=${name})
        lambdaQueryWrapper.like(StringUtils.isNotEmpty(name),Employee::getName,name);
        //添加排序条件
        lambdaQueryWrapper.orderByDesc(Employee::getUpdateTime);
        //执行查询
        employeeService.page(pageInfo,lambdaQueryWrapper);
        return R.success(pageInfo);
    }

    /**
     * 编辑 禁用 修改操作
     * @param employee
     * @return R<String> 前端只需要判断一下code 所以用string
     */
    @PutMapping
    @ApiOperation(value = "编辑/禁用/修改接口")
    public R<String> update(@RequestBody Employee employee,HttpServletRequest request) {
        log.info("employee{}",employee);

        long id = Thread.currentThread().getId();
        log.info("线程id={}",id);

        //将公共属性提取出来作为一个公共属性自动填充类MyMetaObjectHandler
        //填充属性,设置此时修改时间和修改人
        /*Long empId = (Long) request.getSession().getAttribute("employee");
        employee.setUpdateTime(LocalDateTime.now());
        employee.setUpdateUser(empId);*/

        //执行修改
        employeeService.updateById(employee);

        return R.success("员工修改成功");
    }

    /**
     * 根据id获取员工信息
     * @param id
     * @return
     */
    @GetMapping("/{id}")
    @ApiOperation(value = "根据id查询员工接口")
    public R<Employee> getById(@PathVariable("id") String id) {
        log.info("修改员工的id={}",id);
        Employee employee = employeeService.getById(id);
        return R.success(employee);
    }
}
