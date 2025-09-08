package com.example.influencer.domain;


import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.*;

//이 클래스를 JPA 엔티티로 등록
@Entity
//테이블 명(interests) 지정, name 컬럼에 유니크 제약(중복금지) 생성
@Table(
        name = "interests",
        uniqueConstraints = @UniqueConstraint(columnNames = "name"))
@Getter @NoArgsConstructor
public class Interest {
    //기본 키. MySQL의 AUTO_INCREMENT 방식 사용해 DB가 값을 채움
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    //name은 NOT NULL, 길이 80자로 스키마 생성
    @Column(nullable = false, length = 80)
    @NotBlank
    @Size(max = 80)
    private String name;

    @Builder
    private Interest(String name){
        validateName(name);
        this.name = name;
    }

    public void rename(String newName){
        validateName(newName);
        this.name = newName;
    }
    //검증 로직
    private static void validateName(String value){
        if(value == null || value.isBlank()) {
            throw new IllegalArgumentException("name은 비어 있을 수 없습니다.");
        }
        if(value.length() > 80){
            throw new IllegalArgumentException("name은 80자를 넘을 수 없습니다.");
        }
    }
}
