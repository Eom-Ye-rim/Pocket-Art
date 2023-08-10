package com.pocekt.art.dto.response;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.databind.PropertyNamingStrategy;
import com.fasterxml.jackson.databind.annotation.JsonNaming;
import com.pocekt.art.entity.Contest;
import com.pocekt.art.entity.Photo;
import lombok.Getter;
import lombok.Setter;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Getter
@Setter
@JsonNaming(value = PropertyNamingStrategy.SnakeCaseStrategy.class)
public class ContestPageResponse {
    //제목, 작성자, 조회수 , 댓글수
    private String title; //제목
    private String author; //작성자
    private int view_count; //조회수
    private int likeCount; // 좋아요 수
    private int comment_cnt; //댓글 수
    private String firstPhoto;


    public ContestPageResponse(Contest contest) {

        this.title = contest.getTitle();
        this.author = contest.getAuthor();
        this.view_count = contest.getViewCount();
        this.comment_cnt = contest.getCommentList().size();
        List<Photo> photoList = contest.getPhotoList();
        if (photoList != null && !photoList.isEmpty()) {
            this.firstPhoto = photoList.get(0).getFileUrl();
        }
    }

}
