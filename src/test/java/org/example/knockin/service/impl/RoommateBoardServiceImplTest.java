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
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.stream.StreamSupport;
import org.example.knockin.dto.BoardDetailDto;
import org.example.knockin.dto.BoardDto;
import org.example.knockin.dto.BoardDto.Request.FileDto;
import org.example.knockin.dto.BoardListDto;
import org.example.knockin.entity.board.RoommateBoard;
import org.example.knockin.entity.board.RoommateBoardFile;
import org.example.knockin.entity.file.File;
import org.example.knockin.entity.file.FileType;
import org.example.knockin.entity.member.Member;
import org.example.knockin.entity.room.Region;
import org.example.knockin.entity.room.RoomType;
import org.example.knockin.global.exception.BusinessException;
import org.example.knockin.global.exception.FileErrorCode;
import org.example.knockin.global.exception.MemberErrorCode;
import org.example.knockin.global.exception.MetaErrorCode;
import org.example.knockin.global.exception.RoommateBoardErrorCode;
import org.example.knockin.repository.board.RoommateBoardFileRepository;
import org.example.knockin.repository.board.RoommateBoardSearchCondition;
import org.example.knockin.repository.board.RoommateBoardRepository;
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
@DisplayName("룸메이트 게시글 저장 서비스")
class RoommateBoardServiceImplTest {

    @Mock
    private RoommateBoardRepository roommateBoardRepository;

    @Mock
    private RoommateBoardFileRepository roommateBoardFileRepository;

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
    private ArgumentCaptor<List<File>> filesCaptor;

    @Captor
    private ArgumentCaptor<Iterable<RoommateBoardFile>> boardFilesCaptor;

    @Captor
    private ArgumentCaptor<List<File>> uploadedFilesCaptor;

    @Captor
    private ArgumentCaptor<RoommateBoardSearchCondition> searchConditionCaptor;

    @BeforeEach
    void setUpTransactionTemplate() {
        lenient().when(transactionTemplate.execute(any())).thenAnswer(invocation -> {
            TransactionCallback<?> callback = invocation.getArgument(0);
            return callback.doInTransaction(null);
        });
    }

    @Test
    @DisplayName("이미지 업로드가 완료되면 트랜잭션 안에서 게시글과 파일 연결 정보를 저장한다")
    void savePersistsBoardAndUploadedImageLinksInTransaction() throws IOException {
        MultipartFile thumbnailImage = emptyMultipartFile();
        MultipartFile roomImage = emptyMultipartFile();
        BoardDto.Request request = createRequest(
                createFileDto(thumbnailImage, true),
                createFileDto(roomImage, false));
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

        BoardDto.Response response = roommateBoardService.save(request, memberId);

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

        verify(fileService).saveAll(filesCaptor.capture());
        List<File> files = filesCaptor.getValue();
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
        BoardDto.Request request = createRequest(createFileDto(emptyMultipartFile(), true));
        Long memberId = 42L;
        when(memberService.findById(memberId)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> roommateBoardService.save(request, memberId))
                .isInstanceOfSatisfying(BusinessException.class,
                        e -> assertThat(e.getErrorCode()).isEqualTo(MemberErrorCode.MEMBER_NOT_FOUND));

        verifyNoInteractions(metaService, fileService, roommateBoardRepository, roommateBoardFileRepository);
    }

