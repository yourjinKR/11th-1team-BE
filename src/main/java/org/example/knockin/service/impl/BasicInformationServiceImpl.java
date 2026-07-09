package org.example.knockin.service.impl;

import lombok.RequiredArgsConstructor;
import org.example.knockin.entity.member.BasicInformation;
import org.example.knockin.entity.member.Member;
import org.example.knockin.exception.BusinessException;
import org.example.knockin.exception.MemberErrorCode;
import org.example.knockin.repository.member.BasicInformationRepository;
import org.example.knockin.repository.member.row.ChattingRoomBasicInfoRow;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class BasicInformationServiceImpl {
    private final BasicInformationRepository basicInformationRepository;


    public List<BasicInformation> findByMember(Member member) {
        return basicInformationRepository.findByMember(member);
    }

    @Transactional
    public BasicInformation save(BasicInformation basicInformation) {
        return basicInformationRepository.save(basicInformation);
    }

    public ChattingRoomBasicInfoRow findChattingRoomBasicInfoRowByMemberId(Long memberId) {
        return basicInformationRepository.findChattingRoomBasicInfoRow(memberId)
                .orElseThrow(() -> new BusinessException(MemberErrorCode.BASIC_INFO_NOT_FOUND));
    }
}
