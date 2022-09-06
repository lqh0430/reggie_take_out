package com.ithema.reggie.dto;

import com.ithema.reggie.entity.OrderDetail;
import com.ithema.reggie.entity.Orders;
import lombok.Data;
import java.util.List;

@Data
public class OrdersDto extends Orders {

    private String userName;

    private String phone;

    private String address;

    private String consignee;

    private List<OrderDetail> orderDetails;
	
}
