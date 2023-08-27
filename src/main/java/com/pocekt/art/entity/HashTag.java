package com.pocekt.art.entity;

import com.fasterxml.jackson.annotation.JsonBackReference;
import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.*;
import org.hibernate.annotations.OnDelete;
import org.hibernate.annotations.OnDeleteAction;

import javax.persistence.*;

@Entity
@Setter
@Getter
@NoArgsConstructor
public class HashTag {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column
    private String tagname;

    @ManyToOne
    @JsonIgnore
    @JsonBackReference
    @JoinColumn(name="contest_id")
    @OnDelete(action = OnDeleteAction.CASCADE)
    private Contest contest;
    public HashTag(String tagname,Contest contest){
        this.tagname=tagname;
        this.contest=contest;
    }

}
