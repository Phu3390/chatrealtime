package com.example.chatrealtime.mapper;

import java.util.List;

import org.mapstruct.Mapper;

import com.example.chatrealtime.dto.response.MessageResponse;
import com.example.chatrealtime.entity.Message;

@Mapper(componentModel = "spring", uses = { UserMapper.class })
public interface MessageMapper {
    MessageResponse toResponse(Message entity);

    List<MessageResponse> toResponseList(List<Message> entities);
}
