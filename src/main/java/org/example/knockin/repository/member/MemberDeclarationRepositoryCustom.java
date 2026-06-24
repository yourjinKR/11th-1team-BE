package org.example.knockin.repository.member;

import org.example.knockin.dto.BoReportDoneListDto;
import org.example.knockin.dto.BoReportWaitListDto;
import org.springframework.data.domain.Pageable;

import java.util.List;

public interface MemberDeclarationRepositoryCustom {
    List<BoReportWaitListDto.Response.ReportInfo> findReportWaitList(Pageable pageable);
    List<BoReportDoneListDto.Response.ReportInfo> findReportDoneList(Pageable pageable);
}
