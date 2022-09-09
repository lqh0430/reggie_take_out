package com.ithema.reggie.entity;

import com.baomidou.mybatisplus.annotation.*;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.databind.ser.std.ToStringSerializer;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import java.io.Serializable;
import java.time.LocalDateTime;

/**
菜品口味
 */
@Data
@ApiModel("菜品口味")
public class DishFlavor implements Serializable {

    private static final long serialVersionUID = 1L;

    @JsonSerialize(using = ToStringSerializer.class)
    @ApiModelProperty("主键菜品口味id")
    private Long id;

    //菜品id
    @JsonSerialize(using = ToStringSerializer.class)
    @ApiModelProperty("菜品id")
    private Long dishId;

    //口味名称
    @ApiModelProperty("口味名称")
    private String name;

    //口味数据list
    @ApiModelProperty("口味数据list")
    private String value;

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;


    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updateTime;


    @TableField(fill = FieldFill.INSERT)
    private Long createUser;


    @TableField(fill = FieldFill.INSERT_UPDATE)
    private Long updateUser;

    //是否删除
    @TableLogic //逻辑删除
    private Integer isDeleted;

}
