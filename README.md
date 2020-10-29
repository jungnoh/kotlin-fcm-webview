# kotlin-webview
FCM 푸쉬 알림을 받을 수 있는 안드로이드 웹뷰입니다. 여러 프로젝트에서 사용할 수 있게 뼈대만 만들어져 있습니다.

## 기능
- 새로고침, 이전/다음페이지 버튼만 포함된 간단한 웹뷰 UI
- FCM 토큰 생성 및 알림 수신
- alert, confirm 등 다이얼로그 지원
- 파일 선택, 사진 찍기 등 다이얼로그에 대한 Intent 처리

## 사용 방법
1. Firebase에서 프로젝트를 셋팅한 후, google-services.json을 추가합니다.
2. Config.kt에서 시작 페이지, User-Agent를 수정합니다.
이후에는 필요에 맞게 앱을 수정해 사용하면 됩니다.

## 라이선스
MIT