package org.example.knockin.service.impl;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import org.example.knockin.dto.CalendarDto;
import org.example.knockin.entity.member.Member;
import org.example.knockin.entity.room.MyRoommate;
import org.example.knockin.entity.room.RoommateCalendar;
import org.example.knockin.entity.room.RoommateCalendarCategory;
import org.example.knockin.entity.room.RoommateCalendarMember;
import org.example.knockin.entity.room.RoommateMatchingRequired;
import org.example.knockin.entity.room.RoommateRequiredStatus;
import org.example.knockin.global.exception.BusinessException;
import org.example.knockin.global.exception.MyRoommateErrorCode;
import org.example.knockin.repository.room.MyRoommateRepository;
import org.example.knockin.repository.room.RoommateCalendarCategoryRepository;
import org.example.knockin.repository.room.RoommateCalendarMemberRepository;
import org.example.knockin.repository.room.RoommateCalendarRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
@DisplayName("룸메이트 캘린더 서비스")
class CalendarServiceImplTest {

    @Mock
    private RoommateCalendarCategoryRepository roommateCalendarCategoryRepository;

    @Mock
    private MyRoommateRepository myRoommateRepository;

    @Mock
    private RoommateCalendarRepository roommateCalendarRepository;

    @Mock
    private RoommateCalendarMemberRepository roommateCalendarMemberRepository;

    @InjectMocks
    private CalendarServiceImpl calendarService;

    @Test
    @DisplayName("내 룸메이트가 있으면 카테고리와 캘린더와 담당자를 저장하고 수정 시간을 반환한다")
    void saveCalendarSavesCategoryCalendarAndMembersWhenMyRoommateExists() {
        // Given
        Long requesterId = 1L;
        Long requesteeId = 2L;
        MyRoommate myRoommate = myRoommate(10L, requesterId, requesteeId);
        CalendarDto.Request request = calendarRequest(
                "장보기",
                "저녁 재료 사오기",
                LocalDateTime.of(2026, 7, 4, 10, 0),
                LocalDateTime.of(2026, 7, 4, 11, 0),
                "생활",
                List.of(requesterId, requesteeId)
        );

        given(myRoommateRepository.findWithFetchedByMemberId(requesterId)).willReturn(Optional.of(myRoommate));
        given(roommateCalendarCategoryRepository.save(any(RoommateCalendarCategory.class)))
                .willAnswer(invocation -> invocation.getArgument(0));
        given(roommateCalendarRepository.save(any(RoommateCalendar.class)))
                .willAnswer(invocation -> savedCalendar(100L, invocation.getArgument(0)));

        // When
        CalendarDto.Response response = calendarService.saveCalendar(requesterId, request);

        // Then
        ArgumentCaptor<RoommateCalendarCategory> categoryCaptor = ArgumentCaptor.forClass(RoommateCalendarCategory.class);
        verify(roommateCalendarCategoryRepository).save(categoryCaptor.capture());
        assertThat(categoryCaptor.getValue().getName()).isEqualTo("생활");

        ArgumentCaptor<RoommateCalendar> calendarCaptor = ArgumentCaptor.forClass(RoommateCalendar.class);
        verify(roommateCalendarRepository).save(calendarCaptor.capture());
        RoommateCalendar savedCalendar = calendarCaptor.getValue();
        assertThat(savedCalendar.getMyRoommate()).isSameAs(myRoommate);
        assertThat(savedCalendar.getMember()).isSameAs(myRoommate.getRoommateMatchingRequired().getRequester());
        assertThat(savedCalendar.getRoommateCalendarCategory().getName()).isEqualTo("생활");
        assertThat(savedCalendar.getTitle()).isEqualTo("장보기");
        assertThat(savedCalendar.getContents()).isEqualTo("저녁 재료 사오기");
        assertThat(savedCalendar.getStartDate()).isEqualTo(LocalDateTime.of(2026, 7, 4, 10, 0));
        assertThat(savedCalendar.getEndDate()).isEqualTo(LocalDateTime.of(2026, 7, 4, 11, 0));

        @SuppressWarnings("unchecked")
        ArgumentCaptor<List<RoommateCalendarMember>> membersCaptor = ArgumentCaptor.forClass(List.class);
        verify(roommateCalendarMemberRepository).saveAll(membersCaptor.capture());
        assertThat(membersCaptor.getValue())
                .extracting(member -> member.getMember().getId())
                .containsExactly(requesterId, requesteeId);
        assertThat(membersCaptor.getValue())
                .extracting(member -> member.getRoommateCalendar().getId())
                .containsExactly(100L, 100L);
        assertThat(membersCaptor.getValue())
                .extracting(member -> member.getId().getMemberId())
                .containsExactly(requesterId, requesteeId);
        assertThat(membersCaptor.getValue())
                .extracting(member -> member.getId().getRoommateCalendarId())
                .containsExactly(100L, 100L);
        assertThat(response.getUpdatedAt()).isNotNull();
    }

