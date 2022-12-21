package com.golfzonTech4.worktalk.controller;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.golfzonTech4.worktalk.dto.member.TokenDto;
import com.golfzonTech4.worktalk.jwt.JwtFilter;
import com.golfzonTech4.worktalk.jwt.TokenProvider;
import com.golfzonTech4.worktalk.service.KakaoLoginService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletResponse;
import java.net.URI;
import java.util.Map;

@Tag(name = "KakaoController", description = "카카오 로그인 api입니다.")
@RestController
@RequiredArgsConstructor
@Slf4j
public class KakaoController {
    private final TokenProvider tokenProvider;
    private final AuthenticationManagerBuilder authenticationManagerBuilder;

    private final KakaoLoginService kakaoLoginService;

    @Operation(summary = "카카오 로그인 요청", description = "카카오계정으로 로그인을 요청하고, 비회원일 경우 회원가입처리 후 로그인합니다.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "OK"),
            @ApiResponse(responseCode = "400", description = "BAD REQUEST"),
            @ApiResponse(responseCode = "404", description = "NOT FOUND"),
            @ApiResponse(responseCode = "500", description = "INTERNAL SERVER ERROR")
    })
    @GetMapping("/user/kakao/callback")
    public ResponseEntity<?> kakaoLogin(@RequestParam String code, HttpServletResponse response) throws JsonProcessingException {
        // 카카오 서버로부터 받은 인가 코드, JWT 토큰
        Map<String, String> result = kakaoLoginService.kakaoLogin(code);
        HttpHeaders headers = new HttpHeaders();

        String jwt = result.get("jwt");
        String tel = result.get("tel");

        headers.setLocation(URI.create("https://worktalk.link/login?token=" + jwt + "&tel=" + tel));
        headers.add(JwtFilter.AUTHORIZATION_HEADER, "Bearer " + jwt);

        log.info("headers:{}", headers);
        log.info("jwt:{}", jwt);
        log.info("jwt:{}", tel);
        return new ResponseEntity<>(new TokenDto(jwt), headers, HttpStatus.FOUND);
    }

}
