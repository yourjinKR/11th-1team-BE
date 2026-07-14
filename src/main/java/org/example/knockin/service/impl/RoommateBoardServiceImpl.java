package org.example.knockin.service.impl;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.example.knockin.dto.*;
import org.example.knockin.dto.BoardDetailDto.Response.Condition;
import org.example.knockin.dto.BoardDetailDto.Response.ConditionWeight;
import org.example.knockin.dto.BoardDetailDto.Response.FileDetailDto;
import org.example.knockin.dto.BoardDetailDto.Response.Lifestyle;
import org.example.knockin.dto.BoardDto.Request.FileDto;
import org.example.knockin.dto.BoardDto.Response;
import org.example.knockin.dto.BoardEditDto.Response.BoardOptionInfo;
import org.example.knockin.dto.BoardEditDto.Response.RegionInfo;
import org.example.knockin.dto.BoardEditDto.Response.RoomTypeInfo;
import org.example.knockin.dto.BoardModifyDto.Request.ExistingFileDto;
import org.example.knockin.dto.BoardModifyDto.Request.NewFileDto;
import org.example.knockin.dto.Compatibility;
import org.example.knockin.dto.MyBoardListDto;
import org.example.knockin.dto.ReportDto;
import org.example.knockin.entity.auth.AuthenticationType;
import org.example.knockin.entity.board.RoommateBoard;
import org.example.knockin.entity.board.RoommateBoardFile;
import org.example.knockin.entity.board.RoommateBoardOption;
import org.example.knockin.entity.member.Member;
import org.example.knockin.entity.room.Region;
import org.example.knockin.entity.room.RoomType;
import org.example.knockin.exception.BusinessException;
import org.example.knockin.exception.CommonErrorCode;
import org.example.knockin.exception.FileErrorCode;
import org.example.knockin.exception.MemberErrorCode;
import org.example.knockin.exception.MetaErrorCode;
import org.example.knockin.exception.RoommateBoardErrorCode;
import org.example.knockin.global.util.DateUtils;
import org.example.knockin.global.util.StringUtils;
import org.example.knockin.repository.board.RoommateBoardRepository;
import org.example.knockin.repository.auth.row.MemberAuthenticationRow;
import org.example.knockin.repository.board.row.BasicInfoRow;
import org.example.knockin.repository.board.row.BoardBaseRow;
import org.example.knockin.repository.board.row.BoardThumbnailRow;
import org.example.knockin.repository.board.row.EditFormRow;
import org.example.knockin.repository.board.row.MyRoommateBoardRow;
import org.example.knockin.repository.life.row.MatchingLifestyleRow;
import org.example.knockin.repository.life.row.MatchingPreferenceConditionRow;
import org.example.knockin.repository.life.row.MatchingPreferenceConditionWeightRow;
import org.example.knockin.service.RoommateBoardService;
import org.example.knockin.service.RoommateScoreService;
import org.jspecify.annotations.Nullable;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

@Service
@RequiredArgsConstructor
public class RoommateBoardServiceImpl implements RoommateBoardService {
    private final RoommateBoardRepository roommateBoardRepository;
    private final MemberServiceImpl memberService;
    private final MetaServiceImpl metaService;
    private final RoommateScoreService roommateScoreService;
    private final RoommateBoardFileServiceImpl roommateBoardFileService;
    private final PreferenceConditionServiceImpl preferenceConditionService;
    private final MemberLifePatternService memberLifePatternService;
    private final AuthenticationServiceImpl authenticationService;
    private final RoommateBoardOptionServiceImpl roommateBoardOptionService;
    private final RoommateBoardInterestServiceImpl roommateBoardInterestService;
    private final RoommateBoardDeclarationServiceImpl roommateBoardDeclarationService;

    public RoommateBoard findById(Long id) {
        return roommateBoardRepository.findById(id)
                .orElseThrow(() -> new BusinessException(RoommateBoardErrorCode.ROOMMATE_BOARD_NOT_FOUND));
    }

    @Override
    @Transactional
    public BoardDto.Response save(BoardDto.Request request, Long memberId, List<MultipartFile> files) {
        Member member = memberService.findById(memberId)
                .orElseThrow(() -> new BusinessException(MemberErrorCode.MEMBER_NOT_FOUND));

        RoomType roomType = metaService.findByRoomTypeId(request.getRoomTypeId());

        Region region = metaService.findByRegionId(request.getRegionId())
                .orElseThrow(() -> new BusinessException(MetaErrorCode.REGION_NOT_FOUND));

        List<FileDto> fileDtos = request.getImages();

        if (fileDtos != null && !fileDtos.isEmpty()) {
            validateImageMaxCount(fileDtos.size());
            validateThumbnailCount(fileDtos.stream().filter(FileDto::isThumbnail).count());
        }

        List<MultipartFile> imageFiles = toImageFilesFromSaveRequest(fileDtos, files);
        List<Boolean> thumbnails = toThumbnailsFromSaveRequest(fileDtos);
        RoommateBoard savedRoommateBoard = saveRoommateBoard(request, member, roomType, region);
        saveRoommateBoardFiles(savedRoommateBoard, imageFiles, thumbnails);

        roommateBoardOptionService.saveByExtraOptionsIds(savedRoommateBoard, request.getExtraOptionIds());

        return new BoardDto.Response(savedRoommateBoard.getUpdatedAt());
    }