    @Test
    @DisplayName("캘린더 저장 시 내 룸메이트가 없으면 룸메이트 없음 예외를 던지고 저장하지 않는다")
    void saveCalendarThrowsWhenMyRoommateDoesNotExist() {
        // Given
        Long memberId = 1L;
        given(myRoommateRepository.findWithFetchedByMemberId(memberId)).willReturn(Optional.empty());

        // When & Then
        assertThatThrownBy(() -> calendarService.saveCalendar(memberId, calendarRequest(
                "장보기",
                "저녁 재료 사오기",
                LocalDateTime.of(2026, 7, 4, 10, 0),
                LocalDateTime.of(2026, 7, 4, 11, 0),
                "생활",
                List.of(memberId)
        )))
                .isInstanceOfSatisfying(BusinessException.class,
                        exception -> assertThat(exception.getErrorCode()).isEqualTo(MyRoommateErrorCode.NOT_FOUND));
        verifyNoInteractions(roommateCalendarCategoryRepository, roommateCalendarRepository);
        verify(roommateCalendarMemberRepository, never()).saveAll(any());
    }

    private CalendarDto.Request calendarRequest(
            String title,
            String contents,
            LocalDateTime startDate,
            LocalDateTime endDate,
            String categoryName,
            List<Long> memberIds
    ) {
        CalendarDto.Request request = new CalendarDto.Request();
        request.setTitle(title);
        request.setContents(contents);
        request.setStartDate(startDate);
        request.setEndDate(endDate);
        request.setCategoryName(categoryName);
        request.setMembers(memberIds.stream()
                .map(this::calendarMember)
                .toList());
        return request;
    }

    private CalendarDto.CalendarMemberDto calendarMember(Long memberId) {
        CalendarDto.CalendarMemberDto memberDto = new CalendarDto.CalendarMemberDto();
        memberDto.setMemberId(memberId);
        return memberDto;
    }

    private MyRoommate myRoommate(Long id, Long requesterId, Long requesteeId) {
        RoommateMatchingRequired matchingRequired = RoommateMatchingRequired.builder()
                .requester(member(requesterId))
                .requestee(member(requesteeId))
                .status(RoommateRequiredStatus.ACCEPTED)
                .build();

        return MyRoommate.builder()
                .id(id)
                .roommateMatchingRequired(matchingRequired)
                .isDeleted(false)
                .build();
    }

    private Member member(Long id) {
        return Member.builder().id(id).build();
    }

    private RoommateCalendar savedCalendar(Long id, RoommateCalendar calendar) {
        return RoommateCalendar.builder()
                .id(id)
                .myRoommate(calendar.getMyRoommate())
                .member(calendar.getMember())
                .roommateCalendarCategory(calendar.getRoommateCalendarCategory())
                .title(calendar.getTitle())
                .contents(calendar.getContents())
                .startDate(calendar.getStartDate())
                .endDate(calendar.getEndDate())
                .isDeleted(calendar.getIsDeleted())
                .build();
    }
}
