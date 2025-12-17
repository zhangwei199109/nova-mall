package com.example.user.web.convert;

import com.example.user.api.dto.UserDTO;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface UserWebConvert {

    UserDTO toCreateDto(UserDTO dto);

    @Mapping(target = "id", source = "id")
    UserDTO toUpdateDto(Long id, UserDTO dto);
}

