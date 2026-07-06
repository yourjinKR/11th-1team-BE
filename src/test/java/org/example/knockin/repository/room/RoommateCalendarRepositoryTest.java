package org.example.knockin.repository.room;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.tuple;

import jakarta.persistence.EntityManager;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import org.example.knockin.config.QueryDslConfig;
import org.example.knockin.entity.auth.LoginProviderType;
import org.example.knockin.entity.chat.ChattingRequired;
import org.example.knockin.entity.chat.ChattingRequiredStatus;
import org.example.knockin.entity.chat.ChattingRoom;
import org.example.knockin.entity.member.BasicInformation;
import org.example.knockin.entity.member.Gender;
import org.example.knockin.entity.member.Member;
import org.example.knockin.entity.member.MemberRole;
import org.example.knockin.entity.room.ExcludeRoommateCalendar;
import org.example.knockin.entity.room.MyRoommate;
import org.example.knockin.entity.room.RepeatRoommateCalendar;
import org.example.knockin.entity.room.RepeatType;
import org.example.knockin.entity.room.RoommateCalendar;
import org.example.knockin.entity.room.RoommateCalendarCategory;
import org.example.knockin.entity.room.RoommateCalendarMember;
import org.example.knockin.entity.room.RoommateMatchingRequired;
import org.example.knockin.entity.room.RoommateRequiredStatus;
import org.example.knockin.repository.room.row.DailyCalendarMemberRow;
import org.example.knockin.repository.room.row.DailyCalendarRow;
import org.example.knockin.repository.room.row.RepeatCalendarExcludeRow;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ActiveProfiles;

@DataJpaTest
@ActiveProfiles("test")
@Import(QueryDslConfig.class)
@DisplayName("룸메이트 캘린더 리포지토리")
class RoommateCalendarRepositoryTest {

    @Autowired
    private RoommateCalendarRepository roommateCalendarRepository;

    @Autowired
    private EntityManager entityManager;

    @Test
    @DisplayName("특정일 일정 후보 조회 시 일반 일정과 활성 반복 마스터와 담당자와 제외일을 벌크로 조회한다")
    void findDailyCalendarListReturnsBasicRepeatMembersAndExcludes() {
        // Given
        Member requester = persistMember("calendar-requester", "요청자");
        Member requestee = persistMember("calendar-requestee", "요청받은 사람");
        MyRoommate myRoommate = persistMyRoommate(requester, requestee);
        RoommateCalendarCategory category = persistCategory("청소");
        RoommateCalendar basicCalendar = persistCalendar(
                myRoommate,
                requester,
                category,
                "장보기",
                "저녁 재료",
                LocalDateTime.of(2026, 7, 12, 9, 0),
                LocalDateTime.of(2026, 7, 12, 10, 0)
        );
        RoommateCalendar repeatMaster = persistCalendar(
                myRoommate,
                requester,
                category,
                "청소",
                "거실 청소",
                LocalDateTime.of(2026, 7, 5, 14, 0),
                LocalDateTime.of(2026, 7, 5, 16, 0)
        );
        RepeatRoommateCalendar repeatCalendar = persistRepeatCalendar(
                repeatMaster,
                LocalDateTime.of(2026, 7, 26, 16, 0),
                RepeatType.WEEKLY
        );
        persistCalendarMember(basicCalendar, requester);
        persistCalendarMember(repeatMaster, requester);
        persistCalendarMember(repeatMaster, requestee);
        persistExclude(repeatCalendar, LocalDateTime.of(2026, 7, 12, 14, 0));
        entityManager.flush();
        entityManager.clear();

        LocalDateTime from = LocalDateTime.of(2026, 7, 12, 0, 0);
        LocalDateTime to = LocalDateTime.of(2026, 7, 13, 0, 0);

        // When
        List<DailyCalendarRow> calendars = roommateCalendarRepository.findDailyCalendarList(myRoommate.getId(), from, to);
        List<DailyCalendarMemberRow> members = roommateCalendarRepository.findDailyCalendarMembers(
                List.of(basicCalendar.getId(), repeatMaster.getId())
        );
        List<RepeatCalendarExcludeRow> excludes = roommateCalendarRepository.findRepeatCalendarExcludes(
                List.of(repeatCalendar.getId())
        );

        // Then
        assertThat(calendars)
                .extracting(DailyCalendarRow::calendarId, DailyCalendarRow::repeatCalendarId, DailyCalendarRow::repeatEndDate, DailyCalendarRow::repeatType)
                .containsExactly(
                        tuple(repeatMaster.getId(), repeatCalendar.getId(), LocalDateTime.of(2026, 7, 26, 16, 0), RepeatType.WEEKLY),
                        tuple(basicCalendar.getId(), null, null, null)
                );
        assertThat(members)
                .extracting(DailyCalendarMemberRow::calendarId, DailyCalendarMemberRow::memberId, DailyCalendarMemberRow::name)
                .containsExactly(
                        tuple(basicCalendar.getId(), requester.getId(), "요청자"),
                        tuple(repeatMaster.getId(), requester.getId(), "요청자"),
                        tuple(repeatMaster.getId(), requestee.getId(), "요청받은 사람")
                );
        assertThat(excludes)
                .extracting(RepeatCalendarExcludeRow::repeatCalendarId, RepeatCalendarExcludeRow::excludeAt)
                .containsExactly(tuple(repeatCalendar.getId(), LocalDateTime.of(2026, 7, 12, 14, 0)));
    }

