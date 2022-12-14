package com.golfzonTech4.worktalk.service;

import com.golfzonTech4.worktalk.domain.Member;
import com.golfzonTech4.worktalk.domain.Qna;
import com.golfzonTech4.worktalk.domain.Space;
import com.golfzonTech4.worktalk.dto.qna.QnaDetailDto;
import com.golfzonTech4.worktalk.dto.qna.QnaInsertDto;
import com.golfzonTech4.worktalk.dto.qna.QnaSearchDto;
import com.golfzonTech4.worktalk.dto.qna.QnaUpdateDto;
import com.golfzonTech4.worktalk.repository.ListResult;
import com.golfzonTech4.worktalk.repository.qna.QnaRepository;
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
import java.util.Optional;

@Slf4j
@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class QnaService {

    private final SpaceRepository spaceRepository;
    private final QnaRepository qnaRepository;
    private final MemberService memberService;

    @Transactional
    public Qna createQna(QnaInsertDto dto) {
        log.info("createQna()....");
        Optional<String> member = SecurityUtil.getCurrentUsername();
        Optional<Space> space = Optional.ofNullable(spaceRepository.findBySpaceId(dto.getSpaceId()));

        if (!member.isPresent()) throw new EntityNotFoundException("Member Not Found");
        if (!space.isPresent()) throw new EntityNotFoundException("Space Not Found");

        Member findMember = memberService.findByName(member.get());
        Space findSpace = spaceRepository.findBySpaceId(dto.getSpaceId());

        Qna qnaToCreate = new Qna();
        BeanUtils.copyProperties(dto, qnaToCreate);
        qnaToCreate.setMember(findMember);
        qnaToCreate.setSpace(findSpace);

        return qnaRepository.save(qnaToCreate);
    }

    @Transactional
    public void updateQna(Long qnaId, QnaUpdateDto dto) {
        log.info("updateQna()....");
        Optional<String> currentUsername = SecurityUtil.getCurrentUsername();

        Optional<Qna> optionalQna = Optional.ofNullable(qnaRepository.findByQnaId(qnaId));

        if (!currentUsername.isPresent()) throw new EntityNotFoundException("Member Not Found");
        if (!optionalQna.isPresent()) throw new EntityNotFoundException("QnA Not Found");

        Member findMember = memberService.findByName(currentUsername.get());
        //qna???????????? ???????????? ????????? ??????
        if (findMember.getId() == optionalQna.get().getMember().getId()) {
            Qna qna = optionalQna.get();
            qna.setContent(dto.getContent());//dirty checking
        } else
            throw new EntityNotFoundException("?????? ????????? ????????????.");
    }

    @Transactional
    public void deleteQna(Long qnaId) {
        log.info("deleteQna()....");

        Optional<String> currentUsername = SecurityUtil.getCurrentUsername();
        if (!currentUsername.isPresent()) throw new EntityNotFoundException("Member Not Found");

        Optional<Qna> optionalQna = Optional.ofNullable(qnaRepository.findByQnaId(qnaId));

        Member findMember = memberService.findByName(currentUsername.get());
        //qna???????????? ???????????? ????????? ??????
        if (findMember.getId() == optionalQna.get().getMember().getId()) {
            qnaRepository.deleteById(qnaId);
        } else
            throw new EntityNotFoundException("?????? ????????? ????????????.");
    }

    public ListResult getQnasBySpace(PageRequest pageRequest, Long spaceId) {
        log.info("getQnasBySpace()....");
        PageImpl<QnaDetailDto> result = qnaRepository.findQnaDtoListBySpaceId(pageRequest, spaceId);
        return new ListResult(result.getTotalElements(), result.getContent());
    }

    public ListResult getMyQnas(PageRequest pageRequest, QnaSearchDto dto) {
        log.info("getMyQnas()....");

        Optional<String> currentUsername = SecurityUtil.getCurrentUsername();
        if (currentUsername.isEmpty()) throw new EntityNotFoundException("Member not found");
        dto.setSearchUser(currentUsername.get());

        PageImpl<QnaDetailDto> result = qnaRepository.findQnaDtoListByMember(pageRequest, dto);
        return new ListResult(result.getTotalElements(), result.getContent());
    }

    //???????????? ????????? ?????? ??????????????? QnA ???????????????
    public ListResult getQnaHostManagePage(PageRequest pageRequest, QnaSearchDto dto) {
        log.info("getQnaHostManagePage()....");
        Optional<String> currentUsername = SecurityUtil.getCurrentUsername();
        dto.setSearchHost(currentUsername.get());

        PageImpl<QnaDetailDto> result = qnaRepository.findQnaDtoListbyHostSpace(pageRequest, dto);
        return new ListResult(result.getTotalElements(), result.getContent());
    }

}
