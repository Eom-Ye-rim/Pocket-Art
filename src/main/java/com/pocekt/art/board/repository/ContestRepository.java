package com.pocekt.art.board.repository;


import com.pocekt.art.board.domain.Contest;
import org.springframework.context.annotation.Primary;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Primary
@Repository
public interface ContestRepository extends JpaRepository<Contest,Long> , ContestCustomRepository {
    List<Contest> findAllByUsers_Id(Long users_id);
}
