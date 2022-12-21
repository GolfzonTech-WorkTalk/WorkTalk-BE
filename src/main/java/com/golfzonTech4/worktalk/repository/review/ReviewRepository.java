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

    @Query("select r from Review r left join r.reservation re on r.reservation.reserveId = re.reserveId " +
            "left join re.room ro on re.room.roomId = ro.roomId left join ro.space s on s.spaceId = ro.space.spaceId where s.spaceId = :spaceId")
    List<Review> findAllBySpaceId(@Param("spaceId") Long spaceId);

}