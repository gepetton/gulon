<div align="center">

# 📚 Gulon

**독서 모임과 소셜 도서 관리를 위한 통합 플랫폼**

[![Spring Boot](https://img.shields.io/badge/Spring%20Boot-3.5.0-brightgreen.svg)](https://spring.io/projects/spring-boot)
[![Java](https://img.shields.io/badge/Java-17-orange.svg)](https://openjdk.java.net/)
[![MySQL](https://img.shields.io/badge/MySQL-8.0-blue.svg)](https://www.mysql.com/)
[![Redis](https://img.shields.io/badge/Redis-6.0-red.svg)](https://redis.io/)
[![License](https://img.shields.io/badge/License-MIT-yellow.svg)](LICENSE)
[![API Docs](https://img.shields.io/badge/API%20Docs-Swagger-85EA2D.svg)](http://localhost:8080/swagger-ui.html)

**📖 [API 문서](http://localhost:8080/swagger-ui.html) | 🚀 [데모](https://gulon.com)**

</div>

---

## 🌟 프로젝트 소개

Gulon은 독서를 사랑하는 사람들을 위한 **올인원 소셜 독서 플랫폼**입니다. 

독서 그룹을 만들고, 친구들과 함께 책을 읽으며, 진행 상황을 공유하고, 실시간으로 소통할 수 있는 공간을 제공합니다. 또한 네이버 도서 API를 통해 풍부한 도서 정보를 제공하여 새로운 책을 발견하고 관리할 수 있도록 도와줍니다.

### ✨ 주요 특징

- 📖 **독서 그룹 관리**: 공개/비공개 독서 모임 생성 및 참여
- 👥 **소셜 기능**: 실시간 채팅, 독서 진행률 공유, 멤버 활동 추적
- 📚 **도서 관리**: 네이버 도서 API 연동으로 풍부한 도서 정보 제공
- 📊 **독서 통계**: 개인/그룹별 독서 현황 및 통계 대시보드
- 🔐 **보안**: JWT 기반 인증, OAuth2 소셜 로그인 지원
- 🚀 **실시간**: WebSocket과 Redis Stream을 이용한 실시간 메시징

---

## 🛠️ 기술 스택

### Backend
- **Framework**: Spring Boot 3.5.0
- **Language**: Java 17
- **Database**: MySQL 8.0
- **Cache**: Redis 6.0
- **Security**: Spring Security + JWT
- **Documentation**: Swagger/OpenAPI 3.0
- **Mapping**: MapStruct 1.5.5

### 핵심 라이브러리
- **WebSocket**: 실시간 통신
- **Redis Stream**: 메시지 브로커
- **JPA/Hibernate**: ORM
- **Validation**: Bean Validation
- **Testing**: Spring Boot Test

### 외부 API
- **네이버 도서 검색 API**: 도서 정보 제공
- **OAuth2**: 소셜 로그인 (구글, 네이버, 카카오 등)

---

## 🏗️ 시스템 아키텍처

```
┌─────────────────┐    ┌─────────────────┐    ┌─────────────────┐
│   Frontend      │◄──►│   Spring Boot   │◄──►│     MySQL       │
│   (React)       │    │   Application   │    │   Database      │
└─────────────────┘    └─────────────────┘    └─────────────────┘
                              ▲ │                        ▲
                              │ │                        │
                              │ ▼                        │
                       ┌─────────────────┐               │
                       │     Redis       │               │
                       │ (Cache/Stream)  │               │
                       └─────────────────┘               │
                              ▲                          │
                              │                          │
                              ▼                          │
                       ┌─────────────────┐               │
                       │  Naver Book API │               │
                       │                 │               │
                       └─────────────────┘               │
                                                         │
                       ┌─────────────────────────────────┘
                       │
                       ▼
                ┌─────────────────┐
                │   WebSocket     │
                │  (Real-time)    │
                └─────────────────┘
```

---

## 📱 주요 기능

### 👥 사용자 관리
- ✅ 회원가입/로그인
- ✅ OAuth2 소셜 로그인 (구글, 네이버, 카카오)
- ✅ 프로필 관리
- ✅ 사용자 상태 관리

### 📚 도서 관리
- ✅ 네이버 도서 검색 API 연동
- ✅ 도서 정보 캐싱
- ✅ 개인 도서 라이브러리
- ✅ 도서 리뷰 및 평점

### 👨‍👩‍👧‍👦 그룹 관리
- ✅ 독서 그룹 생성/참여
- ✅ 공개/비공개 그룹 설정
- ✅ 그룹 멤버 역할 관리
- ✅ 그룹별 독서 현황

### 📖 독서 기록
- ✅ 개인별 독서 진행률 추적
- ✅ 읽기 시작/완료 날짜 기록
- ✅ 독서 노트 및 하이라이트
- ✅ 독서 통계 대시보드

### 💬 실시간 채팅
- ✅ 그룹 채팅방
- ✅ WebSocket 기반 실시간 메시징
- ✅ 메시지 히스토리 관리
- ✅ 파일 첨부 지원

### 📊 통계 및 분석
- ✅ 개인 독서 통계
- ✅ 그룹별 활동 현황
- ✅ 독서 트렌드 분석
- ✅ 성취 배지 시스템

---

## 📖 API 문서

상세한 API 문서는 Swagger UI에서 확인할 수 있습니다.

**🔗 [Swagger UI 바로가기](http://localhost:8080/swagger-ui.html)**

### 주요 API 엔드포인트

| 기능 | 메서드 | 엔드포인트 | 설명 |
|------|--------|-----------|------|
| 사용자 | `GET` | `/api/users` | 사용자 목록 조회 |
| 사용자 | `POST` | `/api/users` | 사용자 생성 |
| 그룹 | `GET` | `/api/groups` | 그룹 목록 조회 |
| 그룹 | `POST` | `/api/groups` | 그룹 생성 |
| 도서 | `GET` | `/api/books/search` | 도서 검색 |
| 독서기록 | `GET` | `/api/reading-records` | 독서 기록 조회 |
| 메시지 | `POST` | `/api/messages` | 메시지 전송 |

---

## 🗂️ 프로젝트 구조

```
src/
├── main/
│   ├── java/com/gulon/app/
│   │   ├── config/          # 설정 클래스
│   │   ├── controller/      # REST 컨트롤러
│   │   ├── dto/            # 데이터 전송 객체
│   │   ├── entity/         # JPA 엔티티
│   │   ├── mapper/         # MapStruct 매퍼
│   │   ├── repository/     # JPA 레포지토리
│   │   ├── security/       # 보안 설정
│   │   └── service/        # 비즈니스 로직
│   └── resources/
│       ├── application.properties
│       └── static/
└── test/                   # 테스트 코드
```

---
## 📄 라이선스

이 프로젝트는 MIT 라이선스 하에 있습니다. 자세한 내용은 [LICENSE](LICENSE) 파일을 참조하세요.

---

## 📞 contact

- 📧 **Email**: sehyeon73@gmail.com
- 🐛 **버그 리포트**: [Issues](https://github.com/yourusername/gulon-backend/issues)
- 💡 **기능 제안**: [Discussions](https://github.com/yourusername/gulon-frontend/discussions)

---

<div align="center">

**📚 함께 읽는 즐거움, Gulon과 함께하세요! 📚**

⭐ 이 프로젝트가 마음에 드셨다면 Star를 눌러주세요!

</div>