package org.example.knockin.service.impl;

import java.time.LocalDateTime;
import java.util.List;
import lombok.RequiredArgsConstructor;
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
import org.example.knockin.service.RoommateBoardService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class RoommateBoardServiceImpl implements RoommateBoardService {

    private final RoommateBoardRepository roommateBoardRepository;
    private final RoomTypeRepository roomTypeRepository;
    private final RegionRepository regionRepository;
    private final FileRepository fileRepository;
    private final RoommateBoardFileRepository roommateBoardFileRepository;
    private final MemberRepository memberRepository;

    // TODO: 예외처리 custom ErrorCode로 처리 필요
    @Override
    @Transactional
    public BoardDto.Response save(BoardDto.Request request, Long memberId) {
        Member memberRef = memberRepository.getReferenceById(memberId);
        Long roomTypeId = request.getRoomType();
        Long regionId = request.getRegion();

        RoomType roomType = roomTypeRepository.findById(roomTypeId)
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 방 타입 입니다. roomTypeId: " + roomTypeId));

        Region region = regionRepository.findById(regionId)
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 지역명입니다. regionId: " + regionId));

        RoommateBoard roommateBoard = RoommateBoard.builder()
                .member(memberRef)
                .title(request.getTitle())
                .contents(request.getContents())
                .deposit(request.getDeposit())
                .monthlyRent(request.getMountlyRent())
                .managementCost(request.getManagementCost())
                .roomType(roomType)
                .region(region)
                .comeableDate(request.getComeableAt())
                .build();

        RoommateBoard savedRoommateBoard = roommateBoardRepository.save(roommateBoard);
        List<ImageDto> images = request.getImages();

        List<RoommateBoardFile> boardFiles = createRoommateBoardFilesWithThumbnail(savedRoommateBoard, images);
        roommateBoardFileRepository.saveAll(boardFiles);

        LocalDateTime updatedAt = savedRoommateBoard.getUpdatedAt();
        return new BoardDto.Response(updatedAt);
    }

    private List<RoommateBoardFile> createRoommateBoardFilesWithThumbnail(RoommateBoard roommateBoard, List<ImageDto> images) {
        ImageDto thumbnailImageDto = images.stream()
                .filter(imageDto -> Boolean.TRUE.equals(imageDto.getThumbnail()))
                .findFirst()
                .orElseThrow(IllegalArgumentException::new);
        String thumbnailSavedFileName = thumbnailImageDto.getImage();

        List<String> savedFileNames = images.stream()
                .map(ImageDto::getImage)
                .toList();

        List<File> files = fileRepository.findBySavedFileNameIn(savedFileNames);

        List<String> foundSavedFileNames = files.stream()
                .map(File::getSavedFileName)
                .toList();

        List<String> missingFileNames = savedFileNames.stream()
                .filter(savedFileName -> !foundSavedFileNames.contains(savedFileName))
                .toList();

        if (!missingFileNames.isEmpty()) {
            throw new IllegalArgumentException("존재하지 않는 파일이 포함되어 있습니다: " + missingFileNames);
        }

        return files.stream()
                .map(file -> toBoardFile(roommateBoard, file, file.getSavedFileName().equals(thumbnailSavedFileName)))
                .toList();
    }

    private RoommateBoardFile toBoardFile(RoommateBoard roommateBoard, File file, boolean isThumbnail) {
        return RoommateBoardFile.builder()
                .roommateBoard(roommateBoard)
                .file(file)
                .isThumbnail(isThumbnail)
                .build();
    }
}
