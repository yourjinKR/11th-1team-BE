package org.example.knockin.service;

import org.example.knockin.dto.MatchDetailDto;
import org.example.knockin.dto.MatchDto;
import org.example.knockin.dto.MatchListDto;
import org.example.knockin.dto.MatchListDto.Response;
import org.example.knockin.dto.MemberReportDto;
import org.springframework.data.domain.Slice;

public interface RoommateMatchingService {
    Slice<Response> findMatchingList(Long memberId, MatchListDto.Request request);
    MatchDetailDto.Response findMatchingDetail(Long targetMemberId, Long requesterId);

    MatchDto.Response likeMatching(Long senderId, Long receiverId);

    MemberReportDto.Response reportMatching(Long reporterId, Long reportedId, MemberReportDto.Request request);
}
