# 🌿 LeafTracker

**Version 0.2.0 — fiches d'identité enrichies et navigation Leaf**

Application Android locale de gestion des personnes, relations, notes et événements.

## Nouveautés 0.2.0
- Nouvelle icône de lancement LeafTracker.
- Fiches d'identité avec photo JPG/PNG (photo redimensionnée et intégrée au coffre chiffré).
- Nom, prénom, âge, anniversaire, adresse personnelle.
- Séparation Privé / Professionnel, métier et adresse du lieu de travail.
- Personnalité, émotions, centres d'intérêt, habitudes, objectifs et description.
- Consultation, modification, duplication et suppression des fiches.
- Espace de navigation Leaf modules.
- LeafCalendar enrichi : calendriers thématiques, lieu, participants, récurrence indicative, notes/commentaires, mémos et listes.

## Sécurité et fonctionnement
- PIN 4 à 8 chiffres, dérivé PBKDF2-HMAC-SHA256 avec sel aléatoire.
- Verrouillage après échecs, verrouillage manuel et à la mise en arrière-plan.
- Coffre local AES-256-GCM, clé conservée dans Android Keystore.
- Aucune permission Internet, publicité ou télémétrie.

## Limites connues
Cette version est une étape de développement. Les vues calendrier mois/semaine/jour interactives, notifications programmées, récurrences automatiques, partage/synchronisation, éditeur graphique libre LeafGraph, biométrie et export/import chiffré restent à développer. Les participants sont des informations enregistrées dans l'événement, pas encore des invitations partagées. Les rappels et récurrences ne déclenchent pas encore de notifications automatiques.

**Important :** la photo est intégrée au coffre chiffré. Le coffre dépend d'une clé Android Keystore ; la désinstallation ou l'effacement des données peut le rendre irrécupérable. Ne stocke pas encore de données irremplaçables.

## Compiler
Prérequis : JDK 17, Android SDK Platform 35, Gradle 8.9.

```bash
gradle --no-daemon assembleDebug
```

APK : `app/build/outputs/apk/debug/app-debug.apk`. GitHub Actions compile automatiquement l'APK debug à chaque push sur `main`.

- Application ID : `com.leafvibe.leaftracker`
- Version : `0.2.0` (versionCode 2)
- Android minimum : API 26
