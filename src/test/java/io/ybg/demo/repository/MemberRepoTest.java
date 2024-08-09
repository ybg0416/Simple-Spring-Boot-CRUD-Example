package io.ybg.demo.repository;

import io.ybg.demo.entity.MemberEntity;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.test.context.TestConstructor;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

// junit5 -> RequiredArgsConstructor 사용을 위해
@TestConstructor(autowireMode = TestConstructor.AutowireMode.ALL)
@RequiredArgsConstructor
@DataJpaTest
@Slf4j
public class MemberRepoTest {

    private final MemberRepo memberRepo;

    @DisplayName("초기화 상태")
    @Test
    public void initTest() {
        assertThat(memberRepo).isNotNull();
    }

    @Test
    @DisplayName("사용자 추가")
    void addUserTest() {
        // given
        MemberEntity member = getMemberEntity();

        // when
        memberRepo.save(member);
        MemberEntity saveMember = memberRepo.findById(member.getId()).orElseThrow(RuntimeException::new);

        log.info("{}", saveMember);
        log.info("{}", member);

        // then
        assertThat(member).isEqualTo(saveMember);
    }

    @Test
    @DisplayName("사용자 이메일 조회")
    void existsByEmailTest() {
        //given
        MemberEntity member = getMemberEntity();

        // when
        boolean exists1 = memberRepo.existsByEmail(member.getEmail());
        memberRepo.save(member);
        boolean exists2 = memberRepo.existsByEmail(member.getEmail());

        log.info("{}", exists1);
        log.info("{}", exists2);

        // then
        assertThat(exists1).isFalse();
        assertThat(exists2).isTrue();
    }

    @DisplayName("유저 전체 검색")
    @Test
    void findAllTest() {
        //given
        MemberEntity member = getMemberEntity();

        // when
        List<MemberEntity> emptyList = memberRepo.findAll();
        memberRepo.save(member);
        List<MemberEntity> list = memberRepo.findAll();

        log.info("{}", emptyList);
        log.info("{}", list);

        // then
        assertTrue(emptyList.isEmpty());
        assertFalse(list.isEmpty());
        assertThat(list.size()).isEqualTo(1);
    }

    @DisplayName("유저 ID 검색")
    @Test
    void findIDTest() {
        //given
        MemberEntity member = getMemberEntity();

        // when
        boolean emptyMember = memberRepo.findById(1L).isEmpty();
        memberRepo.save(member);
        Optional<MemberEntity> newMember = memberRepo.findById(member.getId());

        log.info("{}", emptyMember);
        log.info("{}", newMember);

        // then
        assertTrue(emptyMember);
        assertFalse(newMember.isEmpty());
        assertThat(newMember.get()).isEqualTo(member);
    }

    @DisplayName("유저 수정")
    @Test
    void updateMemberTest() {
        //given
        MemberEntity member = getMemberEntity();
        final String newName = "테스트";

        // when
        MemberEntity newMember = memberRepo.save(member);
        boolean checkName1 = newMember.getName().equals(newName);
        newMember.setName(newName);
        boolean checkName2 = newMember.getName().equals(newName);

        log.info("{}", checkName1);
        log.info("{}", checkName2);

        // then
        assertFalse(checkName1);
        assertTrue(checkName2);
    }

    @DisplayName("유저 삭제")
    @Test
    void deleteMemberTest() {
        //given
        MemberEntity member = getMemberEntity();

        // when
        MemberEntity newMember = memberRepo.save(member);
        boolean isMember = memberRepo.findById(newMember.getId()).isPresent();
        log.info("{}", isMember);

        memberRepo.deleteById(newMember.getId());
        boolean emptyMember = memberRepo.findById(newMember.getId()).isEmpty();
        log.info("{}", emptyMember);

        // then
        assertTrue(isMember);
        assertTrue(emptyMember);
    }

    private MemberEntity getMemberEntity() {
        return MemberEntity.builder().email("admin@mail.com").name("운영자").phone("010-1234-5678").build();
    }

}