******

### Historial de versiones

******

# v0.1.0

###### 2026/08/13

* `Aviso` Primera versión (código de versión 2, sin predecesor de código 1); requiere AutoJs6 con código de versión no inferior a 5275 (6.8.0+) firmado con el mismo certificado que el plugin
* `Aviso` Actualmente en fase de preparación privada: la publicación pública y el envío al índice oficial de plugins seguirán al lanzamiento formal del host compatible; el alcance de capacidades es CPU / arm64-v8a / detección de objetos
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
