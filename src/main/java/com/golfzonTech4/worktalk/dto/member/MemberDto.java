package com.golfzonTech4.worktalk.dto.member;

import com.golfzonTech4.worktalk.domain.MemberType;
import com.querydsl.core.annotations.QueryProjection;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.ColumnDefault;

import javax.persistence.*;

@Data
@NoArgsConstructor
@Builder
public class MemberDto {

    private Long id;
    private String email;
    private String name;
    private String tel;
    private MemberType memberType;
    private int activated;

    private String kakao;

    @QueryProjection
    public MemberDto(Long id, String email, String name, String tel, MemberType memberType, int activated, String kakao) {
        this.id = id;
        this.email = email;
        this.name = name;
        this.tel = tel;
        this.memberType = memberType;
        this.activated = activated;
        this.kakao = kakao;
    }
}
