package com.pocekt.art.service;


import com.amazonaws.services.kms.model.NotFoundException;
import com.pocekt.art.dto.request.CommentRequest;
import com.pocekt.art.dto.request.CommentRequestMapper;
import com.pocekt.art.dto.response.CommentResponse;
import com.pocekt.art.dto.response.Response;
import com.pocekt.art.entity.Comment;
import com.pocekt.art.board.domain.Contest;
import com.pocekt.art.user.domain.Users;
import com.pocekt.art.user.repository.UsersRepository;
import com.pocekt.art.repository.comment.CommentRepository;
import com.pocekt.art.board.repository.ContestRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class CommentService {

    private final Response response;
    private final CommentRepository commentRepository;
    private final UsersRepository usersRepository;
    private final ContestRepository contestRepository;
    private final CommentRequestMapper commentRequestMapper;

    @Transactional
    public ResponseEntity<?> insert(Long contestId, Users user, CommentRequest commentRequestDTO) {

        Users users = usersRepository.findById(user.getId())
                .orElseThrow(() -> new NotFoundException("Could not found user id"));

        Contest contest = contestRepository.findById(contestId)
                .orElseThrow(() -> new NotFoundException("Could not found contest id  "));

        Comment comment = commentRequestMapper.toEntity(commentRequestDTO);

        Comment parentComment;
        if (commentRequestDTO.getParentId() != null) {
            System.out.println("실행");
            parentComment = commentRepository.findById(commentRequestDTO.getParentId())
                    .orElseThrow(() -> new NotFoundException("Could not found comment id : " + commentRequestDTO.getParentId()));
            comment.setParent(parentComment);
        }

        comment.setAuthor(users.getName());
        comment.setContest(contest);
        comment.setContent(commentRequestDTO.getContent());
        comment.setImg(users.getProfileImg());
        comment.setUsers(users);

        commentRepository.save(comment);
        users.getCommentList().add(comment);

        return response.success(new CommentResponse(comment.getId(),comment.getAuthor(),comment.getContent(), comment.getImg()), "댓글을 등록 하였습니다..", HttpStatus.CREATED);

    }

    @Transactional
    public ResponseEntity<?> delete(Long commentId) {
        Comment comment = commentRepository.findCommentByIdWithParent(commentId)
                .orElseThrow(() -> new NotFoundException("Could not found comment id : " + commentId));
        if(comment.getChildren().size() != 0) { // 자식이 있으면 상태만 변경
            comment.changeIsDeleted(true);
        } else { // 삭제 가능한 조상 댓글을 구해서 삭제
            commentRepository.delete(getDeletableAncestorComment(comment));
        }

        return response.success(comment, "댓글을 삭제 하였습니다..", HttpStatus.OK);
    }

    private Comment getDeletableAncestorComment(Comment comment) {
        Comment parent = comment.getParent(); // 현재 댓글의 부모를 구함
        if(parent != null && parent.getChildren().size() == 1 && parent.getIsDeleted())
            // 부모가 있고, 부모의 자식이 1개(지금 삭제하는 댓글)이고, 부모의 삭제 상태가 TRUE인 댓글이라면 재귀
            return getDeletableAncestorComment(parent);
        return comment; // 삭제해야하는 댓글 반환
    }

    @Transactional
    public ResponseEntity<?> update(Long commentId, CommentRequest commentRequestDTO) {

        Comment comment = commentRepository.findById(commentId)
                .orElseThrow(() -> new NotFoundException("Could not found comment id : " + commentId));
        //TODO 해당 메서드를 호출하는 사옹자와 댓글을 작성한 작성자가 같은지 확인하는 로직이 필요함
        comment.setContent(commentRequestDTO.getContent());

        return response.success(comment, "댓글을 수정 하였습니다..", HttpStatus.OK);
    }
}