package com.golfzonTech4.worktalk.controller;

import com.golfzonTech4.worktalk.dto.space.SpaceSearchDto;
import com.golfzonTech4.worktalk.repository.ListResult;
import com.golfzonTech4.worktalk.service.SpaceService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;

@Controller
@Slf4j
@RequiredArgsConstructor
public class MainController {
    private final SpaceService spaceService;

    //메인페이지 - 전체 사무공간 목록, (오피스,데스크,회의실) 필터기능, 사무공간명, 주소 키워드 검색
    @Operation(summary = "메인페이지", description = "메인페이지의 사무공간 리스트를 출력합니다.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "OK",
                    content = @Content(schema = @Schema(implementation = ListResult.class))),
            @ApiResponse(responseCode = "400", description = "BAD REQUEST"),
            @ApiResponse(responseCode = "404", description = "NOT FOUND"),
            @ApiResponse(responseCode = "500", description = "INTERNAL SERVER ERROR")
    })
    @GetMapping(value = "/main")
    public ResponseEntity<ListResult> mainPage(@ModelAttribute SpaceSearchDto dto) {
        log.info("SpaceSearchDto :{}", dto);
        PageRequest pageRequest = PageRequest.of(dto.getPageNum(), 9);
        return ResponseEntity.ok(spaceService.getMainSpacePage(pageRequest, dto));
    }
}
