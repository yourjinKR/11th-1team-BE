package org.example.knockin.service.impl;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.inOrder;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

import java.io.IOException;
import java.util.ArrayList;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.Period;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicReference;
import java.util.stream.StreamSupport;
import org.example.knockin.dto.BoardDetailDto;
import org.example.knockin.dto.BoardDto;
import org.example.knockin.dto.BoardDto.Request.FileDto;
import org.example.knockin.dto.BoardEditDto;
import org.example.knockin.dto.BoardEditDto.Response.BoardOptionInfo;
import org.example.knockin.dto.BoardListDto;
import org.example.knockin.dto.BoardModifyDto;
import org.example.knockin.dto.BoardModifyDto.Request.ExistingFileDto;
import org.example.knockin.dto.BoardModifyDto.Request.NewFileDto;
import org.example.knockin.dto.ReportDto;
import org.example.knockin.entity.auth.AuthenticationType;
import org.example.knockin.entity.board.RoommateBoard;
import org.example.knockin.entity.board.RoommateBoardDeclaration;
import org.example.knockin.entity.board.RoommateBoardFile;
import org.example.knockin.entity.board.RoommateBoardInterest;
import org.example.knockin.entity.board.RoommateBoardOption;
import org.example.knockin.entity.file.File;
import org.example.knockin.entity.file.FileType;
import org.example.knockin.entity.life.LifePatternType;
import org.example.knockin.entity.member.Gender;
import org.example.knockin.entity.member.Member;
import org.example.knockin.entity.room.Region;
import org.example.knockin.entity.room.RoomExtraOption;
import org.example.knockin.entity.room.RoomType;
import org.example.knockin.global.exception.BusinessException;
import org.example.knockin.global.exception.CommonErrorCode;
import org.example.knockin.global.exception.FileErrorCode;
import org.example.knockin.global.exception.MemberErrorCode;
import org.example.knockin.global.exception.MetaErrorCode;
import org.example.knockin.global.exception.RoommateBoardErrorCode;
import org.example.knockin.repository.auth.AuthenticationRepository;
import org.example.knockin.repository.board.RoommateBoardDeclarationRepository;
import org.example.knockin.repository.board.RoommateBoardFileRepository;
import org.example.knockin.repository.board.RoommateBoardInterestRepository;
import org.example.knockin.repository.board.RoommateBoardOptionRepository;
import org.example.knockin.repository.board.RoommateBoardSearchCondition;
import org.example.knockin.repository.board.RoommateBoardRepository;
import org.example.knockin.repository.board.row.BasicInfoRow;
import org.example.knockin.repository.board.row.EditFormRow;
import org.example.knockin.repository.file.FileRepository;
import org.example.knockin.repository.life.MemberLifePatternRepository;
import org.example.knockin.repository.life.PreferenceConditionRepository;
import org.example.knockin.repository.life.PreferenceConditionWeightRepository;
import org.example.knockin.service.FileService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.InjectMocks;
import org.mockito.InOrder;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.transaction.support.TransactionCallback;
import org.springframework.transaction.support.TransactionTemplate;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.web.multipart.MultipartFile;

@ExtendWith(MockitoExtension.class)
@DisplayName("룸메이트 게시글 서비스")
class RoommateBoardServiceImplTest {

    @Mock
    private RoommateBoardRepository roommateBoardRepository;

    @Mock
    private RoommateBoardFileRepository roommateBoardFileRepository;

    @Mock
    private RoommateBoardOptionRepository roommateBoardOptionRepository;

    @Mock
    private RoommateBoardInterestRepository roommateBoardInterestRepository;

    @Mock
    private RoommateBoardDeclarationRepository roommateBoardDeclarationRepository;

    @Mock
    private FileRepository fileRepository;

    @Mock
    private PreferenceConditionRepository preferenceConditionRepository;

    @Mock
    private PreferenceConditionWeightRepository preferenceConditionWeightRepository;

    @Mock
    private MemberLifePatternRepository memberLifePatternRepository;

    @Mock
    private AuthenticationRepository authenticationRepository;

    @Mock
    private MemberServiceImpl memberService;

    @Mock
    private FileService fileService;

    @Mock
    private MetaServiceImpl metaService;

    @Mock
    private TransactionTemplate transactionTemplate;

    @InjectMocks
    private RoommateBoardServiceImpl roommateBoardService;

    @Captor
    private ArgumentCaptor<RoommateBoard> roommateBoardCaptor;

    @Captor
    private ArgumentCaptor<Iterable<File>> filesCaptor;

    @Captor
    private ArgumentCaptor<Iterable<RoommateBoardFile>> boardFilesCaptor;

    @Captor
    private ArgumentCaptor<RoommateBoardFile> boardFileCaptor;

    @Captor
    private ArgumentCaptor<Iterable<RoommateBoardOption>> boardOptionsCaptor;

    @Captor
    private ArgumentCaptor<List<File>> uploadedFilesCaptor;

    @Captor
    private ArgumentCaptor<RoommateBoardSearchCondition> searchConditionCaptor;

    @Captor
    private ArgumentCaptor<RoommateBoardInterest> roommateBoardInterestCaptor;

    @Captor
    private ArgumentCaptor<RoommateBoardDeclaration> roommateBoardDeclarationCaptor;

    @BeforeEach
    void setUpTransactionTemplate() {
        lenient().when(transactionTemplate.execute(any())).thenAnswer(invocation -> {
            TransactionCallback<?> callback = invocation.getArgument(0);
            return callback.doInTransaction(null);
        });
    }

    @Test
    @DisplayName("좋아요 이력이 없으면 관심 게시글을 새로 저장한다")
    void likeBoardCreatesInterestWhenNotExists() {
        Long boardId = 1L;
        Long memberId = 2L;
        RoommateBoard board = createRoommateBoard(10L);
        Member member = Member.builder().id(memberId).build();

        when(roommateBoardRepository.findByIdForUpdate(boardId)).thenReturn(Optional.of(board));
        when(memberService.findById(memberId)).thenReturn(Optional.of(member));
        when(roommateBoardInterestRepository.findByRoommateBoardAndMember(board, member)).thenReturn(Optional.empty());

        BoardDto.Response response = roommateBoardService.likeBoard(boardId, memberId);

        verify(roommateBoardInterestRepository).save(roommateBoardInterestCaptor.capture());
        RoommateBoardInterest interest = roommateBoardInterestCaptor.getValue();
        assertThat(interest.getRoommateBoard()).isSameAs(board);
        assertThat(interest.getMember()).isSameAs(member);
        assertThat(interest.getIsDeleted()).isFalse();
        assertThat(response.getUpdatedAt()).isNotNull();
    }

    @Test
    @DisplayName("이미 좋아요한 게시글이면 기존 관심 게시글을 삭제 상태로 토글한다")
    void likeBoardTogglesExistingActiveInterestToDeleted() {
        Long boardId = 1L;
        Long memberId = 2L;
        RoommateBoard board = createRoommateBoard(10L);
        Member member = Member.builder().id(memberId).build();
        RoommateBoardInterest interest = RoommateBoardInterest.builder()
                .roommateBoard(board)
                .member(member)
                .isDeleted(false)
                .build();

        when(roommateBoardRepository.findByIdForUpdate(boardId)).thenReturn(Optional.of(board));
        when(memberService.findById(memberId)).thenReturn(Optional.of(member));
        when(roommateBoardInterestRepository.findByRoommateBoardAndMember(board, member)).thenReturn(Optional.of(interest));

        roommateBoardService.likeBoard(boardId, memberId);

        assertThat(interest.getIsDeleted()).isTrue();
        verify(roommateBoardInterestRepository, never()).save(any(RoommateBoardInterest.class));
    }

    @Test
    @DisplayName("삭제 상태의 관심 게시글이면 다시 좋아요 상태로 토글한다")
    void likeBoardTogglesExistingDeletedInterestToActive() {
        Long boardId = 1L;
        Long memberId = 2L;
        RoommateBoard board = createRoommateBoard(10L);
        Member member = Member.builder().id(memberId).build();
        RoommateBoardInterest interest = RoommateBoardInterest.builder()
                .roommateBoard(board)
                .member(member)
                .isDeleted(true)
                .build();

        when(roommateBoardRepository.findByIdForUpdate(boardId)).thenReturn(Optional.of(board));
        when(memberService.findById(memberId)).thenReturn(Optional.of(member));
        when(roommateBoardInterestRepository.findByRoommateBoardAndMember(board, member)).thenReturn(Optional.of(interest));

        roommateBoardService.likeBoard(boardId, memberId);

        assertThat(interest.getIsDeleted()).isFalse();
        verify(roommateBoardInterestRepository, never()).save(any(RoommateBoardInterest.class));
    }

    @Test
    @DisplayName("같은 좋아요 요청이 두 번 들어오면 토글 구현은 좋아요를 취소한다")
    void likeBoardCancelsLikeWhenSameRequestIsRetried() {
        Long boardId = 1L;
        Long memberId = 2L;
        RoommateBoard board = createRoommateBoard(10L);
        Member member = Member.builder().id(memberId).build();
        AtomicReference<RoommateBoardInterest> savedInterest = new AtomicReference<>();

        when(roommateBoardRepository.findByIdForUpdate(boardId)).thenReturn(Optional.of(board));
        when(memberService.findById(memberId)).thenReturn(Optional.of(member));
        when(roommateBoardInterestRepository.findByRoommateBoardAndMember(board, member))
                .thenAnswer(invocation -> Optional.ofNullable(savedInterest.get()));
        when(roommateBoardInterestRepository.save(any(RoommateBoardInterest.class))).thenAnswer(invocation -> {
            RoommateBoardInterest interest = invocation.getArgument(0);
            savedInterest.set(interest);
            return interest;
        });

        roommateBoardService.likeBoard(boardId, memberId);
        assertThat(savedInterest.get().getIsDeleted()).isFalse();

        roommateBoardService.likeBoard(boardId, memberId);

        assertThat(savedInterest.get().getIsDeleted()).isTrue();
    }

