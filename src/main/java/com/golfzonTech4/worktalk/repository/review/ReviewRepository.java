package com.golfzonTech4.worktalk.repository.review;

import com.golfzonTech4.worktalk.domain.Review;
import com.golfzonTech4.worktalk.dto.review.ReviewDetailDto;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.querydsl.QuerydslPredicateExecutor;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ReviewRepository extends JpaRepository<Review, Long>,
        QuerydslPredicateExecutor<Review>, ReviewRepositoryCustom {

    Review findByReviewId(Long reviewId); //review 선택

    //review, reservation, room, space 조인
//    @Query("select distinct new com.golfzonTech4.worktalk.dto.review.ReviewDetailDto" +
//            "(r.reviewId, re.reserveId, r.member.id, r.member.name, r.content, r.lastModifiedDate, r.grade) " +
//            "from Review r left join Reservation re on r.reservation.reserveId = re.reserveId " +
//            "left join re.room ro on re.room.roomId = ro.roomId " +
//            "left join ro.space s on s.spaceId = ro.space.spaceId where s.spaceId = :spaceId")
//    List<ReviewDetailDto> findReviewsDtoListBySpaceId(@Param("spaceId") Long spaceId);//해당 사무공간의 후기 리스트

    @Query("select r from Review r left join r.reservation re on r.reservation.reserveId = re.reserveId " +
            "left join re.room ro on re.room.roomId = ro.roomId left join ro.space s on s.spaceId = ro.space.spaceId where s.spaceId = :spaceId")
    List<Review> findAllBySpaceId(@Param("spaceId") Long spaceId);

    //review, reservation, member 조인 + room, spacename 추가하기
    @Query("select distinct new com.golfzonTech4.worktalk.dto.review.ReviewDetailDto" +
            "(r.reviewId, re.reserveId, r.member.id, r.content, r.lastModifiedDate, r.grade, ro.space.spaceName, ro.roomName, ro.roomType) " +
            "from Review r left join Reservation re on r.reservation.reserveId = re.reserveId " +
            "left join re.room ro on re.room.roomId = ro.roomId " +
            "left join r.member m on m.name = r.member.name where m.name = :name")
    List<ReviewDetailDto> findReviewsDtoListByMember(@Param("name") String name);//접속자의 후기 리스트

}