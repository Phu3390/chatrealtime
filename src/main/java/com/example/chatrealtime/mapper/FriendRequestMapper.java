package com.example.chatrealtime.mapper;

import java.util.List;

import org.mapstruct.Mapper;

import com.example.chatrealtime.dto.response.FriendRequestResponse;
import com.example.chatrealtime.entity.FriendRequest;

@Mapper(componentModel = "spring", imports = { UserMapper.class })
public interface FriendRequestMapper {
    FriendRequestResponse toResponse(FriendRequest entity);

    List<FriendRequestResponse> toListResponse(List<FriendRequest> entities);
}
