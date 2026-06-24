package org.example.knockin.service.impl;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.example.knockin.dto.ChatMessageDto;
import org.example.knockin.dto.ChatRoomCreateDto;
import org.example.knockin.dto.ChatRoomDetailDto;
import org.example.knockin.dto.ChatRoomDto;
import org.example.knockin.dto.ChatRoomDto.Response;
import org.example.knockin.dto.ChatRoomImageDto;
import org.example.knockin.dto.ChatRoomListDto;
import org.example.knockin.dto.ChatRoomLeftEvent;
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
import org.example.knockin.entity.file.File;
import org.example.knockin.entity.file.FileType;
import org.example.knockin.entity.member.Member;
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
import org.example.knockin.repository.file.FileRepository;
import org.example.knockin.repository.member.BasicInformationRepository;
import org.example.knockin.repository.member.MemberRepository;
import org.example.knockin.repository.member.row.ChattingRoomBasicInfoRow;
import org.example.knockin.repository.room.RoommateMatchingRequiredRepository;
import org.example.knockin.service.FileService;
import org.jspecify.annotations.Nullable;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.messaging.simp.SimpMessageSendingOperations;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

@Service
@RequiredArgsConstructor
public class ChatServiceImpl {

    private static final String ROOM_LEAVE_MESSAGE_CONTENTS = "상대방이 나갔습니다.";
    private static final String IMAGE_MESSAGE_CONTENTS = "사진을 보냈습니다.";
    private static final long CHAT_ROOM_LIMIT_PER_MEMBER = 15L;

    private final ChattingRoomRepository chattingRoomRepository;
    private final ChatRoomMemberRepository chatRoomMemberRepository;
    private final SimpMessageSendingOperations messagingTemplate;
    private final ApplicationEventPublisher publisher;
    private final ChatRoomAccessService chatRoomAccessService;
    private final ChatRoomMessageRepository chatRoomMessageRepository;
    private final FileService fileService;
    private final FileRepository fileRepository;
    private final ChatRoomFileRepository chatRoomFileRepository;
    private final BasicInformationRepository basicInformationRepository;
    private final RoommateMatchingRequiredRepository roommateMatchingRequiredRepository;
    private final RoommateBoardRepository roommateBoardRepository;
    private final ChattingRequiredRepository chattingRequiredRepository;
    private final MemberRepository memberRepository;

    public List<ChatRoomListDto.Response> getChatRoomList(Long memberId) {
        return chattingRoomRepository.findByMemberId(memberId);
    }

    @Transactional
    public ChatRoomImageDto.Response uploadImage(Long chatRoomId, Long memberId, MultipartFile multipartFile) {
        validateImageFile(multipartFile);
        chatRoomAccessService.checkCanSendMessage(chatRoomId, memberId);

        try {
            File uploadedFile = fileService.upload(multipartFile, FileType.CHAT_ROOM_IMAGE);
            File savedFile = fileRepository.save(uploadedFile);
            return new ChatRoomImageDto.Response(savedFile.getSavedFileName());
        } catch (IOException e) {
            throw new BusinessException(FileErrorCode.FILE_UPLOAD_FAILED);
        }
    }

    @Transactional
    public ChatRoomDto.Response leaveChatRoom(Long memberId, Long chatRoomId) {
        ChatRoomMember roomMember = chatRoomMemberRepository.findActiveMemberByRoomIdAndMemberId(chatRoomId, memberId)
                .orElseThrow(() -> new BusinessException(ChattingErrorCode.ROOM_MEMBER_NOT_FOUND));
        roomMember.left();

        ChattingRoom chattingRoom = chattingRoomRepository.findById(chatRoomId)
                .orElseThrow(() -> new BusinessException(ChattingErrorCode.ROOM_NOT_FOUND));
        saveMessage(ROOM_LEAVE_MESSAGE_CONTENTS, null, chattingRoom, MessageType.LEFT_ROOM);

        LocalDateTime now = LocalDateTime.now();
        publisher.publishEvent(new ChatRoomLeftEvent(chatRoomId, now, ROOM_LEAVE_MESSAGE_CONTENTS));
        return new Response(now);
    }

    @Transactional
    public void sendUserMessage(Long chatRoomId, ChatMessageDto.Request request, Long senderId) {
        validateMessageRequest(request);
        ChatRoomMember chatRoomMember = chatRoomMemberRepository.findActiveMemberByRoomIdAndMemberId(chatRoomId, senderId)
                .orElseThrow(() -> new BusinessException(ChattingErrorCode.ROOM_MEMBER_NOT_FOUND));
        ChattingRoom chattingRoom = chattingRoomRepository.findById(chatRoomId)
                .orElseThrow(() -> new BusinessException(ChattingErrorCode.ROOM_NOT_FOUND));
        Member member = chatRoomMember.getMember();
        MessageType type = request.getType();

        switch (request.getType()) {
            case TEXT -> {
                saveMessage(request.getMessage(), member, chattingRoom, type);
                publishMessageEvent(chatRoomId, senderId, request);
            }
            case IMAGE -> {
                ChatRoomMessage chatRoomMessage = saveMessage(IMAGE_MESSAGE_CONTENTS, member, chattingRoom, type);
                File file = findFile(request.getImageUrl());
                saveMessageFile(file, chatRoomMessage);
                publishMessageEvent(chatRoomId, senderId, request);
            }
            default -> throw new BusinessException(ChattingErrorCode.MESSAGE_PAYLOAD_INVALID);
        }
    }

