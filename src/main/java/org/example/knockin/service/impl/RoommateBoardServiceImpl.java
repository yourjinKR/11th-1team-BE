package org.example.knockin.service.impl;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import lombok.RequiredArgsConstructor;
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
import org.example.knockin.repository.board.RoommateBoardFileRepository;
import org.example.knockin.repository.board.RoommateBoardRepository;
import org.example.knockin.repository.board.RoommateBoardSearchCondition;
import org.example.knockin.service.FileService;
import org.example.knockin.service.RoommateBoardService;
import org.jspecify.annotations.NullMarked;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.support.TransactionTemplate;
import org.springframework.web.multipart.MultipartFile;

@NullMarked
@Service
@RequiredArgsConstructor
public class RoommateBoardServiceImpl implements RoommateBoardService {

    private final RoommateBoardRepository roommateBoardRepository;
    private final RoommateBoardFileRepository roommateBoardFileRepository;
    private final MemberServiceImpl memberService;
    private final FileService fileService;
    private final TransactionTemplate transactionTemplate;
    private final MetaServiceImpl metaService;

    @Override
    public BoardDto.Response save(BoardDto.Request request, Long memberId) {
        Member member = memberService.findById(memberId)
                .orElseThrow(() -> new BusinessException(MemberErrorCode.MEMBER_NOT_FOUND));

        RoomType roomType = metaService.findByRoomTypeId(request.getRoomType())
                .orElseThrow(() -> new BusinessException(MetaErrorCode.ROOM_TYPE_NOT_FOUND));

        Region region = metaService.findByRegionId(request.getRegion())
                .orElseThrow(() -> new BusinessException(MetaErrorCode.REGION_NOT_FOUND));

        List<FileDto> fileDtos = request.getImages();

        List<FileWithThumbnail> fileWithThumbnails = new ArrayList<>();
        List<File> uploadedFiles = new ArrayList<>();

        try {
            for (FileDto fileDto : fileDtos) {
                MultipartFile rawFile = fileDto.getFile();

                File file = fileService.upload(rawFile, FileType.ROOMMATE_BOARD_IMAGE);
                uploadedFiles.add(file);
                fileWithThumbnails.add(new FileWithThumbnail(file, fileDto.isThumbnail()));
            }
        } catch (IOException e) {
            fileService.deleteAll(uploadedFiles);
            throw new BusinessException(FileErrorCode.FILE_UPLOAD_FAILED);
        } catch (RuntimeException e) {
            fileService.deleteAll(uploadedFiles);
            throw e;
        }

        try {
            return transactionTemplate.execute(
                    status -> saveBoardWithFiles(request, member, roomType, region, fileWithThumbnails));
        } catch (RuntimeException e) {
            fileService.deleteAll(uploadedFiles);
            throw e;
        }
    }

    private BoardDto.Response saveBoardWithFiles(
            BoardDto.Request request,
            Member member,
            RoomType roomType,
            Region region,
            List<FileWithThumbnail> fileWithThumbnails) {

        RoommateBoard roommateBoard = RoommateBoard.builder()
                .member(member)
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

        fileService.saveAll(
                fileWithThumbnails.stream()
                        .map(FileWithThumbnail::file)
                        .toList()
        );

        List<RoommateBoardFile> roommateBoardFiles = fileWithThumbnails.stream()
                .map(fileWithThumbnail ->
                        RoommateBoardFile.builder()
                                .roommateBoard(savedRoommateBoard)
                                .file(fileWithThumbnail.file())
                                .isThumbnail(fileWithThumbnail.thumbNail())
                                .build())
                .toList();

        roommateBoardFileRepository.saveAll(roommateBoardFiles);

        LocalDateTime updatedAt = savedRoommateBoard.getUpdatedAt();
        return new BoardDto.Response(updatedAt);
    }

    @Override
    public Page<BoardListDto.Response> getBoardList(BoardListDto.Request request, Pageable pageable) {
        RoommateBoardSearchCondition condition = new RoommateBoardSearchCondition(
                request.getRegionIds(),
                request.getRoomTypeIds(),
                request.getGender(),
                request.getMinDeposit(),
                request.getMaxDeposit(),
                request.getMinMounthRent(),
                request.getMaxMounthRent(),
                LocalDateTime.now(),
                pageable
        );

        return roommateBoardRepository.search(condition);
    }

    private record FileWithThumbnail(File file, boolean thumbNail) { }
}
