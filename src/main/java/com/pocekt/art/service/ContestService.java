package com.pocekt.art.service;


import com.pocekt.art.dto.request.ContestRequest;
import com.pocekt.art.dto.response.ContestPageResponse;
import com.pocekt.art.dto.response.ContestResponse;
import com.pocekt.art.dto.response.Response;
import com.pocekt.art.entity.*;
import com.pocekt.art.repository.PhotoRepository;
import com.pocekt.art.repository.TagRepository;
import com.pocekt.art.repository.UsersRepository;
import com.pocekt.art.repository.contest.ContestRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.data.redis.core.HashOperations;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

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
    private final TagRepository tagRepository;

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


    //검색 기능 (제목, 제목 + 내용, 작성자 ) , 정렬 기능 (최신순, 좋아요 많은 순) , 카테고리 클릭
    public PageImpl<ContestPageResponse> getContestList(Pageable pageable) {
        PageImpl<ContestPageResponse> result = contestRepository.getContestList(pageable);
        return result;


    }

    public PageImpl<ContestPageResponse> getPageListWithSearch(BoardType boardType, SearchType searchCondition, Pageable pageable) {
        PageImpl<ContestPageResponse> result = contestRepository.getQuestionListPageWithSearch(boardType, searchCondition, pageable);
        return result;
    }

    //컨테스트 response 하나더 만들기 
    public ResponseEntity getTop5ContestsByLikes() {
        List<Contest> contestImage = contestRepository.findTop5ContestsByLikes();
        List<ContestResponse> contestResponses = new ArrayList<>();

        for (Contest contest : contestImage) {
            contestResponses.add(new ContestResponse(contest));
        }
        return response.success(contestResponses, "Top 게시글 확인", HttpStatus.OK);

    }

    public ResponseEntity findById(Users users, Long contestId) {
        try {
            String contestViewCountKey = CONTEST_VIEW_COUNT_KEY + contestId;
            Contest contest = contestRepository.findById(contestId).orElseThrow(() -> new RuntimeException("Contest post not found"));
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
                    contest = contestRepository.findById(contestId).orElseThrow(() -> new RuntimeException("Contest post not found"));
                    System.out.println(contest.getViewCount());

                }
            } else {
                // 새로운 사용자라면 만료 시간(하루) 설정
                redisTemplate.expireAt(contestViewCountKey, Instant.ofEpochSecond(unixTime));
                viewCount = hashOps.increment(contestViewCountKey, "viewCount", 1L);


                contest = contestRepository.findById(contestId).orElseThrow(() -> new RuntimeException("Contest post not found"));
                contest.setViewCount((int) viewCount);
                contestRepository.save(contest);
                System.out.println(contest.getViewCount());
            }
            return response.success(new ContestResponse(contest), "컨테스트 상세 글 확인", HttpStatus.OK);
        } catch (Exception e) {
            return response.fail("컨테스트 상세 글 확인 실패", HttpStatus.BAD_REQUEST);
        }

        // 조회수를 증가시키고 결과를 반환


    }

    //글 생성
    @Transactional
    public ResponseEntity createContest(Users users, String title, String contents,String style,BoardType type,List<String> hashtag, List<MultipartFile> files) throws IOException {
        try {
            System.out.println(users.getProfileImg());
            Contest contest = Contest.builder()
                    .title(title)
                    .boardType(type)
                    .author(users.getName())
                    .userImg(users.getProfileImg())
                    .contents(contents)
                    .style(style)
                    .users(users)
                    .build();

            Users saveUsers = usersRepository.findById(users.getId()).get();
            saveUsers.getContestList().add(contest);
            if (hashtag!=null) {
                List<String> tagList = new ArrayList<>();
                for (String tag :hashtag) {
                    HashTag hashTag = new HashTag(tag, contest);
                    tagRepository.save(hashTag);
                    contest.addHashtag(hashTag);
                    tagList.add(tag);
                }
            }
            if (files!=null) {
                List<Photo> photoList = new ArrayList<>();
                for (MultipartFile multipartFile : files) {
                    Photo photo = Photo.builder()
                            .fileName(multipartFile.getOriginalFilename())
                            .fileSize(multipartFile.getSize())
                            .fileUrl(s3Service.upload(multipartFile))
                            .contest(contest)
                            .build();
                    photoList.add(photo);

                }

                photoRepository.saveAll(photoList);
                contest.setPhotoList(photoList);
            }
            Contest saveContest = contestRepository.save(contest);
            return response.success(new ContestResponse(saveContest), "컨테스트 글 등록 성공", HttpStatus.OK);
        } catch (Exception e) {
            return response.fail(e, "컨테스트 글 등록 실패", HttpStatus.BAD_REQUEST);
        }
    }

    private void postBlankCheck(List<String> files) {
        if (files == null || files.isEmpty()) {
            throw new IllegalArgumentException("Wrong image path");
        }
    }

    //수정
    @Transactional
    public ResponseEntity updateContest(Users users, Long contestId, ContestRequest contestRequest, List<MultipartFile> files) throws IOException {

        try {
            Contest contest = contestRepository.findById(contestId).orElseThrow(() -> new IllegalArgumentException(String.format("contest is not Found!")));

            if (!checkContestLoginUser(users, contest)) {
                return response.fail("컨테스트 수정 실패", HttpStatus.UNAUTHORIZED);
            }

            contest.setTitle(contestRequest.getTitle());
            contest.setAuthor(users.getName());
            contest.setContents(contestRequest.getContents());

            if (!files.isEmpty()) {
                List<Photo> existingPhotos = photoRepository.findByContestId(contestId);
                // 기존 photo 삭제
                photoRepository.deleteAll(existingPhotos);

                for (Photo photo : existingPhotos) {
                    s3Service.deleteFile(photo.getFileUrl());
                }

                //새롭게 등록
                List<Photo> photoList = new ArrayList<>();
                for (MultipartFile multipartFile : files) {
                    Photo photo = Photo.builder()
                            .fileName(multipartFile.getOriginalFilename())
                            .fileSize(multipartFile.getSize())
                            .fileUrl(s3Service.upload(multipartFile))
                            .contest(contest)
                            .build();
                    photoList.add(photo);
                }

                photoRepository.saveAll(photoList);
            }
            users.getContestList().add(contest);
            usersRepository.save(users);
            return response.success("컨테스트 글 수정 성공", HttpStatus.OK);
        } catch (Exception e) {
            return response.fail("컨테스트 글 수정 실패", HttpStatus.BAD_REQUEST);
        }
    }


    //삭제
    @Transactional
    public ResponseEntity deleteContestById(Users users, Long contestId) {
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
