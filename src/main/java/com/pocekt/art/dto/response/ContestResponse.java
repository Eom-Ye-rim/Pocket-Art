package com.pocekt.art.dto.response;



import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.databind.PropertyNamingStrategy;
import com.fasterxml.jackson.databind.annotation.JsonNaming;
import com.pocekt.art.entity.Comment;
import com.pocekt.art.entity.Contest;
import com.pocekt.art.entity.HashTag;
import com.pocekt.art.entity.Photo;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

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
    private List<String> tagList;
    private List<Comment> commentList;
    private String userImg;

    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd'T'HH:mm:ss", timezone = "Asia/Seoul")
    private LocalDateTime createdDate;

    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd'T'HH:mm:ss", timezone = "Asia/Seoul")
    private LocalDateTime updateDate;

    public ContestResponse(Contest contest) {
        this.id = contest.getId();
        this.author = contest.getAuthor();
        this.title = contest.getTitle();
        this.contents = contest.getContents();
        this.userImg = contest.getUserImg();
        this.viewCount = contest.getViewCount();
        this.createdDate = contest.getCreateDate();

        // Extract photo URLs from the photo list
        this.photoList = contest.getPhotoList()
                .stream()
                .map(photo -> photo.getFileUrl())
                .filter(file -> file != null)
                .collect(Collectors.toList());

        // Extract tag names from the tag list
        this.tagList = new ArrayList<>();
        List<HashTag> hashtagList = contest.getTagList();
        if (hashtagList != null) {
            for (HashTag tag : hashtagList) {
                String hashtagName = tag.getTagname();
                if (hashtagName != null) {
                    this.tagList.add(hashtagName);
                }
            }
        }

        // Set the comment list
        this.commentList = contest.getCommentList(); // Assuming you have a getter for commentList in the Contest class
    }
}
