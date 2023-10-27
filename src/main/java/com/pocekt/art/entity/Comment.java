package com.pocekt.art.entity;

import lombok.*;
import net.minidev.json.annotate.JsonIgnore;
import org.hibernate.annotations.ColumnDefault;

import javax.persistence.*;
import java.util.ArrayList;
import java.util.List;

@Entity
@Getter
@Builder
@AllArgsConstructor
@Setter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Comment extends BaseTime{
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name="comment_id")
    private Long id;

    @Column(nullable = false, length = 500)
    private String content;

    @ColumnDefault("FALSE")
    private Boolean isDeleted;

    private String author;

    private String img;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name="users_id")
    @JsonIgnore
    private Users users;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name="contest_id")
    private Contest contest;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "parent_id")

    private Comment parent;


    //children 댓글 추가에 대해
    @OneToMany(mappedBy = "parent", orphanRemoval = true)
    @Builder.Default
    private List<Comment> children = new ArrayList<>();


    public Comment(String content) {
         this.content = content;
    }



    public void changeIsDeleted(Boolean isDeleted) {
        this.isDeleted = isDeleted;
    }



}

