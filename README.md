# SAP JCo Connector

SAP JCo를 사용한 RFC 연결 및 통합 서버 프로젝트입니다.

## 🔒 보안 주의사항

이 프로젝트는 공개용으로 수정되었습니다. 실제 운영 환경에서 사용하기 전에 다음 설정들을 변경해야 합니다:

### 필수 환경 변수 설정

```bash
# SAP 연결 정보
export SAP_HOST=your_sap_host
export SAP_SYSNR=your_system_number
export SAP_CLIENT=your_client_number
export SAP_USER=your_sap_user
export SAP_PASSWD=your_sap_password

# 데이터베이스 연결 정보
export DB_USER=your_db_user
export DB_PASSWORD=your_db_password
```

### 설정 파일 수정

다음 파일들의 민감한 정보를 실제 값으로 변경하세요:

1. `src/main/resources/application.yml`
2. `src/main/resources/config/application-test-local.yml`
3. `src/main/resources/config/application-test-prd.yml`
4. `ABAP_AS_WITHOUT_POOL.jcoDestination`
5. `SERVER.jcoServer`

## 🚀 실행 방법

### 1. 환경 변수 설정
```bash
# Windows
set SAP_HOST=your_sap_host
set SAP_USER=your_sap_user
set SAP_PASSWD=your_sap_password

# Linux/Mac
export SAP_HOST=your_sap_host
export SAP_USER=your_sap_user
export SAP_PASSWD=your_sap_password
```

### 2. 프로젝트 빌드
```bash
./gradlew build
```

### 3. 애플리케이션 실행
```bash
./gradlew bootRun
```

## 📁 프로젝트 구조

```
src/
├── main/
│   ├── java/
│   │   └── jco/jcosaprfclink/
│   │       ├── config/          # 설정 클래스들
│   │       │   ├── aop/         # AOP 설정 (성능 측정 등)
│   │       │   └── saprfc/      # SAP RFC 연결 설정
│   │       │       ├── connection/  # JCo 연결 관리
│   │       │       │   └── handler/ # RFC 함수 핸들러
│   │       │       ├── properties/  # 연결 속성 클래스
│   │       │       └── util/        # RFC 유틸리티
│   │       ├── domain/          # 도메인 모델
│   │       ├── dto/             # 데이터 전송 객체
│   │       ├── exception/       # 예외 처리 클래스
│   │       ├── repository/      # 데이터 접근 계층
│   │       ├── service/         # 비즈니스 로직
│   │       ├── type/            # 열거형 및 타입 정의
│   │       └── utils/           # 유틸리티 클래스
│   └── resources/
│       ├── application.yml      # 메인 설정 파일
│       └── config/              # 환경별 설정 파일
```

## 🔧 주요 기능

- SAP RFC 연결 관리 및 통합
- JCo 서버 설정 및 핸들러
- RFC 함수 처리 및 응답
- 세금계산서 상태 조회
- 예외 처리 및 로깅

## 📝 로그 설정

로그 파일은 `logs/` 디렉토리에 생성됩니다. 민감한 정보가 포함될 수 있으므로 운영 환경에서는 로그 레벨을 적절히 조정하세요.

## ⚠️ 주의사항

1. **보안**: 실제 운영 환경에서는 모든 민감한 정보를 환경 변수로 관리하세요.
2. **로그**: 로그 파일에 민감한 정보가 기록되지 않도록 주의하세요.
3. **네트워크**: 방화벽 설정을 통해 필요한 포트만 열어두세요.

## 📄 라이선스

이 프로젝트는 MIT 라이선스 하에 배포됩니다. 