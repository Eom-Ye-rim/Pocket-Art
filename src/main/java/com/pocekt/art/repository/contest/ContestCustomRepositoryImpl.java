package com.pocekt.art.repository.contest;


import com.pocekt.art.dto.response.ContestPageResponse;
import com.pocekt.art.entity.Contest;
import com.pocekt.art.entity.QContest;
import com.querydsl.core.types.Projections;
import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.jpa.impl.JPAQueryFactory;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.support.QuerydslRepositorySupport;

import java.util.List;

import static com.pocekt.art.entity.QContest.contest;
import static com.pocekt.art.entity.QUsers.users;


public class ContestCustomRepositoryImpl extends QuerydslRepositorySupport implements ContestCustomRepository {
    private JPAQueryFactory queryFactory;

    public ContestCustomRepositoryImpl(JPAQueryFactory jpaQueryFactory) {
        super(Contest.class);
        this.queryFactory = jpaQueryFactory;
    }

    @Override
    public void updateLikeCount(Contest contests) {

        queryFactory.update(contest)
                .set(contest.likeCount, contest.likeCount.add(1))
                .where(contest.eq(contests))
                .execute();

    }

    @Override
    public List<Contest> findTop5ContestsByLikes() {
        QContest contest = QContest.contest;
        return queryFactory.selectFrom(contest)
                .orderBy(contest.likeCount.desc())
                .limit(5)
                .fetch();
    }


    @Override
    public void subLikeCount(Contest contests) {

        queryFactory.update(contest)
                .set(contest.likeCount, contest.likeCount.subtract(1))
                .where(contest.eq(contests))
                .execute();

    }


    @Override
    public List<ContestPageResponse> findPageContest(Pageable pageable, String title, String contents) {

        return queryFactory.select(
                        Projections.bean(ContestPageResponse.class, contest.id,contest.title, contest.contents, users.name,contest.createDate))
                .from(contest)
                .join(users).on(contest.users.name.eq(users.name))
                .where(likeTitle(title), likeContents(contents))
                .offset(pageable.getOffset())
                .limit(pageable.getPageSize())
                .orderBy(contest.createDate.desc())
                .fetch();
    }

    private BooleanExpression likeTitle(String title) {
        if(title == null || title.isEmpty()){
            return null;
        }
        return contest.title.like(title);
    }

    private BooleanExpression likeContents(String contents) {
        if(contents == null || contents.isEmpty()){
            return null;
        }
        return contest.contents.like(contents);
    }


    private BooleanExpression eqUserId(String userId) {
        if(userId == null || userId.isEmpty()){
            return null;
        }
        return users.name.eq(userId);
    }



}