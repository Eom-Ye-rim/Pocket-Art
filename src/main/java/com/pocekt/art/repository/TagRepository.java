package com.pocekt.art.repository;

import com.pocekt.art.entity.HashTag;
import org.springframework.data.jpa.repository.JpaRepository;

public interface TagRepository extends JpaRepository<HashTag,Long> {
}
