package com.golfzonTech4.worktalk.repository.review;

import com.golfzonTech4.worktalk.dto.review.ReviewDetailDto;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;

public interface ReviewRepositoryCustom {

    PageImpl<ReviewDetailDto> findReviewsDtoListBySpaceId(PageRequest pageRequest, Long spaceId); //사무공간 상세페이지 후기 리스트

    PageImpl<ReviewDetailDto> findReviewsDtoListByMember(PageRequest pageRequest, String name); //접속자의 후기 리스트

}
