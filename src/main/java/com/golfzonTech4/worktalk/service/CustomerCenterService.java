package com.golfzonTech4.worktalk.service;

import com.golfzonTech4.worktalk.domain.CustomerCenter;
import com.golfzonTech4.worktalk.domain.Member;
import com.golfzonTech4.worktalk.dto.customercenter.CustomerCenterDetailDto;
import com.golfzonTech4.worktalk.dto.customercenter.CustomerCenterInsertDto;
import com.golfzonTech4.worktalk.dto.customercenter.CustomerCenterSearchDto;
import com.golfzonTech4.worktalk.dto.customercenter.CustomerCenterUpdateDto;
import com.golfzonTech4.worktalk.repository.ListResult;
import com.golfzonTech4.worktalk.repository.customercenter.CustomerCenterRepository;
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
public class CustomerCenterService {
    private final CustomerCenterRepository customerCenterRepository;
    private final MemberService memberService;

    @Transactional
    public CustomerCenter createCustmerCenter(CustomerCenterInsertDto dto) {
        log.info("createCustmerCenter()....");
        Optional<String> member = SecurityUtil.getCurrentUsername();

        if (!member.isPresent()) throw new EntityNotFoundException("Member Not Found");

        Member findMember = memberService.findByName(member.get());

        CustomerCenter ccToCreate = new CustomerCenter();
        BeanUtils.copyProperties(dto, ccToCreate);
        ccToCreate.setMember(findMember);

        return customerCenterRepository.save(ccToCreate);
    }

    @Transactional
    public void updateCustomerCenter(Long ccId, CustomerCenterUpdateDto dto) {
        log.info("updateCustomerCenter()....");
        Optional<String> currentUsername = SecurityUtil.getCurrentUsername();

        Optional<CustomerCenter> optionalCustomerCenter = Optional.ofNullable(customerCenterRepository.findByCcId(ccId));

        if (!currentUsername.isPresent()) throw new EntityNotFoundException("Member Not Found");
        if (!optionalCustomerCenter.isPresent()) throw new EntityNotFoundException("CustomerCenter Not Found");

        Member findMember = memberService.findByName(currentUsername.get());
        //qna???????????? ???????????? ????????? ??????
        if (findMember.getId() == optionalCustomerCenter.get().getMember().getId()) {
            CustomerCenter customerCenter = optionalCustomerCenter.get();
            customerCenter.setContent(dto.getContent());//dirty checking
        } else
            throw new EntityNotFoundException("?????? ????????? ????????????.");
    }

    @Transactional
    public void deleteCustomerCenter(Long ccId) {
        log.info("deleteCustomerCenter()....");

        Optional<String> currentUsername = SecurityUtil.getCurrentUsername();
        if (!currentUsername.isPresent()) throw new EntityNotFoundException("Member Not Found");

        Optional<CustomerCenter> optionalCustomerCenter = Optional.ofNullable(customerCenterRepository.findByCcId(ccId));

        Member findMember = memberService.findByName(currentUsername.get());
        //qna???????????? ???????????? ????????? ??????
        if (findMember.getId() == optionalCustomerCenter.get().getMember().getId()) {
            customerCenterRepository.deleteById(ccId);
        } else
            throw new EntityNotFoundException("?????? ????????? ????????????.");
    }

    public ListResult getMyCustomerCenterList(PageRequest pageRequest, CustomerCenterSearchDto dto) {
        log.info("getMyCustomerCenterList()....");

        Optional<String> currentUsername = SecurityUtil.getCurrentUsername();
        if (currentUsername.isEmpty()) throw new EntityNotFoundException("Member not found");

        PageImpl<CustomerCenterDetailDto> result = customerCenterRepository.findccDtoListByMember(currentUsername.get(), pageRequest, dto);
        return new ListResult(result.getTotalElements(), result.getContent());
    }

    public ListResult getccMasterManage(PageRequest pageRequest, CustomerCenterSearchDto dto) {
        log.info("getccMasterManage()....");

        PageImpl<CustomerCenterDetailDto> result = customerCenterRepository.customerManagePage(pageRequest, dto);
        return new ListResult(result.getTotalElements(), result.getContent());
    }

}
