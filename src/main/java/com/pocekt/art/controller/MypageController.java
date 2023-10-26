package com.pocekt.art.controller;


import com.pocekt.art.auth.AuthUser;
import com.pocekt.art.dto.request.UserRequestDto;
import com.pocekt.art.dto.response.ContestPageResponse;
import com.pocekt.art.dto.response.Response;
import com.pocekt.art.entity.BoardType;
import com.pocekt.art.entity.SearchType;
import com.pocekt.art.entity.Users;
import com.pocekt.art.service.MyPageService;
import com.pocekt.art.service.UsersService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import springfox.documentation.annotations.ApiIgnore;

import java.io.IOException;
import java.util.List;


@RequiredArgsConstructor
@RestController
@RequestMapping("/api/v1/my")
public class MypageController {
    private final UsersService usersService;
    private final MyPageService mypageService;
    private final Response response;

    @PreAuthorize("hasAnyRole('USER')")
    @PostMapping(value = "")
    public ResponseEntity updateInfo(@ApiIgnore @AuthUser Users users,
                                     @RequestBody UserRequestDto.Info info ) throws IOException {

        return mypageService.updateInfo(users,info);
        //return new ResponseEntity(new ApiRes("스터디 등록 성공", HttpStatus.CREATED), HttpStatus.CREATED);
    }

    @PreAuthorize("hasAnyRole('USER')")
    @GetMapping(value = "")
    public ResponseEntity getUsersPost(@ApiIgnore @AuthUser Users users) throws IOException {

        return mypageService.getContestByUserId(users);
    }

    @PreAuthorize("hasAnyRole('USER')")
    @GetMapping(value = "/comment")
    public Object getUsersComment(@ApiIgnore @AuthUser Users users) throws IOException {

        return mypageService.getCommentByUserId(users);
    }

    @PreAuthorize("hasAnyRole('USER')")
    @GetMapping(value = "/like")
    public Object getUsersLikes(@ApiIgnore @AuthUser Users users) throws IOException {

        return mypageService.getLikedContestsByUser(users);
    }


    @GetMapping("/all")
    public ResponseEntity getCommunityList(@ApiIgnore @AuthUser Users users,
            @RequestParam(required = true) BoardType boardType){
        System.out.println(boardType);
        //검색조건중 모든 내용을 입력하지 않고 요청을 보냈을 때 일반 목록 페이지 출력
        List<String> photolist=mypageService.getMyImgaeList(users,boardType);

        return ResponseEntity.ok().body(photolist);
    }

}
