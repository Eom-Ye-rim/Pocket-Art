package com.pocekt.art.repository.comment;


import com.pocekt.art.dto.response.CommentResponse;
import com.pocekt.art.entity.Comment;
import com.querydsl.jpa.impl.JPAQueryFactory;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import static com.pocekt.art.dto.response.CommentResponse.convertCommentToDto;
import static com.pocekt.art.entity.QComment.comment;
import java.util.*;


@RequiredArgsConstructor
@Repository
public class CommentRepositoryImpl implements CommentCustomRepository{

    private final JPAQueryFactory queryFactory;

    @Override
    public List<CommentResponse> findByContestId(Long id) {

        List<Comment> comments = queryFactory.selectFrom(comment)
                .leftJoin(comment.parent).fetchJoin()
                .where(comment.contest.id.eq(id))
                .orderBy(comment.parent.id.asc().nullsFirst(),
                        comment.createDate.asc())
                .fetch();

        List<CommentResponse> commentResponseDTOList = new ArrayList<>();
        Map<Long, CommentResponse> commentDTOHashMap = new HashMap<>();

        comments.forEach(c -> {
            CommentResponse commentResponseDTO = convertCommentToDto(c);
            commentDTOHashMap.put(commentResponseDTO.getId(), commentResponseDTO);
            if (c.getParent() != null) commentDTOHashMap.get(c.getParent().getId()).getChildren().add(commentResponseDTO);
            else commentResponseDTOList.add(commentResponseDTO);
        });
        return commentResponseDTOList;
    }

    @Override
    public Optional<Comment> findCommentByIdWithParent(Long id) {

        Comment selectedComment = queryFactory.select(comment)
                .from(comment)
                .leftJoin(comment.parent).fetchJoin()
                .where(comment.id.eq(id))
                .fetchOne();

        return Optional.ofNullable(selectedComment);
    }
}
