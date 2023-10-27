package com.pocekt.art.service;


import com.amazonaws.services.kms.model.NotFoundException;
import com.pocekt.art.dto.request.UserRequestDto;
import com.pocekt.art.dto.response.ContestPageResponse;
import com.pocekt.art.dto.response.ContestResponse;
import com.pocekt.art.dto.response.MypageContestResponse;
import com.pocekt.art.dto.response.Response;
import com.pocekt.art.entity.*;
import com.pocekt.art.repository.LikeRepository;
import com.pocekt.art.repository.UsersRepository;
import com.pocekt.art.repository.comment.CommentRepository;
import com.pocekt.art.repository.contest.ContestRepository;
import lombok.RequiredArgsConstructor;
import org.aspectj.weaver.ast.Not;
import org.opencv.aruco.Board;
import org.springframework.data.domain.PageImpl;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class MyPageService {
    private final UsersRepository usersRepository;
    private final ContestRepository contestRepository;
    private final CommentRepository commentRepository;
    private final PasswordEncoder passwordEncoder;

    private final LikeRepository likeRepository;
    private final S3Service s3Service;
    private final Response response;

    @Transactional
    public ResponseEntity<?> getInfo(Users user) {
        Users users = usersRepository.findById(user.getId()).orElseThrow(() -> new IllegalArgumentException(String.format("user not Found!")));
        return response.success(users, "회원 정보를 성공적으로 불러왔습니다.", HttpStatus.CREATED);
    }
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

//            if(!file.isEmpty()){
//               String newImg= s3Service.upload(file);
//                users.setProfileImg(newImg);
//            }
            return response.success(users, "회원 정보를 성공적으로 수정하였습니다.", HttpStatus.CREATED);
        }
        catch (Exception e) {
            return response.fail("회원 정보 수정 실패",HttpStatus.BAD_REQUEST);
        }

    }

    public ResponseEntity getContestByUserId(Users users) {
        List<Contest> contestList = contestRepository.findAllByUsers_Id(users.getId());
        List<ContestPageResponse> contestResponses = new ArrayList<>();

        for (Contest contest : contestList) {
            contestResponses.add(new ContestPageResponse(contest));
        }

        return response.success(contestResponses, "컨테스트 리스트 확인", HttpStatus.OK);
    }
    public List<Contest> getLikedContestsByUser(Users users) {
        // 사용자가 좋아요를 클릭한 게시글 목록 가져오기
        List<Likes> likedLikes = likeRepository.findByUsers(users);

        // 좋아요를 클릭한 게시글들을 가져오기
        List<Contest> likedContests = likedLikes.stream()
                .map(like -> like.getContest())
                .collect(Collectors.toList());

        return likedContests;
    }

    //댓글 조회
    public Object getCommentByUserId(Users users) {
        List<Comment> commentList = commentRepository.findAllByUsers_Id(users.getId());

        return commentList;
    }
    public List<String> getMyImgaeList(Users users, BoardType boardType) {
        List<Contest> contestList = contestRepository.findAllByUsers_Id(users.getId());
        System.out.println(boardType);
        List<Contest> filteredContests = null;
        if (boardType == BoardType.AI) {
            filteredContests = contestList.stream()
                    .filter(contest -> contest.getBoardType() == BoardType.AI)
                    .collect(Collectors.toList());
        }
        if (boardType == BoardType.일반) {
            filteredContests = contestList.stream()
                    .filter(contest -> contest.getBoardType() == BoardType.일반)
                    .collect(Collectors.toList());
        }
        List<ContestPageResponse> contestResponses = new ArrayList<>();

        for (Contest contest : filteredContests) {
            contestResponses.add(new ContestPageResponse(contest));
        }

        List<String> firstPhotos = contestResponses.stream()
                .map(contest -> contest.getFirstPhoto()) // 각 Contest에서 getFirstPhoto 호출
                .collect(Collectors.toList());

        return firstPhotos;
    }
}
