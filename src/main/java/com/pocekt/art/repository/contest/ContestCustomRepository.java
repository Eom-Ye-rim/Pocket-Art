package com.pocekt.art.repository.contest;


import com.pocekt.art.dto.response.ContestPageResponse;
import com.pocekt.art.entity.Contest;
import org.springframework.data.domain.Pageable;

import java.util.List;

public interface ContestCustomRepository {
    List<ContestPageResponse> findPageContest(Pageable pageable, String title, String contents);
    void updateLikeCount(Contest contest);
    void subLikeCount(Contest contest);

    List<Contest> findTop5ContestsByLikes();


}
