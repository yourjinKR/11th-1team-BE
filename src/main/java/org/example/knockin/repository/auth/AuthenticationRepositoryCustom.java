package org.example.knockin.repository.auth;

import java.util.List;

import org.example.knockin.dto.BoVerificationApproveListDto;
import org.example.knockin.dto.BoVerificationCancelListDto;
import org.example.knockin.dto.BoVerificationWaitingDetailDto;
import org.example.knockin.dto.BoVerificationWaitingListDto;
import org.example.knockin.entity.auth.AuthenticationType;
import org.springframework.data.domain.Pageable;

public interface AuthenticationRepositoryCustom {
    List<AuthenticationType> getAcceptedAuthenticationTypeByMemberId(Long memberId);

    List<BoVerificationApproveListDto.Response.EmployeeAuthItem> findVerificationApproves(Pageable pageable);

    List<BoVerificationCancelListDto.Response.EmployeeAuthItem> findVerificationCancels(Pageable pageable);

    List<BoVerificationWaitingListDto.Response.EmployeeAuthItem> findVerificationsList(Pageable pageable);

    BoVerificationWaitingDetailDto.Response findVerifications(Long id);
}
