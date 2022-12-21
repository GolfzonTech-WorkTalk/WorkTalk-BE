package com.golfzonTech4.worktalk.repository.review;

import com.golfzonTech4.worktalk.dto.review.QReviewDetailDto;
import com.golfzonTech4.worktalk.dto.review.ReviewDetailDto;
import com.golfzonTech4.worktalk.dto.review.ReviewPagingDto;
import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.jpa.impl.JPAQueryFactory;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;

import javax.persistence.EntityManager;
import java.util.List;

import static com.golfzonTech4.worktalk.domain.QMember.member;
import static com.golfzonTech4.worktalk.domain.QReservation.reservation;
import static com.golfzonTech4.worktalk.domain.QReview.review;
import static com.golfzonTech4.worktalk.domain.QRoom.room;
import static com.golfzonTech4.worktalk.domain.QSpace.space;

@Slf4j
public class ReviewRepositoryCustomImpl implements ReviewRepositoryCustom {

    private JPAQueryFactory queryFactory; // 동적 쿼리 생성 위한 클래스

    // JPAQueryFactory 생성자로 EntityManager 넣어줌
    public ReviewRepositoryCustomImpl(EntityManager em) {
        this.queryFactory = new JPAQueryFactory(em);
    }

    @Override
    public PageImpl<ReviewDetailDto> findReviewsDtoListBySpaceId(PageRequest pageRequest, Long spaceId) {
        List<ReviewDetailDto> content = queryFactory
                .select(
                        new QReviewDetailDto(
                                review.reviewId,
                                reservation.reserveId,
                                review.member.id,
                                review.member.name,
                                review.content,
                                review.lastModifiedDate,
                                review.grade)
                ).distinct()
                .from(review)
                .leftJoin(reservation).on(reservation.reserveId.eq(review.reservation.reserveId))
                .leftJoin(room).on(room.roomId.eq(reservation.room.roomId))
                .leftJoin(space).on(space.spaceId.eq(room.space.spaceId))
                .where(space.spaceId.eq(spaceId))
                .orderBy(review.reviewId.desc())
                .offset(pageRequest.getOffset())
                .limit(pageRequest.getPageSize())
                .fetch();

        long total = queryFactory
                .select(review.countDistinct())
                .from(review)
                .leftJoin(reservation).on(reservation.reserveId.eq(review.reservation.reserveId))
                .leftJoin(room).on(room.roomId.eq(reservation.room.roomId))
                .leftJoin(space).on(space.spaceId.eq(room.space.spaceId))
                .where(space.spaceId.eq(spaceId))
                .fetchOne();

        return new PageImpl<>(content, pageRequest, total);
    }

    @Override
    public PageImpl<ReviewDetailDto> findReviewsDtoListByMember(PageRequest pageRequest, String name) {
        List<ReviewDetailDto> content = queryFactory
                .select(
                        new QReviewDetailDto(
                                review.reviewId,
                                reservation.reserveId,
                                review.member.id,
                                review.content,
                                review.lastModifiedDate,
                                review.grade,
                                room.space.spaceName,
                                room.roomName,
                                room.roomType
                        )
                ).distinct()
                .from(review)
                .leftJoin(reservation).on(reservation.reserveId.eq(review.reservation.reserveId))
                .leftJoin(room).on(room.roomId.eq(reservation.room.roomId))
                .leftJoin(member).on(member.id.eq(review.member.id))
                .where(member.name.eq(name))
                .orderBy(review.reviewId.desc())
                .offset(pageRequest.getOffset())
                .limit(pageRequest.getPageSize())
                .fetch();

        long total = queryFactory
                .select(review.countDistinct())
                .from(review)
                .leftJoin(reservation).on(reservation.reserveId.eq(review.reservation.reserveId))
                .leftJoin(room).on(room.roomId.eq(reservation.room.roomId))
                .leftJoin(member).on(member.id.eq(review.member.id))
                .where(member.name.eq(name))
                .fetchOne();

        return new PageImpl<>(content, pageRequest, total);
    }

    @Override
    public PageImpl<ReviewDetailDto> findReviewDtoListByHostSpace(PageRequest pageRequest, ReviewPagingDto dto) {
        List<ReviewDetailDto> content = queryFactory
                .select(
                        new QReviewDetailDto(
                                review.reviewId,
                                reservation.reserveId,
                                review.member.id,
                                review.content,
                                review.lastModifiedDate,
                                review.grade,
                                room.space.spaceName,
                                room.roomName,
                                room.roomType
                        )
                ).distinct()
                .from(review)
                .leftJoin(reservation).on(reservation.reserveId.eq(review.reservation.reserveId))
                .leftJoin(room).on(room.roomId.eq(reservation.room.roomId))
                .leftJoin(space).on(space.spaceId.eq(room.space.spaceId))
                .leftJoin(member).on(member.id.eq(review.member.id))
                .where(member.name.eq(dto.getName()), eqSapceName(dto.getSpaceName()))
                .orderBy(review.reviewId.desc())
                .offset(pageRequest.getOffset())
                .limit(pageRequest.getPageSize())
                .fetch();

        long total = queryFactory
                .select(review.countDistinct())
                .from(review)
                .leftJoin(reservation).on(reservation.reserveId.eq(review.reservation.reserveId))
                .leftJoin(room).on(room.roomId.eq(reservation.room.roomId))
                .leftJoin(member).on(member.id.eq(review.member.id))
                .where(member.name.eq(dto.getName()), eqSapceName(dto.getSpaceName()))
                .fetchOne();

        return new PageImpl<>(content, pageRequest, total);
    }

    private BooleanExpression eqSapceName(String searchSpaceName) {
        if (searchSpaceName == null || searchSpaceName.isEmpty()) {
            return null;
        }
        return space.spaceName.eq(searchSpaceName);
    }

}
