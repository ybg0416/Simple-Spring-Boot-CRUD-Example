package io.ybg.demo.service;

import io.ybg.demo.entity.MemberEntity;
import io.ybg.demo.repository.MemberRepo;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.dao.DataIntegrityViolationException;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@Slf4j
@RequiredArgsConstructor
@ExtendWith(MockitoExtension.class)
class MemberServiceTest {

    /*
    Given: 테스트를 진행할 행위를 위한 사전 준비
    when: 테스트를 진행할 행위
    then: 테스트를 진행한 행위에 대한 결과 검증
    verify: 메서드 실행 여부 검증
    */
    @InjectMocks
    private MemberService memberService;

    // Test 협력자
    @Mock
    private MemberRepo memberRepo;

    @DisplayName("초기화 상태")
    @Test
    public void initTest() {
        assertThat(memberService).isNotNull();
        assertThat(memberRepo).isNotNull();
    }

    private MemberEntity getMemberEntity() {
        return MemberEntity.builder().email("admin@mail.com").name("운영자").phone("010-1234-5678").id(1L).build();
    }

    @Nested
    @DisplayName("생성")
    class create {
        @DisplayName("정상")
        @Test
        void saveMemberTest() {
            // given
            doReturn(false).when(memberRepo).existsByEmail(anyString());
            doReturn(mock(MemberEntity.class)).when(memberRepo).save(any(MemberEntity.class));

            // when
            MemberEntity savedMember = memberService.saveMember(getMemberEntity());

            log.info("{}", savedMember);

            // then
            assertThat(savedMember).isNotNull();

            // verify
            verify(memberRepo, times(1)).existsByEmail(anyString());
            verify(memberRepo, times(1)).save(any(MemberEntity.class));
        }

        @Test
        @DisplayName("실패 : 중복 이메일")
        void saveMember_dup() {
            // given
            doReturn(true).when(memberRepo).existsByEmail(anyString());

            // when
            RuntimeException result = assertThrows(RuntimeException.class, () -> memberService.saveMember(getMemberEntity()));

            // then
            assertThat(result.getMessage()).isEqualTo("Email already exists");

            // verify
            verify(memberRepo, times(1)).existsByEmail(anyString());
        }

        @Test
        @DisplayName("실패 : db 에러")
        void saveMember_db() {
            // given
            doReturn(false).when(memberRepo).existsByEmail(anyString());
            doThrow(new DataIntegrityViolationException("TEST")).when(memberRepo).save(any(MemberEntity.class));

            // when
            DataIntegrityViolationException result = assertThrows(DataIntegrityViolationException.class, () -> memberService.saveMember(getMemberEntity()));

            // then
            assertThat(result).satisfies(ex -> {
                assertThat(ex.getClass()).isEqualTo(DataIntegrityViolationException.class);
                assertThat(ex.getMessage()).isEqualTo("TEST");
            });

            // verify
            verify(memberRepo, times(1)).existsByEmail(anyString());
            verify(memberRepo, times(1)).save(any(MemberEntity.class));
        }
    }

    @Nested
    @DisplayName("get / check")
    class find {
        @DisplayName("리스트 정상")
        @Test
        void getAllMembersTest() {
            // given
            doReturn(Arrays.asList(mock(MemberEntity.class), mock(MemberEntity.class), mock(MemberEntity.class))).when(memberRepo).findAll();

            // when
            List<MemberEntity> allMembers = memberService.getAllMembers();

            // then
            assertThat(allMembers).isNotEmpty();
            assertThat(allMembers.size()).isEqualTo(3);
            assertThat(allMembers.getFirst().getClass()).isEqualTo(MemberEntity.class);
        }

        @DisplayName("리스트 비어있음")
        @Test
        void getEmptyMembersTest() {
            // given
            doReturn(Collections.emptyList()).when(memberRepo).findAll();

            // when
            List<MemberEntity> allMembers = memberService.getAllMembers();

            // then
            assertThat(allMembers).isEqualTo(Collections.emptyList());
        }

        @Test
        @DisplayName("찾기 ID 정상")
        void getMemberByIdTest() {
            // given
            doReturn(Optional.of(mock(MemberEntity.class))).when(memberRepo).findById(anyLong());

            // when
            MemberEntity findMember = memberService.getMemberById(anyLong());

            // then
            assertThat(findMember).isNotNull();
        }