    private void validateImageMaxCount(long imageCount) {
        if (imageCount > RoommateBoard.IMAGE_MAXIMUM) {
            throw new BusinessException(RoommateBoardErrorCode.ROOMMATE_BOARD_FILE_COUNT_EXCEEDED, RoommateBoard.IMAGE_MAXIMUM);
        }
    }

    private void validateThumbnailCount(long thumbnailCount) {
        if (thumbnailCount != RoommateBoard.THUMBNAIL_MAXIMUM) {
            throw new BusinessException(RoommateBoardErrorCode.ROOMMATE_BOARD_FILE_COUNT_THUMBNAIL_EXCEEDED, RoommateBoard.THUMBNAIL_MAXIMUM);
        }
    }

    private RoommateBoard saveRoommateBoard(BoardDto.Request request, Member member, RoomType roomType, Region region) {
        RoommateBoard roommateBoard = RoommateBoard.builder()
                .member(member)
                .title(request.getTitle())
                .contents(request.getContents())
                .deposit(request.getDeposit())
                .monthlyRent(request.getMountlyRent())
                .managementCost(request.getManagementCost())
                .roomType(roomType)
                .region(region)
                .comeableDateNegotiable(request.getComeableDateNegotiable())
                .comeableDate(request.getComeableDate())
                .build();

        return roommateBoardRepository.save(roommateBoard);
    }

    private void saveRoommateBoardFiles(RoommateBoard roommateBoard, List<MultipartFile> imageFiles, List<Boolean> thumbnails) {
        if (imageFiles.isEmpty()) return;

        try {
            roommateBoardFileService.saveAll(roommateBoard, imageFiles, thumbnails);
        } catch (IOException e) {
            throw new BusinessException(FileErrorCode.FILE_UPLOAD_FAILED);
        }
    }

    @Override
    @Transactional(readOnly = true)
    public Page<BoardListDto.Response> getBoardList(BoardListDto.Request request, Pageable pageable) {
        LocalDateTime endDate = LocalDateTime.now()
                .minusDays(RoommateBoard.COMEABLE_DATE_VISIBLE_GRACE_DAYS);
        Page<BoardBaseRow> baseRows = roommateBoardRepository.search(request, pageable, endDate);

        if (baseRows.isEmpty()) {
            return new PageImpl<>(List.of(), pageable, baseRows.getTotalElements());
        }

        List<Long> boardIds = baseRows.stream()
                .map(BoardBaseRow::boardId)
                .toList();
        List<Long> memberIds = baseRows.stream()
                .map(BoardBaseRow::memberId)
                .distinct()
                .toList();

        Map<Long, String> thumbnailByBoardId = roommateBoardFileService.findThumbnailsByBoardIds(boardIds).stream()
                .collect(Collectors.toMap(
                        BoardThumbnailRow::boardId,
                        BoardThumbnailRow::imageUrl,
                        (first, second) -> first
                ));
        Map<Long, List<AuthenticationType>> authenticationsByMemberId =
                authenticationService.findAcceptedByMemberIds(memberIds).stream()
                        .collect(Collectors.groupingBy(
                                MemberAuthenticationRow::memberId,
                                Collectors.mapping(MemberAuthenticationRow::type, Collectors.toList())
                        ));

        return baseRows.map(row -> toResponse(row, thumbnailByBoardId, authenticationsByMemberId));
    }

    private BoardListDto.Response toResponse(
            BoardBaseRow row,
            Map<Long, String> thumbnailByBoardId,
            Map<Long, List<AuthenticationType>> authenticationsByMemberId
    ) {
        return BoardListDto.Response.builder()
                .id(row.boardId())
                .imageUrl(thumbnailByBoardId.get(row.boardId()))
                .title(row.title())
                .deposit(row.deposit())
                .monthlyRent(row.monthlyRent())
                .managementCost(row.managementCost())
                .roomTypes(List.of(row.roomTypeName()))
                .comeableDate(row.comeableDate())
                .regionFullName(StringUtils.parseToRegionFullName(
                        row.grandParentRegionName(),
                        row.parentRegionName(),
                        row.regionName()
                ))
                .memberName(row.memberName())
                .authentications(authenticationsByMemberId.getOrDefault(row.memberId(), List.of()))
                .hits(row.hits())
                .badges(List.of())
                .build();
    }