    @Test
    @DisplayName("존재하지 않는 방 유형으로 저장을 요청하면 이미지 업로드와 트랜잭션 저장을 수행하지 않는다")
    void saveThrowsWhenRoomTypeDoesNotExist() {
        BoardDto.Request request = createRequest(createFileDto(emptyMultipartFile(), true));
        Long memberId = 42L;
        Member member = org.mockito.Mockito.mock(Member.class);
        when(memberService.findById(memberId)).thenReturn(Optional.of(member));
        when(metaService.findByRoomTypeId(1L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> roommateBoardService.save(request, memberId))
                .isInstanceOfSatisfying(BusinessException.class,
                        e -> assertThat(e.getErrorCode()).isEqualTo(MetaErrorCode.ROOM_TYPE_NOT_FOUND));

        verifyNoInteractions(fileService, roommateBoardRepository, roommateBoardFileRepository);
    }

    @Test
    @DisplayName("존재하지 않는 지역으로 저장을 요청하면 이미지 업로드와 트랜잭션 저장을 수행하지 않는다")
    void saveThrowsWhenRegionDoesNotExist() {
        BoardDto.Request request = createRequest(createFileDto(emptyMultipartFile(), true));
        Long memberId = 42L;
        Member member = org.mockito.Mockito.mock(Member.class);
        RoomType roomType = org.mockito.Mockito.mock(RoomType.class);
        when(memberService.findById(memberId)).thenReturn(Optional.of(member));
        when(metaService.findByRoomTypeId(1L)).thenReturn(Optional.of(roomType));
        when(metaService.findByRegionId(2L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> roommateBoardService.save(request, memberId))
                .isInstanceOfSatisfying(BusinessException.class,
                        e -> assertThat(e.getErrorCode()).isEqualTo(MetaErrorCode.REGION_NOT_FOUND));

        verifyNoInteractions(fileService, roommateBoardRepository, roommateBoardFileRepository);
    }

    @Test
    @DisplayName("이미지 업로드가 실패하면 트랜잭션 저장을 수행하지 않고 업로드 실패 예외를 던진다")
    void saveThrowsWhenFileUploadFails() throws IOException {
        MultipartFile thumbnailImage = emptyMultipartFile();
        BoardDto.Request request = createRequest(createFileDto(thumbnailImage, true));
        Long memberId = 42L;
        Member member = org.mockito.Mockito.mock(Member.class);
        RoomType roomType = org.mockito.Mockito.mock(RoomType.class);
        Region region = org.mockito.Mockito.mock(Region.class);
        when(memberService.findById(memberId)).thenReturn(Optional.of(member));
        when(metaService.findByRoomTypeId(1L)).thenReturn(Optional.of(roomType));
        when(metaService.findByRegionId(2L)).thenReturn(Optional.of(region));
        when(fileService.upload(thumbnailImage, FileType.ROOMMATE_BOARD_IMAGE))
                .thenThrow(new IOException("upload failed"));

        assertThatThrownBy(() -> roommateBoardService.save(request, memberId))
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
                createFileDto(thumbnailImage, true),
                createFileDto(roomImage, false));
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

        assertThatThrownBy(() -> roommateBoardService.save(request, memberId))
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
        BoardDto.Request request = createRequest(createFileDto(thumbnailImage, true));
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

        assertThatThrownBy(() -> roommateBoardService.save(request, memberId))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("db failed");

        verify(fileService).deleteAll(uploadedFilesCaptor.capture());
        assertThat(uploadedFilesCaptor.getValue()).containsExactly(thumbnailFile);
        verifyNoInteractions(roommateBoardFileRepository);
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
    @DisplayName("상세 조회는 조회수를 증가시킨 뒤 게시글 상세 정보를 반환한다")
    void getBoardDetailIncreasesHitsThenReturnsDetail() {
        // Given
        Long boardId = 1L;
        BoardDetailDto.Response expectedResponse = new BoardDetailDto.Response();
        when(roommateBoardRepository.increaseHitsById(boardId)).thenReturn(1);
        when(roommateBoardRepository.viewDetail(boardId)).thenReturn(expectedResponse);

        // When
        BoardDetailDto.Response response = roommateBoardService.getBoardDetail(boardId);

        // Then
        assertThat(response).isSameAs(expectedResponse);
        InOrder inOrder = inOrder(roommateBoardRepository);
        inOrder.verify(roommateBoardRepository).increaseHitsById(boardId);
        inOrder.verify(roommateBoardRepository).viewDetail(boardId);
    }

    @Test
    @DisplayName("상세 조회는 조회수 증가 대상이 없으면 게시글 없음 예외를 던진다")
    void getBoardDetailThrowsWhenHitUpdateDoesNotAffectBoard() {
        // Given
        Long boardId = 999L;
        when(roommateBoardRepository.increaseHitsById(boardId)).thenReturn(0);

        // When & Then
        assertThatThrownBy(() -> roommateBoardService.getBoardDetail(boardId))
                .isInstanceOfSatisfying(BusinessException.class,
                        e -> assertThat(e.getErrorCode()).isEqualTo(RoommateBoardErrorCode.ROOMMATE_BOARD_NOT_FOUND));
        verify(roommateBoardRepository).increaseHitsById(boardId);
        verify(roommateBoardRepository, never()).viewDetail(any());
    }

    private BoardDto.Request createRequest(FileDto... images) {
        BoardDto.Request request = new BoardDto.Request();
        request.setTitle("Looking for a roommate");
        request.setContents("Quiet home near the station");
        request.setDeposit(10_000);
        request.setMountlyRent(500);
        request.setManagementCost(50);
        request.setRoomType(1L);
        request.setRegion(2L);
        request.setComeableAt(LocalDateTime.of(2026, 7, 1, 9, 0));
        request.setImages(List.of(images));
        return request;
    }

    private FileDto createFileDto(MultipartFile file, boolean thumbnail) {
        FileDto fileDto = new FileDto();
        fileDto.setFile(file);
        fileDto.setThumbnail(thumbnail);
        return fileDto;
    }

    private File createFile(String originalFileName, String savedFileName, String extension) {
        return File.builder()
                .type(FileType.ROOMMATE_BOARD_IMAGE)
                .originalFileName(originalFileName)
                .savedFileName(savedFileName)
                .fileExt(extension)
                .build();
    }

    private MultipartFile emptyMultipartFile() {
        return org.mockito.Mockito.mock(MultipartFile.class);
    }

    private <T> List<T> toList(Iterable<T> iterable) {
        return StreamSupport.stream(iterable.spliterator(), false).toList();
    }
}
