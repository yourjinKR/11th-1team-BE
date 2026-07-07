package org.example.knockin.service.impl;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.inOrder;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.when;

import java.io.IOException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import org.example.knockin.dto.ChatMessageDto;
import org.example.knockin.dto.ChatRoomCreateDto;
import org.example.knockin.dto.ChatRoomDetailDto;
import org.example.knockin.dto.ChatRoomDto;
import org.example.knockin.dto.ChatRoomImageDto;
import org.example.knockin.dto.ChatRoomLeftEvent;
import org.example.knockin.dto.ChatRoomListDto;
import org.example.knockin.dto.ChatRoomMessageEvent;
import org.example.knockin.dto.ChatSocketResponse;
import org.example.knockin.dto.EventType;
import org.example.knockin.dto.MessageType;
import org.example.knockin.dto.RoommateRequestDto.RoommateMatchingRequiredInfo;
import org.example.knockin.entity.board.RoommateBoard;
import org.example.knockin.entity.chat.ChatRoomFile;
import org.example.knockin.entity.chat.ChatRoomMember;
import org.example.knockin.entity.chat.ChatRoomMessage;
import org.example.knockin.entity.chat.ChattingRequired;
import org.example.knockin.entity.chat.ChattingRequiredStatus;
import org.example.knockin.entity.chat.ChattingRoom;
import org.example.knockin.entity.chat.ChattingScore;
import org.example.knockin.entity.file.File;
import org.example.knockin.entity.file.FileType;
import org.example.knockin.entity.member.Gender;
import org.example.knockin.entity.member.Member;
import org.example.knockin.entity.room.RoommateRequiredStatus;
import org.example.knockin.global.exception.BusinessException;
import org.example.knockin.global.exception.ChattingErrorCode;
import org.example.knockin.global.exception.FileErrorCode;
import org.example.knockin.global.exception.MemberErrorCode;
import org.example.knockin.global.exception.RoommateBoardErrorCode;
import org.example.knockin.global.util.DateUtils;
import org.example.knockin.repository.board.RoommateBoardRepository;
import org.example.knockin.repository.chat.ChatRoomFileRepository;
import org.example.knockin.repository.chat.ChatRoomMemberRepository;
import org.example.knockin.repository.chat.ChatRoomMessageRepository;
import org.example.knockin.repository.chat.ChattingRequiredRepository;
import org.example.knockin.repository.chat.ChattingRoomRepository;
import org.example.knockin.repository.chat.ChattingScoreRepository;
import org.example.knockin.repository.file.FileRepository;
import org.example.knockin.repository.member.BasicInformationRepository;
import org.example.knockin.repository.member.MemberRepository;
import org.example.knockin.repository.member.row.ChattingRoomBasicInfoRow;
import org.example.knockin.repository.room.RoommateMatchingRequiredRepository;
import org.example.knockin.service.FileService;
import org.example.knockin.service.RoommateScoreService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InOrder;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.messaging.simp.SimpMessageSendingOperations;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.web.multipart.MultipartFile;

@ExtendWith(MockitoExtension.class)
@DisplayName("채팅 서비스")
class ChatServiceImplTest {

    @Mock
    private ChattingRoomRepository chattingRoomRepository;

    @Mock
    private ChatRoomMemberRepository chatRoomMemberRepository;

    @Mock
    private SimpMessageSendingOperations messagingTemplate;

    @Mock
    private ApplicationEventPublisher publisher;

    @Mock
    private ChatRoomAccessService chatRoomAccessService;

    @Mock
    private ChatRoomMessageRepository chatRoomMessageRepository;

    @Mock
    private FileService fileService;

    @Mock
    private FileRepository fileRepository;

    @Mock
    private ChatRoomFileRepository chatRoomFileRepository;

    @Mock
    private BasicInformationRepository basicInformationRepository;

    @Mock
    private RoommateMatchingRequiredRepository roommateMatchingRequiredRepository;

    @Mock
    private RoommateBoardRepository roommateBoardRepository;

    @Mock
    private ChattingRequiredRepository chattingRequiredRepository;

    @Mock
    private MemberRepository memberRepository;

    @Mock
    private RoommateScoreService roommateScoreService;

    @Mock
    private ChattingScoreRepository chattingScoreRepository;

    @InjectMocks
    private ChatServiceImpl chatService;

    @Test
    @DisplayName("회원 식별자로 채팅방 목록을 조회하고 결과를 그대로 반환한다")
    void getChatRoomListReturnsRoomsByMemberId() {
        // Given
        Long memberId = 1L;
        List<ChatRoomListDto.Response> chatRooms = List.of(
                chatRoom(10L, "상대방A", "profile-a.jpg", LocalDateTime.of(2026, 6, 18, 10, 0), ChattingRequiredStatus.ACCEPTED),
                chatRoom(20L, "상대방B", "profile-b.jpg", LocalDateTime.of(2026, 6, 18, 11, 0), ChattingRequiredStatus.PENDING)
        );
        when(chattingRoomRepository.findByMemberId(memberId)).thenReturn(chatRooms);

        // When
        List<ChatRoomListDto.Response> responses = chatService.getChatRoomList(memberId);

        // Then
        assertThat(responses).isSameAs(chatRooms);
        assertThat(responses).extracting(ChatRoomListDto.Response::getChatRoomId)
                .containsExactly(10L, 20L);
        assertThat(responses).extracting(ChatRoomListDto.Response::getMemberName)
                .containsExactly("상대방A", "상대방B");
        assertThat(responses).extracting(ChatRoomListDto.Response::getMemberProfileImageUrl)
                .containsExactly("profile-a.jpg", "profile-b.jpg");
        assertThat(responses).extracting(ChatRoomListDto.Response::getStatus)
                .containsExactly(ChattingRequiredStatus.ACCEPTED, ChattingRequiredStatus.PENDING);
        verify(chattingRoomRepository).findByMemberId(memberId);
    }

