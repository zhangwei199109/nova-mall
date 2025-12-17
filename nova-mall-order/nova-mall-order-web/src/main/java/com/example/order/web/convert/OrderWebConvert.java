package com.example.order.web.convert;

import com.example.order.api.dto.CreateOrderRequest;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface OrderWebConvert {

    CreateOrderRequest toCreateRequest(CreateOrderRequest req);
}

