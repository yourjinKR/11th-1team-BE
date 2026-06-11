package org.example.knockin.repository.member.impl;

import com.querydsl.core.types.Projections;
import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.jpa.JPAExpressions;
import com.querydsl.jpa.impl.JPAQueryFactory;
import lombok.RequiredArgsConstructor;
import org.example.knockin.dto.MyPreferencesAllDto;
import org.example.knockin.dto.MyProfileAllDto;
import org.example.knockin.entity.auth.LoginProviderType;
import org.example.knockin.entity.life.PreferenceConditionWeight;
import org.example.knockin.entity.member.Member;
import org.example.knockin.entity.room.Region;
import org.example.knockin.entity.room.RoomOfferProfile;
import org.example.knockin.entity.room.RoomProfile;
import org.example.knockin.entity.room.RoomSeekerProfile;
import org.example.knockin.global.auth.dto.AuthResponse;
import org.example.knockin.repository.member.MemberRepositoryCustom;
import org.springframework.stereotype.Repository;
import org.springframework.util.StringUtils;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.example.knockin.entity.member.QMember.member;
import static org.example.knockin.entity.life.QPreferenceCondition.preferenceCondition;
import static org.example.knockin.entity.life.QMemberLifePattern.memberLifePattern;
import static org.example.knockin.entity.life.QPreferenceConditionWeight.preferenceConditionWeight;
import static org.example.knockin.entity.member.QBasicInformation.basicInformation;
import static org.example.knockin.entity.life.QLifePatternInformation.lifePatternInformation;
import static org.example.knockin.entity.life.QLifePattern.lifePattern;
import static org.example.knockin.entity.file.QBasicInformationFile.basicInformationFile;
import static org.example.knockin.entity.file.QFile.file;
import static org.example.knockin.entity.room.QRoomProfile.roomProfile;
import static org.example.knockin.entity.room.QOfferRoomType.offerRoomType;
import static org.example.knockin.entity.room.QSeekerRoomType.seekerRoomType;
import static org.example.knockin.entity.room.QRoomType.roomType;
import static org.example.knockin.entity.room.QRegion.region;
import static org.example.knockin.entity.room.QRoomSeekerProfileRegion.roomSeekerProfileRegion;

@Repository
@RequiredArgsConstructor
public class MemberRepositoryImpl implements MemberRepositoryCustom {
    private final JPAQueryFactory jpaQueryFactory;

    @Override
    public Optional<Member> findMemberByProvider(String providerId, LoginProviderType providerType) {
        return Optional.ofNullable(jpaQueryFactory
                .selectFrom(member)
                .where(providerIdEq(providerId), providerTypeEq(providerType))
                .fetchOne());
    }

    public Optional<AuthResponse> findMemberInfo(Member memberEntity) {
        return Optional.ofNullable(jpaQueryFactory
                .select(Projections.fields(AuthResponse.class,
                        JPAExpressions.selectOne()
                                .from(memberLifePattern)
                                .where(memberLifePattern.member.eq(member))
                                .exists().as("basicInfo"),
                        JPAExpressions.selectOne()
                                .from(preferenceCondition)
                                .where(preferenceCondition.member.eq(member))
                                .exists()
                                .and(JPAExpressions.selectOne()
                                                .from(preferenceConditionWeight)
                                                .where(preferenceConditionWeight.member.eq(member))
                                                .exists())
                                .as("preferenceInfo"),
                        member.isDelete.as("isDelete")
                ))
                .from(member)
                .where(member.id.eq(memberEntity.getId()))
                .fetchOne());
    }

    @Override
    public List<Member> findByProfile(Member memberEntity) {
        return jpaQueryFactory.selectFrom(member)
                .leftJoin(basicInformation).on(basicInformation.member.eq(member))
                .leftJoin(basicInformationFile).on(basicInformationFile.basicInformation.eq(basicInformation))
                .leftJoin(basicInformationFile.file, file)
                .where(member.eq(memberEntity)).fetch();
    }

