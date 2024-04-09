package com.pocekt.art.board.repository;


import static com.pocekt.art.board.domain.QContest.contest;

import com.pocekt.art.board.domain.QContest;
import com.pocekt.art.dto.response.ContestPageResponse;
import com.pocekt.art.board.domain.BoardType;
import com.pocekt.art.board.domain.Contest;
import com.pocekt.art.board.domain.SearchType;
import com.querydsl.core.BooleanBuilder;
import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.jpa.JPQLQuery;
import com.querydsl.jpa.impl.JPAQueryFactory;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.support.QuerydslRepositorySupport;

import java.util.List;
import java.util.stream.Collectors;



public class ContestCustomRepositoryImpl extends QuerydslRepositorySupport implements ContestCustomRepository {
    private JPAQueryFactory queryFactory;

    public ContestCustomRepositoryImpl(JPAQueryFactory jpaQueryFactory) {
        super(Contest.class);
        this.queryFactory = jpaQueryFactory;
    }

    @Override
    public void updateLikeCount(Contest contests) {

        queryFactory.update(contest)
                .set(contest.likecnt, contest.likecnt.add(1))
                .where(contest.eq(contests))
                .execute();

    }

    @Override
    public List<Contest> findTop5ContestsByLikes() {
        QContest contest = QContest.contest;
        return queryFactory.selectFrom(contest)
                .orderBy(contest.likecnt.desc())
                .limit(5)
                .fetch();
    }


    @Override
    public void subLikeCount(Contest contests) {

        queryFactory.update(contest)
                .set(contest.likecnt, contest.likecnt.subtract(1))
                .where(contest.eq(contests))
                .execute();

    }
    @Override
    public PageImpl<ContestPageResponse> getContestList(Pageable pageable) {
        System.out.println("실행");
        List<Contest> results = queryFactory
                .selectFrom(contest)
                .orderBy(contest.createDate.desc())
                .offset(pageable.getOffset())
                .limit(pageable.getPageSize())
                .fetch();

        long totalCount = queryFactory
                .selectFrom(contest)
                .fetchCount();

        List<ContestPageResponse> dtoList = results.stream()
                .map(ContestPageResponse::new)
                .collect(Collectors.toList());

        return new PageImpl<>(dtoList, pageable, totalCount);
    }


    @Override
    public PageImpl<ContestPageResponse> getQuestionListPageWithSearch(BoardType boardType, SearchType searchCondition, Pageable pageable) {
        JPQLQuery<Contest> query = queryFactory.select(contest).from(contest);

        BooleanBuilder whereClause = new BooleanBuilder();

        Boolean x=boardType == BoardType.AI;
        System.out.println(x);
        if (searchCondition != null) {
            whereClause.and(ContentMessageTitleEq(searchCondition.getContent(), searchCondition.getTitle()))
                    .and(boardWriterEq(searchCondition.getWriter()));
        }



        if (boardType == BoardType.ALL) {
            whereClause.andAnyOf(
                    contest.boardType.eq(BoardType.AI),
                    contest.boardType.eq(BoardType.일반)
            );
        } else if (boardType != null) {
            whereClause.and(contest.boardType.eq(boardType));
        }

        query.where(whereClause).orderBy(contest.createDate.desc());

        List<Contest> results = getQuerydsl().applyPagination(pageable, query).fetch();
        long totalCount = query.fetchCount();

        // 엔티티를 응답 DTO로 매핑
        List<ContestPageResponse> dtoList = results.stream()
                .map(ContestPageResponse::new)
                .collect(Collectors.toList());

        return new PageImpl<>(dtoList, pageable, totalCount);
    }







    //제목 + 내용에 필요한 동적 쿼리문
    private BooleanExpression ContentMessageTitleEq(String boardContent,String boardTitle){
        // 글 내용 x, 글 제목 o
        if(!boardContent.isEmpty() && !boardTitle.isEmpty()){
            return contest.title.contains(boardTitle).or(contest.contents.contains(boardContent));
        }

        //글 내용 o, 글 제목 x
        if(!boardContent.isEmpty() && boardTitle.isEmpty()){
            return contest.contents.contains(boardContent);
        }

        //글 제목 o
        if(boardContent.isEmpty() && !boardTitle.isEmpty()){
            return contest.title.contains(boardTitle);
        }
        return null;
    }
    //  작성자 검색
    private BooleanExpression boardWriterEq(String boardWriter){
        if(boardWriter.isEmpty()){
            return null;
        }
        return contest.author.contains(boardWriter);
    }
}