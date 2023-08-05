package com.pocekt.art.repository;


import com.pocekt.art.entity.Photo;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface PhotoRepository extends JpaRepository<Photo,Long> {
    List<Photo> findByContestId(Long contestId);

    Optional<Photo> deleteByContestId(Long contestId);
}
