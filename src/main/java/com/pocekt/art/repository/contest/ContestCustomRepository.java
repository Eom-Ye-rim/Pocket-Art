package com.pocekt.art.repository.contest;


import com.pocekt.art.dto.response.ContestPageResponse;
import com.pocekt.art.entity.BoardType;
import com.pocekt.art.entity.Contest;
import com.pocekt.art.entity.SearchType;
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
