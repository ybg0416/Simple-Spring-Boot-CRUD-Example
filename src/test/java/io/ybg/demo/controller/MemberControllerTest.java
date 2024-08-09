package io.ybg.demo.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.ybg.demo.dto.MemberDTO;
import io.ybg.demo.entity.MemberEntity;
import io.ybg.demo.service.MemberService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.MediaType;
import org.springframework.test.context.TestConstructor;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;

import java.util.ArrayList;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest
@Slf4j
@TestConstructor(autowireMode = TestConstructor.AutowireMode.ALL)
@RequiredArgsConstructor
@ExtendWith(MockitoExtension.class)
class MemberControllerTest {

    private final MemberController memberController;
    private final MockMvc mockMvc;
    private final ObjectMapper mapper = new ObjectMapper();

    @MockBean
    private MemberService memberService;

    @DisplayName("초기화 상태")
    @Test
    public void initTest() {
        assertThat(memberController).isNotNull();
        assertThat(memberService).isNotNull();
        assertThat(mockMvc).isNotNull();
    }

    private MemberDTO.UpdateMemberDTO getUpdateDTO() {
        return MemberDTO.UpdateMemberDTO.builder().email("update@mail.com").name("운영자").phone("010-1234-5678").build();
    }

    private MemberDTO.CreateMemberDTO getCreateDTO() {
        return MemberDTO.CreateMemberDTO.builder().email("admin@mail.com").name("운영자").phone("010-1234-5678").build();
    }

    @Nested
    @DisplayName("가입")
    class create {
        @DisplayName("정상")
        @Test
        void createMemberTest() throws Exception {
            // given
            final String url = "/member/v1";
            final long id = 0;
            doReturn(mock(MemberEntity.class)).when(memberService).saveMember(any(MemberEntity.class));

            // when
            final ResultActions resultActions = mockMvc.perform(
                    MockMvcRequestBuilders.post(url)
                            .content(mapper.writeValueAsString(getCreateDTO()))
                            .contentType(MediaType.APPLICATION_JSON)
            );

            // then
            resultActions
                    .andExpect(status().isCreated())
                    .andExpect(redirectedUrl("/member/v1/" + id));
        }

        @DisplayName("실패 : 이메일 중복")
        @Test
        void createMemberTest_dup() throws Exception {
            // given
            final String url = "/member/v1";
            doThrow(new RuntimeException("Email already exists")).when(memberService).saveMember(any(MemberEntity.class));

            // when
            final ResultActions resultActions = mockMvc.perform(
                    MockMvcRequestBuilders.post(url)
                            .content(mapper.writeValueAsString(getCreateDTO()))
                            .contentType(MediaType.APPLICATION_JSON)
            );

            // then
            resultActions.andExpect(status().isBadRequest());
            // TODO ybg 240809 Exception class, ControllerAdvice 추가 후 개선 예정
//                    .andExpect(result -> {
//                        assertThat(result.getResponse().getContentAsString()).contains("Email already exists");
//                    });
        }

        @DisplayName("실패 : db error")
        @Test
        void createMemberTest_db() throws Exception {
            // given
            final String url = "/member/v1";
            doThrow(new DataIntegrityViolationException("TEST")).when(memberService).saveMember(any(MemberEntity.class));

            // when
            final ResultActions resultActions = mockMvc.perform(
                    MockMvcRequestBuilders.post(url)
                            .content(mapper.writeValueAsString(getCreateDTO()))
                            .contentType(MediaType.APPLICATION_JSON)
            );

            // then
            resultActions.andExpect(status().isInternalServerError());
        }
    }

    @Nested
    @DisplayName("조회")
    class find {
        @DisplayName("ID 정상")
        @Test
        void getMemberByIdTest() throws Exception {
            // given
            final String url = "/member/v1/";
            final long id = 1L;
            doReturn(mock(MemberEntity.class)).when(memberService).getMemberById(anyLong());

            // when
            final ResultActions resultActions = mockMvc.perform(
                    MockMvcRequestBuilders.get(url + id)
            );

            // then
            resultActions.andExpect(status().isOk());
        }

        @DisplayName("ID 실패 : 존재하지 않는 회원")
        @Test
        void getMemberByIdTest_notFound() throws Exception {
            // given
            final String url = "/member/v1/";
            final long id = 1L;
            doThrow(new RuntimeException("Find Member with id: " + id + " doesn't exist")).when(memberService).getMemberById(id);

            // when
            final ResultActions resultActions = mockMvc.perform(
                    MockMvcRequestBuilders.get(url + id)
            );

            // then
            resultActions.andExpect(status().isNotFound());
        }

        @DisplayName("리스트 정상")
        @Test
        void getAllMembersTest() throws Exception {
            // given
            final String url = "/member/v1";
            doReturn(new ArrayList<MemberEntity>() {{
                add(mock(MemberEntity.class));
            }}).when(memberService).getAllMembers();

            // when
            final ResultActions resultActions = mockMvc.perform(
                    MockMvcRequestBuilders.get(url)
            );

            log.info("{}", resultActions.andReturn().getResponse().getContentAsString());

            // then
            resultActions.andExpect(status().isOk());
        }

