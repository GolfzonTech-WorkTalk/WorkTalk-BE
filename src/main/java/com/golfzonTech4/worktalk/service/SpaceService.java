package com.golfzonTech4.worktalk.service;

import com.golfzonTech4.worktalk.domain.Member;
import com.golfzonTech4.worktalk.domain.Review;
import com.golfzonTech4.worktalk.domain.Space;
import com.golfzonTech4.worktalk.domain.SpaceImg;
import com.golfzonTech4.worktalk.dto.member.HostDto;
import com.golfzonTech4.worktalk.dto.space.*;
import com.golfzonTech4.worktalk.repository.ListResult;
import com.golfzonTech4.worktalk.repository.member.MemberRepository;
import com.golfzonTech4.worktalk.repository.reservation.ReservationSimpleRepository;
import com.golfzonTech4.worktalk.repository.review.ReviewRepository;
import com.golfzonTech4.worktalk.repository.space.SpaceImgRepository;
import com.golfzonTech4.worktalk.repository.space.SpaceRepository;
import com.golfzonTech4.worktalk.util.SecurityUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.persistence.EntityNotFoundException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Slf4j
@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class SpaceService {

    private final MemberRepository memberRepository;
    private final SpaceRepository spaceRepository;
    private final SpaceImgRepository spaceImgRepository;
    private final ReviewRepository reviewRepository;
    private final ReservationSimpleRepository reservationSimpleRepository;
    private final AwsS3Service awsS3Service;

    //사무공간 등록
    @Transactional
    public Long createSpace(SpaceInsertDto dto) {
        log.info("createSpace : {}", dto);
        Optional<String> currentUsername = SecurityUtil.getCurrentUsername();

        Optional<Member> member = memberRepository.findByName(currentUsername.get());

        if (member.get().getActivated() == 0) {
            throw new EntityNotFoundException("계정 승인이 완료되면 이용할 수 있어요");
        }

        List<String> imageurlList = new ArrayList<>();
        if (!dto.getMultipartFileList().isEmpty()) {
            imageurlList.addAll(awsS3Service.upload(dto.getMultipartFileList()));
        } else {
            imageurlList.add(null);
        }

        Space spaceToCreate = new Space();
        BeanUtils.copyProperties(dto, spaceToCreate);
        spaceToCreate.setMember(member.get());
        spaceRepository.save(spaceToCreate);

        for (String imageurl : imageurlList) {
            SpaceImg spaceImgToCreate = new SpaceImg();
            BeanUtils.copyProperties(dto, spaceImgToCreate);
            spaceImgToCreate.setSpace(spaceToCreate);
            spaceImgToCreate.setSpaceImgUrl(imageurl);
            spaceImgRepository.save(spaceImgToCreate);
        }
        return spaceToCreate.getSpaceId();
    }

    //사무공간 수정
    @Transactional
    public void updateSpace(SpaceUpdateDto dto) {
        log.info("updateSpace()....");
        Space space = spaceRepository.findBySpaceId(dto.getSpaceId());
        log.info("dto:{}", dto);

        if (dto.getMultipartFileList() != null) {
            List<String> newImageurlList = new ArrayList<>();
            newImageurlList.addAll(awsS3Service.upload(dto.getMultipartFileList()));
            for (String imageurl : newImageurlList) {
                SpaceImg spaceImg = new SpaceImg();
                spaceImg.setSpace(space);
                spaceImg.setSpaceImgUrl(imageurl);
                spaceImgRepository.save(spaceImg);
            }
        }
        space.setSpaceDetail(dto.getSpaceDetail());
    }

    //사무공간 단일 선택
    public Space selectSpace(Long spaceId) {
        log.info("selectSpace()....");
        Space space = spaceRepository.findBySpaceId(spaceId);
        if (space != null) {
            return space;
        }

        throw new EntityNotFoundException("해당 사무공간을 찾지 못했습니다.");
    }

    //사무공간 상세페이지
    public List<SpaceDetailDto> getSpaceDetailPage(Long spaceId) {
        log.info("selectSpace()....");
        List<SpaceDetailDto> space = spaceRepository.getSpaceDetailPage(spaceId);
        if (space != null) {
            return space;
        }

        throw new EntityNotFoundException("해당 사무공간을 찾지 못했습니다.");
    }

    //호스트가 등록한 사무공간 리스트 조회(공간관리페이지)
    public List<SpaceMainDto> selectSpaceByHost() {
        log.info("selectSpaceByHost()....");
        Optional<String> currentUsername = SecurityUtil.getCurrentUsername();

        List<SpaceMainDto> result = spaceRepository.getHostSpacePage(currentUsername.get());
        return getSpaceList(result);
    }

    //유저 - 사무공간 리스트 조회
    public ListResult getMainSpacePage(PageRequest pageRequest, SpaceSearchDto dto) {
        PageImpl<SpaceMainDto> result = spaceRepository.getMainSpacePage(pageRequest, dto);
        return new ListResult(result.getTotalElements(), getSpaceList(result.getContent()));
    }

    public List<SpaceMainDto> getSpaceList(List<SpaceMainDto> spaceList) {
        List<SpaceMainDto> spaceMainDtoList = new ArrayList<>();
        for (SpaceMainDto space : spaceList) {
            SpaceMainDto dto = space;

            List<Review> reviewList = reviewRepository.findAllBySpaceId(space.getSpaceId());
            double sum = 0;
            for (Review review : reviewList) {
                sum += review.getGrade();
            }
            double avg = (sum == 0) ? 0 : Math.floor((sum / reviewList.size()) * 10) / 10;
            log.info("avg= " + avg);
            dto.setCount(reviewList.size());
            dto.setGradeAvg(avg);
            dto.setSpaceId(space.getSpaceId());
            dto.setSpaceType(space.getSpaceType());
            dto.setSpaceName(space.getSpaceName());
            dto.setAddress(space.getAddress());
            spaceMainDtoList.add(dto);
        }
        return spaceMainDtoList;

    }

    //사무공간 삭제
    @Transactional
    public void deleteSpace(Long spaceId) {
        log.info("deleteSpace()....");
        List<Long> result = reservationSimpleRepository.findBySpace(spaceId);
        if (result.size() > 0) throw new IllegalStateException("예약 이력이 있어 삭제할 수 없습니다.");
        spaceRepository.deleteById(spaceId);
    }

    //마스터 관리페이지
    public ListResult getSpaceMasterPage(PageRequest pageRequest, SpaceManageSortingDto dto) {
        log.info("getSpaceMasterPage()....");
        PageImpl<SpaceMasterDto> result = spaceRepository.getSpaceMasterPage(pageRequest, dto);
        return new ListResult(result.getTotalElements(), result.getContent());
    }

    //마스터의 사무공간 검수승인
    @Transactional
    public Space ApprovedSpace(Long spaceId) {
        log.info("ApprovedSpace()....");
        Space space = selectSpace(spaceId);
        log.info(String.valueOf(spaceId));
        space.setSpaceStatus("approved");    //변경감지
        return space;
    }

    //마스터의 사무공간 거절
    @Transactional
    public Space RejectedSpace(Long spaceId) {
        log.info("RejectedSpace()....");
        Space space = selectSpace(spaceId);
        log.info(String.valueOf(spaceId));
        space.setSpaceStatus("rejected");    //변경감지
        return space;
    }

    //호스트 가입 승인 여부 확인
    public boolean findHostActivated() {
        Optional<String> currentUsername = SecurityUtil.getCurrentUsername();
        HostDto dto = memberRepository.findActivated(currentUsername.get());

        return (dto != null) ? true : false;
    }

    public List<HostSpaceDto> findSpaces() {
        log.info("findSpaces: {}");
        String name = SecurityUtil.getCurrentUsername().get();
        return spaceRepository.getSpacesByHost(name);
    }
}
