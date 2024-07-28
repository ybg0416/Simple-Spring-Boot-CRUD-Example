package io.ybg.demo.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.ybg.demo.dto.MemberDTO;
import io.ybg.demo.entity.Member;
import io.ybg.demo.mapper.MemberMapper;
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
@RequestMapping("/member/v1")
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
    public ResponseEntity<List<MemberDTO.Info>> getAllMembers() {
        return ResponseEntity.ok().body(MemberMapper.INSTANCE.MemeberToInfo(memberService.getAllMembers()));
    }

    @Operation(summary = "get Member By ID", description = "get Member By ID")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "ID 해당하는 멤버 반환"),
            @ApiResponse(responseCode = "404", description = "존재하지 않는 멤버", content = @Content),
    })
    @GetMapping("/{id}")
    public ResponseEntity<MemberDTO.Info> getMemberById(@Parameter(description = "Member ID") @PathVariable Integer id) {
        Member member = memberService.getMemberById(id);

        if (member == null) {
            return ResponseEntity.notFound().build();
        }

        return ResponseEntity.ok().body(MemberMapper.INSTANCE.MemeberToInfo(member));
    }

    @Operation(summary = "email check", description = "email check")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Member Email 사용 가능 여부 반환", content = @Content(schema = @Schema(implementation = boolean.class))),
    })
    @GetMapping("/email/check/{email}")
    public ResponseEntity<Boolean> checkEmail(@Parameter(description = "Member Email") @PathVariable String email) {
        return ResponseEntity.ok().body(!memberService.isExistingEmail(email));
    }

    @Operation(summary = "add Member", description = "add Member")
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "가입 Member 정보 반환"),
            @ApiResponse(responseCode = "500", description = "가입 실패", content = @Content),
            @ApiResponse(responseCode = "400", description = "가입 실패(이메일 충돌)", content = @Content)
    })
    @PostMapping("/")
    public ResponseEntity<MemberDTO.Info> createMember(@Parameter(description = "Member Info") @RequestBody MemberDTO.Create param) {
        Member member = MemberMapper.INSTANCE.CreateToMember(param);

        try {
            member = memberService.saveMember(member);
        } catch (DataIntegrityViolationException e) {
            log.warn("Error saving Member: {} , {}", e.getMessage(), member);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        } catch (RuntimeException e) {
            log.warn("email already exists: {}, {}", e.getMessage(), member);
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).build();
        }
        return ResponseEntity.created(URI.create("/Members/" + member.getId())).build();
    }

    @Operation(summary = "update Member", description = "update Member")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "수정 Member 정보 반환"),
            @ApiResponse(responseCode = "409", description = "수정 실패", content = @Content)
    })
    @PutMapping("/{id}")
    public ResponseEntity<MemberDTO.Info> updateMember(@PathVariable Integer id, @Parameter(description = "Member Info") @RequestBody @Valid MemberDTO.Update update) {
        Member member = MemberMapper.INSTANCE.UpdateToMember(update);

        try {
            member = memberService.updateMember(id,member);
        } catch (RuntimeException e) {
            log.warn("Error updating Member: {}, {}", e.getMessage(), member);
            return ResponseEntity.status(HttpStatus.CONFLICT).build();
        }
        return ResponseEntity.ok().body(MemberMapper.INSTANCE.MemeberToInfo(member));
    }

    @Operation(summary = "delete Member", description = "delete Member")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Member 삭제 성공", content = @Content),
            @ApiResponse(responseCode = "500", description = "삭제 실패", content = @Content)
    })
    @DeleteMapping("/{id}")
    public ResponseEntity<Boolean> deleteMemberById(@Parameter(description = "Member ID") @PathVariable Integer id) {
        try {
            memberService.deleteMemberById(id);
        } catch (Exception e) {
            log.warn("Error deleting Member: {}, {}", e.getMessage(), id);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
        return ResponseEntity.ok().body(true);
    }
}