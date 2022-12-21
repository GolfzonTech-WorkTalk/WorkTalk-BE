package com.golfzonTech4.worktalk.repository.member;

import com.golfzonTech4.worktalk.domain.Member;
import com.golfzonTech4.worktalk.domain.MemberType;
import com.golfzonTech4.worktalk.dto.member.*;
import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.jpa.impl.JPAQueryFactory;
import lombok.extern.slf4j.Slf4j;

import javax.persistence.EntityManager;
import java.util.List;
import java.util.Optional;

import static com.golfzonTech4.worktalk.domain.QMember.member;
import static com.golfzonTech4.worktalk.domain.QPenalty.penalty;

@Slf4j
public class MemberRepositoryImpl implements MemberRepositoryCustom{
    private final JPAQueryFactory queryFactory;

    public MemberRepositoryImpl(EntityManager em) {
        this.queryFactory = new JPAQueryFactory(em);
    }

    public List<MemberDto> findDeactMemeber(MemberSerachDto dto) {
        return queryFactory
                .select(new QMemberDto(member.id, member.email, member.name, member.tel, member.memberType, member.activated, member.KakaoYn))
                .from(member)
                .where(eqActivated(dto.getActivated()), member.memberType.eq(MemberType.ROLE_HOST))
                .fetch();
    }

    public List<MemberPenaltyDto> findNoshowMember(Integer activated) {
        return queryFactory
                .select(new QMemberPenaltyDto(member.id, member.email, member.name, member.tel, member.memberType, member.activated,
                        penalty.penaltyId, penalty.penaltyReason, penalty.penaltyType, penalty.penaltyDate
                ))
                .from(member)
                .leftJoin(penalty)
                .on(member.id.eq(penalty.member.id))
                .where(eqActivated(activated), member.memberType.eq(MemberType.ROLE_USER))
                .fetch();
    }

    @Override
    public Optional<Member> findByWorkTalk(String email) {
        Member result = queryFactory
                .select(member)
                .from(member)
                .where(member.KakaoYn.eq("N"), member.email.eq(email))
                .fetchOne();
        return Optional.ofNullable(result);
    }

    @Override
    public HostDto findActivated(String name) {
        return queryFactory
                .select(new QHostDto(member.id, member.name, member.activated))
                .from(member)
                .where(member.activated.eq(1), member.name.eq(name))
                .fetchOne();
    }


    private BooleanExpression eqActivated(Integer activated
    ) {
        if (activated == null) {
            return null;
        }
        return member.activated.eq(activated);
    }
}
