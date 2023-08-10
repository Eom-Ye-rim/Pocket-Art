package com.pocekt.art.dto.response;


import com.pocekt.art.entity.Comment;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.List;

@Getter
@NoArgsConstructor
public class CommentResponse {
    private Long id;
    private String content;

    private String author;
    private List<CommentResponse> children = new ArrayList<>();

    public CommentResponse(Long id, String content, String author) {
        this.id = id;
        this.author = author;
        this.content = content;

    }

    public static CommentResponse convertCommentToDto(Comment comment) {
        return comment.getIsDeleted() ?
                new CommentResponse(comment.getId(), "삭제된 댓글입니다.", null) :
                new CommentResponse(comment.getId(), comment.getContent(),comment.getAuthor());
    }

}
