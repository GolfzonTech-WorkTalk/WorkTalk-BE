package com.golfzonTech4.worktalk.dto.qna;

import com.golfzonTech4.worktalk.domain.QnaType;
import com.querydsl.core.annotations.QueryProjection;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
public class QnaDetailDto {
    private Long qnaId;

    private Long spaceId;

    private Long memberId;

    private QnaType type;

    private String content;

    private LocalDateTime lastModifiedDate;

    private Long qnaCommentId;

    private String qnacomment;

    private LocalDateTime qclastModifiedDate;

    private String spaceName;

    private String writer;

    public QnaDetailDto(Long qnaId, Long spaceId, Long memberId, QnaType type, String content, LocalDateTime lastModifiedDate,
                        Long qnaCommentId, String qnacomment, LocalDateTime qclastModifiedDate) {
        this.qnaId = qnaId;
        this.spaceId = spaceId;
        this.memberId = memberId;
        this.type = type;
        this.content = content;
        this.lastModifiedDate = lastModifiedDate;
        this.qnaCommentId = qnaCommentId;
        this.qnacomment = qnacomment;
        this.qclastModifiedDate = qclastModifiedDate;
    }

    @QueryProjection
    public QnaDetailDto(Long qnaId, Long spaceId, Long memberId, QnaType type, String content, LocalDateTime lastModifiedDate,
                        Long qnaCommentId, String qnacomment, LocalDateTime qclastModifiedDate, String spaceName) {
        this.qnaId = qnaId;
        this.spaceId = spaceId;
        this.memberId = memberId;
        this.type = type;
        this.content = content;
        this.lastModifiedDate = lastModifiedDate;
        this.qnacomment = qnacomment;
        this.qnaCommentId = qnaCommentId;
        this.qclastModifiedDate = qclastModifiedDate;
        this.spaceName = spaceName;
    }

    @QueryProjection
    public QnaDetailDto(Long qnaId, Long spaceId, Long memberId, QnaType type, String content, LocalDateTime lastModifiedDate,
                        Long qnaCommentId, String qnacomment, LocalDateTime qclastModifiedDate, String spaceName, String writer) {
        this.qnaId = qnaId;
        this.spaceId = spaceId;
        this.memberId = memberId;
        this.type = type;
        this.content = content;
        this.lastModifiedDate = lastModifiedDate;
        this.qnaCommentId = qnaCommentId;
        this.qnacomment = qnacomment;
        this.qclastModifiedDate = qclastModifiedDate;
        this.spaceName = spaceName;
        this.writer = writer;
    }
}
