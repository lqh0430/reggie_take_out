package com.ithema.reggie.entity;

import com.alibaba.fastjson.annotation.JSONField;
import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.databind.ser.std.ToStringSerializer;
import io.swagger.annotations.ApiModelProperty;
import io.swagger.annotations.ApiOperation;
import lombok.Data;
import java.io.Serializable;
import java.math.BigDecimal;

/**
 * 订单明细
 */
@Data
@ApiOperation("订单明细")
public class OrderDetail implements Serializable {

    private static final long serialVersionUID = 1L;

    @JsonSerialize(using = ToStringSerializer.class)
    @ApiModelProperty("主键订单明细id")
    private Long id;

    //名称
    @ApiModelProperty("订单名称")
    private String name;

    //订单id
    @JsonSerialize(using = ToStringSerializer.class)
    @ApiModelProperty("订单id")
    private Long orderId;

    //菜品id
    @JsonSerialize(using = ToStringSerializer.class)
    @ApiModelProperty("菜品id")
    private Long dishId;

    //套餐id
    @JsonSerialize(using = ToStringSerializer.class)
    @ApiModelProperty("套餐id")
    private Long setmealId;

    //口味
    @ApiModelProperty("口味")
    private String dishFlavor;

    //数量
    @ApiModelProperty("数量")
    private Integer number;

    //金额
    @ApiModelProperty("金额")
    private BigDecimal amount;

    //图片
    @ApiModelProperty("图片")
    private String image;
}
