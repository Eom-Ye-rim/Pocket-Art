package com.pocekt.art.repository;

import com.pocekt.art.board.domain.Contest;
import com.pocekt.art.entity.Likes;
import com.pocekt.art.user.domain.Users;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface LikeRepository extends JpaRepository <Likes,Long> {
    Optional<Likes> findByUsersAndContest(Users users, Contest contest);

    List<Likes> findByUsers(Users users);
}
