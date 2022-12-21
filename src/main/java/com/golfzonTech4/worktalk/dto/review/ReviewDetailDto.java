package com.golfzonTech4.worktalk.dto.review;

import com.golfzonTech4.worktalk.domain.RoomType;
import com.querydsl.core.annotations.QueryProjection;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
public class ReviewDetailDto {

    private Long reviewId;

    private Long reservationId;

    private Long memberId;

    private String writer;

    private String content;

    private LocalDateTime lastModifiedDate;

    private Double grade;

    private String spaceName;

    private String roomName;

    private RoomType roomType;

    @QueryProjection
    public ReviewDetailDto(Long reviewId, Long reservationId, Long memberId, String content, LocalDateTime lastModifiedDate, Double grade, String spaceName, String roomName
            , RoomType roomType) {
        this.reviewId = reviewId;
        this.reservationId = reservationId;
        this.memberId = memberId;
        this.content = content;
        this.lastModifiedDate = lastModifiedDate;
        this.grade = grade;
        this.spaceName = spaceName;
        this.roomName = roomName;
        this.roomType = roomType;
    }

    @QueryProjection
    public ReviewDetailDto(Long reviewId, Long reservationId, Long memberId, String writer, String content, LocalDateTime lastModifiedDate, Double grade) {
        this.reviewId = reviewId;
        this.reservationId = reservationId;
        this.memberId = memberId;
        this.writer = writer;
        this.content = content;
        this.lastModifiedDate = lastModifiedDate;
        this.grade = grade;
    }

    @QueryProjection
    public ReviewDetailDto(Long reviewId, Long reservationId, Long memberId, String writer, String content, LocalDateTime lastModifiedDate, Double grade, String spaceName, String roomName, RoomType roomType) {
        this.reviewId = reviewId;
        this.reservationId = reservationId;
        this.memberId = memberId;
        this.writer = writer;
        this.content = content;
        this.lastModifiedDate = lastModifiedDate;
        this.grade = grade;
        this.spaceName = spaceName;
        this.roomName = roomName;
        this.roomType = roomType;
    }
}
