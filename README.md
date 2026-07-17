# 11th-1team-BE (KnockIn)

> **프로그라피 11기 1팀 룸메이트 매칭 서비스 "KnockIn" 백엔드 애플리케이션 레포지토리입니다.**

---

## 🛠️ Tech Stack (기술 스택)

- **Language**: Java 17
- **Framework**: Spring Boot 3.x
- **Build Tool**: Gradle
- **Database**: JPA (Spring Data JPA), QueryDSL (동적 쿼리 최적화), H2 (Local/Test), MySQL (Production)
- **Security**: Spring Security, OAuth2 Client, JWT (Json Web Token)
- **API Documentation**: Swagger UI (Springdoc OpenAPI 3.x)

---

## 📌 Key Features (주요 기능)

1. **룸메이트 매칭 찾기 (Roommate Matching)**
   - QueryDSL 기반의 성별, 보증금, 월세, 방 형태, 지역 등을 반영한 고성능 동적 조건 필터링 검색
   - 다중 1:N 조인 최적화 처리를 적용한 게시물 조회
2. **보안 및 인증 (Security & Authentication)**
   - OAuth2 소셜 로그인 연동 및 JWT 토큰 기반 무상태(Stateless) 인증 아키텍처
   - API 접근 권한 제어 (비인증 허용 API 분리 설정)
3. **백오피스 관리 기능 (Backoffice BO)**
   - 공지사항, FAQ, 이용약관 등 메타 데이터 관리 API 제공
   - 사용자 신고 관리 및 인증 승인/반려 기능

---

## 🚀 Local Setup & Run (로컬 실행 가이드)

### 1. 🔑 SSL/HTTPS 로컬 Keystore 발급
로컬에서 소셜 로그인 및 HTTPS 프로토콜 검증을 정상적으로 수행하기 위해, 루트에 위치한 `key.pem`과 `cert.pem`을 스프링 부트가 인식할 수 있는 PKCS12 형식의 Keystore 파일로 변환해야 합니다.

프로젝트 루트 디렉토리에서 아래 명령어를 실행하여 `keystore.p12` 파일을 생성합니다:

```bash
openssl pkcs12 -export -out keystore.p12 -inkey key.pem -in cert.pem -name springboot
```

> [!NOTE]
> - 명령어 실행 시 Keystore에 지정할 비밀번호를 묻습니다. 설정한 비밀번호는 아래 `application.yml` 설정에 기입해야 합니다.
> - 생성된 `keystore.p12` 파일은 `src/main/resources/` 하단으로 이동시키거나 외부 경로로 마운트하여 사용할 수 있습니다.

### 2. Build & Run
```bash
# 빌드 및 테스트 수행
./gradlew clean build

# 애플리케이션 실행
./gradlew bootRun
```