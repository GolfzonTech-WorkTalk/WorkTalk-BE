package com.golfzonTech4.worktalk.repository.reservation;

import com.golfzonTech4.worktalk.domain.PaymentStatus;
import com.golfzonTech4.worktalk.domain.ReserveStatus;
import com.golfzonTech4.worktalk.domain.RoomType;
import com.golfzonTech4.worktalk.dto.pay.PayDto;
import com.golfzonTech4.worktalk.dto.pay.QPayDto;
import com.golfzonTech4.worktalk.dto.reservation.QReserveCheckDto;
import com.golfzonTech4.worktalk.dto.reservation.QReserveSimpleDto;
import com.golfzonTech4.worktalk.dto.reservation.ReserveCheckDto;
import com.golfzonTech4.worktalk.dto.reservation.ReserveSimpleDto;
import com.golfzonTech4.worktalk.repository.ListResult;
import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.jpa.impl.JPAQueryFactory;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;

import javax.persistence.EntityManager;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

import static com.golfzonTech4.worktalk.domain.QPay.pay;
import static com.golfzonTech4.worktalk.domain.QReservation.reservation;
import static com.golfzonTech4.worktalk.domain.QReview.review;
import static com.golfzonTech4.worktalk.domain.QRoom.room;
import static com.golfzonTech4.worktalk.domain.QSpace.space;

@Slf4j
public class ReservationSimpleRepositoryImpl implements ReservationSimpleRepositoryCustom {

    private final JPAQueryFactory queryFactory;

    public ReservationSimpleRepositoryImpl(EntityManager em, EntityManager em1) {
        this.queryFactory = new JPAQueryFactory(em);
    }

    @Override
    public ListResult findAllByUser(String name, Integer paid, PaymentStatus paymentStatus, ReserveStatus reserveStatus) {
        log.info("findAllByUser : {}", name);

        List<ReserveSimpleDto> result = queryFactory.select(new QReserveSimpleDto(
                        reservation.room.roomName,
                        reservation.paid,
                        reservation.reserveId,
                        reservation.member.id,
                        reservation.room.roomId,
                        reservation.bookDate,
                        reservation.member.name,
                        reservation.reserveStatus,
                        reservation.paymentStatus,
                        reservation.room.roomType,
                        reservation.reserveAmount,
                        reservation.cancelReason)
                )
                .from(reservation)
                .where(reservation.member.name.eq(name), eqPaid(paid), eqPayStatus(paymentStatus), eqReserveStatus(reserveStatus))
                .fetch();

        return new ListResult((long) result.size(), result);
    }

    @Override
    public PageImpl<ReserveSimpleDto> findAllByUserPage(String name, PageRequest pageRequest,
                                                        ReserveStatus reserveStatus, Integer spaceType) {
        log.info("findAllByUser : {}, {}, {}, {}", name, pageRequest, reserveStatus, spaceType);

        List<ReserveSimpleDto> content = queryFactory.select(new QReserveSimpleDto(
                        reservation.room.roomName,
                        reservation.paid,
                        reservation.reserveId,
                        reservation.member.id,
                        reservation.room.roomId,
                        reservation.bookDate,
                        reservation.member.name,
                        reservation.reserveStatus,
                        reservation.paymentStatus,
                        reservation.room.roomType,
                        reservation.reserveAmount,
                        reservation.cancelReason,
                        review.reviewId)
                )
                .from(reservation)
                .leftJoin(review).on(reservation.reserveId.eq(review.reservation.reserveId))
                .where(reservation.member.name.eq(name), eqReserveStatus(reserveStatus), eqSpaceType(spaceType))
                .orderBy(reservation.reserveId.desc())
                .offset(pageRequest.getOffset())
                .limit(pageRequest.getPageSize())
                .fetch();

        List<Long> reserveIds = content.stream().map(ReserveSimpleDto::getReserveId).collect(Collectors.toList());

        List<PayDto> pays = queryFactory.select(new QPayDto(pay.payId, pay.reservation.reserveId, pay.impUid, pay.merchantUid, pay.customerUid, pay.payStatus, pay.payAmount))
                .from(pay)
                .where(pay.reservation.reserveId.in(reserveIds))
                .orderBy(pay.payId.desc())
                .fetch();

        Map<Long, List<PayDto>> paysMap = pays.stream().collect(Collectors.groupingBy(PayDto::getReserveId));

        content.forEach(p -> p.setPays(paysMap.get(p.getReserveId())));

        Long count = queryFactory
                .select(reservation.count())
                .from(reservation)
                .where(reservation.member.name.eq(name), eqReserveStatus(reserveStatus), eqSpaceType(spaceType))
                .fetchOne();
        return new PageImpl<>(content, pageRequest, count);
    }

