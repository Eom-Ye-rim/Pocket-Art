package com.pocekt.art.controller;


import com.pocekt.art.auth.AuthUser;
import com.pocekt.art.dto.request.ContestRequest;
import com.pocekt.art.entity.Users;
import com.pocekt.art.service.ContestService;
import com.pocekt.art.service.S3Service;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import springfox.documentation.annotations.ApiIgnore;

import java.io.IOException;
import java.util.List;

@RestController
@RequestMapping("/api/v1/contest")
@Slf4j
@RequiredArgsConstructor
public class ContestController {

    private final ContestService contestService;
    private final S3Service s3Service;

    @PreAuthorize("hasAnyRole('USER')")
    @GetMapping("")
    public ResponseEntity getContestList(@PageableDefault Pageable pageable, @RequestParam(required = false) String title, @RequestParam(required = false) String contents) {
        return contestService.getContestList(pageable, title,contents);

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
                                       ContestRequest contestRequest, @RequestPart("files") List<MultipartFile> files ) throws IOException {
        if(files ==null){
            throw new IllegalArgumentException("wrong input image");
        }
        List<String> photoList = s3Service.upload(files);
        return contestService.createContest(users, contestRequest,photoList);
        //return new ResponseEntity(new ApiRes("스터디 등록 성공", HttpStatus.CREATED), HttpStatus.CREATED);
    }

    @PreAuthorize("hasAnyRole('USER')")
    @PutMapping(value = "/{contestId}",consumes = MediaType.MULTIPART_FORM_DATA_VALUE )
    public ResponseEntity updateContest(@ApiIgnore @AuthUser Users users, @PathVariable Long contestId,
                                      ContestRequest contestRequest) throws IOException {
        return contestService.updateContest(users, contestId,contestRequest);
        //return new ResponseEntity(new ApiRes("스터디 수정 성공", HttpStatus.OK), HttpStatus.OK);
    }


    @PreAuthorize("hasAnyRole('USER')")
    @DeleteMapping("/{contestId}/delete")
    public ResponseEntity deleteStudyById(@ApiIgnore @AuthUser Users users,  @PathVariable Long contestId) {
        return contestService.deleteStudyById(users, contestId);
    }

}



