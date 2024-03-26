package com.pocekt.art.entity;

import com.pocekt.art.board.domain.Contest;
import lombok.*;
import org.hibernate.annotations.OnDelete;
import org.hibernate.annotations.OnDeleteAction;

import javax.persistence.*;
import java.util.UUID;

@Entity
@Getter
@Builder
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class Photo {
    @Id
    @GeneratedValue
    @Column(name = "photo_id", columnDefinition = "BINARY(16)")
    private UUID id;

    @Column
    private String fileName;

    @Column
    private String fileUrl;

    @Column
    private Long fileSize;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "contest_id")
    @OnDelete(action = OnDeleteAction.CASCADE)
    private Contest contest;

    public Photo(String fileUrl,Contest contest){
        this.fileUrl=fileUrl;
        this.contest=contest;
    }


}
