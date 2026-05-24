package com.example.chatrealtime.mapper;

import java.util.List;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;

import com.example.chatrealtime.dto.request.RegisterRequest;
import com.example.chatrealtime.dto.request.UpdateProfileRequest;
import com.example.chatrealtime.dto.response.UserResponse;
import com.example.chatrealtime.entity.User;

@Mapper(componentModel = "spring")
public interface UserMapper {
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "passwordHash", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "status", ignore = true)
    User toEntity(RegisterRequest request);

    UserResponse toResponse(User entity);
    
    List<UserResponse> toResponseList(List<User> entities);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "passwordHash", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "status", ignore = true)
    void updateUserEntity(@MappingTarget User entity, UpdateProfileRequest request);
}