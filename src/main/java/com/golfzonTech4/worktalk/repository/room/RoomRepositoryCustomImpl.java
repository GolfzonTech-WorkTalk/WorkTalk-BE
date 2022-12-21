package com.golfzonTech4.worktalk.repository.room;

import com.golfzonTech4.worktalk.dto.room.QRoomDetailDto;
import com.golfzonTech4.worktalk.dto.room.QRoomImgDto;
import com.golfzonTech4.worktalk.dto.room.RoomDetailDto;
import com.golfzonTech4.worktalk.dto.room.RoomImgDto;
import com.querydsl.jpa.impl.JPAQueryFactory;
import lombok.extern.slf4j.Slf4j;

import javax.persistence.EntityManager;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import static com.golfzonTech4.worktalk.domain.QRoom.room;
import static com.golfzonTech4.worktalk.domain.QRoomImg.roomImg;

@Slf4j
public class RoomRepositoryCustomImpl implements RoomRepositoryCustom {

    private JPAQueryFactory queryFactory; // 동적 쿼리 생성 위한 클래스

    // JPAQueryFactory 생성자로 EntityManager 넣어줌
    public RoomRepositoryCustomImpl(EntityManager em) {
        this.queryFactory = new JPAQueryFactory(em);
    }

    @Override
    public List<RoomDetailDto> getRooms(Long spaceId) {
        List<RoomDetailDto> content = queryFactory.select(
                        new QRoomDetailDto(
                                room.roomId,
                                room.roomName,
                                room.roomDetail,
                                room.roomType,
                                room.roomPrice,
                                room.workStart,
                                room.workEnd,
                                room.offeringOption))
                .from(room)
                .where(room.space.spaceId.eq(spaceId))
                .orderBy(room.roomPrice.asc())
                .fetch();

        List<Long> roomIds = content.stream().map(RoomDetailDto::getRoomId).collect(Collectors.toList());

        List<RoomImgDto> images = queryFactory.select(new QRoomImgDto(roomImg.roomImgId, roomImg.roomImgUrl, roomImg.room.roomId))
                .from(roomImg)
                .where(roomImg.room.roomId.in(roomIds))
                .fetch();

        Map<Long, List<RoomImgDto>> imgIdsMap = images.stream().collect(Collectors.groupingBy(RoomImgDto::getRoomId));

        content.forEach(r -> r.setRoomImgDtoList(imgIdsMap.get(r.getRoomId())));


        return content;
    }

    @Override
    public List<RoomDetailDto> getRoomDetailPage(Long roomId) {
        List<RoomDetailDto> content = queryFactory.select(
                        new QRoomDetailDto(
                                room.roomId,
                                room.roomName,
                                room.roomDetail,
                                room.roomType,
                                room.roomPrice,
                                room.workStart,
                                room.workEnd,
                                room.offeringOption))
                .from(room)
                .where(room.roomId.eq(roomId))
                .orderBy(room.roomPrice.asc())
                .fetch();

        List<Long> roomIds = content.stream().map(RoomDetailDto::getRoomId).collect(Collectors.toList());

        List<RoomImgDto> images = queryFactory.select(new QRoomImgDto(roomImg.roomImgId, roomImg.roomImgUrl, roomImg.room.roomId))
                .from(roomImg)
                .where(roomImg.room.roomId.in(roomIds))
                .fetch();

        Map<Long, List<RoomImgDto>> imgIdsMap = images.stream().collect(Collectors.groupingBy(RoomImgDto::getRoomId));

        content.forEach(r -> r.setRoomImgDtoList(imgIdsMap.get(r.getRoomId())));


        return content;
    }

}