package com.pocekt.art.board.dto.response;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.databind.PropertyNamingStrategy;
import com.fasterxml.jackson.databind.annotation.JsonNaming;
import com.pocekt.art.entity.Comment;
import com.pocekt.art.board.domain.Contest;
import com.pocekt.art.entity.HashTag;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@JsonNaming(value = PropertyNamingStrategy.SnakeCaseStrategy.class)
public record ContestResponse (
     Long id,
     String author,
     String title,
     String contents,
     int viewCount,

     List<String> photoList,
     List<String> tagList,
     List<Comment> commentList,
     String userImg,

    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd'T'HH:mm:ss", timezone = "Asia/Seoul")
     LocalDateTime createdDate,

    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd'T'HH:mm:ss", timezone = "Asia/Seoul")
     LocalDateTime updateDate
    ){

    public static ContestResponse of(Contest contest) {
        List<String> tagList = new ArrayList<>(); // tagList를 초기화
        List<HashTag> hashtagList = contest.getTagList();
        if (hashtagList != null) {
            for (HashTag tag : hashtagList) {
                String hashtagName = tag.getTagname();
                if (hashtagName != null) {
                    tagList.add(hashtagName);
                }
            }
        }
        return new ContestResponse(
            contest.getId(),
            contest.getAuthor(),
            contest.getTitle(),
            contest.getContents(),
            contest.getViewCount(),
            contest.getPhotoList().stream()
                .map(photo -> photo.getFileUrl())
                .filter(file -> file != null)
                .collect(Collectors.toList()),
            tagList,
            contest.getCommentList(),
            contest.getUserImg(),
            contest.getCreateDate(),
            contest.getModifiedDate()
        );
    }
}