        @Test
        @DisplayName("찾기 ID 없음")
        void getMemberById_Empty() {
            // given
            final long id = 1L;
            doThrow(new RuntimeException("Find Member with id: " + id + " doesn't exist")).when(memberRepo).findById(anyLong());

            // when
            RuntimeException result = assertThrows(RuntimeException.class, () -> memberService.getMemberById(1L));

            // then
            assertThat(result.getMessage()).isEqualTo("Find Member with id: " + id + " doesn't exist");
        }

        @Nested
        @DisplayName("이메일 체크")
        class check {
            @Test
            @DisplayName("정상")
            void isExistingEmailTest() {
                // given
                doReturn(false).when(memberRepo).existsByEmail(anyString());

                // when
                boolean result = memberService.isExistingEmail(anyString());

                // then
                assertThat(result).isFalse();
            }

            @Test
            @DisplayName("중복")
            void isExistingEmail_dup() {
                // given
                doReturn(true).when(memberRepo).existsByEmail(anyString());

                // when
                boolean result = memberService.isExistingEmail(anyString());

                // then
                assertThat(result).isTrue();
            }
        }
    }

    @Nested
    @DisplayName("수정")
    class update {

        @Test
        @DisplayName("정상")
        void updateMemberTest() {
            // given
            doReturn(Optional.of(mock(MemberEntity.class))).when(memberRepo).findById(anyLong());
            doReturn(mock(MemberEntity.class)).when(memberRepo).save(any(MemberEntity.class));

            // when
            MemberEntity updateMember = memberService.updateMember(1L, getMemberEntity());

            // then
            assertThat(updateMember).isNotNull();

            // verify
            verify(memberRepo, times(1)).save(any(MemberEntity.class));
        }

        @Test
        @DisplayName("실패 : 유저없음")
        void updateMember_notfound() {
            // given
            doReturn(Optional.empty()).when(memberRepo).findById(anyLong());

            // when
            RuntimeException result = assertThrows(RuntimeException.class, () -> memberService.updateMember(1L, getMemberEntity()));

            // then
            assertThat(result.getMessage()).isEqualTo("Update Member with id: " + 1L + " doesn't exist");
        }

        @Test
        @DisplayName("실패 : 중복이메일")
        void updateMember_dup() {
            // given
            doReturn(Optional.of(mock(MemberEntity.class))).when(memberRepo).findById(anyLong());
            doReturn(true).when(memberRepo).existsByEmail(anyString());

            // when
            RuntimeException result = assertThrows(RuntimeException.class, () -> memberService.updateMember(1L, getMemberEntity()));

            // then
            assertThat(result.getMessage()).isEqualTo("Update Email already exists");
        }

        @Test
        @DisplayName("실패 : db 오류")
        void updateMember_db() {
            // given
            doThrow(new DataIntegrityViolationException("TEST")).when(memberRepo).save(any(MemberEntity.class));
            doReturn(Optional.of(mock(MemberEntity.class))).when(memberRepo).findById(anyLong());

            // when
            DataIntegrityViolationException result = assertThrows(DataIntegrityViolationException.class, () -> memberService.updateMember(1L, getMemberEntity()));

            // then
            assertThat(result).satisfies(ex -> {
                assertThat(ex.getClass()).isEqualTo(DataIntegrityViolationException.class);
                assertThat(ex.getMessage()).isEqualTo("TEST");
            });
        }

    }

    @Nested
    @DisplayName("삭제")
    class delete {

        @Test
        @DisplayName("정상")
        void deleteMemberByIdTest() {
            // given
            doReturn(Optional.of(mock(MemberEntity.class))).when(memberRepo).findById(anyLong());

            // when
            memberService.deleteMemberById(anyLong());

            // then
            verify(memberRepo, times(1)).deleteById(anyLong());

            // verify
            verify(memberRepo, times(1)).findById(anyLong());
            verify(memberRepo, times(1)).deleteById(anyLong());
        }

        @Test
        @DisplayName("실패 : 미존재")
        void deleteMemberById_notfound() {
            // given
            doReturn(Optional.empty()).when(memberRepo).findById(anyLong());

            // when
            RuntimeException result = assertThrows(RuntimeException.class, () -> memberService.deleteMemberById(1L));

            // then
            assertThat(result.getMessage()).isEqualTo("Delete Member with id: " + 1L + " doesn't exist");

            // verify
            verify(memberRepo, times(1)).findById(anyLong());
        }

    }
}