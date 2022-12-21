package com.golfzonTech4.worktalk.repository.mileage;

import com.golfzonTech4.worktalk.domain.Mileage;
import com.golfzonTech4.worktalk.domain.Mileage_status;
import com.golfzonTech4.worktalk.domain.QPay;
import com.golfzonTech4.worktalk.domain.QReservation;
import com.querydsl.jpa.impl.JPAQueryFactory;
import lombok.extern.slf4j.Slf4j;

import javax.persistence.EntityManager;

import java.util.Optional;

import static com.golfzonTech4.worktalk.domain.QMileage.mileage;
import static com.golfzonTech4.worktalk.domain.QPay.pay;
import static com.golfzonTech4.worktalk.domain.QReservation.reservation;

@Slf4j
public class MileageRepositoryImpl implements MileageRepositoryCustom{

    private final JPAQueryFactory queryFactory;

    public MileageRepositoryImpl(EntityManager em) {
        this.queryFactory = new JPAQueryFactory(em);
    }

    /**
     * 적립된 마일리지의 총 합
     */
    @Override
    public Integer getTotalSave(Long memberId) {
        Integer result = queryFactory.select(mileage.mileageAmount.sum())
                .from(mileage)
                .where(mileage.member.id.eq(memberId)
                        .and(mileage.status.eq(Mileage_status.SAVED)))
                .fetchOne();
        if (result == null) return 0;
        return result;
    }

    /**
     * 사용된 마일리지의 총 합
     */
    @Override
    public Integer getTotalUse(Long memberId) {
        Integer result = queryFactory.select(mileage.mileageAmount.sum())
                .from(mileage)
                .where(mileage.member.id.eq(memberId)
                        .and(mileage.status.eq(Mileage_status.USED)))
                .fetchOne();
        if (result == null) return 0;
        return result;
    }

    /**
     * 적립예정인 마일리지의 총 합
     */
    @Override
    public Integer getTotalToBeSaved(Long memberId) {
        Integer result = queryFactory.select(mileage.mileageAmount.sum())
                .from(mileage)
                .where(mileage.member.id.eq(memberId)
                        .and(mileage.status.eq(Mileage_status.TO_BE_SAVED)))
                .fetchOne();
        if (result == null) return 0;
        return result;
    }

    @Override
    public Optional<Mileage> findByReservation(Long reserveId) {
        Mileage findMileage = queryFactory.select(mileage)
                .from(mileage)
                .innerJoin(pay).on(mileage.pay.payId.eq(pay.payId))
                .innerJoin(reservation).on(pay.reservation.reserveId.eq(reservation.reserveId))
                .where(mileage.status.eq(Mileage_status.TO_BE_SAVED), reservation.reserveId.eq(reserveId))
                .fetchOne();
        return Optional.ofNullable(findMileage);
    }

}
