package io.ybg.demo.service;

import io.ybg.demo.entity.MemberEntity;
import io.ybg.demo.mapper.MemberMapper;
import io.ybg.demo.repository.MemberRepo;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
@Slf4j
public class MemberService {

    private final MemberRepo memberRepo;

    public List<MemberEntity> getAllMembers() {
        return memberRepo.findAll();
    }

    public MemberEntity getMemberById(Long id) {
        return memberRepo.findById(id).orElseThrow(() -> new RuntimeException("Find Member with id: " + id + " doesn't exist"));
    }

    public MemberEntity saveMember(MemberEntity memberEntity) {

        if (memberRepo.existsByEmail(memberEntity.getEmail())) {
            throw new RuntimeException("Email already exists");
        }

        memberEntity = memberRepo.save(memberEntity);
        log.info("Member saved successfully : {}", memberEntity);
        return memberEntity;
    }

    public MemberEntity updateMember(Long id, MemberEntity memberEntity) {
        Optional<MemberEntity> existingMember = memberRepo.findById(id);

        // 존재 여부 검증
        if (existingMember.isEmpty()) {
            throw new RuntimeException("Update Member with id: " + id + " doesn't exist");
        }
        // 이메일 중복 검증
        if (!memberEntity.getEmail().equals(existingMember.get().getEmail())) {
            if (memberRepo.existsByEmail(memberEntity.getEmail())) {
                throw new RuntimeException("Update Email already exists");
            }
        }
        // 병합
        memberEntity = MemberMapper.INSTANCE.Update(existingMember.get(), memberEntity);
        memberRepo.save(memberEntity);

        log.info("Member with id: {} updated successfully", memberEntity.getId());
        return memberEntity;
    }

    public void deleteMemberById(Long id) {
        memberRepo.findById(id).ifPresentOrElse(
                m -> memberRepo.deleteById(id),
                () -> {
                    throw new RuntimeException("Delete Member with id: " + id + " doesn't exist");
                }
        );
    }

    public boolean isExistingEmail(String email) {
        return memberRepo.existsByEmail(email);
    }
}