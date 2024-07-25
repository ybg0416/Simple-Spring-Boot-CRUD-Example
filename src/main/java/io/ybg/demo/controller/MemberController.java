package io.ybg.demo.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.ybg.demo.entity.Member;
import io.ybg.demo.service.MemberService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.net.URI;
import java.util.List;

@RestController
@RequestMapping("/Member/v1")
@RequiredArgsConstructor
@Validated
@Slf4j
public class MemberController {

    private final MemberService memberService;

    @Operation(summary = "get All Members", description = "get All Members")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "모든 멤버 반환"),
    })
    @GetMapping("/")
    public ResponseEntity<List<Member>> getAllMembers() {
        return ResponseEntity.ok().body(memberService.getAllMembers());
    }

    @Operation(summary = "get Member By ID", description = "get Member By ID")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "ID 해당하는 멤버 반환"),
            @ApiResponse(responseCode = "404", description = "존재하지 않는 멤버", content = @Content),
    })
    @GetMapping("/{id}")
    public ResponseEntity<Member> getMemberById(@Parameter(description = "Member ID") @PathVariable Integer id) {
        Member member = memberService.getMemberById(id);

        if (member == null) {
            return ResponseEntity.notFound().build();
        }

        return ResponseEntity.ok().body(memberService.getMemberById(id));
    }

    @Operation(summary = "email check", description = "email check")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Member Email 존재 여부 반환", content = @Content(schema = @Schema(implementation = boolean.class))),
    })
    @GetMapping("/email/check/{email}")
    public ResponseEntity<Member> checkEmail(@Parameter(description = "Member Email") @PathVariable String email) {
        return memberService.isExistingEmail(email) ? ResponseEntity.status(HttpStatus.CONFLICT).build() : ResponseEntity.ok().build();
    }

    @Operation(summary = "add Member", description = "add Member")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "가입 Member 정보 반환"),
            @ApiResponse(responseCode = "409", description = "가입 실패", content = @Content)
    })
    @PostMapping("/")
    public ResponseEntity<Member> saveMember(@Parameter(description = "Member Info") @RequestBody Member member) {
        try {
            memberService.saveMember(member);
        } catch (DataIntegrityViolationException e) {
            log.warn("Error saving Member: {} , {}", e.getMessage(), member);
            return ResponseEntity.status(HttpStatus.CONFLICT).build();
        }
        return ResponseEntity.created(URI.create("/Members/" + member.getId())).build();
    }

    @Operation(summary = "update Member", description = "update Member")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "수정 Member 정보 반환"),
            @ApiResponse(responseCode = "409", description = "수정 실패", content = @Content)
    })
    @PutMapping("/")
    public ResponseEntity<Member> updateMember(@Parameter(description = "Member Info") @RequestBody @Valid Member member) {
        try {
            memberService.updateMember(member);
        } catch (RuntimeException e) {
            log.warn("Error updating Member: {}, {}", e.getMessage(), member);
            return ResponseEntity.status(HttpStatus.CONFLICT).build();
        }
        return ResponseEntity.ok().body(member);
    }

    @Operation(summary = "delete Member", description = "delete Member")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Member 삭제 성공", content = @Content),
            @ApiResponse(responseCode = "500", description = "삭제 실패", content = @Content)
    })
    @DeleteMapping("/{id}")
    public ResponseEntity<String> deleteMemberById(@Parameter(description = "Member ID") @PathVariable Integer id) {
        try {
            memberService.deleteMemberById(id);
        } catch (Exception e) {
            log.warn("Error deleting Member: {}, {}", e.getMessage(), id);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
        return ResponseEntity.ok().build();
    }
}