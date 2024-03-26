package com.pocekt.art.service;



import com.amazonaws.services.kms.model.NotFoundException;
import com.pocekt.art.dto.response.Response;
import com.pocekt.art.board.domain.Contest;
import com.pocekt.art.entity.Likes;
import com.pocekt.art.user.domain.Users;
import com.pocekt.art.repository.LikeRepository;
import com.pocekt.art.user.repository.UsersRepository;
import com.pocekt.art.board.repository.ContestRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class LikeService {
    private final LikeRepository likeRepository;
    private final UsersRepository usersRepository;
    private final ContestRepository contestRepository;
    private final Response response;


    public ResponseEntity<?> insert(Users user, Long contestId) throws Exception {
        Users users=usersRepository.findById(user.getId())
                .orElseThrow(()->new NotFoundException("Could not found user"));

        Contest contest=contestRepository.findById(contestId)
                .orElseThrow(()->new NotFoundException("Could not found contest"));

        if (likeRepository.findByUsersAndContest(users,contest).isPresent()){
            throw new IllegalArgumentException("already exist data");
        }
        Likes likes = Likes.builder()
                .contest(contest)
                .users(users)
                .build();

        likeRepository.save(likes);
        contestRepository.updateLikeCount(contest);


        return response.success(contest.getLikecnt(), "좋아요를 눌렀습니다..", HttpStatus.CREATED);
    }


    public ResponseEntity<?> delete(Users user, Long contestId) {

        Users users=usersRepository.findById(user.getId())
                .orElseThrow(()->new NotFoundException("Could not found user"));

        Contest contest=contestRepository.findById(contestId)
                .orElseThrow(()->new NotFoundException("Could not found contest"));

        Likes likes = likeRepository.findByUsersAndContest(users, contest)
                .orElseThrow(() -> new NotFoundException("Could not found heart id"));

        likeRepository.delete(likes);
        contestRepository.subLikeCount(contest);

        return response.success(contest.getLikecnt(), "좋아요를 취소하였습니다..", HttpStatus.CREATED);
    }
}