    @Test
    @DisplayName("참여한 채팅방이 없으면 빈 목록을 반환한다")
    void getChatRoomListReturnsEmptyListWhenMemberHasNoRooms() {
        // Given
        Long memberId = 1L;
        when(chattingRoomRepository.findByMemberId(memberId)).thenReturn(List.of());

        // When
        List<ChatRoomListDto.Response> responses = chatService.getChatRoomList(memberId);

        // Then
        assertThat(responses).isEmpty();
        verify(chattingRoomRepository).findByMemberId(memberId);
    }

    @Test
    @DisplayName("채팅방 상세 조회 시 상대 프로필, 메시지 목록, 룸메이트 요청 목록을 반환한다")
    void getChatRoomDetailReturnsProfileMessagesAndRoommateRequests() {
        // Given
        Long chatRoomId = 10L;
        Long memberId = 1L;
        LocalDate opponentBirth = LocalDate.now().minusYears(25);
        ChattingRoom chattingRoom = chattingRoom();
        Member me = member(memberId);
        Member opponent = member(2L);
        ChatRoomMember roomMember = activeRoomMember(me, chattingRoom);
        List<ChatRoomDetailDto.ChatMessage> messages = List.of(
                new ChatRoomDetailDto.ChatMessage(
                        100L,
                        memberId,
                        "안녕하세요",
                        LocalDateTime.of(2026, 6, 23, 10, 0),
                        MessageType.TEXT,
                        null
                )
        );
        List<RoommateMatchingRequiredInfo> matchingRequiredList = List.of(
                RoommateMatchingRequiredInfo.builder()
                        .requiredId(200L)
                        .requesterMemberId(memberId)
                        .requesteeMemberId(opponent.getId())
                        .status(RoommateRequiredStatus.PENDING)
                        .createdAt(LocalDateTime.of(2026, 6, 23, 10, 30))
                        .updatedAt(LocalDateTime.of(2026, 6, 23, 10, 30))
                        .build()
        );
        when(chattingRoomRepository.findById(chatRoomId)).thenReturn(Optional.of(chattingRoom));
        when(chatRoomMemberRepository.findActiveMemberByRoomIdAndMemberId(chatRoomId, memberId))
                .thenReturn(Optional.of(roomMember));
        when(chatRoomMemberRepository.findPartnerMember(roomMember, chatRoomId)).thenReturn(opponent);
        when(basicInformationRepository.findChattingRoomBasicInfoRow(opponent.getId()))
                .thenReturn(Optional.of(new ChattingRoomBasicInfoRow(
                        opponent.getId(),
                        "상대방",
                        opponentBirth,
                        Gender.FEMALE,
                        "opponent-profile.jpg"
                )));
        when(chatRoomMessageRepository.findChatMessageDto(chatRoomId)).thenReturn(messages);
        when(roommateMatchingRequiredRepository.findRequiredDto(chattingRoom)).thenReturn(matchingRequiredList);
        when(roommateScoreService.calculateSimpleScore(memberId, opponent.getId())).thenReturn(100);

        // When
        ChatRoomDetailDto.Response response = chatService.getChatRoomDetail(chatRoomId, memberId);

        // Then
        assertThat(response.getOpponentProfile().getId()).isEqualTo(opponent.getId());
        assertThat(response.getOpponentProfile().getName()).isEqualTo("상대방");
        assertThat(response.getOpponentProfile().getAge()).isEqualTo(DateUtils.calculateAge(opponentBirth));
        assertThat(response.getOpponentProfile().getGender()).isEqualTo(Gender.FEMALE);
        assertThat(response.getOpponentProfile().getMemberProfileImageUrl()).isEqualTo("opponent-profile.jpg");
        assertThat(response.getOpponentProfile().getScore()).isEqualTo(100);
        assertThat(response.getMessages()).isSameAs(messages);
        assertThat(response.getMatchingRequiredList()).isSameAs(matchingRequiredList);
    }

    @Test
    @DisplayName("채팅방 상세 조회 시 활성 채팅방 멤버가 아니면 실패한다")
    void getChatRoomDetailRejectsMemberWhoIsNotActiveRoomMember() {
        // Given
        Long chatRoomId = 10L;
        Long memberId = 1L;
        ChattingRoom chattingRoom = chattingRoom();
        when(chattingRoomRepository.findById(chatRoomId)).thenReturn(Optional.of(chattingRoom));
        when(chatRoomMemberRepository.findActiveMemberByRoomIdAndMemberId(chatRoomId, memberId))
                .thenReturn(Optional.empty());

        // When & Then
        assertThatThrownBy(() -> chatService.getChatRoomDetail(chatRoomId, memberId))
                .isInstanceOfSatisfying(BusinessException.class,
                        exception -> assertThat(exception.getErrorCode()).isEqualTo(ChattingErrorCode.ROOM_MEMBER_NOT_FOUND));
        verify(chatRoomMemberRepository, never()).findPartnerMember(any(), eq(chatRoomId));
        verifyNoInteractions(basicInformationRepository, chatRoomMessageRepository, roommateMatchingRequiredRepository);
    }

