package com.pocekt.art.user.repository;


import static com.pocekt.art.user.exception.UserErrorCode.EMAIL_ALREADY_REGISTERED;
import static com.pocekt.art.user.exception.UserErrorCode.USER_NOT_FOUND;

import com.pocekt.art.common.CustomException;
import com.pocekt.art.user.domain.Users;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface UsersRepository extends JpaRepository<Users, Long> {

    Optional<Users> findByEmail(String email);
    boolean existsByEmail(String email);

    default Users getById(Long userId) {
        return findById(userId)
            .orElseThrow(() ->  new CustomException(USER_NOT_FOUND));
    }

}

