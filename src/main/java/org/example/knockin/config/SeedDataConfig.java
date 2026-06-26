package org.example.knockin.config;

import lombok.RequiredArgsConstructor;
import org.example.knockin.entity.agreement.Agreement;
import org.example.knockin.entity.agreement.AgreementLog;
import org.example.knockin.entity.inquiry.InquiryCategory;
import org.example.knockin.entity.life.LifePattern;
import org.example.knockin.entity.life.LifePatternInformation;
import org.example.knockin.entity.life.LifePatternType;
import org.example.knockin.entity.room.Region;
import org.example.knockin.entity.room.RoomExtraOption;
import org.example.knockin.entity.room.RoomType;
import org.example.knockin.repository.agreement.AgreementLogRepository;
import org.example.knockin.repository.agreement.AgreementRepository;
import org.example.knockin.repository.board.FaqRepository;
import org.example.knockin.repository.inquiry.InquiryCategoryRepository;
import org.example.knockin.repository.life.LifePatternInformationRepository;
import org.example.knockin.repository.life.LifePatternRepository;
import org.example.knockin.repository.room.RegionRepository;
import org.example.knockin.repository.room.RoomExtraOptionRepository;
import org.example.knockin.repository.room.RoomTypeRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;

@Component
@RequiredArgsConstructor
@Profile("!prod")
public class SeedDataConfig implements CommandLineRunner {
    private final AgreementRepository agreementRepository;
    private final AgreementLogRepository agreementLogRepository;
    private final LifePatternRepository lifePatternRepository;
    private final LifePatternInformationRepository lifePatternInformationRepository;
    private final RegionRepository regionRepository;
    private final RoomTypeRepository roomTypeRepository;
    private final RoomExtraOptionRepository roomExtraOptionRepository;
    private final InquiryCategoryRepository inquiryCategoryRepository;