    private Member persistMember(String providerId, String name) {
        Member member = Member.builder()
                .providerType(LoginProviderType.KAKAO)
                .providerId(providerId)
                .role(MemberRole.USER)
                .isDelete(false)
                .build();
        entityManager.persist(member);
        entityManager.persist(BasicInformation.builder()
                .member(member)
                .name(name)
                .birth(LocalDate.of(2000, 1, 1))
                .gender(Gender.MALE)
                .email(providerId + "@test.com")
                .build());
        return member;
    }

    private MyRoommate persistMyRoommate(Member requester, Member requestee) {
        ChattingRequired chattingRequired = ChattingRequired.builder()
                .requester(requester)
                .requestee(requestee)
                .status(ChattingRequiredStatus.ACCEPTED)
                .build();
        entityManager.persist(chattingRequired);

        ChattingRoom chattingRoom = ChattingRoom.builder()
                .chattingRequired(chattingRequired)
                .build();
        entityManager.persist(chattingRoom);

        RoommateMatchingRequired matchingRequired = RoommateMatchingRequired.builder()
                .requester(requester)
                .requestee(requestee)
                .chattingRoom(chattingRoom)
                .status(RoommateRequiredStatus.ACCEPTED)
                .build();
        entityManager.persist(matchingRequired);

        MyRoommate myRoommate = MyRoommate.builder()
                .roommateMatchingRequired(matchingRequired)
                .isDeleted(false)
                .build();
        entityManager.persist(myRoommate);
        return myRoommate;
    }

    private RoommateCalendarCategory persistCategory(String name) {
        RoommateCalendarCategory category = RoommateCalendarCategory.builder()
                .name(name)
                .isDeleted(false)
                .build();
        entityManager.persist(category);
        return category;
    }

    private RoommateCalendar persistCalendar(
            MyRoommate myRoommate,
            Member owner,
            RoommateCalendarCategory category,
            String title,
            String contents,
            LocalDateTime startDate,
            LocalDateTime endDate
    ) {
        RoommateCalendar calendar = RoommateCalendar.builder()
                .myRoommate(myRoommate)
                .member(owner)
                .roommateCalendarCategory(category)
                .title(title)
                .contents(contents)
                .startDate(startDate)
                .endDate(endDate)
                .isDeleted(false)
                .build();
        entityManager.persist(calendar);
        return calendar;
    }

    private RepeatRoommateCalendar persistRepeatCalendar(RoommateCalendar calendar, LocalDateTime endDate, RepeatType repeatType) {
        RepeatRoommateCalendar repeatCalendar = RepeatRoommateCalendar.builder()
                .roommateCalendar(calendar)
                .endDate(endDate)
                .repeatType(repeatType)
                .build();
        entityManager.persist(repeatCalendar);
        return repeatCalendar;
    }

    private void persistCalendarMember(RoommateCalendar calendar, Member member) {
        entityManager.persist(RoommateCalendarMember.of(calendar, member));
    }

    private void persistExclude(RepeatRoommateCalendar repeatCalendar, LocalDateTime excludeAt) {
        entityManager.persist(ExcludeRoommateCalendar.builder()
                .repeatRoommateCalendar(repeatCalendar)
                .excludeAt(excludeAt)
                .build());
    }
}
