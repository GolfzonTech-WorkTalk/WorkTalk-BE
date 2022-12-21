package com.golfzonTech4.worktalk.repository.qna;

import com.golfzonTech4.worktalk.domain.QnaType;
import com.golfzonTech4.worktalk.dto.qna.QQnaDetailDto;
import com.golfzonTech4.worktalk.dto.qna.QnaDetailDto;
import com.golfzonTech4.worktalk.dto.qna.QnaSearchDto;
import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.jpa.impl.JPAQueryFactory;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;

import javax.persistence.EntityManager;
import java.util.List;

import static com.golfzonTech4.worktalk.domain.QMember.member;
import static com.golfzonTech4.worktalk.domain.QQna.qna;
import static com.golfzonTech4.worktalk.domain.QQnaComment.qnaComment;
import static com.golfzonTech4.worktalk.domain.QSpace.space;


@Slf4j
public class QnaRepositoryCustomImpl implements QnaRepositoryCustom {

    private JPAQueryFactory queryFactory; // 동적 쿼리 생성 위한 클래스

    // JPAQueryFactory 생성자로 EntityManager 넣어줌
    public QnaRepositoryCustomImpl(EntityManager em) {
        this.queryFactory = new JPAQueryFactory(em);
    }


    @Override
    public PageImpl<QnaDetailDto> findQnaDtoListbyHostSpace(PageRequest pageRequest, QnaSearchDto dto) {

        List<QnaDetailDto> content = queryFactory
                .select(
                        new QQnaDetailDto(
                                qna.qnaId,
                                qna.space.spaceId,
                                qna.member.id,
                                qna.type,
                                qna.content,
                                qna.lastModifiedDate,
                                qnaComment.qnaCommentId,
                                qnaComment.qnacomment,
                                qnaComment.lastModifiedDate,
                                space.spaceName,
                                qna.member.name)
                ).distinct()
                .from(qna)
                .leftJoin(qnaComment).on(qna.qnaId.eq(qnaComment.qna.qnaId))
                .leftJoin(space).on(qna.space.spaceId.eq(space.spaceId))
                .leftJoin(member).on(space.member.id.eq(member.id))
                .where(member.name.eq(dto.getSearchHost()), eqQnaType(dto.getSearchQnaType()), eqSapceName(dto.getSearchSpaceName()))
                .orderBy(qna.qnaId.desc())
                .offset(pageRequest.getOffset())
                .limit(pageRequest.getPageSize())
                .fetch();

        long total = queryFactory
                .select(qna.countDistinct())
                .from(qna)
                .leftJoin(qnaComment).on(qna.qnaId.eq(qnaComment.qna.qnaId))
                .leftJoin(space).on(qna.space.spaceId.eq(space.spaceId))
                .leftJoin(member).on(space.member.id.eq(member.id))
                .where(member.name.eq(dto.getSearchHost()), eqQnaType(dto.getSearchQnaType()), eqSapceName(dto.getSearchSpaceName()))
                .fetchOne();

        return new PageImpl<>(content, pageRequest, total);
    }

    @Override
    public PageImpl<QnaDetailDto> findQnaDtoListByMember(PageRequest pageRequest, QnaSearchDto dto) {
        List<QnaDetailDto> content = queryFactory
                .select(
                        new QQnaDetailDto(
                                qna.qnaId,
                                qna.space.spaceId,
                                qna.member.id,
                                qna.type,
                                qna.content,
                                qna.lastModifiedDate,
                                qnaComment.qnaCommentId,
                                qnaComment.qnacomment,
                                qnaComment.lastModifiedDate,
                                qna.space.spaceName)
                ).distinct()
                .from(qna)
                .leftJoin(qnaComment).on(qna.qnaId.eq(qnaComment.qna.qnaId))
                .leftJoin(member).on(qna.member.id.eq(member.id))
                .where(member.name.eq(dto.getSearchUser()), eqQnaType(dto.getSearchQnaType()))
                .orderBy(qna.qnaId.desc())
                .offset(pageRequest.getOffset())
                .limit(pageRequest.getPageSize())
                .fetch();

        long total = queryFactory
                .select(qna.countDistinct())
                .from(qna)
                .leftJoin(qnaComment).on(qna.qnaId.eq(qnaComment.qna.qnaId))
                .leftJoin(member).on(qna.member.id.eq(member.id))
                .where(member.name.eq(dto.getSearchUser()), eqQnaType(dto.getSearchQnaType()))
                .fetchOne();

        return new PageImpl<>(content, pageRequest, total);
    }

    @Override
    public PageImpl<QnaDetailDto> findQnaDtoListBySpaceId(PageRequest pageRequest, Long spaceId) {
        List<QnaDetailDto> content = queryFactory
                .select(
                        new QQnaDetailDto(
                                qna.qnaId,
                                qna.space.spaceId,
                                qna.member.id,
                                qna.type,
                                qna.content,
                                qna.lastModifiedDate,
                                qnaComment.qnaCommentId,
                                qnaComment.qnacomment,
                                qnaComment.lastModifiedDate,
                                space.spaceName,
                                qna.member.name)
                ).distinct()
                .from(qna)
                .leftJoin(qnaComment).on(qna.qnaId.eq(qnaComment.qna.qnaId))
                .leftJoin(space).on(qna.space.spaceId.eq(space.spaceId))
                .where(space.spaceId.eq(spaceId))
                .orderBy(qna.qnaId.desc())
                .offset(pageRequest.getOffset())
                .limit(pageRequest.getPageSize())
                .fetch();

        long total = queryFactory
                .select(qna.countDistinct())
                .from(qna)
                .leftJoin(qnaComment).on(qna.qnaId.eq(qnaComment.qna.qnaId))
                .leftJoin(space).on(qna.space.spaceId.eq(space.spaceId))
                .where(space.spaceId.eq(spaceId))
                .fetchOne();

        return new PageImpl<>(content, pageRequest, total);
    }

    private BooleanExpression eqQnaType(QnaType searchQnaType) {
        if (searchQnaType == null) {
            return null;
        }
        return qna.type.eq(searchQnaType);
    }

    private BooleanExpression eqSapceName(String searchSpaceName) {
        if (searchSpaceName == null || searchSpaceName.isEmpty()) {
            return null;
        }
        return space.spaceName.eq(searchSpaceName);
    }

}
