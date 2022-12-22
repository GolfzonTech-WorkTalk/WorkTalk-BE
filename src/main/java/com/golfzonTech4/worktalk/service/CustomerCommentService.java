package com.golfzonTech4.worktalk.service;

import com.golfzonTech4.worktalk.domain.*;
import com.golfzonTech4.worktalk.dto.customercomment.CustomerCommentInsertDto;
import com.golfzonTech4.worktalk.dto.customercomment.CustomerCommentUpdateDto;
import com.golfzonTech4.worktalk.repository.customercenter.CustomerCenterRepository;
import com.golfzonTech4.worktalk.repository.customercenter.CustomerCommentRepository;
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
public class CustomerCommentService {

    private final CustomerCenterRepository customerCenterRepository;
    private final CustomerCommentRepository customerCommentRepository;
    private final MemberService memberService;

    @Transactional
    public CustomerComment createCustomerComment(CustomerCommentInsertDto dto){
        log.info("createCustomerComment()....");
        Optional<String> member = SecurityUtil.getCurrentUsername();

        if(!member.isPresent()) throw new EntityNotFoundException("Member Not Found");

        Member findName = memberService.findByName(member.get());
        CustomerCenter findCcId = customerCenterRepository.findByCcId(dto.getCcId());

        CustomerComment customercommentToCreate = new CustomerComment();
        BeanUtils.copyProperties(dto, customercommentToCreate);
        customercommentToCreate.setCustomerCenter(findCcId);

        return  customerCommentRepository.save(customercommentToCreate);
    }

    @Transactional
    public void updateCustomerComment(Long ccCommentId, CustomerCommentUpdateDto dto) {
        log.info("updateCustomerComment()....");

        Optional<CustomerComment> optionalCustomerComment = Optional.ofNullable(customerCommentRepository.findByCcCommentId(ccCommentId));

            CustomerComment customerComment = optionalCustomerComment.get();
            customerComment.setContent(dto.getContent());//dirty checking

    }

    @Transactional
    public void deleteCustomerComment(Long ccCommentId) {
        log.info("deleteQnaComment()....");
        customerCommentRepository.deleteById(ccCommentId);
    }
}