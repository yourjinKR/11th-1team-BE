package org.example.knockin.service.impl;

import lombok.RequiredArgsConstructor;
import org.example.knockin.dto.BoReportDoneListDto;
import org.example.knockin.dto.BoReportWaitListDto;
import org.example.knockin.entity.member.Member;
import org.example.knockin.entity.member.MemberDeclaration;
import org.example.knockin.exception.MemberErrorCode;
import org.example.knockin.exception.BusinessException;
import org.example.knockin.exception.DeclarationErrorCode;
import org.example.knockin.global.entity.DeclarationType;
import org.example.knockin.global.util.ReportType;
import org.example.knockin.repository.board.RoommateBoardDeclarationRepository;
import org.example.knockin.repository.member.MemberDeclarationRepository;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Comparator;
import java.util.List;
import java.util.stream.Stream;

@Service
@RequiredArgsConstructor
public class DeclarationServiceImpl {
    private final MemberDeclarationRepository memberDeclarationRepository;
    private final RoommateBoardDeclarationRepository roommateBoardDeclarationRepository;

    public List<BoReportWaitListDto.Response.ReportInfo> findReportWaitList(Pageable pageable) {
        List<BoReportWaitListDto.Response.ReportInfo> memberReports = memberDeclarationRepository.findReportWaitList(pageable);
        List<BoReportWaitListDto.Response.ReportInfo> boardReports = roommateBoardDeclarationRepository.findReportWaitList(pageable);

        return Stream.concat(memberReports.stream(), boardReports.stream()).sorted(Comparator.comparing(BoReportWaitListDto.Response.ReportInfo::getCreatedAt).reversed()).toList();
    }

    public List<BoReportDoneListDto.Response.ReportInfo> findReportDoneList(Pageable pageable) {
        List<BoReportDoneListDto.Response.ReportInfo> memberReports = memberDeclarationRepository.findReportDoneList(pageable);
        List<BoReportDoneListDto.Response.ReportInfo> boardReports = roommateBoardDeclarationRepository.findReportDoneList(pageable);

        return Stream.concat(memberReports.stream(), boardReports.stream()).sorted(Comparator.comparing(BoReportDoneListDto.Response.ReportInfo::getCreatedAt).reversed()).toList();
    }

    @Transactional
    public MemberDeclaration reportMember(Member reporter, Member reported, String reason) {
        if (memberDeclarationRepository.existsByReporterAndReported(reporter, reported)) {
            throw new BusinessException(MemberErrorCode.DECLARATION_DUPLICATE);
        }

        MemberDeclaration memberDeclaration = MemberDeclaration.builder()
                .reporter(reporter)
                .reported(reported)
                .reason(reason)
                .declarationType(DeclarationType.PENDING)
                .build();

        return memberDeclarationRepository.save(memberDeclaration);
    }

    @Transactional
    public void reportHidden(Long id, ReportType type, String reason) {
        switch (type) {
            case BOARD -> roommateBoardDeclarationRepository.findById(id).orElseThrow(() -> new BusinessException(DeclarationErrorCode.DECLARATION_NOT_FOUND)).changeDeclarationType(DeclarationType.HIDDEN);
            case MEMBER -> memberDeclarationRepository.findById(id).orElseThrow(() -> new BusinessException(DeclarationErrorCode.DECLARATION_NOT_FOUND)).changeDeclarationType(DeclarationType.HIDDEN);
        }
    }

    @Transactional
    public void reportNoAction(Long id, ReportType type) {
        switch (type) {
            case BOARD -> roommateBoardDeclarationRepository.findById(id).orElseThrow(() -> new BusinessException(DeclarationErrorCode.DECLARATION_NOT_FOUND)).changeDeclarationType(DeclarationType.NOACTION);
            case MEMBER -> memberDeclarationRepository.findById(id).orElseThrow(() -> new BusinessException(DeclarationErrorCode.DECLARATION_NOT_FOUND)).changeDeclarationType(DeclarationType.NOACTION);
        }
    }

    @Transactional
    public void reportSuspended(Long id, ReportType type, String reason) {
        switch (type) {
            case BOARD -> roommateBoardDeclarationRepository.findById(id).orElseThrow(() -> new BusinessException(DeclarationErrorCode.DECLARATION_NOT_FOUND)).changeDeclarationType(DeclarationType.SUSPENDED, reason);
            case MEMBER -> memberDeclarationRepository.findById(id).orElseThrow(() -> new BusinessException(DeclarationErrorCode.DECLARATION_NOT_FOUND)).changeDeclarationType(DeclarationType.SUSPENDED, reason);
        }
    }
}