    @Override
    @Transactional
    public void run(String... args) throws Exception {
        if (agreementRepository.count() > 0) {
            return;
        }

        Agreement termsOfService1 = Agreement.builder().title("서비스 이용약관").contents("상세 내용...").isDeleted(false).isRequired(true).type(1L).build();
        Agreement termsOfService2 = Agreement.builder().title("서비스 이용약관").contents("상세 내용... 수정1").isDeleted(false).isRequired(true).type(1L).build();
        Agreement termsOfService3 = Agreement.builder().title("서비스 이용약관").contents("상세 내용... 수정2").isDeleted(false).isRequired(true).type(1L).build();
        Agreement privacyPolicy = Agreement.builder().title("개인정보 처리방침").contents("상세 내용...").isDeleted(false).isRequired(true).type(2L).build();
        agreementRepository.saveAll(List.of(termsOfService1, termsOfService2, termsOfService3, privacyPolicy));

        AgreementLog termsLog1 = AgreementLog.builder().agreement(termsOfService1).isCurrent(true).build();
        AgreementLog termsLog2 = AgreementLog.builder().agreement(termsOfService2).isCurrent(false).build();
        AgreementLog termsLog3 = AgreementLog.builder().agreement(termsOfService3).isCurrent(false).build();
        AgreementLog privacyLog = AgreementLog.builder().agreement(privacyPolicy).isCurrent(true).build();
        agreementLogRepository.saveAll(List.of(termsLog1, termsLog2, termsLog3, privacyLog));

        LifePattern cleanScale = LifePattern.builder().name("청소 깔끔도").dtype(LifePatternType.SCALE).isDeleted(false).sort(1).build();
        LifePattern smokeYn = LifePattern.builder().name("흡연 여부").dtype(LifePatternType.SINGLE_CHOICE).isDeleted(false).sort(2).build();
        LifePattern mbtiChoice = LifePattern.builder().name("MBTI 성향").dtype(LifePatternType.SINGLE_CHOICE).isDeleted(false).sort(3).build();
        lifePatternRepository.saveAll(List.of(cleanScale, smokeYn, mbtiChoice));

        LifePatternInformation scale1 = LifePatternInformation.builder().lifePattern(cleanScale).dvalue("1").description("자주 안함").build();
        LifePatternInformation scale2 = LifePatternInformation.builder().lifePattern(cleanScale).dvalue("2").description("종종 안함").build();
        LifePatternInformation scale3 = LifePatternInformation.builder().lifePattern(cleanScale).dvalue("3").description("보통").build();
        LifePatternInformation scale4 = LifePatternInformation.builder().lifePattern(cleanScale).dvalue("4").description("깔끔함").build();
        LifePatternInformation scale5 = LifePatternInformation.builder().lifePattern(cleanScale).dvalue("5").description("매우 깔끔함").build();
        LifePatternInformation smokeTrue = LifePatternInformation.builder().lifePattern(smokeYn).dvalue("1").description("흡연자").build();
        LifePatternInformation smokeFalse = LifePatternInformation.builder().lifePattern(smokeYn).dvalue("2").description("비흡연자").build();
        LifePatternInformation mbtiI = LifePatternInformation.builder().lifePattern(mbtiChoice).dvalue("1").description("내향형").build();
        LifePatternInformation mbtiE = LifePatternInformation.builder().lifePattern(mbtiChoice).dvalue("2").description("외향형").build();
        lifePatternInformationRepository.saveAll(List.of(scale1, scale2, scale3, scale4, scale5, smokeTrue, smokeFalse, mbtiI, mbtiE));

        Region seoul = Region.builder().name("서울특별시").scope(1).parent(null).build();
        Region gyeonggi = Region.builder().name("경기도").scope(1).parent(null).build();
        regionRepository.saveAll(List.of(seoul, gyeonggi));

        List<Region> level2Districts = new ArrayList<>();
        List<Region> level3Towns = new ArrayList<>();

        String[] seoulGuList = {"강남구", "마포구", "송파구", "서초구", "성동구", "종로구", "영등포구", "용산구"};
        String[][] seoulDongList = {
                {"역삼동", "삼성동", "청담동", "논현동"},
                {"서교동", "합정동", "망원동", "연남동"},
                {"잠실동", "문정동", "가락동", "방이동"},
                {"반포동", "방배동", "서초동", "양재동"},
                {"성수동", "옥수동", "왕십리동", "마장동"},
                {"혜화동", "명륜동", "삼청동", "평창동"},
                {"여의도동", "당산동", "문래동", "신길동"},
                {"이태원동", "한남동", "이촌동", "후암동"}
        };

        for (int i = 0; i < seoulGuList.length; i++) {
            Region gu = Region.builder().name(seoulGuList[i]).scope(2).parent(seoul).build();
            level2Districts.add(gu);
        }

        String[] gyeonggiSiList = {"수원시 영통구", "성남시 분당구", "고양시 일산동구", "용인시 수지구", "안양시 동안구", "부천시", "남양주시", "화성시"};
        String[][] gyeonggiDongList = {
                {"영통동", "망포동", "매탄동", "이의동"},
                {"삼평동", "서현동", "정자동", "야탑동"},
                {"장항동", "마두동", "백석동", "식사동"},
                {"풍덕천동", "죽전동", "동천동", "상현동"},
                {"범계동", "평촌동", "관양동", "호계동"},
                {"중동", "상동", "심곡동", "소사본동"},
                {"다산동", "별내동", "와부읍", "진접읍"},
                {"동탄동", "향남읍", "봉담읍", "새솔동"}
        };

        for (int i = 0; i < gyeonggiSiList.length; i++) {
            Region si = Region.builder().name(gyeonggiSiList[i]).scope(2).parent(gyeonggi).build();
            level2Districts.add(si);
        }
        regionRepository.saveAll(level2Districts);

        for (int i = 0; i < seoulGuList.length; i++) {
            Region parentGu = level2Districts.get(i);
            for (int j = 0; j < 4; j++) {
                level3Towns.add(Region.builder().name(seoulDongList[i][j]).scope(3).parent(parentGu).build());
            }
        }
        for (int i = 0; i < gyeonggiSiList.length; i++) {
            Region parentSi = level2Districts.get(i + seoulGuList.length);
            for (int j = 0; j < 4; j++) {
                level3Towns.add(Region.builder().name(gyeonggiDongList[i][j]).scope(3).parent(parentSi).build());
            }
        }
        regionRepository.saveAll(level3Towns);

        RoomType oneRoom = RoomType.builder().name("원룸").isDeleted(false).build();
        RoomType twoRoom = RoomType.builder().name("투룸").isDeleted(false).build();
        RoomType threeRoomPlus = RoomType.builder().name("쓰리룸+").isDeleted(false).build();
        RoomType officetel = RoomType.builder().name("오피스텔").isDeleted(false).build();
        RoomType apartment = RoomType.builder().name("아파트").isDeleted(false).build();
        roomTypeRepository.saveAll(List.of(oneRoom, twoRoom, threeRoomPlus, officetel, apartment));

        RoomExtraOption fullOption = RoomExtraOption.builder().name("풀옵션").isDeleted(false).build();
        RoomExtraOption elevator = RoomExtraOption.builder().name("엘레베이터").isDeleted(false).build();
        RoomExtraOption parking = RoomExtraOption.builder().name("주차가능").isDeleted(false).build();
        RoomExtraOption veranda = RoomExtraOption.builder().name("베란다/발코니").isDeleted(false).build();
        RoomExtraOption petAvailable = RoomExtraOption.builder().name("반려동물 협의").isDeleted(false).build();
        RoomExtraOption securityCctv = RoomExtraOption.builder().name("보안/CCTV").isDeleted(false).build();

        roomExtraOptionRepository.saveAll(List.of(fullOption, elevator, parking, veranda, petAvailable, securityCctv));

        InquiryCategory catAccount = InquiryCategory.builder().title("계정/인증").isDeleted(false).build();
        InquiryCategory catRoom = InquiryCategory.builder().title("방 등록/매칭").isDeleted(false).build();
        InquiryCategory catAbuse = InquiryCategory.builder().title("불량유저 신고").isDeleted(false).build();
        InquiryCategory catEtc = InquiryCategory.builder().title("기타 문의").isDeleted(false).build();

        inquiryCategoryRepository.saveAll(List.of(catAccount, catRoom, catAbuse, catEtc));
    }
}
