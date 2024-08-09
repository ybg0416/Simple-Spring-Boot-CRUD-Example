package io.ybg.demo.entity;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.persistence.*;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;
import org.hibernate.annotations.UuidGenerator;
import org.hibernate.validator.constraints.Length;

import java.time.LocalDateTime;
import java.util.UUID;

@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Builder
@Data
@Entity
@Table(indexes = @Index(name = "member_uuid", columnList = "uuid"))
public class MemberEntity {

    @Id
    @GeneratedValue
    @NotNull
    @Schema(description = "사용자 PK", example = "1")
    private Long id;

    @UuidGenerator  // spring boot 3 기준 uuid4
    @Column(columnDefinition = "uuid")
    @Schema(description = "사용자 UUID", example = "1")
    private UUID uuid;

    @NotEmpty
    @Email
    @NotNull(message = "Email cannot be null")
    @Length(min = 2, max = 64, message = "Email not be less than 2 characters")
    @Schema(description = "사용자 이메일", example = "test@naver.com")
    @Column(length = 64, unique = true)
    private String email;

    @Length(min = 2, max = 32, message = "Name not be less than 2 characters")
    @Schema(description = "사용자 이름", example = "홍길동")
    private String name;

    @Length(min = 11, max = 13, message = "Phone not be less than 11 characters")
    @Schema(description = "사용자 전화번호", example = "010-1234-5678")
    private String phone;

    @CreationTimestamp
    private LocalDateTime reg_dt;
    @UpdateTimestamp
    private LocalDateTime mod_dt;

}