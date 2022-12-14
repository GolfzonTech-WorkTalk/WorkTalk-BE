package com.golfzonTech4.worktalk.dto.reservation;

import com.golfzonTech4.worktalk.domain.RoomType;
import com.querydsl.core.annotations.QueryProjection;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;

import java.time.LocalDate;

@Data
@NoArgsConstructor
public class ReserveCheckDto {

    private Long roomId;
    private RoomType roomType;
    //    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd", timezone = "Asia/Seoul")
    @DateTimeFormat(pattern = "yyyy-MM-dd")
    private LocalDate initDate;
    //    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd", timezone = "Asia/Seoul")
    @DateTimeFormat(pattern = "yyyy-MM-dd")
    private LocalDate endDate;
    private Integer initTime;
    private Integer endTime;
    private Long spaceType;

    @QueryProjection
    public ReserveCheckDto(Long roomId, LocalDate initDate, LocalDate endDate, Integer initTime, Integer endTime) {
        this.roomId = roomId;
        this.initDate = initDate;
        this.endDate = endDate;
        this.initTime = initTime;
        this.endTime = endTime;
    }
}
