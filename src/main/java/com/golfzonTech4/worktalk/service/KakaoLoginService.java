package com.golfzonTech4.worktalk.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.golfzonTech4.worktalk.domain.Member;
import com.golfzonTech4.worktalk.dto.member.KakaoUserInfoDto;
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

        // ?????? ?????? = ????????? id + nickname
        String name = kakaoId + nickname;

        // ???????????? = ????????? id
        String password = String.valueOf(kakaoId);
        log.info("name:{}", name);
        log.info("password:{}", password);

        // DB??? ????????? ???????????? ????????? ??????
        Member kakaouser = memberRepository.findByEmail(email).orElse(null);
        log.info("findByEmail:{}", kakaouser);

        HashMap<String, String> map = new HashMap<>();
        // DB??? ?????? ?????? ????????? ????????? ????????????
        if (kakaouser == null) {
            Member member = new Member();
            member.setEmail(email);
            member.setPw(passwordEncoder.encode(password));// ???????????? ?????????
            member.setName(name);
            member.setMemberType(ROLE_USER);
            member.setImgName("profill.png");
            member.setKakaoYn("Y");
            member.setActivated(1);

            Member save = memberRepository.save(member);

            map.put("tel", "false");
        }

        else if (kakaouser != null) {
            if (kakaouser.getTel() == null) {
                map.put("tel", "false");
            } else {
                map.put("tel", "true");
            }
        }

        //????????? ??????
        UsernamePasswordAuthenticationToken authenticationToken =
                new UsernamePasswordAuthenticationToken(email, password); // ?????? ??????

        Authentication authentication = authenticationManagerBuilder.getObject().authenticate(authenticationToken);
        SecurityContextHolder.getContext().setAuthentication(authentication); // Authentication Token => context ??????

        map.put("jwt", tokenProvider.createToken(authentication));

        return map;
    }

    private String getAccessToken(String code) {
        // HTTP Header ???????????? ??????
        HttpHeaders headers = new HttpHeaders();
        headers.add("Content-type", "application/x-www-form-urlencoded;charset=utf-8");

        // HTTP Body ??????
        MultiValueMap<String, String> body = new LinkedMultiValueMap<>();
        body.add("grant_type", "authorization_code");
        body.add("client_id", "79edba60f18097e8e335a7ca1b62de99");                  // REST API ???
        body.add("redirect_uri", "http://15.165.247.125:8100/user/kakao/callback");      // Redirect URI
        body.add("code", code);

        // HttpHeader??? HttpBody??? ????????? ??????????????? ??????
        RestTemplate rt = new RestTemplate();
        rt.setRequestFactory(new HttpComponentsClientHttpRequestFactory()); // ?????? ??????
        HttpEntity<MultiValueMap<String, String>> kakaoTokenRequest =
                new HttpEntity<>(body, headers);

        // Http ???????????? - Post???????????? - ????????? response ????????? ?????? ??????.
        ResponseEntity<String> response = rt.exchange(
                "https://kauth.kakao.com/oauth/token",
                HttpMethod.POST,
                kakaoTokenRequest,
                String.class
        );

        // HTTP ?????? (JSON) -> ????????? ?????? ??????
        String tokenJson = response.getBody();
        JSONObject rjson = new JSONObject(tokenJson);
        String accessToken = rjson.getString("access_token");

        return accessToken;
    }

    private KakaoUserInfoDto getUserInfo(String code) {
        // 1. ???????????? -> ????????? ??????
        String accessToken = getAccessToken(code);
        // 2. ????????? ?????? -> ????????? ????????? ??????
        KakaoUserInfoDto userInfo = getUserInfoByToken(accessToken);

        return userInfo;
    }

    private KakaoUserInfoDto getUserInfoByToken(String accessToken) {
        // HttpHeader ???????????? ??????
        HttpHeaders headers = new HttpHeaders();
        headers.add("Authorization", "Bearer " + accessToken);
        headers.add("Content-type", "application/x-www-form-urlencoded;charset=utf-8");

        // HttpHeader??? HttpBody??? ????????? ??????????????? ??????
        RestTemplate rt = new RestTemplate();
        HttpEntity<MultiValueMap<String, String>> kakaoProfileRequest = new HttpEntity<>(headers);

        // Http ???????????? - Post???????????? - ????????? response ????????? ?????? ??????.
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
