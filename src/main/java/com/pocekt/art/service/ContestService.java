package com.pocekt.art.service;


import com.pocekt.art.dto.request.ContestRequest;
import com.pocekt.art.dto.response.ContestPageResponse;
import com.pocekt.art.dto.response.ContestResponse;
import com.pocekt.art.dto.response.Response;
import com.pocekt.art.entity.Contest;
import com.pocekt.art.entity.Photo;
import com.pocekt.art.entity.Users;
import com.pocekt.art.repository.PhotoRepository;
import com.pocekt.art.repository.UsersRepository;
import com.pocekt.art.repository.contest.ContestRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Pageable;
import org.springframework.data.redis.core.HashOperations;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.IOException;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

@RequiredArgsConstructor
@Slf4j
@Service
public class ContestService {


    private final ContestRepository contestRepository;
    private final UsersRepository usersRepository;

    private final PhotoRepository photoRepository;

    private final S3Service s3Service;

    private final Response response;

    private static final String CONTEST_VIEW_COUNT_KEY = "contest:viewCount:";
    private static final int EXPIRATION_DAYS = 1;
    private final RedisTemplate redisTemplate;



    public class RedisUtil {
        public static long getUnixTime(LocalDateTime localDateTime) {
            return localDateTime.toEpochSecond(ZoneOffset.UTC);
        }
    }


    public ResponseEntity getContestList(Pageable pageable, String title, String contents) {
        try {
            List<ContestPageResponse> contestPageResponseList = contestRepository.findPageContest(pageable, title, contents);

            return response.success(contestPageResponseList, "컨테스트 페이지 조회 성공", HttpStatus.OK);
        }
            catch (Exception e) {
                return response.fail("컨테스트 페이지 조회 실패",HttpStatus.BAD_REQUEST);
            }

    }
    //컨테스트 response 하나더 만들기 
    public ResponseEntity getTop5ContestsByLikes() {
        List<Contest> contestImage=contestRepository.findTop5ContestsByLikes();
        List<ContestResponse> contestResponses = new ArrayList<>();

        for (Contest contest : contestImage) {
            contestResponses.add(new ContestResponse(contest));
        }
        return response.success(contestResponses, "Top 게시글 확인", HttpStatus.OK);

    }

    public ResponseEntity findById(Users users, Long contestId) {
        try {
            String contestViewCountKey = CONTEST_VIEW_COUNT_KEY + contestId;

            HashOperations<String, String, Long> hashOps = redisTemplate.opsForHash();

            // 현재 시간을 UTC 기준으로 계산
            LocalDateTime now = LocalDateTime.now(ZoneOffset.UTC);
            LocalDateTime expirationTime = now.plusDays(EXPIRATION_DAYS);

            long unixTime = RedisUtil.getUnixTime(expirationTime);
            long viewCount = 0;

            // Redis hash에 사용자 이름과 만료 시간을 저장
            boolean isNewUser = hashOps.putIfAbsent(contestViewCountKey, users.getName(), expirationTime.toEpochSecond(ZoneOffset.UTC));
            if (!isNewUser) {
                // 이미 사용자가 저장되어 있으면 만료 시간을 가져옴
                Long expirationTimestamp = hashOps.get(contestViewCountKey, users.getName());
                if (expirationTimestamp != null && expirationTimestamp < now.toEpochSecond(ZoneOffset.UTC)) {
                    // 저장된 만료 시간이 지났으면 사용자 아이디와 만료 시간을 갱신
                    System.out.println("시간 만료");
                    hashOps.put(contestViewCountKey, users.getName(), expirationTime.toEpochSecond(ZoneOffset.UTC));
                } else {
                    // 아직 만료 시간이 지나지 않았으면 조회수를 증가시키지 않음
                    Contest contest = contestRepository.findById(contestId).orElseThrow(() -> new RuntimeException("Contest post not found"));
                    System.out.println(contest.getViewCount());

                }
            } else {
                // 새로운 사용자라면 만료 시간(하루) 설정
                redisTemplate.expireAt(contestViewCountKey, Instant.ofEpochSecond(unixTime));
                viewCount = hashOps.increment(contestViewCountKey, "viewCount", 1L);

            }
            Contest contest = contestRepository.findById(contestId).orElseThrow(() -> new RuntimeException("Contest post not found"));
            contest.setViewCount((int) viewCount);
            contestRepository.save(contest);
            System.out.println(contest.getViewCount());
            return response.success(new ContestResponse(contest), "컨테스트 상세 글 확인", HttpStatus.OK);
        }  catch (Exception e) {
            return response.fail("컨테스트 상세 글 확인 실패",HttpStatus.BAD_REQUEST);
        }

        // 조회수를 증가시키고 결과를 반환



    }

