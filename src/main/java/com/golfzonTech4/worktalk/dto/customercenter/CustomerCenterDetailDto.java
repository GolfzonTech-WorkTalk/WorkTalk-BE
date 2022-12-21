package com.golfzonTech4.worktalk.dto.customercenter;

import com.golfzonTech4.worktalk.domain.CcType;
import com.querydsl.core.annotations.QueryProjection;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
public class CustomerCenterDetailDto {

    private Long ccId;

    private Long memberId;

    private String writer;

    private String title;

    private String content;

    private CcType type;

    private LocalDateTime lastModifiedDate;

    private Long ccCommentId;

    private String ccContent;

    private LocalDateTime ccLastModifiedDate;

    @QueryProjection
    public CustomerCenterDetailDto(Long ccId, Long memberId, String writer, String title, String content, CcType type, LocalDateTime lastModifiedDate, Long ccCommentId, String ccContent, LocalDateTime ccLastModifiedDate) {
        this.ccId = ccId;
        this.memberId = memberId;
        this.writer = writer;
        this.title = title;
        this.content = content;
        this.type = type;
        this.lastModifiedDate = lastModifiedDate;
        this.ccCommentId = ccCommentId;
        this.ccContent = ccContent;
        this.ccLastModifiedDate = ccLastModifiedDate;
    }

}
