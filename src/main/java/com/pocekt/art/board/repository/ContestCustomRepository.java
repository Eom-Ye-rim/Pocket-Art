package com.pocekt.art.board.repository;


import com.pocekt.art.dto.response.ContestPageResponse;
import com.pocekt.art.board.domain.BoardType;
import com.pocekt.art.board.domain.Contest;
import com.pocekt.art.board.domain.SearchType;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;

import java.util.List;

public interface ContestCustomRepository {

    void updateLikeCount(Contest contest);
    void subLikeCount(Contest contest);

    List<Contest> findTop5ContestsByLikes();

    PageImpl<ContestPageResponse> getContestList(Pageable pageable);

    PageImpl<ContestPageResponse> getQuestionListPageWithSearch(BoardType boardType, SearchType searchType, Pageable pageable);



}
