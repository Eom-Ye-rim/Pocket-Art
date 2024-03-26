package com.pocekt.art.board.dto.request;

import com.pocekt.art.board.domain.BoardType;
import java.util.List;

public record ContestRequest (
     String title,
     String contents,
     String style,
     BoardType type,
     List<String> hashtag
){ }
