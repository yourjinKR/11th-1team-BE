package org.example.knockin.service.impl;

import org.example.knockin.dto.AuthEmailListDto;
import org.example.knockin.dto.AuthEmailModifyDto;
import org.example.knockin.dto.AuthEmailSaveDto;
import org.example.knockin.entity.auth.AuthenticationType;
import org.example.knockin.entity.utils.AuthEmail;
import org.example.knockin.exception.AuthEmailErrorCode;
import org.example.knockin.exception.BusinessException;
import org.example.knockin.repository.utils.AuthEmailRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("인증 가능 이메일 도메인 설정 서비스 테스트")
class AuthEmailServiceImplTest {

    @Mock
    private AuthEmailRepository authEmailRepository;

    @InjectMocks
    private AuthEmailServiceImpl authEmailService;

    @Test
    @DisplayName("인증 가능 이메일 도메인 목록 조회 성공 테스트")
    void findAuthEmailListSuccessTest() {
        // given
        AuthEmail email1 = AuthEmail.builder()
                .id(1L)
                .domain("univ.ac.kr")
                .name("대학교 이메일")
                .dtype(AuthenticationType.STUDENT)
                .isDeleted(false)
                .build();
        AuthEmail email2 = AuthEmail.builder()
                .id(2L)
                .domain("company.com")
                .name("직장 이메일")
                .dtype(AuthenticationType.COMPANY)
                .isDeleted(false)
                .build();

        given(authEmailRepository.findAll()).willReturn(List.of(email1, email2));

        // when
        AuthEmailListDto.Response response = authEmailService.findAuthEmailList();

        // then
        assertThat(response).isNotNull();
        assertThat(response.getAuthEmailInfoList()).hasSize(2);

        AuthEmailListDto.Response.AuthEmailInfo info1 = response.getAuthEmailInfoList().get(0);
        assertThat(info1.getId()).isEqualTo(1L);
        assertThat(info1.getDomain()).isEqualTo("univ.ac.kr");
        assertThat(info1.getName()).isEqualTo("대학교 이메일");
        assertThat(info1.getType()).isEqualTo(AuthenticationType.STUDENT);

        AuthEmailListDto.Response.AuthEmailInfo info2 = response.getAuthEmailInfoList().get(1);
        assertThat(info2.getId()).isEqualTo(2L);
        assertThat(info2.getDomain()).isEqualTo("company.com");
        assertThat(info2.getName()).isEqualTo("직장 이메일");
        assertThat(info2.getType()).isEqualTo(AuthenticationType.COMPANY);
    }

    @Test
    @DisplayName("인증 가능 이메일 도메인 등록 성공 테스트")
    void saveAuthEmailSuccessTest() {
        // given
        AuthEmailSaveDto.Request request = new AuthEmailSaveDto.Request();
        request.setDomain("new-univ.ac.kr");
        request.setName("신규 대학교");
        request.setType(AuthenticationType.STUDENT);

        // when
        AuthEmailSaveDto.Response response = authEmailService.saveAuthEmail(request);

        // then
        assertThat(response).isNotNull();
        assertThat(response.getUpdatedAt()).isNotNull();
        verify(authEmailRepository).save(any(AuthEmail.class));
    }

    @Test
    @DisplayName("인증 가능 이메일 도메인 수정 성공 테스트")
    void modifyAuthEmailSuccessTest() {
        // given
        AuthEmailModifyDto.Request request = new AuthEmailModifyDto.Request();
        request.setId(1L);
        request.setDomain("modified-univ.ac.kr");
        request.setName("변경된 대학교");
        request.setType(AuthenticationType.STUDENT);

        AuthEmail authEmail = spy(AuthEmail.builder()
                .id(1L)
                .domain("univ.ac.kr")
                .name("대학교 이메일")
                .dtype(AuthenticationType.STUDENT)
                .isDeleted(false)
                .build());

        given(authEmailRepository.findById(1L)).willReturn(Optional.of(authEmail));

        // when
        AuthEmailModifyDto.Response response = authEmailService.modifyAuthEmail(request);

        // then
        assertThat(response).isNotNull();
        assertThat(response.getUpdatedAt()).isNotNull();
        verify(authEmail).modifyAuthEmail(request);
    }

    @Test
    @DisplayName("인증 가능 이메일 도메인 수정 시 대상을 찾을 수 없으면 BusinessException 발생")
    void modifyAuthEmailNotFoundTest() {
        // given
        AuthEmailModifyDto.Request request = new AuthEmailModifyDto.Request();
        request.setId(1L);
        request.setDomain("modified-univ.ac.kr");
        request.setName("변경된 대학교");
        request.setType(AuthenticationType.STUDENT);

        given(authEmailRepository.findById(1L)).willReturn(Optional.empty());

        // when & then
        assertThatThrownBy(() -> authEmailService.modifyAuthEmail(request))
                .isInstanceOf(BusinessException.class)
                .hasFieldOrPropertyWithValue("errorCode", AuthEmailErrorCode.AUTH_EMAIL_NOT_FOUND);

        verify(authEmailRepository, never()).save(any(AuthEmail.class));
    }
}
