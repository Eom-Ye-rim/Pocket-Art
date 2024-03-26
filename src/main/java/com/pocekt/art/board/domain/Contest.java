package com.pocekt.art.board.domain;

import com.fasterxml.jackson.annotation.JsonBackReference;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.pocekt.art.entity.BaseTime;
import com.pocekt.art.entity.Comment;
import com.pocekt.art.entity.HashTag;
import com.pocekt.art.entity.Photo;
import com.pocekt.art.user.domain.Users;
import java.util.Objects;
import lombok.*;
import org.hibernate.annotations.ColumnDefault;

import javax.persistence.*;
import java.util.ArrayList;
import java.util.List;

@Entity
@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
public class Contest extends BaseTime {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "contest_id")
    private Long id;

    @Column
    private String title; //제목

    @Column
    private String author; //작성자

    @Column
    private String userImg; //작성자 프로필 사진

    @Column
    private String contents; //내용

    @Column
    private String style; // 화풍

    @ColumnDefault("0")
    @Column
    private int likecnt;

    @Enumerated(EnumType.STRING)
    private BoardType boardType;


    @ColumnDefault("0")
    @Column(name="VIEW_COUNT")
    private int viewCount; //조회수

    @OneToMany(mappedBy = "contest")

    @JsonIgnore
    private List<Photo> photoList =new ArrayList<>();

    @ManyToOne
    @JsonIgnore
    @JsonBackReference
    @JoinColumn(name="users_id")
    private Users users;

    @OneToMany(mappedBy = "contest")
    @JsonIgnore
    private List<Comment> commentList = new ArrayList<>();
    @OneToMany(mappedBy = "contest")
    @JsonIgnore
    private List<HashTag> tagList=new ArrayList<>();

    @Builder
    public Contest(String title, String author, String contents, String userImg, BoardType boardType, String style,Users users) {
        this.title = title;
        this.author=author;
        this.userImg=userImg;
        this.contents=contents;
        this.boardType=boardType;
        this.style=style;
        this.users = users;

    }

    public void addHashtag(HashTag hashTag){
        tagList.add(hashTag);
        hashTag.setContest(this);
    }

    public void addPhoto(Photo photo){
        photoList.add(photo);
        photo.setContest(this);
    }

    public static boolean checkContestLoginUser(Users users, Contest contest) {
        return Objects.equals(contest.getUsers().getName(), users.getName());
    }
}

/*
* 제목
* 작성자
* 내용
* 카테고리
* 화풍
* 조회수
* 댓글 수
* 좋아요 수
* 사진
* 댓글  * (추후)
* 인기 많은 사진 (좋아요 수-> top 5 뽑는 것까지)
*  */