    //글 생성
    @Transactional
    public ResponseEntity createContest(Users users, ContestRequest contestRequest, List<String> files) throws IOException {
        try {
            Contest contest = Contest.builder()
                    .title(contestRequest.getTitle())
                    .author(users.getName())
                    .contents(contestRequest.getContents())
                    .category(contestRequest.getCategory())
                    .style(contestRequest.getStyle())
                    .users(users)
                    .build();

            Contest saveContest=contestRepository.save(contest);
            Users saveUsers = usersRepository.findById(users.getId()).get();
            saveUsers.getContestList().add(contest);
            List<String> photoList=new ArrayList<>();

            for (String file : files) {
                    Photo photo =new Photo(file,contest);
                    photoRepository.save(photo);
                    saveContest.writePhoto(photo);
                    photoList.add(photo.getFileUrl());

                }

            return response.success(new ContestResponse(contest), "컨테스트 글 등록 성공", HttpStatus.OK);
        } catch (Exception e) {
            return response.fail(e,"컨테스트 글 등록 실패",HttpStatus.BAD_REQUEST);
        }
    }

    private void postBlankCheck(List<String> files){
        if(files==null || files.isEmpty()){
            throw new IllegalArgumentException("Wrong image path");
        }
    }

    //수정
    @Transactional
    public ResponseEntity updateContest(Users users, Long contestId, ContestRequest contestRequest) throws IOException {

        try {
            Contest contest = contestRepository.findById(contestId).orElseThrow(() -> new IllegalArgumentException(String.format("contest is not Found!")));
            List<Photo> photo = photoRepository.findByContestId(contestId);
            if (checkContestLoginUser(users, contest)) {
                contest.setTitle(contestRequest.getTitle());
                contest.setAuthor(users.getName());
                contest.setContents(contestRequest.getContents());
            }

            usersRepository.save(users);
            return response.success(new ContestResponse(contest), "컨테스트 글 수정 성공", HttpStatus.OK);
        } catch (Exception e) {
            return response.fail("컨테스트 글 수정 실패",HttpStatus.BAD_REQUEST);
        }
    }

    //삭제
    @Transactional
    public ResponseEntity deleteStudyById(Users users, Long contestId) {
        try {
            Contest contest = contestRepository.findById(contestId).orElseThrow(() -> new IllegalArgumentException(String.format("stydy is not Found!")));
            List<Photo> photo = photoRepository.findByContestId(contestId);
            if (checkContestLoginUser(users, contest)) {

                for (Photo existingFile : photo) {
                    s3Service.deleteFile(existingFile.getFileUrl());
                }

                photoRepository.deleteByContestId(contestId);
                contestRepository.deleteById(contestId);
            }
            return response.success(new ContestResponse(contest), "컨테스트 글 삭제 성공", HttpStatus.OK);
        } catch (Exception e) {
            return response.fail("컨테스트 글 삭제 실패",HttpStatus.BAD_REQUEST);
        }
    }


    //수정 및 삭제 권한 체크
    private boolean checkContestLoginUser(Users users, Contest contest) {
        if (!Objects.equals(contest.getUsers().getName(), users.getName())) {
            return false;
        }
        return true;

    }
}
