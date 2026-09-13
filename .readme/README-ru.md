<!--suppress HtmlDeprecatedAttribute, HttpUrlsUsage -->

<div align="center">
  <p>
    <img src="https://github.com/SuperMonster003/AutoJs6-Plugin-Yolo-NCNN/blob/master/app/src/main/res/mipmap/ic_launcher.png?raw=true" alt="autojs6-plugin-yolo-ncnn-ic-launcher" border="0" width="128" />
  </p>

  <h1>AutoJs6 Plugin: YOLO NCNN</h1>

  <p>Полностью офлайновая YOLO-детекция объектов для AutoJs6 с изоляцией процесса (бэкенд NCNN 20260526)</p>

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

### Языки

******

Текущий README.md доступен на следующих языках:

- [简体中文 [zh-Hans]](https://github.com/SuperMonster003/AutoJs6-Plugin-Yolo-NCNN/blob/master/.readme/README-zh-Hans.md)
- [繁體中文 (香港) [zh-Hant-HK]](https://github.com/SuperMonster003/AutoJs6-Plugin-Yolo-NCNN/blob/master/.readme/README-zh-Hant-HK.md)
- [繁體中文 (台灣) [zh-Hant-TW]](https://github.com/SuperMonster003/AutoJs6-Plugin-Yolo-NCNN/blob/master/.readme/README-zh-Hant-TW.md)
- [English [en]](https://github.com/SuperMonster003/AutoJs6-Plugin-Yolo-NCNN/blob/master/.readme/README-en.md)
- [Français [fr]](https://github.com/SuperMonster003/AutoJs6-Plugin-Yolo-NCNN/blob/master/.readme/README-fr.md)
- [Español [es]](https://github.com/SuperMonster003/AutoJs6-Plugin-Yolo-NCNN/blob/master/.readme/README-es.md)
- [日本語 [ja]](https://github.com/SuperMonster003/AutoJs6-Plugin-Yolo-NCNN/blob/master/.readme/README-ja.md)
- [한국어 [ko]](https://github.com/SuperMonster003/AutoJs6-Plugin-Yolo-NCNN/blob/master/.readme/README-ko.md)
- Русский [ru] # текущий
- [العربية [ar]](https://github.com/SuperMonster003/AutoJs6-Plugin-Yolo-NCNN/blob/master/.readme/README-ar.md)

******

### Введение

******

Плагин AutoJs6 YOLO NCNN позволяет скриптам AutoJs6 выполнять YOLO-детекцию объектов целиком на устройстве: передайте изображение и получите массив обнаружений с метками, уверенностями и ограничивающими рамками в пиксельных координатах. Инференс выполняет Tencent NCNN 20260526 в отдельном процессе `:provider`, без доступа к сети и без передачи данных; файлы модели предоставляет пользователь, а APK плагина не содержит ни одной модели.

```text
application ID: io.github.supermonster003.autojs6.plugin.yolo.ncnn
plugin / engine / variant: yolo-ncnn / yolo / ncnn
provider ID: autojs6-yolo-ncnn
discovery actions: org.autojs.plugin.INFO / org.autojs.plugin.YOLO
runtime process: :provider
protocol version: 1.0
backend / task / decoder: ncnn / detect / ultralytics-detect
supported ABI: arm64-v8a
minimum host build: 5275 (AutoJs6 6.8.0+)
```

Приведённые выше сведения — это фиксированная идентичность, по которой хост обнаруживает и привязывает плагин. Модели передаются хостом через файловые дескрипторы только для чтения и всегда остаются внешними ресурсами.

******

### Возможности

******

- Офлайновая детекция объектов YOLO11: передайте объект изображения модуля `images` AutoJs6 и получите метки + уверенности + рамки; всё вычисляется на устройстве.
- Изоляция процесса: инференс выполняется в отдельном процессе `:provider`, поэтому сбой нативного слоя никогда не роняет основной процесс AutoJs6; сервисы защищены разрешением `org.autojs.permission.PLUGIN` и проверками подписи.
- CPU-бэкенд NCNN 20260526: настраиваемое число потоков (по умолчанию 4, до 64), для устройств `arm64-v8a`.
- Совместимость моделей через манифест: `model.json` объявляет входы, выходы и метки, поддерживая от 1 до 256 пользовательских классов; официальные YOLO11 и самообученные модели работают одинаково.
- Проверка безопасности модели: при открытии сессии проверяются заявленная длина и SHA-256 всех трёх файлов, а во время выполнения — фактическая форма выхода графа NCNN; несоответствия отклоняются, а не угадываются.
- Стабильные категории ошибок: отсутствующий компонент, недоступный провайдер, отклонённая модель, неподдерживаемая возможность и похожие случаи возвращают различимые коды ошибок, которые скрипт может обрабатывать точечно.
- Тайм-ауты запросов и очистка через `detector.close()`; нативная работа использует кооперативную отмену и может завершить текущий вызов перед освобождением ресурсов.
- README и CHANGELOG доступны на десяти языках: упрощённый китайский, традиционный китайский (Гонконг/Тайвань), английский, французский, испанский, японский, корейский, русский и арабский.

******

### Быстрый старт

******

- **Установка** — Плагин сейчас находится в приватной подготовительной фазе (см. раздел Статус проекта ниже): публичная загрузка и запись в официальном индексе плагинов появятся только после официального выпуска совместимого хоста AutoJs6 6.8.0 (сборка 5275). До этого можно самостоятельно собрать кандидат TEST-SIGNED по разделу Сборка и установить его в паре с тестовым APK AutoJs6 с тем же отладочным сертификатом; хост и плагин должны быть подписаны одним сертификатом.
- **Включение** — Установка плагина сама по себе не включает YOLO: хост AutoJs6 сохраняет явные переключатели выбора, доверия и включения (маршрут YOLO по умолчанию выключен), поэтому включите и доверьте этот провайдер в хосте. Со стороны скрипта `yolo.load` также требует явную строку компонента в `options.component`; неявного отката нет.
- **Запуск** — Подготовьте каталог модели с тремя файлами `model.json`, `model.ncnn.param` и `model.ncnn.bin` (см. раздел Подготовка модели ниже), откройте детектор через `yolo.load(modelDir, options)`, получите массив обнаружений через `detector.detect(image, options)` и освободите его вызовом `detector.close()`.
- **Диагностика** — Исключения из `yolo.load` и `detector.detect` несут стабильные категории ошибок: `COMPONENT_REQUIRED` (компонент не указан), `PROVIDER_UNAVAILABLE` (хост не находит провайдер или не доверяет ему), `MODEL_REJECTED` (модель или манифест не прошли проверку; детали содержат префиксы вида `MANIFEST_*`), `UNSUPPORTED_CAPABILITY` (запрошена возможность вне CPU/arm64/detect), `SESSION_CLOSED`, `DETECT_FAILED` и другие. При отладке сверяйтесь с разделом Ограничения ниже и со [спецификацией манифеста модели](https://github.com/SuperMonster003/AutoJs6-Plugin-Yolo-NCNN/blob/master/docs/model-manifest-v1.md).

******

### Пример использования

******

Минимальный готовый к запуску пример (см. также `sample/yolo/detect.js` в репозитории хоста):

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

Каждое обнаружение содержит `classId`, `label`, `confidence` и `bounds` (Android `RectF` с полями `left` / `top` / `right` / `bottom` и методами `centerX()` / `centerY()`); координаты — в пикселях входного изображения. Детектор — последовательная сессия на один запрос: длина очереди инференса равна 0, поэтому второй одновременный `detect` на том же детекторе сразу завершится ошибкой, а не встанет в очередь.

******

### Подготовка модели

******

Каталог модели всегда содержит ровно три файла, и имена файлов задают их роли:

```text
models/yolo11n/
|-- model.json
|-- model.ncnn.param
`-- model.ncnn.bin
```

Официальные или самообученные модели Ultralytics YOLO11 detect экспортируются командой `yolo export format=ncnn imgsz=640`, которая создаёт `model.ncnn.param` и `model.ncnn.bin` (см. [руководство Ultralytics по экспорту в NCNN](https://docs.ultralytics.com/integrations/ncnn/)). `model.json` — это документ Model Manifest v1: он объявляет вход (`in0`, RGB NCHW, letterbox 640x640), выход (`out0`, декодер `ultralytics-detect`, форма `[1, 4 + N, 8400]`, где N — число классов) и список меток; полный пример — [fixtures/yolo11n/model-manifest-v1.json](https://github.com/SuperMonster003/AutoJs6-Plugin-Yolo-NCNN/blob/master/fixtures/yolo11n/model-manifest-v1.json).

Запустите `python tools/generate_yolo_ncnn_manifest.py <каталог-экспорта>`, чтобы создать `model.json` непосредственно из Ultralytics `metadata.yaml`. Офлайновый инструмент только на стандартной библиотеке проверяет фиксированный профиль YOLO11/detect/640/batch/метки и структуру графа NCNN `in0`/`out0` до записи; `--check` отклоняет расхождения без изменения файлов. Точная команда экспорта, граница проверки и диагностика описаны в [руководстве по преобразованию модели](https://github.com/SuperMonster003/AutoJs6-Plugin-Yolo-NCNN/blob/master/docs/model-conversion.md).

Манифест — это контракт совместимости, а не инструмент перемаркировки: при открытии сессии проверяются заявленные длины и SHA-256 трёх файлов, а во время выполнения — фактическая форма выхода графа NCNN; несоответствия отклоняются с `MODEL_REJECTED` (префиксы деталей вроде `MANIFEST_SHAPE_INVALID`, `MODEL_GRAPH_REJECTED`). Модели сохраняют лицензию и условия использования своего источника; конвертация в NCNN их не меняет, и плагин не даёт никаких прав на распространение. См. [спецификацию манифеста](https://github.com/SuperMonster003/AutoJs6-Plugin-Yolo-NCNN/blob/master/docs/model-manifest-v1.md) и [политику лицензирования моделей](https://github.com/SuperMonster003/AutoJs6-Plugin-Yolo-NCNN/blob/master/docs/model-license-policy.md).

******

### API скриптов

******

`yolo.load(modelDir, options)` открывает сессию детекции и возвращает `YoloDetector`. `options.component` обязателен (строка компонента вида пакет/класс; для этого плагина: `io.github.supermonster003.autojs6.plugin.yolo.ncnn/.provider.YoloProviderService`). Необязательные: `device` (сейчас принимается только `"cpu"`), `threads` (по умолчанию 4, до 64), `decoderId` (по умолчанию `ultralytics-detect`) и `timeoutMillis` (общий тайм-аут открытия модели, по умолчанию 120000 мс, до 600000 мс).

`detector.detect(image, options)` синхронно выполняет детекцию на одном изображении и возвращает массив обнаружений. `image` — объект изображения модуля `images` AutoJs6 (из `images.read`, скриншота и т. п.). Необязательные: `confidence` (порог уверенности, по умолчанию 0.25), `iouThreshold` (порог IoU для NMS, по умолчанию 0.45), `maxDetections` (по умолчанию 100, до 400) и `timeoutMillis` (по умолчанию 30000 мс).

`detector.close()` освобождает сессию и нативные ресурсы; повторные вызовы безопасны. AutoJs6 также закрывает детекторы по завершении скрипта, но рекомендуется явное освобождение через `try...finally`. Вызов `detect` после закрытия возвращает `SESSION_CLOSED`.

******

### Ограничения

******

Ради предсказуемого поведения запросы вне следующего периметра отклоняются явно, без тихого отката:

- Только CPU-инференс: Vulkan/GPU не поддерживается, `options.device` принимает только `"cpu"`.
- Только `arm64-v8a`: устройства с другими ABI не могут загрузить нативную библиотеку плагина.
- Только детекция объектов (detect): сегментация, поза, OBB, классификация и трекинг не поддерживаются.
- Зарегистрирован только декодер `ultralytics-detect`: неизвестный `decoderId` отклоняется, а не заменяется другим декодером.
- Вход предобрабатывается как letterbox 640x640 (фиксированный профиль manifest v1), пиксели RGBA_8888.
- Последовательный инференс с одним запросом на сессию: лимит очереди — 0, второй одновременный `detect` в той же сессии завершается ошибкой.
- Открытие модели принимает только дескрипторы обычных файлов в режиме чтения (ни каналов, ни сокетов); все три файла должны быть читаемыми.
- Установка плагина сама по себе не включает YOLO: включение, доверие и выбор всегда принадлежат хосту AutoJs6.

******

### Безопасность и изоляция

******

Плагин спроектирован по принципу fail-closed; следующие механизмы действуют всегда:

- Инференс выполняется в отдельном процессе `:provider`, изолированном от основного процесса AutoJs6; сервисы защищены разрешением `org.autojs.permission.PLUGIN` и проверками подписи.
- Модели поступают от хоста как `ParcelFileDescriptor` только для чтения; плагин сам не читает хранилище и не выполняет сетевых запросов.
- Перед открытием сессии проверяются заявленные длины, EOF и SHA-256; всё открытие делит один монотонный дедлайн, и просроченная сессия никогда не публикуется.
- Если рантайм NCNN не удаётся загрузить или инициализировать, плагин отказывает полностью, а не деградирует.
- Некорректный ввод изолируется от активных запросов; когда идентичность запроса удаётся восстановить, публикуется точное терминальное состояние ошибки вместо зависания.
- Смерть обратного вызова и устаревшие сессии обнаруживаются и вычищаются; нативные ресурсы освобождаются отложенно, а закрытие идемпотентно.

******

### Совместимость

******

Требуется AutoJs6 с кодом версии не ниже 5275 (то есть 6.8.0 или новее), подписанный тем же сертификатом, что и плагин; Android 24+ (Android 7.0), targetSdk 36; устройство должно быть `arm64-v8a`. Версия протокола плагина 1.0; текущая версия провайдера 0.1.2 (код версии 2).

******

### Статус проекта

******

Этот репозиторий сейчас — приватный подготовительный архив: совместимый хост AutoJs6 6.8.0 (5275) ещё не выпущен официально, а плагин не опубликован и не внесён в официальный индекс плагинов; значки GitHub выше могут не отображаться, пока репозиторий не станет публичным. Без `sign.properties` `assembleRelease` создаёт неподписанный APK — это лишь свидетельство исходников/сборки, а не публикуемый артефакт. Первый выпуск — 0.1.2 (код версии 2, без предшественника с кодом 1); дефекты исправляются вперёд кодом версии 3, а не откатом. Продакшен-подпись, финальная проверка на устройстве и статус публикации фиксируются внешним архивом свидетельств R6; см. [инженерные заметки](https://github.com/SuperMonster003/AutoJs6-Plugin-Yolo-NCNN/blob/master/docs/engineering-notes.md).

******

### Сборка

******

Рекомендуется JDK 21+; Android SDK должен предоставлять platforms 24 и 36, а также NDK 29.0.14206865 и CMake 3.22.1 (нужны для JNI NCNN). Основные команды:

```powershell
.\gradlew.bat :app:assembleDebug
.\gradlew.bat :app:assembleRc
.\gradlew.bat :app:assembleRelease
```

`assembleRc` создаёт устанавливаемый arm64-only кандидат TEST-SIGNED: он наследует R8 и сжатие ресурсов от release, использует стандартную отладочную подпись, а имя версии оканчивается на `-rc-test-signed`; он предназначен для пары с тестовым APK AutoJs6 того же сертификата при проверке на устройстве. `assembleRelease` — сборка выпуска; без материалов подписи она остаётся неподписанной.

Гейт мейнтейнера `tools/verify-r6-provider-source.ps1` сначала запускает `--check` для всех 22 сгенерированных артефактов README/CHANGELOG на 10 языках и немедленно завершается с ошибкой при расхождении. Затем он по умолчанию стартует с чистых исходников: после `:app:clean` запускает целевые тесты и обе сборки APK, записывает XML тестов и хеши артефактов и сверяет APK со списком из пяти разрешённых файлов assets. Локальная проверка и генерация документации по умолчанию выполняются офлайн (ноль сетевых вызовов), чтобы избежать шума Cloudflare 502/524/529 в сети разработки.

Перед любой сборкой Gradle тот же гейт запускает восемь тестов `tools/generate_yolo_ncnn_manifest.py`, использующих только стандартную библиотеку, и записывает хеши исходников и результат. Регрессия инструмента моделей блокирует офлайновую предпроверку выпуска.

******

### История выпусков

******

# v0.1.2

###### 2026/09/13

* `Исправление` Даты версий используют единый английский формат
* `Улучшение` Проверка версий, подписей и полного набора вариантов APK перед подготовкой файлов для загрузки

# v0.1.1

###### 2026/09/13

* `Улучшение` Проверка выравнивания страниц 16 KB для 64-битных нативных библиотек при сборке, включая контракт manifest и отчеты JSON

# v0.1.0

###### 2026/08/13

* `Примечание` Первый выпуск (код версии 2, без предшественника с кодом 1); требуется AutoJs6 с кодом версии не ниже 5275 (6.8.0+), подписанный тем же сертификатом, что и плагин
* `Примечание` Сейчас приватная подготовительная фаза: публичный выпуск и подача в официальный индекс плагинов последуют за официальным выходом совместимого хоста; периметр возможностей — CPU / arm64-v8a / детекция объектов
* `Новое` Офлайновый `tools/generate_yolo_ncnn_manifest.py` преобразует метаданные Ultralytics YOLO11 NCNN в `model.json`, проверяет фиксированный профиль метаданных/меток/графа и выводит хеши артефактов
* `Новое` Готов провайдер YOLO-детекции с изоляцией процесса: отдельный процесс `:provider` обслуживает инференс `org.autojs.plugin.YOLO` и обнаружение `org.autojs.plugin.INFO`, оба защищены разрешением `org.autojs.permission.PLUGIN`
* `Новое` Встроен CPU-бэкенд NCNN 20260526 с декодером `ultralytics-detect`, поддерживающий модели YOLO11 detect и объявленное манифестом число пользовательских классов (от 1 до 256)
* `Новое` Действует контракт Model Manifest v1: при открытии сессии проверяются заявленные длины и SHA-256, во время выполнения — форма выхода; несоответствия отклоняются со стабильными кодами ошибок
* `Новое` Модели поступают от хоста через дескрипторы только для чтения, всё открытие делит один монотонный дедлайн; APK плагина не содержит моделей и не делает сетевых вызовов
* `Новое` Защита жизненного цикла сессий: последовательный инференс с одним запросом на сессию (нулевая очередь), обнаружение смерти обратного вызова, идемпотентное закрытие и отложенное освобождение нативных ресурсов
* `Исправление` Устаревшие сессии моделей вычищаются при запуске, что исключает остаточное использование нативных ресурсов после аварийного выхода хоста
* `Улучшение` Сборки release и TEST-SIGNED RC проходят через R8 и сжатие ресурсов, упаковка выровнена по ELF 16 KiB
* `Улучшение` APK полностью включает лицензии MPL-2.0, Apache-2.0 для Kotlin и NCNN с замками происхождения, плюс индекс сторонних уведомлений
* `Улучшение` Новый офлайновый гейт исходников/сборки/упаковки `tools/verify-r6-provider-source.ps1`: отклонение расхождений в 22 сгенерированных артефактах README/CHANGELOG, старт с чистых исходников, запись хешей тестов и артефактов, сверка APK со списком из пяти разрешённых файлов assets
* `Зависимости` Зафиксированы NCNN 20260526 (BSD-3-Clause, с замками происхождения и хешей), Kotlin 2.2.21 и AAR протокола YOLO 1.0 (переданы из замороженной ревизии исходников AutoJs6)

##### Подробная история выпусков

* [CHANGELOG-ru.md](https://github.com/SuperMonster003/AutoJs6-Plugin-Yolo-NCNN/blob/master/app/src/main/assets/doc/CHANGELOG-ru.md)

******

### Лицензия

******

Исходный код плагина распространяется под [MPL-2.0](https://github.com/SuperMonster003/AutoJs6-Plugin-Yolo-NCNN/blob/master/LICENSE). APK содержит полную лицензию и уведомления NCNN (BSD-3-Clause плюс сторонние уведомления из апстрима), полный текст Apache-2.0 для рантайма Kotlin и индекс сторонних уведомлений; см. [THIRD_PARTY_NOTICES](https://github.com/SuperMonster003/AutoJs6-Plugin-Yolo-NCNN/blob/master/THIRD_PARTY_NOTICES.md). Модели — внешние ресурсы: они сохраняют лицензию и условия своего источника, а плагин не содержит моделей и не даёт прав на их распространение.

******

### Дополнительные материалы

******

- [Спецификация манифеста модели (Model Manifest v1)](https://github.com/SuperMonster003/AutoJs6-Plugin-Yolo-NCNN/blob/master/docs/model-manifest-v1.md)
- [JSON Schema манифеста модели](https://github.com/SuperMonster003/AutoJs6-Plugin-Yolo-NCNN/blob/master/schemas/model-manifest-v1.schema.json)
- [Политика лицензирования моделей и проверочных ресурсов](https://github.com/SuperMonster003/AutoJs6-Plugin-Yolo-NCNN/blob/master/docs/model-license-policy.md)
- [Заметки к выпуску 0.1.0 (инженерная формулировка)](https://github.com/SuperMonster003/AutoJs6-Plugin-Yolo-NCNN/blob/master/docs/release-notes/0.1.0.md)
- [Инженерные заметки (прежний аудиторский README целиком)](https://github.com/SuperMonster003/AutoJs6-Plugin-Yolo-NCNN/blob/master/docs/engineering-notes.md)
- [Дорожная карта проекта (прошедшие вехи и планы)](https://github.com/SuperMonster003/AutoJs6-Plugin-Yolo-NCNN/blob/master/ROADMAP.md)

******

### Структура ресурсов

******

```text
.readme/common.json
.readme/lang_*.json
.readme/template_readme.md
.changelog/lang_*.json
.changelog/template_changelog.md
.python/generate_markdown.py
```

README и CHANGELOG генерируются офлайн скриптом `.python/generate_markdown.py` из приведённых выше JSON-источников (только стандартная библиотека, ноль сети). Чтобы изменить документацию, правьте JSON-источники, а не сгенерированный Markdown, затем перегенерируйте командами ниже; `--check` проверяет соответствие выходов источникам:

```powershell
python .\.python\generate_markdown.py
python .\.python\generate_markdown.py --check
```

******

### Ссылки

******

- Домашняя страница проекта AutoJs6: https://github.com/SuperMonster003/AutoJs6
- Tencent NCNN: https://github.com/Tencent/ncnn
- Руководство Ultralytics по экспорту в NCNN: https://docs.ultralytics.com/integrations/ncnn/
- Уведомления о сторонних компонентах: https://github.com/SuperMonster003/AutoJs6-Plugin-Yolo-NCNN/blob/master/THIRD_PARTY_NOTICES.md
- Лицензия: https://github.com/SuperMonster003/AutoJs6-Plugin-Yolo-NCNN/blob/master/LICENSE


[16 KB page alignment and build verification](https://github.com/SuperMonster003/AutoJs6-Plugin-Yolo-NCNN/blob/master/docs/16kb.md)
