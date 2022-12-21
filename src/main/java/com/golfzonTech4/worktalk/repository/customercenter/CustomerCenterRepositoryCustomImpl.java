package com.golfzonTech4.worktalk.repository.customercenter;

import com.golfzonTech4.worktalk.domain.CcType;
import com.golfzonTech4.worktalk.domain.MemberType;
import com.golfzonTech4.worktalk.dto.customercenter.CustomerCenterDetailDto;
import com.golfzonTech4.worktalk.dto.customercenter.CustomerCenterSearchDto;
import com.golfzonTech4.worktalk.dto.customercenter.QCustomerCenterDetailDto;
import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.jpa.impl.JPAQueryFactory;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;

import javax.persistence.EntityManager;
import java.util.List;

import static com.golfzonTech4.worktalk.domain.QCustomerCenter.customerCenter;
import static com.golfzonTech4.worktalk.domain.QCustomerComment.customerComment;
import static com.querydsl.core.types.dsl.Expressions.asString;

@Slf4j
public class CustomerCenterRepositoryCustomImpl implements CustomerCenterRepositoryCustom {

    private JPAQueryFactory queryFactory; // 동적 쿼리 생성 위한 클래스

    // JPAQueryFactory 생성자로 EntityManager 넣어줌
    public CustomerCenterRepositoryCustomImpl(EntityManager em) {
        this.queryFactory = new JPAQueryFactory(em);
    }

    @Override
    public PageImpl<CustomerCenterDetailDto> customerManagePage(PageRequest pageRequest, CustomerCenterSearchDto dto) {

        List<CustomerCenterDetailDto> content = queryFactory
                .select(
                        new QCustomerCenterDetailDto(
                                customerCenter.ccId,
                                customerCenter.member.id,
                                customerCenter.member.name,
                                customerCenter.title,
                                customerCenter.content,
                                customerCenter.type,
                                customerCenter.lastModifiedDate,
                                customerComment.ccCommentId,
                                customerComment.content,
                                customerComment.lastModifiedDate
                        )
                )
                .from(customerCenter)
                .leftJoin(customerComment).on(customerCenter.ccId.eq(customerComment.ccCommentId))
                .where(eqMemberType(dto.getSearchMemberType()), eqccType(dto.getSearchccType()))
                .orderBy(customerCenter.ccId.desc())
                .offset(pageRequest.getOffset())
                .limit(pageRequest.getPageSize())
                .fetch();

        long total = queryFactory
                .select(customerCenter.count())
                .from(customerCenter)
                .leftJoin(customerComment).on(customerCenter.ccId.eq(customerComment.ccCommentId))
                .where(eqMemberType(dto.getSearchMemberType()), eqccType(dto.getSearchccType()))
                .fetchOne();

        return new PageImpl<>(content, pageRequest, total);
    }

    @Override
    public PageImpl<CustomerCenterDetailDto> findccDtoListByMember(String name, PageRequest pageRequest, CustomerCenterSearchDto dto) {
        log.info("memberType:{}", name);
        List<CustomerCenterDetailDto> content = queryFactory
                .select(
                        new QCustomerCenterDetailDto(
                                customerCenter.ccId,
                                customerCenter.member.id,
                                asString(name).as("name"),
                                customerCenter.title,
                                customerCenter.content,
                                customerCenter.type,
                                customerCenter.lastModifiedDate,
                                customerComment.ccCommentId,
                                customerComment.content,
                                customerComment.lastModifiedDate
                        )
                )
                .from(customerCenter)
                .leftJoin(customerComment).on(customerCenter.ccId.eq(customerComment.ccCommentId))
                .where(customerCenter.member.name.eq(name), eqccType(dto.getSearchccType()))
                .orderBy(customerCenter.ccId.desc())
                .offset(pageRequest.getOffset())
                .limit(pageRequest.getPageSize())
                .fetch();

        long total = queryFactory
                .select(customerCenter.count())
                .from(customerCenter)
                .leftJoin(customerComment).on(customerCenter.ccId.eq(customerComment.ccCommentId))
                .where(customerCenter.member.name.eq(name), eqccType(dto.getSearchccType()))
                .fetchOne();

        return new PageImpl<>(content, pageRequest, total);
    }

    private BooleanExpression eqMemberType(MemberType searchMemberType) {
        if (searchMemberType == null) {
            return null;
        }
        return customerCenter.member.memberType.eq(searchMemberType);
    }

    private BooleanExpression eqccType(CcType searchccType) {
        if (searchccType == null) {
            return null;
        }
        return customerCenter.type.eq(searchccType);
    }

}