    @Test
    @DisplayName("좋아요할 게시글이 없으면 예외를 던진다")
    void likeBoardThrowsWhenBoardNotFound() {
        Long boardId = 1L;

        when(roommateBoardRepository.findByIdForUpdate(boardId)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> roommateBoardService.likeBoard(boardId, 2L))
                .isInstanceOfSatisfying(BusinessException.class,
                        e -> assertThat(e.getErrorCode()).isEqualTo(RoommateBoardErrorCode.ROOMMATE_BOARD_NOT_FOUND));
        verifyNoInteractions(roommateBoardInterestRepository);
    }

    @Test
    @DisplayName("좋아요할 회원이 없으면 예외를 던진다")
    void likeBoardThrowsWhenMemberNotFound() {
        Long boardId = 1L;
        Long memberId = 2L;
        RoommateBoard board = createRoommateBoard(10L);

        when(roommateBoardRepository.findByIdForUpdate(boardId)).thenReturn(Optional.of(board));
        when(memberService.findById(memberId)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> roommateBoardService.likeBoard(boardId, memberId))
                .isInstanceOfSatisfying(BusinessException.class,
                        e -> assertThat(e.getErrorCode()).isEqualTo(MemberErrorCode.MEMBER_NOT_FOUND));
        verifyNoInteractions(roommateBoardInterestRepository);
    }

    @Test
    @DisplayName("게시글 작성자가 삭제를 요청하면 게시글을 삭제 상태로 변경한다")
    void deleteBoardSoftDeletesWhenRequesterIsOwner() {
        // Given
        Long boardId = 1L;
        Long memberId = 7L;
        RoommateBoard board = createRoommateBoard(memberId);
        when(roommateBoardRepository.findById(boardId)).thenReturn(Optional.of(board));

        // When
        BoardDto.Response response = roommateBoardService.deleteBoard(boardId, memberId);

        // Then
        assertThat(board.getIsDeleted()).isTrue();
        assertThat(response.getUpdatedAt()).isNotNull();
    }

    @Test
    @DisplayName("다른 회원의 게시글을 삭제하면 권한 없음 예외를 던지고 삭제하지 않는다")
    void deleteBoardThrowsWhenRequesterIsNotOwner() {
        // Given
        Long boardId = 1L;
        Long ownerId = 7L;
        Long requesterId = 999L;
        RoommateBoard board = createRoommateBoard(ownerId);
        when(roommateBoardRepository.findById(boardId)).thenReturn(Optional.of(board));

        // When & Then
        assertThatThrownBy(() -> roommateBoardService.deleteBoard(boardId, requesterId))
                .isInstanceOfSatisfying(BusinessException.class,
                        e -> assertThat(e.getErrorCode()).isEqualTo(RoommateBoardErrorCode.ROOMMATE_BOARD_FORBIDDEN));
        assertThat(board.getIsDeleted()).isFalse();
    }

    @Test
    @DisplayName("존재하지 않는 게시글을 삭제하면 게시글 없음 예외를 던진다")
    void deleteBoardThrowsWhenBoardDoesNotExist() {
        // Given
        Long boardId = 999L;
        when(roommateBoardRepository.findById(boardId)).thenReturn(Optional.empty());

        // When & Then
        assertThatThrownBy(() -> roommateBoardService.deleteBoard(boardId, 7L))
                .isInstanceOfSatisfying(BusinessException.class,
                        e -> assertThat(e.getErrorCode()).isEqualTo(RoommateBoardErrorCode.ROOMMATE_BOARD_NOT_FOUND));
    }

    @Test
    @DisplayName("게시글 신고를 요청하면 신고 회원과 게시글 및 사유를 저장한다")
    void reportBoardSavesDeclarationWithMemberBoardAndReason() {
        // Given
        Long boardId = 1L;
        Long memberId = 7L;
        ReportDto.Request request = createReportRequest("부적절한 게시글입니다.");
        RoommateBoard board = createRoommateBoard(10L);
        Member member = Member.builder().id(memberId).build();

        when(roommateBoardRepository.findById(boardId)).thenReturn(Optional.of(board));
        when(memberService.findById(memberId)).thenReturn(Optional.of(member));
        when(roommateBoardDeclarationRepository.findByRoommateBoardAndMember(board, member))
                .thenReturn(Optional.empty());

        // When
        ReportDto.Response response = roommateBoardService.reportBoard(request, boardId, memberId);

        // Then
        verify(roommateBoardDeclarationRepository).save(roommateBoardDeclarationCaptor.capture());
        RoommateBoardDeclaration declaration = roommateBoardDeclarationCaptor.getValue();
        assertThat(declaration.getRoommateBoard()).isSameAs(board);
        assertThat(declaration.getMember()).isSameAs(member);
        assertThat(declaration.getReason()).isEqualTo("부적절한 게시글입니다.");
        assertThat(response.getUpdatedAt()).isNotNull();
    }

    @Test
    @DisplayName("이미 신고한 게시글을 다시 신고하면 중복 신고 예외를 던지고 저장하지 않는다")
    void reportBoardThrowsWhenDeclarationAlreadyExists() {
        // Given
        Long boardId = 1L;
        Long memberId = 7L;
        RoommateBoard board = createRoommateBoard(10L);
        Member member = Member.builder().id(memberId).build();
        RoommateBoardDeclaration existingDeclaration = RoommateBoardDeclaration.builder()
                .roommateBoard(board)
                .member(member)
                .reason("기존 신고 사유")
                .build();

        when(roommateBoardRepository.findById(boardId)).thenReturn(Optional.of(board));
        when(memberService.findById(memberId)).thenReturn(Optional.of(member));
        when(roommateBoardDeclarationRepository.findByRoommateBoardAndMember(board, member))
                .thenReturn(Optional.of(existingDeclaration));

        // When & Then
        assertThatThrownBy(() -> roommateBoardService.reportBoard(createReportRequest("다시 신고합니다."), boardId, memberId))
                .isInstanceOfSatisfying(BusinessException.class,
                        e -> assertThat(e.getErrorCode())
                                .isEqualTo(RoommateBoardErrorCode.ROOMMATE_BOARD_DECLARATION_DUPLICATE));
        verify(roommateBoardDeclarationRepository, never()).save(any(RoommateBoardDeclaration.class));
    }

    @Test
    @DisplayName("존재하지 않는 게시글을 신고하면 게시글 없음 예외를 던지고 회원을 조회하지 않는다")
    void reportBoardThrowsWhenBoardDoesNotExist() {
        // Given
        Long boardId = 999L;
        when(roommateBoardRepository.findById(boardId)).thenReturn(Optional.empty());

        // When & Then
        assertThatThrownBy(() -> roommateBoardService.reportBoard(createReportRequest("신고 사유"), boardId, 7L))
                .isInstanceOfSatisfying(BusinessException.class,
                        e -> assertThat(e.getErrorCode()).isEqualTo(RoommateBoardErrorCode.ROOMMATE_BOARD_NOT_FOUND));
        verifyNoInteractions(roommateBoardDeclarationRepository);
        verifyNoInteractions(memberService);
    }

    @Test
    @DisplayName("존재하지 않는 회원이 신고하면 회원 없음 예외를 던지고 신고를 저장하지 않는다")
    void reportBoardThrowsWhenMemberDoesNotExist() {
        // Given
        Long boardId = 1L;
        Long memberId = 7L;
        RoommateBoard board = createRoommateBoard(10L);

        when(roommateBoardRepository.findById(boardId)).thenReturn(Optional.of(board));
        when(memberService.findById(memberId)).thenReturn(Optional.empty());

        // When & Then
        assertThatThrownBy(() -> roommateBoardService.reportBoard(createReportRequest("신고 사유"), boardId, memberId))
                .isInstanceOfSatisfying(BusinessException.class,
                        e -> assertThat(e.getErrorCode()).isEqualTo(MemberErrorCode.MEMBER_NOT_FOUND));
        verifyNoInteractions(roommateBoardDeclarationRepository);
    }