        @DisplayName("리스트 공백")
        @Test
        void getAllMembersTest_empty() throws Exception {
            // given
            final String url = "/member/v1";
            doReturn(new ArrayList<MemberEntity>()).when(memberService).getAllMembers();

            // when
            final ResultActions resultActions = mockMvc.perform(
                    MockMvcRequestBuilders.get(url)
            );

            // then
            resultActions.andExpect(status().isNoContent());
        }

        @DisplayName("이메일 사용 : true")
        @Test
        void getMemberByEmailTest() throws Exception {
            // given
            final String url = "/member/v1/email/check/";
            final String email = "test@email.com";
            doReturn(false).when(memberService).isExistingEmail(anyString());

            // when
            final ResultActions resultActions = mockMvc.perform(
                    MockMvcRequestBuilders.get(url + email)
            );

            // then
            resultActions
                    .andExpect(status().isOk())
                    .andExpect(content().string("true"));
        }

        @DisplayName("이메일 사용 : false")
        @Test
        void getMemberByEmailTest_false() throws Exception {
            // given
            final String url = "/member/v1/email/check/";
            final String email = "test@email.com";
            doReturn(true).when(memberService).isExistingEmail(anyString());

            // when
            final ResultActions resultActions = mockMvc.perform(
                    MockMvcRequestBuilders.get(url + email)
            );

            // then
            resultActions
                    .andExpect(status().isOk())
                    .andExpect(content().string("false"));
        }
    }

    @Nested
    @DisplayName("수정")
    class modify {
        @DisplayName("정상")
        @Test
        void updateMemberTest() throws Exception {
            // given
            final String url = "/member/v1/";
            final long id = 1L;
            doReturn(mock(MemberEntity.class)).when(memberService).updateMember(anyLong(), any(MemberEntity.class));

            // when
            final ResultActions resultActions = mockMvc.perform(
                    MockMvcRequestBuilders.put(url + id)
                            .content(mapper.writeValueAsString(getUpdateDTO()))
                            .contentType(MediaType.APPLICATION_JSON)
            );

            // then
            resultActions.andExpect(status().isOk());

            // verify
            verify(memberService, times(1)).updateMember(anyLong(), any(MemberEntity.class));
        }

        @DisplayName("실패 : email not found")
        @Test
        void updateMemberTest_notFound() throws Exception {
            // given
            final String url = "/member/v1/";
            final long id = 1L;
            doThrow(new RuntimeException("Email not found")).when(memberService).updateMember(anyLong(), any(MemberEntity.class));

            // when
            final ResultActions resultActions = mockMvc.perform(
                    MockMvcRequestBuilders.put(url + id)
                            .content(mapper.writeValueAsString(getUpdateDTO()))
                            .contentType(MediaType.APPLICATION_JSON)
            );

            // then
            resultActions.andExpect(status().isConflict());

            // verify
            verify(memberService, times(1)).updateMember(anyLong(), any(MemberEntity.class));
        }

        @DisplayName("실패 : DB")
        @Test
        void updateMemberTest_db() throws Exception {
            // given
            final String url = "/member/v1/";
            final long id = 1L;
            doThrow(new DataIntegrityViolationException("TEST")).when(memberService).updateMember(anyLong(), any(MemberEntity.class));
            doReturn(mock(MemberEntity.class)).when(memberService).getMemberById(anyLong());

            // when
            final ResultActions resultActions = mockMvc.perform(
                    MockMvcRequestBuilders.put(url + id)
                            .content(mapper.writeValueAsString(getUpdateDTO()))
                            .contentType(MediaType.APPLICATION_JSON)
            );

            // then
            resultActions.andExpect(status().isInternalServerError());

            // verify
            verify(memberService, times(1)).updateMember(anyLong(), any(MemberEntity.class));
        }
    }

    @Nested
    @DisplayName("삭제")
    class delete {
        @DisplayName("정상")
        @Test
        void deleteMemberByIdTest() throws Exception {
            // given
            final String url = "/member/v1/";
            final long id = 1L;
            doNothing().when(memberService).deleteMemberById(anyLong());

            // when
            final ResultActions resultActions = mockMvc.perform(
                    MockMvcRequestBuilders.delete(url + id)
            );

            // then
            resultActions.andExpect(status().isOk());

            // verify
            verify(memberService, times(1)).deleteMemberById(anyLong());
        }

        @DisplayName("실패 : 존재하지 않음")
        @Test
        void deleteMemberByIdTest_notFound() throws Exception {
            // given
            final String url = "/member/v1/";
            final long id = 1L;
            doThrow(new RuntimeException("Delete Member with id: " + id + " doesn't exist")).when(memberService).deleteMemberById(anyLong());

            // when
            final ResultActions resultActions = mockMvc.perform(
                    MockMvcRequestBuilders.delete(url + id)
            );

            // then
            resultActions.andExpect(status().isConflict());

            // verify
            verify(memberService, times(1)).deleteMemberById(anyLong());
        }

        @DisplayName("실패")
        @Test
        void deleteMemberByIdTest_fail() throws Exception {
            // given
            final String url = "/member/v1/";
            final long id = 1L;
            doThrow(new DataIntegrityViolationException("TEST")).when(memberService).deleteMemberById(anyLong());

            // when
            final ResultActions resultActions = mockMvc.perform(
                    MockMvcRequestBuilders.delete(url + id)
            );

            // then
            resultActions.andExpect(status().isInternalServerError());

            // verify
            verify(memberService, times(1)).deleteMemberById(anyLong());
        }
    }

}

