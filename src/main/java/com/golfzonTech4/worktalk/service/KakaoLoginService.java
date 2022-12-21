package com.golfzonTech4.worktalk.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.golfzonTech4.worktalk.domain.Member;
import com.golfzonTech4.worktalk.dto.member.KakaoUserInfoDto;
import com.golfzonTech4.worktalk.jwt.JwtFilter;
import com.golfzonTech4.worktalk.jwt.TokenProvider;
import com.golfzonTech4.worktalk.repository.member.MemberRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.json.JSONObject;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.http.client.HttpComponentsClientHttpRequestFactory;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;

import java.util.HashMap;
import java.util.Map;

import static com.golfzonTech4.worktalk.domain.MemberType.ROLE_USER;


@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class KakaoLoginService {

    private final MemberRepository memberRepository;

    private final PasswordEncoder passwordEncoder;

    private final AuthenticationManagerBuilder authenticationManagerBuilder;

    private final TokenProvider tokenProvider;


    @Transactional
    public Map<String, String> kakaoLogin(String code) throws JsonProcessingException {
        log.info("KakaoLoginService:kakaoLogin{}", code);

        KakaoUserInfoDto userInfo = getUserInfo(code);
        log.info("userInfo:{}", userInfo);
        Long kakaoId = userInfo.getId();
        String email = userInfo.getEmail();
        String nickname = userInfo.getNickname();

        // 회원 네임 = 카카오 id + nickname
        String name = kakaoId + nickname;

        // 패스워드 = 카카오 id
        String password = String.valueOf(kakaoId);
        log.info("name:{}", name);
        log.info("password:{}", password);

        // DB에 중복된 이메일이 있는지 확인
        Member kakaouser = memberRepository.findByEmail(email).orElse(null);
        log.info("findByEmail:{}", kakaouser);

        HashMap<String, String> map = new HashMap<>();
        // DB에 없을 경우 카카오 정보로 회원가입
        if (kakaouser == null) {
            Member member = new Member();
            member.setEmail(email);
            member.setPw(passwordEncoder.encode(password));// 패스워드 인코딩
            member.setName(name);
            member.setMemberType(ROLE_USER);
            member.setImgName("profill.png");
            member.setKakaoYn("Y");

            Member save = memberRepository.save(member);
            log.info("Member save in:{}", save.getId());
            log.info("sign in:{}", member);
            map.put("tel", "false");
        }

        log.info("tel:{}");

        if (kakaouser.getTel() == null) {
            map.put("tel", "false");
        } else {
            map.put("tel", "true");
        }

        //로그인 처리
        UsernamePasswordAuthenticationToken authenticationToken =
                new UsernamePasswordAuthenticationToken(email, password); // 토큰 생성

        Authentication authentication = authenticationManagerBuilder.getObject().authenticate(authenticationToken);
        SecurityContextHolder.getContext().setAuthentication(authentication); // Authentication Token => context 저장

        map.put("jwt", tokenProvider.createToken(authentication)); // Jwt 토큰 생성

        return map;
    }

    private String getAccessToken(String code) {
        // HTTP Header 오브젝트 생성
        HttpHeaders headers = new HttpHeaders();
        headers.add("Content-type", "application/x-www-form-urlencoded;charset=utf-8");

        // HTTP Body 생성
        MultiValueMap<String, String> body = new LinkedMultiValueMap<>();
        body.add("grant_type", "authorization_code");
        body.add("client_id", "79edba60f18097e8e335a7ca1b62de99");                  // REST API 키
        body.add("redirect_uri", "http://15.165.247.125:8100/user/kakao/callback");      // Redirect URI
        body.add("code", code);

        // HttpHeader와 HttpBody를 하나의 오브젝트에 담기
        RestTemplate rt = new RestTemplate();
        rt.setRequestFactory(new HttpComponentsClientHttpRequestFactory()); // 로그 보기
        HttpEntity<MultiValueMap<String, String>> kakaoTokenRequest =
                new HttpEntity<>(body, headers);

        // Http 요청하기 - Post방식으로 - 그리고 response 변수의 응답 받음.
        ResponseEntity<String> response = rt.exchange(
                "https://kauth.kakao.com/oauth/token",
                HttpMethod.POST,
                kakaoTokenRequest,
                String.class
        );

        // HTTP 응답 (JSON) -> 액세스 토큰 파싱
        String tokenJson = response.getBody();
        JSONObject rjson = new JSONObject(tokenJson);
        String accessToken = rjson.getString("access_token");

        return accessToken;
    }

    private KakaoUserInfoDto getUserInfo(String code) {
        // 1. 인가코드 -> 액세스 토큰
        String accessToken = getAccessToken(code);
        // 2. 액세스 토큰 -> 카카오 사용자 정보
        KakaoUserInfoDto userInfo = getUserInfoByToken(accessToken);

        return userInfo;
    }

    private KakaoUserInfoDto getUserInfoByToken(String accessToken) {
        // HttpHeader 오브젝트 생성
        HttpHeaders headers = new HttpHeaders();
        headers.add("Authorization", "Bearer " + accessToken);
        headers.add("Content-type", "application/x-www-form-urlencoded;charset=utf-8");

        // HttpHeader와 HttpBody를 하나의 오브젝트에 담기
        RestTemplate rt = new RestTemplate();
        HttpEntity<MultiValueMap<String, String>> kakaoProfileRequest = new HttpEntity<>(headers);

        // Http 요청하기 - Post방식으로 - 그리고 response 변수의 응답 받음.
        ResponseEntity<String> response = rt.exchange(
                "https://kapi.kakao.com/v2/user/me",
                HttpMethod.POST,
                kakaoProfileRequest,
                String.class
        );

        JSONObject body = new JSONObject(response.getBody());
        Long id = body.getLong("id");
        String email = body.getJSONObject("kakao_account").getString("email");
        String nickname = body.getJSONObject("properties").getString("nickname");

        return new KakaoUserInfoDto(id, email, nickname);
    }

}
