package com.golfzonTech4.worktalk.repository.space;

import com.golfzonTech4.worktalk.dto.space.*;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;

import java.util.List;

public interface SpaceRepositoryCustom {

    PageImpl<SpaceMainDto> getMainSpacePage(PageRequest pageRequest, SpaceSearchDto dto);

    List<SpaceMainDto> getHostSpacePage(String name);

    List<SpaceDetailDto> getSpaceDetailPage(Long spaceId); //사무공간 상세페이지

    PageImpl<SpaceMasterDto> getSpaceMasterPage(PageRequest pageRequest, SpaceManageSortingDto dto); //마스터의 사무공간 관리 페이지

}
