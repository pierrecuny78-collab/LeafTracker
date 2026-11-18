# 🌿 LeafTracker

**Version 0.1.0 — première base Android hors ligne**

Application locale de gestion des identités, relations, espaces privés/professionnels, notes et événements de calendrier de l'écosystème LeafVibe.

## Fonctionnalités 0.1.0
- PIN obligatoire de 4 à 8 chiffres, dérivé PBKDF2-HMAC-SHA256 avec sel aléatoire.
- Verrouillage après cinq tentatives incorrectes avec temporisation progressive ; verrouillage à la mise en arrière-plan et manuel.
- Coffre local AES-256-GCM, clé conservée dans Android Keystore.
- Fiches identité : créer, consulter, modifier, supprimer ; description et objectif.
- Relations entre personnes, catégories et espaces Privé / Professionnel.
- Vue LeafGraph initiale des identités et connexions.
- LeafCalendar local : événements CRUD et calendriers thématiques.
- LeafNotes : notes et recherche simple.
- Aucune permission Internet, IA, publicité ou télémétrie.

## Limites connues
Cette version n'est pas encore LeafTracker Complete. L'éditeur graphique libre, la biométrie, les vues calendaires avancées, récurrences, rappels, export/import chiffré et autorisations fines seront développés ensuite. Les fonctions de TimeTree sont une inspiration fonctionnelle ; aucune connexion ou synchronisation TimeTree n'existe.

**Important :** le coffre dépend d'une clé Android Keystore. La désinstallation ou l'effacement des données peut rendre le coffre irrécupérable. Ne stocke pas encore de données irremplaçables avant l'export/import chiffré.

## Compiler
Prérequis : JDK 17, Android SDK Platform 35, Gradle 8.9.

```bash
gradle --no-daemon assembleDebug
```

APK : `app/build/outputs/apk/debug/app-debug.apk`.

GitHub Actions compile automatiquement une APK debug à chaque push sur `main`. Voir l'onglet Actions du dépôt pour récupérer l'artefact.

- Application ID : `com.leafvibe.leaftracker`
- Version : `0.1.0` (versionCode 1)
- Minimum Android : API 26
