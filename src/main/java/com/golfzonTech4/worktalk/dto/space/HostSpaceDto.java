package com.golfzonTech4.worktalk.dto.space;

import com.querydsl.core.annotations.QueryProjection;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@ToString
public class HostSpaceDto {

    private String spaceName;

    @QueryProjection
    public HostSpaceDto(String spaceName) {
        this.spaceName = spaceName;
    }
}
