package org.example.knockin.service.impl;

import java.util.List;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import org.example.knockin.dto.RoommateRequestDto.RoommateMatchingRequiredInfo;
import org.example.knockin.entity.chat.ChattingRoom;
import org.example.knockin.entity.member.Member;
import org.example.knockin.entity.room.RoommateMatchingRequired;
import org.example.knockin.entity.room.RoommateRequiredStatus;
import org.example.knockin.exception.BusinessException;
import org.example.knockin.exception.RequiredErrorCode;
import org.example.knockin.repository.room.RoommateMatchingRequiredRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class RoommateMatchingRequiredServiceImpl {

    private final RoommateMatchingRequiredRepository roommateMatchingRequiredRepository;

    public List<RoommateMatchingRequiredInfo> findRequiredDto(ChattingRoom chattingRoom) {
        return roommateMatchingRequiredRepository.findRequiredDto(chattingRoom);
    }

    public Optional<RoommateMatchingRequired> findLatest(Long chatRoomId) {
        return roommateMatchingRequiredRepository.findLatest(chatRoomId);
    }

    public RoommateMatchingRequired findByIdOrThrow(Long requestId) {
        return roommateMatchingRequiredRepository.findById(requestId)
                .orElseThrow(() -> new BusinessException(RequiredErrorCode.ROOMMATE_NOT_FOUND));
    }

    public RoommateMatchingRequired savePending(Member requester, Member requestee, ChattingRoom chattingRoom) {
        RoommateMatchingRequired roommateMatchingRequired = RoommateMatchingRequired.builder()
                .requester(requester)
                .requestee(requestee)
                .chattingRoom(chattingRoom)
                .status(RoommateRequiredStatus.PENDING)
                .build();

        return roommateMatchingRequiredRepository.save(roommateMatchingRequired);
    }

    public Page<RoommateMatchingRequired> findByRequesterIdAndRequesteeId(Long requesterId, Long requesteeId, Pageable pageable) {
        return roommateMatchingRequiredRepository.findByRequesterIdAndRequesteeId(requesterId, requesteeId, pageable);
    }
}
