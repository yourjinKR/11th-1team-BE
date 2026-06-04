package org.example.knockin.service.impl;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.stream.StreamSupport;
import org.example.knockin.dto.BoardDto;
import org.example.knockin.dto.BoardDto.Request.ImageDto;
import org.example.knockin.entity.board.RoommateBoard;
import org.example.knockin.entity.board.RoommateBoardFile;
import org.example.knockin.entity.file.File;
import org.example.knockin.entity.member.Member;
import org.example.knockin.entity.room.Region;
import org.example.knockin.entity.room.RoomType;
import org.example.knockin.repository.board.RoommateBoardFileRepository;
import org.example.knockin.repository.board.RoommateBoardRepository;
import org.example.knockin.repository.file.FileRepository;
import org.example.knockin.repository.member.MemberRepository;
import org.example.knockin.repository.room.RegionRepository;
import org.example.knockin.repository.room.RoomTypeRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
@DisplayName("룸메이트 게시글 저장 서비스")
class RoommateBoardServiceImplTest {

    @Mock
    private RoommateBoardRepository roommateBoardRepository;

    @Mock
    private RoomTypeRepository roomTypeRepository;

    @Mock
    private RegionRepository regionRepository;

    @Mock
    private FileRepository fileRepository;

    @Mock
    private RoommateBoardFileRepository roommateBoardFileRepository;

    @Mock
    private MemberRepository memberRepository;

    @InjectMocks
    private RoommateBoardServiceImpl roommateBoardService;

    @Captor
    private ArgumentCaptor<RoommateBoard> roommateBoardCaptor;

    @Captor
    private ArgumentCaptor<Iterable<RoommateBoardFile>> boardFilesCaptor;

    @Test
    @DisplayName("썸네일 이미지가 포함된 요청이면 게시글과 이미지 정보를 저장한다")
    void savePersistsBoardAndImageLinksWithExactlyOneThumbnail() {
        BoardDto.Request request = createRequest(
                createImage("thumbnail.jpg", true),
                createImage("room.jpg", false));
        Long memberId = 42L;
        Member memberRef = org.mockito.Mockito.mock(Member.class);
        RoomType roomType = org.mockito.Mockito.mock(RoomType.class);
        Region region = org.mockito.Mockito.mock(Region.class);
        RoommateBoard savedRoommateBoard = org.mockito.Mockito.mock(RoommateBoard.class);
        File thumbnailFile = mockFile("thumbnail.jpg");
        File roomFile = mockFile("room.jpg");
        LocalDateTime updatedAt = LocalDateTime.of(2026, 6, 4, 16, 30);

        when(memberRepository.getReferenceById(memberId)).thenReturn(memberRef);
        when(roomTypeRepository.findById(1L)).thenReturn(Optional.of(roomType));
        when(regionRepository.findById(2L)).thenReturn(Optional.of(region));
        when(roommateBoardRepository.save(any(RoommateBoard.class))).thenReturn(savedRoommateBoard);
        when(fileRepository.findBySavedFileNameIn(List.of("thumbnail.jpg", "room.jpg")))
                .thenReturn(List.of(thumbnailFile, roomFile));
        when(savedRoommateBoard.getUpdatedAt()).thenReturn(updatedAt);

        BoardDto.Response response = roommateBoardService.save(request, memberId);

        verify(memberRepository).getReferenceById(memberId);
        verify(roommateBoardRepository).save(roommateBoardCaptor.capture());
        RoommateBoard boardToSave = roommateBoardCaptor.getValue();
        assertThat(boardToSave.getMember()).isSameAs(memberRef);
        assertThat(boardToSave.getTitle()).isEqualTo("Looking for a roommate");
        assertThat(boardToSave.getContents()).isEqualTo("Quiet home near the station");
        assertThat(boardToSave.getDeposit()).isEqualTo(10_000);
        assertThat(boardToSave.getMonthlyRent()).isEqualTo(500);
        assertThat(boardToSave.getManagementCost()).isEqualTo(50);
        assertThat(boardToSave.getRoomType()).isSameAs(roomType);
        assertThat(boardToSave.getRegion()).isSameAs(region);
        assertThat(boardToSave.getComeableDate()).isEqualTo(request.getComeableAt());

        verify(roommateBoardFileRepository).saveAll(boardFilesCaptor.capture());
        List<RoommateBoardFile> boardFiles = StreamSupport.stream(
                boardFilesCaptor.getValue().spliterator(), false).toList();
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
    @DisplayName("존재하지 않는 방 유형으로 저장을 요청하면 예외가 발생한다")
    void saveThrowsWhenRoomTypeDoesNotExist() {
        BoardDto.Request request = createRequest(createImage("thumbnail.jpg", true));
        Long memberId = 42L;
        Member memberRef = org.mockito.Mockito.mock(Member.class);
        when(memberRepository.getReferenceById(memberId)).thenReturn(memberRef);
        when(roomTypeRepository.findById(1L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> roommateBoardService.save(request, memberId))
                .isInstanceOf(IllegalArgumentException.class);

        verifyNoInteractions(regionRepository, roommateBoardRepository, fileRepository,
                roommateBoardFileRepository);
    }

    @Test
    @DisplayName("존재하지 않는 지역으로 저장을 요청하면 예외가 발생한다")
    void saveThrowsWhenRegionDoesNotExist() {
        BoardDto.Request request = createRequest(createImage("thumbnail.jpg", true));
        Long memberId = 42L;
        Member memberRef = org.mockito.Mockito.mock(Member.class);
        RoomType roomType = org.mockito.Mockito.mock(RoomType.class);
        when(memberRepository.getReferenceById(memberId)).thenReturn(memberRef);
        when(roomTypeRepository.findById(1L)).thenReturn(Optional.of(roomType));
        when(regionRepository.findById(2L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> roommateBoardService.save(request, memberId))
                .isInstanceOf(IllegalArgumentException.class);

        verifyNoInteractions(roommateBoardRepository, fileRepository, roommateBoardFileRepository);
    }

    @Test
    @DisplayName("썸네일로 지정된 이미지가 없으면 예외가 발생한다")
    void saveThrowsWhenNoImageIsMarkedAsThumbnail() {
        BoardDto.Request request = createRequest(createImage("room.jpg", null));
        Long memberId = 42L;
        Member memberRef = org.mockito.Mockito.mock(Member.class);
        RoomType roomType = org.mockito.Mockito.mock(RoomType.class);
        Region region = org.mockito.Mockito.mock(Region.class);
        RoommateBoard savedRoommateBoard = org.mockito.Mockito.mock(RoommateBoard.class);
        when(memberRepository.getReferenceById(memberId)).thenReturn(memberRef);
        when(roomTypeRepository.findById(1L)).thenReturn(Optional.of(roomType));
        when(regionRepository.findById(2L)).thenReturn(Optional.of(region));
        when(roommateBoardRepository.save(any(RoommateBoard.class))).thenReturn(savedRoommateBoard);

        assertThatThrownBy(() -> roommateBoardService.save(request, memberId))
                .isInstanceOf(IllegalArgumentException.class);

        verifyNoInteractions(fileRepository, roommateBoardFileRepository);
    }

    private BoardDto.Request createRequest(ImageDto... images) {
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

    private ImageDto createImage(String savedFileName, Boolean thumbnail) {
        ImageDto image = new ImageDto();
        image.setImage(savedFileName);
        image.setThumbnail(thumbnail);
        return image;
    }

    private File mockFile(String savedFileName) {
        File file = org.mockito.Mockito.mock(File.class);
        when(file.getSavedFileName()).thenReturn(savedFileName);
        return file;
    }
}
