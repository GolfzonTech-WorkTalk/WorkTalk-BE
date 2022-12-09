package com.golfzonTech4.worktalk.repository.space;

import com.golfzonTech4.worktalk.domain.QSpace;
import com.golfzonTech4.worktalk.dto.space.QSpaceMainDto;
import com.golfzonTech4.worktalk.dto.space.SpaceMainDto;
import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.jpa.impl.JPAQueryFactory;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;

import javax.persistence.EntityManager;
import java.util.List;

import static com.golfzonTech4.worktalk.domain.QSpace.space;

@Slf4j
public class SpaceRepositoryCustomImpl implements SpaceRepositoryCustom{

    private JPAQueryFactory queryFactory; // 동적 쿼리 생성 위한 클래스

    // JPAQueryFactory 생성자로 EntityManager 넣어줌
    public SpaceRepositoryCustomImpl(EntityManager em){
        this.queryFactory = new JPAQueryFactory(em);
    }

    @Override
    public PageImpl<SpaceMainDto> getMainSpacePage(PageRequest pageRequest, Integer spaceType, String spaceName, String address) {

        QSpace space = QSpace.space;
//        QSpaceImg spaceImg = QSpaceImg.spaceImg;

        List<SpaceMainDto> content = queryFactory
                .select(
                        new QSpaceMainDto(
                                space.spaceId,
                                space.spaceName,
                                space.address,
                                space.spaceType
                                /*space.spaceImg*/)
                )
                .from(space)
                .where(eqSpaceType(spaceType), containName(spaceName), containAddress(address), space.spaceStatus.eq("approved"))
                .orderBy(space.spaceId.desc())
                .offset(pageRequest.getOffset())
                .limit(pageRequest.getPageSize())
                .fetch();

        long total = queryFactory
                .select(space.count())
                .from(space)
                .where(eqSpaceType(spaceType), containName(spaceName), containAddress(address), space.spaceStatus.eq("approved"))
                .fetchOne();

        return new PageImpl<>(content, pageRequest, total);
    }

    private BooleanExpression eqSpaceType(Integer spaceType) {
        if(spaceType == null || spaceType == 0) {
            return null;
        }
        return space.spaceType.eq(spaceType);
    }

    private BooleanExpression containName(String spaceName) {
        if(spaceName == null || spaceName.isEmpty()) {
            return null;
        }
        return space.spaceName.containsIgnoreCase(spaceName);
    }

    private BooleanExpression containAddress(String address) {
        if(address == null || address.isEmpty()) {
            return null;
        }
        return space.address.containsIgnoreCase(address);
    }


}
