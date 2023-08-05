package com.pocekt.art.dto.request;


import com.pocekt.art.common.GenericMapper;
import com.pocekt.art.entity.Comment;
import org.mapstruct.Mapper;
import org.mapstruct.factory.Mappers;

@Mapper(componentModel = "spring")
public interface CommentRequestMapper extends GenericMapper<CommentRequest, Comment> {
    CommentRequestMapper INSTANCE = Mappers.getMapper(CommentRequestMapper.class);
}