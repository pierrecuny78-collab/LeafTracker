# 🌿 LeafTracker

**Version 0.4.0 — Modules spécialisés et LeafGraph**

Application Android hors ligne, protégée par PIN et coffre local chiffré.

## Nouveautés 0.4.0
- LeafGoals : objectifs, motivation, échéance, progression et domaine.
- LeafMoments : journal de vie, souvenirs, lieux, récits et personnes présentes.
- LeafPlaces : carnet des lieux, adresses, catégories et contexte.
- LeafTasks : tâches, descriptions, échéances, priorités et statuts.
- LeafTimeline : chronologie regroupant événements, souvenirs et tâches datés.
- LeafDashboard : compteurs et raccourcis vers les données de chaque module.
- LeafArchive : corbeille avec restauration et suppression définitive.
- LeafGraph : tableau blanc tactile avec cartes d'idées déplaçables et connexions nommées.
- Les modules utilisent chacun leur propre collection de données et leurs propres écrans.
- Les suppressions depuis les listes passent désormais par LeafArchive.

## Inspirations fonctionnelles
- Obsidian Canvas : espace de travail visuel, cartes et liens nommés.
- Milanote : organisation visuelle de notes et idées.
- Notion : vues et données organisées par propriétés.
- Gramps / FamilySearch : relations, chronologie et mémoire familiale.
- Joplin : notes locales, carnets et recherche.

## Confidentialité
- PIN 4 à 8 chiffres.
- Coffre local AES-GCM avec clé Android Keystore.
- Fonctionnement hors ligne, sans compte ni IA.
- Espaces Privé et Professionnel.
- Le widget calendrier affiche uniquement les événements explicitement sélectionnés.

## Limites connues
LeafGraph 0.4.0 propose des cartes déplaçables et des connexions nommées, mais pas encore la suite graphique complète d'un outil de design : images sur la toile, formes libres, sélection multiple, zoom/panoramique avancé, modèles de diagrammes, export PNG et annuler/rétablir restent à développer. Les vues du calendrier, les notifications récurrentes et l'intégration directe TimeTree nécessitent également des versions ultérieures.

**Important :** les données du coffre peuvent devenir irrécupérables si l'application est désinstallée ou si ses données sont effacées. Effectue des essais avec des données non critiques et conserve une sauvegarde avant migration.

## Compilation
JDK 17, Android SDK Platform 35, Gradle 8.9.

```bash
gradle --no-daemon assembleDebug
```

APK : `app/build/outputs/apk/debug/app-debug.apk`

- Application ID : `com.leafvibe.leaftracker`
- Version : `0.4.0` (versionCode 4)
- Android minimum : API 26
