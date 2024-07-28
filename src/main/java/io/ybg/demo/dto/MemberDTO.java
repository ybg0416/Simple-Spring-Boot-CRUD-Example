package io.ybg.demo.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.persistence.Column;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;
import org.hibernate.annotations.UuidGenerator;
import org.hibernate.validator.constraints.Length;
import org.mapstruct.Mapper;
import org.mapstruct.factory.Mappers;

import java.time.LocalDateTime;
import java.util.UUID;

public class MemberDTO {

//    @NotNull
//    @Schema(description = "사용자 PK", example = "1")
//    private Integer id;
//
//    @Schema(description = "사용자 UUID", example = "1")
//    private UUID uuid;
//
//    @NotEmpty(message = "Email cannot be null")
//    @Email
//    @Length(min = 2, max = 64, message = "Email not be less than 2 characters")
//    @Schema(description = "사용자 이메일", nullable = false, example = "test@naver.com")
//    private String email;
//
//    @Length(min = 2, max = 32, message = "Name not be less than 2 characters")
//    @Schema(description = "사용자 이름", example = "홍길동")
//    private String name;
//
//    @Length(min = 11, max = 13, message = "Phone not be less than 11 characters")
//    @Schema(description = "사용자 전화번호", example = "010-1234-5678")
//    private String phone;

    @Getter
    @AllArgsConstructor
    @Builder
    public static class Update {
        @NotEmpty(message = "Email cannot be null")
        @Email
        @Length(min = 2, max = 64, message = "Email not be less than 2 characters")
        @Schema(description = "사용자 이메일", nullable = false, example = "test@naver.com")
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
    public static class Create {
        @NotEmpty(message = "Email cannot be null")
        @Email
        @Length(min = 2, max = 64, message = "Email not be less than 2 characters")
        @Schema(description = "사용자 이메일", nullable = false, example = "test@naver.com")
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
    public static class Info {
        @Schema(description = "사용자 PK", example = "1")
        private Integer id;

        @Schema(description = "사용자 UUID", example = "1")
        private UUID uuid;

        @NotEmpty(message = "Email cannot be null")
        @Email
        @Length(min = 2, max = 64, message = "Email not be less than 2 characters")
        @Schema(description = "사용자 이메일", nullable = false, example = "test@naver.com")
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


