package com.pocekt.art.service;


import com.pocekt.art.dto.request.UserRequestDto;
import com.pocekt.art.dto.response.Response;
import com.pocekt.art.entity.Contest;
import com.pocekt.art.entity.Users;
import com.pocekt.art.repository.UsersRepository;
import com.pocekt.art.repository.comment.CommentRepository;
import com.pocekt.art.repository.contest.ContestRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class MyPageService {
    private final UsersRepository usersRepository;
    private final ContestRepository contestRepository;
    private final CommentRepository commentRepository;
    private final PasswordEncoder passwordEncoder;
    private final Response response;
    @Transactional
    public ResponseEntity<?> updateInfo(Users user, UserRequestDto.Info info) {
        try {
            Users users = usersRepository.findById(user.getId()).orElseThrow(() -> new IllegalArgumentException(String.format("user not Found!")));
            if (info.getEmail()!=null){
                users.setEmail(info.getEmail());
            }
            if(info.getPassword()!=null){
                users.setPassword(passwordEncoder.encode(info.getPassword()));
            }
            if(info.getName()!=null){
                users.setName(info.getName());
            }

            return response.success(users, "회원 정보를 성공적으로 수정하였습니다.", HttpStatus.CREATED);
        }
        catch (Exception e) {
            return response.fail("회원 정보 수정 실패",HttpStatus.BAD_REQUEST);
        }

    }

    public Object getContestByUserId(Users users) {
        List<Contest> contestList = contestRepository.findAllByUsers_Id(users.getId());
        System.out.println(contestList);

        return contestList;
    }

    //댓글 조회
//    public Object getCommentByUserId(Users users) {
//        List<Comment> commentList = commentRepository.findAllByUsers_Id(users.getId());
//
//        return commentList;
//    }




}
