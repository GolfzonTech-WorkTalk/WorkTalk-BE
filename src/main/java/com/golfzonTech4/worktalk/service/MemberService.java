package com.golfzonTech4.worktalk.service;

import com.golfzonTech4.worktalk.domain.Member;
import com.golfzonTech4.worktalk.domain.MemberType;
import com.golfzonTech4.worktalk.domain.ReserveStatus;
import com.golfzonTech4.worktalk.dto.member.MemberDetailDto;
import com.golfzonTech4.worktalk.dto.member.MemberUpdateDto;
import com.golfzonTech4.worktalk.dto.reservation.ReserveSimpleDto;
import com.golfzonTech4.worktalk.exception.NotFoundMemberException;
import com.golfzonTech4.worktalk.repository.ListResult;
import com.golfzonTech4.worktalk.repository.MemberRepository;
import com.golfzonTech4.worktalk.repository.reservation.ReservationSimpleRepository;
import com.golfzonTech4.worktalk.util.SecurityUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional(readOnly = true)
public class MemberService {
    private final MemberRepository memberRepository;
    private final PasswordEncoder passwordEncoder;

    private final ReservationSimpleRepository reservationSimpleRepository;

    @Transactional
    public Long join(MemberDetailDto dto) {
        log.info("signup : {}", dto);

        Member member = new Member();

        member.setEmail(dto.getEmail());
        member.setPw(dto.getPw());
        member.setName(dto.getName());
        member.setTel(dto.getTel());

        // request의 role 값에 따라 회원 구분 및 활성화 여부를 다르게 설정
        if (dto.getRole() == 0) {
            member.setMemberType(MemberType.ROLE_USER);
            member.setActivated(1);
        } else if(dto.getRole() == 1){
            member.setMemberType(MemberType.ROLE_HOST);
        } else {
            member.setMemberType(MemberType.ROLE_MASTER);
            member.setActivated(1);
        }

        member.setPw(passwordEncoder.encode(member.getPw())); // 비밀번호 인코딩
        member.setImgName("profill.png"); // 프로필 이미지 => 기본 이미지로 설정

        findDuplicatesName(member); // 회원명 중복 검증 => 중복 회원 존재할 경우 예외 처리

        return memberRepository.save(member).getId();
    }

    /**
     * 회원 정보 수정 로직
     */
    @Transactional
    public Long update(MemberUpdateDto dto) {
        log.info("update : {}", dto);
        Member findMember = memberRepository.findById(dto.getId()).get();
        if (dto.getTel() != null || !dto.getTel().trim().isEmpty())  findMember.setTel(dto.getTel());
        if (dto.getPw() != null || !dto.getPw().trim().isEmpty()) findMember.setPw(passwordEncoder.encode(dto.getPw()));
        return findMember.getId();
    }

    /**
     * 회원 탈퇴 로직
     * 사용자: 탈퇴 희망 회원이 진핸중인 예약건/ 결제건이 있을 경우 탈퇴 불가
     * 호스트: 탈퇴 희망 회원이 진핸중인 예약건이 있을 경우 탈퇴 불가
     */
    public void leave(Long memberId) {
        log.info("leave : {}", memberId);
        String name = SecurityUtil.getCurrentUsername().get();
        String role = SecurityUtil.getCurrentUserRole().get();
        if (role.equals(MemberType.ROLE_USER)) {
            ListResult findReserves = reservationSimpleRepository.findAllByUser(name, 0, null, ReserveStatus.BOOKED);
            if (findReserves.getCount() != 0) throw new IllegalStateException("진행 중인 예약/결제건이 있는 회원입니다.");
        } else if (role.equals(MemberType.ROLE_HOST)) {
            List<ReserveSimpleDto> findReserves = reservationSimpleRepository.findAllByHost(name, LocalDate.now());
            if (findReserves.size() != 0) throw new IllegalStateException("진행 중인 예약건이 있는 회원입니다.");
        }
        Member deleteMember = memberRepository.findById(memberId).get();
        memberRepository.delete(deleteMember);

    }

    /**
     * 회원명 중복 확인 서비스 로직
     * 중복 시 예외 처리
     */
    public void findDuplicatesName(Member member) {
        log.info("findDuplicatesName : {}", member);
        Optional<Member> result = memberRepository.findByName(member.getName());
        if (!result.isEmpty()) {
            throw new IllegalArgumentException("이미 존재하는 회원입니다.");
        }
    }

    /**
     * 회원 단건 조회(이름 기준)
     */
    public Member findByName(String name) {
        log.info("findByName : {}", name);
        return memberRepository.findByName(name).get();
    }


    /**
     * 회원 이메일 중복 확인 서비스 로직
     * 중복 시 예외 처리
     */
    public void findDuplicatesEmail(Member member) {
        log.info("findDuplicatesName : {}", member);
        Optional<Member> result = memberRepository.findByEmail(member.getEmail());
        if (!result.isEmpty()) {
            throw new IllegalStateException("이미 사용중인 이메일입니다.");
        }
    }

    /**
     * 전체 회원 리스트 조회
     */
    public List<Member> findAll() {
        log.info("findAll");
        return memberRepository.findAll();
    }



    // member -> memberDetailDto
    private static MemberDetailDto getMemberDetailDto(Member member) {
        MemberDetailDto memberDetailDto = new MemberDetailDto();
        
        // 추후 생성자로 수정
        memberDetailDto.setId(member.getId());
        memberDetailDto.setEmail(member.getEmail());
        memberDetailDto.setPw(member.getPw());
        memberDetailDto.setName(member.getName());
        memberDetailDto.setTel(member.getTel());
        memberDetailDto.setMemberType(member.getMemberType());
        memberDetailDto.setImgName(member.getImgName());

        return memberDetailDto;
    }
}
