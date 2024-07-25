package io.ybg.demo.service;

import io.ybg.demo.entity.Member;
import io.ybg.demo.repository.MemberRepo;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
@Slf4j
public class MemberService {

    private final MemberRepo memberRepo;

    public List<Member> getAllMembers() {
        return memberRepo.findAll();
    }

    public Member getMemberById(Integer id) {
        Optional<Member> optionalMember = memberRepo.findById(id);
        if (optionalMember.isPresent()) {
            return optionalMember.get();
        }
        log.info("Find Member with id: {} doesn't exist", id);
        return null;
    }

    public void saveMember(Member member) {
        try {
            if (isExistingEmail(member)) {
                throw new RuntimeException("Email already exists");
            }
            Member addMember = memberRepo.save(member);
            log.info("Member saved successfully : {}", addMember);
        } catch (DataIntegrityViolationException e) {
            throw e;
        }
    }

    public void updateMember(Member member) {
        Optional<Member> existingMember = memberRepo.findById(member.getId());

        // 존재 여부 검증
        if (existingMember.isEmpty()) {
            throw new RuntimeException("Update Member with id: " + member.getId() + " doesn't exist");
        }
        // 이메일 중복 검증
        if (!member.getEmail().equals(existingMember.get().getEmail())) {
            if (isExistingEmail(member)) {
                throw new RuntimeException("Update Email already exists");
            }
        }

        memberRepo.save(member);

        log.info("Member with id: {} updated successfully", member.getId());
    }

    public boolean isExistingEmail(Member member) {
        return isExistingEmail(member.getEmail());
    }

    public boolean isExistingEmail(String email) {
        return memberRepo.existsByEmail(email);
    }

    public void deleteMemberById(Integer id) {
        Optional<Member> existingMember = memberRepo.findById(id);

        // 존재 여부 검증
        if (existingMember.isEmpty()) {
            throw new RuntimeException("Delete Member with id: " + id + " doesn't exist");
        }
        memberRepo.deleteById(id);
    }
}