package com.golfzonTech4.worktalk.dto.room;

import com.golfzonTech4.worktalk.domain.RoomType;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.web.multipart.MultipartFile;

import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;
import java.util.List;

@Getter @Setter @NoArgsConstructor
public class RoomInsertDto {

    private Long roomId;

    private Long spaceId;

    private RoomType roomType;

    @NotEmpty(message = "공간명을 입력해주세요")
    private String roomName;

    private String roomDetail;

    @NotNull(message = "가격을 입력해주세요")
    private int roomPrice;

    @NotNull(message = "시작시간을 입력해주세요")
    private int workStart;

    @NotNull(message = "종료시간을 입력해주세요")
    private int workEnd;

    List<MultipartFile> multipartFileList;

    private String offeringOption;

}
