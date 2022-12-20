package com.golfzonTech4.worktalk.domain;

import lombok.Getter;
import lombok.Setter;

import javax.persistence.*;
import java.io.Serializable;
import java.util.List;

@Entity
@Setter
@Getter
@SequenceGenerator(name = "SEQ_ROOM_GENERATOR", sequenceName = "SEQ_ROOM", initialValue = 1, allocationSize = 50)
public class Room implements Serializable {
    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "SEQ_ROOM_GENERATOR")
    @Column(name = "ROOM_ID")
    private Long roomId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "SPACE_ID", nullable = false)
    private Space space;

    @Enumerated(EnumType.STRING)
    private RoomType roomType;

    @Column(name = "ROOM_NAME", nullable = false, length = 50)
    private String roomName;

    @Column(name = "ROOM_DETAIL", nullable = true, length = 500)
    private String roomDetail;

    @Column(name = "ROOM_PRICE", nullable = false)
    private int roomPrice;

    @Column(name = "WORK_START", nullable = false)
    private int workStart;

    @Column(name = "WORK_END", nullable = false)
    private int workEnd;

    @Column(name = "OFFERING_OPTION", nullable = true, length = 500)
    private String offeringOption;

    @OneToMany(mappedBy = "room", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<RoomImg> roomImgList;

}