    @Override
    public List<MyProfileAllDto.Response.Lifestyle> findByLifePattern(Member memberEntity) {
        return jpaQueryFactory.select(Projections.fields(MyProfileAllDto.Response.Lifestyle.class,
                        memberLifePattern.id.as("id"),
                        lifePattern.id.as("lifestyleId"),
                        lifePattern.name.as("name"),
                        lifePatternInformation.dvalue.as("value"),
                        lifePatternInformation.description.as("description"),
                        lifePattern.dtype.as("type")
                ))
                .from(memberLifePattern)
                .leftJoin(memberLifePattern.lifePatternInformation, lifePatternInformation)
                .leftJoin(lifePatternInformation.lifePattern, lifePattern)
                .where(memberLifePattern.member.eq(memberEntity)).fetch();
    }

    @Override
    public Optional<RoomProfile> findByRoomProfile(Member memberEntity) {
        return Optional.ofNullable(jpaQueryFactory
                .selectFrom(roomProfile)
                .where(roomProfile.member.eq(memberEntity))
                .fetchOne());
    }

    @Override
    public List<MyProfileAllDto.Response.RoomProfile> findRoomTypes(RoomProfile profile) {
        if (profile instanceof RoomOfferProfile offer) {
            return jpaQueryFactory
                    .select(Projections.fields(MyProfileAllDto.Response.RoomProfile.class,
                            roomType.id.as("roomProfileId"),
                            roomType.name.as("roomProfileName")
                    ))
                    .from(offerRoomType)
                    .join(offerRoomType.roomType, roomType)
                    .where(offerRoomType.roomOfferProfile.eq(offer))
                    .fetch();

        } else if (profile instanceof RoomSeekerProfile seeker) {
            return jpaQueryFactory
                    .select(Projections.fields(MyProfileAllDto.Response.RoomProfile.class,
                            roomType.id.as("roomProfileId"),
                            roomType.name.as("roomProfileName")
                    ))
                    .from(seekerRoomType)
                    .join(seekerRoomType.roomType, roomType)
                    .where(seekerRoomType.roomSeekerProfile.eq(seeker))
                    .fetch();
        }

        return Collections.emptyList();
    }

    @Override
    public List<Region> findSeekerRegionEntities(RoomSeekerProfile seeker) {
        return jpaQueryFactory
                .select(region)
                .from(roomSeekerProfileRegion)
                .join(roomSeekerProfileRegion.region, region)
                .where(roomSeekerProfileRegion.roomSeekerProfile.eq(seeker))
                .fetch();
    }

    public List<MyPreferencesAllDto.Response.Lifestyle> findPreferenceLifeStyle(Member memberEntity) {
        return jpaQueryFactory.select(Projections.fields(MyPreferencesAllDto.Response.Lifestyle.class,
                        preferenceCondition.id.as("id"),
                        lifePattern.id.as("lifestyleId"),
                        lifePattern.name.as("name"),
                        lifePatternInformation.dvalue.as("value"),
                        lifePatternInformation.description.as("description"),
                        lifePattern.dtype.as("type")))
                .from(preferenceCondition)
                .join(preferenceCondition.lifePatternInformation, lifePatternInformation)
                .join(lifePatternInformation.lifePattern, lifePattern)
                .where(preferenceCondition.member.eq(memberEntity))
                .fetch();
    }

    public List<MyPreferencesAllDto.Response.Condition> findPreferenceCondition(Member memberEntity) {
        return jpaQueryFactory.select(Projections.fields(MyPreferencesAllDto.Response.Condition.class,
                        lifePattern.id.as("conditionsId"),
                        lifePattern.name.as("name")))
                .from(preferenceConditionWeight)
                .join(preferenceConditionWeight.lifePattern, lifePattern)
                .where(preferenceConditionWeight.member.eq(memberEntity))
                .fetch();
    }

    public Optional<Member> findByProviderId(String providerId) {
        return Optional.ofNullable(jpaQueryFactory.selectFrom(member).where(member.providerId.eq(providerId)).fetchOne());
    }

    public List<Member> findMemberByDelete() {
        return jpaQueryFactory.selectFrom(member).where(member.isDelete.eq(true)).fetch();
    }

    private BooleanExpression providerIdEq(String providerId) {
        return StringUtils.hasText(providerId) ? member.providerId.eq(providerId) : null;
    }

    private BooleanExpression providerTypeEq(LoginProviderType providerType) {
        return providerType != null ? member.providerType.eq(providerType) : null;
    }
}
