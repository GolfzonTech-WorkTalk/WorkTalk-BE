package com.golfzonTech4.worktalk.controller;

import com.golfzonTech4.worktalk.domain.Review;
import com.golfzonTech4.worktalk.dto.review.ReviewInsertDto;
import com.golfzonTech4.worktalk.dto.review.ReviewPagingDto;
import com.golfzonTech4.worktalk.dto.review.ReviewUpdateDto;
import com.golfzonTech4.worktalk.repository.ListResult;
import com.golfzonTech4.worktalk.service.ReviewService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;

@Tag(name = "ReviewController", description = "이용후기 관련 api입니다.")
@Slf4j
@RestController
@RequiredArgsConstructor
public class ReviewController {

    private final ReviewService reviewService;

    //후기 등록
    @Operation(summary = "후기 작성", description = "후기를 작성합니다.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "OK"),
            @ApiResponse(responseCode = "400", description = "BAD REQUEST"),
            @ApiResponse(responseCode = "404", description = "NOT FOUND"),
            @ApiResponse(responseCode = "500", description = "INTERNAL SERVER ERROR")
    })
    @PostMapping("/reviewCreate")
    public ResponseEntity<Review> createReview(@Valid @RequestBody ReviewInsertDto dto) {

        reviewService.createReview(dto);

        return new ResponseEntity("입력완료", HttpStatus.OK);
    }

    //후기 수정
    @Operation(summary = "후기 수정", description = "후기를 수정합니다.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "OK"),
            @ApiResponse(responseCode = "400", description = "BAD REQUEST"),
            @ApiResponse(responseCode = "404", description = "NOT FOUND"),
            @ApiResponse(responseCode = "500", description = "INTERNAL SERVER ERROR")
    })
    @PostMapping("/reviewUpdate/{reviewId}")
    public ResponseEntity<Review> updateReview(@Valid @RequestBody ReviewUpdateDto dto, @PathVariable Long reviewId) {

        reviewService.updateReview(reviewId, dto);

        return new ResponseEntity("수정완료", HttpStatus.OK);
    }

    //후기 삭제
    @Operation(summary = "후기 삭제", description = "후기를 삭제합니다.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "OK"),
            @ApiResponse(responseCode = "400", description = "BAD REQUEST"),
            @ApiResponse(responseCode = "404", description = "NOT FOUND"),
            @ApiResponse(responseCode = "500", description = "INTERNAL SERVER ERROR")
    })
    @DeleteMapping("/reviewDelete/{reviewId}")
    public ResponseEntity<Void> deleteReview(@PathVariable Long reviewId) {
        reviewService.deleteReview(reviewId);
        return ResponseEntity.ok().build();
    }

    //사무공간상세페이지에서 후기 리스트 출력
    @Operation(summary = "해당 사무공간 후기 목록 조회 요청", description = "사무공간 상세페이지에서 후기 목록을 조회합니다.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "OK"),
            @ApiResponse(responseCode = "400", description = "BAD REQUEST"),
            @ApiResponse(responseCode = "404", description = "NOT FOUND"),
            @ApiResponse(responseCode = "500", description = "INTERNAL SERVER ERROR")
    })
    @GetMapping("/spaceOne/reviews")
    public ResponseEntity<ListResult> reviewListBySpace(@ModelAttribute ReviewPagingDto dto) {
        PageRequest pageRequest = PageRequest.of(dto.getPageNum(), 5);
        return ResponseEntity.ok(reviewService.getReviewsBySpace(pageRequest, dto.getSpaceId()));
    }

    //마이페이지에서 후기 리스트 출력
    @Operation(summary = "마이페이지 후기 목록 조회 요청", description = "내가 작성한 후기 목록을 조회합니다.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "OK"),
            @ApiResponse(responseCode = "400", description = "BAD REQUEST"),
            @ApiResponse(responseCode = "404", description = "NOT FOUND"),
            @ApiResponse(responseCode = "500", description = "INTERNAL SERVER ERROR")
    })
    @GetMapping("/myreviews")
    public ResponseEntity<ListResult> myReviewPage(@ModelAttribute ReviewPagingDto dto) {
        PageRequest pageRequest = PageRequest.of(dto.getPageNum(), 10);
        return ResponseEntity.ok(reviewService.getMyReviews(pageRequest));
    }

    @Operation(summary = "호스트의 모든 사무공간 후기 글 목록 조회 요청",
            description = "호스트의 후기관리페이지에서 호스트가 소유한 모든 공간의 후기 목록을 조회하고 후기타입과 사무공간이름별로 소팅합니다.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "OK",
                    content = @Content(schema = @Schema(implementation = ListResult.class))),
            @ApiResponse(responseCode = "400", description = "BAD REQUEST"),
            @ApiResponse(responseCode = "404", description = "NOT FOUND"),
            @ApiResponse(responseCode = "500", description = "INTERNAL SERVER ERROR")
    })
    @GetMapping("/manageHostReview")
    public ResponseEntity<ListResult> reviewListByHostSpace(@ModelAttribute ReviewPagingDto dto) {
        PageRequest pageRequest = PageRequest.of(dto.getPageNum(), 10);
        return ResponseEntity.ok(reviewService.getReviewHostManagePage(pageRequest, dto));
    }

}
