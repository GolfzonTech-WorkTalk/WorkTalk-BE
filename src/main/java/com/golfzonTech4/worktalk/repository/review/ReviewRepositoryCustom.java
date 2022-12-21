package com.golfzonTech4.worktalk.repository.review;

import com.golfzonTech4.worktalk.dto.review.ReviewDetailDto;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;

public interface ReviewRepositoryCustom {

    PageImpl<ReviewDetailDto> findReviewsDtoListBySpaceId(PageRequest pageRequest, Long spaceId);

    PageImpl<ReviewDetailDto> findReviewsDtoListByMember(PageRequest pageRequest, String name);//접속자의 후기 리스트

}