    @Test
    @DisplayName("채팅방 상세 조회 시 상대방 기본 정보가 없으면 실패한다")
    void getChatRoomDetailRejectsWhenOpponentBasicInformationMissing() {
        // Given
        Long chatRoomId = 10L;
        Long memberId = 1L;
        ChattingRoom chattingRoom = chattingRoom();
        Member me = member(memberId);
        Member opponent = member(2L);
        ChatRoomMember roomMember = activeRoomMember(me, chattingRoom);
        when(chattingRoomRepository.findById(chatRoomId)).thenReturn(Optional.of(chattingRoom));
        when(chatRoomMemberRepository.findActiveMemberByRoomIdAndMemberId(chatRoomId, memberId))
                .thenReturn(Optional.of(roomMember));
        when(chatRoomMemberRepository.findPartnerMember(roomMember, chatRoomId)).thenReturn(opponent);
        when(basicInformationRepository.findChattingRoomBasicInfoRow(opponent.getId())).thenReturn(Optional.empty());

        // When & Then
        assertThatThrownBy(() -> chatService.getChatRoomDetail(chatRoomId, memberId))
                .isInstanceOfSatisfying(BusinessException.class,
                        exception -> assertThat(exception.getErrorCode()).isEqualTo(MemberErrorCode.BASIC_INFO_NOT_FOUND));
        verifyNoInteractions(chatRoomMessageRepository, roommateMatchingRequiredRepository);
    }

