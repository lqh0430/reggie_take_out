package com.ithema.reggie.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.ithema.reggie.dto.SetmealDto;
import com.ithema.reggie.entity.Setmeal;

import java.util.List;

/**
 * @version 1.0
 * @Author LQH02
 * @Description
 * @CreateDate 2022/8/30 15:39
 */
public interface SetmealService extends IService<Setmeal> {
    void saveWithDish(SetmealDto setmealDto);

    void deleteWithDish(List<Long> ids);

    void updateSetmealStatusById(Integer status, List<Long> ids);

    SetmealDto getSetmealDtoData(Long id);
}
