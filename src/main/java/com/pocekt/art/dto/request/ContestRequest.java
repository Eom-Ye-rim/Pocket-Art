package com.pocekt.art.dto.request;

import com.pocekt.art.entity.BoardType;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
public class ContestRequest {
    private String title;
    private String contents;
    private String style;
    private BoardType type;

    private List<String> hashtag;



}
