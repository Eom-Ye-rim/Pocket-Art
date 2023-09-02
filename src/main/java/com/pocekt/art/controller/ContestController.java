package com.pocekt.art.controller;


import com.pocekt.art.auth.AuthUser;
import com.pocekt.art.dto.request.ContestRequest;
import com.pocekt.art.dto.response.ContestPageResponse;
import com.pocekt.art.entity.BoardType;
import com.pocekt.art.entity.SearchType;
import com.pocekt.art.entity.Users;
import com.pocekt.art.service.ContestService;
import com.pocekt.art.service.S3Service;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import springfox.documentation.annotations.ApiIgnore;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

@RestController
@RequestMapping("/api/v1/contest")
@Slf4j
@RequiredArgsConstructor
public class ContestController {

    private final ContestService contestService;
    private final S3Service s3Service;

//    @PreAuthorize("hasAnyRole('USER')")
    @PostMapping("/all")
    public ResponseEntity getCommunityList(
            @RequestParam(required = false) BoardType boardType, @RequestBody SearchType searchCondition, Pageable pageable) {
        PageImpl<ContestPageResponse> responseDTO;

        System.out.println(boardType);
        //검색조건중 모든 내용을 입력하지 않고 요청을 보냈을 때 일반 목록 페이지 출력
        if (boardType==null && searchCondition.getContent().isEmpty() && searchCondition.getWriter().isEmpty() && searchCondition.getTitle().isEmpty()) {
            responseDTO = contestService.getContestList(pageable);
        } else {
            if (boardType == null) {
                boardType = BoardType.ALL;
            }
            responseDTO = contestService.getPageListWithSearch(boardType,searchCondition, pageable);

        }
        return ResponseEntity.ok()
                .body(responseDTO);
    }


    @GetMapping("/best")
    public ResponseEntity getBestImageList(){
        return contestService.getTop5ContestsByLikes(); //조회수 뭔가 이상한 것 같기도 하고 ..?
    }



    @PreAuthorize("hasAnyRole('USER')")
    @GetMapping("/{contestId}")
    public ResponseEntity findById(@ApiIgnore @AuthUser Users users, @PathVariable("contestId") Long contestId) {

        return contestService.findById(users,contestId);
    }


    @PreAuthorize("hasAnyRole('USER')")
    @PostMapping(value = "",consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity writeContest(@ApiIgnore @AuthUser Users users,
                                        @RequestPart ContestRequest contestRequest, @RequestPart(required = false) List<MultipartFile> files ) throws IOException {

        System.out.println(contestRequest.getContents());


        return contestService.createContest(users, contestRequest,files);
    }

    @PreAuthorize("hasAnyRole('USER')")
    @PutMapping(value = "/{contestId}",consumes = MediaType.MULTIPART_FORM_DATA_VALUE )
    public ResponseEntity updateContest(@ApiIgnore @AuthUser Users users, @PathVariable Long contestId,
                                      @RequestPart(value = "contestRequest") ContestRequest contestRequest,@RequestPart(required=false ) List<MultipartFile> files) throws IOException {

        return contestService.updateContest(users, contestId,contestRequest,files);
        //return new ResponseEntity(new ApiRes("스터디 수정 성공", HttpStatus.OK), HttpStatus.OK);
    }


    @PreAuthorize("hasAnyRole('USER')")
    @DeleteMapping("/{contestId}/delete")
    public ResponseEntity deleteStudyById(@ApiIgnore @AuthUser Users users,  @PathVariable Long contestId) {
        return contestService.deleteStudyById(users, contestId);
    }

}