    private ChatRoomMessage saveMessage(String contents, @Nullable Member member, ChattingRoom chattingRoom, MessageType type) {
        ChatRoomMessage chatRoomMessage = ChatRoomMessage.builder()
                .contents(contents)
                .member(member)
                .chattingRoom(chattingRoom)
                .type(type)
                .build();

        return chatRoomMessageRepository.save(chatRoomMessage);
    }

    private File findFile(String fileUrl) {
        return fileRepository.findBySavedFileNameAndType(fileUrl, FileType.CHAT_ROOM_IMAGE)
                .orElseThrow(() -> new BusinessException(FileErrorCode.FILE_NOT_FOUND));
    }

    private ChatRoomFile saveMessageFile(File file, ChatRoomMessage chatRoomMessage) {
        ChatRoomFile chatRoomFile = ChatRoomFile.builder()
                .file(file)
                .chatRoomMessage(chatRoomMessage)
                .build();

        return chatRoomFileRepository.save(chatRoomFile);
    }

    private void publishMessageEvent(Long chatRoomId, Long senderId, ChatMessageDto.Request request) {
        ChatRoomMessageEvent event = ChatRoomMessageEvent.builder()
                .chatRoomId(chatRoomId)
                .senderId(senderId)
                .clientMessageId(request.getClientMessageId())
                .messageType(request.getType())
                .message(request.getMessage())
                .imageUrl(request.getImageUrl())
                .build();

        publisher.publishEvent(event);
    }

    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void handleChatRoomLeft(ChatRoomLeftEvent event) {
        ChatSocketResponse<ChatMessageDto.Response> response = ChatSocketResponse.of(
                EventType.SYSTEM_MESSAGE,
                event.chatRoomId(),
                ChatMessageDto.Response.userLeft(event),
                event.leftAt()
        );
        messagingTemplate.convertAndSend("/sub/chats/" + event.chatRoomId(), response);
    }

    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void handleMessageSend(ChatRoomMessageEvent event) {
        ChatSocketResponse<ChatMessageDto.Response> response = ChatSocketResponse.of(
                EventType.USER_MESSAGE,
                event.chatRoomId(),
                ChatMessageDto.Response.chatMessage(event)
        );
        messagingTemplate.convertAndSend("/sub/chats/" + event.chatRoomId(), response);
    }

    private void validateMessageRequest(ChatMessageDto.Request request) {
        if (request == null
                || !StringUtils.hasText(request.getClientMessageId())
                || request.getType() == null) {
            throw new BusinessException(ChattingErrorCode.MESSAGE_PAYLOAD_INVALID);
        }

        switch (request.getType()) {
            case TEXT -> validateTextMessage(request);
            case IMAGE -> validateImageMessage(request);
        }
    }

    @Transactional
    public ChatRoomDetailDto.Response getChatRoomDetail(Long chatRoomId, Long memberId) {
        ChattingRoom chattingRoom = chattingRoomRepository.findById(chatRoomId)
                .orElseThrow(() -> new BusinessException(ChattingErrorCode.ROOM_NOT_FOUND));
        ChatRoomMember chatRoomMember = chatRoomMemberRepository.findActiveMemberByRoomIdAndMemberId(chatRoomId, memberId)
                .orElseThrow(() -> new BusinessException(ChattingErrorCode.ROOM_MEMBER_NOT_FOUND));

        ChatRoomDetailDto.ProfileInfo opponentProfile = getOpponentProfileInfo(chatRoomMember, chatRoomId);
        List<ChatRoomDetailDto.ChatMessage> messages = chatRoomMessageRepository.findChatMessageDto(chatRoomId);
        List<RoommateMatchingRequiredInfo> matchingRequiredList = roommateMatchingRequiredRepository.findRequiredDto(chattingRoom);

        return ChatRoomDetailDto.Response.builder()
                .opponentProfile(opponentProfile)
                .messages(messages)
                .matchingRequiredList(matchingRequiredList)
                .build();
    }

