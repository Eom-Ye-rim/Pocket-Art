package com.pocekt.art.repository.contest;


import com.pocekt.art.entity.Contest;
import org.springframework.context.annotation.Primary;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Primary
@Repository
public interface ContestRepository extends JpaRepository<Contest,Long> , ContestCustomRepository {
    List<Contest> findAllByUsers_Id(UUID users_id);
}
