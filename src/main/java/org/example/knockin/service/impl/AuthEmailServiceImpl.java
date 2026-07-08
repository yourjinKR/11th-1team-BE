package org.example.knockin.service.impl;

import lombok.RequiredArgsConstructor;
import org.example.knockin.dto.AuthEmailListDto;
import org.example.knockin.dto.AuthEmailModifyDto;
import org.example.knockin.dto.AuthEmailSaveDto;
import org.example.knockin.entity.utils.AuthEmail;
import org.example.knockin.exception.AuthEmailErrorCode;
import org.example.knockin.exception.BusinessException;
import org.example.knockin.repository.utils.AuthEmailRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class AuthEmailServiceImpl {
    private final AuthEmailRepository authEmailRepository;

    public AuthEmailListDto.Response findAuthEmailList() {
        List<AuthEmailListDto.Response.AuthEmailInfo> authEmailInfoList = authEmailRepository.findAll().stream().map(item ->
                AuthEmailListDto.Response.AuthEmailInfo.builder().id(item.getId()).domain(item.getDomain()).name(item.getName()).type(item.getDtype()).build()).toList();
        return AuthEmailListDto.Response.builder().authEmailInfoList(authEmailInfoList).build();
    }

    @Transactional
    public AuthEmailSaveDto.Response saveAuthEmail(AuthEmailSaveDto.Request request) {
        authEmailRepository.save(AuthEmail.builder().domain(request.getDomain()).name(request.getName()).dtype(request.getType()).build());
        return AuthEmailSaveDto.Response.builder().updatedAt(LocalDateTime.now()).build();
    }

    @Transactional
    public AuthEmailModifyDto.Response modifyAuthEmail(AuthEmailModifyDto.Request request) {
        AuthEmail authEmail = authEmailRepository.findById(request.getId()).orElseThrow(() -> new BusinessException(AuthEmailErrorCode.AUTH_EMAIL_NOT_FOUND));
        authEmail.modifyAuthEmail(request);
        return AuthEmailModifyDto.Response.builder().updatedAt(LocalDateTime.now()).build();
    }
}
