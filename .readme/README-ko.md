<!--suppress HtmlDeprecatedAttribute, HttpUrlsUsage -->

<div align="center">
  <p>
    <img src="https://github.com/SuperMonster003/AutoJs6-Plugin-Yolo-NCNN/blob/master/app/src/main/res/mipmap/ic_launcher.png?raw=true" alt="autojs6-plugin-yolo-ncnn-ic-launcher" border="0" width="128" />
  </p>

  <h1>AutoJs6 Plugin: YOLO NCNN</h1>

  <p>AutoJs6를 위한 프로세스 격리형, 완전 오프라인 YOLO 객체 탐지 플러그인 (NCNN 20260526 백엔드)</p>

  <p>
    <a href="https://github.com/SuperMonster003/AutoJs6-Plugin-Yolo-NCNN/releases"><img alt="GitHub release (latest by date)" src="https://img.shields.io/github/v/release/SuperMonster003/AutoJs6-Plugin-Yolo-NCNN?label=Release"/></a>
    <a href="https://github.com/SuperMonster003/AutoJs6-Plugin-Yolo-NCNN/issues"><img alt="GitHub closed issues" src="https://img.shields.io/github/issues/SuperMonster003/AutoJs6-Plugin-Yolo-NCNN?color=A24232&label=Issues"/></a>
    <a href="https://github.com/SuperMonster003/AutoJs6-Plugin-Yolo-NCNN/commit/01b9093c55c7c1a78f39246c671e928df244483f"><img alt="Created" src="https://img.shields.io/date/1786442764?color=2e7d32&label=Created"/></a>
    <br>
    <a href="https://developer.android.com/studio/archive"><img alt="Android Studio" src="https://img.shields.io/badge/Android%20Studio-2023.3+-B64FC8"/></a>
    <a href="https://www.jetbrains.com/idea/download/other.html"><img alt="IntelliJ IDEA" src="https://img.shields.io/badge/IntelliJ%20IDEA-2023.3+-EE4677"/></a>
    <a href="https://github.com/SuperMonster003/AutoJs6-Plugin-Yolo-NCNN/blob/master/LICENSE"><img alt="GitHub License" src="https://img.shields.io/github/license/SuperMonster003/AutoJs6-Plugin-Yolo-NCNN?color=534BAE&label=License"/></a>
  </p>
</div>

******

### 언어 (Languages)

******

현재 README.md는 다음 언어를 지원합니다:

