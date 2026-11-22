# MyHeritage (우리家 - 가계도 & 기념일/기일/제사 통합 관리)

가족과 가계도, 생일·기일 등 가족 일정을 기록하고 관리하는 Android 앱입니다. 음력 날짜 변환, D-Day 위젯, 백그라운드 알림, 파일 백업과 복원 기능을 제공합니다.

---

## 🌟 주요 기능

- **2D 뷰포트 인터랙티브 가계도:** 핀치 줌(확대/축소) 및 오프셋 드래그 탐색으로 부모, 형제자매, 배우자, 자녀 간의 족보 계보도를 캔버스 위에서 자유롭게 조망할 수 있습니다.
- **가족 & 인물 체계적 관리:** 이름, 호칭, 성별 구분과 함께 현재 만 나이 및 별세 나이(향년 세세)를 자동 연산하여 시각적 배지로 표시합니다.
- **음/양력 기념일 계산:** 음력으로 등록한 반복 일정의 양력 날짜를 계산해 표시합니다. 알림 작업은 12시간 간격으로 실행되도록 등록되며, 당일과 하루 전 알림을 확인합니다.
- **6종 커스텀 정렬 기능:**
  - 기본 (원래 날짜 오름차순 / 내림차순)
  - 오늘 기준 다가오는 D-Day순 (오름차순 / 내림차순)
  - 올해 양력 달력순 (1월~12월 오름차순 / 내림차순)
- **4x1 라이트·다크 홈 화면 위젯:** 두 가지 테마의 일정 위젯을 제공하며, 인물·가족·기념일 데이터가 변경되면 위젯 갱신을 요청합니다.
- **WorkManager 알림:** 반복 일정의 당일(D-0)과 하루 전(D-1)에 알림을 표시합니다. 작업 주기는 시스템 사정에 따라 달라질 수 있습니다.
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

## 빌드

로컬 debug APK는 `./build.sh debug`, 서명된 release APK는 `./build.sh release`로 빌드합니다. Release 서명 정보는 로컬 `~/.bin/KeyStore/`에서 읽습니다. GitHub Actions는 별도 workflow에서 빌드합니다.

---

## 📱 주요 화면 안내

- **메인 대시보드:** 총 인물/가족/기념일 통계 카드, 바로가기 격자 메뉴 8종 카드, 다가오는 D-Day 퀵 카운트다운 카드 및 About 소개 다이얼로그.
- **가계도 2D 뷰포트:** 자유로운 드래그 & 줌 인터랙션으로 집안 계보도 탐색 및 노드별 나이/별세 세부 정보 확인.
- **기념일 & 기일 목록:** 6종 정렬 필터로 다가오는 일정 및 원래 날짜별 정렬 조회.
- **설정 및 데이터 관리:** 화면 테마 지정, SAF 파일 내보내기/가져오기 복원, 위험 구역 전체 데이터 초기화.

---

## 📄 라이선스

이 프로젝트는 [Apache License v2.0](LICENSE) 라이선스 하에 배포됩니다.
