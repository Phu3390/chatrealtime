// package com.example.chatrealtime.mapper;

// import org.mapstruct.Mapper;
// import org.mapstruct.Mapping;

// import com.example.chatrealtime.dto.request.SendFriendRequest;
// import com.example.chatrealtime.entity.FriendRequest;

// @Mapper(componentModel = "spring")
// public interface FriendRequestMapper {
//     @Mapping(target = "id", ignore = true)
//     @Mapping(target = "passwordHash", ignore = true)
//     @Mapping(target = "createdAt", ignore = true)
//     @Mapping(target = "status", ignore = true)
//     FriendRequest toEntity(SendFriendRequest request);
// }
