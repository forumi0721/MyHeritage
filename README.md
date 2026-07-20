# MyHeritage (우리家 - 가계도 & 기념일/기일/제사 통합 관리)

안전하고 정갈하게 가문의 계보(가계도)를 탐색하고, 생일·기일·제사 등 가문의 소중한 일정을 음/양력 계산 및 D-Day 홈 화면 위젯, 백그라운드 푸시 알림을 통해 스마트하게 관리하는 프리미엄 안드로이드 애플리케이션입니다.

---

## 🌟 주요 기능

- **2D 뷰포트 인터랙티브 가계도:** 핀치 줌(확대/축소) 및 오프셋 드래그 탐색으로 부모, 형제자매, 배우자, 자녀 간의 족보 계보도를 캔버스 위에서 자유롭게 조망할 수 있습니다.
- **가족 & 인물 체계적 관리:** 이름, 호칭, 성별 구분과 함께 현재 만 나이 및 별세 나이(향년 세세)를 자동 연산하여 시각적 배지로 표시합니다.
- **음/양력 기일 & 기념일 자동 연산:** 음력 기일의 연도별 양력 변환과 사망 기록 감지 시 기일 하루 전날 "제사" 일정을 자동 산출하며, 사용자의 매년 반복 지정 옵션과 상호 연동됩니다.
- **6종 커스텀 정렬 기능:** 
  - 기본 (원래 날짜 오름차순 / 내림차순)
  - 오늘 기준 다가오는 D-Day순 (오름차순 / 내림차순)
  - 올해 양력 달력순 (1월~12월 오름차순 / 내림차순)
- **4x1 듀얼 스킨 D-Day 홈 화면 위젯:** 안드로이드 12+ 모던 디자인 규격의 라이트 & 다크 스킨 위젯을 제공하며, 앱 내 데이터 변경 시 0.1초 실시간 라이브 자발적 갱신(Live Broadcast Update)을 지원합니다.
- **WorkManager 백그라운드 푸시 알림:** 매년 반복되는 생일, 기일, 제사 당일(D-0) 및 하루 전(D-1) 상단바 자동 푸시 알림을 발송합니다.
- **Storage Access Framework (SAF) 백업 및 복원:** 사용자가 직접 원하는 저장소 경로와 파일명을 지정하여 내보내고(Export), 복원(Import) 시 프로세스 자동 재부팅을 통해 SQLite 인메모리 락 없이 100% 즉시 데이터를 반영합니다.
- **화면 테마 실시간 스위칭:** 시스템 기본값 / 라이트 / 다크 테마 런타임 스위칭 및 설정 화면 스크롤 안전 뷰를 제공합니다.

---

## 🛠 사용된 기술

- **UI / Framework:** Jetpack Compose, Material Design 3, Navigation3 (Single Activity Architecture)
- **Architecture:** MVVM (Model-View-ViewModel), Coroutines, Flow, StateIn
- **Database:** Room Database (SQLite Engine, Write-Ahead Logging & Full Checkpoint Sync)
- **Background & Widget:** WorkManager (PeriodicWorkRequest), AppWidgetProvider (RemoteViews, Live Broadcast Update)
- **Storage:** Storage Access Framework (SAF) Document Picker Stream I/O

---

## 🚀 설치 및 실행 방법

1. 저장소에서 프로젝트를 클론합니다.
```bash
git clone https://github.com/forumi0721/MyHeritage.git
```
2. Android Studio (최신 버전 권장)에서 프로젝트를 엽니다.
3. 안드로이드 기기 또는 에뮬레이터를 연결하고 앱을 빌드 및 실행합니다.

---

## 📱 주요 화면 안내

- **메인 대시보드:** 총 인물/가족/기념일 통계 카드, 바로가기 격자 메뉴 8종 카드, 다가오는 D-Day 퀵 카운트다운 카드 및 About 소개 다이얼로그.
- **가계도 2D 뷰포트:** 자유로운 드래그 & 줌 인터랙션으로 집안 계보도 탐색 및 노드별 나이/별세 세부 정보 확인.
- **기념일 & 기일 목록:** 6종 정렬 필터로 다가오는 일정 및 원래 날짜별 정렬 조회.
- **설정 및 데이터 관리:** 화면 테마 지정, SAF 파일 내보내기/가져오기 복원, 위험 구역 전체 데이터 초기화.

---

## 📄 라이선스

이 프로젝트는 [Apache License v2.0](LICENSE) 라이선스 하에 배포됩니다.
