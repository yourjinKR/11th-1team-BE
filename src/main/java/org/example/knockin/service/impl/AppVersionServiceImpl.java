package org.example.knockin.service.impl;

import lombok.RequiredArgsConstructor;
import org.example.knockin.dto.AppVersionDto;
import org.example.knockin.dto.AppVersionModifyDto;
import org.example.knockin.dto.AppVersionSaveDto;
import org.example.knockin.entity.utils.AppVersion;
import org.example.knockin.global.exception.AppVersionErrorCode;
import org.example.knockin.global.exception.BusinessException;
import org.example.knockin.repository.utils.AppVersionRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class AppVersionServiceImpl {
    private final AppVersionRepository appVersionRepository;

    public AppVersionDto.Response findAppVersion() {
        AppVersion appVersion = appVersionRepository.findAll().getFirst();
        return AppVersionDto.Response.builder().id(appVersion.getId()).version(appVersion.getVersion()).build();
    }

    @Transactional
    public AppVersionSaveDto.Response saveAppVersion(AppVersionSaveDto.Request request) {
        appVersionRepository.findAll().forEach(AppVersion::deleteAppVersion);
        appVersionRepository.save(AppVersion.builder().version(request.getVersion()).build());
        return AppVersionSaveDto.Response.builder().updatedAt(LocalDateTime.now()).build();
    }

    @Transactional
    public AppVersionModifyDto.Response modifyAppVersion(AppVersionModifyDto.Request request) {
        AppVersion appVersion = appVersionRepository.findById(request.getId()).orElseThrow(() -> new BusinessException(AppVersionErrorCode.APP_VERSION_NOT_FOUND));
        appVersion.modifyVersion(request.getVersion());
        return AppVersionModifyDto.Response.builder().updatedAt(LocalDateTime.now()).build();
    }
}
