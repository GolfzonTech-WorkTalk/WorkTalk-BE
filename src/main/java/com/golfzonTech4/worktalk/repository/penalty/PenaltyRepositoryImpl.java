package com.golfzonTech4.worktalk.repository.penalty;

import com.golfzonTech4.worktalk.dto.penalty.PenaltySearchDto;
import com.golfzonTech4.worktalk.dto.penalty.QPenaltySearchDto;
import com.querydsl.jpa.impl.JPAQueryFactory;
import lombok.extern.slf4j.Slf4j;

import javax.persistence.EntityManager;
import java.util.List;

import static com.golfzonTech4.worktalk.domain.QPenalty.penalty;

@Slf4j
public class PenaltyRepositoryImpl implements PenaltyRepositoryCustom {
    private final JPAQueryFactory queryFactory;

    public PenaltyRepositoryImpl(EntityManager em) {
        this.queryFactory = new JPAQueryFactory(em);
    }

    public List<PenaltySearchDto> findPenalties() {
        log.info("findPenalties");
        return queryFactory.select(new QPenaltySearchDto(
                        penalty.penaltyId, penalty.penaltyReason, penalty.penaltyType, penalty.penaltyDate,
                        penalty.member.id, penalty.member.email, penalty.member.name, penalty.member.tel, penalty.member.memberType,
                        penalty.member.activated
                ))
                .distinct()
                .from(penalty).fetch();
    }
}
