package com.sky.service.impl;

import com.github.pagehelper.Page;
import com.github.pagehelper.PageHelper;
import com.sky.constant.MessageConstant;
import com.sky.constant.PasswordConstant;
import com.sky.constant.StatusConstant;
import com.sky.context.BaseContext;
import com.sky.dto.EmployeeDTO;
import com.sky.dto.EmployeeLoginDTO;
import com.sky.dto.EmployeePageQueryDTO;
import com.sky.entity.Employee;
import com.sky.exception.AccountLockedException;
import com.sky.exception.AccountNotFoundException;
import com.sky.exception.PasswordErrorException;
import com.sky.mapper.EmployeeMapper;
import com.sky.result.PageResult;
import com.sky.service.EmployeeService;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.DigestUtils;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;

@Service
public class EmployeeServiceImpl implements EmployeeService {

    @Autowired
    private EmployeeMapper employeeMapper;

    /**
     * 员工登录
     *
     * @param employeeLoginDTO
     * @return
     */
    public Employee login(EmployeeLoginDTO employeeLoginDTO) {
        String username = employeeLoginDTO.getUsername();
        String password = employeeLoginDTO.getPassword();

        //1、根据用户名查询数据库中的数据
        Employee employee = employeeMapper.getByUsername(username);

        //2、处理各种异常情况（用户名不存在、密码不对、账号被锁定）
        if (employee == null) {
            //账号不存在
            throw new AccountNotFoundException(MessageConstant.ACCOUNT_NOT_FOUND);
        }

        //密码比对
        // 对前端所传密码进行MD5加密处理
        password = DigestUtils.md5DigestAsHex(password.getBytes());
        System.out.println(password);
        if (!password.equals(employee.getPassword())) {
            //密码错误
            throw new PasswordErrorException(MessageConstant.PASSWORD_ERROR);
        }

        if (employee.getStatus() == StatusConstant.DISABLE) {
            //账号被锁定
            throw new AccountLockedException(MessageConstant.ACCOUNT_LOCKED);
        }

        //3、返回实体对象
        return employee;
    }


    /**
     * 新增员工
     */

    @Override
    public void save(EmployeeDTO employeeDTO) {

        //将DTO数据封装到实体类中
        Employee employee = new Employee();

        //使用工具类拷贝相同属性名
        BeanUtils.copyProperties(employeeDTO, employee);

        //设置其他属性 软编码方便后期维护
        employee.setStatus(StatusConstant.ENABLE);

        employee.setPassword(DigestUtils.md5DigestAsHex(PasswordConstant.DEFAULT_PASSWORD.getBytes()));

        employee.setCreateTime(LocalDateTime.now());
        employee.setUpdateTime(LocalDateTime.now());

        //利用令牌token取出登录用户名
        employee.setCreateUser(BaseContext.getCurrentId());
        employee.setUpdateUser(BaseContext.getCurrentId());


        employeeMapper.save(employee);


    }

    /**
     * 分页查询
     *
     * @param employeePageQueryDTO
     * @return
     */

    @Override
    public PageResult pageQuery(EmployeePageQueryDTO employeePageQueryDTO) {

        //利用pageHelper设置查询页码等
        PageHelper.startPage(employeePageQueryDTO.getPage(), employeePageQueryDTO.getPageSize());

        Page<Employee> page = employeeMapper.pageQuery(employeePageQueryDTO);


        long total = page.getTotal();//总页数
        List<Employee> result = page.getResult();//查询结果

        return new PageResult(total, result);


    }

    /**
     * 启用禁用员工账号
     *
     * @param status
     * @param id
     */
    @Override
    public void startorend(Integer status, long id) {
        //将属性封装为实体类 进行动态sql查询
//        Employee employee = new Employee();
//        employee.setStatus(status);
//        employee.setId(id);

        Employee employee = Employee.builder()
                .status(status)
                .id(id).build();

        employeeMapper.update(employee);



    }
    /**
     * 根据id查询员工
     *
     */

    @Override
    public Employee getById(long id) {
        Employee employee=employeeMapper.getById(id);
        //掩盖密码
        employee.setPassword("*****");
        return employee;

    }

    @Override
    public void update(EmployeeDTO employeeDTO) {
        Employee employee=new Employee();
        BeanUtils.copyProperties(employeeDTO, employee);



        employee.setUpdateTime(LocalDateTime.now());
        employee.setUpdateUser(BaseContext.getCurrentId());

        employeeMapper.update(employee);
    }
}
