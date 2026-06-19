package org.example.knockin.service.impl;

import org.example.knockin.dto.EmailConfirmDto;
import org.example.knockin.dto.EmailSendDto;
import org.example.knockin.entity.auth.Authentication;
import org.example.knockin.entity.auth.AuthenticationType;
import org.example.knockin.entity.member.Member;
import org.example.knockin.global.exception.BusinessException;
import org.example.knockin.global.exception.EmailErrorCode;
import org.example.knockin.global.exception.MemberErrorCode;
import org.example.knockin.repository.auth.AuthenticationRepository;
import org.example.knockin.service.MailSendService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.core.io.Resource;
import org.springframework.core.io.ResourceLoader;
import org.springframework.test.util.ReflectionTestUtils;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("인증 서비스 테스트")
class AuthenticationServiceImplTest {

    @Mock
    private AuthenticationRepository authenticationRepository;

    @Mock
    private MemberServiceImpl memberService;

    @Mock
    private MailSendService mailSendService;

    @Mock
    private ResourceLoader resourceLoader;

    @InjectMocks
    private AuthenticationServiceImpl authenticationService;

    @Test
    @DisplayName("메일 웹훅 처리 성공 테스트")
    void mailWebHookSuccessTest() {
        // given
        String payload = "payload";
        Map<String, String> headers = new HashMap<>();
        headers.put("header-key", "header-value");

        given(mailSendService.mailWebHook(payload, headers)).willReturn(new HashMap<>());

        // when
        String result = authenticationService.mailWebHook(payload, headers);

        // then
        assertThat(result).isEqualTo("webhook success");
        verify(mailSendService).mailWebHook(payload, headers);
    }

    @Test
    @DisplayName("학생 이메일 인증번호 발송 성공 테스트")
    void sendAuthNumStudentSuccessTest() throws IOException {
        // given
        Long memberId = 1L;
        EmailSendDto.Request request = new EmailSendDto.Request("student@univ.ac.kr");

        Member member = mock(Member.class);
        given(memberService.findById(memberId)).willReturn(Optional.of(member));

        Resource resource = mock(Resource.class);
        String mockHtml = "<html>${authToken}</html>";
        given(resourceLoader.getResource("classpath:templates/mail/auth_template.html")).willReturn(resource);
        given(resource.getInputStream()).willReturn(new ByteArrayInputStream(mockHtml.getBytes()));

        given(mailSendService.mailSend(anyString(), anyString(), anyString())).willReturn("email_id_123");

        Authentication oldAuth = spy(Authentication.builder().build());
        given(authenticationRepository.findByMemberAndIsDeletedAndIsAcceptedAndType(member, false, false, AuthenticationType.STUDENT))
                .willReturn(List.of(oldAuth));

        // when
        EmailSendDto.Response response = authenticationService.sendAuthNumStudent(request, memberId);

        // then
        assertThat(response).isNotNull();
        assertThat(response.getUpdatedAt()).isNotNull();
        verify(oldAuth).deleteAuthentication();
        verify(authenticationRepository).save(any(Authentication.class));
    }

    @Test
    @DisplayName("직장인 이메일 인증번호 발송 성공 테스트")
    void sendAuthNumCompanySuccessTest() throws IOException {
        // given
        Long memberId = 1L;
        EmailSendDto.Request request = new EmailSendDto.Request("company@company.com");

        Member member = mock(Member.class);
        given(memberService.findById(memberId)).willReturn(Optional.of(member));

        Resource resource = mock(Resource.class);
        String mockHtml = "<html>${authToken}</html>";
        given(resourceLoader.getResource("classpath:templates/mail/auth_template.html")).willReturn(resource);
        given(resource.getInputStream()).willReturn(new ByteArrayInputStream(mockHtml.getBytes()));

        given(mailSendService.mailSend(anyString(), anyString(), anyString())).willReturn("email_id_123");

        Authentication oldAuth = spy(Authentication.builder().build());
        given(authenticationRepository.findByMemberAndIsDeletedAndIsAcceptedAndType(member, false, false, AuthenticationType.COMPANY))
                .willReturn(List.of(oldAuth));

        // when
        EmailSendDto.Response response = authenticationService.sendAuthNumCompany(request, memberId);

        // then
        assertThat(response).isNotNull();
        assertThat(response.getUpdatedAt()).isNotNull();
        verify(oldAuth).deleteAuthentication();
        verify(authenticationRepository).save(any(Authentication.class));
    }

    @Test
    @DisplayName("이메일 발송 시 회원이 없으면 MemberErrorCode.MEMBER_NOT_FOUND 예외 발생")
    void sendAuthNumMemberNotFoundTest() {
        // given
        Long memberId = 1L;
        EmailSendDto.Request request = new EmailSendDto.Request("test@test.com");
        given(memberService.findById(memberId)).willReturn(Optional.empty());

        // when & then
        assertThatThrownBy(() -> authenticationService.sendAuthNumStudent(request, memberId))
                .isInstanceOf(BusinessException.class)
                .hasFieldOrPropertyWithValue("errorCode", MemberErrorCode.MEMBER_NOT_FOUND);
    }

