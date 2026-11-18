# 🌿 LeafTracker

**Version 0.3.0 — LeafCalendar et widget Android**

Application Android hors ligne pour gérer les identités, relations, notes et calendriers.

## Nouveautés 0.3.0
- Navigation simplifiée : retrait des boutons PIN et sous-menus redondants de l'écran principal.
- LeafCalendar regroupe les événements et propose des filtres Mois, Semaine, Jour et Liste.
- Événements avec calendrier, lieu, participants, récurrence indicative et notes.
- Widget Android d'accueil LeafCalendar.
- Chaque événement peut être explicitement autorisé à apparaître sur le widget. Seuls ces événements sélectionnés sont publiés dans le cache du widget.
- Le widget est volontairement vide tant qu'aucun événement n'a été autorisé.

## TimeTree et confidentialité
LeafCalendar reprend localement des usages courants des calendriers partagés (calendriers distincts, événements détaillés, participants, mémos et vues temporelles). Il ne contient pas l'application propriétaire TimeTree, ne se connecte pas à ses comptes et ne synchronise pas ses données. L'application reste hors ligne.

Le widget affiche uniquement les événements pour lesquels la case « Afficher sur le widget » a été cochée. Ces informations sont visibles sur l'écran d'accueil sans ouvrir LeafTracker : ne sélectionne pas d'informations confidentielles.

## Sécurité
- PIN 4 à 8 chiffres et verrouillage après échecs.
- Coffre local AES-256-GCM, clé conservée dans Android Keystore.
- Aucune permission Internet, publicité ou télémétrie.
- Le cache du widget ne contient que les événements explicitement sélectionnés pour l'affichage externe.

## Limites connues
Les vues mois/semaine/jour sont des filtres d'événements, pas encore une grille calendrier complète. Les rappels et récurrences sont enregistrés mais ne déclenchent pas encore de notifications automatiques. Pas de synchronisation multi-appareils, invitations partagées, biométrie ou export/import chiffré. Le widget est un aperçu des événements choisis, pas un éditeur.

**Important :** le coffre dépend d'une clé Android Keystore ; désinstaller l'application ou effacer ses données peut rendre les données irrécupérables. Ne stocke pas encore de données irremplaçables.

## Compiler
Prérequis : JDK 17, Android SDK Platform 35, Gradle 8.9.

```bash
gradle --no-daemon assembleDebug
```

APK : `app/build/outputs/apk/debug/app-debug.apk`.

- Application ID : `com.leafvibe.leaftracker`
- Version : `0.3.0` (versionCode 3)
- Android minimum : API 26