    @Test
    @DisplayName("이미지 업로드가 완료되면 트랜잭션 안에서 게시글과 파일 연결 정보를 저장한다")
    void savePersistsBoardAndUploadedImageLinksInTransaction() throws IOException {
        MultipartFile thumbnailImage = emptyMultipartFile();
        MultipartFile roomImage = emptyMultipartFile();
        BoardDto.Request request = createRequest(
                createFileDto(0, true),
                createFileDto(1, false));
        Long memberId = 42L;
        Member member = org.mockito.Mockito.mock(Member.class);
        RoomType roomType = org.mockito.Mockito.mock(RoomType.class);
        Region region = org.mockito.Mockito.mock(Region.class);
        File thumbnailFile = createFile("thumbnail.jpg", "saved-thumbnail.jpg", "jpg");
        File roomFile = createFile("room.png", "saved-room.png", "png");
        RoommateBoard savedRoommateBoard = org.mockito.Mockito.mock(RoommateBoard.class);
        LocalDateTime updatedAt = LocalDateTime.of(2026, 6, 4, 16, 30);

        when(memberService.findById(memberId)).thenReturn(Optional.of(member));
        when(metaService.findByRoomTypeId(1L)).thenReturn(Optional.of(roomType));
        when(metaService.findByRegionId(2L)).thenReturn(Optional.of(region));
        when(fileService.upload(thumbnailImage, FileType.ROOMMATE_BOARD_IMAGE)).thenReturn(thumbnailFile);
        when(fileService.upload(roomImage, FileType.ROOMMATE_BOARD_IMAGE)).thenReturn(roomFile);
        when(roommateBoardRepository.save(any(RoommateBoard.class))).thenReturn(savedRoommateBoard);
        when(savedRoommateBoard.getUpdatedAt()).thenReturn(updatedAt);

        BoardDto.Response response = roommateBoardService.save(request, memberId, List.of(thumbnailImage, roomImage));

        verify(roommateBoardRepository).save(roommateBoardCaptor.capture());
        RoommateBoard boardToSave = roommateBoardCaptor.getValue();
        assertThat(boardToSave.getMember()).isSameAs(member);
        assertThat(boardToSave.getTitle()).isEqualTo("Looking for a roommate");
        assertThat(boardToSave.getContents()).isEqualTo("Quiet home near the station");
        assertThat(boardToSave.getDeposit()).isEqualTo(10_000);
        assertThat(boardToSave.getMonthlyRent()).isEqualTo(500);
        assertThat(boardToSave.getManagementCost()).isEqualTo(50);
        assertThat(boardToSave.getRoomType()).isSameAs(roomType);
        assertThat(boardToSave.getRegion()).isSameAs(region);
        assertThat(boardToSave.getComeableDate()).isEqualTo(request.getComeableAt());
        assertThat(boardToSave.getIsDeleted()).isFalse();
        assertThat(boardToSave.getHits()).isZero();

        verify(fileRepository).saveAll(filesCaptor.capture());
        List<File> files = toList(filesCaptor.getValue());
        assertThat(files).containsExactly(thumbnailFile, roomFile);

        verify(roommateBoardFileRepository).saveAll(boardFilesCaptor.capture());
        List<RoommateBoardFile> boardFiles = toList(boardFilesCaptor.getValue());
        assertThat(boardFiles).hasSize(2);
        assertThat(boardFiles).extracting(RoommateBoardFile::getRoommateBoard)
                .containsOnly(savedRoommateBoard);
        assertThat(boardFiles).filteredOn(RoommateBoardFile::getIsThumbnail)
                .singleElement()
                .extracting(RoommateBoardFile::getFile)
                .isSameAs(thumbnailFile);
        assertThat(boardFiles).filteredOn(boardFile -> !boardFile.getIsThumbnail())
                .singleElement()
                .extracting(RoommateBoardFile::getFile)
                .isSameAs(roomFile);
        assertThat(response.getUpdatedAt()).isEqualTo(updatedAt);
    }

    @Test
    @DisplayName("존재하지 않는 회원으로 저장을 요청하면 이미지 업로드와 트랜잭션 저장을 수행하지 않는다")
    void saveThrowsWhenMemberDoesNotExist() {
        BoardDto.Request request = createRequest(createFileDto(0, true));
        Long memberId = 42L;
        when(memberService.findById(memberId)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> roommateBoardService.save(request, memberId, List.of(emptyMultipartFile())))
                .isInstanceOfSatisfying(BusinessException.class,
                        e -> assertThat(e.getErrorCode()).isEqualTo(MemberErrorCode.MEMBER_NOT_FOUND));

        verifyNoInteractions(metaService, fileService, roommateBoardRepository, roommateBoardFileRepository);
    }

