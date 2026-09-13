******

### Historique des versions

******

# v0.1.2

###### 2026/09/13

* `Correctif` Les dates de version utilisent un format anglais uniforme
* `Amélioration` Validation des versions, signatures et variantes complètes des APK avant la création des fichiers à télécharger

# v0.1.1

###### 2026/09/13

* `Amélioration` Vérification à la compilation de l'alignement des pages de 16 KB des bibliothèques natives 64 bits, avec contrôle du contrat manifest et rapports JSON

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