    @Override
    @Transactional
    public BoardDetailDto.Response getBoardDetail(Long boardId, Long memberId) {
        increaseHits(boardId);

        BasicInfoRow basicInfoRow = roommateBoardRepository.getBasicInfo(boardId)
                .orElseThrow(() -> new BusinessException(RoommateBoardErrorCode.ROOMMATE_BOARD_NOT_FOUND));

        Long ownerId = basicInfoRow.memberId();

        List<BoardDetailDto.Response.FileDetailDto> images = roommateBoardFileService.findFileDetailDtoByBoardId(boardId);
        List<String> roomExtraOptionNames = roommateBoardOptionService.findExtraOptionNamesByBoardId(boardId);
        List<Long> scoreLookupMemberIds = List.of(ownerId);
        List<MatchingLifestyleRow> lifestyleRows = memberLifePatternService.findMatchingRowByMemberIdsIn(scoreLookupMemberIds);
        List<MatchingPreferenceConditionRow> conditionRows = preferenceConditionService.findRowByMemberIdsIn(scoreLookupMemberIds);
        List<MatchingPreferenceConditionWeightRow> conditionWeightRows =
                preferenceConditionService.findWeightRowByMemberIdsIn(scoreLookupMemberIds);

        List<Lifestyle> lifestyles = lifestyleRows.stream()
                .filter(row -> Objects.equals(row.memberId(), ownerId))
                .map(this::toLifestyle)
                .toList();
        List<Condition> conditions = conditionRows.stream()
                .filter(row -> Objects.equals(row.memberId(), ownerId))
                .map(this::toCondition)
                .toList();
        List<ConditionWeight> conditionWeights = conditionWeightRows.stream()
                .filter(row -> Objects.equals(row.memberId(), ownerId))
                .map(this::toConditionWeight)
                .toList();
        Compatibility compatibility = roommateScoreService.calculateScore(memberId, ownerId);
        List<AuthenticationType> authenticationTypes = authenticationService.findTypesByMemberId(ownerId);

        boolean interested = roommateBoardInterestService.existsActiveByBoardIdAndMemberId(boardId, memberId);

        return toResponse(
                basicInfoRow,
                images,
                roomExtraOptionNames,
                lifestyles,
                conditions,
                conditionWeights,
                authenticationTypes,
                compatibility,
                interested
        );
    }

    private Lifestyle toLifestyle(MatchingLifestyleRow row) {
        return new Lifestyle(row.lifestyleId(), row.name(), row.value(), row.description(), row.type());
    }

    private Condition toCondition(MatchingPreferenceConditionRow row) {
        return new Condition(row.conditionId(), row.name(), row.value(), row.description(), row.type());
    }

    private ConditionWeight toConditionWeight(MatchingPreferenceConditionWeightRow row) {
        return new ConditionWeight(row.conditionWeightId(), row.name());
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
                .memberId(basicInfoRow.memberId())
                .memberName(basicInfoRow.memberName())
                .memberProfileImageUrl(basicInfoRow.memberProfileImageUrl())
                .memberAge(memberAge)
                .gender(basicInfoRow.gender())
                .authentications(authentications)
                .compatibility(compatibility)
                .interested(interested)
                .comeableDateNegotiable(basicInfoRow.comeableDateNegotiable())
                .comeableDate(basicInfoRow.comeableDate())
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

        List<FileDetailDto> images = roommateBoardFileService.findFileDetailDtoByBoardId(boardId);
        List<BoardOptionInfo> roomExtraOptions = roommateBoardOptionService.findExtraOptionsByBoardId(boardId);

        List<Lifestyle> lifestyles = memberLifePatternService.findLifeStyleDtoByMemberId(memberId);
        List<Condition> conditions = preferenceConditionService.findAllConditionByMemberId(memberId);
        List<ConditionWeight> conditionWeights = preferenceConditionService.findAllConditionWeightByMemberId(memberId);

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
                .comeableDateNegotiable(row.comeableDateNegotiable())
                .comeableDate(row.comeableDate())
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

        RoomType roomType = metaService.findByRoomTypeId(request.getRoomTypeId());
        Region region = metaService.findByRegionId(request.getRegionId())
                .orElseThrow(() -> new BusinessException(MetaErrorCode.REGION_NOT_FOUND));

        roommateBoard.modifyBasicInfo(request);
        roommateBoard.modifyRoomType(roomType);
        roommateBoard.modifyRegion(region);

        List<RoommateBoardOption> existingBoardOptions = roommateBoardOptionService.findWithRoomExtraOptionByBoardId(boardId);
        roommateBoardOptionService.deleteByExtraOptionIds(existingBoardOptions, request.getDeleteExtraOptionIds());
        roommateBoardOptionService.saveByExtraOptionsIds(roommateBoard, request.getNewExtraOptionIds());

        List<RoommateBoardFile> existingBoardFiles = roommateBoardFileService.findAllByRoommateBoard(roommateBoard);
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
        roommateBoardFileService.softDelete(boardFile);
    }

