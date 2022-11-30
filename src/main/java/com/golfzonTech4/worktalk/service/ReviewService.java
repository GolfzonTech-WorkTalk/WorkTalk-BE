package com.golfzonTech4.worktalk.service;

import com.golfzonTech4.worktalk.domain.*;
import com.golfzonTech4.worktalk.dto.qna.QnaDetailDto;
import com.golfzonTech4.worktalk.dto.review.ReviewDetailDto;
import com.golfzonTech4.worktalk.dto.review.ReviewUpdateDto;
import com.golfzonTech4.worktalk.dto.review.ReviewInsertDto;
import com.golfzonTech4.worktalk.repository.ReviewRepository;
import com.golfzonTech4.worktalk.repository.reservation.ReservationRepository;
import com.golfzonTech4.worktalk.util.SecurityUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.persistence.EntityNotFoundException;
import java.util.List;
import java.util.Optional;

@Slf4j
@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class ReviewService {

    private final ReviewRepository reviewRepository;
    private final ReservationRepository reservationRepository;
    private final MemberService memberService;

    @Transactional
    public Review createReview(ReviewInsertDto dto){
        log.info("createReview()....");

        Optional<String> member = SecurityUtil.getCurrentUsername();

        if(!member.isPresent()) throw new EntityNotFoundException("Member Not Found");

        Member findMember = memberService.findByName(member.get());
        Reservation findReservation = reservationRepository.findByReserveId(dto.getReserveId());
        Review reviewToCreate = new Review();
        BeanUtils.copyProperties(dto, reviewToCreate);
        reviewToCreate.setMember(findMember);
        reviewToCreate.setReservation(findReservation);
        return  reviewRepository.save(reviewToCreate);
    }

    @Transactional
    public void updateReview(Long reviewId, ReviewUpdateDto dto) {
        log.info("updateReview()....");
        Optional<String> currentUsername = SecurityUtil.getCurrentUsername();
        Optional<Review> optionalReview = Optional.ofNullable(reviewRepository.findByReviewId(reviewId));

        if (!currentUsername.isPresent()) throw new EntityNotFoundException("Member Not Found");

        Member findMember = memberService.findByName(currentUsername.get());
        //후기작성자와 접속자가 같은지 확인
        if (findMember.getId() == optionalReview.get().getMember().getId()) {
            Review review = optionalReview.get();
            review.setContent(dto.getContent());//dirty checking
            review.setGrade(dto.getGrade());//dirty checking
        } else
            throw new EntityNotFoundException("수정 권한이 없습니다.");
    }

    @Transactional
    public void deleteReview(Long reviewId){
        log.info("deleteReview()....");

        Optional<String> currentUsername = SecurityUtil.getCurrentUsername();
        if (!currentUsername.isPresent()) throw new EntityNotFoundException("Member Not Found");

        Optional<Review> optionalReview = Optional.ofNullable(reviewRepository.findByReviewId(reviewId));

        Member findMember = memberService.findByName(currentUsername.get());
        //qna작성자와 접속자가 같은지 확인
        if (findMember.getId() == optionalReview.get().getMember().getId()) {
            reviewRepository.deleteById(reviewId);
        } else
            throw new EntityNotFoundException("삭제 권한이 없습니다.");
    }

    public List<ReviewDetailDto> getReviews(Long spaceId) {
        log.info("getReviews()....");
        return reviewRepository.findReviewsDtoListBySpaceId(spaceId);
    }

    public List<ReviewDetailDto> ReviewListPage() {
        log.info("ReviewListPage()....");

        Optional<String> currentUsername = SecurityUtil.getCurrentUsername();
        if (currentUsername.isEmpty()) throw new EntityNotFoundException("Member not found");

        return reviewRepository.findReviewsDtoListByMember(currentUsername.get());
    }
}
