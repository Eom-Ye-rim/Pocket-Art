package com.pocekt.art.repository.comment;


import com.pocekt.art.entity.Comment;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface CommentRepository extends JpaRepository<Comment,Long>,CommentCustomRepository {
    List<Comment> findAllByUsers_Id(Long users_id);
}
