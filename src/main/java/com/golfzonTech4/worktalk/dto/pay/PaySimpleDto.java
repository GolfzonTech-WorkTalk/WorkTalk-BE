package com.golfzonTech4.worktalk.dto.pay;

import com.golfzonTech4.worktalk.domain.PaymentStatus;
import com.golfzonTech4.worktalk.domain.ReserveStatus;
import com.querydsl.core.annotations.QueryProjection;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
@NoArgsConstructor
public class PaySimpleDto {
    private Long reserveId; // 예약 번호
    private LocalDateTime reserveDate; // 예약 날짜
    private String name; // 예약자명
    private String spaceName; // 공간명
    private String roomName; // 세부 공간명
    private int payAmount; // 결제 금액
    private PaymentStatus payStatus; // 결제 유형
    private ReserveStatus reserveStatus; // 예약 상태
    private String tel; // 예약자 연락처
    private int reserveAmount; // 예약 총 금액

    public PaySimpleDto(LocalDateTime reserveDate, String spaceName, String roomName, int payAmount, PaymentStatus payStatus, ReserveStatus reserveStatus) {
        this.reserveDate = reserveDate;
        this.spaceName = spaceName;
        this.roomName = roomName;
        this.payAmount = payAmount;
        this.payStatus = payStatus;
        this.reserveStatus = reserveStatus;
    }

    public PaySimpleDto(LocalDateTime reserveDate, String spaceName, String roomName, int payAmount, PaymentStatus payStatus, ReserveStatus reserveStatus, String name, int reserveAmount) {
        this.reserveDate = reserveDate;
        this.spaceName = spaceName;
        this.roomName = roomName;
        this.payAmount = payAmount;
        this.payStatus = payStatus;
        this.reserveStatus = reserveStatus;
        this.name = name;
        this.reserveAmount = reserveAmount;
    }

    @QueryProjection
    public PaySimpleDto(Long reserveId, LocalDateTime reserveDate, String spaceName, String roomName, int payAmount, PaymentStatus payStatus, ReserveStatus reserveStatus, String name, String tel, int reserveAmount) {
        this.reserveId = reserveId;
        this.reserveDate = reserveDate;
        this.spaceName = spaceName;
        this.roomName = roomName;
        this.payAmount = payAmount;
        this.payStatus = payStatus;
        this.reserveStatus = reserveStatus;
        this.name = name;
        this.tel = tel;
        this.reserveAmount = reserveAmount;
    }

    @QueryProjection
    public PaySimpleDto(String roomName) {
        this.roomName = roomName;
    }
}


