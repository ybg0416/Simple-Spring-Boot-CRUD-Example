package io.ybg.demo.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.ybg.demo.dto.MemberDTO;
import io.ybg.demo.entity.MemberEntity;
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
    @GetMapping()
    public ResponseEntity<List<MemberDTO.InfoMemberDTO>> getAllMembers() {
        List<MemberEntity> members = memberService.getAllMembers();

        if (members.isEmpty()) {
            return ResponseEntity.noContent().build();
        }

        return ResponseEntity.ok().body(MemberMapper.INSTANCE.MemberToInfo(members));
    }

    @Operation(summary = "get Member By ID", description = "get Member By ID")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "ID 해당하는 멤버 반환"),
            @ApiResponse(responseCode = "404", description = "존재하지 않는 멤버", content = @Content),
    })
    @GetMapping("/{id}")
    public ResponseEntity<MemberDTO.InfoMemberDTO> getMemberById(@Parameter(description = "Member ID") @PathVariable long id) {
        try {
            MemberEntity memberEntity = memberService.getMemberById(id);
            return ResponseEntity.ok().body(MemberMapper.INSTANCE.MemberToInfo(memberEntity));
        } catch (RuntimeException e) {
            return ResponseEntity.notFound().build();
        }
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
    @PostMapping()
    public ResponseEntity<MemberDTO.InfoMemberDTO> createMember(@Parameter(description = "Member Info") @RequestBody MemberDTO.CreateMemberDTO param) {
        MemberEntity memberEntity = MemberMapper.INSTANCE.CreateToMember(param);

        try {
            memberEntity = memberService.saveMember(memberEntity);

        } catch (DataIntegrityViolationException e) {
            log.warn("Error saving Member: {} , {}", e.getMessage(), memberEntity);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).build();
        }
        return ResponseEntity.created(URI.create("/member/v1/" + memberEntity.getId())).build();
    }

    @Operation(summary = "update Member", description = "update Member")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "수정 Member 정보 반환"),
            @ApiResponse(responseCode = "409", description = "수정 실패", content = @Content)
    })
    @PutMapping("/{id}")
    public ResponseEntity<MemberDTO.InfoMemberDTO> updateMember(@PathVariable long id, @Parameter(description = "Member Info") @RequestBody @Valid MemberDTO.UpdateMemberDTO updateDTO) {
        MemberEntity memberEntity = MemberMapper.INSTANCE.UpdateToMember(updateDTO);

        try {
            memberEntity = memberService.updateMember(id, memberEntity);
        } catch (DataIntegrityViolationException e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        } catch (RuntimeException e) {
            log.warn("Error updating Member: {}, {}", e.getMessage(), memberEntity);
            return ResponseEntity.status(HttpStatus.CONFLICT).build();
        }
        return ResponseEntity.ok().body(MemberMapper.INSTANCE.MemberToInfo(memberEntity));
    }

    @Operation(summary = "delete Member", description = "delete Member")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Member 삭제 성공", content = @Content),
            @ApiResponse(responseCode = "500", description = "삭제 실패", content = @Content)
    })
    @DeleteMapping("/{id}")
    public ResponseEntity<Boolean> deleteMemberById(@Parameter(description = "Member ID") @PathVariable long id) {
        try {
            memberService.deleteMemberById(id);
        } catch (DataIntegrityViolationException e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        } catch (RuntimeException e) {
            log.warn("Error deleting Member: {}, {}", e.getMessage(), id);
            return ResponseEntity.status(HttpStatus.CONFLICT).build();
        }
        return ResponseEntity.ok().body(true);
    }
}