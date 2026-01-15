package com.example.user.service.convert;

import com.example.user.api.dto.AddressDTO;
import com.example.user.api.dto.UserDTO;
import com.example.user.service.entity.Address;
import com.example.user.service.entity.User;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.ReportingPolicy;

import java.util.List;

/**
 * 用户领域 DTO/实体转换器，基于 MapStruct。
 */
@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface UserMapStructMapper {

    UserDTO toUserDTO(User user);

    List<UserDTO> toUserDTOList(List<User> users);

    User toUserEntity(UserDTO dto);

    AddressDTO toAddressDTO(Address entity);

    List<AddressDTO> toAddressDTOList(List<Address> entities);

    @Mapping(target = "isDefault", expression = "java(dto.getIsDefault() == null ? 0 : dto.getIsDefault())")
    Address toAddressEntity(AddressDTO dto);
}


