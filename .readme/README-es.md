<!--suppress HtmlDeprecatedAttribute, HttpUrlsUsage -->

<div align="center">
  <p>
    <img src="https://github.com/SuperMonster003/AutoJs6-Plugin-Yolo-NCNN/blob/master/app/src/main/res/mipmap/ic_launcher.png?raw=true" alt="autojs6-plugin-yolo-ncnn-ic-launcher" border="0" width="128" />
  </p>

  <h1>AutoJs6 Plugin: YOLO NCNN</h1>

  <p>Detección de objetos YOLO para AutoJs6, aislada en proceso y totalmente sin conexión (backend NCNN 20260526)</p>

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

### Idiomas

******

El README.md actual admite los siguientes idiomas:

- [简体中文 [zh-Hans]](https://github.com/SuperMonster003/AutoJs6-Plugin-Yolo-NCNN/blob/master/.readme/README-zh-Hans.md)
- [繁體中文 (香港) [zh-Hant-HK]](https://github.com/SuperMonster003/AutoJs6-Plugin-Yolo-NCNN/blob/master/.readme/README-zh-Hant-HK.md)
- [繁體中文 (台灣) [zh-Hant-TW]](https://github.com/SuperMonster003/AutoJs6-Plugin-Yolo-NCNN/blob/master/.readme/README-zh-Hant-TW.md)
- [English [en]](https://github.com/SuperMonster003/AutoJs6-Plugin-Yolo-NCNN/blob/master/.readme/README-en.md)
- [Français [fr]](https://github.com/SuperMonster003/AutoJs6-Plugin-Yolo-NCNN/blob/master/.readme/README-fr.md)
- Español [es] # actual
- [日本語 [ja]](https://github.com/SuperMonster003/AutoJs6-Plugin-Yolo-NCNN/blob/master/.readme/README-ja.md)
- [한국어 [ko]](https://github.com/SuperMonster003/AutoJs6-Plugin-Yolo-NCNN/blob/master/.readme/README-ko.md)
- [Русский [ru]](https://github.com/SuperMonster003/AutoJs6-Plugin-Yolo-NCNN/blob/master/.readme/README-ru.md)
- [العربية [ar]](https://github.com/SuperMonster003/AutoJs6-Plugin-Yolo-NCNN/blob/master/.readme/README-ar.md)

******

### Introducción

******

El plugin AutoJs6 YOLO NCNN permite que los scripts de AutoJs6 ejecuten detección de objetos YOLO íntegramente en el dispositivo: se pasa una imagen y se recibe un arreglo de detecciones con etiquetas, confianzas y cajas delimitadoras en coordenadas de píxel. La inferencia la realiza Tencent NCNN 20260526 en el proceso separado `:provider`, sin acceso a red ni subida de datos; los archivos de modelo los aporta el usuario y el APK del plugin no incluye ningún modelo.

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

La identidad anterior es la que el host usa para descubrir y vincular este plugin. Los modelos se entregan desde el host mediante descriptores de archivo de solo lectura y siempre son recursos externos.

******

### Funciones

******

- Detección de objetos YOLO11 sin conexión: entrega un objeto imagen del módulo `images` de AutoJs6 y recibe etiquetas + confianzas + cajas delimitadoras, todo calculado en el dispositivo.
- Aislamiento de procesos: la inferencia corre en el proceso separado `:provider`, de modo que un fallo de la capa nativa nunca derriba el proceso principal de AutoJs6; los servicios están protegidos por el permiso `org.autojs.permission.PLUGIN` y por comprobaciones de firma.
- Backend de inferencia CPU NCNN 20260526: número de hilos ajustable (por defecto 4, hasta 64), para dispositivos `arm64-v8a`.
- Compatibilidad de modelos dirigida por manifiesto: `model.json` declara entradas, salidas y etiquetas, con 1 a 256 clases personalizadas; los modelos YOLO11 oficiales y los auto-entrenados funcionan por igual.
- Validación de seguridad del modelo: al abrir la sesión se verifican la longitud declarada y el SHA-256 de los tres archivos, y en ejecución se verifica la forma real de la salida del grafo NCNN; las discrepancias se rechazan, nunca se adivinan.
- Categorías de error estables: componente ausente, provider no disponible, modelo rechazado, capacidad no soportada y casos similares devuelven códigos de error decidibles que los scripts pueden manejar con precisión.
- Tiempos de espera y ciclo de vida controlados: tanto la apertura del modelo como cada detección tienen techos de tiempo; `detector.close()` y detener el script liberan de inmediato la sesión y los recursos nativos.
- README y CHANGELOG disponibles en diez idiomas: chino simplificado, chino tradicional (HK/TW), inglés, francés, español, japonés, coreano, ruso y árabe.

******

### Inicio rápido

******

- **Instalar** — Este plugin está actualmente en fase de preparación privada (ver la sección Estado del proyecto más abajo): la descarga pública y la entrada en el índice oficial de plugins llegarán solo después de que se publique formalmente el host compatible AutoJs6 6.8.0 (build 5275). Hasta entonces puede compilar usted mismo un candidato TEST-SIGNED como describe la sección Compilación y emparejarlo con un APK de prueba de AutoJs6 que use el mismo certificado de depuración; host y plugin deben firmarse con el mismo certificado.
- **Activar** — Instalar el plugin no activa YOLO por sí solo: el host AutoJs6 conserva interruptores explícitos de selección, confianza y activación (la ruta YOLO está desactivada por defecto), así que active y confíe en este provider dentro del host. En el lado del script, `yolo.load` también exige la cadena de componente explícita en `options.component`; no existe ningún respaldo implícito.
- **Ejecutar** — Prepare un directorio de modelo con los tres archivos `model.json`, `model.ncnn.param` y `model.ncnn.bin` (ver la sección Preparación del modelo más abajo), abra un detector con `yolo.load(modelDir, options)`, obtenga el arreglo de detecciones con `detector.detect(image, options)` y libérelo con `detector.close()` al terminar.
- **Depurar** — Las excepciones lanzadas por `yolo.load` y `detector.detect` llevan categorías de error estables: `COMPONENT_REQUIRED` (sin componente), `PROVIDER_UNAVAILABLE` (el host no encuentra el provider o no confía en él), `MODEL_REJECTED` (el modelo o el manifiesto no pasó la validación; los detalles llevan prefijos como `MANIFEST_*`), `UNSUPPORTED_CAPABILITY` (se pidió una capacidad fuera de CPU/arm64/detect), `SESSION_CLOSED`, `DETECT_FAILED`, etc. Consulte la sección Límites más abajo y la [especificación del manifiesto de modelo](https://github.com/SuperMonster003/AutoJs6-Plugin-Yolo-NCNN/blob/master/docs/model-manifest-v1.md) al depurar.

******

### Ejemplo de uso

******

Un ejemplo mínimo listo para ejecutar (ver también `sample/yolo/detect.js` en el repositorio del host):

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

Cada detección incluye `classId`, `label`, `confidence` y `bounds` (un `RectF` de Android con los campos `left` / `top` / `right` / `bottom` y los métodos `centerX()` / `centerY()`); las coordenadas son píxeles de la imagen de entrada. Un detector es una sesión serial de una sola petición: la cola de inferencia tiene longitud 0, por lo que un segundo `detect` concurrente sobre el mismo detector falla de inmediato en lugar de encolarse.

******

### Preparación del modelo

******

Un directorio de modelo contiene siempre exactamente tres archivos, y los nombres definen los roles:

```text
models/yolo11n/
|-- model.json
|-- model.ncnn.param
`-- model.ncnn.bin
```

Los modelos Ultralytics YOLO11 detect, oficiales o auto-entrenados, se exportan con `yolo export format=ncnn imgsz=640`, lo que produce `model.ncnn.param` y `model.ncnn.bin` (ver la [guía de exportación NCNN de Ultralytics](https://docs.ultralytics.com/integrations/ncnn/)). `model.json` es un documento Model Manifest v1: declara la entrada (`in0`, RGB NCHW, letterbox 640x640), la salida (`out0`, decodificador `ultralytics-detect`, forma `[1, 4 + N, 8400]` donde N es el número de clases) y la lista de etiquetas; un ejemplo completo es [fixtures/yolo11n/model-manifest-v1.json](https://github.com/SuperMonster003/AutoJs6-Plugin-Yolo-NCNN/blob/master/fixtures/yolo11n/model-manifest-v1.json).

Ejecuta `python tools/generate_yolo_ncnn_manifest.py <directorio-exportado>` para generar `model.json` directamente desde `metadata.yaml` de Ultralytics. La herramienta sin conexión y basada solo en la biblioteca estándar valida el perfil fijo YOLO11/detect/640/batch/etiquetas y la estructura NCNN `in0`/`out0` antes de escribir; `--check` rechaza derivas sin modificar archivos. La [guía de conversión del modelo](https://github.com/SuperMonster003/AutoJs6-Plugin-Yolo-NCNN/blob/master/docs/model-conversion.md) detalla la exportación, el límite de validación y la solución de problemas.

El manifiesto es un contrato de compatibilidad, no una herramienta de re-etiquetado: al abrir la sesión se verifican las longitudes declaradas y el SHA-256 de los tres archivos, y en ejecución se verifica la forma real de la salida del grafo NCNN; las discrepancias se rechazan con `MODEL_REJECTED` (prefijos de detalle como `MANIFEST_SHAPE_INVALID`, `MODEL_GRAPH_REJECTED`). Los modelos conservan la licencia y condiciones de uso de su origen; convertirlos a NCNN no las cambia, y el plugin no otorga derechos de redistribución. Ver la [especificación del manifiesto](https://github.com/SuperMonster003/AutoJs6-Plugin-Yolo-NCNN/blob/master/docs/model-manifest-v1.md) y la [política de licencias de modelos](https://github.com/SuperMonster003/AutoJs6-Plugin-Yolo-NCNN/blob/master/docs/model-license-policy.md).

******

### API de script

******

`yolo.load(modelDir, options)` abre una sesión de detección y devuelve un `YoloDetector`. `options.component` es obligatorio (cadena de componente paquete/clase; para este plugin: `io.github.supermonster003.autojs6.plugin.yolo.ncnn/.provider.YoloProviderService`). Opcionales: `device` (actualmente solo se acepta `"cpu"`), `threads` (por defecto 4, hasta 64), `decoderId` (por defecto `ultralytics-detect`) y `timeoutMillis` (tiempo total de apertura del modelo, por defecto 120000 ms, hasta 600000 ms).

`detector.detect(image, options)` ejecuta la detección de forma síncrona sobre una imagen y devuelve el arreglo de detecciones. `image` es un objeto imagen del módulo `images` de AutoJs6 (de `images.read`, una captura de pantalla, etc.). Opcionales: `confidence` (umbral de confianza, por defecto 0.25), `iouThreshold` (umbral IoU del NMS, por defecto 0.45), `maxDetections` (por defecto 100, hasta 400) y `timeoutMillis` (por defecto 30000 ms).

`detector.close()` libera la sesión y los recursos nativos y puede llamarse varias veces; AutoJs6 también cierra los detectores al terminar el script, pero se recomienda liberar explícitamente con `try...finally`. Llamar a `detect` tras el cierre devuelve `SESSION_CLOSED`.

******

### Límites

******

Para mantener un comportamiento predecible, las peticiones fuera del siguiente alcance se rechazan explícitamente, sin degradación silenciosa:

- Solo inferencia en CPU: Vulkan/GPU no está soportado y `options.device` solo acepta `"cpu"`.
- Solo `arm64-v8a`: los dispositivos con otras ABI no pueden cargar la biblioteca nativa del plugin.
- Solo detección de objetos (detect): segmentación, pose, OBB, clasificación y seguimiento no están soportados.
- Solo está registrado el decodificador `ultralytics-detect`: un `decoderId` desconocido se rechaza en lugar de recurrir a otro decodificador.
- La entrada se preprocesa como letterbox 640x640 (perfil fijo del manifest v1), con píxeles RGBA_8888.
- Inferencia serial de una petición por sesión: el límite de cola es 0, así que un segundo `detect` concurrente en la misma sesión falla.
- La apertura de modelo solo acepta descriptores de solo lectura de archivos regulares (ni tuberías ni sockets); los tres archivos deben ser legibles.
- Instalar este plugin no activa YOLO por sí solo: la activación, la confianza y la selección pertenecen siempre al host AutoJs6.

******

### Seguridad y aislamiento

******

El plugin está diseñado fail-closed; los siguientes mecanismos están siempre en vigor:

- La inferencia corre en el proceso separado `:provider`, aislado del proceso principal de AutoJs6; los servicios están protegidos por el permiso `org.autojs.permission.PLUGIN` y comprobaciones de firma.
- Los modelos llegan del host como instancias `ParcelFileDescriptor` de solo lectura; el plugin no lee almacenamiento por su cuenta ni hace peticiones de red.
- Antes de abrir una sesión se verifican longitudes declaradas, EOF y SHA-256; toda la apertura comparte una única fecha límite monótona y una sesión expirada nunca se publica.
- Si el runtime NCNN no puede cargarse o inicializarse, el plugin falla cerrado en lugar de degradarse.
- Las entradas malformadas se aíslan de las peticiones activas; cuando puede recuperarse la identidad de una petición, se publica un estado de fallo exacto en lugar de quedar colgado.
- La muerte del callback y las sesiones obsoletas se detectan y limpian; los recursos nativos se liberan de forma diferida y el cierre es idempotente.

******

### Compatibilidad

******

Requiere AutoJs6 con código de versión no inferior a 5275 (es decir, 6.8.0 o posterior) firmado con el mismo certificado que el plugin; Android 24+ (Android 7.0), targetSdk 36; el dispositivo debe ser `arm64-v8a`. Versión de protocolo del plugin 1.0; versión actual del provider 0.1.1 (código de versión 2).

******

### Estado del proyecto

******

Este repositorio es actualmente un archivo privado de preparación: el host compatible AutoJs6 6.8.0 (5275) aún no se ha publicado formalmente, y este plugin no está publicado ni listado en el índice oficial de plugins; las insignias de GitHub de arriba pueden no mostrarse hasta que el repositorio sea público. Sin `sign.properties`, `assembleRelease` produce un APK sin firmar que es solo evidencia de fuente/compilación, no un artefacto publicable. La primera versión es 0.1.1 (código de versión 2, sin predecesor de código 1); los defectos se corrigen hacia adelante con el código de versión 3, nunca con reversiones. La firma de producción, la verificación final en dispositivo y el estado de publicación quedan fijados por el archivo externo de evidencias R6; ver las [notas de ingeniería](https://github.com/SuperMonster003/AutoJs6-Plugin-Yolo-NCNN/blob/master/docs/engineering-notes.md).

******

### Compilación

******

Se recomienda JDK 21+; el SDK de Android debe proporcionar las platforms 24 y 36, además del NDK 29.0.14206865 y CMake 3.22.1 (necesarios para el JNI de NCNN). Comandos habituales:

```powershell
.\gradlew.bat :app:assembleDebug
.\gradlew.bat :app:assembleRc
.\gradlew.bat :app:assembleRelease
```

`assembleRc` produce un candidato instalable arm64-only TEST-SIGNED: hereda el R8 y la reducción de recursos de release, usa la firma de depuración estándar y su nombre de versión termina en `-rc-test-signed`; está pensado para emparejarse con un APK de prueba de AutoJs6 del mismo certificado en la verificación sobre dispositivo. `assembleRelease` es la compilación de release y permanece sin firmar cuando falta el material de firma.

La puerta de mantenimiento `tools/verify-r6-provider-source.ps1` primero ejecuta `--check` sobre los 22 artefactos README/CHANGELOG generados para 10 idiomas y falla de inmediato ante cualquier deriva. Después parte por defecto de fuentes limpias: tras `:app:clean` ejecuta las pruebas enfocadas y los dos ensamblados de APK, registra los XML de pruebas y los hashes de los artefactos, y verifica el APK contra una lista blanca de cinco archivos de assets. La verificación local y la generación de documentación se ejecutan sin conexión por defecto (cero llamadas de red) para evitar el ruido Cloudflare 502/524/529 de la red de desarrollo.

Antes de cualquier compilación Gradle, la misma puerta también ejecuta las ocho pruebas basadas solo en la biblioteca estándar de `tools/generate_yolo_ncnn_manifest.py` y registra los hashes de sus fuentes y el recibo, de modo que una regresión de la herramienta de modelos bloquee la prevalidación sin conexión.

******

### Historial de versiones

******

# v0.1.1

###### 2026/09/11

* `Mejora` Verificación de compilación de la alineación de páginas de 16 KB en bibliotecas nativas de 64 bits, con controles del contrato manifest e informes JSON

# v0.1.0

###### 2026/08/13

* `Aviso` Primera versión (código de versión 2, sin predecesor de código 1); requiere AutoJs6 con código de versión no inferior a 5275 (6.8.0+) firmado con el mismo certificado que el plugin
* `Aviso` Actualmente en fase de preparación privada: la publicación pública y el envío al índice oficial de plugins seguirán al lanzamiento formal del host compatible; el alcance de capacidades es CPU / arm64-v8a / detección de objetos
* `Novedad` La herramienta sin conexión `tools/generate_yolo_ncnn_manifest.py` convierte metadatos NCNN de Ultralytics YOLO11 en `model.json`, valida el perfil fijo de metadatos/etiquetas/grafo y emite hashes de artefactos
* `Novedad` Provider de detección de objetos YOLO aislado en proceso: el proceso separado `:provider` sirve la inferencia `org.autojs.plugin.YOLO` y el descubrimiento `org.autojs.plugin.INFO`, ambos protegidos por el permiso `org.autojs.permission.PLUGIN`
* `Novedad` Backend de inferencia CPU NCNN 20260526 integrado con el decodificador `ultralytics-detect`, compatible con modelos YOLO11 detect y números de clases personalizados declarados por manifiesto (1 a 256)
* `Novedad` Contrato Model Manifest v1 en vigor: la apertura de sesión verifica longitudes declaradas y SHA-256, la ejecución verifica la forma de salida, y las discrepancias se rechazan con códigos de error estables
* `Novedad` Los modelos llegan del host mediante descriptores de solo lectura y toda la apertura comparte una única fecha límite monótona; el APK del plugin no incluye modelos ni realiza llamadas de red
* `Novedad` Protección del ciclo de vida de sesiones: inferencia serial de una petición por sesión (cola cero), detección de muerte del callback, cierre idempotente y liberación diferida de recursos nativos
* `Corrección` Las sesiones de modelo obsoletas se limpian al arrancar, evitando recursos nativos residuales tras una salida anómala del host
* `Mejora` Las compilaciones release y RC TEST-SIGNED pasan por R8 y reducción de recursos, con empaquetado alineado ELF de 16 KiB
* `Mejora` El APK incluye íntegramente las licencias MPL-2.0, Apache-2.0 de Kotlin y NCNN con sus bloqueos de procedencia, más un índice de avisos de terceros
* `Mejora` Nueva puerta sin conexión de fuente/compilación/empaquetado `tools/verify-r6-provider-source.ps1`: rechaza derivas en 22 artefactos README/CHANGELOG generados, parte de fuentes limpias, registra hashes de pruebas y artefactos, y verifica el APK contra una lista blanca de cinco archivos de assets
* `Dependencia` NCNN 20260526 fijado (BSD-3-Clause, con bloqueos de procedencia y hash), Kotlin 2.2.21 y AAR del protocolo YOLO 1.0 (transferidos desde una revisión congelada del código de AutoJs6)

##### Para más historial, ver

* [CHANGELOG-es.md](https://github.com/SuperMonster003/AutoJs6-Plugin-Yolo-NCNN/blob/master/.changelog/CHANGELOG-es.md)

******

### Licencia

******

El código fuente del plugin se publica bajo [MPL-2.0](https://github.com/SuperMonster003/AutoJs6-Plugin-Yolo-NCNN/blob/master/LICENSE). El APK incluye la licencia y avisos completos de NCNN (BSD-3-Clause más sus avisos de terceros), el texto íntegro Apache-2.0 del runtime de Kotlin y el índice de avisos de terceros; ver [THIRD_PARTY_NOTICES](https://github.com/SuperMonster003/AutoJs6-Plugin-Yolo-NCNN/blob/master/THIRD_PARTY_NOTICES.md). Los modelos son recursos externos: conservan la licencia y condiciones de su origen, y el plugin no incluye modelos ni otorga derechos de redistribución.

******

### Lecturas adicionales

******

- [Especificación del manifiesto de modelo (Model Manifest v1)](https://github.com/SuperMonster003/AutoJs6-Plugin-Yolo-NCNN/blob/master/docs/model-manifest-v1.md)
- [JSON Schema del manifiesto de modelo](https://github.com/SuperMonster003/AutoJs6-Plugin-Yolo-NCNN/blob/master/schemas/model-manifest-v1.schema.json)
- [Política de licencias de modelos y recursos de validación](https://github.com/SuperMonster003/AutoJs6-Plugin-Yolo-NCNN/blob/master/docs/model-license-policy.md)
- [Notas de la versión 0.1.0 (redacción de ingeniería)](https://github.com/SuperMonster003/AutoJs6-Plugin-Yolo-NCNN/blob/master/docs/release-notes/0.1.0.md)
- [Notas de ingeniería (el antiguo README de auditoría íntegro)](https://github.com/SuperMonster003/AutoJs6-Plugin-Yolo-NCNN/blob/master/docs/engineering-notes.md)
- [Hoja de ruta del proyecto (hitos pasados y planes futuros)](https://github.com/SuperMonster003/AutoJs6-Plugin-Yolo-NCNN/blob/master/ROADMAP.md)

******

### Estructura de recursos

******

```text
.readme/common.json
.readme/lang_*.json
.readme/template_readme.md
.changelog/lang_*.json
.changelog/template_changelog.md
.python/generate_markdown.py
```

README y CHANGELOG se generan sin conexión con `.python/generate_markdown.py` a partir de las fuentes JSON anteriores (solo biblioteca estándar, cero red). Para cambiar la documentación, edite las fuentes JSON en lugar del Markdown generado y regenere con los comandos siguientes; `--check` verifica que las salidas coincidan con las fuentes:

```powershell
python .\.python\generate_markdown.py
python .\.python\generate_markdown.py --check
```

******

### Enlaces

******

- Página del proyecto AutoJs6: https://github.com/SuperMonster003/AutoJs6
- Tencent NCNN: https://github.com/Tencent/ncnn
- Guía de exportación NCNN de Ultralytics: https://docs.ultralytics.com/integrations/ncnn/
- Avisos de componentes de terceros: https://github.com/SuperMonster003/AutoJs6-Plugin-Yolo-NCNN/blob/master/THIRD_PARTY_NOTICES.md
- Licencia: https://github.com/SuperMonster003/AutoJs6-Plugin-Yolo-NCNN/blob/master/LICENSE


[16 KB page alignment and build verification](https://github.com/SuperMonster003/AutoJs6-Plugin-Yolo-NCNN/blob/master/docs/16kb.md)
