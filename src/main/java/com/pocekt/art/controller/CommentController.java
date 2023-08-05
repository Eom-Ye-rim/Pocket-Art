package com.pocekt.art.controller;


import com.pocekt.art.auth.AuthUser;
import com.pocekt.art.dto.request.CommentRequest;
import com.pocekt.art.entity.Users;
import com.pocekt.art.service.CommentService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/comment")
public class CommentController {

    private final CommentService commentService;

    @PostMapping("/{contestId}")
    public ResponseEntity<?> insert(@PathVariable Long contestId,@AuthUser Users user,
                                          @RequestBody CommentRequest commentRequestDTO) {


        return commentService.insert(contestId,user, commentRequestDTO);

    }

    @PutMapping("/{commentId}")
    public ResponseEntity<?> update(@PathVariable Long commentId, @RequestBody CommentRequest commentRequestDTO) {
        return commentService.update(commentId, commentRequestDTO);
        // TODO 뭘 return 하는게 좋을지 고민해보자

    }

    @DeleteMapping("/{commentId}")
    //TODO return Type 명시하기
    public ResponseEntity<?> delete(@PathVariable Long commentId) {
       return  commentService.delete(commentId);

    }

}