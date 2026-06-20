package org.example.knockin.service.impl;

import org.example.knockin.dto.AppVersionDto;
import org.example.knockin.dto.AppVersionModifyDto;
import org.example.knockin.dto.AppVersionSaveDto;
import org.example.knockin.entity.utils.AppVersion;
import org.example.knockin.global.exception.AppVersionErrorCode;
import org.example.knockin.global.exception.BusinessException;
import org.example.knockin.repository.utils.AppVersionRepository;
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
@DisplayName("앱 버전 서비스 테스트")
class AppVersionServiceImplTest {

    @Mock
    private AppVersionRepository appVersionRepository;

    @InjectMocks
    private AppVersionServiceImpl appVersionService;

    @Test
    @DisplayName("앱 버전 조회 성공 테스트")
    void findAppVersionSuccessTest() {
        // given
        AppVersion appVersion = AppVersion.builder()
                .id(1L)
                .version("1.0.0")
                .isDeleted(false)
                .build();
        given(appVersionRepository.findAll()).willReturn(List.of(appVersion));

        // when
        AppVersionDto.Response response = appVersionService.findAppVersion();

        // then
        assertThat(response).isNotNull();
        assertThat(response.getId()).isEqualTo(1L);
        assertThat(response.getVersion()).isEqualTo("1.0.0");
    }

    @Test
    @DisplayName("앱 버전 등록 성공 테스트 (기존 버전 삭제 후 신규 버전 등록)")
    void saveAppVersionSuccessTest() {
        // given
        AppVersionSaveDto.Request request = new AppVersionSaveDto.Request();
        request.setVersion("2.0.0");

        AppVersion oldVersion = spy(AppVersion.builder().id(1L).version("1.0.0").isDeleted(false).build());
        given(appVersionRepository.findAll()).willReturn(List.of(oldVersion));

        // when
        AppVersionSaveDto.Response response = appVersionService.saveAppVersion(request);

        // then
        assertThat(response).isNotNull();
        assertThat(response.getUpdatedAt()).isNotNull();
        verify(oldVersion).deleteAppVersion();
        verify(appVersionRepository).save(any(AppVersion.class));
    }

    @Test
    @DisplayName("앱 버전 수정 성공 테스트")
    void modifyAppVersionSuccessTest() {
        // given
        AppVersionModifyDto.Request request = new AppVersionModifyDto.Request();
        request.setId(1L);
        request.setVersion("1.0.1");

        AppVersion appVersion = spy(AppVersion.builder().id(1L).version("1.0.0").isDeleted(false).build());
        given(appVersionRepository.findById(1L)).willReturn(Optional.of(appVersion));

        // when
        AppVersionModifyDto.Response response = appVersionService.modifyAppVersion(request);

        // then
        assertThat(response).isNotNull();
        assertThat(response.getUpdatedAt()).isNotNull();
        verify(appVersion).modifyVersion("1.0.1");
    }

    @Test
    @DisplayName("앱 버전 수정 시 버전 정보를 찾을 수 없으면 BusinessException 발생")
    void modifyAppVersionNotFoundTest() {
        // given
        AppVersionModifyDto.Request request = new AppVersionModifyDto.Request();
        request.setId(1L);
        request.setVersion("1.0.1");

        given(appVersionRepository.findById(1L)).willReturn(Optional.empty());

        // when & then
        assertThatThrownBy(() -> appVersionService.modifyAppVersion(request))
                .isInstanceOf(BusinessException.class)
                .hasFieldOrPropertyWithValue("errorCode", AppVersionErrorCode.APP_VERSION_NOT_FOUND);

        verify(appVersionRepository, never()).save(any(AppVersion.class));
    }
}
