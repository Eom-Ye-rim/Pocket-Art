package com.pocekt.art.dto.response;



import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.databind.PropertyNamingStrategy;
import com.fasterxml.jackson.databind.annotation.JsonNaming;
import com.pocekt.art.entity.Contest;
import com.pocekt.art.entity.Photo;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Getter
@Setter
@JsonNaming(value = PropertyNamingStrategy.SnakeCaseStrategy.class)
public class ContestResponse {
    private Long id;
    private String author;
    private String title;
    private String contents;
    private int viewCount;

    private List<String> photoList;


    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd'T'HH:mm:ss", timezone = "Asia/Seoul")
    private LocalDateTime createdDate;
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd'T'HH:mm:ss", timezone = "Asia/Seoul")
    private LocalDateTime updateDate;

    public ContestResponse(Contest contest) {
        this.id=contest.getId();
        this.author = contest.getAuthor();
        this.title = contest.getTitle();
        this.contents = contest.getContents();
        this.viewCount = contest.getViewCount();
        this.createdDate = contest.getCreateDate();
        this.photoList = new ArrayList<>();
        List<Photo> photoList = contest.getPhotoList();
        if (photoList != null) {
            for (Photo photo : photoList) {
                String fileUrl = photo.getFileUrl();
                if (fileUrl != null) {
                    this.photoList.add(fileUrl);
                }
            }

        }
    }
}
