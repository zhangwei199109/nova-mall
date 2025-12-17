package com.example.order.service.convert;

import com.example.order.api.dto.OrderDTO;
import com.example.order.api.dto.OrderItemDTO;
import com.example.order.service.entity.Order;
import com.example.order.service.entity.OrderItem;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import java.util.List;

@Mapper(componentModel = "spring")
public interface OrderConvert {

    OrderDTO toDTO(Order entity);

    List<OrderDTO> toDTOs(List<Order> entities);

    Order toEntity(OrderDTO dto);

    OrderItemDTO toItemDTO(OrderItem entity);

    List<OrderItemDTO> toItemDTOs(List<OrderItem> entities);

    @Mapping(target = "orderId", source = "orderId")
    OrderItem toItemEntity(OrderItemDTO dto, Long orderId);
}