    private ChatRoomDetailDto.ProfileInfo getOpponentProfileInfo(ChatRoomMember me, Long chatRoomId) {
        Member opponentMember = chatRoomMemberRepository.findPartnerMember(me, chatRoomId);
        ChattingRoomBasicInfoRow row = basicInformationRepository.findChattingRoomBasicInfoRow(opponentMember)
                .orElseThrow(() -> new BusinessException(MemberErrorCode.BASIC_INFO_NOT_FOUND));

        return ChatRoomDetailDto.ProfileInfo.builder()
                .id(row.memberId())
                .name(row.name())
                .age(DateUtils.calculateAge(row.birth()))
                .gender(row.gender())
                .profileImageUrl(row.profileImageUrl())
                // TODO: 점수 적용
                .score(100)
                .build();
    }

    @Transactional
    public ChatRoomCreateDto.Response createChattingRoom(Long requesterId, ChatRoomCreateDto.Request request) {
        Member requester = memberRepository.findById(requesterId)
                .orElseThrow(() -> new BusinessException(MemberErrorCode.MEMBER_NOT_FOUND));
        Member requestee = memberRepository.findById(request.getRequesteeId())
                .orElseThrow(() -> new BusinessException(MemberErrorCode.MEMBER_NOT_FOUND));

        validateActiveRoomDoesNotExist(requesterId, request.getRequesteeId());
        validateChatRoomLimit(requesterId, request.getRequesteeId());

        RoommateBoard roommateBoard = findRoommateBoardNullSafety(request.getBoardId());
        ChattingRequired chattingRequired = saveChattingRequiredDirectly(requester, requestee, roommateBoard);
        ChattingRoom chattingRoom = saveChattingRoom(chattingRequired);
        saveChattingRoomMembers(chattingRoom, List.of(requester, requestee));
        String contents = request.getChatMessage().getContents();
        ChatRoomMessage chatRoomMessage = saveMessage(contents, requester, chattingRoom, MessageType.TEXT);

        return ChatRoomCreateDto.Response.builder()
                .chatRoomId(chattingRoom.getId())
                .updatedAt(chatRoomMessage.getCreatedAt())
                .build();
    }

    private void validateActiveRoomDoesNotExist(Long requesterId, Long requesteeId) {
        if (chattingRoomRepository.existsActiveRoomBetweenMembers(requesterId, requesteeId)) {
            throw new BusinessException(ChattingErrorCode.ROOM_DUPLICATE);
        }
    }

    private void validateChatRoomLimit(Long requesterId, Long requesteeId) {
        long requesterRoomCount = chattingRoomRepository.countActiveRoomsByMemberId(requesterId);
        long requesteeRoomCount = chattingRoomRepository.countActiveRoomsByMemberId(requesteeId);

        if (requesterRoomCount >= CHAT_ROOM_LIMIT_PER_MEMBER || requesteeRoomCount >= CHAT_ROOM_LIMIT_PER_MEMBER) {
            throw new BusinessException(ChattingErrorCode.ROOM_LIMIT_EXCEEDED);
        }
    }

    private RoommateBoard findRoommateBoardNullSafety(@Nullable Long boardId) {
        if (boardId == null) return null;

        return roommateBoardRepository.findById(boardId)
                .orElseThrow(() -> new BusinessException(RoommateBoardErrorCode.ROOMMATE_BOARD_NOT_FOUND));
    }

    private ChattingRequired saveChattingRequiredDirectly(Member requester, Member requestee, @Nullable RoommateBoard roommateBoard) {
        ChattingRequired chattingRequired = ChattingRequired.builder()
                .requester(requester)
                .requestee(requestee)
                .roommateBoard(roommateBoard)
                .status(ChattingRequiredStatus.ACCEPTED)
                .build();

        return chattingRequiredRepository.save(chattingRequired);
    }

    private ChattingRoom saveChattingRoom(ChattingRequired chattingRequired) {
        ChattingRoom chattingRoom = ChattingRoom.builder().chattingRequired(chattingRequired).build();
        return chattingRoomRepository.save(chattingRoom);
    }

    private List<ChatRoomMember> saveChattingRoomMembers(ChattingRoom chattingRoom, List<Member> members) {
        if (members.size() > 2) {
            throw new BusinessException(ChattingErrorCode.ROOM_CAPACITY_EXCEEDED);
        }

        List<ChatRoomMember> chatRoomMembers = members.stream()
                .map(member -> ChatRoomMember.of(chattingRoom, member))
                .toList();

        return chatRoomMemberRepository.saveAll(chatRoomMembers);
    }

    private void validateTextMessage(ChatMessageDto.Request request) {
        if (!StringUtils.hasText(request.getMessage())) {
            throw new BusinessException(ChattingErrorCode.MESSAGE_PAYLOAD_INVALID);
        }
    }

    private void validateImageMessage(ChatMessageDto.Request request) {
        if (!StringUtils.hasText(request.getImageUrl())) {
            throw new BusinessException(ChattingErrorCode.MESSAGE_PAYLOAD_INVALID);
        }
    }

    private void validateImageFile(MultipartFile multipartFile) {
        if (multipartFile == null || multipartFile.isEmpty()) {
            throw new BusinessException(FileErrorCode.FILE_EMPTY);
        }
    }
}