    @Override
    public PageImpl<ReserveSimpleDto> findAllByHostPage(String name, PageRequest pageRequest, ReserveStatus reserveStatus, Integer spaceType) {
        log.info("findAllByHostPage : {}, {}, {}, {}", name, pageRequest, reserveStatus, spaceType);

        List<ReserveSimpleDto> content = queryFactory.select(new QReserveSimpleDto(room.roomName, reservation.paid, reservation.reserveId,
                        reservation.member.id, room.roomId, reservation.bookDate, reservation.member.name, reservation.reserveStatus,
                        reservation.paymentStatus, room.roomType, reservation.reserveAmount, reservation.cancelReason, review.reviewId))
                .from(reservation)
                .innerJoin(room).on(reservation.room.roomId.eq(room.roomId))
                .innerJoin(space).on(room.space.spaceId.eq(space.spaceId))
                .leftJoin(review).on(reservation.reserveId.eq(review.reservation.reserveId))
                .where(space.member.name.eq(name), eqReserveStatus(reserveStatus), eqSpaceType(spaceType))
                .orderBy(reservation.reserveId.desc())
                .offset(pageRequest.getOffset())
                .limit(pageRequest.getPageSize())
                .fetch();

        Long count = queryFactory.select(reservation.count())
                .from(reservation)
                .innerJoin(room).on(reservation.room.roomId.eq(room.roomId))
                .innerJoin(space).on(room.space.spaceId.eq(space.spaceId))
                .where(space.member.name.eq(name), eqReserveStatus(reserveStatus), eqSpaceType(spaceType))
                .fetchOne();

        return new PageImpl<>(content, pageRequest, count);
    }

    @Override
    public List<ReserveSimpleDto> findAllByTime() {
        log.info("findAllByTime");
        LocalDate today = LocalDate.now();
        int hour = LocalDateTime.now().getHour() + 1; // 현재 시간에서 1시간을 뺀 값: 매 시간 30분에 조회되기에 시간만 비교하면됨.
        log.info("hour: {}", hour);
        // 예시: 12시 30분 조회 시 => 13시의 예약 데이터들을 조회.
        List<ReserveSimpleDto> result = queryFactory.select(new QReserveSimpleDto(
                        reservation.reserveId,
                        reservation.member.id)
                )
                .from(reservation)
                .where(reservation.bookDate.checkInTime.eq(hour)// 이용시간이 임박한 예약건 들 중
                        .and(reservation.paymentStatus.eq(PaymentStatus.PREPAID)) // 선결제 중
                        .and(reservation.reserveStatus.eq(ReserveStatus.BOOKED)) // 예약 진행중인
                        .and(reservation.paid.eq(0))
                        .and(reservation.bookDate.checkInDate.eq(today))) // 오늘 입실인 예약들에 한하여
                .fetch();

        log.info("result.size() : {}", result.size());

        return result;
    }

    /**
     * 예약 상태가 예약 완료인 예약들 중 특정 공간에 해당되는 예약 ID 값을 조회
     */
    @Override
    public List<Long> findBySpace(Long spaceId) {
        log.info("findBySpace");
        return queryFactory.select(reservation.reserveId)
                .from(reservation)
                .innerJoin(room).on(reservation.room.roomId.eq(room.roomId))
                .innerJoin(space).on(room.space.spaceId.eq(space.spaceId))
                .where(space.spaceId.eq(spaceId), reservation.reserveStatus.eq(ReserveStatus.BOOKED))
                .fetch();
    }

    @Override
    public Long countNoShow(Long memberId, ReserveStatus reserveStatus) {
        log.info("findAllByTime : {}, {}", memberId, reserveStatus);

        return queryFactory.select(new QReserveSimpleDto(
                        reservation.reserveId,
                        reservation.member.id)
                )
                .from(reservation)
                .where(reservation.member.id.eq(memberId) // 해당 접속자의 예약 건들 중
                        .and(reservation.reserveStatus.eq(reserveStatus))) // 예약 상태가 NoShow인 예약들
                .fetch().stream().count(); // 의 횟수를 집계하여 반환
    }

