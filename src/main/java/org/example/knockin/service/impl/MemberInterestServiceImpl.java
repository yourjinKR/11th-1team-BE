package org.example.knockin.service.impl;

import java.util.List;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import org.example.knockin.entity.member.Member;
import org.example.knockin.entity.member.MemberInterest;
import org.example.knockin.repository.member.MemberInterestRepository;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class MemberInterestServiceImpl {

    private final MemberInterestRepository memberInterestRepository;

    public List<Long> findActiveReceiverIdsBySenderIdAndReceiverIds(Long senderId, List<Long> receiverIds) {
        return memberInterestRepository.findActiveReceiverIdsBySenderIdAndReceiverIds(senderId, receiverIds);
    }

    public boolean existsBySenderIdAndReceiverId(Long senderId, Long receiverId) {
        return memberInterestRepository.existsBySenderIdAndReceiverId(senderId, receiverId);
    }

    public MemberInterest toggle(Member sender, Member receiver) {
        Optional<MemberInterest> optionalMemberInterest =
                memberInterestRepository.findBySenderIdAndReceiverIdForUpdate(sender.getId(), receiver.getId());

        return optionalMemberInterest.map(memberInterest -> {
            memberInterest.toggle();
            return memberInterest;
        }).orElseGet(() -> save(sender, receiver));
    }

    private MemberInterest save(Member sender, Member receiver) {
        MemberInterest memberInterest = MemberInterest.builder()
                .sender(sender)
                .receiver(receiver)
                .build();
        return memberInterestRepository.save(memberInterest);
    }
}
