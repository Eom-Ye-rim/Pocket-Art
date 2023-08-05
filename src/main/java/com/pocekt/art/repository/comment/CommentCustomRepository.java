package com.pocekt.art.repository.comment;



import com.pocekt.art.dto.response.CommentResponse;
import com.pocekt.art.entity.Comment;

import java.util.List;
import java.util.Optional;

public interface CommentCustomRepository {
    List<CommentResponse> findByContestId(Long id);

    Optional<Comment> findCommentByIdWithParent(Long id);
}
