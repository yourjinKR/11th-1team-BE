package org.example.knockin.service.impl;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.example.knockin.dto.BoardDetailDto;
import org.example.knockin.dto.BoardDetailDto.Response.Compatibility;
import org.example.knockin.dto.BoardDetailDto.Response.Condition;
import org.example.knockin.dto.BoardDetailDto.Response.ConditionWeight;
import org.example.knockin.dto.BoardDetailDto.Response.FileDetailDto;
import org.example.knockin.dto.BoardDetailDto.Response.Lifestyle;
import org.example.knockin.dto.BoardDto;
import org.example.knockin.dto.BoardDto.Request.FileDto;
import org.example.knockin.dto.BoardEditDto;
import org.example.knockin.dto.BoardEditDto.Response.BoardOptionInfo;
import org.example.knockin.dto.BoardEditDto.Response.RegionInfo;
import org.example.knockin.dto.BoardEditDto.Response.RoomTypeInfo;
import org.example.knockin.dto.BoardListDto;
import org.example.knockin.dto.BoardModifyDto;
import org.example.knockin.dto.BoardModifyDto.Request.ExistingFileDto;
import org.example.knockin.dto.BoardModifyDto.Request.NewFileDto;
import org.example.knockin.dto.MyBoardListDto;
import org.example.knockin.entity.auth.AuthenticationType;
import org.example.knockin.entity.board.RoommateBoard;
import org.example.knockin.entity.board.RoommateBoardFile;
import org.example.knockin.entity.board.RoommateBoardInterest;
import org.example.knockin.entity.board.RoommateBoardOption;
import org.example.knockin.entity.file.File;
import org.example.knockin.entity.file.FileType;
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
import org.example.knockin.global.util.DateUtils;
import org.example.knockin.global.util.StringUtils;
import org.example.knockin.repository.auth.AuthenticationRepository;
import org.example.knockin.repository.board.RoommateBoardFileRepository;
import org.example.knockin.repository.board.RoommateBoardInterestRepository;
import org.example.knockin.repository.board.RoommateBoardListRow;
import org.example.knockin.repository.board.RoommateBoardOptionRepository;
import org.example.knockin.repository.board.RoommateBoardRepository;
import org.example.knockin.repository.board.RoommateBoardSearchCondition;
import org.example.knockin.repository.board.row.BasicInfoRow;
import org.example.knockin.repository.board.row.EditFormRow;
import org.example.knockin.repository.board.row.MyRoommateBoardRow;
import org.example.knockin.repository.file.FileRepository;
import org.example.knockin.repository.life.MemberLifePatternRepository;
import org.example.knockin.repository.life.PreferenceConditionRepository;
import org.example.knockin.repository.life.PreferenceConditionWeightRepository;
import org.example.knockin.service.FileService;
import org.example.knockin.service.RoommateBoardService;
import org.jspecify.annotations.NullMarked;
import org.jspecify.annotations.Nullable;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
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
    private final PreferenceConditionRepository preferenceConditionRepository;
    private final MemberLifePatternRepository memberLifePatternRepository;
    private final AuthenticationRepository authenticationRepository;
    private final RoommateBoardOptionRepository roommateBoardOptionRepository;
    private final PreferenceConditionWeightRepository preferenceConditionWeightRepository;
    private final FileRepository fileRepository;
    private final RoommateBoardInterestRepository roommateBoardInterestRepository;

    @Override
    public BoardDto.Response save(BoardDto.Request request, Long memberId, @Nullable List<MultipartFile> files) {
        Member member = memberService.findById(memberId)
                .orElseThrow(() -> new BusinessException(MemberErrorCode.MEMBER_NOT_FOUND));

        RoomType roomType = metaService.findByRoomTypeId(request.getRoomTypeId())
                .orElseThrow(() -> new BusinessException(MetaErrorCode.ROOM_TYPE_NOT_FOUND));

        Region region = metaService.findByRegionId(request.getRegionId())
                .orElseThrow(() -> new BusinessException(MetaErrorCode.REGION_NOT_FOUND));

        List<FileDto> fileDtos = request.getImages();

        List<FileWithThumbnail> fileWithThumbnails = new ArrayList<>();
        List<File> uploadedFiles = new ArrayList<>();
        Set<Integer> usedFileIndexes = new HashSet<>();

        try {
            if (fileDtos != null) {
                for (FileDto fileDto : fileDtos) {
                    MultipartFile rawFile = getImageFile(fileDto.getFileIndex(), files, usedFileIndexes);

                    File file = fileService.upload(rawFile, FileType.ROOMMATE_BOARD_IMAGE);
                    uploadedFiles.add(file);
                    fileWithThumbnails.add(new FileWithThumbnail(file, fileDto.isThumbnail()));
                }
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

        fileRepository.saveAll(
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
                LocalDateTime.now().minusDays(RoommateBoard.COMEABLE_DATE_VISIBLE_GRACE_DAYS),
                pageable
        );

        return roommateBoardRepository.search(condition)
                .map(this::toResponse);
    }

    private BoardListDto.Response toResponse(RoommateBoardListRow row) {
        return BoardListDto.Response.builder()
                .id(row.id())
                .imageUrl(row.imageUrl())
                .title(row.title())
                .deposit(row.deposit())
                .monthlyRent(row.monthlyRent())
                .managementCost(row.managementCost())
                .roomTypes(row.roomTypes())
                .comeableDate(row.comeableDate())
                .regionFullName(row.regionFullName())
                .memberName(row.memberName())
                .authentications(row.authentications())
                .hits(row.hits())
                .badges(row.badges())
                .build();
    }

    @Override
    @Transactional
    public BoardDetailDto.Response getBoardDetail(Long boardId, Long memberId) {
        increaseHits(boardId);

        BasicInfoRow basicInfoRow = roommateBoardRepository.getBasicInfo(boardId)
                .orElseThrow(() -> new BusinessException(RoommateBoardErrorCode.ROOMMATE_BOARD_NOT_FOUND));

        Long ownerId = basicInfoRow.memberId();

        List<BoardDetailDto.Response.FileDetailDto> images = roommateBoardFileRepository.getFileDetailDtoByBoardId(boardId);
        List<String> roomExtraOptionNames = roommateBoardOptionRepository.getExtraOptionsNameByBoardId(boardId);
        List<Lifestyle> lifestyles = memberLifePatternRepository.getLifeStyleDto(ownerId);
        List<Condition> conditions = preferenceConditionRepository.getConditionDtoByMemberId(ownerId);
        List<ConditionWeight> conditionWeights = preferenceConditionWeightRepository.getConditionWeightDtoByMemberId(
                ownerId);
        List<AuthenticationType> authenticationTypes = authenticationRepository.getAcceptedAuthenticationTypeByMemberId(
                ownerId);
        boolean interested = roommateBoardInterestRepository.existsByRoommateBoardIdAndMemberIdAndIsDeletedIsFalse(
                boardId, memberId);

        return toResponse(basicInfoRow, images, roomExtraOptionNames, lifestyles, conditions, conditionWeights, authenticationTypes, new Compatibility(), interested);
    }

    private BoardDetailDto.Response toResponse(
            BasicInfoRow basicInfoRow,
            List<BoardDetailDto.Response.FileDetailDto> images,
            List<String> roomExtraOptionNames,
            List<Lifestyle> lifestyles,
            List<Condition> conditions,
            List<ConditionWeight> conditionWeights,
            List<AuthenticationType> authentications,
            Compatibility compatibility,
            boolean interested
    ) {

        String regionFullName = StringUtils.parseToRegionFullName(
                basicInfoRow.grandParentRegionName(),
                basicInfoRow.parentRegionName(),
                basicInfoRow.regionName());

        int memberAge = DateUtils.calculateAge(basicInfoRow.birth());

        return BoardDetailDto.Response.builder()
                .boardId(basicInfoRow.boardId())
                .images(images)
                .title(basicInfoRow.title())
                .deposit(basicInfoRow.deposit())
                .managementCost(basicInfoRow.managementCost())
                .monthlyRent(basicInfoRow.monthlyRent())
                .roomTypeName(basicInfoRow.roomTypeName())
                .regionFullName(regionFullName)
                .createdAt(basicInfoRow.createdAt())
                .hits(basicInfoRow.hits())
                .contents(basicInfoRow.contents())
                .roomExtraOptionNames(roomExtraOptionNames)
                .lifeStyles(lifestyles)
                .conditions(conditions)
                .conditionWeights(conditionWeights)
                .memberName(basicInfoRow.memberName())
                .memberProfileImageUrl(basicInfoRow.memberProfileImageUrl())
                .memberAge(memberAge)
                .gender(basicInfoRow.gender())
                .authentications(authentications)
                .compatibility(compatibility)
                .interested(interested)
                .build();
    }

    private void increaseHits(Long boardId) {
        int counts = roommateBoardRepository.increaseHitsById(boardId);
        if (counts == 0) throw new BusinessException(RoommateBoardErrorCode.ROOMMATE_BOARD_NOT_FOUND);
    }

    @Override
    public BoardEditDto.Response getEditForm(Long memberId, Long boardId) {
        EditFormRow row = roommateBoardRepository.getEditRow(boardId)
                .orElseThrow(() -> new BusinessException(RoommateBoardErrorCode.ROOMMATE_BOARD_NOT_FOUND));

        List<FileDetailDto> images = roommateBoardFileRepository.getFileDetailDtoByBoardId(boardId);
        List<BoardOptionInfo> roomExtraOptions = roommateBoardOptionRepository.getExtraOptionsByBoardId(boardId);

        List<Lifestyle> lifestyles = memberLifePatternRepository.getLifeStyleDto(memberId);
        List<Condition> conditions = preferenceConditionRepository.getConditionDtoByMemberId(memberId);
        List<ConditionWeight> conditionWeights = preferenceConditionWeightRepository.getConditionWeightDtoByMemberId(
                memberId);

        return BoardEditDto.Response.builder()
                .images(images)
                .title(row.title())
                .deposit(row.deposit())
                .monthlyRent(row.monthlyRent())
                .managementCost(row.managementCost())
                .roomType(new RoomTypeInfo(row.roomTypeId(), row.roomTypeName()))
                .region(new RegionInfo(row.regionId(), StringUtils.parseToRegionFullName(
                        row.grandParentRegionName(),
                        row.parentRegionName(),
                        row.regionName())))
                .comeableAt(row.comeableDate())
                .roomExtraOptions(roomExtraOptions)
                .contents(row.contents())
                .lifeStyles(lifestyles)
                .conditions(conditions)
                .conditionWeights(conditionWeights)
                .build();
    }

    @Override
    @Transactional(rollbackFor = IOException.class)
    public BoardModifyDto.Response modify(Long memberId, Long boardId, BoardModifyDto.Request request,
                                          @Nullable List<MultipartFile> files) {
        RoommateBoard roommateBoard = roommateBoardRepository.findById(boardId)
                .orElseThrow(() -> new BusinessException(RoommateBoardErrorCode.ROOMMATE_BOARD_NOT_FOUND));

        checkingIsOwner(roommateBoard, memberId);

        RoomType roomType = metaService.findByRoomTypeId(request.getRoomTypeId())
                .orElseThrow(() -> new BusinessException(MetaErrorCode.ROOM_TYPE_NOT_FOUND));
        Region region = metaService.findByRegionId(request.getRegionId())
                .orElseThrow(() -> new BusinessException(MetaErrorCode.REGION_NOT_FOUND));

        roommateBoard.modifyBasicInfo(request);
        roommateBoard.modifyRoomType(roomType);
        roommateBoard.modifyRegion(region);

        List<RoommateBoardOption> existingBoardOptions =
                roommateBoardOptionRepository.findWithRoomExtraOptionByBoardId(boardId);
        removeOptions(existingBoardOptions, request.getDeleteExtraOptionIds());

        List<RoomExtraOption> roomExtraOptions = getRoomExtraOptions(request.getNewExtraOptionIds());
        saveOptions(roommateBoard, roomExtraOptions);

        List<RoommateBoardFile> existingBoardFiles = roommateBoardFileRepository.findByRoommateBoard(roommateBoard);
        Map<Long, ExistingFileDto> existingFileDtoMap = toExistingImageMap(request.getExistingImages());
        syncExistingImages(existingBoardFiles, existingFileDtoMap);

        List<NewFileDto> newFileDtos = request.getNewImages();
        saveNewBoardFiles(roommateBoard, newFileDtos, files);

        validateBoardFileResult(roommateBoard);

        return new BoardModifyDto.Response(LocalDateTime.now());
    }

    private void checkingIsOwner(RoommateBoard roommateBoard, Long memberId) {
        Member member = roommateBoard.getMember();
        if (!Objects.equals(member.getId(), memberId)) {
            throw new BusinessException(RoommateBoardErrorCode.ROOMMATE_BOARD_FORBIDDEN);
        }
    }

    private void removeOptions(List<RoommateBoardOption> roommateBoardOptions, List<Long> extraOptionIds) {
        if (extraOptionIds == null || extraOptionIds.isEmpty()) {
            return;
        }

        Map<Long, RoommateBoardOption> extraOptionIdMap = roommateBoardOptions.stream()
                .collect(Collectors.toMap(
                        option -> option.getRoomExtraOption().getId(),
                        Function.identity(),
                        (first, second) -> first)
                );

        List<RoommateBoardOption> deleteTargets = extraOptionIds.stream()
                .map(extraOptionIdMap::get)
                .filter(Objects::nonNull)
                .toList();

        roommateBoardOptionRepository.deleteAll(deleteTargets);
    }

    private List<RoomExtraOption> getRoomExtraOptions(List<Long> newExtraOptionIds) {
        if (newExtraOptionIds == null || newExtraOptionIds.isEmpty()) {
            return List.of();
        }

        List<Long> uniqueIds = newExtraOptionIds.stream().distinct().toList();
        List<RoomExtraOption> roomExtraOptions = metaService.findRoomExtraOptionsByIdIn(uniqueIds);

        if (uniqueIds.size() != roomExtraOptions.size()) {
            throw new BusinessException(MetaErrorCode.EXTRA_OPTION_NOT_FOUND);
        }

        return roomExtraOptions;
    }

    private void saveOptions(RoommateBoard roommateBoard, List<RoomExtraOption> extraOptions) {
        if (extraOptions.isEmpty()) {
            return;
        }

        List<RoommateBoardOption> newBoardOptions = extraOptions.stream()
                .map(extraOption ->
                        RoommateBoardOption.builder()
                                .roommateBoard(roommateBoard)
                                .roomExtraOption(extraOption)
                                .build())
                .toList();

        roommateBoardOptionRepository.saveAll(newBoardOptions);
    }

    private Map<Long, ExistingFileDto> toExistingImageMap(List<ExistingFileDto> existingImages) {
        if (existingImages == null || existingImages.isEmpty()) {
            return Map.of();
        }

        return existingImages.stream()
                .collect(Collectors.toMap(
                        ExistingFileDto::getBoardFileId,
                        Function.identity(),
                        (first, second) -> first)
                );
    }

    private void syncExistingImages(List<RoommateBoardFile> existingBoardFiles,
                                    Map<Long, ExistingFileDto> existingFileDtoMap) {
        for (RoommateBoardFile boardFile : existingBoardFiles) {
            ExistingFileDto dto = existingFileDtoMap.get(boardFile.getId());

            if (dto == null) {
                softDeleteBoardFile(boardFile);
            } else {
                boardFile.modifyIsThumbnail(dto.isThumbnail());
            }
        }
    }

    private void softDeleteBoardFile(RoommateBoardFile boardFile) {
        File file = boardFile.getFile();
        file.softDelete();
        roommateBoardFileRepository.delete(boardFile);
    }

    private void saveNewBoardFiles(RoommateBoard roommateBoard, List<NewFileDto> fileDtos,
                                   @Nullable List<MultipartFile> files) {
        if (fileDtos == null || fileDtos.isEmpty()) {
            return;
        }

        List<File> savedFiles = new ArrayList<>();
        Set<Integer> usedFileIndexes = new HashSet<>();
        try {
            for (NewFileDto fileDto : fileDtos) {
                MultipartFile multipartFile = getImageFile(fileDto.getFileIndex(), files, usedFileIndexes);
                File file = fileService.upload(multipartFile, FileType.ROOMMATE_BOARD_IMAGE);
                savedFiles.add(file);
                fileRepository.save(file);

                RoommateBoardFile roommateBoardFile = RoommateBoardFile.builder()
                        .roommateBoard(roommateBoard)
                        .file(file)
                        .isThumbnail(fileDto.isThumbnail())
                        .build();
                roommateBoardFileRepository.save(roommateBoardFile);
            }
        } catch (IOException e) {
            fileService.deleteAll(savedFiles);
            throw new BusinessException(FileErrorCode.FILE_UPLOAD_FAILED);
        } catch (RuntimeException e) {
            fileService.deleteAll(savedFiles);
            throw e;
        }
    }

    private MultipartFile getImageFile(Integer fileIndex, @Nullable List<MultipartFile> files,
                                       Set<Integer> usedFileIndexes) {
        if (fileIndex == null || fileIndex < 0 || files == null || fileIndex >= files.size()) {
            throw new BusinessException(CommonErrorCode.BAD_REQUEST);
        }

        if (!usedFileIndexes.add(fileIndex)) {
            throw new BusinessException(CommonErrorCode.BAD_REQUEST);
        }

        return files.get(fileIndex);
    }

    private void validateBoardFileResult(RoommateBoard roommateBoard) {
        List<RoommateBoardFile> roommateBoardFiles = roommateBoardFileRepository.findByRoommateBoard(roommateBoard);

        if (roommateBoardFiles.isEmpty()) {
            return;
        }

        if (roommateBoardFiles.size() > RoommateBoard.IMAGE_MAXIMUM) {
            throw new BusinessException(RoommateBoardErrorCode.ROOMMATE_BOARD_FILE_COUNT_EXCEEDED);
        }

        long thumbnailCount = roommateBoardFiles.stream().filter(RoommateBoardFile::getIsThumbnail).count();

        if (thumbnailCount != RoommateBoard.THUMBNAIL_MAXIMUM) {
            throw new BusinessException(RoommateBoardErrorCode.ROOMMATE_BOARD_FILE_COUNT_THUMBNAIL_EXCEEDED);
        }
    }

    private record FileWithThumbnail(File file, boolean thumbNail) { }

    @Override
    public Page<MyBoardListDto.Response.BoardItem> getMyBoardList(Pageable pageable, Member member) {
        Page<MyRoommateBoardRow> rawPage = roommateBoardRepository.findMyBoardList(pageable, member);

        List<MyBoardListDto.Response.BoardItem> boardItems = rawPage.getContent().stream().map(row -> {
            String fullRegionName = getFullRegionName(row.getRegionEntity());

            return MyBoardListDto.Response.BoardItem.builder()
                    .boardId(row.getBoardId())
                    .title(row.getTitle())
                    .deposit(row.getDeposit())
                    .monthlyRent(row.getMonthlyRent())
                    .createdAt(row.getCreatedAt())
                    .memberName(row.getMemberName())
                    .image(row.getImage())
                    .region(fullRegionName)
                    .roomTypes(row.getRoomTypeName())
                    .build();
        }).toList();

        return new PageImpl<>(boardItems, pageable, rawPage.getTotalElements());
    }

    @Override
    @Transactional
    public BoardDto.Response likeBoard(Long boardId, Long memberId) {
        RoommateBoard roommateBoard = roommateBoardRepository.findByIdForUpdate(boardId)
                .orElseThrow(() -> new BusinessException(RoommateBoardErrorCode.ROOMMATE_BOARD_NOT_FOUND));

        Member member = memberService.findById(memberId)
                .orElseThrow(() -> new BusinessException(MemberErrorCode.MEMBER_NOT_FOUND));

        Optional<RoommateBoardInterest> interestOptional = roommateBoardInterestRepository.findByRoommateBoardAndMember(
                roommateBoard, member);

        saveOrToggleLike(member, roommateBoard, interestOptional);
        return new BoardDto.Response(LocalDateTime.now());
    }

    private void saveOrToggleLike(Member member, RoommateBoard roommateBoard,
            Optional<RoommateBoardInterest> interestOptional) {
        if (interestOptional.isPresent()) {
            RoommateBoardInterest roommateBoardInterest = interestOptional.get();
            roommateBoardInterest.likeToggle();
            return;
        }

        RoommateBoardInterest roommateBoardInterest = RoommateBoardInterest.builder()
                .member(member)
                .roommateBoard(roommateBoard)
                .isDeleted(false)
                .build();
        roommateBoardInterestRepository.save(roommateBoardInterest);
    }

    private String getFullRegionName(Region regionEntity) {
        List<String> regionNames = new ArrayList<>();
        Region current = regionEntity;
        while (current != null) {
            regionNames.addFirst(current.getName());
            current = current.getParent();
        }
        return String.join(" ", regionNames);
    }
}
