package com.pocekt.art.controller;


import com.pocekt.art.auth.AuthUser;
import com.pocekt.art.dto.request.UserRequestDto;
import com.pocekt.art.dto.response.Response;
import com.pocekt.art.entity.Users;
import com.pocekt.art.service.MyPageService;
import com.pocekt.art.service.UsersService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import springfox.documentation.annotations.ApiIgnore;

import java.io.IOException;


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
    public Object getUsersPost(@ApiIgnore @AuthUser Users users) throws IOException {

        return mypageService.getContestByUserId(users);
    }

//    @PreAuthorize("hasAnyRole('USER')")
//    @GetMapping(value = "/comment")
//    public Object getUsersComment(@ApiIgnore @AuthUser Users users) throws IOException {
//
//        return mypageService.getCommentByUserId(users);
//    }

}