    @Test
    @DisplayName("존재하지 않는 방 유형으로 저장을 요청하면 이미지 업로드와 트랜잭션 저장을 수행하지 않는다")
    void saveThrowsWhenRoomTypeDoesNotExist() {
        BoardDto.Request request = createRequest(createFileDto(0, true));
        Long memberId = 42L;
        Member member = org.mockito.Mockito.mock(Member.class);
        when(memberService.findById(memberId)).thenReturn(Optional.of(member));
        when(metaService.findByRoomTypeId(1L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> roommateBoardService.save(request, memberId, List.of(emptyMultipartFile())))
                .isInstanceOfSatisfying(BusinessException.class,
                        e -> assertThat(e.getErrorCode()).isEqualTo(MetaErrorCode.ROOM_TYPE_NOT_FOUND));

        verifyNoInteractions(fileService, roommateBoardRepository, roommateBoardFileRepository);
    }

    @Test
    @DisplayName("존재하지 않는 지역으로 저장을 요청하면 이미지 업로드와 트랜잭션 저장을 수행하지 않는다")
    void saveThrowsWhenRegionDoesNotExist() {
        BoardDto.Request request = createRequest(createFileDto(0, true));
        Long memberId = 42L;
        Member member = org.mockito.Mockito.mock(Member.class);
        RoomType roomType = org.mockito.Mockito.mock(RoomType.class);
        when(memberService.findById(memberId)).thenReturn(Optional.of(member));
        when(metaService.findByRoomTypeId(1L)).thenReturn(Optional.of(roomType));
        when(metaService.findByRegionId(2L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> roommateBoardService.save(request, memberId, List.of(emptyMultipartFile())))
                .isInstanceOfSatisfying(BusinessException.class,
                        e -> assertThat(e.getErrorCode()).isEqualTo(MetaErrorCode.REGION_NOT_FOUND));

        verifyNoInteractions(fileService, roommateBoardRepository, roommateBoardFileRepository);
    }

    @Test
    @DisplayName("이미지 업로드가 실패하면 트랜잭션 저장을 수행하지 않고 업로드 실패 예외를 던진다")
    void saveThrowsWhenFileUploadFails() throws IOException {
        MultipartFile thumbnailImage = emptyMultipartFile();
        BoardDto.Request request = createRequest(createFileDto(0, true));
        Long memberId = 42L;
        Member member = org.mockito.Mockito.mock(Member.class);
        RoomType roomType = org.mockito.Mockito.mock(RoomType.class);
        Region region = org.mockito.Mockito.mock(Region.class);
        when(memberService.findById(memberId)).thenReturn(Optional.of(member));
        when(metaService.findByRoomTypeId(1L)).thenReturn(Optional.of(roomType));
        when(metaService.findByRegionId(2L)).thenReturn(Optional.of(region));
        when(fileService.upload(thumbnailImage, FileType.ROOMMATE_BOARD_IMAGE))
                .thenThrow(new IOException("upload failed"));

        assertThatThrownBy(() -> roommateBoardService.save(request, memberId, List.of(thumbnailImage)))
                .isInstanceOfSatisfying(BusinessException.class,
                        e -> assertThat(e.getErrorCode()).isEqualTo(FileErrorCode.FILE_UPLOAD_FAILED));

        verifyNoInteractions(roommateBoardRepository, roommateBoardFileRepository);
    }

    @Test
    @DisplayName("두 번째 이미지 업로드가 실패하면 먼저 업로드된 이미지를 삭제 요청하고 트랜잭션 저장을 수행하지 않는다")
    void saveDeletesUploadedImagesWhenLaterUploadFails() throws IOException {
        MultipartFile thumbnailImage = emptyMultipartFile();
        MultipartFile roomImage = emptyMultipartFile();
        BoardDto.Request request = createRequest(
                createFileDto(0, true),
                createFileDto(1, false));
        Long memberId = 42L;
        Member member = org.mockito.Mockito.mock(Member.class);
        RoomType roomType = org.mockito.Mockito.mock(RoomType.class);
        Region region = org.mockito.Mockito.mock(Region.class);
        File thumbnailFile = createFile("thumbnail.jpg", "saved-thumbnail.jpg", "jpg");
        when(memberService.findById(memberId)).thenReturn(Optional.of(member));
        when(metaService.findByRoomTypeId(1L)).thenReturn(Optional.of(roomType));
        when(metaService.findByRegionId(2L)).thenReturn(Optional.of(region));
        when(fileService.upload(thumbnailImage, FileType.ROOMMATE_BOARD_IMAGE)).thenReturn(thumbnailFile);
        when(fileService.upload(roomImage, FileType.ROOMMATE_BOARD_IMAGE))
                .thenThrow(new IOException("upload failed"));

        assertThatThrownBy(() -> roommateBoardService.save(request, memberId, List.of(thumbnailImage, roomImage)))
                .isInstanceOfSatisfying(BusinessException.class,
                        e -> assertThat(e.getErrorCode()).isEqualTo(FileErrorCode.FILE_UPLOAD_FAILED));

        verify(fileService).deleteAll(uploadedFilesCaptor.capture());
        assertThat(uploadedFilesCaptor.getValue()).containsExactly(thumbnailFile);
        verifyNoInteractions(roommateBoardRepository, roommateBoardFileRepository);
    }

    @Test
    @DisplayName("트랜잭션 저장이 실패하면 업로드된 이미지를 삭제 요청하고 예외를 다시 던진다")
    void saveDeletesUploadedImagesWhenTransactionalSaveFails() throws IOException {
        MultipartFile thumbnailImage = emptyMultipartFile();
        BoardDto.Request request = createRequest(createFileDto(0, true));
        Long memberId = 42L;
        Member member = org.mockito.Mockito.mock(Member.class);
        RoomType roomType = org.mockito.Mockito.mock(RoomType.class);
        Region region = org.mockito.Mockito.mock(Region.class);
        File thumbnailFile = createFile("thumbnail.jpg", "saved-thumbnail.jpg", "jpg");
        when(memberService.findById(memberId)).thenReturn(Optional.of(member));
        when(metaService.findByRoomTypeId(1L)).thenReturn(Optional.of(roomType));
        when(metaService.findByRegionId(2L)).thenReturn(Optional.of(region));
        when(fileService.upload(thumbnailImage, FileType.ROOMMATE_BOARD_IMAGE)).thenReturn(thumbnailFile);
        when(roommateBoardRepository.save(any(RoommateBoard.class))).thenThrow(new IllegalStateException("db failed"));

        assertThatThrownBy(() -> roommateBoardService.save(request, memberId, List.of(thumbnailImage)))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("db failed");

        verify(fileService).deleteAll(uploadedFilesCaptor.capture());
        assertThat(uploadedFilesCaptor.getValue()).containsExactly(thumbnailFile);
        verifyNoInteractions(roommateBoardFileRepository);
    }

    @Test
    @DisplayName("이미지 없이 저장을 요청하면 게시글만 저장하고 파일 업로드를 수행하지 않는다")
    void saveAllowsRequestWithoutImages() {
        BoardDto.Request request = createRequest();
        Long memberId = 42L;
        Member member = org.mockito.Mockito.mock(Member.class);
        RoomType roomType = org.mockito.Mockito.mock(RoomType.class);
        Region region = org.mockito.Mockito.mock(Region.class);
        RoommateBoard savedRoommateBoard = org.mockito.Mockito.mock(RoommateBoard.class);
        LocalDateTime updatedAt = LocalDateTime.of(2026, 6, 4, 16, 30);

        when(memberService.findById(memberId)).thenReturn(Optional.of(member));
        when(metaService.findByRoomTypeId(1L)).thenReturn(Optional.of(roomType));
        when(metaService.findByRegionId(2L)).thenReturn(Optional.of(region));
        when(roommateBoardRepository.save(any(RoommateBoard.class))).thenReturn(savedRoommateBoard);
        when(savedRoommateBoard.getUpdatedAt()).thenReturn(updatedAt);

        BoardDto.Response response = roommateBoardService.save(request, memberId, null);

        assertThat(response.getUpdatedAt()).isEqualTo(updatedAt);
        verifyNoInteractions(fileService);
        verify(fileRepository).saveAll(filesCaptor.capture());
        assertThat(toList(filesCaptor.getValue())).isEmpty();
        verify(roommateBoardFileRepository).saveAll(boardFilesCaptor.capture());
        assertThat(toList(boardFilesCaptor.getValue())).isEmpty();
    }

    @Test
    @DisplayName("게시글 저장 중 이미지 메타데이터가 파일 파트와 매칭되지 않으면 잘못된 요청 예외를 던진다")
    void saveThrowsWhenImageFileIndexDoesNotMatchFilesPart() throws IOException {
        BoardDto.Request request = createRequest(createFileDto(1, true));
        Long memberId = 42L;
        Member member = org.mockito.Mockito.mock(Member.class);
        RoomType roomType = org.mockito.Mockito.mock(RoomType.class);
        Region region = org.mockito.Mockito.mock(Region.class);

        when(memberService.findById(memberId)).thenReturn(Optional.of(member));
        when(metaService.findByRoomTypeId(1L)).thenReturn(Optional.of(roomType));
        when(metaService.findByRegionId(2L)).thenReturn(Optional.of(region));

        assertThatThrownBy(() -> roommateBoardService.save(request, memberId, List.of(emptyMultipartFile())))
                .isInstanceOfSatisfying(BusinessException.class,
                        e -> assertThat(e.getErrorCode()).isEqualTo(CommonErrorCode.BAD_REQUEST));
        verify(fileService, never()).upload(any(MultipartFile.class), any(FileType.class));
        verify(fileService).deleteAll(List.of());
        verifyNoInteractions(roommateBoardRepository, fileRepository, roommateBoardFileRepository);
    }

    @Test
    @DisplayName("목록 조회 시 입주 가능시기 노출 기준을 현재 시각 기준 7일 전으로 전달한다")
    void getBoardListPassesComeableDateGraceEndDate() {
        BoardListDto.Request request = new BoardListDto.Request();
        Pageable pageable = PageRequest.of(0, 20);
        LocalDateTime beforeEndDate = LocalDateTime.now()
                .minusDays(RoommateBoard.COMEABLE_DATE_VISIBLE_GRACE_DAYS);
        when(roommateBoardRepository.search(any(RoommateBoardSearchCondition.class)))
                .thenReturn(new PageImpl<>(List.of(), pageable, 0));

        roommateBoardService.getBoardList(request, pageable);

        LocalDateTime afterEndDate = LocalDateTime.now()
                .minusDays(RoommateBoard.COMEABLE_DATE_VISIBLE_GRACE_DAYS);
        verify(roommateBoardRepository).search(searchConditionCaptor.capture());
        RoommateBoardSearchCondition condition = searchConditionCaptor.getValue();
        assertThat(condition.endDate()).isBetween(beforeEndDate, afterEndDate);
        assertThat(condition.pageable()).isSameAs(pageable);
    }

    @Test
    @DisplayName("상세 조회는 조회수 증가 후 원천 데이터를 응답 DTO로 조립한다")
    void getBoardDetailIncreasesHitsThenComposesResponse() {
        // Given
        Long boardId = 1L;
        Long ownerId = 7L;
        Long viewerId = 42L;
        LocalDate birth = LocalDate.of(1998, 1, 1);
        BasicInfoRow basicInfoRow = createBasicInfoRow(boardId, ownerId, birth);
        List<BoardDetailDto.Response.FileDetailDto> images = List.of(
                new BoardDetailDto.Response.FileDetailDto(101L, "thumbnail.jpg"),
                new BoardDetailDto.Response.FileDetailDto(102L, "room.jpg")
        );
        List<String> roomExtraOptionNames = List.of("풀옵션", "주차 가능");
        BoardDetailDto.Response.Lifestyle sleep = new BoardDetailDto.Response.Lifestyle(
                1L, "취침", "23:00", "일찍 자요", LifePatternType.SCALE);
        BoardDetailDto.Response.Lifestyle visitor = new BoardDetailDto.Response.Lifestyle(
                2L, "방문객", "가끔", "가끔 방문해요", LifePatternType.SINGLE_CHOICE);
        List<BoardDetailDto.Response.Condition> conditions = List.of(
                new BoardDetailDto.Response.Condition(3L, "흡연", "비흡연", "비흡연 선호", LifePatternType.BOOLEAN)
        );
        List<BoardDetailDto.Response.ConditionWeight> conditionWeights = List.of(
                new BoardDetailDto.Response.ConditionWeight(4L, "청결")
        );
        List<AuthenticationType> authenticationTypes = List.of(AuthenticationType.STUDENT);

        when(roommateBoardRepository.increaseHitsById(boardId)).thenReturn(1);
        when(roommateBoardRepository.getBasicInfo(boardId)).thenReturn(Optional.of(basicInfoRow));
        when(roommateBoardFileRepository.getFileDetailDtoByBoardId(boardId)).thenReturn(images);
        when(roommateBoardOptionRepository.getExtraOptionsNameByBoardId(boardId)).thenReturn(roomExtraOptionNames);
        when(memberLifePatternRepository.getLifeStyleDto(ownerId)).thenReturn(List.of(visitor, sleep));
        when(preferenceConditionRepository.getConditionDtoByMemberId(ownerId)).thenReturn(conditions);
        when(preferenceConditionWeightRepository.getConditionWeightDtoByMemberId(ownerId)).thenReturn(conditionWeights);
        when(authenticationRepository.getAcceptedAuthenticationTypeByMemberId(ownerId)).thenReturn(authenticationTypes);
        when(roommateBoardInterestRepository.existsByRoommateBoardIdAndMemberIdAndIsDeletedIsFalse(boardId, viewerId))
                .thenReturn(true);

        // When
        BoardDetailDto.Response response = roommateBoardService.getBoardDetail(boardId, viewerId);

        // Then
        assertThat(response.getBoardId()).isEqualTo(boardId);
        assertThat(response.getImages()).isSameAs(images);
        assertThat(response.getTitle()).isEqualTo("상세 게시글");
        assertThat(response.getDeposit()).isEqualTo(1_000);
        assertThat(response.getManagementCost()).isEqualTo(10);
        assertThat(response.getMonthlyRent()).isEqualTo(50);
        assertThat(response.getRoomTypeName()).isEqualTo("원룸");
        assertThat(response.getRegionFullName()).isEqualTo("서울 강남구 역삼동");
        assertThat(response.getHits()).isEqualTo(3L);
        assertThat(response.getContents()).isEqualTo("상세 내용");
        assertThat(response.getRoomExtraOptionNames()).isSameAs(roomExtraOptionNames);
        assertThat(response.getLifeStyles()).containsExactly(visitor, sleep);
        assertThat(response.getConditions()).isSameAs(conditions);
        assertThat(response.getConditionWeights()).isSameAs(conditionWeights);
        assertThat(response.getMemberName()).isEqualTo("상세작성자");
        assertThat(response.getMemberProfileImageUrl()).isEqualTo("profile.jpg");
        assertThat(response.getMemberAge()).isEqualTo(Period.between(birth, LocalDate.now()).getYears());
        assertThat(response.getGender()).isEqualTo(Gender.FEMALE);
        assertThat(response.getAuthentications()).isSameAs(authenticationTypes);
        assertThat(response.getCompatibility()).isNotNull();
        assertThat(response.isInterested()).isTrue();
        InOrder inOrder = inOrder(roommateBoardRepository);
        inOrder.verify(roommateBoardRepository).increaseHitsById(boardId);
        inOrder.verify(roommateBoardRepository).getBasicInfo(boardId);
        verify(roommateBoardFileRepository).getFileDetailDtoByBoardId(boardId);
        verify(roommateBoardOptionRepository).getExtraOptionsNameByBoardId(boardId);
        verify(memberLifePatternRepository).getLifeStyleDto(ownerId);
        verify(preferenceConditionRepository).getConditionDtoByMemberId(ownerId);
        verify(preferenceConditionWeightRepository).getConditionWeightDtoByMemberId(ownerId);
        verify(authenticationRepository).getAcceptedAuthenticationTypeByMemberId(ownerId);
        verify(roommateBoardInterestRepository).existsByRoommateBoardIdAndMemberIdAndIsDeletedIsFalse(boardId, viewerId);
    }

    @Test
    @DisplayName("상세 조회는 생활패턴과 중요 조건이 없으면 빈 목록을 응답한다")
    void getBoardDetailReturnsEmptyListsWhenMemberHasNoLifeStylesAndConditionWeights() {
        // Given
        Long boardId = 1L;
        Long ownerId = 7L;
        Long viewerId = 42L;
        BasicInfoRow basicInfoRow = createBasicInfoRow(boardId, ownerId, LocalDate.of(1998, 1, 1));
        when(roommateBoardRepository.increaseHitsById(boardId)).thenReturn(1);
        when(roommateBoardRepository.getBasicInfo(boardId)).thenReturn(Optional.of(basicInfoRow));
        when(roommateBoardFileRepository.getFileDetailDtoByBoardId(boardId)).thenReturn(List.of());
        when(roommateBoardOptionRepository.getExtraOptionsNameByBoardId(boardId)).thenReturn(List.of());
        when(memberLifePatternRepository.getLifeStyleDto(ownerId)).thenReturn(List.of());
        when(preferenceConditionRepository.getConditionDtoByMemberId(ownerId)).thenReturn(List.of());
        when(preferenceConditionWeightRepository.getConditionWeightDtoByMemberId(ownerId)).thenReturn(List.of());
        when(authenticationRepository.getAcceptedAuthenticationTypeByMemberId(ownerId)).thenReturn(List.of());
        when(roommateBoardInterestRepository.existsByRoommateBoardIdAndMemberIdAndIsDeletedIsFalse(boardId, viewerId))
                .thenReturn(false);

        // When
        BoardDetailDto.Response response = roommateBoardService.getBoardDetail(boardId, viewerId);

        // Then
        assertThat(response.getLifeStyles()).isEmpty();
        assertThat(response.getConditionWeights()).isEmpty();
        assertThat(response.isInterested()).isFalse();
        verify(roommateBoardInterestRepository).existsByRoommateBoardIdAndMemberIdAndIsDeletedIsFalse(boardId, viewerId);
    }

    @Test
    @DisplayName("상세 조회는 로그인 회원이 관심을 누르지 않은 게시글이면 관심 여부를 false로 응답한다")
    void getBoardDetailReturnsFalseWhenViewerIsNotInterested() {
        // Given
        Long boardId = 1L;
        Long ownerId = 7L;
        Long viewerId = 42L;
        BasicInfoRow basicInfoRow = createBasicInfoRow(boardId, ownerId, LocalDate.of(1998, 1, 1));
        when(roommateBoardRepository.increaseHitsById(boardId)).thenReturn(1);
        when(roommateBoardRepository.getBasicInfo(boardId)).thenReturn(Optional.of(basicInfoRow));
        when(roommateBoardFileRepository.getFileDetailDtoByBoardId(boardId)).thenReturn(List.of());
        when(roommateBoardOptionRepository.getExtraOptionsNameByBoardId(boardId)).thenReturn(List.of());
        when(memberLifePatternRepository.getLifeStyleDto(ownerId)).thenReturn(List.of());
        when(preferenceConditionRepository.getConditionDtoByMemberId(ownerId)).thenReturn(List.of());
        when(preferenceConditionWeightRepository.getConditionWeightDtoByMemberId(ownerId)).thenReturn(List.of());
        when(authenticationRepository.getAcceptedAuthenticationTypeByMemberId(ownerId)).thenReturn(List.of());
        when(roommateBoardInterestRepository.existsByRoommateBoardIdAndMemberIdAndIsDeletedIsFalse(boardId, viewerId))
                .thenReturn(false);

        // When
        BoardDetailDto.Response response = roommateBoardService.getBoardDetail(boardId, viewerId);

        // Then
        assertThat(response.isInterested()).isFalse();
        verify(roommateBoardInterestRepository).existsByRoommateBoardIdAndMemberIdAndIsDeletedIsFalse(boardId, viewerId);
    }

    @Test
    @DisplayName("상세 조회는 조회수 증가 대상이 없으면 게시글 없음 예외를 던진다")
    void getBoardDetailThrowsWhenHitUpdateDoesNotAffectBoard() {
        // Given
        Long boardId = 999L;
        Long viewerId = 42L;
        when(roommateBoardRepository.increaseHitsById(boardId)).thenReturn(0);

        // When & Then
        assertThatThrownBy(() -> roommateBoardService.getBoardDetail(boardId, viewerId))
                .isInstanceOfSatisfying(BusinessException.class,
                        e -> assertThat(e.getErrorCode()).isEqualTo(RoommateBoardErrorCode.ROOMMATE_BOARD_NOT_FOUND));
        verify(roommateBoardRepository).increaseHitsById(boardId);
        verify(roommateBoardRepository, never()).getBasicInfo(any());
        verifyNoInteractions(roommateBoardFileRepository, roommateBoardOptionRepository,
                memberLifePatternRepository, preferenceConditionRepository, preferenceConditionWeightRepository,
                authenticationRepository);
    }

    @Test
    @DisplayName("상세 조회는 조회수 증가 후 기본 정보가 없으면 게시글 없음 예외를 던진다")
    void getBoardDetailThrowsWhenBasicInfoDoesNotExistAfterHitUpdate() {
        // Given
        Long boardId = 999L;
        Long viewerId = 42L;
        when(roommateBoardRepository.increaseHitsById(boardId)).thenReturn(1);
        when(roommateBoardRepository.getBasicInfo(boardId)).thenReturn(Optional.empty());

        // When & Then
        assertThatThrownBy(() -> roommateBoardService.getBoardDetail(boardId, viewerId))
                .isInstanceOfSatisfying(BusinessException.class,
                        e -> assertThat(e.getErrorCode()).isEqualTo(RoommateBoardErrorCode.ROOMMATE_BOARD_NOT_FOUND));
        verify(roommateBoardRepository).increaseHitsById(boardId);
        verify(roommateBoardRepository).getBasicInfo(boardId);
        verifyNoInteractions(roommateBoardFileRepository, roommateBoardOptionRepository,
                memberLifePatternRepository, preferenceConditionRepository, preferenceConditionWeightRepository,
                authenticationRepository);
    }

    @Test
    @DisplayName("수정 폼 조회는 게시글 원천 데이터와 회원 설정 정보를 응답 DTO로 조립한다")
    void getEditFormComposesResponseFromBoardAndMemberSettings() {
        // Given
        Long boardId = 1L;
        Long memberId = 7L;
        EditFormRow editFormRow = createEditFormRow();
        List<BoardDetailDto.Response.FileDetailDto> images = List.of(
                new BoardDetailDto.Response.FileDetailDto(101L, "thumbnail.jpg"),
                new BoardDetailDto.Response.FileDetailDto(102L, "room.jpg")
        );
        List<BoardOptionInfo> roomExtraOptions = List.of(
                new BoardOptionInfo(201L, "풀옵션"),
                new BoardOptionInfo(202L, "주차 가능")
        );
        BoardDetailDto.Response.Lifestyle lifestyle = new BoardDetailDto.Response.Lifestyle(
                1L, "취침", "23:00", "일찍 자요", LifePatternType.SCALE);
        BoardDetailDto.Response.Condition condition = new BoardDetailDto.Response.Condition(
                2L, "흡연", "비흡연", "비흡연 선호", LifePatternType.BOOLEAN);
        BoardDetailDto.Response.ConditionWeight conditionWeight = new BoardDetailDto.Response.ConditionWeight(
                3L, "청결");

        when(roommateBoardRepository.getEditRow(boardId)).thenReturn(Optional.of(editFormRow));
        when(roommateBoardFileRepository.getFileDetailDtoByBoardId(boardId)).thenReturn(images);
        when(roommateBoardOptionRepository.getExtraOptionsByBoardId(boardId)).thenReturn(roomExtraOptions);
        when(memberLifePatternRepository.getLifeStyleDto(memberId)).thenReturn(List.of(lifestyle));
        when(preferenceConditionRepository.getConditionDtoByMemberId(memberId)).thenReturn(List.of(condition));
        when(preferenceConditionWeightRepository.getConditionWeightDtoByMemberId(memberId))
                .thenReturn(List.of(conditionWeight));

        // When
        BoardEditDto.Response response = roommateBoardService.getEditForm(memberId, boardId);

        // Then
        assertThat(response.getImages()).isSameAs(images);
        assertThat(response.getTitle()).isEqualTo("수정 게시글");
        assertThat(response.getDeposit()).isEqualTo(1_000);
        assertThat(response.getMonthlyRent()).isEqualTo(50);
        assertThat(response.getManagementCost()).isEqualTo(10);
        assertThat(response.getRoomType().getRoomTypeId()).isEqualTo(11L);
        assertThat(response.getRoomType().getName()).isEqualTo("원룸");
        assertThat(response.getRegion().getRegionId()).isEqualTo(33L);
        assertThat(response.getRegion().getFullName()).isEqualTo("서울 강남구 역삼동");
        assertThat(response.getComeableAt()).isEqualTo(LocalDateTime.of(2026, 7, 1, 9, 0));
        assertThat(response.getRoomExtraOptions()).isSameAs(roomExtraOptions);
        assertThat(response.getContents()).isEqualTo("수정 내용");
        assertThat(response.getLifeStyles()).containsExactly(lifestyle);
        assertThat(response.getConditions()).containsExactly(condition);
        assertThat(response.getConditionWeights()).containsExactly(conditionWeight);
        verify(memberLifePatternRepository).getLifeStyleDto(memberId);
        verify(preferenceConditionRepository).getConditionDtoByMemberId(memberId);
        verify(preferenceConditionWeightRepository).getConditionWeightDtoByMemberId(memberId);
    }

    @Test
    @DisplayName("수정 폼 조회는 대상 게시글이 없으면 게시글 없음 예외를 던지고 부가 정보를 조회하지 않는다")
    void getEditFormThrowsWhenBoardDoesNotExist() {
        // Given
        Long boardId = 999L;
        Long memberId = 7L;
        when(roommateBoardRepository.getEditRow(boardId)).thenReturn(Optional.empty());

        // When & Then
        assertThatThrownBy(() -> roommateBoardService.getEditForm(memberId, boardId))
                .isInstanceOfSatisfying(BusinessException.class,
                        e -> assertThat(e.getErrorCode()).isEqualTo(RoommateBoardErrorCode.ROOMMATE_BOARD_NOT_FOUND));
        verify(roommateBoardRepository).getEditRow(boardId);
        verifyNoInteractions(roommateBoardFileRepository, roommateBoardOptionRepository,
                memberLifePatternRepository, preferenceConditionRepository, preferenceConditionWeightRepository);
    }

    @Test
    @DisplayName("게시글 수정은 기본 정보와 옵션 및 이미지 최종 상태를 반영한다")
    void modifyUpdatesBoardOptionsAndImageState() throws IOException {
        // Given
        Long boardId = 1L;
        RoommateBoard roommateBoard = createRoommateBoard();
        RoomType newRoomType = org.mockito.Mockito.mock(RoomType.class);
        Region newRegion = org.mockito.Mockito.mock(Region.class);
        BoardModifyDto.Request request = createModifyRequest();
        request.setDeleteExtraOptionIds(List.of(10L));
        request.setNewExtraOptionIds(List.of(20L, 21L));

        RoomExtraOption deleteExtraOption = mockExtraOption(10L);
        RoomExtraOption keepExtraOption = mockExtraOption(11L);
        RoomExtraOption newExtraOption = org.mockito.Mockito.mock(RoomExtraOption.class);
        RoomExtraOption anotherNewExtraOption = org.mockito.Mockito.mock(RoomExtraOption.class);
        RoommateBoardOption deleteBoardOption = RoommateBoardOption.builder()
                .roommateBoard(roommateBoard)
                .roomExtraOption(deleteExtraOption)
                .build();
        RoommateBoardOption keepBoardOption = RoommateBoardOption.builder()
                .roommateBoard(roommateBoard)
                .roomExtraOption(keepExtraOption)
                .build();

        RoommateBoardFile keptImage = RoommateBoardFile.builder()
                .id(101L)
                .roommateBoard(roommateBoard)
                .file(createFile("keep.jpg", "saved-keep.jpg", "jpg"))
                .isThumbnail(true)
                .build();
        RoommateBoardFile thumbnailImage = RoommateBoardFile.builder()
                .id(102L)
                .roommateBoard(roommateBoard)
                .file(createFile("thumbnail.jpg", "saved-thumbnail.jpg", "jpg"))
                .isThumbnail(false)
                .build();
        File deletedFile = createFile("delete.jpg", "saved-delete.jpg", "jpg");
        RoommateBoardFile deletedImage = RoommateBoardFile.builder()
                .id(103L)
                .roommateBoard(roommateBoard)
                .file(deletedFile)
                .isThumbnail(false)
                .build();
        List<RoommateBoardFile> persistedBoardFiles = new ArrayList<>(
                List.of(keptImage, thumbnailImage, deletedImage));

        MultipartFile newMultipartFile = emptyMultipartFile();
        File newFile = createFile("new.jpg", "saved-new.jpg", "jpg");
        request.setExistingImages(List.of(
                createExistingFileDto(101L, false),
                createExistingFileDto(102L, true)));
        request.setNewImages(List.of(createNewFileDto(0, false)));

        when(roommateBoardRepository.findById(boardId)).thenReturn(Optional.of(roommateBoard));
        when(metaService.findByRoomTypeId(11L)).thenReturn(Optional.of(newRoomType));
        when(metaService.findByRegionId(22L)).thenReturn(Optional.of(newRegion));
        when(roommateBoardOptionRepository.findWithRoomExtraOptionByBoardId(boardId))
                .thenReturn(List.of(deleteBoardOption, keepBoardOption));
        when(metaService.findRoomExtraOptionsByIdIn(List.of(20L, 21L)))
                .thenReturn(List.of(newExtraOption, anotherNewExtraOption));
        when(roommateBoardFileRepository.findByRoommateBoard(roommateBoard))
                .thenAnswer(invocation -> new ArrayList<>(persistedBoardFiles));
        org.mockito.Mockito.doAnswer(invocation -> {
            persistedBoardFiles.remove(invocation.getArgument(0));
            return null;
        }).when(roommateBoardFileRepository).delete(any(RoommateBoardFile.class));
        when(fileService.upload(newMultipartFile, FileType.ROOMMATE_BOARD_IMAGE)).thenReturn(newFile);
        when(fileRepository.save(newFile)).thenReturn(newFile);
        when(roommateBoardFileRepository.save(any(RoommateBoardFile.class))).thenAnswer(invocation -> {
            RoommateBoardFile boardFile = invocation.getArgument(0);
            persistedBoardFiles.add(boardFile);
            return boardFile;
        });

        // When
        BoardModifyDto.Response response = roommateBoardService.modify(7L, boardId, request, List.of(newMultipartFile));

        // Then
        assertThat(response.getUpdatedAt()).isNotNull();
        assertThat(roommateBoard.getTitle()).isEqualTo("수정 제목");
        assertThat(roommateBoard.getContents()).isEqualTo("수정 내용");
        assertThat(roommateBoard.getDeposit()).isEqualTo(2_000);
        assertThat(roommateBoard.getMonthlyRent()).isEqualTo(70);
        assertThat(roommateBoard.getManagementCost()).isEqualTo(15);
        assertThat(roommateBoard.getComeableDate()).isEqualTo(LocalDateTime.of(2026, 8, 1, 10, 0));
        assertThat(roommateBoard.getRoomType()).isSameAs(newRoomType);
        assertThat(roommateBoard.getRegion()).isSameAs(newRegion);

        verify(roommateBoardOptionRepository).deleteAll(boardOptionsCaptor.capture());
        assertThat(toList(boardOptionsCaptor.getValue())).containsExactly(deleteBoardOption);
        verify(roommateBoardOptionRepository).saveAll(boardOptionsCaptor.capture());
        assertThat(toList(boardOptionsCaptor.getValue()))
                .extracting(RoommateBoardOption::getRoomExtraOption)
                .containsExactly(newExtraOption, anotherNewExtraOption);

        assertThat(keptImage.getIsThumbnail()).isFalse();
        assertThat(thumbnailImage.getIsThumbnail()).isTrue();
        assertThat(deletedFile.getIsDeleted()).isTrue();
        verify(roommateBoardFileRepository).delete(deletedImage);
        verify(fileRepository).save(newFile);
        verify(roommateBoardFileRepository).save(boardFileCaptor.capture());
        RoommateBoardFile newBoardFile = boardFileCaptor.getValue();
        assertThat(newBoardFile.getRoommateBoard()).isSameAs(roommateBoard);
        assertThat(newBoardFile.getFile()).isSameAs(newFile);
        assertThat(newBoardFile.getIsThumbnail()).isFalse();
        assertThat(persistedBoardFiles).containsExactly(keptImage, thumbnailImage, newBoardFile);
    }

    @Test
    @DisplayName("게시글 수정 후 이미지가 10장을 초과하면 파일 개수 초과 예외를 던진다")
    void modifyThrowsWhenBoardImageCountExceedsLimit() {
        // Given
        Long boardId = 1L;
        RoommateBoard roommateBoard = createRoommateBoard();
        RoomType roomType = org.mockito.Mockito.mock(RoomType.class);
        Region region = org.mockito.Mockito.mock(Region.class);
        List<RoommateBoardFile> boardFiles = new ArrayList<>();
        List<ExistingFileDto> existingImages = new ArrayList<>();
        for (long id = 1; id <= 11; id++) {
            boardFiles.add(RoommateBoardFile.builder()
                    .id(id)
                    .roommateBoard(roommateBoard)
                    .file(createFile("room-" + id + ".jpg", "saved-room-" + id + ".jpg", "jpg"))
                    .isThumbnail(id == 1)
                    .build());
            existingImages.add(createExistingFileDto(id, id == 1));
        }
        BoardModifyDto.Request request = createModifyRequest();
        request.setExistingImages(existingImages);

        when(roommateBoardRepository.findById(boardId)).thenReturn(Optional.of(roommateBoard));
        when(metaService.findByRoomTypeId(11L)).thenReturn(Optional.of(roomType));
        when(metaService.findByRegionId(22L)).thenReturn(Optional.of(region));
        when(roommateBoardFileRepository.findByRoommateBoard(roommateBoard)).thenReturn(boardFiles);

        // When & Then
        assertThatThrownBy(() -> roommateBoardService.modify(7L, boardId, request, null))
                .isInstanceOfSatisfying(BusinessException.class,
                        e -> assertThat(e.getErrorCode())
                                .isEqualTo(RoommateBoardErrorCode.ROOMMATE_BOARD_FILE_COUNT_EXCEEDED));
    }

    @Test
    @DisplayName("게시글 수정 후 이미지가 없으면 썸네일 없이도 수정에 성공한다")
    void modifyAllowsEmptyImageResultWithoutThumbnail() {
        // Given
        Long boardId = 1L;
        RoommateBoard roommateBoard = createRoommateBoard();
        RoomType roomType = org.mockito.Mockito.mock(RoomType.class);
        Region region = org.mockito.Mockito.mock(Region.class);
        File deletedFile = createFile("delete.jpg", "saved-delete.jpg", "jpg");
        RoommateBoardFile deletedImage = RoommateBoardFile.builder()
                .id(101L)
                .roommateBoard(roommateBoard)
                .file(deletedFile)
                .isThumbnail(true)
                .build();
        List<RoommateBoardFile> persistedBoardFiles = new ArrayList<>(List.of(deletedImage));
        BoardModifyDto.Request request = createModifyRequest();
        request.setExistingImages(List.of());
        request.setNewImages(List.of());

        when(roommateBoardRepository.findById(boardId)).thenReturn(Optional.of(roommateBoard));
        when(metaService.findByRoomTypeId(11L)).thenReturn(Optional.of(roomType));
        when(metaService.findByRegionId(22L)).thenReturn(Optional.of(region));
        when(roommateBoardFileRepository.findByRoommateBoard(roommateBoard))
                .thenAnswer(invocation -> new ArrayList<>(persistedBoardFiles));
        org.mockito.Mockito.doAnswer(invocation -> {
            persistedBoardFiles.remove(invocation.getArgument(0));
            return null;
        }).when(roommateBoardFileRepository).delete(any(RoommateBoardFile.class));

        // When
        BoardModifyDto.Response response = roommateBoardService.modify(7L, boardId, request, null);

        // Then
        assertThat(response.getUpdatedAt()).isNotNull();
        assertThat(deletedFile.getIsDeleted()).isTrue();
        assertThat(persistedBoardFiles).isEmpty();
        verify(roommateBoardFileRepository).delete(deletedImage);
        verify(metaService, never()).findRoomExtraOptionsByIdIn(any());
        verify(fileRepository, never()).save(any(File.class));
    }

    @Test
    @DisplayName("게시글 수정 후 썸네일이 정확히 1장이 아니면 썸네일 개수 예외를 던진다")
    void modifyThrowsWhenThumbnailCountIsNotOne() {
        // Given
        Long boardId = 1L;
        RoommateBoard roommateBoard = createRoommateBoard();
        RoomType roomType = org.mockito.Mockito.mock(RoomType.class);
        Region region = org.mockito.Mockito.mock(Region.class);
        RoommateBoardFile firstImage = RoommateBoardFile.builder()
                .id(101L)
                .roommateBoard(roommateBoard)
                .file(createFile("first.jpg", "saved-first.jpg", "jpg"))
                .isThumbnail(true)
                .build();
        RoommateBoardFile secondImage = RoommateBoardFile.builder()
                .id(102L)
                .roommateBoard(roommateBoard)
                .file(createFile("second.jpg", "saved-second.jpg", "jpg"))
                .isThumbnail(false)
                .build();
        BoardModifyDto.Request request = createModifyRequest();
        request.setExistingImages(List.of(
                createExistingFileDto(101L, false),
                createExistingFileDto(102L, false)));

        when(roommateBoardRepository.findById(boardId)).thenReturn(Optional.of(roommateBoard));
        when(metaService.findByRoomTypeId(11L)).thenReturn(Optional.of(roomType));
        when(metaService.findByRegionId(22L)).thenReturn(Optional.of(region));
        when(roommateBoardFileRepository.findByRoommateBoard(roommateBoard))
                .thenReturn(List.of(firstImage, secondImage));

        // When & Then
        assertThatThrownBy(() -> roommateBoardService.modify(7L, boardId, request, null))
                .isInstanceOfSatisfying(BusinessException.class,
                        e -> assertThat(e.getErrorCode())
                                .isEqualTo(RoommateBoardErrorCode.ROOMMATE_BOARD_FILE_COUNT_THUMBNAIL_EXCEEDED));
    }

    @Test
    @DisplayName("다른 회원의 게시글을 수정하면 권한 없음 예외를 던지고 수정 데이터를 조회하지 않는다")
    void modifyThrowsWhenRequesterIsNotBoardOwner() {
        // Given
        Long boardId = 1L;
        RoommateBoard roommateBoard = createRoommateBoard(7L);
        BoardModifyDto.Request request = createModifyRequest();
        when(roommateBoardRepository.findById(boardId)).thenReturn(Optional.of(roommateBoard));

        // When & Then
        assertThatThrownBy(() -> roommateBoardService.modify(999L, boardId, request, null))
                .isInstanceOfSatisfying(BusinessException.class,
                        e -> assertThat(e.getErrorCode()).isEqualTo(RoommateBoardErrorCode.ROOMMATE_BOARD_FORBIDDEN));
        verifyNoInteractions(metaService, roommateBoardOptionRepository, roommateBoardFileRepository,
                fileService, fileRepository);
    }

    @Test
    @DisplayName("신규 추가 옵션 목록이 null이면 옵션 조회 없이 게시글 수정에 성공한다")
    void modifyAllowsNullNewExtraOptionIdsWithoutOptionLookup() {
        // Given
        Long boardId = 1L;
        RoommateBoard roommateBoard = createRoommateBoard();
        RoomType roomType = org.mockito.Mockito.mock(RoomType.class);
        Region region = org.mockito.Mockito.mock(Region.class);
        RoommateBoardFile existingThumbnail = RoommateBoardFile.builder()
                .id(101L)
                .roommateBoard(roommateBoard)
                .file(createFile("thumbnail.jpg", "saved-thumbnail.jpg", "jpg"))
                .isThumbnail(true)
                .build();
        BoardModifyDto.Request request = createModifyRequest();
        request.setNewExtraOptionIds(null);
        request.setExistingImages(List.of(createExistingFileDto(101L, true)));

        when(roommateBoardRepository.findById(boardId)).thenReturn(Optional.of(roommateBoard));
        when(metaService.findByRoomTypeId(11L)).thenReturn(Optional.of(roomType));
        when(metaService.findByRegionId(22L)).thenReturn(Optional.of(region));
        when(roommateBoardFileRepository.findByRoommateBoard(roommateBoard)).thenReturn(List.of(existingThumbnail));

        // When
        BoardModifyDto.Response response = roommateBoardService.modify(7L, boardId, request, null);

        // Then
        assertThat(response.getUpdatedAt()).isNotNull();
        verify(metaService, never()).findRoomExtraOptionsByIdIn(any());
        verify(roommateBoardOptionRepository, never()).saveAll(any());
    }

    @Test
    @DisplayName("존재하지 않는 신규 추가 옵션이 포함되면 추가 옵션 없음 예외를 던진다")
    void modifyThrowsWhenNewExtraOptionDoesNotExist() {
        // Given
        Long boardId = 1L;
        RoommateBoard roommateBoard = createRoommateBoard();
        RoomType roomType = org.mockito.Mockito.mock(RoomType.class);
        Region region = org.mockito.Mockito.mock(Region.class);
        BoardModifyDto.Request request = createModifyRequest();
        request.setNewExtraOptionIds(List.of(20L, 21L));

        when(roommateBoardRepository.findById(boardId)).thenReturn(Optional.of(roommateBoard));
        when(metaService.findByRoomTypeId(11L)).thenReturn(Optional.of(roomType));
        when(metaService.findByRegionId(22L)).thenReturn(Optional.of(region));
        when(metaService.findRoomExtraOptionsByIdIn(List.of(20L, 21L)))
                .thenReturn(List.of(org.mockito.Mockito.mock(RoomExtraOption.class)));

        // When & Then
        assertThatThrownBy(() -> roommateBoardService.modify(7L, boardId, request, null))
                .isInstanceOfSatisfying(BusinessException.class,
                        e -> assertThat(e.getErrorCode()).isEqualTo(MetaErrorCode.EXTRA_OPTION_NOT_FOUND));
        verifyNoInteractions(roommateBoardFileRepository, fileService, fileRepository);
    }

    @Test
    @DisplayName("게시글 수정 중 신규 이미지 업로드가 실패하면 업로드 실패 예외를 던지고 신규 파일 연결을 저장하지 않는다")
    void modifyThrowsWhenNewImageUploadFails() throws IOException {
        // Given
        Long boardId = 1L;
        RoommateBoard roommateBoard = createRoommateBoard();
        RoomType roomType = org.mockito.Mockito.mock(RoomType.class);
        Region region = org.mockito.Mockito.mock(Region.class);
        RoommateBoardFile existingThumbnail = RoommateBoardFile.builder()
                .id(101L)
                .roommateBoard(roommateBoard)
                .file(createFile("thumbnail.jpg", "saved-thumbnail.jpg", "jpg"))
                .isThumbnail(true)
                .build();
        MultipartFile failedFile = emptyMultipartFile();
        BoardModifyDto.Request request = createModifyRequest();
        request.setExistingImages(List.of(createExistingFileDto(101L, true)));
        request.setNewImages(List.of(createNewFileDto(0, false)));

        when(roommateBoardRepository.findById(boardId)).thenReturn(Optional.of(roommateBoard));
        when(metaService.findByRoomTypeId(11L)).thenReturn(Optional.of(roomType));
        when(metaService.findByRegionId(22L)).thenReturn(Optional.of(region));
        when(roommateBoardFileRepository.findByRoommateBoard(roommateBoard)).thenReturn(List.of(existingThumbnail));
        when(fileService.upload(failedFile, FileType.ROOMMATE_BOARD_IMAGE))
                .thenThrow(new IOException("upload failed"));

        // When & Then
        assertThatThrownBy(() -> roommateBoardService.modify(7L, boardId, request, List.of(failedFile)))
                .isInstanceOfSatisfying(BusinessException.class,
                        e -> assertThat(e.getErrorCode()).isEqualTo(FileErrorCode.FILE_UPLOAD_FAILED));
        verify(fileService).deleteAll(List.of());
        verify(fileRepository, never()).save(any(File.class));
        verify(roommateBoardFileRepository, never()).save(any(RoommateBoardFile.class));
    }

    @Test
    @DisplayName("존재하지 않는 게시글을 수정하면 게시글 없음 예외를 던지고 부가 정보를 조회하지 않는다")
    void modifyThrowsWhenBoardDoesNotExist() {
        // Given
        Long boardId = 999L;
        BoardModifyDto.Request request = createModifyRequest();
        when(roommateBoardRepository.findById(boardId)).thenReturn(Optional.empty());

        // When & Then
        assertThatThrownBy(() -> roommateBoardService.modify(7L, boardId, request, null))
                .isInstanceOfSatisfying(BusinessException.class,
                        e -> assertThat(e.getErrorCode()).isEqualTo(RoommateBoardErrorCode.ROOMMATE_BOARD_NOT_FOUND));
        verifyNoInteractions(metaService, fileService, fileRepository);
    }

    @Test
    @DisplayName("게시글 수정 중 신규 이미지 메타데이터가 파일 파트와 매칭되지 않으면 잘못된 요청 예외를 던진다")
    void modifyThrowsWhenNewImageFileIndexDoesNotMatchFilesPart() throws IOException {
        // Given
        Long boardId = 1L;
        RoommateBoard roommateBoard = createRoommateBoard();
        RoomType roomType = org.mockito.Mockito.mock(RoomType.class);
        Region region = org.mockito.Mockito.mock(Region.class);
        RoommateBoardFile existingThumbnail = RoommateBoardFile.builder()
                .id(101L)
                .roommateBoard(roommateBoard)
                .file(createFile("thumbnail.jpg", "saved-thumbnail.jpg", "jpg"))
                .isThumbnail(true)
                .build();
        BoardModifyDto.Request request = createModifyRequest();
        request.setExistingImages(List.of(createExistingFileDto(101L, true)));
        request.setNewImages(List.of(createNewFileDto(1, false)));

        when(roommateBoardRepository.findById(boardId)).thenReturn(Optional.of(roommateBoard));
        when(metaService.findByRoomTypeId(11L)).thenReturn(Optional.of(roomType));
        when(metaService.findByRegionId(22L)).thenReturn(Optional.of(region));
        when(roommateBoardFileRepository.findByRoommateBoard(roommateBoard)).thenReturn(List.of(existingThumbnail));

        // When & Then
        assertThatThrownBy(() -> roommateBoardService.modify(7L, boardId, request, List.of(emptyMultipartFile())))
                .isInstanceOfSatisfying(BusinessException.class,
                        e -> assertThat(e.getErrorCode()).isEqualTo(CommonErrorCode.BAD_REQUEST));
        verify(fileService, never()).upload(any(MultipartFile.class), any(FileType.class));
        verify(fileService).deleteAll(List.of());
        verifyNoInteractions(fileRepository);
    }


    private BoardDto.Request createRequest(FileDto... images) {
        BoardDto.Request request = new BoardDto.Request();
        request.setTitle("Looking for a roommate");
        request.setContents("Quiet home near the station");
        request.setDeposit(10_000);
        request.setMountlyRent(500);
        request.setManagementCost(50);
        request.setRoomTypeId(1L);
        request.setRegionId(2L);
        request.setComeableAt(LocalDateTime.of(2026, 7, 1, 9, 0));
        request.setImages(List.of(images));
        return request;
    }

    private BoardModifyDto.Request createModifyRequest() {
        BoardModifyDto.Request request = new BoardModifyDto.Request();
        request.setTitle("수정 제목");
        request.setContents("수정 내용");
        request.setDeposit(2_000);
        request.setMonthlyRent(70);
        request.setManagementCost(15);
        request.setRoomTypeId(11L);
        request.setRegionId(22L);
        request.setComeableAt(LocalDateTime.of(2026, 8, 1, 10, 0));
        request.setDeleteExtraOptionIds(List.of());
        request.setNewExtraOptionIds(List.of());
        request.setExistingImages(List.of());
        request.setNewImages(List.of());
        return request;
    }

    private FileDto createFileDto(Integer fileIndex, boolean thumbnail) {
        FileDto fileDto = new FileDto();
        fileDto.setFileIndex(fileIndex);
        fileDto.setThumbnail(thumbnail);
        return fileDto;
    }

    private ExistingFileDto createExistingFileDto(Long boardFileId, boolean thumbnail) {
        ExistingFileDto existingFileDto = new ExistingFileDto();
        existingFileDto.setBoardFileId(boardFileId);
        existingFileDto.setThumbnail(thumbnail);
        return existingFileDto;
    }

    private NewFileDto createNewFileDto(Integer fileIndex, boolean thumbnail) {
        NewFileDto newFileDto = new NewFileDto();
        newFileDto.setFileIndex(fileIndex);
        newFileDto.setThumbnail(thumbnail);
        return newFileDto;
    }

    private ReportDto.Request createReportRequest(String contents) {
        ReportDto.Request request = new ReportDto.Request();
        request.setContents(contents);
        return request;
    }

    private RoommateBoard createRoommateBoard() {
        return createRoommateBoard(7L);
    }

    private RoommateBoard createRoommateBoard(Long memberId) {
        return RoommateBoard.builder()
                .member(Member.builder().id(memberId).build())
                .title("기존 제목")
                .contents("기존 내용")
                .deposit(1_000)
                .monthlyRent(50)
                .managementCost(10)
                .roomType(org.mockito.Mockito.mock(RoomType.class))
                .region(org.mockito.Mockito.mock(Region.class))
                .comeableDate(LocalDateTime.of(2026, 7, 1, 9, 0))
                .build();
    }

    private RoomExtraOption mockExtraOption(Long id) {
        RoomExtraOption extraOption = org.mockito.Mockito.mock(RoomExtraOption.class);
        when(extraOption.getId()).thenReturn(id);
        return extraOption;
    }

    private File createFile(String originalFileName, String savedFileName, String extension) {
        return File.builder()
                .type(FileType.ROOMMATE_BOARD_IMAGE)
                .originalFileName(originalFileName)
                .savedFileName(savedFileName)
                .fileExt(extension)
                .build();
    }

    private BasicInfoRow createBasicInfoRow(Long boardId, Long memberId, LocalDate birth) {
        return new BasicInfoRow(
                boardId,
                "상세 게시글",
                1_000,
                10,
                50,
                "원룸",
                "역삼동",
                "강남구",
                "서울",
                LocalDateTime.of(2026, 6, 1, 12, 0),
                3L,
                "상세 내용",
                memberId,
                "상세작성자",
                "profile.jpg",
                birth,
                Gender.FEMALE
        );
    }

    private EditFormRow createEditFormRow() {
        return new EditFormRow(
                "수정 게시글",
                1_000,
                50,
                10,
                11L,
                "원룸",
                33L,
                "역삼동",
                "강남구",
                "서울",
                LocalDateTime.of(2026, 7, 1, 9, 0),
                "수정 내용"
        );
    }

    private MultipartFile emptyMultipartFile() {
        return org.mockito.Mockito.mock(MultipartFile.class);
    }

    private <T> List<T> toList(Iterable<T> iterable) {
        return StreamSupport.stream(iterable.spliterator(), false).toList();
    }
}