    @Test
    @DisplayName("채팅방 생성 요청이 유효하면 승인된 요청과 채팅방과 첫 메시지를 저장한다")
    void createChattingRoomCreatesAcceptedRequestRoomMembersAndFirstMessage() {
        // Given
        Long requesterId = 1L;
        Long requesteeId = 2L;
        Long boardId = 10L;
        Member requester = member(requesterId);
        Member requestee = member(requesteeId);
        RoommateBoard roommateBoard = RoommateBoard.builder().id(boardId).build();
        ChatRoomCreateDto.Request request = chatRoomCreateRequest(requesteeId, boardId, "안녕하세요");
        LocalDateTime messageCreatedAt = LocalDateTime.of(2026, 6, 24, 10, 0);

        when(memberRepository.findById(requesterId)).thenReturn(Optional.of(requester));
        when(memberRepository.findById(requesteeId)).thenReturn(Optional.of(requestee));
        when(chattingRoomRepository.existsActiveRoomBetweenMembers(requesterId, requesteeId)).thenReturn(false);
        when(chattingRoomRepository.countActiveRoomsByMemberId(requesterId)).thenReturn(14L);
        when(chattingRoomRepository.countActiveRoomsByMemberId(requesteeId)).thenReturn(0L);
        when(roommateBoardRepository.findById(boardId)).thenReturn(Optional.of(roommateBoard));
        when(chattingRequiredRepository.save(any(ChattingRequired.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));
        when(chattingRoomRepository.save(any(ChattingRoom.class)))
                .thenAnswer(invocation -> persistedChattingRoom(invocation.getArgument(0), 100L));
        when(chatRoomMemberRepository.saveAll(any()))
                .thenAnswer(invocation -> invocation.getArgument(0));
        when(chatRoomMessageRepository.save(any(ChatRoomMessage.class)))
                .thenAnswer(invocation -> persistedMessage(invocation.getArgument(0), messageCreatedAt));
        List<ChattingScore> chattingScores = List.of(ChattingScore.builder().score(80).build());
        when(roommateScoreService.createChattingScores(any(ChattingRequired.class))).thenReturn(chattingScores);

        // When
        ChatRoomCreateDto.Response response = chatService.createChattingRoom(requesterId, request);

        // Then
        assertThat(response.getChatRoomId()).isEqualTo(100L);
        assertThat(response.getUpdatedAt()).isEqualTo(messageCreatedAt);

        ArgumentCaptor<ChattingRequired> requiredCaptor = ArgumentCaptor.forClass(ChattingRequired.class);
        verify(chattingRequiredRepository).save(requiredCaptor.capture());
        assertThat(requiredCaptor.getValue().getRequester()).isSameAs(requester);
        assertThat(requiredCaptor.getValue().getRequestee()).isSameAs(requestee);
        assertThat(requiredCaptor.getValue().getRoommateBoard()).isSameAs(roommateBoard);
        assertThat(requiredCaptor.getValue().getStatus()).isEqualTo(ChattingRequiredStatus.ACCEPTED);

        ArgumentCaptor<ChattingRoom> roomCaptor = ArgumentCaptor.forClass(ChattingRoom.class);
        verify(chattingRoomRepository).save(roomCaptor.capture());
        assertThat(roomCaptor.getValue().getChattingRequired()).isSameAs(requiredCaptor.getValue());

        @SuppressWarnings("unchecked")
        ArgumentCaptor<Iterable<ChatRoomMember>> membersCaptor = ArgumentCaptor.forClass(Iterable.class);
        verify(chatRoomMemberRepository).saveAll(membersCaptor.capture());
        List<ChatRoomMember> members = ((List<ChatRoomMember>) membersCaptor.getValue());
        assertThat(members).hasSize(2);
        assertThat(members).extracting(ChatRoomMember::getMember).containsExactly(requester, requestee);
        assertThat(members).extracting(ChatRoomMember::getIsLeft).containsExactly(false, false);

        ArgumentCaptor<ChatRoomMessage> messageCaptor = ArgumentCaptor.forClass(ChatRoomMessage.class);
        verify(chatRoomMessageRepository).save(messageCaptor.capture());
        assertThat(messageCaptor.getValue().getContents()).isEqualTo("안녕하세요");
        assertThat(messageCaptor.getValue().getMember()).isSameAs(requester);
        assertThat(messageCaptor.getValue().getChattingRoom().getId()).isEqualTo(100L);
        assertThat(messageCaptor.getValue().getType()).isEqualTo(MessageType.TEXT);
        verify(roommateScoreService).createChattingScores(requiredCaptor.getValue());
        verify(chattingScoreRepository).saveAll(chattingScores);
    }

    @Test
    @DisplayName("두 회원 사이에 활성 채팅방이 이미 있으면 채팅방을 생성하지 않는다")
    void createChattingRoomRejectsDuplicateActiveRoom() {
        // Given
        Long requesterId = 1L;
        Long requesteeId = 2L;
        Member requester = member(requesterId);
        Member requestee = member(requesteeId);
        ChatRoomCreateDto.Request request = chatRoomCreateRequest(requesteeId, null, "안녕하세요");

        when(memberRepository.findById(requesterId)).thenReturn(Optional.of(requester));
        when(memberRepository.findById(requesteeId)).thenReturn(Optional.of(requestee));
        when(chattingRoomRepository.existsActiveRoomBetweenMembers(requesterId, requesteeId)).thenReturn(true);

        // When & Then
        assertThatThrownBy(() -> chatService.createChattingRoom(requesterId, request))
                .isInstanceOfSatisfying(BusinessException.class,
                        exception -> assertThat(exception.getErrorCode()).isEqualTo(ChattingErrorCode.ROOM_DUPLICATE));
        verify(chattingRoomRepository, never()).countActiveRoomsByMemberId(any());
        verifyNoInteractions(roommateBoardRepository, chattingRequiredRepository, chatRoomMemberRepository, chatRoomMessageRepository);
    }

    @Test
    @DisplayName("요청자나 피요청자의 활성 채팅방이 15개 이상이면 채팅방을 생성하지 않는다")
    void createChattingRoomRejectsMemberOverRoomLimit() {
        // Given
        Long requesterId = 1L;
        Long requesteeId = 2L;
        Member requester = member(requesterId);
        Member requestee = member(requesteeId);
        ChatRoomCreateDto.Request request = chatRoomCreateRequest(requesteeId, null, "안녕하세요");

        when(memberRepository.findById(requesterId)).thenReturn(Optional.of(requester));
        when(memberRepository.findById(requesteeId)).thenReturn(Optional.of(requestee));
        when(chattingRoomRepository.existsActiveRoomBetweenMembers(requesterId, requesteeId)).thenReturn(false);
        when(chattingRoomRepository.countActiveRoomsByMemberId(requesterId)).thenReturn(15L);
        when(chattingRoomRepository.countActiveRoomsByMemberId(requesteeId)).thenReturn(0L);

        // When & Then
        assertThatThrownBy(() -> chatService.createChattingRoom(requesterId, request))
                .isInstanceOfSatisfying(BusinessException.class,
                        exception -> assertThat(exception.getErrorCode()).isEqualTo(ChattingErrorCode.ROOM_LIMIT_EXCEEDED));
        verifyNoInteractions(roommateBoardRepository, chattingRequiredRepository, chatRoomMemberRepository, chatRoomMessageRepository);
    }

    @Test
    @DisplayName("요청한 게시글이 없으면 채팅방을 생성하지 않는다")
    void createChattingRoomRejectsMissingRoommateBoard() {
        // Given
        Long requesterId = 1L;
        Long requesteeId = 2L;
        Long boardId = 10L;
        Member requester = member(requesterId);
        Member requestee = member(requesteeId);
        ChatRoomCreateDto.Request request = chatRoomCreateRequest(requesteeId, boardId, "안녕하세요");

        when(memberRepository.findById(requesterId)).thenReturn(Optional.of(requester));
        when(memberRepository.findById(requesteeId)).thenReturn(Optional.of(requestee));
        when(chattingRoomRepository.existsActiveRoomBetweenMembers(requesterId, requesteeId)).thenReturn(false);
        when(chattingRoomRepository.countActiveRoomsByMemberId(requesterId)).thenReturn(0L);
        when(chattingRoomRepository.countActiveRoomsByMemberId(requesteeId)).thenReturn(0L);
        when(roommateBoardRepository.findById(boardId)).thenReturn(Optional.empty());

        // When & Then
        assertThatThrownBy(() -> chatService.createChattingRoom(requesterId, request))
                .isInstanceOfSatisfying(BusinessException.class,
                        exception -> assertThat(exception.getErrorCode()).isEqualTo(RoommateBoardErrorCode.ROOMMATE_BOARD_NOT_FOUND));
        verifyNoInteractions(chattingRequiredRepository, chatRoomMemberRepository, chatRoomMessageRepository);
    }

    @Test
    @DisplayName("채팅방 이미지 파일을 업로드하고 URL을 반환한다")
    void uploadImageUploadsFileAndReturnsUrl() throws IOException {
        // Given
        Long chatRoomId = 10L;
        Long memberId = 1L;
        MultipartFile multipartFile = multipartFile(false);
        File file = chatImage("chat-image.jpg");
        when(fileService.upload(multipartFile, FileType.CHAT_ROOM_IMAGE)).thenReturn(file);
        when(fileRepository.save(file)).thenReturn(file);

        // When
        ChatRoomImageDto.Response response = chatService.uploadImage(
                chatRoomId,
                memberId,
                multipartFile
        );

        // Then
        assertThat(response.getImageUrl()).isEqualTo("chat-image.jpg");
        InOrder inOrder = inOrder(chatRoomAccessService, fileService, fileRepository);
        inOrder.verify(chatRoomAccessService).checkCanSendMessage(chatRoomId, memberId);
        inOrder.verify(fileService).upload(multipartFile, FileType.CHAT_ROOM_IMAGE);
        inOrder.verify(fileRepository).save(file);
    }

    @Test
    @DisplayName("채팅방 이미지 업로드 요청에 파일이 없으면 실패한다")
    void uploadImageRejectsNullFile() {
        assertThatThrownBy(() -> chatService.uploadImage(10L, 1L, null))
                .isInstanceOfSatisfying(BusinessException.class,
                        exception -> assertThat(exception.getErrorCode()).isEqualTo(FileErrorCode.FILE_EMPTY));
        verifyNoInteractions(chatRoomAccessService, fileService, fileRepository);
    }

    @Test
    @DisplayName("채팅방 이미지 업로드 요청 파일이 비어 있으면 실패한다")
    void uploadImageRejectsEmptyFile() {
        // Given
        MultipartFile emptyFile = multipartFile(true);

        // When & Then
        assertThatThrownBy(() -> chatService.uploadImage(10L, 1L, emptyFile))
                .isInstanceOfSatisfying(BusinessException.class,
                        exception -> assertThat(exception.getErrorCode()).isEqualTo(FileErrorCode.FILE_EMPTY));
        verifyNoInteractions(chatRoomAccessService, fileService, fileRepository);
    }

    @Test
    @DisplayName("채팅방 이미지 업로드 중 실패하면 업로드 실패 예외를 던진다")
    void uploadImageThrowsWhenFileUploadFails() throws IOException {
        // Given
        MultipartFile multipartFile = multipartFile(false);
        when(fileService.upload(multipartFile, FileType.CHAT_ROOM_IMAGE))
                .thenThrow(new IOException("upload failed"));

        // When & Then
        assertThatThrownBy(() -> chatService.uploadImage(10L, 1L, multipartFile))
                .isInstanceOfSatisfying(BusinessException.class,
                        exception -> assertThat(exception.getErrorCode()).isEqualTo(FileErrorCode.FILE_UPLOAD_FAILED));
        verifyNoInteractions(fileRepository);
    }

    @Test
    @DisplayName("텍스트 메시지 전송 시 메시지를 저장하고 커밋 후 발행할 이벤트를 등록한다")
    void sendTextMessageSavesMessageAndPublishesEvent() {
        // Given
        Long chatId = 10L;
        Long senderId = 1L;
        Member member = member();
        ChattingRoom chattingRoom = chattingRoom();
        ChatRoomMember roomMember = activeRoomMember(member, chattingRoom);
        ChatMessageDto.Request request = textMessageRequest();
        when(chatRoomMemberRepository.findActiveMemberByRoomIdAndMemberId(chatId, senderId))
                .thenReturn(Optional.of(roomMember));
        when(chattingRoomRepository.findById(chatId)).thenReturn(Optional.of(chattingRoom));
        when(chatRoomMessageRepository.save(any(ChatRoomMessage.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        // When
        chatService.sendUserMessage(chatId, request, senderId);

        // Then
        ArgumentCaptor<ChatRoomMessage> messageCaptor = ArgumentCaptor.forClass(ChatRoomMessage.class);
        verify(chatRoomMessageRepository).save(messageCaptor.capture());
        assertThat(messageCaptor.getValue().getContents()).isEqualTo("안녕하세요");
        assertThat(messageCaptor.getValue().getMember()).isSameAs(member);
        assertThat(messageCaptor.getValue().getChattingRoom()).isSameAs(chattingRoom);
        assertThat(messageCaptor.getValue().getType()).isEqualTo(MessageType.TEXT);

        ArgumentCaptor<Object> eventCaptor = ArgumentCaptor.forClass(Object.class);
        verify(publisher).publishEvent(eventCaptor.capture());
        ChatRoomMessageEvent event = (ChatRoomMessageEvent) eventCaptor.getValue();
        assertThat(event.chatRoomId()).isEqualTo(chatId);
        assertThat(event.senderId()).isEqualTo(senderId);
        assertThat(event.clientMessageId()).isEqualTo("client-message-id");
        assertThat(event.messageType()).isEqualTo(MessageType.TEXT);
        assertThat(event.message()).isEqualTo("안녕하세요");
        assertThat(event.imageUrl()).isNull();
        verifyNoInteractions(messagingTemplate);
    }

    @Test
    @DisplayName("이미지 메시지 전송 시 메시지와 파일 연결을 저장하고 커밋 후 발행할 이벤트를 등록한다")
    void sendImageMessageSavesMessageFileAndPublishesEvent() {
        // Given
        Long chatId = 10L;
        Long senderId = 1L;
        Member member = member();
        ChattingRoom chattingRoom = chattingRoom();
        ChatRoomMember roomMember = activeRoomMember(member, chattingRoom);
        ChatMessageDto.Request request = imageMessageRequest("chat-image.jpg");
        File file = chatImage("chat-image.jpg");
        ChatRoomMessage savedMessage = ChatRoomMessage.builder()
                .contents("사진을 보냈습니다.")
                .member(member)
                .chattingRoom(chattingRoom)
                .type(MessageType.IMAGE)
                .build();
        when(chatRoomMemberRepository.findActiveMemberByRoomIdAndMemberId(chatId, senderId))
                .thenReturn(Optional.of(roomMember));
        when(chattingRoomRepository.findById(chatId)).thenReturn(Optional.of(chattingRoom));
        when(fileRepository.findBySavedFileNameAndType("chat-image.jpg", FileType.CHAT_ROOM_IMAGE))
                .thenReturn(Optional.of(file));
        when(chatRoomMessageRepository.save(any(ChatRoomMessage.class))).thenReturn(savedMessage);
        when(chatRoomFileRepository.save(any(ChatRoomFile.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        // When
        chatService.sendUserMessage(chatId, request, senderId);

        // Then
        ArgumentCaptor<ChatRoomMessage> messageCaptor = ArgumentCaptor.forClass(ChatRoomMessage.class);
        verify(chatRoomMessageRepository).save(messageCaptor.capture());
        assertThat(messageCaptor.getValue().getContents()).isEqualTo("사진을 보냈습니다.");
        assertThat(messageCaptor.getValue().getMember()).isSameAs(member);
        assertThat(messageCaptor.getValue().getChattingRoom()).isSameAs(chattingRoom);
        assertThat(messageCaptor.getValue().getType()).isEqualTo(MessageType.IMAGE);

        ArgumentCaptor<ChatRoomFile> chatRoomFileCaptor = ArgumentCaptor.forClass(ChatRoomFile.class);
        verify(chatRoomFileRepository).save(chatRoomFileCaptor.capture());
        assertThat(chatRoomFileCaptor.getValue().getFile()).isSameAs(file);
        assertThat(chatRoomFileCaptor.getValue().getChatRoomMessage()).isSameAs(savedMessage);

        ArgumentCaptor<Object> eventCaptor = ArgumentCaptor.forClass(Object.class);
        verify(publisher).publishEvent(eventCaptor.capture());
        ChatRoomMessageEvent event = (ChatRoomMessageEvent) eventCaptor.getValue();
        assertThat(event.messageType()).isEqualTo(MessageType.IMAGE);
        assertThat(event.imageUrl()).isEqualTo("chat-image.jpg");
        verifyNoInteractions(messagingTemplate);
    }

    @Test
    @DisplayName("이미지 메시지 URL과 일치하는 채팅방 이미지 파일이 없으면 실패한다")
    void sendImageMessageRejectsUnknownImageUrl() {
        // Given
        Long chatId = 10L;
        Long senderId = 1L;
        Member member = member();
        ChattingRoom chattingRoom = chattingRoom();
        ChatMessageDto.Request request = imageMessageRequest("unknown.jpg");
        when(chatRoomMemberRepository.findActiveMemberByRoomIdAndMemberId(chatId, senderId))
                .thenReturn(Optional.of(activeRoomMember(member, chattingRoom)));
        when(chattingRoomRepository.findById(chatId)).thenReturn(Optional.of(chattingRoom));
        when(fileRepository.findBySavedFileNameAndType("unknown.jpg", FileType.CHAT_ROOM_IMAGE))
                .thenReturn(Optional.empty());

        // When & Then
        assertThatThrownBy(() -> chatService.sendUserMessage(chatId, request, senderId))
                .isInstanceOfSatisfying(BusinessException.class,
                        exception -> assertThat(exception.getErrorCode()).isEqualTo(FileErrorCode.FILE_NOT_FOUND));
        ArgumentCaptor<ChatRoomMessage> messageCaptor = ArgumentCaptor.forClass(ChatRoomMessage.class);
        verify(chatRoomMessageRepository).save(messageCaptor.capture());
        assertThat(messageCaptor.getValue().getMember()).isSameAs(member);
        assertThat(messageCaptor.getValue().getChattingRoom()).isSameAs(chattingRoom);
        assertThat(messageCaptor.getValue().getType()).isEqualTo(MessageType.IMAGE);
        verifyNoInteractions(chatRoomFileRepository, publisher, messagingTemplate);
    }

    @Test
    @DisplayName("채팅방에 참여 중인 멤버가 아니면 메시지를 저장하지 않는다")
    void sendUserMessageRejectsMemberWhoIsNotActiveRoomMember() {
        // Given
        Long chatId = 10L;
        Long senderId = 1L;
        ChatMessageDto.Request request = textMessageRequest();
        when(chatRoomMemberRepository.findActiveMemberByRoomIdAndMemberId(chatId, senderId))
                .thenReturn(Optional.empty());

        // When & Then
        assertThatThrownBy(() -> chatService.sendUserMessage(chatId, request, senderId))
                .isInstanceOfSatisfying(BusinessException.class,
                        exception -> assertThat(exception.getErrorCode()).isEqualTo(ChattingErrorCode.ROOM_MEMBER_NOT_FOUND));
        verifyNoInteractions(chatRoomMessageRepository, chatRoomFileRepository, publisher, messagingTemplate);
    }

    @Test
    @DisplayName("텍스트 메시지 본문이 없으면 메시지를 저장하지 않는다")
    void sendMessageRejectsTextMessageWithoutUserMessage() {
        // Given
        ChatMessageDto.Request request = new ChatMessageDto.Request();
        request.setClientMessageId("client-message-id");
        request.setType(MessageType.TEXT);

        // When & Then
        assertThatThrownBy(() -> chatService.sendUserMessage(10L, request, 1L))
                .isInstanceOfSatisfying(BusinessException.class,
                        exception -> assertThat(exception.getErrorCode()).isEqualTo(ChattingErrorCode.MESSAGE_PAYLOAD_INVALID));
        verifyNoInteractions(chatRoomMemberRepository, chatRoomMessageRepository, messagingTemplate);
    }

    @Test
    @DisplayName("이미지 메시지 URL이 없으면 메시지를 저장하지 않는다")
    void sendMessageRejectsImageUserMessageWithoutImageUrl() {
        // Given
        ChatMessageDto.Request request = new ChatMessageDto.Request();
        request.setClientMessageId("client-message-id");
        request.setType(MessageType.IMAGE);

        // When & Then
        assertThatThrownBy(() -> chatService.sendUserMessage(10L, request, 1L))
                .isInstanceOfSatisfying(BusinessException.class,
                        exception -> assertThat(exception.getErrorCode()).isEqualTo(ChattingErrorCode.MESSAGE_PAYLOAD_INVALID));
        verifyNoInteractions(chatRoomMemberRepository, chatRoomMessageRepository, messagingTemplate);
    }

    @Test
    @DisplayName("메시지 전송 이벤트가 커밋된 후 채팅 메시지를 구독 채널로 발행한다")
    void handleMessageSendPublishesChatMessageToRoomDestination() {
        // Given
        Long chatId = 10L;
        Long senderId = 1L;
        ChatRoomMessageEvent event = ChatRoomMessageEvent.builder()
                .chatRoomId(chatId)
                .senderId(senderId)
                .clientMessageId("client-message-id")
                .messageType(MessageType.TEXT)
                .message("안녕하세요")
                .build();

        // When
        chatService.handleMessageSend(event);

        // Then
        ArgumentCaptor<Object> payloadCaptor = ArgumentCaptor.forClass(Object.class);
        verify(messagingTemplate).convertAndSend(eq("/sub/chats/10"), payloadCaptor.capture());

        ChatSocketResponse<ChatMessageDto.Response> response = (ChatSocketResponse<ChatMessageDto.Response>) payloadCaptor.getValue();
        assertThat(response.getEventType()).isEqualTo(EventType.USER_MESSAGE);
        assertThat(response.getChatRoomId()).isEqualTo(chatId);
        assertThat(response.getCreatedAt()).isNotNull();
        assertThat(response.getPayload().getClientMessageId()).isEqualTo("client-message-id");
        assertThat(response.getPayload().getSenderId()).isEqualTo(senderId);
        assertThat(response.getPayload().getType()).isEqualTo(MessageType.TEXT);
        assertThat(response.getPayload().getContents()).isEqualTo("안녕하세요");
    }

    @Test
    @DisplayName("활성 채팅방 멤버가 채팅방을 나가면 나간 상태로 변경하고 퇴장 이벤트를 발행 요청한다")
    void leaveChatRoomMarksMemberAsLeftAndPublishesUserLeftEvent() {
        // Given
        Long chatRoomId = 10L;
        Long memberId = 1L;
        ChattingRoom chattingRoom = chattingRoom();
        ChatRoomMember roomMember = ChatRoomMember.builder()
                .isLeft(false)
                .build();
        when(chatRoomMemberRepository.findActiveMemberByRoomIdAndMemberId(chatRoomId, memberId))
                .thenReturn(Optional.of(roomMember));
        when(chattingRoomRepository.findById(chatRoomId)).thenReturn(Optional.of(chattingRoom));
        when(chatRoomMessageRepository.save(any(ChatRoomMessage.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        // When
        ChatRoomDto.Response result = chatService.leaveChatRoom(memberId, chatRoomId);

        // Then
        assertThat(roomMember.getIsLeft()).isTrue();
        assertThat(result.getUpdatedAt()).isNotNull();
        ArgumentCaptor<ChatRoomMessage> messageCaptor = ArgumentCaptor.forClass(ChatRoomMessage.class);
        verify(chatRoomMessageRepository).save(messageCaptor.capture());
        assertThat(messageCaptor.getValue().getMember()).isNull();
        assertThat(messageCaptor.getValue().getChattingRoom()).isSameAs(chattingRoom);
        assertThat(messageCaptor.getValue().getType()).isEqualTo(MessageType.LEFT_ROOM);

        ArgumentCaptor<Object> eventCaptor = ArgumentCaptor.forClass(Object.class);
        verify(publisher).publishEvent(eventCaptor.capture());
        verifyNoInteractions(messagingTemplate);

        ChatRoomLeftEvent event = (ChatRoomLeftEvent) eventCaptor.getValue();
        assertThat(event.chatRoomId()).isEqualTo(chatRoomId);
        assertThat(event.leftAt()).isEqualTo(result.getUpdatedAt());
        assertThat(event.message()).isEqualTo("상대방이 나갔습니다.");
    }

    @Test
    @DisplayName("채팅방 나가기 이벤트가 커밋된 후 퇴장 이벤트를 구독 채널로 발행한다")
    void handleChatRoomLeftPublishesUserLeftEventToRoomDestination() {
        // Given
        Long chatRoomId = 10L;
        LocalDateTime leftAt = LocalDateTime.of(2026, 6, 19, 21, 50);
        ChatRoomLeftEvent event = new ChatRoomLeftEvent(chatRoomId, leftAt, "상대방이 나갔습니다.");

        // When
        chatService.handleChatRoomLeft(event);

        // Then
        ArgumentCaptor<Object> payloadCaptor = ArgumentCaptor.forClass(Object.class);
        verify(messagingTemplate).convertAndSend(eq("/sub/chats/10"), payloadCaptor.capture());

        ChatSocketResponse<ChatMessageDto.Response> response = (ChatSocketResponse<ChatMessageDto.Response>) payloadCaptor.getValue();
        assertThat(response.getEventType()).isEqualTo(EventType.SYSTEM_MESSAGE);
        assertThat(response.getChatRoomId()).isEqualTo(chatRoomId);
        assertThat(response.getCreatedAt()).isEqualTo(leftAt);
        assertThat(response.getPayload().getSenderId()).isNull();
        assertThat(response.getPayload().getType()).isEqualTo(MessageType.LEFT_ROOM);
        assertThat(response.getPayload().getContents()).isEqualTo("상대방이 나갔습니다.");
    }

    @Test
    @DisplayName("활성 채팅방 멤버가 아니면 채팅방 나가기를 거부하고 퇴장 이벤트를 발행하지 않는다")
    void leaveChatRoomRejectsMemberWhoIsNotActiveRoomMember() {
        // Given
        Long chatRoomId = 10L;
        Long memberId = 1L;
        when(chatRoomMemberRepository.findActiveMemberByRoomIdAndMemberId(chatRoomId, memberId))
                .thenReturn(Optional.empty());

        // When & Then
        assertThatThrownBy(() -> chatService.leaveChatRoom(memberId, chatRoomId))
                .isInstanceOfSatisfying(BusinessException.class,
                        exception -> assertThat(exception.getErrorCode()).isEqualTo(ChattingErrorCode.ROOM_MEMBER_NOT_FOUND));
        verifyNoInteractions(publisher, messagingTemplate);
    }

    private ChatRoomListDto.Response chatRoom(
            Long chatRoomId,
            String memberName,
            String memberProfileImageUrl,
            LocalDateTime createdAt,
            ChattingRequiredStatus status
    ) {
        ChatRoomListDto.Response response = new ChatRoomListDto.Response();
        response.setChatRoomId(chatRoomId);
        response.setMemberName(memberName);
        response.setMemberProfileImageUrl(memberProfileImageUrl);
        response.setCreatedAt(createdAt);
        response.setStatus(status);
        return response;
    }

    private ChatMessageDto.Request textMessageRequest() {
        ChatMessageDto.Request request = new ChatMessageDto.Request();
        request.setClientMessageId("client-message-id");
        request.setType(MessageType.TEXT);
        request.setMessage("안녕하세요");
        return request;
    }

    private ChatMessageDto.Request imageMessageRequest(String imageUrl) {
        ChatMessageDto.Request request = new ChatMessageDto.Request();
        request.setClientMessageId("client-message-id");
        request.setType(MessageType.IMAGE);
        request.setImageUrl(imageUrl);
        return request;
    }

    private ChatRoomCreateDto.Request chatRoomCreateRequest(Long requesteeId, Long boardId, String contents) {
        ChatRoomCreateDto.ChatMessage chatMessage = new ChatRoomCreateDto.ChatMessage();
        chatMessage.setContents(contents);

        ChatRoomCreateDto.Request request = new ChatRoomCreateDto.Request();
        request.setRequesteeId(requesteeId);
        request.setBoardId(boardId);
        request.setChatMessage(chatMessage);
        return request;
    }

    private ChatRoomMember activeRoomMember(Member member, ChattingRoom chattingRoom) {
        return ChatRoomMember.builder()
                .member(member)
                .chattingRoom(chattingRoom)
                .isLeft(false)
                .build();
    }

    private Member member() {
        return Member.builder().build();
    }

    private Member member(Long id) {
        return Member.builder().id(id).build();
    }

    private ChattingRoom chattingRoom() {
        return ChattingRoom.builder().build();
    }

    private ChattingRoom persistedChattingRoom(ChattingRoom chattingRoom, Long id) {
        ReflectionTestUtils.setField(chattingRoom, "id", id);
        return chattingRoom;
    }

    private ChatRoomMessage persistedMessage(ChatRoomMessage message, LocalDateTime createdAt) {
        ReflectionTestUtils.setField(message, "createdAt", createdAt);
        return message;
    }

    private File chatImage(String savedFileName) {
        return File.builder()
                .type(FileType.CHAT_ROOM_IMAGE)
                .originalFileName(savedFileName)
                .savedFileName(savedFileName)
                .fileExt("jpg")
                .isDeleted(false)
                .build();
    }

    private MultipartFile multipartFile(boolean empty) {
        MultipartFile multipartFile = mock(MultipartFile.class);
        when(multipartFile.isEmpty()).thenReturn(empty);
        return multipartFile;
    }
}
