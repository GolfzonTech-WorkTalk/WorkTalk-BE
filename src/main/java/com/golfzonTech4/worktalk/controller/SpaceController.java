package com.golfzonTech4.worktalk.controller;

import com.golfzonTech4.worktalk.domain.Space;
import com.golfzonTech4.worktalk.dto.space.SpaceImgDeleteDto;
import com.golfzonTech4.worktalk.dto.space.SpaceInsertDto;
import com.golfzonTech4.worktalk.dto.space.SpaceManageSortingDto;
import com.golfzonTech4.worktalk.dto.space.SpaceUpdateDto;
import com.golfzonTech4.worktalk.service.SpaceImgService;
import com.golfzonTech4.worktalk.service.SpaceService;
import io.swagger.v3.oas.annotations.Operation;
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

@Tag(name = "SpaceController", description = "백오피스, 사무공간 관련 api입니다.")
@Slf4j
@RestController
@RequiredArgsConstructor
public class SpaceController {

    private final SpaceService spaceService;
    private final SpaceImgService spaceImgService;

    //호스트가 등록한 사무공간리스트 조회
    @Operation(summary = "사무공간 리스트 조회 요청", description = "백오피스페이지에서 호스트가 등록한 사무공간 목록을 조회합니다.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "OK"),
            @ApiResponse(responseCode = "400", description = "BAD REQUEST"),
            @ApiResponse(responseCode = "404", description = "NOT FOUND"),
            @ApiResponse(responseCode = "500", description = "INTERNAL SERVER ERROR")
    })
    @GetMapping("/host/spaceAll")
    public ResponseEntity findHostSpaces() {
        log.info("findHostSpaces :{}");
        return ResponseEntity.ok(spaceService.selectSpaceByHost());
    }

    //사무공간 상세페이지
    @Operation(summary = "사무공간 상세페이지", description = "사무공간 상세페이지입니다.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "OK"),
            @ApiResponse(responseCode = "400", description = "BAD REQUEST"),
            @ApiResponse(responseCode = "404", description = "NOT FOUND"),
            @ApiResponse(responseCode = "500", description = "INTERNAL SERVER ERROR")
    })
    @GetMapping("/spaceOne/{spaceId}")
    public ResponseEntity findOneSpace(@PathVariable("spaceId") final Long spaceId) {
        return ResponseEntity.ok(spaceService.getSpaceDetailPage(spaceId));
    }

    //호스트의 사무공간 등록
    @Operation(summary = "사무공간 등록", description = "백오피스페이지에서 사무공간을 등록합니다.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "OK"),
            @ApiResponse(responseCode = "400", description = "BAD REQUEST"),
            @ApiResponse(responseCode = "404", description = "NOT FOUND"),
            @ApiResponse(responseCode = "500", description = "INTERNAL SERVER ERROR")
    })
    @PostMapping("/host/spaceCreate")
    public ResponseEntity<Space> createSpace(@Valid SpaceInsertDto dto) {
        log.info("createSpace : {}", dto);
        spaceService.createSpace(dto);

        return new ResponseEntity("입력완료", HttpStatus.OK);
    }

    //호스트 사무공간 수정(사무공간 설명, 이미지)
    @Operation(summary = "사무공간 수정", description = "백오피스페이지에서 사무공간을 수정합니다.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "OK"),
            @ApiResponse(responseCode = "400", description = "BAD REQUEST"),
            @ApiResponse(responseCode = "404", description = "NOT FOUND"),
            @ApiResponse(responseCode = "500", description = "INTERNAL SERVER ERROR")
    })
    @PostMapping("/host/spaceUpdate/{spaceId}")
    public ResponseEntity<Space> updateSpace(@Valid SpaceUpdateDto dto) {
        spaceService.updateSpace(dto);
        return new ResponseEntity("수정완료", HttpStatus.OK);
    }

    //호스트의 사무공간삭제
    @Operation(summary = "사무공간 삭제", description = "백오피스페이지에서 사무공간을 삭제합니다.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "OK"),
            @ApiResponse(responseCode = "400", description = "BAD REQUEST"),
            @ApiResponse(responseCode = "404", description = "NOT FOUND"),
            @ApiResponse(responseCode = "500", description = "INTERNAL SERVER ERROR")
    })
    @DeleteMapping("/host/spaceDelete/{spaceId}")
    public ResponseEntity<Void> deleteSpace(@PathVariable Long spaceId) {
        spaceService.deleteSpace(spaceId);
        return ResponseEntity.ok().build();
    }

    //마스터 사무공간 관리 페이지 - 사무공간 타입, 승인상태 소팅
    @Operation(summary = "마스터계정 사무공간 관리 페이지", description = "전체 사무공간을 출력하고 사무공간 타입과 공간승인상태별로 소팅합니다.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "OK"),
            @ApiResponse(responseCode = "400", description = "BAD REQUEST"),
            @ApiResponse(responseCode = "404", description = "NOT FOUND"),
            @ApiResponse(responseCode = "500", description = "INTERNAL SERVER ERROR")
    })
    @GetMapping("/master/spaceAll")
    public ResponseEntity masterManageSpaces(@ModelAttribute SpaceManageSortingDto dto) {
        PageRequest pageRequest = PageRequest.of(dto.getPageNum(), 10);
        return ResponseEntity.ok(spaceService.getSpaceMasterPage(pageRequest, dto));
    }

    //마스터의 사무공간 승인
    @Operation(summary = "사무공간 승인", description = "마스터계정에서 사무공간을 검수 후 승인합니다.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "OK"),
            @ApiResponse(responseCode = "400", description = "BAD REQUEST"),
            @ApiResponse(responseCode = "404", description = "NOT FOUND"),
            @ApiResponse(responseCode = "500", description = "INTERNAL SERVER ERROR")
    })
    @PostMapping("/spaceApproved/{spaceId}")
    public ResponseEntity spaceApproved(@PathVariable("spaceId") final Long spaceId) {
        spaceService.ApprovedSpace(spaceId);
        return new ResponseEntity("승인완료하였습니다", HttpStatus.OK);
    }

    //마스터의 사무공간 승인거절
    @Operation(summary = "사무공간 거절", description = "마스터계정에서 사무공간을 검수 후 거절합니다.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "OK"),
            @ApiResponse(responseCode = "400", description = "BAD REQUEST"),
            @ApiResponse(responseCode = "404", description = "NOT FOUND"),
            @ApiResponse(responseCode = "500", description = "INTERNAL SERVER ERROR")
    })
    @PostMapping("/spaceRejected/{spaceId}")
    public ResponseEntity spaceRejected(@PathVariable("spaceId") final Long spaceId) {
        spaceService.RejectedSpace(spaceId);
        return new ResponseEntity("승인거절하였습니다", HttpStatus.OK);
    }

    //사무공간 이미지 선택 삭제
    @Operation(summary = "사무공간 이미지 단일삭제", description = "사무공간 수정 시 사진을 선택삭제합니다.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "OK"),
            @ApiResponse(responseCode = "400", description = "BAD REQUEST"),
            @ApiResponse(responseCode = "404", description = "NOT FOUND"),
            @ApiResponse(responseCode = "500", description = "INTERNAL SERVER ERROR")
    })
    @DeleteMapping("/spaceImgDelete")
    public ResponseEntity<Space> spaceImgDelete(@ModelAttribute SpaceImgDeleteDto dto) {
        log.info("spaceImgDelete : {}", dto);
        spaceImgService.deleteSpaceImg(dto);

        return new ResponseEntity("삭제완료", HttpStatus.OK);
    }

    //호스트 가입 승인 여부
    @Operation(summary = "호스트의 가입승인 여부 조회", description = "호스트 계정 승인 여부를 조회합니다.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "OK"),
            @ApiResponse(responseCode = "400", description = "BAD REQUEST"),
            @ApiResponse(responseCode = "404", description = "NOT FOUND"),
            @ApiResponse(responseCode = "500", description = "INTERNAL SERVER ERROR")
    })
    @GetMapping("/hostActivated")
    public ResponseEntity<Object> hostActivated() {
        boolean result = spaceService.findHostActivated(); // 승인 대기 상태면 false, 승인완료면 true
        return new ResponseEntity<Object>(result, HttpStatus.OK);
    }

    @Operation(summary = "호스트가 소유한 세부 사무공간명 목록 조회 요청", description = "해당 호스트가 소유한 세부 사무공간명 목록을 출력합니다.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "OK"),
            @ApiResponse(responseCode = "400", description = "BAD REQUEST"),
            @ApiResponse(responseCode = "404", description = "NOT FOUND"),
            @ApiResponse(responseCode = "500", description = "INTERNAL SERVER ERROR")
    })
    @GetMapping("/getHostSpaceNames")
    public ResponseEntity spaceNameListByHost() {
        log.info("spaceNameListByHost");
        return ResponseEntity.ok(spaceService.findSpaces());
    }

}
