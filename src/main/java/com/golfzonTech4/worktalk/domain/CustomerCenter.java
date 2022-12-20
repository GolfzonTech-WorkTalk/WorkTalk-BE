package com.golfzonTech4.worktalk.domain;

import lombok.Getter;
import lombok.Setter;

import javax.persistence.*;
import java.io.Serializable;
import java.util.List;

@Entity
@Getter
@Setter
@SequenceGenerator(name = "SEQ_CUSTOMER_CENTER_GENERATOR", sequenceName = "SEQ_CUSTOMER_CENTER", initialValue = 1, allocationSize = 50)
@Table(name = "CUSTOMER_CENTER")
public class CustomerCenter extends BaseTimeEntity implements Serializable {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "SEQ_CUSTOMER_CENTER_GENERATOR")
    @Column(name = "CC_ID")
    private Long ccId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "MEMBER_ID", nullable = false)
    private Member member;

    @Column(name = "TITLE", nullable = true, length = 50)
    private String title;

    @Column(name = "CONTENT", nullable = true, length = 2000)
    private String content;

    @Enumerated(EnumType.STRING)
    private CcType type;

    @OneToMany(mappedBy = "customerCenter", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<CustomerComment> customerComments;

}
