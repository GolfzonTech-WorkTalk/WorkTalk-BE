package com.golfzonTech4.worktalk.repository.space;

import com.golfzonTech4.worktalk.domain.Space;
import com.golfzonTech4.worktalk.dto.space.HostSpaceDto;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.querydsl.QuerydslPredicateExecutor;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository

public interface SpaceRepository extends JpaRepository<Space, Long>,
        QuerydslPredicateExecutor<Space>, SpaceRepositoryCustom {

    Space findBySpaceId(Long spaceId); //사무공간 상세페이지

//    @Query("select s from Space s where s.spaceStatus like 'approved'")
//    List<Space> findAllBySpaceStatus(); //메인페이지 사무공간리스트

    List<Space> findAllByMemberId(Long memberId); // 호스트가 등록한 사무공간리스트
//    @Query("select new com.golfzonTech4.worktalk.dto.space.SpaceMainDto (s.spaceId, s.spaceName, s.address, s.spaceType) from Space s " +
//            "where s.member.id = :memberId")
//    List<SpaceMainDto> findAllByMemberId(Long memberId); // 호스트가 등록한 사무공간리스트

    @Query("select new com.golfzonTech4.worktalk.dto.space.HostSpaceDto (s.spaceName) from Space s " +
            "left join s.member m on s.member.id = m.id where m.name = :name")
    public List<HostSpaceDto> getSpacesByHost(@Param("name") String name);
}
