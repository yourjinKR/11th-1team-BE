package org.example.knockin.repository.member;

import java.util.List;
import java.util.Optional;

import org.example.knockin.dto.MyPreferencesAllDto;
import org.example.knockin.dto.MyProfileAllDto;
import org.example.knockin.entity.auth.LoginProviderType;
import org.example.knockin.entity.member.Member;
import org.example.knockin.entity.room.Region;
import org.example.knockin.entity.room.RoomProfile;
import org.example.knockin.entity.room.RoomSeekerProfile;
import org.example.knockin.global.auth.dto.AuthResponse;
import org.example.knockin.repository.member.row.MatchingBasicInfoRow;

public interface MemberRepositoryCustom {
    Optional<Member> findMemberByProvider(String providerId, LoginProviderType providerType);
    Optional<AuthResponse> findMemberInfo(Member member);
    Optional<Member> findByProviderId(String providerId);
    List<Member> findMemberByDelete();
    List<Member> findByProfile(Member memberEntity);
    List<MyProfileAllDto.Response.Lifestyle> findByLifePattern(Member memberEntity);
    Optional<RoomProfile> findByRoomProfile(Member memberEntity);
    List<MyProfileAllDto.Response.RoomProfile> findRoomTypes(RoomProfile profile);
    List<Region> findSeekerRegionEntities(RoomSeekerProfile seeker);
    List<MyPreferencesAllDto.Response.Lifestyle> findPreferenceLifeStyle(Member member);
    List<MyPreferencesAllDto.Response.Condition> findPreferenceCondition(Member member);
    List<MatchingBasicInfoRow> findMatchingBasicRow(List<Long> excludeMemberIds, Integer size);
    Optional<MatchingBasicInfoRow> findMatchingBasicRowById(Long memberId);
}