- [简体中文 [zh-Hans]](https://github.com/SuperMonster003/AutoJs6-Plugin-Yolo-NCNN/blob/master/.readme/README-zh-Hans.md)
- [繁體中文 (香港) [zh-Hant-HK]](https://github.com/SuperMonster003/AutoJs6-Plugin-Yolo-NCNN/blob/master/.readme/README-zh-Hant-HK.md)
- [繁體中文 (台灣) [zh-Hant-TW]](https://github.com/SuperMonster003/AutoJs6-Plugin-Yolo-NCNN/blob/master/.readme/README-zh-Hant-TW.md)
- [English [en]](https://github.com/SuperMonster003/AutoJs6-Plugin-Yolo-NCNN/blob/master/.readme/README-en.md)
- [Français [fr]](https://github.com/SuperMonster003/AutoJs6-Plugin-Yolo-NCNN/blob/master/.readme/README-fr.md)
- [Español [es]](https://github.com/SuperMonster003/AutoJs6-Plugin-Yolo-NCNN/blob/master/.readme/README-es.md)
- [日本語 [ja]](https://github.com/SuperMonster003/AutoJs6-Plugin-Yolo-NCNN/blob/master/.readme/README-ja.md)
- 한국어 [ko] # 현재
- [Русский [ru]](https://github.com/SuperMonster003/AutoJs6-Plugin-Yolo-NCNN/blob/master/.readme/README-ru.md)
- [العربية [ar]](https://github.com/SuperMonster003/AutoJs6-Plugin-Yolo-NCNN/blob/master/.readme/README-ar.md)

******

### 소개

******

AutoJs6 YOLO NCNN 플러그인을 사용하면 AutoJs6 스크립트가 YOLO 객체 탐지를 완전히 기기 내에서 실행할 수 있습니다: 이미지를 전달하면 라벨, 신뢰도, 픽셀 좌표 바운딩 박스를 담은 탐지 결과 배열이 반환됩니다. 추론은 Tencent NCNN 20260526이 독립된 `:provider` 프로세스에서 수행하며, 네트워크 접근도 데이터 업로드도 없습니다. 모델 파일은 사용자가 준비하며, 플러그인 APK에는 어떤 모델도 포함되지 않습니다.

```text
application ID: io.github.supermonster003.autojs6.plugin.yolo.ncnn
plugin / engine / variant: yolo-ncnn / yolo / ncnn
provider ID: autojs6-yolo-ncnn
discovery actions: org.autojs.plugin.INFO / org.autojs.plugin.YOLO
runtime process: :provider
protocol version: 1.0
backend / task / decoder: ncnn / detect / ultralytics-detect
supported ABI: arm64-v8a, armeabi-v7a, x86, x86_64
minimum host build: 5275 (AutoJs6 6.8.0+)
```

위 정보는 호스트가 이 플러그인을 발견하고 바인딩하는 데 사용하는 고정 식별 정보입니다. 모델은 호스트가 읽기 전용 파일 디스크립터로 전달하며 항상 외부 리소스로 남습니다.

******

### 기능

******

- 오프라인 YOLO11 객체 탐지: AutoJs6 `images` 모듈의 이미지 객체를 입력하면 라벨 + 신뢰도 + 바운딩 박스를 출력하며, 전 과정이 기기 내에서 완결됩니다.
- 프로세스 격리: 추론은 독립된 `:provider` 프로세스에서 실행되어 네이티브 계층 오류가 AutoJs6 메인 프로세스에 영향을 주지 않습니다. 서비스는 `org.autojs.permission.PLUGIN` 권한과 서명 검사로 보호됩니다.
- NCNN 20260526 CPU 추론 백엔드: 스레드 수 조절 가능 (기본 4, 최대 64), `arm64-v8a, armeabi-v7a, x86, x86_64` 기기 지원.
- 매니페스트 기반 모델 호환성: `model.json`이 입출력과 라벨을 선언하며, 1개부터 256개까지의 커스텀 클래스를 지원합니다. 공식 YOLO11 모델과 직접 학습한 모델 모두 동일하게 사용할 수 있습니다.
- 모델 안전 검증: 세션 오픈 시 세 모델 파일의 선언 길이와 SHA-256을 검증하고, 실행 시 NCNN 그래프의 실제 출력 형태도 검증합니다. 불일치는 추측 없이 거부됩니다.
- 안정적인 오류 분류: 컴포넌트 누락, Provider 사용 불가, 모델 거부, 미지원 기능 등은 모두 판별 가능한 오류 코드를 반환하여 스크립트가 정확히 처리할 수 있습니다.
- 요청 시간 제한과 `detector.close()`를 통한 정리 지원. 네이티브 작업은 협력적 취소를 사용하며 현재 호출이 끝난 후 리소스를 해제할 수 있습니다.
- README와 CHANGELOG는 간체 중국어/번체 중국어 (홍콩/대만)/영어/프랑스어/스페인어/일본어/한국어/러시아어/아랍어 10개 언어를 지원합니다.

******

### 빠른 시작

******

- **설치** — 이 플러그인은 현재 비공개 스테이징 단계입니다 (아래 프로젝트 상태 절 참조): 호환 호스트 AutoJs6 6.8.0 (빌드 5275)가 정식 출시된 후에야 공개 다운로드와 공식 플러그인 인덱스 등재가 이루어집니다. 그 전에는 아래 빌드 절에 따라 TEST-SIGNED 후보를 직접 빌드하여 같은 디버그 인증서를 쓰는 AutoJs6 테스트 APK와 짝지어 설치할 수 있습니다. 호스트와 플러그인은 동일 인증서로 서명되어야 합니다.
- **활성화** — 플러그인 설치만으로는 YOLO가 켜지지 않습니다: AutoJs6 호스트가 선택, 신뢰, 활성화 스위치를 명시적으로 보유하므로 (YOLO 경로는 기본 비활성), 호스트에서 이 Provider를 활성화하고 신뢰해야 합니다. 스크립트 쪽에서도 `yolo.load`의 `options.component`에 컴포넌트 문자열을 명시해야 하며, 암묵적 폴백은 없습니다.
- **실행** — `model.json`, `model.ncnn.param`, `model.ncnn.bin` 세 파일이 들어 있는 모델 디렉터리를 준비하고 (아래 모델 준비 절 참조), `yolo.load(modelDir, options)`로 탐지기를 열고, `detector.detect(image, options)`로 탐지 배열을 얻은 뒤, 사용이 끝나면 `detector.close()`로 해제합니다.
- **문제 해결** — `yolo.load`와 `detector.detect`가 던지는 예외는 안정적인 오류 분류를 갖습니다: `COMPONENT_REQUIRED` (컴포넌트 미지정), `PROVIDER_UNAVAILABLE` (호스트가 Provider를 찾지 못하거나 신뢰하지 않음), `MODEL_REJECTED` (모델이나 매니페스트가 검증 실패, 상세에 `MANIFEST_*` 등 접두사 포함), `UNSUPPORTED_CAPABILITY` (CPU/detect 밖의 기능 요청), `SESSION_CLOSED`, `DETECT_FAILED` 등. 아래 기능 경계 절과 [모델 매니페스트 명세](https://github.com/SuperMonster003/AutoJs6-Plugin-Yolo-NCNN/blob/master/docs/model-manifest-v1.md)를 대조하며 조사하세요.

******

### 사용 예시

******

바로 실행 가능한 최소 예시입니다 (호스트 저장소의 `sample/yolo/detect.js`도 참조):

```javascript
"use strict";

const providerComponent = "io.github.supermonster003.autojs6.plugin.yolo.ncnn/.provider.YoloProviderService";
const modelDirectory = files.path("./models/yolo11n");

let detector = null;
let image = null;

try {
    detector = yolo.load(modelDirectory, {
        component: providerComponent,
        device: "cpu",
        threads: 4,
        decoderId: "ultralytics-detect",
        timeoutMillis: 120000,
    });
    image = images.read(files.path("./bus.jpg"), true);

    const detections = detector.detect(image, {
        confidence: 0.25,
        iouThreshold: 0.45,
        maxDetections: 100,
        timeoutMillis: 30000,
    });

    detections.forEach((detection) => {
        const bounds = detection.bounds;
        console.log(
            detection.label + " " + detection.confidence.toFixed(4)
            + " [" + bounds.left + ", " + bounds.top + ", " + bounds.right + ", " + bounds.bottom + "]",
        );
    });
} finally {
    if (image !== null) {
        images.recycle(image);
    }
    if (detector !== null) {
        detector.close();
    }
}
```

각 탐지 항목은 `classId`, `label`, `confidence`, `bounds` (Android `RectF`: `left` / `top` / `right` / `bottom` 필드와 `centerX()` / `centerY()` 메서드)를 가지며, 좌표는 입력 이미지의 픽셀 좌표입니다. 탐지기는 단일 요청 직렬 세션으로, 추론 큐 길이가 0이므로 같은 탐지기에 대한 동시 두 번째 `detect`는 대기하지 않고 즉시 실패합니다.

******

### 모델 준비

******

모델 디렉터리는 항상 정확히 세 파일로 구성되며, 파일 이름이 역할을 결정합니다:

```text
models/yolo11n/
|-- model.json
|-- model.ncnn.param
`-- model.ncnn.bin
```

공식 또는 직접 학습한 Ultralytics YOLO11 detect 모델은 `yolo export format=ncnn imgsz=640`로 내보낼 수 있으며, `model.ncnn.param`과 `model.ncnn.bin`이 생성됩니다 ([Ultralytics NCNN 내보내기 가이드](https://docs.ultralytics.com/integrations/ncnn/) 참조). `model.json`은 Model Manifest v1 문서로, 입력 (`in0`, RGB NCHW, 640x640 letterbox), 출력 (`out0`, `ultralytics-detect` 디코더, 형태 `[1, 4 + N, 8400]`, N은 클래스 수), 라벨 목록을 선언합니다. 완전한 예시는 [fixtures/yolo11n/model-manifest-v1.json](https://github.com/SuperMonster003/AutoJs6-Plugin-Yolo-NCNN/blob/master/fixtures/yolo11n/model-manifest-v1.json)입니다.

`python tools/generate_yolo_ncnn_manifest.py <export-directory>`를 실행하면 Ultralytics `metadata.yaml`에서 `model.json`을 바로 생성합니다. 표준 라이브러리만 사용하는 오프라인 도구가 고정 YOLO11/detect/640/batch/레이블 프로필과 NCNN `in0`/`out0` 그래프 구조를 검증한 뒤 기록하며, `--check`는 쓰기 없이 드리프트를 거부합니다. 정확한 내보내기 명령, 검증 경계와 문제 해결은 [모델 변환 가이드](https://github.com/SuperMonster003/AutoJs6-Plugin-Yolo-NCNN/blob/master/docs/model-conversion.md)를 참조하십시오.

매니페스트는 호환성 계약이지 라벨 갈아붙이기 도구가 아닙니다: 세션 오픈 시 세 파일의 선언 길이와 SHA-256을 검증하고, 실행 시 NCNN 그래프의 실제 출력 형태도 검증하며, 불일치는 `MODEL_REJECTED`로 거부됩니다 (상세 접두사는 `MANIFEST_SHAPE_INVALID`, `MODEL_GRAPH_REJECTED` 등). 모델은 출처의 라이선스와 사용 조건을 유지하며, NCNN 변환은 이를 바꾸지 않습니다. 플러그인이 재배포 권리를 부여하지도 않습니다. 자세한 내용은 [모델 매니페스트 명세](https://github.com/SuperMonster003/AutoJs6-Plugin-Yolo-NCNN/blob/master/docs/model-manifest-v1.md)와 [모델 라이선스 정책](https://github.com/SuperMonster003/AutoJs6-Plugin-Yolo-NCNN/blob/master/docs/model-license-policy.md)을 참조하세요.

******

### 스크립트 API

******

`yolo.load(modelDir, options)`는 탐지 세션을 열고 `YoloDetector`를 반환합니다. `options.component`는 필수입니다 (패키지명/클래스명 형태의 컴포넌트 문자열, 이 플러그인은 `io.github.supermonster003.autojs6.plugin.yolo.ncnn/.provider.YoloProviderService`). 선택 항목: `device` (현재 `"cpu"`만 허용), `threads` (기본 4, 최대 64), `decoderId` (기본 `ultralytics-detect`), `timeoutMillis` (모델 오픈 전체 타임아웃, 기본 120000 ms, 최대 600000 ms).

`detector.detect(image, options)`는 이미지 한 장에 대해 동기적으로 탐지를 실행하고 탐지 배열을 반환합니다. `image`는 AutoJs6 `images` 모듈의 이미지 객체입니다 (`images.read`, 스크린샷 등). 선택 항목: `confidence` (신뢰도 임계값, 기본 0.25), `iouThreshold` (NMS IoU 임계값, 기본 0.45), `maxDetections` (기본 100, 최대 400), `timeoutMillis` (기본 30000 ms).

`detector.close()`는 세션과 네이티브 리소스를 해제하며 여러 번 호출해도 안전합니다. 스크립트 종료 시 AutoJs6도 대신 닫아 주지만, `try...finally`로 명시적으로 해제하는 것을 권장합니다. 닫힌 뒤 `detect`를 호출하면 `SESSION_CLOSED`가 반환됩니다.

******

### 기능 경계

******

예측 가능한 동작을 위해, 다음 범위를 벗어나는 요청은 조용한 폴백 없이 명시적으로 거부됩니다:

- CPU 추론만 지원: Vulkan/GPU는 미지원이며 `options.device`는 `"cpu"`만 허용합니다.
- 지원 ABI: `arm64-v8a, armeabi-v7a, x86, x86_64`. universal APK에는 각 ABI의 네이티브 라이브러리가 포함됩니다.
- 객체 탐지 (detect) 작업만 지원: 세그멘테이션, 포즈, OBB, 분류, 추적은 모두 미지원입니다.
- 등록된 디코더는 `ultralytics-detect`뿐: 알 수 없는 `decoderId`는 다른 디코더로 폴백하지 않고 거부됩니다.
- 입력은 640x640 letterbox로 전처리되며 (manifest v1 고정 프로파일), 픽셀 형식은 RGBA_8888입니다.
- 세션당 단일 요청 직렬 추론: 큐 상한이 0이므로 같은 세션의 동시 두 번째 `detect`는 실패합니다.
- 모델 오픈은 일반 파일의 읽기 전용 디스크립터만 허용합니다 (파이프, 소켓 불가). 세 파일 모두 읽을 수 있어야 합니다.
- 이 플러그인 설치만으로 YOLO가 켜지지 않습니다: 활성화, 신뢰, 선택 상태는 항상 AutoJs6 호스트가 보유합니다.

******

### 보안과 격리

******

플러그인은 fail-closed 원칙으로 설계되었으며, 다음 메커니즘이 항상 적용됩니다:

- 추론은 독립된 `:provider` 프로세스에서 실행되어 AutoJs6 메인 프로세스와 격리됩니다. 서비스는 `org.autojs.permission.PLUGIN` 권한과 서명 검사로 보호됩니다.
- 모델은 호스트로부터 읽기 전용 `ParcelFileDescriptor`로 전달됩니다. 플러그인은 스스로 저장소를 읽지 않으며 네트워크 요청도 하지 않습니다.
- 세션 오픈 전에 선언 길이, EOF, SHA-256을 검증합니다. 오픈 전체가 하나의 단조 기한을 공유하며, 만료된 세션은 결코 게시되지 않습니다.
- NCNN 런타임을 로드하거나 초기화할 수 없으면 성능 저하 모드 없이 그대로 실패합니다 (fail closed).
- 손상된 입력은 활성 요청과 격리됩니다. 요청 식별자를 복구할 수 있으면 매달아 두지 않고 정확한 실패 종결 상태를 게시합니다.
- 콜백 측 사망과 오래된 세션은 감지되어 정리됩니다. 네이티브 리소스는 지연 해제되며 닫기 연산은 멱등입니다.

******

### 호환성

******

AutoJs6 버전 코드 5275 이상 (즉 6.8.0 이후)이어야 하며 플러그인과 동일 인증서로 서명되어야 합니다. Android 24+ (Android 7.0), targetSdk 37. 기기는 `arm64-v8a, armeabi-v7a, x86, x86_64`여야 합니다. 플러그인 프로토콜 버전 1.0, 현재 Provider 버전 0.1.3 (버전 코드 36).

******

### 프로젝트 상태

******

이 저장소는 현재 비공개 스테이징 아카이브입니다: 호환 호스트 AutoJs6 6.8.0 (5275)는 아직 정식 출시되지 않았고, 이 플러그인도 공개 배포되거나 공식 플러그인 인덱스에 등재되지 않았습니다. 저장소가 공개되기 전에는 위의 GitHub 배지가 표시되지 않을 수 있습니다. `sign.properties`가 없으면 `assembleRelease`는 서명되지 않은 APK를 생성하며, 이는 소스/빌드 증거일 뿐 배포 가능한 산출물이 아닙니다. 첫 릴리스는 0.1.3 (버전 코드 36, 버전 코드 1의 전신 없음)이며, 결함은 롤백이 아니라 버전 코드 3의 전방 수정으로 해결합니다. 프로덕션 서명, 실기기 최종 검증, 공개 상태는 외부 R6 증거 아카이브가 확정합니다. 자세한 내용은 [엔지니어링 노트](https://github.com/SuperMonster003/AutoJs6-Plugin-Yolo-NCNN/blob/master/docs/engineering-notes.md)를 참조하세요.

******

### 빌드

******

JDK 21+ 권장. Android SDK는 platforms 24과 37, 그리고 NDK 29.0.14206865와 CMake 3.22.1를 제공해야 합니다 (NCNN JNI 컴파일에 필요). 주요 명령:

```powershell
.\gradlew.bat :app:assembleDebug
.\gradlew.bat :app:assembleRc
.\gradlew.bat :app:assembleRelease
```

`assembleRc`는 설치 가능한 universal TEST-SIGNED 후보를 생성합니다: release의 R8과 리소스 축소를 물려받고, 표준 디버그 서명을 사용하며, 버전 이름이 `-rc-test-signed`로 끝납니다. 같은 인증서의 AutoJs6 테스트 APK와 짝지어 실기기 검증에 사용합니다. `assembleRelease`는 릴리스 빌드이며 서명 자료가 없으면 서명되지 않은 채로 남습니다.

메인테이너 게이트 `tools/verify-r6-provider-source.ps1`은 먼저 `--check`로 10개 언어의 README/CHANGELOG 생성물 22개를 모두 검사하며 드리프트가 있으면 즉시 실패합니다. 그런 다음 기본적으로 깨끗한 소스에서 시작합니다: `:app:clean` 후 집중 테스트와 두 가지 APK 조립을 실행하고, 테스트 XML과 산출물 해시를 기록하며, 다섯 파일 애셋 허용 목록으로 APK를 검증합니다. 로컬 검증과 문서 생성 모두 기본적으로 오프라인 (네트워크 호출 0회)으로 실행하여 개발 네트워크의 Cloudflare 502/524/529 노이즈를 피합니다.

Gradle 빌드 전에 같은 게이트가 `tools/generate_yolo_ncnn_manifest.py`의 표준 라이브러리 전용 테스트 8개도 실행하고 소스 해시와 결과를 기록합니다. 모델 도구 회귀는 오프라인 릴리스 사전 검사에서 차단됩니다.

******

### 릴리스 이력

******

# v0.1.3

###### 2026/09/15

* `개선` compileSdk 와 targetSdk 를 37 (Android 17) 로 올리며, 플러그인 동작은 새 대상 버전의 영향을 받지 않음

# v0.1.2

###### 2026/09/13

* `수정` 버전 날짜를 일관된 영어 형식으로 표시
* `수정` 표준 Wake 활성화 진입점을 추가하고 설치된 APK의 네이티브 ABI와 모든 언어의 설명을 표시
* `개선` 다운로드 파일 생성 전에 릴리스 APK의 버전, 서명 및 전체 변형 구성을 검증
* `개선` 네이티브 ABI 패키징과 플러그인 메타데이터를 arm64-v8a, armeabi-v7a, x86, x86_64로 확장하고 범용 APK와 ABI별 APK를 일치시킴

# v0.1.1

###### 2026/09/13

* `개선` 64비트 네이티브 라이브러리의 16 KB 페이지 정렬을 빌드 시 검증, manifest 계약 검사 및 JSON 보고서 지원

##### 더 많은 릴리스 이력은 다음을 참조

* [CHANGELOG-ko.md](https://github.com/SuperMonster003/AutoJs6-Plugin-Yolo-NCNN/blob/master/app/src/main/assets/doc/CHANGELOG-ko.md)

******

### 라이선스

******

플러그인 소스 코드는 [MPL-2.0](https://github.com/SuperMonster003/AutoJs6-Plugin-Yolo-NCNN/blob/master/LICENSE)으로 배포됩니다. APK에는 NCNN의 전체 라이선스와 고지 (BSD-3-Clause 및 업스트림 서드파티 고지), Kotlin 런타임의 Apache-2.0 전문, 서드파티 고지 색인이 포함됩니다. [THIRD_PARTY_NOTICES](https://github.com/SuperMonster003/AutoJs6-Plugin-Yolo-NCNN/blob/master/THIRD_PARTY_NOTICES.md)를 참조하세요. 모델은 외부 리소스로서 출처의 라이선스와 사용 조건을 유지하며, 플러그인은 모델을 포함하지 않고 재배포 권리도 부여하지 않습니다.

******

### 더 읽을거리

******

- [모델 매니페스트 명세 (Model Manifest v1)](https://github.com/SuperMonster003/AutoJs6-Plugin-Yolo-NCNN/blob/master/docs/model-manifest-v1.md)
- [모델 매니페스트 JSON Schema](https://github.com/SuperMonster003/AutoJs6-Plugin-Yolo-NCNN/blob/master/schemas/model-manifest-v1.schema.json)
- [모델 및 검증 애셋 라이선스 정책](https://github.com/SuperMonster003/AutoJs6-Plugin-Yolo-NCNN/blob/master/docs/model-license-policy.md)
- [0.1.0 릴리스 노트 (엔지니어링 문구)](https://github.com/SuperMonster003/AutoJs6-Plugin-Yolo-NCNN/blob/master/docs/release-notes/0.1.0.md)
- [엔지니어링 노트 (구 감사 스타일 README 전문)](https://github.com/SuperMonster003/AutoJs6-Plugin-Yolo-NCNN/blob/master/docs/engineering-notes.md)
- [프로젝트 로드맵 (지난 마일스톤과 향후 계획)](https://github.com/SuperMonster003/AutoJs6-Plugin-Yolo-NCNN/blob/master/ROADMAP.md)

******

### 리소스 구조

******

```text
.readme/common.json
.readme/lang_*.json
.readme/template_readme.md
.changelog/lang_*.json
.changelog/template_changelog.md
.python/generate_markdown.py
```

README와 CHANGELOG는 `.python/generate_markdown.py`가 위의 JSON 소스에서 오프라인으로 생성합니다 (표준 라이브러리만, 네트워크 0회). 문서를 바꾸려면 생성된 Markdown이 아니라 JSON 소스를 편집한 뒤 아래 명령으로 재생성하세요. `--check`는 산출물이 소스와 일치하는지 검증합니다:

```powershell
python .\.python\generate_markdown.py
python .\.python\generate_markdown.py --check
```

******

### 관련 링크

******

- AutoJs6 프로젝트 홈: https://github.com/SuperMonster003/AutoJs6
- Tencent NCNN: https://github.com/Tencent/ncnn
- Ultralytics NCNN 내보내기 가이드: https://docs.ultralytics.com/integrations/ncnn/
- 서드파티 고지: https://github.com/SuperMonster003/AutoJs6-Plugin-Yolo-NCNN/blob/master/THIRD_PARTY_NOTICES.md
- 라이선스: https://github.com/SuperMonster003/AutoJs6-Plugin-Yolo-NCNN/blob/master/LICENSE


[16 KB page alignment and build verification](https://github.com/SuperMonster003/AutoJs6-Plugin-Yolo-NCNN/blob/master/docs/16kb.md)
