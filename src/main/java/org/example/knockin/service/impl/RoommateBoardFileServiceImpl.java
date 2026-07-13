package org.example.knockin.service.impl;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.example.knockin.dto.BoardDetailDto.Response.FileDetailDto;
import org.example.knockin.entity.board.RoommateBoard;
import org.example.knockin.entity.board.RoommateBoardFile;
import org.example.knockin.entity.file.File;
import org.example.knockin.entity.file.FileType;
import org.example.knockin.repository.board.RoommateBoardFileRepository;
import org.example.knockin.repository.board.row.BoardThumbnailRow;
import org.example.knockin.service.FileService;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

@Service
@RequiredArgsConstructor
public class RoommateBoardFileServiceImpl {

    private final RoommateBoardFileRepository roommateBoardFileRepository;
    private final FileService fileService;

    public List<RoommateBoardFile> saveAll(RoommateBoard roommateBoard, List<MultipartFile> multipartFiles, List<Boolean> thumbnails) throws IOException {
        List<RoommateBoardFile> roommateBoardFiles = new ArrayList<>();
        for (int i = 0; i < multipartFiles.size(); i++) {
            File file = saveFile(multipartFiles.get(i));
            roommateBoardFiles.add(saveRoommateBoardFile(roommateBoard, file, thumbnails.get(i)));
        }
        return roommateBoardFiles;
    }

    public RoommateBoardFile save(RoommateBoard roommateBoard, MultipartFile multipartFile, Boolean thumbnail) throws IOException {
        File file = saveFile(multipartFile);
        return saveRoommateBoardFile(roommateBoard, file, thumbnail);
    }

    private File saveFile(MultipartFile multipartFile) throws IOException {
        return fileService.save(multipartFile, FileType.ROOMMATE_BOARD_IMAGE);
    }

    private RoommateBoardFile saveRoommateBoardFile(RoommateBoard roommateBoard, File file, Boolean thumbnail) {
        RoommateBoardFile roommateBoardFile = RoommateBoardFile.builder()
                .roommateBoard(roommateBoard)
                .file(file)
                .isThumbnail(thumbnail)
                .build();

        return roommateBoardFileRepository.save(roommateBoardFile);
    }

    public List<FileDetailDto> findFileDetailDtoByBoardId(Long boardId) {
        return roommateBoardFileRepository.getFileDetailDtoByBoardId(boardId);
    }

    public List<BoardThumbnailRow> findThumbnailsByBoardIds(List<Long> boardIds) {
        return roommateBoardFileRepository.findThumbnailsByBoardIds(boardIds);
    }

    public List<RoommateBoardFile> findAllByRoommateBoard(RoommateBoard roommateBoard) {
        return roommateBoardFileRepository.findByRoommateBoard(roommateBoard);
    }

    public void softDelete(RoommateBoardFile roommateBoardFile) {
        File file = roommateBoardFile.getFile();
        file.softDelete();
        roommateBoardFileRepository.delete(roommateBoardFile);
    }

}