    private void saveNewBoardFiles(RoommateBoard roommateBoard, List<NewFileDto> fileDtos,
                                   @Nullable List<MultipartFile> files) {
        if (fileDtos == null || fileDtos.isEmpty()) {
            return;
        }

        try {
            roommateBoardFileService.saveAll(
                    roommateBoard,
                    toImageFilesFromModifyRequest(fileDtos, files),
                    toThumbnailsFromModifyRequest(fileDtos));
        } catch (IOException e) {
            throw new BusinessException(FileErrorCode.FILE_UPLOAD_FAILED);
        }
    }

    private List<MultipartFile> toImageFilesFromSaveRequest(
            @Nullable List<FileDto> fileDtos,
            @Nullable List<MultipartFile> files) {
        if (fileDtos == null || fileDtos.isEmpty()) {
            return List.of();
        }

        Set<Integer> usedFileIndexes = new HashSet<>();
        return fileDtos.stream()
                .map(fileDto -> getImageFile(fileDto.getFileIndex(), files, usedFileIndexes))
                .toList();
    }

    private List<Boolean> toThumbnailsFromSaveRequest(@Nullable List<FileDto> fileDtos) {
        if (fileDtos == null || fileDtos.isEmpty()) {
            return List.of();
        }

        return fileDtos.stream()
                .map(FileDto::isThumbnail)
                .toList();
    }

    private List<MultipartFile> toImageFilesFromModifyRequest(
            List<NewFileDto> fileDtos,
            @Nullable List<MultipartFile> files) {
        Set<Integer> usedFileIndexes = new HashSet<>();
        return fileDtos.stream()
                .map(fileDto -> getImageFile(fileDto.getFileIndex(), files, usedFileIndexes))
                .toList();
    }

    private List<Boolean> toThumbnailsFromModifyRequest(List<NewFileDto> fileDtos) {
        return fileDtos.stream()
                .map(NewFileDto::isThumbnail)
                .toList();
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
        List<RoommateBoardFile> roommateBoardFiles = roommateBoardFileService.findAllByRoommateBoard(roommateBoard);
        if (roommateBoardFiles.isEmpty()) return;
        validateImageMaxCount(roommateBoardFiles.size());
        validateThumbnailCount(roommateBoardFiles.stream().filter(RoommateBoardFile::getIsThumbnail).count());
    }

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

        roommateBoardInterestService.toggle(member, roommateBoard);

        return new BoardDto.Response(LocalDateTime.now());
    }

    @Override
    public BoardDto.Response deleteBoard(Long boardId, Long memberId) {

        RoommateBoard roommateBoard = roommateBoardRepository.findById(boardId)
                .orElseThrow(() -> new BusinessException(RoommateBoardErrorCode.ROOMMATE_BOARD_NOT_FOUND));

        Member owner = roommateBoard.getMember();

        if (!memberId.equals(owner.getId())) {
            throw new BusinessException(RoommateBoardErrorCode.ROOMMATE_BOARD_FORBIDDEN);
        }

        roommateBoard.softDelete();
        return new Response(LocalDateTime.now());
    }

    @Override
    public ReportDto.Response reportBoard(ReportDto.Request request, Long boardId, Long memberId) {

        RoommateBoard roommateBoard = roommateBoardRepository.findById(boardId)
                .orElseThrow(() -> new BusinessException(RoommateBoardErrorCode.ROOMMATE_BOARD_NOT_FOUND));

        Member member = memberService.findById(memberId)
                .orElseThrow(() -> new BusinessException(MemberErrorCode.MEMBER_NOT_FOUND));

        roommateBoardDeclarationService.report(roommateBoard, member, request.getContents());

        return new ReportDto.Response(LocalDateTime.now());
    }

    @Override
    public List<BoBoardListDto.Response.BoardInfo> findBackOfficeBoardList(Pageable pageable, BoBoardListDto.Request request) {
        return roommateBoardRepository.findBackOfficeBoardList(pageable, request);
    }

    @Override
    public BoBoardDetailDto.Response findBackOffcieBoard(Long id) {
        return roommateBoardRepository.findBackOffcieBoard(id);
    }

    @Transactional
    @Override
    public RoommateBoard deleteBackOfficeBoard(Long id, String rejectReason) {
        RoommateBoard roommateBoard = roommateBoardRepository.findById(id).orElseThrow(() -> new BusinessException(RoommateBoardErrorCode.ROOMMATE_BOARD_NOT_FOUND));
        roommateBoard.softDelete(rejectReason);
        return roommateBoard;
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
