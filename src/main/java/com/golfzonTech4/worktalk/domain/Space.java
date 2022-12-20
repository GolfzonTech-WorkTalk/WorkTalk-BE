package com.golfzonTech4.worktalk.domain;

import com.fasterxml.jackson.annotation.JsonManagedReference;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.ColumnDefault;
import org.hibernate.annotations.DynamicInsert;
import org.hibernate.annotations.DynamicUpdate;

import javax.persistence.*;
import java.io.Serializable;
import java.util.List;

@Entity
@Getter @Setter
@DynamicInsert @DynamicUpdate
@SequenceGenerator(name = "SEQ_SPACE_GENERATOR", sequenceName = "SEQ_SPACE", initialValue = 1, allocationSize = 50)
public class Space implements Serializable {
    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "SEQ_SPACE_GENERATOR")
    @Column(name = "SPACE_ID")
    private Long spaceId;

    @ManyToOne
    @JsonManagedReference //순환참조를 방지하기 위한 어노테이션
    @JoinColumn(name = "MEMBER_ID", nullable = false)
    private Member member;

    @Column(name = "SPACE_NAME", nullable = false, length = 50)
    private String spaceName;

    @Column(name = "SPACE_DETAIL", nullable = true, length = 1000)
    private String spaceDetail;

    @Column(name = "POSTCODE", nullable = false, length = 20)
    private String postcode;

    @Column(name = "ADDRESS", nullable = false, length = 100)
    private String address;

    @Column(name = "DETAIL_ADDRESS", nullable = false, length = 100)
    private String detailAddress;

    @Column(name = "REG_CODE", nullable = false, length = 50)
    private String regCode;

    @Column(name = "SPACE_TYPE", nullable = false)
    private int spaceType;

    @ColumnDefault(value = "'no_setting'")
    @Column(name = "SPACE_STATUS", length = 20)
    private String spaceStatus;

    @OneToMany(mappedBy = "space", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Room> rooms;

    @OneToMany(mappedBy = "space", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<SpaceImg> spaceImgList;

    @OneToMany(mappedBy = "space", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Qna> qnas;

}
