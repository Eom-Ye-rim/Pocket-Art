package com.pocekt.art.dto.request;

import com.pocekt.art.entity.BoardType;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ContestRequest {
    private String title;
    private String contents;
    private String category;
    private String style;
    private BoardType type;


}
