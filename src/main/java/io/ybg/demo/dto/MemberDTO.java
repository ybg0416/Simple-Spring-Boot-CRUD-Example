package io.ybg.demo.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotEmpty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import org.hibernate.validator.constraints.Length;

import java.time.LocalDateTime;
import java.util.UUID;

public class MemberDTO {
    @Getter
    @AllArgsConstructor
    @Builder
    public static class UpdateMemberDTO {
        @NotEmpty(message = "Email cannot be null")
        @Email
        @Length(min = 2, max = 64, message = "Email not be less than 2 characters")
        @Schema(description = "사용자 이메일", example = "test@naver.com")
        private String email;

        @Length(min = 2, max = 32, message = "Name not be less than 2 characters")
        @Schema(description = "사용자 이름", example = "홍길동")
        private String name;

        @Length(min = 11, max = 13, message = "Phone not be less than 11 characters")
        @Schema(description = "사용자 전화번호", example = "010-1234-5678")
        private String phone;
    }

    @Getter
    @AllArgsConstructor
    @Builder
    public static class CreateMemberDTO {
        @NotEmpty(message = "Email cannot be null")
        @Email
        @Length(min = 2, max = 64, message = "Email not be less than 2 characters")
        @Schema(description = "사용자 이메일", example = "test@naver.com")
        private String email;

        @Length(min = 2, max = 32, message = "Name not be less than 2 characters")
        @Schema(description = "사용자 이름", example = "홍길동")
        private String name;

        @Length(min = 11, max = 13, message = "Phone not be less than 11 characters")
        @Schema(description = "사용자 전화번호", example = "010-1234-5678")
        private String phone;
    }

    @Getter
    @AllArgsConstructor
    @Builder
    public static class InfoMemberDTO {
        @Schema(description = "사용자 PK", example = "1")
        private long id;

        @Schema(description = "사용자 UUID", example = "1")
        private UUID uuid;

        @NotEmpty(message = "Email cannot be null")
        @Email
        @Length(min = 2, max = 64, message = "Email not be less than 2 characters")
        @Schema(description = "사용자 이메일", example = "test@naver.com")
        private String email;

        @Length(min = 2, max = 32, message = "Name not be less than 2 characters")
        @Schema(description = "사용자 이름", example = "홍길동")
        private String name;

        @Length(min = 11, max = 13, message = "Phone not be less than 11 characters")
        @Schema(description = "사용자 전화번호", example = "010-1234-5678")
        private String phone;

        private LocalDateTime reg_dt;
        private LocalDateTime mod_dt;
    }
}