    @Override
    public List<ReserveCheckDto> findBookedOffice(Long roomId, LocalDate initDate, LocalDate endDate) {
        log.info("findBookedOffice : {}, {}, {}, {}", roomId, initDate, endDate);
        return queryFactory.select(new QReserveCheckDto(
                        reservation.room.roomId,
                        reservation.bookDate.checkInDate,
                        reservation.bookDate.checkOutDate,
                        reservation.bookDate.checkInTime,
                        reservation.bookDate.checkOutTime
                ))
                .from(reservation)
                .where(reservation.room.roomType.eq(RoomType.OFFICE)
                        .and(reservation.room.roomId.eq(roomId))
                        .and(reservation.bookDate.checkInDate.between(initDate, endDate)
                                .or(reservation.bookDate.checkOutDate.between(initDate, endDate))))
                .fetch();
    }

    @Override
    public List<ReserveCheckDto> findBookedRoom(Long roomId, LocalDate initDate) {
        log.info("findBookedOffice : {}, {}", roomId, initDate);
        return queryFactory.select(new QReserveCheckDto(
                        reservation.room.roomId,
                        reservation.bookDate.checkInDate,
                        reservation.bookDate.checkOutDate,
                        reservation.bookDate.checkInTime,
                        reservation.bookDate.checkOutTime
                ))
                .from(reservation)
                .where(reservation.bookDate.checkInDate.eq(initDate)
                        .and(reservation.room.roomId.eq(roomId))
                        .and(reservation.room.roomType.ne(RoomType.OFFICE))
                        .and(reservation.reserveStatus.eq(ReserveStatus.BOOKED)))
                .fetch();
    }

    @Override
    public List<ReserveCheckDto> checkBookedRoom(Long roomId, RoomType roomType, LocalDate initDate, Integer checkInTime, Integer checkOutTime) {
        log.info("checkBookedRoom : {}, {}, {}, {}, {}", roomId, roomType, initDate, checkInTime, checkOutTime);
        return queryFactory.select(new QReserveCheckDto(
                        reservation.room.roomId,
                        reservation.bookDate.checkInDate,
                        reservation.bookDate.checkOutDate,
                        reservation.bookDate.checkInTime,
                        reservation.bookDate.checkOutTime
                ))
                .from(reservation)
                .where(reservation.bookDate.checkInDate.eq(initDate)
                        .and(reservation.room.roomId.eq(roomId))
                        .and(reservation.room.roomType.eq(roomType))
                        .and(reservation.reserveStatus.eq(ReserveStatus.BOOKED))
                        .and(reservation.bookDate.checkInTime.goe(checkInTime).and(reservation.bookDate.checkInTime.lt(checkOutTime))
                                .or(reservation.bookDate.checkInTime.loe(checkInTime)
                                        .and(reservation.bookDate.checkOutTime.goe(checkOutTime)))
                                .or(reservation.bookDate.checkInTime.loe(checkInTime)
                                        .and(reservation.bookDate.checkOutTime.gt(checkInTime).and(reservation.bookDate.checkOutTime.loe(checkOutTime))))
                                .or(reservation.bookDate.checkInTime.loe(checkInTime)
                                        .and(reservation.bookDate.checkOutTime.goe(checkOutTime))))
                )
                .fetch();
    }

    @Override
    public Optional<ReserveSimpleDto> findRoomName(Long reserveId) {
        log.info("findRoomName : {}", reserveId);
        ReserveSimpleDto result = queryFactory.select(new QReserveSimpleDto(reservation.room.roomName, reservation.bookDate))
                .from(reservation)
                .where(reservation.reserveId.eq(reserveId))
                .fetchOne();
        return Optional.ofNullable(result);
    }

    // 예약의 결제 여부를 나타내는 BooleanExpression
    private BooleanExpression eqPaid(Integer paid) {
        if (paid == null) {
            return null;
        }
        return reservation.paid.eq(paid);
    }

    private BooleanExpression eqPayStatus(PaymentStatus paymentStatus) {
        if (paymentStatus == null) {
            return null;
        }
        return reservation.paymentStatus.eq(paymentStatus);
    }

    private BooleanExpression eqReserveStatus(ReserveStatus status) {
        if (status == null) {
            return null;
        }
        return reservation.reserveStatus.eq(status);
    }

    private BooleanExpression eqSpaceType(Integer spaceType) {
        if (spaceType == null) {
            return null;
        }
        return reservation.room.space.spaceType.eq(spaceType);
    }

}
