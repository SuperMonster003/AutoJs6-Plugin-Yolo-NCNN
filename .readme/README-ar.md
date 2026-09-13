<!--suppress HtmlDeprecatedAttribute, HttpUrlsUsage -->

<div align="center">
  <p>
    <img src="https://github.com/SuperMonster003/AutoJs6-Plugin-Yolo-NCNN/blob/master/app/src/main/res/mipmap/ic_launcher.png?raw=true" alt="autojs6-plugin-yolo-ncnn-ic-launcher" border="0" width="128" />
  </p>

  <h1>AutoJs6 Plugin: YOLO NCNN</h1>

  <p>كشف كائنات YOLO لتطبيق AutoJs6، معزول في عملية مستقلة ويعمل دون اتصال بالكامل (محرك NCNN 20260526)</p>

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

### اللغات (Languages)

******

يدعم README.md الحالي اللغات التالية:

- [简体中文 [zh-Hans]](https://github.com/SuperMonster003/AutoJs6-Plugin-Yolo-NCNN/blob/master/.readme/README-zh-Hans.md)
- [繁體中文 (香港) [zh-Hant-HK]](https://github.com/SuperMonster003/AutoJs6-Plugin-Yolo-NCNN/blob/master/.readme/README-zh-Hant-HK.md)
- [繁體中文 (台灣) [zh-Hant-TW]](https://github.com/SuperMonster003/AutoJs6-Plugin-Yolo-NCNN/blob/master/.readme/README-zh-Hant-TW.md)
- [English [en]](https://github.com/SuperMonster003/AutoJs6-Plugin-Yolo-NCNN/blob/master/.readme/README-en.md)
- [Français [fr]](https://github.com/SuperMonster003/AutoJs6-Plugin-Yolo-NCNN/blob/master/.readme/README-fr.md)
- [Español [es]](https://github.com/SuperMonster003/AutoJs6-Plugin-Yolo-NCNN/blob/master/.readme/README-es.md)
- [日本語 [ja]](https://github.com/SuperMonster003/AutoJs6-Plugin-Yolo-NCNN/blob/master/.readme/README-ja.md)
- [한국어 [ko]](https://github.com/SuperMonster003/AutoJs6-Plugin-Yolo-NCNN/blob/master/.readme/README-ko.md)
- [Русский [ru]](https://github.com/SuperMonster003/AutoJs6-Plugin-Yolo-NCNN/blob/master/.readme/README-ru.md)
- العربية [ar] # الحالي

******

### مقدمة

******

تتيح إضافة AutoJs6 YOLO NCNN لسكربتات AutoJs6 تنفيذ كشف كائنات YOLO بالكامل على الجهاز: مرّر صورة لتحصل على مصفوفة اكتشافات تحمل التسميات ودرجات الثقة وصناديق الإحاطة بإحداثيات البكسل. يُنفّذ الاستدلال بواسطة Tencent NCNN 20260526 في العملية المنفصلة `:provider`، دون أي وصول إلى الشبكة أو رفع بيانات؛ ملفات النموذج يوفرها المستخدم، ولا يتضمن ملف APK للإضافة أي نموذج.

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

المعلومات أعلاه هي الهوية الثابتة التي يعتمدها المضيف لاكتشاف هذه الإضافة والارتباط بها. تُسلَّم النماذج من المضيف عبر واصفات ملفات للقراءة فقط وتبقى دائمًا موارد خارجية.

******

### الميزات

******

- كشف كائنات YOLO11 دون اتصال: مرّر كائن صورة من وحدة `images` في AutoJs6 لتحصل على التسميات + درجات الثقة + صناديق الإحاطة، وكل الحساب يتم على الجهاز.
- عزل العمليات: يعمل الاستدلال في العملية المنفصلة `:provider`، فلا يُسقط أي عطل في الطبقة الأصلية عملية AutoJs6 الرئيسية؛ والخدمات محمية بإذن `org.autojs.permission.PLUGIN` وفحوص التوقيع.
- محرك استدلال NCNN 20260526 على المعالج: عدد خيوط قابل للضبط (افتراضيًا 4، وحتى 64)، لأجهزة `arm64-v8a`.
- توافق نماذج يقوده الـ manifest: يعلن `model.json` المدخلات والمخرجات والتسميات، ويدعم من 1 إلى 256 فئة مخصصة؛ نماذج YOLO11 الرسمية والنماذج المدرَّبة ذاتيًا تعمل بالطريقة نفسها.
- تحقق أمني من النموذج: عند فتح الجلسة يُتحقق من الطول المعلن و SHA-256 للملفات الثلاثة، ويُتحقق أثناء التشغيل من الشكل الفعلي لمخرج مخطط NCNN؛ أي تعارض يُرفض ولا يُخمَّن أبدًا.
- فئات أخطاء مستقرة: المكوّن المفقود، وعدم توفر المزوّد، ورفض النموذج، والقدرة غير المدعومة وأمثالها تعيد رموز أخطاء قابلة للتمييز يمكن للسكربت معالجتها بدقة.
- مهل للطلبات وتنظيف عبر `detector.close()`; يستخدم العمل الأصلي إلغاء تعاونيا وقد يكمل الاستدعاء الحالي قبل تحرير الموارد.
- يتوفر README و CHANGELOG بعشر لغات: الصينية المبسطة والصينية التقليدية (هونغ كونغ/تايوان) والإنجليزية والفرنسية والإسبانية واليابانية والكورية والروسية والعربية.

******

### البدء السريع

******

- **التثبيت** — هذه الإضافة حاليًا في مرحلة تجهيز خاصة (انظر قسم حالة المشروع أدناه): لن يتاح التنزيل العام والإدراج في فهرس الإضافات الرسمي إلا بعد الإصدار الرسمي للمضيف المتوافق AutoJs6 6.8.0 (البنية 5275). حتى ذلك الحين يمكنك بناء مرشح TEST-SIGNED بنفسك كما في قسم البناء أدناه، وإقرانه بملف APK تجريبي من AutoJs6 يستخدم شهادة التصحيح نفسها؛ يجب توقيع المضيف والإضافة بالشهادة نفسها.
- **التفعيل** — تثبيت الإضافة لا يفعّل YOLO من تلقاء نفسه: يحتفظ مضيف AutoJs6 بمفاتيح صريحة للاختيار والثقة والتفعيل (مسار YOLO معطّل افتراضيًا)، لذا فعِّل هذا المزوّد وامنحه الثقة داخل المضيف. ومن جهة السكربت، يشترط `yolo.load` أيضًا سلسلة المكوّن الصريحة في `options.component`؛ ولا يوجد أي تراجع ضمني.
- **التشغيل** — جهّز مجلد نموذج يحوي الملفات الثلاثة `model.json` و `model.ncnn.param` و `model.ncnn.bin` (انظر قسم تجهيز النموذج أدناه)، ثم افتح كاشفًا عبر `yolo.load(modelDir, options)`، واحصل على مصفوفة الاكتشافات عبر `detector.detect(image, options)`، وحرره عند الانتهاء عبر `detector.close()`.
- **استكشاف الأخطاء** — تحمل الاستثناءات الصادرة عن `yolo.load` و `detector.detect` فئات أخطاء مستقرة: `COMPONENT_REQUIRED` (لم يُحدد مكوّن)، `PROVIDER_UNAVAILABLE` (المضيف لا يجد المزوّد أو لا يثق به)، `MODEL_REJECTED` (النموذج أو الـ manifest لم يجتز التحقق؛ التفاصيل تحمل بادئات مثل `MANIFEST_*`)، `UNSUPPORTED_CAPABILITY` (طُلبت قدرة خارج CPU/arm64/detect)، `SESSION_CLOSED`، `DETECT_FAILED` وغيرها. راجع قسم الحدود أدناه و[مواصفة manifest النموذج](https://github.com/SuperMonster003/AutoJs6-Plugin-Yolo-NCNN/blob/master/docs/model-manifest-v1.md) عند التشخيص.

******

### مثال الاستخدام

******

مثال أدنى جاهز للتشغيل (انظر أيضًا `sample/yolo/detect.js` في مستودع المضيف):

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

يحمل كل اكتشاف `classId` و `label` و `confidence` و `bounds` (كائن `RectF` من Android بحقول `left` / `top` / `right` / `bottom` والدالتين `centerX()` / `centerY()`)؛ والإحداثيات ببكسلات الصورة المدخلة. الكاشف جلسة تسلسلية بطلب واحد: طول طابور الاستدلال 0، لذا يفشل `detect` ثانٍ متزامن على الكاشف نفسه فورًا بدل الانتظار.

******

### تجهيز النموذج

******

يحوي مجلد النموذج دائمًا ثلاثة ملفات بالضبط، وأسماء الملفات تحدد الأدوار:

```text
models/yolo11n/
|-- model.json
|-- model.ncnn.param
`-- model.ncnn.bin
```

يمكن تصدير نماذج Ultralytics YOLO11 detect الرسمية أو المدرَّبة ذاتيًا بالأمر `yolo export format=ncnn imgsz=640`، الذي ينتج `model.ncnn.param` و `model.ncnn.bin` (انظر [دليل تصدير NCNN من Ultralytics](https://docs.ultralytics.com/integrations/ncnn/)). أما `model.json` فهو مستند Model Manifest v1: يعلن المدخل (`in0`، RGB NCHW، letterbox بمقاس 640x640)، والمخرج (`out0`، مفكك الترميز `ultralytics-detect`، الشكل `[1, 4 + N, 8400]` حيث N عدد الفئات)، وقائمة التسميات؛ ومثال كامل في [fixtures/yolo11n/model-manifest-v1.json](https://github.com/SuperMonster003/AutoJs6-Plugin-Yolo-NCNN/blob/master/fixtures/yolo11n/model-manifest-v1.json).

شغّل `python tools/generate_yolo_ncnn_manifest.py <export-directory>` لإنشاء `model.json` مباشرة من `metadata.yaml` الخاص بـ Ultralytics. تتحقق الأداة غير المتصلة والمعتمدة على المكتبة القياسية فقط من ملف YOLO11/detect/640/batch/labels الثابت ومن بنية رسم NCNN ذات `in0` و `out0` قبل الكتابة, ويمنع `--check` الانحراف دون كتابة. يوضح [دليل تحويل النموذج](https://github.com/SuperMonster003/AutoJs6-Plugin-Yolo-NCNN/blob/master/docs/model-conversion.md) أمر التصدير وحدود التحقق واستكشاف الأخطاء.

الـ manifest عقد توافق لا أداة لإعادة التسمية: عند فتح الجلسة تُتحقق الأطوال المعلنة و SHA-256 للملفات الثلاثة، ويُتحقق أثناء التشغيل من الشكل الفعلي لمخرج مخطط NCNN؛ ويُرفض أي تعارض بالخطأ `MODEL_REJECTED` (بادئات التفاصيل مثل `MANIFEST_SHAPE_INVALID` و `MODEL_GRAPH_REJECTED`). تحتفظ النماذج برخصة مصدرها وشروط استخدامه؛ والتحويل إلى NCNN لا يغيّرها، ولا تمنح الإضافة أي حقوق لإعادة التوزيع. انظر [مواصفة manifest النموذج](https://github.com/SuperMonster003/AutoJs6-Plugin-Yolo-NCNN/blob/master/docs/model-manifest-v1.md) و[سياسة تراخيص النماذج](https://github.com/SuperMonster003/AutoJs6-Plugin-Yolo-NCNN/blob/master/docs/model-license-policy.md).

******

### واجهة برمجة السكربت

******

يفتح `yolo.load(modelDir, options)` جلسة كشف ويعيد `YoloDetector`. الحقل `options.component` إلزامي (سلسلة مكوّن بصيغة حزمة/صنف؛ ولهذه الإضافة: `io.github.supermonster003.autojs6.plugin.yolo.ncnn/.provider.YoloProviderService`). الاختيارية: `device` (يُقبل حاليًا `"cpu"` فقط)، `threads` (افتراضيًا 4، وحتى 64)، `decoderId` (افتراضيًا `ultralytics-detect`)، و `timeoutMillis` (المهلة الكلية لفتح النموذج، افتراضيًا 120000 مللي ثانية، وحتى 600000 مللي ثانية).

ينفذ `detector.detect(image, options)` الكشف تزامنيًا على صورة واحدة ويعيد مصفوفة الاكتشافات. `image` كائن صورة من وحدة `images` في AutoJs6 (من `images.read` أو لقطة شاشة ونحوهما). الاختيارية: `confidence` (عتبة الثقة، افتراضيًا 0.25)، `iouThreshold` (عتبة IoU لخوارزمية NMS، افتراضيًا 0.45)، `maxDetections` (افتراضيًا 100، وحتى 400)، و `timeoutMillis` (افتراضيًا 30000 مللي ثانية).

يحرر `detector.close()` الجلسة والموارد الأصلية ويمكن استدعاؤه مرارًا بأمان؛ ويغلق AutoJs6 الكواشف أيضًا عند انتهاء السكربت، لكن يُستحسن التحرير الصريح عبر `try...finally`. استدعاء `detect` بعد الإغلاق يعيد `SESSION_CLOSED`.

******

### الحدود

******

حفاظًا على سلوك متوقع، تُرفض الطلبات الخارجة عن النطاق التالي رفضًا صريحًا دون أي تراجع صامت:

- استدلال على المعالج فقط: Vulkan/GPU غير مدعوم، و `options.device` لا يقبل إلا `"cpu"`.
- `arm64-v8a` فقط: الأجهزة ذات ABI أخرى لا يمكنها تحميل المكتبة الأصلية للإضافة.
- كشف الكائنات (detect) فقط: التقسيم والوضعيات و OBB والتصنيف والتتبع كلها غير مدعومة.
- مفكك الترميز المسجل هو `ultralytics-detect` وحده: أي `decoderId` مجهول يُرفض بدل التحول إلى مفكك آخر.
- تُعالَج المدخلات مسبقًا بأسلوب letterbox بمقاس 640x640 (الملف الشخصي الثابت في manifest v1) وبصيغة بكسل RGBA_8888.
- استدلال تسلسلي بطلب واحد لكل جلسة: حد الطابور 0، فيفشل `detect` ثانٍ متزامن في الجلسة نفسها.
- فتح النموذج لا يقبل إلا واصفات قراءة فقط لملفات عادية (لا أنابيب ولا مقابس)؛ ويجب أن تكون الملفات الثلاثة قابلة للقراءة.
- تثبيت هذه الإضافة لا يفعّل YOLO من تلقاء نفسه: التفعيل والثقة والاختيار حالات يملكها مضيف AutoJs6 دائمًا.

******

### الأمان والعزل

******

صُممت الإضافة على مبدأ fail-closed؛ والآليات التالية سارية دائمًا:

- يعمل الاستدلال في العملية المنفصلة `:provider` معزولًا عن عملية AutoJs6 الرئيسية؛ والخدمات محمية بإذن `org.autojs.permission.PLUGIN` وفحوص التوقيع.
- تصل النماذج من المضيف ككائنات `ParcelFileDescriptor` للقراءة فقط؛ ولا تقرأ الإضافة أي تخزين بنفسها ولا ترسل أي طلبات شبكية.
- قبل فتح الجلسة تُتحقق الأطوال المعلنة و EOF و SHA-256؛ ويتشارك الفتح كله مهلة رتيبة واحدة، ولا تُنشر أبدًا جلسة انتهت مهلتها.
- إذا تعذر تحميل بيئة NCNN أو تهيئتها فشلت الإضافة فشلًا مغلقًا بدل التدهور التدريجي.
- تُعزل المدخلات المشوهة عن الطلبات النشطة؛ وعندما يمكن استرداد هوية الطلب تُنشر حالة فشل نهائية دقيقة بدل التعليق.
- يُكتشف موت طرف الاستدعاء والجلسات المتقادمة وتُنظف؛ وتُحرر الموارد الأصلية تحريرًا مؤجلًا، وعمليات الإغلاق عديمة الأثر عند التكرار.

******

### التوافق

******

يتطلب AutoJs6 برمز إصدار لا يقل عن 5275 (أي 6.8.0 أو أحدث) موقّعًا بشهادة الإضافة نفسها؛ و Android 24+ (Android 7.0)، و targetSdk 36؛ ويجب أن يكون الجهاز `arm64-v8a`. إصدار بروتوكول الإضافة 1.0؛ وإصدار المزوّد الحالي 0.1.1 (رمز الإصدار 2).

******

### حالة المشروع

******

هذا المستودع حاليًا أرشيف تجهيز خاص: المضيف المتوافق AutoJs6 6.8.0 (5275) لم يصدر رسميًا بعد، وهذه الإضافة غير منشورة علنًا وغير مدرجة في فهرس الإضافات الرسمي؛ وقد لا تظهر شارات GitHub أعلاه قبل أن يصبح المستودع عامًا. بدون `sign.properties` ينتج `assembleRelease` ملف APK غير موقّع يُعد دليلًا على المصدر/البناء فحسب لا ناتجًا قابلًا للنشر. الإصدار الأول هو 0.1.1 (رمز الإصدار 2، دون سلف برمز 1)؛ وتُصحح العيوب إلى الأمام برمز الإصدار 3 لا بالتراجع. توقيع الإنتاج والتحقق النهائي على الجهاز وحالة النشر يحسمها أرشيف أدلة R6 الخارجي؛ انظر [ملاحظات الهندسة](https://github.com/SuperMonster003/AutoJs6-Plugin-Yolo-NCNN/blob/master/docs/engineering-notes.md).

******

### البناء

******

يوصى بـ JDK 21+؛ ويجب أن يوفر Android SDK المنصتين 24 و 36، إضافة إلى NDK 29.0.14206865 و CMake 3.22.1 (لازمة لترجمة JNI الخاص بـ NCNN). الأوامر الشائعة:

```powershell
.\gradlew.bat :app:assembleDebug
.\gradlew.bat :app:assembleRc
.\gradlew.bat :app:assembleRelease
```

ينتج `assembleRc` مرشحًا قابلًا للتثبيت لمعمارية arm64 فقط بوسم TEST-SIGNED: يرث R8 وتقليص الموارد من release، ويستخدم توقيع التصحيح القياسي، وينتهي اسم إصداره بـ `-rc-test-signed`؛ وهو مخصص للإقران بملف APK تجريبي من AutoJs6 بالشهادة نفسها للتحقق على الجهاز. أما `assembleRelease` فهو بناء الإصدار ويبقى غير موقّع عند غياب مواد التوقيع.

تشغّل بوابة الصيانة `tools/verify-r6-provider-source.ps1` أولًا الخيار `--check` على نواتج README/CHANGELOG المولدة وعددها 22 لعشر لغات, وتفشل فورًا عند وجود أي انحراف. ثم تبدأ افتراضيًا من مصادر نظيفة: بعد `:app:clean` تشغّل الاختبارات المركزة وتجميعي APK كليهما, وتسجل XML الاختبارات وبصمات النواتج, وتتحقق من APK وفق قائمة سماح من خمسة ملفات assets. يعمل التحقق المحلي وتوليد الوثائق كلاهما دون اتصال افتراضيًا (صفر نداءات شبكية) لتجنب ضجيج Cloudflare 502/524/529 في شبكة التطوير.

قبل أي بناء Gradle تشغّل البوابة نفسها أيضًا الاختبارات الثمانية المعتمدة على المكتبة القياسية فقط للأداة `tools/generate_yolo_ncnn_manifest.py`, وتسجل بصمات المصدر والنتيجة, ولذلك يمنع أي تراجع في أداة النماذج الفحص التمهيدي غير المتصل للإصدار.

******

### سجل الإصدارات

******

# v0.1.1

###### 2026/09/13

* `إصلاح` تستخدم تواريخ الإصدارات تنسيقا إنجليزيا موحدا
* `تحسين` التحقق أثناء البناء من محاذاة صفحات 16 KB للمكتبات الأصلية ذات 64 بت, مع فحص عقد manifest وتقارير JSON
* `تحسين` التحقق من إصدار حزم النشر وتوقيعها واكتمال متغيراتها قبل إنشاء ملفات التنزيل

# v0.1.0

###### 2026/08/13

* `تنبيه` الإصدار الأول (رمز الإصدار 2، دون سلف برمز 1)؛ يتطلب AutoJs6 برمز إصدار لا يقل عن 5275 (6.8.0+) موقّعًا بشهادة الإضافة نفسها
* `تنبيه` حاليًا في مرحلة تجهيز خاصة: يأتي النشر العام والتقديم إلى فهرس الإضافات الرسمي بعد الإصدار الرسمي للمضيف المتوافق؛ ونطاق القدرات هو CPU / arm64-v8a / كشف الكائنات
* `جديد` تحوّل الأداة غير المتصلة `tools/generate_yolo_ncnn_manifest.py` بيانات Ultralytics YOLO11 NCNN إلى `model.json`, وتتحقق من ملف metadata/labels/graph الثابت, وتصدر بصمات النواتج
* `جديد` اكتمل مزوّد كشف كائنات YOLO المعزول في عملية مستقلة: تقدم العملية المنفصلة `:provider` خدمة الاستدلال `org.autojs.plugin.YOLO` وخدمة الاكتشاف `org.autojs.plugin.INFO`، وكلتاهما محمية بإذن `org.autojs.permission.PLUGIN`
* `جديد` محرك استدلال NCNN 20260526 على المعالج مدمج مع مفكك الترميز `ultralytics-detect`، بدعم نماذج YOLO11 detect وعدد فئات مخصص يعلنه الـ manifest (من 1 إلى 256)
* `جديد` تفعيل عقد Model Manifest v1: عند فتح الجلسة تُتحقق الأطوال المعلنة و SHA-256، وأثناء التشغيل يُتحقق شكل المخرج، وتُرفض التعارضات برموز أخطاء مستقرة
* `جديد` تصل النماذج من المضيف عبر واصفات للقراءة فقط ويتشارك الفتح كله مهلة رتيبة واحدة؛ ولا يتضمن APK الإضافة أي نموذج ولا يجري أي نداء شبكي
* `جديد` حماية دورة حياة الجلسات: استدلال تسلسلي بطلب واحد لكل جلسة (طابور صفري)، واكتشاف موت طرف الاستدعاء، وإغلاق عديم الأثر عند التكرار، وتحرير مؤجل للموارد الأصلية
* `إصلاح` تُنظف جلسات النماذج المتقادمة عند بدء التشغيل، مما يمنع بقاء موارد أصلية محجوزة بعد خروج المضيف غير الطبيعي
* `تحسين` تمرير بنائي release و TEST-SIGNED RC عبر R8 وتقليص الموارد، مع تعبئة بمحاذاة ELF بمقدار 16 KiB
* `تحسين` يضم APK كاملًا رخص MPL-2.0 و Apache-2.0 الخاصة بـ Kotlin و NCNN مع أقفال المنشأ، إضافة إلى فهرس إشعارات الأطراف الثالثة
* `تحسين` بوابة جديدة دون اتصال للمصدر/البناء/التعبئة `tools/verify-r6-provider-source.ps1`: ترفض الانحراف في 22 ناتج README/CHANGELOG مولد, وتبدأ من مصادر نظيفة, وتسجل بصمات الاختبارات والنواتج, وتتحقق من APK وفق قائمة سماح من خمسة ملفات assets
* `اعتماديات` تثبيت NCNN 20260526 (برخصة BSD-3-Clause مع أقفال المنشأ والبصمات)، و Kotlin 2.2.21، وحزم AAR لبروتوكول YOLO 1.0 (مسلّمة من مراجعة مصدر AutoJs6 مجمّدة)

##### لمزيد من سجل الإصدارات، انظر

* [CHANGELOG-ar.md](https://github.com/SuperMonster003/AutoJs6-Plugin-Yolo-NCNN/blob/master/app/src/main/assets/doc/CHANGELOG-ar.md)

******

### الترخيص

******

يُنشر كود الإضافة المصدري برخصة [MPL-2.0](https://github.com/SuperMonster003/AutoJs6-Plugin-Yolo-NCNN/blob/master/LICENSE). يتضمن APK رخصة NCNN وإشعاراتها كاملة (BSD-3-Clause مع إشعارات الأطراف الثالثة من المنبع)، والنص الكامل لرخصة Apache-2.0 الخاصة ببيئة Kotlin، وفهرس إشعارات الأطراف الثالثة؛ انظر [THIRD_PARTY_NOTICES](https://github.com/SuperMonster003/AutoJs6-Plugin-Yolo-NCNN/blob/master/THIRD_PARTY_NOTICES.md). النماذج موارد خارجية: تحتفظ برخصة مصدرها وشروطه، ولا تتضمن الإضافة نماذج ولا تمنح حقوق إعادة توزيع.

******

### قراءات إضافية

******

- [مواصفة manifest النموذج (Model Manifest v1)](https://github.com/SuperMonster003/AutoJs6-Plugin-Yolo-NCNN/blob/master/docs/model-manifest-v1.md)
- [JSON Schema الخاص بـ manifest النموذج](https://github.com/SuperMonster003/AutoJs6-Plugin-Yolo-NCNN/blob/master/schemas/model-manifest-v1.schema.json)
- [سياسة تراخيص النماذج وأصول التحقق](https://github.com/SuperMonster003/AutoJs6-Plugin-Yolo-NCNN/blob/master/docs/model-license-policy.md)
- [ملاحظات إصدار 0.1.0 (بصياغة هندسية)](https://github.com/SuperMonster003/AutoJs6-Plugin-Yolo-NCNN/blob/master/docs/release-notes/0.1.0.md)
- [ملاحظات الهندسة (نص README التدقيقي السابق كاملًا)](https://github.com/SuperMonster003/AutoJs6-Plugin-Yolo-NCNN/blob/master/docs/engineering-notes.md)
- [خارطة طريق المشروع (المعالم السابقة والخطط القادمة)](https://github.com/SuperMonster003/AutoJs6-Plugin-Yolo-NCNN/blob/master/ROADMAP.md)

******

### بنية الموارد

******

```text
.readme/common.json
.readme/lang_*.json
.readme/template_readme.md
.changelog/lang_*.json
.changelog/template_changelog.md
.python/generate_markdown.py
```

يولَّد README و CHANGELOG دون اتصال بواسطة `.python/generate_markdown.py` من مصادر JSON أعلاه (المكتبة القياسية فقط، صفر شبكة). لتعديل الوثائق حرر مصادر JSON لا ملفات Markdown المولدة، ثم أعد التوليد بالأوامر أدناه؛ ويتحقق `--check` من مطابقة النواتج للمصادر:

```powershell
python .\.python\generate_markdown.py
python .\.python\generate_markdown.py --check
```

******

### روابط

******

- الصفحة الرئيسية لمشروع AutoJs6: https://github.com/SuperMonster003/AutoJs6
- Tencent NCNN: https://github.com/Tencent/ncnn
- دليل تصدير NCNN من Ultralytics: https://docs.ultralytics.com/integrations/ncnn/
- إشعارات مكونات الأطراف الثالثة: https://github.com/SuperMonster003/AutoJs6-Plugin-Yolo-NCNN/blob/master/THIRD_PARTY_NOTICES.md
- الترخيص: https://github.com/SuperMonster003/AutoJs6-Plugin-Yolo-NCNN/blob/master/LICENSE


[16 KB page alignment and build verification](https://github.com/SuperMonster003/AutoJs6-Plugin-Yolo-NCNN/blob/master/docs/16kb.md)
