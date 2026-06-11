package org.example.knockin.repository.member;

import java.util.List;
import java.util.Optional;

import org.example.knockin.dto.MyPreferencesAllDto;
import org.example.knockin.dto.MyProfileAllDto;
import org.example.knockin.entity.auth.LoginProviderType;
import org.example.knockin.entity.life.PreferenceConditionWeight;
import org.example.knockin.entity.member.Member;
import org.example.knockin.entity.room.Region;
import org.example.knockin.entity.room.RoomProfile;
import org.example.knockin.entity.room.RoomSeekerProfile;
import org.example.knockin.global.auth.dto.AuthResponse;

import static org.example.knockin.entity.file.QBasicInformationFile.basicInformationFile;
import static org.example.knockin.entity.file.QFile.file;
import static org.example.knockin.entity.life.QLifePattern.lifePattern;
import static org.example.knockin.entity.life.QLifePatternInformation.lifePatternInformation;
import static org.example.knockin.entity.life.QMemberLifePattern.memberLifePattern;
import static org.example.knockin.entity.member.QBasicInformation.basicInformation;
import static org.example.knockin.entity.member.QMember.member;

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
}
