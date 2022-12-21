package com.golfzonTech4.worktalk.controller;

import com.golfzonTech4.worktalk.domain.Member;
import com.golfzonTech4.worktalk.dto.member.*;
import com.golfzonTech4.worktalk.repository.ListResult;
import com.golfzonTech4.worktalk.service.MemberService;
import com.golfzonTech4.worktalk.util.SecurityUtil;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.util.HashMap;
import java.util.Map;

@Tag(name = "MemberController", description = "회원 정보 관련 api입니다.")
@RestController
@RequiredArgsConstructor
@Slf4j
public class MemberController {

    private final MemberService memberService;

    /**
     * MemberDetailDto 파라미터로 받아서 MemberService의 signup메서드를 호출
     */
    @Operation(summary = "회원 가입 요청", description = "입력된 정보를 기반으로 회원 가입을 진행합ㄴ디ㅏ.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "OK",
                    content = @Content(schema = @Schema(implementation = Long.class))),
            @ApiResponse(responseCode = "400", description = "BAD REQUEST"),
            @ApiResponse(responseCode = "404", description = "NOT FOUND"),
            @ApiResponse(responseCode = "500", description = "이미 존재하는 회원입니다.")
    })
    @PostMapping("/member/join")
    public ResponseEntity<Long> join(
            @Valid @RequestBody MemberDetailDto request) {
        log.info("signupUser: {}", request);

        return ResponseEntity.ok(memberService.join(request));
    }

    /**
     * 회원 프로필 조회 요청
     */
    @Operation(summary = "회원 프로필 조회 요청", description = "접속된 회원 정보를 조회합니다.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "OK",
                    content = @Content(schema = @Schema(implementation = MemberDto.class))),
            @ApiResponse(responseCode = "400", description = "BAD REQUEST"),
            @ApiResponse(responseCode = "404", description = "NOT FOUND"),
            @ApiResponse(responseCode = "500", description = "INTERNAL SERVER ERROR")
    })
    @GetMapping("/member/profile")
    public ResponseEntity<MemberDto> profile() {
        log.info("profile");
        return ResponseEntity.ok(memberService.findProfile());
    }

    /**
     * 회원 정보 수정 요청
     */
    @Operation(summary = "회원 정보 수정 요청", description = "회원의 연락처 및 비밀번호를 수정합니다.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "OK",
                    content = @Content(schema = @Schema(implementation = Long.class))),
            @ApiResponse(responseCode = "400", description = "BAD REQUEST"),
            @ApiResponse(responseCode = "404", description = "NOT FOUND"),
            @ApiResponse(responseCode = "500", description = "존재하지 않는 회원입니다.")
    })
    @PostMapping("/member/update")
    public ResponseEntity<Long> update(
            @Valid @RequestBody MemberUpdateDto dto) {
        log.info("update: {}", dto);
        return ResponseEntity.ok(memberService.update(dto));
    }

    /**
     * 회원 탈퇴 요청
     */
    @Operation(summary = "회원 탈퇴 요청", description = "해당 요청을 보낸 회원의 진행중인 결제/예약 내역을 확인 후 없을 시 탈퇴처리합니다.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "OK",
                    content = @Content(schema = @Schema(implementation = Long.class))),
            @ApiResponse(responseCode = "400", description = "BAD REQUEST"),
            @ApiResponse(responseCode = "404", description = "NOT FOUND"),
            @ApiResponse(responseCode = "500", description = "진행 중인 예약/결제건이 있는 회원입니다.")
    })
    @PostMapping("/member/leave/{memberId}")
    public ResponseEntity<Long> leave(
            @PathVariable(value = "memberId") Long memberId) {
        log.info("leave: {}", memberId);
        // 예약이나 결제건이 있을 경우 예외 발생
        memberService.leave(memberId);
        return ResponseEntity.ok().build();
    }

    /**
     * 사용자 명 중복 확인 요청
     */
    @Operation(summary = "사용자 명 중복 확인 요청", description = "해당 요청건에 대한 회원명 중복 여부를 확인합니다.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "OK"),
            @ApiResponse(responseCode = "400", description = "BAD REQUEST"),
            @ApiResponse(responseCode = "404", description = "NOT FOUND"),
            @ApiResponse(responseCode = "500", description = "이미 존재하는 회원입니다.")
    })
    @PostMapping("/member/nameCheck")
    public ResponseEntity findDuplicated(@RequestBody MemberDetailDto request) {
        log.info("findDuplicated: {}", request);
        Member member = new Member();
        member.setName(request.getName());

        memberService.findDuplicatesName(member); // 중복 회원 존재 시 Exception 발생

        return ResponseEntity.ok().build(); // 종복 회원이 존재하지 않을 시 성공 메세지
    }

    /**
     * 비 승인 호스트 내역 조회 요청
     */
    @Operation(summary = "호스트 회원 목록 조회 요청", description = "전체 호스트를 호출하고, 인가 여부를 기준으로 소팅합니다.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "OK",
                    content = @Content(schema = @Schema(implementation = ListResult.class))),
            @ApiResponse(responseCode = "400", description = "BAD REQUEST"),
            @ApiResponse(responseCode = "404", description = "NOT FOUND"),
            @ApiResponse(responseCode = "500", description = "INTERNAL SERVER ERROR")
    })
    @GetMapping("/member/deactivated")
    public ResponseEntity<ListResult> findDeactMemeber(@ModelAttribute MemberSerachDto dto) {
        log.info("findDeactMemeber");
        return ResponseEntity.ok(memberService.findDeactMemeber(dto));
    }

    /**
     * 호스트 승인 요청
     */
    @Operation(summary = "호스트 회원 승인 요청", description = "마스터 계정에서 해당 호스트 계정을 활성화합니다.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "OK",
                    content = @Content(schema = @Schema(implementation = Integer.class))),
            @ApiResponse(responseCode = "400", description = "BAD REQUEST"),
            @ApiResponse(responseCode = "404", description = "NOT FOUND"),
            @ApiResponse(responseCode = "500", description = "INTERNAL SERVER ERROR")
    })
    @GetMapping("/member/approve/{memberId}")
    public ResponseEntity<Integer> approve(@PathVariable(value = "memberId") Long memberId) {
        log.info("approve : {}", memberId);
        return ResponseEntity.ok(memberService.approve(memberId));
    }

    /**
     * 비밀번호 찾기 요청
     */
    @Operation(summary = "비밀번호 찾기 메일 전송", description = "해당 계정에 대하여 비밀번호를 임의의 코드로 초기화 한 후 해당 코드를 회원 메일로 전송합니다.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "OK"),
            @ApiResponse(responseCode = "400", description = "BAD REQUEST"),
            @ApiResponse(responseCode = "404", description = "NOT FOUND"),
            @ApiResponse(responseCode = "500", description = "소셜로그인을 이용중인 계정입니다 / 가입 정보가 없는 이메일입니다")
    })
    @GetMapping("/member/findPw")
    public ResponseEntity findPw(@RequestParam(value = "email") String email) {
        log.info("findPw : {}", email);
        // 가입된 정보가 없을 경우 예외 처리
        memberService.changePw(email);
        return ResponseEntity.ok().build();
    }

    /**
     * 메일 조회(소셜 로그인 여부 확인)
     */
    @Operation(summary = "메일 기준 조회", description = "입력한 메일을 기준으로 해당 메일을 가진 계정이 있는지 조회합니다.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "OK",
                    content = @Content(schema = @Schema(implementation = Map.class))),
            @ApiResponse(responseCode = "400", description = "BAD REQUEST"),
            @ApiResponse(responseCode = "404", description = "NOT FOUND"),
            @ApiResponse(responseCode = "500", description = "INTERNAL SERVER ERROR")
    })
    @GetMapping("/member/findEmail")
    public ResponseEntity<Map<String, String>> findEmail(@RequestParam(value = "email") String email) {
        log.info("findEmail : {}", email);
        Map<String, String> result = memberService.findEmail(email);
        return ResponseEntity.ok(result);
    }

    /**
     * 사용자 예약 가능 여부
     */
    @Operation(summary = "사용자 예약 가능 여부", description = "요청한 회원의 이용제한 여부, 연락처 유무 여부를 확인.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "OK",
                    content = @Content(schema = @Schema(implementation = Map.class))),
            @ApiResponse(responseCode = "400", description = "BAD REQUEST"),
            @ApiResponse(responseCode = "404", description = "NOT FOUND"),
            @ApiResponse(responseCode = "500", description = "가입되지 않은 회원 정보입니다.")
    })
    @GetMapping("/member/isValid")
    public ResponseEntity<Map<String, Object>> isValid() {
        log.info("isValid");
        String name = SecurityUtil.getCurrentUsername().get();
        Member findMember = memberService.findByName(name);
        Map<String, Object> result = new HashMap<>();
        result.put("activated", findMember.getActivated());
        result.put("tel", findMember.getTel());
        return ResponseEntity.ok(result);
    }
}
