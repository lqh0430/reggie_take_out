package com.ithema.reggie.entity;

import com.baomidou.mybatisplus.annotation.FieldFill;
import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.databind.ser.std.ToStringSerializer;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 菜品
 */
@Data
@ApiModel("菜品")
public class Dish implements Serializable {

    private static final long serialVersionUID = 1L;

    @JsonSerialize(using = ToStringSerializer.class)
    @ApiModelProperty("主键菜品id")
    private Long id;

    //菜品名称
    @ApiModelProperty("菜品名称")
    private String name;

    //菜品分类id
    @JsonSerialize(using = ToStringSerializer.class)
    @ApiModelProperty("菜品分类id")
    private Long categoryId;

    //菜品价格
    @ApiModelProperty("菜品价格")
    private BigDecimal price;

    //商品码
    @ApiModelProperty("商品码")
    private String code;

    //图片
    @ApiModelProperty("图片")
    private String image;

    //描述信息
    @ApiModelProperty("描述信息")
    private String description;

    //0 停售 1 起售
    @ApiModelProperty("状态 0 停售 1 起售")
    private Integer status;

    //顺序
    @ApiModelProperty("排序")
    private Integer sort;

    //级联属性(多种菜品对应一种分类)
    //private Category category;


    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;


    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updateTime;


    @TableField(fill = FieldFill.INSERT)
    private Long createUser;


    @TableField(fill = FieldFill.INSERT_UPDATE)
    private Long updateUser;

}