    @Test
    @DisplayName("학생 이메일 인증번호 검증 성공 테스트")
    void confirmAuthNumStudentSuccessTest() {
        // given
        Long memberId = 1L;
        EmailConfirmDto.Request request = new EmailConfirmDto.Request("student@univ.ac.kr", "123456");

        Member member = mock(Member.class);
        given(memberService.findById(memberId)).willReturn(Optional.of(member));

        Authentication authentication = spy(Authentication.builder()
                .email("student@univ.ac.kr")
                .code("123456")
                .isAccepted(false)
                .isDeleted(false)
                .build());
        ReflectionTestUtils.setField(authentication, "createdAt", LocalDateTime.now());

        given(authenticationRepository.findFirstByMemberAndIsDeletedAndIsAcceptedAndTypeOrderByCreatedAtDesc(member, false, false, AuthenticationType.STUDENT))
                .willReturn(Optional.of(authentication));

        // when
        EmailConfirmDto.Response response = authenticationService.confirmAuthNumStudent(request, memberId);

        // then
        assertThat(response).isNotNull();
        assertThat(response.getUpdatedAt()).isNotNull();
        verify(authentication).acceptAuthentication();
    }

    @Test
    @DisplayName("직장인 이메일 인증번호 검증 성공 테스트")
    void confirmAuthNumCompanySuccessTest() {
        // given
        Long memberId = 1L;
        EmailConfirmDto.Request request = new EmailConfirmDto.Request("company@company.com", "123456");

        Member member = mock(Member.class);
        given(memberService.findById(memberId)).willReturn(Optional.of(member));

        Authentication authentication = spy(Authentication.builder()
                .email("company@company.com")
                .code("123456")
                .isAccepted(false)
                .isDeleted(false)
                .build());
        ReflectionTestUtils.setField(authentication, "createdAt", LocalDateTime.now());

        given(authenticationRepository.findFirstByMemberAndIsDeletedAndIsAcceptedAndTypeOrderByCreatedAtDesc(member, false, false, AuthenticationType.COMPANY))
                .willReturn(Optional.of(authentication));

        // when
        EmailConfirmDto.Response response = authenticationService.confirmAuthNumCompany(request, memberId);

        // then
        assertThat(response).isNotNull();
        assertThat(response.getUpdatedAt()).isNotNull();
        verify(authentication).acceptAuthentication();
    }

    @Test
    @DisplayName("인증번호 유효시간(3분) 경과 시 예외 발생 테스트")
    void confirmAuthNumTimeoutTest() {
        // given
        Long memberId = 1L;
        EmailConfirmDto.Request request = new EmailConfirmDto.Request("student@univ.ac.kr", "123456");

        Member member = mock(Member.class);
        given(memberService.findById(memberId)).willReturn(Optional.of(member));

        Authentication authentication = Authentication.builder()
                .email("student@univ.ac.kr")
                .code("123456")
                .build();
        // 4분 전 생성된 것으로 설정
        ReflectionTestUtils.setField(authentication, "createdAt", LocalDateTime.now().minusMinutes(4));

        given(authenticationRepository.findFirstByMemberAndIsDeletedAndIsAcceptedAndTypeOrderByCreatedAtDesc(member, false, false, AuthenticationType.STUDENT))
                .willReturn(Optional.of(authentication));

        // when & then
        assertThatThrownBy(() -> authenticationService.confirmAuthNumStudent(request, memberId))
                .isInstanceOf(BusinessException.class)
                .hasFieldOrPropertyWithValue("errorCode", EmailErrorCode.EMAIL_VALID_ERROR);
    }

    @Test
    @DisplayName("인증번호가 다르면 예외 발생 테스트")
    void confirmAuthNumMismatchTest() {
        // given
        Long memberId = 1L;
        EmailConfirmDto.Request request = new EmailConfirmDto.Request("student@univ.ac.kr", "wrong_code");

        Member member = mock(Member.class);
        given(memberService.findById(memberId)).willReturn(Optional.of(member));

        Authentication authentication = Authentication.builder()
                .email("student@univ.ac.kr")
                .code("123456")
                .build();
        ReflectionTestUtils.setField(authentication, "createdAt", LocalDateTime.now());

        given(authenticationRepository.findFirstByMemberAndIsDeletedAndIsAcceptedAndTypeOrderByCreatedAtDesc(member, false, false, AuthenticationType.STUDENT))
                .willReturn(Optional.of(authentication));

        // when & then
        assertThatThrownBy(() -> authenticationService.confirmAuthNumStudent(request, memberId))
                .isInstanceOf(BusinessException.class)
                .hasFieldOrPropertyWithValue("errorCode", EmailErrorCode.EMAIL_VALID_ERROR);
    }
}
