package com.golfzonTech4.worktalk.repository.pay;

import com.golfzonTech4.worktalk.domain.*;
import com.golfzonTech4.worktalk.dto.pay.*;
import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.jpa.impl.JPAQueryFactory;
import com.siot.IamportRestClient.response.Payment;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;

import javax.persistence.EntityManager;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static com.golfzonTech4.worktalk.domain.QPay.pay;
import static com.golfzonTech4.worktalk.domain.QReservation.reservation;
import static com.golfzonTech4.worktalk.domain.QRoom.room;
import static com.golfzonTech4.worktalk.domain.QSpace.space;

@Slf4j
public class PayRepositoryImpl implements PayRepositoryCustom {

    private final JPAQueryFactory queryFactory;

    public PayRepositoryImpl(EntityManager em) {
        this.queryFactory = new JPAQueryFactory(em);
    }

    public List<PayInsertDto> findAlByReserveId(Long reserveId) {
        log.info("findAlByReserveId : {}", reserveId);
        return queryFactory.select(new QPayInsertDto(
                        pay.payId, pay.reservation.reserveId, pay.customerUid,
                        pay.impUid, pay.merchantUid, pay.payStatus, pay.payAmount
                ))
                .from(pay)
                .where(pay.reservation.reserveId.eq(reserveId))
                .fetch();
    }

    @Override
    public Optional<PayInsertDto> findByCUid(String customerUid) {
        log.info("findByCustomerUid : {}", customerUid);
        PayInsertDto result = queryFactory.select(new QPayInsertDto(
                        pay.payId, pay.reservation.reserveId, pay.customerUid,
                        pay.impUid, pay.merchantUid, pay.payStatus, pay.payAmount
                ))
                .from(pay)
                .where(pay.customerUid.eq(customerUid) // 같은 customerUid를 갖는 값 중
                        .and(pay.payStatus.eq(PaymentStatus.POSTPAID_BOOKED))) // 보증금 내역을 호출
                .fetchOne();
        return Optional.ofNullable(result);
    }

    @Override
    public PageImpl<PaySimpleDto> findByUser(String name, LocalDateTime reserveDate, PaymentStatus paymentStatus,
                                             PageRequest pageRequest) {

        List<PaySimpleDto> content = queryFactory.select(new QPaySimpleDto(
                        reservation.reserveId, reservation.bookDate.reserveDate, space.spaceName, room.roomName,
                        pay.payAmount, pay.payStatus, reservation.reserveStatus, reservation.member.name,
                        reservation.member.tel, reservation.reserveAmount))
                .from(pay)
                .join(reservation).on(pay.reservation.reserveId.eq(reservation.reserveId))
                .join(room).on(reservation.room.roomId.eq(room.roomId))
                .join(space).on(room.space.spaceId.eq(space.spaceId))
                .where(reservation.member.name.eq(name), eqReserveDate(reserveDate), eqPaymentStatus(paymentStatus))
                .orderBy(reservation.reserveId.desc())
                .orderBy(pay.payId.desc())
                .offset(pageRequest.getOffset())
                .limit(pageRequest.getPageSize())
                .fetch();

        Long count = queryFactory.select(pay.count())
                .from(pay)
                .join(reservation).on(pay.reservation.reserveId.eq(reservation.reserveId))
                .join(room).on(reservation.room.roomId.eq(room.roomId))
                .join(space).on(room.space.spaceId.eq(space.spaceId))
                .where(reservation.member.name.eq(name), eqReserveDate(reserveDate), eqPaymentStatus(paymentStatus))
                .fetchOne();

        return new PageImpl<>(content, pageRequest, count);
    }

    @Override
    public PageImpl<PaySimpleDto> findByHost(String name, LocalDateTime reserveDate, PaymentStatus paymentStatus,
                                             Integer spaceType, String roomName, PageRequest pageRequest) {

        List<PaySimpleDto> content = queryFactory.select(new QPaySimpleDto(
                        reservation.reserveId, reservation.bookDate.reserveDate, space.spaceName, room.roomName,
                        pay.payAmount, pay.payStatus, reservation.reserveStatus, reservation.member.name, reservation.member.tel, reservation.reserveAmount))
                .from(pay)
                .join(reservation).on(pay.reservation.reserveId.eq(reservation.reserveId))
                .join(room).on(reservation.room.roomId.eq(room.roomId))
                .join(space).on(room.space.spaceId.eq(space.spaceId))
                .where(space.member.name.eq(name), eqReserveDate(reserveDate), eqPaymentStatus(paymentStatus),
                        eqRoom(roomName), eqSpaceType(spaceType))
                .orderBy(reservation.reserveId.desc())
                .orderBy(pay.payId.desc())
                .offset(pageRequest.getOffset())
                .limit(pageRequest.getPageSize())
                .fetch();

        Long count = queryFactory.select(pay.count())
                .from(pay)
                .join(reservation).on(pay.reservation.reserveId.eq(reservation.reserveId))
                .join(room).on(reservation.room.roomId.eq(room.roomId))
                .join(space).on(room.space.spaceId.eq(space.spaceId))
                .where(space.member.name.eq(name), eqReserveDate(reserveDate), eqPaymentStatus(paymentStatus),
                        eqRoom(roomName), eqSpaceType(spaceType))
                .fetchOne();

        return new PageImpl<>(content, pageRequest, count);
    }

    @Override
    public List<PayRoomDto> findRooms(String name) {

        List<PayRoomDto> content = queryFactory.select(new QPayRoomDto(room.roomName, space.spaceType))
                .from(pay)
                .join(reservation).on(pay.reservation.reserveId.eq(reservation.reserveId))
                .join(room).on(reservation.room.roomId.eq(room.roomId))
                .join(space).on(room.space.spaceId.eq(space.spaceId))
                .where(space.member.name.eq(name))
                .fetch();

        return content;
    }

    static BooleanExpression eqReserveDate(LocalDateTime reserveDate) {
        if (reserveDate == null) {
            return null;
        }
        return reservation.bookDate.reserveDate.after(reserveDate)
                .or(reservation.bookDate.reserveDate.eq(reserveDate));
    }

    static BooleanExpression eqPaymentStatus(PaymentStatus paymentStatus) {
        if (paymentStatus == null) {
            return null;
        } else if (paymentStatus == PaymentStatus.REFUND) {
            return pay.payStatus.eq(paymentStatus);
        }
        return pay.payStatus.eq(paymentStatus)
                .and(pay.reservation.reserveStatus.ne(ReserveStatus.CANCELED_BY_HOST)
                        .and(pay.reservation.reserveStatus.ne(ReserveStatus.CANCELED_BY_HOST)));
    }

    static BooleanExpression eqRoom(String roomName) {
        if (roomName == null || roomName.isEmpty()) {
            return null;
        }
        return room.roomName.eq(roomName);
    }

    static BooleanExpression eqSpaceType(Integer spaceType) {
        if (spaceType == null) {
            return null;
        }
        return space.spaceType.eq(spaceType);
    }
}
