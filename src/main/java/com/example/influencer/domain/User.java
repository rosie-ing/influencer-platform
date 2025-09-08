package com.example.influencer.domain;

import jakarta.persistence.*;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.*;

import java.util.HashSet;
import java.util.Set;

@Entity
@Table(name = "users",
       indexes = {@Index(name = "idx_users_email", columnList = "email", unique = true)
})
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class User {

    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    //이메 형식 검증 (Bean Validation)
    @Email
    @NotBlank
    @Column(nullable = false, length = 120)
    private String email;

    @NotBlank
    @Column(nullable = false)
    private  String password;

    //선택
    private String nickname;
    private String gender;
    private Integer age;

    //권한 지정
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private  Role role;

    //다대다 interest/ user
    @ManyToMany
    @JoinTable(
            name = "user_interests",
            joinColumns = @JoinColumn(name = "user_id"),
            inverseJoinColumns = @JoinColumn(name = "interest_id")
    )


    @Builder.Default
    //중복 방지
    private Set<Interest> interests = new HashSet<>();



}
