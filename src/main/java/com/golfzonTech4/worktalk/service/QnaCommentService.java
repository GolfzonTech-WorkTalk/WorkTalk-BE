package com.golfzonTech4.worktalk.service;

import com.golfzonTech4.worktalk.domain.Member;
import com.golfzonTech4.worktalk.domain.Qna;
import com.golfzonTech4.worktalk.domain.QnaComment;
import com.golfzonTech4.worktalk.dto.qnacomment.QnaCommentInsertDto;
import com.golfzonTech4.worktalk.dto.qnacomment.QnaCommentUpdateDto;
import com.golfzonTech4.worktalk.repository.qna.QnaCommentRepository;
import com.golfzonTech4.worktalk.repository.qna.QnaRepository;
import com.golfzonTech4.worktalk.util.SecurityUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.persistence.EntityNotFoundException;
import java.util.Optional;

@Slf4j
@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class QnaCommentService {

    private final QnaRepository qnaRepository;
    private final QnaCommentRepository qnaCommentRepository;
    private final MemberService memberService;


    @Transactional
    public QnaComment createQnaComment(QnaCommentInsertDto dto){
        log.info("createQnaComment()....");
        Optional<String> member = SecurityUtil.getCurrentUsername();

        if(!member.isPresent()) throw new EntityNotFoundException("Member Not Found");

        Member findName = memberService.findByName(member.get());
        Qna findQnaId = qnaRepository.findByQnaId(dto.getQnaId());

        QnaComment qnacommentToCreate = new QnaComment();
        BeanUtils.copyProperties(dto, qnacommentToCreate);
        qnacommentToCreate.setMember(findName);
        qnacommentToCreate.setQna(findQnaId);

        return  qnaCommentRepository.save(qnacommentToCreate);
    }

    @Transactional
    public void updateQnaComment(Long qnaCommentId, QnaCommentUpdateDto dto) {
        log.info("updateQnaComment()....");
        Optional<String> currentUsername = SecurityUtil.getCurrentUsername();

        Optional<QnaComment> optionalQnaComment = Optional.ofNullable(qnaCommentRepository.findByQnaCommentId(qnaCommentId));

        if (!currentUsername.isPresent()) throw new EntityNotFoundException("Member Not Found");

        Member findMember = memberService.findByName(currentUsername.get());
        //qna작성자와 접속자가 같은지 확인
        if (findMember.getId() == optionalQnaComment.get().getMember().getId()) {
            QnaComment qna = optionalQnaComment.get();
            qna.setQnacomment(dto.getQnacomment());//dirty checking
        } else
            throw new EntityNotFoundException("수정 권한이 없습니다.");
    }

    @Transactional
    public void deleteQnaComment(Long qnaCommentId){
        log.info("deleteQnaComment()....");

        Optional<String> currentUsername = SecurityUtil.getCurrentUsername();

        Optional<QnaComment> optionalQnaComment = Optional.ofNullable(qnaCommentRepository.findByQnaCommentId(qnaCommentId));
        if (!currentUsername.isPresent()) throw new EntityNotFoundException("Member Not Found");
        Member findMember = memberService.findByName(currentUsername.get());
        //qna작성자와 접속자가 같은지 확인
        if (findMember.getId() == optionalQnaComment.get().getMember().getId()) {
            qnaCommentRepository.deleteById(qnaCommentId);
        } else
            throw new EntityNotFoundException("삭제 권한이 없습니다.");
    }

}
