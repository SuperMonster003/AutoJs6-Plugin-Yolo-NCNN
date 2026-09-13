<!--suppress HtmlDeprecatedAttribute, HttpUrlsUsage -->

<div align="center">
  <p>
    <img src="https://github.com/SuperMonster003/AutoJs6-Plugin-Yolo-NCNN/blob/master/app/src/main/res/mipmap/ic_launcher.png?raw=true" alt="autojs6-plugin-yolo-ncnn-ic-launcher" border="0" width="128" />
  </p>

  <h1>AutoJs6 Plugin: YOLO NCNN</h1>

  <p>Détection d'objets YOLO pour AutoJs6, isolée en processus et entièrement hors ligne (backend NCNN 20260526)</p>

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

### Langues

******

Le README.md actuel prend en charge les langues suivantes:

- [简体中文 [zh-Hans]](https://github.com/SuperMonster003/AutoJs6-Plugin-Yolo-NCNN/blob/master/.readme/README-zh-Hans.md)
- [繁體中文 (香港) [zh-Hant-HK]](https://github.com/SuperMonster003/AutoJs6-Plugin-Yolo-NCNN/blob/master/.readme/README-zh-Hant-HK.md)
- [繁體中文 (台灣) [zh-Hant-TW]](https://github.com/SuperMonster003/AutoJs6-Plugin-Yolo-NCNN/blob/master/.readme/README-zh-Hant-TW.md)
- [English [en]](https://github.com/SuperMonster003/AutoJs6-Plugin-Yolo-NCNN/blob/master/.readme/README-en.md)
- Français [fr] # actuel
- [Español [es]](https://github.com/SuperMonster003/AutoJs6-Plugin-Yolo-NCNN/blob/master/.readme/README-es.md)
- [日本語 [ja]](https://github.com/SuperMonster003/AutoJs6-Plugin-Yolo-NCNN/blob/master/.readme/README-ja.md)
- [한국어 [ko]](https://github.com/SuperMonster003/AutoJs6-Plugin-Yolo-NCNN/blob/master/.readme/README-ko.md)
- [Русский [ru]](https://github.com/SuperMonster003/AutoJs6-Plugin-Yolo-NCNN/blob/master/.readme/README-ru.md)
- [العربية [ar]](https://github.com/SuperMonster003/AutoJs6-Plugin-Yolo-NCNN/blob/master/.readme/README-ar.md)

******

### Introduction

******

Le plugin AutoJs6 YOLO NCNN permet aux scripts AutoJs6 d'exécuter la détection d'objets YOLO entièrement sur l'appareil : passez une image et recevez un tableau de détections avec étiquettes, scores de confiance et boîtes englobantes en coordonnées pixel. L'inférence est réalisée par Tencent NCNN 20260526 dans le processus séparé `:provider`, sans accès réseau ni envoi de données ; les fichiers de modèle sont fournis par l'utilisateur et l'APK du plugin n'embarque aucun modèle.

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

L'identité ci-dessus est celle que l'hôte utilise pour découvrir et lier ce plugin. Les modèles sont transmis par l'hôte via des descripteurs de fichiers en lecture seule et restent toujours des ressources externes.

******

### Fonctionnalités

******

- Détection d'objets YOLO11 hors ligne : fournissez un objet image du module `images` d'AutoJs6, recevez étiquettes + confiances + boîtes englobantes, le tout calculé sur l'appareil.
- Isolation de processus : l'inférence s'exécute dans le processus séparé `:provider`, si bien qu'une défaillance de la couche native n'affecte jamais le processus principal d'AutoJs6 ; les services sont protégés par la permission `org.autojs.permission.PLUGIN` et par des contrôles de signature.
- Backend d'inférence CPU NCNN 20260526 : nombre de threads réglable (par défaut 4, jusqu'à 64), pour les appareils `arm64-v8a`.
- Compatibilité de modèle pilotée par manifeste : `model.json` déclare entrées, sorties et étiquettes, avec 1 à 256 classes personnalisées ; les modèles YOLO11 officiels et auto-entraînés fonctionnent de la même façon.
- Validation de sécurité des modèles : à l'ouverture de session, la longueur déclarée et le SHA-256 des trois fichiers sont vérifiés, puis la forme réelle de la sortie du graphe NCNN est contrôlée à l'exécution ; toute divergence est rejetée, jamais devinée.
- Catégories d'erreur stables : composant manquant, provider indisponible, modèle rejeté, capacité non prise en charge, etc. produisent des codes d'erreur décidables que les scripts peuvent traiter précisément.
- Délais de requête et nettoyage via `detector.close()`; le travail natif utilise une annulation coopérative et peut terminer l'appel en cours avant de libérer les ressources.
- README et CHANGELOG disponibles en dix langues : chinois simplifié, chinois traditionnel (HK/TW), anglais, français, espagnol, japonais, coréen, russe et arabe.

******

### Démarrage rapide

******

- **Installer** — Ce plugin est actuellement en phase de préparation privée (voir la section État du projet ci-dessous) : le téléchargement public et l'entrée dans l'index officiel des plugins n'arriveront qu'après la publication officielle de l'hôte compatible AutoJs6 6.8.0 (build 5275). D'ici là, vous pouvez construire vous-même un candidat TEST-SIGNED comme décrit dans la section Compilation et l'associer à un APK de test AutoJs6 utilisant le même certificat de débogage ; hôte et plugin doivent être signés avec le même certificat.
- **Activer** — Installer le plugin n'active pas YOLO tout seul : l'hôte AutoJs6 conserve des interrupteurs explicites de sélection, de confiance et d'activation (la route YOLO est désactivée par défaut) ; activez donc et faites confiance à ce provider dans l'hôte. Côté script, `yolo.load` exige aussi la chaîne de composant explicite dans `options.component` ; il n'existe aucun repli implicite.
- **Exécuter** — Préparez un répertoire de modèle contenant les trois fichiers `model.json`, `model.ncnn.param` et `model.ncnn.bin` (voir la section Préparation du modèle ci-dessous), ouvrez un détecteur avec `yolo.load(modelDir, options)`, obtenez le tableau de détections avec `detector.detect(image, options)` puis libérez-le avec `detector.close()`.
- **Dépanner** — Les exceptions levées par `yolo.load` et `detector.detect` portent des catégories d'erreur stables : `COMPONENT_REQUIRED` (composant absent), `PROVIDER_UNAVAILABLE` (l'hôte ne trouve pas le provider ou ne lui fait pas confiance), `MODEL_REJECTED` (modèle ou manifeste refusé à la validation ; les détails portent des préfixes comme `MANIFEST_*`), `UNSUPPORTED_CAPABILITY` (capacité demandée hors CPU/arm64/detect), `SESSION_CLOSED`, `DETECT_FAILED`, etc. Consultez la section Limites ci-dessous et la [spécification du manifeste de modèle](https://github.com/SuperMonster003/AutoJs6-Plugin-Yolo-NCNN/blob/master/docs/model-manifest-v1.md) pour le débogage.

******

### Exemple d'utilisation

******

Un exemple minimal prêt à l'emploi (voir aussi `sample/yolo/detect.js` dans le dépôt de l'hôte):

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

Chaque détection porte `classId`, `label`, `confidence` et `bounds` (un `RectF` Android avec les champs `left` / `top` / `right` / `bottom` et les méthodes `centerX()` / `centerY()`) ; les coordonnées sont en pixels de l'image d'entrée. Un détecteur est une session sérielle à requête unique : la file d'inférence a une longueur de 0, donc un second `detect` concurrent sur le même détecteur échoue immédiatement au lieu d'être mis en attente.

******

### Préparation du modèle

******

Un répertoire de modèle contient toujours exactement trois fichiers, dont les noms définissent les rôles:

```text
models/yolo11n/
|-- model.json
|-- model.ncnn.param
`-- model.ncnn.bin
```

Les modèles Ultralytics YOLO11 detect, officiels ou auto-entraînés, s'exportent avec `yolo export format=ncnn imgsz=640`, ce qui produit `model.ncnn.param` et `model.ncnn.bin` (voir le [guide d'export NCNN d'Ultralytics](https://docs.ultralytics.com/integrations/ncnn/)). `model.json` est un document Model Manifest v1 : il déclare l'entrée (`in0`, RGB NCHW, letterbox 640x640), la sortie (`out0`, décodeur `ultralytics-detect`, forme `[1, 4 + N, 8400]` où N est le nombre de classes) et la liste d'étiquettes ; un exemple complet est [fixtures/yolo11n/model-manifest-v1.json](https://github.com/SuperMonster003/AutoJs6-Plugin-Yolo-NCNN/blob/master/fixtures/yolo11n/model-manifest-v1.json).

Exécutez `python tools/generate_yolo_ncnn_manifest.py <répertoire-export>` pour produire `model.json` directement depuis le `metadata.yaml` d'Ultralytics. Cet outil hors ligne limité à la bibliothèque standard valide le profil fixe YOLO11/detect/640/batch/labels et la structure NCNN `in0`/`out0` avant d'écrire ; `--check` rejette toute dérive sans modification. Le [guide de conversion du modèle](https://github.com/SuperMonster003/AutoJs6-Plugin-Yolo-NCNN/blob/master/docs/model-conversion.md) détaille la commande d'export, la limite de validation et le dépannage.

Le manifeste est un contrat de compatibilité, pas un outil de ré-étiquetage : l'ouverture de session vérifie les longueurs déclarées et le SHA-256 des trois fichiers, et la forme réelle de la sortie du graphe NCNN est contrôlée à l'exécution ; toute divergence est rejetée avec `MODEL_REJECTED` (préfixes de détail comme `MANIFEST_SHAPE_INVALID`, `MODEL_GRAPH_REJECTED`). Les modèles conservent la licence et les conditions d'usage de leur source ; la conversion en NCNN ne les change pas, et le plugin n'accorde aucun droit de redistribution. Voir la [spécification du manifeste](https://github.com/SuperMonster003/AutoJs6-Plugin-Yolo-NCNN/blob/master/docs/model-manifest-v1.md) et la [politique de licence des modèles](https://github.com/SuperMonster003/AutoJs6-Plugin-Yolo-NCNN/blob/master/docs/model-license-policy.md).

******

### API de script

******

`yolo.load(modelDir, options)` ouvre une session de détection et renvoie un `YoloDetector`. `options.component` est obligatoire (chaîne de composant paquet/classe ; pour ce plugin : `io.github.supermonster003.autojs6.plugin.yolo.ncnn/.provider.YoloProviderService`). Optionnels : `device` (seul `"cpu"` est accepté actuellement), `threads` (défaut 4, max 64), `decoderId` (défaut `ultralytics-detect`) et `timeoutMillis` (délai total d'ouverture du modèle, défaut 120000 ms, max 600000 ms).

`detector.detect(image, options)` exécute la détection de façon synchrone sur une image et renvoie le tableau de détections. `image` est un objet image du module `images` d'AutoJs6 (issu de `images.read`, d'une capture d'écran, etc.). Optionnels : `confidence` (seuil de confiance, défaut 0.25), `iouThreshold` (seuil IoU du NMS, défaut 0.45), `maxDetections` (défaut 100, max 400) et `timeoutMillis` (défaut 30000 ms).

`detector.close()` libère la session et les ressources natives et peut être appelé plusieurs fois ; AutoJs6 ferme aussi les détecteurs à la fin du script, mais une libération explicite via `try...finally` est recommandée. Appeler `detect` après la fermeture renvoie `SESSION_CLOSED`.

******

### Limites

******

Pour garder un comportement prévisible, les requêtes hors du périmètre suivant sont rejetées explicitement, sans repli silencieux:

- Inférence CPU uniquement : Vulkan/GPU n'est pas pris en charge et `options.device` n'accepte que `"cpu"`.
- `arm64-v8a` uniquement : les appareils d'autres ABI ne peuvent pas charger la bibliothèque native du plugin.
- Détection d'objets (detect) uniquement : segmentation, pose, OBB, classification et suivi ne sont pas pris en charge.
- Seul le décodeur `ultralytics-detect` est enregistré : un `decoderId` inconnu est rejeté au lieu de basculer vers un autre décodeur.
- L'entrée est prétraitée en letterbox 640x640 (profil fixe du manifest v1), pixels RGBA_8888.
- Inférence sérielle à requête unique par session : la limite de file est 0, un second `detect` concurrent sur la même session échoue.
- L'ouverture de modèle n'accepte que des descripteurs en lecture seule de fichiers réguliers (ni tubes ni sockets) ; les trois fichiers doivent être lisibles.
- Installer ce plugin n'active pas YOLO par lui-même : l'activation, la confiance et la sélection appartiennent toujours à l'hôte AutoJs6.

******

### Sécurité et isolation

******

Le plugin est conçu fail-closed ; les mécanismes suivants sont toujours en vigueur:

- L'inférence s'exécute dans le processus séparé `:provider`, isolé du processus principal d'AutoJs6 ; les services sont protégés par la permission `org.autojs.permission.PLUGIN` et des contrôles de signature.
- Les modèles arrivent de l'hôte sous forme de `ParcelFileDescriptor` en lecture seule ; le plugin ne lit aucun stockage de lui-même et n'émet aucune requête réseau.
- Avant l'ouverture d'une session, longueurs déclarées, EOF et SHA-256 sont vérifiés ; toute l'ouverture partage une échéance monotone unique et une session expirée n'est jamais publiée.
- Si le runtime NCNN ne peut pas être chargé ou initialisé, le plugin échoue fermé au lieu de se dégrader.
- Les entrées malformées sont isolées des requêtes actives ; quand l'identité d'une requête peut être retrouvée, un état d'échec exact est publié au lieu de rester suspendu.
- La mort du rappel et les sessions périmées sont détectées et nettoyées ; les ressources natives sont libérées de façon différée et la fermeture est idempotente.

******

### Compatibilité

******

Nécessite AutoJs6 avec un code de version d'au moins 5275 (c'est-à-dire 6.8.0 ou ultérieur) signé avec le même certificat que le plugin ; Android 24+ (Android 7.0), targetSdk 36 ; l'appareil doit être `arm64-v8a`. Version de protocole du plugin 1.0 ; version actuelle du provider 0.1.1 (code de version 2).

******

### État du projet

******

Ce dépôt est actuellement une archive de préparation privée : l'hôte compatible AutoJs6 6.8.0 (5275) n'est pas encore publié officiellement, et ce plugin n'est ni publié publiquement ni référencé dans l'index officiel des plugins ; les badges GitHub ci-dessus peuvent ne pas s'afficher tant que le dépôt n'est pas public. Sans `sign.properties`, `assembleRelease` produit un APK non signé qui n'est qu'une preuve de source/compilation, pas un artefact publiable. La première version est 0.1.1 (code de version 2, sans prédécesseur de code 1) ; les défauts sont corrigés en avant via le code de version 3, jamais par retour arrière. La signature de production, la validation finale sur appareil et le statut de publication sont fixés par l'archive de preuves R6 externe ; voir les [notes d'ingénierie](https://github.com/SuperMonster003/AutoJs6-Plugin-Yolo-NCNN/blob/master/docs/engineering-notes.md).

******

### Compilation

******

JDK 21+ recommandé ; le SDK Android doit fournir les platforms 24 et 36, plus le NDK 29.0.14206865 et CMake 3.22.1 (nécessaires au JNI NCNN). Commandes usuelles:

```powershell
.\gradlew.bat :app:assembleDebug
.\gradlew.bat :app:assembleRc
.\gradlew.bat :app:assembleRelease
```

`assembleRc` produit un candidat installable arm64-only TEST-SIGNED : il hérite du R8 et de la réduction de ressources de release, utilise la signature de débogage standard et son nom de version se termine par `-rc-test-signed` ; il est destiné à être associé à un APK de test AutoJs6 du même certificat pour la vérification sur appareil. `assembleRelease` est la compilation de release et reste non signée en l'absence de matériel de signature.

La porte de mainteneur `tools/verify-r6-provider-source.ps1` contrôle d'abord avec `--check` les 22 artefacts README/CHANGELOG générés pour 10 langues et échoue immédiatement en cas de dérive. Elle part ensuite par défaut de sources propres : après `:app:clean`, elle exécute les tests ciblés et les deux assemblages d'APK, enregistre les XML de test et les empreintes des artefacts, et vérifie l'APK contre une liste blanche de cinq fichiers d'assets. La vérification locale et la génération de docs s'exécutent hors ligne par défaut (zéro appel réseau) pour éviter le bruit Cloudflare 502/524/529 du réseau de développement.

Avant toute compilation Gradle, la même porte exécute aussi les huit tests limités à la bibliothèque standard de `tools/generate_yolo_ncnn_manifest.py` et enregistre les empreintes des sources et le reçu ; toute régression de l'outil modèle bloque ainsi le précontrôle hors ligne.

******

### Historique des versions

******

# v0.1.1

###### 2026/09/13

* `Amélioration` Vérification à la compilation de l'alignement des pages de 16 KB des bibliothèques natives 64 bits, avec contrôle du contrat manifest et rapports JSON
* `Amélioration` Validation des versions, signatures et variantes complètes des APK avant la création des fichiers à télécharger

# v0.1.0

###### 2026/08/13

* `Note` Première version (code de version 2, sans prédécesseur de code 1) ; nécessite AutoJs6 avec un code de version d'au moins 5275 (6.8.0+) signé avec le même certificat que le plugin
* `Note` Actuellement en phase de préparation privée : la publication publique et la soumission à l'index officiel des plugins suivront la sortie officielle de l'hôte compatible ; le périmètre des capacités est CPU / arm64-v8a / détection d'objets
* `Nouveauté` L'outil hors ligne `tools/generate_yolo_ncnn_manifest.py` convertit les métadonnées NCNN Ultralytics YOLO11 en `model.json`, valide le profil fixe métadonnées/labels/graphe et émet les empreintes des artefacts
* `Nouveauté` Provider de détection d'objets YOLO isolé en processus : le processus séparé `:provider` sert l'inférence `org.autojs.plugin.YOLO` et la découverte `org.autojs.plugin.INFO`, tous deux protégés par la permission `org.autojs.permission.PLUGIN`
* `Nouveauté` Backend d'inférence CPU NCNN 20260526 intégré avec le décodeur `ultralytics-detect`, prenant en charge les modèles YOLO11 detect et les nombres de classes personnalisés déclarés par manifeste (1 à 256)
* `Nouveauté` Contrat Model Manifest v1 en place : l'ouverture de session vérifie longueurs déclarées et SHA-256, l'exécution vérifie la forme de sortie, et les divergences sont rejetées avec des codes d'erreur stables
* `Nouveauté` Les modèles arrivent de l'hôte via des descripteurs en lecture seule et toute l'ouverture partage une échéance monotone unique ; l'APK du plugin n'embarque aucun modèle et n'émet aucun appel réseau
* `Nouveauté` Protection du cycle de vie des sessions : inférence sérielle à requête unique par session (file nulle), détection de la mort du rappel, fermeture idempotente et libération différée des ressources natives
* `Correctif` Les sessions de modèle périmées sont nettoyées au démarrage, évitant des ressources natives résiduelles après une sortie anormale de l'hôte
* `Amélioration` Les builds release et RC TEST-SIGNED passent par R8 et la réduction de ressources, avec un empaquetage aligné ELF 16 KiB
* `Amélioration` L'APK embarque intégralement les licences MPL-2.0, Apache-2.0 de Kotlin et NCNN avec leurs verrous de provenance, plus un index des mentions tierces
* `Amélioration` Nouvelle porte hors ligne source/compilation/empaquetage `tools/verify-r6-provider-source.ps1` : rejet de toute dérive parmi 22 artefacts README/CHANGELOG générés, départ de sources propres, enregistrement des empreintes de tests et d'artefacts, vérification de l'APK contre une liste blanche de cinq fichiers d'assets
* `Dépendance` NCNN 20260526 épinglé (BSD-3-Clause, avec verrous de provenance et d'empreintes), Kotlin 2.2.21 et AAR du protocole YOLO 1.0 (transmis depuis une révision source AutoJs6 gelée)

##### Pour plus d'historique, voir

* [CHANGELOG-fr.md](https://github.com/SuperMonster003/AutoJs6-Plugin-Yolo-NCNN/blob/master/app/src/main/assets/doc/CHANGELOG-fr.md)

******

### Licence

******

Le code source du plugin est publié sous [MPL-2.0](https://github.com/SuperMonster003/AutoJs6-Plugin-Yolo-NCNN/blob/master/LICENSE). L'APK embarque la licence et les mentions complètes de NCNN (BSD-3-Clause plus ses mentions tierces amont), le texte intégral Apache-2.0 du runtime Kotlin et l'index des mentions tierces ; voir [THIRD_PARTY_NOTICES](https://github.com/SuperMonster003/AutoJs6-Plugin-Yolo-NCNN/blob/master/THIRD_PARTY_NOTICES.md). Les modèles sont des ressources externes : ils conservent la licence et les conditions de leur source, et le plugin n'embarque aucun modèle et n'accorde aucun droit de redistribution.

******

### Pour aller plus loin

******

- [Spécification du manifeste de modèle (Model Manifest v1)](https://github.com/SuperMonster003/AutoJs6-Plugin-Yolo-NCNN/blob/master/docs/model-manifest-v1.md)
- [JSON Schema du manifeste de modèle](https://github.com/SuperMonster003/AutoJs6-Plugin-Yolo-NCNN/blob/master/schemas/model-manifest-v1.schema.json)
- [Politique de licence des modèles et des ressources de validation](https://github.com/SuperMonster003/AutoJs6-Plugin-Yolo-NCNN/blob/master/docs/model-license-policy.md)
- [Notes de version 0.1.0 (formulation d'ingénierie)](https://github.com/SuperMonster003/AutoJs6-Plugin-Yolo-NCNN/blob/master/docs/release-notes/0.1.0.md)
- [Notes d'ingénierie (l'ancien README d'audit dans son intégralité)](https://github.com/SuperMonster003/AutoJs6-Plugin-Yolo-NCNN/blob/master/docs/engineering-notes.md)
- [Feuille de route du projet (jalons passés et plans futurs)](https://github.com/SuperMonster003/AutoJs6-Plugin-Yolo-NCNN/blob/master/ROADMAP.md)

******

### Organisation des ressources

******

```text
.readme/common.json
.readme/lang_*.json
.readme/template_readme.md
.changelog/lang_*.json
.changelog/template_changelog.md
.python/generate_markdown.py
```

README et CHANGELOG sont générés hors ligne par `.python/generate_markdown.py` à partir des sources JSON ci-dessus (bibliothèque standard uniquement, zéro réseau). Pour modifier la documentation, éditez les sources JSON plutôt que le Markdown généré, puis régénérez avec les commandes ci-dessous ; `--check` vérifie que les sorties correspondent aux sources:

```powershell
python .\.python\generate_markdown.py
python .\.python\generate_markdown.py --check
```

******

### Liens

******

- Page du projet AutoJs6: https://github.com/SuperMonster003/AutoJs6
- Tencent NCNN: https://github.com/Tencent/ncnn
- Guide d'export NCNN d'Ultralytics: https://docs.ultralytics.com/integrations/ncnn/
- Mentions des composants tiers: https://github.com/SuperMonster003/AutoJs6-Plugin-Yolo-NCNN/blob/master/THIRD_PARTY_NOTICES.md
- Licence: https://github.com/SuperMonster003/AutoJs6-Plugin-Yolo-NCNN/blob/master/LICENSE


[16 KB page alignment and build verification](https://github.com/SuperMonster003/AutoJs6-Plugin-Yolo-NCNN/blob/master/docs/16kb.md)
